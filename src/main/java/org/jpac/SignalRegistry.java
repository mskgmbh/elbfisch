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

import java.util.concurrent.ConcurrentHashMap;

/**
 * central registry for all signals instantiated inside an application
 * implemented as singleton
 * @author berndschuster
 */
public class SignalRegistry {
    private static SignalRegistry              instance;
    private ConcurrentHashMap<Integer, Signal> signals;
    
    protected SignalRegistry(){
        instance      = null;
        signals       = new ConcurrentHashMap<Integer, Signal>();
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
        String identifier = signal.getQualifiedIdentifier();
        if (signals.get(identifier.hashCode()) != null){
            throw new SignalAlreadyExistsException(getSignal(identifier));
        }
        signals.put(identifier.hashCode(), signal);
    }

    public void remove(Signal signal) throws SignalNotRegisteredException{
        if (signals.contains(signal)){
           signals.remove(signal);
        }
        else{
            throw new SignalNotRegisteredException(signal.getQualifiedIdentifier());
        }
    }
    /*
     * @return the ArrayList of registered signals
     */
    public ConcurrentHashMap<Integer, Signal> getSignals(){
        return signals;
    }
    
    public Signal getSignal(String qualifiedIdentifier) throws SignalNotRegisteredException{
        Signal signal = signals.get(qualifiedIdentifier.hashCode());
        if (signal == null){
            throw new SignalNotRegisteredException(qualifiedIdentifier);
        }
        return signal; 
    }

    public Signal getSignal(int hashCode) throws SignalNotRegisteredException{
        Signal signal = signals.get(hashCode);
        if (signal == null){
            throw new SignalNotRegisteredException("signal with hashcode " + hashCode);
        }        
        return signal;
    }
}
