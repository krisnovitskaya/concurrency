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
    @DisplayName("create test")
    void testCreate(){
        MyBlockingQueue<String> queue = new MyBlockingQueue<>(3);
        assertNotNull(queue);
    }


    @Test
    @DisplayName("add test")
    void testAdd() throws InterruptedException {
        final int expectedSize = 3;

        MyBlockingQueue<String> queue = new MyBlockingQueue<>(expectedSize);

        CountDownLatch latch = new CountDownLatch(expectedSize);

        for (int i = 0; i < expectedSize; i++) {
            Thread thread = new Thread(() -> {
                queue.enqueue(Thread.currentThread().getName());
                latch.countDown();
            });
            thread.start();
        }

        latch.await();

        //check fill max
        assertEquals(expectedSize, queue.size());

        //add more
        Thread more = new Thread(() -> {
            queue.enqueue(Thread.currentThread().getName());
        });
        more.start();

        Thread.sleep(100);
        //check fill more no add new
        assertEquals(expectedSize, queue.size());

        //check moreThread alive
        assertEquals(true, more.isAlive());

        String value = queue.dequeue();

        Thread.sleep(100);

        //check fill more add new
        assertEquals(expectedSize, queue.size());

        //check moreThread dead
        assertEquals(false, more.isAlive());
    }

    @Test
    @DisplayName("get test")
    void getTest() throws InterruptedException {
        final int expectedSize = 3;
        MyBlockingQueue<String> queue = new MyBlockingQueue<>(expectedSize);

        List<String> resultList = new CopyOnWriteArrayList<>();
        List<Thread> threadGetList = new ArrayList<>();

        CyclicBarrier barrier = new CyclicBarrier(expectedSize * 2);
        for (int i = 0; i < expectedSize + 1; i++) {
            threadGetList.add(new Thread(() -> {
                resultList.add(queue.dequeue());
                try {
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    throw new RuntimeException(e);
                }
            }));
        }

        threadGetList.forEach(Thread::start);



        for (int i = 0; i < expectedSize; i++) {
            Thread thread = new Thread(() -> {
                queue.enqueue(Thread.currentThread().getName());
                try {
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    throw new RuntimeException(e);
                }
            });
            thread.start();
        }

        while(!(barrier.getParties() == expectedSize *2)){
            //wait
        }

        long deadCount = threadGetList.stream().filter(t -> !t.isAlive()).count();

        //
        assertEquals(resultList.size(), deadCount);
    }


    @RepeatedTest(50)
    @DisplayName("full filling and removing")
    void fullFillRemoveTest() throws InterruptedException {
        final int expectedSize = 5;
        final int range = 50;

        Set<String> dataSet = IntStream.range(1, range + 1).boxed().map(integer -> "value".concat(integer.toString())).collect(Collectors.toSet());
        Set<String> resultSet = new ConcurrentSkipListSet<>();

        CountDownLatch latch = new CountDownLatch(range*2);

        MyBlockingQueue<String> queue = new MyBlockingQueue<>(expectedSize);

        for (String s : dataSet) {
            Thread thread = new Thread(() -> {
                queue.enqueue(s);
                latch.countDown();
            });
            thread.start();
        }


        for (int i = 0; i < dataSet.size(); i++) {
            Thread thread = new Thread(() -> {
                resultSet.add(queue.dequeue());
                latch.countDown();
            });
            thread.start();
        }

        latch.await();

        //queue empty
        assertEquals(0, queue.size());

        //assert result count
        assertEquals(range, resultSet.size());

        dataSet.removeAll(resultSet);
        //check data
        assertEquals(0, dataSet.size());

    }
}