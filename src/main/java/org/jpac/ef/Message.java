/**
 * PROJECT   : Elbfisch - java process automation controller (jPac) 
 * MODULE    : Message.java (versatile input output subsystem)
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author berndschuster
 */
public interface Message {
    public void encode(ByteBuf targetByteBuf);
    public void decode(ByteBuf sourceByteBuf);
    
    default void encodeString(String string, ByteBuf targetBuf){
        targetBuf.writeInt(string.length());
        targetBuf.writeBytes(string.getBytes(StandardCharsets.UTF_8));        
    };
    
    default String decodeString(ByteBuf sourceByteBuf){
        int length = sourceByteBuf.readInt();
    	byte[] bytes = new byte[length];
        sourceByteBuf.readBytes(bytes, 0, length);
        return new String(bytes, StandardCharsets.UTF_8);        
    };
    
    default void encodeListOfMessages(List<Message> listOfMessages, ByteBuf targetByteBuf){
        targetByteBuf.writeInt(listOfMessages.size());        
        listOfMessages.forEach(m -> m.encode(targetByteBuf));
    }
    
    default List<Message> decodeListOfMessages(ByteBuf sourceByteBuf){
        int           length         = sourceByteBuf.readInt();
        List<Message> listOfMessages = new ArrayList<Message>(length);
        
        for(int i = 0; i < length; i++){
            listOfMessages.add(MessageFactory.getMessage(sourceByteBuf));
        }
        return listOfMessages;
    };
}
