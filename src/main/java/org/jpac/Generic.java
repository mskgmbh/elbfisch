/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : Generic.java
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
import java.util.function.Supplier;


/**
 * class template for generic objects used as signals, where ValueImpl is a class 
 * implementing the org.jpac.Value interface
 * @author berndschuster
 * 
 */
public class Generic<ValueImpl> extends Signal{    

    /**
     * constructs a generic signal
     * @param containingModule: module, this signal is contained in
     * @param identifier: identifier of the signal
     * @param intrinsicFunction: intrinsic function which will be applied in every cycle to calculate the actual value
     * @param ioDirection: defines the signal as being either an INPUT or OUTPUT signal. (Relevant in distributed applications)
     * @throws org.jpac.SignalAlreadyExistsException
     */
    public Generic(AbstractModule containingModule, String identifier, Supplier<ValueImpl> intrinsicFunction, IoDirection ioDirection) throws SignalAlreadyExistsException{
        super(containingModule, identifier, intrinsicFunction, ioDirection);
    }
	
    /**
     * constructs a generic signal
     * @param containingModule: module, this signal is contained in
     * @param identifier: identifier of the signal
     * @param ioDirection: defines the signal as being either an INPUT or OUTPUT signal. (Relevant in distributed applications)
     * @throws org.jpac.SignalAlreadyExistsException
     */
    public Generic(AbstractModule containingModule, String identifier, IoDirection ioDirection) throws SignalAlreadyExistsException{
        this(containingModule, identifier, null, ioDirection);
    }

    /**
     * constructs a Generic signal
     * @param containingModule: module, this signal is contained in
     * @param identifier: identifier of the signal
     * @param intrinsicFunction: intrinsic function which will be applied in every cycle to calculate the actual value
     * @throws org.jpac.SignalAlreadyExistsException
     */    
    public Generic(AbstractModule containingModule, String identifier, Supplier<ValueImpl> intrinsicFunction) throws SignalAlreadyExistsException{
    	this(containingModule, identifier, intrinsicFunction, IoDirection.UNDEFINED);
    }

    /**
     * constructs a Generic signal
     * @param containingModule: module, this signal is contained in
     * @param identifier: identifier of the signal
     * @throws org.jpac.SignalAlreadyExistsException
     */    
    public Generic(AbstractModule containingModule, String identifier) throws SignalAlreadyExistsException{
    	this(containingModule, identifier, (Supplier<ValueImpl>) null);
    }

    /**
     * constructs a Generic signal with a given default value
     * @param containingModule: module this signal is contained in
     * @param identifier: identifier of the signal
     * @param defaultValue: default value of the signal
     * @throws org.jpac.SignalAlreadyExistsException
     */   
    public Generic(AbstractModule containingModule, String identifier, ValueImpl defaultValue) throws SignalAlreadyExistsException{
        this(containingModule, identifier);
        this.initializing = true;//prevent signal access assertion
        try{set(defaultValue);}catch(SignalAccessException exc){/*cannot happen*/};
        this.initializing = false;        
    }

    @Override
    protected boolean isCompatibleSignal(Signal signal) {
        return signal instanceof Generic;
    }
    
    /**
     * used to set the Generic to the given value
     * @param value: value, the signal is set to
     * @throws org.jpac.SignalAccessException
     */
    public void set(ValueImpl value) throws SignalAccessException{
        setValue((Value)value);
        setValid(true);
    }

    /**
     * used to set the Generic from any thread, which is not a module and not the jPac thread
     * The value is changed synchronized to the jPac cycle
     * @param value: value, the Generic is set to
     * @throws SignalAccessException
     */    
    public void setDeferred(ValueImpl value) throws SignalAccessException{
        setValueDeferred((Value)value);
    }
    
    /**
     * returns the value of the Generic. If the calling module is the containing module the value of this signal is returned.
     * If the calling module is a foreign module the propagated signal is returned.
     * @return see above
     * @throws org.jpac.SignalInvalidException
     */
    @SuppressWarnings("unchecked")
	public ValueImpl get() throws SignalInvalidException{
        return (ValueImpl)getValidatedValue();
    }
    
     /**
     * used to set the intrinsic function of this signal.
     * @param intrinsicFunction 
     */
    public void setIntrinsicFunction(Supplier<ValueImpl> intrinsicFunction){
        setIntrinsicFct(intrinsicFunction);
    }

    @SuppressWarnings("unchecked")
	@Override
    protected void updateValue(Object o, Object arg) throws SignalAccessException {
        try{
            set(((Generic<ValueImpl>)o).get());
        }
        catch(Exception exc){
            throw new SignalAccessException(exc.getMessage());
        }  
    }

    @Override
    protected void propagateSignalInternally() throws SignalInvalidException {
        //physically copy the value to the propagated value
        if (propagatedValue == null && value != null){
           try{
               propagatedValue = value.clone(); 
           }
           catch(CloneNotSupportedException exc){
               throw new SignalInvalidException(exc.getMessage());
           }
        }
        else if (propagatedValue != null && value == null){
            propagatedValue = null;
        }
        else if (propagatedValue != null && value != null){
            propagatedValue.copy(value);
        }
    }

    @SuppressWarnings("unchecked")
	@Override
    protected void applyTypedIntrinsicFunction() throws Exception {
        if (intrinsicFunction != null){
           set((ValueImpl)intrinsicFunction.get()); 
        }
    }
    
    @Override
    protected Value getTypedValue() {
    	throw new UnsupportedOperationException("method getTypedValue() must be implemented by concrete Generic<ValueImpl>");
    	//return new <Generic<ValueImpl>()>;
    }

    @Override
    protected Signal getTypedProxyIoSignal(URI remoteElbfischInstance, IoDirection ioDirection) {
    	throw new UnsupportedOperationException("method getTypedProxyIoSignal() must be implemented by concrete Generic<ValueImpl>");
  		// Signal signal = null;
  		//
  		//try{
  	    //	String sigIdentifier = getIdentifier() + PROXYQUALIFIER;
  		//	URI  sigUri = new URI(remoteElbfischInstance + "/" + getQualifiedIdentifier());
  		//	signal = new <IoGeneric>(containingModule, sigIdentifier, sigUri, ioDirection);
  		//} catch(URISyntaxException exc) {
  		//	throw new RuntimeException("failed to instantiate proxy signal: ", exc);
  		//}
  		//return signal;
    }
    
}
