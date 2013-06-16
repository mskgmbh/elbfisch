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
 *
 * @author berndschuster
 */
public class Handshake {
    static    Logger Log = Logger.getLogger("jpac.Handshake");
    
    private Logical                request;
    private Logical                acknowledge;
    private ProcessEvent           requestedEvent;
    private ProcessEvent           acknowledgedEvent;
    private AbstractModule         requestingModule;
    private AbstractModule         acknowledgingModule;
    private String                 identifier;
    private long                   lastRequestCycle;
    private long                   lastAcknowledgeCycle;
    private ResetRequestRunner     resetRequestRunner;
    private ResetAcknowledgeRunner resetAcknowledgeRunner;
    
    /**
     * implements a handshake mechanism to synchronize two modules which have a producer/consumer relationship
     * @param requestingModule the requesting module (producer)
     * @param acknowledgingModule the acknowledging module (consumer)
     * @param identifier the identifier of this handshake (Corresponds to the identifiers of signals).
     */
    public Handshake(AbstractModule requestingModule, AbstractModule acknowledgingModule, String identifier) throws SignalAlreadyExistsException{
        this.requestingModule       = requestingModule;
        this.acknowledgingModule    = acknowledgingModule;
        this.identifier             = identifier;
        this.request                = new Logical(requestingModule, identifier + ".request", false);
        this.acknowledge            = new Logical(acknowledgingModule, identifier + ".acknowledge", false);
        this.requestedEvent         = request.becomes(true);
        this.acknowledgedEvent      = acknowledge.becomes(true);
        this.lastRequestCycle       = 0L;
        this.lastAcknowledgeCycle   = 0L;
        this.resetRequestRunner     = new ResetRequestRunner();
        this.resetAcknowledgeRunner = new ResetAcknowledgeRunner();
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
     * used by the requesting module to trigger a request to be handled by the acknowledging module
     * @throws SignalAccessException 
     */
    public void request() throws SignalAccessException{
        request.set(true);
        JPac.getInstance().invokeLater(resetAcknowledgeRunner);
    }
    
    /**
     * used by the acknowledging module to trigger an acknowledgement to be handled by the requesting module
     * @throws SignalAccessException 
     */
    public void acknowledge() throws SignalAccessException{
        acknowledge.set(true);
        JPac.getInstance().invokeLater(resetRequestRunner);
    }
        
    /**
     * @return returns true, if a request is pending, which is not acknoweldedged yet
     * @throws SignalInvalidException 
     */
    public boolean isRequested() throws SignalInvalidException{
        boolean result;
        if (Thread.currentThread().equals(requestingModule)){
           result = request.is(true); 
        }
        else{
           result = request.is(true) && acknowledge.is(false);
        }
        return result;
    }
    
    /**
     * @return returns true, if an acknowledgement is pending
     * @throws SignalInvalidException 
     */
    public boolean isAcknowledged() throws SignalInvalidException{
        boolean result;
        if (Thread.currentThread().equals(acknowledgingModule)){
           result = acknowledge.is(true); 
        }
        else{
           result = request.is(false) && acknowledge.is(true);
        }
        return result;
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
        return getClass().getSimpleName() + '(' + identifier + ' ' + requestingModule.getName() + " <-> " + acknowledgingModule.getName() + " status = " + status +')';
    }
        
    private class ResetRequestRunner implements Runnable{
        @Override
        public void run() {
            try{request.set(false);}catch(SignalAccessException exc){/*cannot happen*/};
        }
    }

    private class ResetAcknowledgeRunner implements Runnable{
        @Override
        public void run() {
            try{acknowledge.set(false);}catch(SignalAccessException exc){/*cannot happen*/};
        }
    }
}
