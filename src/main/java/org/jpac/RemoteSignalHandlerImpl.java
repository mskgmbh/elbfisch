/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : RemoteSignalHandlerImpl.java
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

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * implementation of the RemoteSignalHandler
 * @author berndschuster
 */
public class RemoteSignalHandlerImpl extends UnicastRemoteObject implements RemoteSignalHandler{
    static Logger Log = LoggerFactory.getLogger("jpac.Remote");
    
    static private RemoteSignalRegistry registry;
    
    
    public RemoteSignalHandlerImpl() throws RemoteException{
        super();
        registry = RemoteSignalRegistry.getInstance();
    }

    @Override
    public void connect(String jPacInstance, List<RemoteSignalOutput> remoteSignalOutputs) throws RemoteException {
        try {
             if (Log.isDebugEnabled()) Log.debug("remote signal connection requested by " + jPacInstance);
             RemoteSignalPusher   pusher   = registry.getServedInstances().get(jPacInstance);
             if (pusher == null){
                 pusher = registry.addPusher(new RemoteSignalPusher(jPacInstance));
             }
             if (pusher.isDeactivated()){
                 //reactivate pusher
                 pusher.reactivate();
             }
             pusher.register(remoteSignalOutputs);
             if (Log.isDebugEnabled()) Log.debug("remote signals for JPac instance " + jPacInstance + " connected.");
        }
        catch(Exception exc){
            throw new RemoteException("Error: ", exc);
        }
        catch(Error exc){
            throw new RemoteException("Error: ", exc);
        }
    }

    @Override
    public void disconnect(String jPacInstance) throws RemoteException {
        try {
             if (Log.isDebugEnabled()) Log.debug("remote signal disconnection requested by " + jPacInstance);
             RemoteSignalPusher   pusher   = registry.getServedInstances().get(jPacInstance);
             if (pusher != null && !pusher.isDeactivated()){
                 //pusher.unregister();
                 pusher.deactivate();
                 if (Log.isDebugEnabled()) Log.debug("remote signals for " + "//" + jPacInstance + " disconnected.");
             }
             else{
                 Log.error("no active remote signal service for JPac instance " + jPacInstance);
                 throw new RemoteSignalException("no active remote signal service for JPac instance " + jPacInstance);
             }
        }
        catch(Exception exc){
            throw new RemoteException("Error: ", exc);
        }
        catch(Error exc){
            throw new RemoteException("Error: ", exc);
        }
    }

    @Override
    public void push(String jPacInstance, RemoteSignalFrame frame) throws RemoteException {
        try {
             if (Log.isDebugEnabled()) Log.debug("JPac instance " + jPacInstance + " pushing frame " + frame);
             //TODO handling deactivated state !!!!
             RemoteSignalPusher pusher = registry.getPusher(jPacInstance);
             if (pusher == null || pusher.isDeactivated()){
                 Log.error("no active remote signal service for JPac instance " + jPacInstance);
                 throw new RemoteSignalException("no active remote signal service for JPac instance " + jPacInstance);
             }
             pusher.push(frame);
             if (Log.isDebugEnabled()) Log.debug("frame pushed for " + jPacInstance);
        }
        catch(Exception exc){
            throw new RemoteException("Error: ", exc);
        }
        catch(Error exc){
            throw new RemoteException("Error: ", exc);
        }
    }
}
