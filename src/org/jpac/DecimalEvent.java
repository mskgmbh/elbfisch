/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : DecimalEvent.java
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
 * base class of some process events concerning decimal signals
 * @author berndschuster
 */

abstract class DecimalEvent extends ProcessEvent{
    protected Decimal decimal;
    protected double  threshold;
    
    
    DecimalEvent(Decimal decimal, double threshold){
        super();
        this.decimal   = decimal;
        this.threshold = threshold;
    }
    
    public void setDecimal(Decimal decimal){
        this.decimal = decimal;
    }
    
    public Decimal getDecimal(){
        return this.decimal;
    }
    
    /**
     * the threshold, which, if exceeded, causes the process event
     * @param threshold 
     */
    public void setThreshold(double threshold){
        this.threshold = threshold;
    }
    
    public double getThreshold(){
        return this.threshold;
    }
    
    @Override
    public String toString(){
        return getClass().getSimpleName() + "(" + decimal + ")";
    }
}
