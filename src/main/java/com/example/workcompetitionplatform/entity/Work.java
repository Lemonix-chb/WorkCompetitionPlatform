package com.example.workcompetitionplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 作品实体类
 * 对应数据库表：work
 * 使用MyBatis Plus注解
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("work")
public class Work {

    /**
     * 作品ID（主键自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属团队ID
     */
    @TableField(value = "team_id")
    private Long teamId;

    /**
     * 所属赛事ID
     */
    @TableField(value = "competition_id")
    private Long competitionId;

    /**
     * 所属赛道ID
     */
    @TableField(value = "track_id")
    private Long trackId;

    /**
     * 作品编号
     */
    @TableField(value = "work_code")
    private String workCode;

    /**
     * 作品名称
     */
    @TableField(value = "work_name")
    private String workName;

    /**
     * 作品类型：CODE-程序设计, PPT-演示文稿, VIDEO-数媒动漫与短视频
     */
    @TableField(value = "work_type")
    private WorkType workType;

    /**
     * 作品简介
     */
    @TableField(value = "description")
    private String description;

    /**
     * 创新点说明
     */
    @TableField(value = "innovation_points")
    private String innovationPoints;

    /**
     * 关键功能特性
     */
    @TableField(value = "key_features")
    private String keyFeatures;

    /**
     * 技术栈（程序设计类）
     */
    @TableField(value = "tech_stack")
    private String techStack;

    /**
     * 团队分工说明
     */
    @TableField(value = "division_of_labor")
    private String divisionOfLabor;

    /**
     * 目标用户/应用场景
     */
    @TableField(value = "target_users")
    private String targetUsers;

    /**
     * 开发状态：IN_PROGRESS-开发中, COMPLETED-已完成, SUBMITTED-已提交, AWARDED-已获奖
     */
    @TableField(value = "development_status")
    private DevelopmentStatus developmentStatus = DevelopmentStatus.IN_PROGRESS;

    /**
     * 当前版本号
     */
    @TableField(value = "current_version")
    private Integer currentVersion = 1;

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
     * 作品类型枚举
     */
    public enum WorkType {
        CODE,   // 程序设计
        PPT,    // 演示文稿
        VIDEO   // 数媒动漫与短视频
    }

    /**
     * 开发状态枚举
     */
    public enum DevelopmentStatus {
        IN_PROGRESS,  // 开发中
        COMPLETED,    // 已完成
        SUBMITTED,    // 已提交
        AWARDED       // 已获奖
    }

    /**
     * 作品状态枚举
     */
    public enum WorkStatus {
        DRAFT,        // 草稿
        SUBMITTED,    // 已提交
        REVIEWING,    // 评审中
        REVIEWED      // 已评审
    }

    // ========== 显示字段（非数据库字段） ==========

    /**
     * 团队名称（显示字段）
     */
    @TableField(exist = false)
    private String teamName;

    /**
     * 赛事名称（显示字段）
     */
    @TableField(exist = false)
    private String competitionName;

    /**
     * 赛道名称（显示字段）
     */
    @TableField(exist = false)
    private String trackName;

    /**
     * 队长姓名（显示字段）
     */
    @TableField(exist = false)
    private String leaderName;
}