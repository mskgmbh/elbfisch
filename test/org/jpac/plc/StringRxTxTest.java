/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jpac.plc;

import org.jpac.Value;
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
public class StringRxTxTest {
    
    public StringRxTxTest() {
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
     * Test of clone method, of class StringRxTx.
     */
    @Test
    public void testClone() throws Exception {
        System.out.println("clone");
        byte[] bytes = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
        StringRxTx instance    = new StringRxTx(null, new Address(0,Address.NA,16), 0, new Data(bytes));
        StringRxTx newInstance = (StringRxTx)instance.clone();
        Value expResult = null;
        Value result = instance.clone();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
