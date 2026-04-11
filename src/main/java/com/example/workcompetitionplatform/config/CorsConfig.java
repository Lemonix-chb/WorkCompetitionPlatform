package com.example.workcompetitionplatform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * 跨域配置类
 * 配置允许的请求源、方法和头
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Configuration
public class CorsConfig {

    /**
     * 配置CORS过滤器
     *
     * @return CorsFilter实例
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 允许的请求源
        // 使用allowedOriginPatterns替代allowedOrigins，支持通配符
        config.setAllowedOriginPatterns(Arrays.asList("*"));

        // 允许的请求方法
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));

        // 允许的请求头
        config.setAllowedHeaders(Arrays.asList("*"));

        // 允许携带凭证（cookies、authorization headers等）
        config.setAllowCredentials(true);

        // 预检请求的缓存时间（秒）
        // 在此时间内，相同的跨域请求不需要再次发送预检请求
        config.setMaxAge(3600L);

        // 允许浏览器暴露的响应头
        // 默认情况下，浏览器只能访问Cache-Control、Content-Language、Content-Type、Expires、Last-Modified、Pragma
        // 如果需要访问其他响应头，需要在此配置
        config.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Disposition",
                "X-Requested-With"
        ));

        // 注册CORS配置
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}