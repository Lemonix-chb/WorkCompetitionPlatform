# Work Competition Platform — 后端

高校计算机作品竞赛管理与智能评价系统 · Spring Boot 后端服务

## 技术栈

| 组件 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2.2 | Java Web 应用框架 |
| JDK | 17+ | 编译运行环境 |
| MyBatis Plus | 3.5.5 | ORM 持久层（BaseMapper + LambdaQueryWrapper） |
| Spring Security | 6.x | 认证授权（JWT + RBAC） |
| MySQL | 8.0 | 关系型数据库（24张表） |
| Redis | 7.x | 缓存（Lettuce 连接池） |
| Maven | 3.8+ | 项目构建管理 |

## 快速开始

### 环境要求

- JDK 17+
- MySQL 8.0+（数据库：`work_competition_platform`，用户：`root`，密码：`123456`）
- Redis（默认端口 6379，无密码）
- Maven 3.8+

### 初始化数据库

```bash
mysql -u root -p123456 < database/init_database.sql
```

### 启动服务

```bash
# 编译
mvn clean package -DskipTests

# 运行
java -jar target/WorkCompetitionPlatform-0.0.1-SNAPSHOT.jar

# 或开发模式
mvn spring-boot:run
```

服务启动后：
- **API 服务**：`http://localhost:8080`
- **Swagger 文档**：`http://localhost:8080/swagger-ui.html`
- **健康检查**：`http://localhost:8080/actuator/health`

## 项目结构

```
src/main/java/com/example/workcompetitionplatform/
├── WorkCompetitionPlatformApplication.java
├── annotation/RateLimit.java              (接口限流注解)
├── aspect/RateLimitAspect.java            (限流切面)
├── config/
│   ├── SecurityConfig.java               (Spring Security 配置)
│   ├── JwtUtils.java                     (JWT 令牌生成/验证)
│   ├── CorsConfig.java                   (跨域)
│   ├── RedisConfig.java                  (Redis 序列化)
│   ├── AsyncConfig.java                  (异步线程池)
│   ├── JacksonConfig.java                (日期格式)
│   ├── MyMetaObjectHandler.java          (时间戳自动填充)
│   ├── MybatisPlusConfig.java            (分页/逻辑删除)
│   ├── WebMvcConfig.java                 (静态资源)
│   └── GlobalExceptionHandler.java       (全局异常)
├── security/
│   ├── JwtAuthenticationFilter.java      (JWT 过滤器)
│   ├── CustomUserDetails.java
│   └── CustomUserDetailsService.java
├── controller/                           (16个控制器)
│   ├── AuthController.java               (登录/注册)
│   ├── UserController.java               (用户管理)
│   ├── AdminController.java              (管理员统计/分配)
│   ├── StudentController.java            (学生仪表盘)
│   ├── JudgeController.java              (评委操作)
│   ├── CompetitionController.java        (赛事CRUD)
│   ├── TeamController.java               (团队管理)
│   ├── WorkController.java               (作品管理)
│   ├── SubmissionController.java         (提交管理)
│   ├── RegistrationController.java       (报名管理)
│   ├── ReviewController.java             (评审管理)
│   ├── AIReviewController.java           (AI审核+回调)
│   ├── AppealController.java             (申诉)
│   ├── NotificationController.java       (通知)
│   ├── FileUploadController.java         (文件上传/下载)
│   └── SystemController.java             (系统配置)
├── service/                              (19个接口 + 17个实现)
│   └── impl/
│       ├── ReviewServiceImpl.java        (核心编排，13个依赖)
│       ├── AsyncAIReviewService.java     (异步AI调度)
│       ├── SubmissionServiceImpl.java
│       └── ...
├── mapper/                               (24个MyBatis Plus接口)
├── entity/                               (24个实体类)
├── dto/                                  (数据传输对象)
├── exception/BusinessException.java
└── util/                                 (工具类)
    ├── UserContext.java                  (ThreadLocal用户上下文)
    ├── FileValidator.java
    ├── TeamCodeGenerator.java
    └── WorkCodeGenerator.java

src/main/resources/
├── application.properties                (主配置)
├── application-prod.properties           (生产配置)
└── mapper/                               (16个XML Mapper文件)
```

## 架构设计

### 四层架构

```
Controller (@RestController, @PreAuthorize)
    ↓
Service (@Service, @Transactional)
    ↓
Mapper (extends BaseMapper<T>, XML/注解SQL)
    ↓
Entity (@TableName, 内部枚举类)
```

### 认证流程

```
用户登录 → 后端验证密码(BCrypt) → 生成JWT(HS512, 24h)
→ 前端存储 → 后续请求携带 Authorization: Bearer <token>
→ JwtAuthenticationFilter 拦截验证 → 设置 SecurityContext
→ @PreAuthorize 方法级权限检查
```

### 角色权限（RBAC）

| 角色 | 权限 |
|------|------|
| STUDENT | 创建团队、提交作品、查看AI结果、提交申诉 |
| JUDGE | 查看分配作品、评审打分、查看评审历史 |
| ADMIN | 管理赛事/用户/评委、触发AI审核、分配评委、计算成绩 |

## AI 审核流程

```
管理员触发 → POST /api/ai-reviews/perform/{submissionId}
  → submission.status = VALIDATING
  → @Async 异步调用 Python Agent (localhost:8000/review)
  → Python Agent 运行评审（规则引擎 + LLM推理）
  → HTTP 回调 POST /api/ai-reviews/callback
  → @Transactional 保存报告 + 更新状态 + 发送通知
```

**评分公式**：`最终得分 = AI评分 × 0.3 + 评委均分 × 0.7`

## 测试账户

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 学生 | student001 | 123456 |
| 评委 | judge001 | 123456 |
| 管理员 | admin | 123456 |

## 配置说明

`application.properties` 中的关键配置：

```properties
# 数据库
spring.datasource.url=jdbc:mysql://localhost:3306/work_competition_platform?...
spring.datasource.username=root
spring.datasource.password=123456

# JWT
jwt.secret=WorkCompetitionPlatformSecretKey2024HunanAgriculturalUniversity
jwt.expiration=86400000

# Python AI Agent
python.agent.url=http://localhost:8000

# 文件上传
spring.servlet.multipart.max-file-size=300MB
```

---

**开发者**：陈海波

**指导老师**：贺细平
