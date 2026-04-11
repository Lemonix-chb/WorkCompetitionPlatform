package com.example.workcompetitionplatform.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 分页响应DTO
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Long current;

    /**
     * 每页记录数
     */
    private Long size;

    /**
     * 总页数
     */
    private Long pages;

    /**
     * 是否有上一页
     */
    private Boolean hasPrevious;

    /**
     * 是否有下一页
     */
    private Boolean hasNext;

    /**
     * 构造分页响应对象
     *
     * @param records 数据列表
     * @param total 总记录数
     * @param current 当前页码
     * @param size 每页记录数
     * @return 分页响应对象
     */
    public static <T> PageResponse<T> of(List<T> records, Long total, Long current, Long size) {
        Long pages = (total + size - 1) / size;
        Boolean hasPrevious = current > 1;
        Boolean hasNext = current < pages;
        return new PageResponse<>(records, total, current, size, pages, hasPrevious, hasNext);
    }
}