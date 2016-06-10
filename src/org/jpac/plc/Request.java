/**
 * PROJECT   : jPac PLC communication library
 * MODULE    : Request.java
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
import java.io.IOException;
import org.apache.log4j.Logger;
import org.jpac.Address;

/**
 * represents a i/o request
 * @author berndschuster
 */
public abstract class Request {
    protected static Logger Log = Logger.getLogger("jpac.plc");

    public enum DATATYPE {
        NOTYPE (0x00),
        BIT    (0x01),
        BYTE   (0x02),
        WORD   (0x04),
        DWORD  (0x08);
        private final int n;

        DATATYPE(int n){
            this.n = n;
        }
        /**
         * returns the ordinary of the enum value
         * @return the ordinary of the enum value
         */
        public int toInt(){
            return this.n;
        }

        /**
         * returns a value according to a given ordinary
         * @param  n ordinary
         * @throws WrongOrdinaryException
         * @return the enum value according to the given ordinary
         */
        public static DATATYPE getValue(int n)throws WrongOrdinaryException{
            DATATYPE match = null;
            for (DATATYPE p : DATATYPE.values()){
                if (p.toInt() == n) {
                    match = p;
                    break;
                }
            }
            if (match == null){
                throw new WrongOrdinaryException("invalid ordinary: " + n);
            }
            return match;
        }
    }

    protected final int READRESULTHEADERTAG    =   -1;
    protected final int WRITEREQUESTHEADERTAG  =    0;
    protected final int USERDATAHEADERLENGTH   =    4;


    protected Data             data;
    protected DATATYPE         dataType;
    protected Enum             result;
    protected Address          address;
    protected int              dataOffset;

    Request(){
        dataType    = DATATYPE.NOTYPE;
        data        = null;
        address     = null;
        dataOffset  = 0;
    }

    public Request(DATATYPE dataType, Address address, int dataOffset, Data data) throws ValueOutOfRangeException, IndexOutOfRangeException{
        if (dataType == DATATYPE.BIT && address.getSize() != 1){
            throw new ValueOutOfRangeException("exactly one bit per bitwise request can be accessed");
        }
        this.dataType    = dataType;
        this.data        = data;
        this.address     = address; //new Address(address.getDb(), address.getByteIndex(), address.getBitIndex(), address.getSize(), address);
        this.dataOffset  = dataOffset;
    }

    public abstract void write(Connection conn)throws IOException;
    public abstract void writeData(Connection conn)throws IOException;
    public abstract void read(Connection conn)throws IOException, WrongOrdinaryException;
    
    /**
     * @return the data
     */
    public Data getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(Data data) {
        this.data = data;
    }

    public int getBitAddress() {
        return address.getBitIndex();
    }

    public void setBitAddress(int bitAddress) {
        this.address.setBitIndex(bitAddress);
    }

    public int getByteAddress() {
        return address.getByteIndex();
    }

    public void setByteAddress(int byteAddress) {
        this.address.setByteIndex(byteAddress);
    }
    
    /**
     * @return returns the number of bytes occupied by the request inside the PDU's parameters section sent to the PLC
     */
    public abstract int getSendParameterLength();
    /**
     * @return returns the number of bytes occupied by the request inside the PDU's parameters section that will be received from the PLC
     */
    public abstract int getReceiveParameterLength();
    /**
     * @return returns the number of bytes occupied by the request inside the PDU's parameters section sent to the PLC
     */
    public abstract int getSendDataLength();
    /**
     * @return returns the number of bytes occupied by the request inside the PDU's parameters section that will be received from the PLC
     */
    public abstract int getReceiveDataLength();
    /**
     * @return the dataType
     */
    public DATATYPE getDataType() {
        return dataType;
    }

    /**
     * @param dataType the dataType to set
     */
    public void setDataType(DATATYPE dataType) {
        this.dataType = dataType;
    }

    /**
     * @return the result
     */
    public Enum getResult() {
        return result;
    }

    /**
     * @param result the result to set
     */
    public void setResult(Enum result) {
        this.result = result;
    }

    /**
     * @return the dataLength
     */
    public int getDataLength() {
        return address.getSize();
    }

    /**
     * @param dataLength the data length to set
     */
    public void setDataLength(int dataLength) {
        this.address.setSize(dataLength);
    }

    /**
     * @return the dataOffset
     */
    public int getDataOffset() {
        return this.dataOffset;
    }

    /**
     * @param dataOffset the dataOffset to set
     */
    public void setDataOffset(int dataOffset) {
        this.dataOffset = dataOffset;
    }

    public Address getAddress(){
        return this.address;
    }
}
