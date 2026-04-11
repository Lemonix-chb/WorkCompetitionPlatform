<template>
  <div class="profile-page">
    <div class="container container-sm">
      <!-- Page Header -->
      <div class="page-header slide-in">
        <h1 class="page-title">个人设置</h1>
        <p class="body-text">修改个人信息</p>
      </div>

      <!-- Profile Form -->
      <div class="profile-form card scale-in">
        <div class="form-placeholder">
          <div class="empty-icon mb-lg">
            <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
              <circle cx="24" cy="16" r="12" stroke="#bdc3c7" stroke-width="2"/>
              <path d="M12 40C12 34 18 30 24 30C30 30 36 34 36 40" stroke="#bdc3c7" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <h3 class="card-title mb-md">个人信息设置</h3>
          <p class="caption">功能开发中...</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showWarning } from '@/utils/messageUtils'

const router = useRouter()

onMounted(async () => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('userRole')

  if (!token || role !== 'JUDGE') {
    showWarning('您没有权限访问此页面')
    router.push('/login')
  }
})
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
}

.page-header {
  margin-bottom: var(--spacing-2xl);
}

.profile-form {
  padding: var(--spacing-2xl);
}

.form-placeholder {
  text-align: center;
  padding: var(--spacing-xl) 0;
}

.empty-icon {
  opacity: 0.5;
}
</style>