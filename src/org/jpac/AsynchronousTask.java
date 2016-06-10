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
 * Asynchronous tasks can be used to implement long term functions which might exceed the cycle time of an
 * elbfisch application. They can be instantiated and started by modules.
 * @author berndschuster
 */
public abstract class AsynchronousTask{
    private TaskRunner task;
    private Finished   finishedEvent;
    private String     identifier;
    private long       minimumDuration;
    private long       maximumDuration;
    private boolean    taskTerminated;

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
     * Invokes the asynchronous task on a separate thread. On first call a thread (task runner) is started which
     * calls the doIt() method. The thread keeps running after doIt() returns. Afterwards it waits for its
     * next invocation by start(). The thread ends when terminate() is called or the application shuts down.
     * @throws WrongUseException thrown, when start() is called outside the context of a module or jPac
     */
    public void start() throws WrongUseException{
        if (!((Thread.currentThread() instanceof AbstractModule) || (Thread.currentThread() instanceof JPac))){
            throw new WrongUseException("must be invoked inside a module or by jPac");
        }
        if (task == null || taskTerminated){
            String taskId;
            if (Thread.currentThread() instanceof AbstractModule){
                taskId = ((AbstractModule)Thread.currentThread()).getQualifiedName() + '.' + identifier;
            } 
            else {
                taskId = "JPac." + identifier;
            }
            taskTerminated = false;
            task = new TaskRunner(taskId);
        }
        //start task runner
        task.invoke(true);
    }

    /**
     * Used to savely stop the task runner
     * @throws WrongUseException thrown, when start() is called outside the context of a module
     */
    public void terminate() throws WrongUseException{
        if (!((Thread.currentThread() instanceof AbstractModule) || (Thread.currentThread() instanceof JPac))){
            throw new WrongUseException("must be invoked inside a module or by jPac");
        }
        if (task != null){
            //stop task runner
            task.invoke(false);
        }
        taskTerminated = true;
    }
    
    public boolean isTerminated(){
        return taskTerminated;
    }
    
    /**
     * returns a ProcessEvent which indicates the conclusion of an asynchronous task
     * @return a ProcessEvent which indicates the conclusion of an asynchronous task
     */
    public ProcessEvent finished(){
        return finishedEvent;
    }
    
    /**
     * 
     * @return the identifier of the asynchronous task 
     */
    public String getIdentifier(){
        return identifier;
    }
    
    /**
     * must implemented to fulfill the application specific function of this AsynchronousTask.
     * Is called on every call of start(). The invoking module can await() its conclusion by calling
     * asynchTask.finished.await().
     * @throws ProcessException when the user code produces it
     */
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
                synchronized(this){
                    //denote completion
                    done = true;
                    //and wait for next invocation
                    do{
                        try{wait();}catch(InterruptedException exc){}                
                        }
                    while(!startRequested && !stopRequested);
                    startRequested = false;
                }
            }
            while(!stopRequested);
            //denote completion
            stopRequested = false;
            synchronized(this){
                done = true;
            }
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
            boolean returnValue;
            synchronized(this){
                returnValue = done;
            }
            return returnValue;
        }
    }
    
    /**
     * 
     * @return true, if the asynchronous task has come to an end 
     */
    public boolean isFinished(){
        return task == null || task.isDone();
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
