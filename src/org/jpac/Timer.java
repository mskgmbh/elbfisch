/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : Timer.java
 * VERSION   : $Revision: 1.10 $
 * DATE      : $Date: 2012/05/07 06:15:00 $
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
 * LOG       : $Log: Timer.java,v $
 * LOG       : Revision 1.10  2012/05/07 06:15:00  schuster
 * LOG       : TimerExpires fired only inside the cycle directly following the cycle, the timer stopped running
 * LOG       :
 * LOG       : Revision 1.9  2012/04/24 07:02:56  schuster
 * LOG       : introducing TimerExpires event
 * LOG       :
 * LOG       : Revision 1.8  2012/04/19 09:33:37  schuster
 * LOG       : stop() implemented
 * LOG       :
 * LOG       : Revision 1.7  2012/03/09 09:24:24  schuster
 * LOG       : JPac handling breakpoints
 * LOG       :
 * LOG       : Revision 1.6  2012/02/27 07:41:19  schuster
 * LOG       : some minor changes
 * LOG       :
 * LOG       : Revision 1.5  2012/02/23 11:09:33  schuster
 * LOG       : minor correction
 * LOG       :
 * LOG       : Revision 1.4  2012/02/17 07:53:41  schuster
 * LOG       : minor change
 * LOG       :
 * LOG       : Revision 1.3  2012/02/17 07:51:29  schuster
 * LOG       : running state consistent for whole cycle
 * LOG       :
 * LOG       : Revision 1.2  2012/02/16 14:38:24  schuster
 * LOG       : Timer modified
 * LOG       :
 * LOG       : Revision 1.1  2012/02/16 12:55:10  schuster
 * LOG       : Timer added
 * LOG       :
 */

package org.jpac;

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
        timeoutTime = JPac.getInstance().getCycleNanoTime() + periodOfTime;
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
     * @param periodOfTime : period of time in nano seconds
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
