/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : IoSignedInteger.java (versatile input output subsystem)
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
import java.util.Map;

import org.jpac.AbstractModule;
import org.jpac.InconsistencyException;
import org.jpac.IoDirection;
import org.jpac.NumberOutOfRangeException;
import org.jpac.SignalAccessException;
import org.jpac.SignalAlreadyExistsException;
import org.jpac.SignalInvalidException;
import org.jpac.SignedInteger;
import org.jpac.WrongUseException;

/**
 *
 * @author berndschuster
 */
public class IoSignedInteger extends SignedInteger implements IoSignal {
    private IoSignalImpl ioSignalImpl;
    
    /**
     * constructs a decimal input signal
     * @param containingModule: module, this signal is contained in
     * @param identifier: identifier of the signal
     * @param uri: unified resource identifier of the input signal
     * @param ioDirection: input/output
     * @throws SignalAlreadyExistsException: a signal with this identifier is already registered
     * @throws InconsistencyException: an IOHandler for the given URI cannot be instantiated
     * @throws org.jpac.WrongUseException
     */
    public IoSignedInteger(AbstractModule containingModule, String identifier, URI uri, IoDirection ioDirection) throws SignalAlreadyExistsException, InconsistencyException, WrongUseException{
        super(containingModule, identifier, ioDirection);
        this.ioSignalImpl = new IoSignalImpl(this, uri);
    }    

    @Override
    public void propagate() throws SignalInvalidException{
    	try {
    		//tag signal to be to be put out if it is for output and has been changed in this cycle but 
    		//avoid writing back a signal to an external device which caused the change itself 
    		ioSignalImpl.markAsToBePutOut(hasChanged() && !ioSignalImpl.isChangedByCheckIn() && ioDirection != IoDirection.INPUT);
    		super.propagate();
    	} finally {
    		ioSignalImpl.resetChangedByCheckIn();
    	}
    }

    @Override
    public URI getUri(){
        return this.ioSignalImpl.getUri();
    }
    
    public IOHandler getIOHandler(){
        return this.ioSignalImpl.getIoHandler();
    }

	@Override
	public Object getErrorCode() {
		return ioSignalImpl.getErrorCode();
	}

	@Override
	public void setErrorCode(Object errorCode) {
		ioSignalImpl.setErrorCode(errorCode);
	}

	@Override
	public RemoteSignalInfo getRemoteSignalInfo() {
		return ioSignalImpl.getRemoteSignalInfo();
	}

	@Override
	public void checkIn() throws SignalAccessException, NumberOutOfRangeException {
		ioSignalImpl.checkIn();
	}

	@Override
	public void checkOut() throws SignalAccessException, NumberOutOfRangeException {
		ioSignalImpl.checkOut();
	}

	@Override
	public boolean isToBePutOut() {
		return ioSignalImpl.isToBePutOut();
	}

	@Override
	public void resetToBePutOut() {
		ioSignalImpl.resetToBePutOut();		
	}

	@Override
	public void setRemoteSignalInfo(RemoteSignalInfo remoteSignalInfo) {
		ioSignalImpl.setRemoteSignalInfo(remoteSignalInfo);
	}

	@Override	
	public Map<String, String> getParameters(){
		return ioSignalImpl.getParameters();
	}
	
}
