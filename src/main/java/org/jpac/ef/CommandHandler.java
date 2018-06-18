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
    protected List<Byte>                        listOfReceiveResults;
    protected HashMap<Integer, SignalTransport> listOfSignalTransports;

    protected HashMap<Integer, SignalTransport> listOfChangedClientInputTransports;                 
    protected HashSet<SignalTransport>          listOfChangedClientOutputTransports;                 
    protected boolean                           firstSignalValueTransmission;
    protected InetSocketAddress                 remoteSocketAddress;
    
    public CommandHandler(InetSocketAddress remoteSocketAddress){
        this.remoteSocketAddress                 = remoteSocketAddress;
        this.listOfClientInputTransports         = new HashMap<>();
        this.listOfClientOutputTransports        = new HashMap<>();
        this.listOfReceiveResults                = new ArrayList<>();
        this.listOfSignalTransports              = new HashMap<>();
        
        this.listOfChangedClientOutputTransports = new HashSet<>();
        this.listOfChangedClientInputTransports  = new HashMap<>();
        this.firstSignalValueTransmission        = true;
        this.listOfActiveCommandHandlers.add(this);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        out = ctx.alloc().buffer(4096);
    }
     
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Command command;
        Acknowledgement acknowledgement;
        
        try{        
            in      = (ByteBuf) msg;
            command = (Command)MessageFactory.getMessage(in);
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
                Log.info("invalidate " + signal);
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
        Log.info("remote connection for ef://" + remoteSocketAddress.getHostName() + ":" + remoteSocketAddress.getPort() + " opened");
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
        listOfClientInputTransports.put(handle, new SignalTransport(signal));
    }
   
    protected void registerClientOutputSignal(int handle) {
        Signal signal = SignalRegistry.getInstance().getSignal(handle);        
        listOfClientOutputTransports.put(handle, new SignalTransport(signal));
        signal.setConnectedAsTarget(true);//inhibit changes to this signal by other sources
        Log.debug("register " + signal);
    }

    protected void unregisterClientInputSignal(int handle) {
        listOfClientInputTransports.remove(handle);
    }
   
    protected void unregisterClientOutputSignal(int handle) {
        listOfClientOutputTransports.remove(handle);
        Signal signal = SignalRegistry.getInstance().getSignal(handle);
        signal.setConnectedAsTarget(false);
        signal.invalidateDeferred();
    }

    /**
     * called by CommandHandler (command Transceive)
     * @param signalTransport 
     */
    protected List<Byte> updateChangedClientOutputTransports(List<SignalTransport> signalTransports){
        synchronized(listOfClientOutputTransports){
            listOfReceiveResults.clear();
            signalTransports.forEach((st)-> {
                Result result;
                try{
                    SignalTransport target = listOfClientOutputTransports.get(st.getHandle());
                    if (target != null){
                        if (!target.getValue().equals(st.getValue())){
                            //copy only changed values
                            target.getValue().copy(st.getValue());
                            target.setChanged(true);          
                            listOfChangedClientOutputTransports.add(target);
                        }
                        result = Result.NoFault;
                    } 
                    else{
                        result = Result.SignalNotSubscribed; 
                    }
                }
                catch(Exception exc){
                    Log.error("Error accessing signal with handle {} .", st.getHandle(), exc);
                    result = Result.GeneralFailure;
                }
                listOfReceiveResults.add((byte)result.getValue());                    
            });
        }
        return listOfReceiveResults;
    }

    /**
     * called by CommandHandler (command Transceive) 
     * @return  
     */
    protected HashMap<Integer, SignalTransport> retrieveChangedClientInputTransports(){
        synchronized(listOfClientInputTransports){
            listOfSignalTransports.clear();
            listOfChangedClientInputTransports.forEach((handle, signalTransport)-> listOfSignalTransports.put(handle, signalTransport));
            listOfChangedClientInputTransports.clear();
        }
        return listOfSignalTransports;
    }

    /**
     * called by EfService on JPac thread in every cycle
     */
    public void transferChangedClientOutputTransportsToSignals(){
        synchronized(listOfClientOutputTransports){
            listOfChangedClientOutputTransports.forEach((st)-> {
                SignalRegistry.getInstance().getSignal(st.getHandle()).setValue(st.getValue());
                st.setChanged(false);
            });
            listOfChangedClientOutputTransports.clear();
        }
    }

    /**
     * called by EfService on JPac thread in every cycle
     */
    public void transferChangedSignalsToClientInputTransports(){
        synchronized(listOfClientInputTransports){
            listOfClientInputTransports.forEach((handle, signalTransport)->{
                Signal signal     = SignalRegistry.getInstance().getSignal(handle);
                Value sigValue    = signal.getValue();
                Value outputValue = signalTransport.getValue();
                if (firstSignalValueTransmission || !sigValue.equals(outputValue)){
                    //first signal transmission after connection or signal changed
                    signalTransport.getValue().copy(sigValue);
                    signalTransport.setChanged(true);
                    listOfChangedClientInputTransports.put(signalTransport.getHandle(), signalTransport);
                }
            });
            firstSignalValueTransmission  = false;
        }
    }

    /**
     * @return the listOfChangedClientInputTransports
     */
    public HashMap<Integer, SignalTransport> getListOfChangedOutputTransports() {
        return listOfChangedClientInputTransports;
    }
}
