# 🚀 AI审核模块启动指南

## 目录结构
```
WorkCompetitionPlatform/
├── ai-agent-service/          # Python AI Agent微服务
│   ├── app/                   # FastAPI应用代码
│   ├── requirements.txt       # Python依赖
│   └── config.yaml            # DeepSeek API配置
├── src/main/java/.../         # Spring Boot后端
└── database/                  # MySQL数据库
```

---

## 一、环境准备

### 1. 必需服务
确保以下服务已启动：

| 服务 | 端口 | 状态检查命令 |
|------|------|-------------|
| MySQL 8.0+ | 3306 | `mysql -u root -p123456 -e "SELECT 1"` |
| Redis | 6379 | `redis-cli ping` |
| Java 17 | - | `java -version` |
| Python 3.11+ | - | `python --version` |

### 2. Python依赖安装
```bash
cd ai-agent-service
pip install -r requirements.txt
```

**关键依赖**：
- FastAPI (Web框架)
- Uvicorn (ASGI服务器)
- httpx (HTTP客户端)
- Pydantic (数据验证)
- python-dotenv (配置管理)

---

## 二、启动步骤

### Step 1: 启动Spring Boot后端（端口8080）

**方式A：使用Maven**
```bash
cd e:/JavaProject/WorkCompetitionPlatform
mvn spring-boot:run
```

**方式B：运行打包后的JAR**
```bash
cd e:/JavaProject/WorkCompetitionPlatform
java -jar target/WorkCompetitionPlatform-0.0.1-SNAPSHOT.jar
```

**验证启动成功**：
- 访问：http://localhost:8080/swagger-ui.html
- 健康检查：http://localhost:8080/actuator/health
- 预期响应：`{"status":"UP"}`

---

### Step 2: 启动Python AI Agent（端口8001）

**方式A：前台启动（推荐，可看到日志）**
```bash
cd e:/JavaProject/WorkCompetitionPlatform/ai-agent-service
python -m uvicorn app.main:app --host 127.0.0.1 --port 8001 --reload --log-level info
```

**方式B：后台启动**
```bash
cd e:/JavaProject/WorkCompetitionPlatform/ai-agent-service
python -m uvicorn app.main:app --host 127.0.0.1 --port 8001 --log-level info > agent.log 2>&1 &
```

**验证启动成功**：
- 健康检查：http://localhost:8001/health
- 预期响应：`{"status":"healthy","agent":"ready","deepseek_api":"configured"}`
- API文档：http://localhost:8001/docs

---

## 三、测试AI审核功能

### 1. 手动测试（推荐）

**提交审核任务到Python Agent**：
```bash
curl -X POST http://localhost:8001/api/review \
  -H "Content-Type: application/json" \
  -d '{"submission_id":1,"work_type":"CODE","file_path":"/uploads/test.zip"}'
```

**预期响应**：
```json
{
  "submission_id": 1,
  "status": "processing",
  "message": "AI审核已启动，完成后将通过回调接口返回结果"
}
```

**等待10-15秒，然后查看数据库结果**：
```bash
# Windows（UTF-8编码）
mysql -u root -p123456 --default-character-set=utf8mb4 -D work_competition_platform \
  -e "SELECT id, overall_score, review_summary FROM ai_review_report ORDER BY id DESC LIMIT 1"

# Linux/Mac
mysql -u root -p123456 -D work_competition_platform \
  -e "SELECT id, overall_score, review_summary FROM ai_review_report ORDER BY id DESC LIMIT 1"
```

---

### 2. 查看审核结果

**快捷脚本（Windows）**：
```bash
# 项目根目录
view_results_utf8.bat
```

**手动查询**：
```sql
-- 查看AI审核报告
SELECT id, submission_id, overall_score, review_summary, create_time
FROM ai_review_report
ORDER BY create_time DESC LIMIT 5;

-- 查看通知
SELECT id, user_id, title, content, create_time
FROM notification
WHERE user_id IN (5, 6)
ORDER BY create_time DESC LIMIT 5;
```

---

### 3. 测试Spring Boot回调接口

**直接向Spring Boot发送Mock审核结果**：
```bash
curl -X POST http://localhost:8080/api/ai-reviews/callback \
  -H "Content-Type: application/json" \
  -d '{"submissionId":1,"report":{"overallScore":90.0,"innovationScore":22.5,"practicalityScore":22.5,"userExperienceScore":22.5,"documentationScore":22.5,"duplicateRate":3.0,"codeQualityScore":19.0,"reviewSummary":"作品优秀","improvementSuggestions":["继续改进"],"aiModel":"deepseek-chat","riskLevel":"LOW"}}'
```

**预期响应**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

---

## 四、完整审核流程说明

### 自动流程（生产环境）

```
学生提交作品 → Spring Boot创建Submission(status=VALIDATING)
              ↓
         调用Python Agent: POST http://localhost:8001/api/review
              ↓
         Python Agent启动后台审核任务(asyncio.ensure_future)
              ↓
         LangChain Agent执行审核:
         - 代码查重(JPlag)
         - 代码风格检查(Checkstyle)
         - DeepSeek大模型评估
              ↓
         编译审核报告(overallScore, innovationScore等)
              ↓
         回调Spring Boot: POST http://localhost:8080/api/ai-reviews/callback
              ↓
         Spring Boot处理回调:
         - 保存AIReviewReport到数据库
         - 更新Submission状态(VALID/INVALID)
         - 发送Notification通知学生
              ↓
         学生查看审核结果（前端轮询或刷新页面）
```

---

## 五、端口和地址总览

| 服务 | 地址 | 说明 |
|------|------|------|
| Spring Boot主服务 | http://localhost:8080 | 作品管理平台后端 |
| Python AI Agent | http://localhost:8001 | AI审核微服务 |
| MySQL数据库 | localhost:3306 | 数据存储 |
| Redis缓存 | localhost:6379 | 会话缓存 |

**API文档地址**：
- Spring Boot Swagger: http://localhost:8080/swagger-ui.html
- Python Agent Swagger: http://localhost:8001/docs

---

## 六、常见问题排查

### 问题1：Python Agent启动失败

**症状**：端口8001无法访问

**排查步骤**：
```bash
# 1. 检查端口占用
netstat -ano | grep ":8001"

# 2. 停止旧进程
taskkill /F /PID <进程ID>

# 3. 检查Python依赖
pip list | grep -E "fastapi|uvicorn|httpx"

# 4. 重新安装依赖
pip install -r requirements.txt
```

---

### 问题2：回调未执行，数据库无新记录

**症状**：Python Agent返回"processing"但数据库无新记录

**原因**：BackgroundTasks不执行

**解决方案**：已修复，使用asyncio.ensure_future

**验证日志**：
启动时观察Python Agent日志，应该看到：
```
🚀 Received review request for submission X
✅ Async task started for submission X
🚀 Starting background review task for submission X
📝 Executing review for submission X
✅ Agent review completed for submission X
📊 Report compiled: overallScore=85.0
🌐 Sending callback to http://localhost:8080/api/ai-reviews/callback
✅ Callback successful for submission X
```

---

### 问题3：数据库乱码

**症状**：中文显示为乱码

**解决方案**：
```bash
# 使用UTF-8字符集连接
mysql --default-character-set=utf8mb4 -u root -p123456 -D work_competition_platform

# 或使用快捷脚本
view_results_utf8.bat
```

---

### 问题4：Spring Boot回调接口403 Forbidden

**症状**：回调返回403错误

**原因**：Spring Security拦截

**解决方案**：已在SecurityConfig.java中添加：
```java
.requestMatchers("/api/ai-reviews/callback").permitAll()
```

重启Spring Boot即可生效。

---

## 七、配置文件说明

### Python Agent配置（ai-agent-service/config.yaml）
```yaml
deepseek:
  api_key: "sk-325ae1ccf357480ab353a41e8b26ee32"
  api_url: "https://api.deepseek.com/v1/chat/completions"
  model: "deepseek-chat"

spring_boot:
  api_url: "http://localhost:8080/api"
```

### Spring Boot配置（src/main/resources/application.properties）
```properties
# 数据库连接（已配置utf8mb4）
spring.datasource.url=jdbc:mysql://localhost:3306/work_competition_platform?characterEncoding=utf8mb4

# JWT配置
jwt.secret=WorkCompetitionPlatformSecretKey2024
jwt.expiration=86400000

# 文件上传
spring.servlet.multipart.max-file-size=300MB
```

---

## 八、生产环境部署建议

### Docker方式（推荐）
```bash
# 启动所有服务（MySQL + Redis + Spring Boot + Python Agent）
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs ai-agent
docker-compose logs work-competition-platform
```

### 手动部署
1. MySQL和Redis作为独立服务
2. Spring Boot打包为JAR运行
3. Python Agent使用Uvicorn或Gunicorn

---

## 九、监控和日志

### Python Agent日志
```bash
# 前台启动时直接显示在控制台
# 后台启动时查看日志文件
tail -f ai-agent-service/agent.log
```

### Spring Boot日志
```bash
# 启动时控制台输出
# 或查看日志文件（如果配置）
tail -f logs/spring-boot.log
```

### 数据库监控
```sql
-- 查看最新审核记录
SELECT * FROM ai_review_report ORDER BY create_time DESC LIMIT 10;

-- 查看审核统计
SELECT
  COUNT(*) as total_reviews,
  AVG(overall_score) as avg_score,
  MAX(create_time) as latest_review
FROM ai_review_report;
```

---

## 十、停止服务

### 停止Python Agent
```bash
# 找到进程ID
ps aux | grep uvicorn

# 停止进程
kill <PID>
# 或Windows
taskkill /F /PID <PID>
```

### 停止Spring Boot
```bash
# 找到进程ID
jps -l | grep WorkCompetitionPlatform

# 停止进程
kill <PID>
# 或Windows
taskkill /F /PID <PID>
```

### 停止Docker服务
```bash
docker-compose down
```

---

## 十一、快速启动脚本（推荐）

### Windows一键启动
```bash
# 创建启动脚本 start_all.bat
@echo off
echo 启动Spring Boot...
start cmd /k "cd e:\JavaProject\WorkCompetitionPlatform && mvn spring-boot:run"

timeout /t 10

echo 启动Python Agent...
start cmd /k "cd e:\JavaProject\WorkCompetitionPlatform\ai-agent-service && python -m uvicorn app.main:app --host 127.0.0.1 --port 8001 --reload"

echo 所有服务已启动！
pause
```

### Linux/Mac一键启动
```bash
# 创建启动脚本 start_all.sh
#!/bin/bash
echo "启动Spring Boot..."
cd /path/to/WorkCompetitionPlatform
mvn spring-boot:run &

sleep 10

echo "启动Python Agent..."
cd ai-agent-service
python -m uvicorn app.main:app --host 127.0.0.1 --port 8001 &

echo "所有服务已启动！"
```

---

## 总结

**启动顺序**：
1. ✅ MySQL + Redis（基础服务）
2. ✅ Spring Boot（端口8080）
3. ✅ Python AI Agent（端口8001）

**测试流程**：
1. ✅ 健康检查两个服务
2. ✅ 提交审核任务到Python Agent
3. ✅ 等待10-15秒
4. ✅ 查看数据库验证结果

**预期结果**：
- AIReviewReport表新增记录
- Notification表新增通知
- 中文数据正确保存（utf8mb4）

---

**祝您使用顺利！如有问题请参考故障排查章节。** 🚀