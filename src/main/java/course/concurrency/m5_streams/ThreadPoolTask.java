package course.concurrency.m5_streams;

import java.util.concurrent.*;

public class ThreadPoolTask {

    // Task #1
    public ThreadPoolExecutor getLifoExecutor() {

        return new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new BlockingStack<>());
    }

    // Task #2
    public ThreadPoolExecutor getRejectExecutor() {

        return new ThreadPoolExecutor8DiscardCustom();
    }

    static class ThreadPoolExecutor8DiscardCustom extends ThreadPoolExecutor {
        public ThreadPoolExecutor8DiscardCustom() {
            super(8, 8, 0L, TimeUnit.MILLISECONDS, new DiscardQueue(), new ThreadPoolExecutor.DiscardPolicy());
        }

        @Override
        public void setCorePoolSize(int corePoolSize) {
            //no reset
        }

        @Override
        public void setMaximumPoolSize(int maximumPoolSize) {
            //no reset
        }

        static class DiscardQueue<E> extends ArrayBlockingQueue<E> {

            public DiscardQueue() {
                super(1);
            }

            @Override
            public boolean offer(E o) {
                return false;
            }
        }
    }

    static class BlockingStack<E> extends LinkedBlockingDeque<E> {
        @Override
        public boolean offer(E e) {
            return super.offerFirst(e);
        }
    }

}
