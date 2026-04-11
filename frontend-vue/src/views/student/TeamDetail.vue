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
          <button
            class="btn-primary"
            @click="showInviteDialog"
            v-if="team.currentMemberCount < team.maxMemberCount"
          >
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <circle cx="10" cy="8" r="5" stroke="currentColor" stroke-width="2"/>
              <path d="M4 17C4 14 7 12 10 12C13 12 16 14 16 17" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M16 8L19 8M19 8L19 5M19 8L19 11" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            邀请成员
          </button>
          <button
            class="btn-secondary"
            @click="confirmTeam"
            v-if="team.status === 'FORMING'"
          >
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <circle cx="10" cy="10" r="8" stroke="currentColor" stroke-width="2"/>
              <path d="M7 10L9 12L13 8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
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
        width="480px"
      >
        <el-form :model="inviteForm" label-width="100px">
          <el-form-item label="用户ID">
            <el-input
              v-model="inviteForm.invitedUserId"
              placeholder="请输入被邀请用户的ID"
              required
            />
          </el-form-item>
          <el-form-item label="学号">
            <el-input
              v-model="inviteForm.invitedStudentNo"
              placeholder="可选，输入学号查找用户"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="showInviteMemberDialog = false">取消</el-button>
            <el-button type="primary" @click="handleInviteMember" :loading="inviteLoading">
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
const inviteForm = ref({
  invitedUserId: '',
  invitedStudentNo: ''
})
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

    if (data.code === 200) {
      team.value = data.data
    } else {
      showError(data.message || '获取团队信息失败')
      router.push('/student/teams')
    }
  } catch (error) {
    showError('获取团队信息失败')
    router.push('/student/teams')
  }
}

const fetchTeamMembers = async () => {
  try {
    const data = await get(`/teams/${teamId}/members`)

    if (data.code === 200) {
      members.value = data.data || []
    } else {
      showError(data.message || '获取团队成员失败')
    }
  } catch (error) {
    showError('获取团队成员失败')
  }
}

const showInviteDialog = () => {
  showInviteMemberDialog.value = true
}

const handleInviteMember = async () => {
  if (!inviteForm.value.invitedUserId) {
    showWarning('请输入被邀请用户的ID')
    return
  }

  inviteLoading.value = true

  try {
    const params = new URLSearchParams({
      invitedUserId: inviteForm.value.invitedUserId
    })

    const data = await post(`/teams/${teamId}/invite?${params.toString()}`)

    if (data.code === 200) {
      showSuccess('邀请已发送')
      showInviteMemberDialog.value = false
      inviteForm.value = { invitedUserId: '', invitedStudentNo: '' }
    } else {
      showError(data.message || '邀请失败')
    }
  } catch (error) {
    showError('邀请失败，请检查网络连接')
  } finally {
    inviteLoading.value = false
  }
}

const removeMember = async (member) => {
  try {
    await showConfirm(`确定要移除成员 ${member.realName} 吗？`, '移除成员')

    const data = await del(`/teams/${teamId}/members/${member.userId}`)

    if (data.code === 200) {
      showSuccess('成员已移除')
      await fetchTeamMembers()
      await fetchTeamDetail()
    } else {
      showError(data.message || '移除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      showError('移除失败')
    }
  }
}

const confirmTeam = async () => {
  try {
    await showConfirm('确认团队后，团队状态将变为已确认。确定要确认团队吗？', '确认团队')

    const data = await post(`/teams/${teamId}/confirm`)

    if (data.code === 200) {
      showSuccess('团队已确认')
      await fetchTeamDetail()
    } else {
      showError(data.message || '确认失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      showError('确认失败')
    }
  }
}

const quitTeam = async () => {
  try {
    await showConfirm('退出团队后，您将不再是团队成员。确定要退出吗？', '退出团队')

    const data = await post(`/teams/${teamId}/quit`)

    if (data.code === 200) {
      showSuccess('已退出团队')
      router.push('/student/teams')
    } else {
      showError(data.message || '退出失败')
    }
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

    const data = await del(`/teams/${teamId}`)

    if (data.code === 200) {
      showSuccess('团队已解散')
      router.push('/student/teams')
    } else {
      showError(data.message || '解散失败')
    }
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
</style>