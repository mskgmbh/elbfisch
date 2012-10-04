/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : CyclicBuffer.java
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

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * used to synchronize two threads having a producer - consumer relation
 * the data objects stored inside the buffer are recycled as far as possible
 * @author berndschuster
 * 
 */
public class CyclicBuffer<T> {
    private int       length;
    private int       tailIndex;
    private int       headIndex;
    private boolean   full;
    private boolean   empty;
    private Semaphore filledSem;
    private Semaphore freedSem;
    
    private ArrayList<T> buffer;
    
    public CyclicBuffer(int length){
        this.length    = length;
        this.buffer    = new ArrayList<T>(length);
        this.filledSem = new Semaphore(0);
        this.freedSem  = new Semaphore(length);        
        for (int i = 0; i < length; i++){
            this.buffer.add(null);
        }
        init();
    }
    
    private void init(){
        this.tailIndex = 0;
        this.headIndex = 0;
        this.empty     = true;
        this.full      = false;
        this.filledSem.drainPermits();
        int permits = freedSem.availablePermits();
        if (permits < length){
            this.freedSem.release(length - permits);
        }
    }
    
    /**
     * used to clear the buffer
     */
    public void clear(){
        synchronized(this){
            init();
        }
    }
    
    /**
     * returns the next free item inside the buffer to the producer. The item stored inside the cyclic buffer is returned for recycling purposes. 
     * The item cannot be accessed by the consumer, until it is put() again by the producer.
     * @return the item
     * @throws BufferFullException - thrown, if the cyclic buffer is full 
     */
    public T occupy() throws BufferFullException{
        synchronized(this){
            T item = null;
            if (!full){
                item = buffer.get(tailIndex);
            }
            else{
                throw new BufferFullException();            
            }
            return item;
        }
    }
        
    /**
     * used to put a new item into the cyclic buffer. Called by the producer 
     * @param t
     * @throws BufferFullException 
     */
    public void put(T t) throws BufferFullException{
        synchronized(this){
            if (!full){
                buffer.set(tailIndex, t);
                tailIndex++;
                if (tailIndex >= buffer.size()){
                   tailIndex = 0;
                }
                empty = false;
                full  = tailIndex == headIndex;
                filledSem.release();
            }
            else{
                throw new BufferFullException();
            }
        }
    }
    
    /**
     * used to release a data item, which has been fetched by calling get() (consumer)
     * @throws BufferEmptyException thrown, if the buffer does not contain any item
     */
    public void release() throws BufferEmptyException{
        synchronized(this){
            if (!empty){
                headIndex++;
                if (headIndex >= buffer.size()){
                   headIndex = 0;
                }
                full  = false;
                empty = headIndex == tailIndex;
                freedSem.release();
            }
            else{
                throw new BufferEmptyException();
            }
        }
    }
    
    /**
     * used to fetch the next item from the cyclic buffer on the consumer side
     * @return the item
     * @throws BufferEmptyException thrown, if the cyclic buffer does not contain at least one item 
     */
    public T get() throws BufferEmptyException{
        synchronized(this){
            T item = null;
            if (!empty){
               item = buffer.get(headIndex);
            }
            else{
                throw new BufferEmptyException();            
            }
            return item;
        }
    }
    
    /**
     * blocks a calling thread, until the cyclic buffer contains free items
     * @param timeout maximum period of time to wait [ms]
     * @return true, if a timeout occurred
     */
    public boolean waitUntilFreed(int timeout){
        boolean interrupted = false;
        boolean timedout    = false;
        long    timeoutTime = timeout * 1000000L + System.nanoTime();
        do{
           interrupted = false;
           long timeoutNanos = timeoutTime - System.nanoTime();
           timeoutNanos      = timeoutNanos < 0L ? 0L : timeoutNanos;
           try{timedout = !freedSem.tryAcquire(timeoutNanos, TimeUnit.NANOSECONDS);}catch(InterruptedException exc){interrupted = true;};
        }
        while(interrupted);
        return timedout;
    }
        
    /**
     * blocks a calling thread, until the cyclic buffer contains new items
     * @param timeout maximum period of time to wait [ms]
     * @return true, if a timeout occurred
     */
    public boolean waitUntilFilled(int timeout){
        boolean interrupted = false;
        boolean timedout    = false;
        long    timeoutTime = timeout * 1000000L + System.nanoTime();
        do{
           interrupted = false;
           long timeoutNanos = timeoutTime - System.nanoTime();
           timeoutNanos      = timeoutNanos < 0L ? 0L : timeoutNanos;
           try{timedout = !filledSem.tryAcquire(timeoutNanos, TimeUnit.NANOSECONDS);}catch(InterruptedException exc){interrupted = true;};
        }
        while(interrupted);
        return timedout;
    }
    
    /**
     * used to check, if the cyclic buffer is empty
     * @return true: the buffer is empty
     */
    public boolean isEmpty(){
        return empty;
    }
    
    /**
     * used to check, if the cyclic buffer is full
     * @return true: the buffer is full
     */
    public boolean isFull(){
        return full;
    }    
}
