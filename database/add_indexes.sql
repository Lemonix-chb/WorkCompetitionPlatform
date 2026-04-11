-- 数据库性能优化 - 添加索引
-- 执行此脚本以提升查询性能

-- =============================================
-- 作品相关索引
-- =============================================

-- 作品表索引
CREATE INDEX idx_work_team_id ON work(team_id);
CREATE INDEX idx_work_competition_id ON work(competition_id);
CREATE INDEX idx_work_track_id ON work(track_id);
CREATE INDEX idx_work_status ON work(development_status);
CREATE INDEX idx_work_create_time ON work(create_time);

-- 作品附件表索引
CREATE INDEX idx_attachment_work_id ON work_attachment(work_id);
CREATE INDEX idx_attachment_type ON work_attachment(attachment_type);
CREATE INDEX idx_attachment_version ON work_attachment(version);
CREATE INDEX idx_attachment_upload_time ON work_attachment(upload_time);

-- =============================================
-- 团队相关索引
-- =============================================

-- 团队表索引
CREATE INDEX idx_team_leader_id ON team(leader_id);
CREATE INDEX idx_team_track_id ON team(competition_track_id);
CREATE INDEX idx_team_status ON team(status);
CREATE INDEX idx_team_code ON team(team_code);

-- 团队成员表索引
-- 组合索引：快速查询用户在某个团队的状态（覆盖单列索引）
CREATE INDEX idx_team_member_team_user ON team_member(team_id, user_id, status);

-- 团队邀请表索引
CREATE INDEX idx_invitation_team_id ON team_invitation(team_id);
CREATE INDEX idx_invitation_invitee_id ON team_invitation(invitee_id);
CREATE INDEX idx_invitation_status ON team_invitation(status);
CREATE INDEX idx_invitation_expire_time ON team_invitation(expire_time);

-- 团队申请表索引
CREATE INDEX idx_application_team_id ON team_application(team_id);
CREATE INDEX idx_application_applicant_id ON team_application(applicant_id);
CREATE INDEX idx_application_status ON team_application(status);

-- =============================================
-- 报名相关索引
-- =============================================

-- 报名表索引
CREATE INDEX idx_registration_competition_id ON registration(competition_id);
CREATE INDEX idx_registration_track_id ON registration(track_id);
CREATE INDEX idx_registration_time ON registration(registration_time);
CREATE INDEX idx_registration_code ON registration(registration_code);
-- 组合索引：快速查询用户的报名记录（覆盖单列索引）
CREATE INDEX idx_registration_team_status ON registration(team_id, status);

-- =============================================
-- 用户相关索引
-- =============================================

-- 用户表索引
CREATE INDEX idx_user_username ON user(username);
CREATE INDEX idx_user_student_no ON user(student_no);
CREATE INDEX idx_user_teacher_no ON user(teacher_no);
CREATE INDEX idx_user_status ON user(status);

-- 用户角色表索引
CREATE INDEX idx_user_role_user_id ON user_role(user_id);
CREATE INDEX idx_user_role_role ON user_role(role);

-- =============================================
-- 赛事相关索引
-- =============================================

-- 赛事表索引
CREATE INDEX idx_competition_year ON competition(competition_year);
CREATE INDEX idx_competition_status ON competition(status);
CREATE INDEX idx_competition_start_time ON competition(start_time);
CREATE INDEX idx_competition_end_time ON competition(end_time);

-- 赛道表索引
CREATE INDEX idx_track_competition_id ON competition_track(competition_id);
CREATE INDEX idx_track_type ON competition_track(track_type);
CREATE INDEX idx_track_status ON competition_track(status);

-- =============================================
-- 作品提交和评审相关索引
-- =============================================

-- 作品提交表索引
CREATE INDEX idx_submission_work_id ON work_submission(work_id);
CREATE INDEX idx_submission_status ON work_submission(submission_status);
CREATE INDEX idx_submission_time ON work_submission(submission_time);

-- 评审记录表索引
CREATE INDEX idx_review_work_id ON judge_review(work_id);
-- 组合索引：快速查询评委的待评审作品（覆盖单列索引）
CREATE INDEX idx_review_judge_status ON judge_review(judge_id, review_status);

-- AI评审报告表索引
CREATE INDEX idx_ai_review_work_id ON ai_review_report(work_id);
CREATE INDEX idx_ai_review_status ON ai_review_report(review_status);

-- =============================================
-- 申诉相关索引
-- =============================================

-- 申诉表索引
CREATE INDEX idx_appeal_work_id ON appeal(work_id);
CREATE INDEX idx_appeal_applicant_id ON appeal(applicant_id);
CREATE INDEX idx_appeal_status ON appeal(status);
CREATE INDEX idx_appeal_create_time ON appeal(create_time);

-- =============================================
-- 通知相关索引
-- =============================================

-- 通知表索引
CREATE INDEX idx_notification_type ON notification(notification_type);
CREATE INDEX idx_notification_create_time ON notification(create_time);
-- 组合索引：快速查询用户的未读通知（覆盖单列索引）
CREATE INDEX idx_notification_user_unread ON notification(user_id, is_read, create_time);

-- =============================================
-- 查看索引创建结果
-- =============================================

-- 显示所有索引
SELECT
    TABLE_NAME,
    INDEX_NAME,
    GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS INDEX_COLUMNS
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'work_competition_platform'
GROUP BY TABLE_NAME, INDEX_NAME
ORDER BY TABLE_NAME, INDEX_NAME;