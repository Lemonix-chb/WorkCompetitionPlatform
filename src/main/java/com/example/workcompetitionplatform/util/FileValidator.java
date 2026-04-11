package com.example.workcompetitionplatform.util;

import java.util.Set;

/**
 * 文件验证工具类
 * 用于验证文件类型和大小
 *
 * @author 陈海波
 * @since 2026-01-19
 */
public class FileValidator {

    /**
     * 不同类型文件允许的扩展名白名单
     */
    private static final Set<String> SOURCE_EXTENSIONS = Set.of(
            "zip", "rar", "7z", "tar", "gz",
            "java", "py", "js", "ts", "cpp", "c", "h",
            "html", "css", "json", "xml", "yaml", "yml",
            "md", "txt", "sql"
    );

    private static final Set<String> DOCUMENT_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx",
            "odt", "ods", "odp", "rtf", "tex"
    );

    private static final Set<String> DEMO_EXTENSIONS = Set.of(
            "mp4", "avi", "mov", "wmv", "flv", "mkv",
            "webm", "mpeg", "mpg", "3gp", "ogv"
    );

    private static final Set<String> OTHER_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "bmp", "svg",
            "ico", "webp", "psd", "ai",
            "mp3", "wav", "flac", "aac", "ogg"
    );

    /**
     * 最大文件大小（字节）
     * 300MB = 300 * 1024 * 1024
     */
    private static final long MAX_FILE_SIZE = 300L * 1024L * 1024L;

    /**
     * 验证文件类型是否合法
     *
     * @param fileName 文件名
     * @param attachmentType 附件类型（SOURCE/DOCUMENT/DEMO/OTHER）
     * @return 是否合法
     */
    public static boolean isValidFileType(String fileName, String attachmentType) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }

        // 获取文件扩展名（不含点）
        String extension = getFileExtension(fileName).toLowerCase();
        if (extension.isEmpty()) {
            return false;
        }

        // 根据类型检查白名单
        Set<String> allowedExtensions = switch (attachmentType) {
            case "SOURCE" -> SOURCE_EXTENSIONS;
            case "DOCUMENT" -> DOCUMENT_EXTENSIONS;
            case "DEMO" -> DEMO_EXTENSIONS;
            case "OTHER" -> OTHER_EXTENSIONS;
            default -> Set.of();
        };

        return allowedExtensions.contains(extension);
    }

    /**
     * 验证文件大小是否在限制内
     *
     * @param fileSize 文件大小（字节）
     * @return 是否合法
     */
    public static boolean isValidFileSize(long fileSize) {
        return fileSize > 0 && fileSize <= MAX_FILE_SIZE;
    }

    /**
     * 获取文件扩展名（不含点）
     *
     * @param fileName 文件名
     * @return 扩展名（如：zip）
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * 清理文件名中的危险字符
     * 防止路径注入和特殊字符问题
     *
     * @param fileName 原始文件名
     * @return 清理后的安全文件名
     */
    public static String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "unnamed";
        }

        // 移除路径字符
        String cleaned = fileName.replace("/", "_")
                .replace("\\", "_")
                .replace("..", "_");

        // 移除特殊字符，只保留字母、数字、下划线、连字符、点和中文
        cleaned = cleaned.replaceAll("[^a-zA-Z0-9_\\-\\.\\u4e00-\\u9fa5]", "_");

        // 移除连续的下划线
        cleaned = cleaned.replaceAll("_+", "_");

        // 移除首尾的下划线和点（保留扩展名的点）
        if (cleaned.contains(".")) {
            int dotIndex = cleaned.lastIndexOf(".");
            String namePart = cleaned.substring(0, dotIndex).replaceAll("^_+|_+$", "");
            String extPart = cleaned.substring(dotIndex + 1);
            cleaned = namePart + "." + extPart;
        } else {
            cleaned = cleaned.replaceAll("^_+|_+$", "");
        }

        // 限制长度
        if (cleaned.length() > 200) {
            String extension = getFileExtension(cleaned);
            if (!extension.isEmpty()) {
                cleaned = cleaned.substring(0, 200 - extension.length() - 1) + "." + extension;
            } else {
                cleaned = cleaned.substring(0, 200);
            }
        }

        return cleaned.isEmpty() ? "unnamed" : cleaned;
    }

    /**
     * 获取文件类型的友好描述
     *
     * @param attachmentType 附件类型
     * @return 描述文本
     */
    public static String getFileTypeDescription(String attachmentType) {
        return switch (attachmentType) {
            case "SOURCE" -> "源代码文件";
            case "DOCUMENT" -> "文档文件";
            case "DEMO" -> "演示视频";
            case "OTHER" -> "其他文件";
            default -> "未知类型";
        };
    }

    /**
     * 获取允许的文件扩展名列表（用于前端提示）
     *
     * @param attachmentType 附件类型
     * @return 扩展名列表字符串
     */
    public static String getAllowedExtensions(String attachmentType) {
        Set<String> extensions = switch (attachmentType) {
            case "SOURCE" -> SOURCE_EXTENSIONS;
            case "DOCUMENT" -> DOCUMENT_EXTENSIONS;
            case "DEMO" -> DEMO_EXTENSIONS;
            case "OTHER" -> OTHER_EXTENSIONS;
            default -> Set.of();
        };

        return String.join(", ", extensions);
    }
}