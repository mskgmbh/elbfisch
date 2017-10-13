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
import java.util.List;

/**
 *
 * @author berndschuster
 */
public class Transceive extends Command{    
    protected List<SignalTransport> listOfReceivedSignalTransports;
    
    //server
    public Transceive(){
        super(MessageId.CmdTransceive);
        this.listOfReceivedSignalTransports = new ArrayList<>();
    }
    
    //client
    public Transceive(ArrayList<SignalTransport> listOfSignalTransports){
        this();
        this.listOfReceivedSignalTransports = listOfSignalTransports;
    }
    
    //client
    @Override
    public void encode(ByteBuf byteBuf){
        super.encode(byteBuf);
        byteBuf.writeInt(listOfReceivedSignalTransports.size());
        listOfReceivedSignalTransports.forEach((st) -> st.encode(byteBuf));
    }
    
    //server
    @Override
    public void decode(ByteBuf byteBuf){
        super.decode(byteBuf);
        int length = byteBuf.readInt();
        listOfReceivedSignalTransports.clear();
        for(int i = 0; i < length; i++){
            SignalTransport st = new SignalTransport();
            st.decode(byteBuf);
            listOfReceivedSignalTransports.add(st);
            Log.debug("received: {}", st);
        }
    }
    
    //server
    @Override
    public Acknowledgement handleRequest(CommandHandler commandHandler) {
        TransceiveAcknowledgement ack = (TransceiveAcknowledgement)getAcknowledgement();
        //take over received signal values
        ack.setListOfReceiveResults(commandHandler.updateInputValues(listOfReceivedSignalTransports));
        ack.setListOfSignalTransports(commandHandler.updateOutputValues());
        return acknowledgement;
    }

    @Override
    public Acknowledgement getAcknowledgement() {
        if (acknowledgement == null){
            acknowledgement = new TransceiveAcknowledgement();
        }
        return acknowledgement;
    }
    
    @Override
    public String toString(){
        return super.toString() + "(" + (listOfReceivedSignalTransports != null ? listOfReceivedSignalTransports.size() : "") + ")";
    }
}
