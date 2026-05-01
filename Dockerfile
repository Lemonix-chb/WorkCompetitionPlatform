FROM openjdk:17-jdk-alpine

# 设置工作目录
WORKDIR /app

# 安装必要工具（用于调用Docker命令）
RUN apk add --no-cache docker-cli

# 复制Maven构建产物
COPY target/WorkCompetitionPlatform-0.0.1-SNAPSHOT.jar app.jar

# 创建上传目录
RUN mkdir -p /app/uploads /app/logs

# 设置时区
ENV TZ=Asia/Shanghai
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone

# 暴露端口
EXPOSE 8080

# 启动应用
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]