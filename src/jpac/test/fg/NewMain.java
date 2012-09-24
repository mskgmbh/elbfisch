/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : NewMain.java
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
package jpac.test.fg;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jpac.AbstractModule;
import org.reflections.Reflections;

/**
 *
 * @author berndschuster
 */
public class NewMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            FunctionGenerator fg = new FunctionGenerator("FunctionGenerator");
//            Reflections reflections = new Reflections("my.project.prefix");
//            Set<Class<? extends AbstractModule>> s = reflections.getSubTypesOf(org.jpac.AbstractModule.class);
//            for (Class clazz: s){
//                System.out.println(clazz.getName());
//            }
//
//            ServiceLoader<AbstractModule> moduleLoader = ServiceLoader.load(org.jpac.AbstractModule.class);
//	 for (AbstractModule am : moduleLoader) {
//		 System.out.println(am.getName());
//	 }
            fg.start();

//            Method[] methods = FunctionGenerator.class.getMethods();
//            for(Method m: methods){
//                System.out.println(m.getReturnType() + " " + m.getName());
//            }
//            System.out.println(">>>>Fields>>>>>");
//            Field[] fields = FunctionGenerator.class.getDeclaredFields();
//            for(Field f: fields){
//                System.out.println(f.getGenericType() + " : " + org.jpac.AbstractModule.class.isAssignableFrom((Class)(f.getGenericType())) + " " + f.getName());
//            }
        } catch (Exception ex) {
            Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
