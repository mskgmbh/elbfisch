/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : Signal.java
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

import java.lang.Thread.State;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Supplier;
import org.slf4j.LoggerFactory;
import org.jpac.alarm.Alarm;
import org.jpac.vioss.IoCharString;
import org.jpac.vioss.IoDecimal;
import org.jpac.vioss.IoLogical;
import org.jpac.vioss.IoSignedInteger;
import org.slf4j.Logger;

/**
 * implements the base features of a signal
 * 
 */
public abstract class Signal extends Observable implements Observer {
	public final static String PROXYQUALIFIER = "$Proxy";     
	
    private   enum ConnTask{CONNECT,DISCONNECT, REMOTECONNECT, REMOTEDISCONNECT, SIGNALOBSERVERCONNECT, SIGNALOBSERVERDISCONNECT};
    
    static    Logger Log = LoggerFactory.getLogger("jpac.Signal");
    private   String                     identifier;
    protected long                       lastChangeCycleNumber;
    protected long                       propagatedLastChangeCycleNumber;    
    protected long                       lastChangeNanoTime;
    protected JPac                       jPac;
    private   boolean                    connectedAsTarget;
    protected AbstractModule             containingModule;
    private   Set<Signal>                observingSignals;
    @SuppressWarnings("deprecation")
	private   Set<RemoteSignalOutput>    observingRemoteSignalOutputs;
    private   Queue<ConnectionTask>      connectionTasks;
    protected Supplier<?>                intrinsicFunction;
    
    protected Value                      value;
    protected Value                      propagatedValue;
    
    protected boolean                    initializing;
    protected boolean                    justConnectedAsSource;
    protected boolean                    intrinsicFunctionExceptionLogged;
    protected boolean                    inApplyIntrinsicFunction;
    protected IoDirection                ioDirection;
    
    @SuppressWarnings("deprecation")
	public Signal(AbstractModule containingModule, String identifier, Supplier<?> intrinsicFunction, IoDirection ioDirection) throws SignalAlreadyExistsException{
        super();
        this.identifier                       = identifier;
        this.intrinsicFunction                = intrinsicFunction;
        this.ioDirection                      = ioDirection;
        this.lastChangeCycleNumber            = 0L;
        this.propagatedLastChangeCycleNumber  = 0L;
        this.lastChangeNanoTime               = 0L;
        this.jPac                             = JPac.getInstance();
        this.connectedAsTarget                = false;
        this.containingModule                 = containingModule;
        this.observingSignals                 = Collections.synchronizedSet(new HashSet<Signal>());
        this.observingRemoteSignalOutputs     = Collections.synchronizedSet(new HashSet<RemoteSignalOutput>());
        this.connectionTasks                  = new ArrayBlockingQueue<ConnectionTask>(100);
        this.initializing                     = false;
        this.justConnectedAsSource            = false;
        this.intrinsicFunctionExceptionLogged = false;
        this.inApplyIntrinsicFunction         = false;

        this.value              		      = getTypedValue();
        this.propagatedValue    			  = getTypedValue(); 

        if (!containingModule.isRunLocally() && (getIoDirection() == IoDirection.INPUT || getIoDirection() == IoDirection.OUTPUT)) {
        	//the containing module will be run on a separate elbfisch instance and 
        	//this signal is explicitly designated as being either INPUT or OUTPUT:
        	this.intrinsicFunction = null; //disable intrinsicFunction on this instance
        	//The containing module is either proxy or inactive
            if (containingModule.isProxy()) {
	        	//instantiate an io signal as a proxy used to access the remote counterpart of this signal
	        	//and connect it to this signal according to the desired io direction
            	if (this instanceof IoSignal) {
            		if (!this.isProxyIoSignal()) {
		            	Signal ioSig = getTypedProxyIoSignal(containingModule.getEffectiveElbfischInstance(), getIoDirection());
			        	switch(getIoDirection()) {
			        	case INPUT:
			        		//pass incoming io signal from remote counterpart to this
			        		ioSig.connect(this);
			        		break;
			        	case OUTPUT:
			        		//pass outgoing io signal through to remote counterpart
			        		this.connect(ioSig);
			        		break;
			        	case INOUT:
			        	case UNDEFINED:
			        		//INOUTs and UNDEFINEDs are not handled
			        	}
            		}
            	} else {
            		//direction of internal signals are inverse
	            	Signal ioSig = getTypedProxyIoSignal(containingModule.getEffectiveElbfischInstance(), invert(getIoDirection()));
		        	switch(getIoDirection()) {
		        	case INPUT:
		        		//pass incoming signal through to remote counterpart
		        		this.connect(ioSig);
		        		break;
		        	case OUTPUT:
		        		//pass incoming signal from remote counterpart to this
		        		ioSig.connect(this);
		        		break;
		        	case INOUT:
		        	case UNDEFINED:
		        		//INOUTs and UNDEFINEDs are not handled
		        	}            		
            	}
        	}
        }
        SignalRegistry.getInstance().add(this);
    }
        
    protected IoDirection invert(IoDirection ioDirection) {
    	return ioDirection == IoDirection.INPUT ? IoDirection.OUTPUT : IoDirection.INPUT;
    }
    
    protected boolean isProxyIoSignal() {
    	return this.getIdentifier().endsWith(PROXYQUALIFIER);
    }
    
    /**
     * used to propagate the signals state synchronously at start of cycle
     */
    protected void propagate() throws SignalInvalidException{
        //if the signal was connected as source signal in the last cycle
        //let its value be propagated to all observing signals
        if (isJustConnectedAsSource()){
            super.setChanged();
        }
        setJustConnectedAsSource(false);
        //propagate changes occured during the last cycle
        //avoid propagation of changes occured during the actual propagation phase
        if (hasChanged()){
            propagatedLastChangeCycleNumber = JPac.getInstance().getCycleNumber();
            if (Log.isDebugEnabled()) Log.debug ("propagate signal " + this);
            propagateSignalInternally();
            notifyObservers();
        }
    }
    
    @SuppressWarnings("deprecation")
	protected void handleConnections() throws SignalAlreadyConnectedException{
        //handle connection/disconnection of signals requested during last cycle
        //called inside the automation controller only
        while(!connectionTasks.isEmpty()){
            ConnectionTask ct = connectionTasks.remove();
            switch(ct.task){
                case CONNECT:
                    deferredConnect((Signal)ct.target);
                    break;
                case DISCONNECT:
                    deferredDisconnect((Signal)ct.target);
                    break;
                case REMOTECONNECT:
                    deferredConnect((RemoteSignalOutput)ct.target);
                    break;
                case REMOTEDISCONNECT:
                    deferredDisconnect((RemoteSignalOutput)ct.target);
                    break;
                case SIGNALOBSERVERCONNECT:
                    deferredConnect((SignalObserver)ct.target);
                    break;
                case SIGNALOBSERVERDISCONNECT:
                    deferredDisconnect((SignalObserver)ct.target);
                    break;
            }
        }
    }
    
    /**
     * used to connect a signal to another signal. One signal can be connected
     * to multiple signals.
     * The connection is unidirectional: Changes of the connecting signal (sourceSignal) will be
     * propagated to the signals it is connected to (targetSignal): sourceSignal.connect(targetSignal).
     * @param targetSignal
     */
    public void connect(Signal targetSignal) throws SignalAlreadyConnectedException{
        synchronized(this){
        	if (containingModule.isRunLocally() || (containingModule.isProxy() && (this.isProxyIoSignal() || targetSignal.isProxyIoSignal()))) {
        		//owning module is run locally or this or the target signal is a proxy signal: perform connection
	            if (Log.isDebugEnabled()) Log.debug(this + ".connect(" + targetSignal + ")");
	            if (targetSignal.isConnectedAsTarget()){
	                throw new SignalAlreadyConnectedException(targetSignal);
	            }
	            if (targetSignal.getIntrinsicFunction() != null){
	                throw new SignalAccessException("cannot connect to signal with initrinsic function set: " + targetSignal);
	            }
	            try{
	                connectionTasks.add(new ConnectionTask(ConnTask.CONNECT, targetSignal));
	                targetSignal.setConnectedAsTarget(true);
	            }
	            catch(IllegalStateException exc){
	                Log.error("Error connectionTask queue full: ", exc);
	            }
	            catch(Exception exc){
	                Log.error("Error: ", exc);
	            }
        	} else {
	            if (Log.isDebugEnabled()) Log.debug(this + ".connect(" + targetSignal + "): Connection omitted because the owning module does not run on this instance");        		
        	}
        }
    }
    
    /**
     * used to disconnect a signal from another signal
     * @param targetSignal
     */
    public void disconnect(Signal targetSignal){
        synchronized(this){
            if (Log.isDebugEnabled()) Log.debug(this + ".disconnect(" + targetSignal + ")");
            try{
                connectionTasks.add(new ConnectionTask(ConnTask.DISCONNECT, targetSignal));
            }
            catch(IllegalStateException exc){
                Log.error("Error connectionTask queue full: ", exc);
            }
            catch(Exception exc){
                Log.error("Error: ", exc);
            }
        }
    }

    protected void deferredConnect(Signal targetSignal) throws SignalAlreadyConnectedException{
        if (Log.isDebugEnabled()) Log.debug(this + ".deferredConnect(" + targetSignal + ")");
//        if (targetSignal.isConnectedAsTarget()){
//            throw new SignalAlreadyConnectedException(targetSignal);
//        }
        if (targetSignal.getIntrinsicFunction() != null){
            throw new SignalAccessException("cannot connect to signal with initrinsic function set: " + targetSignal);
        }
        addObserver(targetSignal);
        observingSignals.add(targetSignal);
        //invoke propagation of the state of this signal to the new target
        setJustConnectedAsSource(true);       
    }
    
    protected void deferredDisconnect(Signal targetSignal){
        if (Log.isDebugEnabled()) Log.debug(this + ".deferredDisconnect(" + targetSignal + ")");
        deleteObserver(targetSignal);
        targetSignal.setConnectedAsTarget(false);
        //invalidate target signal
        try{targetSignal.invalidate();} catch(SignalAccessException exc) {/*cannot happen here*/};
        observingSignals.remove(targetSignal);
    }
    
    /**
     * used to connect a signal to a RemoteSignalOutput. One signal can be connected
     * to multiple RemoteSignalOutputs.
     * The connection is unidirectional: Changes of the connecting signal (sourceSignal) will be
     * propagated to the remote signal outputs it is connected to (targetSignal): sourceSignal.connect(targetSignal).
     * @param targetSignal
     */
    @SuppressWarnings("deprecation")
	public void connect(RemoteSignalOutput targetSignal) throws SignalAlreadyConnectedException{
        synchronized(this){
            if (Log.isDebugEnabled()) Log.debug(this + ".connect(" + targetSignal + ")");
//            if (targetSignal.isConnectedAsTarget()){
//                throw new SignalAlreadyConnectedException(targetSignal);
//            }
            try{
                connectionTasks.add(new ConnectionTask(ConnTask.REMOTECONNECT, targetSignal));
                targetSignal.setConnectedAsTarget(true);
            }
            catch(IllegalStateException exc){
                Log.error("Error connectionTask queue full: ", exc);
            }
            catch(Exception exc){
                Log.error("Error: ", exc);
            }
            //invoke propagation of the state of this signal to the new target
            setJustConnectedAsSource(true);
        }
    }
    
    /**
     * used to disconnect a signal from another signal
     * @param targetSignal
     */
    @SuppressWarnings("deprecation")
	public void disconnect(RemoteSignalOutput targetSignal){
        synchronized(this){
            if (Log.isDebugEnabled()) Log.debug(this + ".disconnect(" + targetSignal + ")");
            try{
                connectionTasks.add(new ConnectionTask(ConnTask.REMOTEDISCONNECT, targetSignal));
            }
            catch(IllegalStateException exc){
                Log.error("Error connectionTask queue full: ", exc);
            }
            catch(Exception exc){
                Log.error("Error: ", exc);
            }
        }
    }

    @SuppressWarnings("deprecation")
	protected void deferredConnect(RemoteSignalOutput targetSignal) throws SignalAlreadyConnectedException{
        if (Log.isDebugEnabled()) Log.debug(this + ".deferredConnect(" + targetSignal + ")");
        if (targetSignal.isConnectedAsTarget()){
            throw new SignalAlreadyConnectedException(targetSignal);
        }
        addObserver(targetSignal);
        observingRemoteSignalOutputs.add(targetSignal);
    }
    
    @SuppressWarnings("deprecation")
	protected void deferredDisconnect(RemoteSignalOutput targetSignal){
        if (Log.isDebugEnabled()) Log.debug(this + ".deferredDisconnect(" + targetSignal + ")");
        deleteObserver(targetSignal);
        targetSignal.setConnectedAsTarget(false);
        //invalidate target signal
        targetSignal.invalidate();
        observingRemoteSignalOutputs.remove(targetSignal);
    }

    /**
     * used to connect a signal to a signal observer. One signal can be connected
     * to multiple signal observers.
     * @param targetObserver
     */
    public void connect(SignalObserver targetObserver) throws SignalAlreadyConnectedException{
        synchronized(this){
            if (Log.isDebugEnabled()) Log.debug(this + ".connect(" + targetObserver + ")");
            if (targetObserver.isConnectedAsTarget()){
                throw new SignalAlreadyConnectedException(targetObserver);
            }
            try{
                connectionTasks.add(new ConnectionTask(ConnTask.SIGNALOBSERVERCONNECT, targetObserver));
                targetObserver.setConnectedAsTarget(true);
            }
            catch(IllegalStateException exc){
                Log.error("Error connectionTask queue full: ", exc);
            }
            catch(Exception exc){
                Log.error("Error: ", exc);
            }
        }
    }
    
    /**
     * used to disconnect a signal from another signal
     * @param targetObserver
     */
    public void disconnect(SignalObserver targetObserver){
        synchronized(this){
            if (Log.isDebugEnabled()) Log.debug(this + ".disconnect(" + targetObserver + ")");
            try{
                connectionTasks.add(new ConnectionTask(ConnTask.SIGNALOBSERVERDISCONNECT, targetObserver));
            }
            catch(IllegalStateException exc){
                Log.error("Error connectionTask queue full: ", exc);
            }
            catch(Exception exc){
                Log.error("Error: ", exc);
            }
        }
    }

    protected void deferredConnect(SignalObserver targetObserver) throws SignalAlreadyConnectedException{
        if (Log.isDebugEnabled()) Log.debug(this + ".deferredConnect(" + targetObserver + ")");
//        if (targetObserver.isConnectedAsTarget()){
//            throw new SignalAlreadyConnectedException(targetObserver);
//        }
        addObserver(targetObserver);
        //invoke propagation of the state of this signal to the new target
        setJustConnectedAsSource(true);
    }
    
    protected void deferredDisconnect(SignalObserver targetObserver){
        if (Log.isDebugEnabled()) Log.debug(this + ".deferredDisconnect(" + targetObserver + ")");
        deleteObserver(targetObserver);
        targetObserver.setConnectedAsTarget(false);
    }

    /**
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @return the identifier
     */
    public String getQualifiedIdentifier() {
        return getContainingModule() == null ? identifier : getContainingModule().getQualifiedName() + "." + identifier;
    }

    /**
     * @return the value
     */
    public Value getValue(){
        synchronized(this){        
            return accessedByForeignModule() ? propagatedValue : value;
        }
    }
    
    /**
     * used to set the intrinsic function of this signal.
     * @param intrinsicFunction 
     */
    protected void setIntrinsicFct(Supplier intrinsicFunction){
        assertSignalAccess();
        this.intrinsicFunction = intrinsicFunction;
    }

    /**
     * @return the value, if valid
     */
    protected Value getValidatedValue() throws SignalInvalidException {
        if (!accessedByForeignModule() && intrinsicFunction != null && !inApplyIntrinsicFunction){
            applyIntrinsicFunction();//reflect actual state of function to containing module immediately
        }
        if (!isValid()){
            throw new SignalInvalidException(this.toString());
        }
        return getValue();
    }
    
    /**
     * @param value the value to set
     */
    public void setValue(Value value) throws SignalAccessException {
        if (containingModule.getState() != State.TERMINATED){
            //containing module is alive. Handle this signal
            if (!initializing){
                //if not called to set the default value inside the constructor
                //check signal access policy
                assertSignalAccess();
                assertIntrinsicFunctionAccess();
            }
            if (this.value != null && value != null && !this.value.equals(value)){
               if (Log.isDebugEnabled()) Log.debug(this + ".set(" + value + ")");
               this.value.copy(value);
               setChanged();
            } else if (this.value == null && value != null){
                if (Log.isDebugEnabled()) Log.debug(this + ".set(" + value + ")");
                try{
                    this.value = value.clone();
                }
                catch(CloneNotSupportedException exc){
                    throw new SignalAccessException(exc.getMessage());
                }
               setChanged();
            };
        } else{
            //dead modules cannot have valid signals
            setValid(false);
        }
        
    }

    protected void setValueDeferred(Value value){
        JPac.getInstance().invokeLater(new SetValueRunner(this, value));
    }

    public void invalidateDeferred(){
        JPac.getInstance().invokeLater(new InvalidateRunner(this));
    }
    /**
     * @return the propagatedValue
     */
    protected Value getPropagatedValue() {
        return propagatedValue;
    }

    /**
     * @param propagatedValue the propagatedValue to set
     */
    protected void setPropagatedValue(Value propagatedValue) {
        this.propagatedValue.copy(propagatedValue);
    }

    public AbstractModule getContainingModule(){
        return containingModule;
    }

    public Set<Signal> getObservingSignals(){
        return observingSignals;
    }
    
    public IoDirection getIoDirection() {
    	return ioDirection;
    }
    
    @Override
    public String toString(){
        return getClass().getSimpleName() + "(" + containingModule.getName() + '.' + identifier + " = " + (isValid() ? getValue() : "???") + ")";
    }

    public boolean isValid() {
        boolean valid = false;
        synchronized(this){
            valid = accessedByForeignModule() || accessedByJPac() ? propagatedValue != null && propagatedValue.isValid() : value != null && value.isValid();
        }
        return valid;
    }

    protected void setValid(boolean valid) {
        if (value.isValid() != valid){
            if (Log.isDebugEnabled()) Log.debug(this + ".setValid(" + valid + ")");
            value.setValid(valid);
            setChanged();
        }
    }
    
//    protected void setPropagatedSignalValid(boolean valid){
//        if (Log.isDebugEnabled() && (propagatedValue.isValid() != valid)) Log.debug ("propagate change of valid state for signal " + this + " : " + valid);
//        propagatedValue.setValid(valid);
//    }
    
    /**
     * used to invalidate a signal in cases in which a module cannot guarantee the signals integrity
     * If set the intrinsic function is removed, too
     * @throws SignalAccessException 
     */
    public void invalidate() throws SignalAccessException{
        synchronized(this){
            assertSignalAccess();
            intrinsicFunction = null;
            setValid(false);
        }
    }

    /**
     * returns a ProcessEvent that will be fired, if the signal becomes valid
     * @return see above
     */
    public ProcessEvent becomesValid(){
        return new SignalValid(this);
    }

    /**
     * returns a ProcessEvent that will be fired, if the signal becomes invalid
     * @return see above
     */
    public ProcessEvent becomesInvalid(){
        return new SignalInvalid(this);
    }
 
    /**
     * @return the connectedAsTarget
     */
    public boolean isConnectedAsTarget() {
        return connectedAsTarget;
    }

    /**
     * @param connectedAsTarget the connectedAsTarget to set
     */
    public void setConnectedAsTarget(boolean connectedAsTarget) {
        this.connectedAsTarget = connectedAsTarget;
    }

    /**
     * signal has been changed during the actual cycle
     */
    @Override
    public void setChanged(){
        synchronized(this){
            lastChangeNanoTime     = System.nanoTime();
            lastChangeCycleNumber  = jPac.getCycleNumber();
            super.setChanged();//change flag of Observable
        }
    }
    
    /**
     * Returns true, if the signal has changed its value or got valid/invalid
     * Is true for exactly one cycle depending on the calling module:
     * For the containing module this method returns true starting at the time the change occurs
     * until the end of the cycle.
     * For every other consuming module this method returns true during the following cycle.
     * Calling this method on signals connected as target directly or indirectly to the changed signal
     * will return true on the following cycle regardless if the accessing module is the containing one or not 
     */
    public boolean isChanged(){
        boolean retChanged = false;
        synchronized(this){
            retChanged = accessedByForeignModule() || accessedByJPac() ? propagatedLastChangeCycleNumber == jPac.getCycleNumber() : lastChangeCycleNumber == jPac.getCycleNumber();
        }
        return retChanged;
    }
    
   /**
    * returns an unique handle for this signal
    * 
    */
    public int getHandle() {
    	return getQualifiedIdentifier().hashCode();
    }
    
    protected void assertContainingModule() throws SignalAccessException{
        if(accessedByForeignModule()){
            throw new SignalAccessException("signal " + this + " cannot be accessed() by foreign modules");
        }        
    }

    protected void assertSignalAccess() throws SignalAccessException{
        if(accessedByForeignModule()){
            throw new SignalAccessException("signal " + this + " cannot be altered by foreign modules");
        }
        //if connected as a target signal, it cannot be accessed by a module using set(...) directly
        if (isConnectedAsTarget() && !accessedByJPac()){
            throw new SignalAccessException("signal " + this + " cannot be altered directly, because it's connected as target ");
        }
    }
    
    protected void assertIntrinsicFunctionAccess() throws SignalAccessException{
        //if intrinsic function defined signal cannot be set() 
        if (!inApplyIntrinsicFunction && intrinsicFunction != null){
            throw new SignalAccessException("signal " + this + " cannot be set() because its controlled by an intrinsic function");
        }
    }
    
    
    @Override
    public void update(Observable o, Object arg){
        synchronized(this){
            try{
                //take over the valid state of the source signal ...
                setValid(((Signal)o).isValid());
                if (value.isValid()){
                    //...  and its value, if valid ...
                    updateValue(o, arg);
                }
                //propagate alteration of valid state and/or value
                propagate(); 
            }
            catch(Exception exc){
                Log.error("Error: ", exc);
            }
        }
    }
    
    private boolean accessedByForeignModule(){
        boolean isForeignModule;
        Thread  thread = Thread.currentThread();
        isForeignModule = !thread.equals(jPac) && !thread.equals(containingModule) || //access inside work
                          thread.equals(jPac) && jPac.getProcessedModule() != null && jPac.getProcessedModule() != containingModule; //access inside inEveryCycleDo()
//        isForeignModule = !(thread.equals(containingModule) || 
//                            (thread.equals(jPac) && (jPac.getProcessedModule() == null || jPac.getProcessedModule() == containingModule))
//                           );
        return isForeignModule;
    }
    
    private boolean accessedByJPac(){
        return Thread.currentThread().equals(jPac) && (jPac.getProcessedModule() == null);
    }
    
    protected boolean isJustConnectedAsSource() {
        return justConnectedAsSource;
    }

    protected void setJustConnectedAsSource(boolean justConnectedAsSource) {
        this.justConnectedAsSource = justConnectedAsSource;
    }
    
    protected Supplier getIntrinsicFunction(){
        return this.intrinsicFunction;
    }
    
    protected void applyIntrinsicFunction(){
        try{
            inApplyIntrinsicFunction = true;
            applyTypedIntrinsicFunction();
            intrinsicFunctionExceptionLogged = false;
        }
        catch(Exception exc){
            if (Log.isDebugEnabled() && !intrinsicFunctionExceptionLogged) Log.debug("Error: " + toString() + ": evaluation of initrinsic function failed because of " + exc.toString());
            intrinsicFunctionExceptionLogged = true;//log exceptions only once
            setValid(false);
        }
        finally{
            inApplyIntrinsicFunction = false;
        }
    }
    
    protected void setIoDirection(IoDirection ioDirection) {
    	this.ioDirection = ioDirection;
    }
    
    abstract protected boolean isCompatibleSignal(Signal signal);
    abstract protected void updateValue(Object o, Object arg) throws SignalAccessException;
    abstract protected void propagateSignalInternally() throws SignalInvalidException;    
    abstract protected void applyTypedIntrinsicFunction() throws Exception;
    abstract protected Value getTypedValue();
    abstract protected Signal getTypedProxyIoSignal(URI remoteElbfischInstance, IoDirection ioDirection);

    private class ConnectionTask{
        private ConnTask task;
        private Object   target;

        private ConnectionTask(ConnTask task, Signal targetSignal) {
            this.task   = task;
            this.target = targetSignal;
        }  

        @SuppressWarnings("deprecation")
		private ConnectionTask(ConnTask task, RemoteSignalOutput targetSignal) {
            this.task   = task;
            this.target = targetSignal;
        }  

        private ConnectionTask(ConnTask task, SignalObserver targetObserver) {
            this.task   = task;
            this.target = targetObserver;
        }  
    }
    
    private class SetValueRunner implements Runnable{
        private Signal signal;
        private Value  value;
        public SetValueRunner(Signal signal, Value value){
            this.signal = signal;
            this.value  = value;
        }
        @Override
        public void run() {
            try{signal.setValue(value);}catch(SignalAccessException exc){/*cannot happen*/};
        }        
    }

    private class InvalidateRunner implements Runnable{
        private Signal signal;
        public InvalidateRunner(Signal signal){
            this.signal = signal;
        }
        @Override
        public void run() {
            try{signal.invalidate();}catch(SignalAccessException exc){/*cannot happen*/};
        }        
    }
}