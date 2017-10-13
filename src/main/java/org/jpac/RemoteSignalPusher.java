/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : RemoteSignalPusher.java
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

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * is called by JPac synchronized to the cycle to copy remote signals to local target signals
 * @author berndschuster
 */
@Deprecated
class RemoteSignalPusher{
    
    class PushRunner implements Runnable{
        @Override
        public void run() {
            if (Log.isDebugEnabled()) Log.debug(("pushing signals for " + jPacInstance));
            synchronized(frameToBePushed){
                for (RemoteSignalTransport rst: frameToBePushed.getTransports()){
                    Signal signal = servedSignals.get(rst.getSignature());
                    try{
                        signal.setValid(rst.isValid());
                        if (rst.isValid()){
                            signal.setValue(rst.getValue());
                            signal.setValid(true);
                        }
                    }
                    catch(SignalAccessException exc){
                        Log.error("Error: ", exc);
                    }
                }
                framePushed = true;
            }
        }    
    }
    
    class RegisterRunner implements Runnable{
        @Override
        public void run() {
            try{
                if (Log.isDebugEnabled()) Log.debug(("registering signals for " + jPacInstance));
                for (RemoteSignalOutput rso: remoteSignalOutputsToBeRegistered){
                    Signal signal = SignalRegistry.getInstance().getSignal(rso.getRemoteSignalIdentifier());
                    getServedSignals().put(rso.getTransport().getSignature(), signal);
                    signal.setConnectedAsTarget(true);//signal might not be written to by other signals                
                }
            }
            catch(Exception exc){
                Log.error("Error: ", exc);
            }
            catch(Error exc){
                Log.error("Error: ", exc);
            }
        }    
    }

    class UnregisterRunner implements Runnable{
        @Override
        public void run() {
            try{
                for (Entry<Integer, Signal> entry: getServedSignals().entrySet()){
                    entry.getValue().setConnectedAsTarget(false);
                    entry.getValue().setValid(false);            
                }
                getServedSignals().clear();
            }
            catch(Exception exc){
                Log.error("Error: ", exc);
            }
            catch(Error exc){
                Log.error("Error: ", exc);
            }
        }    
    }

    static Logger Log = LoggerFactory.getLogger("jpac.Remote");

    private ConcurrentHashMap<Integer, Signal> servedSignals;
    private String                             jPacInstance;
    private RemoteSignalFrame                  frameToBePushed;
    private boolean                            framePushed;
    private List<RemoteSignalOutput>           remoteSignalOutputsToBeRegistered;
    private boolean                            remoteSignalsRegistered;
    private Long                               recentPushNanoTime;
    private boolean                            deactivated;
    private PushRunner                         pushRunner;
    private RegisterRunner                     registerRunner;               
    private UnregisterRunner                   unregisterRunner;               
    
    RemoteSignalPusher(String jPacInstance){
        this.jPacInstance                      = jPacInstance;
        this.servedSignals                     = null;
        this.frameToBePushed                   = null;
        this.framePushed                       = false;
        this.remoteSignalOutputsToBeRegistered = null;
        this.remoteSignalsRegistered           = false;
        this.recentPushNanoTime                = System.nanoTime();
        this.deactivated                       = false;
        this.pushRunner                        = new PushRunner();
        this.registerRunner                    = new RegisterRunner();
        this.unregisterRunner                  = new UnregisterRunner();
    }
        
    void register(List<RemoteSignalOutput> remoteSignalOutputs) throws SignalNotRegisteredException, SignalAlreadyConnectedException, RemoteSignalException{
        synchronized(this){
            if (!remoteSignalsRegistered){
                for (RemoteSignalOutput rso: remoteSignalOutputs){
                    Signal signal = SignalRegistry.getInstance().getSignal(rso.getRemoteSignalIdentifier());
                    if (signal.isConnectedAsTarget()){
                        //signal already connected by other instance
                        throw new SignalAlreadyConnectedException(signal);
                    }                
                }
                remoteSignalsRegistered            = true;
                remoteSignalOutputsToBeRegistered  = remoteSignalOutputs;
                JPac.getInstance().invokeLater(registerRunner);            
            }
            else{
                throw new RemoteSignalException("remote signals already registered");
            }
        }
    }
    
    void unregister(){
        synchronized(this){
            remoteSignalsRegistered = false;
            JPac.getInstance().invokeLater(unregisterRunner);
        }
    }

    ConcurrentHashMap<Integer, Signal> getServedSignals(){
        if (servedSignals == null){
            servedSignals =  new ConcurrentHashMap<Integer, Signal>();
        }
        return servedSignals;
    }
    
    void push(RemoteSignalFrame frame) throws CloneNotSupportedException{
        synchronized(this){
            if (frameToBePushed == null){
                frameToBePushed = frame.clone();
            }
            else{
                synchronized(frameToBePushed){
                    if (!framePushed){
                       Log.error("frame slip encountered while servicing JPac instance " + jPacInstance);
                       if (frame.signalsEqual(frameToBePushed)){
                           Log.error("  no signal changes lost.");
                       }
                       else{
                           Log.error("  signal changes lost !!!!!");                       
                       }
                    }
                    frameToBePushed.copy(frame);
                    framePushed = false;
                    //set nanoTime for supervision by the watchdog
                    setRecentPushNanoTime(System.nanoTime());
                }
            }
            JPac.getInstance().invokeLater(pushRunner);
        }
    }
    
    String getJPacInstance(){
        return jPacInstance;
    }
    
    void setRecentPushNanoTime(long nanoTime){
        synchronized(recentPushNanoTime){
            recentPushNanoTime = System.nanoTime();
        }
    }
    long getRecentPushNanoTime(){
        synchronized(recentPushNanoTime){
            return recentPushNanoTime;
        }
    }
    
    void deactivate(){
        try{
            unregister();
            this.deactivated = true;
        }
        catch(Exception exc){
            Log.error("Error:", exc);
        }
    }
    
    void reactivate(){
        this.deactivated = false;
        setRecentPushNanoTime(System.nanoTime());
    }
    
    boolean isDeactivated(){
        return this.deactivated;
    }    
}
