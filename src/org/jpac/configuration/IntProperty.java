/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : IntProperty.java
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

package org.jpac.configuration;

import org.apache.commons.configuration.ConfigurationException;

/**
 * represents an int property stored inside the hierarchical xml configuration file ./cfg/org.jpac.Configuration.xml
 * if not already present, this property will be automatically stored inside the configuration file on the next save() operation or on exit
 * of the elbfisch application (if property "org.jpac.JPac.StoreConfigOnShutdown = true).
 * If already present, the corresponding entry inside the configuration file is left untouched.
 *
 * @author berndschuster
 */
public class IntProperty extends Property{
    public IntProperty(Object owningObject, String key, int defaultValue, String comment, boolean classProperty) throws ConfigurationException{  
        super(owningObject, key, defaultValue, comment, classProperty);
    }  

    /**
     * constructs an int property
     * @param owningObject the module which instantiated this property. All properties owned by a module are stored under its qualified name inside the
     * configuration file.
     * @param key the key under which this property is stored inside the hierarchical xml configuration file.
     * @param defaultValue the value the property earns by default
     * @param comment a comment describing the purpose of the property
     * @param classProperty if true, this property is stored under the name of the modules class, instead of its qualified name. It than can be used by all
     * instances of this module class
     * @throws ConfigurationException thrown, if an error occurs while accessing the configuration 
     */
    public IntProperty(Object owningObject, String key, int defaultValue, String comment) throws ConfigurationException{  
        super(owningObject, key, defaultValue, comment, false);
    }  

    /**
     * constructs an int property
     * @param owningObject the module which instantiated this property. All properties owned by a module are stored under its qualified name inside the
     * configuration file.
     * @param key the key under which this property is stored inside the hierarchical xml configuration file.
     * @param defaultValue the value the property earns by default
     * @param classProperty if true, this property is stored under the name of the modules class, instead of its qualified name. It than can be used by all
     * instances of this module class
     * @throws ConfigurationException thrown, if an error occurs while accessing the configuration 
     */
    public IntProperty(Object owningObject, String key, int defaultValue, boolean classProperty) throws ConfigurationException{  
        super(owningObject, key, defaultValue, null, classProperty);
    }  

    /**
     * constructs an int property
     * @param owningObject the module which instantiated this property. All properties owned by a module are stored under its qualified name inside the
     * configuration file.
     * @param key the key under which this property is stored inside the hierarchical xml configuration file.
     * @param defaultValue the value the property earns by default
     * instances of this module class
     * @throws ConfigurationException thrown, if an error occurs while accessing the configuration 
     */
    public IntProperty(Object owningObject, String key, int defaultValue) throws ConfigurationException{  
        super(owningObject, key, defaultValue, null, false);
    }  
    
    /**
     * constructs an int property for read only access. The key must be fully qualified to access the property
     * @param key the fully qualified key of the property
     * @throws ConfigurationException thrown, if an error occurs while accessing the configuration 
     */    
    public IntProperty(String key) throws ConfigurationException{  
        super(key);
    }      
    
    /**
     * returns the value of the property
     * @return the value of the property
     * @throws ConfigurationException thrown, if an error occurs while accessing the configuration 
     */
    public int get() throws ConfigurationException{
        touched = true;        
        return Configuration.getInstance().getInt(key);
    }
    
    /**
     * sets the value of the property
     * @param value the value
     * @throws ConfigurationException thrown, if an error occurs while accessing the configuration 
     */
    public void set(int value) throws ConfigurationException{
        touched = true;        
        Configuration.getInstance().setProperty(key, value);
    }
}
