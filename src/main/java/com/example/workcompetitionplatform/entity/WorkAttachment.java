package com.example.workcompetitionplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 作品附件实体类
 * 对应数据库表：work_attachment
 * 使用MyBatis Plus注解
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("work_attachment")
public class WorkAttachment {

    /**
     * 附件ID（主键自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属作品ID
     */
    @TableField(value = "work_id")
    private Long workId;

    /**
     * 文件名称
     */
    @TableField(value = "file_name")
    private String fileName;

    /**
     * 文件路径
     */
    @TableField(value = "file_path")
    private String filePath;

    /**
     * 文件大小（字节）
     */
    @TableField(value = "file_size")
    private Long fileSize;

    /**
     * 文件类型：SOURCE-源代码, DOCUMENT-文档, DEMO-演示视频, OTHER-其他
     */
    @TableField(value = "attachment_type")
    private AttachmentType attachmentType;

    /**
     * 文件MIME类型
     */
    @TableField(value = "mime_type")
    private String mimeType;

    /**
     * 上传人ID
     */
    @TableField(value = "uploader_id")
    private Long uploaderId;

    /**
     * 上传时间
     */
    @TableField(value = "upload_time")
    private LocalDateTime uploadTime;

    /**
     * 版本号
     */
    @TableField(value = "version")
    private Integer version = 1;

    /**
     * 创建时间（自动填充）
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 附件类型枚举
     */
    public enum AttachmentType {
        SOURCE,    // 源代码
        DOCUMENT,  // 文档
        DEMO,      // 演示视频
        OTHER      // 其他
    }
}