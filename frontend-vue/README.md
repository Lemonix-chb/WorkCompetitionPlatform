# 信息管理与智能评价系统 - Vue 3 前端项目

## 📋 项目概览

**技术栈**: Vue 3 + Vite + Pinia + Vue Router + Element Plus
**开发语言**: JavaScript/TypeScript
**UI框架**: Element Plus
**构建工具**: Vite 5

---

## 🚀 快速开始

### 安装依赖
```bash
cd frontend-vue
npm install
```

### 启动开发服务器
```bash
npm run dev
```
访问：http://localhost:3000

### 构建生产版本
```bash
npm run build
```

### 预览生产版本
```bash
npm run preview
```

---

## 📁 项目结构

```
frontend-vue/
├── public/                     # 静态资源
├── src/
│   ├── api/                    # API接口封装
│   │   ├── auth.js            # 认证API
│   │   ├── user.js            # 用户API
│   │   ├── team.js            # 团队API
│   │   ├── submission.js      # 作品API
│   │   └── review.js          # 评审API
│   ├── components/             # 通用组件
│   │   ├── Navbar.vue         # 导航栏组件
│   │   ├── Sidebar.vue        # 侧边栏组件
│   │   ├── StatCard.vue       # 统计卡片组件
│   │   ├── TrackCard.vue      # 赛道卡片组件
│   │   └── DataTable.vue      # 数据表格组件
│   ├── router/                 # 路由配置
│   │   └ index.js             # 路由定义
│   ├── stores/                 # Pinia状态管理
│   │   ├── user.js            # 用户状态
│   │   ├── team.js            # 团队状态
│   │   └ submission.js        # 作品状态
│   ├── styles/                 # 全局样式
│   │   ├── variables.css      # CSS变量
│   │   ├── global.css         # 全局样式
│   ├── utils/                  # 工具函数
│   │   ├── request.js         # axios封装
│   │   ├── auth.js            # 认证工具
│   │   └ date.js              # 日期处理
│   ├── views/                  # 页面组件
│   │   ├── LoginView.vue      # 登录页面
│   │   ├── NotFoundView.vue   # 404页面
│   │   ├── student/           # 学生页面
│   │   │   ├── HomeView.vue
│   │   │   ├── TeamView.vue
│   │   │   ├── WorksView.vue
│   │   │   └ ResultsView.vue
│   │   ├── judge/             # 评审员页面
│   │   │   ├── HomeView.vue
│   │   │   ├── SubmissionsView.vue
│   │   │   └ ReviewView.vue
│   │   ├── admin/             # 管理员页面
│   │   │   ├── DashboardView.vue
│   │   │   ├── CompetitionsView.vue
│   │   │   ├── UsersView.vue
│   │   │   ├── SubmissionsView.vue
│   │   │   └ ReviewsView.vue
│   ├── App.vue                 # 根组件
│   └ main.js                   # 入口文件
├── index.html                  # HTML模板
├── vite.config.js              # Vite配置
├── package.json                # 项目配置
└── README.md                   # 本文件
```

---

## 🎨 页面设计

### 登录页面 (LoginView.vue)
- ✅ 左侧品牌展示区（蓝色渐变）
- ✅ 右侧登录表单
- ✅ 三种角色选择卡片（学生、评委、管理员）
- ✅ 表单验证
- ✅ 密码提示

### 学生首页
- ✅ Hero区域（赛事介绍）
- ✅ 统计数据卡片（参赛队伍、学生、作品、专家）
- ✅ 赛道介绍卡片（程序设计、演示文稿、短视频）
- ✅ 快速操作区域（组建团队、提交作品、查看结果）

### 评审员首页
- ✅ 作品列表表格
- ✅ 筛选和搜索功能
- ✅ AI初审报告预览
- ✅ 评分表单

### 管理员仪表盘
- ✅ 左侧导航栏
- ✅ 数据统计面板
- ✅ 图表展示区
- ✅ 快速操作入口

---

## 🔧 开发规范

### 组件命名
- 页面组件：`xxxView.vue`
- 通用组件：`XxxComponent.vue`
- 布局组件：`XxxLayout.vue`

### API封装
```javascript
// src/api/auth.js
import request from '@/utils/request'

export const login = (data) => {
  return request.post('/api/auth/login', data)
}

export const logout = () => {
  return request.post('/api/auth/logout')
}
```

### 状态管理
```javascript
// src/stores/user.js
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref({})
  const setUserInfo = (info) => {
    userInfo.value = info
  }
  return { userInfo, setUserInfo }
})
```

---

## 📝 待开发功能

### 学生模块
- [ ] 团队组建页面
- [ ] 作品提交页面
- [ ] 作品管理页面
- [ ] 评审结果页面
- [ ] 个人中心页面

### 评审员模块
- [ ] 作品列表页面
- [ ] 作品详情页面
- [ ] 评分表单页面
- [ ] AI报告详情页面
- [ ] 评审记录页面

### 管理员模块
- [ ] 用户管理页面
- [ ] 赛事管理页面
- [ ] 作品管理页面
- [ ] 评审管理页面
- [ ] 数据统计页面
- [ ] 系统配置页面

---

## 🌐 API对接

### 后端API地址
- 开发环境：http://localhost:8080/api
- 生产环境：http://your-domain.com/api

### API文档
参考：`docs/API_DESIGN.md`

---

## 💡 开发建议

1. **组件化开发**：拆分复杂页面为多个组件
2. **状态管理**：使用Pinia管理全局状态
3. **路由懒加载**：使用动态import优化性能
4. **样式统一**：使用CSS变量和Element Plus主题
5. **类型安全**：建议逐步迁移到TypeScript

---

**开发时间**: 2026-01-19
**开发者**: 陈海波
**指导老师**: 贺细平