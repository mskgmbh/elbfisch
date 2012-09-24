/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : ExclusiveDisjunctiveEvent.java
 * VERSION   : $Revision: 1.2 $
 * DATE      : $Date: 2012/03/09 10:30:28 $
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
 *
 * LOG       : $Log: ExclusiveDisjunctiveEvent.java,v $
 * LOG       : Revision 1.2  2012/03/09 10:30:28  schuster
 * LOG       : Firable.fire(), Fireable.reset() made public
 * LOG       :
 */

package org.jpac;

import java.util.Iterator;
import java.util.ArrayList;


public class ExclusiveDisjunctiveEvent extends ProcessEvent{
    private ArrayList<ProcessEvent> combinedEvents;
    Iterator<ProcessEvent>       eventIterator;

    public ExclusiveDisjunctiveEvent(ProcessEvent anEvent){
        combinedEvents = new ArrayList<ProcessEvent>(10);
        combinedEvents.add(anEvent);
    }
    
    @Override
    public ExclusiveDisjunctiveEvent xor(ProcessEvent anEvent){
        combinedEvents.add(anEvent);
        return this;
    }

    public ExclusiveDisjunctiveEvent clear(){
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
        int     fireCnt  = 0;
        boolean IamFired = false;
        eventIterator = combinedEvents.iterator();
        //give all events the chance to fire
        while(eventIterator.hasNext()){
            if (eventIterator.next().isFired()){
                fireCnt++;
            }
        }
        IamFired = fireCnt == 1;
        if (!IamFired){
            //reset all events, which are fired in this cycle, if it is not exactly one
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
            eventList = eventList + " xor " + e.toString() + firedStr;
        }
        return eventList;
    }

}
