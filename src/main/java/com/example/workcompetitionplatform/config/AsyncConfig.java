package com.example.workcompetitionplatform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务线程池配置
 * 用于 AI 审核等耗时任务的异步执行
 *
 * @author 陈海波
 * @since 2026-04-20
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * AI 审核线程池
     * 限制并发数以控制 DeepSeek API 调用频率
     *
     * @return 线程池执行器
     */
    @Bean("aiReviewExecutor")
    public Executor aiReviewExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数：同时处理 2 个 AI 审核
        executor.setCorePoolSize(2);

        // 最大线程数：高峰期最多 5 个并发
        executor.setMaxPoolSize(5);

        // 队列容量：等待队列最多 10 个任务
        executor.setQueueCapacity(10);

        // 线程名称前缀
        executor.setThreadNamePrefix("ai-review-");

        // 拒绝策略：调用者运行（防止任务丢失）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 等待任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();
        return executor;
    }
}