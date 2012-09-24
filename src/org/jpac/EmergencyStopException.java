/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : EmergencyStopException.java
 * VERSION   : $Revision: 1.2 $
 * DATE      : $Date: 2012/06/18 11:20:53 $
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
 * LOG       : $Log: EmergencyStopException.java,v $
 * LOG       : Revision 1.2  2012/06/18 11:20:53  schuster
 * LOG       : introducing cyclic tasks
 * LOG       :
 */

package org.jpac;


public class EmergencyStopException extends ProcessException{
  
    public EmergencyStopException(String message){
        super(message + " (thrown by " + (Thread.currentThread() instanceof AbstractModule ? (AbstractModule)Thread.currentThread() : Thread.currentThread()) + ")");
        init();
    }
    public EmergencyStopException(Fireable fireable){
        super("caused by " + fireable);
        init();
    }
    
    /**
     * used by jPac internally to propagate emergency stop exceptions to all active modules
     * @param causedBy 
     */
    public EmergencyStopException(EmergencyStopException causedBy){
        super(causedBy.getMessage());
    }

    private void init(){
        if (Thread.currentThread() instanceof AbstractModule){
            ((AbstractModule)Thread.currentThread()).setRequestingEmergencyStop(true);
        }
        JPac.getInstance().requestEmergencyStop(this);
    }
}
