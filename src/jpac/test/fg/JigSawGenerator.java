/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : JigSawGenerator.java
 * VERSION   : $Revision: 1.6 $
 * DATE      : $Date: 2012/06/18 11:20:53 $
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
 *
 * LOG       : $Log: JigSawGenerator.java,v $
 * LOG       : Revision 1.6  2012/06/18 11:20:53  schuster
 * LOG       : introducing cyclic tasks
 * LOG       :
 * LOG       : Revision 1.5  2012/05/07 06:16:47  schuster
 * LOG       : some adaptions concerning update of AbstractModule
 * LOG       :
 * LOG       : Revision 1.4  2012/04/24 06:37:08  schuster
 * LOG       : some improvements concerning consistency
 * LOG       :
 * LOG       : Revision 1.3  2012/03/09 10:30:28  schuster
 * LOG       : Firable.fire(), Fireable.reset() made public
 * LOG       :
 * LOG       : Revision 1.2  2012/03/05 07:23:10  schuster
 * LOG       : introducing Properties
 * LOG       :
 */

package jpac.test.fg;

import org.jpac.AbstractModule;
import org.jpac.Logical;
import org.jpac.Decimal;
import org.jpac.EmergencyStopException;
import org.jpac.Fireable;
import org.jpac.InputInterlockException;
import org.jpac.Module;
import org.jpac.MonitorException;
import org.jpac.OutputInterlockException;
import org.jpac.PeriodOfTime;
import org.jpac.ProcessEvent;
import org.jpac.ProcessException;
import org.jpac.SignalInvalidException;

/**
 *
 * @author berndschuster
 */
public class JigSawGenerator extends Module{
    final private double MAXVALUE  = 1.0;
    final private double MINVALUE  = 0.0;
    final private double INCREMENT = 0.01;

    private class InputSignalsValid extends ProcessEvent{

        @Override
        public boolean fire() {
            return allInputSignalsValid();
        }
    }
    
    private class MonitorEvent extends ProcessEvent{

        @Override
        public boolean fire() throws ProcessException {
            boolean fire = getEnable().is(false) || getReset().is(true);
            Log.debug("MonitorEvent.fire = " + fire );
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
    private Decimal analogOutput;   //output

    public JigSawGenerator(AbstractModule containingModule, Logical enable, Logical reset){
        super(containingModule);
        this.enable = enable;
        this.reset = reset;
        initialize();
    }

    public JigSawGenerator(AbstractModule containingModule, String name, Logical enable, Logical reset){
        super(containingModule, name);
        this.enable = enable;
        this.reset = reset;
        initialize();
    }

    private void initialize(){
//        enable       = new Logical(this, "enable");
//        reset        = new Logical(this, "reset");
        valid        = new Logical(this, "valid");
        analogOutput = new Decimal(this, "analogOutput",MINVALUE,MAXVALUE);


    }

    @Override
    protected void work() throws ProcessException{
        boolean done       = false;
        boolean steppingUp = true;
        double  value      = MINVALUE;
        InputSignalsValid inputSignalsValid = new InputSignalsValid();
        Fireable monitorEvent = null;

        PeriodOfTime stepTime = new PeriodOfTime(500*ms);
        try{
            do{
                System.out.println("JSG: awaiting inputSignalsValid ...");
                inputSignalsValid.await();
                System.out.println("JSG: inputSignalsValid ");
                done = false;
                value = 0.0;
                analogOutput.set(value);
                valid.set(true);
                getEnable().state(true).await();
                //... and start jig saw generation
                //until the enable bit is removed
                status.enter("processing");
                monitorEvent = new MonitorEvent();
                monitorEvent.monitor();
                try{
                    do{
                       try{
                           analogOutput.set(value);
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
                           stepTime.await();
                       }
                       catch(MonitorException exc){
                            Log.error("Error 1: ", exc);
                            monitorEvent.unmonitor();
                            throw new EmergencyStopException("MonitorException thrown : " + exc);
                       }
                       catch(SignalInvalidException ex){
                           Log.error("error: ", ex);
                           //input signals have gone invalid
                           //invalidate own output signals
                           value = 0.0;
                           analogOutput.invalidate();
                           valid.invalidate();
                           //repeat waiting for revalidation
                           done = true;
                       }
                      }
                    while(!done);
                }
                catch (EmergencyStopException ex) {
                   Log.error("Error 2", ex);
                   //invalidate own output signals
                   value = 0.0;
                   analogOutput.invalidate();
                   valid.invalidate();
//                   EmergencyStopAcknowledged esa = new EmergencyStopAcknowledged();
//                   esa.await();
//                   try{
//                       (new NextCycle()).await();
//                   }
//                   catch(EmergencyStopException exc){
//                       //catch emergency stop exception ...+
//                       Log.error("Error 3", ex);
//                   }
                   //... wait a while and acknowledge it
                   PeriodOfTime pot = new PeriodOfTime(2000 * ms);
                   pot.await();
                   //acknowledge the emergency stop
                   acknowledgeEmergencyStop();
                }
                status.leave();
              }
            while(true);
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
