package course.concurrency.exams.auction;

import java.util.concurrent.*;

public class Notifier {
    private final ExecutorService service = ForkJoinPool.commonPool();
//    private final ForkJoinPool service = ForkJoinPool.commonPool();

//        private final ExecutorService service = Executors.newSingleThreadExecutor();
//    private final ExecutorService service = Executors.newFixedThreadPool(20_000);
//    private final ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() -1);

    public void sendOutdatedMessage(Bid bid) {
//        System.out.println(service.getPoolSize() + " " + service.getStealCount());
        service.submit(() -> imitateSending());
    }

    private void imitateSending() {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
    }

    public void shutdown() {service.shutdownNow();}
}
