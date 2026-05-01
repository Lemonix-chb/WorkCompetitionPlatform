# AI审核模块 Docker 配置说明

## 服务概述

本系统使用Docker容器部署AI审核模块,包括以下两个核心服务:

### 1. JPlag - 代码重复检测服务
- **功能**: 检测代码相似度和抄袭
- **镜像**: `jplag/jplag:latest`
- **端口**: 8081 (容器内8080映射到宿主机8081)
- **存储**: `./docker/jplag` - 存储检测结果和临时文件
- **内存**: 最大2GB,初始512MB

### 2. Checkstyle - Java代码风格检查服务
- **功能**: 检查Java代码规范和质量
- **镜像**: `checkstyle/checkstyle:latest`
- **端口**: 8082
- **存储**: `./docker/checkstyle` - 存储检查配置文件
- **配置**: 使用Google Java代码规范

## 快速启动

### 前置要求
- Docker Desktop 4.70+ 或 Docker Engine 29.4+
- 至少4GB可用内存
- uploads目录存在(作品上传目录)

### 启动步骤

```bash
# 1. 配置Docker镜像加速(解决网络问题)
# 编辑 ~/.docker/daemon.json 或 C:\Users\用户名\.docker\daemon.json
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com",
    "https://mirror.baidubce.com"
  ]
}

# 重启Docker Desktop或Docker服务

# 2. 创建必要目录
mkdir -p docker/jplag docker/checkstyle uploads

# 3. 拉取镜像
docker-compose pull

# 4. 启动服务
docker-compose up -d

# 5. 检查服务状态
docker-compose ps
docker-compose logs
```

## 服务管理

```bash
# 查看运行状态
docker-compose ps

# 查看日志
docker-compose logs -f jplag
docker-compose logs -f checkstyle

# 重启服务
docker-compose restart

# 停止服务
docker-compose down

# 完全清理(包括数据)
docker-compose down -v
```

## 健康检查

服务提供健康检查接口:

- **JPlag**: `http://localhost:8081/health`
- **Checkstyle**: `http://localhost:8082/health`

## 与Spring Boot集成

Spring Boot应用通过HTTP API调用容器服务:

```java
// JPlag调用示例
POST http://localhost:8081/api/compare
{
  "submissionId": 123,
  "files": ["path/to/file1.java", "path/to/file2.java"]
}

// Checkstyle调用示例
POST http://localhost:8082/api/check
{
  "file": "path/to/code.java",
  "config": "google_checks.xml"
}
```

## 配置文件

### JPlag配置
位置: `docker/jplag/config.properties`
```properties
# 检测敏感度
sensitivity=DEFAULT
# 最小匹配长度
min-match-length=10
# 结果存储路径
result-dir=/data/results
```

### Checkstyle配置
位置: `docker/checkstyle/google_checks.xml`
- 默认使用Google Java代码规范
- 可自定义检查规则

## 数据持久化

- **JPlag数据**: `./docker/jplag` 映射到容器 `/data`
  - `/data/results` - 检测结果JSON文件
  - `/data/temp` - 临时解压文件

- **Checkstyle数据**: `./docker/checkstyle` 映射到容器 `/config`
  - `/config/google_checks.xml` - 代码规范配置
  - `/config/results` - 检查结果

- **作品文件**: `./uploads` 映射到容器 `/uploads` (只读)
  - 学生上传的作品文件供AI审核读取

## 资源限制

可在docker-compose.yml中调整资源限制:

```yaml
services:
  jplag:
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 2G
        reservations:
          memory: 512M
```

## 网络配置

所有服务运行在 `workcompetition-ai-network` 网络中:

- 网络类型: bridge
- 容器间可通过服务名相互访问
- Spring Boot应用可通过localhost端口访问

## 故障排查

### 镜像拉取失败
```bash
# 检查Docker配置
docker info | grep "Registry Mirrors"

# 手动拉取镜像
docker pull jplag/jplag:latest
docker pull checkstyle/checkstyle:latest

# 使用代理
# 配置HTTP_PROXY和HTTPS_PROXY环境变量
```

### 服务启动失败
```bash
# 查看详细日志
docker-compose logs jplag
docker-compose logs checkstyle

# 检查容器状态
docker inspect workcompetition-jplag
docker inspect workcompetition-checkstyle

# 检查端口占用
netstat -ano | findstr "8081"
netstat -ano | findstr "8082"
```

### 健康检查失败
```bash
# 手动测试接口
curl http://localhost:8081/health
curl http://localhost:8082/health

# 进入容器调试
docker exec -it workcompetition-jplag bash
docker exec -it workcompetition-checkstyle sh
```

## 生产环境建议

1. **使用特定版本镜像**:
   ```yaml
   image: jplag/jplag:v4.3.0
   image: checkstyle/checkstyle:10.12.0
   ```

2. **配置日志驱动**:
   ```yaml
   logging:
     driver: "json-file"
     options:
       max-size: "10m"
       max-file: "3"
   ```

3. **启用自动重启**:
   ```yaml
   restart: always
   ```

4. **配置资源监控**:
   使用Docker Stats或Prometheus监控容器资源使用

5. **定期备份数据**:
   ```bash
   # 备份JPlag结果
   tar -czf jplag-backup.tar.gz docker/jplag/results

   # 备份Checkstyle配置
   tar -czf checkstyle-backup.tar.gz docker/checkstyle
   ```

## 开发环境配置

开发环境可简化配置:

```yaml
version: '3.8'
services:
  jplag:
    image: jplag/jplag:latest
    ports:
      - "8081:8080"
    volumes:
      - ./uploads:/uploads:ro

  checkstyle:
    image: checkstyle/checkstyle:latest
    ports:
      - "8082:8082"
    volumes:
      - ./uploads:/uploads:ro
```

## 注意事项

1. **首次启动时间**: JPlag和Checkstyle镜像较大,首次拉取可能需要5-10分钟
2. **内存占用**: 建议至少分配4GB内存给Docker Desktop
3. **文件权限**: uploads目录需要可读权限
4. **网络访问**: Spring Boot需要能访问localhost:8081和localhost:8082
5. **并发限制**: 建议限制并发审核任务不超过线程池配置(AsyncConfig中设置)

## 下一步

配置完成后,需要:
1. 在Spring Boot中创建调用Docker服务的API客户端
2. 在IReviewService中集成JPlag和Checkstyle结果
3. 测试完整审核流程
4. 监控容器资源使用