/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : Alarm.java
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

package org.jpac.alarm;

import org.apache.log4j.Logger;
import org.jpac.AbstractModule;
import org.jpac.JPac;
import org.jpac.Logical;
import org.jpac.LogicalValue;
import org.jpac.Signal;
import org.jpac.SignalAccessException;
import org.jpac.SignalAlreadyExistsException;
import org.jpac.SignalInvalidException;


/**
 * represents an alarm which can be raised by 
 * - a module or 
 * - by a logical signal which is connected to the alarm as a source signal.
 * Alarms can be shown up to the user over the GUI and acknowledged by him, after they have gone. Alarms have an unique identifier 
 * and a message text, which can be part of a resource bundle to manage internationalization
 * @author berndschuster
 */
public class Alarm extends Signal{
    
    public enum Severity{ALARM('A'),WARNING('W'),MESSAGE('M');
        private final char id;
        private Severity(char id){
            this.id = id;
        }
        public static Severity getById(char id){
            Severity rs = null;
            for(Severity s: values()){
                if (s.id == id){
                    rs = s;
                    break;
                }
            }
            return rs;
        }
    };
    
    protected class AcknowledgedRunner implements Runnable{
        @Override
        public void run() {
            setAcknowlegded(true);
        }
    }
    
    static  Logger Log = Logger.getLogger("jpac.Alarm");    
    
    private LogicalValue       wrapperValue;
    private String             message;
    private LogicalValue       acknowledged;
    private LogicalValue       propagatedAcknowledged;
    private AcknowledgedRunner acknowledgedRunner;
    private boolean            resetOnAcknowledgement;
    private boolean            invertOnUpdate;   
    private Severity           severity;
    private Integer            number;
    
    /**
     * constructs an alarm
     * @param containingModule: module which instantiated the alarm
     * @param identifier: unique identifier of the alarm
     * @param number: a unique number of the alarm (optional, default = null)
     * @param message: a message, that can be displayed over a GUI
     * @param resetOnAcknowledgement: if true, the alarm will be automatically reset on acknowledgement by the user 
     * @param severity: severity of the alarm: ALARM, WARNING, MESSAGE (optional, default = Severity.MESSAGE)
     */
    public Alarm(AbstractModule containingModule, String identifier, Integer number, String message, boolean resetOnAcknowledgement, Severity severity) throws SignalAlreadyExistsException{
        super(containingModule, identifier);
        this.message                = message;
        this.number                 = number;
        this.value                  = new LogicalValue();
        this.propagatedValue        = new LogicalValue(); 
        this.acknowledged           = new LogicalValue();
        this.propagatedAcknowledged = new LogicalValue();
        this.wrapperValue           = new LogicalValue();
        this.resetOnAcknowledgement = resetOnAcknowledgement;
        this.invertOnUpdate         = false;
        this.severity               = severity;

        acknowledged.set(false);
        AlarmQueue.getInstance().register(this);
    }


    /**
     * constructs an alarm with default severity MESSAGE
     * @param containingModule: module which instantiated the alarm
     * @param identifier: identifier of the signal
     * @param message: a message, that can be displayed over a GUI
     * @param resetOnAcknowledgement: if true, the alarm will be automatically reset on acknowledgement by the user
     * @param severity: severity of the alarm: ALARM, WARNING, MESSAGE (optional, default = Severity.MESSAGE)
     */
    public Alarm(AbstractModule containingModule, String identifier, String message, boolean resetOnAcknowledgement, Severity severity) throws SignalAlreadyExistsException{
        this(containingModule, identifier, null, message, resetOnAcknowledgement, severity);
    }

    /**
     * constructs an alarm with default severity MESSAGE
     * @param containingModule: module which instantiated the alarm
     * @param identifier: identifier of the signal
     * @param message: a message, that can be displayed over a GUI
     * @param resetOnAcknowledgement: if true, the alarm will be automatically reset on acknowledgement by the user
     */
    public Alarm(AbstractModule containingModule, String identifier, String message, boolean resetOnAcknowledgement) throws SignalAlreadyExistsException{
        this(containingModule, identifier, null, message, resetOnAcknowledgement, Severity.MESSAGE);
    }
    
    /**
     * used to set/reset the alarm. Whenever the alarm condition is set 
     * the acknowledge state of the alarm is reset.
     * @throws SignalAccessException
     */
    public void set(boolean state) throws SignalAccessException{
        boolean wasValidBefore = isValid();
        boolean lastState      = false;
        if (wasValidBefore){
            lastState = ((LogicalValue)getValue()).get();
        }
        if (Log.isDebugEnabled()) Log.debug(this + ".set(" + state + ")");
        wrapperValue.set(state);
        setValue(wrapperValue);
        if (state){
           setAcknowlegded(false);            
        }
        if (state && (!wasValidBefore || (wasValidBefore && !lastState))){
           //transition from invalid to true, or false to true
           AlarmQueue.getInstance().incrementPendingAlarmsCount(severity);
        }
        else if (!state && wasValidBefore && lastState){
           //transition from true to false
           AlarmQueue.getInstance().decrementPendingAlarmsCount(severity);            
        }
    }

    /**
     * used to raise the alarm. Equivalent to set(true). Whenever the alarm condition is set 
     * the acknowledge state of the alarm is reset.
     * @throws SignalAccessException
     */
    public void raise() throws SignalAccessException{
        set(true);
    }

    /**
     * used to reset the alarm.  Equivalent to set(false). 
     * @throws SignalAccessException
     */
    public void reset() throws SignalAccessException{
        set(false);
    }

    /**
     * @return true, if the alarm is pending.
     */
    public boolean isPending() throws SignalInvalidException{
        boolean pending = false;
        if (Thread.currentThread() == getContainingModule() || Thread.currentThread() == jPac){
            pending = ((LogicalValue)getValue()).is(true);
        }
        else{
            pending = ((LogicalValue)getPropagatedValue()).is(true);
        }
        return pending;
    }
    
    /**
     * @return true, if the alarm is acknowledged.
     */
    public boolean isAcknowledged(){
        boolean state = false;
        if (Thread.currentThread() == getContainingModule() || Thread.currentThread() == jPac){
            state = acknowledged.is(true);
        }
        else{
            state = propagatedAcknowledged.is(true);            
        }
        return state;
    }
    
    /**
     * used to acknowledge a pending alarm.
     * @throws AlarmPendingException thrown, if the alarm is not pending and instantiated with resetOnAcknowledgement = false
     */
    public void acknowledge() throws AlarmPendingException{
        boolean allowed = false;
        if (Log.isDebugEnabled()) Log.debug(this + ".acknowledge()");        
        try{allowed = (isValid() && !isPending()) || resetOnAcknowledgement;}catch(SignalInvalidException exc){/*cannot happen*/};
        if (!allowed){
            throw new AlarmPendingException(this);
        }
        if (Thread.currentThread() == getContainingModule() || Thread.currentThread() == jPac){
            setAcknowlegded(true);
        }
        else{
            //setting acknowledged flag deferred.
            JPac.getInstance().invokeLater(getAcknowledgedRunner());
        }
    }
    
    private void setAcknowlegded(boolean state){
        wrapperValue.set(state);
        if (!this.acknowledged.equals(wrapperValue)){
            this.acknowledged.copy(wrapperValue);
            setChanged();
            if (resetOnAcknowledgement && state){
                try{reset();}catch(SignalAccessException exc){/*cannot happen*/};
            }
        }
    }
    
    protected AcknowledgedRunner getAcknowledgedRunner(){
        if (acknowledgedRunner == null){
            acknowledgedRunner = new AcknowledgedRunner();
        }
        return acknowledgedRunner;
    }

    @Override
    protected boolean isCompatibleSignal(Signal signal) {
        return signal instanceof Logical;
    }

    @Override
    protected void updateValue(Object o, Object arg) throws SignalAccessException {
        //((LogicalValue)getValue()).copy(((LogicalValue)((Logical)o).getValue()));
        try{
            set(((Logical)o).is(invertOnUpdate ? false: true));
        }
        catch(Exception exc){
            Log.error("Error: ", exc);
            throw new SignalAccessException(exc.getMessage());
        }        
    }

    @Override
    protected void propagateSignalInternally() throws SignalInvalidException {
        //physically copy the value to the propagated value
        ((LogicalValue)getPropagatedValue()).copy((LogicalValue)getValue());
        propagatedAcknowledged.copy(acknowledged);
    }
    
    /**
     * @return process event indicatimg, that an alarm has gone 
     */
    public AlarmGone gone(){
        return new AlarmGone(this);        
    }

    /**
     * @return process event indicatimg, that an alarm has been acknowledged 
     */
    public AlarmAcknowledged acknowledged(){
        return new AlarmAcknowledged(this);        
    }
    
    /**
     * @return process event indicatimg, that an alarm has been raised 
     */
    public AlarmRaised raised(){
        return new AlarmRaised(this);        
    }
    
    @Override
    public String toString(){
        return getClass().getSimpleName() + "(" + containingModule.getName() + '.' + getIdentifier() + " = " + (isValid() ? getValue() : "???") + ", ack = " + acknowledged.is(true) + ")";
    }    
    
    /**
     * if the alarm is connected to a logical source signal, the state of the signal is inverted before transfer to the alarm 
     * @param invert 
     */
    public void setInvertOnUpdate(boolean invert){
        this.invertOnUpdate = invert;
    }
    
    /**
     * @return the message of the alarm
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return the severity
     */
    public Severity getSeverity() {
        return severity;
    }
    
    /**
     * @return the number
     */
    public Integer getNumber() {
        return number;
    }

    /**
     * @param number the number to set
     */
    public void setNumber(Integer number) {
        this.number = number;
    }    
}
