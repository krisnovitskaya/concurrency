package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;

public class AuctionOptimistic implements Auction {

    private Notifier notifier;

    public AuctionOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private volatile Bid latestBid;

    public boolean propose(Bid bid) {
        Bid temp = latestBid;
        if (temp == null || bid.getPrice() > latestBid.getPrice()) {
            notifier.sendOutdatedMessage(temp);
            latestBid = bid;
            return true;
        }
        return false;
    }

    public Bid getLatestBid() {
        return latestBid;
    }
}
