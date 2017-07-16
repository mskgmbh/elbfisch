/**
 * PROJECT   : jPac PLC communication library
 * MODULE    : Transaction.java
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

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.jpac.ProcessException;

/**
 * represents an i/o transaction
 * @author Ulbrich
 */
public abstract class Transaction {
    protected static Logger      Log = LoggerFactory.getLogger("jpac.plc");
    protected final int          REQUESTQUEUECAPACITY = 100;
    
    private   Connection         connection;
    protected ArrayBlockingQueue requestQueue;

    public Transaction(Connection conn) {
        this.connection = conn;
    }

    public Connection getConnection() {
        return connection;
    }

    public void addRequest(Request request) throws TooManyRequestsException {
        requestQueue.add(request);
    }

    public void removeAllRequests() {
        requestQueue.clear();
    }
    
    /**
     * used to perform the transaction. Blocks, until the plc returns its acknowledgement 
     * @throws IOException 
     */
    public abstract void transact() throws IOException;
    /**
     * used to perform the transaction. performs a jpac await() operation for waitCycle cycles. If
     * the PLC does not answer during this period of time a TransactionTimeoutException is thrown
     * @throws IOException
     * @throws ProcessException
     */
    public abstract void transact(int waitCycles) throws IOException, ProcessException;
}
