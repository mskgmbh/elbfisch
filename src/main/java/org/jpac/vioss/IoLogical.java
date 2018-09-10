/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
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

package org.jpac.vioss;

import java.net.URI;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.jpac.AbstractModule;
import org.jpac.plc.Address;
import org.jpac.InconsistencyException;
import org.jpac.SignalAlreadyExistsException;
import org.jpac.WrongUseException;
import org.jpac.plc.IoDirection;

/**
 *
 * @author berndschuster
 */
abstract public class IoLogical extends org.jpac.plc.IoLogical implements IoSignal{
    static public Logger Log = LoggerFactory.getLogger("jpac.Signal");

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
    public IoLogical(AbstractModule containingModule, String identifier, URI uri, IoDirection ioDirection) throws SignalAlreadyExistsException, InconsistencyException, WrongUseException{
        super(containingModule, identifier, null, null, ioDirection);
        this.ioSignalImpl = new IoSignalImpl(this, uri);
    }  

    @Override
    public URI getUri(){
        return this.ioSignalImpl.getUri();
    }
    
    public IOHandler getIOHandler() throws InconsistencyException{
        return this.ioSignalImpl.getIOHandler();
    }
}
