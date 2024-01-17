/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : PeriodOfTime.java
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
 * process event indicating, that a given period of time passed
 * @author berndschuster
 */
public class PeriodOfTime extends ProcessEvent{
    private long           periodOfTime;
    private long           timeoutTime;
    private JPac.CycleMode cycleMode;
    private NthCycle       nthCycle;

    /**
     * 
     * @param periodOfTime period of time in nano seconds. In module contexts use helper values ns,millis,ms,sec to specify: 1000 * ms, 1 * sec, 1000 * millis
     */
    public PeriodOfTime(long periodOfTime){
        if (periodOfTime < 0){
            periodOfTime = 0;
        }
        this.periodOfTime = periodOfTime;
        this.cycleMode = JPac.getInstance().getCycleMode();
        switch(this.cycleMode){
            case FreeRunning: 
            case OneCycle:
                break;
            case Bound:
            case LazyBound:
                //compute periodOfTime as rounded number of cycles
                long  cycleTime  = JPac.getInstance().getCycleTime();
                long  cycleCount = (periodOfTime + (cycleTime >> 1))/cycleTime;
                if (cycleCount > Integer.MAX_VALUE){
                    cycleCount = Integer.MAX_VALUE;
                }
                nthCycle = new NthCycle((int)cycleCount);
                break;
        }
    };

    /**
     * 
     * @param periodOfTime period of time in nano seconds. In module contexts use helper values ns,millis,ms,sec to specify: 1000 * ms, 1 * sec, 1000 * millis
     */
    public PeriodOfTime(double periodOfTime){
        if (periodOfTime < 0){
            periodOfTime = 0;
        }
        this.periodOfTime = Math.round(periodOfTime);
        this.cycleMode    = JPac.getInstance().getCycleMode();
        switch(this.cycleMode){
            case FreeRunning:
            case OneCycle: 
                break;
            case Bound:
            case LazyBound:
                //compute periodOfTime as rounded number of cycles
                long  cycleTime  = JPac.getInstance().getCycleTime();
                long  cycleCount = (this.periodOfTime + (cycleTime >> 1))/cycleTime;
                if (cycleCount > Integer.MAX_VALUE){
                    cycleCount = Integer.MAX_VALUE;
                }
                nthCycle = new NthCycle((int)cycleCount);
                break;
        }
    };


    @Override
    public boolean fire() throws ProcessException{
       boolean localFired = false;
        switch(this.cycleMode){
            case FreeRunning:
                localFired = timeoutTime < System.nanoTime();
                break;
            case Bound:
            case LazyBound:
                localFired = nthCycle.fire();
                break;
            case OneCycle:
                //never fire 
                break;
        }
       return localFired;
    }

    @Override
    public void reset(){
        switch(this.cycleMode){
            case FreeRunning:
                timeoutTime = System.nanoTime() + periodOfTime;
                break;
            case Bound:
            case LazyBound:
                nthCycle.reset();
                break;
            case OneCycle:
                break;                
        }
        super.reset();
    }    
}
