package com.example.workcompetitionplatform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC配置类
 *
 * 注意：静态资源配置已在 application.properties 中通过 spring.mvc.static-path-pattern 配置
 * API路径匹配由Spring Boot默认处理，无需额外配置
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    // Spring Boot 默认配置已满足需求
    // 静态资源路径模式：/static/** (application.properties)
    // API路径精确匹配：默认行为
}