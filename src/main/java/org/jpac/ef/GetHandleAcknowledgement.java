/**
 * PROJECT   : Elbfisch - java process automation controller (jPac) 
 * MODULE    : GetHandleAcknowledgement.java (versatile input output subsystem)
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

/**
 *
 * @author berndschuster
 */
public class GetHandleAcknowledgement extends Acknowledgement{
    protected int             handle;
    protected String          signalIdentifier;
    protected BasicSignalType signalType;
    
    public GetHandleAcknowledgement(){
        super(MessageId.AckGetHandle);
        this.signalIdentifier = null;
    }

    public GetHandleAcknowledgement(String signalIdentifier, int handle, BasicSignalType signalType){
        this();
        this.signalIdentifier = signalIdentifier;
        this.handle           = handle;
        this.signalType       = signalType;
        
    }
    
    public void setHandle(int handle){
        this.handle = handle;
    }
    
    public int getHandle(){
        return this.handle;
    }
    
    public void setSignalType(BasicSignalType signalType){
        this.signalType = signalType;
    }
    
    public BasicSignalType getSignalType(){
        return this.signalType;
    }
    
    public String getSignalIdentifier(){
        return signalIdentifier;
    }
    
    //server side
    @Override
    public void encode(ByteBuf byteBuf){
        super.encode(byteBuf);
        encodeString(signalIdentifier, byteBuf);
        byteBuf.writeInt(handle);
        signalType.encode(byteBuf);
    }

    //client side
    @Override
    public void decode(ByteBuf byteBuf){
        super.decode(byteBuf);
        signalIdentifier = decodeString(byteBuf);
        handle           = byteBuf.readInt();
        signalType       = BasicSignalType.fromInt(byteBuf.readInt());
    }
    
 @Override
    public String toString(){
        return super.toString() + ", '" + signalIdentifier + "', " + signalType + ", " + handle + ")";
    }    
}
