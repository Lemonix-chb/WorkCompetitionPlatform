package com.example.workcompetitionplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 裁判统计数据DTO
 * 用于返回裁判首页和统计页面的详细数据
 *
 * @author 陈海波
 * @since 2026-04-20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JudgeStatsDTO {

    // ========== 基础统计 ==========

    /**
     * 待评审作品数量
     */
    private Integer pendingReviews;

    /**
     * 已评审作品数量
     */
    private Integer completedReviews;

    /**
     * 评审总评分
     */
    private Integer totalScores;

    /**
     * 负责赛道数量
     */
    private Integer assignedTracks;

    // ========== 详细统计 ==========

    /**
     * 平均评分
     */
    private BigDecimal averageScore;

    /**
     * 最高评分
     */
    private BigDecimal maxScore;

    /**
     * 最低评分
     */
    private BigDecimal minScore;

    // ========== 评分分布 ==========

    /**
     * 优秀作品数量（评分 >= 85）
     */
    private Integer excellentCount;

    /**
     * 良好作品数量（评分 >= 70 && < 85）
     */
    private Integer goodCount;

    /**
     * 中等作品数量（评分 >= 60 && < 70）
     */
    private Integer averageCount;

    /**
     * 较差作品数量（评分 < 60）
     */
    private Integer poorCount;

    // ========== 维度评分 ==========

    /**
     * 平均创新性得分
     */
    private BigDecimal avgInnovationScore;

    /**
     * 平均实用性得分
     */
    private BigDecimal avgPracticalityScore;

    /**
     * 平均用户体验得分
     */
    private BigDecimal avgUserExperienceScore;

    // ========== 时间统计 ==========

    /**
     * 本周评审数量
     */
    private Integer thisWeekReviews;

    /**
     * 本月评审数量
     */
    private Integer thisMonthReviews;

    /**
     * 平均评审用时（小时）
     */
    private BigDecimal avgReviewTimeHours;

    // ========== 赛道分布 ==========

    /**
     * 各赛道评审数量
     * key: 赛道名称, value: 评审数量
     */
    private Map<String, Integer> trackDistribution;

    /**
     * 各赛道平均评分
     * key: 赛道名称, value: 平均评分
     */
    private Map<String, BigDecimal> trackAverageScores;

    // ========== 最近评审趋势 ==========

    /**
     * 最近7天评审趋势
     * key: 日期（如 "2026-04-20"）, value: 评审数量
     */
    private Map<String, Integer> recentTrend;

    /**
     * 最近评审列表（最近5条）
     */
    private List<RecentReviewDTO> recentReviews;

    /**
     * 最近评审DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentReviewDTO {
        private Long reviewId;
        private String workName;
        private String teamName;
        private BigDecimal overallScore;
        private String reviewDate;
        private String status;
    }
}