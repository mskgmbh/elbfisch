/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : AbstractModule.java
 * VERSION   : $Revision: 1.10 $
 * DATE      : $Date: 2012/07/23 07:36:12 $
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
 * LOG       : $Log: AbstractModule.java,v $
 * LOG       : Revision 1.10  2012/07/23 07:36:12  nouza
 * LOG       : some corrections concerning inEveryCycleDo-exceptions
 * LOG       :
 * LOG       : Revision 1.9  2012/07/17 12:11:51  schuster
 * LOG       : histogramm tracing prohibited for first cycle
 * LOG       :
 * LOG       : Revision 1.8  2012/06/18 11:20:53  schuster
 * LOG       : introducing cyclic tasks
 * LOG       :
 * LOG       : Revision 1.7  2012/05/07 06:16:09  schuster
 * LOG       : initialize() eliminated
 * LOG       :
 * LOG       : Revision 1.6  2012/04/30 06:36:05  schuster
 * LOG       : introducing histogramm acquisition, some minor changes concerning toString()
 * LOG       :
 * LOG       : Revision 1.5  2012/04/24 08:40:58  schuster
 * LOG       : Error exception is logged
 * LOG       :
 * LOG       : Revision 1.4  2012/03/02 08:04:35  schuster
 * LOG       : startup procedure optimized
 * LOG       :
 * LOG       : Revision 1.3  2012/02/27 07:41:19  schuster
 * LOG       : some minor changes
 * LOG       :
 * LOG       : Revision 1.2  2012/02/23 11:11:51  schuster
 * LOG       : units made public
 * LOG       :
 */

package org.jpac;

import java.util.Iterator;
import java.util.Stack;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import org.apache.log4j.Logger;
import org.jpac.statistics.Histogramm;

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

    public enum Status {DORMANT,      //inital status, module is inactive
                        READY,        //module is ready to run and waits for the beginning of the next cycle
                        RUNNING,      //module is running
                        HALTED};      //module is halted
                          
    protected static Logger Log = Logger.getLogger("jpac.Module");

    public  static final long nanos =  1L;
    public  static final long micros = 1000L;
    public  static final long ms     = 1000000L;
    public  static final long millis = 1000000L;
    public  static final long sec    = 1000000000L;

    private JPac                  jPac;
    private int                   moduleIndex;
    private ProcessEvent          awaitedEvent;
    private ArrayList<Fireable>   monitoredEvents;//TODO how can multiple registration of the same event be avoided (for example in a loop) ????
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

    public AbstractModule(AbstractModule containingModule){
        super();
        this.containingModule   = containingModule;
        setSimpleName(getClass().getSimpleName());
        init();
    }

    public AbstractModule(AbstractModule containingModule, String name){
        super();
        this.containingModule   = containingModule;
        setSimpleName(name);
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

    public void shutdown(final int exitCode) throws ProcessException{
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
            new ImpossibleEvent().await();
        }
    }

    abstract protected void work() throws ProcessException;

    void setJPac(JPac jPac) {
        this.jPac = jPac;
    }

    protected JPac getJPac() {
        return this.jPac;
    }

    /**
     * @return the awaitedEvent
     */
    public ProcessEvent getAwaitedProcessEvent() {
        return awaitedEvent;
    }

    /**
     * @param awaitedEvent the awaitedEvent to set
     */
    public void setAwaitedEvent(ProcessEvent awaitedEvent) {
        this.awaitedEvent = awaitedEvent;
    }

    /**
     * @return the simpleName
     */
    public String getSimpleName() {
        return simpleName;
    }

    /**
     * 
     * @return status
     */
    public StatusStack getStatus(){
        return status;
    }

    public AbstractModule getContainingModule(){
        return this.containingModule;
    }
    
    /**
     * @param simpleName the simpleName to set
     */
    protected void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }

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

    abstract protected void preCheckInterlocks() throws InputInterlockException;
    abstract protected void postCheckInterlocks() throws OutputInterlockException;

    /**
     * @return the moduleIndex
     */
    public int getModuleIndex() {
        return moduleIndex;
    }
    
    public void setDebugIndex(int index){
        this.debugIndex = index;
    }

    public int getDebugIndex(){
        return this.debugIndex;
    }
        
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
    
    abstract protected void inEveryCycleDo() throws ProcessException;

}
