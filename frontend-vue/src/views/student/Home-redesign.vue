<template>
  <div class="student-home">
    <div class="container">
      <!-- Academic Header -->
      <div class="page-header slide-in">
        <div class="academic-crest">
          <svg width="56" height="56" viewBox="0 0 56 56" fill="none">
            <!-- Academic Shield -->
            <path d="M28 4L50 14V28C50 42 40 52 28 54C16 52 6 42 6 28V14L28 4Z"
                  fill="var(--color-primary)"
                  stroke="var(--color-accent)"
                  stroke-width="1.5"/>
            <!-- laurel wreath -->
            <path d="M14 42C14 42 18 36 22 38M34 38C34 38 38 36 42 42"
                  stroke="var(--color-accent)"
                  stroke-width="1.2"
                  stroke-linecap="round"/>
            <!-- Achievement Star -->
            <circle cx="28" cy="20" r="6" fill="var(--color-accent)" opacity="0.8"/>
            <path d="M28 14L28 26M20 20L36 20"
                  stroke="var(--color-accent)"
                  stroke-width="1.5"
                  stroke-linecap="round"/>
          </svg>
        </div>
        <div class="header-content">
          <h1 class="page-title">学生工作台</h1>
          <div class="header-subtitle caption">
            <span class="user-name">{{ userName }}</span>
            <span class="separator">•</span>
            <span class="status">参赛进行中</span>
          </div>
        </div>
        <div class="header-decoration"></div>
      </div>

      <!-- Academic Stats Section -->
      <div class="stats-section scale-in">
        <div class="section-header">
          <h2 class="section-title">参赛数据概览</h2>
          <div class="section-divider"></div>
        </div>

        <div class="stats-grid">
          <!-- Competition Stat -->
          <div class="stat-card card">
            <div class="stat-ornament"></div>
            <div class="stat-icon">
              <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
                <!-- Trophy -->
                <path d="M24 8L34 12V22C34 32 28 38 24 40C20 38 14 32 14 22V12L24 8Z"
                      fill="none"
                      stroke="var(--color-accent)"
                      stroke-width="2"/>
                <circle cx="24" cy="16" r="3" fill="var(--color-accent)"/>
                <!-- Laurels -->
                <path d="M12 40L16 36M32 36L36 40"
                      stroke="var(--color-accent)"
                      stroke-width="1.5"
                      stroke-linecap="round"/>
                <rect x="20" y="40" width="8" height="3" rx="1" fill="var(--color-accent)"/>
              </svg>
            </div>
            <div class="stat-number">{{ stats.competitionCount }}</div>
            <div class="stat-label">赛事报名</div>
            <div class="stat-trend caption">
              <span class="trend-indicator active"></span>
              积极参赛
            </div>
          </div>

          <!-- Team Stat -->
          <div class="stat-card card card-accent">
            <div class="stat-ornament accent"></div>
            <div class="stat-icon">
              <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
                <!-- Team Group -->
                <circle cx="18" cy="16" r="7" stroke="var(--color-accent)" stroke-width="2" fill="none"/>
                <circle cx="30" cy="16" r="7" stroke="var(--color-accent)" stroke-width="2" fill="none"/>
                <circle cx="24" cy="28" r="7" stroke="var(--color-accent)" stroke-width="2" fill="none"/>
                <!-- Connection Lines -->
                <path d="M18 23V24C18 28 22 30 24 30C26 30 30 28 30 24V23"
                      stroke="var(--color-accent)"
                      stroke-width="1.5"
                      stroke-linecap="round"/>
              </svg>
            </div>
            <div class="stat-number accent-text">{{ stats.teamCount }}</div>
            <div class="stat-label">团队参与</div>
            <div class="stat-trend caption">
              <span class="trend-indicator accent"></span>
              团队协作
            </div>
          </div>

          <!-- Works Stat -->
          <div class="stat-card card">
            <div class="stat-ornament"></div>
            <div class="stat-icon">
              <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
                <!-- Document -->
                <rect x="8" y="6" width="32" height="36" rx="3" fill="none" stroke="var(--color-accent)" stroke-width="2"/>
                <!-- Text Lines -->
                <line x1="12" y1="12" x2="36" y2="12" stroke="var(--color-accent)" stroke-width="1.5" stroke-linecap="round"/>
                <line x1="12" y1="18" x2="36" y2="18" stroke="var(--color-accent)" stroke-width="1.5" stroke-linecap="round"/>
                <line x1="12" y1="24" x2="28" y2="24" stroke="var(--color-accent)" stroke-width="1.5" stroke-linecap="round"/>
                <!-- Checkmark -->
                <path d="M32 24L34 26L38 22"
                      stroke="var(--color-accent)"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="stat-number">{{ stats.workCount }}</div>
            <div class="stat-label">作品提交</div>
            <div class="stat-trend caption">
              <span class="trend-indicator active"></span>
              持续创作
            </div>
          </div>

          <!-- Awards Stat -->
          <div class="stat-card card">
            <div class="stat-ornament"></div>
            <div class="stat-icon">
              <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
                <!-- Medal -->
                <circle cx="24" cy="12" r="10" stroke="var(--color-accent)" stroke-width="2" fill="none"/>
                <path d="M16 24L12 40L24 34L36 40L32 24"
                      stroke="var(--color-accent)"
                      stroke-width="2"
                      stroke-linejoin="round"/>
                <!-- Star -->
                <path d="M24 6L26 10L30 10L27 13L28 17L24 14L20 17L21 13L18 10L22 10L24 6Z"
                      fill="var(--color-accent)"
                      opacity="0.6"/>
              </svg>
            </div>
            <div class="stat-number">{{ stats.awardCount }}</div>
            <div class="stat-label">获奖成果</div>
            <div class="stat-trend caption">
              <span class="trend-indicator success"></span>
              优秀表现
            </div>
          </div>
        </div>
      </div>

      <!-- Quick Access Section -->
      <div class="actions-section slide-in">
        <div class="section-header">
          <h2 class="section-title">工作台入口</h2>
          <div class="section-divider"></div>
        </div>

        <div class="actions-grid">
          <!-- Teams Link -->
          <router-link to="/student/teams" class="action-card card card-interactive">
            <div class="action-ornament"></div>
            <div class="action-icon-wrapper">
              <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
                <circle cx="14" cy="12" r="6" stroke="var(--color-accent)" stroke-width="2" fill="none"/>
                <circle cx="26" cy="12" r="6" stroke="var(--color-accent)" stroke-width="2" fill="none"/>
                <circle cx="20" cy="24" r="6" stroke="var(--color-accent)" stroke-width="2" fill="none"/>
                <path d="M14 18V19C14 22 18 24 20 24C22 24 26 22 26 19V18"
                      stroke="var(--color-accent)"
                      stroke-width="1.5"
                      stroke-linecap="round"/>
              </svg>
            </div>
            <div class="action-content">
              <h3 class="card-title">团队管理</h3>
              <p class="caption">组建和管理参赛团队</p>
              <div class="action-meta">
                <span class="badge badge-accent">协作空间</span>
              </div>
            </div>
            <div class="action-arrow">
              <svg width="28" height="28" viewBox="0 0 28 28" fill="none">
                <path d="M10 8L18 14L10 20"
                      stroke="var(--color-text-tertiary)"
                      stroke-width="2"
                      stroke-linecap="round"/>
              </svg>
            </div>
          </router-link>

          <!-- Works Link -->
          <router-link to="/student/works" class="action-card card card-interactive card-accent">
            <div class="action-ornament accent"></div>
            <div class="action-icon-wrapper accent">
              <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
                <rect x="6" y="6" width="28" height="28" rx="3" fill="none" stroke="var(--color-accent)" stroke-width="2"/>
                <line x1="10" y1="12" x2="30" y2="12" stroke="var(--color-accent)" stroke-width="1.5" stroke-linecap="round"/>
                <line x1="10" y1="16" x2="30" y2="16" stroke="var(--color-accent)" stroke-width="1.5" stroke-linecap="round"/>
                <line x1="10" y1="20" x2="22" y2="20" stroke="var(--color-accent)" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="action-content">
              <h3 class="card-title accent-text">作品中心</h3>
              <p class="caption">提交作品、查看评审结果</p>
              <div class="action-meta">
                <span class="badge badge-awarded">核心功能</span>
              </div>
            </div>
            <div class="action-arrow accent">
              <svg width="28" height="28" viewBox="0 0 28 28" fill="none">
                <path d="M10 8L18 14L10 20"
                      stroke="var(--color-accent)"
                      stroke-width="2"
                      stroke-linecap="round"/>
              </svg>
            </div>
          </router-link>

          <!-- Invitations Link -->
          <router-link to="/student/invitations" class="action-card card card-interactive">
            <div class="action-ornament"></div>
            <div class="action-icon-wrapper">
              <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
                <circle cx="20" cy="12" r="8" stroke="var(--color-accent)" stroke-width="2" fill="none"/>
                <path d="M10 32C10 26 16 22 20 22C24 22 30 26 30 32"
                      stroke="var(--color-accent)"
                      stroke-width="2"
                      stroke-linecap="round"/>
                <path d="M30 12L34 12M34 12L34 8M34 12L34 16"
                      stroke="var(--color-accent)"
                      stroke-width="2"
                      stroke-linecap="round"/>
              </svg>
            </div>
            <div class="action-content">
              <h3 class="card-title">邀请管理</h3>
              <p class="caption">处理团队邀请与申请</p>
              <div class="action-meta">
                <span class="badge badge-pending">待处理</span>
              </div>
            </div>
            <div class="action-arrow">
              <svg width="28" height="28" viewBox="0 0 28 28" fill="none">
                <path d="M10 8L18 14L10 20"
                      stroke="var(--color-text-tertiary)"
                      stroke-width="2"
                      stroke-linecap="round"/>
              </svg>
            </div>
          </router-link>

          <!-- Profile Link -->
          <router-link to="/student/profile" class="action-card card card-interactive">
            <div class="action-ornament"></div>
            <div class="action-icon-wrapper">
              <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
                <circle cx="20" cy="14" r="8" stroke="var(--color-accent)" stroke-width="2" fill="none"/>
                <path d="M10 34C10 28 16 24 20 24C24 24 30 28 30 34"
                      stroke="var(--color-accent)"
                      stroke-width="2"
                      stroke-linecap="round"/>
              </svg>
            </div>
            <div class="action-content">
              <h3 class="card-title">个人设置</h3>
              <p class="caption">修改个人信息与偏好</p>
              <div class="action-meta">
                <span class="badge badge-secondary">设置</span>
              </div>
            </div>
            <div class="action-arrow">
              <svg width="28" height="28" viewBox="0 0 28 28" fill="none">
                <path d="M10 8L18 14L10 20"
                      stroke="var(--color-text-tertiary)"
                      stroke-width="2"
                      stroke-linecap="round"/>
              </svg>
            </div>
          </router-link>
        </div>
      </div>

      <!-- Academic Motivational Footer -->
      <div class="motivation-section fade-in">
        <div class="motivation-card card">
          <div class="motivation-icon">
            <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
              <!-- Book -->
              <path d="M6 6H26V26H6V6Z" fill="none" stroke="var(--color-accent)" stroke-width="1.5"/>
              <path d="M6 6L16 6L16 26L6 26" fill="var(--color-accent)" opacity="0.1"/>
              <line x1="8" y1="10" x2="14" y2="10" stroke="var(--color-accent)" stroke-width="1" stroke-linecap="round"/>
              <line x1="8" y1="14" x2="14" y2="14" stroke="var(--color-accent)" stroke-width="1" stroke-linecap="round"/>
              <line x1="8" y1="18" x2="14" y2="18" stroke="var(--color-accent)" stroke-width="1" stroke-linecap="round"/>
            </svg>
          </div>
          <div class="motivation-text">
            <p class="body-text">"创新引领未来，实践成就梦想"</p>
            <p class="caption mt-md">积极参与竞赛，展示创意才华</p>
          </div>
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

const userName = computed(() => localStorage.getItem('userName') || '同学')

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
    stats.value = data
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
  position: relative;
  display: flex;
  align-items: center;
  gap: var(--spacing-2xl);
  margin-bottom: var(--spacing-3xl);
  padding: var(--spacing-2xl);
  background: var(--color-bg-card);
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-card);
}

.academic-crest {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  filter: drop-shadow(0 0 12px rgba(52, 152, 219, 0.3));
}

.header-content {
  flex: 1;
}

.header-subtitle {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  margin-top: var(--spacing-md);
}

.user-name {
  font-weight: 600;
  color: var(--color-text-primary);
}

.separator {
  color: var(--color-text-tertiary);
}

.status {
  color: var(--color-text-secondary);
}

.header-decoration {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg,
    var(--color-accent) 0%,
    var(--color-primary-light) 50%,
    transparent 100%);
  border-radius: var(--radius-sm);
}

.section-header {
  margin-bottom: var(--spacing-xl);
}

.section-divider {
  height: 2px;
  background: linear-gradient(90deg, var(--color-accent) 0%, transparent 100%);
  margin-top: var(--spacing-md);
}

.stats-section {
  margin-bottom: var(--spacing-3xl);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-xl);
}

.stat-card {
  position: relative;
  overflow: hidden;
  padding: var(--spacing-2xl);
}

.stat-ornament {
  position: absolute;
  top: 0;
  right: 0;
  width: 60px;
  height: 60px;
  background: radial-gradient(circle at top right,
    rgba(52, 152, 219, 0.08) 0%,
    transparent 70%);
  border-radius: 0 var(--radius-xl) 0 0;
}

.stat-ornament.accent {
  background: radial-gradient(circle at top right,
    rgba(52, 152, 219, 0.15) 0%,
    transparent 70%);
}

.stat-card.card-accent {
  border-left: 4px solid var(--color-accent);
  box-shadow: var(--shadow-accent);
}

.stat-card:hover .stat-ornament {
  background: radial-gradient(circle at top right,
    rgba(52, 152, 219, 0.12) 0%,
    transparent 70%);
}

.stat-icon {
  margin-bottom: var(--spacing-lg);
  opacity: 0.85;
}

.stat-number {
  font-family: var(--font-display);
  font-size: 52px;
  font-weight: 700;
  color: var(--color-primary);
  line-height: 1;
  margin-bottom: var(--spacing-md);
  letter-spacing: -0.05em;
  transition: color var(--transition-fast);
}

.stat-card:hover .stat-number {
  color: var(--color-accent);
}

.accent-text {
  color: var(--color-accent);
}

.stat-label {
  font-family: var(--font-mono);
  font-size: var(--text-xs);
  color: var(--color-text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.12em;
  margin-bottom: var(--spacing-lg);
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding-top: var(--spacing-md);
  border-top: 1px solid var(--color-border-light);
}

.trend-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--status-pending);
}

.trend-indicator.active {
  background: var(--color-accent);
  box-shadow: 0 0 8px rgba(52, 152, 219, 0.4);
}

.trend-indicator.accent {
  background: var(--color-accent);
  animation: pulse 2s ease infinite;
}

.trend-indicator.success {
  background: var(--status-completed);
}

.actions-section {
  margin-bottom: var(--spacing-3xl);
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-xl);
}

.action-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: var(--spacing-xl);
  padding: var(--spacing-2xl);
  text-decoration: none;
  border-left: 4px solid transparent;
  overflow: hidden;
}

.action-ornament {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(90deg,
    transparent 0%,
    rgba(52, 152, 219, 0.08) 50%,
    transparent 100%);
}

.action-ornament.accent {
  background: linear-gradient(90deg,
    var(--color-accent) 0%,
    rgba(52, 152, 219, 0.15) 50%,
    transparent 100%);
}

.action-card.card-accent {
  border-left-color: var(--color-accent);
}

.action-card:hover {
  border-left-color: var(--color-accent);
  transform: translateX(4px);
}

.action-icon-wrapper {
  width: 72px;
  height: 72px;
  background: rgba(52, 152, 219, 0.06);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all var(--transition-base);
}

.action-icon-wrapper.accent {
  background: rgba(52, 152, 219, 0.12);
  box-shadow: var(--shadow-accent);
}

.action-card:hover .action-icon-wrapper {
  background: rgba(52, 152, 219, 0.10);
  transform: scale(1.05);
}

.action-content {
  flex: 1;
}

.action-meta {
  margin-top: var(--spacing-md);
}

.badge-accent {
  background: rgba(52, 152, 219, 0.12);
  color: var(--color-accent);
  border: 1px solid rgba(52, 152, 219, 0.25);
}

.action-arrow {
  opacity: 0.3;
  transition: all var(--transition-fast);
  flex-shrink: 0;
}

.action-arrow.accent {
  opacity: 0.6;
}

.action-card:hover .action-arrow {
  opacity: 1;
  transform: translateX(6px);
}

.motivation-section {
  margin-top: var(--spacing-3xl);
}

.motivation-card {
  display: flex;
  align-items: center;
  gap: var(--spacing-2xl);
  padding: var(--spacing-2xl);
  background: linear-gradient(135deg,
    rgba(52, 152, 219, 0.05) 0%,
    rgba(13, 33, 55, 0.03) 100%);
  border: 1px solid rgba(52, 152, 219, 0.15);
  border-radius: var(--radius-xl);
}

.motivation-icon {
  opacity: 0.8;
}

.motivation-text {
  flex: 1;
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    text-align: center;
    gap: var(--spacing-lg);
  }

  .header-subtitle {
    flex-direction: column;
    gap: var(--spacing-sm);
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .actions-grid {
    grid-template-columns: 1fr;
  }

  .stat-number {
    font-size: 40px;
  }

  .action-card {
    flex-direction: column;
    text-align: center;
    gap: var(--spacing-lg);
    padding: var(--spacing-xl);
  }

  .action-arrow {
    display: none;
  }

  .motivation-card {
    flex-direction: column;
    text-align: center;
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.15);
    opacity: 0.9;
  }
}
</style>