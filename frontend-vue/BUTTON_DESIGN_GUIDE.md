# 按钮设计系统使用指南

## 设计理念

本次按钮UI重新设计遵循**专业竞赛管理系统**的设计语言，提供：

1. **统一层级** - 6种核心按钮类型（主要、强调、次要、成功、警告、危险）
2. **微交互效果** - 悬停提升、点击波纹、加载动画
3. **多尺寸变体** - 小、标准、大三种尺寸
4. **特殊形状** - 药丸形、圆形、图标按钮
5. **无障碍支持** - 焦点状态、禁用状态、键盘导航

## 核心按钮类型

### 1. 主要按钮（藏青权威风格）

```html
<!-- 主要操作按钮 -->
<button class="btn-primary">提交作品</button>

<!-- 带图标 -->
<button class="btn-primary">
  <svg>...</svg>
  提交作品
</button>

<!-- 禁用状态 -->
<button class="btn-primary" disabled>提交作品</button>

<!-- 加载状态 -->
<button class="btn-primary btn-loading">提交中...</button>
```

**适用场景**：提交、保存、确认、主要操作

---

### 2. 强调按钮（科技蓝风格）

```html
<button class="btn-accent">发送审核</button>
<button class="btn-accent btn-lg">开始评审</button>
```

**适用场景**：审核、评审、发送、关键操作

---

### 3. 次要按钮（边框风格）

```html
<button class="btn-secondary">取消</button>
<button class="btn-secondary btn-pill">返回列表</button>
```

**适用场景**：取消、返回、次要操作

---

### 4. 成功按钮（绿色风格）

```html
<button class="btn-success">通过审核</button>
<button class="btn-success btn-elevated">批准提交</button>
```

**适用场景**：通过、批准、完成、成功操作

---

### 5. 警告按钮（橙色风格）

```html
<button class="btn-warning">设为优秀奖</button>
<button class="btn-warning btn-pill">标记待审</button>
```

**适用场景**：标记、提醒、警告、中等风险操作

---

### 6. 危险按钮（红色风格）

```html
<button class="btn-danger">删除作品</button>
<button class="btn-danger btn-sm">移除</button>
```

**适用场景**：删除、拒绝、高风险操作

---

### 7. 文本按钮（极简风格）

```html
<button class="btn-text">查看详情</button>
<button class="btn-ghost">更多选项</button>
```

**适用场景**：辅助操作、链接式按钮

---

## 尺寸变体

### 小尺寸（紧凑）

```html
<button class="btn-primary btn-sm">提交</button>
<button class="btn-accent btn-sm">审核</button>
<button class="btn-danger btn-sm">删除</button>
```

**高度**：32px
**适用场景**：表格内操作、紧凑布局

---

### 标准尺寸

```html
<button class="btn-primary">提交作品</button>
<button class="btn-secondary">取消</button>
```

**高度**：40px（默认）
**适用场景**：表单按钮、主要操作

---

### 大尺寸（强调）

```html
<button class="btn-primary btn-lg">开始新赛事</button>
<button class="btn-accent btn-lg btn-pill">立即评审</button>
```

**高度**：48px
**适用场景**：页面主按钮、重要操作

---

## 形状变体

### 药丸形状（全圆角）

```html
<button class="btn-primary btn-pill">提交作品</button>
<button class="btn-accent btn-pill btn-lg">开始评审</button>
```

---

### 圆形图标按钮

```html
<!-- 小尺寸 -->
<button class="btn-primary btn-icon btn-sm">
  <svg>...</svg>
</button>

<!-- 标准尺寸 -->
<button class="btn-accent btn-icon">
  <svg>...</svg>
</button>

<!-- 大尺寸 -->
<button class="btn-success btn-icon btn-lg">
  <svg>...</svg>
</button>
```

---

## 特殊应用按钮

### 操作按钮（表格内专用）

```html
<!-- 查看详情 -->
<button class="btn-action-detail">
  <svg>...</svg>
  详情
</button>

<!-- 计算结果 -->
<button class="btn-action-calc">
  <svg>...</svg>
  计算
</button>

<!-- 设置奖项 -->
<button class="btn-action-award">
  <svg>...</svg>
  设置奖项
</button>

<!-- 删除 -->
<button class="btn-action-delete">
  <svg>...</svg>
  删除
</button>
```

---

### 下载按钮

```html
<button class="btn-download">
  <svg>...</svg>
  下载文件
</button>
```

---

## 特殊效果

### 提升阴影

```html
<button class="btn-primary btn-elevated">重要提交</button>
```

---

### 波纹效果

```html
<button class="btn-accent btn-ripple">发送审核</button>
```

---

### 加载状态

```html
<button class="btn-primary btn-loading">提交中...</button>
<button class="btn-secondary btn-loading">加载中...</button>
<button class="btn-danger btn-loading">删除中...</button>
```

---

### 禁用状态

```html
<button class="btn-primary" disabled>不可提交</button>
<button class="btn-accent btn-disabled">权限不足</button>
```

---

### 完整宽度

```html
<button class="btn-primary btn-block">提交整个表单</button>

<!-- 响应式宽度（移动端100%） -->
<button class="btn-primary btn-responsive">提交作品</button>
```

---

## 按钮组合

### 按钮组

```html
<div class="btn-group">
  <button class="btn-primary">保存</button>
  <button class="btn-secondary">取消</button>
</div>
```

---

### 响应式按钮组（移动端堆叠）

```html
<div class="btn-group btn-group-responsive">
  <button class="btn-primary">提交</button>
  <button class="btn-secondary">预览</button>
  <button class="btn-danger">删除</button>
</div>
```

---

## Element Plus 按钮

新样式系统自动应用于 Element Plus 按钮：

```vue
<el-button type="primary">主要按钮</el-button>
<el-button type="success">成功按钮</el-button>
<el-button type="warning">警告按钮</el-button>
<el-button type="danger">危险按钮</el-button>
<el-button type="default">默认按钮</el-button>

<!-- 尺寸 -->
<el-button type="primary" size="small">小按钮</el-button>
<el-button type="primary" size="large">大按钮</el-button>
```

---

## 设计特点

### 1. 微交互效果

- **悬停**：按钮向上提升2-3px，阴影增强
- **点击**：波纹扩散动画，按钮下沉
- **加载**：旋转spinner动画
- **禁用**：灰度滤镜，降低透明度

### 2. 渐变背景

主要、强调、成功、警告按钮使用**135度渐变**，增强立体感：

```css
background: linear-gradient(135deg, #0d2137 0%, #1e3a5f 100%);
```

### 3. 阴影系统

三层阴影营造深度：

```css
box-shadow:
  0 2px 4px rgba(13, 33, 55, 0.12),  /* 主阴影 */
  0 1px 2px rgba(0, 0, 0, 0.08);      /* 边缘阴影 */
```

### 4. 过渡动画

使用**cubic-bezier缓动函数**，提供流畅的物理感受：

```css
transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
```

---

## 最佳实践

### 按钮层级选择

1. **主要操作**：`btn-primary`（提交、保存）
2. **关键操作**：`btn-accent`（审核、评审）
3. **次要操作**：`btn-secondary`（取消、返回）
4. **成功操作**：`btn-success`（通过、批准）
5. **警告操作**：`btn-warning`（标记、提醒）
6. **危险操作**：`btn-danger`（删除、拒绝）

### 尺寸选择

- **表格内操作**：`btn-sm`（32px）
- **表单按钮**：默认尺寸（40px）
- **页面主按钮**：`btn-lg`（48px）

### 图标按钮

所有按钮都支持内联SVG图标：

```html
<button class="btn-primary">
  <svg width="18" height="18">
    <!-- SVG路径 -->
  </svg>
  提交作品
</button>
```

图标尺寸自动调整：
- `btn-sm`：16px图标
- 默认：18px图标
- `btn-lg`：20px图标

---

## 无障碍支持

- **焦点状态**：蓝色外轮廓，2px偏移
- **键盘导航**：Tab键切换，Enter键触发
- **禁用提示**：cursor: not-allowed
- **屏幕阅读器**：支持aria-label

---

## 响应式适配

```css
@media (max-width: 768px) {
  .btn-responsive {
    width: 100%;  /* 移动端按钮全宽 */
  }
}
```

---

## 总结

新的按钮设计系统提供：

✅ **6种核心类型** - 藏青、科技蓝、边框、绿、橙、红
✅ **3种尺寸** - 小(32px)、标准(40px)、大(48px)
✅ **微交互** - 悬停提升、点击波纹、加载动画
✅ **特殊形状** - 药丸形、圆形、图标按钮
✅ **统一风格** - 渐变背景、三层阴影、流畅过渡
✅ **无障碍** - 焦点状态、键盘支持、屏幕阅读器友好
✅ **Element Plus集成** - 自动应用新样式

整体设计语言符合**专业竞赛评审管理系统**的定位，避免通用AI模板风格，提供独特、专业、美观的用户体验。