package com.example.workcompetitionplatform.util;

import cn.hutool.core.date.DateUtil;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 团队编码生成器
 * 生成唯一的团队编码，格式：TEAM-年份-序号
 * 例如：TEAM-2026-001
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Component
public class TeamCodeGenerator {

    /**
     * 序号计数器，从1开始
     */
    private final AtomicInteger sequence = new AtomicInteger(1);

    /**
     * 当前年份
     */
    private final int currentYear = DateUtil.year(DateUtil.date());

    /**
     * 生成团队编码
     * 格式：TEAM-年份-序号
     *
     * @return 团队编码
     */
    public String generateTeamCode() {
        int seq = sequence.getAndIncrement();
        return String.format("TEAM-%d-%03d", currentYear, seq);
    }

    /**
     * 生成指定年份和序号的团队编码
     *
     * @param year 年份
     * @param sequence 序号
     * @return 团队编码
     */
    public String generateTeamCode(int year, int sequence) {
        return String.format("TEAM-%d-%03d", year, sequence);
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