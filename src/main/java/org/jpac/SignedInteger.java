/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : SignedInteger.java
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

import org.jpac.vioss.IoSignedInteger;

/**
 * represents a signed integer signal
 */
public class SignedInteger extends Signal{
    protected boolean             rangeChecked;
    protected int                 minValue;
    protected int                 maxValue;
    protected SignedIntegerMapper mapper;
    protected SignedIntegerMapper newMapper;
    protected String              unit;
    private   SignedIntegerValue  wrapperValue;

    /**
     * constructs a signed integer signal with intrinsic and range check
     * @param containingModule: module this signal is contained in
     * @param identifier: identifier of the signal
     * @param minValue: minimum value signalValid for this signed integer
     * @param maxValue: maximum value signalValid for this signed integer
     * @param intrinsicFunction: intrinsic function which will be applied in every cycle to calculate the actual value
     * @param ioDirection: defines the signal as being either an INPUT or OUTPUT signal. (Relevant in distributed applications)
     * @throws org.jpac.SignalAlreadyExistsException
     */
    public SignedInteger(AbstractModule containingModule, String identifier, int minValue, int maxValue, Supplier<Integer> intrinsicFunction, IoDirection ioDirection) throws SignalAlreadyExistsException{
        super(containingModule, identifier, intrinsicFunction, ioDirection);
        this.wrapperValue       = new SignedIntegerValue();
        this.minValue           = minValue;
        this.maxValue           = maxValue;
        this.rangeChecked       = true;//activate range check
        this.mapper             = null;
        this.unit               = null;
    }

    /**
     * constructs a signed integer signal with intrinsic and range check
     * @param containingModule: module this signal is contained in
     * @param identifier: identifier of the signal
     * @param minValue: minimum value signalValid for this signed integer
     * @param maxValue: maximum value signalValid for this signed integer
     * @param intrinsicFunction: intrinsic function which will be applied in every cycle to calculate the actual value
     * @throws org.jpac.SignalAlreadyExistsException
     */
    public SignedInteger(AbstractModule containingModule, String identifier, int minValue, int maxValue, Supplier<Integer> intrinsicFunction) throws SignalAlreadyExistsException{
        this(containingModule, identifier, minValue, maxValue, intrinsicFunction, IoDirection.UNDEFINED);
    }

    /**
     * constructs a signed integer signal with rangecheck
     * @param containingModule: module this signal is contained in
     * @param identifier: identifier of the signal
     * @param minValue: minimum value signalValid for this signed integer
     * @param maxValue: maximum value signalValid for this signed integer
     * @param ioDirection: defines the signal as being either an INPUT or OUTPUT signal. (Relevant in distributed applications)
     * @throws org.jpac.SignalAlreadyExistsException
     */
    public SignedInteger(AbstractModule containingModule, String identifier, int minValue, int maxValue, IoDirection ioDirection) throws SignalAlreadyExistsException{
        this(containingModule, identifier, minValue, maxValue, null, ioDirection);
    }
    
    /**
     * constructs a signed integer signal with rangecheck
     * @param containingModule: module this signal is contained in
     * @param identifier: identifier of the signal
     * @param minValue: minimum value signalValid for this signed integer
     * @param maxValue: maximum value signalValid for this signed integer
     * @throws org.jpac.SignalAlreadyExistsException
     */
    public SignedInteger(AbstractModule containingModule, String identifier, int minValue, int maxValue) throws SignalAlreadyExistsException{
        this(containingModule, identifier, minValue, maxValue, null, IoDirection.UNDEFINED);
    }

    /**
     * constructs a signed integer signal.
     * range check is disabled.
     * @param containingModule: module this signal is contained in
     * @param identifier: identifier of the signal
     * @param ioDirection: defines the signal as being either an INPUT or OUTPUT signal. (Relevant in distributed applications)
     * @throws org.jpac.SignalAlreadyExistsException
     */
    public SignedInteger(AbstractModule containingModule, String identifier, IoDirection ioDirection) throws SignalAlreadyExistsException{
        this(containingModule, identifier, 0, 0, null, ioDirection);
        this.rangeChecked = false;
    }    

    /**
     * constructs a signed integer signal.
     * range check is disabled.
     * @param containingModule: module this signal is contained in
     * @param identifier: identifier of the signal
     * @throws org.jpac.SignalAlreadyExistsException
     */
    public SignedInteger(AbstractModule containingModule, String identifier) throws SignalAlreadyExistsException{
        this(containingModule, identifier, IoDirection.UNDEFINED);
    }    
    
    /**
     * constructs a signed integer signal with intrinsic function
     * range check is disabled.
     * @param containingModule: module this signal is contained in
     * @param identifier: identifier of the signal
     * @param intrinsicFunction: intrinsic function which will be applied in every cycle to calculate the actual value
     * @param ioDirection: defines the signal as being either an INPUT or OUTPUT signal. (Relevant in distributed applications)
     * @throws org.jpac.SignalAlreadyExistsException
     */
    public SignedInteger(AbstractModule containingModule, String identifier, Supplier<Integer> intrinsicFunction, IoDirection ioDirection) throws SignalAlreadyExistsException{
        this(containingModule, identifier, 0, 0, intrinsicFunction, ioDirection);
        this.rangeChecked = false;
    }    

    /**
     * constructs a signed integer signal with intrinsic function
     * range check is disabled.
     * @param containingModule: module this signal is contained in
     * @param identifier: identifier of the signal
     * @param intrinsicFunction: intrinsic function which will be applied in every cycle to calculate the actual value
     * @throws org.jpac.SignalAlreadyExistsException
     */
    public SignedInteger(AbstractModule containingModule, String identifier, Supplier<Integer> intrinsicFunction) throws SignalAlreadyExistsException{
        this(containingModule, identifier, intrinsicFunction, IoDirection.UNDEFINED);
        this.rangeChecked = false;
    }    

    /**
     * constructs a signed integer signal with a given default value.
     * @param containingModule: module this signal is contained in
     * @param identifier: identifier of the signal
     * @param defaultValue: default value of the signal
     * @param ioDirection: defines the signal as being either an INPUT or OUTPUT signal. (Relevant in distributed applications)
     * @throws org.jpac.SignalAlreadyExistsException
     */
    public SignedInteger(AbstractModule containingModule, String identifier, int defaultValue, IoDirection ioDirection) throws SignalAlreadyExistsException{
    	this(containingModule, identifier, ioDirection);
        this.rangeChecked = false;
        this.initializing = true;//prevent signal access assertion
        try{set(defaultValue);}catch(SignalAccessException exc){/*cannot happen*/}catch(NumberOutOfRangeException exc){/*cannot happen*/};
        this.initializing = false;
    }
    
    /**
     * constructs a signed integer signal with a given default value.
     * @param containingModule: module this signal is contained in
     * @param identifier: identifier of the signal
     * @param defaultValue: default value of the signal
     * @throws org.jpac.SignalAlreadyExistsException
     */
    public SignedInteger(AbstractModule containingModule, String identifier, int defaultValue) throws SignalAlreadyExistsException{
    	this(containingModule, identifier);
        this.rangeChecked = false;
        this.initializing = true;//prevent signal access assertion
        try{set(defaultValue);}catch(SignalAccessException exc){/*cannot happen*/}catch(NumberOutOfRangeException exc){/*cannot happen*/};
        this.initializing = false;
    }

    /**
     * constructs a signed integer signal with a given default value and intrinsic range check
     * @param containingModule: module this signal is contained in
     * @param identifier: identifier of the signal
     * @param minValue: minimum value for this decimal
     * @param maxValue: maximum value for this decimal
     * @param defaultValue: default value of the decimal
     * @param ioDirection: defines the signal as being either an INPUT or OUTPUT signal. (Relevant in distributed applications)
     * @throws NumberOutOfRangeException
     */
    public SignedInteger(AbstractModule containingModule, String identifier, int minValue, int maxValue, int defaultValue, IoDirection ioDirection) throws NumberOutOfRangeException, SignalAlreadyExistsException{
        this(containingModule, identifier, minValue, maxValue, ioDirection);
        this.initializing = true;//prevent signal access assertion
        try{set(defaultValue);}catch(SignalAccessException exc){/*cannot happen*/};
        this.initializing = false;
    }
        
    /**
     * constructs a signed integer signal with a given default value and intrinsic range check
     * @param containingModule: module this signal is contained in
     * @param identifier: identifier of the signal
     * @param minValue: minimum value for this decimal
     * @param maxValue: maximum value for this decimal
     * @param defaultValue: default value of the decimal
     * @throws NumberOutOfRangeException
     */
    public SignedInteger(AbstractModule containingModule, String identifier, int minValue, int maxValue, int defaultValue) throws NumberOutOfRangeException, SignalAlreadyExistsException{
        this(containingModule, identifier, minValue, maxValue, IoDirection.UNDEFINED);
        this.initializing = true;//prevent signal access assertion
        try{set(defaultValue);}catch(SignalAccessException exc){/*cannot happen*/};
        this.initializing = false;
    }

    /**
     * used to set the signed integer to the given value
     * @param value: value, the signed integer is set to
     * @throws org.jpac.NumberOutOfRangeException
     * @throws org.jpac.SignalAccessException
     */
    public void set(int value) throws NumberOutOfRangeException, SignalAccessException{
        synchronized(this){
            assertRange(value);
            wrapperValue.set(value);
            wrapperValue.setValid(true);            
            setValue(wrapperValue);
        }
    }
    
    /**
     * used to set the SignedInteger from any thread, which is not a module and not the jPac thread
     * The value is changed synchronized to the jPac cycle
     * @param value: value, the signed integer is set to
     */
    public void setDeferred(int value){
        SignedIntegerValue localWrapperValue = new SignedIntegerValue();
        localWrapperValue.set(value);
        localWrapperValue.setValid(true);
        setValueDeferred(localWrapperValue);
    }
    

    /**
     * returns the value of the signed integer. If the calling module is the containing module the value of this signal is returned.
     * If the calling module is a foreign module the propagated signal is returned.
     * @return see above
     * @throws org.jpac.SignalInvalidException
     */
    public int get() throws SignalInvalidException{
        return ((SignedIntegerValue)getValidatedValue()).get();
    }
    
    /**
     * used to set the intrinsic function of this signal.
     * @param intrinsicFunction 
     */
    public void setIntrinsicFunction(Supplier<Integer> intrinsicFunction){
        setIntrinsicFct(intrinsicFunction);
    }

    /**
     * returns a process event (SignedIntegerExceeds), which is fired, if the signed integer exceeds the given threshold
     * @param threshold: threshold to be supervised
     */    
    public SignedIntegerExceeds exceeds(int threshold){
        return new SignedIntegerExceeds(this, threshold);
    }

    /**
     * returns a process event (SignedIntegerFallsBelow), which is fired, if the signed integer falls below the given threshold
     * @param threshold: threshold to be supervised
     */    
    public SignedIntegerFallsBelow fallsBelow(int threshold){
        return new SignedIntegerFallsBelow(this, threshold);
    }

    /**
     * returns a process event (SignedIntegerChanges), which is fired, if the signed integer changes more than the given 
     * threshold in relation to the given baseValue
     * @param baseValue
     * @param threshold: threshold to be supervised
     */    
    public SignedIntegerChanges changes(int baseValue, int threshold){
        return new SignedIntegerChanges(this, baseValue, threshold);
    }

    /**
     * returns a process event (SignedIntegerChanges), which is fired, if the signed integer changes
     */    
    public SignedIntegerChanges changes(){
        return new SignedIntegerChanges(this, 0, 0);
    }

    private void assertRange(int newValue) throws NumberOutOfRangeException{
        if (isRangeChecked()){
            if (newValue > getMaxValue() || newValue < getMinValue()){
                setValid(false);
                throw new NumberOutOfRangeException(newValue,getMinValue(), getMaxValue());
            }
        }
    }

    /**
     * used to connect this signed integer to another signed integer. One signed integer can be connected
     * to multiple signed integers.
     * The connection is unidirectional: Changes of the connecting signal (sourceSignal) will be
     * propagated to the signals it is connected to (targetSignal): sourceSignal.connect(targetSignal).
     * @param targetSignal
     * @throws org.jpac.SignalAlreadyConnectedException
     */
    public void connect(SignedInteger targetSignal) throws SignalAlreadyConnectedException{
        synchronized(this){
            targetSignal.setNewMapper(null);
            super.connect(targetSignal);
        }
    }

    /**
     * used to connect this signed integer to another signed integer. One signed integer can be connected
     * to multiple signed integers.
     * The connection is unidirectional: Changes of the connecting signal (sourceSignal) will be
     * propagated to the signals it is connected to (targetSignal): sourceSignal.connect(targetSignal).
     * @param targetSignal
     * @param signedIntegerMapper
     * @throws org.jpac.SignalAlreadyConnectedException
     * @throws org.jpac.SignalAccessException
     *
     */
    public void connect(SignedInteger targetSignal, SignedIntegerMapper signedIntegerMapper) throws SignalAlreadyConnectedException, SignalAccessException{
        synchronized(this){
            //the target is responsible for correct value mapping
            targetSignal.setNewMapper(signedIntegerMapper);
            super.connect(targetSignal);
        }
    }

    /**
     * used to connect this signed integer to Decimal. One signed integer can be connected
     * to multiple signals.
     * The connection is unidirectional: Changes of the connecting signal (sourceSignal) will be
     * propagated to the signals it is connected to (targetSignal): sourceSignal.connect(targetSignal).
     * @param targetSignal
     * @throws org.jpac.SignalAlreadyConnectedException
     */
    public void connect(Decimal targetSignal) throws SignalAlreadyConnectedException{
        synchronized(this){
            targetSignal.setNewMapper(new DecimalMapper(Integer.MIN_VALUE, Integer.MAX_VALUE,Integer.MIN_VALUE, Integer.MAX_VALUE));
            super.connect(targetSignal);
        }
    }

    /**
     * used to connect this signed integer to Decimal. One signed integer can be connected
     * to multiple signals.
     * The connection is unidirectional: Changes of the connecting signal (sourceSignal) will be
     * propagated to the signals it is connected to (targetSignal): sourceSignal.connect(targetSignal).
     * @param targetSignal
     * @param decimalMapper
     * @throws org.jpac.SignalAlreadyConnectedException
     * @throws org.jpac.SignalAccessException
     *
     */
    public void connect(Decimal targetSignal, DecimalMapper decimalMapper) throws SignalAlreadyConnectedException, SignalAccessException{
        synchronized(this){
            //the target is responsible for correct value mapping
            targetSignal.setNewMapper(decimalMapper);
            super.connect(targetSignal);
        }
    }
    
    @Override
    protected void deferredConnect(Signal targetSignal) throws SignalAlreadyConnectedException{
        //first install mapper
        if (targetSignal instanceof Decimal){
            ((Decimal)targetSignal).setMapper(((Decimal)targetSignal).getNewMapper());
            ((Decimal)targetSignal).setNewMapper(null);
        } else if (targetSignal instanceof SignedInteger){
            ((SignedInteger)targetSignal).setMapper(((SignedInteger)targetSignal).getNewMapper());
            ((SignedInteger)targetSignal).setNewMapper(null);
        } 
        super.deferredConnect(targetSignal);
    }
    
    @Override
    protected void deferredDisconnect(Signal targetSignal){
        //first remove mapper
        if (targetSignal instanceof Decimal){
            ((Decimal)targetSignal).setMapper(null);
        } else if (targetSignal instanceof SignedInteger){
            ((SignedInteger)targetSignal).setMapper(null);
        } 
        super.deferredDisconnect(targetSignal);
    }
    
    
    /**
     * @return the rangeChecked
     */
    public boolean isRangeChecked() {
        return rangeChecked;
    }

    /**
     * @param rangeChecked the rangeChecked to set
     * @throws org.jpac.SignalAccessException
     */
    protected void setRangeChecked(boolean rangeChecked) throws SignalAccessException {
        assertContainingModule();
        this.rangeChecked = rangeChecked;
    }

    /**
     * @return the minValue
     */
    public int getMinValue() {
        return minValue;
    }

    /**
     * @param minValue the minValue to set
     * @throws org.jpac.SignalAccessException
     */
    protected void setMinValue(int minValue) throws SignalAccessException{
        assertContainingModule();
        this.minValue = minValue;
        setRangeChecked(this.minValue > Integer.MIN_VALUE || this.maxValue < Integer.MAX_VALUE);
    }

    /**
     * @return the maxValue
     */
    public int getMaxValue() {
        return maxValue;
    }

    /**
     * @param maxValue the maxValue to set
     * @throws org.jpac.SignalAccessException
     */
    protected void setMaxValue(int maxValue) throws SignalAccessException{
        assertContainingModule();
        this.maxValue = maxValue;
    }

    /**
     * @return the maxValue
     */
    public String getUnit() {
        return this.unit;
    }

    /**
     * @param unit: string representation of the unit
     * @throws org.jpac.SignalAccessException
     */
    protected void setUnit(String unit) throws SignalAccessException{
        assertContainingModule();
        this.unit = unit;
    }

    /**
     * @return the mapper
     */
    public SignedIntegerMapper getMapper() {
        return mapper;
    }

    /**
     * @param signedIntegerMapper
     */
    protected void setMapper(SignedIntegerMapper signedIntegerMapper){   
        this.mapper = signedIntegerMapper;
    }

    /**
     * @return the mapper
     */
    public SignedIntegerMapper getNewMapper() {
        return newMapper;
    }

    /**
     * @param signedIntegerMapper
     */
    protected void setNewMapper(SignedIntegerMapper signedIntegerMapper){   
        this.newMapper = signedIntegerMapper;
    }

    @Override
    protected boolean isCompatibleSignal(Signal signal) {
        return signal instanceof SignedInteger;
    }

    @Override
    protected void propagateSignalInternally() {
        //physically copy the value to the propagated value
        ((SignedIntegerValue)getPropagatedValue()).copy((SignedIntegerValue)getValue());
    }

    @Override
    protected void updateValue(Object o, Object arg) throws SignalAccessException {
        try{
            if (o instanceof Decimal){
                //this instance must supply a mapper
                set(getMapper().map(((Decimal)o).get()));
            }
            if (o instanceof SignedInteger){
                if (getMapper() != null){
                    set(getMapper().map(((SignedInteger)o).get()));
                }
                else{
                    set(((SignedInteger)o).get());
                }
            }
        }
        catch(Exception exc){
        	if (Log.isDebugEnabled()) Log.error(this + " : " + exc.getMessage());
        	invalidate();
        }
    }

    @Override
    protected void applyTypedIntrinsicFunction() throws Exception {
        if (intrinsicFunction != null){
           set((Integer)intrinsicFunction.get()); 
        }
    }

    @Override
    protected Value getTypedValue() {
    	  return new SignedIntegerValue();
    }

    @Override
    protected Signal getTypedProxyIoSignal(URI remoteElbfischInstance, IoDirection ioDirection) {
		Signal signal = null;
		
		try{
	    	String sigIdentifier = getIdentifier() + PROXYQUALIFIER;
			URI  sigUri = new URI(remoteElbfischInstance + "/" + getQualifiedIdentifier());
			signal = new IoSignedInteger(containingModule, sigIdentifier, sigUri, ioDirection);
		} catch(URISyntaxException exc) {
			throw new RuntimeException("failed to instantiate proxy signal: ", exc);
		}
		return signal;
	}
    
}
