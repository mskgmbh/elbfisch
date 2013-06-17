/**
 * PROJECT   : jPac PLC communication library
 * MODULE    : Data.java
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

import java.util.ArrayList;

/**
 * used as a data storage for data interchanged with a plc.<br>
 * Implements a byte array and some accessor methods for<br>
 * several plc side datatypes.
 */
public abstract class Data {
    public enum Endianness {LITTLEENDIAN,BIGENDIAN};
    protected final byte[]       bitMask = {(byte)0x01,(byte)0x02,(byte)0x04,(byte)0x08,(byte)0x10,(byte)0x20,(byte)0x40,(byte)0x80};
    protected byte[]             bytes;
    protected byte[]             shadowBytes;
    protected boolean            bytesNew;
    protected ArrayList<Integer> modifiedByteIndices;
    
    Endianness                   endianness;
    
    /**
     * constructor. The endianness of the data item is set to "big endian".
     * @param bytes
     */
    public Data(byte[] bytes){
        this(bytes, Endianness.BIGENDIAN);
    }

    /**
     * constructor
     * @param bytes
     * @param endianness used to set the endianness of this data item. This setting decides if the 
     *                   bytes inside this data item are interpreted as little endian or big endian.
     *                   The default is "big endian"
     */
    public Data(byte[] bytes, Endianness endianness){
        this.setBytes(bytes);
        this.endianness = endianness;
    }

    /**
     * used to read a bit
     * @param byteIndex byte offset inside the data buffer
     * @param bitIndex bit offset inside the byte referenced by byteIndex
     * @return true: bit == 1, false: bit == 0
     * @throws AddressException
     */
    public boolean getBIT(int byteIndex, int bitIndex) throws AddressException
    {
        if (bitIndex >= 8 || byteIndex >= getBytes().length){
            throw new AddressException("bit index " + bitIndex + " or byte index " + byteIndex + " invalid");
        }
        return (getBytes()[byteIndex] & bitMask[bitIndex]) != 0;
    }

    /**
     * used to set a bit to a given value: true: bit = 1, false: bit = 0
     * @param byteIndex byte offset inside the data buffer
     * @param bitIndex bit offset inside the byte referenced by byteIndex
     * @param value the value
     * @throws AddressException
     */
    public void setBIT(int byteIndex, int bitIndex, boolean value) throws AddressException
    {
        if (bitIndex >= 8 || byteIndex >= getBytes().length){
            throw new AddressException("bit index " + bitIndex + " or byte index " + byteIndex + " invalid");
        }
        if (value){
            getBytes()[byteIndex] |= bitMask[bitIndex];
        }
        else{
            getBytes()[byteIndex] &= ~bitMask[bitIndex];//Tilde
        }
    }
    
    /**
     * used to read a byte. The value is treated as signed -128 .. 127
     * @param byteIndex byte offset inside the data buffer
     * @return the byte value
     * @throws AddressException
     */
    public int getBYTE(int byteIndex) throws AddressException{
        if (byteIndex < 0 || byteIndex > getBytes().length){
            throw new AddressException("byte index " + byteIndex + " invalid");
        }
        return getBytes()[byteIndex] & 0x000000FF;
    }

    /**
     * used to set a byte. The value is treated as unsigned 0..255
     * @param byteIndex byte offset inside the data buffer
     * @param value the value
     * @throws AddressException
     */
    public void setBYTE(int byteIndex, int value) throws AddressException, ValueOutOfRangeException{
        if (value > 255 || value < 0){
            throw new ValueOutOfRangeException("value: " + value);
        }
        if (byteIndex < 0 || byteIndex >= getBytes().length){
            throw new AddressException("byte index " + byteIndex + " invalid");
        }
        getBytes()[byteIndex] = (byte)value;
    }

    /**
     * used to read a word value. The value is treated as an unsigned 16 bit value: 0..65535
     * @param byteIndex byte offset inside the data buffer
     * @return the word value
     * @throws AddressException
     */
    public int getWORD(int byteIndex) throws AddressException {
        int highByte;
        int word;
        if (byteIndex < 0 || byteIndex + 1 >= getBytes().length){
            throw new AddressException("byte index " + byteIndex + " invalid");
        }
        if (endianness == Endianness.BIGENDIAN){
            highByte = (getBytes()[byteIndex] & 0x000000FF) << 8;
            word =  highByte + (getBytes()[byteIndex + 1] & 0x000000FF);
        }
        else{
            highByte = (getBytes()[byteIndex] & 0x000000FF);
            word     = highByte + ((getBytes()[byteIndex + 1] & 0x000000FF) << 8);            
        }
        return word;
    }

    /**
     * used to set a word value. The value is treated as an unsigned 16 bit value: 0..65535
     * @param byteIndex byte offset inside the data buffer
     * @param value the value
     * @throws AddressException
     */
    public void setWORD(int byteIndex, int value) throws AddressException, ValueOutOfRangeException {
        if (value > 0x0000FFFF || value < 0){
            throw new ValueOutOfRangeException("value: " + value);
        }
        if (byteIndex < 0 || byteIndex + 1 >= getBytes().length){
            throw new AddressException("byte index " + byteIndex + " invalid");
        }
        if (endianness == Endianness.BIGENDIAN){
            getBytes()[byteIndex]     = (byte)(value >> 8);
            getBytes()[byteIndex + 1] = (byte)value;
        }
        else{
            getBytes()[byteIndex]     = (byte)value;
            getBytes()[byteIndex + 1] = (byte)(value >> 8);            
        }
    }

    /**
     * used to read an int value. The value is treated as a signed 16 bit value: -32,768 to 32,767
     * @param byteIndex byte offset inside the data buffer
     * @return the int value
     * @throws AddressException
     */
    public int getINT(int byteIndex) throws AddressException {
        int word = getWORD(byteIndex);
        return word > Short.MAX_VALUE ? word | 0xFFFF0000 : word;
    }

    /**
     * used to set an int value. The value is treated as a signed 16 bit value: -32,768 to 32,767
     * @param byteIndex byte offset inside the data buffer
     * @param value the value
     * @throws AddressException
     */
    public void setINT(int byteIndex, int value) throws AddressException, ValueOutOfRangeException {
        if (value > Short.MAX_VALUE || value < Short.MIN_VALUE){
            throw new ValueOutOfRangeException("value: " + value);
        }
        if (byteIndex < 0 || byteIndex + 1 >= getBytes().length){
            throw new AddressException("byte index " + byteIndex + " invalid");
        }
        if (endianness == Endianness.BIGENDIAN){
            getBytes()[byteIndex]     = (byte)(value >> 8);
            getBytes()[byteIndex + 1] = (byte)value;
        }
        else{
            getBytes()[byteIndex]     = (byte)value;
            getBytes()[byteIndex + 1] = (byte)(value >> 8);            
        }
    }

    /**
     * used to read a dword value. The value is treated as an unsigned 32 bit value: 4,294,967,295
     * @param byteIndex byte offset inside the data buffer
     * @return the value
     * @throws AddressException
     */
    public long getDWORD(int byteIndex) throws AddressException {
        long value;
        if (byteIndex < 0 || byteIndex + 3 >= getBytes().length){
            throw new AddressException("byte index " + byteIndex + " invalid");
        }
        if (endianness == Endianness.BIGENDIAN){
             value =                (bytes[byteIndex]     & 0x000000FF);
             value = (value << 8) + (bytes[byteIndex + 1] & 0x000000FF);
             value = (value << 8) + (bytes[byteIndex + 2] & 0x000000FF);
             value = (value << 8) + (bytes[byteIndex + 3] & 0x000000FF);
        }
        else{
             value =                (bytes[byteIndex + 3] & 0x000000FF);
             value = (value << 8) + (bytes[byteIndex + 2] & 0x000000FF);
             value = (value << 8) + (bytes[byteIndex + 1] & 0x000000FF);
             value = (value << 8) + (bytes[byteIndex]     & 0x000000FF);            
        }
        return value;
    }

    /**
     * used to set a dword value. The value is treated as an unsigned 32 bit value: 4,294,967,295
     * @param byteIndex byte offset inside the data buffer
     * @throws AddressException
     */
    public void setDWORD(int byteIndex, long value) throws AddressException, ValueOutOfRangeException {
        if (value > 0xFFFFFFFFL || value < 0){
            throw new ValueOutOfRangeException("value: " + value);
        }
        if (byteIndex < 0 || byteIndex + 3 >= getBytes().length){
            throw new AddressException("byte index " + byteIndex + " invalid");
        }
        if (endianness == Endianness.BIGENDIAN){
            bytes[byteIndex]     = (byte)(value >> 24);
            bytes[byteIndex + 1] = (byte)(value >> 16);
            bytes[byteIndex + 2] = (byte)(value >>  8);
            bytes[byteIndex + 3] = (byte)value;
        }
        else{
            bytes[byteIndex + 3] = (byte)(value >> 24);
            bytes[byteIndex + 2] = (byte)(value >> 16);
            bytes[byteIndex + 1] = (byte)(value >>  8);
            bytes[byteIndex]     = (byte)value;            
        }
    }

    /**
     * used to read a dint value. The value is treated as an signed 32 bit value: −2,147,483,648 .. 2,147,483,647
     * @param byteIndex byte offset inside the data buffer
     * @return the value
     * @throws AddressException
     */
    public int getDINT(int byteIndex) throws AddressException {
        int value;
        if (byteIndex < 0 || byteIndex + 3 >= getBytes().length){
            throw new AddressException("byte index " + byteIndex + " invalid");
        }
        if (endianness == Endianness.BIGENDIAN){
            value =                (bytes[byteIndex]     & 0x000000FF);
            value = (value << 8) + (bytes[byteIndex + 1] & 0x000000FF);
            value = (value << 8) + (bytes[byteIndex + 2] & 0x000000FF);
            value = (value << 8) + (bytes[byteIndex + 3] & 0x000000FF);
        }
        else{
            value =                (bytes[byteIndex + 3] & 0x000000FF);
            value = (value << 8) + (bytes[byteIndex + 2] & 0x000000FF);
            value = (value << 8) + (bytes[byteIndex + 2] & 0x000000FF);
            value = (value << 8) + (bytes[byteIndex]     & 0x000000FF);            
        }
        return value;
    }

    /**
     * used to set a dint value. The value is treated as an signed 32 bit value: −2,147,483,648 .. 2,147,483,647
     * @param byteIndex byte offset inside the data buffer
     * @throws AddressException
     */
    public void setDINT(int byteIndex, int value) throws AddressException {
        if (byteIndex < 0 || byteIndex + 3 >= getBytes().length){
            throw new AddressException("byte index " + byteIndex + " invalid");
        }
        if (endianness == Endianness.BIGENDIAN){
            bytes[byteIndex]     = (byte)(value >> 24);
            bytes[byteIndex + 1] = (byte)(value >> 16);
            bytes[byteIndex + 2] = (byte)(value >>  8);
            bytes[byteIndex + 3] = (byte)value;
        }
        else{
            bytes[byteIndex + 3] = (byte)(value >> 24);
            bytes[byteIndex + 2] = (byte)(value >> 16);
            bytes[byteIndex + 1] = (byte)(value >>  8);
            bytes[byteIndex]     = (byte)value;            
        }
    }

    /**
     * used to read a string value.
     * @param byteIndex byte offset inside the data buffer
     * @return the value
     * @throws AddressException
     */
    public PlcString getSTRING(int byteIndex, int maxLength) throws StringLengthException, AddressException {
        if (byteIndex < 0 || byteIndex +  1 >= getBytes().length){
            throw new AddressException("byte index " + byteIndex + " invalid");
        }
        byte[] bString    = new byte[maxLength];
        System.arraycopy(bytes, byteIndex, bString, 0, maxLength);
        PlcString plcString = new PlcString(bString, maxLength);
        return plcString;
    }

    /**
     * used to set a string value.
     * @param byteIndex byte offset inside the data buffer
     * @throws AddressException
     */
    public void setSTRING(int byteIndex, PlcString value) throws AddressException {
        if (byteIndex < 0 || byteIndex + value.getMaxLength() > getBytes().length){
            throw new AddressException("byte index " + byteIndex + " invalid or string too long: max. Length: " + value.getMaxLength());
        }
        System.arraycopy(value.toString().getBytes(), 0, bytes, byteIndex, value.toString().length());
    }

    /**
     * @return the bytes
     */
    public byte[] getBytes() {
        return bytes;
    }

    /**
     * @param bytes the bytes to set
     */
    public void setBytes(byte[] bytes) {
        this.bytes       = bytes;
        this.shadowBytes = bytes.clone();
        bytesNew = true;
    }

    /**
     * used to clear the data buffer (all entries are set to 0x00). Next call to isModified() will return "true"
     * if the data buffer is null, nothing happens
     */
    public void clear() {
        clear((byte)0x00);
    }

    /**
     * used to clear the data buffer. Next call to isModified() will return "true"
     * if the data buffer is null, nothing happens
     * @param pattern the buffer is set to pattern
     */
    public void clear(byte pattern) {
        if (bytes != null){
            for (int i = 0; i < bytes.length; i++){
                bytes[i] = pattern;
                shadowBytes[0] = pattern;
            }
            bytesNew = true;
        }
    }

    /**
     * used to to force the whole data item to be marked as changed
     * 
     */
    public void forceModified() {
        bytesNew = true;
    }
    
    /**
     * returns the modified state of the data item.<br>
     * The first call following the instantiation or a setBytes(byte[] bytes) call<br>
     * will always return true to let observers synchronize to the new state.
     * @return true, if the data has changed since the last call of isModified()
     */
    public boolean isModified(){
        boolean modified = false;
        if (bytesNew){
            bytesNew = false;
            modified = true;
            //instantiate modified bytes ArrayList
            modifiedByteIndices = new ArrayList<Integer>(bytes.length);
            //and initially mark all bytes as modified to give observers the chance
            //to synchronize their state to the actual state of the real world
            for (int i = 0; i < bytes.length; i++){
                modifiedByteIndices.add(i);
            }
        }
        else{
            modifiedByteIndices.clear();
            for (int i = 0; i < bytes.length; i++){
                boolean byteModified = bytes[i] != shadowBytes[i];
                if (byteModified){
                    modifiedByteIndices.add(i);
                    shadowBytes[i] = bytes[i];
                    modified = true;//at least one byte is modified
                }
            }
        }
        return modified;
    }

    /**
     * returns a ArrayList containing the indices of the modified bytes inside this data item<br>
     * CAUTION: the ArrayList is computed on calling isModified(). To get an up to date ArrayList,
     * call isModified() first.
     * @return
     */
    public ArrayList<Integer> getModifiedByteIndices(){
        return modifiedByteIndices;
    }

  
    @Override
    public String toString() {
        String rt = "Data[";
        for(byte b : this.bytes) {
            rt+= b + ", ";
        }
        rt+="]";
        return rt;
    }
}
