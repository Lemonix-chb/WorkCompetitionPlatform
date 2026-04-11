package com.example.workcompetitionplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.workcompetitionplatform.entity.AIReviewReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI评审报告Mapper接口
 * 继承MyBatis Plus的BaseMapper，提供基础CRUD操作
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Mapper
public interface AIReviewReportMapper extends BaseMapper<AIReviewReport> {

    /**
     * 根据提交ID查询AI评审报告
     *
     * @param submissionId 提交ID
     * @return AI评审报告实体
     */
    AIReviewReport selectBySubmissionId(@Param("submissionId") Long submissionId);

    /**
     * 根据赛道ID查询AI评审报告列表
     *
     * @param competitionTrackId 赛道ID
     * @return AI评审报告列表
     */
    List<AIReviewReport> selectByCompetitionTrackId(@Param("competitionTrackId") Long competitionTrackId);

    /**
     * 根据评审状态查询AI评审报告列表
     *
     * @param reviewStatus 评审状态
     * @return AI评审报告列表
     */
    List<AIReviewReport> selectByReviewStatus(@Param("reviewStatus") AIReviewReport.ReviewStatus reviewStatus);

    /**
     * 根据赛事ID查询AI评审报告列表
     *
     * @param competitionId 赛事ID
     * @return AI评审报告列表
     */
    List<AIReviewReport> selectByCompetitionId(@Param("competitionId") Long competitionId);
}