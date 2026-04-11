package com.example.workcompetitionplatform.aspect;

import com.example.workcompetitionplatform.annotation.RateLimit;
import com.example.workcompetitionplatform.dto.ApiResponse;
import com.example.workcompetitionplatform.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * API速率限制切面
 * 实现基于用户的速率限制功能
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    /**
     * 用户请求计数器存储
     * Key: userId:methodName
     * Value: RequestCounter
     */
    private final Map<String, RequestCounter> requestCounters = new ConcurrentHashMap<>();

    /**
     * 环绕通知，拦截带有@RateLimit注解的方法
     *
     * @param joinPoint 切点
     * @return 方法执行结果
     * @throws Throwable 异常
     */
    @Around("@annotation(com.example.workcompetitionplatform.annotation.RateLimit)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        // 获取用户ID（未登录用户使用IP地址）
        String userId = getUserIdOrIp();

        // 构建计数器Key
        String key = userId + ":" + method.getName();

        // 获取或创建计数器
        RequestCounter counter = requestCounters.computeIfAbsent(key, k -> new RequestCounter(rateLimit.timeout()));

        // 检查是否超过限制
        if (counter.isExpired()) {
            counter.reset();
        }

        int currentCount = counter.incrementAndGet();
        if (currentCount > rateLimit.value()) {
            log.warn("用户 {} 触发速率限制：方法 {}, 当前次数 {}, 限制 {}",
                    userId, method.getName(), currentCount, rateLimit.value());
            return ApiResponse.error(rateLimit.message());
        }

        log.debug("用户 {} 调用方法 {}，当前次数 {}", userId, method.getName(), currentCount);

        // 执行原方法
        return joinPoint.proceed();
    }

    /**
     * 获取用户ID或IP地址
     */
    private String getUserIdOrIp() {
        try {
            Long userId = UserContext.getCurrentUserId();
            if (userId != null) {
                return String.valueOf(userId);
            }
        } catch (Exception e) {
            // 用户未登录
        }
        // 未登录用户使用默认标识
        return "anonymous";
    }

    /**
     * 请求计数器内部类
     */
    private static class RequestCounter {
        private final AtomicInteger count;
        private final long windowMillis;
        private volatile long startTime;

        public RequestCounter(int timeoutSeconds) {
            this.count = new AtomicInteger(0);
            this.windowMillis = timeoutSeconds * 1000L;
            this.startTime = System.currentTimeMillis();
        }

        public int incrementAndGet() {
            return count.incrementAndGet();
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - startTime > windowMillis;
        }

        public void reset() {
            count.set(0);
            startTime = System.currentTimeMillis();
        }
    }

    /**
     * 清理过期计数器（定时任务）
     * 每5分钟执行一次，防止内存泄漏
     */
    @Scheduled(fixedRate = 300000)
    public void cleanExpiredCounters() {
        requestCounters.entrySet().removeIf(entry -> entry.getValue().isExpired());
        log.debug("清理过期速率限制计数器，当前数量：{}", requestCounters.size());
    }
}