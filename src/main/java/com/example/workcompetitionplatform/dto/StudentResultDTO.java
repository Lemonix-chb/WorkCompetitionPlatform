package com.example.workcompetitionplatform.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * 学生评审结果DTO
 * 用于返回给学生查看的评审结果信息
 *
 * @author 陈海波
 * @since 2026-05-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentResultDTO {

    /**
     * 结果ID
     */
    private Long id;

    /**
     * 提交ID（用于查询AI评审详情）
     */
    private Long submissionId;

    /**
     * 作品ID
     */
    private Long workId;

    /**
     * 作品名称
     */
    private String workName;

    /**
     * 作品编号
     */
    private String workCode;

    /**
     * 团队名称
     */
    private String teamName;

    /**
     * 赛道名称
     */
    private String trackName;

    /**
     * 赛事名称
     */
    private String competitionName;

    /**
     * AI评审得分
     */
    private BigDecimal aiScore;

    /**
     * 评委平均得分
     */
    private BigDecimal judgeAvgScore;

    /**
     * 最终得分（综合得分）
     */
    private BigDecimal finalScore;

    /**
     * 奖项等级
     */
    private String awardLevel;

    /**
     * 奖项等级文本
     */
    private String awardLevelText;

    /**
     * 赛道内排名
     */
    private Integer rankInTrack;

    /**
     * 评审意见
     */
    private String finalComment;

    /**
     * 作品类型
     */
    private String workType;

    /**
     * 作品类型文本
     */
    private String workTypeText;

    /**
     * 提交时间
     */
    private String submissionTime;

    /**
     * 结果生成时间
     */
    private String createTime;
}