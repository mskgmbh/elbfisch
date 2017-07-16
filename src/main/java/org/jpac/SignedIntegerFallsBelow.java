/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : SignedIntegerFallsBelow.java
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
 * process event indicating, that the value of a signed integer signal falls below a given threshold
 * @author berndschuster
 */
public class SignedIntegerFallsBelow extends SignedIntegerEvent{

    /**
     * constructs a SignedIntegerFallsBelow
     * @param signedInteger the SignedInteger to supervise
     * @param threshold the threshold, which, if underrun, causes the process event
     */
    public SignedIntegerFallsBelow(SignedInteger signedInteger, int threshold){
        super(signedInteger, threshold);
    }

    @Override
    public boolean fire() throws ProcessException {
        return signedInteger.get() < threshold;
    }
    
    @Override
    public String toString(){
        return super.toString() + ".fallsBelow(" + threshold + ")";
    }

    @Override
    protected boolean equalsCondition(Fireable fireable){
        boolean equal = false;
        if (fireable instanceof SignedIntegerFallsBelow){
            SignedIntegerFallsBelow sf = (SignedIntegerFallsBelow)fireable;
            equal = this.signedInteger.equals(sf.signedInteger) && threshold == sf.threshold;
        }
        return equal;
    }
}
