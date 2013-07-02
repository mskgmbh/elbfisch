/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : AbstractModule.java
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
 *
 */

package org.jpac;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;
import org.apache.log4j.Logger;
import org.jpac.statistics.Histogramm;

/**
 * base class of elbfisch modules
 * @author berndschuster
 */
public abstract class AbstractModule extends Thread{

    public class StatusStack{

        private Stack   stack   = new Stack();
        private boolean overrun = false;

        public int enter(Object subState){
            overrun = stack.size() > 10;
            if  (!overrun){
                stack.push(subState);
            }
            if (Log.isDebugEnabled()) Log.debug("entering status: " + this);
            return stack.size() - 1;
        }

        public void leave(){
            stack.pop();
        }

        public void resume(int statusIndex){
            for (int i = stack.size() - 1; i > statusIndex; i--){
                stack.remove(i);
            }
        }

        public void reset(){
            stack.remove(0);
        }

        public boolean isOverrun(){
            return overrun;
        }

        @Override
        public String toString(){
            StringBuffer qs = new StringBuffer();
            for(Iterator is = stack.iterator(); is.hasNext();){
                qs.append((is.next()).toString());
                if (is.hasNext()){
                    qs.append('-');
                }
            }
            if (overrun){
                qs.append("!!!! status stack overrun !!!!");
            }
            return qs.toString();
        }
    }

    public enum Status {/**initial state of a module*/
                        DORMANT,
                        /**module is ready to run and waits for the beginning of the first jpac cylce*/
                        READY,
                        /**module is running*/
                        RUNNING,
                        /**module has stopped running, it is halted*/
                        HALTED};      //module is halted
                          
    protected static Logger Log = Logger.getLogger("jpac.Module");

    /**used to denote a span of time in nanoseconds*/
    public  static final long nanos =  1L;
    /**used to denote a span of time in microseconds*/
    public  static final long micros = 1000L;
    /**used to denote a span of time in milliseconds*/
    public  static final long ms     = 1000000L;
    /**used to denote a span of time in milliseconds*/
    public  static final long millis = 1000000L;
    /**used to denote a span of time in seconds*/
    public  static final long sec    = 1000000000L;

    private JPac                  jPac;
    private int                   moduleIndex;
    private ProcessEvent          awaitedEvent;
    private ArrayList<Fireable>   monitoredEvents;
    private String                simpleName;
    private String                qualifiedName;

    protected StatusStack         status;
    private   AbstractModule      containingModule;

    private  boolean              awakenedByProcessEvent;
    private  long                 wakeUpNanoTime;
    private  long                 sleepNanoTime;
    
    private  int                  debugIndex;
    
    private  Histogramm           histogramm;
    private  boolean              requestingEmergencyStop;
    private  boolean              inEveryCycleDoActive;

    /**
     * used to construct a module
     * @param containingModule null  : module is the top most module, which by definition must contain all other modules of the application.
     *                         <>null: module, which instantiated this module and therefor is the containing module
     * @param name: the short name of the module. This name will be supplemented by the name of all containing module in a given hierarchical order
     */
    public AbstractModule(AbstractModule containingModule, String name){
        super();
        this.containingModule   = containingModule;
        setSimpleName(name);
        init();
    }

    /**
     * used to construct a module. It receives the name of it's class
     * @param containingModule the containing (instantiating module) if null, the module is the top most module, which by definition must contain all other modules of the application
     */
    public AbstractModule(AbstractModule containingModule){
        super();
        this.containingModule   = containingModule;
        setSimpleName(getClass().getSimpleName());
        init();
    }

    protected final void init(){
        status                       = new StatusStack();
        awaitedEvent                 = null;
        awakenedByProcessEvent       = false;
        wakeUpNanoTime               = 0L;
        sleepNanoTime                = 0L;
        debugIndex                   = 0;
        inEveryCycleDoActive         = false;
        
        //retrieve instance of the automation controller
        setJPac(JPac.getInstance());
        //register myself as an active module and retrieve my individual module index
        moduleIndex = getJPac().register(this);
        //prepare qualified name for this instance and its subinstances
        setQualifiedName(generateQualifiedName());
        //store fully qualified name for this instance
        setName(getQualifiedName());
        //let the application class initialize its modules and signals
        setPriority(MAX_PRIORITY - 1);
        histogramm  = new Histogramm(getJPac().getCycleTime());
    }

    /**
     * is not called directly by an elbfisch application
     */
    @Override
    public void run() {
        try {
            status.enter(Status.READY);
            //enter initial wait state: wait, until jPac signals the start of the first cycle
            new NextCycle().await();
            //now we are ready to run the automation application
            //willy go ...
            status.leave();
            status.enter(Status.RUNNING);
            work();
        } 
        catch (Exception ex) {
            if (! (ex instanceof ShutdownRequestException)){
                Log.error("Error: ",ex);
            }
        }
        catch (Error ex) {
            Log.error("Error: ",ex);
        }            
        finally{
            status.resume(0);
            status.leave();
            status.enter(Status.HALTED);
            //stop invocation of inEveryCycleDo()
            enableCyclicTasks(false);
            if (isAwakenedByProcessEvent()){
                //tell the automation controller that this module
                //has come to an end
                setAwakenedByProcessEvent(false);
                try{
                    getJPac().decrementAwakenedModulesCount(null);
                } 
                catch(InconsistencyException exc){
                    Log.error("Error: ", exc);
                }
            }
        }
    }

    /**
     * used to shutdown the the elbfisch application. If called by one of the modules, every module will receive 
     * a ShutdownRequestedException in the next following cycle.
     * @param exitCode exit code returned to the system (OS)
     * @throws ProcessException thrown, if an elbfisch specific condition arises
     */
    public void shutdown(final int exitCode) throws ProcessException{
        boolean shutdownInitiated = false;
        if (Log.isInfoEnabled()) Log.debug("SHUTDOWN OF APPLICATION INVOKED BY " + this);
        Thread shutdownThread = new Thread(){
            @Override
            public void run() {
                System.exit(exitCode);
            }
        };
        shutdownThread.start();
        if (Thread.currentThread() instanceof AbstractModule){
            //wait, until the automation controller signals its shutdown
            ImpossibleEvent infinity = new ImpossibleEvent();
            do{
                try{
                    infinity.await();
                }
                catch(ProcessException exc){
                    shutdownInitiated = exc instanceof ShutdownRequestException;
                }
            }
            while(!shutdownInitiated);
        }
    }

    /**
     * central working method of the module. All real time actions of the module are implemented herein.
     * @throws ProcessException if an arising process exception is not handled by the application of work(), the module will stop running
     * and perform some default handling
     */ 
    abstract protected void work() throws ProcessException;

    void setJPac(JPac jPac) {
        this.jPac = jPac;
    }

    protected JPac getJPac() {
        return this.jPac;
    }

    public ProcessEvent getAwaitedProcessEvent() {
        return awaitedEvent;
    }

    public void setAwaitedEvent(ProcessEvent awaitedEvent) {
        this.awaitedEvent = awaitedEvent;
    }

    /**
     * @return the simple name of the module
     */
    public String getSimpleName() {
        return simpleName;
    }

    /**
     * @return status used to retrieve the status stack of the module.
     * a module can enter.
     */
    public StatusStack getStatus(){
        return status;
    }

    /**
     * @return the containing module
     */
    public AbstractModule getContainingModule(){
        return this.containingModule;
    }
    
    protected void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }

    /**
     * 
     * @return the qualified name of the module which is dot separated string of the modules simple name and the names 
     * of all containing modules
     */
    public String getQualifiedName(){
        return qualifiedName;
    }

    protected void setQualifiedName(String qualifiedName){
        this.qualifiedName = qualifiedName;
    }

    private String generateQualifiedName(){
        String name = null;
        if (containingModule != null){
            name = containingModule.getQualifiedName() + '.' + getSimpleName();
        }
        else{
            name = getSimpleName();
        }
        return name;
    }

    protected ArrayList<Fireable> getMonitoredEvents(){
        if (this.monitoredEvents == null){
            this.monitoredEvents = new ArrayList<Fireable>(10);
        }
        return this.monitoredEvents;
    }

    protected void resetMonitoredEvents(){
        if (this.monitoredEvents != null){
            for(Fireable f: monitoredEvents){
                f.reset();
            }
        }
    }

    /**
     * used to acknowledge an emergency stop condition
     */
    public void acknowledgeEmergencyStop(){
        if (Log.isInfoEnabled()) Log.debug("emergency stop acknowledged by " + this);
        jPac.acknowledgeEmergencyStop();
    }
   
    public long getSleepNanoTime(){
        return this.sleepNanoTime;
    }

    public long getWakeUpNanoTime(){
        return this.wakeUpNanoTime;
    }

    public void storeSleepNanoTime(){
        this.sleepNanoTime = System.nanoTime();
        if (getJPac().getCycleNumber() > 1L){
           //module has awakened by a process event at least once
           //store its time consumption for this cycle
           histogramm.update(this.sleepNanoTime - this.wakeUpNanoTime);
        }
    }

    public void resetSleepNanoTime(){
        this.sleepNanoTime = 0L;
    }

    public void storeWakeUpNanoTime(){
        this.wakeUpNanoTime = System.nanoTime();
    }
    
    protected void setAwakenedByProcessEvent(boolean awakenedByProcessEvent) {
        this.awakenedByProcessEvent = awakenedByProcessEvent;
    }

    protected boolean isAwakenedByProcessEvent() {
        return awakenedByProcessEvent;
    }

    @Override
    public String toString(){
        return getName();
    }
    /**
     * Is used to check if pre conditions before invoking await() are fulfilled.
     * Is called, whenever a module returns from ProcessEvent.await().
     * @throws InputInterlockException will be thrown by ProcessEvent.await(), if an interlock violation occurs
     */
    abstract protected void preCheckInterlocks() throws InputInterlockException;

    /**
     * Is used to check if all post conditions are fulfilled when calling a ProcessEvent.await()
     * @throws OutputInterlockException will be thrown by ProcessEvent.await(), if an interlock violation occurs
     */
    abstract protected void postCheckInterlocks() throws OutputInterlockException;

    public int getModuleIndex() {
        return moduleIndex;
    }
    
    public void setDebugIndex(int index){
        this.debugIndex = index;
    }

    public int getDebugIndex(){
        return this.debugIndex;
    }
        
    /**
     * used to start a module. Before starting itself it starts all containing modules
     */
    @Override
    public void start(){
        //start myself on my own thread
        super.start();
        //and wait, until my thread enters its initial wait state (see run())
        while(getState() != State.WAITING);
        if (containingModule == null){
            //if this module is the top most one
            //all engaged modules have been properly prepared to run.
            //Now it is time to tell jPac to start cycling
            jPac.startCycling();
        }        
    }
    
    public Histogramm getHistogramm(){
        return histogramm;
    }
    
    public void setRequestingEmergencyStop(boolean request){
        this.requestingEmergencyStop = request;
    }

    public boolean isRequestingEmergencyStop(){
        return requestingEmergencyStop;
    }
    
    void invokeInEveryCycleDo(){
        if (inEveryCycleDoActive){
            try{
                inEveryCycleDo();
            }
            catch(EmergencyStopException exc){
                //do nothing, all modules will be informed by jPac
            }
            catch(ProcessException exc){
                getAwaitedProcessEvent().setProcessException(new InEveryCycleDoException(exc));
                inEveryCycleDoActive = false;
            }
            catch(UnsupportedOperationException exc){
                //inEveryCycleDo not implemented in this module
                //never call it again
                inEveryCycleDoActive = false;                
            }
            catch(Exception exc){
                getAwaitedProcessEvent().setProcessException(new InEveryCycleDoException(exc));
                inEveryCycleDoActive = false;
            }
            catch(Error exc){
                getAwaitedProcessEvent().setProcessException(new InEveryCycleDoException(exc));                
                inEveryCycleDoActive = false;
            }
        }
    }
    
    protected void enableCyclicTasks(boolean enable){
        this.inEveryCycleDoActive = enable;
    }
    
    /**
     * is invoked by jPac in every cycle to let it handle application specific code, which must be
     * run continously. the application code inside inEveryCycleDo() is stateless. 
     * It must not contain any ProcessEvent.await() calls.
     */
    abstract protected void inEveryCycleDo() throws ProcessException;

}
