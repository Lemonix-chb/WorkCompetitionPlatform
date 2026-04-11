package com.example.workcompetitionplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 团队实体类
 * 对应数据库表：team
 * 使用MyBatis Plus注解
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("team")
public class Team {

    /**
     * 团队ID（主键自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 团队编号（自动生成）
     */
    @TableField(value = "team_code")
    private String teamCode;

    /**
     * 团队名称
     */
    @TableField(value = "team_name")
    private String teamName;

    /**
     * 所属赛道ID
     */
    @TableField(value = "competition_track_id")
    private Long competitionTrackId;

    /**
     * 队长用户ID
     */
    @TableField(value = "leader_id")
    private Long leaderId;

    /**
     * 当前成员数量
     */
    @TableField(value = "current_member_count")
    private Integer currentMemberCount = 1;

    /**
     * 最大成员数量
     */
    @TableField(value = "max_member_count")
    private Integer maxMemberCount = 3;

    /**
     * 团队状态：FORMING-组建中, CONFIRMED-已确认, REGISTERED-已报名, SUBMITTED-已提交, REVIEWED-已评审, AWARDED-已获奖
     */
    @TableField(value = "status")
    private TeamStatus status = TeamStatus.FORMING;

    /**
     * 作品简介
     */
    @TableField(value = "work_description")
    private String workDescription;

    /**
     * 分工说明
     */
    @TableField(value = "division_of_labor")
    private String divisionOfLabor;

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
     * 团队状态枚举
     */
    public enum TeamStatus {
        FORMING,      // 组建中
        CONFIRMED,    // 已确认
        REGISTERED,   // 已报名
        SUBMITTED,    // 已提交
        REVIEWED,     // 已评审
        AWARDED       // 已获奖
    }
}