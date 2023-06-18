package course.concurrency.m2_async.cf.min_price;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PriceAggregator {

    private Executor executor = Executors.newCachedThreadPool();
    private PriceRetriever priceRetriever = new PriceRetriever();

    public void setPriceRetriever(PriceRetriever priceRetriever) {
        this.priceRetriever = priceRetriever;
    }

    private Collection<Long> shopIds = Set.of(10l, 45l, 66l, 345l, 234l, 333l, 67l, 123l, 768l);

    public void setShops(Collection<Long> shopIds) {
        this.shopIds = shopIds;
    }

    public double getMinPrice(long itemId) {
        CompletableFuture<Double>[] priceFuture = shopIds.stream().map(shopId -> prepareResult(itemId, shopId)).toArray(CompletableFuture[]::new);
        CompletableFuture<Void> allOf = CompletableFuture.allOf(priceFuture);
        allOf.join();
        return countMin(priceFuture);
    }

    private CompletableFuture<Double> prepareResult(long itemId, long shopId){
        return CompletableFuture.supplyAsync(() -> priceRetriever.getPrice(itemId, shopId), executor)
                .completeOnTimeout(Double.NaN, 2900, TimeUnit.MILLISECONDS)
                .exceptionally(throwable ->Double.NaN);
//                .completeOnTimeout(Double.POSITIVE_INFINITY, 2900, TimeUnit.MILLISECONDS)
//                .exceptionally(throwable ->Double.POSITIVE_INFINITY);
    }

    private double countMin(CompletableFuture<Double>[] donePrices){
        return Arrays.stream(donePrices).map(cf -> cf.join()).min(Double::compareTo).orElse(Double.NaN);
//        double result = Arrays.stream(donePrices).mapToDouble(f -> f.join()).min().orElse(Double.NaN);
//        return result == Double.POSITIVE_INFINITY ? Double.NaN : result;
    }
}
