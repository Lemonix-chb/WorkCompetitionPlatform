package com.example.workcompetitionplatform.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API速率限制注解
 * 用于防止API滥用和DDoS攻击
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * 时间窗口内允许的最大请求次数
     */
    int value() default 10;

    /**
     * 时间窗口大小（秒）
     */
    int timeout() default 60;

    /**
     * 速率限制的消息提示
     */
    String message() default "请求过于频繁，请稍后再试";
}