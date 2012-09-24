/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jpac.alarm;

import java.util.Observable;
import java.util.Observer;
import org.apache.log4j.Logger;
import org.jpac.EventTimedoutException;
import org.jpac.OutputInterlockException;
import org.jpac.InputInterlockException;
import org.jpac.PeriodOfTime;
import org.jpac.ProcessException;
import org.jpac.AbstractModule;
import org.jpac.Module;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author berndschuster
 */
public class AlarmTest implements Observer {
    static  Logger Log = Logger.getLogger("jpac.Alarm");    

    public void update(Observable o, Object o1) {
        Log.info("UPDATE receiving changed alarm " + o1);
    }
    class AlarmConsumerModule extends Module{
        
        
        public AlarmConsumerModule(AbstractModule m){
            super(m);
        }

        @Override
        protected void work() throws ProcessException {
            PeriodOfTime delay = new PeriodOfTime(1000 * ms);
            for(int i = 0; i < 1; i++){
                Log.info("AlarmConsumerModule: waiting for the alarm to be raised");
                alarm.raised().await();
                Log.info("AlarmConsumerModule: alarm raised : " + alarm.isPending() + " ack: " + alarm.isAcknowledged());
                alarm.gone().await();
                Log.info("AlarmConsumerModule: alarm gone : " + alarm.isPending() + " ack: " + alarm.isAcknowledged());
                alarm.acknowledged().await();
                Log.info("AlarmConsumerModule: alarm acknoledged : " + alarm.isPending() + " ack: " + alarm.isAcknowledged());
                try{
                    alarm.acknowledged().await(1000 * ms);
                }
                catch(EventTimedoutException exc){
                    //should occur, because this alarm had been acknowledged before
                    Log.info("AlarmConsumerModule: acknowledge event only occurs for one cycle: ok : " + alarm.isPending() + " ack: " + alarm.isAcknowledged());
                }
            }
        }

        protected void initialize() {
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
    
    class AlarmTestModule extends Module{
        AlarmConsumerModule consMod;
        
        public AlarmTestModule(AbstractModule m){
            super(m);
            consMod = new AlarmConsumerModule(this);
        }

        @Override
        protected void work() throws ProcessException {
            PeriodOfTime delay = new PeriodOfTime(1000 * ms);
            for(int i = 0; i < 1; i++){
                delay.await();
                Log.info("AlarmTestModule: alarm raise()");
                alarm.raise();
                delay.await();
                alarm.reset();
                alarm.acknowledged().await();
                Log.info("AlarmTestModule: alarm acknowledge() received");
                try{
                    alarm.acknowledged().await(1000 * ms);
                }
                catch(EventTimedoutException exc){
                    //should occur, because this alarm had been acknowledged before
                    Log.info("AlarmTestModule: acknowledge event only occurs for one cycle: ok");
                }
                alarmROA.raise();
                Log.info("AlarmTestModule: wait for acknowledgement on alarmROA");
                alarmROA.acknowledged().await();
                Log.info("AlarmTestModule: acknowledgement on alarmROA received");
                alarmROA.raise();
            }
        }

        @Override
        public void start() {
            consMod.start();
            super.start();
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

    public Alarm alarm;
    public Alarm alarmROA;
    
    public AlarmTest() {
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
     * Test of set method, of class Alarm.
     */
    @Test
    public void test(){
        try{
            Log.info("alarm");
            AlarmQueue.getInstance().addObserver(this);
            AlarmQueue.getInstance().addObserver(this);//must cause an ERROR entry inside the log
            AlarmTestModule module = new AlarmTestModule(null);
            alarm    = new Alarm(module,"TheTestAlarm"," this is a true alarm", false);
            alarmROA = new Alarm(module,"TheTestAlarmROA"," this is a true alarm with reset on acknowledgement", true);
            module.start();
            Log.info("main: wait for isPending()");
            while(!alarm.isPending()) Thread.sleep(200);
            Log.info("main: isPending() received");
            try{
                //alarm not pending. acknowledge() must throw an exception
                alarm.acknowledge();
                assert(false);
            }
            catch(AlarmPendingException exc){
                Log.info("AlarmPendingException occured as expected");
            }
            Log.info("main: wait for !isPending()");
            while(alarm.isPending())Thread.sleep(200);
            try{
                //alarm not pending. acknowledge() must throw an exception
                alarm.acknowledge();
            }
            catch(AlarmPendingException exc){
                Log.info("AlarmPendingException not expected here !!!!");
                assert(false);
            }
            Log.info("main: wait for alarmROA.raise()");
            while(!alarmROA.isPending())Thread.sleep(200);
            alarmROA.acknowledge();
            Thread.sleep(2000);
            Log.info("main: alarmROA.isPending(): " + alarmROA.isPending());
            assert(!alarm.isPending());
            Thread.sleep(2000);
        }
        catch(Exception exc){
            Log.error("Error: ", exc);
        }
    }

}
