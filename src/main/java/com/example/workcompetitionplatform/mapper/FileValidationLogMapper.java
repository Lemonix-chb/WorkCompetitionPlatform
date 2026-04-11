package com.example.workcompetitionplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.workcompetitionplatform.entity.FileValidationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文件验证日志Mapper接口
 * 继承MyBatis Plus的BaseMapper，提供基础CRUD操作
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Mapper
public interface FileValidationLogMapper extends BaseMapper<FileValidationLog> {

    /**
     * 根据提交ID查询文件验证日志
     *
     * @param submissionId 提交ID
     * @return 文件验证日志列表
     */
    List<FileValidationLog> selectBySubmissionId(@Param("submissionId") Long submissionId);

    /**
     * 根据验证状态查询文件验证日志
     *
     * @param validationStatus 验证状态
     * @return 文件验证日志列表
     */
    List<FileValidationLog> selectByValidationStatus(@Param("validationStatus") FileValidationLog.ValidationResult validationStatus);

    /**
     * 根据文件类型查询文件验证日志
     *
     * @param fileType 文件类型
     * @return 文件验证日志列表
     */
    List<FileValidationLog> selectByFileType(@Param("fileType") String fileType);
}