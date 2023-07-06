package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicReference;

public class AuctionOptimistic implements Auction {

    private Notifier notifier;

    public AuctionOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }


    private AtomicReference<Bid> latestBid = new AtomicReference<>();

    public boolean propose(Bid bid) {
        Bid temp = latestBid.get();
        if (temp == null || bid.getPrice() > temp.getPrice()) {
            if (latestBid.compareAndSet(temp, bid)) {
                notifier.sendOutdatedMessage(temp);
                return true;
            }
        }
        return false;
    }

    public Bid getLatestBid() {
        return latestBid.get();
    }
}
