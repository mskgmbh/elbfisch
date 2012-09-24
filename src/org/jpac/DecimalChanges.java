/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : DecimalChanges.java
 * VERSION   : $Revision: 1.4 $
 * DATE      : $Date: 2012/06/22 10:29:45 $
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
 * LOG       : $Log: DecimalChanges.java,v $
 * LOG       : Revision 1.4  2012/06/22 10:29:45  schuster
 * LOG       : change event fired on  >=
 * LOG       :
 * LOG       : Revision 1.3  2012/04/25 15:58:43  schuster
 * LOG       : Change events extended to alterations without base value
 * LOG       :
 * LOG       : Revision 1.2  2012/04/24 06:37:08  schuster
 * LOG       : some improvements concerning consistency
 * LOG       :
 */

package org.jpac;

public class DecimalChanges extends DecimalEvent{ 
    private double baseValue;

    public DecimalChanges(Decimal decimal, double baseValue, double threshold){
        super(decimal, threshold);
        this.baseValue = baseValue;
    }
    
    public void setBaseValue(double baseValue){
        this.baseValue = baseValue;
    }
    
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
