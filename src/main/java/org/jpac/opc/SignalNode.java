/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : SignalNode.java
 * VERSION   : -
 * DATE      : -
 * PURPOSE   : 
 * AUTHOR    : Bernd Schuster, MSK Gesellschaft fuer Automatisierung mbH, Schenefeld
 * REMARKS   : -
 * CHANGES   : CH#n <Kuerzel> <datum> <Beschreibung>
 *
 * This file is part of the jPac process automation controller.
 * jPac is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jPac is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the jPac If not, see <http://www.gnu.org/licenses/>.
 * 
 * This module was implemented on basis of the pi-server example published
 * by Kevin Herron under the following license:
 * 
 * Copyright 2014
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.* 
 * 
 */


package org.jpac.opc;

import com.google.common.collect.ImmutableSet;
import org.eclipse.milo.opcua.sdk.core.AccessLevel;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNodeContext;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.ubyte;
import java.lang.reflect.Field;
import java.util.Observable;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.jpac.JPac;
import org.jpac.Signal;
import org.jpac.SignalAlreadyConnectedException;
import org.jpac.SignalObserver;
import org.jpac.Value;
/**
 *
 * @author berndschuster
 */
abstract public class SignalNode extends UaVariableNode implements SignalObserver{
    static  Logger Log = LoggerFactory.getLogger("jpac.opc");
    
    protected       boolean   connectedAsTarget;
    protected       Signal    signal;
    protected       DataValue dataValue;
    protected final Boolean   lock;
    protected       Value     signalValue;
    protected       Value     lastSignalValue;
    
      public SignalNode(UaNodeContext context, int namespaceIndex, TreeItem signalNode) {
        super(context, new NodeId(namespaceIndex, signalNode.getSignal().getQualifiedIdentifier()), new QualifiedName(namespaceIndex, signalNode.getSignal().getIdentifier()), LocalizedText.english(signalNode.getSignal().getIdentifier()));
        this.lock            = false;
        this.signal          = signalNode.getSignal();
        this.signalValue     = getSignalValue();

        try{this.lastSignalValue = signalValue.clone();}catch(CloneNotSupportedException exc){/*cannot happen*/}
        
        setDataType(getSignalDataType());
        dataValue = new DataValue(new Variant(getSignalValue().getValue()), StatusCode.BAD);
        ImmutableSet<AccessLevel> accessLevels = AccessLevel.NONE;
        Opc.AccessLevel opcAccessLevel = retrieveOpcAccessLevel(signal);
        switch(opcAccessLevel){
            case NONE:
                accessLevels = AccessLevel.NONE;
                break;
            case READ_ONLY:
                accessLevels = AccessLevel.READ_ONLY;
                break;
            case READ_WRITE:
                accessLevels = AccessLevel.READ_WRITE;
                break;
        }
        UByte accessLevel = ubyte(AccessLevel.getMask(accessLevels));
        setAccessLevel(accessLevel);
        setUserAccessLevel(accessLevel);  
        if (opcAccessLevel != Opc.AccessLevel.NONE){
            //if any kind of access is permitted, connect this node to the assigned signal
            try{this.signal.connect(this);}catch(SignalAlreadyConnectedException exc){/*cannot happen*/};
        }
        if (opcAccessLevel == opcAccessLevel.READ_WRITE){
            signal.setConnectedAsTarget(retrieveOpcConnectAsTarget(signal));
        }
    }  
    
    protected Opc.AccessLevel retrieveOpcAccessLevel(Signal signal){
        Field           field       = signal.getContainingModule().getField(signal);
        Opc.AccessLevel accessLevel = JPac.getInstance().getOpcUaDefaultAccesslevel();
        if (field != null){
            //signal is instantiated by the module directly
            if (field.getAnnotation(Opc.class) != null){
                accessLevel = ((Opc)field.getAnnotation(Opc.class)).accessLevel();
            } else if (field.getAnnotation(OpcNone.class) != null){
                accessLevel = Opc.AccessLevel.NONE;
            } else if (field.getAnnotation(OpcReadOnly.class) != null){
                accessLevel = Opc.AccessLevel.READ_ONLY;
            } else if (field.getAnnotation(OpcReadWrite.class) != null){
                accessLevel = Opc.AccessLevel.READ_WRITE;
            }
        }
        else{
            //signal might be embedded inside another class, such like Handshake
            accessLevel = retrieveOpcAccessLevelForEmbeddedSignals(signal);
        }
        return accessLevel;
    }
    
    protected boolean retrieveOpcConnectAsTarget(Signal signal){
        Field   field           = signal.getContainingModule().getField(signal);
        boolean connectAsTarget = false;
        if (field != null){
            //signal is instantiated by the module directly
            if (field.getAnnotation(Opc.class) != null){
                connectAsTarget = ((Opc)field.getAnnotation(Opc.class)).connectAsTarget();
            }
        }
        else{
            //signal might be embedded inside another class, such like Handshake
            connectAsTarget = retrieveOpcConnectAsTargetForEmbeddedSignals(signal);
        }
        return connectAsTarget;
    }

    protected Opc.AccessLevel retrieveOpcAccessLevelForEmbeddedSignals(Signal signal){
      //TODO to be implemented (Handshake etc.)
      Opc.AccessLevel accessLevel = JPac.getInstance().getOpcUaDefaultAccesslevel();
      return accessLevel;
    };

    protected boolean retrieveOpcConnectAsTargetForEmbeddedSignals(Signal signal){
      //TODO to be implemented (Handshake etc.)
      return false;
    };

    
    @Override
    public void update(Observable o, Object o1) {
        Signal sourceSignal = (Signal)o;
        boolean isValid = sourceSignal.isValid();
        Log.debug("SignalNode.update() : {}", sourceSignal);
        synchronized(lock){
            if(isValid){
                lastSignalValue.copy(getSignalValue());
                getSignalValue().copy(sourceSignal.getValue());
            }
            else{
                invalidateSignalValue();//TODO invalidate() problem: invalid signals will cause Status "BAD" on client side
            }
        }
    }   

    @Override
    public void setConnectedAsTarget(boolean connected) {
       this.connectedAsTarget = connected;
       if (!connected){
           invalidateSignalValue();
       }
    }

    @Override
    public boolean isConnectedAsTarget() {
        return this.connectedAsTarget;
    }
    
    @Override
    public synchronized void setValue(DataValue value){
        setSignalValue(value);
    }
    
    @Override
    public DataValue getValue() {
    	Log.debug("SignalNode.getValue {}: {}", signal, getSignalValue().getValue());//TODO
        DataValue theDataValue;
        synchronized(lock){
            dataValue = new DataValue(new Variant(getSignalValue().getValue()), signalValue.isValid() ? StatusCode.GOOD : StatusCode.BAD);
            saveSignalState();   
            theDataValue = dataValue;
        }
        return theDataValue;
    }
            
    protected void saveSignalState(){
        try{
            if (lastSignalValue == null){
                lastSignalValue = signalValue.clone();
            }
            else{
                this.lastSignalValue.copy(signalValue);
            }
            }
        catch(CloneNotSupportedException exc){
            Log.error("Error: ", exc);
        }
    }
            
    abstract protected Value     getSignalValue();
    abstract protected void      setSignalValue(DataValue value);
    abstract protected NodeId    getSignalDataType();
    abstract protected void      invalidateSignalValue();
}
