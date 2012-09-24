/**
 * PROJECT   : jPac PLC communication library
 * MODULE    : AddressException.java
 * VERSION   : $Revision$
 * DATE      : $Date$
 * PURPOSE   : thrown, if an addressing problem is encountered
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
 * is thrown, if address cannot be properly resolved, while accessing plc data
 */
public class AddressException extends Exception{

    /**
     * @param message a description of the particular error, which caused the exception
     */
    public AddressException(String message) {
        super(message);
    }

}
