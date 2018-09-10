/**
 * PROJECT   : Elbfisch - java process automation controller (jPac) 
 * MODULE    : LittleEndianDataOutputStream.java (versatile input output subsystem)
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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author berndschuster
 */
public class LittleEndianDataInputStream {
    private DataInputStream in;
    public LittleEndianDataInputStream(InputStream in) {
        this.in = new DataInputStream(in);
    }
    
    public int readShort() throws IOException{
        int value = 0;
        value = 0xff & in.read();        
        value = value | (0xff00 & (in.read() << 8));
        return value;
    }
    
    public int readInt() throws IOException{
        int value = 0;
        value = 0xff & in.read();        
        value = value | (0xff00     & (in.read() << 8));
        value = value | (0xff0000   & (in.read() << 16));
        value = value | (0xff000000 & (in.read() << 24));
        return value;
    }
    
    public int read() throws IOException{
        return in.read();
    }
    
    public int read(byte[] b, int off, int len) throws IOException{
        return in.read(b, off, len);
    }    
    
    public int read(byte[] b) throws IOException{
        return in.read(b);
    }    

    public void close() throws IOException{
        in.close();
    }
    
    public int available() throws IOException{
        return in.available();
    }

    public long skip(int n) throws IOException{
        return in.skip(n);
    }

}
