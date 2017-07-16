/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : Timer.java
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
 * a timer
 * @author berndschuster
 */
public class Timer {
    private long           lastPollCycle;
    private long           periodOfTime;
    private long           timeoutTime;
    private long           timeoutCycle;
    private JPac.CycleMode cycleMode;
    private String         name;
    
    public Timer(){
        this("NN");
    }
    
    public Timer(String name){
        this.name = name;
        this.lastPollCycle  = 0L;
        this.periodOfTime   = 0L;
        this.timeoutTime    = 0L;
        this.timeoutCycle   = 0L;
        this.cycleMode      = JPac.getInstance().getCycleMode();
    };

    /**
     * used to start the timer
     * @param periodOfTime : period of time in nano seconds
     */
    public void start(long periodOfTime){
        if (periodOfTime < 0){
            periodOfTime = 0;
        }
        this.periodOfTime  = periodOfTime;
        this.lastPollCycle = JPac.getInstance().getCycleNumber();
        switch(cycleMode){
            case FreeRunning:
                break;
            case Bound:
            case LazyBound:
                //compute period of time as rounded number of cycles
                long  cycleTime = JPac.getInstance().getCycleTime();
                timeoutCycle    = lastPollCycle + ((periodOfTime + (cycleTime >> 1))/cycleTime);
                break;
            default:
        }
        timeoutTime = JPac.getInstance().getExpandedCycleNanoTime() + periodOfTime;
    }
    
    /**
     * used to stop the timer.
     */
    public void stop(){
        timeoutTime  = 0;
        timeoutCycle = 0;
    }
    
    /**
     * used to restart the timer with the period of time previously given.
     */
    public void restart(){
        start(periodOfTime);
    }
    
    /**
     * returns the active state of the timer
     * @return true, if the timer is running 
     */
    public boolean isRunning(){
        boolean running = false;
        switch(cycleMode){
            case FreeRunning:
                 running = timeoutTime > JPac.getInstance().getCycleNanoTime();
                 break;

            case Bound:
            case LazyBound:
                 lastPollCycle = JPac.getInstance().getCycleNumber();
                 running       = lastPollCycle < timeoutCycle;
                 break;
        }
        return running;
    }
    /**
     * 
     * @return the period of time until the timer times out, 0, if the timer
     *         already timed out
     */
    public long getRemainingTime(){
        return isRunning() ? timeoutTime - JPac.getInstance().getCycleNanoTime(): 0L;
    }
    
    public TimerExpires expires(){
        return new TimerExpires(this);
    }
    
    @Override
    public String toString(){
        return getClass().getSimpleName() + '(' + name + ')';
    }
}
