/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : Property.java
 * VERSION   : $Revision: 1.3 $
 * DATE      : $Date: 2012/06/27 15:25:34 $
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
 * LOG       : $Log: Property.java,v $
 * LOG       : Revision 1.3  2012/06/27 15:25:34  schuster
 * LOG       : implemented Property.toString()
 * LOG       :
 * LOG       : Revision 1.2  2012/06/27 15:07:44  schuster
 * LOG       : access of existing properties by foreign modules implemented
 * LOG       :
 * LOG       : Revision 1.1  2012/03/05 07:23:09  schuster
 * LOG       : introducing Properties
 * LOG       :
 */

package org.jpac.configuration;

import org.apache.commons.configuration.ConfigurationException;
import org.jpac.AbstractModule;

/**
 *
 * @author berndschuster
 */
public abstract class Property {
    String  key;
    boolean classProperty;
    public Property(Object owningObject, String key, Object defaultValue, String comment, boolean classProperty) throws ConfigurationException{
        assertKey(key);
        this.classProperty = classProperty;
        Configuration configuration = Configuration.getInstance();
        //compute key
        if (classProperty){
            //if it is a class property, prefix its key with the canonical name of the class
            this.key = owningObject.getClass().getCanonicalName().replace(".", "..").concat(".").concat(key);
        }
        else if (owningObject instanceof AbstractModule){
            //if it is a module, prefix its key with the qualified name of the module
            this.key = ((AbstractModule)owningObject).getQualifiedName().concat(".").concat(key);
        }
        else{
            //otherwise, take the given key itself
            this.key = key;
        }
        //add property, if not already part of the configuration
        if (!configuration.containsKey(this.key)){
            configuration.addProperty(this.key, defaultValue == null ? "" : defaultValue);
        }
        if (comment != null){
            String commentKey = this.key + "[@comment]";
            if (configuration.containsKey(commentKey)){
                //if the comment already exists ...
                if (!configuration.getString(commentKey).equals(comment)){
                    //update it, if applicable
                    configuration.setProperty(commentKey, comment);
                }
            }
            else{
                //add it to the property
                configuration.addProperty(this.key + "[@comment]", comment);            
            }
            //mark this comment as been touched during actual session
            configuration.getTouchedProperties().add(commentKey);
        }
        //mark this property as been touched during actual session
        configuration.getTouchedProperties().add(this.key);
    }
    
    public Property(String key) throws ConfigurationException{
        Configuration configuration = Configuration.getInstance();
        this.key = key;
        if (!configuration.containsKey(this.key)){
            throw new ConfigurationException("property " + key + " does not exist!");
        }
    }
    
    private void assertKey(String key) throws ConfigurationException{
        if (key.contains(".")){
            throw new ConfigurationException("a period ('.') is not allowed as part of the key");
        }
    }
        
    public String getKey(){
        return key;
    }

    public boolean isClassProperty(){
        return classProperty;
    }
    
    @Override
    public String toString(){
        String str = super.toString();
        try{
            str = this.getClass().getSimpleName() + "(" + Configuration.getInstance().getProperty(key) + ")";
        }
        catch(ConfigurationException exc){
            //return super.toString()
        }
        return str;
    }
}
