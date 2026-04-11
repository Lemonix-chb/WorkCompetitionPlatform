<template>
  <div class="stats-page">
    <div class="container">
      <!-- Page Header -->
      <div class="page-header slide-in">
        <h1 class="page-title">评审统计</h1>
        <p class="body-text">查看评审数据分析</p>
      </div>

      <!-- Stats Content -->
      <div class="stats-section scale-in">
        <div class="empty-state card">
          <div class="empty-icon">
            <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
              <rect x="8" y="28" width="8" height="16" rx="2" stroke="#bdc3c7" stroke-width="2"/>
              <rect x="18" y="20" width="8" height="24" rx="2" stroke="#bdc3c7" stroke-width="2"/>
              <rect x="28" y="12" width="8" height="32" rx="2" stroke="#bdc3c7" stroke-width="2"/>
            </svg>
          </div>
          <h3 class="card-title mb-md">评审数据统计</h3>
          <p class="caption">功能开发中...</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()

onMounted(async () => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('userRole')

  if (!token || role !== 'JUDGE') {
    ElMessage.warning('您没有权限访问此页面')
    router.push('/login')
  }
})
</script>

<style scoped>
.stats-page {
  min-height: 100vh;
}

.page-header {
  margin-bottom: var(--spacing-2xl);
}

.stats-section {
  animation-delay: 0.1s;
}

.empty-state {
  padding: var(--spacing-3xl);
  text-align: center;
}

.empty-icon {
  margin-bottom: var(--spacing-lg);
  opacity: 0.5;
}
</style>