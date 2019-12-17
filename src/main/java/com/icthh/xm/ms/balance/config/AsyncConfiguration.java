package com.icthh.xm.ms.balance.config;

import io.github.jhipster.async.ExceptionHandlingAsyncTaskExecutor;
import io.github.jhipster.config.JHipsterProperties;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfiguration implements AsyncConfigurer, SchedulingConfigurer {

    private final Logger log = LoggerFactory.getLogger(AsyncConfiguration.class);

    private final JHipsterProperties jhipsterProperties;

    public AsyncConfiguration(JHipsterProperties jhipsterProperties) {
        this.jhipsterProperties = jhipsterProperties;
    }

    @Override
    @Bean(name = "taskExecutor")
    public TaskExecutor getAsyncExecutor() {
        log.debug("Creating Async Task Executor");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(jhipsterProperties.getAsync().getCorePoolSize());
        executor.setMaxPoolSize(jhipsterProperties.getAsync().getMaxPoolSize());
        executor.setQueueCapacity(jhipsterProperties.getAsync().getQueueCapacity());
        executor.setThreadNamePrefix("balance-Executor-");
        return new ExceptionHandlingAsyncTaskExecutor(executor);
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(scheduledTaskExecutor());
    }

    @Bean
    public Executor scheduledTaskExecutor() {
        return Executors.newScheduledThreadPool(jhipsterProperties.getAsync().getCorePoolSize());
    }
}
