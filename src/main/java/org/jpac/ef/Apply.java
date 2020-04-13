/**
 * PROJECT   : Elbfisch - java process automation controller (jPac) 
 * MODULE    : Ping.java (versatile input output subsystem)
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
public class Apply extends Command{
	long elbfischInstanceHash;
	
	//server
	public Apply(){
        super(MessageId.CmdApply);
        this.elbfischInstanceHash = 0L;
    }

    //client
	public Apply(String bindAddress, int port){
        super(MessageId.CmdApply);
        this.elbfischInstanceHash = (bindAddress.hashCode() << 16) + port;
    }
    //client
    @Override
    public void encode(ByteBuf byteBuf){
        super.encode(byteBuf);
        byteBuf.writeInt(MessageId.CmdApply.getValue());
        byteBuf.writeLong(elbfischInstanceHash);
    }
    
    //server
    @Override
    public void decode(ByteBuf byteBuf){
        super.decode(byteBuf);
        elbfischInstanceHash = byteBuf.readLong();
    }
    
    //server
    @Override
    public Acknowledgement handleRequest(CommandHandler commandHandler) {
       Result result = Result.NoFault;
       if (commandHandler.getElbfischInstanceHash() != elbfischInstanceHash) {
    	   //first application
    	   if (CommandHandler.getListOfActiveCommandHandlers().stream().anyMatch(c -> c.getElbfischInstanceHash() == elbfischInstanceHash)) {
	    	   result = Result.ElbfischInstanceAlreadyConnected;
	       } else {
	    	   commandHandler.setElbfischInstanceHash(elbfischInstanceHash);
	       }
       }
       getAcknowledgement().setResult(result);
       return getAcknowledgement();
    }

    @Override
    public Acknowledgement getAcknowledgement() {
        if (acknowledgement == null){
            acknowledgement = new ApplyAcknowledgement();
        }
        return acknowledgement;
    }
}
