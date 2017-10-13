/**
 * PROJECT   : Elbfisch - java process automation controller (jPac) 
 * MODULE    : GetHandles.java (versatile input output subsystem)
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author berndschuster
 */
public class GetHandles extends Command{    
    protected int             handle;
    protected List<GetHandle> listOfGetHandles;
    
    //server
    public GetHandles(){
        super(MessageId.CmdGetHandles);
        this.listOfGetHandles = new ArrayList<>();
    }
    
    //client
    public GetHandles(ArrayList<GetHandle> listOfHandleRequests){
        this();
        this.listOfGetHandles = listOfHandleRequests;
    }
    
    //client
    @Override
    public void encode(ByteBuf byteBuf){
        super.encode(byteBuf);
        byteBuf.writeInt(listOfGetHandles.size());
        listOfGetHandles.forEach((gh) -> gh.encode(byteBuf));
    }
    
    //server
    @Override
    public void decode(ByteBuf byteBuf){
        super.decode(byteBuf);
        int length = byteBuf.readInt();
        for(int i = 0; i < length; i++){
            GetHandle gh = (GetHandle)MessageFactory.getMessage(byteBuf);
            listOfGetHandles.add(gh);
            Log.debug("received: " + gh);
        }
    }
    
    //server
    @Override
    public Acknowledgement handleRequest(CommandHandler commandHandler) {
        List<Message> listOfGetHandleAcks = listOfGetHandles.stream().map(gh ->  (Message)gh.handleRequest(commandHandler)).collect(Collectors.toList());
        acknowledgement = new GetHandlesAcknowledgement(listOfGetHandleAcks);
        return acknowledgement;
    }

    @Override
    public Acknowledgement getAcknowledgement() {
        if (acknowledgement == null){
            acknowledgement = new GetHandlesAcknowledgement();
        }
        return acknowledgement;
    }
    
    @Override
    public String toString(){
        return super.toString() + "(" + (listOfGetHandles != null ? listOfGetHandles.size() : "") + ")";
    }
}
