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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Supplier;
import org.jpac.alarm.Alarm;
import org.jpac.vioss.IoLogical;

/**
 * represents a boolean signal
 */

public class Logical extends Signal{
    private LogicalValue    wrapperValue;
    private boolean         invertOnUpdate;

    /**
     * constructs a logical signal
     * @param containingModule: module, this signal is contained in
     * @param identifier: identifier of the signal
     * @param intrinsicFunction: intrinsic function which will be applied in every cycle to calculate the actual value
     * @param ioDirection: defines the signal as being either an INPUT or OUTPUT signal. (Relevant in distributed applications)
     * @throws org.jpac.SignalAlreadyExistsException
     */
    public Logical(AbstractModule containingModule, String identifier, Supplier<Boolean> intrinsicFunction, IoDirection ioDirection) throws SignalAlreadyExistsException{
        super(containingModule, identifier, intrinsicFunction, ioDirection);
        this.wrapperValue   = new LogicalValue();
        this.invertOnUpdate = false;
    }

    /**
     * constructs a logical signal
     * @param containingModule: module, this signal is contained in
     * @param identifier: identifier of the signal
     * @param intrinsicFunction: intrinsic function which will be applied in every cycle to calculate the actual value
     * @throws org.jpac.SignalAlreadyExistsException
     */
    public Logical(AbstractModule containingModule, String identifier, Supplier<Boolean> intrinsicFunction) throws SignalAlreadyExistsException{
        this(containingModule, identifier, intrinsicFunction, IoDirection.UNDEFINED);
    }

    /**
     * constructs a logical signal
     * @param containingModule: module, this signal is contained in
     * @param identifier: identifier of the signal
     * @param ioDirection: defines the signal as being either an INPUT or OUTPUT signal. (Relevant in distributed applications)
     * @throws org.jpac.SignalAlreadyExistsException
     */
    public Logical(AbstractModule containingModule, String identifier, IoDirection ioDirection) throws SignalAlreadyExistsException{
        this(containingModule, identifier, null, ioDirection);
    }

    /**
     * constructs a logical signal
     * @param containingModule: module, this signal is contained in
     * @param identifier: identifier of the signal
     * @throws org.jpac.SignalAlreadyExistsException
     */
    public Logical(AbstractModule containingModule, String identifier) throws SignalAlreadyExistsException{
        this(containingModule, identifier, null, IoDirection.UNDEFINED);
    }
    

    /**
     * constructs a logical signal
     * @param containingModule: module, this signal is contained in
     * @param identifier: identifier of the signal
     * @param defaultState: default state of this logical
     * @param ioDirection: defines the signal as being either an INPUT or OUTPUT signal. (Relevant in distributed applications)
     * @throws org.jpac.SignalAlreadyExistsException
     */
    public Logical(AbstractModule containingModule, String identifier, boolean defaultState, IoDirection ioDirection) throws SignalAlreadyExistsException{
        this(containingModule, identifier, null, ioDirection);
        this.initializing = true;//prevent signal access assertion
        try{set(defaultState);}catch(SignalAccessException exc){/*cannot happen*/};
        this.initializing = false;
    }

    /**
     * constructs a logical signal
     * @param containingModule: module, this signal is contained in
     * @param identifier: identifier of the signal
     * @param defaultState: default state of this logical
     * @throws org.jpac.SignalAlreadyExistsException
     */
    public Logical(AbstractModule containingModule, String identifier, boolean defaultState) throws SignalAlreadyExistsException{
        this(containingModule, identifier, defaultState, IoDirection.UNDEFINED);
    }

    /**
     * used to set the logical to the given state
     * @param state: state, the logical is set to
     * @throws SignalAccessException
     */
    public void set(boolean state) throws SignalAccessException{
        synchronized(this){
            wrapperValue.set(state);
            wrapperValue.setValid(true);
            setValue(wrapperValue);
        }
    }

    /**
     * used to set the logical from any thread, which is not a module and not the jPac thread
     * The value is changed synchronized to the jPac cycle
     * @param state: state, the logical is set to
     */
    public void setDeferred(boolean state){
        LogicalValue localWrapperValue = new LogicalValue();
        localWrapperValue.set(state);
        localWrapperValue.setValid(true);
        setValueDeferred(localWrapperValue);
    }

    /**
     * returns the value of the logical. If the calling module is the containing module the value of this signal is returned.
     * If the calling module is a foreign module the propagated signal is returned.
     * @return see above
     * @throws org.jpac.SignalInvalidException
     */
    public boolean get() throws SignalInvalidException{
        return ((LogicalValue)getValidatedValue()).get();
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
     * used to set the intrinsic function of this signal.
     * @param intrinsicFunction 
     */
    public void setIntrinsicFunction(Supplier<Boolean> intrinsicFunction){
        setIntrinsicFct(intrinsicFunction);
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
    protected void updateValue(Signal o) throws SignalAccessException {
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

    @Override
    protected void applyTypedIntrinsicFunction() throws Exception {
        if (intrinsicFunction != null){
            set((Boolean)intrinsicFunction.get());
        }
    }
    
    @Override
    protected Value getTypedValue() {
    	return new LogicalValue();
    }

    @Override
	protected Signal getTypedProxyIoSignal(URI remoteElbfischInstance, IoDirection ioDirection) {
		Signal signal = null;
		
		try{
	    	String sigIdentifier = getIdentifier() + PROXYQUALIFIER;
			URI  sigUri = new URI(remoteElbfischInstance + "/" + getQualifiedIdentifier());
			signal = new IoLogical(containingModule, sigIdentifier, sigUri, ioDirection);
		} catch(URISyntaxException exc) {
			throw new RuntimeException("failed to instantiate proxy signal: ", exc);
		}
		return signal;
	}
    
}
