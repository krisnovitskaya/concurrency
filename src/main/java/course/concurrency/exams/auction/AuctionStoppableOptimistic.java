package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicMarkableReference;

public class AuctionStoppableOptimistic implements AuctionStoppable {

    private Notifier notifier;

    public AuctionStoppableOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private AtomicMarkableReference<Bid> latestBid = new AtomicMarkableReference<>(new Bid(null, null, -1L), false);

    public boolean propose(Bid bid) {
        Bid temp = null;
        do{
            temp = latestBid.getReference();
            if(latestBid.isMarked() || bid.getPrice() <= temp.getPrice()){
                return false;
            }
        }while (!latestBid.compareAndSet(temp, bid, false, false));

        notifier.sendOutdatedMessage(temp);
        return true;
    }

    public Bid getLatestBid() {
        return latestBid.getReference();
    }

    public Bid stopAuction() {
        do{
            if (latestBid.isMarked()) {
                return latestBid.getReference();
            }
        } while (!latestBid.attemptMark(latestBid.getReference(), true));
        return latestBid.getReference();
    }
}
