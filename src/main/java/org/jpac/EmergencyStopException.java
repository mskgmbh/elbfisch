/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : EmergencyStopException.java
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

/**
 * thrown, if an emergency stop condition arises. Can be thrown by any module. Once thrown, all other modules will receive the EmergencyStopException in the next cycle
 * @author berndschuster
 */
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
