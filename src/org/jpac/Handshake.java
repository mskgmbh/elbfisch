/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : Handshake.java
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

package org.jpac;

import org.apache.log4j.Logger;

/**
 * implements a handshake mechanism to synchronize two modules which have a producer/consumer relationship
 * @author berndschuster
 */
public class Handshake {
    public static final int OK           = 0;
    public static final int GENERALERROR = -1;
    
    static    Logger Log = Logger.getLogger("jpac.Handshake");
    
    private Logical                request;
    private Logical                acknowledge;
    private Logical                active;
    private SignedInteger          resultSig;
    private ProcessEvent           requestedEvent;
    private ProcessEvent           acknowledgedEvent;
    private ProcessEvent           activeEvent;
    private AbstractModule         requestingModule;
    private AbstractModule         acknowledgingModule;
    private String                 identifier;
    private long                   lastRequestCycle;
    private long                   lastAcknowledgeCycle;
    private RequestRunner          requestRunner;
    private ResetRequestRunner     resetRequestRunner;
    private ResetAcknowledgeRunner resetAcknowledgeRunner;
    private int                    result;
    
    /**
     * @param servingModule the acknowledging module (consumer)
     * @param identifier the identifier of this handshake (Corresponds to the identifiers of signals).
     */
    public Handshake(AbstractModule servingModule, String identifier) throws SignalAlreadyExistsException{
        this.requestingModule       = null;
        this.acknowledgingModule    = servingModule;
        this.identifier             = identifier;
        this.request                = new Logical(servingModule, identifier + ".request", false);
        this.acknowledge            = new Logical(servingModule, identifier + ".acknowledge", false);
        this.active                 = new Logical(servingModule, identifier + ".active", false);
        this.resultSig              = new SignedInteger(servingModule, identifier + ".result", OK);
        this.acknowledgedEvent      = acknowledge.state(true);
        this.activeEvent            = active.state(true);
        this.requestedEvent         = request.state(true);
        this.lastRequestCycle       = 0L;
        this.lastAcknowledgeCycle   = 0L;
        this.requestRunner          = new RequestRunner();
        this.resetRequestRunner     = new ResetRequestRunner();
        this.resetAcknowledgeRunner = new ResetAcknowledgeRunner();
        this.result                 = OK;
    }
    
    /**
     * @return returns a ProcessEvent which is fired, when the handshake is requested. Can be awaited by the acknowledging module
     */
    public ProcessEvent requested(){
        return requestedEvent;
    }

    /**
     * @return returns a ProcessEvent which is fired, when the handshake is acknowledged. Can be awaited by the requesting module
     */
    public ProcessEvent acknowledged(){
        return acknowledgedEvent;
    }
    
    /**
     * @return returns a ProcessEvent which is fired, when the handshake is acknowledged. Can be awaited by the requesting module
     */
    public ProcessEvent active(){
        return activeEvent;
    }

    /**
     * used by the requesting module to trigger a request to be handled by the acknowledging module
     * @throws SignalAccessException 
     */
    public void request() throws SignalAccessException, WrongUseException{
        if (requestingModule != null && !Thread.currentThread().equals(requestingModule)){
            throw new SignalAccessException("handshake is seized by " + requestingModule);
        }
        if (active.isConnectedAsTarget() || resultSig.isConnectedAsTarget() || acknowledge.isConnectedAsTarget() || request.isConnectedAsTarget()){
            throw new SignalAccessException("handshake cannot be requested directly, because it's acknowledge, active, resultSig or request signal is connected as a target ");            
        }
        JPac.getInstance().invokeLater(requestRunner);
        JPac.getInstance().invokeLater(resetAcknowledgeRunner);
    }

    /**
     * used by the requesting module to remove a pending request to abort it
     * HINT: the request will automatically be reset on every acknowledgement
     * @throws SignalAccessException 
     */
    public void resetRequest() throws SignalAccessException, WrongUseException{
        if (requestingModule != null && !Thread.currentThread().equals(requestingModule)){
            throw new SignalAccessException("handshake is seized by " + requestingModule);
        }
        if (active.isConnectedAsTarget() || resultSig.isConnectedAsTarget() || acknowledge.isConnectedAsTarget() || request.isConnectedAsTarget()){
            throw new SignalAccessException("request of handshake cannot be reset directly, because it's acknowledge, active, resultSig or request signal is connected as a target ");            
        }
        JPac.getInstance().invokeLater(resetRequestRunner);
        JPac.getInstance().invokeLater(resetAcknowledgeRunner);
    }    
    /**
     * used by the acknowledgeing module to indicate, that the processing of a request has begun
     * @throws SignalAccessException 
     */
    public void setActive() throws SignalAccessException{
        if (active.isConnectedAsTarget()){
            throw new SignalAccessException("handshake cannot be set active directly, because it's active signal is connected as a target ");            
        }        
        active.set(true);
    }

    /**
     * used by the acknowledging module to trigger an acknowledgement to be handled by the requesting module
     * @throws SignalAccessException 
     */
    public void acknowledge(int result) throws SignalAccessException, NumberOutOfRangeException{
        if (active.isConnectedAsTarget() || resultSig.isConnectedAsTarget() || acknowledge.isConnectedAsTarget() || request.isConnectedAsTarget()){
            throw new SignalAccessException("handshake cannot be acknowledged directly, because it's acknowledge, active, resultSig or request signal is connected as a target ");            
        }
        this.result = result;
        active.set(false);
        resultSig.set(result);
        acknowledge.set(true);
        JPac.getInstance().invokeLater(resetRequestRunner);
    }

    /**
     * used by the acknowledging module to trigger an acknowledgement to be handled by the requesting module
     * @throws SignalAccessException 
     */
    public void acknowledge() throws SignalAccessException, NumberOutOfRangeException{
        acknowledge(OK);
    }
        
    /**
     * @return returns true, if a request is pending, which is not acknoweldedged yet
     * @throws SignalInvalidException 
     */
    public boolean isRequested() throws SignalInvalidException{
        return request != null ? request.is(true) : false;
    }
    
    /**
     * @return returns true, if an acknowledgement is pending
     * @throws SignalInvalidException 
     */
    public boolean isAcknowledged() throws SignalInvalidException{
        return acknowledge.is(true);
    }    
    
    /**
     * @return returns true, if processing is pending
     * @throws SignalInvalidException 
     */
    public boolean isActive() throws SignalInvalidException{
        return active.is(true);
    }
    
    /**
     * used to seize the handshake. Can be done by a requesting module to make shure
     * that no other module can make make request over this handshake.
     * @param requestingModule
     * @throws WrongUseException
     * @throws SignalAlreadyExistsException 
     */
    public void seize(AbstractModule requestingModule) throws WrongUseException, SignalAlreadyExistsException{
        if (requestingModule == null){
            throw new NullPointerException("requesting module must not be 'null'");
        }
        if (this.requestingModule != null){
            throw new WrongUseException("requesting module already joined: " + this.requestingModule.getQualifiedName());
        }
        this.requestingModule = requestingModule;
    }

    @Override
    public String toString(){
        boolean requested;
        boolean acknowledged;
        String  status;
        try{
            requested    = isRequested();
            acknowledged = isAcknowledged();
            if (requested && !acknowledged){
                status = "REQUESTED";
            } else if (!requested && acknowledged){
                status = "ACKNOWLEDGED";
            } else {
                status = "TRANSIENT";
            }
        }
        catch(SignalInvalidException exc){
            status = "???";
        }
        return getClass().getSimpleName() + '(' + identifier + ' ' + requestingModule + " <-> " + acknowledgingModule + " status = " + status +')';
    }

    /**
     * @return the request
     */
    public Logical getRequest() {
        return request;
    }

    /**
     * @return the acknowledge
     */
    public Logical getAcknowledge() {
        return acknowledge;
    }

    /**
     * @return the active
     */
    public Logical getActive() {
        return active;
    }
    
    /**
     * @return the result 
     */
    public int getResult(){
        return result;
    }

    /**
     * @return the resultSig
     */
    public SignedInteger getResultSig() {
        return resultSig;
    }
    
    private class RequestEvent extends ProcessEvent{
        @Override
        public boolean fire() throws ProcessException {
            return request != null ? request.is(true) : false;
        }  
    }
        
    private class RequestRunner implements Runnable{
        @Override
        public void run() {
            try{
                request.set(true);
            }
            catch(SignalAccessException exc){/*cannot happen*/};
        }
    }

    private class ResetRequestRunner implements Runnable{
        @Override
        public void run() {
            try{
                request.set(false);
            }
            catch(SignalAccessException exc){/*cannot happen*/};
        }
    }

    private class ResetAcknowledgeRunner implements Runnable{
        @Override
        public void run() {
            try{
                result = OK;
                resultSig.set(OK);
                active.set(false);
                acknowledge.set(false);
            }
            catch(SignalAccessException exc){/*cannot happen*/}
            catch(NumberOutOfRangeException exc){/*cannot happen*/};
        }
    }
}
