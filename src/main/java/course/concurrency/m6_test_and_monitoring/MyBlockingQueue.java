package course.concurrency.m6_test_and_monitoring;


import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MyBlockingQueue<T> implements SimpleQueue<T>{
    private static final int START_HEAD_INDEX = 0;
    private static final int START_TAIL_INDEX = -1;
    private final int capacity;
    private int size;
    private T[] data;

    private int headIndex;
    private int tailIndex;

    private final ReentrantLock lock;
    private final Condition enqueueCond;
    private final Condition dequeueCond;
    public MyBlockingQueue(int capacity) {
        if(capacity <= 0) throw new UnsupportedOperationException("queue capacity must be > 0");
        this.capacity = capacity;
        this.size = 0;
        this.headIndex = START_HEAD_INDEX;
        this.tailIndex = START_TAIL_INDEX;
        this.data = (T[]) new Object[capacity];
        this.lock = new ReentrantLock();
        this.enqueueCond = lock.newCondition();
        this.dequeueCond = lock.newCondition();
    }

    @Override
    public void enqueue(T value) {
        try {
            lock.lock();
            while (true) {
                if (size < capacity) {
                    if (tailIndex < capacity - 1) {
                        tailIndex++;
                    } else {
                        tailIndex = 0;
                    }
                    data[tailIndex] = value;
                    size++;
                    dequeueCond.signalAll();
                    break;
                } else {
                    enqueueCond.await();
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public T dequeue() {
        try {
            lock.lock();
            while (true) {
                if (size > 0) {
                    T returnedValue = data[headIndex];
                    size--;
                    headIndex++;
                    if(headIndex >= capacity){
                        headIndex = START_HEAD_INDEX;
                    }
                    enqueueCond.signalAll();
                    return returnedValue;
                } else {
                    dequeueCond.await();
                }
            }
        }catch (InterruptedException e){
            throw new RuntimeException();
        }finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        try {
            lock.lock();
            return size;
        }finally {
            lock.unlock();
        }
    }


}
