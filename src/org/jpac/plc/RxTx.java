/**
 * PROJECT   : jPac PLC communication library
 * MODULE    : RxTx.java
 * VERSION   : $Revision$
 * DATE      : $Date$
 * PURPOSE   : used to transfer single byte objects from/to a plc
 * AUTHOR    : Bernd Schuster, MSK Gesellschaft fuer Automatisierung mbH, Schenefeld
 * REMARKS   : -
 * CHANGES   : CH#n <Kuerzel> <datum> <Beschreibung>
 *
 * This file is part of the jPac PLC communication library.
 * The jPac PLC communication library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The jPac PLC communication library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the jPac PLC communication library.  If not, see <http://www.gnu.org/licenses/>.
 *
 * LOG       : $Log$
 */

package org.jpac.plc;

import org.jpac.IndexOutOfRangeException;
import org.jpac.plc.Request.DATATYPE;
import java.io.IOException;
import org.apache.log4j.Logger;

/**
 * Used to transfer a byte value between the java application and the plc.
 * Can be used standalone or in the context of a complex data structure (STRUCT)
 */
public abstract class RxTx {
    static Logger Log = Logger.getLogger("jpac.plc");
    
    protected Connection          conn;
    protected Data                data;
    protected WriteRequest        txReq;
    protected ReadRequest         rxReq;
    protected TransmitTransaction txTrans;
    protected ReceiveTransaction  rxTrans;
    protected Address             address;
    protected int                 dataOffset;

    /**
     * @param conn an open TCP/IP connection to the plc
     * @param address address of the data item exchanged with the plc
     * @param dataOffset the byte offset inside {@link #data}, holding the data item to be exchanged with the plc
     * @param data instance of {@link Data} used to hold the data item exchanged with the plc.
     * @throws IndexOutOfRangeException
     */
    public RxTx(Connection conn, Address address, int dataOffset, Data data) throws IndexOutOfRangeException{
        this.conn       = conn;
        this.data       = data;
        this.txReq      = null;
        this.rxReq      = null;
        this.txTrans    = null;
        this.rxTrans    = null;
        if (address.getByteIndex() < Address.NA || address.getBitIndex() < Address.NA || dataOffset < 0)
            throw new IndexOutOfRangeException();
        this.address    = address;
        this.dataOffset = dataOffset;
    }

    public static int getSize(){
        throw new UnsupportedOperationException("must be implemented by subclasses !!!!!");
    }

    /**
     * used to writeInt the represented data item to the plc. Only the portion of {@link #data} occupied by the data item is written.
     * @throws IOException
     */
    public void write() throws IOException{
        TransmitTransaction trans = getTxTrans();
        //prepare command (may have been used in previous read, writeInt operations
        trans.removeAllRequests();
        try{
            WriteRequest req = getWriteRequest();
            trans.addRequest(req);
        }
        catch(Exception exc)
        {Log.error("Error:",exc);
         throw new IOException(exc);
        }
        //writeInt the bit out to the plc
        trans.transact();
        //clear command
        trans.removeAllRequests();
    }
    
    /**
     * used to read the data item from plc and store it inside {@link #data}.
     * @return this
     * @throws IOException
     */
    public RxTx read() throws IOException{
        ReceiveTransaction trans = getRxTrans();
        //prepare command (may have been used in previous read, writeInt operations
        trans.removeAllRequests();
        try{
            ReadRequest req = getReadRequest();
            trans.addRequest(req);
        }
        catch(Exception exc)
        {Log.error("Error:",exc);
         throw new IOException(exc);
        }
        //read the bit from the plc
        trans.transact();
        //clear command
        trans.removeAllRequests();
        return this;
    }

    protected Connection getConnection() {
        return conn;
    }

    protected Data getData() {
        return data;
    }

    /**
     * returns the {@link WriteRequest}, used to writeInt the data item to the plc
     */
    public WriteRequest getWriteRequest() throws ValueOutOfRangeException, IndexOutOfRangeException{
        if (txReq == null){
           txReq = getConnection().generateWriteRequest(DATATYPE.BYTE, address , dataOffset, data);
        }
        return txReq;
    }

    /**
     * returns the {@link ReadRequest}, used to read the data item from the plc
     */
    public ReadRequest getReadRequest() throws ValueOutOfRangeException, IndexOutOfRangeException{
        if (rxReq == null){
           rxReq = getConnection().generateReadRequest(DATATYPE.BYTE, address, dataOffset, data);
        }
        return rxReq;
    }

    /**
     * @return the txCmd
     */
    protected TransmitTransaction getTxTrans() {
        if (txTrans == null)
            txTrans = conn.generateTransmitTransaction();
        return txTrans;
    }

    /**
     * @return the rxCmd
     */
    protected ReceiveTransaction getRxTrans() {
        if (rxTrans == null)
            rxTrans = conn.generateReceiveTransaction();
        return rxTrans;
    }

    public Address getAddress(){
        return address;
    }
}
