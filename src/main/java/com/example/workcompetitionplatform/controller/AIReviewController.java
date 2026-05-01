package com.example.workcompetitionplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.workcompetitionplatform.dto.ApiResponse;
import com.example.workcompetitionplatform.dto.AIReviewCallbackRequest;
import com.example.workcompetitionplatform.entity.AIReviewReport;
import com.example.workcompetitionplatform.entity.AIReviewDetail;
import com.example.workcompetitionplatform.entity.Submission;
import com.example.workcompetitionplatform.entity.TeamMember;
import com.example.workcompetitionplatform.entity.Notification;
import com.example.workcompetitionplatform.mapper.SubmissionMapper;
import com.example.workcompetitionplatform.mapper.TeamMemberMapper;
import com.example.workcompetitionplatform.mapper.AIReviewReportMapper;
import com.example.workcompetitionplatform.mapper.NotificationMapper;
import com.example.workcompetitionplatform.service.IReviewService;
import com.example.workcompetitionplatform.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI评审控制器
 * 提供AI初审相关的API接口
 *
 * @author 陈海波
 * @since 2026-04-13
 */
@RestController
@RequestMapping("/api/ai-reviews")
@Tag(name = "AI评审管理", description = "AI初审相关接口")
public class AIReviewController {

    @Autowired
    private IReviewService reviewService;

    @Autowired
    private SubmissionMapper submissionMapper;

    @Autowired
    private TeamMemberMapper teamMemberMapper;

    @Autowired
    private AIReviewReportMapper aiReviewReportMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    /**
     * 执行AI初审
     *
     * @param submissionId 提交ID
     * @return AI评审报告
     */
    @PostMapping("/perform/{submissionId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "执行AI初审", description = "对指定提交的作品执行AI初审")
    public ApiResponse<AIReviewReport> performAIReview(
            @Parameter(description = "提交ID") @PathVariable Long submissionId) {
        try {
            AIReviewReport report = reviewService.performAIReview(submissionId);
            return ApiResponse.success(report);
        } catch (Exception e) {
            return ApiResponse.error(500, "AI审核失败: " + e.getMessage());
        }
    }

    /**
     * 获取AI评审报告
     *
     * @param submissionId 提交ID
     * @return AI评审报告
     */
    @GetMapping("/report/{submissionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'JUDGE', 'STUDENT')")
    @Operation(summary = "获取AI评审报告", description = "根据提交ID查询AI评审报告")
    public ApiResponse<AIReviewReport> getAIReviewReport(
            @Parameter(description = "提交ID") @PathVariable Long submissionId) {
        AIReviewReport report = reviewService.getAIReviewReport(submissionId);
        if (report == null) {
            return ApiResponse.error(404, "AI评审报告不存在");
        }
        return ApiResponse.success(report);
    }

    /**
     * 获取AI评审详情列表
     *
     * @param reportId 报告ID
     * @return 评审详情列表
     */
    @GetMapping("/details/{reportId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'JUDGE', 'STUDENT')")
    @Operation(summary = "获取AI评审详情", description = "根据报告ID查询评审详情列表")
    public ApiResponse<List<AIReviewDetail>> getAIReviewDetails(
            @Parameter(description = "报告ID") @PathVariable Long reportId) {
        List<AIReviewDetail> details = reviewService.listAIReviewDetails(reportId);
        return ApiResponse.success(details);
    }

    /**
     * 查询作品的AI审核状态
     * 学生可查看自己作品的审核进度
     *
     * @param workId 作品ID
     * @return 审核状态信息
     */
    @GetMapping("/status/{workId}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "查询AI审核状态", description = "学生查看自己作品的AI审核进度")
    public ApiResponse<Map<String, Object>> getAIReviewStatus(
            @Parameter(description = "作品ID") @PathVariable Long workId) {

        // 查询作品的提交记录
        LambdaQueryWrapper<Submission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Submission::getWorkId, workId);
        Submission submission = submissionMapper.selectOne(wrapper);

        if (submission == null) {
            return ApiResponse.error(404, "作品未提交");
        }

        // 验证用户是作品团队成员
        Long userId = UserContext.getCurrentUserId();
        LambdaQueryWrapper<TeamMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(TeamMember::getTeamId, submission.getTeamId());
        memberWrapper.eq(TeamMember::getUserId, userId);
        TeamMember member = teamMemberMapper.selectOne(memberWrapper);
        if (member == null) {
            return ApiResponse.error(403, "无权查看此作品的审核状态");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("status", submission.getStatus().name());
        result.put("validationResult", submission.getValidationResult());
        result.put("submissionId", submission.getId());

        // 如果审核完成，返回报告概要
        if (submission.getStatus() == Submission.SubmissionStatus.VALID
                || submission.getStatus() == Submission.SubmissionStatus.INVALID) {
            AIReviewReport report = reviewService.getAIReviewReport(submission.getId());
            if (report != null) {
                result.put("overallScore", report.getOverallScore());
                result.put("riskLevel", report.getRiskLevel() != null ? report.getRiskLevel().name() : null);
                result.put("reviewSummary", report.getReviewSummary());
                result.put("duplicateRate", report.getDuplicateRate());
            }
        }

        return ApiResponse.success(result);
    }

    /**
     * 获取作品的完整AI审核报告
     * 学生查看详细审核结果
     *
     * @param workId 作品ID
     * @return 完整AI审核报告
     */
    @GetMapping("/full-report/{workId}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "获取完整AI审核报告", description = "学生查看作品的完整AI审核报告")
    public ApiResponse<AIReviewReport> getFullReportByWorkId(
            @Parameter(description = "作品ID") @PathVariable Long workId) {

        // 查询作品的提交记录
        LambdaQueryWrapper<Submission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Submission::getWorkId, workId);
        Submission submission = submissionMapper.selectOne(wrapper);

        if (submission == null) {
            return ApiResponse.error(404, "作品未提交");
        }

        // 验证用户是作品团队成员
        Long userId = UserContext.getCurrentUserId();
        LambdaQueryWrapper<TeamMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(TeamMember::getTeamId, submission.getTeamId());
        memberWrapper.eq(TeamMember::getUserId, userId);
        TeamMember member = teamMemberMapper.selectOne(memberWrapper);
        if (member == null) {
            return ApiResponse.error(403, "无权查看此作品的审核报告");
        }

        // 检查审核是否完成
        if (submission.getStatus() != Submission.SubmissionStatus.VALID
                && submission.getStatus() != Submission.SubmissionStatus.INVALID) {
            return ApiResponse.error(400, "AI审核尚未完成");
        }

        AIReviewReport report = reviewService.getAIReviewReport(submission.getId());
        if (report == null) {
            return ApiResponse.error(404, "AI评审报告不存在");
        }

        return ApiResponse.success(report);
    }

    /**
     * Python Agent回调接口
     * 接收AI审核完成的报告，保存到数据库并更新状态
     *
     * @param request AI审核回调请求
     * @return 操作结果
     */
    @PostMapping("/callback")
    @Operation(summary = "Python Agent回调接口", description = "接收AI审核完成的报告")
    public ApiResponse<Void> receiveAIReviewCallback(
            @Parameter(description = "AI审核回调请求") @RequestBody AIReviewCallbackRequest request) {

        try {
            // 1. 提取报告数据
            AIReviewCallbackRequest.AIReviewReportDTO reportDTO = request.getReport();
            Long submissionId = request.getSubmissionId();

            // 2. 创建并保存AIReviewReport
            AIReviewReport report = new AIReviewReport();
            report.setSubmissionId(submissionId);

            // 获取提交信息以查询teamId
            Submission submission = submissionMapper.selectById(submissionId);
            if (submission == null) {
                return ApiResponse.error(404, "提交记录不存在");
            }
            report.setTeamId(submission.getTeamId());

            // 设置评分
            report.setOverallScore(reportDTO.getOverallScore());
            report.setInnovationScore(reportDTO.getInnovationScore());
            report.setPracticalityScore(reportDTO.getPracticalityScore());
            report.setUserExperienceScore(reportDTO.getUserExperienceScore());
            report.setDocumentationScore(reportDTO.getDocumentationScore());

            // 设置代码质量指标（如果有）
            if (reportDTO.getDuplicateRate() != null) {
                report.setDuplicateRate(reportDTO.getDuplicateRate());
            }
            if (reportDTO.getCodeQualityScore() != null) {
                report.setCodeQualityScore(reportDTO.getCodeQualityScore());
            }

            // 设置文本内容
            report.setReviewSummary(reportDTO.getReviewSummary());

            // 将建议列表转换为字符串
            if (reportDTO.getImprovementSuggestions() != null && !reportDTO.getImprovementSuggestions().isEmpty()) {
                String suggestions = String.join("\n", reportDTO.getImprovementSuggestions());
                report.setImprovementSuggestions(suggestions);
            }

            report.setAiModel(reportDTO.getAiModel());

            // 设置风险等级
            try {
                report.setRiskLevel(AIReviewReport.RiskLevel.valueOf(reportDTO.getRiskLevel()));
            } catch (IllegalArgumentException e) {
                report.setRiskLevel(AIReviewReport.RiskLevel.LOW);
            }

            // 保存报告
            aiReviewReportMapper.insert(report);

            // 3. 更新Submission状态
            if (report.getRiskLevel() == AIReviewReport.RiskLevel.HIGH) {
                submission.setStatus(Submission.SubmissionStatus.INVALID);
            } else {
                submission.setStatus(Submission.SubmissionStatus.VALID);
            }
            submissionMapper.updateById(submission);

            // 4. 发送通知给团队成员
            LambdaQueryWrapper<TeamMember> memberWrapper = new LambdaQueryWrapper<>();
            memberWrapper.eq(TeamMember::getTeamId, submission.getTeamId());
            List<TeamMember> members = teamMemberMapper.selectList(memberWrapper);

            for (TeamMember member : members) {
                Notification notification = new Notification();
                notification.setUserId(member.getUserId());
                notification.setTitle("AI审核完成通知");
                notification.setNotificationType(Notification.NotificationType.SYSTEM);

                String statusText = submission.getStatus() == Submission.SubmissionStatus.VALID ? "通过" : "未通过";
                String content = String.format(
                    "您的作品AI审核已完成，综合得分%.1f分，审核结果：%s。%s",
                    report.getOverallScore(),
                    statusText,
                    report.getRiskLevel() == AIReviewReport.RiskLevel.HIGH ? "请改进后重新提交。" : ""
                );

                notification.setContent(content);
                notificationMapper.insert(notification);
            }

            return ApiResponse.success();

        } catch (Exception e) {
            return ApiResponse.error(500, "处理AI审核回调失败: " + e.getMessage());
        }
    }

    /**
     * 根据提交ID获取AI审核报告（供评委使用）
     *
     * @param submissionId 提交ID
     * @return AI审核报告
     */
    @GetMapping("/judge-report/{submissionId}")
    @PreAuthorize("hasRole('JUDGE')")
    @Operation(summary = "评委获取AI审核报告", description = "评委评审时查看AI审核结果作为参考")
    public ApiResponse<AIReviewReport> getJudgeAIReviewReport(
            @Parameter(description = "提交ID") @PathVariable Long submissionId) {

        AIReviewReport report = reviewService.getAIReviewReport(submissionId);
        if (report == null) {
            return ApiResponse.success(null); // 没有报告时返回null，不影响评审流程
        }

        return ApiResponse.success(report);
    }
}