package com.example.workcompetitionplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 赛事实体类
 * 对应数据库表：competition
 * 使用MyBatis Plus注解
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("competition")
public class Competition {

    /**
     * 赛事ID（主键自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 赛事名称
     */
    @TableField(value = "competition_name")
    private String competitionName;

    /**
     * 赛事年份
     */
    @TableField(value = "competition_year")
    private Integer competitionYear;

    /**
     * 赛事简介
     */
    @TableField(value = "description")
    private String description;

    /**
     * 报名开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "registration_start")
    private LocalDateTime registrationStart;

    /**
     * 报名截止时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "registration_end")
    private LocalDateTime registrationEnd;

    /**
     * 作品提交开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "submission_start")
    private LocalDateTime submissionStart;

    /**
     * 作品提交截止时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "submission_end")
    private LocalDateTime submissionEnd;

    /**
     * 评审开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "review_start")
    private LocalDateTime reviewStart;

    /**
     * 评审结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "review_end")
    private LocalDateTime reviewEnd;

    /**
     * 赛事状态：DRAFT-草稿, PUBLISHED-已发布, ONGOING-进行中, FINISHED-已结束
     */
    @TableField(value = "status")
    private CompetitionStatus status = CompetitionStatus.DRAFT;

    /**
     * 主办单位
     */
    @TableField(value = "organizer")
    private String organizer;

    /**
     * 联系人
     */
    @TableField(value = "contact_person")
    private String contactPerson;

    /**
     * 联系电话
     */
    @TableField(value = "contact_phone")
    private String contactPhone;

    /**
     * 联系邮箱
     */
    @TableField(value = "contact_email")
    private String contactEmail;

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
     * 赛事状态枚举
     */
    public enum CompetitionStatus {
        DRAFT,      // 草稿
        PUBLISHED,  // 已发布
        ONGOING,    // 进行中
        FINISHED    // 已结束
    }
}