/**
 * PROJECT   : Elbfisch - java process automation controller (jPac) 
 * MODULE    : PingAcknowledgement.java (versatile input output subsystem)
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
public class PingAcknowledgement extends Acknowledgement{
    protected int handle;
    
    public PingAcknowledgement(){
        super(MessageId.AckPing);
    }
    
     //server
    @Override
    public void encode(ByteBuf byteBuf){
        super.encode(byteBuf);
    }
    
    //client
    @Override
    public void decode(ByteBuf byteBuf){
        super.decode(byteBuf);
    }
}
