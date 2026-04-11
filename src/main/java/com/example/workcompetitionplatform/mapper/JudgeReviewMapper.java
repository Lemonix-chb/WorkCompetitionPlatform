package com.example.workcompetitionplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.workcompetitionplatform.entity.JudgeReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评委评审Mapper接口
 * 继承MyBatis Plus的BaseMapper，提供基础CRUD操作
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Mapper
public interface JudgeReviewMapper extends BaseMapper<JudgeReview> {

    /**
     * 根据提交ID查询评委评审记录
     *
     * @param submissionId 提交ID
     * @return 评委评审记录列表
     */
    List<JudgeReview> selectBySubmissionId(@Param("submissionId") Long submissionId);

    /**
     * 根据评委ID查询评委评审记录
     *
     * @param judgeUserId 评委用户ID
     * @return 评委评审记录列表
     */
    List<JudgeReview> selectByJudgeUserId(@Param("judgeUserId") Long judgeUserId);

    /**
     * 根据评审状态查询评委评审记录
     *
     * @param reviewStatus 评审状态
     * @return 评委评审记录列表
     */
    List<JudgeReview> selectByReviewStatus(@Param("reviewStatus") JudgeReview.ReviewStatus reviewStatus);

    /**
     * 根据赛道ID查询评委评审记录
     *
     * @param competitionTrackId 赛道ID
     * @return 评委评审记录列表
     */
    List<JudgeReview> selectByCompetitionTrackId(@Param("competitionTrackId") Long competitionTrackId);

    /**
     * 根据赛事ID查询评委评审记录
     *
     * @param competitionId 赛事ID
     * @return 评委评审记录列表
     */
    List<JudgeReview> selectByCompetitionId(@Param("competitionId") Long competitionId);

    /**
     * 根据提交ID和评委ID查询评委评审记录
     *
     * @param submissionId 提交ID
     * @param judgeUserId 评委用户ID
     * @return 评委评审记录实体
     */
    JudgeReview selectBySubmissionIdAndJudgeUserId(@Param("submissionId") Long submissionId, @Param("judgeUserId") Long judgeUserId);

    /**
     * 查询待分配评审的作品提交（已提交但未分配评委）
     *
     * @param competitionId 赛事ID（可选，null表示查询所有）
     * @return 待分配的作品提交列表
     */
    List<JudgeReview> selectSubmissionsAwaitingAssignment(@Param("competitionId") Long competitionId);

    /**
     * 查询评委当前的待评审任务数量
     *
     * @param judgeUserId 评委用户ID
     * @return 待评审任务数量
     */
    int countPendingTasksByJudge(@Param("judgeUserId") Long judgeUserId);
}