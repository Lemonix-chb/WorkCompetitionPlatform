<template>
  <div class="invitations-page">
    <div class="container">
      <!-- Page Header -->
      <div class="page-header slide-in">
        <h1 class="page-title">团队邀请</h1>
        <p class="body-text">管理收到的团队邀请</p>
      </div>

      <!-- Invitations List -->
      <div class="invitations-section scale-in">
        <div v-if="invitations.length === 0" class="empty-state card">
          <div class="empty-icon">
            <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
              <rect x="8" y="8" width="32" height="28" rx="3" stroke="#bdc3c7" stroke-width="2"/>
              <path d="M8 16H40M16 24H32M16 28H28" stroke="#bdc3c7" stroke-width="2" stroke-linecap="round"/>
              <circle cx="40" cy="12" r="6" fill="#5a7fa8" stroke="none"/>
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
                <span class="info-label caption">邀请人</span>
                <span class="info-value body-text">{{ invitation.inviterName || '队长' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label caption">过期时间</span>
                <span class="info-value caption">{{ formatDate(invitation.expireTime) }}</span>
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
              {{ invitation.status === 'ACCEPTED' ? '已接受' : '已拒绝' }} · {{ formatDate(invitation.processTime) }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { get, post } from '@/utils/api'
import { showSuccess, showError, showConfirm } from '@/utils/messageUtils'
import { formatDateTime } from '@/utils/dateUtils'

const router = useRouter()
const invitations = ref([])

onMounted(async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
    return
  }

  await fetchInvitations()
})

const fetchInvitations = async () => {
  try {
    const data = await get('/teams/invitations/pending')

    if (data.code === 200) {
      invitations.value = data.data || []
    } else {
      showError(data.message || '获取邀请列表失败')
    }
  } catch (error) {
    showError('获取邀请列表失败')
  }
}

const acceptInvitation = async (invitation) => {
  try {
    const data = await post(`/teams/invitations/${invitation.id}/accept`)

    if (data.code === 200) {
      showSuccess('已接受邀请')
      await fetchInvitations()
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

const formatDate = (dateStr) => {
  return formatDateTime(dateStr)
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
</script>

<style scoped>
.invitations-page {
  min-height: 100vh;
}

.invitations-section {
  animation-delay: 0.1s;
}

.invitation-card {
  padding: var(--spacing-xl);
  transition: all var(--transition-fast);
}

.invitation-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

.invitation-info {
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

.invitation-actions {
  margin-top: var(--spacing-lg);
}

.invitation-processed {
  padding: var(--spacing-md);
  background: rgba(0, 0, 0, 0.02);
  border-radius: var(--radius-md);
  text-align: center;
  color: var(--color-text-light);
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
  .invitation-actions {
    flex-direction: column;
  }

  .invitation-actions button {
    width: 100%;
    justify-content: center;
  }
}
</style>