# 统一卡片尺寸系统使用指南

## 设计目标

确保所有卡片在布局中保持统一、协调的尺寸，避免视觉不平衡和参差不齐的问题。

---

## 核心尺寸标准

### 基础卡片尺寸（5级）

| 尺寸类 | 最小高度 | 内边距 | 适用场景 |
|--------|----------|---------|----------|
| `.card-xs` / `.card-small` | 120px | 12px | 紧凑场景、表格内 |
| `.card-sm` / `.card-standard` | 180px | 16px | 标准场景、列表项 |
| `.card-md` / `.card-medium` | 240px | 24px | 常用场景、内容展示 |
| `.card-lg` / `.card-large` | 320px | 32px | 重要内容、详情页 |
| `.card-xl` / `.card-extra-large` | 400px | 48px | 特殊场景、大型展示 |

---

## 统计卡片尺寸（固定高度）

### 标准统计卡片

```html
<div class="stat-card card">
  <div class="stat-icon">
    <svg width="40" height="40">...</svg>
  </div>
  <div class="stat-number">12</div>
  <div class="stat-label">赛事报名</div>
</div>
```

**尺寸**：
- 固定高度：200px
- 内边距：24px
- 内容居中对齐

---

### 小统计卡片

```html
<div class="stat-card-sm card">
  <!-- 高度：160px -->
</div>
```

**适用场景**：紧凑布局、移动端

---

### 大统计卡片

```html
<div class="stat-card-lg card">
  <!-- 高度：240px -->
</div>
```

**适用场景**：重要指标、首页展示

---

## 操作卡片尺寸（固定高度）

### 标准操作卡片

```html
<router-link to="/teams" class="action-card card card-hover">
  <div class="action-icon">
    <svg width="24" height="24">...</svg>
  </div>
  <div class="action-content">
    <h3 class="card-title">我的团队</h3>
    <p class="caption">管理参赛团队信息</p>
  </div>
  <div class="action-arrow">
    <svg width="20" height="20">...</svg>
  </div>
</router-link>
```

**尺寸**：
- 固定高度：80px
- 内边距：16px
- 横向布局

---

### 小操作卡片

```html
<div class="action-card-sm card">
  <!-- 高度：64px -->
</div>
```

---

### 大操作卡片

```html
<div class="action-card-lg card">
  <!-- 高度：100px -->
</div>
```

---

## 网格卡片等高系统

### 问题：网格中卡片高度不一

**之前**：
```html
<div class="grid grid-2">
  <div class="card">内容少，卡片矮</div>
  <div class="card">内容多，卡片高</div>
  <!-- 视觉不平衡 -->
</div>
```

---

### 解决方案1：网格等高类

```html
<div class="grid-2-equal">
  <div class="card">
    <div class="card-header">标题</div>
    <div class="card-body">内容少</div>
    <div class="card-footer">底部操作</div>
  </div>
  <div class="card">
    <div class="card-header">标题</div>
    <div class="card-body">内容多</div>
    <div class="card-footer">底部操作</div>
  </div>
  <!-- 两个卡片等高，底部操作对齐 -->
</div>
```

**原理**：
- `align-items: stretch` 强制网格子元素拉伸
- Flex布局 + `margin-top: auto` 底部内容自动填充

---

### 解决方案2：统一高度类

```html
<div class="grid grid-4">
  <div class="stat-card card">统计1</div>
  <div class="stat-card card">统计2</div>
  <div class="stat-card card">统计3</div>
  <div class="stat-card card">统计4</div>
  <!-- 所有统计卡片固定200px高度 -->
</div>
```

---

### 解决方案3：网格等高修饰符

```html
<div class="grid grid-2 grid-equal">
  <!-- grid-equal修饰符强制子元素等高 -->
</div>
```

---

## 卡片内容布局标准

### 标准三段式布局

```html
<div class="card card-md">
  <div class="card-header">
    <h3 class="card-title">卡片标题</h3>
    <p class="caption">说明文字</p>
  </div>

  <div class="card-body">
    <p class="body-text">主要内容区域</p>
  </div>

  <div class="card-footer">
    <button class="btn-primary btn-sm">操作按钮</button>
  </div>
</div>
```

**布局结构**：
- `.card-header`：标题区（底部边框分隔）
- `.card-body`：内容区（自适应高度）
- `.card-footer`：操作区（顶部边框分隔）

---

## 固定高度 vs 自适应高度

### 固定高度模式

```html
<!-- 固定200px高度 -->
<div class="card card-fixed card-height-md">
  <!-- 内容超出会被裁剪 -->
</div>

<!-- 固定高度 + 滚动 -->
<div class="card card-fixed-scroll card-height-lg">
  <!-- 内容超出时可滚动 -->
</div>
```

**适用场景**：
- 统计卡片、数据卡片
- 固定展示区域
- 网格等高需求

---

### 自适应高度模式

```html
<!-- 最小高度 + 内容自适应 -->
<div class="card card-flexible">
  <!-- 最小160px，内容多时自动增高 -->
</div>

<!-- 高度范围控制 -->
<div class="card card-range">
  <!-- 最小160px，最大320px，超出滚动 -->
</div>
```

**适用场景**：
- 内容不确定的卡片
- 详情卡片、列表卡片
- 需要灵活布局的场景

---

## 卡片对齐方式

### 内容顶部对齐

```html
<div class="card card-align-top">
  <!-- 内容从顶部开始排列 -->
</div>
```

---

### 内容居中对齐

```html
<div class="stat-card card card-align-center">
  <!-- 统计卡片：内容居中 -->
</div>
```

---

### 内容底部对齐

```html
<div class="card card-align-bottom">
  <!-- 内容底部对齐 -->
</div>
```

---

### 内容垂直分布

```html
<div class="card card-align-stretch">
  <div class="card-header">标题</div>
  <div class="card-body">内容</div>
  <div class="card-footer">底部</div>
  <!-- 三部分均匀分布 -->
</div>
```

---

## 特殊卡片尺寸

### 表单卡片

```html
<div class="form-card card">
  <!-- 最小高度360px，适合表单场景 -->
</div>
```

---

### 详情卡片

```html
<div class="detail-card card">
  <!-- 最小高度280px，详情展示 -->
</div>
```

---

### 空状态卡片

```html
<div class="empty-card card">
  <!-- 固定高度320px，居中显示 -->
</div>
```

---

### 加载卡片

```html
<div class="loading-card card">
  <!-- 固定高度200px，居中显示加载动画 -->
</div>
```

---

## 响应式尺寸调整

### 自动适配不同屏幕

所有卡片尺寸在小屏幕会自动缩小：

| 屏幕宽度 | 卡片调整 |
|----------|----------|
| ≥768px | 标准尺寸 |
| 480-768px | 高度减少20% |
| <480px | 高度减少30%，内边距减少 |

**示例**：

```html
<!-- 桌面：200px -->
<!-- 手机：160px -->
<div class="stat-card card">
  <!-- 自动响应式调整 -->
</div>
```

---

## 自定义卡片高度

### CSS变量控制

```html
<!-- 自定义300px高度 -->
<div class="card card-fixed" style="--card-height: 300px">
  <!-- 使用CSS变量覆盖默认高度 -->
</div>
```

---

### 预定义高度类

```html
<div class="card card-fixed card-height-xs">120px</div>
<div class="card card-fixed card-height-sm">180px</div>
<div class="card card-fixed card-height-md">240px</div>
<div class="card card-fixed card-height-lg">320px</div>
<div class="card card-fixed card-height-xl">400px</div>
```

---

## 实用案例

### 案例1：统计页面（4列等高）

```html
<div class="container">
  <div class="grid-4-equal">
    <div class="stat-card card">
      <div class="stat-icon">
        <svg width="40" height="40">...</svg>
      </div>
      <div class="stat-number">12</div>
      <div class="stat-label">赛事报名</div>
    </div>

    <!-- 其他统计卡片 -->
    <div class="stat-card card">...</div>
    <div class="stat-card card">...</div>
    <div class="stat-card card">...</div>
  </div>
</div>
```

**效果**：所有统计卡片固定200px高度，整齐划一

---

### 案例2：快速访问页面（2列等高）

```html
<div class="grid-2-equal">
  <router-link to="/teams" class="action-card card card-hover">
    <div class="action-icon">
      <svg width="24" height="24">...</svg>
    </div>
    <div class="action-content">
      <h3 class="card-title">我的团队</h3>
      <p class="caption">管理参赛团队信息</p>
    </div>
    <div class="action-arrow">
      <svg width="20" height="20">...</svg>
    </div>
  </router-link>

  <!-- 其他操作卡片 -->
  <router-link to="/works" class="action-card card card-hover">...</router-link>
</div>
```

**效果**：所有操作卡片固定80px高度，底部箭头对齐

---

### 案例3：内容卡片（自适应高度）

```html
<div class="grid grid-3 grid-equal">
  <div class="card card-md card-align-stretch">
    <div class="card-header">
      <h3 class="card-title">项目进展</h3>
    </div>
    <div class="card-body">
      <p class="body-text">简要说明</p>
    </div>
    <div class="card-footer">
      <button class="btn-primary btn-sm">查看详情</button>
    </div>
  </div>

  <!-- 其他卡片 -->
</div>
```

**效果**：最小240px高度，内容自适应，网格等高

---

### 案例4：数据表格（无固定高度）

```html
<div class="data-table card">
  <!-- 表格内容，高度自适应 -->
</div>
```

---

## 卡片阴影和边框统一

### 标准阴影

```html
<div class="card card-shadow-standard">
  <!-- 标准多层阴影 -->
</div>
```

---

### 悬停阴影

```html
<div class="card card-hover card-shadow-hover">
  <!-- 悬停时阴影增强 -->
</div>
```

---

### 无阴影

```html
<div class="card card-shadow-none">
  <!-- 扁平卡片，无阴影 -->
</div>
```

---

### 强调边框

```html
<div class="card card-border-accent">
  <!-- 2px蓝色边框 -->
</div>
```

---

## 内容溢出处理

### 内容裁剪

```html
<div class="card card-truncate-content">
  <!-- 内容溢出时裁剪并显示省略号 -->
</div>
```

---

### 内容滚动

```html
<div class="card card-scroll-content">
  <!-- 内容溢出时可滚动 -->
</div>
```

---

## 最佳实践

### 1. 统计页面使用固定高度

```html
<div class="grid-4-equal">
  <div class="stat-card card">固定200px</div>
  <div class="stat-card card">固定200px</div>
  <div class="stat-card card">固定200px</div>
  <div class="stat-card card">固定200px</div>
</div>
```

---

### 2. 操作列表使用固定高度

```html
<div class="grid-2-equal">
  <div class="action-card card">固定80px</div>
  <div class="action-card card">固定80px</div>
</div>
```

---

### 3. 内容网格使用等高布局

```html
<div class="grid grid-3 grid-equal">
  <div class="card card-md">自适应等高</div>
  <div class="card card-md">自适应等高</div>
  <div class="card card-md">自适应等高</div>
</div>
```

---

### 4. 表格页面使用自适应高度

```html
<div class="data-table card">
  <!-- 高度自适应内容 -->
</div>
```

---

## 尺寸选择指南

| 使用场景 | 推荐尺寸 | 类名组合 |
|----------|----------|----------|
| 统计卡片首页 | 200px固定 | `.stat-card.card` |
| 统计卡片紧凑 | 160px固定 | `.stat-card-sm.card` |
| 操作卡片导航 | 80px固定 | `.action-card.card` |
| 内容卡片标准 | 240px最小 | `.card-md` |
| 详情卡片展示 | 320px最小 | `.card-lg` |
| 表单卡片填写 | 360px最小 | `.form-card.card` |
| 表格卡片容器 | 自适应 | `.data-table.card` |
| 空状态提示 | 320px固定 | `.empty-card.card` |
| 加载状态 | 200px固定 | `.loading-card.card` |

---

## 总结

统一卡片尺寸系统提供：

✅ **5级基础尺寸** - 120px到400px全覆盖
✅ **统计卡片固定尺寸** - 200px标准，160px小，240px大
✅ **操作卡片固定尺寸** - 80px标准，64px小，100px大
✅ **网格等高系统** - 强制网格内卡片等高
✅ **内容布局标准** - 三段式header/body/footer
✅ **固定/自适应高度** - 固定高度和灵活高度模式
✅ **对齐方式控制** - 顶部/居中/底部/垂直分布
✅ **响应式自动调整** - 不同屏幕自动适配
✅ **特殊场景卡片** - 表单、详情、空状态、加载
✅ **自定义高度支持** - CSS变量和预定义类

整体卡片系统**统一、协调、美观**，确保界面视觉平衡和专业性。