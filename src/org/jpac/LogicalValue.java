/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : LogicalValue.java
 * VERSION   : $Revision: 1.2 $
 * DATE      : $Date: 2012/03/30 13:54:35 $
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
 * LOG       : $Log: LogicalValue.java,v $
 * LOG       : Revision 1.2  2012/03/30 13:54:35  schuster
 * LOG       : introducing remote signal handling
 * LOG       :
 */

package org.jpac;

import java.io.Serializable;

/**
 *
 * @author berndschuster
 */
public class LogicalValue implements Value, Cloneable, Serializable{
    boolean value = false;

    public void set(boolean value){
       this.value = value;  
    }
    
    public boolean get(){
        return this.value;
    }
    
    public boolean is(boolean state){
        return this.value == state;
    }
    
    public void copy(Value aValue) {
        this.value = ((LogicalValue)aValue).get();
    }

    public boolean equals(Value aValue) {
        return aValue instanceof LogicalValue && this.value == ((LogicalValue)aValue).get();
    }
    
    @Override
    public String toString(){
        return Boolean.toString(this.value);
    }

    @Override
    public Value clone() throws CloneNotSupportedException {
        return (LogicalValue) super.clone();
    }
}
