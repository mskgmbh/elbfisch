/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : CharString.java
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
 * represents a char string signal
 */
public class CharString extends Signal{
    
    private   CharStringValue  wrapperValue;
    
    /**
     * constructs a char string signal with intrinsic range check
     * @param containingModule: module this signal is contained in
     * @param identifier: identifier of the signal
     * @param minValue: minimum value signalValid for this char string
     * @param maxValue: maximum value signalValid for this char string
     */
    public CharString(AbstractModule containingModule, String identifier){
        super(containingModule, identifier);
        this.value              = new CharStringValue();
        this.propagatedValue    = new CharStringValue(); 
        this.wrapperValue       = new CharStringValue();
    }
    
    /**
     * used to set the char string to the given value
     * @param value: value, the char string is set to
     */
    public void set(String value) throws SignalAccessException{
        wrapperValue.set(value);
        setValue(wrapperValue);
    }

    /**
     * returns the value of the char string. If the calling module is the containing module the value of this signal is returned.
     * If the calling module is a foreign module the propagated signal is returned.
     * @return see above
     */
    public String get() throws SignalInvalidException{
        return ((CharStringValue)getValidatedValue()).get();
    }

    /**
     * returns a process event (CharStringChanges), which is fired, if the char string changes
     * @param threshold: threshold to be supervised
     */    
    public CharStringChanges changes(){
        return new CharStringChanges(this);
    }

    /**
     * used to connect this char string to another char string. One char string can be connected
     * to multiple char strings.
     * The connection is unidirectional: Changes of the connecting signal (sourceSignal) will be
     * propagated to the signals it is connected to (targetSignal): sourceSignal.connect(targetSignal).
     * @param targetSignal
     */
    public void connect(CharString targetSignal) throws SignalAlreadyConnectedException{
        super.connect(targetSignal);
    }

    @Override
    protected void deferredConnect(Signal targetSignal) throws SignalAlreadyConnectedException{
        super.deferredConnect(targetSignal);
    }

    @Override
    protected void deferredDisconnect(Signal targetSignal){
        super.deferredDisconnect(targetSignal);
    }
    

    @Override
    protected boolean isCompatibleSignal(Signal signal) {
        return signal instanceof CharString;
    }

    @Override
    protected void propagateSignalInternally() {
        //physically copy the value to the propagated value
        ((CharStringValue)getPropagatedValue()).copy((CharStringValue)getValue());
    }

    @Override
    protected void updateValue(Object o, Object arg) throws SignalAccessException {
        try{
            if (o instanceof CharString){
               set(((CharString)o).get());
            }
        }
        catch(Exception exc){
            Log.error("Error: ", exc);
            throw new SignalAccessException(exc.getMessage());
        }
    }
}
