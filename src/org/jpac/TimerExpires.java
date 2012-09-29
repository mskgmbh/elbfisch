/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : TimerExpires.java
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

/**
 *
 * @author berndschuster
 */
public class TimerExpires extends ProcessEvent{
    private Timer   timer;
    private long    lastRunningCycle;
    
    protected TimerExpires(Timer timer){
        super();
        this.timer            = timer;
        this.lastRunningCycle = timer.isRunning() ? JPac.getInstance().getCycleNumber() : 0L;
    }

    @Override
    public boolean fire() throws ProcessException {
        if (timer.isRunning()){
            lastRunningCycle = JPac.getInstance().getCycleNumber();
        }
        //return true during the cycle, in which the state of the timer changes from "running" to "expired"
        return (!timer.isRunning()) && (lastRunningCycle == JPac.getInstance().getCycleNumber() - 1);
    }
    
    @Override
    public String toString(){
        return super.toString() + "(" + timer + ")";
    }

    @Override
    protected boolean equalsCondition(Fireable fireable){
        boolean equal = false;
        if (fireable instanceof TimerExpires){
            TimerExpires sf = (TimerExpires)fireable;
            equal = this.timer.equals(sf.timer);
        }
        return equal;
    }
}
