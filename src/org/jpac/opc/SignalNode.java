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

import com.digitalpetri.opcua.sdk.core.AccessLevel;
import com.digitalpetri.opcua.sdk.server.api.UaNamespace;
import com.digitalpetri.opcua.sdk.server.model.UaVariableNode;
import com.digitalpetri.opcua.stack.core.types.builtin.DataValue;
import com.digitalpetri.opcua.stack.core.types.builtin.LocalizedText;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.QualifiedName;
import com.digitalpetri.opcua.stack.core.types.builtin.StatusCode;
import com.digitalpetri.opcua.stack.core.types.builtin.Variant;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UByte;
import static com.digitalpetri.opcua.stack.core.types.builtin.unsigned.Unsigned.ubyte;
import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.Observable;
import org.apache.log4j.Logger;
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
    static  Logger Log = Logger.getLogger("jpac.opc");
    
    protected       boolean   connectedAsTarget;
    protected       Signal    signal;
    protected       DataValue dataValue;
    protected final Boolean   lock;
    protected       Value     signalValue;
    protected       Value     lastSignalValue;
    protected       boolean   valid;    
    protected       boolean   lastValid;
    
    public SignalNode(UaNamespace nameSpace, TreeItem signalNode) {
        super(nameSpace, new NodeId(nameSpace.getNamespaceIndex(), signalNode.getSignal().getQualifiedIdentifier()), new QualifiedName(nameSpace.getNamespaceIndex(), signalNode.getSignal().getIdentifier()), LocalizedText.english(signalNode.getSignal().getIdentifier()));
        this.lock            = false;
        this.signal          = signalNode.getSignal();
        this.signalValue     = getSignalValue();
        this.lastSignalValue = null;
        this.valid           = false;
        this.lastValid       = this.valid;        
        
        setDataType(getSignalDataType());
        dataValue = new DataValue(new Variant(getSignalValue().getValue()), StatusCode.BAD);
        setValue(dataValue);
        EnumSet<AccessLevel> accessLevels = AccessLevel.NONE;
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
    
    protected Opc.AccessLevel retrieveOpcAccessLevelForEmbeddedSignals(Signal signal){
      //TODO to be implemented (Handshake etc.)
      Opc.AccessLevel accessLevel = JPac.getInstance().getOpcUaDefaultAccesslevel();
      return accessLevel;
    };

    @Override
    public void update(Observable o, Object o1) {
        Signal sourceSignal = (Signal)o;
        boolean isValid = sourceSignal.isValid();
        synchronized(lock){
            if(isValid){
                lastSignalValue.copy(getSignalValue());
                lastValid       = valid;
                getSignalValue().copy(sourceSignal.getValue());
                valid           = true;
            }
            else{
                invalidateSignalValue();
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
        DataValue theDataValue;
        synchronized(lock){
            if (signalValueChanged()){
                dataValue = new DataValue(new Variant(getSignalValue().getValue()), valid ? StatusCode.GOOD : StatusCode.BAD);
                saveSignalState();
                Log.info("getValue():" + dataValue);//TODO Test!!!!
            }            
            theDataValue = dataValue;
        }
        return theDataValue;
    }
    
    protected boolean signalValueChanged(){
        return lastValid != valid || !signalValue.equals(lastSignalValue);
    }
        
    protected void saveSignalState(){
        try{
            if (lastSignalValue == null){
                lastSignalValue = signalValue.clone();
            }
            else{
                this.lastSignalValue.copy(signalValue);
            }
            this.lastValid = valid;
            }
        catch(CloneNotSupportedException exc){
            Log.error("Error: ", exc);
        }
    }
    
    protected void setValid(boolean valid){
        this.valid = valid;
    }
    
    abstract protected Value     getSignalValue();
    abstract protected void      setSignalValue(DataValue value);
    abstract protected NodeId    getSignalDataType();
    abstract protected void      invalidateSignalValue();
}
