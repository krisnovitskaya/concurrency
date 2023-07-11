package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicReference;

public class AuctionOptimistic implements Auction {

    private Notifier notifier;

    public AuctionOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }


    private AtomicReference<Bid> latestBid = new AtomicReference<>(new Bid(null, null, 0L));

    public boolean propose(Bid bid) {
        Bid temp = null;
        do{
            temp = latestBid.get();
            if(bid.getPrice() <= temp.getPrice()) {
                return false;
            }
        }while (latestBid.compareAndSet(temp, bid));

        notifier.sendOutdatedMessage(temp);
        return true;
    }

    public Bid getLatestBid() {
        return latestBid.get();
    }
}
