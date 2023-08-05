package course.concurrency.exams.auction;

public class AuctionPessimistic implements Auction {
    private final Object lock = new Object();
    private Notifier notifier;

    public AuctionPessimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private volatile Bid latestBid = new Bid(null, null, 0L);

    public boolean propose(Bid bid) {
            if(bid.getPrice() > latestBid.getPrice()){
                synchronized (lock){
                    if(bid.getPrice() > latestBid.getPrice()){
                        notifier.sendOutdatedMessage(latestBid);
                        latestBid = bid;
                        return true;
                    }
                    return false;
                }
            } return false;
    }

    public Bid getLatestBid() {
        return latestBid;
    }
}
