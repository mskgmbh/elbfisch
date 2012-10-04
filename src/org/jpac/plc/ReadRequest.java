/**
 * PROJECT   : jPac PLC communication library
 * MODULE    : ReadRequest.java
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

/**
 * represents a read request. Can be added to a an instance of {@link ReadMultipleData} and will contain the
 * data supplied by the plc on return.
 */
public abstract class ReadRequest extends Request{
    public ReadRequest() {
    }

    /**
     * useful, if the Data item is supplied externally
     * @param dataType actually two data types are supported: DATATYPE.BIT for accessing BOOL type data items and DATATYPE.BYTE for all other data types
     * @param address a fully qualified address of the data item to be retrieved (@link Address}
     * @param dataOffset
     * @param dataOffset the offset of the data item inside the local copy of the data (see parameter "data")
     * @throws ValueOutOfRangeException thrown, if the combination of the given parameters is inconsistent
     */
    public ReadRequest(DATATYPE dataType, Address address, int dataOffset, Data data) throws ValueOutOfRangeException, IndexOutOfRangeException{
        super(dataType, address, dataOffset, data);
    }

    /**
     * used to write the read request to the plc as part of a ISO data packet
     * @param conn a valid connection to the plc
     * @throws IOException
     */
    @Override
    public abstract void write(Connection conn) throws IOException;

    /**
     * used to read the data replied by the plc as part of an ISO data packet
     * @param conn a valid connection to the plc
     * @throws IOException
     * @throws WrongOrdinaryException thrown, if the data returned by the plc is inconsistent
     */
    @Override
    public abstract void read(Connection conn) throws IOException;
    
    /**
     * used to write a data item to the plc as part of the read request (Actually nothing is written)
     * @param conn
     * @throws IOException
     */
    @Override
    public abstract void writeData(Connection conn) throws IOException;
}