package course.concurrency.m3_task;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DeadLockExample {

    public static void main(String[] args) throws InterruptedException {
        DeadLockData data11 = new DeadLockData(1, 1);
        DeadLockData data12 = new DeadLockData(1, 2);
        DeadLockData data21 = new DeadLockData(2, 1);
        DeadLockData data22 = new DeadLockData(2, 2);

        Map<DeadLockData, String> cMap = new ConcurrentHashMap<>();
        cMap.put(data11, "data11");
        cMap.put(data12, "data12");
        cMap.put(data21, "data21");
        cMap.put(data22, "data22");


        Thread thread1 = new Thread(() -> {
            cMap.compute(data11, (deadLockData, s) -> {
                try {
                    Thread.sleep(10);
                    return cMap.compute(data22, (deadLockData2, s2) -> s2.concat("done1"));
                } catch (InterruptedException e) {
                    return "fail1";
                }
            });
        });

        Thread thread2 = new Thread(() -> {
            cMap.computeIfPresent(data21, (deadLockData, s) -> {
                try {
                    Thread.sleep(10);
                    return cMap.compute(data11, (deadLockData2, s2) -> s2.concat("done2"));
                } catch (InterruptedException e) {
                    return "fail2";
                }
            });
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        //will not print
        System.out.println("DeadLock did not Happened");
    }


    static class DeadLockData{
        private final int someValue;

        private final int anotherValue;

        public DeadLockData(int someValue, int anotherValue) {
            this.someValue = someValue;
            this.anotherValue = anotherValue;
        }

        public int getAnotherValue() {
            return anotherValue;
        }

        public int getSomeValue() {
            return someValue;
        }


        @Override
        public int hashCode() {
            return someValue;
        }
    }
}
