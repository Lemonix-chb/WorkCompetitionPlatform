package com.example.workcompetitionplatform.service;

import com.example.workcompetitionplatform.dto.LLMEvaluationResult;
import com.example.workcompetitionplatform.entity.Work;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * 大模型评估服务接口
 *
 * @author 陈海波
 * @since 2026-04-13
 */
public interface LLMEvaluationService {

    /**
     * 调用大模型进行作品评估
     *
     * @param work 作品实体
     * @param keyFiles 关键文件（按类型分类）
     * @param workType 作品类型
     * @return 大模型评估结果
     */
    LLMEvaluationResult evaluateByLLM(Work work, Map<String, List<File>> keyFiles, Work.WorkType workType);

    /**
     * 生成评审摘要
     *
     * @param result 大模型评估结果
     * @return 评审摘要文本
     */
    String generateReviewSummary(LLMEvaluationResult result);

    /**
     * 生成改进建议
     *
     * @param result 大模型评估结果
     * @return 改进建议文本（换行分隔）
     */
    String generateImprovementSuggestions(LLMEvaluationResult result);
}