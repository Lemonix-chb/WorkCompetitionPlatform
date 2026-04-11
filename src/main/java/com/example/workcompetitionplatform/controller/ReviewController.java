package com.example.workcompetitionplatform.controller;

import com.example.workcompetitionplatform.dto.ApiResponse;
import com.example.workcompetitionplatform.entity.AIReviewReport;
import com.example.workcompetitionplatform.entity.JudgeReview;
import com.example.workcompetitionplatform.entity.ReviewResult;
import com.example.workcompetitionplatform.entity.User;
import com.example.workcompetitionplatform.exception.BusinessException;
import com.example.workcompetitionplatform.mapper.UserMapper;
import com.example.workcompetitionplatform.service.IReviewService;
import com.example.workcompetitionplatform.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评审管理控制器
 * 提供AI评审、评委评审、评审结果查询和奖项设置的接口
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Slf4j
@Tag(name = "评审管理")
@RestController
@RequestMapping("/api")
public class ReviewController {

    @Autowired
    private IReviewService reviewService;

    @Autowired
    private UserMapper userMapper;

    /**
     * 查询作品评审结果
     * 查询指定作品的评审结果详情
     *
     * @param id 提交ID
     * @return API响应（包含评审结果）
     */
    @Operation(summary ="查询作品评审结果")
    @GetMapping("/reviews/submission/{id}")
    public ApiResponse<ReviewResult> getReviewResultBySubmission(@Parameter(description ="提交ID") @PathVariable Long id) {
        ReviewResult reviewResult = reviewService.getReviewResult(id);

        if (reviewResult == null) {
            return ApiResponse.notFound("评审结果不存在");
        }

        return ApiResponse.success(reviewResult);
    }

    /**
     * 查询AI评审报告
     * 查询指定作品的AI初审报告
     *
     * @param submissionId 提交ID
     * @return API响应（包含AI评审报告）
     */
    @Operation(summary ="查询AI评审报告")
    @GetMapping("/reports/ai/{submissionId}")
    public ApiResponse<AIReviewReport> getAIReviewReport(@Parameter(description ="提交ID") @PathVariable Long submissionId) {
        AIReviewReport aiReport = reviewService.getAIReviewReport(submissionId);

        if (aiReport == null) {
            return ApiResponse.notFound("AI评审报告不存在");
        }

        return ApiResponse.success(aiReport);
    }

    /**
     * 执行AI初审
     * 对指定作品执行AI智能初审（管理员和教师）
     *
     * @param submissionId 提交ID
     * @return API响应（包含AI评审报告）
     */
    @Operation(summary ="执行AI初审")
    @PostMapping("/reviews/ai/{submissionId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ApiResponse<AIReviewReport> performAIReview(@Parameter(description ="提交ID") @PathVariable Long submissionId) {
        try {
            // 执行AI初审
            AIReviewReport aiReport = reviewService.performAIReview(submissionId);

            log.info("AI初审执行成功：{}", submissionId);

            return ApiResponse.success("AI初审执行成功", aiReport);
        } catch (BusinessException e) {
            log.error("AI初审执行失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 评委评分
     * 评委提交评审结果
     *
     * @param judgeReviewId 评委评审ID
     * @param score 评审分数
     * @param comments 评审意见
     * @return API响应
     */
    @Operation(summary ="评委评分")
    @PostMapping("/reviews/judge")
    @PreAuthorize("hasRole('JUDGE')")
    public ApiResponse<Void> submitJudgeReview(
            @Parameter(description ="评委评审ID") @RequestParam Long judgeReviewId,
            @Parameter(description ="评审分数") @RequestParam Integer score,
            @Parameter(description ="评审意见") @RequestParam(required = false) String comments) {

        try {
            // 执行评委评审
            boolean success = reviewService.submitJudgeReview(judgeReviewId, score, comments);

            if (!success) {
                return ApiResponse.error("评委评分失败");
            }

            log.info("评委评分成功：{}", judgeReviewId);

            return ApiResponse.success("评分已提交");
        } catch (BusinessException e) {
            log.error("评委评分失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 查询我的评审任务
     * 查询分配给当前评委的所有评审任务
     *
     * @return API响应（包含评审任务列表）
     */
    @Operation(summary ="查询我的评审任务")
    @GetMapping("/reviews/my")
    @PreAuthorize("hasRole('JUDGE')")
    public ApiResponse<List<JudgeReview>> listMyJudgeTasks() {
        Long judgeUserId = UserContext.getCurrentUserId();
        List<JudgeReview> judgeTasks = reviewService.listJudgeTasks(judgeUserId);
        return ApiResponse.success(judgeTasks);
    }

    /**
     * 查询作品的评委评审列表
     * 查询指定作品的所有评委评审记录
     *
     * @param submissionId 提交ID
     * @return API响应（包含评委评审列表）
     */
    @Operation(summary ="查询作品的评委评审列表")
    @GetMapping("/reviews/judge/submission/{submissionId}")
    public ApiResponse<List<JudgeReview>> listJudgeReviews(@Parameter(description ="提交ID") @PathVariable Long submissionId) {
        List<JudgeReview> judgeReviews = reviewService.listJudgeReviews(submissionId);
        return ApiResponse.success(judgeReviews);
    }

    /**
     * 分配评委评审任务
     * 管理员为指定作品分配评委评审任务（基于作品ID）
     *
     * @param workId 作品ID
     * @param judgeUserId 评委用户ID
     * @return API响应（包含评委评审）
     */
    @Operation(summary ="分配评委评审任务")
    @PostMapping("/reviews/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<JudgeReview> assignJudgeReview(
            @Parameter(description ="作品ID") @RequestParam Long workId,
            @Parameter(description ="评委用户ID") @RequestParam Long judgeUserId) {

        try {
            // 分配评审任务
            JudgeReview judgeReview = reviewService.assignJudgeReview(workId, judgeUserId);

            log.info("评审任务分配成功：作品 {} -> 评委 {}", workId, judgeUserId);

            return ApiResponse.success("评审任务已分配", judgeReview);
        } catch (BusinessException e) {
            log.error("评审任务分配失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 计算最终评审结果
     * 综合AI初审和评委评审，计算最终评审结果（管理员）
     *
     * @param submissionId 提交ID
     * @return API响应（包含评审结果）
     */
    @Operation(summary ="计算最终评审结果")
    @PostMapping("/reviews/calculate/{submissionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ReviewResult> calculateFinalResult(@Parameter(description ="提交ID") @PathVariable Long submissionId) {
        try {
            // 计算最终评审结果
            ReviewResult reviewResult = reviewService.calculateFinalResult(submissionId);

            log.info("最终评审结果计算成功：{}", submissionId);

            return ApiResponse.success("评审结果已计算", reviewResult);
        } catch (BusinessException e) {
            log.error("最终评审结果计算失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 设置奖项等级
     * 管理员为评审结果设置奖项等级
     *
     * @param reviewResultId 评审结果ID
     * @param awardLevel 奖项等级
     * @return API响应
     */
    @Operation(summary ="设置奖项等级")
    @PostMapping("/reviews/award/{reviewResultId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> setAwardLevel(
            @Parameter(description ="评审结果ID") @PathVariable Long reviewResultId,
            @Parameter(description ="奖项等级") @RequestParam ReviewResult.AwardLevel awardLevel) {

        try {
            // 设置奖项等级
            boolean success = reviewService.setAwardLevel(reviewResultId, awardLevel);

            if (!success) {
                return ApiResponse.error("奖项设置失败");
            }

            log.info("奖项等级设置成功：{} -> {}", reviewResultId, awardLevel);

            return ApiResponse.success("奖项已设置");
        } catch (BusinessException e) {
            log.error("奖项等级设置失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 查询赛道获奖团队排名
     * 查询指定赛道的获奖团队排名列表
     *
     * @param competitionTrackId 车道ID
     * @return API响应（包含评审结果列表）
     */
    @Operation(summary ="查询赛道获奖团队排名")
    @GetMapping("/reviews/ranking/{competitionTrackId}")
    public ApiResponse<List<ReviewResult>> listAwardedTeams(@Parameter(description ="车道ID") @PathVariable Long competitionTrackId) {
        List<ReviewResult> awardedTeams = reviewService.listAwardedTeams(competitionTrackId);
        return ApiResponse.success(awardedTeams);
    }

    /**
     * 查询赛道评审结果列表
     * 查询指定赛道的所有评审结果
     *
     * @param competitionTrackId 车道ID
     * @return API响应（包含评审结果列表）
     */
    @Operation(summary ="查询赛道评审结果列表")
    @GetMapping("/reviews/track/{competitionTrackId}")
    public ApiResponse<List<ReviewResult>> listReviewResultsByTrack(@Parameter(description ="车道ID") @PathVariable Long competitionTrackId) {
        List<ReviewResult> reviewResults = reviewService.listByCompetitionTrackId(competitionTrackId);
        return ApiResponse.success(reviewResults);
    }

    /**
     * 查询赛事评审结果列表
     * 查询指定赛事的所有评审结果（管理员）
     *
     * @param competitionId 赛事ID
     * @return API响应（包含评审结果列表）
     */
    @Operation(summary ="查询赛事评审结果列表")
    @GetMapping("/reviews/competition/{competitionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<ReviewResult>> listReviewResultsByCompetition(@Parameter(description ="赛事ID") @PathVariable Long competitionId) {
        List<ReviewResult> reviewResults = reviewService.listReviewResultsByCompetitionId(competitionId);
        return ApiResponse.success(reviewResults);
    }

    /**
     * 获取评委待评审任务数量
     * 统计评委的待评审任务数量
     *
     * @return API响应
     */
    @Operation(summary ="获取评委待评审任务数量")
    @GetMapping("/reviews/pending-count")
    @PreAuthorize("hasRole('JUDGE')")
    public ApiResponse<Integer> getJudgePendingTaskCount() {
        Long judgeUserId = UserContext.getCurrentUserId();
        int count = reviewService.getJudgePendingTaskCount(judgeUserId);
        return ApiResponse.success(count);
    }

    /**
     * 检查提交是否已完成所有评审
     * 检查指定作品是否已完成所有评审流程
     *
     * @param submissionId 提交ID
     * @return API响应
     */
    @Operation(summary ="检查提交是否已完成所有评审")
    @GetMapping("/reviews/completed/{submissionId}")
    public ApiResponse<Boolean> isReviewCompleted(@Parameter(description ="提交ID") @PathVariable Long submissionId) {
        boolean completed = reviewService.isReviewCompleted(submissionId);
        return ApiResponse.success(completed);
    }

    /**
     * 查询评委列表
     * 查询所有评委用户（管理员）
     *
     * @return API响应（包含评委列表）
     */
    @Operation(summary = "查询评委列表")
    @GetMapping("/judges")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<User>> listJudges() {
        List<User> judges = userMapper.selectByRole("JUDGE");
        return ApiResponse.success(judges);
    }

    /**
     * 查询待分配的作品提交
     * 查询已提交但未分配评委的作品列表（管理员）
     *
     * @param competitionId 赛事ID（可选）
     * @return API响应（包含待分配作品列表）
     */
    @Operation(summary = "查询待分配的作品提交")
    @GetMapping("/reviews/submissions-awaiting")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<JudgeReview>> listSubmissionsAwaitingAssignment(
            @Parameter(description = "赛事ID（可选）") @RequestParam(required = false) Long competitionId) {
        List<JudgeReview> submissions = reviewService.listSubmissionsAwaitingAssignment(competitionId);
        return ApiResponse.success(submissions);
    }

    /**
     * 批量分配评委
     * 管理员为多个作品批量分配相同的评委
     *
     * @param submissionIds 提交ID列表
     * @param judgeUserId 评委用户ID
     * @return API响应
     */
    @Operation(summary = "批量分配评委")
    @PostMapping("/reviews/batch-assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> batchAssignJudgeReview(
            @Parameter(description = "提交ID列表") @RequestBody List<Long> submissionIds,
            @Parameter(description = "评委用户ID") @RequestParam Long judgeUserId) {

        try {
            int successCount = reviewService.batchAssignJudgeReview(submissionIds, judgeUserId);

            log.info("批量分配成功：{} 个作品分配给评委 {}", successCount, judgeUserId);

            return ApiResponse.success(String.format("已成功分配%d个作品", successCount));
        } catch (BusinessException e) {
            log.error("批量分配失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 自动平均分配评委
     * 自动将待分配作品平均分配给所有评委
     *
     * @param competitionId 赛事ID（可选，null表示所有赛事）
     * @param judgesPerSubmission 每个作品分配的评委数量（默认2）
     * @return API响应（包含分配统计信息）
     */
    @Operation(summary = "自动平均分配评委")
    @PostMapping("/reviews/auto-assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> autoAssignJudges(
            @Parameter(description = "赛事ID（可选）") @RequestParam(required = false) Long competitionId,
            @Parameter(description = "每个作品分配的评委数量") @RequestParam(defaultValue = "2") int judgesPerSubmission) {

        try {
            String statistics = reviewService.autoAssignJudges(competitionId, judgesPerSubmission);

            log.info("自动分配完成：{}", statistics);

            return ApiResponse.success(statistics);
        } catch (BusinessException e) {
            log.error("自动分配失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }
}