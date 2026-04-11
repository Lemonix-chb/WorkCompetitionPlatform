<template>
  <div class="login-page">
    <div class="login-container">
      <!-- Login Form -->
      <div class="login-card card scale-in">
        <div class="login-header">
          <div class="logo-icon">
            <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
              <rect width="48" height="48" rx="12" fill="#5a7fa8"/>
              <path d="M12 24H36M24 12V36" stroke="white" stroke-width="3" stroke-linecap="round"/>
            </svg>
          </div>
          <h1 class="page-title">登录</h1>
          <p class="caption">竞赛管理系统</p>
        </div>

        <form @submit.prevent="handleLogin" class="login-form">
          <!-- Username Input -->
          <div class="form-group">
            <label class="form-label">用户名 / 学号 / 工号</label>
            <input
              v-model="form.username"
              type="text"
              class="form-input"
              placeholder="请输入用户名、学号或工号"
              required
            />
          </div>

          <!-- Password Input -->
          <div class="form-group">
            <label class="form-label">密码</label>
            <input
              v-model="form.password"
              type="password"
              class="form-input"
              placeholder="请输入密码"
              required
            />
          </div>

          <!-- Submit Button -->
          <button type="submit" class="btn-primary w-full" :disabled="loading">
            <svg v-if="!loading" width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M7 5L12 10L7 15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </form>

        <!-- Register Link -->
        <div class="login-footer">
          <router-link to="/register" class="link">
            没有账号？立即注册
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

const form = ref({
  username: '',
  password: ''
})

const loading = ref(false)

onMounted(() => {
  // 如果用户已登录，直接跳转到对应页面
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
      router.push('/')
    }
  }
})

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请填写用户名和密码')
    return
  }

  loading.value = true

  try {
    const response = await fetch('/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(form.value)
    })

    const data = await response.json()

    if (data.code === 200 && data.data) {
      // 保存token和用户信息
      localStorage.setItem('token', data.data.token)
      localStorage.setItem('userId', data.data.userId)
      localStorage.setItem('userName', data.data.realName || form.value.username)
      localStorage.setItem('userRole', data.data.role)

      ElMessage.success('登录成功')

      // 获取redirect参数或根据角色跳转
      const redirect = route.query.redirect
      if (redirect) {
        router.push(redirect)
      } else {
        // 根据角色跳转到对应主页（角色为大写的STUDENT/JUDGE/ADMIN）
        if (data.data.role === 'STUDENT') {
          router.push('/student')
        } else if (data.data.role === 'JUDGE') {
          router.push('/judge')
        } else if (data.data.role === 'ADMIN') {
          router.push('/admin')
        } else {
          router.push('/')
        }
      }
    } else {
      ElMessage.error(data.message || '登录失败')
    }
  } catch (error) {
    console.error('Login error:', error)
    ElMessage.error('登录失败，请检查网络连接')
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
  background: var(--color-bg-main);
  padding: var(--spacing-xl);
}

.login-container {
  width: 100%;
  max-width: 420px;
}

.login-card {
  padding: var(--spacing-2xl);
}

.login-header {
  text-align: center;
  margin-bottom: var(--spacing-2xl);
}

.logo-icon {
  margin-bottom: var(--spacing-lg);
}

.login-header h1 {
  margin-bottom: var(--spacing-sm);
}

.login-header p {
  color: var(--color-text-secondary);
}

.login-form {
  margin-bottom: var(--spacing-xl);
}

.login-footer {
  text-align: center;
  padding-top: var(--spacing-lg);
  border-top: 1px solid var(--color-border-light);
}

@media (max-width: 768px) {
  .login-page {
    padding: var(--spacing-lg);
  }

  .login-card {
    padding: var(--spacing-xl);
  }
}
</style>