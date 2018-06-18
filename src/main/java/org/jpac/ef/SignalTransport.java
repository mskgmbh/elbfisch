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

package org.jpac.ef;

import io.netty.buffer.ByteBuf;
import java.util.Observable;
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
public class SignalTransport implements SignalObserver{    
    protected Logger            Log = LoggerFactory.getLogger("jpac.ef");
    protected Integer           handle;
    protected Value             value;
    protected boolean           changed;
    protected BasicSignalType   signalType;
    transient protected boolean connected;
    
    public SignalTransport(Signal signal){
        try{
            if (signal != null){
                this.handle     = signal.getQualifiedIdentifier().hashCode();
                this.value      = signal.getValue().clone();
                this.changed    = false;
                this.signalType = BasicSignalType.fromSignal(signal);
                this.connected  = false;
            }
        }
        catch(Exception exc){
            Log.error("Error: ", exc);
        }
    }
    
    protected Value getValueFromSignalType(BasicSignalType sigType){
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
        this.value = value;
    }
            
    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    @Override
    public void setConnectedAsTarget(boolean connected) {
        this.connected = connected;
    }

    @Override
    public boolean isConnectedAsTarget() {
        return this.connected;
    }

    @Override
    public void update(Observable o, Object arg) {
        Signal sourceSignal = (Signal)o;
        if (value == null){
            try{value = sourceSignal.getValue().clone();}catch(CloneNotSupportedException exc){/*cannot happen*/};
        }
        else{
            value.copy(sourceSignal.getValue());
        }
        setChanged(true);
    }

    @Override
    public String toString(){
        return getClass() + "('" + handle + "', " + signalType + ", " + value + ")";
    }    
}
