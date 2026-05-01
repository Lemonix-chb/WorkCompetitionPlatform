package com.example.workcompetitionplatform.service;

import com.example.workcompetitionplatform.entity.Notification;
import com.example.workcompetitionplatform.entity.Submission;
import com.example.workcompetitionplatform.entity.Work;
import com.example.workcompetitionplatform.mapper.NotificationMapper;
import com.example.workcompetitionplatform.mapper.SubmissionMapper;
import com.example.workcompetitionplatform.mapper.WorkMapper;
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
 * 异步 AI 审核服务
 * 将审核任务派发到 Python AI Agent，由 Agent 异步处理后回调
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
    private NotificationMapper notificationMapper;

    @Value("${python.agent.url}")
    private String pythonAgentUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 异步派发 AI 审核任务到 Python Agent
     * Agent 处理完成后通过回调接口 /api/ai-reviews/callback 返回结果
     *
     * @param submissionId 提交ID
     * @param userId       用户ID（用于发送错误通知）
     */
    @Async("aiReviewExecutor")
    public void performAIReviewAsync(Long submissionId, Long userId) {
        log.info("派发 AI 审核任务至 Python Agent，submissionId={}, userId={}", submissionId, userId);

        try {
            Submission submission = submissionMapper.selectById(submissionId);
            if (submission == null) {
                log.error("提交记录不存在，submissionId={}", submissionId);
                return;
            }

            Work work = workMapper.selectById(submission.getWorkId());
            if (work == null) {
                log.error("作品不存在，workId={}", submission.getWorkId());
                return;
            }

            String workType = work.getWorkType() != null ? work.getWorkType().name() : "CODE";
            String filePath = submission.getFilePath();

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("submission_id", submissionId);
            requestBody.put("work_type", workType);
            requestBody.put("file_path", filePath);
            requestBody.put("work_description", work.getDescription());

            String url = pythonAgentUrl + "/api/review";
            log.info("发送审核请求到 Python Agent: url={}, submissionId={}, workType={}", url, submissionId, workType);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(url, entity, (Class<Map<String, Object>>)(Class<?>)Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Python Agent 已接受审核任务，submissionId={}, response={}", submissionId, response.getBody());
            } else {
                log.warn("Python Agent 返回非成功状态码，submissionId={}, status={}", submissionId, response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("派发 AI 审核任务失败，submissionId={}, error={}", submissionId, e.getMessage(), e);

            Submission submission = submissionMapper.selectById(submissionId);
            if (submission != null) {
                submission.setStatus(Submission.SubmissionStatus.INVALID);
                submission.setValidationResult("AI审核服务暂时不可用：" + e.getMessage());
                submissionMapper.updateById(submission);
            }

            sendNotification(userId, Notification.NotificationType.SYSTEM,
                    "AI审核异常通知",
                    "AI审核服务暂时不可用，请联系管理员处理。");
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
