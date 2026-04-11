# 信息管理与智能评价系统 - API接口设计文档

## 📋 文档说明

**版本**: v1.0
**基础URL**: `http://localhost:8080/api`
**认证方式**: JWT Token（请求头：`Authorization: Bearer <token>`）
**响应格式**: JSON
**编码**: UTF-8

---

## 🔐 认证授权接口

### 1. 用户登录
```http
POST /api/auth/login
```

**请求体**:
```json
{
  "username": "student001",
  "password": "student123"
}
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": 5,
      "username": "student001",
      "realName": "陈海波",
      "email": "chenhaibo@hunau.edu.cn",
      "roles": ["PARTICIPANT"]
    }
  }
}
```

### 2. 用户注册
```http
POST /api/auth/register
```

**请求体**:
```json
{
  "username": "student011",
  "password": "student123",
  "realName": "张三",
  "email": "zhangsan@hunau.edu.cn",
  "phone": "18811112222",
  "college": "农学院",
  "major": "农学",
  "studentNo": "2021011"
}
```

**成功响应** (201):
```json
{
  "code": 201,
  "message": "注册成功",
  "data": {
    "userId": 15,
    "username": "student011"
  }
}
```

### 3. 用户登出
```http
POST /api/auth/logout
Authorization: Bearer <token>
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "登出成功"
}
```

### 4. 获取当前用户信息
```http
GET /api/auth/info
Authorization: Bearer <token>
```

**成功响应** (200):
```json
{
  "code": 200,
  "data": {
    "id": 5,
    "username": "student001",
    "realName": "陈海波",
    "email": "chenhaibo@hunau.edu.cn",
    "phone": "18812345678",
    "college": "信息与智能科学技术学院",
    "major": "信息管理",
    "studentNo": "2021001",
    "roles": ["PARTICIPANT"],
    "status": "ACTIVE"
  }
}
```

---

## 👤 用户管理接口

### 1. 查询用户列表（管理员）
```http
GET /api/users?page=1&size=10&role=PARTICIPANT&status=ACTIVE
Authorization: Bearer <token>
```

**查询参数**:
- `page`: 页码（默认1）
- `size`: 每页数量（默认10）
- `role`: 角色过滤（可选）
- `status`: 状态过滤（可选）
- `keyword`: 关键词搜索（可选）

**成功响应** (200):
```json
{
  "code": 200,
  "data": {
    "content": [
      {
        "id": 5,
        "username": "student001",
        "realName": "陈海波",
        "email": "chenhaibo@hunau.edu.cn",
        "college": "信息与智能科学技术学院",
        "status": "ACTIVE",
        "createTime": "2026-01-19 10:30:00"
      }
    ],
    "totalElements": 10,
    "totalPages": 1,
    "size": 10,
    "number": 1
  }
}
```

### 2. 查询用户详情
```http
GET /api/users/{id}
Authorization: Bearer <token>
```

**成功响应** (200):
```json
{
  "code": 200,
  "data": {
    "id": 5,
    "username": "student001",
    "realName": "陈海波",
    "email": "chenhaibo@hunau.edu.cn",
    "phone": "18812345678",
    "college": "信息与智能科学技术学院",
    "major": "信息管理",
    "studentNo": "2021001",
    "status": "ACTIVE",
    "createTime": "2026-01-19 10:30:00",
    "updateTime": "2026-01-19 15:20:00"
  }
}
```

### 3. 更新用户信息
```http
PUT /api/users/{id}
Authorization: Bearer <token>
```

**请求体**:
```json
{
  "realName": "陈海波",
  "email": "chenhaibo_new@hunau.edu.cn",
  "phone": "18899998888",
  "college": "信息与智能科学技术学院",
  "major": "信息管理与信息系统"
}
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 5,
    "username": "student001"
  }
}
```

### 4. 修改密码
```http
PUT /api/users/{id}/password
Authorization: Bearer <token>
```

**请求体**:
```json
{
  "oldPassword": "student123",
  "newPassword": "newPassword123"
}
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "密码修改成功"
}
```

### 5. 更新用户状态（管理员）
```http
PUT /api/users/{id}/status
Authorization: Bearer <token>
```

**请求体**:
```json
{
  "status": "DISABLED"
}
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "状态更新成功"
}
```

---

## 🏆 赛事管理接口

### 1. 查询赛事列表
```http
GET /api/competitions?year=2024&status=PUBLISHED
Authorization: Bearer <token>
```

**成功响应** (200):
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "competitionName": "2024年湖南农业大学计算机作品赛",
      "competitionYear": 2024,
      "status": "PUBLISHED",
      "registrationStart": "2024-11-25 00:00:00",
      "registrationEnd": "2024-12-08 23:59:59",
      "submissionStart": "2024-11-25 00:00:00",
      "submissionEnd": "2024-12-08 23:59:59"
    }
  ]
}
```

### 2. 查询赛事详情
```http
GET /api/competitions/{id}
Authorization: Bearer <token>
```

**成功响应** (200):
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "competitionName": "2024年湖南农业大学计算机作品赛",
    "competitionYear": 2024,
    "description": "为提升我校非计算机专业本科生信息技术应用能力...",
    "registrationStart": "2024-11-25 00:00:00",
    "registrationEnd": "2024-12-08 23:59:59",
    "submissionStart": "2024-11-25 00:00:00",
    "submissionEnd": "2024-12-08 23:59:59",
    "reviewStart": "2024-12-09 00:00:00",
    "reviewEnd": "2024-12-16 23:59:59",
    "status": "PUBLISHED",
    "organizer": "教务处",
    "contactPerson": "贺细平",
    "contactPhone": "13755162334",
    "contactEmail": "390199309@qq.com"
  }
}
```

### 3. 查询赛事赛道
```http
GET /api/competitions/{id}/tracks
Authorization: Bearer <token>
```

**成功响应** (200):
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "trackName": "程序设计作品",
      "trackCode": "CODE_TRACK",
      "trackType": "CODE",
      "maxTeamSize": 3,
      "minTeamSize": 1,
      "description": "可选语言：Python、C/C++等...",
      "maxFileSizeMb": 100,
      "status": "ACTIVE"
    },
    {
      "id": 2,
      "trackName": "演示文稿作品",
      "trackCode": "PPT_TRACK",
      "trackType": "PPT",
      "maxTeamSize": 3,
      "minTeamSize": 1,
      "description": "制作工具：WPS演示文稿...",
      "maxFileSizeMb": 300,
      "status": "ACTIVE"
    },
    {
      "id": 3,
      "trackName": "数媒动漫与短视频作品",
      "trackCode": "VIDEO_TRACK",
      "trackType": "VIDEO",
      "maxTeamSize": 3,
      "minTeamSize": 1,
      "description": "拍摄内容健康阳光...",
      "maxFileSizeMb": 300,
      "status": "ACTIVE"
    }
  ]
}
```

### 4. 创建赛事（管理员）
```http
POST /api/competitions
Authorization: Bearer <token>
```

**请求体**:
```json
{
  "competitionName": "2025年湖南农业大学计算机作品赛",
  "competitionYear": 2025,
  "description": "赛事简介...",
  "registrationStart": "2025-03-01 00:00:00",
  "registrationEnd": "2025-03-31 23:59:59",
  "submissionStart": "2025-03-01 00:00:00",
  "submissionEnd": "2025-04-15 23:59:59",
  "organizer": "教务处",
  "contactPerson": "贺细平",
  "contactPhone": "13755162334",
  "contactEmail": "390199309@qq.com"
}
```

**成功响应** (201):
```json
{
  "code": 201,
  "message": "赛事创建成功",
  "data": {
    "competitionId": 2
  }
}
```

---

## 👥 团队管理接口

### 1. 创建团队
```http
POST /api/teams
Authorization: Bearer <token>
```

**请求体**:
```json
{
  "teamName": "创新开发团队",
  "competitionTrackId": 1,
  "workDescription": "基于AI的智能推荐系统"
}
```

**成功响应** (201):
```json
{
  "code": 201,
  "message": "团队创建成功",
  "data": {
    "teamId": 1,
    "teamCode": "TEAM-2024-001"
  }
}
```

### 2. 查询团队详情
```http
GET /api/teams/{id}
Authorization: Bearer <token>
```

**成功响应** (200):
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "teamCode": "TEAM-2024-001",
    "teamName": "创新开发团队",
    "competitionTrackId": 1,
    "trackName": "程序设计作品",
    "leaderId": 5,
    "leaderName": "陈海波",
    "currentMemberCount": 2,
    "maxMemberCount": 3,
    "status": "FORMING",
    "workDescription": "基于AI的智能推荐系统",
    "members": [
      {
        "userId": 5,
        "username": "student001",
        "realName": "陈海波",
        "memberRole": "LEADER",
        "joinTime": "2026-01-19 10:30:00"
      },
      {
        "userId": 6,
        "username": "student002",
        "realName": "李明",
        "memberRole": "MEMBER",
        "joinTime": "2026-01-19 11:00:00"
      }
    ],
    "createTime": "2026-01-19 10:30:00"
  }
}
```

### 3. 查询我的团队
```http
GET /api/teams/my
Authorization: Bearer <token>
```

**成功响应** (200):
```json
{
  "code": 200,
  "data": {
    "teamId": 1,
    "teamCode": "TEAM-2024-001",
    "teamName": "创新开发团队",
    "memberRole": "LEADER",
    "status": "FORMING"
  }
}
```

### 4. 邀请成员
```http
POST /api/teams/{id}/invite
Authorization: Bearer <token>
```

**请求体**:
```json
{
  "inviteeStudentNo": "2021003",
  "message": "邀请你加入我们的团队，一起参加竞赛！"
}
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "邀请发送成功",
  "data": {
    "invitationId": 1
  }
}
```

### 5. 申请加入团队
```http
POST /api/teams/{id}/apply
Authorization: Bearer <token>
```

**请求体**:
```json
{
  "message": "希望能加入贵团队，我擅长前端开发。"
}
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "申请提交成功",
  "data": {
    "applicationId": 1
  }
}
```

### 6. 接受邀请
```http
POST /api/teams/invitations/{id}/accept
Authorization: Bearer <token>
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "已接受邀请，成功加入团队"
}
```

### 7. 拒绝邀请
```http
POST /api/teams/invitations/{id}/reject
Authorization: Bearer <token>
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "已拒绝邀请"
}
```

### 8. 接受申请（队长）
```http
POST /api/teams/applications/{id}/accept
Authorization: Bearer <token>
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "已接受申请，该成员已加入团队"
}
```

### 9. 拒绝申请（队长）
```http
POST /api/teams/applications/{id}/reject
Authorization: Bearer <token>
```

**请求体**:
```json
{
  "reason": "团队人数已满"
}
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "已拒绝申请"
}
```

### 10. 成员退出团队
```http
DELETE /api/teams/{teamId}/members/{memberId}
Authorization: Bearer <token>
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "已退出团队"
}
```

---

## 📝 作品提交接口

### 1. 提交作品
```http
POST /api/submissions
Authorization: Bearer <token>
Content-Type: multipart/form-data
```

**表单数据**:
- `teamId`: 团队ID
- `workName`: 作品名称
- `description`: 作品简介
- `file`: 作品文件（ZIP/RAR）
- `divisionOfLabor`: 分工说明

**成功响应** (201):
```json
{
  "code": 201,
  "message": "作品提交成功",
  "data": {
    "submissionId": 1,
    "submissionCode": "SUB-2024-001",
    "version": 1
  }
}
```

### 2. 查询提交详情
```http
GET /api/submissions/{id}
Authorization: Bearer <token>
```

**成功响应** (200):
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "submissionCode": "SUB-2024-001",
    "teamId": 1,
    "teamName": "创新开发团队",
    "workName": "基于AI的智能推荐系统",
    "description": "作品简介...",
    "fileName": "project.zip",
    "fileSizeMb": 25.5,
    "fileType": "CODE",
    "version": 1,
    "status": "SUBMITTED",
    "submissionTime": "2026-01-19 15:30:00",
    "isFinalVersion": false
  }
}
```

### 3. 查询我的提交
```http
GET /api/submissions/my
Authorization: Bearer <token>
```

**成功响应** (200):
```json
{
  "code": 200,
  "data": [
    {
      "submissionId": 1,
      "submissionCode": "SUB-2024-001",
      "teamName": "创新开发团队",
      "workName": "基于AI的智能推荐系统",
      "status": "SUBMITTED",
      "submissionTime": "2026-01-19 15:30:00"
    }
  ]
}
```

### 4. 查询团队提交记录
```http
GET /api/submissions/team/{teamId}
Authorization: Bearer <token>
```

**成功响应** (200):
```json
{
  "code": 200,
  "data": [
    {
      "submissionId": 1,
      "version": 1,
      "status": "SUBMITTED",
      "submissionTime": "2026-01-19 15:30:00",
      "isFinalVersion": false
    },
    {
      "submissionId": 2,
      "version": 2,
      "status": "SUBMITTED",
      "submissionTime": "2026-01-20 10:00:00",
      "isFinalVersion": true
    }
  ]
}
```

---

## ⭐ 评审管理接口

### 1. 查询作品评审结果
```http
GET /api/reviews/submission/{id}
Authorization: Bearer <token>
```

**成功响应** (200):
```json
{
  "code": 200,
  "data": {
    "submissionId": 1,
    "workName": "基于AI的智能推荐系统",
    "aiReview": {
      "overallScore": 85.5,
      "innovationScore": 90.0,
      "practicalityScore": 82.0,
      "userExperienceScore": 84.5,
      "duplicateRate": 15.2,
      "riskLevel": "LOW",
      "reviewSummary": "作品整体质量优秀...",
      "improvementSuggestions": "建议进一步优化..."
    },
    "judgeReviews": [
      {
        "judgeName": "贺细平老师",
        "innovationScore": 88,
        "practicalityScore": 85,
        "userExperienceScore": 87,
        "overallScore": 86.7,
        "reviewComment": "作品创新性强...",
        "reviewTime": "2024-12-10 14:30:00"
      }
    ],
    "finalResult": {
      "aiScore": 85.5,
      "aiWeight": 0.3,
      "judgeAvgScore": 86.7,
      "judgeWeight": 0.7,
      "finalScore": 86.34,
      "awardLevel": "FIRST",
      "rankInTrack": 5
    }
  }
}
```

### 2. 评委评分
```http
POST /api/reviews/judge
Authorization: Bearer <token>
```

**请求体**:
```json
{
  "submissionId": 1,
  "innovationScore": 88,
  "practicalityScore": 85,
  "userExperienceScore": 87,
  "overallScore": 86.7,
  "reviewComment": "作品创新性强，实用性好，用户体验优秀。建议在算法优化方面进一步提升。"
}
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "评分提交成功"
}
```

### 3. 查询我的评审任务（评委）
```http
GET /api/reviews/my
Authorization: Bearer <token>
```

**成功响应** (200):
```json
{
  "code": 200,
  "data": [
    {
      "submissionId": 1,
      "submissionCode": "SUB-2024-001",
      "workName": "基于AI的智能推荐系统",
      "teamName": "创新开发团队",
      "trackName": "程序设计作品",
      "status": "PENDING",
      "deadline": "2024-12-12 23:59:59"
    }
  ]
}
```

### 4. 查询AI评审报告
```http
GET /api/reports/ai/{submissionId}
Authorization: Bearer <token>
```

**成功响应** (200):
```json
{
  "code": 200,
  "data": {
    "reportId": 1,
    "submissionId": 1,
    "overallScore": 85.5,
    "innovationScore": 90.0,
    "practicalityScore": 82.0,
    "userExperienceScore": 84.5,
    "codeQualityScore": 88.0,
    "documentationScore": 80.0,
    "duplicateRate": 15.2,
    "complexityLevel": "MEDIUM",
    "riskLevel": "LOW",
    "reviewSummary": "作品整体质量优秀，创新性强...",
    "improvementSuggestions": "建议进一步优化用户界面...",
    "aiModel": "DeepSeek-V3",
    "reviewTime": "2024-12-09 10:00:00",
    "details": [
      {
        "checkItem": "代码结构",
        "checkResult": "良好",
        "score": 90.0,
        "weight": 0.2,
        "comment": "代码结构清晰，模块化设计合理"
      }
    ]
  }
}
```

---

## 📬 通知管理接口

### 1. 查询通知列表
```http
GET /api/notifications?page=1&size=20&type=INVITE&isRead=false
Authorization: Bearer <token>
```

**成功响应** (200):
```json
{
  "code": 200,
  "data": {
    "content": [
      {
        "id": 1,
        "title": "团队邀请",
        "content": "陈海波邀请您加入团队"创新开发团队"",
        "notificationType": "INVITE",
        "relatedId": 1,
        "relatedType": "TEAM_INVITATION",
        "isRead": false,
        "createTime": "2026-01-19 11:00:00"
      }
    ],
    "totalElements": 5,
    "totalPages": 1
  }
}
```

### 2. 查询未读通知数量
```http
GET /api/notifications/unread-count
Authorization: Bearer <token>
```

**成功响应** (200):
```json
{
  "code": 200,
  "data": {
    "unreadCount": 3
  }
}
```

### 3. 标记通知已读
```http
PUT /api/notifications/{id}/read
Authorization: Bearer <token>
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "已标记为已读"
}
```

### 4. 全部标记已读
```http
PUT /api/notifications/read-all
Authorization: Bearer <token>
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "已全部标记为已读"
}
```

---

## 📢 申诉管理接口

### 1. 提交申诉
```http
POST /api/appeals
Authorization: Bearer <token>
```

**请求体**:
```json
{
  "submissionId": 1,
  "appealType": "SCORE",
  "appealContent": "对评审结果存在异议，评分标准不够透明...",
  "appealMaterials": "/uploads/appeal/evidence.pdf"
}
```

**成功响应** (201):
```json
{
  "code": 201,
  "message": "申诉提交成功",
  "data": {
    "appealId": 1
  }
}
```

### 2. 查询申诉详情
```http
GET /api/appeals/{id}
Authorization: Bearer <token>
```

**成功响应** (200):
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "submissionId": 1,
    "workName": "基于AI的智能推荐系统",
    "appellantId": 5,
    "appellantName": "陈海波",
    "appealType": "SCORE",
    "appealContent": "对评审结果存在异议...",
    "status": "PENDING",
    "submitTime": "2024-12-11 10:00:00"
  }
}
```

### 3. 查询我的申诉
```http
GET /api/appeals/my
Authorization: Bearer <token>
```

**成功响应** (200):
```json
{
  "code": 200,
  "data": [
    {
      "appealId": 1,
      "workName": "基于AI的智能推荐系统",
      "appealType": "SCORE",
      "status": "PENDING",
      "submitTime": "2024-12-11 10:00:00"
    }
  ]
}
```

### 4. 处理申诉（管理员）
```http
POST /api/appeals/{id}/process
Authorization: Bearer <token>
```

**请求体**:
```json
{
  "status": "ACCEPTED",
  "processResult": "经核实，评审过程公正，评分合理。申诉不予支持。"
}
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "申诉处理完成"
}
```

---

## 📊 统一响应格式

### 成功响应
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    // 业务数据
  }
}
```

### 错误响应
```json
{
  "code": 400,
  "message": "请求参数错误",
  "errors": [
    {
      "field": "username",
      "message": "用户名不能为空"
    }
  ]
}
```

### 分页响应
```json
{
  "code": 200,
  "data": {
    "content": [],
    "totalElements": 100,
    "totalPages": 10,
    "size": 10,
    "number": 1
  }
}
```

---

## ⚠️ 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 201 | 创建成功 |
| 204 | 无内容（删除成功） |
| 400 | 请求参数错误 |
| 401 | 未授权（Token无效或过期） |
| 403 | 禁止访问（权限不足） |
| 404 | 资源不存在 |
| 409 | 资源冲突（如重复报名） |
| 500 | 服务器内部错误 |

---

## 🔒 权限说明

### 角色权限对照表

| 接口 | 学生 | 评委 | 管理员 |
|------|------|------|--------|
| `/api/auth/**` | ✅ | ✅ | ✅ |
| `/api/users/**` | 自己 | 自己 | 所有 |
| `/api/competitions/**` | 查看 | 查看 | 全部 |
| `/api/teams/**` | 自己团队 | 查看 | 全部 |
| `/api/submissions/**` | 自己提交 | 分配任务 | 全部 |
| `/api/reviews/**` | 查看结果 | 评分 | 全部 |
| `/api/appeals/**` | 自己申诉 | 查看 | 全部 |
| `/api/notifications/**` | 自己通知 | 自己通知 | 全部 |

---

## 📝 开发说明

### 请求头
```
Authorization: Bearer <token>
Content-Type: application/json
Accept: application/json
```

### 时间格式
- 日期时间：`yyyy-MM-dd HH:mm:ss`
- 示例：`2024-12-08 23:59:59`

### 文件上传
- 支持格式：`.zip`, `.rar`, `.pdf`, `.doc`, `.docx`
- 最大大小：300MB

---

**文档版本**: v1.0  
**更新时间**: 2026-01-19  
**维护者**: 陈海波