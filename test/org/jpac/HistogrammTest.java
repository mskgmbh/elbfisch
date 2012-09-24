/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jpac;

import org.jpac.statistics.Histogramm;
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
public class HistogrammTest {
    Histogramm histogramm;
    long       cycleTime;
    
    public HistogrammTest() {
        cycleTime  = 100000000L;
        histogramm = new Histogramm(cycleTime);
        
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
     * Test of update method, of class Histogramm.
     */
    @Test
    public void testUpdate() {
        System.out.println("update");
        for (long l = 0; l < 1000000L; l++){
            histogramm.update(Math.round(Math.random() * cycleTime));
        }
        //force out of bounds situation
        histogramm.update(-(2 * cycleTime));
        histogramm.update(cycleTime + 1);
        System.out.println("Histogramm: " + histogramm.toCSV());
    }

    /**
     * Test of getValues method, of class Histogramm.
     */
    @Test
    public void testGetValues() {
        System.out.println("getValues");
    }

    /**
     * Test of toCSV method, of class Histogramm.
     */
    @Test
    public void testToCSV() {
        System.out.println("toCSV");
        fail("The test case is a prototype.");
    }
}
