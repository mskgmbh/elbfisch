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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author berndschuster
 */
public class TransceiveAcknowledgement extends Acknowledgement{
    protected List<Byte>                        listOfTransferResults;
    protected HashMap<Integer, SignalTransport> listOfSignalTransports;

    public TransceiveAcknowledgement(){
        this(new ArrayList<>(), new HashMap<>());
    }
    
    public TransceiveAcknowledgement(List<Byte> listOfTransferResults, HashMap<Integer, SignalTransport> listOfSignalTransports){
        super(MessageId.AckTransceive);
        this.listOfTransferResults   = listOfTransferResults;
        this.listOfSignalTransports  = listOfSignalTransports;     
    }
            
    //server
    @Override
    public void encode(ByteBuf byteBuf){
        super.encode(byteBuf);
        byteBuf.writeInt(listOfTransferResults.size());
        listOfTransferResults.forEach((r) -> byteBuf.writeByte(r));
        byteBuf.writeInt(listOfSignalTransports.size());
        listOfSignalTransports.forEach((h, st) -> st.encode(byteBuf));
    }
    
    //client
    @Override
    public void decode(ByteBuf byteBuf){
        super.decode(byteBuf);
        listOfTransferResults.clear();
        int length = byteBuf.readInt();
        for(int i = 0; i < length; i++){
            byte b = byteBuf.readByte();
            listOfTransferResults.add(b);
            Log.debug("received tx result for {} : {}", i, Result.fromInt(b));
        }
        listOfSignalTransports.clear();
        length = byteBuf.readInt();
        for(int i = 0; i < length; i++){
            SignalTransport st = new SignalTransport(null);//TODO object pooling ????
            st.decode(byteBuf);
            listOfSignalTransports.put(st.getHandle(), st);
            Log.debug("received value {}", st);
        }
    }
    
    public HashMap<Integer, SignalTransport> getListOfSignalTransports(){
        return this.listOfSignalTransports;
    }

    public List<Byte> getListOfReceiveResults() {
        return listOfTransferResults;
    }

    /**
     * @param listOfReceiveResults the listOfTransferResults to set
     */
    public void setListOfReceiveResults(List<Byte> listOfReceiveResults) {
        this.listOfTransferResults = listOfReceiveResults;
    }

    /**
     * @param listOfReceiveResults the listOfTransferResults to set
     */
    public void setListOfSignalTransports(HashMap<Integer, SignalTransport> listOfSignalTransports) {
        this.listOfSignalTransports = listOfSignalTransports;
    }

    @Override
    public String toString(){
        return super.toString() + (listOfSignalTransports != null ? ", " +  listOfSignalTransports.size() : "") + ")";
    }    
}
