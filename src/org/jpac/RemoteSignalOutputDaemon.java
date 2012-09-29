/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : RemoteSignalOutputDaemon.java
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

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import org.apache.log4j.Logger;

/**
 *
 * @author berndschuster
 */
public class RemoteSignalOutputDaemon extends Thread{
    static       Logger Log                = Logger.getLogger("jpac.Remote");
    static final long   STOPTDAEMONTIMEOUT = 3000000000L;// 3 sec.
    static final int    CONNECTRETRYTIME   = 2000;       // 2 sec.
    static final int    DEFAULTPUSHTIME    = 1000;       // 1 sec.
    
        
    private boolean                         stopRunning;
    private RemoteSignalConnection          remoteSignalConnection;
    private boolean                         online;
    private RemoteSignalFrame               recentRemoteSignalFrame;
    private CyclicBuffer<RemoteSignalFrame> frameBuffer;
    private RemoteSignalHandler             remoteSignalHandler;
    private RemoteSignalFrame               lastFrameTransferred;

    public RemoteSignalOutputDaemon(RemoteSignalConnection remoteSignalConnection) {
        super();
        this.remoteSignalConnection  = remoteSignalConnection;
        this.stopRunning             = false;
        this.frameBuffer             = new CyclicBuffer<RemoteSignalFrame>(10);
        this.recentRemoteSignalFrame = null;
        this.remoteSignalHandler     = null;
        this.lastFrameTransferred    = new RemoteSignalFrame();
        this.online                  = false;
    }
    
    @Override
    public void run(){
        boolean           done                   = false;
        boolean           newFrameArrived        = false;
        boolean           atLeastOneFrameArrived = false;
        RemoteSignalFrame remoteSignalFrame      = null;
        String            jPacInstance           = JPac.getInstance().getInstanceIdentifier();
        setName("RemoteSignalOutputDaemon");
        try{
            if(Log.isDebugEnabled()) Log.debug("remote signal output to JPac instance " + remoteSignalConnection.getRemoteJPacInstance() + " started ...");
            do{
                online = connectSignals();
                while(online && !stopRunning){
                   try{
                       newFrameArrived = !frameBuffer.waitUntilFilled(DEFAULTPUSHTIME);
                       if (online && !stopRunning){
                           if (newFrameArrived){
                               atLeastOneFrameArrived = true;                       
                               remoteSignalFrame = frameBuffer.get();
                               //Log.debug("pushing frame = " + remoteSignalFrame);
                               getRemoteSignalHandler().push(jPacInstance,remoteSignalFrame);
                               //store this frame for cyclic repetition
                               lastFrameTransferred.copy(remoteSignalFrame);
                               //release received frame
                               frameBuffer.release();
                           }
                           else{
                               //send last frame transferred periodically
                               if (atLeastOneFrameArrived){
                                    //Log.debug("pushing last frame = " + lastFrameTransferred);//TODO raus
                                    getRemoteSignalHandler().push(jPacInstance, lastFrameTransferred);                           
                               }
                           }
                       }
                   }
                   catch(java.rmi.RemoteException exc){
                       Log.error("Connection to remote JPac instance " + remoteSignalConnection.getRemoteJPacInstance() + " lost !!!");
                       online = false;
                   }
                }
            }
            while(!stopRunning);
        }
        
        catch(Exception exc){
            Log.error("Error: ", exc);
        }
        catch(Error exc){
            Log.error("Error: ", exc);
        }
        finally{
            if(Log.isDebugEnabled()) Log.debug("remote signal output to JPac instance " + remoteSignalConnection.getRemoteJPacInstance() + " stopped");
            if (online){
                try{
                    getRemoteSignalHandler().disconnect(jPacInstance);
                }
                catch(Exception exc){
                    Log.error("Error: ", exc);
                }
            }
            online = false;
        }
    }
    
    public void stopDaemon(){
        if (Log.isDebugEnabled()) Log.debug("   stopping RemoteSignalOutputDaemon for " + remoteSignalConnection.getRemoteJPacInstance());
        stopRunning = true;
//        long timeoutTime = System.nanoTime() + STOPTDAEMONTIMEOUT;
//        while(getState() != State.TERMINATED && System.nanoTime() < timeoutTime);
//        if (System.nanoTime() < timeoutTime){
//            if (Log.isInfoEnabled()) Log.info("   RemoteSignalOutputDaemon for " + remoteSignalConnection.getRemoteJPacInstance() + " stopped");
//        }
//        else{
//            Log.error("   failed to stop RemoteSignalOutputDaemon for " + remoteSignalConnection.getRemoteJPacInstance() + " status: " + getState());
//        }
    }
    
    public CyclicBuffer<RemoteSignalFrame> getFrameBuffer(){
        return this.frameBuffer;
    }
    
    @Override
    public String toString(){
        return getClass().getCanonicalName() + "(" + remoteSignalConnection.getRemoteJPacInstance() + ")";
    }
    
    public boolean push(RemoteSignalFrame remoteSignalFrame){
        RemoteSignalFrame frame = null;
        synchronized(this){
           try{
               if (!online){
                  //if the connection to the remote JPac instance is offline
                  //first remove the contence of the frame buffer,
                  //so that the remote host will receive the recent state of
                  //the pushed signals, when he goes online again
                  frameBuffer.clear();
               }
               //if online check, if at least one of the signals to be transported has changed to avoid needless traffic
               if ((!remoteSignalFrame.signalsEqual(recentRemoteSignalFrame)) && online){
                  //if so, push the new frame into the cyclic buffer
                  if (frameBuffer.isFull()){
                     //if the frame buffer is full, drop the oldest entry !!!!!
                     RemoteSignalFrame droppedFrame = null; 
                     droppedFrame = frameBuffer.get();
                     frameBuffer.release();
                     Log.error("remote signal frame " + droppedFrame + " dropped for remote JPac instance " + remoteSignalConnection.getRemoteJPacInstance());
                  }
                  //get the next free entry
                  frame = frameBuffer.occupy();
                  //and (re)initialize it, if necessary
                  if (frame == null || frame.structureDifferent(remoteSignalFrame)){
                     if (frame != null && Log.isDebugEnabled()) Log.debug("frame structure changed for remote JPac instance " + remoteSignalConnection.getRemoteJPacInstance());
                     frame = remoteSignalFrame.clone();
                  }
                  else{
                     //or take over the contence of the new frame
                     frame.copy(remoteSignalFrame);
                  }
                  //actualize the cycle number
                  frame.setCycleNumber(remoteSignalFrame.getCycleNumber());
                  //and push the new frame into the queue
                  frameBuffer.put(frame);
                  //keep in mind this recent frame for comparison in future cycles
                  if (recentRemoteSignalFrame == null || recentRemoteSignalFrame.structureDifferent(frame)){
                      recentRemoteSignalFrame = frame.clone();
                  }
                  else{
                      recentRemoteSignalFrame.copy(frame);
                  }
                  if (Log.isDebugEnabled()) Log.debug("frame queued for transfer to remote JPac instance " + remoteSignalConnection.getRemoteJPacInstance());//+ " = " + frame);
               }
            }
            catch(Exception exc){
                Log.error("Error:", exc);
            }
            catch(Error exc){
                Log.error("Error:", exc);                
            }
        }
        return online;
    }
    
    private boolean connectSignals() throws Exception{
        boolean connected    = false;
        String  jPacInstance = JPac.getInstance().getInstanceIdentifier();
        remoteSignalHandler  = null;//force renewal of the remote signal handler;
        if (Log.isDebugEnabled()) Log.debug("connecting ...");                
        do{
            try{
                if (InetAddress.getByName(remoteSignalConnection.getHost()).isReachable(1000)){
                    //if host of remote instance is reachable, connect desired signals.
                    getRemoteSignalHandler().connect(jPacInstance, remoteSignalConnection.getOutputSignals());
                    connected = true;                
                    if (Log.isDebugEnabled()) Log.debug("... connected");                
                }
            }
            catch(java.net.ConnectException exc){
                //thrown by InetAdress... .isReachable())
                //do nothing
            }
            catch(java.rmi.ConnectException exc){
                if (Log.isDebugEnabled()) Log.debug("connection failed: ",exc);
            }
            catch(NotBoundException exc){
                if (Log.isDebugEnabled()) Log.debug("connection failed: ",exc);
            }
            catch(RemoteException exc){
                if (Log.isDebugEnabled()) Log.debug("connection failed: ",exc);
            }
            catch(Error exc){
                Log.error("other errors: ", exc);
                throw new Exception(exc);
            }
            if (!connected && !stopRunning){
                //wait a period of time before trying it once more
                try{Thread.sleep(CONNECTRETRYTIME);}catch(InterruptedException exc){};
            }
        }
        while(!connected && !stopRunning);
        return connected;
    }
        
    RemoteSignalHandler getRemoteSignalHandler() throws RemoteException, NotBoundException, MalformedURLException, UnknownHostException{
        if (remoteSignalHandler == null){
            remoteSignalHandler = (RemoteSignalHandler) Naming.lookup("//" + remoteSignalConnection.getRemoteJPacInstance() + "/" + "RemoteSignalService");
        }
        return remoteSignalHandler;
    }
}
