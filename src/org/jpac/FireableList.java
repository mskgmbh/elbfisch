/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : FireableList.java
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
import org.apache.log4j.Logger;

/**
 * used by jPac internally 
 */
public class FireableList extends ArrayList<Fireable>{
    static Logger Log = Logger.getLogger("jpac.fireable");
    private int ActiveEventsCount;
    String  identifier;

    FireableList(String id) {
        super(100);
        ActiveEventsCount = 0;
        if (id != null){
            identifier = id;
        }
        else{
            identifier = "";
        }
    }

    public boolean remove(Fireable obj) {
        synchronized(this){
            boolean removed = super.remove(obj);
            //Log.debug(obj + " removed from " + identifier + "(" + removed + ")");
            return removed;
        }
    }

    @Override
    public boolean add(Fireable f) {
        synchronized(this){
           boolean added = super.add(f);
            //Log.debug(f + " added to " + identifier + "("+ added + ")");
            if(added){
                ActiveEventsCount++;
            }
        return added;
        }
    }

    @Override
    public Iterator<Fireable> iterator() {
        return (Iterator<Fireable>)super.iterator();
    }

    @Override
    public void clear() {
        synchronized(this){
            super.clear();
            ActiveEventsCount = 0;
        }
    }

    public void decActiveEventsCount() {
        synchronized(this){
            if (ActiveEventsCount > 0)
                ActiveEventsCount--;
        }
    }

    public int getActiveEventsCount() {
        return ActiveEventsCount;
    }

}
