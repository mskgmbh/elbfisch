/**
 * PROJECT   : Elbfisch - java process automation controller (jPac) 
 * MODULE    : IoSignalImpl.java (versatile input output subsystem)
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

import java.net.URI;
import org.jpac.InconsistencyException;
import org.jpac.NumberOutOfRangeException;
import org.jpac.SignalAccessException;
import org.jpac.WrongUseException;
import org.jpac.plc.Address;
import org.jpac.plc.AddressException;
import org.jpac.plc.Connection;
import org.jpac.plc.IoDirection;
import org.jpac.plc.WriteRequest;

/**
 *
 * @author berndschuster
 */
public class IoSignalImpl{
    public URI       uri;
    public IOHandler ioHandler;
    public IoSignal  containingSignal;
    public Address   address;

    public IoSignalImpl(IoSignal containingSignal, URI uri) throws InconsistencyException, WrongUseException {
        this.containingSignal = containingSignal;
        this.uri              = uri;
        this.address          = null;
        switch(((IoSignal)containingSignal).getIoDirection()){
            case INPUT:
                getIOHandler().registerInputSignal((IoSignal)containingSignal); 
                break;
            case OUTPUT:
                getIOHandler().registerOutputSignal((IoSignal)containingSignal); 
                break;
            case INOUT:
                getIOHandler().registerInputSignal((IoSignal)containingSignal); 
                getIOHandler().registerOutputSignal((IoSignal)containingSignal); 
                break;
            default:
                throw new WrongUseException("signal '" + uri.getPath().replace("/","") + "'  must be either input or output or both: ");
        }                
    }
    
    /**
     * returns the IOHandler, this signal is assigned to
     * @return 
     * @throws org.jpac.InconsistencyException 
     */
    protected IOHandler getIOHandler() throws InconsistencyException{
        if (ioHandler == null){
            try {
                ioHandler = IOHandlerFactory.getHandlerFor(getAddress(), getUri());
            } catch (ClassNotFoundException ex) {
                throw new InconsistencyException("no IOHandler found for " + uri);
            }            
        }
        return ioHandler;
    }
    
    public void setUri(URI uri){
        this.uri = uri;
    }
    
    public URI getUri() {
        return this.uri;
    }
    
    public IoSignal getContainingSignal(){
        return this.containingSignal;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Address getAddress() {
        return this.address;
    }
}
