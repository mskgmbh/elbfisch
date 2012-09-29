/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : StringProperty.java
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
 *
 * @author berndschuster
 */
public class StringProperty extends Property{
    public StringProperty(Object owningObject, String key, String defaultValue, String comment, boolean classProperty) throws ConfigurationException{  
        super(owningObject, key, defaultValue, comment, classProperty);
    }  

    public StringProperty(Object owningObject, String key, String defaultValue, String comment) throws ConfigurationException{  
        super(owningObject, key, defaultValue, comment, false);
    }  

    public StringProperty(Object owningObject, String key, String defaultValue, boolean classProperty) throws ConfigurationException{  
        super(owningObject, key, defaultValue, null, classProperty);
    }  

    public StringProperty(Object owningObject, String key, String defaultValue) throws ConfigurationException{  
        super(owningObject, key, defaultValue, null, false);
    }  
    
    public StringProperty(String key) throws ConfigurationException{  
        super(key);
    }      
    
    public String get() throws ConfigurationException{
        return Configuration.getInstance().getString(key);
    }
    
    public void set(String value) throws ConfigurationException{
        Configuration.getInstance().setProperty(key, value);
    }
}
