/**
 * PROJECT   : Elbfisch - java process automation controller (jPac) 
 * MODULE    : BasicSignalType.java (versatile input output subsystem)
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
package org.jpac.ef;

import io.netty.buffer.ByteBuf;
import java.io.IOException;
import org.jpac.CharString;
import org.jpac.Decimal;
import org.jpac.Logical;
import org.jpac.Signal;
import org.jpac.SignedInteger;

/**
 *
 * @author berndschuster
 */
public enum BasicSignalType {
    Logical       ("org.jpac.Logical".hashCode()),
    SignedInteger ("org.jpac.SignedInteger".hashCode()),
    Decimal       ("org.jpac.Decimal".hashCode()),
    CharString    ("org.jpac.CharString".hashCode()),
    Unknown       (-1);
    
    int type;
    
    BasicSignalType(int type){
        this.type = type;
    }
        
    public void encode(ByteBuf byteBuf){
        byteBuf.writeInt(type);
    }

    public boolean equals(BasicSignalType type){
        return this.type == type.type;
    }    

    public static int size(){
        return 4;
    }

    public static BasicSignalType fromInt(int result){
        BasicSignalType  retValue = BasicSignalType.Unknown;
        for (BasicSignalType res: BasicSignalType.values()){
            if (res.type == result){
                retValue = res;
            }
        }
        return retValue;
    }    

    public static BasicSignalType fromSignal(Signal signal){
        BasicSignalType  retValue = BasicSignalType.Unknown;
        if (signal instanceof Logical){
           retValue = BasicSignalType.Logical; 
        } else if (signal instanceof Decimal){
           retValue = BasicSignalType.Decimal; 
        } else if (signal instanceof SignedInteger){
           retValue = BasicSignalType.SignedInteger; 
        } else if (signal instanceof CharString){
           retValue = BasicSignalType.CharString; 
        } 
        return retValue;
    }    
}
