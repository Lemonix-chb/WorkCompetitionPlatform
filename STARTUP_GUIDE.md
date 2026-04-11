# 🚀 项目启动指南

## ✅ 已完成准备工作

### 1. 数据库已初始化
- ✅ 数据库名: work_competition_platform
- ✅ 23张表已创建
- ✅ 测试数据已导入

### 2. 前端依赖已安装
- ✅ npm install 完成
- ✅ 161个依赖包已安装
- ✅ 前端服务器正在启动

---

## 📋 手动启动步骤（推荐）

由于命令行环境限制，建议您按以下步骤手动启动：

### 步骤1: 启动后端

#### 方式1: 双击批处理文件
```
双击运行: e:\JavaProject\WorkCompetitionPlatform\start-backend.bat
```

#### 方式2: 使用IDE启动
1. 打开IDEA
2. 设置 Project SDK 为 JDK 17:
   ```
   C:\Users\Lemonix-chb\.jdks\ms-17.0.18
   ```
3. 运行 `WorkCompetitionPlatformApplication` 主类

#### 方式3: 命令行启动
```bash
# 打开CMD，执行：
cd e:\JavaProject\WorkCompetitionPlatform
set JAVA_HOME=C:\Users\Lemonix-chb\.jdks\ms-17.0.18
mvn clean package -DskipTests
java -jar target\WorkCompetitionPlatform-0.0.1-SNAPSHOT.jar
```

**后端启动成功标志**:
```
========================================
信息管理与智能评价系统启动成功！
API文档地址: http://localhost:8080/doc.html
========================================
```

---

### 步骤2: 启动前端

#### 方式1: 命令行启动（已自动启动）
```bash
cd e:\JavaProject\WorkCompetitionPlatform\frontend-vue
npm run dev
```

#### 方式2: 查看前端服务器状态
前端正在后台启动中，稍后会显示：
```
VITE v5.0.8  ready in xxx ms

➜  Local:   http://localhost:3000/
➜  Network: use --host to expose
```

---

## 🌐 访问地址

### 后端
- **应用地址**: http://localhost:8080
- **API文档**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs

### 前端
- **应用地址**: http://localhost:3000

---

## 🔐 测试账号

| 角色 | 账号 | 密码 | 登录后跳转 |
|------|------|------|-----------|
| 学生 | student001 | 123456 | /student |
| 学生 | 2021001 | 123456 | /student |
| 评委 | judge001 | 123456 | /judge |
| 管理员 | admin | 123456 | /admin |

---

## 🔧 常见问题

### Q1: 后端编译失败
**原因**: JDK版本不匹配
**解决**: 确保使用JDK 17
```bash
# 检查当前JDK版本
java -version
# 应显示: openjdk version "17.0.18"
```

### Q2: 前端启动失败
**原因**: 端口3000被占用
**解决**: 修改vite.config.js中的端口
```javascript
server: {
  port: 3001  // 改为其他端口
}
```

### Q3: 数据库连接失败
**原因**: MySQL未启动或密码错误
**解决**:
```bash
# 1. 启动MySQL服务
# 2. 检查密码是否为123456
# 3. 检查数据库是否存在
mysql -u root -p123456 -e "SHOW DATABASES LIKE 'work_competition%';"
```

### Q4: Redis连接失败
**原因**: Redis未启动
**解决**:
```bash
redis-server
# 默认端口: 6379
```

---

## 📊 启动顺序建议

1. ✅ **数据库** - MySQL已启动并初始化
2. ⏳ **Redis** - 如需使用缓存功能，请手动启动
3. ⏳ **后端** - 请手动启动（见上方步骤）
4. ✅ **前端** - 已自动启动

---

## 💡 快速测试

启动成功后，按以下顺序测试：

1. **访问前端**: http://localhost:3000
2. **登录测试**: 使用admin账号登录
3. **查看API文档**: http://localhost:8080/swagger-ui.html
4. **测试API**: 使用Swagger测试登录接口

---

## 🎯 下一步

启动成功后，您可以：
- 测试所有API接口
- 开发更多前端页面
- 集成AI评审模块
- 部署到生产环境

---

**更新时间**: 2026-01-19 20:50
**状态**: 前端已启动，后端待手动启动