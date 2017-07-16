/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : SignalRegistry.java
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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * central registry for all signals instantiated inside an application
 * implemented as singleton
 * @author berndschuster
 */
public class SignalRegistry {
    private static SignalRegistry              instance;
    private List<Signal>                       signals;
    private ConcurrentHashMap<String,Integer>  signalIndices;
    private int                                lastIndex;
    
    protected SignalRegistry(){
        instance      = null;
        signals       = Collections.synchronizedList(new ArrayList<Signal>());
        signalIndices = new ConcurrentHashMap<String, Integer>();
        lastIndex     = 0;
    }
    /**
     * @return the instance of the signal registry
     */
    public static SignalRegistry getInstance(){
        if (instance == null) {
            instance = new SignalRegistry();
        }
        return instance;
    }
     
    /**
     * used to add a signal to the registry
     * @param signal
     */
    public void add(Signal signal) throws SignalAlreadyExistsException{
        String identifier = signal.getContainingModule().getQualifiedName() + '.' + signal.getIdentifier();
        if (signalIndices.get(identifier) != null){
            throw new SignalAlreadyExistsException(signal);
        }
        signals.add(lastIndex, signal);
        signalIndices.put(identifier, lastIndex);
        lastIndex++;
    }

    public void remove(Signal signal) throws SignalNotRegisteredException{
        if (signals.contains(signal)){
           signals.remove(signal);
        }
        else{
            throw new SignalNotRegisteredException(signal.toString());
        }
    }
    /*
     * @return the ArrayList of registered signals
     */
    public List<Signal> getSignals(){
        return signals;
    }
    
    public Signal getSignal(String identifier) throws SignalNotRegisteredException{
        Integer index = signalIndices.get(identifier);
        if (index == null){
            throw new SignalNotRegisteredException(identifier);
        }        
        return signals.get(signalIndices.get(identifier));
    }
}
