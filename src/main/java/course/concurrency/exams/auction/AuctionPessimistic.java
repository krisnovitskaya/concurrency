package course.concurrency.exams.auction;

public class AuctionPessimistic implements Auction {
    private final Object lock = new Object();
    private Notifier notifier;

    public AuctionPessimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private Bid latestBid;

    public boolean propose(Bid bid) {
        synchronized (lock) {
            if (latestBid == null || bid.getPrice() > latestBid.getPrice()) {
                notifier.sendOutdatedMessage(latestBid);
                latestBid = bid;
                return true;
            }
            return false;
        }
    }

    public Bid getLatestBid() {
        synchronized (lock) {
            return latestBid;
        }
    }
}
