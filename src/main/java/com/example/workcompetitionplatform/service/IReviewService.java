package com.example.workcompetitionplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workcompetitionplatform.entity.AIReviewReport;
import com.example.workcompetitionplatform.entity.AIReviewDetail;
import com.example.workcompetitionplatform.entity.JudgeReview;
import com.example.workcompetitionplatform.entity.ReviewResult;

import java.util.List;

/**
 * 评审服务接口
 *
 * @author 陈海波
 * @since 2026-01-19
 */
public interface IReviewService extends IService<ReviewResult> {

    /**
     * 执行AI初审
     *
     * @param submissionId 提交ID
     * @return AI评审报告实体
     */
    AIReviewReport performAIReview(Long submissionId);

    /**
     * 根据提交ID查询AI评审报告
     *
     * @param submissionId 提交ID
     * @return AI评审报告实体
     */
    AIReviewReport getAIReviewReport(Long submissionId);

    /**
     * 根据AI评审报告ID查询评审详情列表
     *
     * @param aiReviewReportId AI评审报告ID
     * @return AI评审详情列表
     */
    List<AIReviewDetail> listAIReviewDetails(Long aiReviewReportId);

    /**
     * 分配评委评审任务
     *
     * @param workId 作品ID（修改为基于作品分配）
     * @param judgeUserId 评委用户ID
     * @return 评委评审实体
     */
    JudgeReview assignJudgeReview(Long workId, Long judgeUserId);

    /**
     * 评委提交评审结果
     *
     * @param judgeReviewId 评委评审ID
     * @param score 评审分数
     * @param comments 评审意见
     * @return 是否成功
     */
    boolean submitJudgeReview(Long judgeReviewId, Integer score, String comments);

    /**
     * 根据提交ID查询评委评审列表
     *
     * @param submissionId 提交ID
     * @return 评委评审列表
     */
    List<JudgeReview> listJudgeReviews(Long submissionId);

    /**
     * 根据评委ID查询评审任务列表
     *
     * @param judgeUserId 评委用户ID
     * @return 评委评审列表
     */
    List<JudgeReview> listJudgeTasks(Long judgeUserId);

    /**
     * 根据赛事ID查询评委评审列表
     *
     * @param competitionId 赛事ID
     * @return 评委评审列表
     */
    List<JudgeReview> listByCompetitionId(Long competitionId);

    /**
     * 计算最终评审结果（综合AI初审和评委评审）
     *
     * @param submissionId 提交ID
     * @return 评审结果实体
     */
    ReviewResult calculateFinalResult(Long submissionId);

    /**
     * 根据提交ID查询评审结果
     *
     * @param submissionId 提交ID
     * @return 评审结果实体
     */
    ReviewResult getReviewResult(Long submissionId);

    /**
     * 根据赛道ID查询评审结果列表
     *
     * @param competitionTrackId 赛道ID
     * @return 评审结果列表
     */
    List<ReviewResult> listByCompetitionTrackId(Long competitionTrackId);

    /**
     * 根据赛事ID查询评审结果列表
     *
     * @param competitionId 赛事ID
     * @return 评审结果列表
     */
    List<ReviewResult> listReviewResultsByCompetitionId(Long competitionId);

    /**
     * 查询赛道获奖团队排名列表
     *
     * @param competitionTrackId 车道ID
     * @return 评审结果列表（按分数降序）
     */
    List<ReviewResult> listAwardedTeams(Long competitionTrackId);

    /**
     * 设置奖项等级
     *
     * @param reviewResultId 评审结果ID
     * @param awardLevel 奖项等级
     * @return 是否成功
     */
    boolean setAwardLevel(Long reviewResultId, ReviewResult.AwardLevel awardLevel);

    /**
     * 检查提交是否已完成所有评审
     *
     * @param submissionId 提交ID
     * @return 是否已完成所有评审
     */
    boolean isReviewCompleted(Long submissionId);

    /**
     * 获取赛道评审完成数量
     *
     * @param competitionTrackId 车道ID
     * @return 评审完成数量
     */
    int getCompletedReviewCount(Long competitionTrackId);

    /**
     * 获取评委待评审任务数量
     *
     * @param judgeUserId 评委用户ID
     * @return 待评审任务数量
     */
    int getJudgePendingTaskCount(Long judgeUserId);

    /**
     * 查询待分配的作品提交列表
     *
     * @param competitionId 赛事ID（可选，null表示所有赛事）
     * @return 待分配的作品提交列表
     */
    List<JudgeReview> listSubmissionsAwaitingAssignment(Long competitionId);

    /**
     * 批量分配评委评审任务
     *
     * @param submissionIds 提交ID列表
     * @param judgeUserId 评委用户ID
     * @return 成功分配的数量
     */
    int batchAssignJudgeReview(List<Long> submissionIds, Long judgeUserId);

    /**
     * 自动平均分配评委
     * 将待分配作品平均分配给所有评委
     *
     * @param competitionId 赛事ID（可选，null表示所有赛事）
     * @param judgesPerSubmission 每个作品分配的评委数量
     * @return 分配统计信息字符串
     */
    String autoAssignJudges(Long competitionId, int judgesPerSubmission);
}