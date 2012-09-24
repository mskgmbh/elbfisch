/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : ModuleExplorer.java
 * VERSION   : $Revision$
 * DATE      : $Date$
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
 * LOG       : $Log$
 */

package org.jpac.utilities;

import java.lang.reflect.Field;
import jpac.test.fg.FunctionGenerator;

/**
 *
 * @author berndschuster
 */
public class ModuleExplorer {
    private Class          module;
    private ModuleModel    model;
    
    public ModuleExplorer(Class module, ModuleModel model){
        this.module = module;
        this.model  = model;
    }
    
    public void explore(){
//            Method[] methods = module.getMethods();
//            for(Method m: methods){
//                System.out.println(m.getReturnType() + " " + m.getName());
//            }
            Field[] fields = module.getDeclaredFields();
            for(Field f: fields){
                //f.get(f); Objekt kann ermittelt werden
                if (isModule((Class)f.getGenericType())){
                    (new ModuleExplorer((Class)f.getGenericType(),model.addContainedModule(f))).explore();
                } else if (isSignal((Class)f.getGenericType())){
                    model.addSignal(f);
                }
            }
    }
    
    public static void main(String[] args) {
        try {
//            FunctionGenerator fg = new FunctionGenerator("FunctionGenerator");            
            ModuleModel mainModel = new ModuleModelImpl(FunctionGenerator.class);
            ModuleExplorer modex = new ModuleExplorer(FunctionGenerator.class, mainModel);
            modex.explore();
            System.exit(0);//kill automation controller
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    protected boolean isModule(Class clazz){
        return org.jpac.AbstractModule.class.isAssignableFrom(clazz);
    }
    
    protected boolean isSignal(Class clazz){
        return org.jpac.Signal.class.isAssignableFrom(clazz);
    }
    
    
}
