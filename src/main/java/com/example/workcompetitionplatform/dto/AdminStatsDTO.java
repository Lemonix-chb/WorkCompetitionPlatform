package com.example.workcompetitionplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 管理员统计数据DTO
 * 用于返回管理员首页的统计数据
 *
 * @author 陈海波
 * @since 2026-04-11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsDTO {

    /**
     * 赛事总数
     */
    private Integer totalCompetitions;

    /**
     * 进行中的赛事数
     */
    private Integer activeCompetitions;

    /**
     * 团队总数
     */
    private Integer totalTeams;

    /**
     * 本周新增团队数
     */
    private Integer newTeamsThisWeek;

    /**
     * 作品总数
     */
    private Integer totalWorks;

    /**
     * 待评审作品数
     */
    private Integer pendingWorks;

    /**
     * 评审中的作品数
     */
    private Integer reviewingWorks;

    /**
     * 已完成评审数
     */
    private Integer completedReviews;

    /**
     * 已获奖作品数
     */
    private Integer awardedWorks;

    /**
     * 用户总数
     */
    private Integer totalUsers;

    /**
     * 学生用户数
     */
    private Integer totalStudents;

    /**
     * 评委用户数
     */
    private Integer totalJudges;

    /**
     * 程序设计作品数
     */
    private Integer codeWorks;

    /**
     * 演示文稿作品数
     */
    private Integer pptWorks;

    /**
     * 数媒动漫作品数
     */
    private Integer videoWorks;

    /**
     * AI评审通过数
     */
    private Integer aiValidCount;

    /**
     * AI评审未通过数
     */
    private Integer aiInvalidCount;

    /**
     * 作品提交趋势（近7天）
     */
    private SubmissionTrend submissionTrend;

    /**
     * 提交趋势内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubmissionTrend {
        /**
         * 日期列表（近7天）
         */
        private List<String> dates;

        /**
         * 提交数量列表
         */
        private List<Integer> counts;
    }
}