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
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Iterator;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.jpac.AsynchronousTask;
import org.jpac.CyclicTask;
import org.jpac.ProcessException;
import org.jpac.WrongUseException;

/**
 * represents the central hierarchical configuration of an elbfisch application. All modules store there properties here
 * Will be automatically instantiated on the first instantiation of a property by any module
 * @author berndschuster
 */
public class Configuration extends XMLConfiguration{
    static Logger Log = LoggerFactory.getLogger("jpac.JPac");
    
    private static Configuration      instance;
    private static HashSet<String>    touchedProperties;
    private static boolean            changed;
    private static boolean            invokeSaveOperation;
    private static BooleanProperty    cleanupOnSave;
    private static boolean            loadedSuccessFully;
    private static boolean            backupedSuccessFully;
    private static ConfigurationSaver configurationSaver;
    private static File               backupConfigFile;
    
    private Configuration() throws ConfigurationException, UnsupportedEncodingException{
        super();
        URL configFileUrl = this.getClass().getClassLoader().getResource("org.jpac.Configuration.xml");
        if (configFileUrl != null){
            File configFile  = new File(URLDecoder.decode(configFileUrl.getFile(), "UTF-8"));
            backupConfigFile = new File(configFile.getAbsolutePath() + ".bak");
            setFile(configFile);
            try{
                load();
                loadedSuccessFully = true;
                //configuration loaded successfully
                //store a backup version, if it has been modified since last session
                if (configFile.lastModified() > backupConfigFile.lastModified()){
                    save(backupConfigFile);
                    backupedSuccessFully = true;
                }
            }
            catch(ConfigurationException exc){
                if (!loadedSuccessFully){
                    Log.error("error occured while loading the configuration: ", exc);
                    //configuration file cannot be read
                    //try the backup file
                    if (backupConfigFile.exists()){
                        load(backupConfigFile);//throw exception up, if this operation fails
                        //and store it as the actual configuration
                        save();//throw exception up, if this operation fails
                        Log.error("backup configuration loaded instead and saved as actual configuration.");
                    }
                }
                else if (!backupedSuccessFully){
                    Log.error("error occured while backing up the configuration: ", exc);
                    try{
                        backupConfigFile.delete();//try to remove backup configuration
                        save(backupConfigFile);//and retry backing up the current configuration
                    }
                    catch(Exception ex){
                        Log.error("error occured while backing up the configuration (2nd trial): ", exc);
                    }
                }
            }
        }
        else{
            //no configuration file found in class path. Set default
            setFile(new File("./cfg/org.jpac.Configuration.xml"));
        }
    }
    
    public static Configuration getInstance() throws ConfigurationException{
        if (instance == null){
            try{
                instance = new Configuration();
            }
            catch(Exception exc){
                throw new ConfigurationException(exc);
            }
        }
        return instance;
    }
    
    public HashSet<String> getTouchedProperties(){
        if (touchedProperties == null){
            touchedProperties = new HashSet<String>(10);
        }
        return touchedProperties;
    }
    
    public void cleanUp(){
        for(Iterator<String> keys = getKeys(); keys.hasNext();){
            String key = keys.next();
            if (!getTouchedProperties().contains(key)){
                Log.info("key:" + key);
                clearTree(key);
            }
        }
    }
    
    @Override
    public void save() throws ConfigurationException{        
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
        changed             = true;
        invokeSaveOperation = true;
    }
    
    @Override
    public void clearPropertyDirect(String key){
        super.clearPropertyDirect(key);
        changed = true;
        invokeSaveOperation = true;
    }
    
    @Override
    public void addPropertyDirect(String key, Object value){
        super.addPropertyDirect(key, value);
        changed = true;
        invokeSaveOperation = true;
    }
        
    public ConfigurationSaver getConfigurationSaver(){
        if (configurationSaver == null){
            configurationSaver = new ConfigurationSaver();
        }
        return configurationSaver;
    }
    
    class ConfigurationSaver implements CyclicTask{
        Runner runner = null;
        @Override
        public void run() {
            if (invokeSaveOperation && runner.isFinished()){
                try{runner.start();}catch(WrongUseException exc){/*cannot happen*/}
                invokeSaveOperation = false;
            }
        }

        @Override
        public void prepare() {
            runner = new Runner();
        }

        @Override
        public void stop() {
            if (runner != null){
                try{runner.terminate();}catch(WrongUseException exc){/*cannot happen*/};
            }
        }

        @Override
        public boolean isFinished() {
            return runner.isFinished();
        }
        
        class Runner extends AsynchronousTask{
            @Override
            public void doIt() throws ProcessException {
                if (Log.isDebugEnabled()) Log.debug("saving configuration ...");
                try{
                    save();
                    save(backupConfigFile);//save a copy 
                }
                catch(ConfigurationException exc){
                    Log.error("Error: ", exc);
                }
                if (Log.isDebugEnabled()) Log.debug("... saving of configuration done.");
            }
        }
    }
    
}
