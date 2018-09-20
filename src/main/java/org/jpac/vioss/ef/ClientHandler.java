/**
 * PROJECT   : Elbfisch - java process automation controller (jPac) 
 * MODULE    : ClientHandler.java (versatile input output subsystem)
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

package org.jpac.vioss.ef;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.jpac.InconsistencyException;
import org.jpac.ef.Acknowledgement;
import org.jpac.ef.Command;
import org.jpac.ef.MessageFactory;
import org.jpac.ef.MessageId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author berndschuster
 */
public class ClientHandler extends ChannelInboundHandlerAdapter {
    protected final static int      ONEPERMIT = 1;
    protected Logger  Log = LoggerFactory.getLogger("jpac.ef");
        
    protected ByteBuf               txByteBuf;
    protected Semaphore             serverResponded;
    protected ChannelHandlerContext context;
    protected Acknowledgement       receivedAcknowledgement;
    protected boolean               transactionInProgress;
    protected Command               actualCommand;
    protected boolean               transactionSucceeded;
    
    public ClientHandler(){
        super();
        this.serverResponded       = new Semaphore(ONEPERMIT);
        this.transactionInProgress = false;
        this.transactionSucceeded  = false;
    }
    
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        context   = ctx;
        if (txByteBuf != null) {
        	txByteBuf.release();
        }
        txByteBuf = ctx.alloc().buffer(32000);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
    	transactionSucceeded = false;
        ByteBuf rxByteBuf = (ByteBuf) msg; // (1)
        try {
        	MessageId receivedMessageId = MessageFactory.readMessageId(rxByteBuf);
        	if (receivedMessageId.equals(actualCommand.getAcknowledgement().getMessageId())) {
	            receivedAcknowledgement = actualCommand.getAcknowledgement();
	            receivedAcknowledgement.decode(rxByteBuf);
	            Log.debug("acknowledgement received from server: " + receivedAcknowledgement);
	            transactionSucceeded = true;
        	} else {
        		throw new InconsistencyException("received " + receivedMessageId + " expected " + actualCommand.getAcknowledgement().getMessageId());
        	}
       
        }  catch(Exception exc) {
        	Log.error("Error: ", exc);
        	throw exc;
        
        } finally {
            //tell transact()ing thread, that an acknowledgement arrived
            serverResponded.release();
            rxByteBuf.release();
        }
    }
    
//    @Override
//    public void channelInactive(ChannelHandlerContext ctx) {
//    
//    }    
    
    public Acknowledgement transact(Command command) throws InterruptedException, InconsistencyException, TimeoutException{
        boolean done = false;
        startAsynchronously(command);
        try{
            //wait until server responses (serverResponded released by channelRead())
            done = serverResponded.tryAcquire(3000, TimeUnit.MILLISECONDS);
            if(!done){
                throw new TimeoutException("server did not respond in time");
            }
            if (!transactionSucceeded) {
            	throw new InconsistencyException("transaction failed: " + command);
            }
        }
        finally{
            serverResponded.release();
            transactionInProgress = false;
        }
        return receivedAcknowledgement;
    }

    public void startAsynchronously(Command command) throws InterruptedException, InconsistencyException {
        if (transactionInProgress){
            throw new InconsistencyException("Nested transaction not allowed. Command : " + command + " ignored");
        }
        txByteBuf.retain();//increment reference count to avoid destruction of buffer
        txByteBuf.clear();
        actualCommand = command;
        actualCommand.encode(txByteBuf);
        Log.debug("sending " + command + " to server ...");
        transactionInProgress = true;
        //acquire semaphore
        serverResponded.tryAcquire();
        context.channel().eventLoop().submit(() -> context.writeAndFlush(txByteBuf)).sync();
    }
    
    public boolean isCommandAcknowledged(){
        return serverResponded.availablePermits() == ONEPERMIT;
    }
    
    public boolean isTransactionInProgress(){
        return this.transactionInProgress;
    }

    public void resetTransactionInProgress(){
        this.transactionInProgress = false;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Log.error("Error: " + cause);
        ctx.close();
    }
}
