/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : RemoteSignalOutput.java
 * VERSION   : $Revision: 1.1 $
 * DATE      : $Date: 2012/03/30 13:54:35 $
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
 *
 * LOG       : $Log: RemoteSignalOutput.java,v $
 * LOG       : Revision 1.1  2012/03/30 13:54:35  schuster
 * LOG       : introducing remote signal handling
 * LOG       :
 */

package org.jpac;

import java.io.Serializable;
import java.util.Observable;
import java.util.Observer;
import org.apache.log4j.Logger;

/**
 *
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
    private String                remoteSignalName;
    
    public RemoteSignalOutput(String identifier, String host, int port, String signalName){
        this.identifier        = identifier;
        this.remoteHost        = host;
        this.remotePort        = port;
        this.remoteSignalName  = signalName;
        this.jPacInstance      = JPac.getInstance().getInstanceIdentifier();
        this.connectedAsTarget = false;
        this.index             = RemoteSignalRegistry.getInstance().addOutput(this);        
        this.transport         = new RemoteSignalTransport(this.index, identifier, signalName);
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
    
    public String getHost(){
        return remoteHost;
    }
    
    public int getPort(){
        return remotePort;
    }
    
    public String getSignalName(){
        return remoteSignalName;
    }
    
    RemoteSignalTransport getTransport(){
        return transport;
    }
    
    public void invalidate(){
        transport.setValid(false);
    }
    
    public void setIndex(int index){
        this.index = index;
    }
    
    @Override
    public String toString(){
        return getClass().getSimpleName() + "(//" + remoteHost + ':' + remotePort + '/' + remoteSignalName + ")";
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean isCompatible(Assignable item) {
        return true;//RemoteSignalOutput is independent of type
    }
    
    public String getJPacInstance(){
        return jPacInstance;
    }
}
