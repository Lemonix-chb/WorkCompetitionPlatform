package com.example.workcompetitionplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 作品提交实体类
 * 对应数据库表：submission
 * 使用MyBatis Plus注解
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("submission")
public class Submission {

    /**
     * 提交ID（主键自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联作品ID
     */
    @TableField(value = "work_id")
    private Long workId;

    /**
     * 提交团队ID
     */
    @TableField(value = "team_id")
    private Long teamId;

    /**
     * 提交编号
     */
    @TableField(value = "submission_code")
    private String submissionCode;

    /**
     * 实际提交人（队长）ID
     */
    @TableField(value = "submitter_id")
    private Long submitterId;

    /**
     * 原始文件名
     */
    @TableField(value = "file_name")
    private String fileName;

    /**
     * 存储路径
     */
    @TableField(value = "file_path")
    private String filePath;

    /**
     * 文件大小(MB)
     */
    @TableField(value = "file_size_mb")
    private BigDecimal fileSizeMb;

    /**
     * 作品类型：CODE-程序设计, PPT-演示文稿, VIDEO-数媒动漫与短视频
     */
    @TableField(value = "file_type")
    private FileType fileType;

    /**
     * 提交版本号
     */
    @TableField(value = "version")
    private Integer version;

    /**
     * 提交时间
     */
    @TableField(value = "submission_time")
    private LocalDateTime submissionTime;

    /**
     * 提交状态：UPLOADED-已上传, VALIDATING-校验中, VALID-校验通过, INVALID-校验失败, SUBMITTED-已提交, REVIEWED-已评审
     */
    @TableField(value = "status")
    private SubmissionStatus status;

    /**
     * 文件校验结果
     */
    @TableField(value = "validation_result")
    private String validationResult;

    /**
     * 是否为最终提交版本
     */
    @TableField(value = "is_final_version")
    private Boolean isFinalVersion = false;

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

    // ========== 显示字段（非数据库字段） ==========

    /**
     * 团队名称（显示字段）
     */
    @TableField(exist = false)
    private String teamName;

    /**
     * 作品名称（显示字段）
     */
    @TableField(exist = false)
    private String workName;

    /**
     * 赛事ID（显示字段）
     */
    @TableField(exist = false)
    private Long competitionId;

    /**
     * 赛道ID（显示字段）
     */
    @TableField(exist = false)
    private Long trackId;

    /**
     * 赛道名称（显示字段）
     */
    @TableField(exist = false)
    private String trackName;

    /**
     * 作品类型枚举
     */
    public enum FileType {
        CODE,   // 程序设计
        PPT,    // 演示文稿
        VIDEO   // 数媒动漫与短视频
    }

    /**
     * 提交状态枚举
     */
    public enum SubmissionStatus {
        UPLOADED,    // 已上传
        VALIDATING,  // 校验中
        VALID,       // 校验通过
        INVALID,     // 校验失败
        SUBMITTED,   // 已提交
        REVIEWED     // 已评审
    }
}