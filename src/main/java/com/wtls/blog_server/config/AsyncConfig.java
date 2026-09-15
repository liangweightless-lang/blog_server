package com.wtls.blog_server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 通用异步任务线程池配置
 * 支持在方法上使用 @Async 标注直接进行多线程异步执行
 */
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    @Override
    @Bean(name = "taskExecutor")
    public ThreadPoolTaskExecutor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int cpuCores = Runtime.getRuntime().availableProcessors();
        
        // 核心线程数：CPU 核心数 * 2
        executor.setCorePoolSize(Math.max(4, cpuCores * 2));
        // 最大线程数
        executor.setMaxPoolSize(Math.max(16, cpuCores * 4));
        // 阻塞缓冲队列大小
        executor.setQueueCapacity(500);
        // 空闲线程存活时间（秒）
        executor.setKeepAliveSeconds(60);
        // 线程名前缀
        executor.setThreadNamePrefix("blog-async-");
        // 拒绝策略：由调用者线程直接执行，确保高并发满载时不丢失任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 容器关闭时等待所有异步任务执行完成（优雅停机）
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (Throwable throwable, Method method, Object... params) -> {
            log.error(">>> [异步任务异常] 执行方法: {} 发生未捕获异常, 参数: {}", method.getName(), params, throwable);
        };
    }
}
