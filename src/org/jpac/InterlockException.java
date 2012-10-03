/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : InterlockException.java
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

package org.jpac;

import java.util.Iterator;
import java.util.ArrayList;

/**
 * base class for interlock exceptions
 * @author berndschuster
 */
abstract public class InterlockException extends ProcessException {
    ArrayList<Signal> signals;
    String         message;

    public InterlockException(){
        signals = new ArrayList<Signal>(10);
    }

    public void add(Signal signal){
        signals.add(signal);
    }

    @Override
    public String getMessage() {
        if (message == null){
            StringBuffer signalList = new StringBuffer();
            for (Iterator i = signals.iterator(); i.hasNext();){
                signalList.append(i.next());signalList.append("; ");
            }
            message = "interlock detected by module " + ((AbstractModule)Thread.currentThread()).getName() + " :" + signalList;
        }
        return message;
    }
}
