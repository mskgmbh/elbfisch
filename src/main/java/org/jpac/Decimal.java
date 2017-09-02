/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : Decimal.java
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

import java.util.function.Supplier;

/**
 * represents a decimal signal
 */
public class Decimal extends Signal{
    protected boolean        rangeChecked;
    protected double         minValue;
    protected double         maxValue;
    protected DecimalMapper  mapper;
    protected DecimalMapper  newMapper;
    protected String         unit;
    private   DecimalValue   wrapperValue;
    
    /**
     * constructs a decimal signal with intrinsic range check
     * @param containingModule: module this signal is contained in
     * @param identifier: identifier of the signal
     * @param minValue: minimum value signalValid for this decimal
     * @param maxValue: maximum value signalValid for this decimal
     * @throws org.jpac.SignalAlreadyExistsException
     */
    public Decimal(AbstractModule containingModule, String identifier, double minValue, double maxValue) throws SignalAlreadyExistsException{
        super(containingModule, identifier);
        this.minValue           = minValue;
        this.maxValue           = maxValue;
        this.rangeChecked       = true;//activate range check
        this.intrinsicFunction  = null;
        this.mapper             = null;
        this.unit               = null;
        this.value              = new DecimalValue();
        this.propagatedValue    = new DecimalValue(); 
        this.wrapperValue       = new DecimalValue();
    }
    
    /**
     * constructs a decimal signal with intrinsic range check
     * @param containingModule: module this signal is contained in
     * @param identifier: identifier of the signal
     * @param minValue: minimum value signalValid for this decimal
     * @param maxValue: maximum value signalValid for this decimal
     * @param intrinsicFunction: intrinsic function which will be applied in every cycle to calculate the actual value
     * @throws org.jpac.SignalAlreadyExistsException
     */
    public Decimal(AbstractModule containingModule, String identifier, double minValue, double maxValue, Supplier<Double> intrinsicFunction) throws SignalAlreadyExistsException{
        this(containingModule, identifier, minValue, maxValue);
        this.rangeChecked       = true;
        this.intrinsicFunction  = intrinsicFunction;
    }

    /**
     * constructs a decimal signal without range check.
     * @param containingModule: module this signal is contained in
     * @param identifier: identifier of the signal
     * @throws org.jpac.SignalAlreadyExistsException
     */
    public Decimal(AbstractModule containingModule, String identifier) throws SignalAlreadyExistsException{
        this(containingModule, identifier, 0.0, 0.0);
        this.rangeChecked      = false;
        this.intrinsicFunction = null;
    }    

    /**
     * constructs a decimal signal without range check.
     * @param containingModule: module this signal is contained in
     * @param identifier: identifier of the signal
     * @param intrinsicFunction: intrinsic function which will be applied in every cycle to calculate the actual value
     * @throws org.jpac.SignalAlreadyExistsException
     */
    public Decimal(AbstractModule containingModule, String identifier, Supplier<Double> intrinsicFunction) throws SignalAlreadyExistsException{
        this(containingModule, identifier, 0.0, 0.0);
        this.rangeChecked      = false;
        this.intrinsicFunction = intrinsicFunction;
    }    

    /**
     * constructs a decimal signal with a given default value and without range check.
     * @param containingModule: module this signal is contained in
     * @param identifier: identifier of the signal
     * @param defaultValue: default value of the signal
     * @throws org.jpac.SignalAlreadyExistsException
     */
    public Decimal(AbstractModule containingModule, String identifier, double defaultValue) throws SignalAlreadyExistsException{
        this(containingModule, identifier);
        this.rangeChecked      = false;
        this.intrinsicFunction = null;
        this.initializing      = true;//prevent signal access assertion
        try{set(defaultValue);}catch(SignalAccessException exc){/*cannot happen*/}catch(NumberOutOfRangeException exc){/*cannot happen*/};
        this.initializing      = false;
    }
    
    /**
     * constructs a decimal signal with a given default value and intrinsic range check
     * @param containingModule: module this signal is contained in
     * @param identifier: identifier of the signal
     * @param minValue: minimum value signalValid for this decimal
     * @param maxValue: maximum value signalValid for this decimal
     * @param defaultValue: default value of the decimal
     * @throws NumberOutOfRangeException
     * @throws org.jpac.SignalAlreadyExistsException
     */
    public Decimal(AbstractModule containingModule, String identifier, double minValue, double maxValue, double defaultValue) throws NumberOutOfRangeException, SignalAlreadyExistsException{
        this(containingModule, identifier, minValue, maxValue);
        this.rangeChecked      = true;
        this.intrinsicFunction = null;
        this.initializing      = true;//prevent signal access assertion
        try{set(defaultValue);}catch(SignalAccessException exc){/*cannot happen*/};
        this.initializing      = false;
    }

    
    /**
     * used to set the decimal to the given value
     * @param value: value, the decimal is set to
     * @throws org.jpac.NumberOutOfRangeException
     * @throws org.jpac.SignalAccessException
     */
    public void set(double value) throws NumberOutOfRangeException, SignalAccessException{
        synchronized(this){
            assertRange(value);
            wrapperValue.set(value);
            setValue(wrapperValue);
        }
    }
    
    /**
     * used to set the Decimal from any thread, which is not a module and not the jPac thread
     * The value is changed synchronized to the jPac cycle
     * @param value: value, the decimal is set to
     */
    public void setDeferred(double value){
        DecimalValue localWrapperValue = new DecimalValue();
        localWrapperValue.set(value);
        setValueDeferred(localWrapperValue);
    }
    

    /**
     * returns the value of the decimal. If the calling module is the containing module the value of this signal is returned.
     * If the calling module is a foreign module the propagated signal is returned.
     * @return see above
     * @throws org.jpac.SignalInvalidException
     */
    public double get() throws SignalInvalidException{
        return ((DecimalValue)getValidatedValue()).get();
    }

    /**
     * returns a process event (DecimalExceeds), which is fired, if the decimal exceeds the given threshold
     * @param threshold: threshold to be supervised
     */
    public DecimalExceeds exceeds(double threshold){
        return new DecimalExceeds(this, threshold);
    }

    /**
     * returns a process event (DecimalFallsBelow), which is fired, if the decimal falls below the given threshold
     * @param threshold: threshold to be supervised
     */
    public DecimalFallsBelow fallsBelow(double threshold){
        return new DecimalFallsBelow(this, threshold);
    }

    /**
     * returns a process event (DecimalChanges), which is fired, if the decimal changes above the given threshold in relation to the given baseValue
     * @param baseValue
     * @param threshold: threshold to be supervised
     */
    public DecimalChanges changes(double baseValue, double threshold){
        return new DecimalChanges(this, baseValue, threshold);
    }

    /**
     * returns a process event (DecimalChanges), which is fired, whenever the decimal changes.
     * CAUTION: may be fired unintentionally due to noise in the last decimal places
     */
    public DecimalChanges changes(){
        return new DecimalChanges(this, 0.0, 0.0);
    }

    private void assertRange(double newValue) throws NumberOutOfRangeException{
        if (isRangeChecked()){
            if (newValue > getMaxValue() || newValue < getMinValue()){
                setValid(false);
                throw new NumberOutOfRangeException(newValue,getMinValue(), getMaxValue());
            }
        }
    }

    /**
     * used to connect this decimal to another decimal. One decimal can be connected
     * to multiple decimals.
     * The connection is unidirectional: Changes of the connecting signal (sourceSignal) will be
     * propagated to the signals it is connected to (targetSignal): sourceSignal.connect(targetSignal).
     * @param targetSignal
     */
    public void connect(Decimal targetSignal) throws SignalAlreadyConnectedException{
        synchronized(this){
            targetSignal.setNewMapper(null);
            super.connect(targetSignal);
        }
    }

    /**
     * used to connect this decimal to another decimal. One decimal can be connected
     * to multiple decimals.
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

    /**
     * used to connect this decimal to another decimal. One decimal can be connected
     * to multiple decimals.
     * The connection is unidirectional: Changes of the connecting signal (sourceSignal) will be
     * propagated to the signals it is connected to (targetSignal): sourceSignal.connect(targetSignal).
     * @param targetSignal
     * @throws org.jpac.SignalAlreadyConnectedException
     */
    public void connect(SignedInteger targetSignal) throws SignalAlreadyConnectedException{
        synchronized(this){
            targetSignal.setNewMapper(new SignedIntegerMapper(Integer.MIN_VALUE, Integer.MAX_VALUE,Integer.MIN_VALUE, Integer.MAX_VALUE));        
            super.connect(targetSignal);
        }
    }

    /**
     * used to connect this decimal to another decimal. One decimal can be connected
     * to multiple decimals.
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
    public double getMinValue() {
        return minValue;
    }

    /**
     * @param minValue the minValue to set
     * @throws org.jpac.SignalAccessException
     */
    protected void setMinValue(double minValue) throws SignalAccessException{
        assertContainingModule();
        this.minValue = minValue;
        setRangeChecked(this.minValue > Double.MIN_VALUE || this.maxValue < Double.MAX_VALUE);
    }

    /**
     * @return the maxValue
     */
    public double getMaxValue() {
        return maxValue;
    }

    /**
     * @param maxValue the maxValue to set
     * @throws org.jpac.SignalAccessException
     */
    protected void setMaxValue(double maxValue) throws SignalAccessException{
        assertContainingModule();
        this.maxValue = maxValue;
        setRangeChecked(this.minValue > Double.MIN_VALUE || this.maxValue < Double.MAX_VALUE);
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
    public DecimalMapper getMapper() {
        return mapper;
    }

    /**
     * @param decimalMapper
     */
    protected void setMapper(DecimalMapper decimalMapper){   
        this.mapper = decimalMapper;
    }

    /**
     * @return the mapper
     */
    public DecimalMapper getNewMapper() {
        return newMapper;
    }

    /**
     * @param decimalMapper
     */
    protected void setNewMapper(DecimalMapper decimalMapper){   
        this.newMapper = decimalMapper;
    }

    @Override
    protected boolean isCompatibleSignal(Signal signal) {
        return signal instanceof Decimal;
    }

    @Override
    protected void propagateSignalInternally() {
        //physically copy the value to the propagated value
        ((DecimalValue)getPropagatedValue()).copy((DecimalValue)getValue());
    }

    @Override
    protected void updateValue(Object o, Object arg) throws SignalAccessException {
        try{
            if (o instanceof Decimal){
                if (getMapper() != null){
                    set(getMapper().map(((Decimal)o).get()));
                }
                else{
                    set(((Decimal)o).get());
                }
            }
            if (o instanceof SignedInteger){
                //this instance must supply a mapper
                set(getMapper().map(((SignedInteger)o).get()));
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
           set((Double)intrinsicFunction.get()); 
        }
    }
}
