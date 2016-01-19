/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : Queue<T>.java
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

import java.util.concurrent.LinkedBlockingQueue;

/**
 * a queue is useful in cases, where one producing module has to convey 
 * asynchronously produced items to one consuming module
 * @author berndschuster
 * @param <T> type of the items to queue
 */
public class Queue<T> {
    private SignedInteger          size;
    private Integer                maxSize;
    private AbstractModule         producer;
    private AbstractModule         consumer;
    private String                 identifier;
    
    private LinkedBlockingQueue<T> queue;
    
    private CyclicTaskRunner       cyclicTaskRunner;
    
    private int                    prodSize;
    private int                    consSize;
    
    public Queue(AbstractModule producer, AbstractModule consumer, String identifier, Integer maxSize) throws SignalAlreadyExistsException, WrongUseException{
        if (consumer == null){
            throw new WrongUseException("consumer must not be null");
        }
        if (producer == null){
            throw new WrongUseException("producer must not be null");
        }
        this.producer         = producer;
        this.consumer         = consumer;
        this.identifier       = identifier;
        this.size             = new SignedInteger(consumer, "queue." + identifier + ".size", 0);
        this.maxSize          = maxSize;
        this.queue            = new LinkedBlockingQueue<>();
        this.prodSize         = 0;
        this.consSize         = 0;
        this.cyclicTaskRunner = new CyclicTaskRunner();
        JPac.getInstance().registerCyclicTask(cyclicTaskRunner);
    }

    public Queue(AbstractModule consumer, AbstractModule producer, String identifier) throws SignalAlreadyExistsException, WrongUseException{
        this(consumer, producer, identifier, null);
    }

    /**
     * used to enqueue an item to the queue. Can only be called by the producing module.
     * @param item
     * @throws org.jpac.SignalAccessException  queue accessed by 
     * @throws org.jpac.InconsistencyException 
     */
    public void enqueue(T item) throws SignalAccessException, InconsistencyException{        
        if (!Thread.currentThread().equals(producer)){
            throw new SignalAccessException("queue " + this + " can only be enqueued by " + producer.getQualifiedName());
        }
        if (isFull()){
            throw new SignalAccessException("queue " + this + " full");            
        }
        
        try{
            queue.add(item);
            prodSize++;
        }
        catch(IllegalStateException exc){
            throw new InconsistencyException("failed to enqueue an item: " + exc);
        }
    }
    
    /**
     * used to dequeue an item from the queue. Can only be called by the consumer module (Provided, that it seized this queue before).
     * @throws SignalAccessException 
     */
    public T dequeue() throws SignalAccessException{
        T item = null;
        if (!Thread.currentThread().equals(consumer)){
            throw new SignalAccessException("queue " + this + " can only be dequeued by " + consumer.getQualifiedName());
        }
        if (isEmpty()){
            throw new SignalAccessException("queue " + this + " empty");                        
        }
        
        item = queue.poll();
        consSize--;  
        if (consSize < 0){
            consSize = 0;
        }
        return item;
    }
    
    /**
     * used to retrieve the head item of the queue without removing it.
     * @throws SignalAccessException 
     */
    public T peek() throws SignalAccessException{
        T item = null;
        if (!Thread.currentThread().equals(consumer)){
            throw new SignalAccessException("queue " + this + " can only be dequeued by " + consumer.getQualifiedName());
        }
        if (isEmpty()){
            throw new SignalAccessException("queue " + this + " empty");                        
        }

        item = queue.peek();
        return item;
    }
    
    /**
     * queue contains 'maxSize' items. No further items can be enqueued until items are dequeued
     * @return true = queue is full
     * @throws org.jpac.SignalAccessException
     */
    public boolean isFull() throws SignalAccessException{
        int actualSize = getSize();
        return maxSize != null ? actualSize >= maxSize : false;
    }

    /**
     * queue contains no items. No further items can be dequeued until new items are enqueued
     * @return true = queue is empty
     * @throws org.jpac.SignalAccessException
     */
    public boolean isEmpty() throws SignalAccessException{
        int actualSize = getSize();
        return actualSize == 0;
    }
    
    /**
     * queue contains items. Subsequent dequeue(), peek() operations will succeed
     * @return true = queue contains items
     * @throws org.jpac.SignalAccessException
     */
    public boolean containsItems() throws SignalAccessException{
        int actualSize = getSize();
        return actualSize > 0 && (maxSize != null ? actualSize <= maxSize : true);        
    }
    
    /**
     * 
     * @return size of the queue depending of the module calling it:
     *         producer: can not be less than the size of the queue at the beginning 
     *                   of the cycle and increases by one on every successful enqueue() 
     *                   operation.
     *         consumer: can not be greater than the size of the queue at the beginning
     *                   of the cycle and decreases by one on every successful dequeue()
     *                   operation
     * @throws org.jpac.SignalAccessException 
     */
    public int getSize() throws SignalAccessException{
      int result = 0;
      if (Thread.currentThread().equals(producer)){
          result = prodSize;
      } else if (Thread.currentThread().equals(consumer)){
          result = consSize;
      } else if (Thread.currentThread().equals(JPac.getInstance())){
          result = queue.size();
      } else {
          throw new SignalAccessException("queue must not be accessed by a module which is neither producer nor consumer");
      }
      return result;
    }
    
    /**
     * @return a process event which will be fired, as soon as the queue contains at least one item
     */
    public ProcessEvent notEmpty(){
        ProcessEvent pe = new ProcessEvent(){
            @Override
            public boolean fire() throws ProcessException {
                return containsItems();
            };
        };
        return pe;
    }

    /**
     * @return a process event which will be fired, as soon as the queue is able to receive at least one new item
     */
    public ProcessEvent notFull(){
        ProcessEvent pe = new ProcessEvent(){
            @Override
            public boolean fire() throws ProcessException {
                return !isFull();
            };
        };
        return pe;
    }
    
    private class CyclicTaskRunner implements CyclicTask{
        
        @Override
        public void run() {
            //update size values for both producer and consumer side
            prodSize = consSize = queue.size();
            try{size.set(prodSize);}catch(NumberOutOfRangeException | SignalAccessException exc){/*cannot happen*/};
        }
        @Override
        public void prepare() {/*nothing to do*/}
        @Override
        public void stop() {/*nothing to do*/}
        @Override
        public boolean isFinished() {return true;}//default
    } 
    
    @Override
    public String toString(){
        return consumer.getQualifiedName() + ".queue." + identifier + "(" + queue.size() +")";
    }
}
