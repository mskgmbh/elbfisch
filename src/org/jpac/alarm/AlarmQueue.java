/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : AlarmQueue.java
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;

/**
 * central registry for all alarms instantiated inside an application
 * implemented as singleton
 * @author berndschuster
 */
public class AlarmQueue extends Observable implements Observer{
    static  Logger Log = Logger.getLogger("jpac.Alarm");    
    
    private static final int                    QUEUESIZE = 2000;
    
    private static AlarmQueue                   instance;
    private List<Alarm>                         alarms;
    private ConcurrentHashMap<String,Integer>   alarmIndices;
    private int                                 lastIndex;
    private ArrayBlockingQueue<AlarmQueueEntry> queue;  
    private Observer                            observer;
    
    private Integer                             pendingAlarmsCountForSeverityAlarm;
    private Integer                             pendingAlarmsCountForSeverityWarning;
    private Integer                             pendingAlarmsCountForSeverityMessage;
    
    protected AlarmQueue(){
        this.alarms        = Collections.synchronizedList(new ArrayList<Alarm>());
        this.alarmIndices  = new ConcurrentHashMap<String, Integer>();
        this.lastIndex     = 0;
        this.queue         = new ArrayBlockingQueue<AlarmQueueEntry>(QUEUESIZE);
        this.observer      = null;
        this.pendingAlarmsCountForSeverityAlarm   = 0;
        this.pendingAlarmsCountForSeverityWarning = 0;
        this.pendingAlarmsCountForSeverityMessage = 0;   
    }
    /**
     * @return the instance of the alarm queue
     */
    public static AlarmQueue getInstance(){
        if (instance == null) {
            instance = new AlarmQueue();
        }
        return instance;
    }
     
    /**
     * used to register an alarm to the alarm queue
     * @param alarm
     */
    public void register(Alarm alarm){
        alarms.add(lastIndex, alarm);
        alarmIndices.put(alarm.getContainingModule().getQualifiedName() + '.' + alarm.getIdentifier(), lastIndex);
        lastIndex++;
        //register myself as an observer for the registered alarm
        alarm.addObserver(this);
    }

    /*
     * @return the ArrayList of registered alarms
     */
    public List getAlarms(){
        return alarms;
    }
    
    public Alarm getAlarm(String identifier){
        Integer index = alarmIndices.get(identifier);
        return alarms.get(alarmIndices.get(identifier));
       
    }

    public void update(Observable o, Object o1) {
        if (o instanceof Alarm){
           //queue changed alarm
           boolean succeeded = queue.offer(new AlarmQueueEntry((Alarm)o));
           if (succeeded){
               //if the observer has not already been notified
               if (!hasChanged()){
                   //inform it about a new queued alarm to be handled by it
                   setChanged();
                   notifyObservers(o);
               }               
           }
           else
           {
              Log.error("Failed to queue alarm " + o); 
           }
        }
    }
    /**
     * used to add an observer to alarm queue. Only one observer a time is permitted
     * @param observer 
     */
    @Override
    public void addObserver(Observer observer){
        if (this.observer == null){
            super.addObserver(observer);
            this.observer = observer;
        }
        else{
            Log.error("Attempt to add " + observer + " as an additional observer to the alarm queue. Current observer is " + this.observer);
        }
    }

    public ArrayBlockingQueue<AlarmQueueEntry> getQueue() {
        return queue;
    }
    
    void incrementPendingAlarmsCount(Alarm.Severity severity){
        switch(severity){
            case ALARM:
                synchronized(pendingAlarmsCountForSeverityAlarm){
                    pendingAlarmsCountForSeverityAlarm++;
                }
                break;                
            case WARNING:
                synchronized(pendingAlarmsCountForSeverityWarning){
                    pendingAlarmsCountForSeverityWarning++;
                }
                break;                
            case MESSAGE:
                synchronized(pendingAlarmsCountForSeverityMessage){
                    pendingAlarmsCountForSeverityMessage++;
                }
                break;                
        }
    }

    void decrementPendingAlarmsCount(Alarm.Severity severity){
        boolean inconsistent = false;
        switch(severity){
            case ALARM:
                synchronized(pendingAlarmsCountForSeverityAlarm){
                    pendingAlarmsCountForSeverityAlarm--;
                    if (pendingAlarmsCountForSeverityAlarm < 0){
                        pendingAlarmsCountForSeverityAlarm = 0;//force counter to '0'
                        inconsistent = true;
                    }
                }
                if (inconsistent){
                   Log.error("counter for pending alarms of severity ALARM is inconsistent"); 
                }
                break;                
            case WARNING:
                synchronized(pendingAlarmsCountForSeverityWarning){
                    pendingAlarmsCountForSeverityWarning--;
                    if (pendingAlarmsCountForSeverityWarning < 0){
                        pendingAlarmsCountForSeverityWarning = 0;//force counter to '0'
                        inconsistent = true;
                    }
                }
                if (inconsistent){
                   Log.error("counter for pending alarms of severity WARNING is inconsistent"); 
                }
                break;                
            case MESSAGE:
                synchronized(pendingAlarmsCountForSeverityMessage){
                    pendingAlarmsCountForSeverityMessage--;
                    if (pendingAlarmsCountForSeverityMessage < 0){
                        pendingAlarmsCountForSeverityMessage = 0;//force counter to '0'
                        inconsistent = true;
                    }
                }
                if (inconsistent){
                   Log.error("counter for pending alarms of severity MESSAGE is inconsistent"); 
                }
                break;                
        }
    }    
    
    public boolean isAtLeastOneAlarmPending(Alarm.Severity severity){
        boolean pending = false;
        switch(severity){
            case ALARM:
                pending = pendingAlarmsCountForSeverityAlarm > 0;
                break;
            case WARNING:
                pending = pendingAlarmsCountForSeverityWarning > 0;
                break;
            case MESSAGE:
                pending = pendingAlarmsCountForSeverityMessage > 0;
                break;
        }
        return pending;
    }

}
