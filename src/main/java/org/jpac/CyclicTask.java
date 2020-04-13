/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : CyclicTask.java 
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
 * used to implement cyclic tasks. Cyclic tasks are a new powerful feature of jPac. They can be used to implement functions that will be executed
 * in every jPac cycle. This mechanism is used by jPac itself to track and save changes of configuration properties and to implement the cyclic 
 * interchange of process data using the vioss (versatile input/ouput subsystem).
 * Feel free to use it for your needs.
 * 
 * @author berndschuster
 */
public interface CyclicTask {
    /**
     * is called by jPac on every cycle before modules are invoked.
     * Hint: This method must be implemented short, fast and robust because it effects the
     *       jPac cycle directly
     */
    public void run();
    
    /**
     * used to do some initializing. Is called by jPac just before it starts processing.
     */
    public void prepare();
    
    /**
     * is called by jPac during the shutdown. Can be used to clean up the context of the CyclicTask
     */
    public void stop();
    
    /**
     * used to check, if a cyclic has come to an end after it has been terminated by stop()
     * @return 
     */
    public boolean isFinished();
}
