package com.webank.wedpr.components.dataset.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasetExecutorConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatasetExecutorConfig.class);

    @Value("${wedpr.dataset.threadPool.namePrefix}")
    private String namePrefix;

    @Value("${wedpr.dataset.threadPool.corePoolSize}")
    private int corePoolSize;

    @Value("${wedpr.dataset.threadPool.maxPoolSize}")
    private int maxPoolSize;

    @Value("${wedpr.dataset.threadPool.queueCapacity}")
    private int queueCapacity;

    @Bean(name = "datasetAsyncExecutor")
    public ThreadPoolTaskExecutor newThreadPoolTaskExecutor() {

        // Thread Pool
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(namePrefix);
        // In the case of a full queue, the task is directly rejected and a
        // RejectedExecutionException is thrown.
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());

        executor.initialize();

        logger.info(
                "initialize dataset thread pool, namePrefix: {}, corePoolSize: {}, maxPoolSize: {}, queueCapacity: {}",
                namePrefix,
                corePoolSize,
                maxPoolSize,
                queueCapacity);

        return executor;
    }
}
