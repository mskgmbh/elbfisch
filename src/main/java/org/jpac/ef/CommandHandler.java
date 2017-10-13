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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.jpac.Signal;
import org.jpac.SignalNotRegisteredException;
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
    protected final int ENDOFLIST = -1;      
    
    static final List<CommandHandler> listOfActiveCommandHandlers = new ArrayList<>();

    protected ByteBuf in;
    protected ByteBuf out;
    
    protected HashMap<Integer, SignalTransport> listOfOutputValues;
    protected HashMap<Integer, SignalTransport> listOfInputValues;
    protected List<Byte>                        listOfReceiveResults;
    protected List<SignalTransport>             listOfSignalTransports;

    protected int[]                             handlesOfChangedOutputValues;                 
    protected int[]                             handlesOfChangedInputSignals;                 
    protected int                               inputIndex, outputIndex;
    protected boolean                           inputValuesTransferred, outputValuesTransferred;
    protected boolean                           firstSignalValueTransmission;
    
    public CommandHandler(){
        this.listOfActiveCommandHandlers.add(this);

        this.listOfOutputValues     = new HashMap<>();
        this.listOfInputValues      = new HashMap<>();
        this.listOfReceiveResults   = new ArrayList<>();
        this.listOfSignalTransports = new ArrayList<>();
        
        //prepare list of input signals adding SignalTransports for all signals for reuse during rapid data exchange 
        SignalRegistry.getInstance().getSignals().forEach((sig)->{
            int handle = SignalRegistry.getInstance().getIndex(sig.getQualifiedIdentifier());
            try{this.listOfInputValues.put(handle, new SignalTransport(handle, sig.getValue().clone()));}catch(CloneNotSupportedException exc){/*cannot happen*/}
        });
        handlesOfChangedInputSignals = new int[SignalRegistry.getInstance().getSignals().size() + 1];
        handlesOfChangedOutputValues = new int[SignalRegistry.getInstance().getSignals().size() + 1];
        firstSignalValueTransmission = true;
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
        super.channelActive(ctx);
        if (listOfActiveCommandHandlers.contains(this)){
            listOfActiveCommandHandlers.remove(this);
        }
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

    protected void addToListOfOutputSignals(int handle) {
        listOfOutputValues.put(handle, new SignalTransport(handle, SignalRegistry.getInstance().getSignal(handle).getValue()));
    }
   
    /**
     * called by CommandHandler (command Transceive)
     * @param signalTransport 
     */
    public List<Byte> updateInputValues(List<SignalTransport> signalTransports){
        synchronized(listOfInputValues){
            if (!inputValuesTransferred){
                listOfReceiveResults.clear();
                inputIndex = 0;
                signalTransports.forEach((st) -> {
                    try{
                    SignalTransport target =  listOfInputValues.get(st.getHandle());
                    listOfReceiveResults.add((byte)Result.NoFault.getValue());
                    if (!target.getValue().equals(st.getValue())){
                        //copy only changed values
                        target.getValue().copy(st.getValue());
                        target.setChanged(true);          
                        handlesOfChangedInputSignals[inputIndex++] = st.getHandle();
                    }
                    }
                    catch(Exception exc){
                        Log.error("Error: Signal with handle {} not registered.", st.getHandle(), exc);
                        listOfReceiveResults.add((byte)Result.SignalNotRegistered.getValue());                    
                    }
                    
                });
                handlesOfChangedInputSignals[inputIndex] = ENDOFLIST;
                inputValuesTransferred         = true;
            }
        }
        return listOfReceiveResults;
    }

    /**
     * called by CommandHandler (command Transceive) 
     * @return  
     */
    public List<SignalTransport> updateOutputValues(){
        synchronized(listOfOutputValues){
            if (outputValuesTransferred){
                listOfSignalTransports.clear();
                int i = 0;
                while(handlesOfChangedOutputValues[i] != ENDOFLIST){
                    SignalTransport signalTransport = listOfOutputValues.get(handlesOfChangedOutputValues[i++]);
                    listOfSignalTransports.add(signalTransport);
                }
                outputValuesTransferred = false;
            }
        }
        return listOfSignalTransports;
    }

    /**
     * called by JPac in every cycle
     */
    public void transferInputValuesToSignals(){
        synchronized(listOfInputValues){
            if (inputValuesTransferred){
                int i = 0;
                while(handlesOfChangedInputSignals[i] != ENDOFLIST){
                    SignalTransport signalTransport = listOfInputValues.get(handlesOfChangedInputSignals[i++]);
                    if (signalTransport.isChanged()){
                        SignalRegistry.getInstance().getSignal(signalTransport.getHandle()).setValue(signalTransport.getValue());
                        signalTransport.setChanged(false);
                    }
                }
                inputValuesTransferred = false;
            }
        }
    }

    /**
     * called by JPac in every cycle
     */
    public void transferSubcribedSignalsToOutputValues(){
        synchronized(listOfOutputValues){
            if (!outputValuesTransferred){
                outputIndex = 0;
                listOfOutputValues.forEach((handle, signalTransport)->{
                    Signal signal     = SignalRegistry.getInstance().getSignal(handle);
                    Value sigValue    = signal.getValue();
                    Value outputValue = signalTransport.getValue();
                    if (firstSignalValueTransmission || !sigValue.equals(outputValue)){
                        signalTransport.getValue().copy(sigValue);
                        signalTransport.setChanged(true);
                    }
                    handlesOfChangedOutputValues[outputIndex++] = handle;
                });
                handlesOfChangedOutputValues[outputIndex] = ENDOFLIST;
                outputValuesTransferred                   = true;
                firstSignalValueTransmission              = false;//transfer all signals on first transmission
            }
        }
    }

    /**
     * @return the listOfOutputValues
     */
    public HashMap<Integer, SignalTransport> getListOfOutputValues() {
        return listOfOutputValues;
    }

    /**
     * @return the listOfInputValues
     */
    public HashMap<Integer, SignalTransport> getListOfInputValues() {
        return listOfInputValues;
    }
}
