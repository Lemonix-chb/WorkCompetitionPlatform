<template>
  <div class="login-page">
    <!-- Academic Background Pattern -->
    <div class="academic-pattern"></div>

    <!-- Login Card -->
    <div class="login-container scale-in">
      <div class="login-card">
        <!-- Header with Academic Crest -->
        <div class="login-header">
          <div class="academic-crest">
            <svg width="80" height="80" viewBox="0 0 80 80" fill="none">
              <!-- Shield Shape -->
              <path d="M40 4L72 16V40C72 60 56 72 40 76C24 72 8 60 8 40V16L40 4Z"
                    fill="var(--color-primary)"
                    stroke="var(--color-accent)"
                    stroke-width="2"/>
              <!-- Inner Emblem -->
              <circle cx="40" cy="38" r="18" stroke="var(--color-accent)" stroke-width="2" fill="none"/>
              <!-- Laurels -->
              <path d="M24 50C24 50 28 44 32 48M48 48C48 48 52 44 56 50"
                    stroke="var(--color-accent)"
                    stroke-width="1.5"
                    stroke-linecap="round"/>
              <!-- Star Award -->
              <path d="M40 26L42 32L48 32L43 36L45 42L40 38L35 42L37 36L32 32L38 32L40 26Z"
                    fill="var(--color-accent)"/>
              <!-- Academic Text Lines -->
              <line x1="28" y1="54" x2="52" y2="54" stroke="var(--color-accent)" stroke-width="1.5" stroke-linecap="round"/>
              <line x1="30" y1="58" x2="50" y2="58" stroke="var(--color-accent)" stroke-width="1" stroke-linecap="round"/>
              <line x1="32" y1="62" x2="48" y2="62" stroke="var(--color-accent)" stroke-width="1" stroke-linecap="round"/>
            </svg>
          </div>

          <h1 class="page-title">作品评审管理系统</h1>
          <p class="login-subtitle">Work Competition Review Platform</p>
        </div>

        <!-- Login Form -->
        <form @submit.prevent="handleLogin" class="login-form">
          <!-- Username Field -->
          <div class="form-group">
            <label class="form-label">
              <span class="label-icon">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <circle cx="8" cy="5" r="3" stroke="var(--color-text-secondary)" stroke-width="1.5"/>
                  <path d="M2 14C2 11 5 9 8 9C11 9 14 11 14 14"
                        stroke="var(--color-text-secondary)"
                        stroke-width="1.5"
                        stroke-linecap="round"/>
                </svg>
              </span>
              用户名 / 学号 / 工号
            </label>
            <input
              v-model="form.username"
              type="text"
              class="form-input"
              placeholder="请输入您的账号"
              required
            />
          </div>

          <!-- Password Field -->
          <div class="form-group">
            <label class="form-label">
              <span class="label-icon">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <rect x="2" y="6" width="12" height="8" rx="2" stroke="var(--color-text-secondary)" stroke-width="1.5"/>
                  <circle cx="8" cy="10" r="1.5" fill="var(--color-text-secondary)"/>
                  <path d="M4 6V4C4 2 6 1 8 1C10 1 12 2 12 4V6"
                        stroke="var(--color-text-secondary)"
                        stroke-width="1.5"
                        stroke-linecap="round"/>
                </svg>
              </span>
              密码
            </label>
            <input
              v-model="form.password"
              type="password"
              class="form-input"
              placeholder="请输入您的密码"
              required
            />
          </div>

          <!-- Submit Button -->
          <button type="submit" class="btn-login" :disabled="loading">
            <span v-if="!loading" class="btn-content">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M16 10L4 10M4 10L8 6M4 10L8 14"
                      stroke="currentColor"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"/>
              </svg>
              <span class="btn-text">登录系统</span>
            </span>
            <span v-else class="loading-text">
              <svg class="spinner" width="20" height="20" viewBox="0 0 20 20" fill="none">
                <circle cx="10" cy="10" r="8" stroke="var(--color-accent)" stroke-width="2" stroke-dasharray="40" stroke-dashoffset="10"/>
              </svg>
              正在验证...
            </span>
          </button>
        </form>

        <!-- Footer Links -->
        <div class="login-footer">
          <div class="footer-divider">
            <span class="divider-text">首次使用</span>
          </div>

          <router-link to="/register" class="register-link">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M8 2V14M2 8H14"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"/>
            </svg>
            <span>立即注册账号</span>
          </router-link>

          <router-link to="/competitions" class="guest-link">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.5"/>
              <path d="M8 5V8L10 10"
                    stroke="currentColor"
                    stroke-width="1.5"
                    stroke-linecap="round"/>
            </svg>
            <span>浏览赛事信息</span>
          </router-link>
        </div>
      </div>

      <!-- Academic Year Badge -->
      <div class="academic-badge">
        <span class="badge-year">2026</span>
        <span class="badge-text">湖南农业大学 · 计算机科学竞赛</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { post } from '@/utils/api'
import { showSuccess, showError, showWarning } from '@/utils/messageUtils'

const router = useRouter()
const route = useRoute()

const form = ref({
  username: '',
  password: ''
})

const loading = ref(false)

onMounted(() => {
  const token = localStorage.getItem('token')
  if (token) {
    const role = localStorage.getItem('userRole')
    if (role === 'STUDENT') {
      router.push('/student')
    } else if (role === 'JUDGE') {
      router.push('/judge')
    } else if (role === 'ADMIN') {
      router.push('/admin')
    } else {
      localStorage.clear()
    }
  }
})

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    showWarning('请填写用户名和密码')
    return
  }

  loading.value = true

  try {
    const data = await post('/auth/login', form.value)

    localStorage.setItem('token', data.token)
    localStorage.setItem('userId', data.userId)
    localStorage.setItem('userName', data.realName || form.value.username)
    localStorage.setItem('userRole', data.role)

    showSuccess('登录成功')

    const redirect = route.query.redirect
    if (redirect) {
      router.push(redirect)
    } else {
      if (data.role === 'STUDENT') {
        router.push('/student')
      } else if (data.role === 'JUDGE') {
        router.push('/judge')
      } else if (data.role === 'ADMIN') {
        router.push('/admin')
      } else {
        localStorage.clear()
      }
    }
  } catch (error) {
    console.error('Login error:', error)
    showError('登录失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
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
    repeating-linear-gradient(
      45deg,
      var(--color-primary) 0px,
      var(--color-primary) 2px,
      transparent 2px,
      transparent 20px
    ),
    repeating-linear-gradient(
      -45deg,
      var(--color-primary) 0px,
      var(--color-primary) 2px,
      transparent 2px,
      transparent 20px
    );
}

.login-container {
  width: 100%;
  max-width: 480px;
  padding: var(--spacing-xl);
  z-index: 10;
}

.login-card {
  background: var(--color-bg-card);
  border-radius: var(--radius-2xl);
  padding: var(--spacing-3xl);
  box-shadow:
    0 0 0 1px var(--color-border-light),
    0 12px 40px rgba(13, 33, 55, 0.15);
  border-top: 4px solid var(--color-accent);
}

.login-header {
  text-align: center;
  margin-bottom: var(--spacing-2xl);
}

.academic-crest {
  margin-bottom: var(--spacing-xl);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  position: relative;
  filter: drop-shadow(0 0 8px rgba(52, 152, 219, 0.3));
}

.login-header h1 {
  font-family: var(--font-display);
  font-size: var(--text-4xl);
  font-weight: 600;
  color: var(--color-primary);
  letter-spacing: -0.02em;
  margin-bottom: var(--spacing-sm);
  position: relative;
  padding-bottom: var(--spacing-md);
}

.login-header h1::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 3px;
  background: var(--color-accent);
  border-radius: var(--radius-sm);
}

.login-subtitle {
  font-family: var(--font-body);
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.login-form {
  margin-bottom: var(--spacing-xl);
}

.form-group {
  margin-bottom: var(--spacing-lg);
}

.form-label {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-family: var(--font-body);
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-sm);
  letter-spacing: 0.01em;
}

.label-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.form-input {
  font-family: var(--font-body);
  font-size: var(--text-base);
  padding: 14px 18px;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-lg);
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
  box-shadow:
    0 0 0 4px rgba(52, 152, 219, 0.12),
    0 2px 8px rgba(13, 33, 55, 0.08);
}

.form-input::placeholder {
  color: var(--color-text-light);
}

.btn-login {
  width: 100%;
  padding: 14px 24px;
  border: none;
  border-radius: var(--radius-lg);
  background: var(--color-primary);
  color: var(--color-text-white);
  font-family: var(--font-body);
  font-size: var(--text-base);
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
}

.btn-login:hover:not(:disabled) {
  background: var(--color-primary-medium);
  transform: translateY(-2px);
  box-shadow:
    0 0 0 1px var(--color-accent),
    0 8px 20px rgba(13, 33, 55, 0.2),
    0 0 20px rgba(52, 152, 219, 0.15);
}

.btn-login:active:not(:disabled) {
  transform: translateY(0);
}

.btn-login:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-content {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.btn-text {
  letter-spacing: 0.02em;
}

.loading-text {
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

.login-footer {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.footer-divider {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-md) 0;
}

.footer-divider::before,
.footer-divider::after {
  content: '';
  width: 60px;
  height: 1px;
  background: var(--color-border);
}

.divider-text {
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
  padding: 0 var(--spacing-sm);
  letter-spacing: 0.1em;
}

.register-link,
.guest-link {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  padding: 12px 20px;
  border-radius: var(--radius-md);
  text-decoration: none;
  font-size: var(--text-sm);
  font-weight: 500;
  transition: all var(--transition-fast);
}

.register-link {
  background: rgba(52, 152, 219, 0.08);
  color: var(--color-accent-dark);
  border: 1px solid rgba(52, 152, 219, 0.2);
}

.register-link:hover {
  background: rgba(52, 152, 219, 0.12);
  border-color: var(--color-accent);
  transform: translateY(-1px);
}

.guest-link {
  background: transparent;
  color: var(--color-text-secondary);
  border: 1px solid var(--color-border);
}

.guest-link:hover {
  color: var(--color-accent);
  border-color: var(--color-accent);
  background: rgba(52, 152, 219, 0.05);
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

@media (max-width: 768px) {
  .login-container {
    padding: var(--spacing-lg);
  }

  .login-card {
    padding: var(--spacing-xl);
    border-radius: var(--radius-xl);
  }

  .login-header h1 {
    font-size: var(--text-3xl);
  }

  .academic-crest svg {
    width: 64px;
    height: 64px;
  }
}

@media (max-width: 480px) {
  .login-header h1 {
    font-size: var(--text-2xl);
  }

  .login-card {
    padding: var(--spacing-lg);
  }

  .academic-badge {
    font-size: var(--text-xs);
  }
}
</style>