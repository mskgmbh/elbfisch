/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : IoCharString.java
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
import org.jpac.CharString;
import org.jpac.SignalAccessException;
import org.jpac.SignalAlreadyExistsException;
import org.jpac.SignalInvalidException;

/**
 *
 * @author berndschuster
 */
public class IoCharString extends CharString implements IoSignal{
    static  Logger  Log = Logger.getLogger("jpac.Signal");      
    
    private Address      address;
    private Data         data;
    private Data         strData;
    private WriteRequest writeRequest;
    private IoDirection  ioDirection;
    private Connection   connection;
    private boolean      changedByCheck;
    private PlcString    plcString;
    private boolean      inCheck;
    private boolean      outCheck;
    private boolean      toBePutOut;
    
    
    public IoCharString(AbstractModule containingModule, String name, Data data, Address address, IoDirection ioDirection) throws SignalAlreadyExistsException, StringLengthException{
        super(containingModule, name);
        this.data        = data;
        this.address     = address;
        this.ioDirection = ioDirection;
        this.plcString   = new PlcString("",address.getSize() - 2);//TODO suitable for S7 strings, but not for others
    }
    
    /**
     * used to checkIn, if this signal has been changed by the plc. If so, the signal change is automatically
     * propagated to all connected signals
     * @throws SignalAccessException
     * @throws AddressException 
     */    
    @Override
    public void checkIn() throws SignalAccessException, AddressException {
        try{
            inCheck = true;
            set(data.getSTRING(address.getByteIndex(),address.getSize()).toString());//TODO checkIn signed integer behaviour
        }
        catch(StringLengthException exc){
            throw new SignalAccessException(exc.getMessage());
        }
        finally{
            inCheck = false;
        }
    }
    
    /**
     * used to check, if this signal has been changed by this jPac instance. If so, the signal change is
     * propagated to the process image (data)
     * @throws SignalAccessException
     * @throws AddressException 
     */
    @Override
    public void checkOut() throws SignalAccessException, AddressException{
        throw new UnsupportedOperationException("to be implemented");
//        try{
//            outCheck = true;
//            if (isToBePutOut()){
//                try{data.setSTRING(address.getByteIndex(), isValid() ? plcString : new PlcString("",address.getSize() - 2));}catch(StringLengthException exc){/*cannot happen*/}
//            }
//        }
//        finally{
//            outCheck = false;
//        }
    }
    
    
    @Override
    public void set(String value) throws SignalAccessException {
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
        boolean errorOccured = false;
        try{
            if (strData == null || this.connection == null || this.connection != connection){
                this.strData      = connection.generateDataObject(address.getSize());
                this.connection   = connection;
                this.writeRequest = null;
            }
            strData.clear();
            if (isValid()){
                plcString.setStringBytes(get());
                strData.setSTRING(0, plcString);
            }
            if (writeRequest == null){
               writeRequest = connection.generateWriteRequest(Request.DATATYPE.BYTE, address, 0, strData);
            }
            else{
               writeRequest.setData(strData);
            }
        }
        catch(Exception exc){
            Log.error("Error: ",exc);
            errorOccured = true;
        }
        return errorOccured ? null : writeRequest;  
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
       return super.toString() + (ioDirection == IoDirection.INPUT ? " <- " : " -> ") + address.toString(); 
    }    
}
