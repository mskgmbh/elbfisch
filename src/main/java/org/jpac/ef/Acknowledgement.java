/**
 * PROJECT   : Elbfisch - java process automation controller (jPac) 
 * MODULE    : Acknowledgement.java (versatile input output subsystem)
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
public class Acknowledgement implements Message{
    protected Logger  Log = LoggerFactory.getLogger("jpac.ef");
    
    protected MessageId messageId;
    protected Result    result;
    
    public Acknowledgement(MessageId messageId){
        this.messageId = messageId;
        this.result    = Result.NoFault;
    }
    
    public void setResult(Result result){
        this.result = result;
    }

    public Result getResult(){
        return this.result;
    }
    
    public MessageId getMessageId(){
        return messageId;
    }

    @Override
    public void encode(ByteBuf targetByteBuf) {
        messageId.encode(targetByteBuf);
        result.encode(targetByteBuf);
    }

    @Override
    public void decode(ByteBuf sourceByteBuf) {
        result = Result.fromInt(sourceByteBuf.readShort());
    }
    
    @Override
    public String toString(){
        return getClass().getSimpleName() + "(" + result;
    }
}
