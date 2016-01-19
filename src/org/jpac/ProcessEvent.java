/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : ProcessEvent.java
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

import java.util.ArrayList;

/**
 * base class for all process events which can be await()ed by modules
 */
public abstract class ProcessEvent extends Fireable{
    final static int                   STACKTRACEOFFSET = 3;
    
    private   boolean                  timedout;
    private   boolean                  timeoutActive;
    private   boolean                  emergencyStopOccured;
    private   String                   emergencyStopCause;
    private   boolean                  shutdownRequested;
    private   boolean                  monitoredEventOccured;
    private   long                     timeoutPeriod;
    private   long                     timeoutNanoTime;
    private   String                   statusString;
    private   ArrayList<Fireable>      monitoredEvents;
    
    private   int                      tracePoint;//debug

    public ProcessEvent(){
        super();
        initStates();
    }

    @Override
    protected void initStates(){
        super.initStates();
        timedout                = false;
        timeoutActive           = false;
        emergencyStopOccured    = false;
        emergencyStopCause      = null;
        shutdownRequested       = false;
        monitoredEventOccured   = false;
        monitoredEvents         = null;
    }

    /**
     * used to check, if an process event is currently fired
     * @return true: the process event is currently fired
     * @throws ProcessException  
     */
    @Override
    public boolean evaluateFiredCondition() throws ProcessException {
        //fired state persist for the whole process cycle
        //even though the fire-condition may change
        if (!fired){
            //check monitored events first ...
            if (monitoredEvents != null){
                //check whole list of monitored events
                for(Fireable f : monitoredEvents){
                    if (f != this && f.evaluateFiredCondition()){
                        //if at least one is fired
                        //prepare notification of the observing module
                        monitoredEventOccured = true;
                        fired                 = true;
                        if (Log.isDebugEnabled()) Log.debug(this + " fired caused by monitored event : " + f);
                    }
                }
            }
            //... then check own fire condition
            super.evaluateFiredCondition();
        }
        return fired;
    }
    
    /**
     * used to await the process event. Can be called inside the work() method of a module. The module is suspended, until the awaited process event
     * occured (is fired).
     * @param nanoseconds: maximum period of time to wait [ns]. In module contexts use helper values ns,millis,ms,sec to specify: 1000 * ms, 1 * sec, 1000 * millis
     * @return the process event itself
     * @throws EventTimedoutException thrown, if a time out occured
     * @throws EmergencyStopException thrown, if one of the other modules has encountered a emergency stop condition
     * @throws ShutdownRequestException thrown, if one of the other modules or the OS requests a shutdown of the elbfisch application
     * @throws ProcessException thrown, if an arbitrary process exception has been thrown
     * @throws OutputInterlockException thrown, if postCheckInterlocks() encountered an error condition
     * @throws InputInterlockException thrown, if preCheckInterlocks() encountered an error condition
     * @throws MonitorException thrown, if a monitored event occured
     * @throws InconsistencyException thrown, if JPac encountered an internal error
     */
    public synchronized ProcessEvent await(long nanoseconds) throws EventTimedoutException, EmergencyStopException, ShutdownRequestException, ProcessException, OutputInterlockException, InputInterlockException, MonitorException, InconsistencyException{
        awaitImpl(true, nanoseconds, true);
        if (isTimedout())
            throw new EventTimedoutException(this);
        return this;
    }

    /**
     * used to await the process event. Can be called inside the work() method of a module. The module is suspended, until the awaited process event
     * occured (is fired).
     * @return the process event itself
     * @throws EventTimedoutException thrown, if a time out occured
     * @throws EmergencyStopException thrown, if one of the other modules has encountered a emergency stop condition
     * @throws ShutdownRequestException thrown, if one of the other modules or the OS requests a shutdown of the elbfisch application
     * @throws ProcessException thrown, if an arbitrary process exception has been thrown
     * @throws OutputInterlockException thrown, if postCheckInterlocks() encountered an error condition
     * @throws InputInterlockException thrown, if preCheckInterlocks() encountered an error condition
     * @throws MonitorException thrown, if a monitored event occured
     * @throws InconsistencyException thrown, if JPac encountered an internal error
     */
    public synchronized ProcessEvent await() throws EmergencyStopException, ShutdownRequestException, ProcessException, OutputInterlockException, InputInterlockException, MonitorException, InconsistencyException{
        //do not propagate EventTimedoutException because
        //timeout conditions are not handled here
        awaitImpl(false, 0, true);
        return this;
    }

    private ProcessEvent awaitImpl(boolean withTimeout, long nanoseconds, boolean noteStatus) throws ShutdownRequestException, EmergencyStopException, ProcessException, OutputInterlockException, InputInterlockException, MonitorException, InconsistencyException{
        StackTraceElement[] stackTrace;
        StackTraceElement   stackTraceElement; 
        CharString[]        stacktraceSignals;
        int                 traceLength;
        int                 i,j;
        String              methodName; 
        
        if (!(Thread.currentThread() instanceof AbstractModule)){
            throw new InconsistencyException("ProcessEvents cannot be awaited outside the work() context of modules");
        }
        AbstractModule module = (AbstractModule)Thread.currentThread();
        //reinitialize state var's
        reset();
        //if notation of the modules status is requested, do it here
        if (noteStatus){
            module.getStatus().enter(getStatusString());
        }
        //check for interlock conditions produced by the observing module
        module.postCheckInterlocks();
        //get and reset list of events monitored by the module
        module.resetMonitoredEvents();
        monitoredEvents = module.getMonitoredEvents();
        //store the module awaiting me
        setObservingModule(module);
        module.setAwaitedEvent(this);
        //propagagate current trace point
        stackTrace         = module.getStackTrace();
        stacktraceSignals  = module.getStackTraceSignals();
        traceLength        = stackTrace.length;
        i                  = 0;
        j                  = traceLength;
        //trace back to Module.work()
        do{j--;} while(j > 0 && !stackTrace[j].getMethodName().equals("work"));
        if (j >= 0){
            do{
                stackTraceElement = stackTrace[j]; 
                methodName        = stackTraceElement.getMethodName();
                stacktraceSignals[i].set(stackTraceElement.getClassName() + "." + methodName + "(): " + stackTraceElement.getLineNumber());
                i++;j--;
            }
            while(j >= STACKTRACEOFFSET && i < stacktraceSignals.length);
        }
        while(i < stacktraceSignals.length){
            stacktraceSignals[i++].invalidate();
        }
        //register myself as an active waiting event
        register();
        //prepare timeout related vars
        setTimeoutPeriod(nanoseconds);
        setTimeoutNanoTime(JPac.getInstance().getExpandedCycleNanoTime() + nanoseconds);
        timeoutActive = withTimeout;
        //now lay observing module to sleep until this event occurs
        synchronized(this){
            if (module.isAwakenedByProcessEvent()){
                //tell the automation controller that one of the modules, awakened by an process event
                //has come to an end for this cycle
                tracePoint = 1;
                module.setAwakenedByProcessEvent(false);
                tracePoint = 2;
                module.storeSleepNanoTime();
                tracePoint = 3;
                module.getJPac().indicateCheckBack(this);
                tracePoint = 4;
            }
            else{
                tracePoint = 99;
            }
            //wait, until an ProcessEvent or a timeout occurs
            do{
                try {wait();
                } 
                catch (InterruptedException ex) {
                    Log.info("InterruptedException occured for " + this);
                }
              }
            while(!isTimedout() && !isFired() && !isEmergencyStopOccured() && !isShutdownRequested() && !isProcessExceptionThrown());
            module.storeWakeUpNanoTime();
            module.resetSleepNanoTime();// invalidate sleepNanoTime
        }
        //if notation of the status was requested on call, remove it here
        if (noteStatus){
            module.getStatus().leave();
        }
        //no module is awaiting me
        //setObservingModule(null);
        //handle exceptions in order of relevance
        if (isShutdownRequested())
            throw new ShutdownRequestException();
        if (isEmergencyStopOccured())
            throw new EmergencyStopException(JPac.getInstance().getEmergencyStopExceptionCausedBy());
        if (isMonitoredEventOccured())
            throw new MonitorException(monitoredEvents);
        if (isProcessExceptionThrown()){
            if (getProcessException() instanceof InEveryCycleDoException){
                throw new InEveryCycleDoException(getProcessException().getCause());
            } else if (getProcessException() instanceof AsynchronousTaskException){
                throw new AsynchronousTaskException(getProcessException().getCause());                
            } else{
                throw new ProcessException(getProcessException());
            }
        }
        module.setAwaitedEvent(null);
        //check for incoming interlock conditions to be handled by the observing module
        module.preCheckInterlocks();
        tracePoint = 0;
        return this;
    }

    /**
     * used to implement a conjunctive process event inline
     * @param anotherProcessEvent an process event which is "and'ed" to this process event 
     * @return the combined (conjunctive event)
     */
    public ConjunctiveEvent and(ProcessEvent anotherProcessEvent){
        //create a new conjunctive event and add myself as the first event
        ConjunctiveEvent conjEvent = new ConjunctiveEvent(this);
        //now add the other event
        conjEvent.and(anotherProcessEvent);
        return conjEvent;
    }

    /**
     * used to implement a disjunctive process event inline
     * @param anotherProcessEvent an process event which is "or'ed" to this process event 
     * @return the combined (disjunctive event)
     */
    public DisjunctiveEvent or(ProcessEvent anotherProcessEvent){
        //create a new disjunctive event and add myself as the first event
        DisjunctiveEvent disjEvent = new DisjunctiveEvent(this);
        //now add the other event
        disjEvent.or(anotherProcessEvent);
        return disjEvent;
    }

    /**
     * used to implement a exclusive disjunctive process event inline
     * @param anotherProcessEvent an process event which is "xor'ed" to this process event 
     * @return the combined (exclusive disjunctive event)
     */
    
    public ExclusiveDisjunctiveEvent xor(ProcessEvent anotherProcessEvent){
        //create a new exclusive disjunctive event and add myself as the first event
        ExclusiveDisjunctiveEvent disjEvent = new ExclusiveDisjunctiveEvent(this);
        //now add the other event
        disjEvent.xor(anotherProcessEvent);
        return disjEvent;
    }

    /**
     * used to check, if the process event timed out, while has been await()'ed by a module
     * @return 
     */
    public boolean evaluateTimedOutCondition() {
        boolean localTimedout = false;
        if (timeoutActive){
            localTimedout = this.timedout || (JPac.getInstance().getExpandedCycleNanoTime() - getTimeoutNanoTime()) > 0;
            this.timedout = localTimedout;
        }
        else{
            localTimedout = false;
        }
        return localTimedout;
    }
    
    /**
     * 
     * @return true, if this process event has been timed out in this cycle 
     */
    public boolean isTimedout() {
        return this.timedout;
    }
    

    private String getStatusString(){
        if (statusString == null){
           statusString = "await(" + this.toString() + ")";
        }
        return statusString;
    }
    
    private long getTimeoutPeriod() {
        return timeoutPeriod;
    }

    private void setTimeoutPeriod(long timeoutPeriod) {
        this.timeoutPeriod = timeoutPeriod;
    }

    private long getTimeoutNanoTime() {
        return timeoutNanoTime;
    }

    private void setTimeoutNanoTime(long timeoutNanoTime) {
        this.timeoutNanoTime = timeoutNanoTime;
    }

    protected boolean isEmergencyStopOccured() {
        return emergencyStopOccured;
    }

    protected void setEmergencyStopOccured(boolean emergencyStopOccured) {
        this.emergencyStopOccured = emergencyStopOccured;
    }

    protected void setEmergencyStopCause(String cause) {
        this.emergencyStopCause = cause;
    }

    protected void setShutdownRequested(boolean shutdownRequested) {
        this.shutdownRequested = shutdownRequested;
    }

    protected boolean isShutdownRequested() {
        return this.shutdownRequested;
    }

    protected boolean isMonitoredEventOccured() {
        return this.monitoredEventOccured;
    }
    
    public int getTracePoint(){
        return this.tracePoint;
    }

    @Override
    public String toString(){
        return this.getClass().getSimpleName();
    } 
}
