/**
 * PROJECT   : Elbfisch - java process automation controller (jPac) 
 * MODULE    : CommandHandler.java (versatile input output subsystem)
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
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.jpac.Signal;
import org.jpac.SignalRegistry;
import org.jpac.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author berndschuster
 */
public class CommandHandler extends ChannelInboundHandlerAdapter {
    protected Logger    Log       = LoggerFactory.getLogger("jpac.ef");
    
    static final List<CommandHandler> listOfActiveCommandHandlers = new ArrayList<>();

    protected ByteBuf in;
    protected ByteBuf out;
    
    protected HashMap<Integer, SignalTransport> listOfClientInputTransports;
    protected HashMap<Integer, SignalTransport> listOfClientOutputTransports;

    protected boolean                           firstSignalValueTransmission;
    protected InetSocketAddress                 remoteSocketAddress;
    protected MessageFactory                    messageFactory;
	protected SignalTransport                   target;
	protected int                               index;                      
    
    public CommandHandler(InetSocketAddress remoteSocketAddress){
        this.remoteSocketAddress                 = remoteSocketAddress;
        this.listOfClientInputTransports         = new HashMap<>();
        this.listOfClientOutputTransports        = new HashMap<>();
        
        this.firstSignalValueTransmission        = true;
        
        this.messageFactory                      = new MessageFactory(this);
        this.listOfActiveCommandHandlers.add(this);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        out = ctx.alloc().buffer(4096);
    }
     
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Command         command;
        Acknowledgement acknowledgement;
        
        try{        
            in      = (ByteBuf) msg;
            command = (Command)messageFactory.getRecycledMessage(in);
            Log.debug("received command {}", command);
            acknowledgement = command.handleRequest(this);
            Log.debug("{} acknowledged with {}", command, acknowledgement);
            out.clear();
            acknowledgement.encode(out);
            out.retain();//"out" should be reused for all acknowledgements until context ist closed
            ctx.writeAndFlush(out);
            in.release();
        } catch(Exception exc){
            Log.error("Error: ", exc);
        }
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception{
        super.channelInactive(ctx);
        //disconnect all received signals, and invalidate them
        listOfClientOutputTransports.keySet().forEach(
                (h)-> {Signal signal = SignalRegistry.getInstance().getSignal(h);
                signal.setConnectedAsTarget(false);
                signal.invalidateDeferred();
        });
        if (listOfActiveCommandHandlers.contains(this)){
            listOfActiveCommandHandlers.remove(this);
        }
        Log.info("remote connection for ef://" + remoteSocketAddress.getHostName() + ":" + remoteSocketAddress.getPort() + " closed");
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception{
        super.channelActive(ctx);
        Log.info("remote connection for ef://" + remoteSocketAddress.getHostName() + ":" + remoteSocketAddress.getPort() + " established");
        firstSignalValueTransmission = true;//invoke transfer of all client input signals on first transmission regardless if changed or not
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception{
        super.channelActive(ctx);
        if (listOfActiveCommandHandlers.contains(this)){
            listOfActiveCommandHandlers.remove(this);
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
        
    public static List<CommandHandler> getListOfActiveCommandHandlers(){
        return listOfActiveCommandHandlers;
    }

    protected void registerClientInputSignal(int handle) {
        Signal signal = SignalRegistry.getInstance().getSignal(handle);
        SignalTransport st = new SignalTransport(signal);
        listOfClientInputTransports.put(handle, st);
    }
   
    protected void registerClientOutputSignal(int handle) {
        Signal signal = SignalRegistry.getInstance().getSignal(handle);        
        SignalTransport st = new SignalTransport(signal);
        signal.setConnectedAsTarget(true);
        listOfClientOutputTransports.put(handle, st);
        Log.debug("register " + signal);
    }

    protected void unregisterClientInputSignal(int handle) {
    	SignalTransport st = listOfClientInputTransports.get(handle);
    	st.getConnectedSignal().disconnect(st);
        listOfClientInputTransports.remove(handle);
    }
   
    protected void unregisterClientOutputSignal(int handle) {
    	SignalTransport st = listOfClientOutputTransports.get(handle);
    	st.getConnectedSignal().setConnectedAsTarget(false);
        st.getConnectedSignal().invalidateDeferred();
        listOfClientOutputTransports.remove(handle);
    }

    /**
     * called by CommandHandler (command Transceive.handleRequest())
     * @param signalTransport 
     */
    protected void updateClientOutputTransports(List<SignalTransport> signalTransports){
        synchronized(listOfClientOutputTransports){
            signalTransports.forEach((st)-> {
                try{
                    SignalTransport target = listOfClientOutputTransports.get(st.getHandle());
                    if (target != null){
                        if (!target.getValue().equals(st.getValue())){
                            //copy only changed values
                            target.getValue().copy(st.getValue());
                            target.setChanged(true);          
                        }
                    } 
                }
                catch(Exception exc){
                	if (target != null) {
                		Log.error("Error accessing " + target.getConnectedSignal(),  exc);
                	} else {
                		Log.error("Error accessing signal with handle " + st.getHandle(),  exc);                		
                	}
                }
            });
        }
        return;
    }

    /**
     * called by EfService on JPac thread in every cycle
     */
    public void transferChangedClientOutputTransportsToSignals(){
        synchronized(listOfClientOutputTransports){
            listOfClientOutputTransports.values().forEach((st)-> st.getConnectedSignal().setValue(st.getValue()));
        }
    }

    /**
     * called by EfService on JPac thread in every cycle
     */
    public void transferChangedSignalsToClientInputTransports(){
        synchronized(listOfClientInputTransports){
            listOfClientInputTransports.values().forEach((st)->	st.setValue(st.getConnectedSignal().getValue()));
        }
    }

    /**
     * @return the listOfClientInputTransports
     */
    public HashMap<Integer, SignalTransport> getListOfClientInputTransports() {
        return listOfClientInputTransports;
    }

    /**
     * @return the listOfClientOutputTransports
     */
    public HashMap<Integer, SignalTransport> getListOfClientOutputTransports() {
        return listOfClientOutputTransports;
    }
}
