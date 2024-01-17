/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : TreeItem.java
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

package org.jpac.opc;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Objects;
import org.jpac.AbstractModule;
import org.jpac.Signal;

/**
 *
 * @author berndschuster
 */
public class TreeItem{
    private String                partialIdentifier;
    private ArrayList<TreeItem> subNodes;
    private Signal                signal;

    public TreeItem(String partialIdentifier, Signal signal){
        this.partialIdentifier = new String(partialIdentifier);
        this.signal            = signal;
    }

    public TreeItem(String partialIdentifier){
        this(partialIdentifier, null);
    }

    public ArrayList<TreeItem> getSubNodes(){
        if (subNodes == null){
            subNodes = new ArrayList<TreeItem>();
        }
        return subNodes;
    }

    @Override
    public boolean equals(Object signalNode){
        return this.partialIdentifier.equals(((TreeItem)signalNode).partialIdentifier);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.partialIdentifier);
        return hash;
    }


    public Signal getSignal(){
        return this.signal;
    }

    public Field getSignalField(){
        Field field = null;
        if (signal != null){
            field = this.signal.getContainingModule().getField(signal);
        }
        return field;
    }

    public AbstractModule getModule(){
        AbstractModule module = null;
        if (signal != null){
            module = this.signal.getContainingModule();
        }
        return module;
    }

    public String getPartialIdentifier(){
        return this.partialIdentifier;
    }
    
    public void setSignal(Signal signal){
        this.signal = signal;
    }

    @Override
    public String toString(){
        return getClass().getSimpleName() + "(" + partialIdentifier + ", " + getSignal() + ")";
    }
}

