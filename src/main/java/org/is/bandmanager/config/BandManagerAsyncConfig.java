package org.is.bandmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;


@Configuration
@EnableAsync
@EnableScheduling
public class BandManagerAsyncConfig {

    private final static int CORE_POOL_SIZE = 5;

    private final static int MAX_POOL_SIZE = 10;

    private final static int QUEUE_CAPACITY = 100;

    private final static int IMPORT_CORE_POOL_SIZE = 1;

    private final static int IMPORT_MAX_POOL_SIZE = 1;

    private final static int IMPORT_QUEUE_CAPACITY = 500;

    @Bean(name = "cleanupTaskExecutor")
    public TaskExecutor cleanUpTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix("cleanup-");
	    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean("subscriptionTaskExecutor")
    public TaskExecutor subscriptionTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix("subscription-");
	    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
	    executor.initialize();
        return executor;
    }

    @Bean("importTaskExecutor")
    public TaskExecutor importTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // Импорт выполняем последовательно: один поток + очередь задач
        executor.setCorePoolSize(IMPORT_CORE_POOL_SIZE);
        executor.setMaxPoolSize(IMPORT_MAX_POOL_SIZE);
        executor.setQueueCapacity(IMPORT_QUEUE_CAPACITY);
        executor.setThreadNamePrefix("import-");
	    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
	    executor.initialize();
        return executor;
    }

}
