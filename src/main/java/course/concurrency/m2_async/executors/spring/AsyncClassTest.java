package course.concurrency.m2_async.executors.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

@Component
public class AsyncClassTest {

    @Autowired
    public ApplicationContext context;

    @Autowired
    @Qualifier("threadPoolTaskExecutor2")
    private Executor executor;

    @Async("threadPoolTaskExecutor1")
    public void runAsyncTask() {
        System.out.println("runAsyncTask: " + Thread.currentThread().getName());
//        executor.submit(this::internalTask);
        executor.execute(this::internalTask);
    }
    @Async("threadPoolTaskExecutor1")
    public void internalTask() {
        System.out.println("internalTask: " + Thread.currentThread().getName());
    }
}
