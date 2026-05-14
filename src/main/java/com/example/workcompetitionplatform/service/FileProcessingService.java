package com.example.workcompetitionplatform.service;

import com.example.workcompetitionplatform.entity.Work;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * 文件处理服务接口
 *
 * @author 陈海波
 * @since 2026-04-13
 */
public interface FileProcessingService {

    /**
     * 解压提交的ZIP文件
     *
     * @param submissionId 提交ID
     * @param zipPath ZIP文件路径
     * @return 解压后的目录
     */
    File unzipSubmission(Long submissionId, String zipPath);

    /**
     * 校验文件类型是否允许
     *
     * @param filename 文件名
     * @param workType 作品类型
     * @return 是否允许
     */
    boolean validateFileType(String filename, Work.WorkType workType);

    /**
     * 提取关键文件
     *
     * @param unzippedDir 解压后的目录
     * @param workType 作品类型
     * @return 关键文件映射（按类别分组）
     */
    Map<String, List<File>> extractKeyFiles(File unzippedDir, Work.WorkType workType);

    /**
     * 清理临时文件
     *
     * @param submissionId 提交ID
     */
    void cleanupTempFiles(Long submissionId);

    /**
     * 获取临时目录路径
     *
     * @param submissionId 提交ID
     * @return 临时目录路径
     */
    String getTempDirPath(Long submissionId);
}