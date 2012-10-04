/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : CyclicBufferTest.java
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
