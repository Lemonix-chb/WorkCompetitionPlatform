package com.example.workcompetitionplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workcompetitionplatform.entity.Submission;
import com.example.workcompetitionplatform.entity.FileValidationLog;

import java.util.List;

/**
 * 作品提交服务接口
 *
 * @author 陈海波
 * @since 2026-01-19
 */
public interface ISubmissionService extends IService<Submission> {

    /**
     * 提交作品
     *
     * @param teamId 团队ID
     * @param competitionTrackId 赛道ID
     * @param submitterUserId 提交人ID
     * @param filePath 文件路径
     * @param fileName 文件名
     * @param fileSize 文件大小
     * @return 提交记录实体
     */
    Submission submitWork(Long teamId, Long competitionTrackId, Long submitterUserId,
                          String filePath, String fileName, Long fileSize);

    /**
     * 根据团队ID查询提交记录列表
     *
     * @param teamId 团队ID
     * @return 提交记录列表
     */
    List<Submission> listByTeamId(Long teamId);

    /**
     * 根据赛道ID查询提交记录列表
     *
     * @param competitionTrackId 赛道ID
     * @return 提交记录列表
     */
    List<Submission> listByCompetitionTrackId(Long competitionTrackId);

    /**
     * 根据赛事ID查询提交记录列表
     *
     * @param competitionId 赛事ID
     * @return 提交记录列表
     */
    List<Submission> listByCompetitionId(Long competitionId);

    /**
     * 根据提交状态查询提交记录列表
     *
     * @param status 提交状态
     * @return 提交记录列表
     */
    List<Submission> listByStatus(Submission.SubmissionStatus status);

    /**
     * 根据用户ID查询提交记录列表
     *
     * @param userId 用户ID
     * @return 提交记录列表
     */
    List<Submission> listByUserId(Long userId);

    /**
     * 更新提交状态
     *
     * @param submissionId 提交ID
     * @param status 提交状态
     * @return 是否成功
     */
    boolean updateStatus(Long submissionId, Submission.SubmissionStatus status);

    /**
     * 检查团队是否已提交作品
     *
     * @param teamId 团队ID
     * @param competitionTrackId 赛道ID
     * @return 是否已提交
     */
    boolean hasTeamSubmitted(Long teamId, Long competitionTrackId);

    /**
     * 获取团队最新的提交记录
     *
     * @param teamId 团队ID
     * @param competitionTrackId 赛道ID
     * @return 提交记录实体
     */
    Submission getLatestSubmission(Long teamId, Long competitionTrackId);

    /**
     * 验证提交的文件
     *
     * @param submissionId 提交ID
     * @param filePath 文件路径
     * @param fileName 文件名
     * @param fileSize 文件大小
     * @return 文件验证日志实体
     */
    FileValidationLog validateFile(Long submissionId, String filePath, String fileName, Long fileSize);

    /**
     * 查询提交的文件验证日志列表
     *
     * @param submissionId 提交ID
     * @return 文件验证日志列表
     */
    List<FileValidationLog> listFileValidationLogs(Long submissionId);

    /**
     * 检查文件是否符合赛道要求
     *
     * @param competitionTrackId 赛道ID
     * @param fileName 文件名
     * @param fileSize 文件大小
     * @return 是否符合要求
     */
    boolean checkFileRequirements(Long competitionTrackId, String fileName, Long fileSize);

    /**
     * 获取赛道提交数量
     *
     * @param competitionTrackId 赛道ID
     * @return 提交数量
     */
    int getSubmissionCount(Long competitionTrackId);

    /**
     * 获取赛事提交数量
     *
     * @param competitionId 赛事ID
     * @return 提交数量
     */
    int getCompetitionSubmissionCount(Long competitionId);

    /**
     * 查询所有提交记录（管理员）
     *
     * @return 提交记录列表
     */
    List<Submission> listAllSubmissions();
}