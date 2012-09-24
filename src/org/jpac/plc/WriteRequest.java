/**
 * PROJECT   : jPac PLC communication library
 * MODULE    : WriteRequest.java
 * VERSION   : $Revision$
 * DATE      : $Date$
 * PURPOSE   : represents a write request
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
import java.io.IOException;
import org.jpac.Address;

/**
 * represents a write request. Can be added to a an instance of {@link WriteMultipleData}.
 */
public abstract class WriteRequest extends Request{
    public WriteRequest() {
    }
    
    /**
     * @param dataType actually two data types are supported: DATATYPE.BIT for accessing BOOL type data items and DATATYPE.BYTE for all other data types
     * @param db the datablock inside the plc, which contains the data to be written to
     * @param byteAddress the byte address of the data inside the data block (db)
     * @param bitAddress the bit address of data inside the byte addressed by "byteAddress". Applicable, if the data to be written is of the plc type BOOL
     * @param dataOffset the offset of the data item inside the local copy of the data (see parameter "data")
     * @param dataLength the length of the data item, to be written
     * @param data a local copy of the data, to be written to the plc
     * @throws ValueOutOfRangeException thrown, if the combination of the given parameters is inconsistent
     * @throws IndexOutOfRangeException thrown, if one of the address of offset values are out of range.
     */
//    public WriteRequest(DATATYPE dataType, int db, int byteAddress, int bitAddress, Address.Io io,  int dataOffset, int dataLength, Data data) throws ValueOutOfRangeException, IndexOutOfRangeException{
//        super(dataType, db, byteAddress, bitAddress, io, dataOffset, dataLength, data);
//    }

    /**
     * @param dataType actually two data types are supported: DATATYPE.BIT for accessing BOOL type data items and DATATYPE.BYTE for all other data types
     * @param address a fully qualified address of the data item to be retrieved (@link Address}
     * @param dataOffset the offset of the data item inside the local copy of the data (see parameter "data")
     * @throws ValueOutOfRangeException thrown, if the combination of the given parameters is inconsistent
     */
    public WriteRequest(DATATYPE dataType, Address address, int dataOffset, Data data) throws ValueOutOfRangeException, IndexOutOfRangeException{
        super(dataType, address, dataOffset, data);
    }

    /**
     * used to write a write request to the plc as part of an ISO data packet
     * @param conn a valid connection to the plc
     * @throws IOException
     */
    @Override
    public abstract void write(Connection conn) throws IOException;

    /**
     * used to write the data portion of the write request as part of a ISO data packet
     * @param conn a valid connection to a plc
     * @throws IOException
     */
    public abstract void writeData(Connection conn) throws IOException;
    /**
     * not applicable for WriteRequest
     * @param conn
     * @throws IOException
     * @throws WrongOrdinaryException
     */
    @Override
    public abstract void read(Connection conn) throws IOException, WrongOrdinaryException;
}
