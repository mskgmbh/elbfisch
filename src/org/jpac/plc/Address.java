/**
 * PROJECT   : jPac PLC communication library
 * MODULE    : Address.java
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

/**
 * represents the address of a data item stored in a plc or i/o device
 * @author berndschuster
 */
public class Address implements org.jpac.Address, Cloneable{
    protected int bitIndex;
    protected int byteIndex;
    protected int size;

    /**
     * constructs an address object
     * @param byteIndex byte address of the data item
     * @param bitIndex bit address of the data item
     * @param size size of the data item [byte]
     * @throws IndexOutOfRangeException 
     */
    public Address(int byteIndex, int bitIndex, int size) throws IndexOutOfRangeException{
        //check for consistency
        if (byteIndex < NA)
            throw new IndexOutOfRangeException();
        if (bitIndex < NA || bitIndex > 7)
            throw new IndexOutOfRangeException();

        this.byteIndex = byteIndex;
        if (size < NA || size == 0)
            throw new IndexOutOfRangeException();
        this.bitIndex  = bitIndex;
        this.size      = size;
    }

    public int getBitIndex() {
        return this.bitIndex;
    }

    public int getByteIndex() {
        return this.byteIndex;
    }

    public int getSize() {
        return this.size;
    }

    public void setBitIndex(int bitIndex) {
        this.bitIndex = bitIndex;
    }

    public void setByteIndex(int byteIndex) {
        this.byteIndex = byteIndex;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public String toString() {
        String address = (getByteIndex() != NA ? getByteIndex() : "-") + ";" + (getBitIndex() != NA ? getBitIndex() : "-") + ";" + (getSize() != NA ? getSize() : "-");
        return getClass().getSimpleName() + "(" + address + ")";
    }

    /**
     * @return a string representation of the object as a character separated string (';')
     */
    public String asCSV() {
        return (getByteIndex() != NA ? getByteIndex() : "") + ";" + (getBitIndex() != NA ? getBitIndex() : "") + ";" + (getSize() != NA ? getSize() : "");
    }

    @Override
    public Object clone() throws CloneNotSupportedException{
        Address cloned;
        cloned = (Address) super.clone();
        cloned.setBitIndex(bitIndex);
        cloned.setByteIndex(byteIndex);
        cloned.setSize(size);
        return cloned;
    }
}
