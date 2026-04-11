package com.example.workcompetitionplatform.controller;

import com.example.workcompetitionplatform.dto.ApiResponse;
import com.example.workcompetitionplatform.entity.Submission;
import com.example.workcompetitionplatform.exception.BusinessException;
import com.example.workcompetitionplatform.service.ISubmissionService;
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
 * 作品提交控制器
 * 提供作品提交、查询和文件验证的接口
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Slf4j
@Tag(name = "作品提交")
@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    @Autowired
    private ISubmissionService submissionService;

    /**
     * 提交作品
     * 团队提交参赛作品
     *
     * @param teamId 团队ID
     * @param competitionTrackId 赛道ID
     * @param filePath 文件路径
     * @param fileName 文件名
     * @param fileSize 文件大小
     * @return API响应（包含提交记录）
     */
    @Operation(summary ="提交作品")
    @PostMapping
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')")
    public ApiResponse<Submission> submitWork(
            @Parameter(description ="团队ID") @RequestParam Long teamId,
            @Parameter(description ="赛道ID") @RequestParam Long competitionTrackId,
            @Parameter(description ="文件路径") @RequestParam String filePath,
            @Parameter(description ="文件名") @RequestParam String fileName,
            @Parameter(description ="文件大小") @RequestParam Long fileSize) {

        try {
            // 获取当前用户ID作为提交人
            Long submitterUserId = UserContext.getCurrentUserId();

            // 执行作品提交
            Submission submission = submissionService.submitWork(
                    teamId,
                    competitionTrackId,
                    submitterUserId,
                    filePath,
                    fileName,
                    fileSize
            );

            log.info("作品提交成功：团队 {} -> 赛道 {}", teamId, competitionTrackId);

            return ApiResponse.success("作品提交成功", submission);
        } catch (BusinessException e) {
            log.error("作品提交失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 查询提交详情
     * 根据提交ID查询提交详细信息
     *
     * @param id 提交ID
     * @return API响应（包含提交详情）
     */
    @Operation(summary ="查询提交详情")
    @GetMapping("/{id}")
    public ApiResponse<Submission> getSubmissionById(@Parameter(description ="提交ID") @PathVariable Long id) {
        Submission submission = submissionService.getById(id);

        if (submission == null) {
            return ApiResponse.notFound("提交记录不存在");
        }

        return ApiResponse.success(submission);
    }

    /**
     * 查询我的提交
     * 查询当前用户的所有提交记录
     *
     * @return API响应（包含提交列表）
     */
    @Operation(summary ="查询我的提交")
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Submission>> listMySubmissions() {
        Long userId = UserContext.getCurrentUserId();
        List<Submission> submissions = submissionService.listByUserId(userId);
        return ApiResponse.success(submissions);
    }

    /**
     * 查询团队提交记录
     * 查询指定团队的所有提交记录
     *
     * @param teamId 团队ID
     * @return API响应（包含提交列表）
     */
    @Operation(summary ="查询团队提交记录")
    @GetMapping("/team/{teamId}")
    public ApiResponse<List<Submission>> listTeamSubmissions(@Parameter(description ="团队ID") @PathVariable Long teamId) {
        List<Submission> submissions = submissionService.listByTeamId(teamId);
        return ApiResponse.success(submissions);
    }

    /**
     * 查询赛道提交记录
     * 查询指定赛道的所有提交记录（管理员和评委）
     *
     * @param competitionTrackId 赛道ID
     * @return API响应（包含提交列表）
     */
    @Operation(summary ="查询赛道提交记录")
    @GetMapping("/track/{competitionTrackId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ApiResponse<List<Submission>> listTrackSubmissions(@Parameter(description ="赛道ID") @PathVariable Long competitionTrackId) {
        List<Submission> submissions = submissionService.listByCompetitionTrackId(competitionTrackId);
        return ApiResponse.success(submissions);
    }

    /**
     * 查询赛事提交记录
     * 查询指定赛事的所有提交记录（管理员）
     *
     * @param competitionId 赛事ID
     * @return API响应（包含提交列表）
     */
    @Operation(summary ="查询赛事提交记录")
    @GetMapping("/competition/{competitionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<Submission>> listCompetitionSubmissions(@Parameter(description ="赛事ID") @PathVariable Long competitionId) {
        List<Submission> submissions = submissionService.listByCompetitionId(competitionId);
        return ApiResponse.success(submissions);
    }

    /**
     * 查询提交的文件验证日志
     * 查询指定提交的文件验证日志记录
     *
     * @param id 提交ID
     * @return API响应（包含文件验证日志列表）
     */
    @Operation(summary ="查询提交的文件验证日志")
    @GetMapping("/{id}/validation-logs")
    public ApiResponse<List<?>> listFileValidationLogs(@Parameter(description ="提交ID") @PathVariable Long id) {
        List<?> validationLogs = submissionService.listFileValidationLogs(id);
        return ApiResponse.success(validationLogs);
    }

    /**
     * 获取团队最新提交
     * 查询团队在指定赛道的最新提交记录
     *
     * @param teamId 团队ID
     * @param competitionTrackId 赛道ID
     * @return API响应（包含最新提交）
     */
    @Operation(summary ="获取团队最新提交")
    @GetMapping("/latest")
    public ApiResponse<Submission> getLatestSubmission(
            @Parameter(description ="团队ID") @RequestParam Long teamId,
            @Parameter(description ="赛道ID") @RequestParam Long competitionTrackId) {

        Submission submission = submissionService.getLatestSubmission(teamId, competitionTrackId);

        if (submission == null) {
            return ApiResponse.notFound("未找到提交记录");
        }

        return ApiResponse.success(submission);
    }

    /**
     * 检查团队是否已提交
     * 检查团队在指定赛道是否已提交作品
     *
     * @param teamId 团队ID
     * @param competitionTrackId 赛道ID
     * @return API响应
     */
    @Operation(summary ="检查团队是否已提交")
    @GetMapping("/check-submitted")
    public ApiResponse<Boolean> checkTeamSubmitted(
            @Parameter(description ="团队ID") @RequestParam Long teamId,
            @Parameter(description ="赛道ID") @RequestParam Long competitionTrackId) {

        boolean submitted = submissionService.hasTeamSubmitted(teamId, competitionTrackId);
        return ApiResponse.success(submitted);
    }

    /**
     * 获取赛道提交数量
     * 统计指定赛道的提交数量
     *
     * @param competitionTrackId 赛道ID
     * @return API响应
     */
    @Operation(summary ="获取赛道提交数量")
    @GetMapping("/count/track/{competitionTrackId}")
    public ApiResponse<Integer> getTrackSubmissionCount(@Parameter(description ="赛道ID") @PathVariable Long competitionTrackId) {
        int count = submissionService.getSubmissionCount(competitionTrackId);
        return ApiResponse.success(count);
    }

    /**
     * 获取赛事提交数量
     * 统计指定赛事的提交数量（管理员）
     *
     * @param competitionId 赛事ID
     * @return API响应
     */
    @Operation(summary ="获取赛事提交数量")
    @GetMapping("/count/competition/{competitionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Integer> getCompetitionSubmissionCount(@Parameter(description ="赛事ID") @PathVariable Long competitionId) {
        int count = submissionService.getCompetitionSubmissionCount(competitionId);
        return ApiResponse.success(count);
    }

    /**
     * 查询所有提交记录
     * 查询系统中所有的提交记录（管理员）
     *
     * @return API响应（包含提交列表）
     */
    @Operation(summary = "查询所有提交记录")
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('JUDGE')")
    public ApiResponse<List<Submission>> listAllSubmissions() {
        List<Submission> submissions = submissionService.listAllSubmissions();
        return ApiResponse.success(submissions);
    }
}