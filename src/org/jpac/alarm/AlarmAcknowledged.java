/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : AlarmAcknowledged.java
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

/**
 * process event indicating, that an alarm has been acknowledged by the user
 * @author berndschuster
 */
public class AlarmAcknowledged extends ProcessEvent{
    private Alarm alarm;
    private long  lastNotAcknowledgedCycle;
    
    public AlarmAcknowledged(Alarm alarm){
        super();
        this.alarm = alarm;
        this.lastNotAcknowledgedCycle = !alarm.isAcknowledged() ? JPac.getInstance().getCycleNumber() : 0L;
    }

    @Override
    public boolean fire() throws ProcessException {
        if (!alarm.isAcknowledged()){
            lastNotAcknowledgedCycle = JPac.getInstance().getCycleNumber();
        }
        //return true during the cycle, in which the acknowledge state of the alarm changes from "false" to "true"
        return (alarm.isAcknowledged()) && (lastNotAcknowledgedCycle == JPac.getInstance().getCycleNumber() - 1);
    }

    @Override
    public String toString(){
        return super.toString() + "(" + alarm + ")";
    }

    @Override
    protected boolean equalsCondition(Fireable fireable){
        boolean equal = false;
        if (fireable instanceof AlarmAcknowledged){
            AlarmAcknowledged aa = (AlarmAcknowledged)fireable;
            equal = this.alarm.equals(aa.alarm);
        }
        return equal;
    }
    
}
