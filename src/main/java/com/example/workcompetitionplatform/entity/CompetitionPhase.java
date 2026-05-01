package com.example.workcompetitionplatform.entity;

/**
 * 赛事阶段枚举
 * 定义赛事在不同时期所处的状态
 *
 * @author 陈海波
 * @since 2026-05-01
 */
public enum CompetitionPhase {
    BEFORE_REGISTRATION,   // 报名未开始
    REGISTRATION,          // 报名进行中
    BEFORE_SUBMISSION,     // 报名已截止，提交未开始
    SUBMISSION,            // 提交进行中
    REVIEW,                // 评审进行中
    FINISHED               // 已结束
}