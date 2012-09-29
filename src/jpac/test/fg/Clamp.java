/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : Clamp.java
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
import org.jpac.Decimal;
import org.jpac.InputInterlockException;
import org.jpac.Module;
import org.jpac.OutputInterlockException;
import org.jpac.ProcessEvent;
import org.jpac.ProcessException;
import org.jpac.SignalInvalidException;

/**
 *
 * @author berndschuster
 */
public class Clamp extends Module{
    final private double MAXVALUE  = 0.6;
    final private double MINVALUE  = 0.2;
    private int cnt = 0;

    private class InputSignalsValid extends ProcessEvent{

        @Override
        public boolean fire() {
            return allInputSignalsValid();
        }
    }


    private Decimal analogInput;   //input
    private Decimal analogOutput;  //input
    public  Decimal accessTestDecimal;

    public Clamp(AbstractModule containingModule){
        super(containingModule);
        initialize();
    }

    public Clamp(AbstractModule containingModule, String name){
        super(containingModule, name);
        initialize();
    }

    private void initialize(){
        analogInput = new Decimal(this, "analogIn");
        analogOutput = new Decimal(this, "analogOutput",MINVALUE,MAXVALUE);
        accessTestDecimal = new Decimal(this,"accessTestDecimal");
    }

    @Override
    protected void work() throws ProcessException{
        boolean done         = false;
        double  value        = 0.0;
        double  clampedValue = 0.0;
        InputSignalsValid inputSignalsValid = new InputSignalsValid();
        try{
            do{
                //wait, until input signal become valid
                inputSignalsValid.await();
                enableCyclicTasks(true);
                done = false;
                status.enter("processing");
                do{
                   try{
                       value = getAnalogInput().get();
                       clampedValue = value;
                       if (value > MAXVALUE){
                           clampedValue = MAXVALUE;
                       }
                       if (value < MINVALUE){
                           clampedValue = MINVALUE;
                       }
                       getAnalogOutput().set(clampedValue);
                       Log.info("clamp analog output: " + getAnalogOutput().get());
                       getAnalogInput().changes(value, 0.1).await();
                    }
                    catch(SignalInvalidException ex){
                        Log.error("Error", ex);
                        //input signals have gone invalid
                        //invalidate own output signals
                        value = MINVALUE;
                        getAnalogOutput().invalidate();
                        //repeat waiting for revalidation
                        done = true;
                    }
                  }
                while(!done);
                status.leave();
              }
            while(true);
           }
        finally{
            //module will be shut down
            //invalidate own output signals
            value = MINVALUE;
            status.resume(0);
            getAnalogOutput().invalidate();
        }
    }

    @Override
    protected void inEveryCycleDo() throws ProcessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private boolean allInputSignalsValid(){
        return getAnalogInput().isValid();
    }

    @Override
    public void preCheckInterlocks() throws InputInterlockException {
        //nothing to check
    }

    @Override
    public void postCheckInterlocks() throws OutputInterlockException {
        //nothing to check
    }

    /**
     * @return the analogOutput
     */
    public Decimal getAnalogOutput() {
        return analogOutput;
    }

    /**
     * @return the analogInput
     */
    public Decimal getAnalogInput() {
        return analogInput;
    }
}
