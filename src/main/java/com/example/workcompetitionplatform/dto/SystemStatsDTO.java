package com.example.workcompetitionplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 系统详细统计数据DTO
 * 用于返回管理员数据统计页面的详细统计信息
 *
 * @author 陈海波
 * @since 2026-04-11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemStatsDTO {

    /**
     * 赛事总数
     */
    private Integer totalCompetitions;

    /**
     * 赛道总数
     */
    private Integer totalTracks;

    /**
     * 团队总数
     */
    private Integer totalTeams;

    /**
     * 作品总数
     */
    private Integer totalWorks;

    /**
     * 提交总数
     */
    private Integer totalSubmissions;

    /**
     * 用户总数
     */
    private Integer totalUsers;

    /**
     * 学生用户数
     */
    private Integer studentCount;

    /**
     * 评委用户数
     */
    private Integer judgeCount;

    /**
     * 管理员用户数
     */
    private Integer adminCount;

    /**
     * 已完成评审数
     */
    private Integer completedReviews;

    /**
     * 待评审数
     */
    private Integer pendingReviews;

    /**
     * 已设置奖项数
     */
    private Integer awardedCount;

    /**
     * 学院分布统计（学院名称 -> 学生数量）
     */
    private Map<String, Integer> collegeDistribution;

    /**
     * 专业分布统计（专业名称 -> 学生数量）
     */
    private Map<String, Integer> majorDistribution;

    /**
     * 奖项等级分布（奖项等级 -> 作品数量）
     */
    private Map<String, Integer> awardLevelDistribution;
}