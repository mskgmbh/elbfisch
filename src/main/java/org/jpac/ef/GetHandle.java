/**
 * PROJECT   : Elbfisch - java process automation controller (jPac) 
 * MODULE    : GetHandle.java (versatile input output subsystem)
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
import org.jpac.Signal;
import org.jpac.SignalNotRegisteredException;
import org.jpac.SignalRegistry;

/**
 *
 * @author berndschuster
 */
public class GetHandle extends Command{    
    protected int             handle;
    protected String          signalIdentifier;
    protected BasicSignalType signalType;
    
    //server
    public GetHandle(){
        super(MessageId.CmdGetHandle);
        this.signalIdentifier = null;
    }
    
    //client
    public GetHandle(String signalIdentifier, BasicSignalType signalType){
        this();
        this.signalIdentifier = signalIdentifier;
        this.signalType       = signalType;
    }
    
    //client
    @Override
    public void encode(ByteBuf byteBuf){
        super.encode(byteBuf);
        encodeString(signalIdentifier, byteBuf);
        signalType.encode(byteBuf);
    }
    
    //server
    @Override
    public void decode(ByteBuf byteBuf){
        super.decode(byteBuf);
        signalIdentifier = decodeString(byteBuf);
        signalType       = BasicSignalType.fromInt(byteBuf.readInt());
    }
    
    //client
    public int getHandle(){
        return ((GetHandleAcknowledgement)getAcknowledgement()).getHandle();
    }

    //server
    @Override
    public Acknowledgement handleRequest(CommandHandler commandHandler) {
        Log.debug("handleRequest(): " + this);
        try{
            Signal signal = SignalRegistry.getInstance().getSignal(signalIdentifier);
            int    index  = signal.hashCode();
            acknowledgement = new GetHandleAcknowledgement(signalIdentifier, index, BasicSignalType.fromSignal(signal));
            acknowledgement.setResult(Result.NoFault);
        }
        catch(SignalNotRegisteredException exc){
            acknowledgement = new GetHandleAcknowledgement(signalIdentifier, 0, signalType);
            acknowledgement.setResult(Result.SignalNotRegistered);            
        }
        catch(Exception exc){
            acknowledgement = new GetHandleAcknowledgement(signalIdentifier, 0, signalType);
            acknowledgement.setResult(Result.GeneralFailure);
        }
        return getAcknowledgement();
    }

    @Override
    public Acknowledgement getAcknowledgement() {
        return acknowledgement;
    }
    
    @Override
    public String toString(){
        return super.toString() + "('" + signalIdentifier + "', " + signalType +", " + handle + ")";
    }
}
