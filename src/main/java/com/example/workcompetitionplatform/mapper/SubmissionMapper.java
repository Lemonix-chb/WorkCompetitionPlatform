package com.example.workcompetitionplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.workcompetitionplatform.entity.Submission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 作品提交Mapper接口
 * 继承MyBatis Plus的BaseMapper，提供基础CRUD操作
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Mapper
public interface SubmissionMapper extends BaseMapper<Submission> {

    /**
     * 根据团队ID查询提交记录
     *
     * @param teamId 团队ID
     * @return 提交记录列表
     */
    List<Submission> selectByTeamId(@Param("teamId") Long teamId);

    /**
     * 根据赛道ID查询提交记录
     *
     * @param competitionTrackId 赛道ID
     * @return 提交记录列表
     */
    List<Submission> selectByCompetitionTrackId(@Param("competitionTrackId") Long competitionTrackId);

    /**
     * 根据提交状态查询提交记录
     *
     * @param status 提交状态
     * @return 提交记录列表
     */
    List<Submission> selectByStatus(@Param("status") Submission.SubmissionStatus status);

    /**
     * 根据用户ID查询提交记录
     *
     * @param userId 用户ID
     * @return 提交记录列表
     */
    List<Submission> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据赛事ID查询提交记录
     *
     * @param competitionId 赛事ID
     * @return 提交记录列表
     */
    List<Submission> selectByCompetitionId(@Param("competitionId") Long competitionId);

    /**
     * 查询所有提交记录（管理员）
     *
     * @return 提交记录列表
     */
    List<Submission> selectAllSubmissions();
}