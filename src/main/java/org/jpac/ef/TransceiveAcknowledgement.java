/**
 * PROJECT   : Elbfisch - java process automation controller (jPac) 
 * MODULE    : TransceiveAcknowledgement.java (versatile input output subsystem)
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
import java.util.HashMap;

/**
 *
 * @author berndschuster
 */
public class TransceiveAcknowledgement extends Acknowledgement{
    protected HashMap<Integer, SignalTransport> listOfSignalTransports;
    protected boolean                           signalsAddedOrRemoved;

    public TransceiveAcknowledgement(){
        super(MessageId.AckTransceive);
        this.listOfSignalTransports = new HashMap<>(100);     
        this.signalsAddedOrRemoved  = false;
    }
                
    //server
    @Override
    public void encode(ByteBuf byteBuf){
        super.encode(byteBuf);
        byteBuf.writeInt(listOfSignalTransports.size());
        listOfSignalTransports.values().forEach((st) -> st.encode(byteBuf));			
    }
    
    //client
    @Override
    public void decode(ByteBuf byteBuf){
    	SignalTransport st     = new SignalTransport(null);
    	SignalTransport target = null;
    	
        super.decode(byteBuf);
        int length = byteBuf.readInt();
        for(int i = 0; i < length; i++){
        	st.decode(byteBuf);
        	target = listOfSignalTransports.get(st.getHandle());
        	if (target == null) {
        		target = new SignalTransport(null);
        		listOfSignalTransports.put(st.getHandle(), target);
        	}
    		target.copyData(st);
            Log.debug("received value {}", listOfSignalTransports.get(i));
        }
    }
        
    //server
    public void updateListOfSignalTransports(HashMap<Integer, SignalTransport> updatedListOfSignalTransports) {
    	synchronized(updatedListOfSignalTransports) {
    		if (signalsAddedOrRemoved) {
    			//a difference of contence between the updateListOfSignalTransport and the local listOfSignalTransports
    			//detected during last update. Clear local copy to refresh its content
    			listOfSignalTransports.clear();
    		}
	    	updatedListOfSignalTransports.values().forEach((st) -> {
	    		SignalTransport target = listOfSignalTransports.get(st.getHandle()); 
	    		if (target == null) {
	    			target = new SignalTransport(null);
	    			listOfSignalTransports.put(st.getHandle(), target);
	    		}
	    		target.copyData(st);
	    	});
	    	signalsAddedOrRemoved = updatedListOfSignalTransports.size() != listOfSignalTransports.size();
    	}
    }
    
    public HashMap<Integer, SignalTransport> getListOfSignalTransports(){
        return this.listOfSignalTransports;
    }   
    
    @Override
    public String toString(){
        return super.toString() + (listOfSignalTransports != null ? ", " +  listOfSignalTransports.size() : "") + ")";
    }    
}
