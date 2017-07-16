/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : FunctionGenerator.java
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

import org.jpac.Logical;
import org.jpac.Decimal;
import org.jpac.EmergencyStopException;
import org.jpac.InputInterlockException;
import org.jpac.Module;
import org.jpac.OutputInterlockException;
import org.jpac.PeriodOfTime;
import org.jpac.ProcessException;
import org.jpac.RemoteSignalOutput;

/**
 *
 * @author berndschuster
 */
public class FunctionGenerator extends Module{

    private JigSawGenerator jsg;
    private Clamp           clamp;
    private Inverter        inverter;

    private Logical         jsgEnable;
    private Logical         jsgReset;

    private Decimal         signalOutput;
    private Decimal         invertedSignalOutput;
    private Decimal         clampedSignalOutput;
        
    private int                cnt;

    public FunctionGenerator(){
        super(null);
        initialize();
    }

    public FunctionGenerator(String name){
        super(null, name);
        initialize();
    }

    private void initialize(){
        try{
            //the function generator can enabled and reset by another module ...
            //instantiate 2 signals accordingly
            jsgEnable       = new Logical(this, "enable");
            jsgReset        = new Logical(this, "reset");
            //the function generator consists of a jig saw generator ....
            jsg             = new JigSawGenerator(this, "JigSawGenerator",jsgEnable, jsgReset);
            //a clamp unit ...
            clamp           = new Clamp(this, "ClampingUnit");
            //... and an analog  inverter
            inverter        = new Inverter(this, "AnalogInverter");

            //the function generator provides 3 output signals:
            //the output for the jig saw signal
            signalOutput            = new Decimal(this, "SignalOutput",-100,100);
            //an output for a clamped jig saw signal
            clampedSignalOutput     = new Decimal(this, "ClampedOutput",-100,100);
            //and an output which provides the inverted signal of "signalOutput"
            invertedSignalOutput    = new Decimal(this, "InvertedOutput",-100,100);
        
            //connect the analog output of the jig saw generator to the analog input of the clamp unit
            jsg.getAnalogOutput().connect(clamp.getAnalogInput());
            //connect the analog output of the jig saw generator to the analog input of the analog inverter
            jsg.getAnalogOutput().connect(inverter.getAnalogInput());
            //connect the analog output of the clamp unit to the corresponding analog output of the function generator
            clamp.getAnalogOutput().connect(clampedSignalOutput);
            //connect the analog output of the jig saw generator to the analog output of the function generator
            jsg.getAnalogOutput().connect(signalOutput);
            //connect the analog output of the inverter to the corresponding analog output of the function generator
            inverter.getAnalogOutput().connect(invertedSignalOutput);            
        }
        catch(Exception exc){
            Log.error("Error: ", exc);
        }
    }

    @Override
    protected void work() throws ProcessException{
        PeriodOfTime pot = new PeriodOfTime(4 * sec);
        try{
            //enable the jig saw generator
            jsgEnable.set(true);
            jsgReset.set(false);

            int i = 0;
            //for 10 times do nothing very meaningful ....
            do{
                //wait a period of time
                pot.await();
                //Log.info("function generator : " + i);
                i++;
            }
            while(i < 100);
            shutdown(0);
            //throw new EmergencyStopException("stopped by function generator");
        }
        finally{
            Log.debug("shutting down : ");
        }
    }
    
    @Override
    public void start(){
        //start the containing modules...
        jsg.start();
        inverter.start();
        clamp.start();
        //then start myself
        super.start();
    }

    @Override
    public void preCheckInterlocks() throws InputInterlockException {
    }

    @Override
    public void postCheckInterlocks() throws OutputInterlockException {
    }

    @Override
    protected void inEveryCycleDo() throws ProcessException {
        //nothing to do for the function generator in every cycle
    }

}
