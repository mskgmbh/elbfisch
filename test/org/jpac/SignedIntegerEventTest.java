/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : SignedIntegerEventTest.java
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
public class SignedIntegerEventTest {
    
    class ConnectModule extends Module{

        @Override
        protected void inEveryCycleDo() throws ProcessException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        class InnerModule extends Module{

            public SignedInteger lo10;
            public SignedInteger lo11;
            
            public InnerModule(AbstractModule containingModule){
                super(containingModule);
                initialize();
            }
            
            @Override
            protected void work() throws ProcessException {
                double value = 0.0;
                ProcessEvent nc = new NextCycle();
                //SignedIntegerChanges lt = lo10.changes(200,50);
                SignedIntegerChanges lt = lo10.changes();
                //SignedIntegerExceeds lt = lo10.exceeds(100);
                //SignedIntegerFallsBelow lt = lo10.fallsBelow(100);
                try{
                    do{
                        Log.info("InnerModule.lo10 = " + (lo10.isValid() ? lo10.get() : "--") + " InnerModule.lo11 = " + (lo11.isValid() ? lo11.get() : "--"));
                        if (lo10.isValid() && lo11.isValid()){
                            //Log.info("InnerModule.lo10.isChanged = " + lo10.isChanged() + " InnerModule.lo11.isChanged = " + lo11.isChanged());
                            //value = lo10.get();
                        }
                        lt.await();
                        //lt.setThreshold(lt.getThreshold() + 0.1);
                    }
                    while(true);
                }
                catch(Exception exc){
                    Log.error("Error: ", exc);
                }
            }

            private void initialize() {
                lo10 = new SignedInteger(this,"lo10");
                lo11 = new SignedInteger(this,"lo11");
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
        
        SignedInteger lo1;
        SignedInteger lo2;
        InnerModule innerModule;
        
        public ConnectModule(AbstractModule owningModule, String identifier){
            super(owningModule, identifier);
            initialize();
        }

        @Override
        protected void work() throws ProcessException {
            NextCycle nc = new NextCycle();
            try{
                lo1.set(0);
                lo2.set(0);
                nc.await();
                for (int i = 0; i < 25; i++){
                    lo1.set(lo1.get() + 10);
                    if (i % 2 == 0)
                        lo2.set(lo2.get() - 2);
                    Log.info("i = " + i + " lo1 = " + (lo1.isValid() ? lo1.get() : "--") + " lo2 = " + (lo2.isValid() ? lo2.get() : "--"));
                    //Log.info("    " + i + " lo1.isChanged = " + lo1.isChanged() + " lo2.isChanged = " + lo2.isChanged());
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
                lo1 = new SignedInteger(this,"lo1");
                lo2 = new SignedInteger(this,"lo2");
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

    public SignedIntegerEventTest() {
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