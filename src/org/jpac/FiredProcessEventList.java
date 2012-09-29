/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : FiredProcessEventList.java
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

public class FiredProcessEventList {
    private static ArrayList<ProcessEvent>  processEventArrayList;
    private static FiredProcessEventList processEventList = null;
    private static int                   count;

    private FiredProcessEventList() {
        processEventArrayList = new ArrayList<ProcessEvent>(100);
        count = 0;
    }
    public static synchronized FiredProcessEventList getInstance() {
        if (processEventList == null) {
            processEventList = new FiredProcessEventList();
        }
        return processEventList;
    }

    public void add(ProcessEvent pe) {
        processEventArrayList.add(pe);
        count++;
    }

    public boolean contains(ProcessEvent pe) {
        return processEventArrayList.contains(pe);
    }

    public Iterator<ProcessEvent> iterator() {
        return processEventArrayList.iterator();
    }

    public void decCount() {
        if (count > 0)
            count--;
    }

    public int getCount() {
        return count;
    }

    public void reset() {
        if (processEventArrayList != null) {
            processEventArrayList.clear();
        }
    }

}
