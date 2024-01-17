package org.jpac.vioss;

import java.net.URI;
import java.util.Map;

import org.jpac.AbstractModule;
import org.jpac.Generic;
import org.jpac.InconsistencyException;
import org.jpac.IoDirection;
import org.jpac.NumberOutOfRangeException;
import org.jpac.SignalAccessException;
import org.jpac.SignalAlreadyExistsException;
import org.jpac.SignalInvalidException;
import org.jpac.WrongUseException;

public class IoGeneric<ValueImpl> extends Generic<ValueImpl> implements IoSignal{
	private IoSignalImpl ioSignalImpl;
	
    /**
     * constructs a logical input signal
     * @param containingModule: module, this signal is contained in
     * @param identifier: identifier of the signal
     * @param uri: unified resource identifier of the input signal
     * @param ioDirection: input/output
     * @throws SignalAlreadyExistsException: a signal with this identifier is already registered
     * @throws InconsistencyException: an IOHandler for the given URI cannot be instantiated
     * @throws org.jpac.WrongUseException
     */
    public IoGeneric(AbstractModule containingModule, String identifier, URI uri, IoDirection ioDirection) throws SignalAlreadyExistsException, InconsistencyException, WrongUseException{
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
