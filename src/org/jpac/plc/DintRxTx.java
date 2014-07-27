/**
 * PROJECT   : jPac PLC communication library
 * MODULE    : DintRxTx.java
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
import org.jpac.IndexOutOfRangeException;

/**
 * Used to transfer a signed 32 bit integer (DINT) value between the java application and the plc.
 * Can be used standalone or in the context of a complex data structure (STRUCT)
 */
public class DintRxTx extends RxTx{
    /**
     * @param conn an open TCP/IP connection to the plc
     * @param address address of the data item exchanged with the plc
     * @param dataOffset the byte offset inside {@link #data}, holding the data item to be exchanged with the plc
     * @param data instance of {@link Data} used to hold the data item exchanged with the plc.
     * @throws IndexOutOfRangeException
     */
    public DintRxTx(Connection conn, Address address, int dataOffset, Data data) throws IndexOutOfRangeException{
        super(conn, address, dataOffset, data);
        if (address.getByteIndex() <= Address.NA || address.getBitIndex() != Address.NA  || address.getSize() > getSize())
            throw new IndexOutOfRangeException();
    }

    /**
     * used to read the data item from plc and store it inside {@link #data}.
     * @return this
     * @throws IOException
     */
    @Override
    public DintRxTx read() throws IOException{
        super.read();
        return this;
    }

    /**
     * used to set the data item to a given value. <br>
     * The data item is written to {@link #data} first.<br>
     * The actual transfer to the plc is done by invocation of {@link #writeInt()}: aRxTx.set(aValue).writeInt();
     * @param value a value
     * @return this. Useful in cases where the data item should be written to the plc immediately (see above).
     * @throws AddressException
     */
    public DintRxTx set(int value) throws AddressException, ValueOutOfRangeException {
        getData().setDINT(dataOffset, value);
        return this;
    }

    /**
     * used to retrieve the current value of the data item. <br>
     * The data item is locally retrieved from {@link #data}.<br>
     * to get the actual value from the plc use {@link #read()} first: value = aRxTx.read().get();
     * @return value
     * @throws AddressException
     */
    public long get() throws AddressException {
        return getData().getDINT(dataOffset);
    }


    public static int getSize() {
        return 4;
    }
}
