/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jpac.remote;

import org.jpac.CyclicBuffer;
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
public class CyclicBufferTest {
    
    public CyclicBufferTest() {
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
     * Test of occupy method, of class CyclicBuffer.
     */
    @Test
    public void test() throws Exception {
        CyclicBuffer<Integer> cb = new CyclicBuffer<Integer>(10);
        //fill buffer
        for (int i = 0; i < 100; i++){
            Integer n = cb.occupy();
            if (n == null){
                n = new Integer(i);
            }
            n = i;
            cb.put(n);

            n = cb.get();
            System.out.println(n);
            cb.release();
        }
//        for (int i = 0; i < 10; i++){
//            Integer n = cb.get();
//            System.out.println(n);
//            if (n == null){
//                n = new Integer(i);
//            }
//            cb.release();
//        }
    }
}
