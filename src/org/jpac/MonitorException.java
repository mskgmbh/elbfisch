/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : MonitorException.java
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

import java.util.ArrayList;

/**
 * thrown at a module (inside work()), if an event (Fireable) has been fired, which is monitored by it
 * @author berndschuster
 */
public class MonitorException extends ProcessException{
    ArrayList<Fireable> fireables;

    public MonitorException(ArrayList<Fireable> fireables){
        super("");
        this.fireables = new ArrayList<Fireable>();
        for (Fireable f: fireables){
            this.fireables.add(f);
        }
    }

    public MonitorException(MonitorException original){
        super(original);
        this.fireables = original.getFireables();
    }
    
    /**
     *
     * @return a ArrayList of fireables which caused this exception
     */
    public ArrayList<Fireable> getFireables(){
        return fireables;
    }

    /**
     * used to check, if a certain fireable is causal for a thrown monitor exception
     * CAUTION: if a fireable has been found as being causal for the monitor exception, its
     *          fired state is reset. Subsequent calls to potentialCause.isFired() will return 'false'.
     * @param potentialCause
     * @return 
     */
    public boolean isCausal(Fireable potentialCause){
        boolean fired = false;
        for(Fireable f: this.fireables){
            if (f.equals(potentialCause) && f.isFired()){
                f.reset();
                fired = true;
            }
        }        
        return fired;
    }
    
    /**
     * used to check, if at least one fired firable is present in the list
     * @return true, if at least one fired firebale is present in the list
     */
    public boolean isAnyCauseLeft(){
        boolean found = false;
        for(Fireable f: this.fireables){
            if (f.isFired()){
                found = true;
            }
        }                
        return found;
    }
    
    @Override
    public String toString(){
        StringBuilder firedEvents = new StringBuilder();
        firedEvents.append(getClass().getCanonicalName()).append(':');
        for(Fireable f: this.fireables){
            if (f.fired){
                firedEvents.append("\n        ").append(f.toString());
            }
        }
        return firedEvents.toString();
    }
}
