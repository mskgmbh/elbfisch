/**
 * PROJECT   : Elbfisch - java process automation controller (jPac) 
 * MODULE    : MessageId.java (versatile input output subsystem)
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
public enum MessageId {
    CmdUndefined    ( 99),
    CmdPing         ( 01),
    CmdGetHandles   ( 02),
    CmdGetHandle    ( 03),
    CmdTransceive   ( 04),
    CmdBrowse       ( 05),
    CmdSubscribe    ( 06),
    CmdUnsubscribe  ( 07),
    CmdApply        (  8),
    AckUndefined    (-99),
    AckPing         (-01),
    AckGetHandles   (-02),
    AckGetHandle    (-03),
    AckTransceive   (-04),
    AckBrowse       (-05),
    AckSubscribe    (-06),
    AckUnsubscribe  (-07),
    AckApply        (- 8);

    private short id;

    MessageId(int id){
        this.id = (short)id;
    }

    public boolean equals(MessageId ci){
        return this.id == ci.id;
    }    

    public static int size(){
        return 2;
    }
    
    public void encode(ByteBuf byteBuf){
        byteBuf.writeShort(id);
    }

    public void decode(ByteBuf byteBuf){
        id = byteBuf.readShort();
    }
    
    public static MessageId fromInt(int id){
        boolean found = false;
        int     idx   = 0;
        MessageId[] ids = MessageId.values();
        for(int i = 0; i < ids.length && !found; i++){
            found = ids[i].id == id;
            if (found){
                idx = i;
            }
        }
        return ids[idx];
    }
    
    public int getValue(){
        return id;
    }
}