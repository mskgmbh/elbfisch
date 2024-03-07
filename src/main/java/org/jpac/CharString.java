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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Supplier;
import org.jpac.vioss.IoCharString;

/**
 * represents a char string signal
 */
public class CharString extends Signal{
    
    private   CharStringValue  wrapperValue;
    
    /**
     * constructs a char string signal
     * @param containingModule: module this signal is contained in
     * @param identifier: identifier of the signal
     * @param intrinsicFunction: intrinsic function which will be applied in every cycle to calculate the actual value
     * @param ioDirection: defines the signal as being either an INPUT or OUTPUT signal. (Relevant in distributed applications)
     * @throws org.jpac.SignalAlreadyExistsException
     */
    public CharString(AbstractModule containingModule, String identifier, Supplier<String> intrinsicFunction, IoDirection ioDirection) throws SignalAlreadyExistsException{
        super(containingModule, identifier, intrinsicFunction, ioDirection);
        this.wrapperValue       = new CharStringValue();
    }

    /**
     * constructs a char string signal
     * @param containingModule: module this signal is contained in
     * @param identifier: identifier of the signal
     * @param intrinsicFunction: intrinsic function which will be applied in every cycle to calculate the actual value
     * @throws org.jpac.SignalAlreadyExistsException
     */
    public CharString(AbstractModule containingModule, String identifier, Supplier<String> intrinsicFunction) throws SignalAlreadyExistsException{
        super(containingModule, identifier, intrinsicFunction, IoDirection.UNDEFINED);
        this.wrapperValue       = new CharStringValue();
    }

    /**
     * constructs a char string signal
     * @param containingModule: module this signal is contained in
     * @param identifier: identifier of the signal
     * @param ioDirection: defines the signal as being either an INPUT or OUTPUT signal. (Relevant in distributed applications)
     * @throws org.jpac.SignalAlreadyExistsException
     */
    public CharString(AbstractModule containingModule, String identifier, IoDirection ioDirection) throws SignalAlreadyExistsException{
        this(containingModule, identifier, (Supplier<String>) null, ioDirection);
    }
    
    /**
     * constructs a char string signal
     * @param containingModule: module this signal is contained in
     * @param identifier: identifier of the signal
     * @throws org.jpac.SignalAlreadyExistsException
     */
    public CharString(AbstractModule containingModule, String identifier) throws SignalAlreadyExistsException{
        this(containingModule, identifier, (Supplier<String>) null);
    }

    /**
     * constructs a char string signal
     * @param containingModule: module this signal is contained in
     * @param identifier: identifier of the signal
     * @param defaultValue: default value of the CharString
     * @param ioDirection: defines the signal as being either an INPUT or OUTPUT signal. (Relevant in distributed applications)
     * @throws org.jpac.SignalAlreadyExistsException
     * 
     */
    public CharString(AbstractModule containingModule, String identifier, String defaultValue, IoDirection ioDirection) throws SignalAlreadyExistsException{
        this(containingModule, identifier, ioDirection);
        this.initializing = true;//prevent signal access assertion
        try{set(defaultValue);}catch(SignalAccessException exc){/*cannot happen*/};
        this.initializing = false;
    }

    /**
     * constructs a char string signal
     * @param containingModule: module this signal is contained in
     * @param identifier: identifier of the signal
     * @param defaultValue: default value of the CharString
     * @throws org.jpac.SignalAlreadyExistsException
     * 
     */
    public CharString(AbstractModule containingModule, String identifier, String defaultValue) throws SignalAlreadyExistsException{
        this(containingModule, identifier);
        this.initializing = true;//prevent signal access assertion
        try{set(defaultValue);}catch(SignalAccessException exc){/*cannot happen*/};
        this.initializing = false;
    }

    /**
     * used to set the char string to the given value
     * @param value: value, the char string is set to
     * @throws org.jpac.SignalAccessException when a given module is not allowed to set the signal
     */
    public void set(String value) throws SignalAccessException{
        synchronized(this){
            wrapperValue.set(value);
            wrapperValue.setValid(true);            
            setValue(wrapperValue);
        }
    }
    
    /**
     * used to set the CharString from any thread, which is not a module and not the jPac thread
     * The value is changed synchronized to the jPac cycle
     * @param value: value, the char string is set to
     */
    public void setDeferred(String value){
        CharStringValue localWrapperValue = new CharStringValue();
        localWrapperValue.set(value);
        localWrapperValue.setValid(true);
        setValueDeferred(localWrapperValue);
    }
    

    /**
     * returns the value of the char string. If the calling module is the containing module the value of this signal is returned.
     * If the calling module is a foreign module the propagated signal is returned.
     * @return see above
     * @throws org.jpac.SignalInvalidException if the valid is not valid in the case of accessing it.
     */
    public String get() throws SignalInvalidException{
        return ((CharStringValue)getValidatedValue()).get();
    }
    
    /**
     * used to set the intrinsic function of this signal.
     * @param intrinsicFunction 
     */
    public void setIntrinsicFunction(Supplier<String> intrinsicFunction){
        setIntrinsicFct(intrinsicFunction);
    }

    /**
     * returns a process event (CharStringChanges), which is fired, if the char string changes
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
     * @throws org.jpac.SignalAlreadyConnectedException
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
    protected void updateValue(Signal o) throws SignalAccessException {
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

    @Override
    protected void applyTypedIntrinsicFunction() throws Exception {
        if (intrinsicFunction != null){
           set((String)intrinsicFunction.get()); 
        }
    }
    
    @Override
	protected Value getTypedValue() {
		return new CharStringValue();
	}

    @Override
	protected Signal getTypedProxyIoSignal(URI remoteElbfischInstance, IoDirection ioDirection) {
		Signal signal = null;
		
		try{
	    	String sigIdentifier = getIdentifier() + PROXYQUALIFIER;
			URI  sigUri = new URI(remoteElbfischInstance + "/" + getQualifiedIdentifier());
			signal = new IoCharString(containingModule, sigIdentifier, sigUri, ioDirection);
		} catch(URISyntaxException exc) {
			throw new RuntimeException("failed to instantiate proxy signal: ", exc);
		}
		return signal;
	}    
}
