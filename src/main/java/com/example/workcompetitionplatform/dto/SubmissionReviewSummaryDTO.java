package com.example.workcompetitionplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 提交作品评审状态汇总DTO
 * 用于管理员查看所有提交作品的评审进度
 *
 * @author 陈海波
 * @since 2026-04-11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionReviewSummaryDTO {

    /**
     * 提交ID
     */
    private Long submissionId;

    /**
     * 评审结果ID（用于设置奖项）
     */
    private Long reviewResultId;

    /**
     * 提交编号
     */
    private String submissionCode;

    /**
     * 团队ID
     */
    private Long teamId;

    /**
     * 团队名称
     */
    private String teamName;

    /**
     * 作品ID
     */
    private Long workId;

    /**
     * 作品名称
     */
    private String workName;

    /**
     * 赛道ID
     */
    private Long competitionTrackId;

    /**
     * 赛道名称
     */
    private String trackName;

    /**
     * 提交时间
     */
    private java.time.LocalDateTime submissionTime;

    /**
     * 提交状态
     */
    private String submissionStatus;

    /**
     * 已完成评审数
     */
    private Integer completedReviewCount;

    /**
     * 待评审数
     */
    private Integer pendingReviewCount;

    /**
     * 总评审任务数
     */
    private Integer totalReviewCount;

    /**
     * 评审进度（百分比）
     */
    private BigDecimal reviewProgress;

    /**
     * 平均评审分数（已完成评审的平均分）
     */
    private BigDecimal averageScore;

    /**
     * AI评审分数
     */
    private BigDecimal aiScore;

    /**
     * 最终分数
     */
    private BigDecimal finalScore;

    /**
     * 奖项等级
     */
    private String awardLevel;

    /**
     * 是否已完成所有评审
     */
    private Boolean allReviewsCompleted;

    /**
     * 是否已计算最终结果
     */
    private Boolean finalResultCalculated;

    /**
     * 评委评审详情列表
     */
    private List<JudgeReviewDetail> judgeReviews;

    /**
     * 评委评审详情
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JudgeReviewDetail {
        /**
         * 评委ID
         */
        private Long judgeId;

        /**
         * 评委姓名
         */
        private String judgeName;

        /**
         * 总体评分
         */
        private BigDecimal overallScore;

        /**
         * 评审状态
         */
        private String reviewStatus;

        /**
         * 评审时间
         */
        private java.time.LocalDateTime reviewTime;

        /**
         * 评审意见
         */
        private String reviewComment;
    }
}