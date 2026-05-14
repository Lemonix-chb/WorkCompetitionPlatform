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
import { showSuccess } from '@/utils/messageUtils'

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
    showSuccess('请先登录后报名参赛')
    router.push(`/login?redirect=/competitions/${route.params.id}/tracks`)
    return
  }
  showSuccess(`已选择${track.trackName}赛道`)
  // TODO: 跳转到报名页面
}
</script>

<style scoped>
.tracks-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f5f5f7 0%, #ffffff 100%);
}

.loading-state, .error-state, .empty-state {
  padding: 80px 0;
  text-align: center;
}

.spinner {
  width: 48px;
  height: 48px;
  border: 4px solid rgba(0, 113, 227, 0.15);
  border-top-color: var(--color-apple-blue);
  border-radius: 50%;
  animation: spin 0.8s cubic-bezier(0.4, 0, 0.2, 1) infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Track Card Enhancement */
.card {
  background: #ffffff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04), 0 1px 2px rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(0, 0, 0, 0.04);
  position: relative;
  overflow: hidden;
}

.card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, var(--color-apple-blue), #5ac8fa);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08), 0 4px 8px rgba(0, 0, 0, 0.06);
  border-color: rgba(0, 113, 227, 0.1);
}

.card:hover::before {
  opacity: 1;
}

/* Track Badge */
.track-badge {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  background: linear-gradient(135deg, rgba(0, 113, 227, 0.08) 0%, rgba(0, 113, 227, 0.04) 100%);
  color: var(--color-apple-blue);
  padding: 10px 16px;
  border-radius: 50px;
  border: 1px solid rgba(0, 113, 227, 0.15);
  box-shadow: 0 1px 4px rgba(0, 113, 227, 0.1);
}

.badge-icon {
  font-size: 20px;
  line-height: 1;
}

.badge-text {
  font-family: var(--font-text);
  font-size: 15px;
  font-weight: 600;
  letter-spacing: -0.24px;
}

/* Track Title */
.tile-heading {
  font-size: 28px;
  font-weight: 700;
  letter-spacing: -0.56px;
  color: #1d1d1f;
  margin-top: 8px;
}

/* Specs Grid */
.specs-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  background: rgba(245, 245, 247, 0.5);
  padding: 20px;
  border-radius: 12px;
  margin-top: 8px;
}

.spec-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 12px;
  background: #ffffff;
  border-radius: 8px;
  border: 1px solid rgba(0, 0, 0, 0.04);
  transition: all 0.2s ease;
}

.spec-item:hover {
  border-color: rgba(0, 113, 227, 0.2);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.06);
}

.spec-item.spec-full {
  grid-column: 1 / -1;
  background: linear-gradient(135deg, rgba(245, 245, 247, 0.5) 0%, rgba(255, 255, 255, 0.8) 100%);
}

.spec-item.spec-highlight {
  background: linear-gradient(135deg, rgba(255, 149, 0, 0.06) 0%, rgba(255, 149, 0, 0.02) 100%);
  border-color: rgba(255, 149, 0, 0.3);
}

.spec-item .caption:first-child {
  font-weight: 600;
  color: #86868b;
  letter-spacing: -0.32px;
}

.spec-item .card-title {
  font-size: 20px;
  font-weight: 700;
  color: #1d1d1f;
  letter-spacing: -0.4px;
}

.spec-item .body-text {
  font-size: 15px;
  color: #1d1d1f;
  font-weight: 500;
}

/* Track Section */
.track-section {
  background: rgba(245, 245, 247, 0.5);
  padding: 16px 20px;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.section-title {
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 600;
  line-height: 1.22;
  letter-spacing: -0.4px;
  color: #1d1d1f;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title::before {
  content: '';
  width: 4px;
  height: 18px;
  background: var(--color-apple-blue);
  border-radius: 2px;
}

.track-section .caption {
  font-size: 15px;
  line-height: 1.5;
  color: #424245;
  letter-spacing: -0.24px;
}

/* Track Footer */
.track-footer {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

.track-footer .btn-primary {
  background: linear-gradient(135deg, var(--color-apple-blue) 0%, #007aff 100%);
  box-shadow: 0 2px 8px rgba(0, 113, 227, 0.3), 0 1px 2px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
}

.track-footer .btn-primary::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
}

.track-footer .btn-primary:hover::before {
  left: 100%;
}

.track-footer .btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 113, 227, 0.4), 0 2px 4px rgba(0, 0, 0, 0.12);
}

/* Grid Layout */
.grid-auto {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(420px, 1fr));
  gap: 32px;
}

@media (max-width: 640px) {
  .specs-grid {
    grid-template-columns: 1fr;
    gap: 16px;
    padding: 16px;
  }

  .grid-auto {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .card {
    padding: 24px;
    border-radius: 14px;
  }

  .tile-heading {
    font-size: 24px;
    letter-spacing: -0.48px;
  }

  .track-badge {
    padding: 8px 14px;
    gap: 8px;
  }
}
</style>