/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : HistogrammTest.java
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
