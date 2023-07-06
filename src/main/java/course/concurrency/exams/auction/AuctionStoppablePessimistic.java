package course.concurrency.exams.auction;

public class AuctionStoppablePessimistic implements AuctionStoppable {
    private final Object lock = new Object();
    private Notifier notifier;

    public AuctionStoppablePessimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private Bid latestBid;

    private boolean stopped;

    public boolean propose(Bid bid) {
        synchronized (lock) {
            if (stopped) {
                return false;
            }

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

    public Bid stopAuction() {
        synchronized (lock) {
            this.stopped = true;
            return latestBid;
        }
    }
}
