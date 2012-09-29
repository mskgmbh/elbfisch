/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : JigSawGenerator.java
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

package jpac.test.fg;

import org.jpac.AbstractModule;
import org.jpac.Logical;
import org.jpac.Decimal;
import org.jpac.Fireable;
import org.jpac.InputInterlockException;
import org.jpac.Module;
import org.jpac.MonitorException;
import org.jpac.OutputInterlockException;
import org.jpac.PeriodOfTime;
import org.jpac.ProcessEvent;
import org.jpac.ProcessException;
import org.jpac.ShutdownRequestException;

/**
 *
 * @author berndschuster
 */
public class JigSawGenerator extends Module{
    final private double MAXVALUE  = 1.0;
    final private double MINVALUE  = 0.0;
    final private double INCREMENT = 0.01;

    //user defined process event. Will be fired, when all
    //input signal went valid
    private class InputSignalsValid extends ProcessEvent{
        @Override
        public boolean fire() {
            return allInputSignalsValid();
        }
    }
    
    //user defined process event. Will be fired, when
    //the enable input is set to false, or the reset
    //signal is activated by setting it to true or 
    //both went invalid
    private class MonitorEvent extends ProcessEvent{
        @Override
        public boolean fire() throws ProcessException {
            boolean fire = !(getEnable().isValid() && getReset().isValid()) || getEnable().is(false) || getReset().is(true);
            return fire;
        }

        @Override
        protected boolean equalsCondition(Fireable fireable){
           return fireable instanceof MonitorEvent; 
        }
    }


    private Logical     enable;         //input
    private Logical     reset;          //input
    private Logical     valid;          //output
    private Decimal     analogOutput;   //output

    //constructor receiving too input signals from the containing module
    public JigSawGenerator(AbstractModule containingModule, String name, Logical enable, Logical reset){
        super(containingModule, name);
        this.enable = enable;
        this.reset = reset;
        initialize();
    }

    private void initialize(){
        //instantiate own output signals
        valid        = new Logical(this, "valid");
        analogOutput = new Decimal(this, "analogOutput",MINVALUE,MAXVALUE);
    }

    @Override
    protected void work() throws ProcessException{
        boolean done                        = false;
        boolean steppingUp                  = true;
        double  value                       = MINVALUE;
        InputSignalsValid inputSignalsValid = new InputSignalsValid();
        MonitorEvent monitorEvent           = null;

        PeriodOfTime stepTime = new PeriodOfTime(500*ms);
        try{
            do{ //wait until the containing module initializes the input
                //signals (making them valid)
                System.out.println("JSG: awaiting inputSignalsValid ...");
                inputSignalsValid.await();
                System.out.println("JSG: inputSignalsValid ");
                //inputs signals went valid, initialize own output signals
                //making them valid, too
                done = false;
                value = 0.0;
                analogOutput.set(value);
                valid.set(true);
                //wait, until the containing module enables the jig saw generation
                getEnable().state(true).await();
                //... and start jig saw generation
                //until the enable bit is removed
                status.enter("processing");
                //instantiate an event, which supervises the enable/reset inputs and monitor it
                monitorEvent = new MonitorEvent();
                monitorEvent.monitor();
                do{
                   try{//put out the actual value to the analog output
                       analogOutput.set(value);
                       Log.info("jsg analog output: " + analogOutput.get());
                       //prepare value for next cycle
                       if (steppingUp){
                           value += INCREMENT;
                           if (value > MAXVALUE){
                               value = MAXVALUE;
                               steppingUp = false;
                           }
                       }
                       else{
                           value -= INCREMENT;
                           if (value < MINVALUE){
                               value = MINVALUE;
                               steppingUp = true;
                           }
                       }
                       //wait a period of time
                       stepTime.await();
                   }
                   catch(MonitorException ex){
                       Log.error("monitor event occured: ", ex);
                       if (!(getEnable().isValid() && getReset().isValid()) || getReset().is(true)){
                           //if the enable or reset input went invalid or the reset input has been set to true
                           //invalidate own output signals...
                           value = 0.0;
                           analogOutput.invalidate();
                           valid.invalidate();
                           //... and stop processing, until they went valid again
                           done = true;
                       } else if (getEnable().is(false)){
                           //if the enable input went to false
                           //force the output to zero
                           value = 0.0;
                           analogOutput.set(value);
                           //... and stop processing, until it is enabled again
                           done = true;
                       }
                   }
                  }
                while(!done);
                status.leave();
              }
            while(true);
        }
        catch(ShutdownRequestException exc){
            Log.info("shutdown requested by other module. jig saw generator stops processing");
        }
        finally{
            //module will be shut down
            //invalidate own output signals
            value = 0.0;
            analogOutput.invalidate();
            valid.invalidate();
        }
    }

    @Override
    protected void inEveryCycleDo() throws ProcessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private boolean allInputSignalsValid(){
        return getReset().isValid() && getEnable().isValid();
    }

    @Override
    public void preCheckInterlocks() throws InputInterlockException {
        //nothing to check
    }

    @Override
    public void postCheckInterlocks() throws OutputInterlockException {
        try{
            if (analogOutput.isValid() && valid.isValid()){
                if (analogOutput.get() != 0.0 && valid.is(false)){
                    OutputInterlockException oe = new OutputInterlockException();
                    oe.add(analogOutput);
                    oe.add(valid);
                    throw oe;
                }
            }
        }
        catch(Exception exc){
            Log.error("Error: ",exc);
        }
    }


    /**
     * @return the valid
     */
    public Logical getValid() {
        return valid;
    }

    /**
     * @return the analogOutput
     */
    public Decimal getAnalogOutput() {
        return analogOutput;
    }

    /**
     * @return the enable
     */
    public Logical getEnable() {
        return enable;
    }

    /**
     * @return the reset
     */
    public Logical getReset() {
        return reset;
    }

    /**
     * @param enable the enable to set
     */
    public void setEnable(Logical enable) {
        this.enable = enable;
    }

    /**
     * @param reset the reset to set
     */
    public void setReset(Logical reset) {
        this.reset = reset;
    }

}
