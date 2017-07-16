/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : Fireable.java
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

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * base class of all event, which might occur during the life time of an elbfisch application
 * @author berndschuster
 */
public abstract class Fireable{
    static Logger Log = LoggerFactory.getLogger("jpac.Fireable");

    protected   boolean                  fired;
    private     boolean                  processed;
    private     AbstractModule           observingModule;
    private     long                     cycleNumber;
    private     boolean                  notified;
    private     ProcessException         processException;

    public Fireable(){
        initStates();
        observingModule         = null;
    }

    protected void initStates(){
        fired                         = false;
        processed                     = false;
        cycleNumber                   = 0;
        notified                      = false;
        setProcessException(null);
    }

    /**
     * is called by jPac in every cycle to check, if the fire condition occured
     * the fire condition must be true for at least one call. on occurrence it is latched by the Fireable, until it is processed.
     * Must not be called by the application directly.
     * @return true, if the fire condition occured
     * @throws ProcessException if the application code produces an process exception. It is passed onto the awaiting module.
     */
    public abstract boolean fire() throws ProcessException;

    /**
     * checks, if the event has been fired
     * @return true: event is fired
     * @throws ProcessException 
     */
    public boolean evaluateFiredCondition() throws ProcessException {
        //fired state persist for the whole process cycle
        //even though the fire-condition may change
        if (!fired){
            try{
                fired = fire() || isProcessExceptionThrown();
            }
            catch(ProcessException exc){
                //store the exception, for further handling by the observing module(s)
                setProcessException(exc);
                fired = true;
                //and rethrow it
                throw exc;
            }
        }
        return fired;
    }
    
    /**
     * used to check, if a fireable has been fired. Keeps its state, until reset() is called.
     * @return true: the Fireable has been fired
     * @throws ProcessException 
     */
    public boolean isFired(){
        return fired;
    }
    

//    public void setProcessed(){
//        //decrement the count of fired event, which are not processed
//        FireableList fpel = getObservingModule().getJPac().getFiredEventList();
//        if (fpel.contains(this)){
//            fpel.decActiveEventsCount();
//        }
//        //this event is processed
//        if (Log.isDebugEnabled()) Log.debug("fireable " + this + " processed by " + getObservingModule().getName());
//        processed = true;
//    }

    public void notifyObservingModule() throws InconsistencyException {
        //a module can be notified only once per cycle
        //this may occur due to a regular fire() condition of this process event
        //or by of one or more process exceptions
        //related to the observing module
        if (!notified){
            //remove fireable from list of awaited events
            //unregister();
            setCycleNumber(getObservingModule().getJPac().getCycleNumber());
            if (Log.isDebugEnabled()){
                String logString = getCycleNumber() + " firing " + this + "(" + this.hashCode() + ") at module " + getObservingModule().getName();
                if (getProcessException() != null){
                    logString += " caused by " + getProcessException();
                } else if (this instanceof ProcessEvent && ((ProcessEvent)this).isTimedout()){
                    logString += " caused by timeout";                    
                }
                Log.debug(logString);
            }
            notified = true;
            //tell the module, that it has been awakened by an event.
            getObservingModule().setAwakenedByProcessEvent(true);
            synchronized(this){
                //increment the count of modules awakened inside the current cycle
                getObservingModule().getJPac().incrementAwakenedModulesCount();
                notify();
            }
        }
    }


    public void register() throws InconsistencyException{
        //register myself as an active waiting event
        if (!getObservingModule().getJPac().getAwaitedEventList().add(this)){
            //throw new InconsistencyException(this + " already registered in list of awaited events !");
            Log.error(this + " already registered in list of awaited events !");
        }
    }

    public void unregister() throws InconsistencyException{
        //unregister myself as an active waiting event
        if (!getObservingModule().getJPac().getAwaitedEventList().remove(this)){
            throw new InconsistencyException(this + " not registered in list of awaited events !");            
        }
    }

    /**
     * used to reset the Fireable to its initial state.
     */
    public void reset(){
        initStates();
    }

    /**
     * used to set a monitor on a Fireable. If it is fired, a MonitorException is thrown to the monitoring module
     * if the Firable is already monitored, this call will be ignored. BUT: There might be situations, where jPac is unable
     * to detect, that this Fireable or a certain complex fire condition is already monitored. To avoid this reliably, aspecially inside of
     * loops, instantiate the fireable once outside the loop and call monitor() upon this instance.
     * @throws InconsistencyException thrown, if an jpac internal problem arose 
     */
    public void monitor() throws InconsistencyException{
        AbstractModule module = (AbstractModule)Thread.currentThread();
        //reinitialize state var's
        reset();
        //store the module observing me
        setObservingModule(module);
        //check, if I am already monitored by this module
        boolean fireableAlreadyRegistered = false; 
        for(Fireable f: module.getMonitoredEvents()){
            if(this.equals(f) || this.equalsCondition(f)){
                fireableAlreadyRegistered = true;
                break;
            }
        }
        if(!fireableAlreadyRegistered){
            //I'm not monitored by this module yet
            //register myself as an active waiting event
            if (module.getMonitoredEvents().size() >= module.MAXNUMBEROFMONITORS){
                throw new InconsistencyException("maximum number of monitors reached. Use unmonitor() for monitors which are no longer used");
            }
            module.getMonitoredEvents().add(this);
        }
    }

    /**
     * used to reset a monitor on a Fireable.
     */
    public void unmonitor(){
        if (getObservingModule() != null){
            getObservingModule().getMonitoredEvents().remove(this);
        }
    }

    protected void setObservingModule(Thread aThread) throws InconsistencyException{
        if (aThread != null && (observingModule != null && observingModule != aThread)){//TODO check this
            throw new InconsistencyException("Event " + this + " is already observed by module " + observingModule);
        }
        observingModule = (AbstractModule)aThread;
    }

    protected AbstractModule getObservingModule() {
        return observingModule;
    }

    public boolean isProcessed() {
        return processed;
    }

    @Override
    public String toString(){
        return this.getClass().getSimpleName();
    }

    /**
     * @return the cycleNumber
     */
    public long getCycleNumber() {
        return cycleNumber;
    }

    /**
     * @param cycleNumber the cycleNumber to set
     */
    private void setCycleNumber(long cycleNumber) {
        this.cycleNumber = cycleNumber;
    }

    /**
     * @return the processException
     */
    protected ProcessException getProcessException() {
        return processException;
    }

    /**
     * @param processException the processException to set
     */
    protected void setProcessException(ProcessException processException) {
        this.processException = processException;
    }

    protected boolean isProcessExceptionThrown(){
        return processException != null;
    }
    
    /**
     * used, to check, if this fireable implements the identical fire condition as f.
     * CAUTION: if not overwritten, equalsCondition() returns false by default
     * @param f the Fireable to check
     * @return true, if both fire conditions match
     */
    protected boolean equalsCondition(Fireable f){
        //Note: leave this code herein unchanged. It is implemented this way designedly.
        return false;
    }
}
