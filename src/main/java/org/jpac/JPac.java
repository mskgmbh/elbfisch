/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : JPac.java
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

import org.eclipse.milo.opcua.stack.core.Stack;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.jpac.configuration.BooleanProperty;
import org.jpac.configuration.Configuration;
import org.jpac.configuration.DoubleProperty;
import org.jpac.configuration.IntProperty;
import org.jpac.configuration.LongProperty;
import org.jpac.configuration.Property;
import org.jpac.configuration.StringProperty;
import org.jpac.console.TelnetService;
import org.jpac.opc.Opc;
import org.jpac.opc.OpcUaService;
import org.jpac.snapshot.Snapshot;
import org.jpac.statistics.Histogram;

/**
 * central runtime engine of JPac
 * @author berndschuster
 */
public class JPac extends Thread {
    static Logger Log = LoggerFactory.getLogger("jpac.JPac");

    private     final int       OWNMODULEINDEX                       = 0;
    private     final long      DEFAULTCYCLETIME                     = 100000000L; // 100 ms
    private     final long      DEFAULTCYCLETIMEOUTTIME              = 1000000000L;// 1 s
    private     final int       MAXSHUTDOWNTIME                      = 2000;       // 2 s
    private     final int       EXITCODENORMALSHUTDOWN               = 0;
    private     final int       EXITCODEINITIALIZATIONERROR          = 100;
    private     final int       EXITCODEINTERNALERROR                = 101;
    private     final long      DEFAULTSHUTDOWNTIMEOUTTIME           = 5000000000L; //5 s
    private     final String    OPCACCESSLEVELNONE                   = "NONE";
    private     final String    OPCACCESSLEVELREADONLY               = "READ_ONLY";
    private     final String    OPCACCESSLEVELREADWRITE              = "READ_WRITE";
    private     final int       CONSOLESERVICEDEFAULTPORT            = 8023;
    private     final String    DEFAULTCONSOLESERVICEBINDADDRESS     = "localhost";
    
    private     final String    CFGDIR                               = "./cfg";
    private     final String    DATADIR                              = "./data";
    
    public enum CycleMode{OneCycle, Bound, LazyBound, FreeRunning}
    
    public      enum    Status{initializing, ready, running, halted};

    protected   static JPac  instance   = null;

    private     int                    tracePoint;//used for internal trace purposes
    private     Set<Fireable>          awaitedEventList;
    private     Set<Fireable>          awaitedSimEventList;
    private     Set<Fireable>          firedEventList;
    private     long                   minRemainingCycleTime;
    private     long                   maxRemainingCycleTime;
    private     long                   expectedCycleEndTime;
    private     long                   shutdownRequestTime;
    private     long                   cycleStartTime;
    private     long                   expansionTime;
    private     long                   numberOfCyclesExceeded;
    private     long                   cycleNumber;
    private     Status                 status;
    private     boolean                emergencyStopRequested;
    private     boolean                emergencyStopActive;
    private     EmergencyStopException emergencyStopCausedBy;
    private     boolean                emergencyStopIsToBeThrown;

    private     boolean                readyToShutdown;

    private     Semaphore              startCycle;
    private     Semaphore              cycleEnded;
    
    private     boolean                shutdownPending;
    
    private     final List<Runnable>   synchronizedTasks;
    private     final List<CyclicTask> cyclicTasks;
    
    private LongProperty    propCycleTime;
    private LongProperty    propCycleTimeoutTime;
    private StringProperty  propCycleMode;
    private BooleanProperty propRunningStandalone;
    private BooleanProperty propEnableTrace;
    private BooleanProperty propPauseOnBreakPoint;
    private IntProperty     propTraceTimeMinutes;
    private BooleanProperty propRemoteSignalsEnabled;
    private IntProperty     propRemoteSignalPort;
    private StringProperty  propHistogramFile;
    private LongProperty    propCyclicTaskShutdownTimeoutTime;
    private LongProperty    propMaxShutdownTime;
    private BooleanProperty propOpcUaServiceEnabled;
    private IntProperty     propOpcUaServicePort;
    private StringProperty  propOpcUaServiceName;
    private DoubleProperty  propOpcUaMinSupportedSampleInterval;
    private StringProperty  propOpcUaDefaultAccessLevel;
    private BooleanProperty propConsoleServiceEnabled;
    private IntProperty     propConsoleServicePort;
    private StringProperty  propConsoleBindAddress;
    private BooleanProperty propGenerateSnapshotOnShutdown;
    
    private String            instanceIdentifier;
    private long              cycleTime;
    private long              cycleTimeoutTime;
    private CycleMode         cycleMode;
    private boolean           runningStandalone;
    private boolean           enableTrace;
    private boolean           pauseOnBreakPoint;
    private int               traceTimeMinutes;
    private boolean           remoteSignalsEnabled;
    private int               remoteSignalPort;
    private String            histogramFile;
    private long              cyclicTaskShutdownTimeoutTime;
    private long              maxShutdownTime;
    private boolean           opcUaServiceEnabled;
    private int               opcUaServicePort;
    private String            opcUaServiceName;
    private double            opcUaMinSupportedSampleInterval;
    private Opc.AccessLevel   opcUaDefaultAccessLevel;
    private List<String>      opcUaBindAddresses;
    private boolean           consoleServiceEnabled;
    private int               consoleServicePort;
    private String            consoleBindAddress;
    private boolean           generateSnapshotOnShutdown;
    
    private CountingLock      activeEventsLock;
    
    private Synchronisation   startCycling;
    private Synchronisation   shutdownRequest;
    
    private boolean           immediateShutdownRequested;
    private int               exitCode;
        
    private ProcessEvent      awaitedEventOfLastModule;
    
    private TraceQueue      traceQueue;
    
    private Histogram       cycleHistogram;   //used to determine the overall load per cycle
    private Histogram       systemHistogram;  //used to determine the system load during a cycle
    private Histogram       modulesHistogram; //used to determine the load produced by the application modules during a cycle
    
    private AbstractModule  processedModule;   //
    
    private int             incrementCounter;
    private int             decrementCounter;

    private boolean         stopBeforeStartup;
    
    private String          versionNumber;
    private String          buildNumber;
    private String          buildDate;
    private String          projectName;
    
    private OpcUaService    opcUaService;   
    private TelnetService   consoleService;
    
    //    private ArrayList<AbstractModule> moduleList;
    private Hashtable<String, AbstractModule> moduleList;


    protected JPac(){
        super();
        setName(getClass().getSimpleName());
        
        tracePoint                  = 0;
        minRemainingCycleTime       = Long.MAX_VALUE;
        maxRemainingCycleTime       = 0;
        expectedCycleEndTime        = 0;
        cycleStartTime              = 0;
        expansionTime               = 0;
        status                      = Status.initializing;
        cycleNumber                 = 0;

        awaitedEventList            = Collections.synchronizedSet(new HashSet<Fireable>());
        awaitedSimEventList         = Collections.synchronizedSet(new HashSet<Fireable>());
        firedEventList              = new HashSet<Fireable>();

        readyToShutdown             = false;
        emergencyStopRequested      = false;
        emergencyStopActive         = false;
        emergencyStopIsToBeThrown   = false;
        emergencyStopCausedBy       = null;
        
        synchronizedTasks           = Collections.synchronizedList(new ArrayList<Runnable>());
        cyclicTasks                 = Collections.synchronizedList(new ArrayList<CyclicTask>());
        
        startCycle                  = new Semaphore(1);
        cycleEnded                  = new Semaphore(1);
        
        startCycling                = new Synchronisation();
        shutdownRequest             = new Synchronisation();
        
        immediateShutdownRequested  = false;        
        
        shutdownPending             = false;
                
        activeEventsLock            = new CountingLock();
        awaitedEventOfLastModule    = null;
        
        moduleList                  = new Hashtable<>(20);
        traceQueue                  = null;
        
        cycleHistogram             = null;
        systemHistogram            = null;
        modulesHistogram           = null;
        processedModule             = null;
        
        exitCode                    = 0;
        
        incrementCounter            = 0;
        decrementCounter            = 0;        
        
        try{
            propCycleTime                       = new LongProperty(this,"CycleTime",DEFAULTCYCLETIME,"[ns]",true);
            propCycleTimeoutTime                = new LongProperty(this,"CycleTimeoutTime",DEFAULTCYCLETIMEOUTTIME,"[ns]",true);
            propCycleMode                       = new StringProperty(this,"CycleMode",CycleMode.LazyBound.toString(),"[OneCycle | Bound | LazyBound | FreeRunning]",true);
            propRunningStandalone               = new BooleanProperty(this,"RunningStandalone",true,"must be true, if Elbfisch is run standalone",true);
            propEnableTrace                     = new BooleanProperty(this,"EnableTrace",false,"enables tracing of the module activity",true);
            propTraceTimeMinutes                = new IntProperty(this,"TraceTimeMinutes",0,"used to estimate the length of the trace buffer [min]",true);
            propPauseOnBreakPoint               = new BooleanProperty(this,"pauseOnBreakPoint", false, "cycle is paused, until all modules enter waiting state", true);
            propRemoteSignalsEnabled            = new BooleanProperty(this,"RemoteSignalsEnabled", false, "enable connections to/from remote JPac instances", true);
            propRemoteSignalPort                = new IntProperty(this,"RemoteSignalPort",10002,"server port for remote signal access",true);
            propHistogramFile                   = new StringProperty(this,"HistogramFile","./data/histogram.csv","file in which the histograms are stored", true);
            propCyclicTaskShutdownTimeoutTime   = new LongProperty(this,"CyclicTaskShutdownTimeoutTime",DEFAULTSHUTDOWNTIMEOUTTIME,"Timeout for all cyclic tasks to stop on shutdown [ns]",true);
            propMaxShutdownTime                 = new LongProperty(this,"MaxShutdownTime",DEFAULTSHUTDOWNTIMEOUTTIME,"period of time in which all modules must have been terminated in case of a shutdown [ns]",true);
            propOpcUaServiceEnabled             = new BooleanProperty(this,"OpcUa.ServiceEnabled",false,"enables the opc ua service", true);
            propOpcUaServicePort                = new IntProperty(this,"OpcUa.ServicePort",OpcUaService.DEFAULTPORT,"port over which the opc ua service is provided", true);
            propOpcUaServiceName                = new StringProperty(this,"OpcUa.ServiceName",OpcUaService.DEFAULTSERVERNAME,"name of the server instance", true);
            propOpcUaMinSupportedSampleInterval = new DoubleProperty(this,"OpcUa.MinSupportedSampleInterval",OpcUaService.MINIMUMSUPPORTEDSAMPLEINTERVAL,"minimum supported sample interval [ms]", true);
            propOpcUaDefaultAccessLevel         = new StringProperty(this,"OpcUa.DefaultAccessLevel","NONE","access levels can be NONE,READ_ONLY,READ_WRITE", true);
            propConsoleServiceEnabled           = new BooleanProperty(this,"Console.ServiceEnabled",false,"enables the console service", true);
            propConsoleServicePort              = new IntProperty(this,"Console.ServicePort",CONSOLESERVICEDEFAULTPORT,"port over which the console service is provided", true);
            propConsoleBindAddress              = new StringProperty(this,"Console.BindAddress",DEFAULTCONSOLESERVICEBINDADDRESS,"address the console service is bound to", true);
            propGenerateSnapshotOnShutdown      = new BooleanProperty(this,"GenerateSnapShotOnShutdown",false,"used to enable the generation of a snapshot on shutdown", true);
            
            instanceIdentifier              = InetAddress.getLocalHost().getHostName() + ":" + propRemoteSignalPort.get();
            cycleTime                       = propCycleTime.get();
            cycleTimeoutTime                = propCycleTimeoutTime.get();
            cycleMode                       = CycleMode.valueOf(propCycleMode.get());
            runningStandalone               = propRunningStandalone.get();
            enableTrace                     = propEnableTrace.get();
            traceTimeMinutes                = propTraceTimeMinutes.get();
            pauseOnBreakPoint               = propPauseOnBreakPoint.get();
            remoteSignalsEnabled            = propRemoteSignalsEnabled.get();
            remoteSignalPort                = propRemoteSignalPort.get();
            histogramFile                   = propHistogramFile.get();
            cyclicTaskShutdownTimeoutTime   = propCyclicTaskShutdownTimeoutTime.get();
            maxShutdownTime                 = propMaxShutdownTime.get();
            
            opcUaServiceEnabled             = propOpcUaServiceEnabled.get();
            opcUaServicePort                = propOpcUaServicePort.get();
            opcUaServiceName                = propOpcUaServiceName.get();
            opcUaMinSupportedSampleInterval = propOpcUaMinSupportedSampleInterval.get();
            opcUaBindAddresses              = new ArrayList<>();
            Configuration configuration     = Configuration.getInstance();
            opcUaBindAddresses              = configuration.getList("org..jpac..JPac.OpcUa.BindAddresses.BindAddress");
            
            consoleServiceEnabled           = propConsoleServiceEnabled.get();
            consoleServicePort              = propConsoleServicePort.get();
            consoleBindAddress              = propConsoleBindAddress.get();
            generateSnapshotOnShutdown      = propGenerateSnapshotOnShutdown.get();
            
            if (opcUaServiceEnabled){
                if (propOpcUaDefaultAccessLevel.get().equals(OPCACCESSLEVELNONE)){
                    opcUaDefaultAccessLevel = Opc.AccessLevel.NONE;
                } else if (propOpcUaDefaultAccessLevel.get().equals(OPCACCESSLEVELREADONLY)){
                    opcUaDefaultAccessLevel = Opc.AccessLevel.READ_ONLY;
                } else if (propOpcUaDefaultAccessLevel.get().equals(OPCACCESSLEVELREADWRITE)){
                    opcUaDefaultAccessLevel = Opc.AccessLevel.READ_WRITE;
                } else {
                    opcUaDefaultAccessLevel = Opc.AccessLevel.NONE;                    
                }
            }
            try{
                //get version.number, build.number and build.date
                Class clazz = JPac.class;
                String className = clazz.getSimpleName() + ".class";
                String classPath = clazz.getResource(className).toString();
                String manifestPath = "";
                if(classPath.endsWith("org.jpac/build/classes/org/jpac/JPac.class")){
                    //instantiated inside IDE
                    manifestPath = classPath.replace("build/classes/org/jpac/JPac.class", "").concat("MANIFEST.MF");
                }
                else{
                    //contained in org.jpac.jar
                    manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
                }
                Manifest manifest = new Manifest(new URL(manifestPath).openStream());
                Attributes attr = manifest.getMainAttributes();
                versionNumber = attr.getValue("Bundle-Version");
                buildNumber   = attr.getValue("Bundle-Build");
                buildDate     = attr.getValue("Bundle-Date");
                projectName   = attr.getValue("Bundle-Name");
            }
            catch(Exception exc){
                //build information cannot be retrieved
                versionNumber = "unknown";
                buildNumber   = "unknown";
                buildDate     = "unknown";
            }
            //install configuration saver
            try{registerCyclicTask(Configuration.getInstance().getConfigurationSaver());}catch(WrongUseException exc){/*cannot happen*/}
        }
        catch(ConfigurationException | IOException ex){
            Log.error("Error: ", ex);
            //properties cannot be initialized
            //kill application
            System.exit(99);            
        }
                
        //install a shutdown hook to handle application shutdowns
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
        setPriority(MAX_PRIORITY);
        //start instance of the automationController
        start();
    }

    public static JPac getInstance(){
        if (instance == null) {
            instance = new JPac();
        }
        return instance;
    }

    @Override
    public void run(){
        boolean done                 = false;
        long    systemLoadStartTime  = 0L; //used to compute the system histogram
        long    modulesLoadStartTime = 0L; //used to compute the modules histogram
        
        if (Log.isInfoEnabled()) Log.info("STARTING");
        try{
            try{
                //wait, until start up is requested
                setStatus(Status.ready);
                waitForStartUpSignal();
                if (stopBeforeStartup){
                    //termination requested before first cycle (see ShutdownHook)
                    //shut down immediately
                    if (Log.isInfoEnabled()) Log.info("SHUTDOWN COMPLETE");
                    readyToShutdown = true;// inform the shutdown hook that we are done
                    return;
                }
                prepareTrace();
                prepareHistogramms();
                prepareOpcUaService();
                prepareConsoleService();
                prepareRemoteConnections(); 
                prepareCyclicTasks();
            }
            catch(Exception exc){
                //if an error occured during preparation of the system, shutdown immediately
                Log.error("Error: ", exc);
                invokeImmediateShutdown(EXITCODEINITIALIZATIONERROR);
            }
            catch(Error exc){
                //if an error occured during preparation of the system, shutdown immediately
                Log.error("Error: ", exc);
                invokeImmediateShutdown(EXITCODEINITIALIZATIONERROR);
            }
            
            if (Log.isInfoEnabled()) Log.info("running in " + cycleMode + " mode ...");
            setStatus(Status.running);
            if (getCycleMode() == CycleMode.OneCycle){
                prepareOneCycleMode();
            }
            //initialize values for cycle time computation
            initializeCycle();
            do{//!done && running
                try{
                    tracePoint = 1000;
                    if (getCycleMode() == CycleMode.OneCycle){
                        waitForStartCycleSignal();
                        //initialize values for cycle time computation
                        initializeCycle();
                    }                
                    systemLoadStartTime = System.nanoTime();
                    tracePoint = 1100;
                    //update cycle var's for this cycle
                    prepareCycle();
                    
                    //handle deferred tasks, which must be synchronized to the cycle:
                    //propagation of signals, connect/disconnect of signals etc.
                    tracePoint = 1200;
                    handleDeferredTasks();
                    //now fire events awaited by application modules
                    tracePoint = 1300;
                    handleFireables(getAwaitedEventList());
                    
                    //acquire system histogram information
                    modulesLoadStartTime = System.nanoTime();
                    systemHistogram.update(modulesLoadStartTime - systemLoadStartTime);
                    //invoke the inEveryCycleDo() for every active module
                    tracePoint = 1400;
                    handleInEveryCycleDos();                    
                    //now start up application modules which have been awakenend before by fired process events
                    tracePoint = 1450;
                    handleAwakenedModules();
                    
                    //acquire modules histogram information
                    modulesHistogram.update(System.nanoTime() - modulesLoadStartTime);
                    
                    //handle emergency stop requests occured in current cycle
                    emergencyStopIsToBeThrown = false; //true only for one cycle
                    if (emergencyStopRequested){
                        emergencyStopRequested    = false;
                        //throw EmergencyStopException an all awaiting modules in next cycle
                        emergencyStopIsToBeThrown = true;
                    }

                    //acquire overall load histogram information
                    cycleHistogram.update(System.nanoTime() - systemLoadStartTime);

                    tracePoint = 1500;
                    long remainingCycleTime = expectedCycleEndTime - System.nanoTime();
                    acquireStatistics(remainingCycleTime);
                    if(getCycleMode() != CycleMode.FreeRunning){
                        //if not in FreeRunning  mode synchronize to the end of the cycle
                        //now, wait for the end of the cycle
                        wait4EndOfCycle(remainingCycleTime);
                    }
                }
                catch (Exception ex){
                    Log.error("Error",ex);
                    invokeImmediateShutdown(EXITCODEINTERNALERROR);
                }
                catch (Error ex){
                    Log.error("Error",ex);
                    invokeImmediateShutdown(EXITCODEINTERNALERROR);
                }
                //check, if the application is to be shutdown immediately
                if (isImmediateShutdownRequested()){
                    if (generateSnapshotOnShutdown){
                        generateSnapshot();
                    }
                    done = true;
                    //shutdown all active modules
                    shutdownModulesImmediately(getAwaitedEventList());
                }
                //check, if the application is to be shutdown normally
                if (isNormalShutdownRequested()){
                    if (!shutdownPending){
                        shutdownPending = true;
                        if (generateSnapshotOnShutdown){
                            generateSnapshot();
                        }
                        shutdownModules(getAwaitedEventList());
                    }
                    done = allModulesShutdown() || shutdownTimeExceeded();
                }
                if (getCycleMode() == CycleMode.OneCycle){
                    signalEndOfCycle();
                }            
                //trace cycle statistics, if applicable
                traceCycle();
            }
            while(!done);
            if (!allModulesShutdown()){
                getModules().values().stream().forEach((m)-> {if(m.getState() != State.TERMINATED) Log.error("failed to shutdown module '{}' in time.", m.getQualifiedName());});
            }
            //shutdown RemoteSignalConnection's
            closeRemoteConnections();
            //stop opc ua service, if running
            stopOpcUaService();
            //stop opc ua service, if running
            stopConsoleService();
            //clean up context of registered cyclic tasks
            stopCyclicTasks();
            //acknowledge request
            acknowledgeShutdownRequest();
            //new state is halted
            setStatus(Status.halted);
            if (Log.isInfoEnabled()){
                ArrayList<String> lines = logStatistics();
                lines.forEach(l -> Log.info(l));
                Log.info("SHUTDOWN COMPLETE");
            }
            readyToShutdown = true;// inform the shutdown hook that we are done
            try{sleep(MAXSHUTDOWNTIME);} catch (InterruptedException ex){}
            //jUnit test need special handling
            if (runningStandalone){
                System.exit(exitCode);
            }
        }
        catch(Exception exc){
            Log.error("Error: ", exc);
        }
        catch(Error exc){
            Log.error("Error: ", exc);
        }
    };

    private void handleFireables(Set<Fireable> fireableList) throws SomeEventsNotProcessedException, InconsistencyException{
        boolean fired = false;
        //check, if some of the currently registered Fireables can be fired in this cycle
        for (Fireable f: fireableList) {
            try{//the fireable is fired, if
                //it is fired by its own,
                //or if it is a ProcessEvent and an emergency stop is pending and the ProcessEvent is not awaited by a module, which threw
                //an emergency stop exception during the last cycle,
                //or if it is a ProcessEvent and it timed out during this cycle
                boolean fFired    = f.evaluateFiredCondition();
                boolean fTimedOut = (f instanceof ProcessEvent) && ((ProcessEvent)f).evaluateTimedOutCondition();
                fired = fFired ||
                        (f instanceof ProcessEvent && (emergencyStopIsToBeThrown && !((ProcessEvent)f).getObservingModule().isRequestingEmergencyStop()) ||
                                                       fTimedOut                                                                                       );
            }
            catch(ProcessException exc){
                //the fireable threw a process exception
                //let the observing module handle this
                fired = true;
            }
            if (fired){
                //add Fireable to the list of fired events
                if (f instanceof ProcessEvent && emergencyStopIsToBeThrown){
                    //if an emergency stop request is pending,
                    //let the awaiting module know about it
                    ((ProcessEvent)f).setEmergencyStopOccured(true);
                    ((ProcessEvent)f).setEmergencyStopCause(emergencyStopCausedBy.getMessage());                        
                }
                getFiredEventList().add(f);
            }
            if (f instanceof ProcessEvent && ((ProcessEvent)f).getObservingModule().isRequestingEmergencyStop()){
                //emergency stop request has been recognized above. Reset it instantly
               ((ProcessEvent)f).getObservingModule().setRequestingEmergencyStop(false);                                                 
            }
        }
        //remove fireables from awaited event list
        for (Fireable f: getFiredEventList()) {
             fireableList.remove(f);
        }        
    }
    
    private void handleInEveryCycleDos(){
        try{
            for (AbstractModule module: moduleList.values()){
                //invoke inEveryCycleDo() for all active modules
                processedModule = module;
                module.invokeInEveryCycleDo();
                processedModule = null;
            }
        }
        finally{
            processedModule = null;
        }
    }
    
    private void handleAwakenedModules() throws SomeEventsNotProcessedException, InconsistencyException{
        try{
            //awaken associated modules
            for (Fireable f: getFiredEventList()) {
                tracePoint = 1501;
                f.notifyObservingModule();
            }
            //wait until all fired events are processed by the associated modules
            switch(getCycleMode()){
                case OneCycle:
                     //wait until all modules have completed their tasks without time limit
                     activeEventsLock.waitForUnlock();
                     break;
                case Bound:
                     //wait until all modules have completed their tasks but not beyond the end of the cycle
                     activeEventsLock.waitForUnlock(expectedCycleEndTime - System.nanoTime());
                     break;
                case LazyBound:
                     //wait until all modules have completed their tasks but not beyond the end of the cycle
                     activeEventsLock.waitForUnlock(expectedCycleEndTime - System.nanoTime());
                     if (activeEventsLock.getCount() > 0){
                        //cycle exceedance encountered. Give the application time to bring the cycle to an end
                        activeEventsLock.waitForUnlock(cycleTimeoutTime);
                        //record cycle exceedance
                        numberOfCyclesExceeded++;
                     }
                     break;
                case FreeRunning:
                     //wait until all modules have completed their tasks without time limit
                     activeEventsLock.waitForUnlock();
                     break;
            }
            if (activeEventsLock.getCount() > 0){
                if (pauseOnBreakPoint){
                    //a module might have run on a break point
                    //wait until all modules have completed their tasks without time limit
                    if (Log.isInfoEnabled()) Log.info("jPac paused ...");
                    activeEventsLock.waitForUnlock();  
                    //lengthen time line for the time halted on the break point (time line is freezed for that period of time)
                    expansionTime += System.nanoTime() - expectedCycleEndTime;
                    if (Log.isInfoEnabled()) Log.info("jPac continued ...");
                }
                else{
                    //assert failure, if at least one fired event
                    //has failed to be properly handled during the last cycle
                    Log.error("at least one module hung up !!!!: ");
                    Log.error("  elapsed cycle time     : " + (System.nanoTime() - cycleStartTime ));
                    Log.error("  trace point            : " + tracePoint);
                    Log.error("  activeEventsCount      : " + activeEventsLock.getCount());
                    Log.error("  max. activeEventsCount : " + activeEventsLock.getMaxCount());
                    Log.error("  incrementCounter       : " + incrementCounter);
                    Log.error("  decrementCounter       : " + decrementCounter);
                    
                    for (Fireable f: getFiredEventList()){
                        AbstractModule module = ((ProcessEvent)f).getObservingModule();
                        if (module.getState() != Thread.State.WAITING){
                            Log.error("  module '" + module + "' invoked by " + f + " hung up in state " + module.getStatus());                        
                        }
                        else{
                            Log.info("  module '" + module + "' invoked by " + f + " state " + module.getStatus());                                                    
                        }
                    }
                    throw new SomeEventsNotProcessedException(getFiredEventList());
                }
            }
            else{
                //all modules came to an end for this cycle
                //wait until the last made its wait() call
                synchronizeOnLastModule();
            }
        }
        finally{
            //clear list of fired ProcessEvents (simulation or productive) for next usage
            getFiredEventList().clear();
        }

    }

    private void shutdownModulesImmediately(Set<Fireable> fireableList) throws InconsistencyException{
        try{
            //invoke all waiting modules and let them handle their ShutdownException
            for (Fireable f: fireableList) {
                if (f instanceof ProcessEvent){
                    //retrieve all pending process events
                    getFiredEventList().add(f);
                }
            }
            if (Log.isInfoEnabled()) Log.info("shutting down modules ...");
            //awaken waiting modules with a ShutdownException
            //which is automatically thrown by means of the ProcessEvent,
            //if this.shutdownRequested = true
            for (Fireable f: getFiredEventList()) {
                AbstractModule module = f.getObservingModule();
                String moduleName = module.toString();
                if (Log.isDebugEnabled()) Log.debug("   shutting down module " + moduleName + " ...");
                ((ProcessEvent)f).setShutdownRequested(true);
                f.notifyObservingModule();
                boolean moduleEnded = false;
                do
                  try{
                      module.join(MAXSHUTDOWNTIME);
                      moduleEnded = true;
                  }
                  catch(InterruptedException exc){}
                while(!moduleEnded);
                if (module.getState() == Thread.State.TERMINATED){
                    if (Log.isDebugEnabled()) Log.debug("   module " + moduleName + " shutdown succeeded");
                }
                else{
                    Log.error("   !!!! failed to shutdown module " + moduleName + ". It's current state is " + module.getStatus());                    
                }
            }
            if (Log.isInfoEnabled()) Log.info("... shutting down modules done");
        }
        finally{
            //clear list of fired ProcessEvents (simulation or productive) for next usage
            getFiredEventList().clear();
        }
    }
    
    private void shutdownModules(Set<Fireable> fireableList) throws InconsistencyException{
        try{
            //invoke all waiting modules and let them handle their ShutdownException
            for (Fireable f: fireableList) {
                if (f instanceof ProcessEvent){
                    //retrieve all pending process events
                    getFiredEventList().add(f);
                }
            }
            if (Log.isInfoEnabled()) Log.info("requesting shutdown of modules");
            //awaken waiting modules with a ShutdownException
            //which is automatically thrown by means of the ProcessEvent,
            //if this.shutdownRequested = true
            for (Fireable f: getFiredEventList()) {
                AbstractModule module = f.getObservingModule();
                ((ProcessEvent)f).setShutdownRequested(true);
                f.notifyObservingModule();
            }
        }
        finally{
            //clear list of fired ProcessEvents (simulation or productive) for next usage
            getFiredEventList().clear();
        }
    }
    
    private void generateSnapshot(){
        try{
            Snapshot snapshot = getSnapshot();
            snapshot.dump(getDataDir());
            Log.info("snapshot dumped to '" + snapshot.getFilename() + "'");
        }
        catch(Exception exc){
            Log.error("Error: ", exc);
        }
    }
    
    private boolean allModulesShutdown(){
        return getModules().values().stream().allMatch((m) -> m.getState() == State.TERMINATED);
    }
    
    /**
     * used to propagate signal states to connected signal instances
     */
    private void handleDeferredTasks() throws SignalAlreadyConnectedException, SignalInvalidException, ConfigurationException, RemoteSignalException {
        synchronized(synchronizedTasks){
            for(Runnable r: synchronizedTasks){
                //run synchronized task
                r.run();
            }            
            //everything done. Prepare list for next cycle
            synchronizedTasks.clear();
        }
        
        synchronized(cyclicTasks){
            for(CyclicTask ct: cyclicTasks){
                //run cyclic task
                ct.run();
            }            
        }
                
        List<Signal> signals = SignalRegistry.getInstance().getSignals();
        synchronized(signals){
            for(Signal s: signals){
                //handle requested (dis)connections of signals
                s.handleConnections();
                //apply intrinsic function
                s.applyIntrinsicFunction();
                //propagate signal alterations
                s.propagate();
            }
        }
        pushSignalsOverRemoteConnections();        
    }
    
    /*
     * used to invoke a task synchronized to the next jpac cycle
     * CAUTION: The given task may not call invokeLater() directly or indirectly by itself !
     */
    public void invokeLater(Runnable task){
        synchronized(synchronizedTasks){
            synchronizedTasks.add(task);
        }
    }

    /*
     * used to register a task, which is run at the beginning of every jpac cycle
     * @param  task cyclic task 
     * @throws WrongUseException, if the given task is already registered
     */
    public void registerCyclicTask(CyclicTask task) throws WrongUseException{
        synchronized(cyclicTasks){
            if (cyclicTasks.contains(task)){
                throw new WrongUseException("cyclic task " + task + " already registered.");
            }
            cyclicTasks.add(task);
        }
    }

    /*
     * used to register a task, which is run at the beginning of every jpac cycle
     */
    public void unregisterCyclicTask(CyclicTask task){
        synchronized(cyclicTasks){
            cyclicTasks.remove(task);
        }
    }

    /*
     * @return the awaitedEventList
     */
    protected Set<Fireable> getAwaitedEventList() {
        return awaitedEventList;
    }

    /**
     * @return the awaitedSimEventList
     */
    protected Set<Fireable> getAwaitedSimEventList() {
        return awaitedSimEventList;
    }

    /**
     * @return the firedEventList
     */
    protected Set<Fireable> getFiredEventList() {
        return firedEventList;
    }
    
    protected boolean shutdownTimeExceeded(){
        return (System.nanoTime() - shutdownRequestTime) > maxShutdownTime;
    }

    /**
     * @return the cycleNumber
     */
    public long getCycleNumber() {
        return cycleNumber;
    }

    /**
     * @return the cycleTime
     */
    public long getCycleTime() {
        return cycleTime;
    }
    /**
     * invoked a shutdown of the elbfisch application.
     * If called by a module shutdown() returns immediately. Otherwise it blocks, until the shutdown is acknowledged by jPac
     * @param exitCode 
     */
    
    protected void shutdown(int exitCode, boolean waitForAcknowledgement) {
        if (Log.isInfoEnabled()) Log.info("shutdown requested. Informing jPac ...");
        this.shutdownRequestTime = System.nanoTime();
        this.exitCode            = exitCode;
        shutdownRequest.request(waitForAcknowledgement);
    }

    public void shutdownDeferred(int exitCode) {
        shutdown(exitCode, false);
    }

    public void invokeImmediateShutdown(int exitCode) {
        immediateShutdownRequested = true;
        shutdownRequestTime        = System.nanoTime();
        exitCode                   = exitCode; 
    }

    public void startCycling() {
        if (Log.isInfoEnabled()) Log.info("startCycling requested");
        startCycling.request(true);
        if (Log.isInfoEnabled()) Log.info("startCycling() acknowledged");
    }
    
    public boolean isNormalShutdownRequested() {
        return shutdownRequest.isRequested();
    }

    public boolean isImmediateShutdownRequested() {
        return immediateShutdownRequested;
    }

    protected void acknowledgeShutdownRequest(){
        if (shutdownRequest.isRequested()){
            shutdownRequest.acknowledge();
        }
        immediateShutdownRequested = false;
    }

    /**
     * @return the emergencyStopRequested
     */
    public boolean isEmergencyStopActive() {
        return emergencyStopActive;
    }

    public void acknowledgeEmergencyStop(){
        emergencyStopActive    = false;
        emergencyStopCausedBy  = null;
    }

    public void requestEmergencyStop(EmergencyStopException causedBy) {
        //set emergencyStopRequested. Will be reset by the automation controller after notification of all modules
        if (!this.emergencyStopActive){
            if (Log.isDebugEnabled()){Log.debug("EMERGENCY STOP REQUESTESD requestEmergencyStop: " + emergencyStopRequested);};
            //set request only once per emergency stop case
            this.emergencyStopRequested = true;
            this.emergencyStopActive    = true;
            this.emergencyStopCausedBy  = causedBy;
        }
    }

    /**
     * @return the emergencyStopCause
     */
    public String getEmergencyStopCause() {
        return emergencyStopCausedBy.getMessage();
    }

    protected void incrementAwakenedModulesCount() {
        incrementCounter++;// debug
        activeEventsLock.increment();
    }    

    protected void decrementAwakenedModulesCount(ProcessEvent awaitedEvent) throws InconsistencyException {
        decrementCounter++;// debug
        if (activeEventsLock.decrement()){
            //if last module went to sleep, store its awaited event
            awaitedEventOfLastModule = awaitedEvent;
        }
    }    
    
    protected void indicateCheckBack(ProcessEvent awaitedEvent) throws InconsistencyException{
        tracePoint = 100;
        if (enableTrace){
            AbstractModule module = awaitedEvent.getObservingModule();
            getTraceQueue().putTrace(cycleNumber, module.getModuleIndex(), module.getWakeUpNanoTime(), module.getSleepNanoTime());
        }
        tracePoint = 101;
        decrementAwakenedModulesCount(awaitedEvent);
        tracePoint = 102;
    }

    protected void synchronizeOnLastModule() throws InconsistencyException {
       if (awaitedEventOfLastModule != null){
           synchronized(awaitedEventOfLastModule){
               //do nothing meaningful.
               //Actually wait, until the module leaves the monitor 
               //on its awaited process event by calling awaitedEvent.wait()
               //Acquire it and leave it instantly
               //Note: see synchronized(this){} block inside ProcessEvent.awaitImpl() 
               long dummy = awaitedEventOfLastModule.getCycleNumber();
           }
       }
    }    
    
    protected void prepareOneCycleMode(){
        startCycle.acquireUninterruptibly();        
    }
    
    protected void waitForStartUpSignal(){
        if (Log.isInfoEnabled()) Log.info("awaiting start up signal ...");
        startCycling.waitForRequest();
        startCycling.acknowledge();
    }

    protected void waitForStartCycleSignal(){
        startCycle.acquireUninterruptibly();
    }
    
    protected void signalEndOfCycle(){
        //signal end of cycle
        cycleEnded.release();
        //aquire start semaphore in preparation of next cycle
        //must be released instantly
        startCycle.acquireUninterruptibly();
    }
    
    public void invokeNextCycle(){
        //aquire end of cycle semaphore in preparation for next cycle
        //must be released instantly
        cycleEnded.acquireUninterruptibly();
        //start cycle
        startCycle.release();
        //wait, until cycle has finished
        cycleEnded.acquireUninterruptibly();
    }

    protected void prepareCycle(){
        //compute cycle time
        cycleStartTime       = System.nanoTime();//the cycle starts now
        expectedCycleEndTime = cycleStartTime + getCycleTime();
//        expectedCycleEndTime = nextCycleStartTime + getCycleTime();
//        if (expectedCycleEndTime < cycleStartTime){
//            //if last cycle exceeded its end time more than one cycle time
//            //sync cycle to current time
//            expectedCycleEndTime = cycleStartTime + getCycleTime();
//        }
//        nextCycleStartTime = expectedCycleEndTime;
        cycleNumber++;
                
        //initialize active event counter
        activeEventsLock.reset();  
        //reset awaited event of last module
        awaitedEventOfLastModule = null;
        
        //debugging info
        incrementCounter = 0;
        decrementCounter = 0;
    }
    
    protected void traceCycle(){
        if (enableTrace){
            getTraceQueue().putTrace(cycleNumber, OWNMODULEINDEX, cycleStartTime, System.nanoTime());
        }
    }

    protected void initializeCycle(){
        //set cycleEndTime, nextCycleStartTime to actual time, as if I am at the end of a previous cycle
        expectedCycleEndTime = System.nanoTime();
//        nextCycleStartTime   = expectedCycleEndTime;
    }

    protected void wait4EndOfCycle(long remainingCycleTime){
        boolean done = remainingCycleTime <= 0;
        while(!done){
            try{    
                Thread.sleep(remainingCycleTime / 1000000l, (int)(remainingCycleTime % 1000000));
                done = true;
            }
            catch(InterruptedException exc){};
        }
    }
    
    protected void acquireStatistics(long remainingCycleTime){
        if(cycleNumber > 1){
            //acquire some statistics concerning the cycle time
            if (remainingCycleTime > getCycleTime()){
                remainingCycleTime = getCycleTime();
            }
            else if (remainingCycleTime < 0){
                remainingCycleTime = 0;
            }
            if (minRemainingCycleTime > remainingCycleTime)
                minRemainingCycleTime = remainingCycleTime;
            else if (maxRemainingCycleTime < remainingCycleTime){
                maxRemainingCycleTime = remainingCycleTime;
            }
        }
    }
    
    public ArrayList<String> logStatistics(){
        ArrayList<String> lines = new ArrayList<>();
        if (minRemainingCycleTime < 0){
            minRemainingCycleTime = 0;
        }
        if (maxRemainingCycleTime > getCycleTime()){
            maxRemainingCycleTime = getCycleTime();
        }
        
        int percentageMinRemainingCycleTime = (int)(100L * minRemainingCycleTime/getCycleTime());
        int percentageMaxRemainingCycleTime = (int)(100L * maxRemainingCycleTime/getCycleTime());
        int percentageCyclesExceeded        = (int)(100L * numberOfCyclesExceeded/getCycleNumber());        
        lines.add("cycle mode                : " + getCycleMode());
        lines.add("cycle time                : " + getCycleTime() + " ns");
        if (getCycleMode() != CycleMode.FreeRunning){
            lines.add("min remaining cycle time  : " + minRemainingCycleTime + " ns (" + percentageMinRemainingCycleTime + "%)");
            lines.add("max remaining cycle time  : " + maxRemainingCycleTime + " ns (" + percentageMaxRemainingCycleTime + "%)");
        }
        if (getCycleMode() == CycleMode.LazyBound){
            lines.add("number of cycles exceeded : " + numberOfCyclesExceeded + " of " + getCycleNumber() + " (" + percentageCyclesExceeded + "%)");
        }
        return lines;
    }
    
    public ArrayList<String> showStateOfModule(String qualifiedName){
        ArrayList<String> lines = new ArrayList<>();
        AbstractModule module = getModules().values().stream().filter(m -> m.getQualifiedName().equals(qualifiedName)).findFirst().get();
        if (module != null){
            CharString[] cs = module.getStackTraceSignals();
            for (int i = 0; i < cs.length; i++){
                if (cs[i].isValid()){
                    try{lines.add(cs[i].get());}catch(SignalInvalidException exc){/*cannot happen*/};
                }
                else{
                    break;
                }
            }
        }
        return lines;
    }

    public ArrayList<Histogram> getHistograms(){
        ArrayList<Histogram> histograms = new ArrayList<>();
        histograms.add(cycleHistogram);
        histograms.add(systemHistogram);
        histograms.add(modulesHistogram);
        getModules().values().stream().sorted((m1,m2) -> m1.getQualifiedName().compareTo(m2.getQualifiedName())).forEach(m -> histograms.add(m.getHistogram()));
        return histograms;
    }
    
    public Snapshot getSnapshot(){
        Snapshot snapshot = new Snapshot();    
        return snapshot;
    }

    protected void setStatus(Status status){
        this.status = status;
    }

    public Status getStatus(){
        return this.status;
    }
    
    public String getDataDir(){
        return this.DATADIR;
    }
    
    public String getCfgDir(){
        return this.CFGDIR;
    }

    public String getHistogramFile(){
        return this.histogramFile;
    }
    
    protected int register(AbstractModule module)throws ModuleAlreadyRegisteredException{
        if (!moduleList.containsKey(module.getQualifiedName())){
            moduleList.put(module.getQualifiedName(), module);
        }
        else{
            throw new ModuleAlreadyRegisteredException(module.getQualifiedName());
        }
        return moduleList.size() - 1;
    }
    
    protected void prepareTrace() throws InvalidPropertyException{
        if (enableTrace){
           if (traceTimeMinutes <= 0){
               throw new InvalidPropertyException("traceTimeMinutes must be a positive integer:" + traceTimeMinutes);
           }
           if (Log.isInfoEnabled()) Log.info("tracing enabled");
           //determine estimated number of entries in relation to the given trace interval
           //assuming that one sample (ModuleTrace) will be generated per cycle
           int numberOfEntries = (int)(traceTimeMinutes * 60000000000L / cycleTime);
           traceQueue = new TraceQueue(numberOfEntries);
        }
    }
    
    protected void prepareRemoteConnections() throws ConfigurationException, RemoteException, RemoteSignalException{
        if (remoteSignalsEnabled){
            //start serving incoming remote signal requests
            RemoteSignalServer.start(remoteSignalPort);
            //instantiate outgoing remote signals
            ConcurrentHashMap<String, RemoteSignalConnection> remoteHosts = RemoteSignalRegistry.getInstance().getRemoteHosts();
            for (Entry<String, RemoteSignalConnection> entry: remoteHosts.entrySet()){
                entry.getValue().open();
            }
        }
    }
    
    protected void closeRemoteConnections(){
        final long CLOSECONNECTIONTIMEOUT = 3000000000L; // 3 sec
        try{
            if (remoteSignalsEnabled){
                if (Log.isInfoEnabled()) Log.info("closing remote connections ...");
                //invoke closure of all open remote connections
                ConcurrentHashMap<String, RemoteSignalConnection> remoteHosts = RemoteSignalRegistry.getInstance().getRemoteHosts();
                for (Entry<String, RemoteSignalConnection> entry: remoteHosts.entrySet()){
                    entry.getValue().close();
                }
                //wait for all connections to close
                for (Entry<String, RemoteSignalConnection> entry: remoteHosts.entrySet()){
                    long timeoutTime = System.nanoTime() + CLOSECONNECTIONTIMEOUT;
                    while(!entry.getValue().isClosed() && System.nanoTime() < timeoutTime);
                    if (!entry.getValue().isClosed()){
                        Log.error("   failed to close remote connection to " + entry.getValue().getRemoteJPacInstance());
                    }
                }                    
                RemoteSignalRegistry.getInstance().stopWatchdog();
                if (Log.isInfoEnabled()) Log.info("... closing of remote connections done");
            }
        }
        catch(Exception exc){
            Log.error("Error:", exc);
        }
        catch(Error exc){
            Log.error("Error:", exc);
        }
    }

    protected void prepareCyclicTasks(){
         synchronized(cyclicTasks){
            for(CyclicTask ct: cyclicTasks){
                //prepare cyclic task
                ct.prepare();
            }            
        }
    }
    
    protected void stopCyclicTasks(){
        boolean atLeastOneCyclicTaskRunning = false;
        int     ticks                       = 0;
        synchronized(cyclicTasks){
            //stop all cyclic tasks
            for(CyclicTask ct: cyclicTasks){
                ct.stop();
            }            
            //wait for all cyclic tasks coming to an end
            do{
                atLeastOneCyclicTaskRunning = false;
                for(CyclicTask ct: cyclicTasks){
                    atLeastOneCyclicTaskRunning = atLeastOneCyclicTaskRunning || !ct.isFinished();
                }  
                if (atLeastOneCyclicTaskRunning){
                    try{Thread.sleep(100);}catch(InterruptedException exc){}
                }
                ticks++;
            }
            while(atLeastOneCyclicTaskRunning && ticks < (cyclicTaskShutdownTimeoutTime/100000000L));
            if (atLeastOneCyclicTaskRunning){
                for(CyclicTask ct: cyclicTasks){
                    if (!ct.isFinished()){
                        Log.error("cyclic task " + ct.getClass().getCanonicalName() + " did not stop in time.");
                    }
                }  
            }
        }
    }

    protected void prepareOpcUaService() throws Exception{
        if (opcUaServiceEnabled){
            opcUaService = new OpcUaService(opcUaServiceName, opcUaBindAddresses, opcUaServicePort, opcUaMinSupportedSampleInterval);
            opcUaService.start();
            Log.info("OPC UA service started");
        }
    }
    
    protected void stopOpcUaService() {
        //stop opc server, if running
        if (opcUaService != null){
            Log.info("stopping opc ua service ...");
            if (opcUaService.getServer() != null){
                opcUaService.getServer().shutdown();
                Stack.releaseSharedResources();
            }
            Log.info("opc ua service stopped");
        }        
    }
    
    protected void prepareConsoleService() throws Exception{
        if (consoleServiceEnabled){
            consoleService = new TelnetService(false, consoleBindAddress, consoleServicePort);
            Log.info("console service started");            
        }
    }

    protected void stopConsoleService(){
        //stop console server, if running
        if (consoleService != null){
            Log.info("stopping console service ...");
            consoleService.stop();
            Log.info("console service stopped");
        }        
    }

    public Boolean cleanUpConfiguration() throws ConfigurationException{
        boolean done = false;
        try{
            Configuration.getInstance().cleanUp();
            done = true;
        }
        catch(ConfigurationException exc){
            Log.error("Error:", exc);
        }
        return done;
    }
    
    protected void pushSignalsOverRemoteConnections() throws ConfigurationException, RemoteSignalException{
        if (remoteSignalsEnabled){
            ConcurrentHashMap<String, RemoteSignalConnection> remoteHosts = RemoteSignalRegistry.getInstance().getRemoteHosts();
            //remoteHosts.entrySet().iterator()
            for (Entry<String, RemoteSignalConnection> entry: remoteHosts.entrySet()){
                entry.getValue().pushSignals(cycleNumber);
            }
        }        
    }
    
    /**
     * @return the traceQueue
     */
    public TraceQueue getTraceQueue() {
        return traceQueue;
    }
    
    protected void prepareHistogramms(){
        cycleHistogram   = new Histogram("overall cycle", cycleTime);
        systemHistogram  = new Histogram("system load", cycleTime);
        modulesHistogram = new Histogram("modules load", cycleTime);
    }
    
    public Histogram getSystemHistogramm(){
        return systemHistogram;
    }
    public Histogram getModulesHistogramm(){
        return modulesHistogram;
    }

    /**
     * 
     * @return the systems nanotime synchronized to the current cycle.
     */
    public long getCycleNanoTime(){
        return cycleStartTime;
    }
    
    /**
     * 
     * @return the start time of the current cycle (ns) corrected by the time spent on break points during a debug session
     */
    public long getExpandedCycleNanoTime(){
        return cycleStartTime - expansionTime;
    }
    
    public AbstractModule getModule(int i){
        return moduleList.get(i);
    }

    public Hashtable<String, AbstractModule> getModules(){
        return moduleList;
    }

    /**
     * @return the cycleMode
     */
    public CycleMode getCycleMode() {
        return cycleMode;
    }
    
    public String getInstanceIdentifier(){
        return instanceIdentifier;
    }
    
    public void setEmergencyStopExceptionCausedBy(EmergencyStopException causedBy){
        this.emergencyStopCausedBy = causedBy;
    }
 
    public EmergencyStopException getEmergencyStopExceptionCausedBy(){
        return this.emergencyStopCausedBy;
    }
    
    public AbstractModule getProcessedModule(){
        return this.processedModule;
    }
    
    public void stopBeforeStartUp(){
        if (Log.isInfoEnabled()) Log.info("aborting jPac before startup");
        this.stopBeforeStartup = true;
        startCycling.request(true);
    }
    
    public Opc.AccessLevel getOpcUaDefaultAccesslevel(){
        return this.opcUaDefaultAccessLevel;
    }
        
    public String getVersion(){
        return versionNumber;
    }
    
    public String getBuild(){
        return buildNumber;
    }

    public String getBuildDate(){
        return buildDate;
    }
    
    public String getProjectName(){
        return projectName;
    }

    protected void waitUntilShutdownComplete(){
        int times100ms;
        for(times100ms = 0; times100ms < 100 && !readyToShutdown; times100ms++){
            try {Thread.sleep(100);}catch(InterruptedException ex) {}
        }
        if (times100ms >= 100){
            //automation controller did not finish in time
            Log.error("jPac stopped abnormaly !");
        }            
    }

    /**
     * used to shutdown the Elbfisch environment by an embedding application. Blocks, until shutdown has completed.
     * HINT: Do not use it from inside an Elbfisch module. Use Module.shutdown(<exit code>) instead.
     */
    public void shutdownGraceFully() {
        switch (status){
            case initializing:
            case ready:
                //JPac has been initialized but did not start cycling
                stopBeforeStartUp();
                waitUntilShutdownComplete();                    
                break;
            case running:
                if (!readyToShutdown){
                    shutdown(EXITCODENORMALSHUTDOWN, true);
                    waitUntilShutdownComplete();                    
                }
                break;
            case halted:
                //nothing to do
                break;
        }
    }
        
    class ShutdownHook extends Thread{
        public ShutdownHook(){
            setName(getClass().getSimpleName());
        }
        
        @Override
        public void run() {
            shutdownGraceFully();
        }
    }
    
    protected class CountingLock{
        private       int    count       = 0;
        private       int    maxcount    = 0;
        private final Object zeroReached = new Object();
        
        protected void increment(){
            synchronized(zeroReached){
                count++;
                maxcount++;
            }
        }
        
        protected boolean decrement() throws InconsistencyException{
            synchronized(zeroReached){
                count--;
                if (count < 0){
                    throw new InconsistencyException("CountingLock.decrement(): module counter inconsistent !!!!!!");
                }
                if (count == 0){
                    zeroReached.notify();
                }
            }
            return count == 0;
        }
        
        protected int getCount(){
            int localCount = 0;
            synchronized(zeroReached){
                localCount = count;
            }
            return localCount;
        }
        
        protected int getMaxCount(){
            int localCount = 0;
            synchronized(zeroReached){
                localCount = maxcount;
            }
            return localCount;
        }

        protected void reset(){
            synchronized(zeroReached){
                count    = 0;
                maxcount = 0;
            }
        }
        
        protected void waitForUnlock(){
            synchronized(zeroReached){
                while(count > 0 && !shutdownRequest.isRequested()){
                    try{zeroReached.wait(100);}catch(InterruptedException exc){};
                }
            }            
        }
        
        protected void waitForUnlock(long waittime){//nanoseconds
            boolean waitReturned = false;
            if (waittime > 0){
                synchronized(zeroReached){
                    while(count > 0 && !waitReturned){
                        try{
                            zeroReached.wait(waittime / 1000000L, (int)(waittime % 1000000));
                            //wait returned normally or due to timeout
                            waitReturned = true;
                        }catch(InterruptedException exc){};
                    }
                }
            }
            else{
                if (Log.isDebugEnabled()) Log.debug("cycle time expired before synchronization on modules !!!!!!: " + (-waittime) + " ns");
            }
        }
    }
    
    protected class Synchronisation{
        private boolean       requested                = false;
        private boolean       acknowledged             = false;
        private final Object  syncPointRequest         = new Object();
        private final Object  syncPointAcknowledgement = new Object();
        
        protected void waitForRequest(){
            synchronized(syncPointRequest){
                while(!requested){
                    try{syncPointRequest.wait();}catch(InterruptedException exc){};
                }
                requested = false;
            }            
        }
        
        protected void acknowledge(){
            synchronized(syncPointAcknowledgement){
                requested    = false;
                acknowledged = true;
                syncPointAcknowledgement.notify();
            }
        }

        protected void request(boolean waitForAcknowledgement){
            synchronized(syncPointRequest){
               acknowledged = false;
               requested    = true;
               syncPointRequest.notify();
            }
            if (waitForAcknowledgement){
                synchronized(syncPointAcknowledgement){
                    while(!acknowledged){
                        try{syncPointAcknowledgement.wait();}catch(InterruptedException exc){};
                    }
                   acknowledged = false;
                }
            }
        }
        
        protected boolean isRequested(){
            return requested;
        }
        
    }

    /**
     * used to hold the latest tracing information for a given number of execution cycles.
     */
    public class TraceQueue extends ArrayBlockingQueue<ModuleTrace>{
        private int  numberOfEntries;
                
        protected TraceQueue(int numberOfEntries){
            super(numberOfEntries);
            this.numberOfEntries = numberOfEntries;
        }
                
        public void putTrace(long cycleNumber, int moduleIndex, long startNanos, long endNanos) {
            ModuleTrace moduleTrace;
            synchronized(this){
                moduleTrace = getNextModuleTrace();
                moduleTrace.setCycleNumber(cycleNumber);
                moduleTrace.setModuleIndex(moduleIndex);
                moduleTrace.setStartNanos(startNanos);
                moduleTrace.setEndNanos(endNanos);
                //put the actual entry
                try{super.put(moduleTrace);} catch(InterruptedException exc){/*cannot happen*/}
            }
        }
        private ModuleTrace getNextModuleTrace(){
            ModuleTrace moduleTrace;
            if(remainingCapacity() == 0){
                //queue is full, remove the oldest entry (head)
                //and recycle it
                moduleTrace = poll();
            }
            else{
                //create an new module trace record
                moduleTrace = new ModuleTrace();
            }
            return moduleTrace;
        }
    }

    public class ModuleTrace{
        private long cycleNumber;
        private long moduleIndex;
        private long startNanos;
        private long endNanos;
                
        public long getStartNanos() {
            return startNanos;
        }

        public long getEndNanos() {
            return endNanos;
        }
        
        protected void setCycleNumber(long cycleNumber){
            this.cycleNumber = cycleNumber;
        }
        protected void setModuleIndex(int moduleIndex){
            this.moduleIndex = moduleIndex;
        }
        protected void setStartNanos(long nanos){
            this.startNanos = nanos;
        }
        protected void setEndNanos(long nanos){
            this.endNanos = nanos;
        }
        
        public String toCSV(){
            StringBuffer csvString = new StringBuffer();
            csvString.append(cycleNumber);csvString.append(';');
            csvString.append(moduleIndex);csvString.append(';');
            csvString.append(startNanos);csvString.append(';');
            csvString.append(endNanos);csvString.append(';');
            return csvString.toString();
        }
    }
    
}
