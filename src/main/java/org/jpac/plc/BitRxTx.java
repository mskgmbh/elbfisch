/**
 * PROJECT   : jPac PLC communication library
 * MODULE    : BitRxTx.java
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

package org.jpac.plc;

import org.jpac.IndexOutOfRangeException;
import org.jpac.plc.Request.DATATYPE;
import java.io.IOException;

/**
 * Used to transfer a bit value (boolean) between the java application and the plc.
 * Can be used standalone or in the context of a complex data structure (STRUCT)
 */
public class BitRxTx extends RxTx{

    private Data bitData;

    /**
     * @param conn an open TCP/IP connection to the plc
     * @param address address of the data item exchanged with the plc
     * @param dataOffset the byte offset inside {@link #data}, holding the data item to be exchanged with the plc
     * @param data instance of {@link Data} used to hold the data item exchanged with the plc.
     * @throws IndexOutOfRangeException
     */
    public BitRxTx(Connection conn, Address address, int dataOffset, Data data) throws IndexOutOfRangeException{
        super(conn, address, dataOffset, data);
        if (address.getByteIndex() <= Address.NA || address.getBitIndex() <= Address.NA  || address.getSize() != getSize())
            throw new IndexOutOfRangeException();
    }
    /**
     * used to set the bit to a given boolean value. <br>
     * The data item is written to {@link #data} first (true == 1, false == 0). <br>
     * The actual transfer to the plc is done by invocation of writeInt(): aBitRxTx.set(true).writeInt();
     * @param state boolean value
     * @return this. Useful in cases where the bit should be written to the plc immediately (see above).
     * @throws AddressException
     */
    public BitRxTx set(boolean state) throws AddressException {
        getData().setBIT(dataOffset, address.getBitIndex(), state);
        return this;
    }
    /**
     * used to retrieve the current state of the bit. <br>
     * The data item is locally retrieved from {@link #data} (true == 1, false == 0). <br>
     * to get the actual value from the plc use {@link #read()} first: boolean bitState = aBitRxTx.read().is(true);
     * @param state expected state of the bit as a boolean value
     * @return true, if the state of the bit equals the expected state
     * @throws AddressException
     */
    public boolean is(boolean state) throws AddressException {
        return getData().getBIT(dataOffset, address.getBitIndex()) == state;
    }

    /**
     * used to read the data item from plc and store it inside {@link #data}.
     * @return this. Useful in cases, where a data item retrieved from the plc should immediately be processed: boolean bitState = aBitRxTx.read().is(true);
     * @throws IOException
     */
    @Override
    public BitRxTx read() throws IOException{
        ReceiveTransaction trans = getRxTrans();
        //prepare command (may have been used in previous read, writeInt operations
        trans.removeAllRequests();
        try{
            ReadRequest req  = getReadRequest();
            trans.addRequest(req);
            //read the bit from the plc
            trans.transact();
            //copy bit into data
            data.setBIT(dataOffset, address.getBitIndex(), getReadRequest().getData().getBYTE(0) != 0x00);
            //clear command
            trans.removeAllRequests();
        }
        catch(Exception exc)
        {Log.error("Error:",exc);
         throw new IOException(exc);
        }
        return this;
    }

    /**
     * returns the {@link WriteRequest}, used to write the data item to the plc
     */
    @Override
    public WriteRequest getWriteRequest()  throws ValueOutOfRangeException, IndexOutOfRangeException{
        boolean errorOccured = false;
        try{
            if (bitData == null){
                bitData = conn.generateDataObject(1);
            }
            bitData.setBYTE(0, data.getBIT(dataOffset, address.getBitIndex()) ? 0x01 : 0x00);
            if (txReq == null){
               txReq = getConnection().generateWriteRequest(DATATYPE.BIT, address, 0, bitData);
            }
            else{
               txReq.setData(bitData);
            }
        }
        catch(Exception exc){
            Log.error("Error: " + exc);
            errorOccured = true;
        }
        return errorOccured ? null : txReq;
    }

    /**
     * returns the {@link ReadRequest}, used to read the data item from the plc
     */
    @Override
    public ReadRequest getReadRequest()  throws ValueOutOfRangeException, IndexOutOfRangeException{
        if (rxReq == null){
            if (bitData == null){
                bitData = conn.generateDataObject(1);
            }
           rxReq = getConnection().generateReadRequest(DATATYPE.BIT, address, 0, bitData);
        }
        return rxReq;
    }

    /**
     * 
     * @return the size of this item in bytes
     */
    public static int getSize(){
        return 1;
    }
    
    @Override
    public String toString(){
        String str = null;
        try{
            str = new Boolean(is(true)).toString();
        }
        catch(AddressException exc)
        {
            str = super.toString();
        };
        return str;
    }
}
