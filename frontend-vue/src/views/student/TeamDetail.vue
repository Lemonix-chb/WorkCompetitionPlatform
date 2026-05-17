<template>
  <div class="team-detail-page">
    <div class="container container-sm">
      <!-- Page Header -->
      <div class="page-header slide-in">
        <div class="breadcrumb">
          <router-link to="/student/teams" class="link">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M10 4L6 8L10 12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            返回团队列表
          </router-link>
        </div>
        <h1 class="page-title">{{ team.teamName }}</h1>
        <div class="team-code caption">团队编号：{{ team.teamCode }}</div>
      </div>

      <!-- Team Overview -->
      <div class="team-overview card scale-in">
        <div class="overview-header">
          <div class="team-main-info">
            <h2 class="section-title">{{ team.teamName }}</h2>
            <span :class="getStatusClass(team.status)" class="badge">
              {{ getStatusText(team.status) }}
            </span>
          </div>
          <div class="team-role-badge">
            <span :class="isLeader ? 'badge-primary' : 'badge-secondary'" class="badge">
              {{ isLeader ? '队长' : '队员' }}
            </span>
          </div>
        </div>

        <!-- Team Stats -->
        <div class="team-stats">
          <div class="stat-item">
            <div class="stat-value">{{ team.currentMemberCount }}</div>
            <div class="stat-label caption">当前成员</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ team.maxMemberCount }}</div>
            <div class="stat-label caption">最大人数</div>
          </div>
          <div class="stat-item">
            <div class="stat-value caption">{{ formatDate(team.createTime) }}</div>
            <div class="stat-label caption">创建日期</div>
          </div>
        </div>

        <!-- Team Actions (仅队长可见) -->
        <div v-if="isLeader" class="team-actions flex gap-md">
          <!-- 邀请成员按钮（组建中状态且未满员） -->
          <button
            class="action-btn-primary"
            @click="showInviteDialog"
            v-if="team.status === 'FORMING' && team.currentMemberCount < team.maxMemberCount"
          >
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <circle cx="8" cy="6" r="4" stroke="currentColor" stroke-width="1.5"/>
              <path d="M3 13C3 10.5 5.5 8.5 8 8.5C10.5 8.5 13 10.5 13 13" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              <path d="M13 6L15 6M15 6L15 4M15 6L15 8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            邀请成员
          </button>

          <!-- 邀请成员禁用提示（已锁定或已满员） -->
          <button
            class="action-btn-disabled"
            disabled
            v-if="team.status !== 'FORMING' || team.currentMemberCount >= team.maxMemberCount"
            :title="getInviteDisabledReason()"
          >
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <circle cx="8" cy="6" r="4" stroke="currentColor" stroke-width="1.5"/>
              <path d="M3 13C3 10.5 5.5 8.5 8 8.5C10.5 8.5 13 10.5 13 13" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <span>无法邀请</span>
            <span class="disabled-reason">{{ getInviteDisabledReasonShort() }}</span>
          </button>

          <!-- 确认团队按钮（组建中状态） -->
          <button
            class="action-btn-secondary"
            @click="confirmTeam"
            v-if="team.status === 'FORMING'"
          >
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.5"/>
              <path d="M6 8L7 9L10 7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            确认团队
          </button>
        </div>
      </div>

      <!-- Team Members -->
      <div class="team-members-section slide-in">
        <h2 class="section-title">团队成员</h2>

        <div v-if="members.length === 0" class="empty-state card">
          <div class="empty-icon">
            <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
              <circle cx="24" cy="16" r="10" stroke="#bdc3c7" stroke-width="2"/>
              <path d="M12 40C12 34 18 30 24 30C30 30 36 34 36 40" stroke="#bdc3c7" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <h3 class="card-title mb-md">暂无团队成员</h3>
        </div>

        <div v-else class="grid grid-auto">
          <div v-for="member in members" :key="member.id" class="member-card card card-flat">
            <div class="member-avatar">
              <span class="avatar-text">{{ member.realName ? member.realName.charAt(0) : '?' }}</span>
            </div>
            <div class="member-details">
              <div class="member-name-row">
                <span class="member-name body-text">{{ member.realName || '未知成员' }}</span>
                <span :class="getMemberRoleClass(member.memberRole)" class="badge">
                  {{ member.memberRole === 'LEADER' ? '队长' : '队员' }}
                </span>
              </div>
              <div class="member-meta caption">
                <span>{{ member.studentNo || '未填写学号' }}</span>
                <span>{{ member.major || '未填写专业' }}</span>
              </div>
              <div class="member-meta caption">
                加入时间：{{ formatDate(member.joinTime) }}
              </div>
            </div>
            <!-- 队长可以移除队员 -->
            <div class="member-actions" v-if="isLeader && member.memberRole !== 'LEADER'">
              <button class="btn-danger btn-sm" @click="removeMember(member)">移除</button>
            </div>
          </div>
        </div>

        <!-- 队员可以退出团队 -->
        <div v-if="!isLeader" class="member-quit-section">
          <button class="btn-danger" @click="quitTeam">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M7 17H5C4 17 3 16 3 15V5C3 4 4 3 5 3H7" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M14 13L17 10L14 7M17 10H7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            退出团队
          </button>
        </div>

        <!-- 队长可以解散团队 -->
        <div v-if="isLeader" class="team-dissolve-section">
          <button class="btn-danger" @click="dissolveTeam">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <circle cx="10" cy="10" r="8" stroke="currentColor" stroke-width="2"/>
              <path d="M6 6L14 14M14 6L6 14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            解散团队
          </button>
        </div>
      </div>

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
                placeholder="输入学号或姓名搜索学生"
                clearable
                @keyup.enter="searchStudent"
                @input="onStudentSearchInput"
              />
              <button class="action-btn-primary" @click="searchStudent" :disabled="searchingStudent" style="flex:none;padding:10px 20px;">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><circle cx="7" cy="7" r="5" stroke="currentColor" stroke-width="1.5"/><path d="M11 11L14 14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
                {{ searchingStudent ? '搜索中...' : '搜索' }}
              </button>
            </div>

            <!-- Single Result -->
            <div v-if="foundStudent" class="student-result card card-flat mb-lg">
              <h4 class="caption mb-md" style="color:var(--color-text-secondary)">搜索结果</h4>
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
              <div class="mt-lg">
                <button class="action-btn-primary" @click="selectStudent" style="padding:8px 16px;">
                  选择此学生
                </button>
              </div>
            </div>
            <!-- Multiple Results -->
            <div v-else-if="searchedStudents.length > 0" class="students-list mb-lg">
              <h4 class="caption mb-md" style="color:var(--color-text-secondary)">搜索结果（{{ searchedStudents.length }} 人）</h4>
              <div v-for="student in searchedStudents" :key="student.id" class="student-option card card-flat">
                <div class="student-info">
                  <div class="student-avatar">
                    <span class="avatar-text">{{ student.realName.charAt(0) }}</span>
                  </div>
                  <div class="student-details">
                    <h4 class="card-title">{{ student.realName }}</h4>
                    <div class="student-meta caption">
                      <span>学号：{{ student.studentNo }}</span>
                      <span v-if="student.major">专业：{{ student.major }}</span>
                    </div>
                  </div>
                </div>
                <button class="action-btn-secondary" @click="selectFoundStudent(student)" style="padding:6px 12px;flex:none;">
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
            <div class="confirmation-box card card-flat">
              <div class="info-row">
                <span class="caption" style="color:var(--color-text-secondary)">邀请对象</span>
                <span class="body-text">{{ selectedStudent.realName }}</span>
              </div>
              <div class="info-row">
                <span class="caption" style="color:var(--color-text-secondary)">学号</span>
                <span class="body-text">{{ selectedStudent.studentNo }}</span>
              </div>
              <div class="info-row">
                <span class="caption" style="color:var(--color-text-secondary)">加入团队</span>
                <span class="body-text">{{ team.teamName }}</span>
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
              :loading="inviteLoading"
            >
              发送邀请
            </el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { get, post, del } from '@/utils/api'
import { showSuccess, showError, showWarning, showConfirm } from '@/utils/messageUtils'
import { formatDateTime } from '@/utils/dateUtils'

const router = useRouter()
const route = useRoute()

const teamId = route.params.id
const team = ref({})
const members = ref([])
const currentUserId = computed(() => localStorage.getItem('userId'))

const isLeader = computed(() => {
  return team.value.leaderId === parseInt(currentUserId.value)
})

const showInviteMemberDialog = ref(false)
const searchStudentKeyword = ref('')
const searchedStudents = ref([])
const foundStudent = ref(null)
const selectedStudent = ref(null)
const searchingStudent = ref(false)
const inviteLoading = ref(false)

onMounted(async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
    return
  }

  await fetchTeamDetail()
  await fetchTeamMembers()
})

const fetchTeamDetail = async () => {
  try {
    const data = await get(`/teams/${teamId}`)
    team.value = data
  } catch (error) {
    showError('获取团队信息失败')
    router.push('/student/teams')
  }
}

const fetchTeamMembers = async () => {
  try {
    const data = await get(`/teams/${teamId}/members`)
    members.value = data || []
  } catch (error) {
    showError('获取团队成员失败')
  }
}

const showInviteDialog = () => {
  searchStudentKeyword.value = ''
  searchedStudents.value = []
  foundStudent.value = null
  selectedStudent.value = null
  showInviteMemberDialog.value = true
}

const closeInviteDialog = () => {
  showInviteMemberDialog.value = false
  searchStudentKeyword.value = ''
  searchedStudents.value = []
  foundStudent.value = null
  selectedStudent.value = null
}

const searchStudent = async () => {
  if (!searchStudentKeyword.value) {
    showWarning('请输入学号')
    return
  }

  searchingStudent.value = true
  try {
    const params = new URLSearchParams({ keyword: searchStudentKeyword.value })
    const data = await get(`/users/students/search?${params.toString()}`)
    searchedStudents.value = data || []

    if (searchedStudents.value.length === 1) {
      foundStudent.value = searchedStudents.value[0]
      showSuccess(`找到学生: ${foundStudent.value.realName} (${foundStudent.value.studentNo})`)
    } else if (searchedStudents.value.length > 1) {
      showSuccess(`找到 ${searchedStudents.value.length} 个匹配的学生`)
    } else {
      ElMessage.info('未找到匹配的学生')
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

let searchDebounceTimer = null
const onStudentSearchInput = () => {
  if (searchDebounceTimer) clearTimeout(searchDebounceTimer)
  if (!searchStudentKeyword.value) {
    searchedStudents.value = []
    foundStudent.value = null
    selectedStudent.value = null
    return
  }
  searchDebounceTimer = setTimeout(async () => {
    searchingStudent.value = true
    try {
      const params = new URLSearchParams({ keyword: searchStudentKeyword.value })
      const data = await get(`/users/students/search?${params.toString()}`)
      searchedStudents.value = data || []
      foundStudent.value = null
      selectedStudent.value = null
      if (searchedStudents.value.length === 1) {
        foundStudent.value = searchedStudents.value[0]
      }
    } catch {
      searchedStudents.value = []
    } finally {
      searchingStudent.value = false
    }
  }, 300)
}

const handleInviteMember = async () => {
  if (!selectedStudent.value) {
    showWarning('请先选择要邀请的学生')
    return
  }

  inviteLoading.value = true
  try {
    const params = new URLSearchParams({
      inviteeStudentNo: selectedStudent.value.studentNo
    })
    await post(`/teams/${teamId}/invite?${params.toString()}`)

    showSuccess('邀请已发送')
    closeInviteDialog()
    await fetchTeamDetail()
  } catch (error) {
    showError('邀请失败')
  } finally {
    inviteLoading.value = false
  }
}

const removeMember = async (member) => {
  try {
    await showConfirm(`确定要移除成员 ${member.realName} 吗？`, '移除成员')

    await del(`/teams/${teamId}/members/${member.userId}`)

    showSuccess('成员已移除')
    await fetchTeamMembers()
    await fetchTeamDetail()
  } catch (error) {
    if (error !== 'cancel') {
      showError('移除失败')
    }
  }
}

const confirmTeam = async () => {
  try {
    await showConfirm('确认团队后，团队状态将变为已确认。确定要确认团队吗？', '确认团队')

    await post(`/teams/${teamId}/confirm`)

    showSuccess('团队已确认')
    await fetchTeamDetail()
  } catch (error) {
    if (error !== 'cancel') {
      showError('确认失败')
    }
  }
}

const quitTeam = async () => {
  try {
    await showConfirm('退出团队后，您将不再是团队成员。确定要退出吗？', '退出团队')

    await post(`/teams/${teamId}/quit`)

    showSuccess('已退出团队')
    router.push('/student/teams')
  } catch (error) {
    if (error !== 'cancel') {
      showError('退出失败')
    }
  }
}

const dissolveTeam = async () => {
  try {
    await ElMessageBox.confirm(
      '解散团队后，所有成员将被移除，团队数据将删除。此操作不可撤销，确定要解散吗？',
      '解散团队',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }
    )

    await del(`/teams/${teamId}`)

    showSuccess('团队已解散')
    router.push('/student/teams')
  } catch (error) {
    if (error !== 'cancel') {
      showError('解散失败')
    }
  }
}

const getMemberRoleClass = (role) => {
  return {
    'badge-primary': role === 'LEADER',
    'badge-secondary': role === 'MEMBER'
  }
}

const getStatusClass = (status) => {
  return {
    'badge-primary': status === 'CONFIRMED' || status === 'REGISTERED',
    'badge-warning': status === 'FORMING',
    'badge-gray': status === 'SUBMITTED' || status === 'REVIEWED' || status === 'AWARDED'
  }
}

const getStatusText = (status) => {
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

const getInviteDisabledReason = () => {
  if (team.value.status !== 'FORMING') {
    return '团队已确认锁定，无法邀请新成员'
  }
  if (team.value.currentMemberCount >= team.value.maxMemberCount) {
    return '团队成员已满，无法邀请新成员'
  }
  return '无法邀请成员'
}

const getInviteDisabledReasonShort = () => {
  if (team.value.status !== 'FORMING') {
    return '(已锁定)'
  }
  if (team.value.currentMemberCount >= team.value.maxMemberCount) {
    return '(已满员)'
  }
  return ''
}

const formatDate = (dateStr) => {
  return formatDateTime(dateStr)
}
</script>

<style scoped>
.team-detail-page {
  min-height: 100vh;
}

.breadcrumb {
  margin-bottom: var(--spacing-lg);
}

.breadcrumb a {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.team-code {
  color: var(--color-text-secondary);
  margin-top: var(--spacing-sm);
}

.team-overview {
  margin-bottom: var(--spacing-2xl);
}

.overview-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--spacing-lg);
}

.team-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--spacing-lg);
  padding: var(--spacing-lg) 0;
  border-top: 1px solid var(--color-border-light);
  border-bottom: 1px solid var(--color-border-light);
  margin-bottom: var(--spacing-lg);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-sm);
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.team-actions {
  margin-top: var(--spacing-lg);
  gap: var(--spacing-sm) !important;
}

.team-members-section {
  animation-delay: 0.1s;
}

.member-card {
  display: flex;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
  transition: all var(--transition-fast);
}

.member-avatar {
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

.member-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.member-name-row {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.member-name {
  font-weight: 500;
}

.member-meta {
  display: flex;
  gap: var(--spacing-md);
  color: var(--color-text-secondary);
}

.member-actions {
  align-self: center;
}

.empty-state {
  padding: var(--spacing-3xl);
  text-align: center;
}

.empty-icon {
  margin-bottom: var(--spacing-lg);
  opacity: 0.5;
}

.team-role-badge {
  display: flex;
  align-items: center;
}

.member-quit-section,
.team-dissolve-section {
  margin-top: var(--spacing-xl);
  padding-top: var(--spacing-lg);
  border-top: 1px solid var(--color-border-light);
  display: flex;
  justify-content: center;
}

.btn-sm {
  padding: var(--spacing-sm) var(--spacing-md);
  font-size: 14px;
}

@media (max-width: 768px) {
  .team-stats {
    grid-template-columns: 1fr;
    gap: var(--spacing-md);
  }

  .team-actions {
    flex-direction: column;
  }

  .team-actions button {
    width: 100%;
    justify-content: center;
  }
}

/* Invite Dialog Styles */
.invite-steps {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.step-section {
  padding: var(--spacing-md) 0;
}

.search-form {
  display: flex;
  gap: var(--spacing-md);
  align-items: center;
}

.student-result,
.student-option {
  padding: var(--spacing-lg);
  border: 1px solid var(--color-border-light);
  border-radius: 8px;
}

.student-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-sm);
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

.student-details {
  flex: 1;
  min-width: 0;
}

.student-meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-md);
  color: var(--color-text-secondary);
}

.confirmation-box {
  padding: var(--spacing-lg);
}

.confirmation-box .info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-sm) 0;
}

.confirmation-box .info-row + .info-row {
  border-top: 1px solid var(--color-border-light);
}

.empty-result {
  padding: var(--spacing-lg);
  text-align: center;
  color: var(--color-text-secondary);
  background: rgba(0,0,0,0.02);
  border-radius: 8px;
}

/* Apple-style Action Buttons - 与Teams.vue团队卡片按钮保持一致 */
.action-btn-primary {
  flex: 1;
  padding: 8px 14px;
  border-radius: 10px;
  background: linear-gradient(135deg, #0071e3 0%, #0077ed 100%);
  color: white;
  border: none;
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 113, 227, 0.2);
  white-space: nowrap;
}

.action-btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 113, 227, 0.3);
}

.action-btn-secondary {
  flex: 1;
  padding: 8px 14px;
  border-radius: 10px;
  background: rgba(0, 113, 227, 0.08);
  color: #0071e3;
  border: 1px solid rgba(0, 113, 227, 0.2);
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;
}

.action-btn-secondary:hover {
  background: rgba(0, 113, 227, 0.12);
  border-color: rgba(0, 113, 227, 0.3);
  transform: translateY(-2px);
}

.action-btn-disabled {
  flex: 1;
  padding: 8px 14px;
  border-radius: 10px;
  border: 1px solid rgba(142, 142, 147, 0.15);
  background: rgba(142, 142, 147, 0.06);
  color: #86868b;
  font-weight: 600;
  font-size: 13px;
  cursor: not-allowed;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  opacity: 0.6;
  transition: none;
  pointer-events: none;
  white-space: nowrap;
}

.disabled-reason {
  font-size: 10px;
  color: rgba(142, 142, 147, 0.8);
  font-weight: 400;
  margin-left: 6px;
}
</style>