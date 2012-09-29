/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : GenericPlugTest.java
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
public class GenericPlugTest {
    static Module module, module1, module2;
    public GenericPlugTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        module = new Module(null, "module") {

            @Override
            protected void work() {
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
        };

        module1 = new Module(null, "module1") {

            @Override
            protected void work() {
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
        };

        module2 = new Module(null, "module2") {

            @Override
            protected void work() {
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
        };
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
    public void testPlug() throws Exception {
        System.out.println("plug");

        System.out.println("create subPlug ...");
        Plug subPlug  = new Plug(module2, "theSubPlug");
        Signal subSignalSource = new Logical(module2, "theSubSignalSource");
        Signal subSignalSink = new Logical(module2, "theSubSignalSink");        
        subPlug.addPin(0,true).assign(subSignalSource);
        subPlug.addPin(1,false).assign(subSignalSink);
        System.out.println(subPlug.getPin(0).getIdentifier());
        System.out.println(subPlug.getPin(1).getIdentifier());
        System.out.println("... done");

        System.out.println("create thePlug ...");
        Plug jack = new Plug(module1, "thePlug");
        Signal signalSource = new Logical(module1, "theSignalSource");
        Signal signalSink = new Logical(module1, "theSignalSink");
        jack.addPin(0).assign(subPlug);
        jack.addPin(1,true).assign(signalSource);
        jack.addPin(2,false).assign(signalSink);
        for (Pin pin: jack.getSignalPins()){
            System.out.println(pin);
        }
        for (String id: jack.getQualifiedSignalPinIdentifiers()){
            System.out.println(id);
        }


        System.out.println("... done");

        GenericPlug instance = new GenericPlug(module,"theGenericPlug");
        instance.plug(jack);
        //instance.addPin(3).assign(subPlug.getPin(0));

        System.out.println(">>>>>>");
        for (Pin pin: instance.getSignalPins()){
            System.out.println(pin);
        }
        for (String id: instance.getQualifiedSignalPinIdentifiers()){
            System.out.println(id);
        }
    }

    /**
     * Test of isCompatible method, of class GenericPlug.
     */
    @Test
    public void testIsCompatible() {
        System.out.println("isCompatible");
    }

}