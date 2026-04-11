package com.example.workcompetitionplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.workcompetitionplatform.entity.AIReviewDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI评审详情Mapper接口
 * 继承MyBatis Plus的BaseMapper，提供基础CRUD操作
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Mapper
public interface AIReviewDetailMapper extends BaseMapper<AIReviewDetail> {

    /**
     * 根据AI评审报告ID查询所有详情
     *
     * @param aiReviewReportId AI评审报告ID
     * @return AI评审详情列表
     */
    List<AIReviewDetail> selectByAiReviewReportId(@Param("aiReviewReportId") Long aiReviewReportId);

    /**
     * 根据评审维度查询AI评审详情
     *
     * @param reviewDimension 评审维度
     * @return AI评审详情列表
     */
    List<AIReviewDetail> selectByReviewDimension(@Param("reviewDimension") AIReviewDetail.ReviewDimension reviewDimension);

    /**
     * 根据评审等级查询AI评审详情
     *
     * @param reviewLevel 评审等级
     * @return AI评审详情列表
     */
    List<AIReviewDetail> selectByReviewLevel(@Param("reviewLevel") AIReviewDetail.ReviewLevel reviewLevel);
}