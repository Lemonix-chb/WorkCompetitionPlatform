<template>
  <div class="registration-page">
    <div class="container">
      <!-- Page Header -->
      <div class="page-header slide-in">
        <h1 class="page-title">赛事报名</h1>
        <p class="body-text">选择赛事并报名参赛</p>
      </div>

      <!-- Competitions List -->
      <div class="competitions-section scale-in">
        <h2 class="section-title">当前可报名赛事</h2>

        <!-- Loading -->
        <div v-if="loading" class="loading-state">
          <div class="spinner"></div>
        </div>

        <!-- Error -->
        <div v-if="error" class="empty-state card">
          <p class="body-text">{{ error }}</p>
        </div>

        <!-- Competitions Grid -->
        <div v-else-if="competitions.length > 0" class="competitions-grid-apple">
          <div v-for="comp in competitions" :key="comp.id" class="competition-card-apple">
            <!-- Competition Header -->
            <div class="competition-header">
              <div class="competition-title-section">
                <h3 class="competition-name">{{ comp.competitionName }}</h3>
                <div class="competition-year-badge">{{ comp.competitionYear }}</div>
              </div>
              <span :class="getStatusClass(comp)" class="status-badge">
                {{ getStatusText(comp) }}
              </span>
            </div>

            <!-- Countdown Banner (固定高度区域) -->
            <div class="countdown-banner-wrapper">
              <div v-if="getCountdownText(comp)" class="countdown-banner">
                <div class="countdown-icon">
                  <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
                    <circle cx="9" cy="9" r="8" stroke="currentColor" stroke-width="2"/>
                    <path d="M9 5V9L12 11" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  </svg>
                </div>
                <span class="countdown-text">{{ getCountdownText(comp) }}</span>
              </div>
              <div v-else class="countdown-placeholder"></div>
            </div>

            <!-- Competition Stats -->
            <div class="competition-stats">
              <div class="stat-item">
                <div class="stat-icon">
                  <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                    <rect x="4" y="4" width="12" height="12" rx="2" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M7 7H13M7 10H13M7 13H10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                </div>
                <div class="stat-content">
                  <div class="stat-label">主办单位</div>
                  <div class="stat-value">{{ comp.organizer }}</div>
                </div>
              </div>

              <div class="stat-item">
                <div class="stat-icon">
                  <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                    <circle cx="10" cy="10" r="7" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M10 6V10L13 12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                </div>
                <div class="stat-content">
                  <div class="stat-label">报名截止</div>
                  <div class="stat-value">{{ formatDate(comp.registrationEnd) }}</div>
                </div>
              </div>

              <div class="stat-item">
                <div class="stat-icon">
                  <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                    <path d="M10 4H16V16H4V4H10Z" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M10 4V8M8 6H12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                </div>
                <div class="stat-content">
                  <div class="stat-label">赛道数</div>
                  <div class="stat-value">{{ comp.trackCount || 3 }}</div>
                </div>
              </div>
            </div>

            <!-- Description (固定高度，截断) -->
            <div class="competition-description">
              <p class="description-text">{{ comp.description || '暂无描述' }}</p>
            </div>

            <!-- Action Buttons (固定高度) -->
            <div class="competition-actions">
              <!-- 已报名 -->
              <div v-if="isRegistered(comp)" class="action-btn-group">
                <span class="action-btn-registered">
                  <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
                    <circle cx="9" cy="9" r="6" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M6 9L8 11L12 7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  <span>已报名</span>
                </span>
                <button class="action-btn-withdraw" @click="handleWithdraw(comp)" :disabled="withdrawing">
                  退赛
                </button>
              </div>

              <!-- 可报名 -->
              <button v-else-if="canRegister(comp)" class="action-btn-primary" @click="handleRegister(comp)">
                <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
                  <rect x="4" y="4" width="10" height="10" rx="2" stroke="currentColor" stroke-width="1.5"/>
                  <path d="M7 7H11M7 9H11M7 11H9" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
                <span>报名参赛</span>
              </button>

              <!-- 已截止 -->
              <button v-else class="action-btn-disabled" disabled>
                <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
                  <circle cx="9" cy="9" r="6" stroke="currentColor" stroke-width="1.5"/>
                  <path d="M6 6L12 12M12 6L6 12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
                <span>报名已截止</span>
              </button>
            </div>
          </div>
        </div>

        <!-- Empty -->
        <div v-else class="empty-state card">
          <div class="empty-icon">
            <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
              <rect x="8" y="8" width="32" height="28" rx="3" stroke="#bdc3c7" stroke-width="2"/>
              <path d="M12 16H36M12 20H36M12 24H28" stroke="#bdc3c7" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <h3 class="card-title mb-md">暂无可报名赛事</h3>
          <p class="caption">等待新赛事发布</p>
        </div>
      </div>

      <!-- Registration Dialog -->
      <el-dialog
        v-model="showRegistrationDialog"
        title="赛事报名"
        width="600px"
      >
        <div v-if="selectedCompetition" class="registration-steps">
          <!-- Step 1: Select Track -->
          <div class="step-section mb-xl">
            <h3 class="section-title">选择赛道</h3>
            <div class="tracks-grid">
              <div
                v-for="track in tracks"
                :key="track.id"
                class="track-option card"
                :class="{ 'track-selected': selectedTrackId === track.id }"
                @click="selectTrack(track.id)"
              >
                <div class="track-header">
                  <h4 class="card-title">{{ track.trackName }}</h4>
                  <svg v-if="selectedTrackId === track.id" width="20" height="20" viewBox="0 0 20 20" fill="none">
                    <circle cx="10" cy="10" r="8" stroke="#5a7fa8" stroke-width="2"/>
                    <path d="M7 10L9 12L13 8" stroke="#5a7fa8" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </div>
                <p class="caption">{{ track.description || '暂无描述' }}</p>
              </div>
            </div>
          </div>

          <!-- Step 2: Select Team -->
          <div v-if="selectedTrackId" class="step-section mb-xl">
            <h3 class="section-title">选择团队</h3>
            <div v-if="userTeams.length === 0" class="empty-state card mb-lg">
              <p class="caption">您还没有团队，请先创建团队</p>
              <router-link to="/student/teams" class="btn-primary">
                创建团队
              </router-link>
            </div>
            <div v-else class="teams-grid">
              <div
                v-for="team in userTeams"
                :key="team.id"
                class="team-option card"
                :class="{ 'team-selected': selectedTeamId === team.id }"
                @click="selectTeam(team.id)"
              >
                <div class="team-header">
                  <h4 class="card-title">{{ team.teamName }}</h4>
                  <svg v-if="selectedTeamId === team.id" width="20" height="20" viewBox="0 0 20 20" fill="none">
                    <circle cx="10" cy="10" r="8" stroke="#5a7fa8" stroke-width="2"/>
                    <path d="M7 10L9 12L13 8" stroke="#5a7fa8" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </div>
                <p class="caption">成员：{{ team.currentMemberCount }}/{{ team.maxMemberCount }}</p>
                <p class="caption">状态：{{ getTeamStatusText(team.status) }}</p>
              </div>
            </div>
          </div>

          <!-- Confirmation -->
          <div v-if="selectedTrackId && selectedTeamId" class="step-section">
            <h3 class="section-title">确认报名</h3>
            <div class="confirmation-box card">
              <div class="info-row">
                <span class="info-label caption">赛事</span>
                <span class="info-value body-text">{{ selectedCompetition.competitionName }}</span>
              </div>
              <div class="info-row">
                <span class="info-label caption">赛道</span>
                <span class="info-value body-text">{{ getSelectedTrackName() }}</span>
              </div>
              <div class="info-row">
                <span class="info-label caption">团队</span>
                <span class="info-value body-text">{{ getSelectedTeamName() }}</span>
              </div>
            </div>
          </div>
        </div>

        <template #footer>
          <div class="dialog-footer">
            <el-button @click="showRegistrationDialog = false">取消</el-button>
            <el-button
              type="primary"
              @click="confirmRegistration"
              :disabled="!selectedTrackId || !selectedTeamId"
              :loading="registering"
            >
              确认报名
            </el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useCompetitionStore } from '../../stores/competition'
import { get, post, del } from '@/utils/api'
import { showSuccess, showError, showWarning } from '@/utils/messageUtils'
import { formatDateTime } from '@/utils/dateUtils'

const router = useRouter()
const store = useCompetitionStore()

const { competitions, loading, error, fetchCompetitions } = store

const tracks = ref([])
const userTeams = ref([])
const userRegistrations = ref([])
const selectedCompetition = ref(null)
const selectedTrackId = ref(null)
const selectedTeamId = ref(null)
const showRegistrationDialog = ref(false)
const registering = ref(false)
const withdrawing = ref(false)
const timeStatusMap = ref({})

onMounted(async () => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('userRole')

  if (!token || role !== 'STUDENT') {
    showWarning('您没有权限访问此页面')
    router.push('/login')
    return
  }

  await Promise.all([
    fetchCompetitions(),
    fetchMyRegistrations()
  ])

  await fetchAllTimeStatuses()
})

const fetchAllTimeStatuses = async () => {
  if (!competitions.value || competitions.value.length === 0) {
    console.log('No competitions available, time status check skipped')
    // 不显示警告，只在控制台记录日志
    return
  }

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

const fetchMyRegistrations = async () => {
  try {
    const data = await get('/registrations/my')
    userRegistrations.value = data || []
  } catch (error) {
    console.error('获取报名记录失败', error)
  }
}

const fetchTracks = async (competitionId) => {
  try {
    const data = await get(`/competitions/${competitionId}/tracks`)
    tracks.value = data || []
  } catch (error) {
    showError('获取赛道失败')
  }
}

const fetchUserTeams = async () => {
  try {
    const data = await get('/teams/my')
    userTeams.value = data || []
  } catch (error) {
    showError('获取团队失败')
  }
}

const handleRegister = async (comp) => {
  const timeStatus = timeStatusMap.value[comp.id]
  if (timeStatus && !timeStatus.canRegister) {
    const endTime = formatDateTime(timeStatus.registrationEnd)
    showWarning(`报名已截止，截止时间：${endTime}`)
    return
  }

  selectedCompetition.value = comp
  selectedTrackId.value = null
  selectedTeamId.value = null

  await fetchTracks(comp.id)
  await fetchUserTeams()

  showRegistrationDialog.value = true
}

const selectTrack = (trackId) => {
  selectedTrackId.value = trackId
  selectedTeamId.value = null // Reset team selection
}

const selectTeam = (teamId) => {
  selectedTeamId.value = teamId
}

const confirmRegistration = async () => {
  if (!selectedTrackId.value || !selectedTeamId.value) {
    showWarning('请选择赛道和团队')
    return
  }

  registering.value = true

  try {
    const params = new URLSearchParams({
      competitionId: selectedCompetition.value.id,
      trackId: selectedTrackId.value,
      teamId: selectedTeamId.value
    })

    await post(`/registrations?${params.toString()}`)
    showSuccess('报名成功！')
    showRegistrationDialog.value = false
    await fetchMyRegistrations()
    router.push('/student/teams')
  } catch (error) {
    showError('报名失败，请稍后重试')
  } finally {
    registering.value = false
  }
}

const handleWithdraw = async (comp) => {
  try {
    await ElMessageBox.confirm(
      `确定要退出「${comp.competitionName}」吗？退赛后需要重新报名才能参赛。`,
      '退赛确认',
      { confirmButtonText: '确定退赛', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }

  withdrawing.value = true
  try {
    await del(`/registrations?competitionId=${comp.id}`)
    showSuccess('退赛成功')
    await fetchMyRegistrations()
  } catch (error) {
    showError('退赛失败，请稍后重试')
  } finally {
    withdrawing.value = false
  }
}

const getSelectedTrackName = () => {
  const track = tracks.value.find(t => t.id === selectedTrackId.value)
  return track ? track.trackName : ''
}

const getSelectedTeamName = () => {
  const team = userTeams.value.find(t => t.id === selectedTeamId.value)
  return team ? team.teamName : ''
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

const getTeamStatusText = (status) => {
  const texts = {
    FORMING: '组建中',
    CONFIRMED: '已确认',
    REGISTERED: '已报名',
    SUBMITTED: '已提交',
    REVIEWED: '已评审',
    AWARDED: '已获奖'
  }
  return texts[status] || status
}

const formatDate = (dateStr) => {
  return formatDateTime(dateStr)
}

const getCountdownText = (comp) => {
  const timeStatus = timeStatusMap.value[comp.id]
  if (!timeStatus) return ''

  if (timeStatus.currentPhase === 'REGISTRATION' && timeStatus.registrationDaysRemaining) {
    return `报名剩余 ${timeStatus.registrationDaysRemaining} 天`
  }
  return ''
}

const canRegister = (comp) => {
  const timeStatus = timeStatusMap.value[comp.id]
  if (!timeStatus) {
    return comp.status === 'PUBLISHED' || comp.status === 'ONGOING'
  }
  return timeStatus.canRegister
}

const isRegistered = (comp) => {
  return userRegistrations.value.some(
    reg => reg.competitionId === comp.id && reg.status !== 'CANCELLED'
  )
}
</script>

<style scoped>
.registration-page {
  min-height: 100vh;
}

.page-header {
  margin-bottom: var(--spacing-2xl);
}

.competitions-section {
  animation-delay: 0.1s;
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

/* Apple-style Competition Cards Grid */
.competitions-grid-apple {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: var(--spacing-xl);
}

.competition-card-apple {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(245, 245, 247, 0.92) 100%);
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 20px;
  padding: var(--spacing-xl);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  min-height: 380px;
}

.competition-card-apple:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 113, 227, 0.15);
  border-color: rgba(0, 113, 227, 0.3);
}

/* Competition Header */
.competition-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--spacing-md);
  gap: var(--spacing-md);
}

.competition-title-section {
  flex: 1;
  min-width: 0;
}

.competition-name {
  font-size: 22px;
  font-weight: 600;
  color: #000;
  letter-spacing: -0.02em;
  margin: 0 0 8px 0;
  line-height: 1.2;
}

.competition-year-badge {
  display: inline-block;
  padding: 4px 12px;
  background: rgba(0, 113, 227, 0.1);
  color: #0071e3;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 600;
}

.status-badge {
  padding: 6px 14px;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
}

.badge-primary {
  background: linear-gradient(135deg, #0071e3 0%, #0057b8 100%);
  color: white;
}

.badge-accent {
  background: linear-gradient(135deg, #34c759 0%, #2db54e 100%);
  color: white;
}

.badge-warning {
  background: linear-gradient(135deg, #ff9500 0%, #e68600 100%);
  color: white;
}

.badge-gray {
  background: rgba(142, 142, 147, 0.15);
  color: #8e8e93;
}

/* Countdown Banner (Fixed Height) */
.countdown-banner-wrapper {
  width: 100%;
  min-height: 44px;
  margin-bottom: var(--spacing-md);
  display: flex;
  align-items: center;
}

.countdown-banner {
  width: 100%;
  padding: 10px 16px;
  background: linear-gradient(135deg, rgba(255, 149, 0, 0.15) 0%, rgba(255, 149, 0, 0.08) 100%);
  border: 1px solid rgba(255, 149, 0, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #ff9500;
}

.countdown-icon {
  display: flex;
  align-items: center;
}

.countdown-text {
  font-size: 14px;
  font-weight: 600;
  letter-spacing: -0.01em;
}

.countdown-placeholder {
  width: 100%;
  min-height: 44px;
  visibility: hidden;
}

/* Competition Stats (Three Columns) */
.competition-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: rgba(0, 113, 227, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #0071e3;
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 12px;
  color: #8e8e93;
  letter-spacing: 0.01em;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 14px;
  color: #000;
  font-weight: 500;
  letter-spacing: -0.01em;
}

/* Description (Fixed Height) */
.competition-description {
  flex: 1;
  margin-bottom: var(--spacing-lg);
  min-height: 60px;
}

.description-text {
  font-size: 14px;
  color: #3c3c43;
  line-height: 1.5;
  letter-spacing: -0.01em;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* Action Buttons (Fixed Height) */
.competition-actions {
  min-height: 48px;
  display: flex;
  align-items: center;
}

.action-btn-registered,
.action-btn-primary,
.action-btn-disabled {
  padding: 12px 20px;
  border-radius: 980px;
  font-size: 15px;
  font-weight: 500;
  letter-spacing: -0.01em;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.action-btn-registered {
  background: rgba(52, 199, 89, 0.1);
  color: #34c759;
  border: 1px solid rgba(52, 199, 89, 0.3);
}

.action-btn-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.action-btn-group .action-btn-registered {
  padding: 12px 20px;
  border-radius: 980px;
  font-size: 15px;
  font-weight: 500;
  letter-spacing: -0.01em;
  border: 1px solid rgba(52, 199, 89, 0.3);
  background: rgba(52, 199, 89, 0.1);
  color: #34c759;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.action-btn-withdraw {
  padding: 10px 18px;
  border-radius: 980px;
  font-size: 14px;
  font-weight: 500;
  border: 1px solid rgba(255, 59, 48, 0.3);
  background: rgba(255, 59, 48, 0.08);
  color: #ff3b30;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.action-btn-withdraw:hover:not(:disabled) {
  background: rgba(255, 59, 48, 0.15);
  border-color: rgba(255, 59, 48, 0.5);
}

.action-btn-withdraw:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.action-btn-primary {
  background: linear-gradient(135deg, #0071e3 0%, #0057b8 100%);
  color: white;
  cursor: pointer;
}

.action-btn-primary:hover {
  transform: scale(1.02);
  box-shadow: 0 4px 12px rgba(0, 113, 227, 0.3);
}

.action-btn-disabled {
  background: rgba(142, 142, 147, 0.15);
  color: #8e8e93;
  cursor: not-allowed;
  opacity: 0.6;
}

.empty-state {
  padding: var(--spacing-3xl);
  text-align: center;
}

.empty-icon {
  margin-bottom: var(--spacing-lg);
  opacity: 0.5;
}

/* Registration Dialog Styles */
.registration-steps {
  padding: var(--spacing-md);
}

.tracks-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--spacing-lg);
}

.teams-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: var(--spacing-md);
}

/* Track Option Card (Fixed Height) */
.track-option {
  padding: var(--spacing-lg);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid rgba(0, 0, 0, 0.08);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.95);
  height: 140px;  /* 固定高度 */
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  position: relative;
  overflow: hidden;
}

.track-option:hover {
  border-color: rgba(0, 113, 227, 0.3);
  transform: translateY(-3px);
  box-shadow: 0 8px 16px rgba(0, 113, 227, 0.12);
}

.track-selected {
  border-color: #0071e3;
  background: linear-gradient(135deg, rgba(0, 113, 227, 0.08) 0%, rgba(0, 113, 227, 0.04) 100%);
}

/* Team Option Card (Fixed Height) */
.team-option {
  padding: var(--spacing-lg);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid rgba(0, 0, 0, 0.08);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.95);
  height: 140px;  /* 固定高度 */
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  position: relative;
  overflow: hidden;
}

.team-option:hover {
  border-color: rgba(0, 113, 227, 0.3);
  transform: translateY(-3px);
  box-shadow: 0 8px 16px rgba(0, 113, 227, 0.12);
}

.team-selected {
  border-color: #0071e3;
  background: linear-gradient(135deg, rgba(0, 113, 227, 0.08) 0%, rgba(0, 113, 227, 0.04) 100%);
}

.track-header,
.team-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--spacing-md);
  margin-bottom: 12px;
}

.track-header .card-title,
.team-header .card-title {
  font-size: 18px;
  font-weight: 600;
  color: #000;
  letter-spacing: -0.02em;
  margin: 0;
}

/* Track Description (截断显示) */
.track-option .caption {
  font-size: 14px;
  color: #3c3c43;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;  /* 最多显示2行 */
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Team Info (Fixed Layout) */
.team-option .caption {
  font-size: 14px;
  color: #3c3c43;
  line-height: 1.4;
  margin-bottom: 4px;
}

.confirmation-box {
  padding: var(--spacing-lg);
  background: rgba(245, 245, 247, 0.95);
  border-radius: 12px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-sm) 0;
  border-bottom: 1px solid var(--color-border-light);
}

.info-row:last-child {
  border-bottom: none;
}

@media (max-width: 768px) {
  .competitions-grid-apple {
    grid-template-columns: 1fr;
  }

  .competition-stats {
    grid-template-columns: 1fr;
  }

  .tracks-grid {
    grid-template-columns: 1fr;
  }

  .teams-grid {
    grid-template-columns: 1fr;
  }
}
</style>