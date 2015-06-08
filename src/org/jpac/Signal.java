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

import java.util.Collections;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import org.apache.log4j.Logger;

/**
 * implements the base features of a signal
 * 
 */
public abstract class Signal extends Observable implements Observer {
        
    private   enum ConnTask{CONNECT,DISCONNECT, REMOTECONNECT, REMOTEDISCONNECT, SIGNALOBSERVERCONNECT, SIGNALOBSERVERDISCONNECT};
    
    static    Logger Log = Logger.getLogger("jpac.Signal");
    private   String                     identifier;
    protected long                       lastChangeCycleNumber;
    protected long                       propagatedLastChangeCycleNumber;    
    protected long                       lastChangeNanoTime;
    protected JPac                       jPac;
    protected boolean                    signalValid;
    protected boolean                    propagatedSignalValid;
    private   boolean                    connectedAsTarget;
    protected AbstractModule             containingModule;
    private   Set<Signal>                observingSignals;
    private   Set<RemoteSignalOutput>    observingRemoteSignalOutputs;
    private   Queue<ConnectionTask>      connectionTasks;
    
    protected Value                      value;
    protected Value                      propagatedValue;
    
    protected boolean                    initializing;
    protected boolean                    justConnectedAsSource;
    
    public Signal(AbstractModule containingModule, String identifier) throws SignalAlreadyExistsException{
        super();
        this.identifier                      = identifier;
        this.lastChangeCycleNumber           = 0L;
        this.propagatedLastChangeCycleNumber = 0L;
        this.lastChangeNanoTime              = 0L;
        this.jPac                            = JPac.getInstance();
        this.signalValid                     = false;//signal is initially invalid
        this.propagatedSignalValid           = false;//signal is initially invalid
        this.connectedAsTarget               = false;
        this.containingModule                = containingModule;
        this.observingSignals                = Collections.synchronizedSet(new HashSet<Signal>());
        this.observingRemoteSignalOutputs    = Collections.synchronizedSet(new HashSet<RemoteSignalOutput>());
        this.connectionTasks                 = new ArrayBlockingQueue<ConnectionTask>(100);
        this.value                           = null;
        this.propagatedValue                 = null; 
        this.initializing                    = false;
        this.justConnectedAsSource           = false;
        SignalRegistry.getInstance().add(this);
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
            setPropagatedSignalValid(signalValid);//propagate valid state of the signal
            propagatedLastChangeCycleNumber = JPac.getInstance().getCycleNumber();
            if (signalValid){
                //if signal valid, then propagate its value, too
                if (Log.isDebugEnabled()) Log.debug ("propagate signal " + this);
                propagateSignalInternally();
            }
            notifyObservers();
        }
    }
    
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
            if (Log.isDebugEnabled()) Log.debug(this + ".connect(" + targetSignal + ")");
            if (targetSignal.isConnectedAsTarget()){
                throw new SignalAlreadyConnectedException(targetSignal);
            }
            try{
                connectionTasks.add(new ConnectionTask(ConnTask.CONNECT, targetSignal));
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
        if (targetSignal.isConnectedAsTarget()){
            throw new SignalAlreadyConnectedException(targetSignal);
        }
        addObserver(targetSignal);
        targetSignal.setConnectedAsTarget(true);
        observingSignals.add(targetSignal);
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
    public void connect(RemoteSignalOutput targetSignal) throws SignalAlreadyConnectedException{
        synchronized(this){
            if (Log.isDebugEnabled()) Log.debug(this + ".connect(" + targetSignal + ")");
            if (targetSignal.isConnectedAsTarget()){
                throw new SignalAlreadyConnectedException(targetSignal);
            }
            try{
                connectionTasks.add(new ConnectionTask(ConnTask.REMOTECONNECT, targetSignal));
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

    protected void deferredConnect(RemoteSignalOutput targetSignal) throws SignalAlreadyConnectedException{
        if (Log.isDebugEnabled()) Log.debug(this + ".deferredConnect(" + targetSignal + ")");
        if (targetSignal.isConnectedAsTarget()){
            throw new SignalAlreadyConnectedException(targetSignal);
        }
        addObserver(targetSignal);
        targetSignal.setConnectedAsTarget(true);
        observingRemoteSignalOutputs.add(targetSignal);
    }
    
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
        if (targetObserver.isConnectedAsTarget()){
            throw new SignalAlreadyConnectedException(targetObserver);
        }
        addObserver(targetObserver);
        targetObserver.setConnectedAsTarget(true);
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
     * @return the value
     */
    public Value getValue(){
        synchronized(this){        
            return accessedByForeignModule() ? propagatedValue : value;
        }
    }

    /**
     * @return the value, if valid
     */
    protected Value getValidatedValue() throws SignalInvalidException {
        if (!isValid()){
            throw new SignalInvalidException(this.toString());
        }
        return getValue();
    }
    
    /**
     * @param value the value to set
     */
    protected void setValue(Value value) throws SignalAccessException {
        if (!initializing){
            //if not called to set the default value inside the constructor
            //check signal access policy
            assertSignalAccess();
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
        } else if (this.value != null && value == null){
           if (Log.isDebugEnabled()) Log.debug(this + ".set(" + value + ")");
           this.value = null;
           setChanged();
        };
        setValid(true);
    }

    protected void setValueDeferred(Value value){
        JPac.getInstance().invokeLater(new SetValueRunner(value));
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
    
    @Override
    public String toString(){
        return getClass().getSimpleName() + "(" + containingModule.getName() + '.' + identifier + " = " + (isValid() ? getValue() : "???") + ")";
    }

    /**
     * @return the signalValid
     */
    public boolean isValid() {
        boolean valid = false;
        synchronized(this){
            valid = accessedByForeignModule() || accessedByJPac() ? propagatedSignalValid : signalValid;
        }
        return valid;
    }

    /**
     * @param signalValid the signalValid to set
     */
    protected void setValid(boolean valid) {
        if (this.signalValid != valid){
            if (Log.isDebugEnabled()) Log.debug(this + ".setValid(" + valid + ")");
            this.signalValid = valid;
            setChanged();
        }
    }
    
    protected void setPropagatedSignalValid(boolean valid){
            if (Log.isDebugEnabled() && (propagatedSignalValid != valid)) Log.debug ("propagate change of valid state for signal " + this + " : " + valid);
            propagatedSignalValid = valid;
    }
    
    public void invalidate() throws SignalAccessException{
        synchronized(this){
            assertSignalAccess();
            setValid(false);
        }
    }

    /**
     * returns a ProcessEvent that will be fired, if the signal becomes valid
     * @param state
     * @return see above
     */
    public ProcessEvent becomesValid(){
        return new SignalValid(this);
    }

    /**
     * returns a ProcessEvent that will be fired, if the signal becomes invalid
     * @param state
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
    
    @Override
    public void update(Observable o, Object arg){
        synchronized(this){
            try{
                //take over the valid state of the source signal ...
                setValid(((Signal)o).isValid());
                if (signalValid){
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

    abstract protected boolean isCompatibleSignal(Signal signal);
    abstract protected void updateValue(Object o, Object arg) throws SignalAccessException;
    abstract protected void propagateSignalInternally() throws SignalInvalidException;    

    private class ConnectionTask{
        private ConnTask task;
        private Object   target;

        private ConnectionTask(ConnTask task, Signal targetSignal) {
            this.task   = task;
            this.target = targetSignal;
        }  

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
        Value value;
        public SetValueRunner(Value value){
            this.value = value;
        }
        @Override
        public void run() {
            try{setValue(value);}catch(SignalAccessException exc){/*cannot happen*/};
        }        
    }
}