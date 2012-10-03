/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : Assignable.java
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
 * defines the basic properties of an assignable item, used in conjunction with plugs
 * @author berndschuster
 */
public interface Assignable {
        /** @return the identifier*/
        public String getIdentifier();
        /** @return the containing module*/
        public AbstractModule getContainingModule();
        /** @return true, if the Assignable can be assigned to item*/
        public boolean isCompatible(Assignable item);
}
