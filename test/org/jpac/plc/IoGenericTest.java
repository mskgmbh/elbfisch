/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jpac.plc;

import org.jpac.Generic;
import org.jpac.InputInterlockException;
import org.jpac.Module;
import org.jpac.NextCycle;
import org.jpac.NumberOutOfRangeException;
import org.jpac.OutputInterlockException;
import org.jpac.ProcessException;
import org.jpac.SignalAccessException;
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
public class IoGenericTest {
    Module                   module;
    Generic<UDT_Test_3Bytes> generic;
    UDT_Test_3Bytes          udt;
    IoGeneric                instance;
    Data                     data;
    
    public IoGenericTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of check method, of class IoGeneric.
     */
    @Test
    public void testCheck() throws Exception {
        System.out.println("check");
        byte[] bytes = {0,1,2};
        data       = new Data(bytes);
        module     = new TestModule(null,"module");
        generic    = new Generic<UDT_Test_3Bytes>(module, "generic");
        udt        = new UDT_Test_3Bytes(null, new Address(0,Address.NA,UDT_Test_3Bytes.getSize()), 0, new Data(bytes));
        instance   = new IoGeneric(generic, udt);
        module.start();
        while(module.isAlive()) Thread.sleep(500);
        assert(true);
    }

    /**
     * Test of getWriteRequest method, of class IoGeneric.
     */
    @Test
    public void testGetWriteRequest() {
        System.out.println("getWriteRequest");
        Connection connection = null;
        IoGeneric instance = null;
        WriteRequest expResult = null;
        WriteRequest result = instance.getWriteRequest(connection);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class IoGeneric.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        IoGeneric instance = null;
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
    class TestModule extends Module{

        public TestModule(Module containingModule, String identifier){
            super(containingModule,identifier);
        }
        
        @Override
        protected void work() throws ProcessException{
            try{
                instance.check();
                new NextCycle().await();
                instance.check();       
                new NextCycle().await();
                data.getBytes()[1] = 9;
                instance.check();       
            }
            catch(Exception exc){
                exc.printStackTrace();
            }
        }

        @Override
        protected void preCheckInterlocks() throws InputInterlockException {
            //nothing to do
        }

        @Override
        protected void postCheckInterlocks() throws OutputInterlockException {
            //nothing to do
        }

        @Override
        protected void inEveryCycleDo() throws ProcessException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
}
