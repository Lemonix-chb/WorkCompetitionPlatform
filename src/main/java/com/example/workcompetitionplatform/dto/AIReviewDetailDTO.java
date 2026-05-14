package com.example.workcompetitionplatform.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * AI评审详情DTO
 * 用于返回给学生查看的完整AI评审报告
 * 包含评审总结、亮点、不足、改进建议等详细信息
 *
 * @author 陈海波
 * @since 2026-05-03
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIReviewDetailDTO {

    /**
     * 报告ID
     */
    private Long id;

    /**
     * 提交ID
     */
    private Long submissionId;

    /**
     * 作品类型（CODE/PPT/VIDEO）
     */
    private String workType;

    /**
     * 综合得分（0-100）
     */
    private BigDecimal overallScore;

    /**
     * 创新性得分（0-25）
     */
    private BigDecimal innovationScore;

    /**
     * 实用性得分（0-25）
     */
    private BigDecimal practicalityScore;

    /**
     * 用户体验得分（0-25）
     */
    private BigDecimal userExperienceScore;

    /**
     * 文档质量得分（0-25）
     */
    private BigDecimal documentationScore;

    /**
     * 代码质量得分（CODE作品专用，0-25）
     */
    private BigDecimal codeQualityScore;

    /**
     * 创意得分（PPT作品专用，0-25）
     */
    private BigDecimal creativityScore;

    /**
     * 视觉效果得分（PPT/VIDEO作品，0-25）
     */
    private BigDecimal visualEffectScore;

    /**
     * 内容呈现得分（PPT作品专用，0-25）
     */
    private BigDecimal contentPresentationScore;

    /**
     * 原创性得分（PPT/VIDEO作品，PPT: 0-25, VIDEO: 0-20）
     */
    private BigDecimal originalityScore;

    /**
     * 故事性得分（VIDEO作品专用，0-30）
     */
    private BigDecimal storyScore;

    /**
     * 导演技巧得分（VIDEO作品专用，0-25）
     */
    private BigDecimal directorSkillScore;

    /**
     * 评审总结（200-500字详细评价）
     */
    private String reviewSummary;

    /**
     * 作品亮点列表（3-7条）
     */
    private List<String> strengths;

    /**
     * 不足之处列表（2-6条）
     */
    private List<String> weaknesses;

    /**
     * 改进建议列表（3-7条）
     */
    private List<String> improvementSuggestions;

    /**
     * 风险等级（LOW/MEDIUM/HIGH）
     */
    private String riskLevel;

    /**
     * 评审Agent类型（CodeReviewerAgent/PPTReviewerAgent/VideoAnalyzerAgent）
     */
    private String agentType;

    /**
     * AI模型名称
     */
    private String aiModel;

    /**
     * 评审时间
     */
    private String reviewTime;

    /**
     * 代码重复率（CODE作品专用）
     */
    private BigDecimal duplicateRate;
}