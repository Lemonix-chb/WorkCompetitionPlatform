package com.example.workcompetitionplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 大模型评估结果DTO
 *
 * @author 陈海波
 * @since 2026-04-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LLMEvaluationResult {
    /**
     * 创新性评分（0-100）
     */
    private BigDecimal innovationScore;

    /**
     * 实用性评分（0-100）
     */
    private BigDecimal practicalityScore;

    /**
     * 用户体验评分（0-100）
     */
    private BigDecimal userExperienceScore;

    /**
     * 文档质量评分（0-100）
     */
    private BigDecimal documentationScore;

    /**
     * 评审摘要（自然语言）
     */
    private String summary;

    /**
     * 改进建议（分点列举）
     */
    private List<String> suggestions;

    /**
     * 使用的AI模型
     */
    private String aiModel;
}