/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : PlugTest.java
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

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class PlugTest {
    static Module module, module1, module2;
    public PlugTest() {
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
     * Test of getPins method, of class Plug.
     */
    @Test
    public void testGetPins() {
        System.out.println("getPins");
        try {
            Plug instance = new Plug(module, "thePlug");
            Signal signal = new Logical(module, "theSignal");
            HashMap<Object, Pin> result = instance.getPins();
            assert (result.size() == 0);
            instance.addPin(0,true).assign(signal);
            assert (result.size() == 1);
            instance.addPin(10,true).assign(signal);
            assert (result.size() == 2);
        }
        catch (PinAlreadyAssignedException ex) {
            Logger.getLogger(PlugTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        }
    }

    /**
     * Test of getPin method, of class Plug.
     */
    @Test
    public void testGetPin() {
        System.out.println("getPin");
        //add, retrieve pin
        try {
            Plug instance = new Plug(module, "thePlug");
            Signal signal = new Logical(module, "theSignal");
            instance.addPin(10,true).assign(signal);
            Pin result = instance.getPin(10);
            assert (result.getAssignedItem() == signal);
        }
        catch (PinAlreadyAssignedException ex) {
            Logger.getLogger(PlugTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        }
        catch (PinNotAssignedException ex) {
            Logger.getLogger(PlugTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        }
        //add, retrieve pin
        try {
            Plug instance = new Plug(module, "thePlug");
            Signal signal = new Logical(module, "theSignal");
            //instance.addPin(10).assign(signal, true);
            Pin result = instance.getPin(10);
            fail();
        }
        catch (PinNotAssignedException ex) {
            //Logger.getLogger(PlugTest.class.getName()).log(Level.SEVERE, null, ex);
            assert(true);
        }
        //assign pin
        try {
            Plug instance = new Plug(module, "thePlug");
            Signal signal = new Logical(module, "theSignal");
            instance.addPin(10,true).assign(signal);
            instance.addPin(10,true).assign(signal);
            fail();
        }
        catch (PinAlreadyAssignedException ex) {
            //Logger.getLogger(PlugTest.class.getName()).log(Level.SEVERE, null, ex);
            assert(true);
        }
    }

    /**
     * Test of plug method, of class Plug.
     */
    @Test
    public void testPlug() throws Exception {
        System.out.println("plug");
        try {
            Plug instance = new Plug(module, "thePlug");
            Plug subPlug  = new Plug(module, "theSubPlug");
            Signal signalSource = new Logical(module, "theSignalSource");
            Signal signalSink = new Logical(module, "theSignalSink");
            Signal subSignalSource = new Logical(module, "theSubSignalSource");
            Signal subSignalSink = new Logical(module, "theSubSignalSink");
            subPlug.addPin(0,true).assign(subSignalSource);
            subPlug.addPin(1,false).assign(subSignalSink);
            instance.addPin(0).assign(subPlug);
            instance.addPin(1,true).assign(signalSource);
            instance.addPin(2,false).assign(signalSink);//????

            Plug jack = new Plug(module, "theJack");
            Plug subJack  = new Plug(module, "theSubJack");
            Signal jackSignalSource = new Logical(module, "theJackSignalSource");
            Signal jackSignalSink = new Logical(module, "theJackSignalSink");
            Signal subJackSignalSource = new Logical(module, "theSubJackSignalSource");
            Signal subJackSignalSink = new Logical(module, "theSubJackSignalSink");
            subJack.addPin(1,true).assign(subJackSignalSource);
            subJack.addPin(0,false).assign(subJackSignalSink);
            jack.addPin(0).assign(subJack);
            jack.addPin(2,true).assign(jackSignalSource);
            jack.addPin(1,false).assign(jackSignalSink);

            assert (instance.getPin(0).getAssignedItem() == subPlug);
            assert (instance.getPin(1).getAssignedItem() == signalSource && instance.getPin(1).isSource());
            assert (instance.getPin(2).getAssignedItem() == signalSink  && !instance.getPin(2).isSource());

            instance.plug(jack);

            assert(instance.getPin(0).getAssignedItem() instanceof Plug);
            assert(instance.getPin(1).getAssignedItem() instanceof Logical);
            assert(instance.getPin(2).getAssignedItem() instanceof Logical);
        }
        catch (PinAlreadyAssignedException ex) {
            Logger.getLogger(PlugTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        }
        catch (PinNotAssignedException ex) {
            Logger.getLogger(PlugTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        }
    }

    /**
     * Test of isCompatible method, of class Plug.
     */
    @Test
    public void testIsCompatible() {
        System.out.println("isCompatible");
        try {
            Plug instance = new Plug(module, "thePlug");
            Plug subPlug  = new Plug(module, "theSubPlug");
            Signal signalSource = new Logical(module, "theSignalSource");
            Signal signalSink = new Logical(module, "theSignalSink");
            Signal subSignalSource = new Logical(module, "theSubSignalSource");
            Signal subSignalSink = new Logical(module, "theSubSignalSink");
            subPlug.addPin(0,true).assign(subSignalSource);
            subPlug.addPin(1,false).assign(subSignalSink);
            instance.addPin(0).assign(subPlug);
            instance.addPin(1,true).assign(signalSource);
            instance.addPin(2,false).assign(signalSink);

            //number of pins differ
            Plug jack = new Plug(module, "theJack");
            Plug subJack  = new Plug(module, "theSubJack");
            Signal jackSignalSource = new Logical(module, "theJackSignalSource");
            Signal jackSignalSink = new Logical(module, "theJackSignalSink");
            Signal subJackSignalSource = new Logical(module, "theSubJackSignalSource");
            Signal subJackSignalSink = new Logical(module, "theSubJackSignalSink");
            subJack.addPin(1,true).assign(subJackSignalSource);
            subJack.addPin(0,false).assign(subJackSignalSink);
            jack.addPin(0).assign(subJack);
            try{
                instance.plug(jack);
                fail();
            }
            catch(PlugIncompatibleException ex){
                //exception must have been thrown
            }

            jack.addPin(2,true).assign(jackSignalSource);
            try{
                instance.plug(jack);
                fail();
            }
            catch(PlugIncompatibleException ex){
                //exception must have been thrown
            }

            jack.addPin(1,false).assign(jackSignalSink);

            assert (instance.getPin(0).getAssignedItem() == subPlug);
            assert (instance.getPin(1).getAssignedItem() == signalSource && instance.getPin(1).isSource());
            assert (instance.getPin(2).getAssignedItem() == signalSink  && !instance.getPin(2).isSource());

            try{
                instance.plug(jack);
            }
            catch(PlugIncompatibleException ex){
                Logger.getLogger(PlugTest.class.getName()).log(Level.SEVERE, null, ex);
                fail();
            }

            assert(instance.getPin(0).getAssignedItem() instanceof Plug);
            assert(instance.getPin(1).getAssignedItem() instanceof Logical);
            assert(instance.getPin(2).getAssignedItem() instanceof Logical);

            //a plug cannot be connected to a signal
            instance = new Plug(module, "thePlug");
            subPlug  = new Plug(module, "theSubPlug");
            signalSource = new Logical(module, "theSignalSource");
            signalSink = new Logical(module, "theSignalSink");
            subSignalSource = new Logical(module, "theSubSignalSource");
            subSignalSink = new Logical(module, "theSubSignalSink");
            subPlug.addPin(0,true).assign(subSignalSource);
            subPlug.addPin(1,false).assign(subSignalSink);
            instance.addPin(0).assign(subPlug);
            instance.addPin(1,true).assign(signalSource);
            instance.addPin(2,false).assign(signalSink);

            jack = new Plug(module, "theJack");
            subJack  = new Plug(module, "theSubJack");
            jackSignalSource = new Logical(module, "theJackSignalSource");
            jackSignalSink = new Logical(module, "theJackSignalSink");
            subJackSignalSource = new Logical(module, "theSubJackSignalSource");
            subJackSignalSink = new Logical(module, "theSubJackSignalSink");
            subJack.addPin(1,true).assign(subJackSignalSource);
            subJack.addPin(0,false).assign(subJackSignalSink);
            jack.addPin(2).assign(subJack);
            jack.addPin(0,true).assign(jackSignalSource);
            jack.addPin(1,false).assign(jackSignalSink);
            try{
                instance.plug(jack);
                fail();
            }
            catch(PlugIncompatibleException ex){
                //exception must have been thrown
            }

            //a source must be connected to a sink
            instance = new Plug(module, "thePlug");
            subPlug  = new Plug(module, "theSubPlug");
            signalSource = new Logical(module, "theSignalSource");
            signalSink = new Logical(module, "theSignalSink");
            subSignalSource = new Logical(module, "theSubSignalSource");
            subSignalSink = new Logical(module, "theSubSignalSink");
            subPlug.addPin(0,true).assign(subSignalSource);
            subPlug.addPin(1,false).assign(subSignalSink);
            instance.addPin(0).assign(subPlug);
            instance.addPin(1,true).assign(signalSource);
            instance.addPin(2,false).assign(signalSink);

            jack = new Plug(module, "theJack");
            subJack  = new Plug(module, "theSubJack");
            jackSignalSource = new Logical(module, "theJackSignalSource");
            jackSignalSink = new Logical(module, "theJackSignalSink");
            subJackSignalSource = new Logical(module, "theSubJackSignalSource");
            subJackSignalSink = new Logical(module, "theSubJackSignalSink");
            subJack.addPin(0,false).assign(subJackSignalSource);
            subJack.addPin(1,true).assign(subJackSignalSink);
            jack.addPin(0).assign(subJack);
            jack.addPin(1,true).assign(jackSignalSource);
            jack.addPin(2,false).assign(jackSignalSink);
            try{
                instance.plug(jack);
                fail();//FAIL
            }
            catch(PlugIncompatibleException ex){
               //must never be thrown
            }

            //signals are incompatible
            instance = new Plug(module, "thePlug");
            subPlug  = new Plug(module, "theSubPlug");
            signalSource = new Logical(module, "theSignalSource");
            signalSink = new Logical(module, "theSignalSink");
            subSignalSource = new Logical(module, "theSubSignalSource");
            subSignalSink = new Logical(module, "theSubSignalSink");
            subPlug.addPin(0,true).assign(subSignalSource);
            subPlug.addPin(1,false).assign(subSignalSink);
            instance.addPin(0).assign(subPlug);
            instance.addPin(1,true).assign(signalSource);
            instance.addPin(2,false).assign(signalSink);

            jack = new Plug(module, "theJack");
            subJack  = new Plug(module, "theSubJack");
            jackSignalSource = new Decimal(module, "theJackSignalSource");
            jackSignalSink = new Logical(module, "theJackSignalSink");
            subJackSignalSource = new Logical(module, "theSubJackSignalSource");
            subJackSignalSink = new Logical(module, "theSubJackSignalSink");
            subJack.addPin(1,true).assign(subJackSignalSource);
            subJack.addPin(0,false).assign(subJackSignalSink);
            jack.addPin(0).assign(subJack);
            jack.addPin(2,true).assign(jackSignalSource);
            jack.addPin(1,false).assign(jackSignalSink);
            try{
                instance.plug(jack);
                fail();
            }
            catch(PlugIncompatibleException ex){
                //exception must have been thrown
            }

            //one pin is unassigned (instance side)
            instance = new Plug(module, "thePlug");
            subPlug  = new Plug(module, "theSubPlug");
            signalSource = new Logical(module, "theSignalSource");
            signalSink = new Logical(module, "theSignalSink");
            subSignalSource = new Logical(module, "theSubSignalSource");
            subSignalSink = new Logical(module, "theSubSignalSink");
            subPlug.addPin(0,true).assign(subSignalSource);
            subPlug.addPin(1,false).assign(subSignalSink);
            instance.addPin(0).assign(subPlug);
            //instance.addPin(1).assign(signalSource, true);
            instance.addPin(2,false).assign(signalSink);

            jack = new Plug(module, "theJack");
            subJack  = new Plug(module, "theSubJack");
            jackSignalSource = new Decimal(module, "theJackSignalSource");
            jackSignalSink = new Logical(module, "theJackSignalSink");
            subJackSignalSource = new Logical(module, "theSubJackSignalSource");
            subJackSignalSink = new Logical(module, "theSubJackSignalSink");
            subJack.addPin(1,true).assign(subJackSignalSource);
            subJack.addPin(0,false).assign(subJackSignalSink);
            jack.addPin(0).assign(subJack);
            jack.addPin(2,true).assign(jackSignalSource);
            jack.addPin(1,false).assign(jackSignalSink);
            try{
                instance.plug(jack);
                fail();
            }
            catch(PlugIncompatibleException ex){
                //exception must have been thrown
            }

            //one pin is unassigned (jack side)
            instance = new Plug(module, "thePlug");
            subPlug  = new Plug(module, "theSubPlug");
            signalSource = new Logical(module, "theSignalSource");
            signalSink = new Logical(module, "theSignalSink");
            subSignalSource = new Logical(module, "theSubSignalSource");
            subSignalSink = new Logical(module, "theSubSignalSink");
            subPlug.addPin(0,true).assign(subSignalSource);
            subPlug.addPin(1,false).assign(subSignalSink);
            instance.addPin(0).assign(subPlug);
            instance.addPin(1,true).assign(signalSource);
            instance.addPin(2,false).assign(signalSink);

            jack = new Plug(module, "theJack");
            subJack  = new Plug(module, "theSubJack");
            jackSignalSource = new Decimal(module, "theJackSignalSource");
            jackSignalSink = new Logical(module, "theJackSignalSink");
            subJackSignalSource = new Logical(module, "theSubJackSignalSource");
            subJackSignalSink = new Logical(module, "theSubJackSignalSink");
            subJack.addPin(1,true).assign(subJackSignalSource);
            subJack.addPin(0,false).assign(subJackSignalSink);
            jack.addPin(0).assign(subJack);
            jack.addPin(2,true).assign(jackSignalSource);
            //jack.addPin(1).assign(jackSignalSink, false);
            try{
                instance.plug(jack);
                fail();
            }
            catch(PlugIncompatibleException ex){
                //exception must have been thrown
            }
        }
        catch (PinAlreadyAssignedException ex) {
            Logger.getLogger(PlugTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        }
        catch (PinNotAssignedException ex) {
            Logger.getLogger(PlugTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        }
        catch(SignalAlreadyConnectedException ex){
            Logger.getLogger(PlugTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        }

    }

    /**
     * Test of getIdentifier method, of class Plug.
     */
    @Test
    public void testGetIdentifier() {
        System.out.println("getIdentifier");
        Plug instance = new Plug(module, "thePlug");
        assert(instance.getIdentifier() == "thePlug");
    }

    /**
     * Test of getQualifiedIdentifier method, of class Plug.
     */

    /**
     * Test of toString method, of class Plug.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
    }

    /**
     * Test of addPin method, of class Plug.
     */
    @Test
    public void testAddPin() {
        System.out.println("addPin");
        //see above
    }

    /**
     * Test of isPinAssigned method, of class Plug.
     */
    @Test
    public void testIsPinAssigned() {
        System.out.println("isPinAssigned");
    }

    /**
     * Test of getSignalPins method, of class Plug.
     */
    @Test
    public void testGetSignalPins() throws PinIncompatibleException {
        try {
            System.out.println("getSignalPins");
            Plug instance = new Plug(module, "thePlug");
            Plug subPlug = new Plug(module, "theSubPlug");
            Signal signalSource = new Logical(module, "theSignalSource");
            Signal signalSink = new Logical(module, "theSignalSink");
            Signal subSignalSource = new Logical(module, "theSubSignalSource");
            Signal subSignalSink = new Logical(module, "theSubSignalSink");
            subPlug.addPin(0,true).assign(subSignalSource);
            subPlug.addPin(1,false).assign(subSignalSink);
            instance.addPin(0).assign(subPlug);
            instance.addPin(1,true).assign(signalSource);
            instance.addPin(2,false).assign(signalSink);

            for (String id : instance.getQualifiedSignalPinIdentifiers()){
                System.out.println(id);
            }
            for (Pin is : instance.getSignalPins()){
                System.out.println(is);
            }

            System.out.println("\n>>>>>>>>>>>>> subPlug contained in module1");
            instance = new Plug(module, "thePlug");
            subPlug = new Plug(module1, "theSubPlug");
            signalSource = new Logical(module, "theSignalSource");
            signalSink = new Logical(module, "theSignalSink");
            subSignalSource = new Logical(module1, "theSubSignalSource");
            subSignalSink = new Logical(module1, "theSubSignalSink");
            subPlug.addPin(0,true).assign(subSignalSource);
            subPlug.addPin(1,false).assign(subSignalSink);
            instance.addPin(0).assign(subPlug);
            instance.addPin(1,true).assign(signalSource);
            instance.addPin(2,false).assign(signalSink);

            String[] ids = new String[]{"module1.theSubPlug.theSubSignalSource",
                                        "module1.theSubPlug.theSubSignalSink",
                                        "theSignalSource",
                                        "theSignalSink"};
            int i = 0;
            for (String id : instance.getQualifiedSignalPinIdentifiers()){
                System.out.println(id);
                assert(id.equals(ids[i++]));
            }
            for (Pin is : instance.getSignalPins()){
                System.out.println(is);
            }

            System.out.println("\n>>>>>>>>>>>>> signalSource contained in module2");
            instance = new Plug(module, "thePlug");
            subPlug = new Plug(module1, "theSubPlug");
            signalSource = new Logical(module2, "theSignalSource");
            signalSink = new Logical(module, "theSignalSink");
            subSignalSource = new Logical(module1, "theSubSignalSource");
            subSignalSink = new Logical(module1, "theSubSignalSink");
            subPlug.addPin(0,true).assign(subSignalSource);
            subPlug.addPin(1,false).assign(subSignalSink);
            instance.addPin(0).assign(subPlug);
            instance.addPin(1,true).assign(signalSource);
            instance.addPin(2,false).assign(signalSink);

            ids = new String[]{ "module1.theSubPlug.theSubSignalSource",
                                "module1.theSubPlug.theSubSignalSink",
                                "module2.theSignalSource",
                                "theSignalSink"};
            i = 0;
            for (String id : instance.getQualifiedSignalPinIdentifiers()){
                System.out.println(id);
                assert(id.equals(ids[i++]));
            }
            for (Pin is : instance.getSignalPins()){
                System.out.println(is);
            }

            System.out.println("\n>>>>>>>>>>>>> subsubPlug contained in module2");
            instance = new Plug(module, "thePlug");
            subPlug = new Plug(module1, "theSubPlug");
            Plug subsubPlug = new Plug(module2,"theSubSubPlug");
            signalSource = new Logical(module, "theSignalSource");
            signalSink = new Logical(module, "theSignalSink");
            subSignalSource = new Logical(module1, "theSubSignalSource");
            subSignalSink = new Logical(module1, "theSubSignalSink");
            Logical subsubSignalSource = new Logical(module2, "theSubsubSignalSource");
            Logical subsubSignalSink = new Logical(module2, "theSubsubSignalSink");
            subPlug.addPin(0,true).assign(subSignalSource);
            subPlug.addPin(1,false).assign(subSignalSink);
            subsubPlug.addPin(0,true).assign(subsubSignalSource);
            subsubPlug.addPin(1,false).assign(subsubSignalSink);
            instance.addPin(0).assign(subPlug);
            instance.addPin(1).assign(subsubPlug);
            instance.addPin(2,true).assign(signalSource);
            instance.addPin(3,false).assign(signalSink);

            ids = new String[]{ "module1.theSubPlug.theSubSignalSource",
                                "module1.theSubPlug.theSubSignalSink",
                                "module2.theSubSubPlug.theSubsubSignalSource",
                                "module2.theSubSubPlug.theSubsubSignalSink",
                                "theSignalSource",
                                "theSignalSink"};
            i = 0;
            for (String id : instance.getQualifiedSignalPinIdentifiers()){
                System.out.println(id);
                assert(id.equals(ids[i++]));
            }
            for (Pin is : instance.getSignalPins()){
                System.out.println(is);
            }

            System.out.println("\n>>>>>>>>>>>>> subsubPlug contained in module2 with subsubsubPlug assigned");
            instance = new Plug(module, "thePlug");
            subPlug = new Plug(module1, "theSubPlug");
            subsubPlug = new Plug(module2,"theSubSubPlug");
            Plug subsubsubPlug = new Plug(module2,"theSubSubSubPlug");
            signalSource = new Logical(module, "theSignalSource");
            signalSink = new Logical(module, "theSignalSink");
            subSignalSource = new Logical(module1, "theSubSignalSource");
            subSignalSink = new Logical(module1, "theSubSignalSink");
            subsubSignalSource = new Logical(module2, "theSubsubSignalSource");
            subsubSignalSink = new Logical(module2, "theSubsubSignalSink");
            Logical subsubsubSignalSource = new Logical(module2, "theSubsubsubSignalSource");
            Logical subsubsubSignalSink = new Logical(module2, "theSubsubsubSignalSink");

            subsubsubPlug.addPin(0,true).assign(subsubsubSignalSource,"sub3SignalSource");//named pin
            subsubsubPlug.addPin(1,false).assign(subsubsubSignalSink, "sub3SignalSink"); //named pin

            subsubPlug.addPin(0,true).assign(subsubSignalSource);
            subsubPlug.addPin(1,false).assign(subsubSignalSink);
            subsubPlug.addPin(2).assign(subsubsubPlug);

            subPlug.addPin(0,true).assign(subSignalSource);
            subPlug.addPin(1,false).assign(subSignalSink);
            subPlug.addPin(2).assign(subsubPlug);

            instance.addPin(0).assign(subPlug);
            instance.addPin(1,true).assign(signalSource);
            instance.addPin(2,false).assign(signalSink);

            ids = new String[]{ "module1.theSubPlug.theSubSignalSource",
                                "module1.theSubPlug.theSubSignalSink",
                                "module1.theSubPlug.module2.theSubSubPlug.theSubsubSignalSource",
                                "module1.theSubPlug.module2.theSubSubPlug.theSubsubSignalSink",
                                "module1.theSubPlug.module2.theSubSubPlug.theSubSubSubPlug.sub3SignalSource",
                                "module1.theSubPlug.module2.theSubSubPlug.theSubSubSubPlug.sub3SignalSink",
                                "theSignalSource",
                                "theSignalSink"};
            i = 0;
            for (String id : instance.getQualifiedSignalPinIdentifiers()){
                System.out.println(id);
                assert(id.equals(ids[i++]));
            }

            for (Pin is : instance.getSignalPins()){
                System.out.println(is);
            }

            System.out.println("\n>>>>>>>>>>>>> named pins");
            instance = new Plug(module, "thePlug");
            subPlug = new Plug(module1, "theSubPlug");
            subsubPlug = new Plug(module2,"theSubSubPlug");
            subsubsubPlug = new Plug(module2,"theSubSubSubPlug");
            signalSource = new Logical(module, "theSignalSource");
            signalSink = new Logical(module, "theSignalSink");
            subSignalSource = new Logical(module1, "theSubSignalSource");
            subSignalSink = new Logical(module1, "theSubSignalSink");
            subsubSignalSource = new Logical(module2, "theSubsubSignalSource");
            subsubSignalSink = new Logical(module2, "theSubsubSignalSink");
            subsubsubSignalSource = new Logical(module2, "theSubsubsubSignalSource");
            subsubsubSignalSink = new Logical(module2, "theSubsubsubSignalSink");

            subsubsubPlug.addPin(0,true).assign(subsubsubSignalSource,"sub3SignalSource");//named pin
            subsubsubPlug.addPin(1,false).assign(subsubsubSignalSink, "sub3SignalSink"); //named pin

            subsubPlug.addPin(0,true).assign(subsubSignalSource);
            subsubPlug.addPin(1,false).assign(subsubSignalSink);
            subsubPlug.addPin(2).assign(subsubsubPlug,"sub3Plug");//named pin

            subPlug.addPin(0,true).assign(subSignalSource);
            subPlug.addPin(1,false).assign(subSignalSink);
            subPlug.addPin(2).assign(subsubPlug,"sub2Plug");//named pin

            instance.addPin(0).assign(subPlug,"sub1Plug");//named pin
            instance.addPin(1,true).assign(signalSource);
            instance.addPin(2,false).assign(signalSink);

            ids = new String[]{ "sub1Plug.theSubSignalSource",
                                "sub1Plug.theSubSignalSink",
                                "sub1Plug.sub2Plug.theSubsubSignalSource",
                                "sub1Plug.sub2Plug.theSubsubSignalSink",
                                "sub1Plug.sub2Plug.sub3Plug.sub3SignalSource",
                                "sub1Plug.sub2Plug.sub3Plug.sub3SignalSink",
                                "theSignalSource",
                                "theSignalSink"};
            i = 0;
            for (String id : instance.getQualifiedSignalPinIdentifiers()){
                System.out.println(id);
                assert(id.equals(ids[i++]));
            }

            for (Pin is : instance.getSignalPins()){
                System.out.println(is);
            }

            System.out.println("\n>>>>>>>>>>>>> assign  pins");
            instance = new Plug(module, "thePlug");
            subPlug = new Plug(module1, "theSubPlug");
            subsubPlug = new Plug(module2,"theSubSubPlug");
            subsubsubPlug = new Plug(module2,"theSubSubSubPlug");
            signalSource = new Logical(module, "theSignalSource");
            signalSink = new Logical(module, "theSignalSink");
            subSignalSource = new Logical(module1, "theSubSignalSource");
            subSignalSink = new Logical(module1, "theSubSignalSink");
            subsubSignalSource = new Logical(module2, "theSubsubSignalSource");
            subsubSignalSink = new Logical(module2, "theSubsubSignalSink");
            subsubsubSignalSource = new Logical(module2, "theSubsubsubSignalSource");
            subsubsubSignalSink = new Logical(module2, "theSubsubsubSignalSink");

            subsubsubPlug.addPin(0,true).assign(subsubsubSignalSource);
            subsubsubPlug.addPin(1,false).assign(subsubsubSignalSink);

            subsubPlug.addPin(0,true).assign(subsubSignalSource);
            subsubPlug.addPin(1,false).assign(subsubSignalSink);
            subsubPlug.addPin(2).assign(subsubsubPlug);
            subsubPlug.addPin(3,true).assign(subsubsubPlug.getPin(0));

            subPlug.addPin(0,true).assign(subSignalSource);
            subPlug.addPin(1,false).assign(subSignalSink);
            subPlug.addPin(2).assign(subsubPlug);
            subPlug.addPin(3).assign(subsubPlug,"NamedSubSubPlugPin");//named

            instance.addPin(0,true).assign(subPlug.getPin(0));
            instance.addPin(1).assign(subPlug.getPin(2));
            instance.addPin(2,true).assign(subsubPlug.getPin(0));
            instance.addPin(3,true).assign(subsubPlug.getPin(0),"Pin0ofSubSubPlugPin");//named
            instance.addPin(4).assign(subsubPlug.getPin(2),"NamedSubSubPlugPin");//named
            instance.addPin(5).assign(subPlug.getPin(3));//named on previous level

            ids = new String[]{ "module1.theSubPlug.theSubSignalSource",
                                "module1.theSubPlug.module2.theSubSubPlug.theSubsubSignalSource",
                                "module1.theSubPlug.module2.theSubSubPlug.theSubsubSignalSink",
                                "module1.theSubPlug.module2.theSubSubPlug.theSubSubSubPlug.theSubsubsubSignalSource",
                                "module1.theSubPlug.module2.theSubSubPlug.theSubSubSubPlug.theSubsubsubSignalSink",
                                "module1.theSubPlug.module2.theSubSubPlug.theSubSubSubPlug.theSubsubsubSignalSource",
                                "module2.theSubSubPlug.theSubsubSignalSource",
                                "Pin0ofSubSubPlugPin",
                                "NamedSubSubPlugPin.theSubsubsubSignalSource",
                                "NamedSubSubPlugPin.theSubsubsubSignalSink",
                                "module1.theSubPlug.NamedSubSubPlugPin.theSubsubSignalSource",
                                "module1.theSubPlug.NamedSubSubPlugPin.theSubsubSignalSink",
                                "module1.theSubPlug.NamedSubSubPlugPin.theSubSubSubPlug.theSubsubsubSignalSource",
                                "module1.theSubPlug.NamedSubSubPlugPin.theSubSubSubPlug.theSubsubsubSignalSink",
                                "module1.theSubPlug.NamedSubSubPlugPin.theSubSubSubPlug.theSubsubsubSignalSource"};
            i = 0;
            for (String id : instance.getQualifiedSignalPinIdentifiers()){
                System.out.println(id);
                assert(id.equals(ids[i++]));
            }

            for (Pin is : instance.getSignalPins()){
                System.out.println(is);
            }
        } catch (PinAlreadyAssignedException ex) {
            Logger.getLogger(PlugTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        }
        catch (PinNotAssignedException ex) {
            Logger.getLogger(PlugTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        }
    }

    /**
     * Test of retrieveSignalPins method, of class Plug.
     */
    @Test
    public void testRetrieveSignalPins() {
        System.out.println("retrieveSignalPins");
     }

    /**
     * Test of getContainingModule method, of class Plug.
     */
    @Test
    public void testGetContainingModule() {
        System.out.println("getContainingModule");
    }

}