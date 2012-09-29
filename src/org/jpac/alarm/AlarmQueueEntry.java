/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : AlarmQueueEntry.java
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

import org.jpac.SignalInvalidException;

/**
 *
 * @author berndschuster
 */
public class AlarmQueueEntry {
    private Alarm   alarm;
    private boolean pending;
    private boolean acknowledged;
    
    AlarmQueueEntry(Alarm alarm){
        this.alarm        = alarm;
        try{
            if (alarm.isValid()){
                this.pending      = alarm.isPending();
                this.acknowledged = alarm.isAcknowledged();
            }
            else{
                this.pending      = false;
                this.acknowledged = false;
            }
        }
        catch(SignalInvalidException exc){/*cannot happen*/}
    }
    /**
     * @return the alarm
     */
    public Alarm getAlarm() {
        return alarm;
    }

    /**
     * @param alarm the alarm to set
     */
    void setAlarm(Alarm alarm) {
        this.alarm = alarm;
    }

    /**
     * @return the pending
     */
    public boolean isPending() {
        return pending;
    }

    /**
     * @param pending the pending to set
     */
    void setPending(boolean pending) {
        this.pending = pending;
    }

    /**
     * @return the acknowledged
     */
    public boolean isAcknowledged() {
        return acknowledged;
    }

    /**
     * @param acknowledged the acknowledged to set
     */
    void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }
}
