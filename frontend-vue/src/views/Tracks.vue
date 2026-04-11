<template>
  <div class="tracks-page">
    <!-- Hero Section -->
    <section class="section-dark">
      <div class="container">
        <button
          v-if="route.query.from === 'admin'"
          class="btn-secondary mb-lg"
          @click="router.push('/admin/competitions')"
        >
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M10 4L16 10L10 16" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          返回赛事管理
        </button>
        <router-link
          v-else
          to="/competitions"
          class="link link-dark link-learn-more mb-lg"
        >
          返回赛事列表
        </router-link>
        <h1 v-if="currentCompetition" class="hero-title" style="font-size: 40px; line-height: 1.10;">
          {{ currentCompetition.competitionName }}
        </h1>
        <h2 class="subheading">选择参赛赛道</h2>
      </div>
    </section>

    <!-- Tracks Section -->
    <section class="section-light">
      <div class="container">
        <h3 class="section-heading">参赛赛道</h3>

        <!-- Loading -->
        <div v-if="loading" class="loading-state flex flex-center">
          <div class="spinner"></div>
        </div>

        <!-- Error -->
        <div v-if="error" class="error-state text-center">
          <p class="body-text">{{ error }}</p>
        </div>

        <!-- Tracks Grid -->
        <div v-else-if="tracks.length > 0" class="grid grid-auto">
          <div v-for="track in tracks" :key="track.id" class="card">
            <!-- Badge -->
            <div class="track-badge mb-md">
              <span class="badge-icon">{{ getTrackIcon(track.trackType) }}</span>
              <span class="badge-text">{{ getTrackTypeText(track.trackType) }}</span>
            </div>

            <!-- Title -->
            <h4 class="tile-heading mb-md">{{ track.trackName }}</h4>

            <!-- Description -->
            <p class="body-text mb-lg">{{ track.description }}</p>

            <!-- Specs -->
            <div class="specs-grid mb-lg">
              <div class="spec-item">
                <span class="caption">团队人数</span>
                <span class="card-title">{{ track.minTeamSize }} - {{ track.maxTeamSize }} 人</span>
              </div>

              <div class="spec-item">
                <span class="caption">提交邮箱</span>
                <span class="body-text">{{ track.submissionEmail }}</span>
              </div>

              <div class="spec-item">
                <span class="caption">文件限制</span>
                <span class="body-text">{{ track.maxFileSizeMb }} MB</span>
              </div>

              <!-- PPT特有 -->
              <div v-if="track.minPages" class="spec-item spec-highlight">
                <span class="caption">页数要求</span>
                <span class="body-text">至少 {{ track.minPages }} 页</span>
              </div>

              <!-- PPT/VIDEO -->
              <div v-if="track.aspectRatio" class="spec-item">
                <span class="caption">画面比例</span>
                <span class="body-text">{{ track.aspectRatio }}</span>
              </div>

              <!-- VIDEO特有 -->
              <div v-if="track.minDurationSec" class="spec-item spec-highlight">
                <span class="caption">时长要求</span>
                <span class="body-text">{{ track.minDurationSec }} - {{ track.maxDurationSec }} 秒</span>
              </div>

              <div v-if="track.resolution" class="spec-item">
                <span class="caption">分辨率</span>
                <span class="body-text">{{ track.resolution }}</span>
              </div>

              <!-- File Types -->
              <div class="spec-item spec-full">
                <span class="caption">允许格式</span>
                <span class="caption">{{ track.allowedFileTypes }}</span>
              </div>
            </div>

            <!-- Submission Format -->
            <div class="track-section mb-md">
              <h5 class="section-title">提交格式</h5>
              <p class="caption">{{ track.submissionFormat }}</p>
            </div>

            <!-- Review Criteria -->
            <div class="track-section mb-lg">
              <h5 class="section-title">评审标准</h5>
              <p class="caption">{{ track.reviewCriteria }}</p>
            </div>

            <!-- Action -->
            <div class="track-footer">
              <button
                v-if="route.query.from === 'admin'"
                class="btn-secondary"
                style="width: 100%;"
                @click="router.push('/admin/competitions')"
              >
                返回赛事管理
              </button>
              <button
                v-else
                class="btn-primary"
                style="width: 100%;"
                @click="selectTrack(track)"
              >
                选择此赛道报名
              </button>
            </div>
          </div>
        </div>

        <!-- Empty -->
        <div v-else class="empty-state text-center">
          <p class="body-text">暂无赛道信息</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useCompetitionStore } from '../stores/competition'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const store = useCompetitionStore()

const { currentCompetition, tracks, loading, error, fetchCompetitionById, fetchTracks } = store

onMounted(async () => {
  await fetchCompetitionById(route.params.id)
  await fetchTracks(route.params.id)
})

const getTrackIcon = (type) => {
  return { CODE: '💻', PPT: '📊', VIDEO: '🎬' }[type] || '🏆'
}

const getTrackTypeText = (type) => {
  return { CODE: '程序设计', PPT: '演示文稿', VIDEO: '数媒视频' }[type] || type
}

const selectTrack = (track) => {
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.info('请先登录后报名参赛')
    router.push(`/login?redirect=/competitions/${route.params.id}/tracks`)
    return
  }
  ElMessage.success(`已选择${track.trackName}赛道`)
  // TODO: 跳转到报名页面
}
</script>

<style scoped>
.tracks-page {
  min-height: 100vh;
}

.loading-state, .error-state, .empty-state {
  padding: 64px 0;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(0, 0, 0, 0.48);
  border-top-color: var(--color-apple-blue);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.track-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: rgba(0, 113, 227, 0.1);
  color: var(--color-apple-blue);
  padding: 8px 12px;
  border-radius: var(--radius-pill);
}

.badge-icon {
  font-size: 18px;
}

.badge-text {
  font-family: var(--font-text);
  font-size: 14px;
  font-weight: 600;
  letter-spacing: -0.224px;
}

.specs-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 17px;
}

.spec-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.spec-item.spec-full {
  grid-column: 1 / -1;
}

.spec-item.spec-highlight {
  background: rgba(255, 149, 0, 0.1);
  padding: 8px;
  border-radius: 5px;
}

.section-title {
  font-family: var(--font-display);
  font-size: 17px;
  font-weight: 600;
  line-height: 1.24;
  letter-spacing: -0.374px;
  color: var(--color-near-black);
  margin-bottom: 8px;
}

@media (max-width: 640px) {
  .specs-grid {
    grid-template-columns: 1fr;
  }
}
</style>