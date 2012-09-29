/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : RemoteSignalInput.java
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
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import org.apache.log4j.Logger;

/**
 *
 * @author berndschuster
 */
public class RemoteSignalInput extends Observable implements Assignable{
    static Logger Log = Logger.getLogger("jpac.Remote");
    
    private class ConnectionTask{
        private ConnTask task;
        private Object   targetSignal;

        private ConnectionTask(ConnTask task, Signal targetSignal) {
            this.task         = task;
            this.targetSignal = targetSignal;
        }  
    }
    
    private   enum ConnTask{CONNECT,DISCONNECT};    

    private RemoteSignalTransport transport;
    private int                   index;
    private boolean               connectedAsTarget;  
    private AbstractModule        containingModule;
    private String                identifier;
    
    private String                host;
    private int                   port;
    private String                signalName;
    private String                remoteSignalName;
    private List<Signal>          observingSignals;
    private List<ConnectionTask>  connectionTasks;
    
    
    public RemoteSignalInput(AbstractModule containingModule, String identifier, String host, int port, String signalName){
        super();
        this.containingModule     = containingModule;
        this.identifier           = identifier;
        this.host                 = host;
        this.port                 = port;
        this.signalName           = signalName;
        this.remoteSignalName     = RemoteSignalServer.getUrl() + "/" + containingModule.getQualifiedName() + "." + identifier;
        this.observingSignals     = Collections.synchronizedList(new ArrayList<Signal>());
        this.connectionTasks      = Collections.synchronizedList(new ArrayList<ConnectionTask>());
        this.connectedAsTarget    = false;
        this.index                = RemoteSignalRegistry.getInstance().addInput(this);        
        this.transport            = new RemoteSignalTransport(this.index, identifier, signalName);//TODO ??      
    }

    protected void handleConnections() throws SignalAlreadyConnectedException{
        //handle connection/disconnection of signals requested during last cycle
        //called inside the automation controller only
        synchronized(connectionTasks){
            if (!connectionTasks.isEmpty()){
                for (ConnectionTask ct: connectionTasks){
                    switch(ct.task){
                        case CONNECT:
                            deferredConnect((Signal)ct.targetSignal);
                            break;
                        case DISCONNECT:
                            deferredDisconnect((Signal)ct.targetSignal);
                            break;
                    }
                }
                //remove all entries inside the connection task list.
                connectionTasks.clear();
            }   
        }
    }
    
    /**
     * used to connect a remote signal input to a signal. One remote signal input can be connected
     * to multiple signals.
     * The connection is unidirectional: Changes of the connecting remote signal input (remoteSignalInput) will be
     * propagated to the signals it is connected to (targetSignal): remoteSignalInput.connect(targetSignal).
     * @param targetSignal
     */
    public void connect(Signal targetSignal) throws SignalAlreadyConnectedException{
        if (Log.isDebugEnabled()) Log.debug(this + ".connect(" + targetSignal + ")");
        if (targetSignal.isConnectedAsTarget()){
            throw new SignalAlreadyConnectedException(targetSignal);
        }
        connectionTasks.add(new ConnectionTask(ConnTask.CONNECT, targetSignal));
    }
    
    /**
     * used to disconnect a signal from another signal
     * @param targetSignal
     */
    public void disconnect(Signal targetSignal){
        if (Log.isDebugEnabled()) Log.debug(this + ".disconnect(" + targetSignal + ")");
        connectionTasks.add(new ConnectionTask(ConnTask.DISCONNECT, targetSignal));
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
    
    public String getIdentifier() {
        return identifier;
    }

    public AbstractModule getContainingModule() {
        return containingModule;
    }
    
    public String getHost(){
        return host;
    }
    
    public int getPort(){
        return port;
    }
    
    public String getSignalName(){
        return signalName;
    }
    
    public void invalidate(){
        transport.setValid(false);
    }
    
    public boolean isCompatible(Assignable item) {
        return true;//RemoteSignalOutput is independent of type
    }
    
    public void setIndex(int index){
        this.index = index;
    }
    
    RemoteSignalTransport getTransport(){
        return transport;
    }
    
    @Override
    public String toString(){
        return getClass().getSimpleName() + "(//" + host + ':' + port + '/' + signalName + ")";
    }
}
