
/**
 * PROJECT   : jPac PLC communication library
 * MODULE    : Connection.java
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

import java.io.*;
import org.apache.log4j.Logger;
import org.jpac.IndexOutOfRangeException;
import org.jpac.plc.Request.DATATYPE;

/**
 * represents a TCP/IP connection to a plc or to a i/o device.
 *
 */
public abstract class Connection {
    static Logger Log = Logger.getLogger("jpac.plc");

    protected boolean            connected;
    protected String             host;
    protected int                port;
    protected boolean            debug;
    protected boolean            autoConnect;
    
    /**
     * an instance of Connection is created and the connection to given device is initiated immediately
     * @param host ip address of the device (e.g. 192.168.0.1)
     * @param debug switch on/off generation of debug information
     * @throws IOException
     */
    public Connection(String host, int port, boolean debug) throws IOException{
        this(host, port, debug, true);
    }

    /**
     * an instance of Connection is created and the connection to given device is initiated immediately
     * @param host ip address of the device (e.g. 192.168.0.1)
     * @param debug switch on/off generation of debug information
     * @throws IOException
     */
    public Connection(String host, int port, boolean debug, boolean autoConnect) throws IOException{
        this.host        = host;
        this.port        = port;
        this.debug       = debug;
        this.autoConnect = autoConnect;
    }
            
    /**
     *  used to initialize the connection.
     * @throws IOException
     */
    protected abstract void initialize() throws IOException;
      

    /**
     * use to close an existing connection.
     */
    public abstract void close() throws IOException;

    /**
     * use to generate a new read request
     */
    public abstract ReadRequest generateReadRequest(DATATYPE dataType, Address addr, int dataOffset, Data data) throws ValueOutOfRangeException, IndexOutOfRangeException;

    /**
     * use to generate a nrew writeInt request
     */
    public abstract WriteRequest generateWriteRequest(DATATYPE dataType, Address addr, int dataOffset, Data data) throws ValueOutOfRangeException, IndexOutOfRangeException;

    /**
     * use to generate a new receive transaction
     */
    public abstract ReceiveTransaction generateReceiveTransaction();

    /**
     * use to generate a new transmit transaction
     */
    public abstract TransmitTransaction generateTransmitTransaction();

    public abstract Data generateDataObject(int size);
    
    public abstract int getMaxTransferLength();

    /**
     * @return the debug
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @param debug the debug to set
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * writes maxlength bytes to the device
     *
     */
    public abstract void write(int maxLength);

    /**
     * writes an array of bytes to the device
     *
     */
    public abstract void write(byte[] stringBytes);

    /**
     * reads one byte from the device
     *
     */
    public abstract int read();

    /**
     * reads an array of bytes from the device
     *
     */
    public abstract void read(byte[] stringBytes);
}
