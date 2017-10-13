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
import org.jpac.Value;

/**
 *
 * @author berndschuster
 */
public class SignalTransport {    
    protected int       handle;
    protected Value     value;
    protected boolean   changed;
    
    public SignalTransport(){
    }
    
    public SignalTransport(int handle, Value value){
        this();
        this.handle = handle;
        this.value  = value;
    }
    
    public void encode(ByteBuf byteBuf){
        byteBuf.writeInt(handle);
        value.encode(byteBuf);
    }
    
    public void decode(ByteBuf byteBuf){
        handle = byteBuf.readInt();
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
    public String toString(){
        return super.toString() + "('" + handle + "', " + value + ")";
    }    
}
