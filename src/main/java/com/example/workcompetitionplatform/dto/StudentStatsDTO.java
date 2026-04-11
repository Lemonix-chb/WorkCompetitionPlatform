package com.example.workcompetitionplatform.dto;

import lombok.Data;

/**
 * 学生统计数据DTO
 * 用于返回学生个人中心的统计数据
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Data
public class StudentStatsDTO {

    /**
     * 赛事报名数量
     */
    private Integer competitionCount;

    /**
     * 团队参与数量
     */
    private Integer teamCount;

    /**
     * 作品提交数量
     */
    private Integer workCount;

    /**
     * 获奖数量
     */
    private Integer awardCount;
}