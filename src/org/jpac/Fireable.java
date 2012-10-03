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

import org.apache.log4j.Logger;

/**
 * base class of all event, which might occur during the life time of an elbfisch application
 * @author berndschuster
 */
public abstract class Fireable{
    static Logger Log = Logger.getLogger("jpac.Fireable");

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
     * used to check, if a fireable has been fired. Keeps its state, until reset() is called.
     * @return true: the Fireable has been fired
     * @throws ProcessException 
     */
    public boolean isFired() throws ProcessException {
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

    public void notifyObservingModule() {
        //a module can be notified only once per cycle
        //this may occur due to a regular fire() condition of this process event
        //or by of one or more process exceptions
        //related to the observing module
        if (!notified){
            if (observingModule instanceof SimulationModule){
                //if calling module is a simulation module
                //remove me from the process event list for the simulation modules
                getObservingModule().getJPac().getAwaitedSimEventList().remove(this);
            }
            else if(observingModule instanceof Module){
                //... else from the regular process event list
                getObservingModule().getJPac().getAwaitedEventList().remove(this);
            }
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
            //increment the count of modules awakened inside the current cycle
            //and tell the module, that it has been awakened by an event.
            getObservingModule().setAwakenedByProcessEvent(true);
            synchronized(this){
                getObservingModule().getJPac().incrementAwakenedModulesCount();
                notify();
            }
        }
    }


    public void register(){
        //register myself as an active waiting event
        if (observingModule instanceof SimulationModule){
            //if calling module is a simulation module
            //store the event inside the simulation process event list
            synchronized(getObservingModule().getJPac().getAwaitedSimEventList()){
                getObservingModule().getJPac().getAwaitedSimEventList().add(this);
            }
        }
        else if (observingModule instanceof Module){
            //if calling moduel is a regualar module
            //store the event inside the regular process event list
            synchronized(getObservingModule().getJPac().getAwaitedEventList()){
                getObservingModule().getJPac().getAwaitedEventList().add(this);
            }
        }
    }

    public void unregister(){
        //register myself as an active waiting event
        if (observingModule instanceof SimulationModule){
            //if calling module is a simulation module
            //store the event inside the simulation process event list
            synchronized(getObservingModule().getJPac().getAwaitedSimEventList()){
                getObservingModule().getJPac().getAwaitedSimEventList().remove(this);
            }
        }
        else if (observingModule instanceof Module){
            //if calling moduel is a regualar module
            //store the event inside the regular process event list
            synchronized(getObservingModule().getJPac().getAwaitedEventList()){
                getObservingModule().getJPac().getAwaitedEventList().remove(this);
            }
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
            if(this.equalsCondition(f)){
                fireableAlreadyRegistered = true;
                break;
            }
        }
        if(!fireableAlreadyRegistered){
            //I'm not monitored by this module yet
            //register myself as an active waiting event
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
     * 
     * @param f the Fireable to check
     * @return true, if both fire conditions match
     */
    protected boolean equalsCondition(Fireable f){
        //Note: leave this code herein unchanged. It is implemented this way designedly.
        throw new UnsupportedOperationException("must be implemented if this fireable is to be monitored");
    }
}
