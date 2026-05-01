<template>
  <div class="competitions-page">
    <!-- Hero Section -->
    <div class="hero-section">
      <div class="container">
        <div class="hero-content slide-in">
          <h1 class="hero-title">计算机作品竞赛</h1>
          <p class="hero-subtitle body-text">展现创新实力，点亮科技梦想</p>
          <div class="hero-actions flex gap-md">
            <button class="btn-secondary" @click="scrollToList">
              查看赛事
            </button>
            <router-link v-if="isLoggedIn" :to="userHomePath" class="btn-primary">
              个人中心
            </router-link>
            <router-link v-else to="/login" class="btn-primary">
              立即报名
            </router-link>
          </div>
        </div>
      </div>
    </div>

    <!-- Competitions Section -->
    <div class="competitions-section" id="competitions-list">
      <div class="container">
        <h2 class="section-title">当前赛事</h2>

        <!-- Loading -->
        <div v-if="loading" class="loading-state">
          <div class="spinner"></div>
        </div>

        <!-- Error -->
        <div v-if="error" class="error-state card">
          <p class="body-text">{{ error }}</p>
        </div>

        <!-- Competitions Grid -->
        <div v-else-if="competitions.length > 0" class="grid grid-auto scale-in">
          <div v-for="comp in competitions" :key="comp.id" class="competition-card card card-hover">
            <!-- Header -->
            <div class="card-header">
              <h3 class="card-title">{{ comp.competitionName }}</h3>
              <span :class="getStatusClass(comp)" class="badge">
                {{ getStatusText(comp) }}
              </span>
            </div>

            <!-- Countdown -->
            <div v-if="getCountdownText(comp)" class="countdown-section mb-md">
              <span class="countdown-text accent">{{ getCountdownText(comp) }}</span>
            </div>

            <!-- Description -->
            <p class="body-text mb-lg">{{ comp.description }}</p>

            <!-- Info Grid -->
            <div class="info-grid mb-lg">
              <div class="info-item">
                <span class="info-label caption">赛事年份</span>
                <span class="info-value body-text">{{ comp.competitionYear }}</span>
              </div>
              <div class="info-item">
                <span class="info-label caption">主办单位</span>
                <span class="info-value body-text">{{ comp.organizer }}</span>
              </div>
              <div class="info-item">
                <span class="info-label caption">报名时间</span>
                <span class="info-value caption">{{ formatDateTime(comp.registrationStart) }} - {{ formatDateTime(comp.registrationEnd) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label caption">联系方式</span>
                <span class="info-value caption">{{ comp.contactPhone }}</span>
              </div>
            </div>

            <!-- Actions -->
            <div class="card-footer flex gap-md">
              <router-link :to="`/competitions/${comp.id}/tracks`" class="link">
                查看赛道详情
              </router-link>
              <button
                v-if="isLoggedIn"
                :disabled="!canRegister(comp)"
                :class="canRegister(comp) ? 'btn-primary' : 'btn-disabled'"
                @click="handleRegister(comp)"
              >
                {{ canRegister(comp) ? (userRole === 'STUDENT' ? '报名参赛' : '进入管理') : '报名已截止' }}
              </button>
              <button
                v-if="!isLoggedIn && canRegister(comp)"
                class="btn-secondary"
                @click="handleRegister(comp)"
              >
                登录报名
              </button>
            </div>
          </div>
        </div>

        <!-- Empty -->
        <div v-else class="empty-state card">
          <div class="empty-icon">
            <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
              <rect x="8" y="8" width="32" height="28" rx="3" stroke="#bdc3c7" stroke-width="2"/>
              <path d="M8 16H40M16 24H32M16 28H28" stroke="#bdc3c7" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <h3 class="card-title mb-md">暂无赛事信息</h3>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useCompetitionStore } from '../stores/competition'
import { formatDateTime } from '@/utils/dateUtils'
import { showSuccess } from '@/utils/messageUtils'
import { get } from '@/utils/api'

const router = useRouter()
const store = useCompetitionStore()

const { competitions, loading, error, fetchCompetitions } = store

const isLoggedIn = computed(() => localStorage.getItem('token'))
const userRole = computed(() => localStorage.getItem('userRole'))

const timeStatusMap = ref({})

const userHomePath = computed(() => {
  const role = userRole.value
  if (role === 'STUDENT') return '/student'
  if (role === 'JUDGE') return '/judge'
  if (role === 'ADMIN') return '/admin'
  return '/login'
})

onMounted(async () => {
  await fetchCompetitions()
  await fetchAllTimeStatuses()
})

const fetchAllTimeStatuses = async () => {
  const promises = competitions.value.map(comp =>
    get(`/competitions/${comp.id}/time-status`)
      .then(status => {
        timeStatusMap.value[comp.id] = status
      })
      .catch(error => {
        console.error(`Failed to fetch time status for competition ${comp.id}`, error)
      })
  )
  await Promise.all(promises)
}

const scrollToList = () => {
  document.getElementById('competitions-list').scrollIntoView({ behavior: 'smooth' })
}

const getStatusClass = (comp) => {
  const timeStatus = timeStatusMap.value[comp.id]
  if (!timeStatus) {
    return {
      'badge-primary': comp.status === 'PUBLISHED',
      'badge-warning': comp.status === 'ONGOING',
      'badge-gray': comp.status === 'FINISHED' || comp.status === 'DRAFT'
    }
  }

  const phase = timeStatus.currentPhase
  return {
    'badge-primary': phase === 'REGISTRATION',
    'badge-accent': phase === 'SUBMISSION',
    'badge-warning': phase === 'REVIEW',
    'badge-gray': phase === 'FINISHED' || phase === 'BEFORE_REGISTRATION' || phase === 'BEFORE_SUBMISSION'
  }
}

const getStatusText = (comp) => {
  const timeStatus = timeStatusMap.value[comp.id]
  if (!timeStatus) {
    const texts = {
      DRAFT: '草稿',
      PUBLISHED: '已发布',
      ONGOING: '进行中',
      FINISHED: '已结束'
    }
    return texts[comp.status] || comp.status
  }

  const texts = {
    BEFORE_REGISTRATION: '报名未开始',
    REGISTRATION: '报名进行中',
    BEFORE_SUBMISSION: '报名已截止',
    SUBMISSION: '提交进行中',
    REVIEW: '评审进行中',
    FINISHED: '已结束'
  }
  return texts[timeStatus.currentPhase] || timeStatus.currentPhase
}

const canRegister = (comp) => {
  const timeStatus = timeStatusMap.value[comp.id]
  if (!timeStatus) {
    return comp.status === 'PUBLISHED' || comp.status === 'ONGOING'
  }
  return timeStatus.canRegister
}

const getCountdownText = (comp) => {
  const timeStatus = timeStatusMap.value[comp.id]
  if (!timeStatus) return ''

  if (timeStatus.currentPhase === 'REGISTRATION' && timeStatus.registrationDaysRemaining) {
    return `报名剩余 ${timeStatus.registrationDaysRemaining} 天`
  }
  if (timeStatus.currentPhase === 'SUBMISSION' && timeStatus.submissionDaysRemaining) {
    return `提交剩余 ${timeStatus.submissionDaysRemaining} 天`
  }
  return ''
}

const handleRegister = (comp) => {
  const token = localStorage.getItem('token')
  if (!token) {
    showSuccess('请先登录后再报名')
    router.push('/login')
  } else {
    const userRole = localStorage.getItem('userRole')
    if (userRole === 'STUDENT') {
      router.push(`/competitions/${comp.id}/tracks`)
    } else if (userRole === 'JUDGE') {
      router.push('/judge')
    } else if (userRole === 'ADMIN') {
      router.push('/admin')
    } else {
      router.push(`/competitions/${comp.id}/tracks`)
    }
  }
}
</script>

<style scoped>
.competitions-page {
  min-height: 100vh;
}

.hero-section {
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  padding: var(--spacing-3xl) 0;
  min-height: 50vh;
  display: flex;
  align-items: center;
}

.hero-content {
  text-align: center;
  max-width: 600px;
  margin: 0 auto;
}

.hero-title {
  font-size: 40px;
  font-weight: 600;
  color: var(--color-text-white);
  margin-bottom: var(--spacing-md);
  letter-spacing: -0.02em;
}

.hero-subtitle {
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: var(--spacing-xl);
}

.hero-actions {
  justify-content: center;
}

.competitions-section {
  padding: var(--spacing-3xl) 0;
}

.loading-state {
  padding: var(--spacing-3xl) 0;
  text-align: center;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--color-border);
  border-top-color: var(--color-accent);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-state, .empty-state {
  padding: var(--spacing-3xl);
  text-align: center;
}

.competition-card {
  padding: var(--spacing-xl);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-md);
}

.countdown-section {
  padding: var(--spacing-sm) var(--spacing-md);
  background: rgba(90, 127, 168, 0.1);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
}

.countdown-text {
  font-weight: 600;
  font-size: 14px;
}

.btn-disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background: var(--color-border);
  color: var(--color-text-secondary);
  border: none;
  pointer-events: none;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-md);
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.info-label {
  color: var(--color-text-secondary);
}

.info-value {
  font-weight: 500;
}

.empty-icon {
  margin-bottom: var(--spacing-lg);
  opacity: 0.5;
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 32px;
  }

  .hero-section {
    min-height: 40vh;
    padding: var(--spacing-2xl) 0;
  }

  .hero-actions {
    flex-direction: column;
    align-items: center;
  }

  .hero-actions button,
  .hero-actions a {
    width: 100%;
    justify-content: center;
    max-width: 300px;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .card-footer {
    flex-direction: column;
  }

  .card-footer button,
  .card-footer a {
    width: 100%;
    justify-content: center;
  }
}
</style>