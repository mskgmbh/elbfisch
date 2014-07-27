/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : RemoteSignalRegistry.java
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

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;

/**
 * central registry for all signals instantiated inside an application
 * implemented as singleton
 * @author berndschuster
 */
public class RemoteSignalRegistry {
    static Logger Log = Logger.getLogger("jpac.Remote");
    
    class ServedInstancesWatchdog extends Thread{
        final long WATCHDOGTIMEOUTTIME = 2000000000L;//2 sec
        final long WATCHDOGCYCLETIME   = (WATCHDOGTIMEOUTTIME/1000000L)/4;// 1/4 WATCHDOGTIMEOUTTIME
        boolean finished;
        
        @Override
        public void run(){
            long actualNanotime;
            setName("RemoteSignalRegistry.ServedInstancesWatchdog");
            try{
               finished = false;
               do{
                  try{Thread.sleep(WATCHDOGCYCLETIME);}catch(InterruptedException exc){};
                  actualNanotime = System.nanoTime();
                  for (Entry<String, RemoteSignalPusher> entry: servedInstances.entrySet()){
                      if (!entry.getValue().isDeactivated() && (entry.getValue().getRecentPushNanoTime() + WATCHDOGTIMEOUTTIME) < actualNanotime){
                         //connection for this serviced JPac instance lost
                         //disconnect assigned signals
                         Log.error("deactivateing remote signal service for JPac instance " + entry.getKey());            
                         entry.getValue().deactivate();
                      }
                  }
               }
               while(!finished);
            }
            catch(Exception exc){
                Log.error("Error: ", exc);            
            }                
            catch(Error exc){
                Log.error("Error: ", exc);            
            }
            finally{
                if (Log.isDebugEnabled()) Log.debug("watchdog for served instances stopped");
            }
        }        
        
        void stopRunning(){
            finished = true;
        }
    }
        
    private static RemoteSignalRegistry                       instance;
    private ConcurrentHashMap<String, RemoteSignalConnection> remoteHosts;
    private ConcurrentHashMap<String, RemoteSignalPusher>     servedInstances;
    private ServedInstancesWatchdog                           servedInstancesWatchdog;
    
    RemoteSignalRegistry(){
        instance                = null;
        remoteHosts             = null;
        servedInstances         = null;
        servedInstancesWatchdog = null;
    }
    /**
     * @return the instance of the signal registry
     */
    public static RemoteSignalRegistry getInstance(){
        if (instance == null) {
            instance = new RemoteSignalRegistry();
        }
        return instance;
    }
    
    /*
     * used to register a new remote output signal on the pushing side
     */
    int addOutput(RemoteSignalOutput remoteSignal){
        String host = remoteSignal.getHost();
        int    port = remoteSignal.getPort();
        String key  = host + ':' + port;
        int    index = 0;
        //if a new host is to be accessed, create a new remote signal list
        if (!getRemoteHosts().containsKey(key)){
            getRemoteHosts().put(key, new RemoteSignalConnection(host, port));
        }
        //assign the remote signal to the specified host 
        index = getRemoteHosts().get(key).addOutput((RemoteSignalOutput)remoteSignal);            
        return index;
    }
    
    /*
     * used to retrieve the list remote connections on the pushing side
     */
    public ConcurrentHashMap<String, RemoteSignalConnection> getRemoteHosts(){
        if (remoteHosts == null){
            remoteHosts = new ConcurrentHashMap<String, RemoteSignalConnection>();
        }
        return remoteHosts;
    }
    
    /*
     * add a pusher on the serving side
     */
    RemoteSignalPusher addPusher(RemoteSignalPusher remoteSignalPusher){
        getServedInstances().put(remoteSignalPusher.getJPacInstance(), remoteSignalPusher);     
        return remoteSignalPusher;
    }
    
    /*
     * add a pusher on the serving side
     */
    RemoteSignalPusher getPusher(String jPacInstance){
        return getServedInstances().get(jPacInstance);
    }

    /*
     * used to retrieve the list of served JPac instances on the serving side
     */
    ConcurrentHashMap<String, RemoteSignalPusher> getServedInstances(){
        if (servedInstances == null){
            servedInstances = new ConcurrentHashMap<String, RemoteSignalPusher>();
            if (servedInstancesWatchdog == null){
                //at least one remote JPac instance is to be served
                //start the watchdog to supervise the connection
                servedInstancesWatchdog = new ServedInstancesWatchdog();
                servedInstancesWatchdog.start();
            }
        }
        return servedInstances;
    }
    
    void stopWatchdog(){
        if (servedInstancesWatchdog != null){
            servedInstancesWatchdog.stopRunning();
            while(servedInstancesWatchdog.getState() != Thread.State.TERMINATED);
        }
    }
}
