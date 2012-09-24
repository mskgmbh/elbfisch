/**
 * PROJECT   : jPac PLC communication library
 * MODULE    : StringLengthException.java
 * VERSION   : $Revision$
 * DATE      : $Date$
 * PURPOSE   : thrown, if a string can not be stored inside the space reserved for it inside
 *             the PLC
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

/**
 * thrown, if the maximum size of a plc string would be exceeded while writing a java string to the plc
 */
public class StringLengthException extends Exception{

    public StringLengthException(String message) {
        super(message);
    }

}
