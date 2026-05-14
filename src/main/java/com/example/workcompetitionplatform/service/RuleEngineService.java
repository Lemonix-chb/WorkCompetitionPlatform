package com.example.workcompetitionplatform.service;

import com.example.workcompetitionplatform.entity.AIReviewDetail;
import com.example.workcompetitionplatform.entity.Work;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 规则引擎服务接口
 *
 * @author 陈海波
 * @since 2026-04-13
 */
public interface RuleEngineService {

    /**
     * 执行基础规范检查
     *
     * @param submissionId 提交ID
     * @param workType 作品类型
     * @param files 关键文件
     * @return 检查详情列表
     */
    List<AIReviewDetail> performBasicChecks(Long submissionId, Work.WorkType workType, Map<String, List<File>> files);

    /**
     * 检查目录结构（程序设计作品）
     *
     * @param dir 解压后的目录
     * @return 检查详情
     */
    AIReviewDetail checkDirectoryStructure(File dir);

    /**
     * 检查文件命名规范
     *
     * @param files 文件列表
     * @param workType 作品类型
     * @return 检查详情
     */
    AIReviewDetail checkFileNaming(List<File> files, Work.WorkType workType);

    /**
     * 计算代码注释覆盖率（程序设计作品）
     *
     * @param codeFiles 代码文件列表
     * @return 检查详情
     */
    AIReviewDetail calculateCommentCoverage(List<File> codeFiles);

    /**
     * 检查演示文稿页数
     *
     * @param pptFile PPT文件
     * @return 检查详情
     */
    AIReviewDetail checkPPTPageCount(File pptFile);

    /**
     * 检查视频时长
     *
     * @param videoFile 视频文件
     * @return 检查详情
     */
    AIReviewDetail checkVideoDuration(File videoFile);

    /**
     * 计算基础规范评分
     *
     * @param basicChecks 基础检查结果列表
     * @return 基础规范评分（0-100）
     */
    BigDecimal calculateBasicScore(List<AIReviewDetail> basicChecks);
}