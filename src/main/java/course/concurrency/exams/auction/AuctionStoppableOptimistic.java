package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicReference;

public class AuctionStoppableOptimistic implements AuctionStoppable {

    private Notifier notifier;

    public AuctionStoppableOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private AtomicMarkableReference<Bid> latestBid = new AtomicMarkableReference<>(null, false);

    public boolean propose(Bid bid) {
        boolean[] mark = new boolean[1];
        Bid temp = latestBid.get(mark);
        if(mark[0]) {
            return false;
        }
        if (temp == null || bid.getPrice() > temp.getPrice()) {
            if (latestBid.compareAndSet(temp, bid, false, false)) {
                notifier.sendOutdatedMessage(temp);
                return true;
            }
        }
        return false;
    }

    public Bid getLatestBid() {
        return latestBid.get(new boolean[1]);
    }

    public Bid stopAuction() {
        for (; ;) {
            if(latestBid.attemptMark(latestBid.getReference(), true)){
                break;
            }
        }
        return latestBid.getReference();
    }
}
