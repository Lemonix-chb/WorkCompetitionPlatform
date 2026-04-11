package com.example.workcompetitionplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 赛道实体类
 * 对应数据库表：competition_track
 * 使用MyBatis Plus注解
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("competition_track")
public class CompetitionTrack {

    /**
     * 赛道ID（主键自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属赛事ID
     */
    @TableField(value = "competition_id")
    private Long competitionId;

    /**
     * 赛道名称
     */
    @TableField(value = "track_name")
    private String trackName;

    /**
     * 赛道编码
     */
    @TableField(value = "track_code")
    private String trackCode;

    /**
     * 作品类型：CODE-程序设计, PPT-演示文稿, VIDEO-数媒动漫与短视频
     */
    @TableField(value = "track_type")
    private TrackType trackType;

    /**
     * 团队最大人数
     */
    @TableField(value = "max_team_size")
    private Integer maxTeamSize = 3;

    /**
     * 团队最小人数
     */
    @TableField(value = "min_team_size")
    private Integer minTeamSize = 1;

    /**
     * 赛道说明
     */
    @TableField(value = "description")
    private String description;

    /**
     * 提交格式要求
     */
    @TableField(value = "submission_format")
    private String submissionFormat;

    /**
     * 最大文件大小(MB)
     */
    @TableField(value = "max_file_size_mb")
    private Integer maxFileSizeMb;

    /**
     * 允许的文件类型
     */
    @TableField(value = "allowed_file_types")
    private String allowedFileTypes;

    /**
     * 作品提交邮箱（不同赛道对应不同负责老师）
     */
    @TableField(value = "submission_email")
    private String submissionEmail;

    /**
     * 最小页数要求（PPT赛道至少12页）
     */
    @TableField(value = "min_pages")
    private Integer minPages;

    /**
     * 最小时长（秒，视频赛道）
     */
    @TableField(value = "min_duration_sec")
    private Integer minDurationSec;

    /**
     * 最大时长（秒，视频赛道）
     */
    @TableField(value = "max_duration_sec")
    private Integer maxDurationSec;

    /**
     * 画面比例（如16:9）
     */
    @TableField(value = "aspect_ratio")
    private String aspectRatio;

    /**
     * 分辨率要求（如1080p）
     */
    @TableField(value = "resolution")
    private String resolution;

    /**
     * 评审标准
     */
    @TableField(value = "review_criteria")
    private String reviewCriteria;

    /**
     * 赛道状态：ACTIVE-启用, DISABLED-禁用
     */
    @TableField(value = "status")
    private TrackStatus status = TrackStatus.ACTIVE;

    /**
     * 创建时间（自动填充）
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 作品类型枚举
     */
    public enum TrackType {
        CODE,   // 程序设计
        PPT,    // 演示文稿
        VIDEO   // 数媒动漫与短视频
    }

    /**
     * 赛道状态枚举
     */
    public enum TrackStatus {
        ACTIVE,    // 启用
        DISABLED   // 禁用
    }
}