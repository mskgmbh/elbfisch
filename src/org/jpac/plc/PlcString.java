/**
 * PROJECT   : jPac PLC communication library
 * MODULE    : PlcString.java
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

/**
 * represents a plc string<br>
 * a plc string consists of a byte array containing the string<br>
 * and 2 additional maintenance bytes, used to store the maximum and current length of the string<br>
 */
public class PlcString {
    private int    maxLength;
    private int    actualLength;
    private byte[] stringBytes;

    /**
     * @param string the java string to be stored as a plc string
     * @param maxLength the maximum length of the plc string
     * @throws StringLengthException
     */
    public PlcString(String string, int maxLength) throws StringLengthException{
        this.stringBytes = new byte[maxLength];
        if (maxLength > 255 || string.length() > 255 || string.length() > maxLength){
            throw new StringLengthException("maximum string length > 255");
        }
        this.maxLength = maxLength;
        if (string != null){
           setStringBytes(string);
        }
        else{
            this.actualLength = 0;
        }
    }

    /**
     * @param string an array of bytes to be stored as a plc string
     * @param actualLength the actual length of the plc string (the maximum length is deduce from string.length)
     * @throws StringLengthException
     */
    public PlcString(byte[] string, int actualLength) throws StringLengthException{
        this.stringBytes = string;
        if (actualLength > 255 || string.length > 255 || actualLength > string.length){
            throw new StringLengthException("actual string length > 255");
        }
        if (string != null){
            if (string.length > 255){
                throw new StringLengthException("maximum string length > 255");
            }
            this.maxLength    = string.length;
            this.actualLength = actualLength;
        }
        else{
            this.actualLength = 0;
            this.maxLength    = 0;
        }
    }

    /**
     * @return the maximum length of the plc string
     */
    public int getMaxLength() {
        return maxLength;
    }

    /**
     * @return the actual length of the plc string
     */
    public int getActualLength() {
        return actualLength;
    }

    @Override
    public String toString() {
        StringBuffer str = new StringBuffer(256);
        for (int i = 0; i < this.actualLength; i++){
            str.append((char)stringBytes[i]);
        }
        return str.toString();
    }

    public byte[] getStringBytes() {
        return stringBytes;
    }

    /**
     * used to set the string
     * @param string the java string
     * @throws StringLengthException
     */
    public void setStringBytes(String string) throws StringLengthException {
        if (string.length() > this.maxLength){
            throw new StringLengthException("actual length > maximum string length");
        }
        if (string.length() > 255){
            throw new StringLengthException("actual length > 255");
        }
        System.arraycopy(string.getBytes(), 0, this.stringBytes, 0, string.length());
        this.actualLength = string.length();
    }
    /**
     * used to writeInt a plc string to the plc over a pure (non ISO) TCP/IP connection
     * @param conn an open connection to the plc
     * @throws IOException
     */
//    public void write(Connection conn) throws IOException{
//        System.err.println("write() not tested yet");
//        conn.writeInt(maxLength);
//        conn.writeInt(actualLength);
//        conn.writeInt(stringBytes);
////        conn.getOutputStream().write(maxLength);
////        conn.getOutputStream().write(actualLength);
////        conn.getOutputStream().write(stringBytes);
//    }
//
//    /**
//     * used to read a plc string from the plc over a pure (non ISO) TCP/IP connection
//     * @param conn
//     * @throws IOException
//     */
//    public void read(Connection conn) throws IOException{
//        System.err.println("read() not tested yet");
//        maxLength    = conn.readInt();
//        actualLength = conn.readInt();
//        conn.readString(stringBytes);
//    }
}
