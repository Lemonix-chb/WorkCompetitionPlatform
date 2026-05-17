package com.example.workcompetitionplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workcompetitionplatform.dto.AIReviewDetailDTO;
import com.example.workcompetitionplatform.dto.LLMEvaluationResult;
import com.example.workcompetitionplatform.dto.SubmissionReviewSummaryDTO;
import com.example.workcompetitionplatform.dto.SubmissionReviewSummaryDTO.JudgeReviewDetail;
import com.example.workcompetitionplatform.entity.*;
import com.example.workcompetitionplatform.mapper.*;
import com.example.workcompetitionplatform.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final TeamMapper teamMapper;
    private final CompetitionTrackMapper competitionTrackMapper;

    // AI评审服务注入
    private final FileProcessingService fileProcessingService;
    private final RuleEngineService ruleEngineService;
    private final CodeQualityService codeQualityService;
    private final LLMEvaluationService llmEvaluationService;
    private final AsyncAIReviewService asyncAIReviewService;

    public ReviewServiceImpl(AIReviewReportMapper aiReviewReportMapper,
                              AIReviewDetailMapper aiReviewDetailMapper,
                              JudgeReviewMapper judgeReviewMapper,
                              SubmissionMapper submissionMapper,
                              WorkMapper workMapper,
                              UserMapper userMapper,
                              TeamMapper teamMapper,
                              CompetitionTrackMapper competitionTrackMapper,
                              FileProcessingService fileProcessingService,
                              RuleEngineService ruleEngineService,
                              CodeQualityService codeQualityService,
                              LLMEvaluationService llmEvaluationService,
                              AsyncAIReviewService asyncAIReviewService) {
        this.aiReviewReportMapper = aiReviewReportMapper;
        this.aiReviewDetailMapper = aiReviewDetailMapper;
        this.judgeReviewMapper = judgeReviewMapper;
        this.submissionMapper = submissionMapper;
        this.workMapper = workMapper;
        this.userMapper = userMapper;
        this.teamMapper = teamMapper;
        this.competitionTrackMapper = competitionTrackMapper;
        this.fileProcessingService = fileProcessingService;
        this.ruleEngineService = ruleEngineService;
        this.codeQualityService = codeQualityService;
        this.llmEvaluationService = llmEvaluationService;
        this.asyncAIReviewService = asyncAIReviewService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AIReviewReport performAIReview(Long submissionId) {
        // 1. 获取提交信息和作品
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new RuntimeException("提交记录不存在");
        }

        Work work = workMapper.selectById(submission.getWorkId());
        if (work == null) {
            throw new RuntimeException("作品不存在");
        }

        // 2. 更新提交状态为正在评审
        submission.setStatus(Submission.SubmissionStatus.VALIDATING);
        submission.setValidationResult("AI评审进行中...");
        submissionMapper.updateById(submission);

        // 3. 异步派发到Python Agent（获取提交者ID用于错误通知）
        Long userId = submission.getSubmitterId();
        asyncAIReviewService.performAIReviewAsync(submissionId, userId);

        // 4. 返回临时报告（实际结果通过回调接口更新）
        AIReviewReport tempReport = new AIReviewReport();
        tempReport.setSubmissionId(submissionId);
        tempReport.setTeamId(submission.getTeamId());
        tempReport.setReviewSummary("AI评审进行中，请稍后查看结果...");
        tempReport.setAiModel("Python-Agent");
        tempReport.setReviewTime(LocalDateTime.now());

        return tempReport;
    }

    /**
     * 计算总体评分（不同作品类型的权重不同）
     */
    private BigDecimal calculateOverallScore(BigDecimal basicScore, BigDecimal codeQualityScore,
                                              LLMEvaluationResult llmResult, Work.WorkType workType) {
        BigDecimal llmAverageScore = llmResult.getInnovationScore()
            .add(llmResult.getPracticalityScore())
            .add(llmResult.getUserExperienceScore())
            .add(llmResult.getDocumentationScore())
            .divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP);

        switch (workType) {
            case CODE:
                // 程序设计：基础规范30% + 代码质量40% + 大模型30%
                return basicScore.multiply(BigDecimal.valueOf(0.30))
                    .add(codeQualityScore.multiply(BigDecimal.valueOf(0.40))
                    .add(llmAverageScore.multiply(BigDecimal.valueOf(0.30))));

            case PPT:
                // 演示文稿：基础规范50% + 大模型50%
                return basicScore.multiply(BigDecimal.valueOf(0.50))
                    .add(llmAverageScore.multiply(BigDecimal.valueOf(0.50)));

            case VIDEO:
                // 数媒视频：基础规范40% + 大模型60%
                return basicScore.multiply(BigDecimal.valueOf(0.40))
                    .add(llmAverageScore.multiply(BigDecimal.valueOf(0.60)));

            default:
                return llmAverageScore;
        }
    }

    /**
     * 判定风险等级
     */
    private AIReviewReport.RiskLevel determineRiskLevel(BigDecimal duplicateRate, BigDecimal overallScore) {
        if (duplicateRate.compareTo(BigDecimal.valueOf(30)) > 0) {
            return AIReviewReport.RiskLevel.HIGH;
        }

        if (duplicateRate.compareTo(BigDecimal.valueOf(20)) > 0 ||
            overallScore.compareTo(BigDecimal.valueOf(60)) < 0) {
            return AIReviewReport.RiskLevel.MEDIUM;
        }

        return AIReviewReport.RiskLevel.LOW;
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
        judgeReview.setOverallScore(new BigDecimal(score.toString()));
        judgeReview.setReviewComment(comments);
        judgeReview.setStatus(JudgeReview.ReviewStatus.SUBMITTED);
        judgeReview.setReviewTime(LocalDateTime.now());

        return judgeReviewMapper.updateById(judgeReview) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitJudgeReviewWithDimensions(Long judgeReviewId, Integer innovationScore,
        Integer practicalityScore, Integer userExperienceScore, Integer overallScore, String comments) {
        JudgeReview judgeReview = judgeReviewMapper.selectById(judgeReviewId);
        if (judgeReview == null) {
            throw new RuntimeException("评委评审任务不存在");
        }

        if (judgeReview.getStatus() != JudgeReview.ReviewStatus.DRAFT) {
            throw new RuntimeException("评审任务已完成");
        }

        // 更新三个维度的评分
        judgeReview.setInnovationScore(new BigDecimal(innovationScore.toString()));
        judgeReview.setPracticalityScore(new BigDecimal(practicalityScore.toString()));
        judgeReview.setUserExperienceScore(new BigDecimal(userExperienceScore.toString()));
        judgeReview.setOverallScore(new BigDecimal(overallScore.toString()));
        judgeReview.setReviewComment(comments);
        judgeReview.setStatus(JudgeReview.ReviewStatus.SUBMITTED);
        judgeReview.setReviewTime(LocalDateTime.now());

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
        result.setAiScore(BigDecimal.valueOf(aiScore));
        result.setJudgeAvgScore(BigDecimal.valueOf(judgeAvgScore));
        result.setFinalScore(BigDecimal.valueOf(finalScore));
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

    @Override
    public List<SubmissionReviewSummaryDTO> listSubmissionReviewSummary(Long competitionId) {
        // 批量查询优化：避免N+1查询问题
        // 1. 先查询作品（根据赛事ID过滤，如果competitionId为null则查询所有）
        LambdaQueryWrapper<Work> workWrapper = new LambdaQueryWrapper<>();
        if (competitionId != null) {
            workWrapper.eq(Work::getCompetitionId, competitionId);
        }
        List<Work> works = workMapper.selectList(workWrapper);

        if (works.isEmpty()) {
            return new ArrayList<>();
        }

        // 提取作品ID用于查询提交记录
        Set<Long> workIds = works.stream().map(Work::getId).collect(Collectors.toSet());
        Map<Long, Work> workMap = works.stream().collect(Collectors.toMap(Work::getId, w -> w));

        // 2. 查询这些作品的提交记录
        LambdaQueryWrapper<Submission> submissionWrapper = new LambdaQueryWrapper<>();
        submissionWrapper.in(Submission::getWorkId, workIds);
        List<Submission> submissions = submissionMapper.selectList(submissionWrapper);

        if (submissions.isEmpty()) {
            return new ArrayList<>();
        }

        // 3. 批量查询关联实体（避免逐个查询）
        // 提取所有ID用于批量查询
        Set<Long> teamIds = submissions.stream().map(Submission::getTeamId).collect(Collectors.toSet());
        Set<Long> submissionIds = submissions.stream().map(Submission::getId).collect(Collectors.toSet());

        // 批量查询团队
        List<Team> teams = teamMapper.selectList(new LambdaQueryWrapper<Team>().in(Team::getId, teamIds));
        Map<Long, Team> teamMap = teams.stream().collect(Collectors.toMap(Team::getId, t -> t));

        // 批量查询赛道（从作品中提取赛道ID）
        Set<Long> trackIds = works.stream().map(Work::getTrackId).filter(id -> id != null).collect(Collectors.toSet());
        Map<Long, CompetitionTrack> trackMap;
        if (trackIds.isEmpty()) {
            trackMap = new java.util.HashMap<>();
        } else {
            List<CompetitionTrack> tracks = competitionTrackMapper.selectList(new LambdaQueryWrapper<CompetitionTrack>().in(CompetitionTrack::getId, trackIds));
            trackMap = tracks.stream().collect(Collectors.toMap(CompetitionTrack::getId, t -> t));
        }

        // 批量查询所有评委评审记录
        List<JudgeReview> allJudgeReviews = judgeReviewMapper.selectList(new LambdaQueryWrapper<JudgeReview>().in(JudgeReview::getSubmissionId, submissionIds));

        // 批量查询评委用户信息
        Set<Long> judgeIds = allJudgeReviews.stream().map(JudgeReview::getJudgeId).collect(Collectors.toSet());
        Map<Long, User> judgeMap;
        if (judgeIds.isEmpty()) {
            judgeMap = new java.util.HashMap<>();
        } else {
            List<User> judges = userMapper.selectList(new LambdaQueryWrapper<User>().in(User::getId, judgeIds));
            judgeMap = judges.stream().collect(Collectors.toMap(User::getId, u -> u));
        }

        // 按提交ID分组评审记录
        Map<Long, List<JudgeReview>> reviewsBySubmission = allJudgeReviews.stream()
            .collect(Collectors.groupingBy(JudgeReview::getSubmissionId));

        // 批量查询评审结果
        List<ReviewResult> reviewResults = baseMapper.selectList(new LambdaQueryWrapper<ReviewResult>().in(ReviewResult::getSubmissionId, submissionIds));
        Map<Long, ReviewResult> resultMap = reviewResults.stream()
            .collect(Collectors.toMap(ReviewResult::getSubmissionId, r -> r));

        // 3. 构建汇总列表（使用预加载的数据）
        List<SubmissionReviewSummaryDTO> summaryList = new ArrayList<>();

        for (Submission submission : submissions) {
            Work work = workMap.get(submission.getWorkId());
            if (work == null) continue;

            Team team = teamMap.get(submission.getTeamId());
            if (team == null) continue;

            CompetitionTrack track = trackMap.get(work.getTrackId());
            if (track == null) continue;

            // 使用预加载的评审记录
            List<JudgeReview> judgeReviews = reviewsBySubmission.getOrDefault(submission.getId(), new ArrayList<>());

            // 统计评审状态
            int completedCount = 0;
            int pendingCount = 0;
            BigDecimal totalScore = BigDecimal.ZERO;
            List<JudgeReviewDetail> reviewDetails = new ArrayList<>();

            for (JudgeReview review : judgeReviews) {
                User judge = judgeMap.get(review.getJudgeId());

                JudgeReviewDetail detail = new JudgeReviewDetail();
                detail.setJudgeId(review.getJudgeId());
                detail.setJudgeName(judge != null ? judge.getRealName() : "评委" + review.getJudgeId());
                detail.setOverallScore(review.getOverallScore());
                detail.setReviewStatus(review.getStatus() != null ? review.getStatus().name() : null);
                detail.setReviewTime(review.getReviewTime());
                detail.setReviewComment(review.getReviewComment());

                reviewDetails.add(detail);

                JudgeReview.ReviewStatus status = review.getStatus();
                if (status == JudgeReview.ReviewStatus.SUBMITTED || status == JudgeReview.ReviewStatus.CONFIRMED) {
                    completedCount++;
                    if (review.getOverallScore() != null) {
                        totalScore = totalScore.add(review.getOverallScore());
                    }
                } else {
                    pendingCount++;
                }
            }

            // 计算平均分和进度
            BigDecimal averageScore = null;
            if (completedCount > 0) {
                averageScore = totalScore.divide(BigDecimal.valueOf(completedCount), 2, RoundingMode.HALF_UP);
            }

            int totalReviewCount = judgeReviews.size();
            BigDecimal progress = BigDecimal.ZERO;
            if (totalReviewCount > 0) {
                progress = BigDecimal.valueOf(completedCount)
                    .divide(BigDecimal.valueOf(totalReviewCount), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            }

            // 使用预加载的评审结果
            ReviewResult reviewResult = resultMap.get(submission.getId());

            // 构建汇总DTO
            SubmissionReviewSummaryDTO summary = new SubmissionReviewSummaryDTO();
            summary.setSubmissionId(submission.getId());
            summary.setReviewResultId(reviewResult != null ? reviewResult.getId() : null);
            summary.setSubmissionCode(submission.getSubmissionCode());
            summary.setTeamId(submission.getTeamId());
            summary.setTeamName(team.getTeamName());
            summary.setWorkId(work.getId());
            summary.setWorkName(work.getWorkName());
            summary.setCompetitionTrackId(track.getId());
            summary.setTrackName(track.getTrackName());
            summary.setSubmissionTime(submission.getSubmissionTime());
            summary.setSubmissionStatus(submission.getStatus() != null ? submission.getStatus().name() : null);
            summary.setCompletedReviewCount(completedCount);
            summary.setPendingReviewCount(pendingCount);
            summary.setTotalReviewCount(totalReviewCount);
            summary.setReviewProgress(progress);
            summary.setAverageScore(averageScore);
            summary.setAiScore(reviewResult != null ? reviewResult.getAiScore() : null);
            summary.setFinalScore(reviewResult != null ? reviewResult.getFinalScore() : null);
            summary.setAwardLevel(reviewResult != null && reviewResult.getAwardLevel() != null ?
                reviewResult.getAwardLevel().name() : null);
            summary.setAllReviewsCompleted(completedCount == totalReviewCount && totalReviewCount > 0);
            summary.setFinalResultCalculated(reviewResult != null);
            summary.setJudgeReviews(reviewDetails);

            summaryList.add(summary);
        }

        return summaryList;
    }

    @Override
    public AIReviewDetailDTO getAIReviewDetailDTO(Long submissionId) {
        // 1. 查询AI评审报告
        AIReviewReport report = getAIReviewReport(submissionId);
        if (report == null) {
            return null;
        }

        // 2. 查询AI评审详细项
        List<AIReviewDetail> details = listAIReviewDetails(report.getId());

        // 3. 解析详细项：分类亮点、不足、建议
        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();
        List<String> improvementSuggestions = new ArrayList<>();

        for (AIReviewDetail detail : details) {
            String checkItem = detail.getCheckItem();
            String checkResult = detail.getCheckResult();

            if (checkItem != null && checkResult != null) {
                if (checkItem.startsWith("作品亮点")) {
                    strengths.add(checkResult);
                } else if (checkItem.startsWith("不足之处")) {
                    weaknesses.add(checkResult);
                } else if (checkItem.startsWith("改进建议")) {
                    improvementSuggestions.add(checkResult);
                }
            }
        }

        // 4. 构建DTO
        com.example.workcompetitionplatform.dto.AIReviewDetailDTO dto =
            new com.example.workcompetitionplatform.dto.AIReviewDetailDTO();

        dto.setId(report.getId());
        dto.setSubmissionId(report.getSubmissionId());
        dto.setOverallScore(report.getOverallScore());
        dto.setInnovationScore(report.getInnovationScore());
        dto.setPracticalityScore(report.getPracticalityScore());
        dto.setUserExperienceScore(report.getUserExperienceScore());
        dto.setDocumentationScore(report.getDocumentationScore());
        dto.setCodeQualityScore(report.getCodeQualityScore());
        dto.setDuplicateRate(report.getDuplicateRate());

        dto.setReviewSummary(report.getReviewSummary());
        dto.setStrengths(strengths);
        dto.setWeaknesses(weaknesses);
        dto.setImprovementSuggestions(improvementSuggestions);

        dto.setRiskLevel(report.getRiskLevel() != null ? report.getRiskLevel().name() : "LOW");
        dto.setAgentType(report.getAiModel());
        dto.setAiModel(report.getAiModel());

        if (report.getReviewTime() != null) {
            dto.setReviewTime(report.getReviewTime().toString());
        }

        // 5. 查询作品类型
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission != null) {
            Work work = workMapper.selectById(submission.getWorkId());
            if (work != null && work.getWorkType() != null) {
                dto.setWorkType(work.getWorkType().name());
            }
        }

        // 通用评分维度（存入通用字段，前端按workType读取不同维度）
        dto.setInnovationScore(report.getInnovationScore());
        dto.setPracticalityScore(report.getPracticalityScore());
        dto.setUserExperienceScore(report.getUserExperienceScore());
        dto.setDocumentationScore(report.getDocumentationScore());
        dto.setCodeQualityScore(report.getCodeQualityScore());
        dto.setDuplicateRate(report.getDuplicateRate());

        // PPT/VIDEO作品从通用字段重映射到类型专用字段
        // 通用字段实际含义：innovation≈创意, userExperience≈视觉效果, documentation≈内容呈现/导演技巧, practicality≈原创性
        dto.setCreativityScore(report.getInnovationScore());
        dto.setVisualEffectScore(report.getUserExperienceScore());
        dto.setContentPresentationScore(report.getDocumentationScore());
        dto.setOriginalityScore(report.getPracticalityScore());
        dto.setStoryScore(report.getInnovationScore());
        dto.setDirectorSkillScore(report.getDocumentationScore());

        return dto;
    }

    @Override
    public AIReviewDetailDTO getAIReviewDetailDTO(Long submissionId, Submission submission) {
        AIReviewReport report = getAIReviewReport(submissionId);
        if (report == null) {
            return null;
        }

        List<AIReviewDetail> details = listAIReviewDetails(report.getId());

        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();
        List<String> improvementSuggestions = new ArrayList<>();

        for (AIReviewDetail detail : details) {
            String checkItem = detail.getCheckItem();
            String checkResult = detail.getCheckResult();

            if (checkItem != null && checkResult != null) {
                if (checkItem.startsWith("作品亮点")) {
                    strengths.add(checkResult);
                } else if (checkItem.startsWith("不足之处")) {
                    weaknesses.add(checkResult);
                } else if (checkItem.startsWith("改进建议")) {
                    improvementSuggestions.add(checkResult);
                }
            }
        }

        com.example.workcompetitionplatform.dto.AIReviewDetailDTO dto =
            new com.example.workcompetitionplatform.dto.AIReviewDetailDTO();

        dto.setId(report.getId());
        dto.setSubmissionId(report.getSubmissionId());
        dto.setOverallScore(report.getOverallScore());
        dto.setInnovationScore(report.getInnovationScore());
        dto.setPracticalityScore(report.getPracticalityScore());
        dto.setUserExperienceScore(report.getUserExperienceScore());
        dto.setDocumentationScore(report.getDocumentationScore());
        dto.setCodeQualityScore(report.getCodeQualityScore());
        dto.setDuplicateRate(report.getDuplicateRate());

        dto.setReviewSummary(report.getReviewSummary());
        dto.setStrengths(strengths);
        dto.setWeaknesses(weaknesses);
        dto.setImprovementSuggestions(improvementSuggestions);

        dto.setRiskLevel(report.getRiskLevel() != null ? report.getRiskLevel().name() : "LOW");
        dto.setAgentType(report.getAiModel());
        dto.setAiModel(report.getAiModel());

        if (report.getReviewTime() != null) {
            dto.setReviewTime(report.getReviewTime().toString());
        }

        // Use passed submission to avoid duplicate query
        if (submission != null) {
            Work work = workMapper.selectById(submission.getWorkId());
            if (work != null && work.getWorkType() != null) {
                dto.setWorkType(work.getWorkType().name());
            }
        }

        // CODE作品评分维度
        dto.setInnovationScore(report.getInnovationScore());
        dto.setPracticalityScore(report.getPracticalityScore());
        dto.setUserExperienceScore(report.getUserExperienceScore());
        dto.setDocumentationScore(report.getDocumentationScore());
        dto.setCodeQualityScore(report.getCodeQualityScore());
        dto.setDuplicateRate(report.getDuplicateRate());

        // PPT/VIDEO作品从通用字段重映射到类型专用字段
        dto.setCreativityScore(report.getInnovationScore());
        dto.setVisualEffectScore(report.getUserExperienceScore());
        dto.setContentPresentationScore(report.getDocumentationScore());
        dto.setOriginalityScore(report.getPracticalityScore());
        dto.setStoryScore(report.getInnovationScore());
        dto.setDirectorSkillScore(report.getDocumentationScore());

        return dto;
    }
}