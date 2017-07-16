/**
 * PROJECT   : jPac PLC communication library
 * MODULE    : Types.java
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
 *
 * @author berndschuster
 */
public enum Types {BOOL(1),  // size is at least 1 byte
                   BYTE(1),
                   WORD(2),
                   DWORD(4),
                   CHAR(1),
                   INT(2),
                   DINT(4),
                   REAL(4),
                   STRING(0),// size is variable
                   UDT(0),   // size is variable
                   DB(0),    // size is variable
                   UNDEFINED(0);
                   
           private int size;

           Types(int size){
               this.size = size;
           }

           public int getSize(){
               return this.size;
           }
           public static Types get(String symbol){
               Types value = UNDEFINED;
               if (symbol == null)
                  return UNDEFINED;
               if (BOOL.toString().equals(symbol))
                  value = BOOL;
               if (BYTE.toString().equals(symbol))
                  value = BYTE;
               if (WORD.toString().equals(symbol))
                  value = WORD;
               if (DWORD.toString().equals(symbol))
                  value = DWORD;
               if (CHAR.toString().equals(symbol))
                  value = CHAR;
               if (INT.toString().equals(symbol))
                  value = INT;
               if (DINT.toString().equals(symbol))
                  value = DINT;
               if (REAL.toString().equals(symbol))
                  value = REAL;
               if (STRING.toString().equals(symbol))
                  value = STRING;
               if (symbol.startsWith(UDT.toString()))
                  value = UDT;
               if (symbol.startsWith(DB.toString()))
                  value = DB;
               return value;
           }
}
