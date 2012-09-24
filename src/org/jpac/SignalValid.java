/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : SignalValid.java
 * VERSION   : $Revision: 1.2 $
 * DATE      : $Date: 2012/03/09 10:30:28 $
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
 * LOG       : $Log: SignalValid.java,v $
 * LOG       : Revision 1.2  2012/03/09 10:30:28  schuster
 * LOG       : Firable.fire(), Fireable.reset() made public
 * LOG       :
 */

package org.jpac;

/**
 *
 * @author berndschuster
 */
public class SignalValid extends ProcessEvent{
    Signal signal;
    public SignalValid(Signal signal){
        this.signal = signal;
    }

    @Override
    public boolean fire() {
        return signal.isValid();
    }

    @Override
    protected boolean equalsCondition(Fireable fireable){
        boolean equal = false;
        if (fireable instanceof SignalValid){
            SignalValid sv = (SignalValid)fireable;
            equal = this.signal.equals(sv.signal);
        }
        return equal;
    }
    

}
