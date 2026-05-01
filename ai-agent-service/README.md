# README - AI Agent Service

## 快速启动指南

### 1. 安装依赖

```bash
cd ai-agent-service
pip install -r requirements.txt
```

### 2. 配置DeepSeek API Key

编辑 `config.yaml` 文件，设置你的DeepSeek API Key：

```yaml
deepseek_api_key: "sk-your-actual-api-key-here"
```

或者在环境变量中设置：

```bash
export DEEPSEEK_API_KEY="sk-your-api-key-here"
```

### 3. 启动服务

**Linux/Mac:**
```bash
bash start.sh
```

**Windows:**
```bash
start.bat
```

或直接运行：
```bash
python -m uvicorn app.main:app --host 0.0.0.0 --port 8001 --reload
```

### 4. 测试服务

服务启动后，访问：
- **API文档**: http://localhost:8001/docs
- **健康检查**: http://localhost:8001/health
- **根路径**: http://localhost:8001/

### 5. 运行测试

```bash
python -m pytest tests/test_api.py -v
```

## API使用示例

### 提交审核任务

```bash
curl -X POST http://localhost:8001/api/review \
  -H "Content-Type: application/json" \
  -d '{
    "submission_id": 123,
    "work_type": "CODE",
    "file_path": "/uploads/work.zip",
    "work_description": "Java程序设计作品"
  }'
```

预期响应：
```json
{
  "submission_id": 123,
  "status": "processing",
  "message": "AI审核已启动，完成后将通过回调接口返回结果"
}
```

### 查询审核状态

```bash
curl http://localhost:8001/api/status/123
```

## 项目结构

```
ai-agent-service/
├── app/
│   ├── main.py          # FastAPI主入口
│   ├── config.py        # Pydantic配置管理
│   ├── api/             # API路由和模型
│   ├── agent/           # LangChain Agent核心
│   ├── services/        # 业务服务
│   └── utils/           # 工具模块
├── tests/               # 单元测试
├── requirements.txt     # Python依赖
├── config.yaml          # 配置文件
└── start.sh/start.bat   # 启动脚本
```

## 下一步

当前框架已可启动，接下来需要：
1. 实现真实的LangChain Agent（替换Mock版本）
2. 集成JPlag/Checkstyle Docker工具
3. 实现DeepSeek API真实调用
4. 创建Docker配置文件
5. Spring Boot回调接口集成

## 注意事项

- 当前使用Mock实现，返回模拟审核结果
- DeepSeek API Key需要在config.yaml中配置
- 需要Python 3.11+环境
- FastAPI端口8001，与Spring Boot 8080端口不同