/**
 * PROJECT   : jPac PLC communication library
 * MODULE    : LobRxTx.java
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

import java.io.IOException;
import org.apache.log4j.Logger;
import org.jpac.IndexOutOfRangeException;
import org.jpac.Value;
import org.jpac.plc.Request.DATATYPE;

/**
 * Used to transfer a large data item between the java application and the plc or other device.<br>
 * "Large" means, that the size of the data item exceeds the maximum pdu size, <br>
 * which is the size of the chunk of data which can be transferred by a<br>
 * single transaction.<br>
 * To transfer, large data items are splitted into smaller chunks of date,<br>
 * which then are transferred sequentially within multiple transactions.<br>
 * CAUTION: During this transmission sequence, the data will be inconsistent on <br>
 *          the partcular receiving side (critical section). Make sure, that the software <br>
 *          of the receiving side waits until the transmission completed, before<br>
 *          processing the data <br><br>
 */
abstract public class LobRxTx implements Value{
    static Logger Log = Logger.getLogger("jpac.plc");
    
    protected   Connection          conn;
    protected   Data                data;
    protected   WriteRequest        txReq;
    protected   ReadRequest         rxReq;
    protected   TransmitTransaction txTrans;
    protected   ReceiveTransaction  rxTrans;
    protected   Address             address;
    protected   int                 dataOffset;

   /**
    * useful in situations, where the instance is part of complex data structure (STRUCT)
    * @param conn an open TCP/IP connection to the plc or other device
    * @param db the data block (DB) to be accessed
    * @param address the address of the data item inside the plc or other device
    * @param dataOffset the byte offset inside {@link #data}, holding the data item to be exchanged with the plc or other device
    * @param data instance of {@link Data} used to hold the data item exchanged with the plc or other device.
    * @throws IndexOutOfRangeException
    */
    public LobRxTx(Connection conn, Address address, int dataOffset, Data data) throws IndexOutOfRangeException{
        this.conn       = conn;
        this.data       = data;
        this.txReq      = null;
        this.rxReq      = null;
        this.txTrans    = null;
        this.rxTrans    = null;
        if (address.getBitIndex() != Address.NA || dataOffset < 0)
            throw new IndexOutOfRangeException();
        this.dataOffset = dataOffset;
        this.address    = address;
    }

    public static int getSize(){
        throw new UnsupportedOperationException("must be implemented by subclasses !!!!!");
    }

   /**
     * used to write the represented data item to the plc or other device.
     * @throws IOException
     */
    public void write() throws IOException {
        boolean             done            = false;
        boolean             requestAssigned = false;
        int                 chunkSize       = conn.getMaxTransferLength();
        TransmitTransaction trans           = getTxTrans();
        int                 txOffset        = 0;//remove : address.getByteIndex();
        //prepare command (may have been used in previous read, write operations
        trans.removeAllRequests();
        //and write out the data object in 1 to n chunks of data
        try{
            WriteRequest req = getWriteRequest();
            do{
                if (address.getSize() - txOffset > chunkSize){
                    //prepare a chunk of data to transmit
                    req.setByteAddress(address.getByteIndex() + txOffset);
                    req.setDataOffset(dataOffset + txOffset);
                    req.setDataLength(chunkSize);
                    txOffset += chunkSize;
                }
                else{
                    //prepare the remaining bytes
                    req.setByteAddress(address.getByteIndex() + txOffset);
                    req.setDataOffset(dataOffset + txOffset);
                    req.setDataLength(address.getSize() - txOffset);
                    done = true;
                }
                //assign the request to the cmd once
                if (!requestAssigned){
                    try{trans.addRequest(req);
                    }
                    catch(TooManyRequestsException exc)
                    {Log.error("Error:",exc);
                     throw new IOException();
                    }
                    requestAssigned = true;
                }
                //write the chunk out to the plc or other device
                trans.transact();
            }
            while(!done);
        }
        catch(Exception exc){
           Log.error("Error:",exc);
           throw new IOException(exc);
        }
        //clear command
        trans.removeAllRequests();
    }

    /**
     * used to read the represented data item from the plc or other device.
     * @throws IOException
     */
    public LobRxTx read() throws IOException {
        boolean              done            = false;
        boolean              requestAssigned = false;
        int                  chunkSize       = conn.getMaxTransferLength();
        ReceiveTransaction   trans           = getRxTrans();
        int                  txOffset        = 0;
        //prepare command (may have been used in previous read, write operations
        trans.removeAllRequests();
        //and write out the data object in 1 to n chunks of data
        try{
            ReadRequest  req = getReadRequest();
            do{
                if (address.getSize() - txOffset > chunkSize){
                    //prepare a chunk of data to transmit
                    req.setByteAddress(address.getByteIndex() + txOffset);
                    req.setDataOffset(dataOffset + txOffset);
                    req.setDataLength(chunkSize);
                    txOffset += chunkSize;
                }
                else{
                    //prepare the remaining bytes
                    req.setByteAddress(address.getByteIndex() + txOffset);
                    req.setDataOffset(dataOffset + txOffset);
                    req.setDataLength(address.getSize() - txOffset);
                    done = true;
                }
                //assign the request to the cmd once
                if (!requestAssigned){
                    trans.addRequest(req);
                    requestAssigned = true;
                }
                //write the chunk out to the plc or other device
                trans.transact();
            }
            while(!done);
        }
        catch(Exception exc){
           Log.error("Error:",exc);
           throw new IOException(exc);
        }
        //clear command
        trans.removeAllRequests();
        return this;
    }
    
    /**
     * adds this LobRxTx to the data items to be written to the plc on next getTxTrans().transact() operation
     * multiple writeDeferred() calls for different parts of a complex data item may be issued before transacting them collectively
     * CAUTION: the transaction buffer must be cleared before the first writeDeferred() call.
     *          Intermediate write() operations are not permitted.
     *          The size of the LobRxtx must not exceed the maximum transfer length of the connection
     * somedata.getTxTrans().removeAllRequests();
     *      ...
     *      somedata.something.set(...);
     *      somedata.something.writeDeferred();
     *      ...
     *      somedata.somethingelse.set(...);
     *      somedata.somethingelse.writeDeferred();
     *      ...
     * somedata.getTxTrans().transact(); 
     * @throws IOException
     */
    public void writeDeferred() throws IOException{
        if (address.getSize() > conn.getMaxTransferLength()){
            throw new IOException("size of data structure exceeds max. transfer length: " + getSize() + " > " + conn.getMaxTransferLength());
        }
        TransmitTransaction trans = getTxTrans();        
        try{
            WriteRequest req = getWriteRequest();
            trans.addRequest(req);
        }
        catch(Exception exc)
        {Log.error("Error:",exc);
         throw new IOException(exc);
        }
    }
        
    protected Connection getConnection() {
        return conn;
    }

    protected Data getData() {
        return data;
    }

    protected void setData(Data data) {
        this.data = data;
    }

    /**
     * returns the {@link WriteRequest}, used to write the data item to the plc or other device
     */
    @SuppressWarnings("empty-statement")
    public WriteRequest getWriteRequest() throws ValueOutOfRangeException, IndexOutOfRangeException{
        Address reqAddress = null;
        if (txReq == null){
           try{reqAddress = (Address) address.clone();}catch(CloneNotSupportedException exc){};
           txReq = getConnection().generateWriteRequest(DATATYPE.BYTE, reqAddress , dataOffset, data);
        }
        return txReq;
    }

    /**
     * returns the {@link ReadRequest}, used to read the data item from the plc or other device
     */
    @SuppressWarnings("empty-statement")
    public ReadRequest getReadRequest() throws ValueOutOfRangeException, IndexOutOfRangeException{
        Address reqAddress = null;
        if (rxReq == null){
           try{reqAddress = (Address) address.clone();}catch(CloneNotSupportedException exc){};
           rxReq = getConnection().generateReadRequest(DATATYPE.BYTE, reqAddress, dataOffset, data);
        }
        return rxReq;
    }

    /**
     * @return the txTrans
     */
    protected TransmitTransaction getTxTrans() {
        if (txTrans == null)
            txTrans = conn.generateTransmitTransaction();
        return txTrans;
    }

    /**
     * @return the rxTrans
     */
    protected ReceiveTransaction getRxTrans() {
        if (rxTrans == null)
            rxTrans = conn.generateReceiveTransaction();
        return rxTrans;
    }
    
    public Address getAddress(){
        return address;
    }

    /**
     * used to get a copy of this LobRxTx. Copied are it's data item, it's address and it's offset in a containing data LobRxTx
     * @param aValue 
     */
    @Override
    public void copy(Value aValue){
        this.data.copy(((LobRxTx)aValue).getData());
        this.address.setByteIndex(((LobRxTx)aValue).getAddress().getByteIndex());
        this.address.setBitIndex(((LobRxTx)aValue).getAddress().getBitIndex());
        this.address.setSize(((LobRxTx)aValue).getAddress().getSize());
        this.dataOffset = ((LobRxTx)aValue).dataOffset;
    };
    
    /**
     * used to check the equality of of this LobRxTx to aValue. Both values a said to be equal, if their data, address and offset are identical
     * @param aValue
     * @return 
     */
    @Override
    public boolean equals(Value aValue){
        return this.data.equals(((LobRxTx)aValue).getData()) && 
               this.address.getByteIndex() == ((LobRxTx)aValue).getAddress().getByteIndex() &&
               this.address.getBitIndex()  == ((LobRxTx)aValue).getAddress().getBitIndex()  &&
               this.address.getSize()      == ((LobRxTx)aValue).getAddress().getSize()      &&
               this.dataOffset             == ((LobRxTx)aValue).dataOffset;
    };
    

    /**
     * used to clone this LobRxTx. Cloneing comprises its address, its offset inside a containing data structure and its data
     * REMARKS: the connection related properties such like Read-, WriteRequest or the connection itself is not cloned to avoid
     *          access from several threads. I/O over the connection should be done by the owner of the assigned signal
     * @return
     * @throws CloneNotSupportedException 
     */    
    @Override
    public abstract Value clone() throws CloneNotSupportedException;
}
