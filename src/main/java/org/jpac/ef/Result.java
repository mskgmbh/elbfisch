/**
 * PROJECT   : Elbfisch - java process automation controller (jPac) 
 * MODULE    : Result.java (versatile input output subsystem)
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

/**
 *
 * @author berndschuster
 */
public enum Result {
    NoFault                          (  0),
    SignalNotRegistered              (  1),
    SignalTypeMismatched             (  2),
    IoDirectionMustBeInOrOut         (  3),
    SignalAlreadyConnectedAsTarget   (  4),
    SignalNotSubscribed              (  5),
    ElbfischInstanceAlreadyConnected (  6),
    GeneralFailure                   ( 99),
    Unknown                          ( -1);
    
    int value;
    
    Result(int result){
        this.value = result;
    }
        
    public void encode(ByteBuf byteBuf){
        byteBuf.writeShort(value);
    }

    public boolean equals(Result type){
        return this.value == type.value;
    }    

    public static int size(){
        return 2;
    }
    
    public int getValue(){
        return value;
    }

    public static Result fromInt(int result){
        Result  retValue = Result.Unknown;
        int     idx   = 0;
        for (Result res: Result.values()){
            if (res.value == result){
                retValue = res;
            }
        }
        return retValue;
    }    
}
