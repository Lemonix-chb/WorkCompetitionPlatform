# 信息管理与智能评价系统 - 数据库设计方案

## 一、用户与权限模块

### 1. 用户表 (user)
```sql
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名（学号/工号）',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    college VARCHAR(100) COMMENT '学院',
    major VARCHAR(100) COMMENT '专业',
    student_no VARCHAR(50) UNIQUE COMMENT '学号',
    status ENUM('ACTIVE', 'DISABLED', 'PENDING') DEFAULT 'ACTIVE' COMMENT '账号状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='用户基础信息表';
```

### 2. 角色表 (role)
```sql
CREATE TABLE role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) UNIQUE NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) UNIQUE NOT NULL COMMENT '角色编码',
    description VARCHAR(200) COMMENT '角色描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT='角色表';

-- 初始化角色数据
INSERT INTO role (role_name, role_code, description) VALUES
('管理员', 'ADMIN', '赛事组织者，可管理赛事、审核报名、分配评委'),
('评委', 'JUDGE', '评审专家，可查看作品、打分、填写评语'),
('参赛者', 'PARTICIPANT', '参赛学生，可组建团队、提交作品、查看结果');
```

### 3. 用户角色关联表 (user_role)
```sql
CREATE TABLE user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_role (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (role_id) REFERENCES role(id)
) COMMENT='用户角色关联表';
```

### 4. 权限表 (permission)
```sql
CREATE TABLE permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(100) UNIQUE NOT NULL COMMENT '权限编码',
    resource_type ENUM('MENU', 'BUTTON', 'API') COMMENT '资源类型',
    resource_url VARCHAR(200) COMMENT '资源路径',
    description VARCHAR(200) COMMENT '权限描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT='权限表';
```

### 5. 角色权限关联表 (role_permission)
```sql
CREATE TABLE role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_perm (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES role(id),
    FOREIGN KEY (permission_id) REFERENCES permission(id)
) COMMENT='角色权限关联表';
```

## 二、赛事管理模块

### 6. 赛事表 (competition)
```sql
CREATE TABLE competition (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
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
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='赛事基本信息表';
```

### 7. 赛道表 (competition_track)
```sql
CREATE TABLE competition_track (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    competition_id BIGINT NOT NULL COMMENT '所属赛事',
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
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (competition_id) REFERENCES competition(id)
) COMMENT='赛道表';

-- 初始化2024年赛事的三个赛道
INSERT INTO competition_track (competition_id, track_name, track_code, track_type, max_team_size, min_team_size) VALUES
(1, '程序设计作品', 'CODE_TRACK', 'CODE', 3, 1),
(1, '演示文稿作品', 'PPT_TRACK', 'PPT', 3, 1),
(1, '数媒动漫与短视频作品', 'VIDEO_TRACK', 'VIDEO', 3, 1);
```

## 三、团队与报名模块（核心）

### 8. 团队表 (team)
```sql
CREATE TABLE team (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    team_code VARCHAR(50) UNIQUE NOT NULL COMMENT '团队编号（自动生成）',
    team_name VARCHAR(100) NOT NULL COMMENT '团队名称',
    competition_track_id BIGINT NOT NULL COMMENT '所属赛道',
    leader_id BIGINT NOT NULL COMMENT '队长用户ID',
    current_member_count INT DEFAULT 1 COMMENT '当前成员数量',
    max_member_count INT DEFAULT 3 COMMENT '最大成员数量',
    status ENUM('FORMING', 'CONFIRMED', 'REGISTERED', 'SUBMITTED', 'REVIEWED', 'AWARDED') DEFAULT 'FORMING' COMMENT '团队状态',
    work_description TEXT COMMENT '作品简介',
    division_of_labor TEXT COMMENT '分工说明',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (competition_track_id) REFERENCES competition_track(id),
    FOREIGN KEY (leader_id) REFERENCES user(id)
) COMMENT='团队表';
```

### 9. 团队成员表 (team_member)
```sql
CREATE TABLE team_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    team_id BIGINT NOT NULL COMMENT '团队ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    member_role ENUM('LEADER', 'MEMBER') NOT NULL COMMENT '成员角色',
    student_no VARCHAR(50) COMMENT '学号',
    real_name VARCHAR(50) COMMENT '姓名',
    major VARCHAR(100) COMMENT '专业',
    college VARCHAR(100) COMMENT '学院',
    join_time DATETIME COMMENT '加入时间',
    status ENUM('ACTIVE', 'QUIT') DEFAULT 'ACTIVE' COMMENT '成员状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_team_user (team_id, user_id),
    UNIQUE KEY uk_user_track_active (user_id, team_id) COMMENT '同一用户在同一团队只能有一条记录',
    FOREIGN KEY (team_id) REFERENCES team(id),
    FOREIGN KEY (user_id) REFERENCES user(id)
) COMMENT='团队成员表';
```

### 10. 团队邀请表 (team_invitation) ⭐核心
```sql
CREATE TABLE team_invitation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    team_id BIGINT NOT NULL COMMENT '邀请方团队',
    inviter_id BIGINT NOT NULL COMMENT '邀请人（队长）',
    invitee_id BIGINT NOT NULL COMMENT '被邀请人',
    invitee_student_no VARCHAR(50) COMMENT '被邀请人学号',
    invitee_email VARCHAR(100) COMMENT '被邀请人邮箱',
    message VARCHAR(500) COMMENT '邀请消息',
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'EXPIRED', 'CANCELLED') DEFAULT 'PENDING' COMMENT '邀请状态',
    expire_time DATETIME COMMENT '过期时间（默认7天）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    process_time DATETIME COMMENT '处理时间',
    FOREIGN KEY (team_id) REFERENCES team(id),
    FOREIGN KEY (inviter_id) REFERENCES user(id),
    FOREIGN KEY (invitee_id) REFERENCES user(id)
) COMMENT='团队邀请表（队长发起）';
```

### 11. 团队申请表 (team_application) ⭐核心
```sql
CREATE TABLE team_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    team_id BIGINT NOT NULL COMMENT '申请加入的团队',
    applicant_id BIGINT NOT NULL COMMENT '申请人',
    applicant_student_no VARCHAR(50) COMMENT '申请人学号',
    applicant_email VARCHAR(100) COMMENT '申请人邮箱',
    message VARCHAR(500) COMMENT '申请消息',
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'CANCELLED') DEFAULT 'PENDING' COMMENT '申请状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    process_time DATETIME COMMENT '处理时间',
    processor_id BIGINT COMMENT '处理人（队长）',
    FOREIGN KEY (team_id) REFERENCES team(id),
    FOREIGN KEY (applicant_id) REFERENCES user(id),
    FOREIGN KEY (processor_id) REFERENCES user(id)
) COMMENT='团队申请表（队员主动申请）';
```

### 12. 报名表 (registration)
```sql
CREATE TABLE registration (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    team_id BIGINT NOT NULL COMMENT '报名团队',
    competition_id BIGINT NOT NULL COMMENT '所属赛事',
    track_id BIGINT NOT NULL COMMENT '所属赛道',
    registration_code VARCHAR(50) UNIQUE COMMENT '报名编号',
    submitter_id BIGINT NOT NULL COMMENT '提交报名的人（队长）',
    registration_time DATETIME COMMENT '报名时间',
    status ENUM('DRAFT', 'PENDING', 'APPROVED', 'REJECTED') DEFAULT 'DRAFT' COMMENT '报名状态',
    review_comment VARCHAR(500) COMMENT '审核意见',
    reviewer_id BIGINT COMMENT '审核人（管理员）',
    review_time DATETIME COMMENT '审核时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (team_id) REFERENCES team(id),
    FOREIGN KEY (competition_id) REFERENCES competition(id),
    FOREIGN KEY (track_id) REFERENCES competition_track(id),
    FOREIGN KEY (submitter_id) REFERENCES user(id),
    FOREIGN KEY (reviewer_id) REFERENCES user(id)
) COMMENT='报名表';
```

## 四、作品提交模块

### 13. 作品提交表 (submission)
```sql
CREATE TABLE submission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    team_id BIGINT NOT NULL COMMENT '提交团队',
    competition_id BIGINT NOT NULL COMMENT '所属赛事',
    track_id BIGINT NOT NULL COMMENT '所属赛道',
    submission_code VARCHAR(50) UNIQUE NOT NULL COMMENT '提交编号',
    submitter_id BIGINT NOT NULL COMMENT '实际提交人（队长）',
    file_name VARCHAR(200) NOT NULL COMMENT '原始文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '存储路径',
    file_size_mb DECIMAL(10,2) COMMENT '文件大小(MB)',
    file_type ENUM('CODE', 'PPT', 'VIDEO') NOT NULL COMMENT '作品类型',
    submission_time DATETIME NOT NULL COMMENT '提交时间',
    status ENUM('UPLOADED', 'VALIDATING', 'VALID', 'INVALID', 'SUBMITTED', 'REVIEWED') DEFAULT 'UPLOADED' COMMENT '提交状态',
    validation_result TEXT COMMENT '文件校验结果',
    version INT DEFAULT 1 COMMENT '版本号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (team_id) REFERENCES team(id),
    FOREIGN KEY (competition_id) REFERENCES competition(id),
    FOREIGN KEY (track_id) REFERENCES competition_track(id),
    FOREIGN KEY (submitter_id) REFERENCES user(id)
) COMMENT='作品提交表';
```

### 14. 文件校验记录表 (file_validation_log)
```sql
CREATE TABLE file_validation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    submission_id BIGINT NOT NULL COMMENT '提交记录ID',
    validation_type ENUM('FORMAT', 'SIZE', 'CONTENT', 'DUPLICATE', 'STRUCTURE') COMMENT '校验类型',
    validation_result ENUM('PASS', 'FAIL', 'WARNING') COMMENT '校验结果',
    error_message TEXT COMMENT '错误信息',
    error_location VARCHAR(200) COMMENT '错误位置（文件名/行号）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (submission_id) REFERENCES submission(id)
) COMMENT='文件校验记录表';
```

## 五、AI评审模块

### 15. AI初审报告表 (ai_review_report)
```sql
CREATE TABLE ai_review_report (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
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
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (submission_id) REFERENCES submission(id),
    FOREIGN KEY (team_id) REFERENCES team(id)
) COMMENT='AI初审报告表';
```

### 16. AI评审明细表 (ai_review_detail)
```sql
CREATE TABLE ai_review_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    report_id BIGINT NOT NULL COMMENT '报告ID',
    check_item VARCHAR(100) NOT NULL COMMENT '检查项',
    check_result VARCHAR(50) COMMENT '检查结果',
    score DECIMAL(5,2) COMMENT '得分',
    weight DECIMAL(3,2) COMMENT '权重',
    comment TEXT COMMENT '评语',
    file_path VARCHAR(200) COMMENT '相关文件路径',
    line_number INT COMMENT '相关代码行号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (report_id) REFERENCES ai_review_report(id)
) COMMENT='AI评审明细表';
```

## 六、人工评审模块

### 17. 评委评分表 (judge_review)
```sql
CREATE TABLE judge_review (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
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
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_submission_judge (submission_id, judge_id),
    FOREIGN KEY (submission_id) REFERENCES submission(id),
    FOREIGN KEY (team_id) REFERENCES team(id),
    FOREIGN KEY (judge_id) REFERENCES user(id)
) COMMENT='评委评分表';
```

### 18. 评审结果汇总表 (review_result)
```sql
CREATE TABLE review_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
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
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (team_id) REFERENCES team(id),
    FOREIGN KEY (submission_id) REFERENCES submission(id)
) COMMENT='评审结果汇总表';
```

## 七、申诉与反馈模块

### 19. 申诉表 (appeal)
```sql
CREATE TABLE appeal (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    team_id BIGINT NOT NULL COMMENT '申诉团队',
    submission_id BIGINT NOT NULL COMMENT '作品提交ID',
    appellant_id BIGINT NOT NULL COMMENT '申诉人（队长）',
    appeal_type ENUM('SCORE', 'VALIDATION', 'AI_REVIEW', 'OTHER') COMMENT '申诉类型',
    appeal_content TEXT NOT NULL COMMENT '申诉内容',
    appeal_materials VARCHAR(500) COMMENT '申诉材料路径',
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'PROCESSING') DEFAULT 'PENDING' COMMENT '申诉状态',
    submit_time DATETIME COMMENT '提交时间',
    process_time DATETIME COMMENT '处理时间',
    processor_id BIGINT COMMENT '处理人（管理员）',
    process_result TEXT COMMENT '处理结果',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (team_id) REFERENCES team(id),
    FOREIGN KEY (submission_id) REFERENCES submission(id),
    FOREIGN KEY (appellant_id) REFERENCES user(id),
    FOREIGN KEY (processor_id) REFERENCES user(id)
) COMMENT='申诉表';
```

### 20. 通知消息表 (notification)
```sql
CREATE TABLE notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '接收用户',
    title VARCHAR(100) NOT NULL COMMENT '通知标题',
    content TEXT COMMENT '通知内容',
    notification_type ENUM('INVITE', 'APPLICATION', 'REGISTRATION', 'SUBMISSION', 'REVIEW', 'APPEAL', 'SYSTEM') COMMENT '通知类型',
    related_id BIGINT COMMENT '关联业务ID',
    related_type VARCHAR(50) COMMENT '关联业务类型',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    read_time DATETIME COMMENT '阅读时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id)
) COMMENT='通知消息表';
```

## 八、系统配置模块

### 21. 系统配置表 (system_config)
```sql
CREATE TABLE system_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) UNIQUE NOT NULL COMMENT '配置键',
    config_value VARCHAR(500) COMMENT '配置值',
    config_type ENUM('STRING', 'INT', 'BOOLEAN', 'JSON') COMMENT '配置类型',
    description VARCHAR(200) COMMENT '配置说明',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='系统配置表';
```

## 核心业务逻辑索引设计

### 关键业务约束索引

```sql
-- 确保同一用户在同一赛道只能参与一个团队（活跃状态）
CREATE UNIQUE INDEX uk_user_track_active ON team_member(user_id)
WHERE EXISTS (
    SELECT 1 FROM team t
    WHERE t.id = team_member.team_id
    AND t.status IN ('FORMING', 'CONFIRMED', 'REGISTERED', 'SUBMITTED')
);

-- 确保同一赛道下团队编号唯一
CREATE UNIQUE INDEX uk_team_code_track ON team(team_code, competition_track_id);
```

## 数据库初始化脚本说明

1. 按模块顺序执行建表语句
2. 执行角色和权限初始化数据
3. 执行赛道初始化数据
4. 配置系统默认参数

## 团队组建业务规则总结

### 规则1：同一赛道唯一性
- 用户在同一赛道只能参与一个活跃团队
- 系统需要在加入团队前校验：`team_member` 表中该用户是否已有该赛道的活跃记录

### 规则2：双向邀请机制
- 队长邀请队员 → 邀请进入 `team_invitation` 表（状态：PENDING）
- 队员申请加入 → 申请进入 `team_application` 表（状态：PENDING）
- 双方均可拒绝：状态变为 REJECTED

### 规则3：团队人数限制
- 最小：1人（队长）
- 最大：3人（队长+2队员）
- 加入前校验：`current_member_count < max_member_count`

### 规则4：队长权限
- 创建/解散团队
- 邀请/移除队员
- 处理加入申请
- 提交作品/报名
- 发起申诉

### 规则5：队员权限
- 申请加入团队（通过搜索团队名）
- 接受/拒绝邀请
- 查看团队信息/作品状态/评审结果
- 退出团队（退出后可加入其他团队）

---

**设计完成时间**: 2026-01-19
**设计者**: 陈海波
**指导老师**: 贺细平