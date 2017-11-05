/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : SnapshotModule.java
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

import java.util.ArrayList;
import java.util.List;
import org.jpac.AbstractModule;
import org.jpac.Signal;
import org.jpac.SignalRegistry;

/**
 *
 * @author berndschuster
 */
public class SnapshotModule {
    public String qualifiedName;
    public List<SnapshotSignal> signals;
    
    public SnapshotModule(AbstractModule module){
        this.qualifiedName = module.getQualifiedName();
        this.signals       = new ArrayList<SnapshotSignal>();
        
        SignalRegistry.getInstance().getSignals().values().stream()
            .filter(s -> s.getContainingModule().equals(module))
            .sorted((s1,s2) -> s1.getIdentifier().compareTo(s1.getIdentifier()))
            .forEach(s -> {if (!isUnusedStackTrace(s)) signals.add(new SnapshotSignal(s));});
    }
    private boolean isUnusedStackTrace(Signal signal){
        return signal.getIdentifier().startsWith(":StackTrace[") && !signal.isValid();
    }
}
