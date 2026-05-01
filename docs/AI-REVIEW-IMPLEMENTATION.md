# AI初审引擎实现方案

## 📌 需求背景

根据《信息管理与智能评价系统开题报告》和《湖南农业大学计算机作品赛通知》，AI初审引擎需要：

- **解决痛点**：人工初审标准不统一、反馈滞后、公平性质疑
- **目标**：即时反馈、量化评分、可解释评语、风险标签
- **支持**：程序设计、演示文稿、数媒视频三种作品类型

---

## 🏗️ 系统架构

### 总体流程

```
作品上传 → ZIP解压 → 文件校验 → 类型识别 → 规则引擎 → 大模型评估 → 报告生成 → 实时反馈
```

### 核心模块

#### 1. 文件处理模块
**职责**：解压、分类、提取

**功能**：
- ZIP自动解压到临时目录
- 文件类型白名单校验（按赛道）
- 提取关键文件：
  - **程序设计**：源代码（.py/.c/.cpp/.java）、README、说明文档
  - **演示文稿**：.pptx/.pdf、特殊元素（字体/图片/音视频）
  - **数媒视频**：.mp4/.avi、剧本、拍摄说明

**技术**：
- Apache Commons Compress（解压）
- 文件类型枚举（FileType.java）
- 白名单配置（SystemConfig表）

#### 2. 规则引擎模块
**职责**：基础规范性检查

**检查项**（存入ai_review_detail表）：
```
程序设计作品：
- 目录结构完整性：是否有README、src目录、文档
- 文件命名规范：是否符合命名要求
- 代码文件数量：是否在合理范围
- 注释覆盖率：统计代码注释比例
- 函数复杂度：函数平均行数

演示文稿作品：
- 页数检查：≥12页
- 页面比例：16:9
- 原创元素：原创图片/动画占比
- 内容合规性：无违规内容

数媒视频作品：
- 时长检查：60-180秒
- 画质检查：1080p
- 片头片尾：完整性
- 字幕检查：是否有字幕
```

**评分权重**：
- 基础规范：30%
- 代码质量：40%
- 大模型评估：30%

#### 3. 代码质量分析模块（程序设计专用）
**职责**：量化代码质量

**技术栈**：
- **JPlag**：代码查重（检测抄袭）
- **Checkstyle**：代码风格检查（命名规范、缩进、注释）
- **自定义脚本**：
  - 注释覆盖率计算
  - 函数复杂度分析
  - 目录结构评分

**指标**：
- `duplicate_rate`：重复率（%）
- `code_quality_score`：代码质量评分
- `complexity_level`：结构复杂度（LOW/MEDIUM/HIGH）

#### 4. 大模型评估模块
**职责**：深度语义评估 + 可解释评语

**技术**：DeepSeek API / OpenAI API

**评估维度**：
```json
{
  "innovation_score": "创新性（0-100）",
  "practicality_score": "实用性（0-100）",
  "user_experience_score": "用户体验（0-100）",
  "documentation_score": "文档质量（0-100）",
  "review_summary": "评审摘要（自然语言）",
  "improvement_suggestions": "改进建议（分点列举）"
}
```

**Prompt示例**（程序设计）：
```
你是一位资深的计算机作品评审专家。请分析以下参赛作品：

作品名称：{work_name}
赛道：{track_type}
文件列表：{file_list}
核心代码：{code_samples}

请从以下维度评分（0-100分）并给出具体建议：

1. **创新性**：是否有新颖的算法、架构或应用场景？
2. **实用性**：能否解决实际问题？是否有可运行性？
3. **用户体验**：界面设计、交互流程、错误处理
4. **文档质量**：README完整性、代码注释质量、说明文档清晰度

输出格式：
```json
{
  "scores": {...},
  "summary": "总结段落",
  "suggestions": ["建议1", "建议2", ...]
}
```

#### 5. 评分汇总模块
**职责**：加权计算、风险判定

**公式**：
```java
// 程序设计作品
overallScore = 基础规范(30%) + 代码质量(40%) + 大模型评估(30%)

// 演示文稿作品
overallScore = 基础规范(50%) + 大模型评估(50%)

// 数媒视频作品
overallScore = 基础规范(40%) + 大模型评估(60%)
```

**风险等级判定**：
```java
if (duplicate_rate > 30%) return HIGH;
if (duplicate_rate > 20% || overallScore < 60) return MEDIUM;
return LOW;
```

---

## 📊 数据库设计

### ai_review_report表（主报告）
| 字段 | 说明 | 用途 |
|------|------|------|
| overall_score | 总体评分 | 最终AI评分 |
| innovation_score | 创新性 | 大模型评估 |
| practicality_score | 实用性 | 大模型评估 |
| user_experience_score | 用户体验 | 大模型评估 |
| code_quality_score | 代码质量 | 规则引擎+JPlag |
| documentation_score | 文档质量 | 大模型评估 |
| duplicate_rate | 重复率 | JPlag查重 |
| complexity_level | 复杂度 | LOW/MEDIUM/HIGH |
| risk_level | 风险等级 | LOW/MEDIUM/HIGH |
| review_summary | 摘要 | 大模型生成 |
| improvement_suggestions | 建议 | 大模型生成 |
| ai_model | 模型名称 | DeepSeek/GPT-4 |

### ai_review_detail表（检查明细）
| 字段 | 说明 | 示例 |
|------|------|------|
| check_item | 检查项 | "目录结构完整性" |
| check_result | 结果 | "PASS"/"FAIL" |
| score | 评分 | 85.00 |
| weight | 权重 | 0.30 |
| comment | 说明 | "包含README和src目录" |
| file_path | 文件路径 | "/src/main.py" |
| line_number | 行号 | 42 |

---

## 🔧 实现步骤

### 第1步：文件处理服务
**类名**：`FileProcessingService`

**方法**：
```java
// 解压ZIP文件
File unzipSubmission(Long submissionId, String zipPath)

// 校验文件类型
boolean validateFileType(String filename, TrackType trackType)

// 提取关键文件
Map<String, List<File>> extractKeyFiles(File unzippedDir, TrackType trackType)
```

### 第2步：规则引擎服务
**类名**：`RuleEngineService`

**方法**：
```java
// 执行基础规范检查
List<AIReviewDetail> performBasicChecks(Long submissionId, TrackType trackType, Map<String, List<File>> files)

// 检查目录结构
AIReviewDetail checkDirectoryStructure(File dir)

// 检查文件命名
AIReviewDetail checkFileNaming(List<File> files)

// 计算注释覆盖率
AIReviewDetail calculateCommentCoverage(List<File> codeFiles)
```

### 第3步：代码质量分析服务
**类名**：`CodeQualityService`

**方法**：
```java
// JPlag查重
BigDecimal checkDuplicateRate(Long submissionId, List<File> codeFiles)

// Checkstyle风格检查
AIReviewDetail checkCodeStyle(File codeFile)

// 函数复杂度分析
ComplexityLevel analyzeFunctionComplexity(List<File> codeFiles)
```

**外部工具集成**：
- JPlag：Docker容器运行
- Checkstyle：命令行工具

### 第4步：大模型评估服务
**类名**：`LLMEvaluationService`

**方法**：
```java
// 调用DeepSeek API
LLMEvaluationResult evaluateByLLM(Work work, List<File> keyFiles, TrackType trackType)

// 生成可解释评语
String generateReviewSummary(LLMEvaluationResult result)

// 生成改进建议
String generateImprovementSuggestions(LLMEvaluationResult result)
```

**API配置**（SystemConfig表）：
```sql
INSERT INTO system_config (config_key, config_value) VALUES
('ai_model', 'deepseek'),
('deepseek_api_key', 'sk-...'),
('deepseek_api_url', 'https://api.deepseek.com/v1/chat/completions');
```

### 第5步：AI评审服务整合
**类名**：`AIReviewService`（实现IReviewService.performAIReview）

**流程**：
```java
@Transactional
public AIReviewReport performAIReview(Long submissionId) {
    // 1. 获取提交信息和作品类型
    Submission submission = submissionMapper.selectById(submissionId);
    Work work = workMapper.selectById(submission.getWorkId());
    TrackType trackType = work.getTrackType();
    
    // 2. 解压文件
    File unzippedDir = fileProcessingService.unzipSubmission(submissionId, submission.getFilePath());
    
    // 3. 提取关键文件
    Map<String, List<File>> keyFiles = fileProcessingService.extractKeyFiles(unzippedDir, trackType);
    
    // 4. 规则引擎检查（基础规范）
    List<AIReviewDetail> basicChecks = ruleEngineService.performBasicChecks(submissionId, trackType, keyFiles);
    
    // 5. 代码质量分析（仅程序设计）
    BigDecimal duplicateRate = BigDecimal.ZERO;
    BigDecimal codeQualityScore = BigDecimal.ZERO;
    ComplexityLevel complexityLevel = ComplexityLevel.LOW;
    
    if (trackType == TrackType.CODE) {
        duplicateRate = codeQualityService.checkDuplicateRate(submissionId, keyFiles.get("code"));
        AIReviewDetail styleCheck = codeQualityService.checkCodeStyle(keyFiles.get("code").get(0));
        basicChecks.add(styleCheck);
        complexityLevel = codeQualityService.analyzeFunctionComplexity(keyFiles.get("code"));
        codeQualityScore = calculateCodeQualityScore(basicChecks, duplicateRate);
    }
    
    // 6. 大模型评估
    LLMEvaluationResult llmResult = llmEvaluationService.evaluateByLLM(work, keyFiles, trackType);
    
    // 7. 计算总分
    BigDecimal overallScore = calculateOverallScore(basicChecks, codeQualityScore, llmResult, trackType);
    
    // 8. 判定风险等级
    RiskLevel riskLevel = determineRiskLevel(duplicateRate, overallScore);
    
    // 9. 保存AI审核报告
    AIReviewReport report = new AIReviewReport();
    report.setSubmissionId(submissionId);
    report.setTeamId(submission.getTeamId());
    report.setOverallScore(overallScore);
    report.setInnovationScore(llmResult.getInnovationScore());
    report.setPracticalityScore(llmResult.getPracticalityScore());
    report.setUserExperienceScore(llmResult.getUserExperienceScore());
    report.setCodeQualityScore(codeQualityScore);
    report.setDocumentationScore(llmResult.getDocumentationScore());
    report.setDuplicateRate(duplicateRate);
    report.setComplexityLevel(complexityLevel);
    report.setRiskLevel(riskLevel);
    report.setReviewSummary(llmResult.getSummary());
    report.setImprovementSuggestions(llmResult.getSuggestions());
    report.setAiModel("DeepSeek");
    report.setReviewTime(LocalDateTime.now());
    
    aiReviewReportMapper.insert(report);
    
    // 10. 保存检查明细
    for (AIReviewDetail detail : basicChecks) {
        detail.setReportId(report.getId());
        aiReviewDetailMapper.insert(detail);
    }
    
    // 11. 更新提交状态
    submission.setStatus(SubmissionStatus.VALID);
    submissionMapper.updateById(submission);
    
    return report;
}
```

---

## 🎯 评分细则

### 程序设计作品评分权重

| 检查项 | 权重 | 满分 | 说明 |
|--------|------|------|------|
| **基础规范（30%）** ||||
| 目录结构完整性 | 10% | 10分 | README、src目录、文档齐全 |
| 文件命名规范 | 5% | 5分 | 符合命名要求 |
| 注释覆盖率 | 10% | 10分 | ≥30%满分，<10%0分 |
| 函数复杂度 | 5% | 5分 | 平均≤20行满分 |
| **代码质量（40%）** ||||
| 代码风格 | 20% | 20分 | Checkstyle评分 |
| 重复率 | 20% | 20分 | <10%满分，>30%0分 |
| **大模型评估（30%）** ||||
| 创新性 | 10% | 10分 | 新颖程度 |
| 实用性 | 10% | 10分 | 解决实际问题 |
| 文档质量 | 10% | 10分 | README质量 |

### 演示文稿作品评分权重

| 检查项 | 权重 | 满分 | 说明 |
|--------|------|------|------|
| **基础规范（50%）** ||||
| 页数检查 | 20% | 20分 | ≥12页满分 |
| 页面比例 | 10% | 10分 | 16:9满分 |
| 原创元素 | 10% | 10分 | ≥50%原创满分 |
| 内容合规 | 10% | 10分 | 无违规内容 |
| **大模型评估（50%）** ||||
| 创新性 | 15% | 15分 | 设计新颖 |
| 视觉效果 | 20% | 20分 | 美观程度 |
| 内容呈现 | 15% | 15分 | 信息清晰 |

### 数媒视频作品评分权重

| 检查项 | 权重 | 满分 | 说明 |
|--------|------|------|------|
| **基础规范（40%）** ||||
| 时长检查 | 15% | 15分 | 60-180秒满分 |
| 画质检查 | 10% | 10分 | 1080p满分 |
| 片头片尾 | 10% | 10分 | 完整性 |
| 字幕检查 | 5% | 5分 | 有字幕 |
| **大模型评估（60%）** ||||
| 故事性 | 25% | 25分 | 故事连贯 |
| 视觉效果 | 20% | 20分 | 画面质量 |
| 原创性 | 15% | 15分 | 原创 |

---

## 🚀 部署方案

### Docker化AI工具链

```yaml
# docker-compose.yml
services:
  jplag:
    image: jplag/jplag:latest
    volumes:
      - ./uploads:/data
    command: ["-l", "python", "-s", "/data/submissions/"]
  
  checkstyle:
    image: checkstyle/checkstyle:latest
    volumes:
      - ./uploads:/data
```

### 系统配置管理

```sql
-- SystemConfig表存储配置
INSERT INTO system_config VALUES
('ai_duplicate_threshold', '30'), -- 重复率阈值
('ai_comment_threshold', '30'),   -- 注释覆盖率阈值
('ai_model_timeout', '60000'),    -- 大模型超时（毫秒）
('ai_max_file_size', '300');      -- 最大文件大小（MB）
```

---

## ✅ 验证指标

### 成功标准

- ✅ 重复率检测准确率 > 95%
- ✅ 评分一致性（同一作品多次评分偏差 < 5分）
- ✅ 反馈延迟 < 5秒（从上传到报告生成）
- ✅ 可解释性（每个扣分项有具体说明）
- ✅ 支持三种作品类型评审

---

## 📅 实施计划

### 第一阶段（2026.2.16 - 2.28）
- 完成文件处理模块
- 实现规则引擎基础检查
- JPlag/Checkstyle集成

### 第二阶段（2026.3.1 - 3.15）
- DeepSeek API集成
- 大模型评估服务
- 评分汇总逻辑

### 第三阶段（2026.3.16 - 3.31）
- 前端AI报告展示
- 改进建议可视化
- 测试与优化

---

## 🔗 API设计

### POST /api/reviews/ai/{submissionId}
**请求**：执行AI初审
**响应**：
```json
{
  "code": 200,
  "message": "AI初审完成",
  "data": {
    "id": 1,
    "overallScore": 85.5,
    "duplicateRate": 12.3,
    "riskLevel": "LOW",
    "reviewSummary": "作品整体质量良好...",
    "improvementSuggestions": "1. 增加README中的使用说明\n2. 提高代码注释覆盖率",
    "details": [
      {
        "checkItem": "目录结构完整性",
        "checkResult": "PASS",
        "score": 10.0,
        "comment": "包含README和src目录"
      }
    ]
  }
}
```

### GET /api/reports/ai/{submissionId}
**请求**：获取AI审核报告
**响应**：完整的AIReviewReport对象

---

## 💡 未来扩展

### 可扩展点
- **新算法插件化**：通过SystemConfig动态加载
- **多模型对比**：DeepSeek vs GPT-4 vs Claude
- **多语言支持**：Python、C/C++、Java、JavaScript
- **视频内容分析**：帧数检查、音频质量
- **PPT元素分析**：原创图片识别、动画复杂度

---

**文档版本**：v2.0 - 实现完成版
**更新时间**：2026-04-13
**作者**：陈海波
**实现状态**：✅ 已完成基础实现

---

## 🎉 实现完成情况

### ✅ 已完成模块

1. **文件处理模块** ([FileProcessingServiceImpl](src/main/java/com/example/workcompetitionplatform/service/impl/FileProcessingServiceImpl.java))
   - ZIP文件解压功能
   - 文件类型白名单校验
   - 关键文件提取（按作品类型分类）
   - 临时文件清理

2. **规则引擎模块** ([RuleEngineServiceImpl](src/main/java/com/example/workcompetitionplatform/service/impl/RuleEngineServiceImpl.java))
   - 目录结构完整性检查
   - 文件命名规范检查
   - 代码注释覆盖率计算
   - PPT页数估算检查
   - 视频时长估算检查
   - 基础规范评分计算

3. **代码质量分析模块** ([CodeQualityServiceImpl](src/main/java/com/example/workcompetitionplatform/service/impl/CodeQualityServiceImpl.java))
   - 代码重复率检测（简化实现）
   - 代码风格检查（简化实现）
   - 函数复杂度分析
   - 代码质量评分计算

4. **大模型评估模块** ([LLMEvaluationServiceImpl](src/main/java/com/example/workcompetitionplatform/service/impl/LLMEvaluationServiceImpl.java))
   - DeepSeek API集成框架
   - 评估提示词生成
   - 评分结果解析
   - Mock结果生成（API失败时）
   - 评审摘要生成
   - 改进建议生成

5. **AI评审整合服务** ([ReviewServiceImpl.performAIReview](src/main/java/com/example/workcompetitionplatform/service/impl/ReviewServiceImpl.java:51))
   - 完整的AI审核流程集成
   - 不同作品类型的权重计算
   - 风险等级判定
   - 报告和明细保存

6. **API控制器** ([AIReviewController](src/main/java/com/example/workcompetitionplatform/controller/AIReviewController.java))
   - POST /api/ai-reviews/perform/{submissionId} - 执行AI初审
   - GET /api/ai-reviews/report/{submissionId} - 获取AI评审报告
   - GET /api/ai-reviews/details/{reportId} - 获取评审详情

### ⚠️ 待优化项

1. **代码查重**：当前为简化实现，建议集成JPlag工具
2. **代码风格检查**：当前为简化实现，建议集成Checkstyle工具
3. **PPT解析**：当前基于文件大小估算，建议集成Apache POI
4. **视频解析**：当前基于文件大小估算，建议集成FFmpeg或MediaInfo
5. **DeepSeek API**：需要配置真实的API Key和URL
6. **文件上传路径**：需要确保application.properties配置正确

### 📝 使用说明

#### 配置系统参数

在数据库`system_config`表中添加以下配置：

```sql
INSERT INTO system_config (config_key, config_value, config_type, description) VALUES
('deepseek_api_key', 'your-api-key-here', 'STRING', 'DeepSeek API密钥'),
('deepseek_api_url', 'https://api.deepseek.com/v1/chat/completions', 'STRING', 'DeepSeek API地址'),
('ai_model', 'deepseek-chat', 'STRING', '使用的AI模型');
```

#### 执行AI初审

管理员可以通过以下方式触发AI审核：

```bash
# API调用
POST http://localhost:8080/api/ai-reviews/perform/{submissionId}
Authorization: Bearer {admin-jwt-token}

# 响应示例
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "submissionId": 10,
    "overallScore": 85.5,
    "innovationScore": 78,
    "practicalityScore": 82,
    "userExperienceScore": 75,
    "codeQualityScore": 88,
    "documentationScore": 85,
    "duplicateRate": 12.3,
    "complexityLevel": "LOW",
    "riskLevel": "LOW",
    "reviewSummary": "作品整体质量良好...",
    "improvementSuggestions": "1. 增加README...",
    "aiModel": "DeepSeek",
    "reviewTime": "2026-04-13T..."
  }
}
```

#### 查看评审报告

学生、评委、管理员均可查看：

```bash
GET http://localhost:8080/api/ai-reviews/report/{submissionId}
Authorization: Bearer {jwt-token}
```

---

**文档版本**：v1.0
**更新时间**：2026-04-13
**作者**：陈海波