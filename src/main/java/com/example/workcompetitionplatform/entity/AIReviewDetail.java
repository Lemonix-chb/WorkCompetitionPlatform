package com.example.workcompetitionplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI评审明细实体类
 * 对应数据库表：ai_review_detail
 * 使用MyBatis Plus注解
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_review_detail")
public class AIReviewDetail {

    /**
     * 明细ID（主键自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 报告ID
     */
    @TableField(value = "report_id")
    private Long reportId;

    /**
     * 检查项
     */
    @TableField(value = "check_item")
    private String checkItem;

    /**
     * 检查结果
     */
    @TableField(value = "check_result")
    private String checkResult;

    /**
     * 得分
     */
    @TableField(value = "score")
    private BigDecimal score;

    /**
     * 权重
     */
    @TableField(value = "weight")
    private BigDecimal weight;

    /**
     * 评语
     */
    @TableField(value = "comment")
    private String comment;

    /**
     * 相关文件路径
     */
    @TableField(value = "file_path")
    private String filePath;

    /**
     * 相关代码行号
     */
    @TableField(value = "line_number")
    private Integer lineNumber;

    /**
     * 创建时间（自动填充）
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 评审维度枚举
     */
    public enum ReviewDimension {
        INNOVATION,      // 创新性
        PRACTICALITY,    // 实用性
        USER_EXPERIENCE, //用户体验
        CODE_QUALITY,    // 代码质量
        DOCUMENTATION    // 文档质量
    }

    /**
     * 评审等级枚举
     */
    public enum ReviewLevel {
        EXCELLENT,  // 优秀
        GOOD,       // 良好
        AVERAGE,    // 一般
        POOR,       // 较差
        FAIL        // 不合格
    }
}