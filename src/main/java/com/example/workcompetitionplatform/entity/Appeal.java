package com.example.workcompetitionplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 申诉实体类
 * 对应数据库表：appeal
 * 使用MyBatis Plus注解
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("appeal")
public class Appeal {

    /**
     * 申诉ID（主键自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 申诉团队ID
     */
    @TableField(value = "team_id")
    private Long teamId;

    /**
     * 作品提交ID
     */
    @TableField(value = "submission_id")
    private Long submissionId;

    /**
     * 申诉人（队长）ID
     */
    @TableField(value = "appellant_id")
    private Long appellantId;

    /**
     * 申诉类型：SCORE-分数申诉, VALIDATION-校验申诉, AI_REVIEW-AI评审申诉, OTHER-其他
     */
    @TableField(value = "appeal_type")
    private AppealType appealType;

    /**
     * 申诉内容
     */
    @TableField(value = "appeal_content")
    private String appealContent;

    /**
     * 申诉材料路径
     */
    @TableField(value = "appeal_materials")
    private String appealMaterials;

    /**
     * 申诉状态：PENDING-待处理, ACCEPTED-已接受, REJECTED-已拒绝, PROCESSING-处理中
     */
    @TableField(value = "status")
    private AppealStatus status = AppealStatus.PENDING;

    /**
     * 提交时间
     */
    @TableField(value = "submit_time")
    private LocalDateTime submitTime;

    /**
     * 处理时间
     */
    @TableField(value = "process_time")
    private LocalDateTime processTime;

    /**
     * 处理人（管理员）ID
     */
    @TableField(value = "processor_id")
    private Long processorId;

    /**
     * 处理结果
     */
    @TableField(value = "process_result")
    private String processResult;

    /**
     * 创建时间（自动填充）
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 申诉类型枚举
     */
    public enum AppealType {
        SCORE,       // 分数申诉
        VALIDATION,  // 校验申诉
        AI_REVIEW,   // AI评审申诉
        OTHER        // 其他
    }

    /**
     * 申诉状态枚举
     */
    public enum AppealStatus {
        PENDING,      // 待处理
        ACCEPTED,     // 已接受
        REJECTED,     // 已拒绝
        PROCESSING    // 处理中
    }
}