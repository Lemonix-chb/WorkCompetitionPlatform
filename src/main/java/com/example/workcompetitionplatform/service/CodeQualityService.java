package com.example.workcompetitionplatform.service;

import com.example.workcompetitionplatform.entity.AIReviewDetail;
import com.example.workcompetitionplatform.entity.AIReviewReport;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

/**
 * 代码质量分析服务接口
 *
 * @author 陈海波
 * @since 2026-04-13
 */
public interface CodeQualityService {

    /**
     * 检查代码重复率（使用JPlag）
     *
     * @param submissionId 提交ID
     * @param codeFiles 代码文件列表
     * @return 重复率（百分比）
     */
    BigDecimal checkDuplicateRate(Long submissionId, List<File> codeFiles);

    /**
     * 检查代码风格（使用Checkstyle）
     *
     * @param codeFile 代码文件
     * @return 代码风格检查详情
     */
    AIReviewDetail checkCodeStyle(File codeFile);

    /**
     * 分析函数复杂度
     *
     * @param codeFiles 代码文件列表
     * @return 复杂度等级
     */
    AIReviewReport.ComplexityLevel analyzeFunctionComplexity(List<File> codeFiles);

    /**
     * 计算代码质量评分
     *
     * @param styleCheckResult 风格检查结果
     * @param duplicateRate 重复率
     * @param complexityLevel 复杂度等级
     * @return 代码质量评分（0-100）
     */
    BigDecimal calculateCodeQualityScore(AIReviewDetail styleCheckResult, BigDecimal duplicateRate, AIReviewReport.ComplexityLevel complexityLevel);
}