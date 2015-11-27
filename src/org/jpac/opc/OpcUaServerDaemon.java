/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : OpcUaServerDaemon.java
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

package org.jpac.opc;

import org.apache.log4j.Logger;

/**
 *
 * @author berndschuster
 */
public class OpcUaServerDaemon extends Thread{
    static  Logger Log = Logger.getLogger("jpac.opc");
    
    OpcUaService service;
    String       serverName;
    int          port;
    
    public OpcUaServerDaemon(String serverName, int port){
        this.serverName = serverName;
        this.port       = port;
    }
    
    
    @Override
    public void run(){
        Log.info("starting up opc ua server daemon ...");
        try{
            service = new OpcUaService(serverName, port);
        }
        catch(Exception exc){
            Log.error("Error: ", exc);
        }
        Log.info("opc ua server daemon stopped");
    }
}
