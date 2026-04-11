package com.example.workcompetitionplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workcompetitionplatform.entity.Appeal;

import java.util.List;

/**
 * 申诉服务接口
 *
 * @author 陈海波
 * @since 2026-01-19
 */
public interface IAppealService extends IService<Appeal> {

    /**
     * 提交申诉
     *
     * @param submissionId 提交ID
     * @param appellantUserId 申诉人ID
     * @param appealReason 申诉理由
     * @return 申诉实体
     */
    Appeal submitAppeal(Long submissionId, Long appellantUserId, String appealReason);

    /**
     * 处理申诉
     *
     * @param appealId 申诉ID
     * @param handlerUserId 处理人ID
     * @param accepted 是否接受申诉
     * @param handlingResult 处理结果
     * @return 是否成功
     */
    boolean handleAppeal(Long appealId, Long handlerUserId, boolean accepted, String handlingResult);

    /**
     * 根据提交ID查询申诉列表
     *
     * @param submissionId 提交ID
     * @return 申诉列表
     */
    List<Appeal> listBySubmissionId(Long submissionId);

    /**
     * 根据申诉人ID查询申诉列表
     *
     * @param appellantUserId 申诉人ID
     * @return 申诉列表
     */
    List<Appeal> listByAppellantUserId(Long appellantUserId);

    /**
     * 根据申诉状态查询申诉列表
     *
     * @param status 申诉状态
     * @return 申诉列表
     */
    List<Appeal> listByStatus(Appeal.AppealStatus status);

    /**
     * 根据赛事ID查询申诉列表
     *
     * @param competitionId 赛事ID
     * @return 申诉列表
     */
    List<Appeal> listByCompetitionId(Long competitionId);

    /**
     * 查询待处理的申诉列表
     *
     * @return 申诉列表
     */
    List<Appeal> listPendingAppeals();

    /**
     * 检查提交是否存在未处理的申诉
     *
     * @param submissionId 提交ID
     * @return 是否存在未处理的申诉
     */
    boolean hasPendingAppeal(Long submissionId);
}