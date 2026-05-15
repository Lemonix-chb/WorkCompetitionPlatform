<template>
  <div class="profile-page fade-in">
    <div class="container">
      <h1 class="page-title">个人中心</h1>

      <!-- Profile Overview Card -->
      <div class="card profile-overview mb-xl">
        <div class="overview-content">
          <div class="avatar-section">
            <div class="avatar-circle">
              {{ avatarLetter }}
            </div>
            <div class="avatar-info">
              <h2 class="user-name">{{ profile.realName || '--' }}</h2>
              <p class="user-role">
                <span class="badge badge-student">学生</span>
                <span v-if="profile.status" class="badge" :class="statusClass">{{ statusLabel }}</span>
              </p>
            </div>
          </div>
          <div class="stats-row">
            <div class="stat-item">
              <span class="stat-label">学号</span>
              <span class="stat-value">{{ profile.studentNo || '--' }}</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-label">学院</span>
              <span class="stat-value">{{ profile.college || '--' }}</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-label">专业</span>
              <span class="stat-value">{{ profile.major || '--' }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Edit Profile + Change Password -->
      <div class="profile-grid">
        <!-- Edit Profile Card -->
        <div class="card">
          <h3 class="card-title">编辑个人信息</h3>
          <form @submit.prevent="handleUpdate" class="profile-form">
            <!-- Username (readonly) -->
            <div class="form-group">
              <label class="form-label">用户名</label>
              <input v-model="profile.username" type="text" class="form-input" readonly disabled />
            </div>

            <!-- Student No (readonly) -->
            <div class="form-group">
              <label class="form-label">学号</label>
              <input v-model="profile.studentNo" type="text" class="form-input" readonly disabled />
            </div>

            <!-- Real Name (readonly) -->
            <div class="form-group">
              <label class="form-label">真实姓名</label>
              <input v-model="profile.realName" type="text" class="form-input" readonly disabled />
            </div>

            <!-- Email -->
            <div class="form-group">
              <label class="form-label">邮箱</label>
              <input v-model="profile.email" type="email" class="form-input" placeholder="请输入邮箱" />
            </div>

            <!-- Phone -->
            <div class="form-group">
              <label class="form-label">手机号</label>
              <input v-model="profile.phone" type="text" class="form-input" placeholder="请输入手机号" />
            </div>

            <!-- College -->
            <div class="form-group">
              <label class="form-label">学院</label>
              <input v-model="profile.college" type="text" class="form-input" placeholder="请输入学院" />
            </div>

            <!-- Major -->
            <div class="form-group">
              <label class="form-label">专业</label>
              <input v-model="profile.major" type="text" class="form-input" placeholder="请输入专业" />
            </div>

            <button type="submit" class="btn-primary w-full" :disabled="saving">
              {{ saving ? '保存中...' : '保存修改' }}
            </button>
          </form>
        </div>

        <!-- Change Password Card -->
        <div class="card">
          <h3 class="card-title">修改密码</h3>
          <form @submit.prevent="handleChangePassword" class="password-form">
            <div class="form-group">
              <label class="form-label">旧密码</label>
              <div class="password-wrapper">
                <input v-model="passwordForm.oldPassword" :type="showOldPwd ? 'text' : 'password'" class="form-input" placeholder="请输入旧密码" required />
                <button type="button" class="toggle-pwd" @click="showOldPwd = !showOldPwd">
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <path v-if="!showOldPwd" d="M2 8C2 8 4.5 3.5 8 3.5C11.5 3.5 14 8 14 8C14 8 11.5 12.5 8 12.5C4.5 12.5 2 8 2 8Z" stroke="currentColor" stroke-width="1.2"/>
                    <circle v-if="!showOldPwd" cx="8" cy="8" r="2" stroke="currentColor" stroke-width="1.2"/>
                    <path v-else d="M3 3L13 13M6 5.5C6.7 4.8 7.8 4.3 9 4.3C11 4.3 12.8 6.3 13.5 8C12.8 9.7 11 11.7 9 11.7M4.5 8C5.2 6.3 7 4.3 9 4.3" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
                  </svg>
                </button>
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">新密码</label>
              <div class="password-wrapper">
                <input v-model="passwordForm.newPassword" :type="showNewPwd ? 'text' : 'password'" class="form-input" placeholder="请输入新密码（6-40字符）" required minlength="6" />
                <button type="button" class="toggle-pwd" @click="showNewPwd = !showNewPwd">
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <path v-if="!showNewPwd" d="M2 8C2 8 4.5 3.5 8 3.5C11.5 3.5 14 8 14 8C14 8 11.5 12.5 8 12.5C4.5 12.5 2 8 2 8Z" stroke="currentColor" stroke-width="1.2"/>
                    <circle v-if="!showNewPwd" cx="8" cy="8" r="2" stroke="currentColor" stroke-width="1.2"/>
                    <path v-else d="M3 3L13 13M6 5.5C6.7 4.8 7.8 4.3 9 4.3C11 4.3 12.8 6.3 13.5 8C12.8 9.7 11 11.7 9 11.7M4.5 8C5.2 6.3 7 4.3 9 4.3" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
                  </svg>
                </button>
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">确认新密码</label>
              <div class="password-wrapper">
                <input v-model="passwordForm.confirmPassword" :type="showConfirmPwd ? 'text' : 'password'" class="form-input" placeholder="请再次输入新密码" required />
                <button type="button" class="toggle-pwd" @click="showConfirmPwd = !showConfirmPwd">
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <path v-if="!showConfirmPwd" d="M2 8C2 8 4.5 3.5 8 3.5C11.5 3.5 14 8 14 8C14 8 11.5 12.5 8 12.5C4.5 12.5 2 8 2 8Z" stroke="currentColor" stroke-width="1.2"/>
                    <circle v-if="!showConfirmPwd" cx="8" cy="8" r="2" stroke="currentColor" stroke-width="1.2"/>
                    <path v-else d="M3 3L13 13M6 5.5C6.7 4.8 7.8 4.3 9 4.3C11 4.3 12.8 6.3 13.5 8C12.8 9.7 11 11.7 9 11.7M4.5 8C5.2 6.3 7 4.3 9 4.3" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
                  </svg>
                </button>
              </div>
            </div>

            <button type="submit" class="btn-secondary w-full" :disabled="changingPwd">
              {{ changingPwd ? '修改中...' : '修改密码' }}
            </button>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { get, put } from '@/utils/api'
import { showSuccess, showError, showWarning } from '@/utils/messageUtils'

const router = useRouter()

const profile = ref({
  id: null,
  username: '',
  studentNo: '',
  realName: '',
  email: '',
  phone: '',
  college: '',
  major: '',
  status: ''
})

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const saving = ref(false)
const changingPwd = ref(false)
const showOldPwd = ref(false)
const showNewPwd = ref(false)
const showConfirmPwd = ref(false)

const avatarLetter = computed(() => {
  const name = profile.value.realName
  return name ? name.charAt(0).toUpperCase() : '?'
})

const statusClass = computed(() => {
  const s = profile.value.status
  if (s === 'ACTIVE') return 'badge-success'
  if (s === 'PENDING') return 'badge-warning'
  if (s === 'DISABLED') return 'badge-gray'
  return 'badge-gray'
})

const statusLabel = computed(() => {
  const s = profile.value.status
  if (s === 'ACTIVE') return '已激活'
  if (s === 'PENDING') return '待审核'
  if (s === 'DISABLED') return '已禁用'
  return s
})

onMounted(async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
    return
  }
  await fetchProfile()
})

const fetchProfile = async () => {
  try {
    const data = await get('/auth/info')
    profile.value = {
      id: data.id,
      username: data.username || '',
      studentNo: data.studentNo || '',
      realName: data.realName || '',
      email: data.email || '',
      phone: data.phone || '',
      college: data.college || '',
      major: data.major || '',
      status: data.status || ''
    }
  } catch (error) {
    console.error('获取个人信息失败', error)
  }
}

const handleUpdate = async () => {
  if (!profile.value.id) {
    showWarning('用户信息加载中，请稍后重试')
    return
  }

  saving.value = true

  try {
    await put(`/users/${profile.value.id}`, {
      email: profile.value.email,
      phone: profile.value.phone,
      college: profile.value.college,
      major: profile.value.major
    })
    showSuccess('个人信息更新成功')
  } catch (error) {
    console.error('更新失败', error)
  } finally {
    saving.value = false
  }
}

const handleChangePassword = async () => {
  if (!passwordForm.value.oldPassword) {
    showWarning('请输入旧密码')
    return
  }
  if (!passwordForm.value.newPassword || passwordForm.value.newPassword.length < 6) {
    showWarning('新密码长度至少6个字符')
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    showWarning('两次输入的新密码不一致')
    return
  }
  if (!profile.value.id) {
    showWarning('用户信息加载中，请稍后重试')
    return
  }

  changingPwd.value = true

  try {
    await put(`/users/${profile.value.id}/password`, {
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    showSuccess('密码修改成功，请重新登录')
    passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
    setTimeout(() => {
      localStorage.clear()
      router.push('/login')
    }, 1500)
  } catch (error) {
    console.error('密码修改失败', error)
  } finally {
    changingPwd.value = false
  }
}
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
}

/* Profile Overview */
.profile-overview {
  padding: var(--spacing-2xl);
}

.overview-content {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xl);
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
}

.avatar-circle {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-accent) 0%, var(--color-primary) 100%);
  color: var(--color-text-white);
  font-family: var(--font-display);
  font-size: 32px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(90, 127, 168, 0.3);
}

.user-name {
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-xs);
}

.user-role {
  display: flex;
  gap: var(--spacing-sm);
  flex-wrap: wrap;
}

.badge-student {
  background: rgba(90, 127, 168, 0.12);
  color: var(--color-accent);
  font-size: 12px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: var(--radius-sm);
}

.stats-row {
  display: flex;
  align-items: center;
  gap: 0;
  background: var(--color-bg-main);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg) var(--spacing-xl);
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-bottom: 4px;
}

.stat-value {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.stat-divider {
  width: 1px;
  height: 32px;
  background: var(--color-border);
}

/* Profile Grid */
.profile-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-xl);
}

/* Forms */
.profile-form,
.password-form {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
  margin-top: var(--spacing-lg);
}

.form-input:read-only,
.form-input:disabled {
  background: var(--color-bg-main);
  color: var(--color-text-secondary);
  cursor: not-allowed;
}

.password-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.password-wrapper .form-input {
  padding-right: 40px;
}

.toggle-pwd {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  color: var(--color-text-secondary);
  padding: 4px;
  display: flex;
  align-items: center;
  border-radius: var(--radius-sm);
  transition: color var(--transition-fast);
}

.toggle-pwd:hover {
  color: var(--color-accent);
}

.w-full {
  width: 100%;
}

.btn-primary {
  padding: 12px 20px;
  background: var(--color-primary);
  color: var(--color-text-white);
  border: none;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.btn-primary:hover:not(:disabled) {
  background: var(--color-primary-dark);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(13, 33, 55, 0.2);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  padding: 12px 20px;
  background: var(--color-bg-white);
  color: var(--color-primary);
  border: 2px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.btn-secondary:hover:not(:disabled) {
  border-color: var(--color-accent);
  color: var(--color-accent);
  background: rgba(90, 127, 168, 0.05);
}

.btn-secondary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }

  .profile-overview {
    padding: var(--spacing-xl);
  }

  .stats-row {
    flex-direction: column;
    gap: var(--spacing-md);
    padding: var(--spacing-md);
  }

  .stat-divider {
    width: 60px;
    height: 1px;
  }

  .avatar-section {
    flex-direction: column;
    text-align: center;
  }
}
</style>
