/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : DecimalChanges.java
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
 * process event indicating, that the value of a decimal signal has changed
 * @author berndschuster
 */
public class DecimalChanges extends DecimalEvent{ 
    private double baseValue;

    /**
     * constructs a DecimalChanges
     * @param decimal the decimal to supervise
     * @param baseValue the value the deviation is related to
     * @param threshold the deviation, which, if exceeded will be cause the process event
     */
    public DecimalChanges(Decimal decimal, double baseValue, double threshold){
        super(decimal, threshold);
        this.baseValue = baseValue;
    }
    
    /**
     * set the value, the deviation is related to
     * @param baseValue 
     */
    public void setBaseValue(double baseValue){
        this.baseValue = baseValue;
    }
    
    /**
     * 
     * @return the base value 
     */
    public double getBaseValue(){
        return this.baseValue;
    }
    
    @Override
    public boolean fire() throws ProcessException {
        boolean fire = false;
        if (threshold > 0.00000000000000001){
            fire = Math.abs(decimal.get() - baseValue) >= threshold;
        }
        else{
            fire = decimal.isChanged();
        }
        return fire;
    }
    
    @Override
    public String toString(){
        return super.toString() + ".changes(" + threshold + ")";
    }
    
    @Override
    protected boolean equalsCondition(Fireable fireable){
        boolean equal = false;
        if (fireable instanceof DecimalChanges){
            DecimalChanges dc = (DecimalChanges)fireable;
            equal = this.decimal.equals(dc.decimal) && Math.abs(threshold - dc.threshold) < 0.00000000000000001;
        }
        return equal;
    }
}
