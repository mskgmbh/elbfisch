/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : Observable.java
 * VERSION   : -
 * DATE      : -
 * PURPOSE   : type safe subsitute for java.util.Observable
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

import java.util.HashSet;

public abstract class Observable {
    protected HashSet<Observer<Observable>> listOfObservers = new HashSet<>();
    protected boolean                       changed         = false;

    @SuppressWarnings("unchecked")
    public void addObserver(Observer<?> o){
        listOfObservers.add((Observer<Observable>)o);
    }

    protected void 	clearChanged(){
        changed = false;
    }

    public int countObservers(){
        return listOfObservers.size();
    }

    public void deleteObserver(Observer<?> o){
        listOfObservers.remove(o);
    }

    public void deleteObservers(){
        listOfObservers.clear();
    }

    public boolean hasChanged(){
        return changed;    
    }

    public void notifyObservers(){
        if (hasChanged()){
            listOfObservers.forEach(o -> o.update(this));
            clearChanged();
        }
    }

    public void notifyObservers(Object arg){
        if (hasChanged()){
            listOfObservers.forEach(o -> o.update(this, arg));
            clearChanged();
        }
    }
       
    public void setChanged(){
        changed = true;
    }        
}

