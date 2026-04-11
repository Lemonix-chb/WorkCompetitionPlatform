-- ============================================
-- 信息管理与智能评价系统数据库初始化脚本
-- 湖南农业大学计算机作品赛平台
-- 创建时间: 2026-01-19
-- ============================================

-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS work_competition_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE work_competition_platform;

-- ============================================
-- 第一部分：用户与权限模块
-- ============================================

-- 1.1 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名（学号/工号）',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    college VARCHAR(100) COMMENT '学院',
    major VARCHAR(100) COMMENT '专业',
    student_no VARCHAR(50) UNIQUE COMMENT '学号',
    status ENUM('ACTIVE', 'DISABLED', 'PENDING') DEFAULT 'ACTIVE' COMMENT '账号状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户基础信息表';

-- 1.2 角色表
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(50) UNIQUE NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) UNIQUE NOT NULL COMMENT '角色编码',
    description VARCHAR(200) COMMENT '角色描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 1.3 用户角色关联表
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_user_id (user_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 1.4 权限表
DROP TABLE IF EXISTS `permission`;
CREATE TABLE `permission` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(100) UNIQUE NOT NULL COMMENT '权限编码',
    resource_type ENUM('MENU', 'BUTTON', 'API') COMMENT '资源类型',
    resource_url VARCHAR(200) COMMENT '资源路径',
    description VARCHAR(200) COMMENT '权限描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 1.5 角色权限关联表
DROP TABLE IF EXISTS `role_permission`;
CREATE TABLE `role_permission` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_role_perm (role_id, permission_id),
    KEY idx_role_id (role_id),
    KEY idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- ============================================
-- 第二部分：赛事管理模块
-- ============================================

-- 2.1 赛事表
DROP TABLE IF EXISTS `competition`;
CREATE TABLE `competition` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '赛事ID',
    competition_name VARCHAR(200) NOT NULL COMMENT '赛事名称',
    competition_year INT NOT NULL COMMENT '赛事年份',
    description TEXT COMMENT '赛事简介',
    registration_start DATETIME NOT NULL COMMENT '报名开始时间',
    registration_end DATETIME NOT NULL COMMENT '报名截止时间',
    submission_start DATETIME COMMENT '作品提交开始时间',
    submission_end DATETIME COMMENT '作品提交截止时间',
    review_start DATETIME COMMENT '评审开始时间',
    review_end DATETIME COMMENT '评审结束时间',
    status ENUM('DRAFT', 'PUBLISHED', 'ONGOING', 'FINISHED') DEFAULT 'DRAFT' COMMENT '赛事状态',
    organizer VARCHAR(200) COMMENT '主办单位',
    contact_person VARCHAR(50) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    contact_email VARCHAR(100) COMMENT '联系邮箱',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='赛事基本信息表';

-- 2.2 赛道表
DROP TABLE IF EXISTS `competition_track`;
CREATE TABLE `competition_track` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '赛道ID',
    competition_id BIGINT NOT NULL COMMENT '所属赛事ID',
    track_name VARCHAR(100) NOT NULL COMMENT '赛道名称',
    track_code VARCHAR(50) NOT NULL COMMENT '赛道编码',
    track_type ENUM('CODE', 'PPT', 'VIDEO') NOT NULL COMMENT '作品类型',
    max_team_size INT DEFAULT 3 COMMENT '团队最大人数',
    min_team_size INT DEFAULT 1 COMMENT '团队最小人数',
    description TEXT COMMENT '赛道说明',
    submission_format VARCHAR(500) COMMENT '提交格式要求',
    max_file_size_mb INT COMMENT '最大文件大小(MB)',
    allowed_file_types VARCHAR(500) COMMENT '允许的文件类型',
    review_criteria TEXT COMMENT '评审标准',
    status ENUM('ACTIVE', 'DISABLED') DEFAULT 'ACTIVE' COMMENT '赛道状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_competition_id (competition_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='赛道表';

-- ============================================
-- 第三部分：团队与报名模块
-- ============================================

-- 3.1 作品表
DROP TABLE IF EXISTS `work`;
CREATE TABLE `work` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '作品ID',
    team_id BIGINT NOT NULL COMMENT '所属团队ID',
    competition_id BIGINT NOT NULL COMMENT '所属赛事ID',
    track_id BIGINT NOT NULL COMMENT '所属赛道ID',
    work_code VARCHAR(50) UNIQUE NOT NULL COMMENT '作品编号',
    work_name VARCHAR(200) NOT NULL COMMENT '作品名称',
    work_type ENUM('CODE', 'PPT', 'VIDEO') NOT NULL COMMENT '作品类型',
    description TEXT COMMENT '作品简介',
    innovation_points TEXT COMMENT '创新点说明',
    key_features TEXT COMMENT '关键功能特性',
    tech_stack VARCHAR(500) COMMENT '技术栈（程序设计类）',
    division_of_labor TEXT COMMENT '团队分工说明',
    target_users TEXT COMMENT '目标用户/应用场景',
    development_status ENUM('IN_PROGRESS', 'COMPLETED', 'SUBMITTED', 'AWARDED') COMMENT '开发状态',
    current_version INT DEFAULT 1 COMMENT '当前版本号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_team_id (team_id),
    KEY idx_competition_id (competition_id),
    KEY idx_track_id (track_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作品基本信息表';

-- 3.2 团队表
DROP TABLE IF EXISTS `team`;
CREATE TABLE `team` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '团队ID',
    team_code VARCHAR(50) UNIQUE NOT NULL COMMENT '团队编号（自动生成）',
    team_name VARCHAR(100) NOT NULL COMMENT '团队名称',
    competition_track_id BIGINT NOT NULL COMMENT '所属赛道ID',
    leader_id BIGINT NOT NULL COMMENT '队长用户ID',
    current_member_count INT DEFAULT 1 COMMENT '当前成员数量',
    max_member_count INT DEFAULT 3 COMMENT '最大成员数量',
    status ENUM('FORMING', 'CONFIRMED', 'REGISTERED', 'SUBMITTED', 'REVIEWED', 'AWARDED') DEFAULT 'FORMING' COMMENT '团队状态',
    work_description TEXT COMMENT '作品简介',
    division_of_labor TEXT COMMENT '分工说明',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_competition_track_id (competition_track_id),
    KEY idx_leader_id (leader_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队表';

-- 3.3 团队成员表
DROP TABLE IF EXISTS `team_member`;
CREATE TABLE `team_member` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '成员ID',
    team_id BIGINT NOT NULL COMMENT '团队ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    member_role ENUM('LEADER', 'MEMBER') NOT NULL COMMENT '成员角色',
    student_no VARCHAR(50) COMMENT '学号',
    real_name VARCHAR(50) COMMENT '姓名',
    major VARCHAR(100) COMMENT '专业',
    college VARCHAR(100) COMMENT '学院',
    join_time DATETIME COMMENT '加入时间',
    status ENUM('ACTIVE', 'QUIT') DEFAULT 'ACTIVE' COMMENT '成员状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_team_user (team_id, user_id),
    KEY idx_team_id (team_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队成员表';

-- 3.4 团队邀请表（队长发起）
DROP TABLE IF EXISTS `team_invitation`;
CREATE TABLE `team_invitation` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '邀请ID',
    team_id BIGINT NOT NULL COMMENT '邀请方团队ID',
    inviter_id BIGINT NOT NULL COMMENT '邀请人（队长）ID',
    invitee_id BIGINT NOT NULL COMMENT '被邀请人ID',
    invitee_student_no VARCHAR(50) COMMENT '被邀请人学号',
    invitee_email VARCHAR(100) COMMENT '被邀请人邮箱',
    message VARCHAR(500) COMMENT '邀请消息',
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'EXPIRED', 'CANCELLED') DEFAULT 'PENDING' COMMENT '邀请状态',
    expire_time DATETIME COMMENT '过期时间（默认7天）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    process_time DATETIME COMMENT '处理时间',
    KEY idx_team_id (team_id),
    KEY idx_inviter_id (inviter_id),
    KEY idx_invitee_id (invitee_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队邀请表（队长发起）';

-- 3.5 团队申请表（队员主动申请）
DROP TABLE IF EXISTS `team_application`;
CREATE TABLE `team_application` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '申请ID',
    team_id BIGINT NOT NULL COMMENT '申请加入的团队ID',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    applicant_student_no VARCHAR(50) COMMENT '申请人学号',
    applicant_email VARCHAR(100) COMMENT '申请人邮箱',
    message VARCHAR(500) COMMENT '申请消息',
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'CANCELLED') DEFAULT 'PENDING' COMMENT '申请状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    process_time DATETIME COMMENT '处理时间',
    processor_id BIGINT COMMENT '处理人（队长）ID',
    KEY idx_team_id (team_id),
    KEY idx_applicant_id (applicant_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队申请表（队员主动申请）';

-- 3.6 报名表
DROP TABLE IF EXISTS `registration`;
CREATE TABLE `registration` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报名ID',
    team_id BIGINT NOT NULL COMMENT '报名团队ID',
    competition_id BIGINT NOT NULL COMMENT '所属赛事ID',
    track_id BIGINT NOT NULL COMMENT '所属赛道ID',
    registration_code VARCHAR(50) UNIQUE COMMENT '报名编号',
    submitter_id BIGINT NOT NULL COMMENT '提交报名的人（队长）ID',
    registration_time DATETIME COMMENT '报名时间',
    status ENUM('DRAFT', 'PENDING', 'APPROVED', 'REJECTED') DEFAULT 'DRAFT' COMMENT '报名状态',
    review_comment VARCHAR(500) COMMENT '审核意见',
    reviewer_id BIGINT COMMENT '审核人（管理员）ID',
    review_time DATETIME COMMENT '审核时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_team_id (team_id),
    KEY idx_competition_id (competition_id),
    KEY idx_track_id (track_id),
    KEY idx_submitter_id (submitter_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报名表';

-- 3.7 团队成员报名关联表（队长报名后队员自动报名）
DROP TABLE IF EXISTS `team_registration`;
CREATE TABLE `team_registration` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    registration_id BIGINT NOT NULL COMMENT '团队报名记录ID',
    team_id BIGINT NOT NULL COMMENT '团队ID',
    user_id BIGINT NOT NULL COMMENT '队员用户ID',
    member_role ENUM('LEADER', 'MEMBER') NOT NULL COMMENT '队员角色',
    student_no VARCHAR(50) COMMENT '学号',
    real_name VARCHAR(50) COMMENT '姓名',
    major VARCHAR(100) COMMENT '专业',
    college VARCHAR(100) COMMENT '学院',
    registration_status ENUM('AUTO_REGISTERED', 'CONFIRMED', 'CANCELLED') DEFAULT 'AUTO_REGISTERED' COMMENT '报名状态',
    register_time DATETIME COMMENT '报名时间（队长提交时自动填充）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_registration_id (registration_id),
    KEY idx_team_id (team_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队成员报名关联表';

-- ============================================
-- 第四部分：作品提交模块
-- ============================================

-- 4.1 作品提交表
DROP TABLE IF EXISTS `submission`;
CREATE TABLE `submission` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '提交ID',
    work_id BIGINT NOT NULL COMMENT '关联作品ID',
    team_id BIGINT NOT NULL COMMENT '提交团队ID',
    submission_code VARCHAR(50) UNIQUE NOT NULL COMMENT '提交编号',
    submitter_id BIGINT NOT NULL COMMENT '实际提交人（队长）ID',
    file_name VARCHAR(200) NOT NULL COMMENT '原始文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '存储路径',
    file_size_mb DECIMAL(10,2) COMMENT '文件大小(MB)',
    file_type ENUM('CODE', 'PPT', 'VIDEO') NOT NULL COMMENT '作品类型',
    version INT NOT NULL COMMENT '提交版本号',
    submission_time DATETIME NOT NULL COMMENT '提交时间',
    status ENUM('UPLOADED', 'VALIDATING', 'VALID', 'INVALID', 'SUBMITTED', 'REVIEWED') COMMENT '提交状态',
    validation_result TEXT COMMENT '文件校验结果',
    is_final_version BOOLEAN DEFAULT FALSE COMMENT '是否为最终提交版本',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_work_id (work_id),
    KEY idx_team_id (team_id),
    KEY idx_submitter_id (submitter_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作品提交记录表（支持多版本）';

-- 4.2 文件校验记录表
DROP TABLE IF EXISTS `file_validation_log`;
CREATE TABLE `file_validation_log` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '校验记录ID',
    submission_id BIGINT NOT NULL COMMENT '提交记录ID',
    validation_type ENUM('FORMAT', 'SIZE', 'CONTENT', 'DUPLICATE', 'STRUCTURE') COMMENT '校验类型',
    validation_result ENUM('PASS', 'FAIL', 'WARNING') COMMENT '校验结果',
    error_message TEXT COMMENT '错误信息',
    error_location VARCHAR(200) COMMENT '错误位置（文件名/行号）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_submission_id (submission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件校验记录表';

-- ============================================
-- 第五部分：AI评审模块
-- ============================================

-- 5.1 AI初审报告表
DROP TABLE IF EXISTS `ai_review_report`;
CREATE TABLE `ai_review_report` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报告ID',
    submission_id BIGINT NOT NULL COMMENT '作品提交ID',
    team_id BIGINT NOT NULL COMMENT '团队ID',
    overall_score DECIMAL(5,2) COMMENT '综合得分',
    innovation_score DECIMAL(5,2) COMMENT '创新性得分',
    practicality_score DECIMAL(5,2) COMMENT '实用性得分',
    user_experience_score DECIMAL(5,2) COMMENT '用户体验得分',
    code_quality_score DECIMAL(5,2) COMMENT '代码质量得分',
    documentation_score DECIMAL(5,2) COMMENT '文档质量得分',
    duplicate_rate DECIMAL(5,2) COMMENT '重复率(%)',
    complexity_level ENUM('LOW', 'MEDIUM', 'HIGH') COMMENT '结构复杂度',
    risk_level ENUM('LOW', 'MEDIUM', 'HIGH') COMMENT '风险等级',
    review_summary TEXT COMMENT '评审摘要',
    improvement_suggestions TEXT COMMENT '改进建议',
    review_time DATETIME COMMENT '评审时间',
    ai_model VARCHAR(50) COMMENT '使用的AI模型',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_submission_id (submission_id),
    KEY idx_team_id (team_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI初审报告表';

-- 5.2 AI评审明细表
DROP TABLE IF EXISTS `ai_review_detail`;
CREATE TABLE `ai_review_detail` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '明细ID',
    report_id BIGINT NOT NULL COMMENT '报告ID',
    check_item VARCHAR(100) NOT NULL COMMENT '检查项',
    check_result VARCHAR(50) COMMENT '检查结果',
    score DECIMAL(5,2) COMMENT '得分',
    weight DECIMAL(3,2) COMMENT '权重',
    comment TEXT COMMENT '评语',
    file_path VARCHAR(200) COMMENT '相关文件路径',
    line_number INT COMMENT '相关代码行号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_report_id (report_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI评审明细表';

-- ============================================
-- 第六部分：人工评审模块
-- ============================================

-- 6.1 评委评分表
DROP TABLE IF EXISTS `judge_review`;
CREATE TABLE `judge_review` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评审ID',
    submission_id BIGINT NOT NULL COMMENT '作品提交ID',
    team_id BIGINT NOT NULL COMMENT '团队ID',
    judge_id BIGINT NOT NULL COMMENT '评委ID',
    innovation_score DECIMAL(5,2) COMMENT '创新性得分',
    practicality_score DECIMAL(5,2) COMMENT '实用性得分',
    user_experience_score DECIMAL(5,2) COMMENT '用户体验得分',
    overall_score DECIMAL(5,2) COMMENT '综合得分',
    review_comment TEXT COMMENT '评审意见',
    review_time DATETIME COMMENT '评审时间',
    status ENUM('DRAFT', 'SUBMITTED', 'CONFIRMED') DEFAULT 'DRAFT' COMMENT '评审状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_submission_judge (submission_id, judge_id),
    KEY idx_submission_id (submission_id),
    KEY idx_team_id (team_id),
    KEY idx_judge_id (judge_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评委评分表';

-- 6.2 评审结果汇总表
DROP TABLE IF EXISTS `review_result`;
CREATE TABLE `review_result` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '结果ID',
    team_id BIGINT NOT NULL COMMENT '团队ID',
    submission_id BIGINT NOT NULL COMMENT '作品提交ID',
    ai_score DECIMAL(5,2) COMMENT 'AI评审得分',
    ai_weight DECIMAL(3,2) DEFAULT 0.3 COMMENT 'AI评分权重',
    judge_avg_score DECIMAL(5,2) COMMENT '评委平均得分',
    judge_weight DECIMAL(3,2) DEFAULT 0.7 COMMENT '评委评分权重',
    final_score DECIMAL(5,2) COMMENT '最终得分',
    award_level ENUM('FIRST', 'SECOND', 'THIRD', 'NONE') COMMENT '奖项等级',
    rank_in_track INT COMMENT '赛道排名',
    final_comment TEXT COMMENT '最终评语',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_team_id (team_id),
    KEY idx_submission_id (submission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评审结果汇总表';

-- ============================================
-- 第七部分：申诉与反馈模块
-- ============================================

-- 7.1 申诉表
DROP TABLE IF EXISTS `appeal`;
CREATE TABLE `appeal` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '申诉ID',
    team_id BIGINT NOT NULL COMMENT '申诉团队ID',
    submission_id BIGINT NOT NULL COMMENT '作品提交ID',
    appellant_id BIGINT NOT NULL COMMENT '申诉人（队长）ID',
    appeal_type ENUM('SCORE', 'VALIDATION', 'AI_REVIEW', 'OTHER') COMMENT '申诉类型',
    appeal_content TEXT NOT NULL COMMENT '申诉内容',
    appeal_materials VARCHAR(500) COMMENT '申诉材料路径',
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'PROCESSING') DEFAULT 'PENDING' COMMENT '申诉状态',
    submit_time DATETIME COMMENT '提交时间',
    process_time DATETIME COMMENT '处理时间',
    processor_id BIGINT COMMENT '处理人（管理员）ID',
    process_result TEXT COMMENT '处理结果',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_team_id (team_id),
    KEY idx_submission_id (submission_id),
    KEY idx_appellant_id (appellant_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='申诉表';

-- 7.2 通知消息表
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '通知ID',
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    title VARCHAR(100) NOT NULL COMMENT '通知标题',
    content TEXT COMMENT '通知内容',
    notification_type ENUM('INVITE', 'APPLICATION', 'REGISTRATION', 'SUBMISSION', 'REVIEW', 'APPEAL', 'SYSTEM') COMMENT '通知类型',
    related_id BIGINT COMMENT '关联业务ID',
    related_type VARCHAR(50) COMMENT '关联业务类型',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    read_time DATETIME COMMENT '阅读时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_user_id (user_id),
    KEY idx_notification_type (notification_type),
    KEY idx_is_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知消息表';

-- ============================================
-- 第八部分：系统配置模块
-- ============================================

-- 8.1 系统配置表
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    config_key VARCHAR(100) UNIQUE NOT NULL COMMENT '配置键',
    config_value VARCHAR(500) COMMENT '配置值',
    config_type ENUM('STRING', 'INT', 'BOOLEAN', 'JSON') COMMENT '配置类型',
    description VARCHAR(200) COMMENT '配置说明',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- ============================================
-- ============================================
-- 第二部分：初始化基础数据
-- ============================================

-- 1. 初始化角色数据
INSERT INTO `role` (`role_name`, `role_code`, `description`) VALUES
('管理员', 'ADMIN', '赛事组织者，可管理赛事、审核报名、分配评委'),
('评委', 'JUDGE', '评审专家，可查看作品、打分、填写评语'),
('参赛者', 'PARTICIPANT', '参赛学生，可组建团队、提交作品、查看结果');

-- 2. 初始化管理员用户（密码: admin123，使用BCrypt加密）
INSERT INTO `user` (`username`, `password`, `real_name`, `email`, `phone`, `college`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 'admin@hunau.edu.cn', '13755162334', '教务处', 'ACTIVE');

-- 分配管理员角色
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES (1, 1);

-- 3. 初始化评委用户（密码: judge123）
INSERT INTO `user` (`username`, `password`, `real_name`, `email`, `phone`, `college`, `status`) VALUES
('judge001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '贺细平老师', '390199309@qq.com', '13755162334', '信息与智能科学技术学院', 'ACTIVE'),
('judge002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '张老师', 'lemonhunau@qq.com', '13800138000', '信息与智能科学技术学院', 'ACTIVE'),
('judge003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '何老师', '120246530@qq.com', '13900139000', '信息与智能科学技术学院', 'ACTIVE');

-- 分配评委角色
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES (2, 2), (3, 2), (4, 2);

-- 4. 初始化参赛学生用户（密码: student123）
INSERT INTO `user` (`username`, `password`, `real_name`, `email`, `phone`, `college`, `major`, `student_no`, `status`) VALUES
('student001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '陈海波', 'chenhaibo@hunau.edu.cn', '18812345678', '信息与智能科学技术学院', '信息管理', '2021001', 'ACTIVE'),
('student002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '李明', 'liming@hunau.edu.cn', '18823456789', '农学院', '农学', '2021002', 'ACTIVE'),
('student003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '王芳', 'wangfang@hunau.edu.cn', '18834567890', '园艺学院', '园艺', '2021003', 'ACTIVE'),
('student004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '张伟', 'zhangwei@hunau.edu.cn', '18845678901', '动物科学技术学院', '动物科学', '2021004', 'ACTIVE'),
('student005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '刘洋', 'liuyang@hunau.edu.cn', '18856789012', '食品科学技术学院', '食品科学', '2021005', 'ACTIVE'),
('student006', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '赵静', 'zhaojing@hunau.edu.cn', '18867890123', '植物保护学院', '植物保护', '2021006', 'ACTIVE'),
('student007', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '周强', 'zhouqiang@hunau.edu.cn', '18878901234', '资源环境学院', '环境工程', '2021007', 'ACTIVE'),
('student008', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '吴敏', 'wumin@hunau.edu.cn', '18889012345', '经济学院', '经济学', '2021008', 'ACTIVE'),
('student009', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '孙丽', 'sunli@hunau.edu.cn', '18890123456', '管理学院', '工商管理', '2021009', 'ACTIVE'),
('student010', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '郑华', 'zhenghua@hunau.edu.cn', '18901234567', '人文社会科学学院', '社会工作', '2021010', 'ACTIVE');

-- 分配参赛者角色
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES
(5, 3), (6, 3), (7, 3), (8, 3), (9, 3), (10, 3), (11, 3), (12, 3), (13, 3), (14, 3);

-- 5. 初始化2024年赛事信息
INSERT INTO `competition` (
    `competition_name`, `competition_year`, `description`,
    `registration_start`, `registration_end`,
    `submission_start`, `submission_end`,
    `review_start`, `review_end`,
    `status`, `organizer`, `contact_person`, `contact_phone`, `contact_email`
) VALUES (
    '2024年湖南农业大学计算机作品赛', 2024,
    '为提升我校非计算机专业本科生信息技术应用能力，展现信息技术在多专业领域的创新应用，凸显学生跨学科融合能力，决定举办2024年湖南农业大学计算机作品赛。',
    '2024-11-25 00:00:00', '2024-12-08 23:59:59',
    '2024-11-25 00:00:00', '2024-12-08 23:59:59',
    '2024-12-09 00:00:00', '2024-12-16 23:59:59',
    'PUBLISHED', '教务处', '贺细平', '13755162334', '390199309@qq.com'
);

-- 6. 初始化三个赛道信息
INSERT INTO `competition_track` (
    `competition_id`, `track_name`, `track_code`, `track_type`,
    `max_team_size`, `min_team_size`,
    `description`, `submission_format`, `max_file_size_mb`, `allowed_file_types`, `review_criteria`
) VALUES
(1, '程序设计作品', 'CODE_TRACK', 'CODE', 3, 1,
 '可选语言：Python、C/C++等。作品要求：源代码可运行，具有创新性、实用性和良好的用户体验。参赛作品必须为原创，严禁抄袭。',
 '源代码及作品说明文档以压缩包形式提交（.rar或.zip）',
 100, '.py,.c,.cpp,.java,.zip,.rar,.pdf,.doc,.docx',
 '根据作品的创新性（30%）、实用性（30%）和用户体验（40%）等因素综合评定'),
(1, '演示文稿作品', 'PPT_TRACK', 'PPT', 3, 1,
 '制作工具：WPS演示文稿。至少12页，页面比例为16:9。作品保存为PPTX，必须为原创且不得添加密码。合理使用文字、图片、背景、动画、链接、幻灯片切换等。',
 '作品文件及相关特殊元素以压缩包形式提交（.rar或.zip）',
 300, '.pptx,.ppt,.zip,.rar,.pdf,.jpg,.png,.mp3,.mp4',
 '根据作品的创意（25%）、视觉效果（25%）、内容呈现（25%）和原创性（25%）等因素综合评定'),
(1, '数媒动漫与短视频作品', 'VIDEO_TRACK', 'VIDEO', 3, 1,
 '拍摄内容健康阳光、积极向上、青春洋溢。内容丰富、转场合理，故事性强，须有字幕、片头及片尾。作品必须原创，严禁抄袭。作品时长60秒至180秒，画面比例为16:9。',
 '作品文件及必要信息文档以压缩包形式提交（.rar或.zip）',
 300, '.mp4,.avi,.mov,.zip,.rar,.pdf,.doc,.docx',
 '根据作品的故事性（30%）、视觉效果（25%）、导演技巧（25%）和原创性（20%）等因素综合评定');

-- 7. 初始化系统配置
INSERT INTO `system_config` (`config_key`, `config_value`, `config_type`, `description`) VALUES
('team.invitation.expire_days', '7', 'INT', '团队邀请过期天数'),
('ai.review.enabled', 'true', 'BOOLEAN', '是否启用AI初审'),
('ai.review.weight', '0.3', 'STRING', 'AI评审权重'),
('judge.review.weight', '0.7', 'STRING', '评委评审权重'),
('file.upload.max_size_mb', '300', 'INT', '文件上传最大大小(MB)'),
('duplicate.rate.threshold', '30', 'INT', '代码重复率阈值(%)');

-- ============================================
-- 数据初始化完成
-- ============================================

-- 显示初始化结果
SELECT '数据库初始化完成！' AS message;
SELECT COUNT(*) AS user_count FROM `user`;
SELECT COUNT(*) AS role_count FROM `role`;
SELECT COUNT(*) AS competition_count FROM `competition`;
SELECT COUNT(*) AS track_count FROM `competition_track`;