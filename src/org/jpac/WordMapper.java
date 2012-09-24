/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : WordMapper.java
 * VERSION   : $Revision$
 * DATE      : $Date$
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
 *
 * LOG       : $Log$
 */

package org.jpac;

/**
 *
 * @author berndschuster
 */
public class WordMapper {
    protected boolean signed;
    protected int     numOfBytes;
    protected int     bitMask;
    protected int     signBitMask;
    public WordMapper(int numOfBytes, boolean signed){
        this.signed     = signed;
        if (numOfBytes > 4){
            throw new NumberFormatException();
        }
        this.numOfBytes  = numOfBytes;
        if (numOfBytes == 4){
            this.bitMask = 0x00000000;
        }
        else{
            this.bitMask = 0xFFFFFFFF << (8 * numOfBytes);
        }
        this.signBitMask = 1  << (8 * numOfBytes -1);
    }

    public int getMaxValue(){
        int maxValue = 0;
        switch(numOfBytes){
            case 1: maxValue = signed ? 0x7F      : 0xFF;      break;
            case 2: maxValue = signed ? 0x7FFF    : 0xFFFF;    break;
            case 3: maxValue = signed ? 0x7FFFFF  : 0xFFFFFF;  break;
            case 4: maxValue = signed ? 0x7FFFFFFF: 0x7FFFFFFF;break;
        }
        return maxValue;
    }

    public int getMinValue(){
        int minValue = 0;
        switch(numOfBytes){
            case 1: minValue = signed ? 0xFFFFFF80 : 0;break;
            case 2: minValue = signed ? 0xFFFF8000 : 0;break;
            case 3: minValue = signed ? 0xFF800000 : 0;break;
            case 4: minValue = signed ? 0x80000000 : 0;break;
        }
        return minValue;
    }
    
    public int toInt(int wordValue){
        //expand sign bit
        if (signed && ((wordValue & signBitMask) != 0)){
            wordValue = wordValue | bitMask;
        }
        return wordValue;
    }

    public int toWord(int intValue){
        //clear unused high bits
        return intValue & ~bitMask;
    }
}
