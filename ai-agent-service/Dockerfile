FROM python:3.11-slim

# 设置工作目录
WORKDIR /app

# 安装系统依赖
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 复制依赖文件
COPY requirements.txt .

# 安装Python依赖
RUN pip install --no-cache-dir -r requirements.txt

# 复制应用代码
COPY app/ ./app/
COPY config.yaml .

# 创建必要目录
RUN mkdir -p uploads temp

# 设置环境变量
ENV PYTHONUNBUFFERED=1
ENV DEEPSEEK_API_KEY=${DEEPSEEK_API_KEY}

# 暴露端口
EXPOSE 8001

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8001/health || exit 1

# 启动FastAPI服务
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8001"]