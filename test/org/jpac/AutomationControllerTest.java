/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : ModuleExplorer.java
 * VERSION   : $Revision: 1.4 $
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
 * LOG       : $Log: AutomationControllerTest.java,v $
 * LOG       : Revision 1.4  2012/06/18 11:20:53  schuster
 * LOG       : introducing cyclic tasks
 * LOG       :
 * LOG       : Revision 1.3  2012/05/07 06:17:15  schuster
 * LOG       : some new tests
 * LOG       :
 * LOG       : Revision 1.2  2012/03/05 07:23:10  schuster
 * LOG       : introducing Properties
 * LOG       :
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
public class AutomationControllerTest {
    
    class TestBoundModeModule extends Module{
        
        public boolean startWork = false;
        public int     cycleCount = 0;
                
        public TestBoundModeModule(AbstractModule m, int cycleCount){
            super(m);
            this.cycleCount = cycleCount;
            initialize();
        }

        @Override
        protected void work() throws ProcessException {
            NthCycle event = new NthCycle(cycleCount);
//            for(int i = 0; i < 10; i++){
            for(;;){
                event.await();
                for(long l = 0; l < 1000;l++){
                    long m = l;
                }
            }
            //force a CycleTimeoutException
            //try{Thread.sleep(1);}catch(InterruptedException exc){};
        }

        private void initialize() {
        }

        @Override
        protected void preCheckInterlocks() throws InputInterlockException {
        }

        @Override
        protected void postCheckInterlocks() throws OutputInterlockException {
        }

        @Override
        protected void inEveryCycleDo() throws ProcessException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
    public AutomationControllerTest() {
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
    public void testStartStopCycling() {
        System.out.println("start/stop");
        JPac a = JPac.getInstance();
        //let the automation controller enter ready state
        try{Thread.sleep(100);}catch(InterruptedException exc){exc.printStackTrace();};
        if (a.getStatus() != JPac.Status.ready){
            fail();
        }
                
        a.startCycling();
        long lastCycleNumber = a.getCycleNumber();
        long cycleNumber = lastCycleNumber;
        System.out.println("1 cycleNumber : " + cycleNumber);
        //let the automation controller enter running state
        try{Thread.sleep(100);}catch(InterruptedException exc){exc.printStackTrace();};
        if (a.getStatus() != JPac.Status.running){
            fail();
        }
        cycleNumber = a.getCycleNumber();
        System.out.println("2 cycleNumber : " + cycleNumber);
        //the cycle number must increase continously
        if (cycleNumber <= lastCycleNumber){
            fail();
        }
        lastCycleNumber = cycleNumber;
        a.stopCycling();
        //let the automation controller enter stopped state
        try{Thread.sleep(100);}catch(InterruptedException exc){exc.printStackTrace();};
        if (a.getStatus() != JPac.Status.ready){
            fail();
        }
        lastCycleNumber = a.getCycleNumber();
        System.out.println("3 cycleNumber : " + lastCycleNumber);
        try{Thread.sleep(100);}catch(InterruptedException exc){exc.printStackTrace();};
        //the cycle number must be the same
        cycleNumber = a.getCycleNumber();
        System.out.println("4 cycleNumber : " + cycleNumber);
        if (cycleNumber != lastCycleNumber){
            fail();
        }
        //restart the automation controller
        a.startCycling();
        lastCycleNumber = a.getCycleNumber();
        System.out.println("5 cycleNumber : " + lastCycleNumber);
        //let the automation controller enter running state
        try{Thread.sleep(100);}catch(InterruptedException exc){exc.printStackTrace();};
        if (a.getStatus() != JPac.Status.running){
            fail();
        }
        cycleNumber = a.getCycleNumber();
        System.out.println("6 cycleNumber : " + cycleNumber);
        //the cycle number must increase continously
        if (cycleNumber <= lastCycleNumber){
            fail();
        }   
        a.shutdown();
    }

    @Test
    public void testLazyBoundMode() throws InterruptedException {
        System.out.println("Lazy bound mode");
        JPac a = JPac.getInstance();
//        JPac.Properties properties = a.copyProperties();
//        properties.cycleTime          = 10000000L;
//        properties.cycleTimeoutTime   = 10 * properties.cycleTime;
//        properties.cycleMode          = JPac.CycleMode.Bound;
//        properties.runningInsideAnIde = false;   
//        properties.runningInjUnitTest = true;
//        
//        properties.traceTimeMinutes   = 1;
//        properties.enableTrace        = false;
//        a.updateProperties(properties);
        
        TestBoundModeModule m1 = new TestBoundModeModule(null,10);
        m1.start();

//        TestBoundModeModule m2 = new TestBoundModeModule(m1,9);
//        m2.start();
//
//        TestBoundModeModule m3 = new TestBoundModeModule(m1,8);
//        m3.start();
//
//        TestBoundModeModule m4 = new TestBoundModeModule(m1,7);
//        m4.start();
//
//        TestBoundModeModule m5 = new TestBoundModeModule(m1,6);
//        m5.start();
//
//        TestBoundModeModule m6 = new TestBoundModeModule(m1,5);
//        m6.start();
//
//        TestBoundModeModule m7 = new TestBoundModeModule(m1,4);
//        m7.start();
//
//        TestBoundModeModule m8 = new TestBoundModeModule(m1,3);
//        m8.start();
//
//        TestBoundModeModule m9 = new TestBoundModeModule(m1,2);
//        m9.start();
        int  i = 0;
        while(i < 100){
            try{Thread.sleep(1000);}catch(InterruptedException exc){exc.printStackTrace();};
            System.out.println(i);
            i++;
        }
//        for (int j = 0; j < 999999; j++){
////        for (;;){
//            try{
//                System.out.println(a.getTraceQueue().take().toCSV());
//            }
//            catch(InterruptedException exc)
//            {exc.printStackTrace();
//            }
//        }
        System.out.println("9999 reached ");        
        a.shutdown();
        assert(true);
    }
    /**
     * Test of setShutdownRequested method, of class JPac.
     */
    @Test
    public void testSetShutdownRequested() {
        System.out.println("setShutdownRequested");
        boolean shutdownRequested = false;
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of stopp method, of class JPac.
     */
    @Test
    public void testStopCycling() {
        System.out.println("stopCycling");
    }

    /**
     * Test of invokeLater method, of class JPac.
     */
    @Test
    public void testInvokeLater() {
        System.out.println("invokeLater");
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
    /**
     * Test of startUp method, of class JPac.
     */

    /**
     * Test of getInstance method, of class JPac.
     */
    @Test
    public void testGetInstance() {
        System.out.println("getInstance");
    }

    /**
     * Test of run method, of class JPac.
     */
    @Test
    public void testRun() {
        System.out.println("run");
    }

    /**
     * Test of getAwaitedEventList method, of class JPac.
     */
    @Test
    public void testGetAwaitedEventList() {
        System.out.println("getAwaitedEventList");
    }

    /**
     * Test of getAwaitedSimEventList method, of class JPac.
     */
    @Test
    public void testGetAwaitedSimEventList() {
        System.out.println("getAwaitedSimEventList");
    }

    /**
     * Test of getFiredEventList method, of class JPac.
     */
    @Test
    public void testGetFiredEventList() {
        System.out.println("getFiredEventList");
    }

    /**
     * Test of getCycleNumber method, of class JPac.
     */
    @Test
    public void testGetCycleNumber() {
        System.out.println("getCycleNumber");
    }

    /**
     * Test of getCycleTime method, of class JPac.
     */
    @Test
    public void testGetCycleTime() {
        System.out.println("getCycleTime");
    }
    /**
     * Test of acknowledgeStopRequest method, of class JPac.
     */
    @Test
    public void testAcknowledgeStopRequest() {
        System.out.println("acknowledgeStopRequest");
    }

    /**
     * Test of isStopCyclingRequested method, of class JPac.
     */
    @Test
    public void testIsStopCyclingRequested() {
        System.out.println("isStopCyclingRequested");
    }

    /**
     * Test of isShutdownRequested method, of class JPac.
     */
    @Test
    public void testIsShutdownRequested() {
        System.out.println("isShutdownRequested");
    }

    /**
     * Test of isEmergencyStopActive method, of class JPac.
     */
    @Test
    public void testIsEmergencyStopActive() {
        System.out.println("isEmergencyStopActive");
    }

    /**
     * Test of acknowledgeEmergencyStop method, of class JPac.
     */
    @Test
    public void testAcknowledgeEmergencyStop() {
        System.out.println("acknowledgeEmergencyStop");
    }

    /**
     * Test of requestEmergencyStop method, of class JPac.
     */
    @Test
    public void testRequestEmergencyStop() {
        System.out.println("requestEmergencyStop");
    }

    /**
     * Test of getEmergencyStopCause method, of class JPac.
     */
    @Test
    public void testGetEmergencyStopCause() {
        System.out.println("getEmergencyStopCause");
    }

    /**
     * Test of getAwakenedModulesCount method, of class JPac.
     */
    @Test
    public void testGetAwakenedModulesCount() {
        System.out.println("getAwakenedModulesCount");
    }

    /**
     * Test of incrementAwakenedModulesCount method, of class JPac.
     */
    @Test
    public void testIncrementAwakenedModulesCount() {
        System.out.println("incrementAwakenedModulesCount");
    }

    /**
     * Test of decrementAwakenedModulesCount method, of class JPac.
     */
    @Test
    public void testDecrementAwakenedModulesCount() {
        System.out.println("decrementAwakenedModulesCount");
    }

    /**
     * Test of prepareOneCycleMode method, of class JPac.
     */
    @Test
    public void testPrepareOneCycleMode() {
        System.out.println("prepareOneCycleMode");
    }

    /**
     * Test of waitForStartUpSignal method, of class JPac.
     */
    @Test
    public void testWaitForStartUpSignal() {
        System.out.println("waitForStartUpSignal");
    }

    /**
     * Test of waitForStartCycleSignal method, of class JPac.
     */
    @Test
    public void testWaitForStartCycleSignal() {
        System.out.println("waitForStartCycleSignal");
    }

    /**
     * Test of signalEndOfCycle method, of class JPac.
     */
    @Test
    public void testSignalEndOfCycle() {
        System.out.println("signalEndOfCycle");
    }

    /**
     * Test of invokeNextCycle method, of class JPac.
     */
    @Test
    public void testInvokeNextCycle() {
        System.out.println("invokeNextCycle");
    }

    /**
     * Test of prepareCycle method, of class JPac.
     */
    @Test
    public void testPrepareCycle() {
        System.out.println("prepareCycle");
    }

    /**
     * Test of wait4AllEventsHandled method, of class JPac.
     */
    @Test
    public void testWait4AllEventsHandled_0args() {
        System.out.println("wait4AllEventsHandled");
    }

    /**
     * Test of wait4AllEventsHandled method, of class JPac.
     */
    @Test
    public void testWait4AllEventsHandled_long() {
        System.out.println("wait4AllEventsHandled");
    }

    /**
     * Test of wait4EndOfCycle method, of class JPac.
     */
    @Test
    public void testWait4EndOfCycle() {
        System.out.println("wait4EndOfCycle");
    }

    /**
     * Test of logStatistics method, of class JPac.
     */
    @Test
    public void testLogStatistics() {
        System.out.println("logStatistics");
    }

    /**
     * Test of loadProperties method, of class JPac.
     */
    @Test
    public void testLoadProperties() {
        System.out.println("loadProperties");
    }

    /**
     * Test of getProperties method, of class JPac.
     */
    @Test
    public void testGetProperties() {
    }
    
}
