package course.concurrency.exams.auction;


import java.util.concurrent.*;

public class Notifier {
    private final ExecutorService service = ForkJoinPool.commonPool();
//    private final ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() -1);

//    ExecutorService service = ForkJoinPool.commonPool();
//   ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);

    public void sendOutdatedMessage(Bid bid) {
        service.execute(() -> imitateSending());
    }

    private void imitateSending() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
    }

    public void shutdown() {
        service.shutdownNow();
    }
}
