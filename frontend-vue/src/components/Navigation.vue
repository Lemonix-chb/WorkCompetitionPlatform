<template>
  <nav class="nav">
    <div class="nav-container">
      <!-- Logo -->
      <router-link to="/" class="nav-logo">
        <span class="logo-text">竞赛平台</span>
      </router-link>

      <!-- Links -->
      <div class="nav-links">
        <router-link to="/competitions" class="nav-link">赛事</router-link>
        <router-link v-if="isLoggedIn" :to="userHomePath" class="nav-link">我的</router-link>
        <router-link v-if="!isLoggedIn" to="/login" class="nav-link">登录</router-link>
      </div>

      <!-- User Actions -->
      <div class="nav-actions">
        <button v-if="isLoggedIn" class="nav-button" @click="logout">
          <span class="user-name">{{ userName }}</span>
          <span class="logout-text">退出</span>
        </button>
        <router-link v-else to="/login" class="nav-link-icon">
          <el-icon><User /></el-icon>
        </router-link>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'

const router = useRouter()

const isLoggedIn = computed(() => localStorage.getItem('token'))
const userName = computed(() => localStorage.getItem('userName') || '用户')
const userRole = computed(() => localStorage.getItem('userRole'))

// 根据角色返回对应的中心页面路径
const userHomePath = computed(() => {
  const role = userRole.value
  if (role === 'STUDENT') {
    return '/student'
  } else if (role === 'JUDGE') {
    return '/judge'
  } else if (role === 'ADMIN') {
    return '/admin'
  }
  return '/'
})

const logout = () => {
  localStorage.clear()
  ElMessage.success('已退出登录')
  router.push('/')
}
</script>

<style scoped>
.nav {
  position: sticky;
  top: 0;
  z-index: 1000;
  height: 48px;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: saturate(180%) blur(20px);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
  width: 100%;
}

.nav-container {
  max-width: 980px;
  height: 100%;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.nav-logo {
  text-decoration: none;
}

.logo-text {
  font-family: var(--font-display);
  font-size: 17px;
  font-weight: 600;
  color: #ffffff;
  letter-spacing: -0.374px;
}

.nav-links {
  display: flex;
  gap: 24px;
  flex: 1;
  justify-content: center;
}

.nav-link {
  font-family: var(--font-text);
  font-size: 12px;
  font-weight: 400;
  color: #ffffff;
  text-decoration: none;
  line-height: 48px;
  padding: 0 8px;
  transition: all 0.3s ease;
  border-bottom: 1px solid transparent;
}

.nav-link:hover {
  border-bottom-color: rgba(255, 255, 255, 0.8);
}

.nav-link.router-link-active {
  border-bottom-color: #ffffff;
}

.nav-actions {
  flex: 0 0 auto;
}

.nav-button {
  font-family: var(--font-text);
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  background: transparent;
  border: none;
  cursor: pointer;
  display: flex;
  gap: 8px;
  align-items: center;
}

.nav-button:hover {
  opacity: 0.7;
}

.logout-text {
  color: rgba(255, 255, 255, 0.48);
}

.nav-link-icon {
  color: #ffffff;
  font-size: 16px;
  line-height: 48px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.nav-link-icon:hover {
  opacity: 0.8;
}

@media (max-width: 640px) {
  .nav-container {
    padding: 0 16px;
  }

  .nav-links {
    gap: 16px;
  }
}
</style>