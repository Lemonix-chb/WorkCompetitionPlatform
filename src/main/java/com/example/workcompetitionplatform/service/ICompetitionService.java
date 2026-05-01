package com.example.workcompetitionplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workcompetitionplatform.dto.CompetitionTimeStatusDTO;
import com.example.workcompetitionplatform.entity.Competition;
import com.example.workcompetitionplatform.entity.CompetitionTrack;

import java.util.List;

/**
 * 赛事服务接口
 *
 * @author 陈海波
 * @since 2026-01-19
 */
public interface ICompetitionService extends IService<Competition> {

    /**
     * 根据赛事年份查询赛事列表
     *
     * @param year 赛事年份
     * @return 赛事列表
     */
    List<Competition> listByYear(Integer year);

    /**
     * 根据赛事状态查询赛事列表
     *
     * @param status 赛事状态
     * @return 赛事列表
     */
    List<Competition> listByStatus(Competition.CompetitionStatus status);

    /**
     * 查询当前进行中的赛事
     *
     * @return 赛事列表
     */
    List<Competition> listOngoingCompetitions();

    /**
     * 查询当前已发布的赛事
     *
     * @return 赛事列表
     */
    List<Competition> listPublishedCompetitions();

    /**
     * 查询赛事的所有赛道
     *
     * @param competitionId 赛事ID
     * @return 赛道列表
     */
    List<CompetitionTrack> listCompetitionTracks(Long competitionId);

    /**
     * 根据赛道编码查询赛道
     *
     * @param trackCode 赛道编码
     * @return 赛道实体
     */
    CompetitionTrack getTrackByCode(String trackCode);

    /**
     * 根据赛道ID查询赛道详情
     *
     * @param trackId 赛道ID
     * @return 赛道实体
     */
    CompetitionTrack getTrackById(Long trackId);

    /**
     * 创建赛事
     *
     * @param competition 赛事实体
     * @return 赛事实体
     */
    Competition createCompetition(Competition competition);

    /**
     * 创建赛道
     *
     * @param track 赛道实体
     * @return 赛道实体
     */
    CompetitionTrack createTrack(CompetitionTrack track);

    /**
     * 发布赛事
     *
     * @param competitionId 赛事ID
     * @return 是否成功
     */
    boolean publishCompetition(Long competitionId);

    /**
     * 开始赛事
     *
     * @param competitionId 赛事ID
     * @return 是否成功
     */
    boolean startCompetition(Long competitionId);

    /**
     * 结束赛事
     *
     * @param competitionId 赛事ID
     * @return 是否成功
     */
    boolean finishCompetition(Long competitionId);

    /**
     * 检查赛事是否在报名时间内
     *
     * @param competitionId 赛事ID
     * @return 是否在报名时间内
     */
    boolean isInRegistrationPeriod(Long competitionId);

    /**
     * 检查赛事是否在提交时间内
     *
     * @param competitionId 赛事ID
     * @return 是否在提交时间内
     */
    boolean isInSubmissionPeriod(Long competitionId);

    /**
     * 检查赛事是否在评审时间内
     *
     * @param competitionId 赛事ID
     * @return 是否在评审时间内
     */
    boolean isInReviewPeriod(Long competitionId);

    /**
     * 获取赛事时间状态信息
     *
     * @param competitionId 赛事ID
     * @return 时间状态DTO
     */
    CompetitionTimeStatusDTO getTimeStatus(Long competitionId);

    /**
     * 检查用户是否已报名该赛事
     *
     * @param competitionId 赛事ID
     * @param userId 用户ID
     * @return 是否已报名
     */
    boolean hasUserRegistered(Long competitionId, Long userId);

    /**
     * 检查团队是否已报名该赛事
     *
     * @param competitionId 赛事ID
     * @param teamId 团队ID
     * @return 是否已报名
     */
    boolean hasTeamRegistered(Long competitionId, Long teamId);

    /**
     * 获取赛事报名数量
     *
     * @param competitionId 赛事ID
     * @return 报名数量
     */
    int getRegistrationCount(Long competitionId);

    /**
     * 获取赛道报名数量
     *
     * @param trackId 赛道ID
     * @return 报名数量
     */
    int getTrackRegistrationCount(Long trackId);
}