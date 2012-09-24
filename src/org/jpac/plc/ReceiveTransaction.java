/**
 * PROJECT   : jPac PLC communication library
 * MODULE    : ReceiveTransaction.java
 * VERSION   : $Revision: 1.3 $
 * DATE      : $Date: 2012/06/18 14:52:02 $
 * PURPOSE   : used to transact receiving data items to the plc
 * AUTHOR    : Andreas Ulbrich, MSK Gesellschaft fuer Automatisierung mbH, Schenefeld
 * REMARKS   : -
 * CHANGES   : CH#n <Kuerzel> <datum> <Beschreibung>
 *
 * This file is part of the jPac PLC communication library.
 * The jPac PLC communication library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The jPac PLC communication library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the jPac PLC communication library.  If not, see <http://www.gnu.org/licenses/>.
 *
 * LOG       : $Log: ReceiveTransaction.java,v $
 * LOG       : Revision 1.3  2012/06/18 14:52:02  schuster
 * LOG       : recommit because of a syntax error
 * LOG       :
 * LOG       : Revision 1.2  2012/06/18 14:46:31  ulbrich
 * LOG       : setData() added for ReceiveTransaction
 * LOG       :
 */

package org.jpac.plc;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

/**
 *
 * @author Ulbrich
 */
public abstract class ReceiveTransaction extends Transaction {
    protected Data receiveData;

    public ReceiveTransaction(Connection conn) {
        super(conn);
        requestQueue = new ArrayBlockingQueue<ReadRequest>(REQUESTQUEUECAPACITY);
    }
    
    public abstract void transact() throws IOException;

    public Data getData() {
        return receiveData;
    }

    public void setData(Data data) {
       receiveData = data;
    }    
}
    
