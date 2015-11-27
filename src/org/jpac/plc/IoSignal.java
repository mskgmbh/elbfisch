/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : IoSignal.java
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

import org.jpac.NumberOutOfRangeException;
import org.jpac.SignalAccessException;

/**
 *
 * @author berndschuster
 */
public interface IoSignal {
    public void setAddress(Address address);
    public void checkIn() throws SignalAccessException, AddressException, NumberOutOfRangeException;
    public void checkOut() throws SignalAccessException, AddressException, NumberOutOfRangeException;
    public boolean isToBePutOut();
    public void resetToBePutOut();
    public Address getAddress();
    public WriteRequest getWriteRequest(Connection connection);
    public void setIoDirection(IoDirection ioDirection);
    public IoDirection getIoDirection();
    public void invalidate() throws SignalAccessException;
}
