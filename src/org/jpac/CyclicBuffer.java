/**
 * PROJECT   : jPac java process automation controller
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
 *
 * @author berndschuster
 * used to synchronize two threads having a producer - consumer relation
 * the data objects stored inside the buffer are recycled as far as possible
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
    
    public void clear(){
        synchronized(this){
            init();
        }
    }
    
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

    public boolean isEmpty(){
        return empty;
    }
    
    public boolean isFull(){
        return full;
    }    
}
