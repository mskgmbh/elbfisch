/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : IoDecimal.java
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
import org.jpac.Decimal;
import org.jpac.NumberOutOfRangeException;
import org.jpac.SignalAccessException;
import org.jpac.SignalAlreadyExistsException;
import org.jpac.SignalInvalidException;

/**
 *
 * @author berndschuster
 */
public class IoDecimal extends Decimal implements IoSignal{
    static  Logger           Log         = LoggerFactory.getLogger("jpac.Signal");      
    private final static int DEFAULTSIZE = 6;
    
    private   Address      address;
    private   Data         data;
    private   Data         intData;
    private   WriteRequest writeRequest;
    private   IoDirection  ioDirection;
    private   Connection   connection;
    protected boolean      changedByCheck;
    protected boolean      inCheck;
    protected boolean      outCheck;
    protected boolean      toBePutOut;    

    public IoDecimal(AbstractModule containingModule, String name, Data data, Address address, IoDirection ioDirection) throws SignalAlreadyExistsException{
        super(containingModule, name);
        int minVal        = 0;
        int maxVal        = 0;
        this.data         = data;
        this.address      = address;
        this.ioDirection  = ioDirection;
        int size          = address == null ? DEFAULTSIZE : address.getSize();
    }
    
    /**
     * used to checkIn, if this signal has been changed by the plc. If so, the signal change is automatically
     * propagated to all connected signals
     * @throws SignalAccessException
     * @throws AddressException 
     * @throws org.jpac.NumberOutOfRangeException 
     */    
    @Override
    public void checkIn() throws SignalAccessException, AddressException, NumberOutOfRangeException {
        throw new UnsupportedOperationException("not implemented yet");
    }
    
   /**
     * used to check, if this signal is changed and therefore to be put out to the plc.
     * @throws SignalAccessException
     * @throws AddressException 
     * @throws org.jpac.NumberOutOfRangeException 
     */    
    @Override
    public void checkOut() throws SignalAccessException, AddressException, NumberOutOfRangeException {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public void set(double value) throws SignalAccessException, NumberOutOfRangeException{
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
     * returns a write request suitable for transmitting this signal to the plc
     * @param connection
     * @return 
     */
    @Override
    public WriteRequest getWriteRequest(Connection connection){
       throw new UnsupportedOperationException("not implemented yet");
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
        
    @Override
    public String toString(){
       String ts = null;
       if (address != null){
           ts = super.toString() + (ioDirection == IoDirection.INPUT ? " <- " : " -> ") + address.toString(); 
       }
       else{
           ts = super.toString(); 
       }
       return ts;
    }    
}
