/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jpac.plc;

import java.util.ArrayList;
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
public class DataTest {
    
    public DataTest() {
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
     * Test of equals method, of class Data.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        byte[] thisBytes = {0,1,2,3,4,5,6,7,8,9};
        byte[] thatBytes = {0,0,0,0,0,0,0,0,0,0};
        Data thisData = new Data(thisBytes,Data.Endianness.BIGENDIAN);
        Data thatData = new Data(thatBytes,Data.Endianness.LITTLEENDIAN);
        assert(!thisData.equals(thatData));
        thatData = new Data(thatBytes,Data.Endianness.BIGENDIAN);
        assert(!thisData.equals(thatData));
        thatData = new Data(thisBytes,Data.Endianness.LITTLEENDIAN);
        assert(!thisData.equals(thatData));
        thatData = new Data(thisBytes,Data.Endianness.BIGENDIAN);
        assert(thisData.equals(thatData));
    }

    /**
     * Test of copy method, of class Data.
     */
    @Test
    public void testCopy() {
        System.out.println("copy");
        byte[] thisBytes = {0,1,2,3,4,5,6,7,8,9};
        byte[] thatBytes = {0,0,0,0,0,0,0,0,0,0};
        Data thisData = new Data(thisBytes,Data.Endianness.BIGENDIAN);
        Data thatData = new Data(thatBytes,Data.Endianness.LITTLEENDIAN);
        thisData.copy(thatData);
        assert(thisData.equals(thatData));
    }

    /**
     * Test of clone method, of class Data.
     */
    @Test
    public void testClone() throws Exception {
        System.out.println("clone");
        byte[] thisBytes = {0,1,2,3,4,5,6,7,8,9};
        Data thisData = new Data(thisBytes,Data.Endianness.BIGENDIAN);
        Data thatData = thisData.clone();
        assert(thisData.equals(thatData) && thisData != thatData && thisData.bytes != thatData.bytes && thisData.endianness == thatData.endianness);
        thisData.endianness = Data.Endianness.LITTLEENDIAN;
        assert(thisData.endianness != thatData.endianness);
    }
}
