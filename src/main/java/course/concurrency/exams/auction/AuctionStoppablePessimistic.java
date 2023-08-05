package course.concurrency.exams.auction;

public class AuctionStoppablePessimistic implements AuctionStoppable {

    private final Object lock = new Object();

    private Notifier notifier;

    public AuctionStoppablePessimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private volatile Bid latestBid = new Bid(null, null, -1L);

    private volatile boolean isStopped = false;

    public boolean propose(Bid bid) {
        if(isStopped){
            return false;
        }
        if(bid.getPrice() > latestBid.getPrice()){
            synchronized (lock){
                if(bid.getPrice() > latestBid.getPrice()){
                    if(!isStopped) {
                        notifier.sendOutdatedMessage(latestBid);
                        latestBid = bid;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Bid getLatestBid() {
        return latestBid;
    }

    public Bid stopAuction() {
        isStopped = true;
        synchronized (lock) {
            return latestBid;
        }
    }
}
