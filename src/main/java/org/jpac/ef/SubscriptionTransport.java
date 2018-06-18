/**
 * PROJECT   : Elbfisch - java process automation controller (jPac) 
 * MODULE    : SubscriptionTransport.java (versatile input output subsystem)
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
import org.jpac.WrongUseException;
import org.jpac.plc.IoDirection;

/**
 *
 * @author berndschuster
 */
public class SubscriptionTransport {    
    protected int             handle;
    protected BasicSignalType signalType;
    protected IoDirection     ioDirection;
    
    public SubscriptionTransport(){
    }
    
    public SubscriptionTransport(int handle, BasicSignalType signalType, IoDirection ioDirection) throws WrongUseException{
        this();
        setHandle(handle);
        setSignalType(signalType);
        setIoDirection(ioDirection);
    }
    
    public void encode(ByteBuf byteBuf){
        byteBuf.writeInt(handle);
        byteBuf.writeInt(signalType.toInt());
        byteBuf.writeInt(ioDirection.toInt());
    }
    
    public void decode(ByteBuf byteBuf){
        handle      = byteBuf.readInt();
        signalType  = BasicSignalType.fromInt(byteBuf.readInt());
        ioDirection = IoDirection.fromInt(byteBuf.readInt());
    }

    public int getHandle() {
        return handle;
    }

    public void setHandle(int handle) {
        this.handle = handle;
    }
    
    public void setSignalType(BasicSignalType signalType){
        this.signalType = signalType;
    }
    
    public BasicSignalType getSignalType(){
        return this.signalType;
    }

    public IoDirection getIoDirection() {
        return ioDirection;
    }

    public void setIoDirection(IoDirection ioDirection) throws WrongUseException{
        if (ioDirection != IoDirection.INPUT && ioDirection != IoDirection.OUTPUT){
            throw new WrongUseException("IoDirection must be either IN or OUT");
        }
        this.ioDirection = ioDirection;
    }            

    @Override
    public String toString(){
        return super.toString() + "('" + handle + "', " + ioDirection + ")";
    }    
}
