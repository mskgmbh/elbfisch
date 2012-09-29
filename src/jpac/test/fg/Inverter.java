/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : Inverter.java
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
import org.jpac.EmergencyStopAcknowledged;
import org.jpac.EmergencyStopException;
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
public class Inverter extends Module{

    private class InputSignalsValid extends ProcessEvent{

        @Override
        public boolean fire() throws ProcessException{
            return getAnalogInput().isValid();
        }
    }
    
    private Decimal analogInput;   //input
    private Decimal analogOutput;   //input

    public Inverter(AbstractModule containingModule){
        super(containingModule);
        initialize();
    }

    public Inverter(AbstractModule containingModule, String name){
        super(containingModule, name);
        initialize();
    }

    private void initialize(){
        analogInput = new Decimal(this, "analogIn");
        analogOutput = new Decimal(this, "analogOutput");
    }

    @Override
    protected void work() throws ProcessException{
        boolean done       = false;
        InputSignalsValid inputSignalsValid = new InputSignalsValid();

        try {
            do{
                status.enter("processing");
                try{
                    //wait, until all input signal become valid
                    inputSignalsValid.await();
                    done = false;
                    do{
                        try{
                            do{
                               double value = getAnalogInput().get();
                               getAnalogOutput().set( -value);
                               Log.info("Inverter output: " + getAnalogOutput().get());
                               getAnalogInput().changes(value, 0.1).await();
                            }
                            while(true);
                        }
                        catch(SignalInvalidException ex){
                            //input signals have gone invalid
                            //invalidate own output signals
                            getAnalogOutput().invalidate();
                            done = true;
                        }
                      }
                    while(!done);
                }
                catch(EmergencyStopException ex){
                   Log.error("Error", ex);
                   //invalidate own output signals
                   analogOutput.invalidate();
                   EmergencyStopAcknowledged esa = new EmergencyStopAcknowledged();
                   esa.await();             
                }
                status.leave();
            }
            while(true);
        }
        finally{
            //module will be shut down
            //invalidate own output signals
            getAnalogOutput().invalidate();
            status.resume(0);
        }
    }

    @Override
    protected void inEveryCycleDo() throws ProcessException {
        throw new UnsupportedOperationException("Not supported yet.");
        //Log.info("inverter cyclic inverter");
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
