package com.sse.rcp.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

@Slf4j
@Data
@EnableAsync
@Configuration
@ConfigurationProperties(prefix = "spring.threads")
public class ThreadPoolConfig {

    private int corePoolSize;
    private int maxPoolSize;
    private int queueCapacity;
    private int keepAliveSeconds;
    private boolean waitForTasksToCompleteOnShutdown;
    private int awaitTerminationSeconds;
    private String threadNamePrefix;

    @Bean("taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(corePoolSize);
        taskExecutor.setMaxPoolSize(maxPoolSize);
        taskExecutor.setQueueCapacity(queueCapacity);
        taskExecutor.setKeepAliveSeconds(keepAliveSeconds);
        taskExecutor.setThreadNamePrefix(threadNamePrefix);
        taskExecutor.setWaitForTasksToCompleteOnShutdown(waitForTasksToCompleteOnShutdown);
        taskExecutor.setAwaitTerminationSeconds(awaitTerminationSeconds);
        // 装饰Runnable
        taskExecutor.setTaskDecorator(new TaskDecorator() {
            @Override
            public Runnable decorate(Runnable runnable) {
                return () -> {
                    Exception stackRecord = new Exception();
                    try {

                        runnable.run();
                        if (runnable instanceof Future) {
                            ((Future) runnable).get();
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        log.error("taskExecutor task exp:{}", e.getMessage());
                        log.error("taskExecutor Error Stack Trace!!! -> :{}", stackRecord.getStackTrace());
                        throw new IllegalThreadStateException(e.getMessage());
                    } catch (Exception e) {
                        log.error("taskExecutor task Error! -> :{}", e.getMessage());
                        log.error("taskExecutor Error Stack Trace!!! -> :{}", stackRecord.getStackTrace());
                        throw e;
                    }
                };
            }
        });
        return taskExecutor;
    }
}
