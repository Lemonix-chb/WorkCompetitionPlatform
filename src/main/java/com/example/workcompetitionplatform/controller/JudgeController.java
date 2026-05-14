package com.example.workcompetitionplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.workcompetitionplatform.dto.ApiResponse;
import com.example.workcompetitionplatform.dto.JudgeStatsDTO;
import com.example.workcompetitionplatform.dto.JudgeStatsDTO.RecentReviewDTO;
import com.example.workcompetitionplatform.entity.JudgeReview;
import com.example.workcompetitionplatform.entity.Submission;
import com.example.workcompetitionplatform.entity.CompetitionTrack;
import com.example.workcompetitionplatform.entity.Work;
import com.example.workcompetitionplatform.mapper.JudgeReviewMapper;
import com.example.workcompetitionplatform.mapper.SubmissionMapper;
import com.example.workcompetitionplatform.mapper.WorkMapper;
import com.example.workcompetitionplatform.mapper.CompetitionTrackMapper;
import com.example.workcompetitionplatform.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 裁判控制器
 * 提供裁判相关的统计和功能接口
 *
 * @author 陈海波
 * @since 2026-04-11
 */
@Slf4j
@Tag(name = "裁判功能")
@RestController
@RequestMapping("/api/judge")
public class JudgeController {

    @Autowired
    private JudgeReviewMapper judgeReviewMapper;

    @Autowired
    private SubmissionMapper submissionMapper;

    @Autowired
    private WorkMapper workMapper;

    @Autowired
    private CompetitionTrackMapper trackMapper;

    /**
     * 查询裁判详细统计数据
     * 返回裁判的完整评审统计信息
     *
     * @return API响应（包含详细统计数据）
     */
    @Operation(summary = "查询裁判详细统计数据")
    @GetMapping("/stats")
    public ApiResponse<JudgeStatsDTO> getJudgeStats() {
        Long userId = UserContext.getCurrentUserId();

        JudgeStatsDTO stats = new JudgeStatsDTO();

        // 1. 基础统计
        QueryWrapper<JudgeReview> pendingWrapper = new QueryWrapper<>();
        pendingWrapper.eq("judge_id", userId).eq("status", "DRAFT");
        Long pendingCount = judgeReviewMapper.selectCount(pendingWrapper);
        stats.setPendingReviews(pendingCount.intValue());

        QueryWrapper<JudgeReview> completedWrapper = new QueryWrapper<>();
        completedWrapper.eq("judge_id", userId)
                .in("status", "SUBMITTED", "CONFIRMED");
        Long completedCount = judgeReviewMapper.selectCount(completedWrapper);
        stats.setCompletedReviews(completedCount.intValue());

        // 2. 评分统计
        QueryWrapper<JudgeReview> scoreWrapper = new QueryWrapper<>();
        scoreWrapper.eq("judge_id", userId)
                .in("status", "SUBMITTED", "CONFIRMED")
                .isNotNull("overall_score");
        List<JudgeReview> completedReviews = judgeReviewMapper.selectList(scoreWrapper);

        if (!completedReviews.isEmpty()) {
            List<BigDecimal> scores = completedReviews.stream()
                    .map(JudgeReview::getOverallScore)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            BigDecimal totalScore = scores.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.setTotalScores(totalScore.intValue());

            BigDecimal avgScore = totalScore.divide(
                    BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
            stats.setAverageScore(avgScore);

            stats.setMaxScore(scores.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
            stats.setMinScore(scores.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO));

            // 3. 评分分布
            stats.setExcellentCount((int) scores.stream().filter(s -> s.compareTo(new BigDecimal("85")) >= 0).count());
            stats.setGoodCount((int) scores.stream().filter(s -> s.compareTo(new BigDecimal("70")) >= 0 && s.compareTo(new BigDecimal("85")) < 0).count());
            stats.setAverageCount((int) scores.stream().filter(s -> s.compareTo(new BigDecimal("60")) >= 0 && s.compareTo(new BigDecimal("70")) < 0).count());
            stats.setPoorCount((int) scores.stream().filter(s -> s.compareTo(new BigDecimal("60")) < 0).count());

            // 4. 维度评分统计
            BigDecimal totalInnovation = completedReviews.stream()
                    .map(JudgeReview::getInnovationScore)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalPracticality = completedReviews.stream()
                    .map(JudgeReview::getPracticalityScore)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalUX = completedReviews.stream()
                    .map(JudgeReview::getUserExperienceScore)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            int count = completedReviews.size();
            stats.setAvgInnovationScore(totalInnovation.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
            stats.setAvgPracticalityScore(totalPracticality.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
            stats.setAvgUserExperienceScore(totalUX.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
        } else {
            stats.setTotalScores(0);
            stats.setAverageScore(BigDecimal.ZERO);
            stats.setMaxScore(BigDecimal.ZERO);
            stats.setMinScore(BigDecimal.ZERO);
            stats.setExcellentCount(0);
            stats.setGoodCount(0);
            stats.setAverageCount(0);
            stats.setPoorCount(0);
            stats.setAvgInnovationScore(BigDecimal.ZERO);
            stats.setAvgPracticalityScore(BigDecimal.ZERO);
            stats.setAvgUserExperienceScore(BigDecimal.ZERO);
        }

        // 5. 时间统计
        LocalDate today = LocalDate.now();
        LocalDateTime weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();

        QueryWrapper<JudgeReview> weekWrapper = new QueryWrapper<>();
        weekWrapper.eq("judge_id", userId)
                .in("status", "SUBMITTED", "CONFIRMED")
                .ge("review_time", weekStart);
        Long weekCount = judgeReviewMapper.selectCount(weekWrapper);
        stats.setThisWeekReviews(weekCount.intValue());

        QueryWrapper<JudgeReview> monthWrapper = new QueryWrapper<>();
        monthWrapper.eq("judge_id", userId)
                .in("status", "SUBMITTED", "CONFIRMED")
                .ge("review_time", monthStart);
        Long monthCount = judgeReviewMapper.selectCount(monthWrapper);
        stats.setThisMonthReviews(monthCount.intValue());

        // 6. 赛道分布统计
        Map<String, Integer> trackDistribution = new LinkedHashMap<>();
        Map<String, BigDecimal> trackAverageScores = new LinkedHashMap<>();

        // 获取所有已评审的submission，查询其赛道信息
        for (JudgeReview review : completedReviews) {
            Submission submission = submissionMapper.selectById(review.getSubmissionId());
            if (submission != null) {
                Work work = workMapper.selectById(submission.getWorkId());
                if (work != null && work.getTrackId() != null) {
                    CompetitionTrack track = trackMapper.selectById(work.getTrackId());
                    if (track != null) {
                        String trackName = track.getTrackName();
                        trackDistribution.merge(trackName, 1, Integer::sum);

                        // 计算赛道平均分（稍后汇总）
                        BigDecimal score = review.getOverallScore();
                        if (score != null) {
                            // 这里先计数，后面再计算平均分
                        }
                    }
                }
            }
        }

        // 计算各赛道平均评分
        Map<String, List<BigDecimal>> trackScoresMap = new HashMap<>();
        for (JudgeReview review : completedReviews) {
            if (review.getOverallScore() != null) {
                Submission submission = submissionMapper.selectById(review.getSubmissionId());
                if (submission != null) {
                    Work work = workMapper.selectById(submission.getWorkId());
                    if (work != null && work.getTrackId() != null) {
                        CompetitionTrack track = trackMapper.selectById(work.getTrackId());
                        if (track != null) {
                            trackScoresMap.computeIfAbsent(track.getTrackName(), k -> new ArrayList<>())
                                    .add(review.getOverallScore());
                        }
                    }
                }
            }
        }

        for (Map.Entry<String, List<BigDecimal>> entry : trackScoresMap.entrySet()) {
            BigDecimal avg = entry.getValue().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(entry.getValue().size()), 2, RoundingMode.HALF_UP);
            trackAverageScores.put(entry.getKey(), avg);
        }

        stats.setTrackDistribution(trackDistribution);
        stats.setTrackAverageScores(trackAverageScores);
        stats.setAssignedTracks(trackDistribution.size());

        // 7. 最近7天评审趋势
        Map<String, Integer> recentTrend = new LinkedHashMap<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

            QueryWrapper<JudgeReview> dayWrapper = new QueryWrapper<>();
            dayWrapper.eq("judge_id", userId)
                    .in("status", "SUBMITTED", "CONFIRMED")
                    .ge("review_time", dayStart)
                    .lt("review_time", dayEnd);
            Long dayCount = judgeReviewMapper.selectCount(dayWrapper);

            recentTrend.put(date.format(dateFormatter), dayCount.intValue());
        }
        stats.setRecentTrend(recentTrend);

        // 8. 最近评审列表
        QueryWrapper<JudgeReview> recentWrapper = new QueryWrapper<>();
        recentWrapper.eq("judge_id", userId)
                .in("status", "SUBMITTED", "CONFIRMED")
                .orderByDesc("review_time")
                .last("LIMIT 5");
        List<JudgeReview> recentReviewsList = judgeReviewMapper.selectList(recentWrapper);

        List<RecentReviewDTO> recentReviews = new ArrayList<>();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (JudgeReview review : recentReviewsList) {
            RecentReviewDTO dto = new RecentReviewDTO();
            dto.setReviewId(review.getId());
            dto.setOverallScore(review.getOverallScore());
            dto.setReviewDate(review.getReviewTime() != null ?
                    review.getReviewTime().format(dateTimeFormatter) : "未知时间");
            dto.setStatus(review.getStatus().name());

            // 获取作品和团队名称
            Submission submission = submissionMapper.selectById(review.getSubmissionId());
            if (submission != null) {
                Work work = workMapper.selectById(submission.getWorkId());
                if (work != null) {
                    dto.setWorkName(work.getWorkName());
                }
                // 团队名称需要通过team_id查询
                dto.setTeamName("团队-" + review.getTeamId());
            }

            recentReviews.add(dto);
        }
        stats.setRecentReviews(recentReviews);

        log.info("查询裁判详细统计数据：用户ID {}，待评审 {}，已评审 {}，平均分 {}",
                userId, pendingCount, completedCount, stats.getAverageScore());

        return ApiResponse.success(stats);
    }
}