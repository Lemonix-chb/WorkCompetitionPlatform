package com.example.workcompetitionplatform.util;

import cn.hutool.core.date.DateUtil;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 作品编码生成器
 * 生成唯一的作品编码和报名编码
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Component
public class WorkCodeGenerator {

    /**
     * 序号计数器，从1开始
     */
    private final AtomicInteger sequence = new AtomicInteger(1);

    /**
     * 当前年份
     */
    private final int currentYear = DateUtil.year(DateUtil.date());

    /**
     * 生成作品编码
     * 格式：WORK-年份-序号
     *
     * @return 作品编码
     */
    public String generateWorkCode() {
        int seq = sequence.getAndIncrement();
        return String.format("WORK-%d-%03d", currentYear, seq);
    }

    /**
     * 生成指定年份和序号的作品编码
     *
     * @param year 年份
     * @param sequence 序号
     * @return 作品编码
     */
    public String generateWorkCode(int year, int sequence) {
        return String.format("WORK-%d-%03d", year, sequence);
    }

    /**
     * 生成报名编码
     * 格式：REG-UUID前8位
     *
     * @return 报名编码
     */
    public static String generateRegistrationCode() {
        return "REG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * 重置序号计数器
     * 通常在新的一年开始时调用
     *
     * @param startSequence 起始序号
     */
    public void resetSequence(int startSequence) {
        sequence.set(startSequence);
    }

    /**
     * 获取当前序号
     *
     * @return 当前序号
     */
    public int getCurrentSequence() {
        return sequence.get();
    }

    /**
     * 获取当前年份
     *
     * @return 当前年份
     */
    public int getCurrentYear() {
        return currentYear;
    }
}