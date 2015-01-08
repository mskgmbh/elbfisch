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
import org.jpac.AbstractModule;
import org.jpac.Generic;
import org.jpac.IndexOutOfRangeException;
import org.jpac.SignalAccessException;
import org.jpac.SignalAlreadyExistsException;
import org.jpac.SignalInvalidException;

/**
 *
 * @author berndschuster
 */
public class IoGeneric<ValueImpl> extends Generic<ValueImpl> implements IoSignal{
    static  Logger  Log = Logger.getLogger("jpac.Signal");      

    private org.jpac.plc.Address    address;
    private Data                    data;
    private WriteRequest            writeRequest;
    private IoDirection             ioDirection;
    private Connection              connection;
    private boolean                 changedByCheck;
    private boolean                 inCheck;
    private boolean                 toBePutOut;   
    private LobRxTx                 lobRxTx;
    
    public IoGeneric(AbstractModule containingModule, String identifier, Data data, Address address, IoDirection ioDirection) throws SignalAlreadyExistsException, IndexOutOfRangeException{
        super(containingModule, identifier);
        this.data        = data;
        this.address     = address;
        this.ioDirection = ioDirection;
        this.lobRxTx     = new LobRxTx(null, address, 0, data);
    }
    
    @Override
    public void check() throws SignalAccessException, AddressException {
        try{
            inCheck = true;
            set((ValueImpl)lobRxTx);
        }
        finally{
            inCheck = false;
        }
    }
    
    @Override
    public void set(ValueImpl value) throws SignalAccessException{
        super.set(value);
        changedByCheck = isChanged() && inCheck;
    }
    
    @Override
    public void propagate() throws SignalInvalidException{
        //this signal has been altered inside the Elbfisch application  (not by the external device).
        //Mark it as to be put out to the external device
        if (hasChanged() && signalValid && !changedByCheck){
            toBePutOut = true;
        }
        changedByCheck = false;
        super.propagate();
    }
    
    @Override
    public boolean isToBePutOut(){
        return toBePutOut;
    }
    
    @Override
    public void resetToBePutOut(){
        toBePutOut = false;
    }
    
    @Override
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
    
    /**
     * @return the address of the signal
     */
    public Address getAddress(){
        return this.address;
    }    
    
    @Override
    public String toString(){
       return "PlcGeneric<" + lobRxTx.getClass().getSimpleName() + "> <-> " + lobRxTx.getAddress().toString(); 
    }    
}
