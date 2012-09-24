/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : Clamp.java
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
 * LOG       : $Log: Clamp.java,v $
 * LOG       : Revision 1.6  2012/06/18 11:20:53  schuster
 * LOG       : introducing cyclic tasks
 * LOG       :
 * LOG       : Revision 1.5  2012/05/07 06:16:47  schuster
 * LOG       : some adaptions concerning update of AbstractModule
 * LOG       :
 * LOG       : Revision 1.4  2012/04/30 06:36:05  schuster
 * LOG       : introducing histogramm acquisition, some minor changes concerning toString()
 * LOG       :
 * LOG       : Revision 1.3  2012/04/24 06:37:08  schuster
 * LOG       : some improvements concerning consistency
 * LOG       :
 * LOG       : Revision 1.2  2012/03/05 07:23:10  schuster
 * LOG       : introducing Properties
 * LOG       :
 */
package jpac.test.fg;

import org.jpac.AbstractModule;
import org.jpac.Decimal;
import org.jpac.EmergencyStopAcknowledged;
import org.jpac.EmergencyStopException;
import org.jpac.ImpossibleEvent;
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
        boolean done       = false;
        boolean steppingUp = true;
        double  value = MINVALUE;
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
                       if (value > MAXVALUE){
                           value = MAXVALUE;
                       }
                       if (value < MINVALUE){
                           value = MINVALUE;
                       }
                       getAnalogOutput().set(value);
                       //try{Thread.sleep(30);}catch(Exception ex){};
                       getAnalogInput().changes(value, 0.1).await();
                       //Log.info("clamp work() accessTest: " + accessTestDecimal.get());
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
                    catch (EmergencyStopException ex) {
                        Log.error("Error", ex);
                        //input signals have gone invalid
                        //invalidate own output signals
                        value = MINVALUE;
                        getAnalogOutput().invalidate();
                        EmergencyStopAcknowledged esa = new EmergencyStopAcknowledged();
                        //wait for reinvocation
                        esa.await();
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
        //throw new UnsupportedOperationException("Not supported yet.");
        if (cnt++ == 20){
            //shutdown(99);
            //new ImpossibleEvent().await();
            //throw new EmergencyStopException("clamp cycle failed !!!");
            //accessTestDecimal.get();
//            int i = 0;
//            int j = 1000/i;
        }
//        accessTestDecimal.set(cnt++);
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
