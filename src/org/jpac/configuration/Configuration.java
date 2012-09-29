/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : Configuration.java
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

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

/**
 *
 * @author berndschuster
 */
public class Configuration extends XMLConfiguration{
    private static Configuration   instance;
    private static HashSet<String> touchedProperties;
    private static boolean         changed;
    private static BooleanProperty cleanupOnSave;
    
    private Configuration() throws ConfigurationException{
        super();
        File configFile = new File("./cfg/org.jpac.Configuration.xml");
        setFile(configFile);
        if (configFile.exists()){
           load();
        }
    }
    
    public static Configuration getInstance() throws ConfigurationException{
        if (instance == null){
            instance = new Configuration();
        }
        return instance;
    }
    
    public HashSet<String> getTouchedProperties(){
        if (touchedProperties == null){
            touchedProperties = new HashSet<String>(10);
        }
        return touchedProperties;
    }
    
    @Override
    public void save() throws ConfigurationException{        
      //check, if the configuration has to be cleaned up
      if(isCleanupOnSave()){
          //reset flag: This flag must be explictly set for every cleanup
          cleanupOnSave.set(false);
          //remove all properties which have not been touched during actual session
          for(Iterator<String> keys = getKeys();keys.hasNext();){
              String key = keys.next();
              if (!getTouchedProperties().contains(key)){
                  clearProperty(key);
              }
          }
      }
      //if the configuration has changed during this session
      if (changed){
          //then save the cleaned up configuration
          super.save();
          changed = false;
      }
    }
    
    @Override
    public void setProperty(String key, Object value){
        super.setProperty(key, value);
        changed = true;
    }
    
    @Override
    public void clearPropertyDirect(String key){
        super.clearPropertyDirect(key);
        changed = true;
    }
    
    @Override
    public void addPropertyDirect(String key, Object value){
        super.addPropertyDirect(key, value);
        changed = true;
    }

    /**
     * @return the cleanupOnSave
     */
    public static boolean isCleanupOnSave() throws ConfigurationException {
        if (cleanupOnSave == null){
            cleanupOnSave = new BooleanProperty(null,"CleanupOnSave",false,"removes unused properties on next save(). !!! Will be automatically reset !!!");        
        }
        return cleanupOnSave.get();
    }

    /**
     * @param aCleanupOnSave the cleanupOnSave to set
     */
    public static void setCleanupOnSave(boolean cleanUp) throws ConfigurationException {
        cleanupOnSave.set(cleanUp);
    }
    
}
