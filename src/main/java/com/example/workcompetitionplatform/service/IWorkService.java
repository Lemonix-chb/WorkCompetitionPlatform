package com.example.workcompetitionplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workcompetitionplatform.entity.Work;

import java.util.List;

/**
 * 作品服务接口
 *
 * @author 陈海波
 * @since 2026-01-19
 */
public interface IWorkService extends IService<Work> {

    /**
     * 创建作品
     *
     * @param workName 作品名称
     * @param teamId 团队ID
     * @param competitionId 赛事ID
     * @param trackId 赛道ID
     * @param workType 作品类型
     * @return 作品实体
     */
    Work createWork(String workName, Long teamId, Long competitionId, Long trackId, Work.WorkType workType);

    /**
     * 根据作品编号查询作品
     *
     * @param workCode 作品编号
     * @return 作品实体
     */
    Work getByWorkCode(String workCode);

    /**
     * 根据团队ID查询作品列表
     *
     * @param teamId 团队ID
     * @return 作品列表
     */
    List<Work> listByTeamId(Long teamId);

    /**
     * 根据赛道ID查询作品列表
     *
     * @param trackId 赛道ID
     * @return 作品列表
     */
    List<Work> listByTrackId(Long trackId);

    /**
     * 根据赛事ID查询作品列表
     *
     * @param competitionId 赛事ID
     * @return 作品列表
     */
    List<Work> listByCompetitionId(Long competitionId);

    /**
     * 根据用户ID查询作品列表（用户所在团队的作品）
     *
     * @param userId 用户ID
     * @return 作品列表
     */
    List<Work> listUserWorks(Long userId);

    /**
     * 更新作品信息
     *
     * @param workId 作品ID
     * @param workName 作品名称
     * @param description 作品简介
     * @param innovationPoints 创新点说明
     * @param keyFeatures 关键功能特性
     * @param techStack 技术栈
     * @param divisionOfLabor 团队分工说明
     * @param targetUsers 目标用户/应用场景
     * @return 是否成功
     */
    boolean updateWorkInfo(Long workId, String workName, String description, String innovationPoints,
                          String keyFeatures, String techStack, String divisionOfLabor, String targetUsers);

    /**
     * 提交作品
     *
     * @param workId 作品ID
     * @return 是否成功
     */
    boolean submitWork(Long workId);

    /**
     * 查询作品详情（包含作品信息和团队信息）
     *
     * @param workId 作品ID
     * @return 作品实体
     */
    Work getWorkDetail(Long workId);

    /**
     * 标记作品完成
     *
     * @param workId 作品ID
     * @param userId 用户ID（用于权限验证）
     * @return 是否成功
     */
    boolean markWorkComplete(Long workId, Long userId);
}