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
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.stream.Collectors;

import org.jpac.InconsistencyException;
import org.jpac.IoDirection;
import org.jpac.Signal;
import org.jpac.SignalAccessException;
import org.jpac.Value;
import org.jpac.WrongUseException;

/**
 *
 * @author berndschuster
 */
public class IoSignalImpl{
    private URI              		uri;
    private Object           		errorCode;
    private IOHandler        		ioHandler;
    private RemoteSignalInfo 		remoteSignalInfo;
    private Signal           		containingSignal;    
    private boolean        			changedByCheckIn;
    private boolean          		toBePutout;
    private Map<String, String>     parameters; 
    
    public IoSignalImpl(Signal containingSignal, URI uri) throws InconsistencyException, WrongUseException {
        this.setContainingSignal(containingSignal);
        this.setUri(uri);
        this.parameters       = seizeParameters(uri.getQuery());
        this.remoteSignalInfo = null;
        switch(((Signal)containingSignal).getIoDirection()){
            case INPUT:
                getIOHandler().registerInputSignal(containingSignal); 
                break;
            case OUTPUT:
                getIOHandler().registerOutputSignal(containingSignal); 
                break;
            case INOUT:
            case UNDEFINED:
                throw new WrongUseException("signal '" + uri.getPath().replace("/","") + "'  must be either input or output");
        }                
    }
    
	public Map<String, String> seizeParameters(String query) {
	    if (query == null || query.equals("")) {
	        return Collections.emptyMap();
	    }
	    Map<String, String> params = Arrays.stream(query.split("&")).map(this::seizeParameter).collect(Collectors.toMap(SimpleImmutableEntry::getKey, SimpleImmutableEntry::getValue));
	    return params;
	}

	public SimpleImmutableEntry<String, String> seizeParameter(String it) {
	    final int idx = it.indexOf("=");
	    final String key = idx > 0 ? it.substring(0, idx) : it;
	    final String value = idx > 0 && it.length() > idx + 1 ? it.substring(idx + 1) : null;
	    return new SimpleImmutableEntry<>(key.trim(), value != null ? value.trim() : "");
	}	

	/**
     * returns the IOHandler, this signal is assigned to
     * @return 
     * @throws org.jpac.InconsistencyException 
     */
    protected IOHandler getIOHandler() throws InconsistencyException{
        if (getIoHandler() == null){
        	setIoHandler(IOHandlerFactory.getHandlerFor(getUri(), ((Signal)containingSignal).getIoDirection()));
        }
        return getIoHandler();
    }
    
    protected Signal getContainingSignal(){
        return this.containingSignal;
    }

    protected void checkIn() throws SignalAccessException {
        //subscribed value may have changed on remote side. Take over a copy of it (copy is done by setValue() internally).
    	Value value;
    	RemoteSignalInfo rsi = ((IoSignal)containingSignal).getRemoteSignalInfo();
    	synchronized (rsi) {
        	value = rsi.getValue();			
		}
    	if (value.isValid()) {
            containingSignal.setValue(value);    		
    	} else {
    		containingSignal.invalidate();
    	}
        changedByCheckIn = containingSignal.hasChanged();
    }

    protected void checkOut() throws SignalAccessException {
    	//transfer locally changed signal to remote side
    	((IoSignal)containingSignal).getRemoteSignalInfo().getValue().copy(containingSignal.getValue());
    }
    
    protected void markAsToBePutOut(boolean signalChangedLocally) {
 		//avoid writing back a signal to an external device which caused the change itself 
    	if (signalChangedLocally) {
    		setToBePutOut(true);
    	}
    }

    protected void setToBePutOut(boolean value) {
    	this.toBePutout = value;
    };

    protected boolean isToBePutOut() {
    	return this.toBePutout;
    };
    protected void resetToBePutOut() {
    	this.toBePutout = false;
    }
    
    protected boolean isChangedByCheckIn() {
    	return this.changedByCheckIn;
    }

    protected void resetChangedByCheckIn() {
    	this.changedByCheckIn  = false;
    }
    
    protected Map<String,String> getParameters(){
    	return parameters;
    }
    
    protected URI getUri() {
		return uri;
	}

	protected void setUri(URI uri) {
		this.uri = uri;
	}

	protected Object getErrorCode() {
		return errorCode;
	}

	protected void setErrorCode(Object errorCode) {
		this.errorCode = errorCode;
	}

	protected IOHandler getIoHandler() {
		return ioHandler;
	}

	protected void setIoHandler(IOHandler ioHandler) {
		this.ioHandler = ioHandler;
	}

	protected RemoteSignalInfo getRemoteSignalInfo() {
		return remoteSignalInfo;
	}

	protected void setRemoteSignalInfo(RemoteSignalInfo remoteSignalInfo) {
		this.remoteSignalInfo = remoteSignalInfo;
	}

	protected void setContainingSignal(Signal containingSignal) {
		this.containingSignal = containingSignal;
	}
}
