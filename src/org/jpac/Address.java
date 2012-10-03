/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
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
 *
 */

package org.jpac;

/**
 * defines an address of a data item which resides on a plc or another device
 * @author Bernd Schuster
 */

public interface Address {
    /**value not applicable in a given context*/
    public final static int NA = -1;
    /**@return the bit index of bit value*/
    public int  getBitIndex();
    /**@return the byte index of a data item*/
    public int  getByteIndex();
    /**@return the size of a data item [byte]*/
    public int  getSize();
    /**set the bit index of bit value*/
    public void setBitIndex(int bitIndex);
    /**set the byte index of a data item value*/
    public void setByteIndex(int byteIndex);
    /**set the size of a data item [byte]*/
    public void setSize(int size);
}
