/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : ConnectTest.java
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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author berndschuster
 */
public class ConnectTest {
    
    class ConnectModule extends Module{

        @Override
        protected void inEveryCycleDo() throws ProcessException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        class InnerModule extends Module{

            public Decimal dec10;
            public Decimal dec11;
            
            public InnerModule(AbstractModule containingModule){
                super(containingModule);
                initialize();
            }
            
            @Override
            protected void work() throws ProcessException {
                ProcessEvent nc = new NextCycle();
                try{
                    do{
                        Log.info("InnerModule.dec10 = " + (dec10.isValid() ? dec10.get() : "--") + " InnerModule.dec11 = " + (dec11.isValid() ? dec11.get() : "--"));
                        Log.info("InnerModule.dec10.isChanged = " + dec10.isChanged() + " InnerModule.dec11.isChanged() = " + dec11.isChanged());
                        nc.await();
                    }
                    while(true);
                }
                catch(Exception exc){
                    Log.error("Error: ", exc);
                }
            }

            private void initialize() {
                try {
                    dec10 = new Decimal(this,"dec10");
                    dec11 = new Decimal(this,"dec11");
                } catch (SignalAlreadyExistsException ex) {
                    Log.error("Error: ", ex);
                }
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
        
        Decimal     dec0;
        Decimal     dec1;
        Decimal     dec2;
        InnerModule innerModule;
        
        public ConnectModule(AbstractModule owningModule, String identifier){
            super(owningModule, identifier);
            initialize();
        }

        @Override
        protected void work() throws ProcessException {
            NextCycle nc = new NextCycle();
            try{
                dec1.set(0.0);
                //dec1.set(0.0);
                //dec2.set(0.0);
                //nc.await();
                for (int i = 0; i < 10; i++){
                    if (i % 2 != 0)
                        dec1.set(i+1);
                    Log.info("i = " + i + " dec0 = " + (dec0.isValid() ? dec0.get() : "--") + " dec1 = " + (dec1.isValid() ? dec1.get() : "--") + " dec2 = " + (dec2.isValid() ? dec2.get() : "--"));
                    Log.info("    " + i + " dec0.isChanged = " + dec0.isChanged() + " dec1.isChanged() = " + dec1.isChanged() + " dec2.isChanged() = " + dec2.isChanged());
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
                dec0 = new Decimal(this,"dec0");
                dec1 = new Decimal(this,"dec1");
                dec2 = new Decimal(this,"dec2");
                dec1.connect(dec2);
                dec2.connect(dec0);
                dec1.connect(innerModule.dec10);
                dec1.connect(innerModule.dec11);
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

    public ConnectTest() {
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