package com.example.workcompetitionplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 评审结果汇总实体类
 * 对应数据库表：review_result
 * 使用MyBatis Plus注解
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("review_result")
public class ReviewResult {

    /**
     * 结果ID（主键自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 团队ID
     */
    @TableField(value = "team_id")
    private Long teamId;

    /**
     * 作品提交ID
     */
    @TableField(value = "submission_id")
    private Long submissionId;

    /**
     * AI评审得分
     */
    @TableField(value = "ai_score")
    private BigDecimal aiScore;

    /**
     * AI评分权重
     */
    @TableField(value = "ai_weight")
    private BigDecimal aiWeight = new BigDecimal("0.3");

    /**
     * 评委平均得分
     */
    @TableField(value = "judge_avg_score")
    private BigDecimal judgeAvgScore;

    /**
     * 评委评分权重
     */
    @TableField(value = "judge_weight")
    private BigDecimal judgeWeight = new BigDecimal("0.7");

    /**
     * 最终得分
     */
    @TableField(value = "final_score")
    private BigDecimal finalScore;

    /**
     * 奖项等级：FIRST-一等奖, SECOND-二等奖, THIRD-三等奖, NONE-无奖项
     */
    @TableField(value = "award_level")
    private AwardLevel awardLevel;

    /**
     * 赛道排名
     */
    @TableField(value = "rank_in_track")
    private Integer rankInTrack;

    /**
     * 最终评语
     */
    @TableField(value = "final_comment")
    private String finalComment;

    /**
     * 创建时间（自动填充）
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间（自动填充）
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 奖项等级枚举
     */
    public enum AwardLevel {
        FIRST,   // 一等奖
        SECOND,  // 二等奖
        THIRD,   // 三等奖
        NONE     // 无奖项
    }
}