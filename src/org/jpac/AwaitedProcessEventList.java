/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : AwaitedProcessEventList.java
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

import java.util.ArrayList;
import org.apache.log4j.Logger;

/**
 * used to maintain process events which are awaited at a given time. Used by jPac internally
 * @author berndschuster
 */
class AwaitedProcessEventList extends ArrayList<Fireable>{
    static Logger Log = Logger.getLogger("jpac.fireable");

    private static AwaitedProcessEventList processEventList = null;

    private AwaitedProcessEventList() {
        super(100);
    }
    public static synchronized AwaitedProcessEventList getInstance() {
        if (processEventList == null) {
            processEventList = new AwaitedProcessEventList();
        }
        return processEventList;
    }

    public void reset() {
        if (processEventList != null) {
            processEventList.clear();
        }
    }

    @Override
    public synchronized boolean remove(Object obj) {
        boolean removed = false;
        synchronized(this){
            removed = super.remove((Fireable)obj);
        }
        Log.debug(obj + " removed from " + this.getClass().getName() + "(" + removed + ")");
        return removed;
    }

    @Override
    public synchronized boolean add(Fireable f) {
        boolean added = false;
        synchronized(this){
            added = super.add(f);
        }
        Log.debug(f + " added to " + this.getClass().getName() + "(" + added + ")");
        return added;
    }
    
}
