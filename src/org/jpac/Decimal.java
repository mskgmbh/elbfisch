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

/**
 * represents a decimal signal
 */
public class Decimal extends Signal{
    protected boolean       rangeChecked;
    protected double        minValue;
    protected double        maxValue;
    protected DecimalMapper mapper;
    protected DecimalMapper targetSignalMapper;
    protected String        unit;
    private   DecimalValue  wrapperValue;
    
    /**
     * constructs a decimal signal with intrinsic range check
     * @param containingModule: module this signal is contained in
     * @param identifier: identifier of the signal
     * @param minValue: minimum value signalValid for this decimal
     * @param maxValue: maximum value signalValid for this decimal
     */
    public Decimal(AbstractModule containingModule, String identifier, double minValue, double maxValue){
        super(containingModule, identifier);
        this.minValue           = minValue;
        this.maxValue           = maxValue;
        this.rangeChecked       = true;//activate range check
        this.mapper             = null;
        this.targetSignalMapper = null;
        this.unit               = null;
        this.value              = new DecimalValue();
        this.propagatedValue    = new DecimalValue(); 
        this.wrapperValue       = new DecimalValue();
    }
    
    /**
     * constructs a decimal signal without range check.
     * @param containingModule: module this signal is contained in
     * @param identifier: identifier of the signal
     */
    public Decimal(AbstractModule containingModule, String identifier){
        this(containingModule, identifier, 0.0, 0.0);
        this.rangeChecked = false;
    }    

    /**
     * constructs a decimal signal with a given default value and without range check.
     * @param containingModule: module this signal is contained in
     * @param identifier: identifier of the signal
     * @param defaultValue: default value of the signal
     */
    public Decimal(AbstractModule containingModule, String identifier, double defaultValue){
        this(containingModule, identifier);
        this.initializing = true;//prevent signal access assertion
        try{set(defaultValue);}catch(SignalAccessException exc){/*cannot happen*/}catch(NumberOutOfRangeException exc){/*cannot happen*/};
        this.initializing = false;
    }
    
    /**
     * constructs a decimal signal with a given default value and intrinsic range check
     * @param containingModule: module this signal is contained in
     * @param identifier: identifier of the signal
     * @param minValue: minimum value signalValid for this decimal
     * @param maxValue: maximum value signalValid for this decimal
     * @param defaultValue: default value of the decimal
     * @throws NumberOutOfRangeException: if the default value is less than minValue or greater than maxValue
     */
    public Decimal(AbstractModule containingModule, String identifier, double minValue, double maxValue, double defaultValue) throws NumberOutOfRangeException{
        this(containingModule, identifier, minValue, maxValue);
        this.initializing = true;//prevent signal access assertion
        try{set(defaultValue);}catch(SignalAccessException exc){/*cannot happen*/};
        this.initializing = false;
    }

    
    /**
     * used to set the decimal to the given value
     * @param value: value, the decimal is set to
     */
    public void set(double value) throws NumberOutOfRangeException, SignalAccessException{
        assertRange(value);
        wrapperValue.set(value);
        setValue(wrapperValue);
    }

    /**
     * returns the value of the decimal. If the calling module is the containing module the value of this signal is returned.
     * If the calling module is a foreign module the propagated signal is returned.
     * @return see above
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
     * @param threshold: threshold to be supervised
     */
    public DecimalChanges changes(double baseValue, double threshold){
        return new DecimalChanges(this, baseValue, threshold);
    }

    /**
     * returns a process event (DecimalChanges), which is fired, whenever the decimal changes.
     * CAUTION: may be fired unintentionally due to noise in the last decimal places
     * @param threshold: threshold to be supervised
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
        super.connect(targetSignal);
    }

    /**
     * used to connect this decimal to another decimal. One decimal can be connected
     * to multiple decimals.
     * The connection is unidirectional: Changes of the connecting signal (sourceSignal) will be
     * propagated to the signals it is connected to (targetSignal): sourceSignal.connect(targetSignal).
     * @param targetSignal
     * @param mapper used to map the source decimal to the target decimal.
     *
     */
    public void connect(Decimal targetSignal, DecimalMapper decimalMapper) throws SignalAlreadyConnectedException, SignalAccessException{
        //the target is responsible for correct value mapping
        targetSignalMapper = decimalMapper;
        super.connect(targetSignal);
    }

    @Override
    protected void deferredConnect(Signal targetSignal) throws SignalAlreadyConnectedException{
        super.deferredConnect(targetSignal);
        ((Decimal)targetSignal).setMapper(targetSignalMapper);
    }

    @Override
    protected void deferredDisconnect(Signal targetSignal){
        super.deferredDisconnect(targetSignal);
        ((Decimal)targetSignal).setMapper(null);
        //clear target signal mapper
        targetSignalMapper = null;
    }
    
    
    /**
     * @return the rangeChecked
     */
    public boolean isRangeChecked() {
        return rangeChecked;
    }

    /**
     * @param rangeChecked the rangeChecked to set
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
     */
    protected void setMinValue(double minValue) throws SignalAccessException{
        assertContainingModule();
        this.minValue = minValue;
    }

    /**
     * @return the maxValue
     */
    public double getMaxValue() {
        return maxValue;
    }

    /**
     * @param maxValue the maxValue to set
     */
    protected void setMaxValue(double maxValue) throws SignalAccessException{
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
     * @param mapper the mapper to set
     */
    protected void setMapper(DecimalMapper decimalMapper){   
        this.mapper = decimalMapper;
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
                if (getMapper() != null){
                    set(getMapper().map(((SignedInteger)o).get()));
                }
                else{
                    set(((SignedInteger)o).get());
                }
            }
        }
        catch(Exception exc){
            Log.error("Error: ", exc);
            throw new SignalAccessException(exc.getMessage());
        }
    }
}
