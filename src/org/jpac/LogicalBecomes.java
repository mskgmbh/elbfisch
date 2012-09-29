/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : LogicalBecomes.java
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

public class LogicalBecomes extends LogicalEvent{
    private boolean state;
    
    public LogicalBecomes(Logical logical, boolean state){
        super(logical);
        this.state = state;
    }
    
    public void setState(boolean state){
        this.state = state;
    }

    @Override
    public boolean fire() throws ProcessException {
        return logical.isToggledTo(state);
    }

    @Override
    public String toString(){
        return super.toString() + ".becomes(" + state + ")";
    }
    
    @Override
    protected boolean equalsCondition(Fireable fireable){
        boolean equal = false;
        if (fireable instanceof LogicalBecomes){
            LogicalBecomes bt = (LogicalBecomes)fireable;
            equal = this.logical.equals(bt.logical);
        }
        return equal;
    }
}
