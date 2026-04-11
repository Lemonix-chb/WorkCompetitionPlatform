package com.example.workcompetitionplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.workcompetitionplatform.entity.ReviewResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评审结果Mapper接口
 * 继承MyBatis Plus的BaseMapper，提供基础CRUD操作
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Mapper
public interface ReviewResultMapper extends BaseMapper<ReviewResult> {

    /**
     * 根据提交ID查询评审结果
     *
     * @param submissionId 提交ID
     * @return 评审结果实体
     */
    ReviewResult selectBySubmissionId(@Param("submissionId") Long submissionId);

    /**
     * 根据赛道ID查询评审结果列表
     *
     * @param competitionTrackId 赛道ID
     * @return 评审结果列表
     */
    List<ReviewResult> selectByCompetitionTrackId(@Param("competitionTrackId") Long competitionTrackId);

    /**
     * 根据奖项等级查询评审结果列表
     *
     * @param awardLevel 奖项等级
     * @return 评审结果列表
     */
    List<ReviewResult> selectByAwardLevel(@Param("awardLevel") ReviewResult.AwardLevel awardLevel);

    /**
     * 根据赛事ID查询评审结果列表
     *
     * @param competitionId 赛事ID
     * @return 评审结果列表
     */
    List<ReviewResult> selectByCompetitionId(@Param("competitionId") Long competitionId);

    /**
     * 根据赛道ID查询获奖团队排名列表（按总分降序）
     *
     * @param competitionTrackId 赛道ID
     * @return 评审结果列表
     */
    List<ReviewResult> selectAwardedTeamsByTrackId(@Param("competitionTrackId") Long competitionTrackId);
}