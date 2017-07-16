/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : AlarmGone.java
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

package org.jpac.alarm;

import org.jpac.Fireable;
import org.jpac.JPac;
import org.jpac.ProcessEvent;
import org.jpac.ProcessException;
import org.jpac.SignalInvalidException;

/**
 * process event indicating that an alarm condition has been gone
 * @author berndschuster
 */
public class AlarmGone extends ProcessEvent{
    private Alarm alarm;
    private long  lastPendingCycle;
    
    public AlarmGone(Alarm alarm){
        super();
        this.alarm = alarm;
        if (alarm.isValid()){
            try{this.lastPendingCycle = alarm.isPending() ? JPac.getInstance().getCycleNumber() : 0L;} catch(SignalInvalidException exc){/*cannot happen*/};
        }
        else{
            this.lastPendingCycle = 0L;
        }
    }

    @Override
    public boolean fire() throws ProcessException {
        if (alarm.isPending()){
            lastPendingCycle = JPac.getInstance().getCycleNumber();
        }
        //return true during the cycle, in which the state of the alarm changes from "pending" to "not pending"
        return (!alarm.isPending()) && (lastPendingCycle == JPac.getInstance().getCycleNumber() - 1);
    }

    @Override
    public String toString(){
        return super.toString() + "(" + alarm + ")";
    }

    @Override
    protected boolean equalsCondition(Fireable fireable){
        boolean equal = false;
        if (fireable instanceof AlarmGone){
            AlarmGone ag = (AlarmGone)fireable;
            equal = this.alarm.equals(ag.alarm);
        }
        return equal;
    }
    
}
