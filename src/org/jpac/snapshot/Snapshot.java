/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : Snapshot.java
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
 *
 */

package org.jpac.snapshot;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.log4j.Logger;
import org.jpac.JPac;

/**
 *
 * @author berndschuster
 */
public class Snapshot {
    static Logger Log = Logger.getLogger("jpac.JPac");

    protected String                    created;
    protected String                    runningOnJVM;
    protected SnapshotBuild             build;
    protected SnapshotJars              jars;
    protected ArrayList<SnapshotModule> modules;
    protected String                    filename;
    protected LocalDateTime             dateTime;

    public Snapshot(){
        dateTime     = LocalDateTime.now();
        created      = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS"));        
        runningOnJVM = System.getProperty("java.version");
        build        = new SnapshotBuild();
        modules      = new ArrayList<SnapshotModule>();
        jars         = new SnapshotJars();
        JPac.getInstance().getModules().stream().sorted((m1,m2) -> m1.getName().compareTo(m2.getName())).forEach(m -> modules.add(new SnapshotModule(m)));
    }
    
    public void dump(String path) throws FileNotFoundException, XMLStreamException{
        String timestamp = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS"));        
        
        filename = path + "/snapshot-" + timestamp + ".xml";
        XMLStreamWriter xmlSW    = null;
        try{            
            StaxDriver staxDriver = new StaxDriver();
            XStream xstream = new XStream(new PureJavaReflectionProvider(), staxDriver);
            xstream.useAttributeFor(SnapshotModule.class, "qualifiedName");
            xstream.useAttributeFor(SnapshotSignal.class, "identifier");
            xstream.useAttributeFor(SnapshotSignal.class, "valid");
            xstream.useAttributeFor(SnapshotSignal.class, "value");
            xstream.alias("Elbfisch", Snapshot.class);        
            xstream.alias("build", SnapshotBuild.class);        
            xstream.alias("module", SnapshotModule.class);        
            xstream.alias("signal", SnapshotSignal.class);        
            xstream.alias("jars", SnapshotJars.class);        

            FileOutputStream fos = new FileOutputStream(filename);
            xstream.marshal(this, new PrettyPrintWriter(new OutputStreamWriter(fos)));
        }catch (Exception exc){
            Log.error("Error writing snapshot file " + filename, exc);
        }
        finally{
            if(xmlSW != null){
                xmlSW.close();
            }
        }
    }
    
    public String getFilename(){
        return this.filename;
    }
}
