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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author berndschuster
 */
public class LittleEndianDataOutputStream {
    private DataOutputStream out;
    public LittleEndianDataOutputStream(OutputStream out) {
        this.out = new DataOutputStream(out);
    }
    
    public void writeShort(int value) throws IOException{
        out.write((byte) (0xff & value));        
        out.write((byte) (0xff & (value >> 8)));
    }
    
    public void writeInt(int value) throws IOException{
        out.write((byte) (0xff & value));   
        out.write((byte) (0xff & (value >> 8)));
        out.write((byte) (0xff & (value >> 16)));
        out.write((byte) (0xff & (value >> 24)));
    }
    
    public void write(byte b) throws IOException{
        out.write(b);
    }
    
    public void write(byte[] b) throws IOException{
        out.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException{
        out.write(b, off, len);
    }

    public void flush() throws IOException{
        out.flush();
    }

    public void close() throws IOException{
        out.close();
    }
}
