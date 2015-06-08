/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : Logical.java
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

import org.jpac.alarm.Alarm;

/**
 * represents a boolean signal
 */

public class Logical extends Signal{
    private LogicalValue wrapperValue;
    private boolean      invertOnUpdate;

    /**
     * constructs a logical signal
     * @param containingModule: module, this signal is contained in
     * @param identifier: identifier of the signal
     * @throws org.jpac.SignalAlreadyExistsException
     */
    public Logical(AbstractModule containingModule, String identifier) throws SignalAlreadyExistsException{
        super(containingModule, identifier);
        value           = new LogicalValue();
        propagatedValue = new LogicalValue(); 
        wrapperValue    = new LogicalValue();
        invertOnUpdate  = false;
    }
    
    /**
     * constructs a logical signal
     * @param containingModule: module, this signal is contained in
     * @param identifier: identifier of the signal
     * @param defaultState: default state of this logical
     * @throws org.jpac.SignalAlreadyExistsException
     */
    public Logical(AbstractModule containingModule, String identifier, boolean defaultState) throws SignalAlreadyExistsException{
        this(containingModule, identifier);
        this.initializing = true;//prevent signal access assertion
        try{set(defaultState);}catch(SignalAccessException exc){/*cannot happen*/};
        this.initializing = false;
    }

    /**
     * used to set the logical to the given state
     * @param state: state, the logical is set to
     * @throws SignalAccessException, if the module invoking this method is
     *         not the containing module
     */
    public void set(boolean state) throws SignalAccessException{
        synchronized(this){
            wrapperValue.set(state);
            setValue(wrapperValue);
        }
    }

    /**
     * used to set the logical from any thread, which is not a module and not the jPac thread
     * The value is changed synchronized to the jPac cycle
     * @param state: state, the logical is set to
     * @throws SignalAccessException, if the module invoking this method is
     *         not the containing module
     */
    public void setDeferred(boolean state) throws SignalAccessException{
        synchronized(this){
            LogicalValue localWrapperValue = new LogicalValue();
            localWrapperValue.set(state);
            setValueDeferred(localWrapperValue);
        }
    }

    /**
     * returns true, if the given "state" is equal to the logicals state. If the calling module is the containing module
     * the state of the signal is returned. If the calling module is a foreign module the propagated (synchronized) signal is returned.
     * @param state: expected state
     * @return see above
     * @throws org.jpac.SignalInvalidException
     */
    public boolean is(boolean state) throws SignalInvalidException{
        return ((LogicalValue)getValidatedValue()).is(state);
    }
    
    /**
     * Returns true, if the Logical has toggled
     * Is true for exactly one cycle depending on the calling module:
     * For the containing module this method returns true starting at the time the toggle occurs
     * until the end of the cycle.
     * For every other consuming module this method returns true during the following cycle.
     * Calling this method on signals connected as target directly or indirectly to the changed signal
     * will return true on the following cycle regardless if the accessing module is the containing one or not 
     */
    
    public boolean isToggled() throws SignalInvalidException{
       synchronized(this){
            if (!isValid()){
               throw new SignalInvalidException(this.toString());
            }
            return isChanged();
       }
    }
    
    
    /**
     * Returns true, if the logicals state changed to the given state.
     * Is true for exactly one cycle depending on the calling module:
     * For the containing module this method returns true starting at the time the toggle occurs
     * until the end of the cycle.
     * For every other consuming module this method returns true during the following cycle.
     * Calling this method on signals connected as target directly or indirectly to the changed signal
     * will return true on the following cycle regardless if the accessing module is the containing one or not 
     * @param state
     * @return 
     */
    public boolean isToggledTo(boolean state) throws SignalInvalidException{
        synchronized(this){
            return isToggled() && ((LogicalValue)getValue()).is(state);
        }
    }

    /**
     * returns a ProcessEvent that will be fired on a change from !state to state
     * @param state
     * @return see above
     */
    public ProcessEvent becomes(boolean state){
        return new LogicalBecomes(this, state);
    }

    /**
     * returns a ProcessEvent that will be fired on change of the Logical's state
     * @return see above
     */ 
    public ProcessEvent toggles(){
        return new LogicalToggles(this);
    }

    /**
     * returns a ProcessEvent that will be fired, if the Logical becomes "state"
     * @param state
     * @return see above
     */
    public ProcessEvent state(boolean state){
        return new LogicalState(this,state);
    }

    /**
     * used to connect this Logical to another Logical. One Logical can be connected
     * to multiple Logicals.
     * The connection is unidirectional: Changes of the connecting signal (sourceSignal) will be
     * propagated to the signals it is connected to (targetSignal): sourceSignal.connect(targetSignal).
     * @param targetSignal the target signal
     * @param invert signal will be inverted on propagation over this connection
     * @throws org.jpac.SignalAlreadyConnectedException
     */
    public void connect(Logical targetSignal, boolean invert) throws SignalAlreadyConnectedException{
        synchronized(this){
            super.connect(targetSignal);
            targetSignal.setInvertOnUpdate(invert);
        }
    }

    /**
     * used to connect this Logical to another Logical. One Logical can be connected
     * to multiple Logicals.
     * The connection is unidirectional: Changes of the connecting signal (sourceSignal) will be
     * propagated to the signals it is connected to (targetSignal): sourceSignal.connect(targetSignal).
     * @param targetSignal the target signal
     */
    public void connect(Logical targetSignal) throws SignalAlreadyConnectedException{
        this.connect(targetSignal,false);
    }

    /**
     * used to connect this Logical to an Alarm. One Logical can be connected
     * to multiple Logicals.
     * @param alarm the target alarm
     * @param invert signal will be inverted on propagation over this connection
     * @throws org.jpac.SignalAlreadyConnectedException
     */
    public void connect(Alarm alarm, boolean invert) throws SignalAlreadyConnectedException{
        synchronized(this){
            super.connect(alarm);
            alarm.setInvertOnUpdate(invert);
        }
    }

    /**
     * used to connect this Logical to an Alarm.
     * @param alarm the target alarm
     * @throws org.jpac.SignalAlreadyConnectedException
     */
    public void connect(Alarm alarm) throws SignalAlreadyConnectedException{
        this.connect(alarm,false);
    }

    @Override
    protected boolean isCompatibleSignal(Signal signal){
        return signal instanceof Logical;
    }

    @Override
    protected void updateValue(Object o, Object arg) throws SignalAccessException {
        //((LogicalValue)getValue()).copy(((LogicalValue)((Logical)o).getValue()));
        try{
            set(((Logical)o).is(invertOnUpdate ? false: true));
        }
        catch(Exception exc){
            Log.error("Error: ", exc);
            throw new SignalAccessException(exc.getMessage());
        }
    }

    @Override
    protected void propagateSignalInternally() throws SignalInvalidException {
        //physically copy the value to the propagated value
        ((LogicalValue)getPropagatedValue()).copy((LogicalValue)getValue());
    }
    
    protected void setInvertOnUpdate(boolean invert){
        this.invertOnUpdate = invert;
    }
}
