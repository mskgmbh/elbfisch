/**
 * PROJECT   : Elbfisch - java process automation controller (jPac) 
 * MODULE    : Subscribe.java (versatile input output subsystem)
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
import org.jpac.SignalNotRegisteredException;
import org.jpac.SignalRegistry;

/**
 *
 * @author berndschuster
 */
public class Subscribe extends Command{    
    protected List<Integer> listOfHandles;
    
    //server
    public Subscribe(){
        super(MessageId.CmdSubscribe);
        this.listOfHandles = new ArrayList<>();
    }
    
    //client
    public Subscribe(ArrayList<Integer> handles){
        this();
        this.listOfHandles = handles;
    }
    
    //client
    @Override
    public void encode(ByteBuf byteBuf){
        super.encode(byteBuf);
        byteBuf.writeInt(listOfHandles.size());
        listOfHandles.forEach((h) -> byteBuf.writeInt(h));
    }
    
    //server
    @Override
    public void decode(ByteBuf byteBuf){
        super.decode(byteBuf);
        int length = byteBuf.readInt();
        for(int i = 0; i < length; i++){
            int h = byteBuf.readInt();
            listOfHandles.add(h);
            Log.debug("received: {}", h);
        }
    }
    
    //server
    @Override
    public Acknowledgement handleRequest(CommandHandler commandHandler) {
        List<Integer> listOfResults = new ArrayList<>();
        //preset list of results
        for(int i = 0; i < listOfHandles.size(); i++){
            listOfResults.add(Result.GeneralFailure.getValue());
        }
        int i = 0;
        //handle subscriptions
        for(int handle: listOfHandles){
            try{
                SignalRegistry.getInstance().getSignal(handle);//check availability of requested signal
                commandHandler.addToListOfOutputSignals(handle);
                listOfResults.set(i, Result.NoFault.getValue());
            } catch(SignalNotRegisteredException exc){
                listOfResults.set(i, Result.SignalNotRegistered.getValue());                
            }
            i++;
        }
        acknowledgement = new SubscribeAcknowledgement(listOfResults);
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
        return super.toString() + "(" + (listOfHandles != null ? listOfHandles.size() : "") + ")";
    }
}
