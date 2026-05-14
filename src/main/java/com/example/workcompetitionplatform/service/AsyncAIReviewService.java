package com.example.workcompetitionplatform.service;

import com.example.workcompetitionplatform.entity.Notification;
import com.example.workcompetitionplatform.entity.Submission;
import com.example.workcompetitionplatform.entity.TeamMember;
import com.example.workcompetitionplatform.entity.Work;
import com.example.workcompetitionplatform.mapper.NotificationMapper;
import com.example.workcompetitionplatform.mapper.SubmissionMapper;
import com.example.workcompetitionplatform.mapper.TeamMemberMapper;
import com.example.workcompetitionplatform.mapper.WorkMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 异步 AI 评审服务
 * 将评审任务派发到 Python AI Agent，由 Agent 异步处理后回调
 *
 * @author 陈海波
 * @since 2026-04-20
 */
@Slf4j
@Service
public class AsyncAIReviewService {

    @Autowired
    private SubmissionMapper submissionMapper;

    @Autowired
    private WorkMapper workMapper;

    @Autowired
    private TeamMemberMapper teamMemberMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    @Value("${python.agent.url}")
    private String pythonAgentUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 异步派发 AI 评审任务到 Python Agent
     * Agent 处理完成后通过回调接口 /api/ai-reviews/callback 返回结果
     *
     * @param submissionId 提交ID
     * @param userId       用户ID（用于发送错误通知）
     */
    @Async("aiReviewExecutor")
    public void performAIReviewAsync(Long submissionId, Long userId) {
        log.info("派发 AI 评审任务至 Python Agent，submissionId={}, userId={}", submissionId, userId);
        System.out.println("========================================");
        System.out.println("[ASYNC TASK] AI评审任务已触发");
        System.out.println("  - submissionId: " + submissionId);
        System.out.println("  - userId: " + userId);
        System.out.println("  - 线程名: " + Thread.currentThread().getName());
        System.out.println("========================================");

        try {
            Submission submission = submissionMapper.selectById(submissionId);
            if (submission == null) {
                log.error("提交记录不存在，submissionId={}", submissionId);
                System.out.println("[ERROR] 提交记录不存在: " + submissionId);
                return;
            }

            Work work = workMapper.selectById(submission.getWorkId());
            if (work == null) {
                log.error("作品不存在，workId={}", submission.getWorkId());
                System.out.println("[ERROR] 作品不存在: " + submission.getWorkId());
                return;
            }

            String workType = work.getWorkType() != null ? work.getWorkType().name() : "CODE";
            String filePath = submission.getFilePath();

            // 查询团队队长的学院信息（用于区分评审标准）
            String college = "";
            try {
                LambdaQueryWrapper<TeamMember> memberWrapper = new LambdaQueryWrapper<>();
                memberWrapper.eq(TeamMember::getTeamId, submission.getTeamId());
                memberWrapper.eq(TeamMember::getMemberRole, TeamMember.MemberRole.LEADER);
                TeamMember leader = teamMemberMapper.selectOne(memberWrapper);
                if (leader != null && leader.getCollege() != null) {
                    college = leader.getCollege();
                }
            } catch (Exception e) {
                log.warn("查询团队学院信息失败：{}", e.getMessage());
            }

            System.out.println("[INFO] 准备发送Python Agent请求:");
            System.out.println("  - workType: " + workType);
            System.out.println("  - filePath: " + filePath);
            System.out.println("  - college: " + college);
            System.out.println("  - workDescription: " + work.getDescription());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("submission_id", submissionId);
            requestBody.put("work_type", workType);
            requestBody.put("file_path", filePath);
            requestBody.put("work_description", work.getDescription());
            requestBody.put("college", college);

            String url = pythonAgentUrl + "/api/review";
            log.info("发送评审请求到 Python Agent: url={}, submissionId={}, workType={}", url, submissionId, workType);
            System.out.println("[REQUEST] Python Agent URL: " + url);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            System.out.println("[INFO] 正在发送HTTP POST请求...");
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(url, entity, (Class<Map<String, Object>>)(Class<?>)Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Python Agent 已接受评审任务，submissionId={}, response={}", submissionId, response.getBody());
                System.out.println("[SUCCESS] Python Agent响应成功");
                System.out.println("  - 响应状态: " + response.getStatusCode());
                System.out.println("  - 响应内容: " + response.getBody());
            } else {
                log.warn("Python Agent 返回非成功状态码，submissionId={}, status={}", submissionId, response.getStatusCode());
                System.out.println("[WARNING] Python Agent返回非成功状态: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("派发 AI 评审任务失败，submissionId={}, error={}", submissionId, e.getMessage(), e);
            System.out.println("[ERROR] AI评审任务失败: " + e.getMessage());
            e.printStackTrace();

            Submission submission = submissionMapper.selectById(submissionId);
            if (submission != null) {
                submission.setStatus(Submission.SubmissionStatus.INVALID);
                submission.setValidationResult("AI评审服务暂时不可用：" + e.getMessage());
                submissionMapper.updateById(submission);
                System.out.println("[INFO] Submission状态已更新为INVALID");
            }

            sendNotification(userId, Notification.NotificationType.SYSTEM,
                    "AI评审异常通知",
                    "AI评审服务暂时不可用，请联系管理员处理。");
        }
    }

    private void sendNotification(Long userId, Notification.NotificationType type,
                                  String title, String content) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setNotificationType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setIsRead(false);
        notification.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(notification);
        log.info("发送通知成功，userId={}, title={}", userId, title);
    }
}
