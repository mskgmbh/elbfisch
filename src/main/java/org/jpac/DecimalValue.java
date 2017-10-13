/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : DecimalValue.java
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
 * represents a Decimal value
 * @author berndschuster
 */
public class DecimalValue implements Value, Cloneable, Serializable{
    protected double  value = 0.0;
    protected boolean valid = false;
    
    public void set(double value){
        this.value = value;
    }
    
    public double get(){
        return value;
    }
    
    @Override
    public Object getValue(){
        return get();
    }
    
    @Override
    public void setValue(Object value){
        set((double) value);
    }
    
    @Override
    public void copy(Value aValue){
        set(((DecimalValue)aValue).get());
        this.valid = aValue.isValid();        
    }

    @Override
    public boolean equals(Value aValue) {
        return aValue instanceof DecimalValue && Math.abs(this.value - ((DecimalValue)aValue).get()) < 0.00000000000000001 && this.valid == aValue.isValid();
    }
    
    public boolean equals(double aValue) {
        return Math.abs(this.value - aValue) < 0.00000000000000001;
    }

    @Override
    public String toString(){
        return Double.toString(value);
    }

    @Override
    public Value clone() throws CloneNotSupportedException {
        return (DecimalValue) super.clone();
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
        byteBuf.writeDouble(get());
    }

    @Override
    public void decode(ByteBuf byteBuf){
        valid = byteBuf.readByte() == 0 ? false : true;
        set(byteBuf.readDouble());
    }
    
}
