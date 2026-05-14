<template>
  <div class="judge-home">
    <div class="container">
      <!-- Academic Page Header -->
      <div class="page-header slide-in">
        <div class="header-content">
          <div class="academic-badge-header">
            <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
              <!-- Academic Shield -->
              <path d="M16 1.5L28.5 6.5V16C28.5 24 23 29 16 30.5C9 29 3.5 24 3.5 16V6.5L16 1.5Z"
                    fill="var(--color-primary)"
                    stroke="var(--color-accent)"
                    stroke-width="0.75"/>
              <!-- Judge Badge -->
              <circle cx="16" cy="12" r="4" stroke="var(--color-accent)" stroke-width="1" fill="none"/>
              <path d="M10 24C10 20 13 18 16 18C19 18 22 20 22 24"
                    stroke="var(--color-accent)"
                    stroke-width="1"
                    stroke-linecap="round"/>
              <!-- Magnifying Glass -->
              <circle cx="22" cy="22" r="2" stroke="var(--color-accent)" stroke-width="0.75" fill="none"/>
              <line x1="23.5" y1="23.5" x2="25" y2="25" stroke="var(--color-accent)" stroke-width="0.75" stroke-linecap="round"/>
            </svg>
          </div>
          <div class="header-text">
            <h1 class="page-title">评审中心</h1>
            <p class="header-subtitle">欢迎回来，{{ userName }}</p>
          </div>
        </div>
        <div class="header-meta">
          <span class="meta-label">当前状态</span>
          <span class="badge badge-reviewing">在线评审</span>
        </div>
      </div>

      <!-- Stats Cards -->
      <div class="stats-section scale-in">
        <div class="section-header">
          <h2 class="section-title">评审概览</h2>
          <div class="section-divider"></div>
        </div>

        <div class="grid grid-4">
          <div class="stat-card card">
            <div class="stat-icon">
              <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
                <!-- Work Document -->
                <rect x="10" y="8" width="28" height="32" rx="3" fill="none" stroke="var(--color-accent)" stroke-width="2"/>
                <line x1="14" y1="14" x2="34" y2="14" stroke="var(--color-accent)" stroke-width="1.5" stroke-linecap="round"/>
                <line x1="14" y1="20" x2="34" y2="20" stroke="var(--color-accent)" stroke-width="1.5" stroke-linecap="round"/>
                <line x1="14" y1="26" x2="26" y2="26" stroke="var(--color-accent)" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="stat-number">{{ stats.pendingReviews }}</div>
            <div class="stat-label">待评审作品</div>
            <div class="stat-trend">
              <span class="trend-text pending">等待处理</span>
            </div>
          </div>

          <div class="stat-card card">
            <div class="stat-icon">
              <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
                <!-- Completed Check -->
                <circle cx="24" cy="24" r="18" stroke="var(--status-completed)" stroke-width="2" fill="none"/>
                <path d="M16 24L20 28L32 18"
                      stroke="var(--status-completed)"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="stat-number">{{ stats.completedReviews }}</div>
            <div class="stat-label">已评审作品</div>
            <div class="stat-trend">
              <span class="trend-text completed">已完成</span>
            </div>
          </div>

          <div class="stat-card card card-accent">
            <div class="stat-icon">
              <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
                <!-- Award Medal -->
                <circle cx="24" cy="16" r="12" stroke="var(--color-accent)" stroke-width="2" fill="none"/>
                <path d="M20 12L24 16L28 12M24 16V28" stroke="var(--color-accent)" stroke-width="1.5" stroke-linecap="round"/>
                <!-- Laurels -->
                <path d="M16 32C16 32 18 28 20 30M28 30C28 30 30 28 32 32"
                      stroke="var(--color-accent)"
                      stroke-width="1.5"
                      stroke-linecap="round"/>
              </svg>
            </div>
            <div class="stat-number accent-text">{{ stats.totalScores }}</div>
            <div class="stat-label">评审总评分</div>
            <div class="stat-trend">
              <span class="trend-text accent">累计得分</span>
            </div>
          </div>

          <div class="stat-card card">
            <div class="stat-icon">
              <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
                <!-- Track Path -->
                <path d="M8 24C8 24 16 16 24 16C32 16 40 24 40 24"
                      stroke="var(--color-accent)"
                      stroke-width="2"
                      stroke-linecap="round"/>
                <circle cx="8" cy="24" r="4" fill="var(--color-accent)"/>
                <circle cx="24" cy="16" r="4" fill="var(--color-accent)"/>
                <circle cx="40" cy="24" r="4" fill="var(--color-accent)"/>
              </svg>
            </div>
            <div class="stat-number">{{ stats.assignedTracks }}</div>
            <div class="stat-label">负责赛道</div>
            <div class="stat-trend">
              <span class="trend-text">分配任务</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Quick Actions -->
      <div class="actions-section slide-in">
        <div class="section-header">
          <h2 class="section-title">快速访问</h2>
          <div class="section-divider"></div>
        </div>

        <div class="grid grid-2">
          <router-link to="/judge/pending" class="action-card card card-interactive card-accent">
            <div class="action-icon-wrapper">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <rect x="6" y="6" width="20" height="20" rx="2" fill="none" stroke="var(--color-accent)" stroke-width="2"/>
                <line x1="10" y1="10" x2="22" y2="10" stroke="var(--color-accent)" stroke-width="1.5" stroke-linecap="round"/>
                <line x1="10" y1="14" x2="22" y2="14" stroke="var(--color-accent)" stroke-width="1.5" stroke-linecap="round"/>
                <line x1="10" y1="18" x2="16" y2="18" stroke="var(--color-accent)" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="action-content">
              <h3 class="card-title accent-text">待评审作品</h3>
              <p class="caption">查看待评审的作品列表</p>
              <div class="action-meta">
                <span class="badge badge-pending">{{ stats.pendingReviews }} 个待处理</span>
              </div>
            </div>
            <div class="action-arrow accent-arrow">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                <path d="M8 6L16 12L8 18" stroke="var(--color-accent)" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
          </router-link>

          <router-link to="/judge/reviewed" class="action-card card card-interactive">
            <div class="action-icon-wrapper">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <circle cx="16" cy="16" r="12" stroke="var(--status-completed)" stroke-width="2" fill="none"/>
                <path d="M10 16L13 19L22 12"
                      stroke="var(--status-completed)"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="action-content">
              <h3 class="card-title">已评审作品</h3>
              <p class="caption">查看已完成的评审记录</p>
              <div class="action-meta">
                <span class="badge badge-completed">{{ stats.completedReviews }} 个已完成</span>
              </div>
            </div>
            <div class="action-arrow">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                <path d="M8 6L16 12L8 18" stroke="var(--color-text-tertiary)" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
          </router-link>

          <router-link to="/judge/stats" class="action-card card card-interactive">
            <div class="action-icon-wrapper">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <rect x="8" y="24" width="4" height="4" rx="1" fill="var(--color-accent)" opacity="0.6"/>
                <rect x="12" y="18" width="4" height="10" rx="1" fill="var(--color-accent)" opacity="0.8"/>
                <rect x="16" y="12" width="4" height="16" rx="1" fill="var(--color-accent)"/>
                <rect x="20" y="16" width="4" height="12" rx="1" fill="var(--color-accent)" opacity="0.8"/>
              </svg>
            </div>
            <div class="action-content">
              <h3 class="card-title">评审统计</h3>
              <p class="caption">查看评审数据分析</p>
              <div class="action-meta">
                <span class="badge badge-ai">详细统计</span>
              </div>
            </div>
            <div class="action-arrow">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                <path d="M8 6L16 12L8 18" stroke="var(--color-text-tertiary)" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
          </router-link>

          <router-link to="/judge/profile" class="action-card card card-interactive">
            <div class="action-icon-wrapper">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <circle cx="16" cy="12" r="6" stroke="var(--color-accent)" stroke-width="2" fill="none"/>
                <path d="M8 26C8 22 12 20 16 20C20 20 24 22 24 26"
                      stroke="var(--color-accent)"
                      stroke-width="2"
                      stroke-linecap="round"/>
              </svg>
            </div>
            <div class="action-content">
              <h3 class="card-title">个人设置</h3>
              <p class="caption">修改个人信息</p>
              <div class="action-meta">
                <span class="badge">账号管理</span>
              </div>
            </div>
            <div class="action-arrow">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                <path d="M8 6L16 12L8 18" stroke="var(--color-text-tertiary)" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { get } from '@/utils/api'
import { showWarning, showError } from '@/utils/messageUtils'

const router = useRouter()

const userName = computed(() => localStorage.getItem('userName') || '评审员')

const stats = ref({
  pendingReviews: 0,
  completedReviews: 0,
  totalScores: 0,
  assignedTracks: 0
})

onMounted(async () => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('userRole')

  if (!token) {
    router.push('/login')
    return
  }

  if (role !== 'JUDGE') {
    showWarning('您没有权限访问评审员页面')
    router.push('/login')
    return
  }

  await fetchStats()
})

const fetchStats = async () => {
  try {
    const data = await get('/judge/stats')
    stats.value = data
  } catch (error) {
    console.error('获取统计数据失败', error)
    showError('获取统计数据失败')
  }
}
</script>

<style scoped>
.judge-home {
  min-height: 100vh;
}

.container {
  padding-top: var(--spacing-xl);
}

/* Page Header */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-3xl);
  padding: var(--spacing-xl);
  background: var(--color-bg-card);
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-card);
}

.header-content {
  display: flex;
  align-items: center;
  gap: var(--spacing-xl);
  flex: 1;
}

.academic-badge-header {
  display: flex;
  align-items: center;
  justify-content: center;
  filter: drop-shadow(0 0 8px rgba(52, 152, 219, 0.4));
}

.header-text {
  flex: 1;
}

.header-subtitle {
  font-family: var(--font-body);
  font-size: var(--text-base);
  color: var(--color-text-secondary);
  margin-top: var(--spacing-sm);
}

.header-meta {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.meta-label {
  font-size: var(--text-sm);
  color: var(--color-text-tertiary);
}

/* Section Headers */
.section-header {
  margin-bottom: var(--spacing-xl);
}

.section-divider {
  height: 2px;
  background: linear-gradient(90deg, var(--color-accent) 0%, transparent 100%);
  margin-top: var(--spacing-md);
}

/* Stats Section */
.stats-section {
  margin-bottom: var(--spacing-3xl);
}

.stat-card {
  position: relative;
  overflow: hidden;
}

.stat-icon {
  margin-bottom: var(--spacing-md);
  opacity: 0.9;
}

.stat-number {
  font-family: var(--font-display);
  font-size: var(--text-4xl);
  font-weight: 700;
  color: var(--color-primary);
  line-height: 1;
  margin-bottom: var(--spacing-sm);
  letter-spacing: -0.04em;
}

.accent-text {
  color: var(--color-accent);
}

.stat-label {
  font-family: var(--font-mono);
  font-size: var(--text-xs);
  color: var(--color-text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.1em;
  margin-bottom: var(--spacing-md);
}

.stat-trend {
  padding-top: var(--spacing-md);
  border-top: 1px solid var(--color-border-light);
}

.trend-text {
  font-family: var(--font-body);
  font-size: var(--text-sm);
  color: var(--color-text-tertiary);
}

.trend-text.pending {
  color: var(--status-pending);
}

.trend-text.completed {
  color: var(--status-completed);
}

.trend-text.accent {
  color: var(--color-accent);
  font-weight: 600;
}

/* Actions Section */
.actions-section {
  margin-bottom: var(--spacing-2xl);
}

.action-card {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  padding: var(--spacing-xl);
  cursor: pointer;
  text-decoration: none;
  border-left: 4px solid transparent;
  position: relative;
  overflow: hidden;
}

.action-card:hover {
  border-left-color: var(--color-accent);
}

.action-card.card-accent {
  border-left-color: var(--color-accent);
}

.action-icon-wrapper {
  width: 64px;
  height: 64px;
  background: rgba(52, 152, 219, 0.08);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all var(--transition-fast);
}

.action-card:hover .action-icon-wrapper {
  background: rgba(52, 152, 219, 0.12);
}

.action-content {
  flex: 1;
}

.action-meta {
  margin-top: var(--spacing-md);
}

.action-arrow {
  opacity: 0.3;
  transition: all var(--transition-fast);
  flex-shrink: 0;
}

.action-card:hover .action-arrow {
  opacity: 1;
  transform: translateX(4px);
}

.accent-arrow {
  opacity: 0.6;
}

.action-card:hover .accent-arrow {
  opacity: 1;
}

/* Responsive */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: var(--spacing-lg);
  }

  .header-meta {
    width: 100%;
    justify-content: flex-start;
  }

  .grid-4 {
    grid-template-columns: repeat(2, 1fr);
  }

  .grid-2 {
    grid-template-columns: 1fr;
  }

  .stat-number {
    font-size: var(--text-3xl);
  }

  .action-icon-wrapper {
    width: 56px;
    height: 56px;
  }
}

@media (max-width: 480px) {
  .grid-4 {
    grid-template-columns: 1fr;
  }

  .action-card {
    flex-direction: column;
    text-align: center;
    gap: var(--spacing-md);
    padding: var(--spacing-lg);
  }

  .action-arrow {
    display: none;
  }
}
</style>