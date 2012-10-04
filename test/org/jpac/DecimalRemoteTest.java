/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : DecimalEventTest.java
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

/**
 *
 * @author berndschuster
 */
public class DecimalRemoteTest {
    
    class ConnectModule extends Module{
        private RemoteSignalOutput remoteSignal;

        
        @Override
        protected void inEveryCycleDo() throws ProcessException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
                
        Decimal lo1;
        
        public ConnectModule(AbstractModule owningModule, String identifier){
            super(owningModule, identifier);
            initialize();
        }

        @Override
        protected void work() throws ProcessException {
            NthCycle nc = new NthCycle(20);
            try{
                lo1.set(234.0);
                nc.await();
                for (int i = 0; i < 2500; i++){
                    lo1.set(lo1.get() + 1.0);
                    System.out.println("lo1 = " + lo1.get());
                    nc.await();
                }            
            }
            catch(Exception exc){
                exc.printStackTrace();
            }
        }

        @Override
        public void start(){
            super.start();
        }
        
        private void initialize() {
            try{
                lo1 = new Decimal(this,"lo1");
                remoteSignal = new RemoteSignalOutput("remoteSignal","192.168.0.99",10002,"ApplicationGlobalModule.angeforderteTemperaturSlave1");
//                  remoteSignal  = new RemoteSignalOutput("remoteSignal","localhost",10003,"RemoteJPacTest.SignalInput");
                  lo1.connect(remoteSignal);
            }
            catch(Exception exc){
                exc.printStackTrace();                
            }
        }

        @Override
        protected void preCheckInterlocks() throws InputInterlockException {
        }

        @Override
        protected void postCheckInterlocks() throws OutputInterlockException {
        }
        
    }

    public DecimalRemoteTest() {
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

    /**
     * Test of plug method, of class GenericPlug.
     */
    @Test
    public void testConnect() throws Exception {
        System.out.println("connect");
        try{
            Module module = new ConnectModule(null, "module1");
            module.start();
            try{Thread.sleep(300000);}catch(Exception exc){};
        }
        catch(Exception exc){
            exc.printStackTrace();
        }

    }

    /**
     * Test of isCompatible method, of class GenericPlug.
     */
    @Test
    public void testIsCompatible() {
    }

}