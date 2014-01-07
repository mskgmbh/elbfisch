/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : IoGeneric.java
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

package org.jpac.plc;

import org.apache.log4j.Logger;
import org.jpac.Generic;
import org.jpac.NumberOutOfRangeException;
import org.jpac.SignalAccessException;
import org.jpac.SignalAlreadyExistsException;

/**
 *
 * @author berndschuster
 */
public class IoGeneric{
    static  Logger  Log = Logger.getLogger("com.msk.atlas.IoHandler");      
    
    private Generic      signal;
    private LobRxTx      lobRxTx;
    
    public IoGeneric(Generic signal, LobRxTx lobRxTx) throws SignalAlreadyExistsException{
        this.signal  = signal;
        this.lobRxTx = lobRxTx;
    }
    
    public void check() throws SignalAccessException, AddressException, NumberOutOfRangeException{
        signal.set(lobRxTx);
        if (signal.isChanged()){
           if (Log.isDebugEnabled()) Log.debug(this + " changed");
        }
    }
    
    public WriteRequest getWriteRequest(Connection connection){
        WriteRequest writeRequest = null;
        try{
            writeRequest = lobRxTx.getWriteRequest();
        }   
        catch(Exception exc){
            Log.error("Error: ", exc);
        }
        return writeRequest;  
    }
    
    
    @Override
    public String toString(){
       return "PlcGeneric<" + lobRxTx.getClass().getSimpleName() + "> <-> " + lobRxTx.getAddress().toString(); 
    }    
}
