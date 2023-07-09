package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicReference;

public class AuctionOptimistic implements Auction {

    private Notifier notifier;

    public AuctionOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }


    private AtomicReference<Bid> latestBid = new AtomicReference<>(new Bid(null, null, 0L));

//    public boolean propose(Bid bid) {
//        Bid temp = null;
//        Bid resultCompare = null;
//        try {
//            while (true) {
//                temp = latestBid.get();
//                if (bid.getPrice() > temp.getPrice()) {
//                    resultCompare = latestBid.compareAndExchange(temp, bid);
//                    if (temp == resultCompare) {
//                        notifier.sendOutdatedMessage(temp);
//                        return true;
//                    }
//                } else {
//                    return false;
//                }
//            }
//        } catch (NullPointerException e){
//            while (true) {
//                temp = latestBid.get();
//                if (temp == null || bid.getPrice() > temp.getPrice()) {
//                    resultCompare = latestBid.compareAndExchange(temp, bid);
//                    if (temp == resultCompare) {
//                        notifier.sendOutdatedMessage(temp);
//                        return true;
//                    }
//                } else {
//                    return false;
//                }
//            }
//        }
//    }


    public boolean propose(Bid bid) {
        Bid temp = null;
        Bid resultCompare = null;

        do{
            temp = latestBid.get();
            if (bid.getPrice() > temp.getPrice()) {
                resultCompare = latestBid.compareAndExchange(temp, bid);
            } else {
                return false;
            }
        }while (temp == resultCompare);

        notifier.sendOutdatedMessage(temp);
        return true;

    }

    public Bid getLatestBid() {
        return latestBid.get();
    }
}
