/**
 * PROJECT   : Elbfisch - java process automation controller (jPac) 
 * MODULE    : SignalInfo.java
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

package org.jpac.vioss;

import org.jpac.BasicSignalType;
import org.jpac.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author berndschuster
 */
public class RemoteSignalInfo{
    static public Logger Log = LoggerFactory.getLogger("jpac.vioss.IOHandler");
    
    protected String          identifier;
    protected BasicSignalType type;
    protected Value           value;
    
    public RemoteSignalInfo() {
    	this.identifier = null;
    	this.type       = BasicSignalType.Unknown;
    	this.value      = null;
    }    
    
    public RemoteSignalInfo(String identifier, BasicSignalType type) {
    	this.identifier = identifier;
    	this.type       = type;
    	this.value      = type.newValue();
    }
        
    public BasicSignalType getType(){
        return this.type;
    }
    
    public String getIdentifier(){
        return identifier;
    }
        
    public Value getValue() {
		return value;
	}

	public void setValue(Value value) {
		this.value = value;
	}

	@Override
    public String toString(){
        return super.toString() + ", '" + identifier + "', " + type + ")";
    }    
}
