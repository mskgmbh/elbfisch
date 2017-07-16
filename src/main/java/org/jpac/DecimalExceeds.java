/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : DecimalExceeds.java
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
 * process event indicating, that the value of a decimal signal exceeds a given value
 * @author berndschuster
 */

public class DecimalExceeds extends DecimalEvent{

    /**
     * constructs a DecimalExceeds
     * @param decimal the decimal to supervise
     * @param threshold the threshold, which, if exceeded, causes the process event
     */
    public DecimalExceeds(Decimal decimal, double threshold){
        super(decimal, threshold);
    }

    @Override
    public boolean fire() throws ProcessException {
        return decimal.get() > threshold;
    }

    @Override
    public String toString(){
        return super.toString() + ".exceeds(" + threshold + ")";
    }
    
    @Override
    protected boolean equalsCondition(Fireable fireable){
        boolean equal = false;
        if (fireable instanceof DecimalExceeds){
            DecimalExceeds de = (DecimalExceeds)fireable;
            equal = this.decimal.equals(de.decimal) && Math.abs(threshold - de.threshold) < 0.00000000000000001;
        }
        return equal;
    }
}
