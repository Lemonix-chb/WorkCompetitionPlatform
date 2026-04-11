<template>
  <div class="admin-home">
    <div class="container">
      <!-- Page Header -->
      <div class="page-header slide-in">
        <h1 class="page-title">管理中心</h1>
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
            <div class="stat-number">{{ stats.totalCompetitions }}</div>
            <div class="stat-label caption">赛事总数</div>
          </div>

          <div class="stat-card card card-flat">
            <div class="stat-icon">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <circle cx="10" cy="10" r="6" stroke="#5a7fa8" stroke-width="2"/>
                <circle cx="22" cy="10" r="6" stroke="#5a7fa8" stroke-width="2"/>
                <circle cx="16" cy="22" r="6" stroke="#5a7fa8" stroke-width="2"/>
                <path d="M10 16V17C10 18.5 13 20 16 20C19 20 22 18.5 22 17V16" stroke="#5a7fa8" stroke-width="2"/>
              </svg>
            </div>
            <div class="stat-number">{{ stats.totalTeams }}</div>
            <div class="stat-label caption">报名团队</div>
          </div>

          <div class="stat-card card card-flat">
            <div class="stat-icon">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <rect x="6" y="6" width="20" height="20" rx="2" stroke="#5a7fa8" stroke-width="2"/>
                <path d="M10 10H22M10 14H22M10 18H16" stroke="#5a7fa8" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="stat-number">{{ stats.totalWorks }}</div>
            <div class="stat-label caption">提交作品</div>
          </div>

          <div class="stat-card card card-flat">
            <div class="stat-icon">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <circle cx="16" cy="12" r="8" stroke="#5a7fa8" stroke-width="2"/>
                <path d="M8 24C8 20 12 18 16 18C20 18 24 20 24 24" stroke="#5a7fa8" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="stat-number">{{ stats.totalUsers }}</div>
            <div class="stat-label caption">注册用户</div>
          </div>
        </div>
      </div>

      <!-- Quick Actions -->
      <div class="actions-section slide-in">
        <h2 class="section-title">系统管理</h2>
        <div class="grid grid-3">
          <router-link to="/admin/competitions" class="action-card card card-hover">
            <div class="action-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                <rect x="4" y="4" width="16" height="16" rx="2" stroke="#5a7fa8" stroke-width="2"/>
                <path d="M8 8H16M8 11H16M8 14H12" stroke="#5a7fa8" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="action-content">
              <h3 class="card-title">赛事管理</h3>
              <p class="caption">管理赛事和赛道</p>
            </div>
            <div class="action-arrow">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M7 5L12 10L7 15" stroke="#7f8c8d" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
          </router-link>

          <router-link to="/admin/judges" class="action-card card card-hover">
            <div class="action-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="8" r="5" stroke="#5a7fa8" stroke-width="2"/>
                <path d="M6 20C6 17 9 15 12 15C15 15 18 17 18 20" stroke="#5a7fa8" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="action-content">
              <h3 class="card-title">评审员管理</h3>
              <p class="caption">管理评审员信息</p>
            </div>
            <div class="action-arrow">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M7 5L12 10L7 15" stroke="#7f8c8d" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
          </router-link>

          <router-link to="/admin/students" class="action-card card card-hover">
            <div class="action-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="8" r="5" stroke="#5a7fa8" stroke-width="2"/>
                <path d="M6 20C6 17 9 15 12 15C15 15 18 17 18 20" stroke="#5a7fa8" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="action-content">
              <h3 class="card-title">参赛者管理</h3>
              <p class="caption">管理学生信息</p>
            </div>
            <div class="action-arrow">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M7 5L12 10L7 15" stroke="#7f8c8d" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
          </router-link>

          <router-link to="/admin/works" class="action-card card card-hover">
            <div class="action-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                <rect x="4" y="4" width="16" height="16" rx="2" stroke="#5a7fa8" stroke-width="2"/>
                <path d="M8 8H16M8 11H16M8 14H12" stroke="#5a7fa8" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="action-content">
              <h3 class="card-title">作品管理</h3>
              <p class="caption">管理提交作品</p>
            </div>
            <div class="action-arrow">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M7 5L12 10L7 15" stroke="#7f8c8d" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
          </router-link>

          <router-link to="/admin/reviews" class="action-card card card-hover">
            <div class="action-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                <path d="M4 6L12 3L20 6V12C20 16 16 19 12 19C8 19 4 16 4 12V6Z" stroke="#5a7fa8" stroke-width="2" stroke-linejoin="round"/>
                <path d="M8 12L10 14L16 8" stroke="#5a7fa8" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="action-content">
              <h3 class="card-title">评审管理</h3>
              <p class="caption">分配评审任务</p>
            </div>
            <div class="action-arrow">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M7 5L12 10L7 15" stroke="#7f8c8d" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
          </router-link>

          <router-link to="/admin/stats" class="action-card card card-hover">
            <div class="action-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                <rect x="4" y="14" width="3" height="6" rx="1" stroke="#5a7fa8" stroke-width="2"/>
                <rect x="9" y="10" width="3" height="10" rx="1" stroke="#5a7fa8" stroke-width="2"/>
                <rect x="14" y="6" width="3" height="14" rx="1" stroke="#5a7fa8" stroke-width="2"/>
              </svg>
            </div>
            <div class="action-content">
              <h3 class="card-title">数据统计</h3>
              <p class="caption">系统数据分析</p>
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

const userName = computed(() => localStorage.getItem('userName') || '管理员')

const stats = ref({
  totalCompetitions: 1,
  totalTeams: 0,
  totalWorks: 0,
  totalUsers: 15
})

onMounted(async () => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('userRole')

  if (!token) {
    router.push('/login')
    return
  }

  if (role !== 'ADMIN') {
    ElMessage.warning('您没有权限访问管理员页面')
    router.push('/login')
    return
  }

  await fetchStats()
})

const fetchStats = async () => {
  try {
    // TODO: 实现管理员统计API
    // const response = await fetch('/api/admin/stats')
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
.admin-home {
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