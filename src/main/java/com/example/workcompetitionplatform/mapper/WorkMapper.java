package com.example.workcompetitionplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.workcompetitionplatform.entity.Work;
import org.apache.ibatis.annotations.Mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 作品Mapper接口
 * 继承MyBatis Plus的BaseMapper，提供基础CRUD操作
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Mapper
public interface WorkMapper extends BaseMapper<Work> {

    /**
     * 根据提交ID查询作品
     *
     * @param submissionId 提交ID
     * @return 作品实体
     */
    Work selectBySubmissionId(@Param("submissionId") Long submissionId);

    /**
     * 根据赛道ID查询作品列表
     *
     * @param competitionTrackId 赛道ID
     * @return 作品列表
     */
    List<Work> selectByCompetitionTrackId(@Param("competitionTrackId") Long competitionTrackId);

    /**
     * 根据作品状态查询作品列表
     *
     * @param status 作品状态
     * @return 作品列表
     */
    List<Work> selectByStatus(@Param("status") Work.WorkStatus status);

    /**
     * 根据用户ID查询作品列表（用户所在团队的作品）
     *
     * @param userId 用户ID
     * @return 作品列表
     */
    List<Work> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询所有作品列表（管理员）
     *
     * @return 作品列表（包含详细信息）
     */
    List<Work> selectAllWorks();

    /**
     * 根据赛事ID查询作品列表（包含详细信息）
     *
     * @param competitionId 赛事ID
     * @return 作品列表
     */
    List<Work> selectByCompetitionId(@Param("competitionId") Long competitionId);
}