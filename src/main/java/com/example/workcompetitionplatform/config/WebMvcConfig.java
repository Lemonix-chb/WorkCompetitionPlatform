package com.example.workcompetitionplatform.config;

import org.springframework.lang.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC配置类
 * 配置路径匹配和静态资源处理
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 配置路径匹配
     * 确保API路径使用精确匹配，不会被静态资源处理器拦截
     *
     * @param configurer PathMatchConfigurer配置对象
     */
    @Override
    public void configurePathMatch(@NonNull PathMatchConfigurer configurer) {
        // 确保API路径使用精确匹配
        // Spring Boot 2.0+默认已禁用后缀模式匹配
    }

    /**
     * 配置静态资源处理
     * 明确指定静态资源路径，避免与API路径冲突
     *
     * @param registry ResourceHandlerRegistry注册器
     */
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // 配置静态资源路径（如果有）
        // registry.addResourceHandler("/static/**")
        //         .addResourceLocations("classpath:/static/");

        // 确保上传文件目录不会与API路径冲突
        // 不在这里配置上传文件路径，因为它们通过Controller处理
    }
}