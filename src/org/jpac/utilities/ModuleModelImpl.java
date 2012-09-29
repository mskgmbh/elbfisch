/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : ModuleModelImpl.java
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

package org.jpac.utilities;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 *
 * @author berndschuster
 */
public class ModuleModelImpl implements ModuleModel{
    private String                     name;
    private Class                      thisModule;
    private ArrayList<ModuleModelImpl> containedModules;
    private ArrayList<Field>           ownedSignals;
    
    public ModuleModelImpl(Class module){
        thisModule       = module;
        name             = module.getSimpleName(); 
        containedModules = new ArrayList<ModuleModelImpl>(10);
        ownedSignals     = new ArrayList<Field>(10);    
    }

    public void setName(String name) {
        this.name = name;
    }

    public ModuleModel addContainedModule(Field module) {
        ModuleModelImpl modmodImp = new ModuleModelImpl((Class)module.getGenericType());
        System.out.println("adding module " + module);
        containedModules.add(modmodImp);
        return modmodImp;
    }

    public void addSignal(Field signal) {
        System.out.println("   " + name + " adding signal " + signal);
        ownedSignals.add(signal);
    }    
}
