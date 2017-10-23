/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : CharStringValue.java
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
import java.nio.charset.StandardCharsets;

/**
 * represents the value of a char string signal
 * @author berndschuster
 * represents a Decimal value
 */
public class CharStringValue implements Value, Cloneable, Serializable{
    
    protected String            value = new String();
    protected transient boolean valid = false;//transient to be compatible to legacy RemoteSignals

    
    public void set(String value){
        this.value = value;
    }
    
    public String get(){
        return value;
    }
    
    @Override
    public Object getValue(){
        return get();
    }

    @Override
    public void setValue(Object value){
        set((String) value);
    }
    
    @Override
    public void copy(Value aValue){
        if (((CharStringValue)aValue).get() != null){
            set(new String(((CharStringValue)aValue).get()));         
        }
        else{
            set(null);
        }
        this.valid = aValue.isValid();        
    }

    @Override
    public boolean equals(Value aValue) {
        return (aValue instanceof CharStringValue && this.value == null && ((CharStringValue)aValue).get() == null || 
                aValue instanceof CharStringValue && this.value != null && this.value.equals(((CharStringValue)aValue).get())) && this.valid == aValue.isValid() ;
    }
    
    @Override
    public String toString(){
        return value;
    }

    @Override
    public Value clone() throws CloneNotSupportedException {
        return (CharStringValue) super.clone();
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
        byteBuf.writeInt(get().length());
        byteBuf.writeBytes(get().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void decode(ByteBuf byteBuf){
        valid = byteBuf.readByte() == 0 ? false : true;
        int length   = byteBuf.readInt();
        byte[] bytes = new byte[2 * length];
        byteBuf.readBytes(bytes);
        set(new String(bytes, StandardCharsets.UTF_8));
    }    
}
