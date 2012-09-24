/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : RemoteSignalHandler.java
 * VERSION   : $Revision: 1.2 $
 * DATE      : $Date: 2012/04/02 06:32:18 $
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
 * LOG       : $Log: RemoteSignalHandler.java,v $
 * LOG       : Revision 1.2  2012/04/02 06:32:18  schuster
 * LOG       : some improvements concerning remote signals
 * LOG       :
 * LOG       : Revision 1.1  2012/03/30 13:54:35  schuster
 * LOG       : introducing remote signal handling
 * LOG       :
 */

package org.jpac;

import java.rmi.RemoteException;
import java.util.List;

/**
 *
 * @author berndschuster
 */
public interface RemoteSignalHandler extends java.rmi.Remote{
    void              connect(String jPacInstance, List<RemoteSignalOutput> remoteSignalOutput) throws RemoteException;
    void              disconnect(String jPacInstance) throws RemoteException;
    void              push(String jPacInstance, RemoteSignalFrame frame) throws RemoteException;
    RemoteSignalFrame pull() throws RemoteException;
}
