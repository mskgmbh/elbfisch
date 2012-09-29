/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : ConjunctiveEvent.java
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


public class ConjunctiveEvent extends ProcessEvent{
    private ArrayList<ProcessEvent> combinedEvents;
    Iterator<ProcessEvent>          eventIterator;
    
    protected ConjunctiveEvent(ProcessEvent anEvent){
        combinedEvents = new ArrayList<ProcessEvent>(10);
        combinedEvents.add(anEvent);
    }
    
    @Override
    public ConjunctiveEvent and(ProcessEvent anEvent){
        combinedEvents.add(anEvent);
        return this;
    }

    public ConjunctiveEvent clear(){
        combinedEvents.clear();
        return this;
    }

    @Override
    public void reset(){
        //reset own context
        super.reset();
        //reset all conjunctive events
        Iterator<ProcessEvent> e = combinedEvents.iterator();
        while(e.hasNext()){
            e.next().reset();
        }

    }

    @Override
    public boolean fire() throws ProcessException {
        boolean IamFired = true;
        ProcessEvent processEvent;
        eventIterator = combinedEvents.iterator();
        while(eventIterator.hasNext() && IamFired){
            IamFired = IamFired && eventIterator.next().isFired();
        }
        if (!IamFired){
            //reset all events, which are fired in this cycle, if at least one is not
            eventIterator = combinedEvents.iterator();
            while(eventIterator.hasNext()){
                ProcessEvent event = eventIterator.next();
                if (event.isFired()){
                    //all events must occur simultaneously
                    event.reset();
                }
            }
        }
        return IamFired;
    }

    @Override
    public String toString(){
        String  eventList = "";
        eventIterator = combinedEvents.iterator();
        if (eventIterator.hasNext()){
            eventList = eventIterator.next().toString();
        }
        while(eventIterator.hasNext()){
            eventList = eventList + " and " + eventIterator.next().toString();
        }
        return eventList;
    }

}
