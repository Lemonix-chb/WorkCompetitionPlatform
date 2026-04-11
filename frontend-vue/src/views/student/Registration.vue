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
        <div v-else-if="competitions.length > 0" class="grid grid-auto">
          <div v-for="comp in competitions" :key="comp.id" class="competition-card card card-hover">
            <!-- Header -->
            <div class="card-header">
              <h3 class="card-title">{{ comp.competitionName }}</h3>
              <span :class="getStatusClass(comp.status)" class="badge">
                {{ getStatusText(comp.status) }}
              </span>
            </div>

            <!-- Description -->
            <p class="body-text mb-lg">{{ comp.description }}</p>

            <!-- Info -->
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
                <span class="info-label caption">报名截止</span>
                <span class="info-value caption">{{ formatDate(comp.registrationEnd) }}</span>
              </div>
            </div>

            <!-- Action -->
            <div class="card-footer">
              <button
                v-if="isRegistered(comp)"
                class="btn-secondary"
                disabled
              >
                <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                  <circle cx="10" cy="10" r="8" stroke="currentColor" stroke-width="2"/>
                  <path d="M7 10L9 12L13 8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                已报名
              </button>
              <button
                v-else-if="canRegister(comp)"
                class="btn-primary"
                @click="handleRegister(comp)"
              >
                <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                  <rect x="4" y="4" width="12" height="12" rx="2" stroke="currentColor" stroke-width="2"/>
                  <path d="M7 7H13M7 10H13M7 13H10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
                报名参赛
              </button>
              <button v-else class="btn-secondary" disabled>
                已截止报名
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
import { ElMessage } from 'element-plus'
import { useCompetitionStore } from '../../stores/competition'

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

onMounted(async () => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('userRole')

  if (!token || role !== 'STUDENT') {
    ElMessage.warning('您没有权限访问此页面')
    router.push('/login')
    return
  }

  await Promise.all([
    fetchCompetitions(),
    fetchMyRegistrations()
  ])
})

const fetchMyRegistrations = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch('/api/registrations/my', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    const data = await response.json()

    if (data.code === 200) {
      userRegistrations.value = data.data || []
    }
  } catch (error) {
    console.error('获取报名记录失败', error)
  }
}

const fetchTracks = async (competitionId) => {
  try {
    const response = await fetch(`/api/competitions/${competitionId}/tracks`)
    const data = await response.json()

    if (data.code === 200) {
      tracks.value = data.data || []
    } else {
      ElMessage.error('获取赛道失败')
    }
  } catch (error) {
    ElMessage.error('获取赛道失败')
  }
}

const fetchUserTeams = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch('/api/teams/my', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    const data = await response.json()

    if (data.code === 200) {
      userTeams.value = data.data || []
    } else {
      ElMessage.error('获取团队失败')
    }
  } catch (error) {
    ElMessage.error('获取团队失败')
  }
}

const handleRegister = async (comp) => {
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
    ElMessage.warning('请选择赛道和团队')
    return
  }

  registering.value = true

  try {
    const token = localStorage.getItem('token')
    const params = new URLSearchParams({
      competitionId: selectedCompetition.value.id,
      trackId: selectedTrackId.value,
      teamId: selectedTeamId.value
    })

    const response = await fetch(`/api/registrations?${params.toString()}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })

    const data = await response.json()

    if (data.code === 200) {
      ElMessage.success('报名成功！')
      showRegistrationDialog.value = false
      await fetchMyRegistrations()
      router.push('/student/teams')
    } else {
      ElMessage.error(data.message || '报名失败')
    }
  } catch (error) {
    ElMessage.error('报名失败，请稍后重试')
  } finally {
    registering.value = false
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

const getStatusClass = (status) => {
  return {
    'badge-primary': status === 'PUBLISHED',
    'badge-warning': status === 'ONGOING',
    'badge-gray': status === 'FINISHED' || status === 'DRAFT'
  }
}

const getStatusText = (status) => {
  const texts = {
    DRAFT: '草稿',
    PUBLISHED: '已发布',
    ONGOING: '进行中',
    FINISHED: '已结束'
  }
  return texts[status] || status
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
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const canRegister = (comp) => {
  return comp.status === 'PUBLISHED' || comp.status === 'ONGOING'
}

const isRegistered = (comp) => {
  // 检查用户是否已报名该赛事
  return userRegistrations.value.some(reg => reg.competitionId === comp.id)
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

.card-footer {
  margin-top: var(--spacing-lg);
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

.track-option,
.team-option {
  padding: var(--spacing-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
  border: 2px solid var(--color-border);
  min-height: 120px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.track-option:hover {
  border-color: var(--color-accent);
  transform: translateY(-2px);
  box-shadow: var(--shadow-card);
}

.track-selected,
.team-selected {
  border-color: var(--color-accent);
  background: rgba(90, 127, 168, 0.08);
}

.track-header,
.team-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--spacing-md);
}

.confirmation-box {
  padding: var(--spacing-lg);
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
  .info-grid {
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