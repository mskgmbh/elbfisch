/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : DisjunctiveEvent.java
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
 * used to combine a number events in a disjunctive way. It is fired, if "process event 1" OR "process event 2" OR .... are fired at a given time
 * @author berndschuster
 */
public class DisjunctiveEvent extends ProcessEvent{
    private ArrayList<ProcessEvent> combinedEvents;
    Iterator<ProcessEvent>       eventIterator;

    protected DisjunctiveEvent(ProcessEvent anEvent){
        combinedEvents = new ArrayList<ProcessEvent>(10);
        combinedEvents.add(anEvent);
    }
    
    @Override
    public DisjunctiveEvent or(ProcessEvent anEvent){
        combinedEvents.add(anEvent);
        return this;
    }

    @Override
    public void reset(){
        //reset own context
        super.reset();
        //reset all disjunctive events
        Iterator<ProcessEvent> e = combinedEvents.iterator();
        while(e.hasNext()){
            e.next().reset();
        }

    }

    @Override
    public boolean fire() throws ProcessException {
        boolean IamFired = false;
        boolean combinedEventFired = false;
        eventIterator = combinedEvents.iterator();
        //give all events the chance to fire
        while(eventIterator.hasNext()){
            combinedEventFired = eventIterator.next().isFired();
            IamFired = IamFired || combinedEventFired;
        }
        return IamFired;
    }

    @Override
    public String toString(){
        String  eventList = "";
        String  firedStr  = "";
        ProcessEvent e    = null;
        eventIterator = combinedEvents.iterator();
        if (eventIterator.hasNext()){
            e = eventIterator.next();
            try{
                firedStr =  e.isFired() ? "[fired !]" : "";
            }
            catch(ProcessException exc)
            {
                firedStr = "???";
            }
            eventList = e.toString() + firedStr;
        }
        while(eventIterator.hasNext()){
            e = eventIterator.next();
            try{
                firedStr =  e.isFired() ? "[fired !]" : "";
            }
            catch(ProcessException exc)
            {
                firedStr = "???";
            }
            eventList = eventList + " or " + e.toString() + firedStr;
        }
        return eventList;
    }

}
