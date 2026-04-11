package com.example.workcompetitionplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文件校验记录实体类
 * 对应数据库表：file_validation_log
 * 使用MyBatis Plus注解
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("file_validation_log")
public class FileValidationLog {

    /**
     * 校验记录ID（主键自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 提交记录ID
     */
    @TableField(value = "submission_id")
    private Long submissionId;

    /**
     * 校验类型：FORMAT-格式校验, SIZE-大小校验, CONTENT-内容校验, DUPLICATE-重复检测, STRUCTURE-结构校验
     */
    @TableField(value = "validation_type")
    private ValidationType validationType;

    /**
     * 校验结果：PASS-通过, FAIL-失败, WARNING-警告
     */
    @TableField(value = "validation_result")
    private ValidationResult validationResult;

    /**
     * 错误信息
     */
    @TableField(value = "error_message")
    private String errorMessage;

    /**
     * 错误位置（文件名/行号）
     */
    @TableField(value = "error_location")
    private String errorLocation;

    /**
     * 创建时间（自动填充）
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 校验类型枚举
     */
    public enum ValidationType {
        FORMAT,     // 格式校验
        SIZE,       // 大小校验
        CONTENT,    // 内容校验
        DUPLICATE,  // 重复检测
        STRUCTURE   // 结构校验
    }

    /**
     * 校验结果枚举
     */
    public enum ValidationResult {
        PASS,     // 通过
        FAIL,     // 失败
        WARNING   // 警告
    }
}