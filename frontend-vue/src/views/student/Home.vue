<template>
  <div class="student-home">
    <div class="container">
      <!-- Page Header -->
      <div class="page-header slide-in">
        <h1 class="page-title">个人中心</h1>
        <p class="body-text">欢迎回来，{{ userName }}</p>
      </div>

      <!-- Stats Cards -->
      <div class="stats-section scale-in">
        <div class="grid grid-4">
          <div class="stat-card card card-flat">
            <div class="stat-icon">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <rect x="4" y="4" width="24" height="24" rx="4" stroke="#5a7fa8" stroke-width="2"/>
                <path d="M9 12H23M9 16H23M9 20H17" stroke="#5a7fa8" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="stat-number">{{ stats.competitionCount }}</div>
            <div class="stat-label caption">赛事报名</div>
          </div>

          <div class="stat-card card card-flat">
            <div class="stat-icon">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <circle cx="10" cy="10" r="5" stroke="#5a7fa8" stroke-width="2"/>
                <circle cx="22" cy="10" r="5" stroke="#5a7fa8" stroke-width="2"/>
                <circle cx="16" cy="22" r="5" stroke="#5a7fa8" stroke-width="2"/>
                <path d="M10 15V16C10 17.5 12 19 16 19C20 19 22 17.5 22 16V15" stroke="#5a7fa8" stroke-width="2"/>
              </svg>
            </div>
            <div class="stat-number">{{ stats.teamCount }}</div>
            <div class="stat-label caption">团队参与</div>
          </div>

          <div class="stat-card card card-flat">
            <div class="stat-icon">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <rect x="5" y="5" width="22" height="22" rx="3" stroke="#5a7fa8" stroke-width="2"/>
                <path d="M10 10H22M10 14H22M10 18H18M10 22H15" stroke="#5a7fa8" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="stat-number">{{ stats.workCount }}</div>
            <div class="stat-label caption">作品提交</div>
          </div>

          <div class="stat-card card card-flat">
            <div class="stat-icon">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <circle cx="16" cy="10" r="7" stroke="#5a7fa8" stroke-width="2"/>
                <path d="M11 17L8 28L16 24L24 28L21 17" stroke="#5a7fa8" stroke-width="2" stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="stat-number">{{ stats.awardCount }}</div>
            <div class="stat-label caption">获奖数量</div>
          </div>
        </div>
      </div>

      <!-- Quick Actions -->
      <div class="actions-section slide-in">
        <h2 class="section-title">快速访问</h2>
        <div class="grid grid-2">
          <router-link to="/student/teams" class="action-card card card-hover">
            <div class="action-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                <circle cx="8" cy="8" r="4" stroke="#5a7fa8" stroke-width="2"/>
                <circle cx="16" cy="8" r="4" stroke="#5a7fa8" stroke-width="2"/>
                <circle cx="12" cy="18" r="4" stroke="#5a7fa8" stroke-width="2"/>
                <path d="M8 12V13C8 14 10 15 12 15C14 15 16 14 16 13V12" stroke="#5a7fa8" stroke-width="2"/>
              </svg>
            </div>
            <div class="action-content">
              <h3 class="card-title">我的团队</h3>
              <p class="caption">管理参赛团队信息</p>
            </div>
            <div class="action-arrow">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M7 5L12 10L7 15" stroke="#7f8c8d" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
          </router-link>

          <router-link to="/student/works" class="action-card card card-hover">
            <div class="action-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                <rect x="4" y="4" width="16" height="16" rx="2" stroke="#5a7fa8" stroke-width="2"/>
                <path d="M8 8H16M8 11H16M8 14H12" stroke="#5a7fa8" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="action-content">
              <h3 class="card-title">我的作品</h3>
              <p class="caption">查看提交的作品</p>
            </div>
            <div class="action-arrow">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M7 5L12 10L7 15" stroke="#7f8c8d" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
          </router-link>

          <router-link to="/student/invitations" class="action-card card card-hover">
            <div class="action-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="8" r="5" stroke="#5a7fa8" stroke-width="2"/>
                <path d="M6 20C6 17 9 15 12 15C15 15 18 17 18 20" stroke="#5a7fa8" stroke-width="2" stroke-linecap="round"/>
                <path d="M18 8L21 8M21 8L21 5M21 8L21 11" stroke="#5a7fa8" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="action-content">
              <h3 class="card-title">团队邀请</h3>
              <p class="caption">处理团队邀请请求</p>
            </div>
            <div class="action-arrow">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M7 5L12 10L7 15" stroke="#7f8c8d" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
          </router-link>

          <router-link to="/student/profile" class="action-card card card-hover">
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
import { get } from '@/utils/api'
import { showError } from '@/utils/messageUtils'

const router = useRouter()

const userName = computed(() => localStorage.getItem('userName') || '用户')

const stats = ref({
  competitionCount: 0,
  teamCount: 0,
  workCount: 0,
  awardCount: 0
})

onMounted(async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
    return
  }

  await fetchStats()
})

const fetchStats = async () => {
  try {
    const data = await get('/student/stats')

    if (data.code === 200) {
      stats.value = data.data
    }
  } catch (error) {
    showError('获取统计数据失败')
    console.error('获取统计数据失败', error)
  }
}
</script>

<style scoped>
.student-home {
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