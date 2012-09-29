/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : LongProperty.java
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
public class LongProperty extends Property{
    public LongProperty(Object owningObject, String key, long defaultValue, String comment, boolean classProperty) throws ConfigurationException{  
        super(owningObject, key, defaultValue, comment, classProperty);
    }  

    public LongProperty(Object owningObject, String key, long defaultValue, String comment) throws ConfigurationException{  
        super(owningObject, key, defaultValue, comment, false);
    }  

    public LongProperty(Object owningObject, String key, long defaultValue, boolean classProperty) throws ConfigurationException{  
        super(owningObject, key, defaultValue, null, classProperty);
    }  

    public LongProperty(Object owningObject, String key, long defaultValue) throws ConfigurationException{  
        super(owningObject, key, defaultValue, null, false);
    }  
    
    public LongProperty(String key) throws ConfigurationException{  
        super(key);
    }      
    
    public long get() throws ConfigurationException{
        return Configuration.getInstance().getLong(key);
    }
    
    public void set(long value) throws ConfigurationException{
        Configuration.getInstance().setProperty(key, value);
    }
}
