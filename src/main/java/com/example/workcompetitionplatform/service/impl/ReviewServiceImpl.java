package com.example.workcompetitionplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workcompetitionplatform.entity.*;
import com.example.workcompetitionplatform.mapper.*;
import com.example.workcompetitionplatform.service.IReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 评审服务实现类
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewResultMapper, ReviewResult> implements IReviewService {

    private final AIReviewReportMapper aiReviewReportMapper;
    private final AIReviewDetailMapper aiReviewDetailMapper;
    private final JudgeReviewMapper judgeReviewMapper;
    private final SubmissionMapper submissionMapper;
    private final WorkMapper workMapper;
    private final UserMapper userMapper;

    public ReviewServiceImpl(AIReviewReportMapper aiReviewReportMapper,
                              AIReviewDetailMapper aiReviewDetailMapper,
                              JudgeReviewMapper judgeReviewMapper,
                              SubmissionMapper submissionMapper,
                              WorkMapper workMapper,
                              UserMapper userMapper) {
        this.aiReviewReportMapper = aiReviewReportMapper;
        this.aiReviewDetailMapper = aiReviewDetailMapper;
        this.judgeReviewMapper = judgeReviewMapper;
        this.submissionMapper = submissionMapper;
        this.workMapper = workMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AIReviewReport performAIReview(Long submissionId) {
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new RuntimeException("提交记录不存在");
        }

        // 创建AI评审报告
        AIReviewReport report = new AIReviewReport();
        report.setSubmissionId(submissionId);
        // Submission 没有 competitionTrackId 字段，需要通过 Work 表查询
        // report.setCompetitionTrackId(1L); // TODO: 通过 Work 表查询 trackId
        // AIReviewReport 没有 status 字段，不需要设置

        // TODO: 实现AI评审逻辑（调用AI服务进行评审）
        // 这里简化处理，实际需要调用AI服务进行评审
        report.setOverallScore(new java.math.BigDecimal("85"));
        report.setReviewSummary("AI初审结果：作品整体质量良好，符合要求。");

        // 保存AI评审报告
        aiReviewReportMapper.insert(report);

        return report;
    }

    @Override
    public AIReviewReport getAIReviewReport(Long submissionId) {
        return aiReviewReportMapper.selectBySubmissionId(submissionId);
    }

    @Override
    public List<AIReviewDetail> listAIReviewDetails(Long aiReviewReportId) {
        return aiReviewDetailMapper.selectByAiReviewReportId(aiReviewReportId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JudgeReview assignJudgeReview(Long workId, Long judgeUserId) {
        // 查询作品
        Work work = workMapper.selectById(workId);
        if (work == null) {
            throw new RuntimeException("作品不存在");
        }

        // 检查作品状态（必须已提交）
        if (work.getDevelopmentStatus() != Work.DevelopmentStatus.SUBMITTED) {
            throw new RuntimeException("只能为已提交的作品分配评委");
        }

        // 查询submission记录（作品提交时已创建）
        LambdaQueryWrapper<Submission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Submission::getWorkId, workId);
        Submission submission = submissionMapper.selectOne(wrapper);

        if (submission == null) {
            throw new RuntimeException("作品提交记录不存在，请重新提交作品");
        }

        // 检查是否已分配给该评委
        JudgeReview existingReview = judgeReviewMapper.selectBySubmissionIdAndJudgeUserId(submission.getId(), judgeUserId);
        if (existingReview != null) {
            throw new RuntimeException("该评委已被分配此作品");
        }

        // 创建评委评审任务
        JudgeReview judgeReview = new JudgeReview();
        judgeReview.setSubmissionId(submission.getId());
        judgeReview.setJudgeId(judgeUserId);
        judgeReview.setTeamId(work.getTeamId());
        judgeReview.setStatus(JudgeReview.ReviewStatus.DRAFT);

        // 保存评委评审任务
        judgeReviewMapper.insert(judgeReview);

        return judgeReview;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitJudgeReview(Long judgeReviewId, Integer score, String comments) {
        JudgeReview judgeReview = judgeReviewMapper.selectById(judgeReviewId);
        if (judgeReview == null) {
            throw new RuntimeException("评委评审任务不存在");
        }

        if (judgeReview.getStatus() != JudgeReview.ReviewStatus.DRAFT) {
            throw new RuntimeException("评审任务已完成");
        }

        // 更新评审结果
        judgeReview.setOverallScore(new java.math.BigDecimal(score.toString()));
        judgeReview.setReviewComment(comments);
        judgeReview.setStatus(JudgeReview.ReviewStatus.SUBMITTED);
        judgeReview.setReviewTime(java.time.LocalDateTime.now());

        return judgeReviewMapper.updateById(judgeReview) > 0;
    }

    @Override
    public List<JudgeReview> listJudgeReviews(Long submissionId) {
        return judgeReviewMapper.selectBySubmissionId(submissionId);
    }

    @Override
    public List<JudgeReview> listJudgeTasks(Long judgeUserId) {
        return judgeReviewMapper.selectByJudgeUserId(judgeUserId);
    }

    @Override
    public List<JudgeReview> listByCompetitionId(Long competitionId) {
        return judgeReviewMapper.selectByCompetitionId(competitionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewResult calculateFinalResult(Long submissionId) {
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new RuntimeException("提交记录不存在");
        }

        // 获取AI评审报告
        AIReviewReport aiReport = getAIReviewReport(submissionId);
        if (aiReport == null) {
            throw new RuntimeException("AI评审报告不存在");
        }

        // 获取所有评委评审结果
        List<JudgeReview> judgeReviews = listJudgeReviews(submissionId);
        if (judgeReviews.isEmpty()) {
            throw new RuntimeException("评委评审结果不存在");
        }

        // 检查所有评委是否已完成评审
        boolean allCompleted = judgeReviews.stream()
                .allMatch(review -> review.getStatus() == JudgeReview.ReviewStatus.SUBMITTED);
        if (!allCompleted) {
            throw new RuntimeException("部分评委尚未完成评审");
        }

        // 计算最终分数（AI分数权重30%，评委分数权重70%）
        double aiScore = aiReport.getOverallScore().doubleValue();
        double judgeAvgScore = judgeReviews.stream()
                .mapToDouble(review -> review.getOverallScore().doubleValue())
                .average()
                .orElse(0);

        double finalScore = aiScore * 0.3 + judgeAvgScore * 0.7;

        // 创建评审结果
        ReviewResult result = new ReviewResult();
        result.setSubmissionId(submissionId);
        result.setTeamId(submission.getTeamId());
        result.setAiScore(new java.math.BigDecimal(aiScore));
        result.setJudgeAvgScore(new java.math.BigDecimal(judgeAvgScore));
        result.setFinalScore(new java.math.BigDecimal(finalScore));
        result.setJudgeAvgScore(new java.math.BigDecimal(judgeAvgScore));
        result.setFinalScore(new java.math.BigDecimal(finalScore));
        // result.setStatus(ReviewResult.ResultStatus.COMPLETED); // ReviewResult 没有 status 字段
        // 保存评审结果
        save(result);

        return result;
    }

    @Override
    public ReviewResult getReviewResult(Long submissionId) {
        return baseMapper.selectBySubmissionId(submissionId);
    }

    @Override
    public List<ReviewResult> listByCompetitionTrackId(Long competitionTrackId) {
        return baseMapper.selectByCompetitionTrackId(competitionTrackId);
    }

    @Override
    public List<ReviewResult> listReviewResultsByCompetitionId(Long competitionId) {
        return baseMapper.selectByCompetitionId(competitionId);
    }

    @Override
    public List<ReviewResult> listAwardedTeams(Long competitionTrackId) {
        return baseMapper.selectAwardedTeamsByTrackId(competitionTrackId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setAwardLevel(Long reviewResultId, ReviewResult.AwardLevel awardLevel) {
        ReviewResult result = getById(reviewResultId);
        if (result == null) {
            throw new RuntimeException("评审结果不存在");
        }

        result.setAwardLevel(awardLevel);
        return updateById(result);
    }

    @Override
    public boolean isReviewCompleted(Long submissionId) {
        // 检查AI评审是否完成
        AIReviewReport aiReport = getAIReviewReport(submissionId);
        if (aiReport == null) {
            return false;
        }

        // 检查评委评审是否全部完成
        List<JudgeReview> judgeReviews = listJudgeReviews(submissionId);
        if (judgeReviews.isEmpty()) {
            return false;
        }

        return judgeReviews.stream()
                .allMatch(review -> review.getStatus() == JudgeReview.ReviewStatus.SUBMITTED);
    }

    @Override
    public int getCompletedReviewCount(Long competitionTrackId) {
        // ReviewResult 没有 competitionTrackId 和 status 字段
        // 需要通过 Work 表关联查询，这里简化处理
        LambdaQueryWrapper<ReviewResult> wrapper = new LambdaQueryWrapper<>();
        return (int) count(wrapper);
    }

    @Override
    public int getJudgePendingTaskCount(Long judgeUserId) {
        LambdaQueryWrapper<JudgeReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JudgeReview::getJudgeId, judgeUserId)
                .eq(JudgeReview::getStatus, JudgeReview.ReviewStatus.DRAFT);
        return judgeReviewMapper.selectCount(wrapper).intValue();
    }

    @Override
    public List<JudgeReview> listSubmissionsAwaitingAssignment(Long competitionId) {
        return judgeReviewMapper.selectSubmissionsAwaitingAssignment(competitionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchAssignJudgeReview(List<Long> submissionIds, Long judgeUserId) {
        int successCount = 0;
        for (Long submissionId : submissionIds) {
            try {
                assignJudgeReview(submissionId, judgeUserId);
                successCount++;
            } catch (Exception e) {
                // 跳过已分配的作品
            }
        }
        return successCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String autoAssignJudges(Long competitionId, int judgesPerSubmission) {
        // 查询所有评委
        List<User> judges = userMapper.selectByRole("JUDGE");
        if (judges.isEmpty()) {
            throw new RuntimeException("系统中没有评委用户");
        }

        // 查询待分配的作品
        List<JudgeReview> submissionsAwaiting = listSubmissionsAwaitingAssignment(competitionId);
        if (submissionsAwaiting.isEmpty()) {
            return "没有待分配的作品";
        }

        // 查询每个评委当前的待评审任务数量
        int[] judgeTaskCounts = new int[judges.size()];
        for (int i = 0; i < judges.size(); i++) {
            judgeTaskCounts[i] = judgeReviewMapper.countPendingTasksByJudge(judges.get(i).getId());
        }

        int totalAssigned = 0;
        int submissionIndex = 0;

        // 轮流分配作品给评委（优先分配给待评审任务最少的评委）
        for (JudgeReview submission : submissionsAwaiting) {
            for (int j = 0; j < judgesPerSubmission && submissionIndex < submissionsAwaiting.size(); j++) {
                // 找到待评审任务最少的评委
                int minTaskCount = Integer.MAX_VALUE;
                int selectedJudgeIndex = -1;

                for (int i = 0; i < judges.size(); i++) {
                    if (judgeTaskCounts[i] < minTaskCount) {
                        minTaskCount = judgeTaskCounts[i];
                        selectedJudgeIndex = i;
                    }
                }

                if (selectedJudgeIndex >= 0) {
                    try {
                        assignJudgeReview(submission.getSubmissionId(), judges.get(selectedJudgeIndex).getId());
                        judgeTaskCounts[selectedJudgeIndex]++;
                        totalAssigned++;
                    } catch (Exception e) {
                        // 跳过已分配的作品
                    }
                }
            }
            submissionIndex++;
        }

        // 生成统计信息
        StringBuilder statistics = new StringBuilder();
        statistics.append(String.format("自动分配完成！共分配%d个作品给%d位评委。\n", totalAssigned, judges.size()));
        statistics.append("评委待评审任务分布：\n");
        for (int i = 0; i < judges.size(); i++) {
            statistics.append(String.format("- %s: %d个待评审任务\n",
                    judges.get(i).getRealName(),
                    judgeTaskCounts[i]));
        }

        return statistics.toString();
    }
}