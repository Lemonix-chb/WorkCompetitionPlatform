package com.example.workcompetitionplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 信息管理与智能评价系统主启动类
 * 湖南农业大学计算机作品赛平台
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableScheduling
public class WorkCompetitionPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkCompetitionPlatformApplication.class, args);
        System.out.println("========================================");
        System.out.println("信息管理与智能评价系统启动成功！");
        System.out.println("API文档地址: http://localhost:8080/doc.html");
        System.out.println("========================================");
    }

}
