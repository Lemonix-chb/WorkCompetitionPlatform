package com.example.workcompetitionplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统配置实体类
 * 对应数据库表：system_config
 * 使用MyBatis Plus注解
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("system_config")
public class SystemConfig {

    /**
     * 配置ID（主键自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 配置键
     */
    @TableField(value = "config_key")
    private String configKey;

    /**
     * 配置值
     */
    @TableField(value = "config_value")
    private String configValue;

    /**
     * 配置类型：STRING-字符串, INT-整数, BOOLEAN-布尔, JSON-JSON对象
     */
    @TableField(value = "config_type")
    private ConfigType configType;

    /**
     * 配置说明
     */
    @TableField(value = "description")
    private String description;

    /**
     * 创建时间（自动填充）
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间（自动填充）
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 配置类型枚举
     */
    public enum ConfigType {
        STRING,   // 字符串
        INT,      // 整数
        BOOLEAN,  // 布尔
        JSON      // JSON对象
    }
}