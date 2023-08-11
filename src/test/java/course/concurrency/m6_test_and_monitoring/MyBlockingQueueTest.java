package course.concurrency.m6_test_and_monitoring;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class MyBlockingQueueTest {


    @Test
    @DisplayName("check capacity when create")
    void capacityTest(){
        assertThrows(UnsupportedOperationException.class, () -> new MyBlockingQueue<>(0));
        assertThrows(UnsupportedOperationException.class, () -> new MyBlockingQueue<>(-1));
        assertDoesNotThrow(() -> new MyBlockingQueue<>(1));
    }

    @Test
    @DisplayName("check enqueue dequeue")
    void enqueueDequeueTest(){
        final int capacity = 5;
        SimpleQueue<String> simpleQueue = new MyBlockingQueue<>(capacity);

        final int expectedStartSize = 0;
        assertEquals(expectedStartSize, simpleQueue.size());

        simpleQueue.enqueue("val1");
        assertEquals(1 , simpleQueue.size());
        simpleQueue.enqueue("val2");
        assertEquals(2 , simpleQueue.size());


        simpleQueue.dequeue();
        assertEquals(1 , simpleQueue.size());
        simpleQueue.dequeue();
        assertEquals(expectedStartSize , simpleQueue.size());

    }

    @Test
    @DisplayName("check fifo")
    void fifoTest(){
        List<String> enqueueData = List.of("a", "b", "c",  "d", "f", "g");
        final int testSize = enqueueData.size();
        List<String> dequeueData = new ArrayList<>();
        MyBlockingQueue<String> simpleQueue = new MyBlockingQueue<>(testSize);

        for (String val : enqueueData) {
            simpleQueue.enqueue(val);
        }

        for (int i = 0; i < testSize; i++) {
            dequeueData.add(simpleQueue.dequeue());
        }

        assertEquals(enqueueData, dequeueData);
    }

    @Test
    @DisplayName("check thread is blocking when get from empty")
    void dequeueBlockingTest() throws InterruptedException {

        MyBlockingQueue<String> myBlockingQueue = new MyBlockingQueue<>(1);

        Thread dequeueThread = new Thread(() -> {
           myBlockingQueue.dequeue();
        });
        dequeueThread.setDaemon(true);
        dequeueThread.start();

        Thread.sleep(100);

        assertEquals("WAITING", dequeueThread.getState().name());


        //when add and wait
        myBlockingQueue.enqueue("any");

        Thread.sleep(100);

        //then
        assertEquals("TERMINATED", dequeueThread.getState().name());

        assertEquals(0, myBlockingQueue.size());
    }

    @Test
    @DisplayName("check thread is blocking when add to full")
    void enqueueBlockingTest() throws InterruptedException {
        String val1 = "val1";
        String val2 = "val2";
        final int capacity = 1;
        MyBlockingQueue<String> myBlockingQueue = new MyBlockingQueue<>(capacity);

        myBlockingQueue.enqueue(val1);
        assertEquals(capacity, myBlockingQueue.size());

        Thread enqueueThread = new Thread(() -> {
            myBlockingQueue.enqueue(val2);
        });
        enqueueThread.setDaemon(true);
        enqueueThread.start();

        Thread.sleep(100);

        assertEquals("WAITING", enqueueThread.getState().name());

        assertEquals(capacity, myBlockingQueue.size());

        //when dequeue 1
        myBlockingQueue.dequeue();
        //then wait and check enqueue2 done
        Thread.sleep(100);

        assertEquals(capacity, myBlockingQueue.size());
        assertEquals("TERMINATED", enqueueThread.getState().name());
    }


    @RepeatedTest(50)
    @DisplayName("full filling and removing")
    void fullFillRemoveTest() throws InterruptedException {
        final int expectedSize = 5;
        final int range = 50;

        Set<String> dataSet = IntStream.range(1, range + 1).boxed().map(integer -> "value".concat(integer.toString())).collect(Collectors.toSet());
        Set<String> resultSet = new ConcurrentSkipListSet<>();

        ExecutorService serviceAdd = Executors.newSingleThreadExecutor();
        ExecutorService serviceGet = Executors.newSingleThreadExecutor();

        MyBlockingQueue<String> queue = new MyBlockingQueue<>(expectedSize);


        serviceAdd.execute(() -> {
            for (String data : dataSet) {
                queue.enqueue(data);
            }
        });

        serviceGet.execute(() -> {
            for (String data : dataSet) {
                resultSet.add(queue.dequeue());
            }
        });


        serviceGet.awaitTermination(100, TimeUnit.MILLISECONDS);

        //queue empty
        assertEquals(0, queue.size());

        //assert result count
        assertEquals(range, resultSet.size());

        dataSet.removeAll(resultSet);
        //check data
        assertEquals(0, dataSet.size());
    }
}


