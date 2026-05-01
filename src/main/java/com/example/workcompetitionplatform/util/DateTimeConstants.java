package com.example.workcompetitionplatform.util;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 时间日期常量工具类
 * 统一管理应用中的时区和日期格式
 *
 * @author 陈海波
 * @since 2026-05-01
 */
public class DateTimeConstants {

    /**
     * 应用统一时区：中国标准时间
     * 所有时间比较和显示都应使用此时区
     */
    public static final ZoneId APP_TIMEZONE = ZoneId.of("Asia/Shanghai");

    /**
     * 标准日期时间格式：yyyy-MM-dd HH:mm:ss
     * 用于API响应、错误消息、数据库显示
     */
    public static final DateTimeFormatter STANDARD_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 仅日期格式：yyyyMMdd
     * 用于生成编号、文件命名
     */
    public static final DateTimeFormatter DATE_ONLY_FORMAT =
        DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 紧凑时间格式：yyyyMMddHHmmss
     * 用于文件命名、唯一标识生成
     */
    public static final DateTimeFormatter COMPACT_FORMAT =
        DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * 显示格式：yyyy-MM-dd HH:mm
     * 用于用户界面展示（精确到分钟）
     */
    public static final DateTimeFormatter DISPLAY_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * 短日期格式：MM-dd
     * 用于简化日期显示
     */
    public static final DateTimeFormatter SHORT_DATE_FORMAT =
        DateTimeFormatter.ofPattern("MM-dd");

    /**
     * 获取当前时间（应用时区）
     *
     * @return 当前时间
     */
    public static java.time.LocalDateTime now() {
        return java.time.LocalDateTime.now(APP_TIMEZONE);
    }
}