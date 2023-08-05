package course.concurrency.m2_async.executors.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class CustomConfiguration {

    @Bean(name = "threadPoolTaskExecutor1")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(20);
        executor.setKeepAliveSeconds(10);
        executor.setThreadNamePrefix("Custom threadPoolTaskExecutor1=");
        executor.initialize();
        return executor;
    }

    @Bean(name = "threadPoolTaskExecutor2")
    public Executor asyncExecutor2() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(20);
        executor.setKeepAliveSeconds(10);
        executor.setThreadNamePrefix("Custom threadPoolTaskExecutor2=");
        executor.initialize();
        return executor;
    }

}
