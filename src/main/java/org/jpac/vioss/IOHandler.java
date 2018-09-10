/**
 * PROJECT   : Elbfisch - java process automation controller (jPac) 
 * MODULE    : IOHandler.java (versatile input output subsystem)
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

package org.jpac.vioss;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.jpac.Address;
import org.jpac.CyclicTask;
import org.jpac.JPac;
import org.jpac.NumberOutOfRangeException;
import org.jpac.Signal;
import org.jpac.SignalAccessException;
import org.jpac.SignalRegistry;
import org.jpac.WrongUseException;
import org.jpac.plc.AddressException;
import org.jpac.plc.IoDirection;

/**
 * implements basic functions of vioss IOHandlers.
 * @author berndschuster
 */
abstract public class IOHandler implements CyclicTask{
    static public Logger Log = LoggerFactory.getLogger("jpac.vioss.IOHandler");
    
    private URI             uri; 
    private List<IoSignal>  inputSignals;
    private List<IoSignal>  outputSignals;
    private boolean         processingStarted;
    private boolean         processingAborted;
    
    private String          toString;
    
    public IOHandler(URI uri)  throws IllegalUriException {
        this.inputSignals       = Collections.synchronizedList(new ArrayList<IoSignal>());
        this.outputSignals      = Collections.synchronizedList(new ArrayList<IoSignal>());
        this.uri                = uri;
        this.processingStarted  = false;
        try{JPac.getInstance().registerCyclicTask(this);}catch(WrongUseException exc){/*cannot happen*/};
    }
    
    protected void putSignalsToOutputProcessImage(){
        synchronized(outputSignals){
            for (IoSignal outputSignal: outputSignals){
                try{outputSignal.checkOut();} catch(Exception exc){/*cannot happen*/};
            }
        }        
    }
    
    protected void seizeSignalsFromInputProcessImage() throws SignalAccessException, AddressException{
        synchronized(inputSignals){
            for (IoSignal inputSignal: inputSignals){
                try{inputSignal.checkIn();}catch(NumberOutOfRangeException exc){/*cannot happen*/}
            }
        }
    }

    /**
     * @return the uri
     */
    public URI getUri() {
        return uri;
    }

    /**
     * @used to register an InputSignal. If it is already registered, it is omitted. Must be called before
     *  the first jPac cycle
     * @param signal input signal to regiester
     * @throws org.jpac.WrongUseException thrown, if called while jPac is running
     */
    public void registerInputSignal(IoSignal signal) throws WrongUseException {
        if (processingStarted){
            throw new WrongUseException("all input/output signals must be registered before jpac is started");
        }
        if (signal.getIoDirection() != IoDirection.INOUT && signal.getIoDirection() != IoDirection.INPUT){
            throw new WrongUseException("signal must be an input signal");            
        }
        if (!inputSignals.contains(signal)){
            inputSignals.add(signal);
        }
    }

    /**
     * @used to register an OutputSignal. If it is already registered, it is omitted. Must be called before
     *  the first jPac cycle
     * @param signal output signal to regiester
     * @throws org.jpac.WrongUseException thrown, if called while jPac is running
     */
    public void registerOutputSignal(IoSignal signal) throws WrongUseException {
        if (processingStarted){
            throw new WrongUseException("all input/output signals must be registered before jpac is started");
        }
        if (signal.getIoDirection() != IoDirection.INOUT && signal.getIoDirection() != IoDirection.OUTPUT){
            throw new WrongUseException("signal must be an output signal");            
        }
        if (!outputSignals.contains(signal)){
            outputSignals.add(signal);
        }
    }
    
    public void discardSignal(IoSignal signal){
        if (inputSignals.contains(signal)){
            inputSignals.remove(signal);
        }
        if (outputSignals.contains(signal)){
            outputSignals.remove(signal);
        }
        SignalRegistry.getInstance().remove((Signal) signal);
    }

    protected void stopProcessing(){
//        JPac.getInstance().unregisterCyclicTask(this);        
//        Log.error(this.getClass().getCanonicalName() + ": processing stopped !!");   
    }
    
    /**
     * @return the list of registered inputSignals
     */
    public List<IoSignal> getInputSignals() {
        return inputSignals;
    }

    /**
     * @return the list of registered outputSignals
     */
    public List<IoSignal> getOutputSignals() {
        return outputSignals;
    }
    
    @Override
    public String toString(){
        if (toString == null){
            toString = getClass().getCanonicalName() + "(" + getTargetInstance()     + ")";
        }
        return toString;
    }
    
    /**
     * @return the processingStarted
     */
    public boolean isProcessingStarted() {
        return processingStarted;
    }

    /**
     * @param processingStarted the processingStarted to set
     */
    protected void setProcessingStarted(boolean processingStarted) {
        this.processingStarted = processingStarted;
    }
    
    /**
     * @return the processingAborted
     */
    public boolean isProcessingAborted() {
        return processingAborted;
    }
    
    public String getTargetInstance(){
        return uri.toString().replace(uri.getPath(),"");
    }

    /**
     * @param processingAborted the processingStopped to set
     */
    protected void setProcessingAborted(boolean processingAborted) {
        this.processingAborted = processingAborted;
    }

    /**
     * is called by jPac on every cycle. Performs the phyiscal input/output processing and the
     * transfer to the corresponding ProcessSignal's
     * Hint: This method must be implemented must be short, fast and robust because it effects the
     *       jPac cycle directly
     */
    @Override
    abstract public void run();

    /**
     * used to checkIn, if this IOHandler handles the data item with the given address
     * @param address address to checkIn
     * @param uri     uri to checkIn
     * @return true : this IOHandler handles this data item 
     */
    abstract public boolean handles(Address address, URI uri);
    
    /**
     * used to do some initializing after all InputSignal's and OutputSignal's are registered for this
     * IOHandler instance. Is called by jPac just before it starts processing.
     */
    @Override
    abstract public void prepare();
    
    /**
     * is called by jPac during the shutdown. Can be used to clean up the context of the IOHandler, for example by
     * closing remote connections etc.
     */
    @Override
    abstract public void stop();
    
    /**
     *
     * @return the scheme handled by this IOHandler 
     */
    abstract public String getHandledScheme();    
}
