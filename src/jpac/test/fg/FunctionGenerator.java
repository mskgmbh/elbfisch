/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : FunctionGenerator.java
 * VERSION   : $Revision: 1.5 $
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
 * LOG       : $Log: FunctionGenerator.java,v $
 * LOG       : Revision 1.5  2012/06/18 11:20:53  schuster
 * LOG       : introducing cyclic tasks
 * LOG       :
 * LOG       : Revision 1.4  2012/05/07 06:16:47  schuster
 * LOG       : some adaptions concerning update of AbstractModule
 * LOG       :
 * LOG       : Revision 1.3  2012/04/24 06:37:08  schuster
 * LOG       : some improvements concerning consistency
 * LOG       :
 * LOG       : Revision 1.2  2012/03/05 07:23:10  schuster
 * LOG       : introducing Properties
 * LOG       :
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
    
    private RemoteSignalOutput remoteSignal;
    private RemoteSignalOutput remoteInvertedSignal;
    private RemoteSignalOutput remoteClampedSignal;
    
    private int                cnt;

//    private Plug            channel1;
//    private Plug            channel2;
//    private Plug            channel3;
//
//
    public FunctionGenerator(){
        super(null);
        initialize();
    }

    public FunctionGenerator(String name){
        super(null, name);
        initialize();
    }

    private void initialize(){
        jsgEnable       = new Logical(this, "enable");
        jsgReset        = new Logical(this, "reset");
        jsg             = new JigSawGenerator(this, "JigSawGenerator",jsgEnable, jsgReset);
        clamp           = new Clamp(this, "ClampingUnit");
        inverter        = new Inverter(this, "AnalogInverter");

        signalOutput            = new Decimal(this, "SignalOutput",-100,100);
        clampedSignalOutput     = new Decimal(this, "ClampedOutput",-100,100);
        invertedSignalOutput    = new Decimal(this, "InvertedOutput",-100,100);
        

//        channel1 = new Plug(this, "Channel1");
//        channel2 = new Plug(this, "Channel2");
//        channel3 = new Plug(this, "Channel3");
        try{
            remoteSignal          = new RemoteSignalOutput("remoteSignal","localhost",10003,"RemoteJPacTest.SignalInput");
            remoteInvertedSignal  = new RemoteSignalOutput("remoteInvertedSignal","localhost",10003,"RemoteJPacTest.InvertedInput");
            remoteClampedSignal   = new RemoteSignalOutput("remoteClampedSignal","localhost",10003,"RemoteJPacTest.ClampedInput");        
            
            jsg.getAnalogOutput().connect(clamp.getAnalogInput());
            jsg.getAnalogOutput().connect(inverter.getAnalogInput());
            clamp.getAnalogOutput().connect(clampedSignalOutput);
            jsg.getAnalogOutput().connect(signalOutput);
            inverter.getAnalogOutput().connect(invertedSignalOutput);
            
            jsg.getAnalogOutput().connect(remoteSignal);
            inverter.getAnalogOutput().connect(remoteInvertedSignal);
            clamp.getAnalogOutput().connect(remoteClampedSignal);
            
//            channel1.addPin(0,true).assign(jsg.getAnalogOutput());
//            channel1.addPin(1,true).assign(clamp.getAnalogOutput());
//            channel1.addPin(2,true).assign(inverter.getAnalogOutput());
//
//            channel2.addPin(0,true).assign(jsg.getAnalogOutput());
//            channel2.addPin(1,true).assign(clamp.getAnalogOutput());
//            channel2.addPin(2,true).assign(inverter.getAnalogOutput());
//
//            channel3.addPin(0,true).assign(jsg.getAnalogOutput());
//            channel3.addPin(1,true).assign(clamp.getAnalogOutput());
//            channel3.addPin(2,true).assign(inverter.getAnalogOutput());
        }
        catch(Exception exc){
            Log.error("Error: ", exc);
        }
    }

    @Override
    protected void work() throws ProcessException{
        PeriodOfTime pot = new PeriodOfTime(4000 * ms);
        try{
            jsgEnable.set(true);
            jsgReset.set(false);

            int i = 0;
            do{
                try{
                    pot.await();
                    Log.info("function generator : " + i);
//                    Log.info("clamp.accessTestDecimal : " + clamp.accessTestDecimal.get());
//                    clamp.accessTestDecimal.set(1234.567);
                    i++;
//                    if (i == 3 || i == 6){
//                        //force an emergency stop exception
//                        jsgEnable.set(false);
//                    }
                }
                catch(EmergencyStopException ex){
                    Log.error("ignoreing emergency stop exception: " + i);
                    jsgEnable.set(true);
                }
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
        jsg.start();
        inverter.start();
        clamp.start();
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
        //throw new UnsupportedOperationException("Not supported yet.");
        //Log.info("function generator cyclic");
//        if (cnt++ == 20){
//           clamp.accessTestDecimal.set(1234.567);
//        }
    }

}
