# Work Competition Platform — Vue 3 前端

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue 3 | 3.3.4 | 渐进式前端框架（Composition API + `<script setup>`） |
| Vite | 5.0.8 | 开发服务器与生产构建 |
| Pinia | 2.1.7 | 状态管理（Composition API 风格） |
| Vue Router | 4.2.5 | SPA 路由（嵌套路由 + 懒加载） |
| Element Plus | 2.4.4 | UI 组件库（表格/表单/对话框/选择器等） |
| ECharts | 6.0.0 | 数据可视化图表（管理员统计页） |
| Axios | 1.6.2 | HTTP 客户端（JWT 拦截器） |

## 快速开始

```bash
cd frontend-vue
npm install
npm run dev        # 开发服务器 → http://localhost:3000
npm run build      # 生产构建 → dist/
npm run preview    # 预览生产构建
npm run lint       # ESLint 代码检查
```

## 项目结构

```
frontend-vue/
├── package.json
├── vite.config.js                  (port 3000, proxy /api → :8080)
├── index.html
└── src/
    ├── main.js                     (入口：挂载 Pinia/Router/ElementPlus/Icons)
    ├── App.vue                     (根组件：<router-view />)
    ├── router/index.js             (17条路由 + 全局 beforeEach 守卫)
    ├── stores/competition.js       (赛事 Store)
    ├── layouts/
    │   ├── StudentLayout.vue       (学生端侧边栏布局)
    │   ├── JudgeLayout.vue         (评委端侧边栏布局)
    │   └── AdminLayout.vue         (管理员端侧边栏布局)
    ├── components/
    │   ├── Sidebar.vue             (统一侧边栏：按角色动态菜单、可折叠、移动端)
    │   └── SidebarIcons.js         (11个自定义 SVG 图标)
    ├── utils/
    │   ├── api.js                  (Axios 实例：JWT注入 + 统一错误处理)
    │   ├── dateUtils.js            (日期格式化)
    │   ├── messageUtils.js         (消息提示封装)
    │   └── attachmentUtils.js      (附件工具 + useWorkAttachments composable)
    ├── styles/
    │   ├── professional.css        (核心设计系统)
    │   ├── buttons.css
    │   ├── card-sizes.css
    │   ├── layout.css
    │   └── main.css
    └── views/
        ├── Login.vue               (登录 — 公共)
        ├── Register.vue            (注册 — 公共)
        ├── Competitions.vue        (赛事列表 — 公共)
        ├── Tracks.vue              (赛道详情 — 公共)
        ├── student/                (学生端 8页)
        │   ├── Home.vue
        │   ├── Registration.vue
        │   ├── Teams.vue           (三级Tab：我的团队/邀请/申请)
        │   ├── TeamDetail.vue
        │   ├── Works.vue           (Dashboard风格：AI评分条+进度流)
        │   ├── Results.vue
        │   ├── Invitations.vue
        │   └── Profile.vue
        ├── judge/                  (评委端 5页)
        │   ├── Home.vue
        │   ├── Pending.vue         (卡片网格+评分Dialog)
        │   ├── Reviewed.vue
        │   ├── Stats.vue
        │   └── Profile.vue
        └── admin/                  (管理员端 7页)
            ├── Home.vue
            ├── Competitions.vue    (赛事CRUD+赛道管理)
            ├── Judges.vue
            ├── Students.vue
            ├── Works.vue           (批量/自动分配评委)
            ├── Reviews.vue         (评审结果+奖项设置)
            └── Stats.vue           (ECharts可视化)
```

## 路由架构

- **公共页面**（无需登录）：`/login`、`/register`、`/competitions`、`/competitions/:id/tracks`
- **学生端**（/student）：Home、Registration、Teams、TeamDetail、Works、Results、Invitations、Profile
- **评委端**（/judge）：Home、Pending、Reviewed、Stats、Profile
- **管理员端**（/admin）：Home、Competitions、Judges、Students、Works、Reviews、Stats

路由守卫 `beforeEach`：检查 `meta.requiresAuth` → 验证 token 存在 → 验证 role 匹配 → 放行或重定向 `/login`。

## API 代理

开发环境通过 Vite proxy 将 `/api` 请求转发到 `http://localhost:8080`。

生产环境需在 Nginx 等反向代理中配置同等规则。

## 设计系统

深藏青 + 专业蓝配色方案，衬线标题字体（Cormorant Garamond）+ 无衬线正文字体（IBM Plex Sans）。详见 `src/styles/professional.css`。

## 测试账户

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 学生 | student001 | 123456 |
| 评委 | judge001 | 123456 |
| 管理员 | admin | 123456 |

---

**开发者**: 陈海波

**指导老师**: 贺细平
