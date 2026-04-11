# 数据库初始化说明

## 方式一：通过MySQL命令行执行（推荐）

### 步骤：

1. **打开MySQL命令行工具**
   ```bash
   mysql -u root -p
   ```
   输入您的MySQL root密码

2. **执行初始化脚本**
   ```sql
   source e:/JavaProject/WorkCompetitionPlatform/database/init_database.sql;
   ```

3. **验证初始化结果**
   ```sql
   USE work_competition_platform;
   SHOW TABLES;
   SELECT COUNT(*) FROM user;
   SELECT COUNT(*) FROM role;
   SELECT COUNT(*) FROM competition;
   SELECT COUNT(*) FROM competition_track;
   ```

## 方式二：通过MySQL Workbench执行

### 步骤：

1. 打开MySQL Workbench
2. 连接到本地MySQL服务器（输入root密码）
3. 打开SQL脚本文件：`e:\JavaProject\WorkCompetitionPlatform\database\init_database.sql`
4. 点击"Execute"按钮（闪电图标）执行脚本

## 方式三：使用Navicat等图形化工具

### 步骤：

1. 打开Navicat for MySQL
2. 连接到本地MySQL服务器
3. 选择"运行SQL文件"
4. 选择脚本路径：`e:\JavaProject\WorkCompetitionPlatform\database\init_database.sql`
5. 点击"开始"执行

## 初始化数据清单

执行成功后，数据库将包含：

### 用户数据（共14个）
- **1个管理员**：admin / admin123
- **3个评委**：judge001, judge002, judge003 / judge123
- **10个学生**：student001-student010 / student123

### 赛事数据
- **1个赛事**：2024年湖南农业大学计算机作品赛
- **3个赛道**：
  - 程序设计作品（CODE_TRACK）
  - 演示文稿作品（PPT_TRACK）
  - 数媒动漫与短视频作品（VIDEO_TRACK）

### 系统配置
- 团队邀请过期天数：7天
- AI评审权重：30%
- 评委评审权重：70%
- 文件上传限制：300MB
- 代码重复率阈值：30%

## 常见问题

### Q1: 提示"Access denied"
**解决方案**：检查MySQL root密码是否正确，或修改 `application.properties` 中的密码配置：
```properties
spring.datasource.password=您的实际密码
```

### Q2: 提示"Database already exists"
**解决方案**：脚本已检测并处理，不会报错。如需重新初始化，先手动删除数据库：
```sql
DROP DATABASE IF EXISTS work_competition_platform;
```

### Q3: 密码加密说明
脚本中的密码已使用BCrypt加密（实际项目中需要通过Java代码生成）：
- admin123 → `$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH`
- judge123 → 同上
- student123 → 同上

**注意**：当前脚本中的密码哈希值仅用于演示，实际运行时需要在Java后端通过BCryptPasswordEncoder生成真实密码哈希。

## 验证脚本执行成功

执行以下SQL查询：

```sql
-- 查看所有用户
SELECT id, username, real_name, email, college, major, student_no FROM user;

-- 查看角色分配
SELECT u.username, r.role_name FROM user u
JOIN user_role ur ON u.id = ur.user_id
JOIN role r ON ur.role_id = r.id;

-- 查看赛事信息
SELECT * FROM competition;

-- 查看赛道信息
SELECT track_name, track_type, max_team_size, min_team_size FROM competition_track;

-- 查看系统配置
SELECT config_key, config_value, description FROM system_config;
```

## 下一步

数据库初始化完成后：

1. 启动Spring Boot应用：`mvn spring-boot:run`
2. 访问系统：http://localhost:8080
3. 使用测试账号登录验证功能

---

**创建时间**: 2026-01-19
**作者**: 陈海波
**指导老师**: 贺细平