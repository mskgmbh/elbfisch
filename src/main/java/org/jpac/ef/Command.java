/**
 * PROJECT   : Elbfisch - java process automation controller (jPac) 
 * MODULE    : Command.java (versatile input output subsystem)
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author berndschuster
 */
abstract public class Command implements Message{
    protected Logger  Log = LoggerFactory.getLogger("jpac.ef");

    protected Acknowledgement acknowledgement;
    protected MessageId       commandId;
    
    
    public Command(MessageId commandId){
        this.commandId       = commandId;
    }
        
    
    @Override
    public String toString(){
        return getClass().getSimpleName();
    }   
    
    //client
    @Override
    public void encode(ByteBuf byteBuf){
        commandId.encode(byteBuf);;
    }

    //server
    @Override
    public void decode(ByteBuf byteBuf){
        //commandId must have been already read 
    }

    //server side
    abstract public Acknowledgement handleRequest(CommandHandler commandHandler);        
    abstract public Acknowledgement getAcknowledgement();    
}
