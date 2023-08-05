package course.concurrency.m3_shared.immutable;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class OrderService {

    private ConcurrentHashMap<Long, Order> currentOrders = new ConcurrentHashMap<>();
    private AtomicLong nextId = new AtomicLong(0L);


    public long createOrder(List<Item> items) {
        long id = nextId.incrementAndGet();
        currentOrders.put(id, Order.createNewOrder(id, items));
        return id;
    }

    public void updatePaymentInfo(long orderId, PaymentInfo paymentInfo) {
        Order updated = currentOrders.computeIfPresent(orderId, (id, oldVal) -> oldVal.withPaymentInfo(paymentInfo));
        checkAndDeliver(updated);
    }

    private void checkAndDeliver(Order order) {
        if (order.checkStatus()) {
            deliver(order);
        }
    }

    public void setPacked(long orderId) {
        Order updated = currentOrders.computeIfPresent(orderId, (id, oldVal) -> oldVal.withPacked(true));
        checkAndDeliver(updated);
    }

    private void deliver(Order order) {
        /* ... */
        currentOrders.computeIfPresent(order.getId(), (id, oldVal) -> oldVal.withStatus(Order.Status.DELIVERED));
    }

    public boolean isDelivered(long orderId) {
        return currentOrders.get(orderId).getStatus().equals(Order.Status.DELIVERED);
    }
}
