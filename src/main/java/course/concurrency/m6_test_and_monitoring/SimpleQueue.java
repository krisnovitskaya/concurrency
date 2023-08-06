package course.concurrency.m6_test_and_monitoring;

public interface SimpleQueue<T> {
    void enqueue(T value);
    T dequeue();

    int size();
}
