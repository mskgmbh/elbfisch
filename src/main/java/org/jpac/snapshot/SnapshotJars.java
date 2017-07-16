/**
 * PROJECT   : <???>
 * MODULE    : <???>.java
 * VERSION   : $Revision$
 * DATE      : $Date$
 * PURPOSE   : <???>
 * AUTHOR    : Bernd Schuster, MSK Gesellschaft fuer Automatisierung mbH, Schenefeld
 * REMARKS   : -
 * CHANGES   : CH#n <Kuerzel> <datum> <Beschreibung>
 * LOG       : $Log$
 */

package org.jpac.snapshot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 *
 * @author berndschuster
 */
public class SnapshotJars {
    ArrayList<String> libDir;
    public SnapshotJars(){
        libDir = new ArrayList<>();
        File f = new File("./lib");
        ArrayList<File> jarFiles = new ArrayList<File>(Arrays.asList(f.listFiles()));        
        jarFiles.forEach(file -> libDir.add(file.getName()));
        Collections.sort(libDir);
    }
}
