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

import org.jpac.BasicSignalType;
import org.jpac.Signal;
import org.jpac.SignalNotRegisteredException;
import org.jpac.SignalRegistry;

/**
 *
 * @author berndschuster
 */
public class Subscribe extends Command{    
    protected List<SubscriptionTransport> listOfSubScriptionTransports;
    
    //server
    public Subscribe(){
        super(MessageId.CmdSubscribe);
        this.listOfSubScriptionTransports = new ArrayList<>();
    }
    
    //client
    public Subscribe(ArrayList<SubscriptionTransport> handles){
        this();
        this.listOfSubScriptionTransports = handles;
    }
    
    //client
    @Override
    public void encode(ByteBuf byteBuf){
        super.encode(byteBuf);
        byteBuf.writeInt(listOfSubScriptionTransports.size());
        listOfSubScriptionTransports.forEach((st) -> st.encode(byteBuf));
    }
    
    //server
    @Override
    public void decode(ByteBuf byteBuf){
        super.decode(byteBuf);
        int length = byteBuf.readInt();
        for(int i = 0; i < length; i++){
            SubscriptionTransport st = new SubscriptionTransport();
            st.decode(byteBuf);
            listOfSubScriptionTransports.add(st);
            Log.debug("received: {}", st);
        }
    }
    
    //server
    @Override
    public Acknowledgement handleRequest(CommandHandler commandHandler) {
        List<Integer> listOfResults = new ArrayList<>();
        Result        result;
        //handle subscriptions
        for(SubscriptionTransport st: listOfSubScriptionTransports){
            try{
                Signal signal = SignalRegistry.getInstance().getSignal(st.getHandle());//check availability of requested signal
                if (BasicSignalType.fromSignal(signal) != st.getSignalType()){
                    result = Result.SignalTypeMismatched; 
                } else {
	                switch(st.getIoDirection()){
	                    case INPUT://input from clients point of view   
	                        commandHandler.registerClientInputSignal(st.getHandle());
	                        result = Result.NoFault;
	                        break;
	                    case OUTPUT://output from clients point of view
	                        if (signal.isConnectedAsTarget()){
	                            result = Result.SignalAlreadyConnectedAsTarget; 
	                        }
	                        else{
	                            commandHandler.registerClientOutputSignal(st.getHandle());
	                            result = Result.NoFault;
	                        }
	                        break;
	                    default:
	                        result = Result.IoDirectionMustBeInOrOut;
	                }
                }
            } 
            catch(SignalNotRegisteredException exc){
                result = Result.SignalNotRegistered;                
            }
            catch(Exception exc){
                result = Result.GeneralFailure;                
            }
            listOfResults.add(result.getValue());
        }
        acknowledgement = new SubscribeAcknowledgement(listOfResults);
        return acknowledgement;
    }

    @Override
    public Acknowledgement getAcknowledgement() {
        if (acknowledgement == null){
            acknowledgement = new SubscribeAcknowledgement();
        }
        return acknowledgement;
    }
    
    @Override
    public String toString(){
        return super.toString() + "(" + (listOfSubScriptionTransports != null ? listOfSubScriptionTransports.size() : "") + ")";
    }
}
