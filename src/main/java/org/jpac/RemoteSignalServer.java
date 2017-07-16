/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : RemoteSignalServer.java
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
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * serves incoming remote connections
 * @author berndschuster
 */
public class RemoteSignalServer {
   static Logger Log = LoggerFactory.getLogger("jpac.Remote");
   private static final int    DEFAULTPORT = 10002;
   private static final String SERVICENAME = "RemoteSignalService";
   static               int    port        = DEFAULTPORT;
    
   public static void start(int serverPort) throws RemoteException {
      try{
      System.setSecurityManager( new RMISecurityManager() );       
      port = serverPort;
      String hostname = InetAddress.getLocalHost().getHostName();
      LocateRegistry.createRegistry(port);
      RemoteSignalHandler remoteSignalHandler = new RemoteSignalHandlerImpl();
      String urlString = "//" + hostname + ":" + port + "/" + SERVICENAME;
      Naming.rebind( urlString, remoteSignalHandler);
      if (Log.isInfoEnabled()) Log.info( "Remote signal service started as " + urlString);
      }
      catch(Exception exc){
          throw new RemoteException("Error:",exc);
      }
   }    

   public static void start() throws RemoteException, MalformedURLException, NotBoundException, UnknownHostException {
      start(DEFAULTPORT);
   }
   
   public static void stop(){
      throw new UnsupportedOperationException("Not supported yet.");       
   }

   public static int getPort(){
       return port;
   }
   
   public static String getUrl(){
       String url = null;
       try{url = "//" + InetAddress.getLocalHost().getHostName() + ":" + port;}catch(UnknownHostException exc){};
       return url;
   }

//   public static void main (String[] args) {
//      // set the security manager
//      System.setSecurityManager( new RMISecurityManager() );
//
//      try{
//         RemoteSignalServer.start();
//      }
//      catch(Exception exc){
//          Log.error("Error: ", exc);
//      }
//      catch(Error exc){
//          Log.error("Error: ", exc);
//      }
//   }
   
}
