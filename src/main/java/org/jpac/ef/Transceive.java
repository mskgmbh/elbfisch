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
    protected List<SignalTransport>     listOfSignalTransports;
    
    //server
    public Transceive(){
        super(MessageId.CmdTransceive);
        this.listOfSignalTransports = new ArrayList<>();
        this.acknowledgement        = new TransceiveAcknowledgement();
    }
    
    //client
    public Transceive(ArrayList<SignalTransport> listOfSignalTransports){
        this();
        this.listOfSignalTransports = listOfSignalTransports;
    }
    
    //client
    @Override
    public void encode(ByteBuf byteBuf){
        super.encode(byteBuf);
        byteBuf.writeInt(listOfSignalTransports.size());
        listOfSignalTransports.forEach((st) -> st.encode(byteBuf));
        Log.debug("Transceive.encode");//TODO
    }
    
    //server
    @Override
    public void decode(ByteBuf byteBuf){
        super.decode(byteBuf);
        int length = byteBuf.readInt();
        listOfSignalTransports.clear();
        for(int i = 0; i < length; i++){
            SignalTransport st = new SignalTransport(null);//TODO recycle object ????
            st.decode(byteBuf);
            listOfSignalTransports.add(st);
            Log.debug("received: {}", st);
        }
    }
    
    //server
    @Override
    public Acknowledgement handleRequest(CommandHandler commandHandler) {
        //take over received signal values
        ((TransceiveAcknowledgement)acknowledgement).setListOfReceiveResults(commandHandler.updateChangedClientOutputTransports(listOfSignalTransports));
        ((TransceiveAcknowledgement)acknowledgement).setListOfSignalTransports(commandHandler.retrieveChangedClientInputTransports());
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
