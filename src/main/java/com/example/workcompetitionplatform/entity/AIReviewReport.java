package com.example.workcompetitionplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI初审报告实体类
 * 对应数据库表：ai_review_report
 * 使用MyBatis Plus注解
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_review_report")
public class AIReviewReport {

    /**
     * 报告ID（主键自增）
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
     * 综合得分
     */
    @TableField(value = "overall_score")
    private BigDecimal overallScore;

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
     * 代码质量得分
     */
    @TableField(value = "code_quality_score")
    private BigDecimal codeQualityScore;

    /**
     * 文档质量得分
     */
    @TableField(value = "documentation_score")
    private BigDecimal documentationScore;

    /**
     * 重复率(%)
     */
    @TableField(value = "duplicate_rate")
    private BigDecimal duplicateRate;

    /**
     * 结构复杂度：LOW-低, MEDIUM-中, HIGH-高
     */
    @TableField(value = "complexity_level")
    private ComplexityLevel complexityLevel;

    /**
     * 风险等级：LOW-低, MEDIUM-中, HIGH-高
     */
    @TableField(value = "risk_level")
    private RiskLevel riskLevel;

    /**
     * 评审摘要
     */
    @TableField(value = "review_summary")
    private String reviewSummary;

    /**
     * 改进建议
     */
    @TableField(value = "improvement_suggestions")
    private String improvementSuggestions;

    /**
     * 评审时间
     */
    @TableField(value = "review_time")
    private LocalDateTime reviewTime;

    /**
     * 使用的AI模型
     */
    @TableField(value = "ai_model")
    private String aiModel;

    /**
     * 创建时间（自动填充）
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 评审状态枚举
     */
    public enum ReviewStatus {
        PENDING,    // 待评审
        IN_PROGRESS,// 评审中
        COMPLETED,  // 已完成
        FAILED      // 评审失败
    }

    /**
     * 复杂度等级枚举
     */
    public enum ComplexityLevel {
        LOW,     // 低
        MEDIUM,  // 中
        HIGH     // 高
    }

    /**
     * 风险等级枚举
     */
    public enum RiskLevel {
        LOW,     // 低
        MEDIUM,  // 中
        HIGH     // 高
    }
}