/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : SimulationProcessEventList.java
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

public class SimulationProcessEventList extends ArrayList<ProcessEvent>{
    private static SimulationProcessEventList processEventList = null;

    private SimulationProcessEventList() {
        super(100);
    }
    public static synchronized SimulationProcessEventList getInstance() {
        if (processEventList == null) {
            processEventList = new SimulationProcessEventList();
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
        boolean removed = super.remove(obj);
        System.out.println(obj + " removed from " + this.getClass().getName() + "(" + removed + ")");
        if (!removed){
            System.out.println("not removed !!!!! ");
        }
        return removed;
    }

    @Override
    public synchronized boolean add(ProcessEvent e) {
        boolean added = super.add(e);
        System.out.println(e + " added to " + this.getClass().getName() + "(" + added + ")");
        return added;
    }
}
