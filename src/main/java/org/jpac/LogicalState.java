/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : LogicalState.java
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
 * process event indicating, that a logical signal has a given state
 * @author berndschuster
 */
public class LogicalState extends LogicalEvent{
    private boolean state;
    
    public LogicalState(Logical logical, boolean state){
        super(logical);
        this.state = state;
    }
    
    public void setState(boolean state){
        this.state = state;
    }

    public boolean fire() throws SignalInvalidException{
        return logical.is(state);
    }

    @Override
    public String toString(){
        return super.toString() + ".to(" + state + ")";
    }
    
    @Override
    protected boolean equalsCondition(Fireable fireable){
        boolean equal = false;
        if (fireable instanceof LogicalState){
            LogicalState bt = (LogicalState)fireable;
            equal = this.logical.equals(bt.logical);
        }
        return equal;
    }
    
}
