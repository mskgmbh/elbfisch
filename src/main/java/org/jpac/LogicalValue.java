/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : LogicalValue.java
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

package org.jpac;

import io.netty.buffer.ByteBuf;
import java.io.Serializable;

/**
 * represents the value of a logical signal
 * @author berndschuster
 */
public class LogicalValue implements Value, Cloneable, Serializable{
    static final long serialVersionUID = -1258642332401824933L;//to be compatible to legacy RemoteSignals
    
    protected           boolean value = false;
    protected transient boolean valid = false;//transient to be compatible to legacy RemoteSignals
    
    public void set(boolean value){
       this.value = value;  
    }
    
    public boolean get(){
        return this.value;
    }
 
    @Override
    public void setValue(Object aValue){
        set((boolean) aValue);
    }

    @Override
    public Object getValue(){
        return get();
    }
    
    public boolean is(boolean state){
        return this.value == state;
    }
    
    @Override
    public void copy(Value aValue) {
        this.value = ((LogicalValue)aValue).get();
        this.valid = ((LogicalValue)aValue).isValid();
    }

    @Override
    public boolean equals(Value aValue) {
        return aValue instanceof LogicalValue && this.value == ((LogicalValue)aValue).get() && this.valid == aValue.isValid();
    }
    
    @Override
    public String toString(){
        return Boolean.toString(this.value);
    }

    @Override
    public Value clone() throws CloneNotSupportedException {
        return (LogicalValue) super.clone();
    }

    @Override
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    @Override
    public boolean isValid() {
        return this.valid;
    }

    @Override
    public void encode(ByteBuf byteBuf){
        byteBuf.writeByte(valid ? 1 : 0);
        byteBuf.writeBoolean(get());
    }

    @Override
    public void decode(ByteBuf byteBuf){
        valid = byteBuf.readByte() == 0 ? false : true;
        set(byteBuf.readBoolean());
    }
}
