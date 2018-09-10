/**
 * PROJECT   : Elbfisch - versatile input output subsystem (vioss) for the Revolution Pi
 * MODULE    : IoLogical.java (versatile input output subsystem)
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

package org.jpac.vioss.ef;

import java.net.URI;
import org.jpac.AbstractModule;
import org.jpac.InconsistencyException;
import org.jpac.SignalAccessException;
import org.jpac.SignalAlreadyExistsException;
import org.jpac.WrongUseException;
import org.jpac.ef.SignalInfo;
import org.jpac.ef.SignalTransport;
import org.jpac.plc.AddressException;
import org.jpac.plc.IoDirection;
import org.jpac.plc.StringLengthException;

/**
 *
 * @author berndschuster
 */
public class IoCharString extends org.jpac.vioss.IoCharString implements IoSignal{
    protected SignalTransport signalTransport; 
    protected SignalInfo      signalInfo;
    protected String          remoteSignalIdentifier;
    
    public IoCharString(AbstractModule containingModule, String identifier, URI uri, IoDirection ioDirection) throws SignalAlreadyExistsException, InconsistencyException, WrongUseException, StringLengthException{
        super(containingModule, identifier, uri, ioDirection);
        this.remoteSignalIdentifier = uri.getPath().substring(1);//path starts with a slash "/<plcIdentifier>"
        this.signalTransport        = new SignalTransport(this);
        try{this.signalTransport.setValue(getValue().clone());}catch(CloneNotSupportedException exc){/*cannot happen*/};
    }  
    
    @Override
    public void checkIn() throws SignalAccessException, AddressException {
        try{
            inCheck = true;
            SignalTransport st = ((org.jpac.vioss.ef.IOHandler)getIOHandler()).getListOfReceivedSignalTransports().get(signalInfo.getHandle());
            if (st != null){
                //subscribed value changed on remote side. Take it over.
                setValue(st.getValue());
            }
        }
        finally{
            inCheck = false;
        }
    }

    @Override
    public void checkOut() throws SignalAccessException, AddressException{
        try{
            outCheck = true;
            signalTransport.getValue().copy(getValue());
            ((org.jpac.vioss.ef.IOHandler)getIOHandler()).getListOfSignalTransportsToBeTransmitted().add(signalTransport);
        }
        finally{
            outCheck = false;
        }
    }
    
    @Override
    public Object getErrorCode(){
        return null;
    }        

    @Override
    public void setSignalInfo(SignalInfo signalInfo) {
        this.signalInfo = signalInfo;
        this.signalTransport.setHandle(signalInfo.getHandle());
    }

    @Override
    public SignalInfo getSignalInfo() {
        return this.signalInfo;
    }

    @Override
    public void setSignalTransport(SignalTransport signalTransport) {
        this.signalTransport = signalTransport;
    }

    @Override
    public SignalTransport getSignalTransport() {
        return this.signalTransport;
    }
}
