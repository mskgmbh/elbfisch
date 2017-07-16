/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : RemoteSignalTransport.java
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

import java.io.Serializable;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * used to transport signals between jPac instances
 * @author berndschuster
 */
class RemoteSignalTransport implements Serializable, Cloneable{
    static Logger Log = LoggerFactory.getLogger("jpac.Remote");
    
//    public enum State {UNDEFINED, ADDED, PRESENT, REMOVED};
//    
    private String  targetSignal;
    private String  sourceSignal;
    private int     index;
    private boolean valid;
    private Value   value;
    private int     signature;
//    private State   state;
    
    RemoteSignalTransport(int index, String sourceSignal, String targetSignal){
        this.sourceSignal = sourceSignal;
        this.targetSignal = targetSignal;
        this.index        = index;
        this.valid        = false;
        this.value        = null;
        this.signature    = (sourceSignal + targetSignal + index).hashCode();
//        this.state        = State.UNDEFINED;
    }

    /**
     * @return the targetSignal
     */
    String getTargetSignal() {
        return targetSignal;
    }

    /**
     * @param targetSignal the targetSignal to set
     */
    void setTargetSignal(String targetSignal) {
        this.targetSignal = targetSignal;
    }

    /**
     * @return the targetSignal
     */
    String getSourceSignal() {
        return sourceSignal;
    }

    /**
     * @param targetSignal the targetSignal to set
     */
    void setSourceSignal(String sourceSignal) {
        this.sourceSignal = sourceSignal;
    }

    /**
     * @return the valid
     */
    boolean isValid() {
        return valid;
    }

    /**
     * @param valid the valid to set
     */
    void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * @return the value
     */
    Value getValue() {
        return value;
    }

    /**
     * @param value the value to clone
     */
    void setCloneOfValue(Value value) throws CloneNotSupportedException {
        //value may be null, if not have been set, yet
        if (value != null){
            if (this.value == null){
               //if value not set, yet, initialize it on first call
               this.value = value.clone();
            }
            else{
               //reuse present instance to avoid data garbage 
               this.value.copy(value);
            }
        }
    }
    
    int getIndex(){
        return index;
    }
    
    void setIndex(int index){
        this.index = index;
    }

    int getSignature(){
        return signature;
    }
    
//    State getState(){
//        return state;
//    }
//    
//    void setState(State state){
//        this.state = state;
//    }
//    
    boolean equals(RemoteSignalTransport remoteSignalTransport){
        boolean equal = false;
        if (remoteSignalTransport != null){
            equal = signature == remoteSignalTransport.getSignature() &&
                    valid     == remoteSignalTransport.isValid()      &&                 
//                    state     == remoteSignalTransport.getState()     &&
                    (value == null && remoteSignalTransport.getValue() == null || value.equals(remoteSignalTransport.getValue()));
        }
        return equal;
    }
    
    @Override
    public RemoteSignalTransport clone() throws CloneNotSupportedException{
        RemoteSignalTransport cloned = new RemoteSignalTransport(getIndex(), getSourceSignal(), getTargetSignal());
        cloned.setValid(valid);
        cloned.setCloneOfValue(value);
        return cloned;
    }
    
    public void copyValue(RemoteSignalTransport sourceTransport) throws CloneNotSupportedException{
        if (sourceTransport != null){
            setValid(sourceTransport.isValid());
            setCloneOfValue(sourceTransport.getValue());
        }
    }

    @Override
    public String toString(){
        return getClass().getSimpleName() + "(" + index + ", " + targetSignal + ", " + valid + ", " + value + ")";
    }
}
