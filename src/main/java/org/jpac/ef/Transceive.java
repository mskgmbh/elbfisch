/**
 * PROJECT   : Elbfisch - java process automation controller (jPac) 
 * MODULE    : Transceive.java (versatile input output subsystem)
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

import org.jpac.vioss.ef.SignalTransport;

/**
 *
 * @author berndschuster
 */
public class Transceive extends Command{    
    protected HashMap<Integer, SignalTransport> listOfClientOutputTransports;
    protected int                               transportsCount;
    protected int                               transportsCountIndex;
    protected SignalTransport                   receivedSignalTransport;
    
    //client, server
    public Transceive(HashMap<Integer, SignalTransport> listOfClientInputTransports, HashMap<Integer, SignalTransport> listOfClientOutputTransports){
        super(MessageId.CmdTransceive);
        this.listOfClientOutputTransports  = listOfClientOutputTransports;
        this.acknowledgement               = new TransceiveAcknowledgement(listOfClientInputTransports);
        this.receivedSignalTransport       = new SignalTransport();
    }
    
    //client
    @Override
    public void encode(ByteBuf byteBuf){
        super.encode(byteBuf);
    	transportsCountIndex  = byteBuf.writerIndex();
        byteBuf.writeInt(0);//reserve space for the transports count
    	transportsCount       = 0;
        synchronized(listOfClientOutputTransports) {
        	listOfClientOutputTransports.values().forEach((st) -> {
	        		if (st.isChanged()) {
	        			st.encode(byteBuf);
	        			st.setChanged(false);
	        			transportsCount++;
	        		}
	        	});			
        }
        //insert actual number of transports to be transmitted into the stream
        int lastWriterIndex = byteBuf.writerIndex();
        byteBuf.writerIndex(transportsCountIndex);
        byteBuf.writeInt(transportsCount);
        //and restore writer index
        byteBuf.writerIndex(lastWriterIndex); 
    }
    
    //server
    @Override
    public void decode(ByteBuf byteBuf){
    	SignalTransport target;
        super.decode(byteBuf);
        int length = byteBuf.readInt();
        synchronized(listOfClientOutputTransports) {
	        for(int i = 0; i < length; i++){
	            receivedSignalTransport.decode(byteBuf);
	            target = listOfClientOutputTransports.get(receivedSignalTransport.getHandle());
	            target.copyData(receivedSignalTransport);
	            target.setChanged(true);
	        }			
        }
    }
    
    //server
    @Override
    public Acknowledgement handleRequest(CommandHandler commandHandler) {
        //received signal values taken over during message decoding
        return acknowledgement;
    }

    @Override
    public Acknowledgement getAcknowledgement() {
        return acknowledgement;
    }
    
    public HashMap<Integer, SignalTransport> getListOfClientOutputTransports(){
        return this.listOfClientOutputTransports;
    }
    
    @Override
    public String toString(){
        return super.toString() + "(" + (listOfClientOutputTransports != null ? listOfClientOutputTransports.size() : "") + ")";
    }
}
