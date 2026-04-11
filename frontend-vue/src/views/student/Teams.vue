<template>
  <div class="teams-page">
    <div class="container">
      <!-- Page Header -->
      <div class="page-header slide-in">
        <h1 class="page-title">我的团队</h1>
        <p class="body-text">管理参赛团队，协作共创作品</p>
      </div>

      <!-- Tabs -->
      <div class="tabs-section scale-in mb-lg">
        <div class="tabs-wrapper">
          <div class="tabs-group">
            <button
              :class="['tab-btn', { active: activeTab === 'myTeams' }]"
              @click="activeTab = 'myTeams'"
            >
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <circle cx="16" cy="8" r="5" stroke="currentColor" stroke-width="2"/>
                <path d="M4 17C4 14 7 12 10 12C13 12 16 14 16 17" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              我的团队
            </button>
            <button
              :class="['tab-btn', { active: activeTab === 'invitations' }]"
              @click="activeTab = 'invitations'"
            >
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <rect x="4" y="4" width="12" height="12" rx="2" stroke="currentColor" stroke-width="2"/>
                <path d="M7 7H13M7 10H13M7 13H10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              收到的邀请
              <span v-if="pendingInvitationsCount > 0" class="badge badge-primary">{{ pendingInvitationsCount }}</span>
            </button>
            <button
              :class="['tab-btn', { active: activeTab === 'applications' }]"
              @click="activeTab = 'applications'"
            >
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <circle cx="10" cy="8" r="5" stroke="currentColor" stroke-width="2"/>
                <path d="M4 17C4 14 7 12 10 12C13 12 16 14 16 17" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <path d="M16 8L19 8M19 8L19 5M19 8L19 11" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              申请加入
            </button>
          </div>
          <button v-if="activeTab === 'myTeams'" class="btn-primary tab-action-btn" @click="showCreateDialog">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M10 4V16M4 10H16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            创建新团队
          </button>
        </div>
      </div>

      <!-- My Teams Tab -->
      <div v-show="activeTab === 'myTeams'" class="teams-section">
        <!-- Teams List -->
        <div v-if="teams.length === 0" class="empty-state card">
          <div class="empty-icon">
            <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
              <circle cx="16" cy="16" r="8" stroke="#bdc3c7" stroke-width="2"/>
              <circle cx="32" cy="16" r="8" stroke="#bdc3c7" stroke-width="2"/>
              <circle cx="24" cy="32" r="8" stroke="#bdc3c7" stroke-width="2"/>
              <path d="M16 24V25C16 26.5 19 28 24 28C29 28 32 26.5 32 25V24" stroke="#bdc3c7" stroke-width="2"/>
            </svg>
          </div>
          <h3 class="card-title mb-md">暂无团队信息</h3>
          <p class="caption">创建团队开始参赛吧</p>
        </div>

        <div v-else class="grid grid-auto">
          <div v-for="team in teams" :key="team.id" class="team-card card card-hover">
            <!-- Team Header -->
            <div class="team-header">
              <h3 class="card-title">{{ team.teamName }}</h3>
              <span :class="getTeamStatusClass(team.status)" class="badge">
                {{ getTeamStatusText(team.status) }}
              </span>
            </div>

            <!-- Team Info -->
            <div class="team-info">
              <div class="info-row">
                <span class="info-label caption">赛道</span>
                <span class="info-value body-text">{{ getTrackName(team) }}</span>
              </div>
              <div class="info-row">
                <span class="info-label caption">成员</span>
                <span class="info-value body-text">{{ team.currentMemberCount }} / {{ team.maxMemberCount }}</span>
              </div>
              <div class="info-row">
                <span class="info-label caption">团队编号</span>
                <span class="info-value caption">{{ team.teamCode }}</span>
              </div>
            </div>

            <!-- Team Actions -->
            <div class="team-actions flex gap-md">
              <!-- 队长操作 -->
              <button v-if="isTeamLeader(team)" class="btn-primary" @click="manageTeam(team)">管理团队</button>
              <button v-if="isTeamLeader(team)" class="btn-secondary" @click="showTeamApplications(team)">
                <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                  <rect x="4" y="4" width="12" height="12" rx="2" stroke="currentColor" stroke-width="2"/>
                  <path d="M7 7H13M7 10H13M7 13H10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
                处理申请
                <span v-if="team.pendingApplicationsCount > 0" class="badge badge-primary ml-sm">{{ team.pendingApplicationsCount }}</span>
              </button>
              <button
                v-if="isTeamLeader(team) && team.currentMemberCount < team.maxMemberCount && team.status === 'FORMING'"
                class="btn-secondary"
                @click="openInviteDialog(team)"
              >
                <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                  <circle cx="10" cy="8" r="5" stroke="currentColor" stroke-width="2"/>
                  <path d="M4 17C4 14 7 12 10 12C13 12 16 14 16 17" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  <path d="M16 8L19 8M19 8L19 5M19 8L19 11" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
                邀请成员
              </button>

              <!-- 队员操作 -->
              <button v-if="!isTeamLeader(team)" class="btn-primary" @click="viewTeam(team)">查看团队</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Invitations Tab -->
      <div v-show="activeTab === 'invitations'" class="invitations-section">
        <!-- Invitations List -->
        <div v-if="invitations.length === 0" class="empty-state card">
          <div class="empty-icon">
            <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
              <rect x="8" y="8" width="32" height="28" rx="3" stroke="#bdc3c7" stroke-width="2"/>
              <path d="M8 16H40M16 24H32M16 28H28" stroke="#bdc3c7" stroke-width="2" stroke-linecap="round"/>
              <circle cx="40" cy="12" r="6" fill="#bdc3c7" stroke="none"/>
            </svg>
          </div>
          <h3 class="card-title mb-md">暂无团队邀请</h3>
          <p class="caption">等待其他团队邀请你加入吧</p>
        </div>

        <div v-else class="grid grid-auto">
          <div v-for="invitation in invitations" :key="invitation.id" class="invitation-card card card-flat">
            <!-- Invitation Header -->
            <div class="invitation-header">
              <h3 class="card-title">{{ invitation.teamName || '团队邀请' }}</h3>
              <span :class="getInvitationStatusClass(invitation.status)" class="badge">
                {{ getInvitationStatusText(invitation.status) }}
              </span>
            </div>

            <!-- Invitation Info -->
            <div class="invitation-info">
              <div class="info-row">
                <span class="info-label caption">团队名称</span>
                <span class="info-value body-text">{{ invitation.teamName || '未知团队' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label caption">邀请人</span>
                <span class="info-value body-text">{{ invitation.inviterName || '未知' }} ({{ invitation.inviterNo || '无编号' }})</span>
              </div>
              <div class="info-row">
                <span class="info-label caption">过期时间</span>
                <span class="info-value caption">{{ formatDateTime(invitation.expireTime) }}</span>
              </div>
            </div>

            <!-- Invitation Actions -->
            <div v-if="invitation.status === 'PENDING'" class="invitation-actions flex gap-md">
              <button class="btn-primary" @click="acceptInvitation(invitation)">
                <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                  <circle cx="10" cy="10" r="8" stroke="currentColor" stroke-width="2"/>
                  <path d="M7 10L9 12L13 8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                接受邀请
              </button>
              <button class="btn-secondary" @click="rejectInvitation(invitation)">
                拒绝
              </button>
            </div>

            <!-- Processed Status -->
            <div v-else class="invitation-processed caption">
              {{ invitation.status === 'ACCEPTED' ? '已接受' : '已拒绝' }} · {{ formatDateTime(invitation.processTime) }}
            </div>
          </div>
        </div>
      </div>

      <!-- Applications Tab -->
      <div v-show="activeTab === 'applications'" class="applications-section">
        <!-- Search Section -->
        <div class="search-section card card-flat mb-lg">
          <h3 class="section-title mb-md">搜索团队申请加入</h3>
          <div class="search-form flex gap-md">
            <el-input
              v-model="searchTeamKeyword"
              placeholder="输入团队名称搜索"
              clearable
              style="width: 300px"
              @keyup.enter="searchTeams"
            />
            <button class="btn-secondary" @click="searchTeams">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <circle cx="10" cy="10" r="8" stroke="currentColor" stroke-width="2"/>
                <path d="M10 6V10L13 13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              搜索
            </button>
          </div>
        </div>

        <!-- Search Results -->
        <div v-if="searchedTeams.length > 0" class="search-results mb-lg">
          <h3 class="result-title section-title mb-md">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <circle cx="10" cy="10" r="8" stroke="currentColor" stroke-width="2"/>
              <path d="M10 6V10L13 13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            搜索结果（找到 {{ searchedTeams.length }} 个团队）
          </h3>
          <div class="grid grid-auto">
            <div v-for="team in searchedTeams" :key="team.id" class="team-result-card card card-hover">
              <!-- Team Header -->
              <div class="team-header">
                <h3 class="card-title">{{ team.teamName }}</h3>
                <span :class="getTeamStatusClass(team.status)" class="badge">
                  {{ getTeamStatusText(team.status) }}
                </span>
              </div>

              <!-- Team Brief Info -->
              <div class="team-brief-info mt-md">
                <div class="brief-item">
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <circle cx="8" cy="5" r="3" stroke="#5a7fa8" stroke-width="1.5"/>
                    <path d="M2 13C2 10 5 8 8 8C11 8 14 10 14 13" stroke="#5a7fa8" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                  <span class="body-text">成员 {{ team.currentMemberCount }}/{{ team.maxMemberCount }}</span>
                </div>
                <div class="brief-item" v-if="team.leaderName">
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <circle cx="8" cy="8" r="6" stroke="#5a7fa8" stroke-width="1.5"/>
                    <path d="M8 5V8M8 11V11" stroke="#5a7fa8" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                  <span class="body-text">队长：{{ team.leaderName }}</span>
                </div>
                <div class="brief-item" v-if="team.teamCode">
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <rect x="3" y="3" width="10" height="10" rx="2" stroke="#5a7fa8" stroke-width="1.5"/>
                    <path d="M5 7H11M5 9H9" stroke="#5a7fa8" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                  <span class="caption">团队编号：{{ team.teamCode }}</span>
                </div>
              </div>

              <!-- Apply Action -->
              <div class="team-actions mt-lg">
                <button
                  class="btn-primary"
                  @click="applyToTeam(team)"
                  v-if="team.currentMemberCount < team.maxMemberCount && team.status === 'FORMING'"
                >
                  <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                    <circle cx="10" cy="8" r="5" stroke="currentColor" stroke-width="2"/>
                    <path d="M4 17C4 14 7 12 10 12C13 12 16 14 16 17" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    <path d="M16 8L19 8M19 8L19 5M19 8L19 11" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  </svg>
                  申请加入
                </button>
                <button v-else class="btn-secondary" disabled>
                  团队已满或已锁定
                </button>
              </div>
            </div>
          </div>
        </div>
        <div v-else-if="searchTeamKeyword && searchedTeams.length === 0" class="empty-result card mb-lg">
          <p class="caption text-center">未找到名称包含 "{{ searchTeamKeyword }}" 的团队</p>
        </div>

        <!-- My Applications -->
        <div v-if="myApplications.length > 0" class="my-applications">
          <h3 class="section-title mb-md">我的申请</h3>
          <div class="grid grid-auto">
            <div v-for="application in myApplications" :key="application.id" class="application-card card">
              <!-- Application Header -->
              <div class="application-header">
                <h3 class="card-title">{{ application.teamName }}</h3>
                <span :class="getApplicationStatusClass(application.status)" class="badge">
                  {{ getApplicationStatusText(application.status) }}
                </span>
              </div>

              <!-- Application Info -->
              <div class="application-info">
                <div class="info-row">
                  <span class="info-label caption">申请时间</span>
                  <span class="info-value caption">{{ formatDateTime(application.createTime) }}</span>
                </div>
              </div>

              <!-- Cancel Action -->
              <div v-if="application.status === 'PENDING'" class="application-actions">
                <button class="btn-secondary" @click="cancelApplication(application)">
                  取消申请
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Empty State -->
        <div v-if="searchedTeams.length === 0 && myApplications.length === 0" class="empty-state card">
          <div class="empty-icon">
            <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
              <circle cx="16" cy="16" r="8" stroke="#bdc3c7" stroke-width="2"/>
              <circle cx="32" cy="16" r="8" stroke="#bdc3c7" stroke-width="2"/>
              <circle cx="24" cy="32" r="8" stroke="#bdc3c7" stroke-width="2"/>
              <path d="M16 24V25C16 26.5 19 28 24 28C29 28 32 26.5 32 25V24" stroke="#bdc3c7" stroke-width="2"/>
            </svg>
          </div>
          <h3 class="card-title mb-md">搜索团队申请加入</h3>
          <p class="caption">输入团队名称搜索并申请加入</p>
        </div>
      </div>

      <!-- Create Team Dialog -->
      <el-dialog
        v-model="showCreateTeamDialog"
        title="创建新团队"
        width="480px"
      >
        <el-form :model="createTeamForm" label-width="100px">
          <el-form-item label="团队名称" required>
            <el-input
              v-model="createTeamForm.teamName"
              placeholder="请输入团队名称"
            />
          </el-form-item>
          <el-form-item label="选择赛道" required>
            <el-select
              v-model="createTeamForm.competitionTrackId"
              placeholder="请选择参赛赛道"
            >
              <el-option
                v-for="track in tracks"
                :key="track.id"
                :label="track.trackName"
                :value="track.id"
              />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="showCreateTeamDialog = false">取消</el-button>
            <el-button type="primary" @click="handleCreateTeam" :loading="loading">
              创建团队
            </el-button>
          </div>
        </template>
      </el-dialog>

      <!-- Invite Member Dialog -->
      <el-dialog
        v-model="showInviteMemberDialog"
        title="邀请成员"
        width="500px"
      >
        <div class="invite-steps">
          <!-- Step 1: Search Student -->
          <div class="step-section">
            <h3 class="section-title mb-md">搜索学生</h3>
            <div class="search-form flex gap-md mb-lg">
              <el-input
                v-model="searchStudentKeyword"
                placeholder="输入学号搜索学生"
                clearable
                @keyup.enter="searchStudent"
              />
              <button class="btn-secondary" @click="searchStudent" :loading="searchingStudent">
                搜索
              </button>
            </div>

            <!-- Search Result -->
            <div v-if="foundStudent" class="student-result card mb-lg">
              <h4 class="result-title caption mb-md">搜索结果</h4>
              <div class="student-info">
                <div class="student-avatar">
                  <span class="avatar-text">{{ foundStudent.realName.charAt(0) }}</span>
                </div>
                <div class="student-details">
                  <h4 class="card-title">{{ foundStudent.realName }}</h4>
                  <div class="student-meta caption">
                    <span>学号：{{ foundStudent.studentNo }}</span>
                    <span v-if="foundStudent.major">专业：{{ foundStudent.major }}</span>
                    <span v-if="foundStudent.college">学院：{{ foundStudent.college }}</span>
                  </div>
                </div>
              </div>
              <div class="student-actions mt-lg">
                <button class="btn-primary" @click="selectStudent">
                  选择此学生
                </button>
              </div>
            </div>
            <div v-else-if="searchedStudents.length > 0" class="students-list mb-lg">
              <h4 class="result-title caption mb-md">搜索结果（{{ searchedStudents.length }} 人）</h4>
              <div v-for="student in searchedStudents" :key="student.id" class="student-option card">
                <div class="student-info">
                  <div class="student-avatar">
                    <span class="avatar-text">{{ student.realName.charAt(0) }}</span>
                  </div>
                  <div class="student-details">
                    <h4 class="card-title">{{ student.realName }}</h4>
                    <div class="student-meta caption">
                      <span>学号：{{ student.studentNo }}</span>
                      <span v-if="student.major">专业：{{ student.major }}</span>
                      <span v-if="student.college">学院：{{ student.college }}</span>
                    </div>
                  </div>
                </div>
                <button class="btn-secondary" @click="selectFoundStudent(student)">
                  选择
                </button>
              </div>
            </div>
            <div v-else-if="searchStudentKeyword && !searchingStudent" class="empty-result caption">
              未找到学号为 "{{ searchStudentKeyword }}" 的学生
            </div>
          </div>

          <!-- Step 2: Confirm Invitation -->
          <div v-if="selectedStudent" class="step-section">
            <h3 class="section-title mb-md">确认邀请</h3>
            <div class="confirmation-box card">
              <div class="info-row">
                <span class="info-label caption">邀请对象</span>
                <span class="info-value body-text">{{ selectedStudent.realName }}</span>
              </div>
              <div class="info-row">
                <span class="info-label caption">学号</span>
                <span class="info-value body-text">{{ selectedStudent.studentNo }}</span>
              </div>
              <div class="info-row">
                <span class="info-label caption">加入团队</span>
                <span class="info-value body-text">{{ invitingTeam?.teamName }}</span>
              </div>
            </div>
          </div>
        </div>

        <template #footer>
          <div class="dialog-footer">
            <el-button @click="closeInviteDialog">取消</el-button>
            <el-button
              type="primary"
              @click="handleInviteMember"
              :disabled="!selectedStudent"
              :loading="inviting"
            >
              发送邀请
            </el-button>
          </div>
        </template>
      </el-dialog>

      <!-- Team Applications Dialog (队长处理申请) -->
      <el-dialog
        v-model="showApplicationsDialog"
        title="处理团队申请"
        width="700px"
      >
        <div v-if="selectedTeam" class="applications-dialog-content">
          <h3 class="section-title mb-lg">{{ selectedTeam.teamName }} - 待处理申请</h3>

          <div v-if="teamApplications.length === 0" class="empty-state card mb-lg">
            <p class="caption text-center">暂无待处理的申请</p>
          </div>

          <div v-else class="applications-list">
            <div v-for="application in teamApplications" :key="application.id" class="application-item card mb-md">
              <div class="applicant-info">
                <div class="applicant-avatar">
                  <span class="avatar-text">{{ getApplicantName(application).charAt(0) }}</span>
                </div>
                <div class="applicant-details">
                  <h4 class="card-title">{{ getApplicantName(application) }}</h4>
                  <div class="applicant-meta caption">
                    <span>学号：{{ application.applicantStudentNo }}</span>
                    <span v-if="application.message">申请理由：{{ application.message }}</span>
                  </div>
                  <div class="applicant-time caption">
                    申请时间：{{ formatDateTime(application.createTime) }}
                  </div>
                </div>
              </div>

              <div class="application-actions flex gap-md mt-lg">
                <button class="btn-primary" @click="acceptApplication(application)" :loading="processingApplication">
                  <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                    <circle cx="10" cy="10" r="8" stroke="currentColor" stroke-width="2"/>
                    <path d="M7 10L9 12L13 8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  接受申请
                </button>
                <button class="btn-secondary" @click="rejectApplication(application)" :loading="processingApplication">
                  拒绝申请
                </button>
              </div>
            </div>
          </div>
        </div>

        <template #footer>
          <div class="dialog-footer">
            <el-button @click="showApplicationsDialog = false">关闭</el-button>
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
import { get, post, del } from '@/utils/api'
import { showSuccess, showError, showWarning, showConfirm } from '@/utils/messageUtils'
import { formatDateTime } from '@/utils/dateUtils'

const router = useRouter()

const activeTab = ref('myTeams')
const teams = ref([])
const tracks = ref([])
const invitations = ref([])
const myApplications = ref([])
const searchedTeams = ref([])
const searchedStudents = ref([])
const foundStudent = ref(null)
const selectedStudent = ref(null)
const invitingTeam = ref(null)
const selectedTeam = ref(null)
const teamApplications = ref([])
const currentUserId = ref(null)

const searchTeamKeyword = ref('')
const searchStudentKeyword = ref('')

const showCreateTeamDialog = ref(false)
const showInviteMemberDialog = ref(false)
const showApplicationsDialog = ref(false)

const createTeamForm = ref({
  teamName: '',
  competitionTrackId: ''
})

const loading = ref(false)
const searchingStudent = ref(false)
const inviting = ref(false)
const processingApplication = ref(false)

const pendingInvitationsCount = computed(() => {
  return invitations.value.filter(inv => inv.status === 'PENDING').length
})

onMounted(async () => {
  // 获取当前用户ID
  const userId = localStorage.getItem('userId')
  if (userId) {
    currentUserId.value = parseInt(userId)
  }

  await Promise.all([
    fetchTeams(),
    fetchTracks(),
    fetchInvitations(),
    fetchMyApplications()
  ])
})

const fetchTeams = async () => {
  try {
    const data = await get('/teams/my')

    if (data.code === 200) {
      teams.value = data.data || []
    } else {
      showError(data.message || '获取团队列表失败')
    }
  } catch (error) {
    showError('获取团队列表失败')
  }
}

const fetchTracks = async () => {
  try {
    const data = await get('/competitions/1/tracks')

    if (data.code === 200) {
      tracks.value = data.data || []
    }
  } catch (error) {
    console.error('获取赛道列表失败', error)
  }
}

const fetchInvitations = async () => {
  try {
    const data = await get('/teams/invitations/pending')

    if (data.code === 200) {
      invitations.value = data.data || []
    }
  } catch (error) {
    console.error('获取邀请列表失败', error)
  }
}

const fetchMyApplications = async () => {
  try {
    const data = await get('/teams/applications/my')

    if (data.code === 200) {
      myApplications.value = data.data || []
    }
  } catch (error) {
    console.error('获取申请列表失败', error)
  }
}

const showCreateDialog = () => {
  createTeamForm.value = { teamName: '', competitionTrackId: '' }
  showCreateTeamDialog.value = true
}

const handleCreateTeam = async () => {
  if (!createTeamForm.value.teamName) {
    showWarning('请输入团队名称')
    return
  }
  if (!createTeamForm.value.competitionTrackId) {
    showWarning('请选择赛道')
    return
  }

  loading.value = true

  try {
    const params = new URLSearchParams({
      teamName: createTeamForm.value.teamName,
      competitionTrackId: createTeamForm.value.competitionTrackId
    })

    const data = await post(`/teams?${params.toString()}`)

    if (data.code === 200) {
      showSuccess('团队创建成功')
      showCreateTeamDialog.value = false
      await fetchTeams()
    } else {
      showError(data.message || '创建团队失败')
    }
  } catch (error) {
    showError('创建团队失败')
  } finally {
    loading.value = false
  }
}

const manageTeam = (team) => {
  router.push(`/student/teams/${team.id}`)
}

const viewTeam = (team) => {
  router.push(`/student/teams/${team.id}`)
}

const isTeamLeader = (team) => {
  return team.leaderId === currentUserId.value
}

const showTeamApplications = async (team) => {
  selectedTeam.value = team
  await fetchTeamApplications(team.id)
  showApplicationsDialog.value = true
}

const fetchTeamApplications = async (teamId) => {
  try {
    const data = await get(`/teams/${teamId}/applications/pending`)

    if (data.code === 200) {
      teamApplications.value = data.data || []
    } else {
      showError(data.message || '获取申请列表失败')
    }
  } catch (error) {
    showError('获取申请列表失败')
  }
}

const getApplicantName = (application) => {
  // 返回申请人姓名或学号
  return application.applicantName || application.applicantStudentNo || '未知申请人'
}

const acceptApplication = async (application) => {
  try {
    const data = await post(`/teams/applications/${application.id}/accept`)

    if (data.code === 200) {
      showSuccess('已接受申请')
      await fetchTeamApplications(selectedTeam.value.id)
      await fetchTeams()
    } else {
      showError(data.message || '接受申请失败')
    }
  } catch (error) {
    showError('接受申请失败')
  }
}

const rejectApplication = async (application) => {
  try {
    await ElMessageBox.prompt('请输入拒绝理由（可选）', '拒绝申请', {
      confirmButtonText: '确定拒绝',
      cancelButtonText: '取消',
      inputPattern: /.*/,
      inputErrorMessage: ''
    })

    const reason = ElMessageBox.prompt.inputValue || ''
    const params = new URLSearchParams({
      responseReason: reason
    })

    const data = await post(`/teams/applications/${application.id}/reject?${params.toString()}`)

    if (data.code === 200) {
      showSuccess('已拒绝申请')
      await fetchTeamApplications(selectedTeam.value.id)
    } else {
      showError(data.message || '拒绝申请失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      showError('拒绝申请失败')
    }
  }
}

const openInviteDialog = (team) => {
  invitingTeam.value = team
  searchStudentKeyword.value = ''
  searchedStudents.value = []
  foundStudent.value = null
  selectedStudent.value = null
  showInviteMemberDialog.value = true
}

const closeInviteDialog = () => {
  showInviteMemberDialog.value = false
  invitingTeam.value = null
  selectedStudent.value = null
  foundStudent.value = null
  searchStudentKeyword.value = ''
  searchedStudents.value = []
}

const searchStudent = async () => {
  if (!searchStudentKeyword.value) {
    showWarning('请输入学号')
    return
  }

  searchingStudent.value = true

  try {
    const params = new URLSearchParams({
      keyword: searchStudentKeyword.value
    })

    const data = await get(`/users/students/search?${params.toString()}`)

    if (data.code === 200) {
      searchedStudents.value = data.data || []

      // 如果只有一个结果,自动选择
      if (searchedStudents.value.length === 1) {
        foundStudent.value = searchedStudents.value[0]
        showSuccess(`找到学生: ${foundStudent.value.realName} (${foundStudent.value.studentNo})`)
      } else if (searchedStudents.value.length > 1) {
        showSuccess(`找到 ${searchedStudents.value.length} 个匹配的学生`)
      } else {
        ElMessage.info('未找到匹配的学生')
      }
    } else {
      showError(data.message || '搜索失败')
    }
  } catch (error) {
    showError('搜索失败')
  } finally {
    searchingStudent.value = false
  }
}

const selectFoundStudent = (student) => {
  foundStudent.value = student
  searchedStudents.value = []
}

const selectStudent = () => {
  if (foundStudent.value) {
    selectedStudent.value = foundStudent.value
    foundStudent.value = null
  }
}

const handleInviteMember = async () => {
  if (!selectedStudent.value) {
    showWarning('请选择要邀请的学生')
    return
  }

  inviting.value = true

  try {
    const params = new URLSearchParams({
      invitedUserId: selectedStudent.value.id
    })

    const data = await post(`/teams/${invitingTeam.value.id}/invite?${params.toString()}`)

    if (data.code === 200) {
      showSuccess('邀请已发送')
      closeInviteDialog()
      await fetchTeams()
    } else {
      showError(data.message || '邀请失败')
    }
  } catch (error) {
    showError('邀请失败')
  } finally {
    inviting.value = false
  }
}

const searchTeams = async () => {
  if (!searchTeamKeyword.value) {
    showWarning('请输入团队名称')
    return
  }

  try {
    const params = new URLSearchParams({
      keyword: searchTeamKeyword.value
    })

    const data = await get(`/teams/search?${params.toString()}`)

    if (data.code === 200) {
      searchedTeams.value = data.data || []

      if (searchedTeams.value.length > 0) {
        showSuccess(`找到 ${searchedTeams.value.length} 个匹配的团队`)
      } else {
        ElMessage.info('未找到匹配的团队')
      }
    } else {
      showError(data.message || '搜索失败')
    }
  } catch (error) {
    showError('搜索失败')
  }
}

const applyToTeam = async (team) => {
  try {
    const data = await post(`/teams/${team.id}/apply`)

    if (data.code === 200) {
      showSuccess('申请已发送')
      await fetchMyApplications()
    } else {
      showError(data.message || '申请失败')
    }
  } catch (error) {
    showError('申请失败')
  }
}

const cancelApplication = async (application) => {
  try {
    await showConfirm('确定要取消此申请吗？', '取消申请')

    const data = await del(`/teams/applications/${application.id}`)

    if (data.code === 200) {
      showSuccess('申请已取消')
      await fetchMyApplications()
    } else {
      showError(data.message || '取消失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      showError('取消失败')
    }
  }
}

const acceptInvitation = async (invitation) => {
  try {
    const data = await post(`/teams/invitations/${invitation.id}/accept`)

    if (data.code === 200) {
      showSuccess('已接受邀请')
      await Promise.all([fetchInvitations(), fetchTeams()])
    } else {
      showError(data.message || '接受邀请失败')
    }
  } catch (error) {
    showError('接受邀请失败')
  }
}

const rejectInvitation = async (invitation) => {
  try {
    await showConfirm('确定要拒绝这个邀请吗？', '拒绝邀请')

    const data = await post(`/teams/invitations/${invitation.id}/reject`)

    if (data.code === 200) {
      showSuccess('已拒绝邀请')
      await fetchInvitations()
    } else {
      showError(data.message || '拒绝邀请失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      showError('拒绝邀请失败')
    }
  }
}

const getTrackName = (team) => {
  const track = tracks.value.find(t => t.id === team.competitionTrackId)
  return track ? track.trackName : '未知赛道'
}

const getTeamStatusClass = (status) => {
  return {
    'badge-primary': status === 'CONFIRMED' || status === 'REGISTERED',
    'badge-warning': status === 'FORMING',
    'badge-gray': status === 'SUBMITTED' || status === 'REVIEWED' || status === 'AWARDED'
  }
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

const getInvitationStatusClass = (status) => {
  return {
    'badge-primary': status === 'ACCEPTED',
    'badge-warning': status === 'PENDING',
    'badge-gray': status === 'REJECTED' || status === 'EXPIRED' || status === 'CANCELLED'
  }
}

const getInvitationStatusText = (status) => {
  const texts = {
    PENDING: '待处理',
    ACCEPTED: '已接受',
    REJECTED: '已拒绝',
    EXPIRED: '已过期',
    CANCELLED: '已取消'
  }
  return texts[status] || status
}

const getApplicationStatusClass = (status) => {
  return {
    'badge-primary': status === 'ACCEPTED',
    'badge-warning': status === 'PENDING',
    'badge-gray': status === 'REJECTED'
  }
}

const getApplicationStatusText = (status) => {
  const texts = {
    PENDING: '待审核',
    ACCEPTED: '已通过',
    REJECTED: '已拒绝'
  }
  return texts[status] || status
}

</script>

<style scoped>
.teams-page {
  min-height: 100vh;
}

.page-header {
  margin-bottom: var(--spacing-xl);
}

.tabs-section {
  padding: var(--spacing-lg);
}

.tabs-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--spacing-lg);
  flex-wrap: wrap;
}

.tabs-group {
  display: flex;
  gap: var(--spacing-md);
  flex-wrap: wrap;
}

.tab-btn {
  padding: var(--spacing-md) var(--spacing-lg);
  border-radius: 8px;
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  color: var(--color-text);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.tab-btn:hover {
  background: var(--color-surface-light);
}

.tab-btn.active {
  background: var(--color-accent);
  color: white;
  border-color: var(--color-accent);
}

.tab-action-btn {
  flex-shrink: 0;
}

.team-card,
.invitation-card,
.application-card,
.team-result-card {
  padding: var(--spacing-xl);
  transition: all var(--transition-fast);
}

.team-card:hover {
  transform: translateY(-2px);
}

.team-header,
.invitation-header,
.application-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

.team-info,
.invitation-info,
.application-info {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
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

.info-label {
  color: var(--color-text-secondary);
}

.info-value {
  font-weight: 500;
}

.team-actions,
.invitation-actions,
.application-actions {
  margin-top: var(--spacing-lg);
}

.invitation-processed {
  padding: var(--spacing-md);
  background: rgba(0, 0, 0, 0.02);
  border-radius: var(--radius-md);
  text-align: center;
  color: var(--color-text-light);
}

.search-section {
  padding: var(--spacing-xl);
}

.search-form {
  flex-wrap: wrap;
}

.invite-steps {
  padding: var(--spacing-md);
}

.step-section {
  margin-bottom: var(--spacing-xl);
}

.section-title {
  font-family: var(--font-display);
  font-size: 17px;
  font-weight: 600;
  margin-bottom: var(--spacing-md);
}

.result-title {
  font-family: var(--font-display);
  font-size: 15px;
  font-weight: 600;
  color: var(--color-accent);
  display: flex;
  align-items: center;
  gap: 8px;
}

.team-brief-info {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.brief-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-text-secondary);
}

.empty-result {
  padding: var(--spacing-lg);
  background: rgba(0, 0, 0, 0.02);
  border-radius: var(--radius-md);
}

.student-result,
.student-option {
  padding: var(--spacing-lg);
}

.student-info {
  display: flex;
  gap: var(--spacing-md);
  align-items: center;
}

.student-avatar {
  width: 48px;
  height: 48px;
  background: var(--color-accent);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.avatar-text {
  font-size: 20px;
  font-weight: 600;
  color: white;
}

.student-details {
  flex: 1;
}

.student-meta {
  display: flex;
  gap: var(--spacing-md);
  color: var(--color-text-secondary);
}

.student-actions {
  margin-top: var(--spacing-md);
}

.students-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.student-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.confirmation-box {
  padding: var(--spacing-lg);
}

.applications-dialog-content {
  padding: var(--spacing-md);
}

.applications-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.application-item {
  padding: var(--spacing-lg);
}

.applicant-info {
  display: flex;
  gap: var(--spacing-md);
  align-items: flex-start;
}

.applicant-avatar {
  width: 48px;
  height: 48px;
  background: var(--color-accent);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.applicant-details {
  flex: 1;
}

.applicant-meta {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
  color: var(--color-text-secondary);
}

.applicant-time {
  color: var(--color-text-light);
  margin-top: var(--spacing-xs);
}

.ml-sm {
  margin-left: 8px;
}

.empty-state {
  padding: var(--spacing-3xl);
  text-align: center;
}

.empty-icon {
  margin-bottom: var(--spacing-lg);
  opacity: 0.5;
}

@media (max-width: 768px) {
  .tabs-wrapper {
    flex-direction: column;
    align-items: stretch;
  }

  .tabs-group {
    flex-direction: column;
  }

  .tab-btn {
    width: 100%;
    justify-content: center;
  }

  .tab-action-btn {
    width: 100%;
    justify-content: center;
  }

  .team-actions button,
  .invitation-actions button,
  .application-actions button {
    width: 100%;
    justify-content: center;
  }

  .search-form > * {
    width: 100%;
  }
}
</style>