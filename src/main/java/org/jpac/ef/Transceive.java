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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author berndschuster
 */
public class Transceive extends Command{    
    protected ArrayList<SignalTransport>  listOfSignalTransports;
    protected SignalTransport        st;
    
    //server
    public Transceive(){
        super(MessageId.CmdTransceive);
        this.listOfSignalTransports = new ArrayList<>(100);
        this.acknowledgement        = new TransceiveAcknowledgement();
        this.st  					= new SignalTransport(null);
    }
    
    //client
    public Transceive(ArrayList<SignalTransport> listOfSignalTransports){
        super(MessageId.CmdTransceive);
        this.listOfSignalTransports  = listOfSignalTransports;
        this.acknowledgement         = new TransceiveAcknowledgement();
    }
    
    //client
    @Override
    public void encode(ByteBuf byteBuf){
        super.encode(byteBuf);
        byteBuf.writeInt(listOfSignalTransports.size());
        listOfSignalTransports.forEach((st) -> st.encode(byteBuf));
    }
    
    //server
    @Override
    public void decode(ByteBuf byteBuf){
    	SignalTransport target;
        super.decode(byteBuf);
        int length = byteBuf.readInt();
        listOfSignalTransports.ensureCapacity(length);
        for(int i = 0; i < length; i++){
            st.decode(byteBuf);
            if (i >= listOfSignalTransports.size()) {
            	SignalTransport nSt = new SignalTransport(null);
            	nSt.setValue(SignalTransport.getValueFromSignalType(st.getSignalType()));
            	listOfSignalTransports.add(i, nSt);
            }
            target = listOfSignalTransports.get(i);
            target.copyData(st);
            Log.debug("received: {}", st);
        }			
    }
    
    //server
    @Override
    public Acknowledgement handleRequest(CommandHandler commandHandler) {
        //take over received signal values
    	commandHandler.updateClientOutputTransports(listOfSignalTransports);
    	((TransceiveAcknowledgement)acknowledgement).updateListOfSignalTransports(commandHandler.getListOfClientInputTransports());
        return acknowledgement;
    }

    @Override
    public Acknowledgement getAcknowledgement() {
        return acknowledgement;
    }
    
    public List<SignalTransport> getListOfSignalTransports(){
        return this.listOfSignalTransports;
    }
    
    @Override
    public String toString(){
        return super.toString() + "(" + (listOfSignalTransports != null ? listOfSignalTransports.size() : "") + ")";
    }
}
