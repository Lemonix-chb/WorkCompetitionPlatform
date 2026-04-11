<template>
  <div class="judge-home">
    <div class="container">
      <!-- Page Header -->
      <div class="page-header slide-in">
        <h1 class="page-title">评审中心</h1>
        <p class="body-text">欢迎回来，{{ userName }}</p>
      </div>

      <!-- Stats Cards -->
      <div class="stats-section scale-in">
        <div class="grid grid-4">
          <div class="stat-card card card-flat">
            <div class="stat-icon">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <rect x="5" y="5" width="22" height="22" rx="3" stroke="#5a7fa8" stroke-width="2"/>
                <path d="M10 10H22M10 14H22M10 18H18" stroke="#5a7fa8" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="stat-number">{{ stats.pendingReviews }}</div>
            <div class="stat-label caption">待评审作品</div>
          </div>

          <div class="stat-card card card-flat">
            <div class="stat-icon">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <circle cx="16" cy="16" r="12" stroke="#5a7fa8" stroke-width="2"/>
                <path d="M12 16L14 18L20 12" stroke="#5a7fa8" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="stat-number">{{ stats.completedReviews }}</div>
            <div class="stat-label caption">已评审作品</div>
          </div>

          <div class="stat-card card card-flat">
            <div class="stat-icon">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <circle cx="16" cy="10" r="7" stroke="#5a7fa8" stroke-width="2"/>
                <path d="M11 17L8 28L16 24L24 28L21 17" stroke="#5a7fa8" stroke-width="2" stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="stat-number">{{ stats.totalScores }}</div>
            <div class="stat-label caption">评审总评分</div>
          </div>

          <div class="stat-card card card-flat">
            <div class="stat-icon">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <path d="M8 16H24V24C24 26 22 28 16 28C10 28 8 26 8 24V16Z" stroke="#5a7fa8" stroke-width="2"/>
                <path d="M8 16C8 12 12 8 16 8C20 8 24 12 24 16" stroke="#5a7fa8" stroke-width="2"/>
              </svg>
            </div>
            <div class="stat-number">{{ stats.assignedTracks }}</div>
            <div class="stat-label caption">负责赛道</div>
          </div>
        </div>
      </div>

      <!-- Quick Actions -->
      <div class="actions-section slide-in">
        <h2 class="section-title">快速访问</h2>
        <div class="grid grid-2">
          <router-link to="/judge/pending" class="action-card card card-hover">
            <div class="action-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                <rect x="4" y="4" width="16" height="16" rx="2" stroke="#5a7fa8" stroke-width="2"/>
                <path d="M8 8H16M8 11H16M8 14H12" stroke="#5a7fa8" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="action-content">
              <h3 class="card-title">待评审作品</h3>
              <p class="caption">查看待评审的作品列表</p>
            </div>
            <div class="action-arrow">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M7 5L12 10L7 15" stroke="#7f8c8d" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
          </router-link>

          <router-link to="/judge/reviewed" class="action-card card card-hover">
            <div class="action-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="12" r="10" stroke="#5a7fa8" stroke-width="2"/>
                <path d="M8 12L10 14L16 8" stroke="#5a7fa8" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="action-content">
              <h3 class="card-title">已评审作品</h3>
              <p class="caption">查看已完成的评审记录</p>
            </div>
            <div class="action-arrow">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M7 5L12 10L7 15" stroke="#7f8c8d" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
          </router-link>

          <router-link to="/judge/stats" class="action-card card card-hover">
            <div class="action-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                <rect x="4" y="14" width="3" height="6" rx="1" stroke="#5a7fa8" stroke-width="2"/>
                <rect x="9" y="10" width="3" height="10" rx="1" stroke="#5a7fa8" stroke-width="2"/>
                <rect x="14" y="6" width="3" height="14" rx="1" stroke="#5a7fa8" stroke-width="2"/>
              </svg>
            </div>
            <div class="action-content">
              <h3 class="card-title">评审统计</h3>
              <p class="caption">查看评审数据分析</p>
            </div>
            <div class="action-arrow">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M7 5L12 10L7 15" stroke="#7f8c8d" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
          </router-link>

          <router-link to="/judge/profile" class="action-card card card-hover">
            <div class="action-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="8" r="5" stroke="#5a7fa8" stroke-width="2"/>
                <path d="M6 20C6 17 9 15 12 15C15 15 18 17 18 20" stroke="#5a7fa8" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="action-content">
              <h3 class="card-title">个人设置</h3>
              <p class="caption">修改个人信息</p>
            </div>
            <div class="action-arrow">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M7 5L12 10L7 15" stroke="#7f8c8d" stroke-width="2" stroke-linecap="round"/>
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
import { ElMessage } from 'element-plus'

const router = useRouter()

const userName = computed(() => localStorage.getItem('userName') || '评审员')

const stats = ref({
  pendingReviews: 0,
  completedReviews: 0,
  totalScores: 0,
  assignedTracks: 3
})

onMounted(async () => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('userRole')

  if (!token) {
    router.push('/login')
    return
  }

  if (role !== 'JUDGE') {
    ElMessage.warning('您没有权限访问评审员页面')
    router.push('/login')
    return
  }

  await fetchStats()
})

const fetchStats = async () => {
  try {
    // TODO: 实现评审员统计API
    // const response = await fetch('/api/judge/stats')
    // const data = await response.json()
    // if (data.code === 200) {
    //   stats.value = data.data
    // }
  } catch (error) {
    console.error('获取统计数据失败', error)
  }
}
</script>

<style scoped>
.judge-home {
  min-height: 100vh;
}

.page-header {
  margin-bottom: var(--spacing-3xl);
}

.stats-section {
  margin-bottom: var(--spacing-3xl);
}

.stat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: var(--spacing-xl);
  transition: all var(--transition-fast);
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-icon {
  margin-bottom: var(--spacing-md);
  opacity: 0.7;
}

.stat-number {
  font-size: 32px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-sm);
}

.stat-label {
  color: var(--color-text-secondary);
}

.actions-section {
  margin-bottom: var(--spacing-2xl);
}

.action-card {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  padding: var(--spacing-lg);
  cursor: pointer;
  text-decoration: none;
  border-left: 3px solid transparent;
  transition: all var(--transition-fast);
}

.action-card:hover {
  border-left-color: var(--color-accent);
}

.action-icon {
  width: 48px;
  height: 48px;
  background: rgba(90, 127, 168, 0.08);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-content {
  flex: 1;
}

.action-arrow {
  opacity: 0.4;
  transition: opacity var(--transition-fast);
}

.action-card:hover .action-arrow {
  opacity: 1;
}

@media (max-width: 768px) {
  .stat-number {
    font-size: 24px;
  }

  .action-icon {
    width: 40px;
    height: 40px;
  }
}
</style>