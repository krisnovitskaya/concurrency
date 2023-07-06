package course.concurrency.exams.auction;

import java.util.concurrent.*;

public class Notifier {
    private final ExecutorService service = ForkJoinPool.commonPool();

    public void sendOutdatedMessage(Bid bid) {
        CompletableFuture.runAsync(() -> imitateSending(), service);
    }

    private void imitateSending() {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
    }

    public void shutdown() {service.shutdown();}
}
