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

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.jpac.AbstractModule;
import org.jpac.Generic;
import org.jpac.IndexOutOfRangeException;
import org.jpac.SignalAccessException;
import org.jpac.SignalAlreadyExistsException;
import org.jpac.SignalInvalidException;

/**
 *
 * @author berndschuster
 * @param <ValueImpl> class must implement Value interface
 */
abstract public class IoGeneric<ValueImpl> extends Generic<ValueImpl> implements IoSignal{
    static  Logger  Log = LoggerFactory.getLogger("jpac.Signal");      

    private org.jpac.plc.Address    address;
    private Data                    data;
    private WriteRequest            writeRequest;
    private IoDirection             ioDirection;
    private boolean                 changedByCheck;
    private boolean                 inCheck;
    private boolean                 toBePutOut;   
    
    public IoGeneric(AbstractModule containingModule, String identifier, Data data, Address address, IoDirection ioDirection) throws SignalAlreadyExistsException, IndexOutOfRangeException{
        super(containingModule, identifier);
        this.data        = data;
        this.address     = address;
        this.ioDirection = ioDirection;
    }
    
    @Override
    public void set(ValueImpl value) throws SignalAccessException{
        super.set(value);
        changedByCheck = isChanged() && inCheck;
    }
    
    @Override
    public void propagate() throws SignalInvalidException{
        if (hasChanged() && signalValid && !changedByCheck){
            //this signal has been altered inside the Elbfisch application  (not by the external device).
            //Mark it as to be put out to the external device
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
        
    /**
     * @param address address of the signal
     */
    @Override
    public void setAddress(Address address){
        this.address = address;
    }
    
    /**
     * @return the address of the signal
     */
    @Override
    public Address getAddress(){
        return this.address;
    }   
    
    /**
     * 
     * @param ioDirection to be set 
     */
    @Override
    public void setIoDirection(IoDirection ioDirection){
        this.ioDirection = ioDirection;
    }
    
    /**
     *
     * @return ioDirection
     */
    @Override
    public IoDirection getIoDirection(){
        return this.ioDirection;
    }        
    
    @Override
    public String toString(){
       return "IoGeneric<" + value + "> <-> " + getAddress().toString(); 
    }
    
    @Override
    abstract public void checkIn() throws SignalAccessException, AddressException;
    
    @Override
    abstract public void checkOut() throws SignalAccessException, AddressException;
    
    @Override
    abstract public WriteRequest getWriteRequest(Connection connection);
}
