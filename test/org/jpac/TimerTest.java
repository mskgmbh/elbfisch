/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : TimerTest.java
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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author berndschuster
 */
public class TimerTest {
    
    class TimerTestModule extends Module{
        public long rt1,rt2;
        
        public TimerTestModule(AbstractModule m){
            super(m);
        }

        @Override
        protected void work() throws ProcessException {
            PeriodOfTime delay = new PeriodOfTime(1000 * ms);
            Timer timer = new Timer("TestTimer");
//            for(int i = 0; i < 10; i++){
//            timer.start(500 * ms);
            timer.start(2000 * ms);
            for(int i = 0; i < 1; i++){
                delay.await();
                delay.await();
                delay.await();
                System.out.println("timer event " + new Long(System.nanoTime()/(ms*1000)) + " remaining time: " + timer.getRemainingTime() + " isRunning: " + timer.isRunning());
                timer.restart();
                delay.await();
                rt1 = timer.getRemainingTime();
                System.out.println("delay event " + new Long(System.nanoTime()/(ms*1000)) + " remaining time: " + rt1 + " isRunning: " + timer.isRunning());
                rt2 = timer.getRemainingTime();
                delay.await();
                delay.await();
                System.out.println("timer event " + new Long(System.nanoTime()/(ms*1000)) + " remaining time: " + timer.getRemainingTime() + " isRunning: " + timer.isRunning());
            }
            timer.start(1000 * ms);
            TimerExpires te = timer.expires();
            te.await();
            //the following event must not occure, because the timer already timed out
            te.await(2 * sec);
            //force a CycleTimeoutException
            //try{Thread.sleep(1);}catch(InterruptedException exc){};
        }

        protected void initialize() {
        }

        @Override
        protected void preCheckInterlocks() throws InputInterlockException {
        }

        @Override
        protected void postCheckInterlocks() throws OutputInterlockException {
        }

        @Override
        protected void inEveryCycleDo() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
    public TimerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }


    @Test
    public void testTimer() throws InterruptedException {
        System.out.println("timer");
        JPac a = JPac.getInstance();
//        properties.cycleTime          = 10000000L;
//        properties.cycleTimeoutTime   = 10 * properties.cycleTime;
//        properties.cycleMode          = JPac.CycleMode.Bound;
//        properties.runningInsideAnIde = false;   
//        properties.runningInjUnitTest = true;
//        
//        properties.traceTimeMinutes   = 1;
//        properties.enableTrace        = false;
//        a.updateProperties(properties);
        
        TimerTestModule m1 = new TimerTestModule(null);
        m1.start();

        int  i = 0;
        while(i < 6){
            try{Thread.sleep(1000);}catch(InterruptedException exc){exc.printStackTrace();};
            i++;
        }
        assert(m1.rt1 == m1.rt2);//rt1,rt2 must be equal in same cycle
        assert(Math.abs(m1.rt1 - 1000 * Module.ms) < 2 * JPac.getInstance().getCycleTime());
        try{Thread.sleep(4000);}catch(InterruptedException exc){exc.printStackTrace();};
        a.shutdown();
    }    
}
