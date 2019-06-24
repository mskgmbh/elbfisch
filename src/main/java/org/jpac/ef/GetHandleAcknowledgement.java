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

import org.jpac.BasicSignalType;
import org.jpac.Signal;

import io.netty.buffer.ByteBuf;

/**
 *
 * @author berndschuster
 */
public class GetHandleAcknowledgement extends Acknowledgement{
    private String          signalIdentifier;
    private int             handle;
    private BasicSignalType signalType;

	public GetHandleAcknowledgement(){
        super(MessageId.AckGetHandle);
    }       

    public GetHandleAcknowledgement(String signalIdentifier, int handle, BasicSignalType signalType){
    	this();
        this.signalIdentifier = signalIdentifier;
        this.handle           = handle;
        this.signalType		  = signalType;
    }
		
	public GetHandleAcknowledgement(Signal signal){
    	this(signal.getQualifiedIdentifier(), signal.getHandle(), BasicSignalType.fromSignal(signal));
    }
    
   //server side
    @Override
    public void encode(ByteBuf byteBuf){
        super.encode(byteBuf);
        encodeString(signalIdentifier, byteBuf);
        byteBuf.writeInt(handle);
        byteBuf.writeInt(signalType.toInt());
    }

    //client side
    @Override
    public void decode(ByteBuf byteBuf){
        super.decode(byteBuf);
        signalIdentifier = decodeString(byteBuf);
        handle           = byteBuf.readInt();
        signalType       = BasicSignalType.fromInt(byteBuf.readInt());
    }
    
	public String getSignalIdentifier() {
		return signalIdentifier;
	}
	public void setSignalIdentifier(String signalIdentifier) {
		this.signalIdentifier = signalIdentifier;
	}
	public int getHandle() {
		return handle;
	}
	public void setHandle(int handle) {
		this.handle = handle;
	}
	public BasicSignalType getSignalType() {
		return signalType;
	}
	public void setSignalType(BasicSignalType signalType) {
		this.signalType = signalType;
	}    
}
