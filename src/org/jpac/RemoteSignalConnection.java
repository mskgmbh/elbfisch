/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : RemoteSignalFrame.java
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

/**
 * represents a connection to a remote Jpac instance. Will be automatically created on instantiating remote signals
 * @author berndschuster
 */
public class RemoteSignalConnection {
    private String                       host;
    private int                          port;
    private List<RemoteSignalOutput>     outputSignals;
    private List<RemoteSignalInput>      inputSignals;
    private int                          inputIndex;
    private int                          outputIndex;
    private RemoteSignalOutputDaemon     remoteSignalOutputDaemon;
    //TODO private RemoteSignalInputDaemon     remoteSignalInputDaemon;
    private RemoteSignalFrame            outputFrame;
    private String                       remoteJPacInstance;
    
    RemoteSignalConnection(String host, int port){
        this.host          = host;
        this.port          = port;
        this.inputSignals  = Collections.synchronizedList(new ArrayList<RemoteSignalInput>());
        this.outputSignals = Collections.synchronizedList(new ArrayList<RemoteSignalOutput>());
        this.inputIndex    = 0;
        this.outputIndex   = 0;
        
        this.remoteSignalOutputDaemon = null;
        this.outputFrame              = new RemoteSignalFrame();
        this.remoteJPacInstance       = host + ":" + port; 
    }
        
    public String getHost(){
        return host;
    }
    
    public int getPort(){
        return port;
    }
    
    int addInput(RemoteSignalInput remoteInputSignal){
        remoteInputSignal.setIndex(inputIndex);
        inputSignals.add(inputIndex, remoteInputSignal);
        return inputIndex++;
    }
    
    int addOutput(RemoteSignalOutput remoteOutputSignal){
        remoteOutputSignal.setIndex(outputIndex);
        outputSignals.add(outputIndex, remoteOutputSignal);
        return outputIndex++;
    }
    
    public List<RemoteSignalInput> getInputSignals(){
        return inputSignals;
    }
    
    public List<RemoteSignalOutput> getOutputSignals(){
        return outputSignals;
    }
    
    public RemoteSignalOutputDaemon getOutputDaemon(){
        return remoteSignalOutputDaemon;
    }
    
    public void open() throws RemoteSignalException{
        RemoteSignalHandler remoteSignalHandler = null;
        try{
            //before this method can be called, all remote signals 
            //for this connection must have been properly registered.
            //Create a new frame ...
            //... and add references of the transports of all engaged remote output signals.
            for (RemoteSignalOutput rso: outputSignals){
                outputFrame.add(rso.getTransport());
            }
            //Then open the connection:
            //instantiate an output daemon,
            remoteSignalOutputDaemon = new RemoteSignalOutputDaemon(this);
            remoteSignalOutputDaemon.start();
            //TODO implement input connections
        }
        catch(Exception exc){
            throw new RemoteSignalException(exc);
        }
    }
    
    public void close() throws RemoteSignalException{
        if (remoteSignalOutputDaemon != null){
            remoteSignalOutputDaemon.stopDaemon();
        }
    }
    
    public boolean isClosed(){
        return !remoteSignalOutputDaemon.isAlive();
    }
    
    public void pushSignals(long cycleNumber){
        if (remoteSignalOutputDaemon != null){
            outputFrame.setCycleNumber(cycleNumber);
            remoteSignalOutputDaemon.push(outputFrame);
        }
    }
    
    public String getRemoteJPacInstance(){
        return remoteJPacInstance;
    }
}
