/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : DecimalExceeds.java
 * VERSION   : $Revision: 1.2 $
 * DATE      : $Date: 2012/04/24 06:37:08 $
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
 *
 * LOG       : $Log: DecimalExceeds.java,v $
 * LOG       : Revision 1.2  2012/04/24 06:37:08  schuster
 * LOG       : some improvements concerning consistency
 * LOG       :
 */

package org.jpac;

public class DecimalExceeds extends DecimalEvent{

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
