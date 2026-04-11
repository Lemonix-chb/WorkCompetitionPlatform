package com.example.workcompetitionplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 评委评分实体类
 * 对应数据库表：judge_review
 * 使用MyBatis Plus注解
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("judge_review")
public class JudgeReview {

    /**
     * 评审ID（主键自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 作品提交ID
     */
    @TableField(value = "submission_id")
    private Long submissionId;

    /**
     * 团队ID
     */
    @TableField(value = "team_id")
    private Long teamId;

    /**
     * 评委ID
     */
    @TableField(value = "judge_id")
    private Long judgeId;

    /**
     * 创新性得分
     */
    @TableField(value = "innovation_score")
    private BigDecimal innovationScore;

    /**
     * 实用性得分
     */
    @TableField(value = "practicality_score")
    private BigDecimal practicalityScore;

    /**
     * 用户体验得分
     */
    @TableField(value = "user_experience_score")
    private BigDecimal userExperienceScore;

    /**
     * 综合得分
     */
    @TableField(value = "overall_score")
    private BigDecimal overallScore;

    /**
     * 评审意见
     */
    @TableField(value = "review_comment")
    private String reviewComment;

    /**
     * 评审时间
     */
    @TableField(value = "review_time")
    private LocalDateTime reviewTime;

    /**
     * 评审状态：DRAFT-草稿, SUBMITTED-已提交, CONFIRMED-已确认
     */
    @TableField(value = "status")
    private ReviewStatus status = ReviewStatus.DRAFT;

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
     * 提交编号（显示字段）
     */
    @TableField(exist = false)
    private String submissionCode;

    /**
     * 作品名称（显示字段）
     */
    @TableField(exist = false)
    private String workName;

    /**
     * 团队名称（显示字段）
     */
    @TableField(exist = false)
    private String teamName;

    /**
     * 评委姓名（显示字段）
     */
    @TableField(exist = false)
    private String judgeName;

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
     * 提交时间（显示字段，用于待分配列表）
     */
    @TableField(exist = false)
    private LocalDateTime submissionTime;

    /**
     * 提交状态（显示字段，用于待分配列表）
     */
    @TableField(exist = false)
    private String submissionStatus;

    /**
     * 评审状态枚举
     */
    public enum ReviewStatus {
        DRAFT,      // 草稿
        SUBMITTED,  // 已提交
        CONFIRMED   // 已确认
    }
}