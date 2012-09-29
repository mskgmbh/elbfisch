/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : LogicalEventTest.java
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
public class LogicalEventTest {
    
    class ConnectModule extends Module{

        @Override
        protected void inEveryCycleDo() throws ProcessException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        class InnerModule extends Module{

            public Logical lo10;
            public Logical lo11;
            
            public InnerModule(AbstractModule containingModule){
                super(containingModule);
                initialize();
            }
            
            @Override
            protected void work() throws ProcessException {
                ProcessEvent nc = new NextCycle();
                //ProcessEvent lt = lo10.toggles();
                //ProcessEvent lt = lo11.toggles();
                //ProcessEvent lt = lo10.becomes(true);
                ProcessEvent lt = lo11.becomes(false);
                //ProcessEvent lt = lo11.state(true);
                //ProcessEvent lt = lo11.state(false);
                try{
                    do{
                        Log.info("InnerModule.lo10 = " + (lo10.isValid() ? lo10.is(true) : "--") + " InnerModule.lo11 = " + (lo11.isValid() ? lo11.is(true) : "--"));
                        if (lo10.isValid() && lo11.isValid())
                            Log.info("InnerModule.lo10.isToggled = " + lo10.isToggled() + " InnerModule.lo11.isToggled() = " + lo11.isToggled());
                        lt.await();
                    }
                    while(true);
                }
                catch(Exception exc){
                    Log.error("Error: ", exc);
                }
            }

            private void initialize() {
                lo10 = new Logical(this,"lo10");
                lo11 = new Logical(this,"lo11");
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
        
        Logical lo1;
        Logical lo2;
        InnerModule innerModule;
        
        public ConnectModule(AbstractModule owningModule, String identifier){
            super(owningModule, identifier);
        }

        @Override
        protected void work() throws ProcessException {
            NextCycle nc = new NextCycle();
            try{
                lo1.set(false);
                lo2.set(false);
                nc.await();
                for (int i = 0; i < 10; i++){
                    lo1.set(!lo1.is(true));
                    if (i % 2 == 0)
                        lo2.set(lo2.is(false));
                    Log.info("i = " + i + " lo1 = " + (lo1.isValid() ? lo1.is(true) : "--") + " lo2 = " + (lo2.isValid() ? lo2.is(true) : "--"));
                    Log.info("    " + i + " lo1.isToggled = " + lo1.isToggled() + " lo2.isToggled() = " + lo2.isToggled());
                    nc.await();
                }            
            }
            catch(Exception exc){
                exc.printStackTrace();
            }
        }

        @Override
        public void start(){
            innerModule.start();
            super.start();
        }
        
        private void initialize() {
            try{
                innerModule = new InnerModule(this);
                lo1 = new Logical(this,"lo1");
                lo2 = new Logical(this,"lo2");
                lo1.connect(innerModule.lo10);
                lo2.connect(innerModule.lo11);
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

    public LogicalEventTest() {
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
            try{Thread.sleep(3000);}catch(Exception exc){};
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