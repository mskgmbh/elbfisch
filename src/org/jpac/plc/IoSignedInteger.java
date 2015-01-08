/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : IoSignedInteger.java
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
import org.jpac.JPac;
import org.jpac.NumberOutOfRangeException;
import org.jpac.SignalAccessException;
import org.jpac.SignalAlreadyExistsException;
import org.jpac.SignalInvalidException;
import org.jpac.SignedInteger;

/**
 *
 * @author berndschuster
 */
public class IoSignedInteger extends SignedInteger implements IoSignal{
    static  Logger  Log = Logger.getLogger("jpac.Signal");      
    
    private Address      address;
    private Data         data;
    private Data         intData;
    private WriteRequest writeRequest;
    private IoDirection  ioDirection;
    private Connection   connection;
    private boolean      changedByCheck;
    private boolean      inCheck;
    private boolean      toBePutOut;    
    
    public IoSignedInteger(AbstractModule containingModule, String name, Data data, Address address, IoDirection ioDirection) throws SignalAlreadyExistsException{
        super(containingModule, name);
        int minVal       = 0;
        int maxVal       = 0;
        this.data        = data;
        this.address     = address;
        this.ioDirection = ioDirection;
        switch(address.getSize()){
            case 1:
                minVal = Byte.MIN_VALUE;
                maxVal = Byte.MAX_VALUE;
                break;
            case 2:
                minVal = Short.MIN_VALUE;
                maxVal = Short.MAX_VALUE;
                break;
            case 4:
                minVal = Integer.MIN_VALUE;
                maxVal = Integer.MAX_VALUE;
                break;
            
        }
        minValue     = minVal;
        maxValue     = maxVal;
        rangeChecked = true;
    }
    
    /**
     * used to check, if this signal has been changed by the plc. If so, the signal change is automatically
     * propagated to all connected signals
     * @throws SignalAccessException
     * @throws AddressException 
     */    
    @Override
    public void check() throws SignalAccessException, AddressException {
        try{
            inCheck = true;
            switch(address.getSize()){
                case 1:
                    set(data.getBYTE(address.getByteIndex()));//TODO check signed integer behaviour
                    break;
                case 2:
                    set(data.getINT(address.getByteIndex()));//TODO check signed integer behaviour   
                    break;
                case 4:
                    set(data.getDINT(address.getByteIndex()));        
                    break;
            }
        }
        catch(NumberOutOfRangeException exc){
            throw new SignalAccessException(exc.getMessage());
        }
        finally{
            inCheck = false;
        }
    }
    
    @Override
    public void set(int value) throws SignalAccessException, NumberOutOfRangeException{
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

    /**
     * returns a write request suitable for transmitting this signal to the plc
     * @param connection
     * @return 
     */
    public WriteRequest getWriteRequest(Connection connection){
        boolean errorOccured = false;
        try{
            if (intData == null || this.connection == null || this.connection != connection){
                this.intData      = connection.generateDataObject(4);
                this.connection   = connection;
                this.writeRequest = null;
            }
            switch(address.getSize()){
                case 1:
                    intData.setBYTE(0, isValid() ? get() : 0);
                    break;
                case 2:
                    intData.setINT(0, isValid() ? get() : 0);
                    break;
                case 4:
                    intData.setDINT(0, isValid() ? get() : 0);                    
                    break;
            }
            if (writeRequest == null){
               writeRequest = connection.generateWriteRequest(Request.DATATYPE.BYTE, address, 0, intData);
            }
            else{
               writeRequest.setData(intData);
            }
        }
        catch(Exception exc){
            Log.error("Error: ",exc);
            errorOccured = true;
        }
        return errorOccured ? null : writeRequest;  
    }
    
    /**
     * @return the ioDirection
     */
    public IoDirection getIoDirection() {
        return ioDirection;
    }

    /**
     * @return the address of the signal
     */
    public Address getAddress(){
        return this.address;
    }    
        
    @Override
    public String toString(){
       return super.toString() + (ioDirection == IoDirection.INPUT ? " <- " : " -> ") + address.toString(); 
    }    
}
