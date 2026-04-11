<template>
  <div class="results-page">
    <!-- Hero -->
    <section class="section-dark">
      <div class="container">
        <h1 class="hero-title">成绩查询</h1>
        <h2 class="subheading">查看作品评审结果</h2>
      </div>
    </section>

    <!-- Results List -->
    <section class="section-light">
      <div class="container">
        <div v-if="results.length === 0" class="empty-state text-center">
          <p class="body-text">暂无评审结果</p>
        </div>

        <div v-else class="grid grid-auto">
          <div v-for="result in results" :key="result.id" class="card">
            <h4 class="tile-heading mb-md">{{ result.workName }}</h4>
            <div class="result-info mb-lg">
              <p class="body-text">赛道：{{ result.trackName }}</p>
              <p class="body-text">最终得分：{{ result.finalScore }}</p>
              <p class="body-text">奖项：{{ result.awardLevel }}</p>
              <p class="body-text">排名：第{{ result.rank }}名</p>
            </div>
            <button class="btn-pill" @click="viewDetail(result)">查看详情</button>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const results = ref([])

onMounted(async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
  }

  // TODO: 从后端获取评审结果
  // await fetchResults()
})

const viewDetail = (result) => {
  ElMessage.info('详情功能开发中...')
}

const fetchResults = async () => {
  try {
    const response = await fetch('/api/student/results')
    const data = await response.json()
    if (data.code === 200) {
      results.value = data.data || []
    }
  } catch (error) {
    console.error('获取成绩失败', error)
  }
}
</script>

<style scoped>
.results-page {
  min-height: 100vh;
}

.result-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.empty-state {
  padding: 64px 0;
}
</style>