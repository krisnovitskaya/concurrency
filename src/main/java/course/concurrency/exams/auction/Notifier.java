package course.concurrency.exams.auction;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public class Notifier {

//    ExecutorService service = ForkJoinPool.commonPool();
    ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);

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
