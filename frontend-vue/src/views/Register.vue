<template>
  <div class="register-page">
    <div class="academic-pattern"></div>

    <div class="register-container scale-in">
      <div class="register-card">
        <!-- Header -->
        <div class="register-header">
          <div class="academic-crest">
            <svg width="72" height="72" viewBox="0 0 80 80" fill="none">
              <path d="M40 4L72 16V40C72 60 56 72 40 76C24 72 8 60 8 40V16L40 4Z"
                    fill="var(--color-primary)"
                    stroke="var(--color-accent)"
                    stroke-width="2"/>
              <circle cx="40" cy="36" r="16" stroke="var(--color-accent)" stroke-width="1.5" fill="none"/>
              <path d="M40 24L41.5 29.5L47 29.5L42.5 33L44 38.5L40 35L36 38.5L37.5 33L33 29.5L38.5 29.5L40 24Z"
                    fill="var(--color-accent)"/>
              <line x1="28" y1="52" x2="52" y2="52" stroke="var(--color-accent)" stroke-width="1.5" stroke-linecap="round"/>
              <line x1="30" y1="56" x2="50" y2="56" stroke="var(--color-accent)" stroke-width="1" stroke-linecap="round"/>
            </svg>
          </div>
          <h1 class="page-title">创建账号</h1>
          <p class="register-subtitle">Join the Work Competition Platform</p>
        </div>

        <!-- Form -->
        <form @submit.prevent="handleRegister" class="register-form">
          <!-- Username -->
          <div class="form-group">
            <label class="form-label">
              <span class="label-icon">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <circle cx="8" cy="5" r="3" stroke="currentColor" stroke-width="1.5"/>
                  <path d="M2 14C2 11 5 9 8 9C11 9 14 11 14 14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
              </span>
              用户名 *
            </label>
            <input v-model="form.username" type="text" class="form-input" placeholder="6-20个字符" required minlength="6" maxlength="20" />
          </div>

          <!-- Password -->
          <div class="form-group">
            <label class="form-label">
              <span class="label-icon">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <rect x="2" y="6" width="12" height="8" rx="2" stroke="currentColor" stroke-width="1.5"/>
                  <circle cx="8" cy="10" r="1.5" fill="currentColor"/>
                  <path d="M4 6V4C4 2 6 1 8 1C10 1 12 2 12 4V6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
              </span>
              密码 *
            </label>
            <div class="password-wrapper">
              <input v-model="form.password" :type="showPassword ? 'text' : 'password'" class="form-input" placeholder="6-40个字符" required minlength="6" maxlength="40" />
              <button type="button" class="toggle-password" @click="showPassword = !showPassword">
                <svg v-if="!showPassword" width="18" height="18" viewBox="0 0 18 18" fill="none">
                  <path d="M2 9C2 9 5 4 9 4C13 4 16 9 16 9C16 9 13 14 9 14C5 14 2 9 2 9Z" stroke="currentColor" stroke-width="1.3"/>
                  <circle cx="9" cy="9" r="2.5" stroke="currentColor" stroke-width="1.3"/>
                </svg>
                <svg v-else width="18" height="18" viewBox="0 0 18 18" fill="none">
                  <path d="M6.5 3.5C7.3 3.2 8.1 3 9 3C13 3 16 9 16 9C16 9 14.5 12 11.5 13.5M10.5 5.5C11.6 6 12.5 7.3 12.5 9C12.5 11 10.9 12.5 9 12.5M4 6L14 14M2 9C2 9 5 4 9 4M3.5 7.5L7 11L11 15" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
                </svg>
              </button>
            </div>
          </div>

          <!-- Confirm Password -->
          <div class="form-group">
            <label class="form-label">
              <span class="label-icon">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <rect x="2" y="6" width="12" height="8" rx="2" stroke="currentColor" stroke-width="1.5"/>
                  <path d="M5 9L7.5 11.5L11.5 7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M4 6V4C4 2 6 1 8 1C10 1 12 2 12 4V6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
              </span>
              确认密码 *
            </label>
            <div class="password-wrapper">
              <input v-model="confirmPassword" :type="showConfirm ? 'text' : 'password'" class="form-input" placeholder="请再次输入密码" required />
              <button type="button" class="toggle-password" @click="showConfirm = !showConfirm">
                <svg v-if="!showConfirm" width="18" height="18" viewBox="0 0 18 18" fill="none">
                  <path d="M2 9C2 9 5 4 9 4C13 4 16 9 16 9C16 9 13 14 9 14C5 14 2 9 2 9Z" stroke="currentColor" stroke-width="1.3"/>
                  <circle cx="9" cy="9" r="2.5" stroke="currentColor" stroke-width="1.3"/>
                </svg>
                <svg v-else width="18" height="18" viewBox="0 0 18 18" fill="none">
                  <path d="M6.5 3.5C7.3 3.2 8.1 3 9 3C13 3 16 9 16 9C16 9 14.5 12 11.5 13.5M10.5 5.5C11.6 6 12.5 7.3 12.5 9C12.5 11 10.9 12.5 9 12.5M4 6L14 14M2 9C2 9 5 4 9 4M3.5 7.5L7 11L11 15" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
                </svg>
              </button>
            </div>
          </div>

          <!-- Real Name -->
          <div class="form-group">
            <label class="form-label">
              <span class="label-icon">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <rect x="2" y="2" width="12" height="12" rx="2" stroke="currentColor" stroke-width="1.3"/>
                  <path d="M5 6H11M5 9H10M5 12H8" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
                </svg>
              </span>
              真实姓名 *
            </label>
            <input v-model="form.realName" type="text" class="form-input" placeholder="请输入真实姓名" required />
          </div>

          <!-- Email -->
          <div class="form-group">
            <label class="form-label">
              <span class="label-icon">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <rect x="1" y="3" width="14" height="10" rx="2" stroke="currentColor" stroke-width="1.3"/>
                  <path d="M1 4L7.5 8.5L14 4" stroke="currentColor" stroke-width="1.3" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </span>
              邮箱 *
            </label>
            <input v-model="form.email" type="email" class="form-input" placeholder="请输入邮箱地址" required />
          </div>

          <!-- Phone + Student No on one row -->
          <div class="form-row">
            <div class="form-group form-half">
              <label class="form-label">手机号</label>
              <input v-model="form.phone" type="text" class="form-input" placeholder="选填" />
            </div>
            <div class="form-group form-half">
              <label class="form-label">学号 *</label>
              <input v-model="form.studentNo" type="text" class="form-input" placeholder="请输入学号" required />
            </div>
          </div>

          <!-- College + Major -->
          <div class="form-row">
            <div class="form-group form-half">
              <label class="form-label">学院</label>
              <input v-model="form.college" type="text" class="form-input" placeholder="选填" />
            </div>
            <div class="form-group form-half">
              <label class="form-label">专业</label>
              <input v-model="form.major" type="text" class="form-input" placeholder="选填" />
            </div>
          </div>

          <!-- Submit -->
          <button type="submit" class="btn-register" :disabled="loading">
            <span v-if="!loading" class="btn-content">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M8 4L14 10L8 16" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <span class="btn-text">提交注册</span>
            </span>
            <span v-else class="loading-text">
              <svg class="spinner" width="20" height="20" viewBox="0 0 20 20" fill="none">
                <circle cx="10" cy="10" r="8" stroke="currentColor" stroke-width="2" stroke-dasharray="40" stroke-dashoffset="10"/>
              </svg>
              正在提交...
            </span>
          </button>
        </form>

        <!-- Login Link -->
        <div class="register-footer">
          <div class="footer-divider">
            <span class="divider-text">已有账号</span>
          </div>
          <router-link to="/login" class="login-link">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M12 8H4M4 8L7 5M4 8L7 11" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span>返回登录</span>
          </router-link>
        </div>
      </div>

      <!-- Badge -->
      <div class="academic-badge">
        <span class="badge-year">2026</span>
        <span class="badge-text">湖南农业大学 · 计算机科学竞赛</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { post } from '@/utils/api'
import { showSuccess, showError, showWarning } from '@/utils/messageUtils'

const router = useRouter()

const form = ref({
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: '',
  college: '',
  major: '',
  studentNo: '',
  role: 'STUDENT'
})

const confirmPassword = ref('')
const loading = ref(false)
const showPassword = ref(false)
const showConfirm = ref(false)

const handleRegister = async () => {
  if (!form.value.username || !form.value.password || !form.value.realName || !form.value.email || !form.value.studentNo) {
    showWarning('请填写所有必填字段')
    return
  }

  if (form.value.username.length < 6 || form.value.username.length > 20) {
    showWarning('用户名长度必须在6-20个字符之间')
    return
  }

  if (form.value.password.length < 6) {
    showWarning('密码长度至少6个字符')
    return
  }

  if (form.value.password !== confirmPassword.value) {
    showWarning('两次输入的密码不一致')
    return
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(form.value.email)) {
    showWarning('请输入正确的邮箱格式')
    return
  }

  loading.value = true

  try {
    await post('/auth/register', form.value)
    showSuccess('注册成功，请等待管理员审核')
    router.push('/login')
  } catch (error) {
    console.error('Register error:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: var(--color-bg-main);
}

.academic-pattern {
  position: absolute;
  inset: 0;
  opacity: 0.03;
  background-image:
    repeating-linear-gradient(45deg, var(--color-primary) 0px, var(--color-primary) 2px, transparent 2px, transparent 20px),
    repeating-linear-gradient(-45deg, var(--color-primary) 0px, var(--color-primary) 2px, transparent 2px, transparent 20px);
}

.register-container {
  width: 100%;
  max-width: 520px;
  padding: var(--spacing-xl);
  z-index: 10;
}

.register-card {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: var(--spacing-3xl) var(--spacing-2xl);
  box-shadow: 0 0 0 1px var(--color-border-light), 0 12px 40px rgba(13, 33, 55, 0.15);
  border-top: 4px solid var(--color-accent);
}

.register-header {
  text-align: center;
  margin-bottom: var(--spacing-2xl);
}

.academic-crest {
  margin-bottom: var(--spacing-lg);
  display: inline-flex;
  filter: drop-shadow(0 0 8px rgba(90, 127, 168, 0.3));
}

.register-header h1 {
  font-family: var(--font-display);
  font-size: 24px;
  font-weight: 600;
  color: var(--color-primary);
  letter-spacing: -0.02em;
  margin-bottom: var(--spacing-xs);
  padding-bottom: var(--spacing-md);
  position: relative;
}

.register-header h1::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  background: var(--color-accent);
  border-radius: var(--radius-sm);
}

.register-subtitle {
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-label {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-xs);
}

.label-icon {
  display: flex;
  align-items: center;
  color: var(--color-text-secondary);
}

.form-input {
  font-family: var(--font-primary);
  font-size: 14px;
  padding: 11px 14px;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-bg-white);
  color: var(--color-text-primary);
  width: 100%;
  transition: all var(--transition-fast);
}

.form-input:hover {
  border-color: var(--color-text-tertiary);
}

.form-input:focus {
  outline: none;
  border-color: var(--color-accent);
  box-shadow: 0 0 0 3px rgba(90, 127, 168, 0.12);
}

.form-input::placeholder {
  color: var(--color-text-light);
}

.password-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.password-wrapper .form-input {
  padding-right: 44px;
}

.toggle-password {
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

.toggle-password:hover {
  color: var(--color-accent);
}

.form-row {
  display: flex;
  gap: var(--spacing-md);
}

.form-half {
  flex: 1;
}

.btn-register {
  width: 100%;
  margin-top: var(--spacing-sm);
  padding: 14px 24px;
  border: none;
  border-radius: var(--radius-md);
  background: var(--color-primary);
  color: var(--color-text-white);
  font-family: var(--font-body);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
}

.btn-register:hover:not(:disabled) {
  background: var(--color-primary-dark);
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(13, 33, 55, 0.25);
}

.btn-register:active:not(:disabled) {
  transform: translateY(0);
}

.btn-register:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-content {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.register-footer {
  margin-top: var(--spacing-xl);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.footer-divider {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  padding: var(--spacing-sm) 0 var(--spacing-md);
}

.footer-divider::before,
.footer-divider::after {
  content: '';
  width: 50px;
  height: 1px;
  background: var(--color-border);
}

.divider-text {
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
  padding: 0 var(--spacing-sm);
  letter-spacing: 0.1em;
}

.login-link {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  padding: 10px 20px;
  border-radius: var(--radius-md);
  text-decoration: none;
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--color-accent);
  background: rgba(90, 127, 168, 0.08);
  border: 1px solid rgba(90, 127, 168, 0.2);
  transition: all var(--transition-fast);
}

.login-link:hover {
  background: rgba(90, 127, 168, 0.12);
  border-color: var(--color-accent);
  transform: translateY(-1px);
}

.academic-badge {
  text-align: center;
  margin-top: var(--spacing-xl);
  padding: var(--spacing-md);
  border-radius: var(--radius-md);
  background: rgba(13, 33, 55, 0.04);
  border: 1px solid var(--color-border-light);
}

.badge-year {
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  font-weight: 700;
  color: var(--color-accent);
  letter-spacing: -0.04em;
  margin-right: var(--spacing-sm);
}

.badge-text {
  font-family: var(--font-body);
  font-size: var(--text-xs);
  color: var(--color-text-secondary);
  letter-spacing: 0.02em;
}

@media (max-width: 600px) {
  .register-container {
    padding: var(--spacing-lg);
  }

  .register-card {
    padding: var(--spacing-xl) var(--spacing-lg);
  }

  .form-row {
    flex-direction: column;
    gap: var(--spacing-md);
  }

  .register-header h1 {
    font-size: 22px;
  }
}
</style>
