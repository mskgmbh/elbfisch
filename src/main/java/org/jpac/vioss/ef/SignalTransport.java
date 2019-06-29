/**
 * PROJECT   : Elbfisch - java process automation controller (jPac) 
 * MODULE    : SignalTransport.java (versatile input output subsystem)
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
 */

package org.jpac.vioss.ef;

import io.netty.buffer.ByteBuf;
import java.util.Observable;

import org.jpac.BasicSignalType;
import org.jpac.CharStringValue;
import org.jpac.DecimalValue;
import org.jpac.LogicalValue;
import org.jpac.Signal;
import org.jpac.SignalObserver;
import org.jpac.SignedIntegerValue;
import org.jpac.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author berndschuster
 */
public class SignalTransport implements SignalObserver {    
	transient protected Logger  Log = LoggerFactory.getLogger("jpac.ef");
    transient protected boolean connected;
    transient protected Signal  connectedSignal;
    protected Integer           handle;
    protected Value             value;
    protected boolean           changed;
    protected BasicSignalType   signalType;
    
    public SignalTransport(){
        this.handle          = 0;
        this.value           = null;
        this.changed         = true;
        this.signalType      = BasicSignalType.Unknown;
        this.connected       = false;
        this.connectedSignal = null;
    }
    
    public SignalTransport(int handle, BasicSignalType signalType){
        this.handle          = handle;
        this.value           = getValueFromSignalType(signalType);
        this.changed         = true;
        this.signalType      = signalType;
        this.connected       = false;
        this.connectedSignal = null;
    }

    public SignalTransport(Signal signal){
    	try {
        this.handle          = signal.getHandle();
        this.value           = signal.getValue().clone();
        this.changed         = true;
        this.signalType      = BasicSignalType.fromSignal(signal);
        this.connected       = false;//connected state must be assigned by the calling method if need be
        this.connectedSignal = signal;
    	} catch(CloneNotSupportedException exc) {
    		/*cannot happen*/
    	}
    }

    protected static Value getValueFromSignalType(BasicSignalType sigType){
        Value value;
        switch(sigType){
            case Logical:
                value = new LogicalValue();
                break;
            case SignedInteger:
                value = new SignedIntegerValue();
                break;
            case Decimal:
                value = new DecimalValue();
                break;
            case CharString:
                value = new CharStringValue();
                break;
            default:
                value = null;
                break;
        }
        return value;
    }
    
    public void encode(ByteBuf byteBuf){
        byteBuf.writeInt(handle);
        signalType.encode(byteBuf);
        value.encode(byteBuf);
        setChanged(false);
    }
    
    public void decode(ByteBuf byteBuf){
        handle     = byteBuf.readInt();
        signalType = BasicSignalType.decode(byteBuf);
        value      = getValueFromSignalType(signalType);
        value.decode(byteBuf);
    }
    
    public void propagate() {
    	if (connectedSignal != null && changed) {
    		connectedSignal.setValue(value);
    		changed = false;
    	}
    }

    public int getHandle() {
        return handle;
    }

    public void setHandle(int handle) {
        this.handle = handle;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
    	if (this.value == null) {
    		try{this.value = value.clone();}catch(CloneNotSupportedException exc) {/*cannot happen*/}
        	setChanged(true);
    	} else if (!this.value.equals(value)) {
    		this.value.copy(value);
        	setChanged(true);
    	}
    }
            
    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }
    
    public Signal getConnectedSignal() {
    	return this.connectedSignal;
    }

    @Override
    public void setConnectedAsTarget(boolean connected) {
        this.connected = connected;
    }

    @Override
    public boolean isConnectedAsTarget() {
        return this.connected;
    }
    
    public BasicSignalType getSignalType() {
    	return this.signalType;
    }

    @Override
    public void update(Observable o, Object arg) {
        value.copy(((Signal)o).getValue());
        setChanged(true);
    }
    
    public void copyData(SignalTransport source) {
    	setValue(source.getValue());
        this.handle          = source.handle;
        this.signalType      = source.signalType;
        this.changed         = source.changed;        
    }

    @Override
    public String toString(){
        return getClass() + "('" + handle + "', " + signalType + ", " + value + ")";
    }    
}
