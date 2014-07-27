/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : RemoteSignalOutput.java
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

import java.io.Serializable;
import java.util.Observable;
import java.util.Observer;
import org.apache.log4j.Logger;

/**
 * represents a remote output signal
 * @author berndschuster
 */
public class RemoteSignalOutput implements Observer, Serializable{
    static Logger Log = Logger.getLogger("jpac.Remote");

    private RemoteSignalTransport transport;
    private int                   index;
    private boolean               connectedAsTarget;  
    private String                identifier;
    private String                jPacInstance;
    private String                remoteHost;
    private int                   remotePort;
    private String                remoteSignalIdentifier;
    
    public RemoteSignalOutput(String identifier, String host, int port, String remoteSignalIdentifier){
        this.identifier             = identifier;
        this.remoteHost             = host;
        this.remotePort             = port;
        this.remoteSignalIdentifier = remoteSignalIdentifier;
        this.jPacInstance           = JPac.getInstance().getInstanceIdentifier();
        this.connectedAsTarget      = false;
        this.index                  = RemoteSignalRegistry.getInstance().addOutput(this);//TODO critical section concerning index        
        this.transport              = new RemoteSignalTransport(this.index, identifier, remoteSignalIdentifier);
    } 

    @Override
    public void update(Observable o, Object arg) {
        try{
            if (o instanceof Signal){
               //copy state of the signal to the transport container
                transport.setValid(((Signal)o).isValid());
                transport.setCloneOfValue(((Signal)o).getValue());
            }
            else{
                Log.error(this + " cannot be updated by " + o);
            }
        }
        catch(Exception exc){
            Log.error("Error: ", exc);
        }       
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
     * 
     * @return the host, the remote connection refers to 
     */
    public String getHost(){
        return remoteHost;
    }
    
    /**
     * 
     * @return the network port, the remote connection is refers to
     */
    public int getPort(){
        return remotePort;
    }
    
    /**
     * 
     * @return the name of the remote signal 
     */
    public String getRemoteSignalIdentifier(){
        return remoteSignalIdentifier;
    }
    
    /**
     * 
     * @return the remote transport used by this 
     */
    RemoteSignalTransport getTransport(){
        return transport;
    }
    
    /**
     * used to invalidate the remote input signal
     */
    public void invalidate(){
        transport.setValid(false);
    }
    
    /**
     * used to set the index
     * @param index 
     */
    public void setIndex(int index){
        this.index = index;
    }
    
    /**
     * 
     * @return the identifier 
     */
    public String getIdentifier() {
        return identifier;
    }
    
    public String getJPacInstance(){
        return jPacInstance;
    }

    @Override
    public String toString(){
        return getClass().getSimpleName() + "(//" + remoteHost + ':' + remotePort + '/' + remoteSignalIdentifier + ")";
    }
}
