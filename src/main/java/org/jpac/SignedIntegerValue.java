/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : SignedIntegerValue.java
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
 * represents the value of a signed integer signal
 * @author berndschuster
 * represents a Decimal value
 */
public class SignedIntegerValue implements Value, Cloneable, Serializable{
    static final long serialVersionUID = -1131118539659815694L;//to be compatible to legacy RemoteSignals
    
    protected int               value = 0;
    protected transient boolean valid = false;//transient to be compatible to legacy RemoteSignals
    
    public void set(int value){
        this.value = value;
    }
        
    public int get(){
        return value;
    }
    
    @Override
    public Object getValue(){
        return get();
    }    
    
    @Override
    public void setValue(Object value){
        set((int) value);
    }
    
    @Override
    public void copy(Value aValue){
        set(((SignedIntegerValue)aValue).get());
        this.valid = aValue.isValid();         
    }

    @Override
    public boolean equals(Value aValue) {
        return aValue instanceof SignedIntegerValue && this.value == ((SignedIntegerValue)aValue).get() && this.valid == aValue.isValid();
    }
    
    public boolean equals(int aValue) {
        return this.value == aValue;
    }

    @Override
    public String toString(){
        return Integer.toString(value);
    }

    @Override
    public Value clone() throws CloneNotSupportedException {
        return (SignedIntegerValue) super.clone();
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
        byteBuf.writeInt(get());
    }

    @Override
    public void decode(ByteBuf byteBuf){
        valid = byteBuf.readByte() == 0 ? false : true;
        set(byteBuf.readInt());
    }
    
}
