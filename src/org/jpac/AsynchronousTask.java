/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : AsynchronousTask.java
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
 *
 */

package org.jpac;

/**
 *
 * @author berndschuster
 */
public abstract class AsynchronousTask{
    private TaskRunner task;
    private Finished   finishedEvent;
    private String     identifier;
    private long       minimumDuration;
    private long       maximumDuration;

    /**
     * constructs a asynchronous task
     * @param identifier identifier of the asynchronous task. 
     */
    public AsynchronousTask(String identifier){
        this.task            = null;
        this.finishedEvent   = new Finished();
        this.identifier      = identifier;
        this.minimumDuration = Long.MAX_VALUE;
        this.maximumDuration = Long.MIN_VALUE;
    }
    
    /**
     * constructs a asynchronous task
     * using the class name as its identifier. 
     */
    public AsynchronousTask(){
        this(null);
        this.identifier = getClass().getSimpleName();
    }

    /**
     * Invokes the asynchronous task on separate thread. On first call a thread (task runner) is started which
     * calls the doIt() method. The thread keeps running after doIt() returns and waiting for its
     * next invocation by start(). The thread keeps on running, until terminate() is called or the application shuts down.
     * @throws WrongUseException thrown, when start() is called outside the context of a module
     */
    public void start() throws WrongUseException{
        if (!(Thread.currentThread() instanceof AbstractModule)){
            throw new WrongUseException("must be invoked inside a module");
        }
        if (task == null){
            task = new TaskRunner(((AbstractModule)Thread.currentThread()).getQualifiedName() + '.' + identifier);
        }
        //start task runner
        task.invoke(true);
    }

    /**
     * Used to savely stop the task runner
     * @throws WrongUseException thrown, when start() is called outside the context of a module
     */
    public void terminate() throws WrongUseException{
        if (!(Thread.currentThread() instanceof AbstractModule)){
            throw new WrongUseException("must be invoked inside a module");
        }
        if (task != null){
            //stop task runner
            task.invoke(false);
            //a new task runner
            task = null;
        }
    }
    
    public ProcessEvent finished(){
        return finishedEvent;
    }
    
    public String getIdentifier(){
        return identifier;
    }
    
    public abstract void doIt()throws ProcessException;
    
    private class TaskRunner extends Thread{
        private boolean done;
        private boolean startRequested;
        private boolean stopRequested;
        
        public TaskRunner(String identifier){
            this.setName(identifier);
            startRequested = false;
            stopRequested  = false;
        }
        
        @Override
        public void run(){
            long duration;
            long startTime;
            do{
                try{
                    //perform task
                    startTime = System.nanoTime();
                    doIt();
                    duration = System.nanoTime() - startTime;
                    if (duration < getMinimumDuration()){
                        minimumDuration = duration;
                    }
                    if (duration > getMaximumDuration()){
                        maximumDuration = duration;
                    }
                }
                catch(ProcessException exc){
                    finishedEvent.setProcessException(new AsynchronousTaskException(exc));
                }
                catch(Exception exc){
                    finishedEvent.setProcessException(new AsynchronousTaskException(exc));
                }
                catch(Error exc){
                    finishedEvent.setProcessException(new AsynchronousTaskException(exc));
                };
                //denote completion
                done = true;
                //wait for next invocation
                synchronized(this){
                    do{
                        try{wait();}catch(InterruptedException exc){}                
                        }
                    while(!startRequested && !stopRequested);
                    startRequested = false;
                }
            }
            while(!stopRequested);
        }
        
        public void invoke(boolean startStop){
            if (startStop){ 
                startRequested = true;
                stopRequested  = false;
            }
            else{
                startRequested = false;
                stopRequested  = true;
            }
            if (!this.isAlive() && startRequested){
                done = false;
                start();
            }
            else{
                done = false;
                synchronized(this){
                    notify();
                }
            }
        }
        
        public boolean isDone(){
            return done;
        }
    }
    
    /**
     * 
     * @return true, if the asynchronous task has come to an end 
     */
    public boolean isFinished(){
        return task.isDone();
    }

    /**
     * used to retrieve the minimum duration of doIt()
     * @return the minimumDuration
     */
    public long getMinimumDuration() {
        return minimumDuration;
    }

    /**
     * used to retrieve the maximum duration of doIt()
     * @return the maximumDuration
     */
    public long getMaximumDuration() {
        return maximumDuration;
    }
    
    private class Finished extends ProcessEvent{
        @Override
        public boolean fire() throws ProcessException {
            return isFinished();
        }
    }
}
