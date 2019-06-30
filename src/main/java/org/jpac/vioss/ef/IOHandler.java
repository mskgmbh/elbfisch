/**
 * PROJECT   : Elbfisch - java process automation controller (jPac) 
 * MODULE    : IOHandler.java (versatile input output subsystem)
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

package org.jpac.vioss.ef;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.jpac.AsynchronousTask;
import org.jpac.InconsistencyException;
import org.jpac.ProcessException;
import org.jpac.Signal;
import org.jpac.SignalAccessException;
import org.jpac.WrongUseException;
import org.jpac.ef.Browse;
import org.jpac.ef.BrowseAcknowledgement;
import org.jpac.ef.Result;
import org.jpac.ef.Subscribe;
import org.jpac.ef.SubscribeAcknowledgement;
import org.jpac.ef.SubscriptionTransport;
import org.jpac.ef.Transceive;
import org.jpac.ef.Unsubscribe;
import org.jpac.ef.UnsubscribeAcknowledgement;
import org.jpac.vioss.IllegalUriException;
import org.jpac.vioss.IoSignal;

/**
 *
 * @author berndschuster
 */
public class IOHandler extends org.jpac.vioss.IOHandler{
    private final static String  HANDLEDSCHEME       = "EF";
    private final static int     CONNECTIONRETRYTIME = 1000;//ms  

    public enum State         {IDLE, CONNECTING, SUBSCRIBING, TRANSCEIVING, CLOSINGCONNECTION, STOPPING, STOPPED};  
    
    private State                 state;
    private Connection            connection;
    private ConnectionRunner      connectionRunner;
    private SubscriptionRunner    subscriptionRunner;
    private CloseConnectionRunner closeConnectionRunner;
    private AsynchronousTask      currentlyActiveRunner;
    private boolean               connected;
    private boolean               subscribed;
    private boolean               connectionClosed;
    private boolean               operationPending;
    private boolean               justEnteredState;
    
    private HashMap<String, RemoteSignalInfo> listOfRemoteSignalInfos;
    private HashMap<Integer, SignalTransport> listOfClientInputTransports; 
    private HashMap<Integer, SignalTransport> listOfClientOutputTransports; 
    
    private Transceive transceive;
    
    public IOHandler(URI uri, SubnodeConfiguration subnodeConfiguration) throws IllegalUriException {
        super(uri, subnodeConfiguration);
        if (!getHandledScheme().equals(uri.getScheme().toUpperCase())){
            throw new IllegalUriException("scheme '" + uri.getScheme() + "' not handled by " + toString());
        }
        this.connectionRunner             = new ConnectionRunner(this + " connection runner");
        this.subscriptionRunner           = new SubscriptionRunner(this + "subscription runner");
        this.closeConnectionRunner        = new CloseConnectionRunner(this + "close connection runner");
        this.state                        = State.IDLE;
        this.listOfClientInputTransports  = new HashMap<>();
        this.listOfClientOutputTransports = new HashMap<>();
        this.transceive                   = new Transceive(this.listOfClientInputTransports, this.listOfClientOutputTransports);
    }

    @Override
    public void run(){
        try{
            switch(state){
                case IDLE:
                    connected            = false;
                    subscribed           = false;
                    connectionClosed     = false;
                    justEnteredState     = true;
                    state                = State.CONNECTING;
                    //connect right away
                case CONNECTING:
                    operationPending = connecting(justEnteredState);
                    justEnteredState = false;
                    if (!operationPending){
                        if (connected){
                            state            = State.SUBSCRIBING;
                            justEnteredState = true;
                        }
                        else{
                            state            = State.STOPPING;
                            justEnteredState = true;
                        }
                    }
                    break;
                case SUBSCRIBING:
                    operationPending = subscribing(justEnteredState);
                    justEnteredState = false;
                    if (!operationPending){
                        if (subscribed){
                            state            = State.TRANSCEIVING;
                            justEnteredState = true;
                        }
                        else{
                            state            = State.CLOSINGCONNECTION;
                            justEnteredState = true;
                        }
                    }
                    break;
                case TRANSCEIVING:
                    justEnteredState = false;                            
                    try{
                        transceiving();
                    }
                    catch(Exception exc){
                        //server did not respond in time. Connection is supposed to be broken
                        //Just invalidate input signals and reconnect
                        invalidateInputSignals();
                        Log.error(this + " : Connection lost. Cause: " + exc);
                        state            = State.IDLE;
                        justEnteredState = true;                            
                    }
//                    catch(Exception exc){
//                        Log.error("Error: ", exc);
//                        state            = State.CLOSINGCONNECTION;
//                        justEnteredState = true;                            
//                    }
                    break;
                case CLOSINGCONNECTION:
                    invalidateInputSignals();
                    operationPending = closingConnection(justEnteredState);
                    justEnteredState = true;
                    if (!operationPending){
                        if (connectionClosed){
                            //try to reestablish connection
                            state            = State.IDLE;
                            justEnteredState = true;
                        }
                        else{
                            //connection failed to be stopped regulary
                            //just stop processing
                            state            = State.STOPPED;
                            justEnteredState = true;                            
                        }
                    }                        
                    break;
                case STOPPING:
                    justEnteredState = false;                            
                    try{
                        invalidateInputSignals();
                        if (connectionRunner.isRunning()){
                            connectionRunner.terminate();
                        }
                        if (connected){
                           unsubscribeSignals();
                           closeConnection();
                        }   
                    }
                    catch(Exception exc){
                        Log.error("Error: " + exc);
                    }
                    connected = false;
                    Log.info(this + " stopped.");
                    state            = State.STOPPED;
                    justEnteredState = true;                            
                    break;                
                case STOPPED:
                    justEnteredState = false;                            
                    //do nothing
                    break;
            }
        }
        catch(Exception exc){
            Log.error("Error:", exc);
            state            = State.STOPPING;//stop processing
            justEnteredState = true;                            
        }
        catch(Error err){
            Log.error("Error:", err);
            state            = State.STOPPING;//stop processing
            justEnteredState = true;                            
        }
    }
    
    @Override
    public void prepare() {
        try{
            Log.info("starting up " + this);
            setProcessingStarted(true);        
        }
        catch(Exception exc){
            Log.error("Error: ", exc);
        }
        catch(Error err){
            Log.error("Error: ", err);
        }
    }

    @Override
    public void stop() {
        Log.info("shutting down " + this);
        connectionRunner.terminate();
        subscriptionRunner.terminate();
        if (currentlyActiveRunner != null){
            currentlyActiveRunner.terminate();
        }
        closeConnection();
        state = State.STOPPING;
    }

    /**
     * is called in every cycle while in state CONNECTING
     */
    protected boolean connecting(boolean firstCall) throws WrongUseException, InconsistencyException{
        boolean pending = true;
        if (!connected){
            if (firstCall){                
                connectionRunner.start();
            }
            else{
                //connect to plc in progress 
                if (connectionRunner.isFinished()){
                    if(connectionRunner.isConnectionEstablished()){
                       connection = connectionRunner.getConnection();
                       connectionRunner.terminate();
                       connected  = true;
                    }
                    else{
                       connected  = false;
                    }
                    pending = false;
                }
            }
        }
        else{
            throw new InconsistencyException("might not be called in connected state");
        }
        return pending;
    };
    
    /**
     * is called in every cycle while in state SUBSCRIBING
     */
    protected boolean subscribing(boolean firstCall) throws WrongUseException, InconsistencyException{
        boolean pending = true;
        if (firstCall){
            subscriptionRunner.start();
        }
        else{
            if (subscriptionRunner.isFinished()){
                subscribed = subscriptionRunner.isSubscriptionSucceeded();
                subscriptionRunner.terminate();
                pending = false;
            }
        }
        return pending;
    };

    /**
     * is called in every cycle while in state TRANSCEIVING
     */
    protected boolean transceiving() throws TimeoutException, InterruptedException, SignalAccessException{
        boolean                   allSignalsProperlyTransferred = true;
        //put out output signals
        synchronized(listOfClientOutputTransports) {
	        for(Signal ios: getOutputSignals()){
	        	IoSignal ioSig = (IoSignal)ios;
	            if (ioSig.isToBePutOut() || connection.isJustConnected()){
	                //transmit signal on first transmission of a new connection or if it has changed in last cycle
	            	ioSig.resetToBePutOut();
	            	ioSig.checkOut();//transfer value to RemoteSignalInfo
	            	((RemoteSignalInfo)ioSig.getRemoteSignalInfo()).getSignalTransport().setValue(((RemoteSignalInfo)ioSig.getRemoteSignalInfo()).getValue());
	            }
	        }
        }
        //start transception
        connection.getClientHandler().transact(transceive);
        connection.resetJustConnected();//reset justConnected flag if not already reset
        connection.getClientHandler().resetTransactionInProgress();
        //propagate input signals
        synchronized(listOfClientInputTransports) {
	        for(Signal ios: getInputSignals()){
	        	IoSignal ioSig = (IoSignal)ios;
	        	ioSig.getRemoteSignalInfo().setValue(((RemoteSignalInfo)ioSig.getRemoteSignalInfo()).getSignalTransport().getValue());
	            ioSig.checkIn();
	        }
        }
        return allSignalsProperlyTransferred;
    };    
    
    protected boolean closingConnection(boolean firstCall) throws IOException, WrongUseException{
        boolean pending = true;
        if (connected){
            if (firstCall){                
                closeConnectionRunner.start();
            }
            else{
                //connect to plc in progress 
                if (closeConnectionRunner.isFinished()){
                    if (!closeConnectionRunner.isCloseOperationSucceeded()){
                        Log.error("Failed to close connection");
                    }
                    //treat connection from my side as closed anyway
                    connectionClosed = true;
                    connected        = false;
                    pending          = false;
                }
            }
        }
        else{
            throw new InconsistencyException("might not be called in unconnected state");
        }
        return pending;
    };
    
    protected void prepareSignalsForTransfer(){
        //assign local IoSignal's to remote counterpart ...
        for (Signal ios: getInputSignals()){
            RemoteSignalInfo si = listOfRemoteSignalInfos.get(((org.jpac.vioss.IoSignal)ios).getPath());
            if (si != null){
                ((org.jpac.vioss.IoSignal)ios).setRemoteSignalInfo(si);
                ((RemoteSignalInfo)((org.jpac.vioss.IoSignal)ios).getRemoteSignalInfo()).getSignalTransport().setHandle(si.getHandle());             
            }
        }
        for (Signal ios: getOutputSignals()){
            RemoteSignalInfo si = listOfRemoteSignalInfos.get(((org.jpac.vioss.IoSignal)ios).getPath());
            if (si != null){
                ((org.jpac.vioss.IoSignal)ios).setRemoteSignalInfo(si);
                ((RemoteSignalInfo)((org.jpac.vioss.IoSignal)ios).getRemoteSignalInfo()).getSignalTransport().setHandle(si.getHandle());             
            }
        }        
    }
            
    protected void logIoSignalsWhichFailedToConnect(){
        for (Signal ios: getInputSignals()){
            if (((org.jpac.vioss.IoSignal)ios).getRemoteSignalInfo() == null){
                Log.error("failed to retrieve signal info for " + ((org.jpac.vioss.IoSignal)ios).getUri());
            }
        }
        for (Signal ios: getOutputSignals()){
            if (((org.jpac.vioss.IoSignal)ios).getRemoteSignalInfo() == null){
                Log.error("failed to retrieve signal info for " + ((org.jpac.vioss.IoSignal)ios).getUri());
            }
        }
    }   
    
    protected boolean subscribeSignals(){
        boolean done = false;
        try{
            ArrayList<SubscriptionTransport> subscriptionTransports = new ArrayList<>();

            for (Signal ios: getInputSignals()){
            	RemoteSignalInfo rsi = (RemoteSignalInfo)((org.jpac.vioss.IoSignal)ios).getRemoteSignalInfo();
                if (rsi != null){
                    SubscriptionTransport st = new SubscriptionTransport(rsi.getHandle(), rsi.getType(), ios.getIoDirection());
                    subscriptionTransports.add(st);
                    listOfClientInputTransports.put(rsi.getHandle(), rsi.getSignalTransport());
                }
            }
            for (Signal ios: getOutputSignals()){
            	RemoteSignalInfo rsi = (RemoteSignalInfo)((org.jpac.vioss.IoSignal)ios).getRemoteSignalInfo();
                if (rsi != null){                    
                	SubscriptionTransport st = new SubscriptionTransport(rsi.getHandle(), rsi.getType(), ios.getIoDirection());
                    subscriptionTransports.add(st);
                    listOfClientOutputTransports.put(rsi.getHandle(), rsi.getSignalTransport());
                }
            }
            if (subscriptionTransports.size() > 0){
                Subscribe subscribe = new Subscribe(subscriptionTransports);
                SubscribeAcknowledgement sa = (SubscribeAcknowledgement)connection.getClientHandler().transact(subscribe);
                for(int i = 0; i < subscriptionTransports.size(); i++){
                    if (sa.getListOfResults().get(i) != Result.NoFault.getValue()){
                        Log.error("Failed to subscribe signal " + getSignalIdentifierByRemoteHandle(subscriptionTransports.get(i).getHandle()) +" , error code : " + Result.fromInt(sa.getListOfResults().get(i)));
                    }
                }
            }
            else{
                Log.error("no signals to be subscribed ");
            }
            done = true;
        }
        catch(Exception exc){
            Log.error("general error occured while subscribing signals", exc);
            done = false;
        }
        return done;
    }
    
    protected void unsubscribeSignals(){
        try{
            ArrayList<SubscriptionTransport> subscriptionTransports = new ArrayList<>();

            for (Signal ios: getInputSignals()){
            	RemoteSignalInfo rsi = (RemoteSignalInfo)((org.jpac.vioss.IoSignal)ios).getRemoteSignalInfo();
                if (rsi != null){                    
                    SubscriptionTransport st = new SubscriptionTransport(rsi.getHandle(), rsi.getType(), ios.getIoDirection());
                    subscriptionTransports.add(st);
                    listOfClientInputTransports.remove(rsi.getHandle());
                }
            }
            for (Signal ios: getOutputSignals()){
            	RemoteSignalInfo rsi = (RemoteSignalInfo)((org.jpac.vioss.IoSignal)ios).getRemoteSignalInfo();
                if (rsi != null){                    
                    SubscriptionTransport st = new SubscriptionTransport(rsi.getHandle(), rsi.getType(), ios.getIoDirection());
                    subscriptionTransports.add(st);
                    listOfClientOutputTransports.remove(rsi.getHandle());
                }
            }
            Unsubscribe unsubscribe = new Unsubscribe(subscriptionTransports);
            UnsubscribeAcknowledgement usa = (UnsubscribeAcknowledgement)connection.getClientHandler().transact(unsubscribe);
            for(int i = 0; i < subscriptionTransports.size(); i++){
                if (usa.getListOfResults().get(i) != Result.NoFault.getValue()){
                    Log.error("Failed to unsubscribe signal for connection to " + getSignalIdentifierByRemoteHandle(subscriptionTransports.get(i).getHandle()) + ", error code : " + Result.fromInt(usa.getListOfResults().get(i)));
                }
            }
        }
        catch(Exception exc){
            Log.error("general error occured while unsubscribing signals", exc);
        }
    }
    
    protected String getSignalIdentifierByRemoteHandle(int handle) {
    	String signalIdentifier = null;
    	signalIdentifier = listOfRemoteSignalInfos.values().stream().filter((si) -> si.getHandle() == handle).findFirst().get().getIdentifier();
    	return signalIdentifier;
    }

    protected void invalidateInputSignals() throws SignalAccessException{
        for (Signal ios: getInputSignals()){
            ios.invalidate();
        }        
    }
    
    protected boolean closeConnection(){
        boolean done = false;
        try{
            if (connection != null ){
                unsubscribeSignals();
               //TODO connection.close();
                done = true;
            }
        }
        finally{
            connected = false;
        }
        return done;
    }
    
    public Connection getConnection(){
        return this.connection;
    }
    
    public HashMap<Integer, SignalTransport> getListOfClientOutputTransports(){
        return listOfClientOutputTransports;
    }
    
    public HashMap<Integer, SignalTransport> getListOfClientInputTransports(){
        return listOfClientInputTransports;
    }
    
    @Override
    public boolean handles(URI uri) {
        boolean isHandledByThisInstance = false;
        try{
            isHandledByThisInstance  = uri != null;
            isHandledByThisInstance &= this.getUri().getScheme().equals(uri.getScheme());
            InetAddress[] ia         = InetAddress.getAllByName(this.getUri().getHost());
            InetAddress[] ib         = InetAddress.getAllByName(uri.getHost());
            isHandledByThisInstance &= ia[0].equals(ib[0]);
            isHandledByThisInstance &= this.getUri().getPort() == uri.getPort();
        }
        catch(UnknownHostException exc){};
        return isHandledByThisInstance;
    }
    
    @Override
    public String getHandledScheme() {
        return HANDLEDSCHEME;
    }    
    
    @Override
    public boolean isFinished() {
        return state == State.STOPPING;
    }
    
    
    class ConnectionRunner extends AsynchronousTask{ 
        private boolean     connected;
        private Connection  connection;
        private boolean     running;
        private int         browseAttempts;
        
        public ConnectionRunner(String identifier){
            super(identifier);
        }
        
        @Override
        public void doIt() throws ProcessException {
            boolean   exceptionOccured = false;
            
            connected = false;
            running   = true;
            try{
                if (currentlyActiveRunner == null){
                    currentlyActiveRunner = this;
                }
                else{
                    throw new InconsistencyException("runners should not be invoked in parallel.");
                }
                Log.info("establishing connection for " + getInputSignals().size() + " input and " + getOutputSignals().size() + " output signals ...");
                browseAttempts = 0;
                do{
                                 	
                    do{
                        try{
                        	connection = new Connection(getUri().getHost(), getUri().getPort());
                            //wait, until plc is running
                            connected  = true;
                        }
                        catch(Exception exc){
                            if (Log.isDebugEnabled())Log.error("Error:", exc);
                            try{Thread.sleep(CONNECTIONRETRYTIME);}catch(InterruptedException ex){/*cannot happen*/};
                        }
                    }
                    while(!connected && !isTerminated());
                    if (connected && !isTerminated()){
                        //try to retrieve variable handles
                        try{
                            //retrieve index of remote signals
                        	browseAttempts++;
                            Browse                browse    = new Browse();
                            BrowseAcknowledgement browseAck = (BrowseAcknowledgement)connection.getClientHandler().transact(browse);
                            listOfRemoteSignalInfos         = new HashMap<>(browseAck.getListOfGetHandleAcks().size());
                            browseAck.getListOfGetHandleAcks().forEach((gha) ->	listOfRemoteSignalInfos.put(gha.getSignalIdentifier(), new RemoteSignalInfo(gha.getSignalIdentifier(), gha.getSignalType(), gha.getHandle(), new SignalTransport(gha.getHandle(), gha.getSignalType()))));
                            prepareSignalsForTransfer();
                            //log signals which failed to connect
                            logIoSignalsWhichFailedToConnect();        
                        }
                        catch(Exception exc){
                            //close connection
                            //TODO try{closeConnection();}catch(Exception ex){};
                            //TODO exceptionOccured = true;
                            if (Log.isDebugEnabled()){Log.error("Error:", exc);};
                            connected = false;
                            try{Thread.sleep(CONNECTIONRETRYTIME);}catch(InterruptedException ex){/*cannot happen*/};//TODO                                                        
                        }
                    }
                    else {
                        try{Thread.sleep(CONNECTIONRETRYTIME);}catch(InterruptedException ex){/*cannot happen*/};
                    }
                }
                while(!connected && !isTerminated() && !exceptionOccured);
                if (connected){
                    Log.info("... connection established. " + browseAttempts + " attempts made to get signal dictionary");            
                }
            }
            finally{
                running               = false;
                currentlyActiveRunner = null;  
            }
        }
        
        public boolean isConnectionEstablished(){
            return connected;
        }

        public Connection getConnection(){
            return connection;
        }

        public boolean isRunning(){
            return running;
        }
    }    
    
    class SubscriptionRunner extends AsynchronousTask{
        boolean subscriptionSucceeded = false;
        boolean running               = false;
        
        public SubscriptionRunner(String identifier){
            super(identifier);
        }
        
        @Override
        public void doIt() throws ProcessException {
            try{
                if (currentlyActiveRunner == null){
                    currentlyActiveRunner = this;
                }
                else{
                    throw new InconsistencyException("runners should not be invoked in parallel.");
                }
                Log.debug("subscribing signals ...");
                running = true;
                subscriptionSucceeded = subscribeSignals();
                running = false;
                Log.debug("... subscription of signals done. Success = " + subscriptionSucceeded);        
            }
            finally{
                running               = false;
                currentlyActiveRunner = null;                
            }
        }
        public boolean isSubscriptionSucceeded(){
            return subscriptionSucceeded;
        }

        public boolean isRunning(){
            return running;
        }
    }    

    class CloseConnectionRunner extends AsynchronousTask{
        boolean closeOperationSucceeded = false;
        boolean running                 = false;
        
        public CloseConnectionRunner(String identifier){
            super(identifier);
        }
        
        @Override
        public void doIt() throws ProcessException {
            try{
                if (currentlyActiveRunner == null){
                    currentlyActiveRunner = this;
                }
                else{
                    throw new InconsistencyException("runners should not be invoked in parallel.");
                }
                Log.debug("closing connection ...");
                running = true;
                closeOperationSucceeded = closeConnection();
                running = false;
                Log.debug("... connection closed. Success = " + closeOperationSucceeded);            
            }
            finally{
                running               = false;
                currentlyActiveRunner = null;                
            }
        }

        public boolean isCloseOperationSucceeded(){
            return closeOperationSucceeded;
        }

        public boolean isRunning(){
            return running;
        }
    }    
}
