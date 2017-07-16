/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : SignedIntegerChanges.java
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
 * process event indicating, that the value of a signed integer signal has changed
 * @author berndschuster
 */
public class SignedIntegerChanges extends SignedIntegerEvent{ 
    private int baseValue;
    
    /**
     * constructs a SignedIntegerChanges
     * @param signedInteger the SignedInteger to supervise
     * @param baseValue the value the deviation is related to
     * @param threshold the deviation, which, if exceeded will be cause the process event
     */
    public SignedIntegerChanges(SignedInteger signedInteger, int baseValue, int threshold){
        super(signedInteger, threshold);
        this.baseValue = baseValue;
    }
    
    /**
     * set the value, the deviation is related to
     * @param baseValue 
     */
    public void setBaseValue(int baseValue){
        this.baseValue = baseValue;
    }
    
    /**
     * 
     * @return the base value 
     */
    public int getBaseValue(){
        return this.baseValue;
    }
    
    @Override
    public boolean fire() throws ProcessException {
        boolean fire = false;
        if (threshold != 0){
            fire = Math.abs(signedInteger.get() - baseValue) >= threshold;
        }
        else{
            fire = signedInteger.isChanged();
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
        if (fireable instanceof SignedIntegerChanges){
            SignedIntegerChanges sc = (SignedIntegerChanges)fireable;
            equal = this.signedInteger.equals(sc.signedInteger) && threshold == sc.threshold;
        }
        return equal;
    }
}
