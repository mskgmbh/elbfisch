/**
 * PROJECT   : Elbfisch - java process automation controller (jPac) 
 * MODULE    : MessageFactory.java (versatile input output subsystem)
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
import org.jpac.InconsistencyException;

/**
 *
 * @author berndschuster
 */
public class MessageFactory {
    protected CommandHandler commandHandler;
    protected Transceive     recycledTransceive;

    public MessageFactory(CommandHandler commandHandler) {
    	this.commandHandler     = commandHandler;
    	this.recycledTransceive = new Transceive(commandHandler.getListOfClientInputTransports(), commandHandler.getListOfClientOutputTransports());
    }
    
    public static Message getMessage(ByteBuf byteBuf) {
        Message message = null;
        short  messageId = byteBuf.readShort();
        MessageId commandId = MessageId.fromInt(messageId);
    	message = getCommand(commandId);
        if (message != null){
            message.decode(byteBuf);
        } else {
            throw new InconsistencyException("illegal message id received: " + commandId);        	
        }
        return message;
    }
    
    public Message getRecycledMessage(ByteBuf byteBuf) throws InconsistencyException{
        Message message = null;
        MessageId commandId = readMessageId(byteBuf);
        //Up to now, only the transceive command is recycled because of its frequent use
        if (commandId == MessageId.CmdTransceive) {
        	message = recycledTransceive;
        } else {
        	message = getCommand(commandId);
        }
        if (message != null){
            message.decode(byteBuf);
        } else {
            throw new InconsistencyException("illegal message id received: " + commandId);        	
        }
        return message;
    }
    
    public static MessageId readMessageId(ByteBuf byteBuf) {
    	return MessageId.fromInt(byteBuf.readShort());
    }

    protected static Message getCommand(MessageId commandId) {
    	Message message = null;
        switch(commandId) {
        case CmdPing:
            message = new Ping();
            break;
        case CmdGetHandle:
            message = new GetHandle();
            break;
        case CmdGetHandles:
            message = new GetHandles();
            break;
        case CmdBrowse:
            message = new Browse();
            break;
        case CmdSubscribe:
            message = new Subscribe();
            break;
        case CmdUnsubscribe:
            message = new Unsubscribe();
            break;
        case CmdTransceive:
            message = null;//Transceive command can only be used as a recylced instance      		
            break;
        case CmdApply:
            message = new Apply();      		
            break;
        case AckPing:
            message = new PingAcknowledgement();
            break;
        case AckGetHandle:
            message = new GetHandleAcknowledgement();
            break;
        case AckGetHandles:
            message = new GetHandlesAcknowledgement();
            break;
        case AckBrowse:
            message = new BrowseAcknowledgement();
            break;
        case AckSubscribe:
            message = new SubscribeAcknowledgement();
            break;
        case AckUnsubscribe:
            message = new UnsubscribeAcknowledgement();
            break;
        case AckTransceive:
            message = null;//Transceive command can only be used as a recylced instance   
            break;
        case AckApply:
            message = new ApplyAcknowledgement();
            break;
         default:
        }
        return message;
    }
}
