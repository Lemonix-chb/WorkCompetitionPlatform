<template>
  <div class="stats-page">
    <div class="container">
      <!-- Page Header -->
      <div class="page-header slide-in">
        <div class="header-content">
          <h1 class="page-title">评审统计</h1>
          <p class="header-subtitle">详细数据分析与可视化</p>
        </div>
        <div class="header-meta">
          <span class="badge badge-ai">实时更新</span>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="loading-state fade-in">
        <div class="spinner-wrapper">
          <svg class="spinner" width="48" height="48" viewBox="0 0 48 48" fill="none">
            <circle cx="24" cy="24" r="20" stroke="var(--color-accent)" stroke-width="4" stroke-dasharray="80" stroke-dashoffset="20"/>
          </svg>
        </div>
        <p class="caption">加载统计数据...</p>
      </div>

      <!-- Stats Content -->
      <div v-else class="stats-content">
        <!-- Core Stats Cards -->
        <div class="stats-grid scale-in">
          <div class="stat-card card">
            <div class="stat-icon">
              <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
                <rect x="6" y="6" width="28" height="28" rx="3" stroke="var(--color-accent)" stroke-width="2"/>
                <path d="M12 12H28M12 16H28M12 20H20" stroke="var(--color-accent)" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="stat-number">{{ stats.pendingReviews || 0 }}</div>
            <div class="stat-label">待评审作品</div>
            <div class="stat-trend pending">
              <span class="trend-badge badge-pending">等待处理</span>
            </div>
          </div>

          <div class="stat-card card">
            <div class="stat-icon">
              <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
                <circle cx="20" cy="20" r="15" stroke="var(--status-completed)" stroke-width="2"/>
                <path d="M14 20L18 24L26 16" stroke="var(--status-completed)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="stat-number">{{ stats.completedReviews || 0 }}</div>
            <div class="stat-label">已评审作品</div>
            <div class="stat-trend">
              <span class="trend-badge badge-completed">已完成</span>
            </div>
          </div>

          <div class="stat-card card card-accent">
            <div class="stat-icon">
              <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
                <circle cx="20" cy="10" r="8" stroke="var(--color-accent)" stroke-width="2" fill="none"/>
                <path d="M12 24L8 32L20 28L32 32L28 24" stroke="var(--color-accent)" stroke-width="2" stroke-linejoin="round"/>
                <path d="M14 14L20 10L26 14" stroke="var(--color-accent)" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="stat-number accent-text">{{ stats.averageScore || 0 }}</div>
            <div class="stat-label">平均评分</div>
            <div class="stat-trend">
              <span class="trend-range">最高 {{ stats.maxScore || 0 }} · 最低 {{ stats.minScore || 0 }}</span>
            </div>
          </div>

          <div class="stat-card card">
            <div class="stat-icon">
              <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
                <rect x="8" y="28" width="5" height="8" rx="1" fill="var(--color-accent)" opacity="0.6"/>
                <rect x="14" y="22" width="5" height="14" rx="1" fill="var(--color-accent)" opacity="0.8"/>
                <rect x="20" y="16" width="5" height="20" rx="1" fill="var(--color-accent)"/>
                <rect x="26" y="20" width="5" height="16" rx="1" fill="var(--color-accent)" opacity="0.8"/>
              </svg>
            </div>
            <div class="stat-number">{{ stats.thisWeekReviews || 0 }}</div>
            <div class="stat-label">本周评审</div>
            <div class="stat-trend">
              <span class="trend-badge badge-reviewing">本月 {{ stats.thisMonthReviews || 0 }} 个</span>
            </div>
          </div>
        </div>

        <!-- Score Distribution -->
        <div class="distribution-section slide-in">
          <div class="section-header">
            <h2 class="section-title">评分分布</h2>
            <div class="section-divider"></div>
          </div>

          <div class="distribution-grid">
            <div class="distribution-chart card">
              <h3 class="card-title mb-xl">评分等级分布</h3>

              <!-- Bar Chart -->
              <div class="bar-chart">
                <div class="bar-row">
                  <div class="bar-label">优秀</div>
                  <div class="bar-container">
                    <div class="bar-fill excellent" :style="{ width: getPercentage(stats.excellentCount, stats.completedReviews) + '%' }">
                      <span class="bar-value">{{ stats.excellentCount || 0 }}</span>
                    </div>
                  </div>
                </div>

                <div class="bar-row">
                  <div class="bar-label">良好</div>
                  <div class="bar-container">
                    <div class="bar-fill good" :style="{ width: getPercentage(stats.goodCount, stats.completedReviews) + '%' }">
                      <span class="bar-value">{{ stats.goodCount || 0 }}</span>
                    </div>
                  </div>
                </div>

                <div class="bar-row">
                  <div class="bar-label">中等</div>
                  <div class="bar-container">
                    <div class="bar-fill average" :style="{ width: getPercentage(stats.averageCount, stats.completedReviews) + '%' }">
                      <span class="bar-value">{{ stats.averageCount || 0 }}</span>
                    </div>
                  </div>
                </div>

                <div class="bar-row">
                  <div class="bar-label">较差</div>
                  <div class="bar-container">
                    <div class="bar-fill poor" :style="{ width: getPercentage(stats.poorCount, stats.completedReviews) + '%' }">
                      <span class="bar-value">{{ stats.poorCount || 0 }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <div class="chart-legend">
                <div class="legend-item">
                  <span class="legend-dot excellent"></span>
                  <span class="legend-text">优秀 (≥85分)</span>
                </div>
                <div class="legend-item">
                  <span class="legend-dot good"></span>
                  <span class="legend-text">良好 (70-84分)</span>
                </div>
                <div class="legend-item">
                  <span class="legend-dot average"></span>
                  <span class="legend-text">中等 (60-69分)</span>
                </div>
                <div class="legend-item">
                  <span class="legend-dot poor"></span>
                  <span class="legend-text">较差 (低于60分)</span>
                </div>
              </div>
            </div>

            <!-- Dimension Scores -->
            <div class="dimension-card card">
              <h3 class="card-title mb-xl">维度评分分析</h3>

              <div class="dimension-list">
                <div class="dimension-item">
                  <div class="dimension-header">
                    <span class="dimension-label">创新性</span>
                    <span class="dimension-score">{{ stats.avgInnovationScore || 0 }}</span>
                  </div>
                  <div class="dimension-bar">
                    <div class="dimension-fill" :style="{ width: ((stats.avgInnovationScore || 0) / 100 * 100) + '%' }"></div>
                  </div>
                </div>

                <div class="dimension-item">
                  <div class="dimension-header">
                    <span class="dimension-label">实用性</span>
                    <span class="dimension-score">{{ stats.avgPracticalityScore || 0 }}</span>
                  </div>
                  <div class="dimension-bar">
                    <div class="dimension-fill" :style="{ width: ((stats.avgPracticalityScore || 0) / 100 * 100) + '%' }"></div>
                  </div>
                </div>

                <div class="dimension-item">
                  <div class="dimension-header">
                    <span class="dimension-label">用户体验</span>
                    <span class="dimension-score">{{ stats.avgUserExperienceScore || 0 }}</span>
                  </div>
                  <div class="dimension-bar">
                    <div class="dimension-fill" :style="{ width: ((stats.avgUserExperienceScore || 0) / 100 * 100) + '%' }"></div>
                  </div>
                </div>
              </div>

              <div class="dimension-summary">
                <p class="caption">三个维度平均分对比，满分均为100分</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Recent Trend -->
        <div class="trend-section fade-in">
          <div class="section-header">
            <h2 class="section-title">评审趋势</h2>
            <div class="section-divider"></div>
          </div>

          <div class="trend-chart card">
            <h3 class="card-title mb-xl">最近7天评审数量</h3>

            <div class="line-chart">
              <div class="chart-container">
                <div v-for="(count, date) in stats.recentTrend" :key="date" class="chart-column">
                  <div class="column-value">{{ count }}</div>
                  <div class="column-bar" :style="{ height: (count * 20) + 'px' }"></div>
                  <div class="column-label">{{ date }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Track Distribution -->
        <div class="track-section slide-in">
          <div class="section-header">
            <h2 class="section-title">赛道分布</h2>
            <div class="section-divider"></div>
          </div>

          <div class="track-table card">
            <div class="table-header">
              <div class="table-row">
                <div class="table-cell">赛道名称</div>
                <div class="table-cell">评审数量</div>
                <div class="table-cell">平均评分</div>
                <div class="table-cell">占比</div>
              </div>
            </div>

            <div class="table-body">
              <div v-if="Object.keys(stats.trackDistribution || {}).length === 0" class="empty-row">
                <p class="caption text-center">暂无赛道数据</p>
              </div>

              <div v-for="(count, trackName) in stats.trackDistribution" :key="trackName" class="table-row">
                <div class="table-cell">
                  <span class="track-name">{{ trackName }}</span>
                </div>
                <div class="table-cell">
                  <span class="badge badge-reviewing">{{ count }} 个</span>
                </div>
                <div class="table-cell">
                  <span class="score-display">{{ (stats.trackAverageScores && stats.trackAverageScores[trackName]) || '-' }}</span>
                </div>
                <div class="table-cell">
                  <span class="percentage-display">{{ getPercentage(count, stats.completedReviews) }}%</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Recent Reviews -->
        <div class="recent-section fade-in">
          <div class="section-header">
            <h2 class="section-title">最近评审</h2>
            <div class="section-divider"></div>
          </div>

          <div class="recent-list">
            <div v-if="!stats.recentReviews || stats.recentReviews.length === 0" class="empty-state card">
              <p class="caption text-center">暂无评审记录</p>
            </div>

            <div v-for="review in stats.recentReviews" :key="review.reviewId" class="review-item card">
              <div class="review-header">
                <div class="review-info">
                  <h4 class="review-work">{{ review.workName || '未知作品' }}</h4>
                  <p class="review-team caption">{{ review.teamName || '未知团队' }}</p>
                </div>
                <div class="review-score">
                  <span class="score-badge" :class="getScoreClass(review.overallScore)">
                    {{ review.overallScore || '-' }} 分
                  </span>
                </div>
              </div>

              <div class="review-footer">
                <span class="review-date">
                  <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                    <circle cx="7" cy="7" r="6" stroke="var(--color-text-tertiary)" stroke-width="1"/>
                    <path d="M7 3V7L9 9" stroke="var(--color-text-tertiary)" stroke-width="1" stroke-linecap="round"/>
                  </svg>
                  {{ review.reviewDate }}
                </span>
                <span class="review-status badge" :class="'badge-' + (review.status === 'SUBMITTED' ? 'completed' : 'reviewing')">
                  {{ review.status === 'SUBMITTED' ? '已提交' : '已确认' }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { get } from '@/utils/api'
import { showWarning, showError } from '@/utils/messageUtils'

const router = useRouter()

const loading = ref(true)

const stats = ref({
  pendingReviews: 0,
  completedReviews: 0,
  totalScores: 0,
  assignedTracks: 0,
  averageScore: 0,
  maxScore: 0,
  minScore: 0,
  excellentCount: 0,
  goodCount: 0,
  averageCount: 0,
  poorCount: 0,
  avgInnovationScore: 0,
  avgPracticalityScore: 0,
  avgUserExperienceScore: 0,
  thisWeekReviews: 0,
  thisMonthReviews: 0,
  trackDistribution: {},
  trackAverageScores: {},
  recentTrend: {},
  recentReviews: []
})

onMounted(async () => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('userRole')

  if (!token || role !== 'JUDGE') {
    showWarning('您没有权限访问此页面')
    router.push('/login')
    return
  }

  await fetchStats()
})

const fetchStats = async () => {
  loading.value = true

  try {
    const data = await get('/judge/stats')
    stats.value = data
  } catch (error) {
    console.error('获取统计数据失败', error)
    showError('获取统计数据失败')
  } finally {
    loading.value = false
  }
}

const getPercentage = (count, total) => {
  if (!total || total === 0) return 0
  return Math.round((count / total) * 100)
}

const getScoreClass = (score) => {
  if (!score) return 'score-average'
  if (score >= 85) return 'score-excellent'
  if (score >= 70) return 'score-good'
  if (score >= 60) return 'score-average'
  return 'score-poor'
}
</script>

<style scoped>
.stats-page {
  min-height: 100vh;
}

.container {
  padding-top: var(--spacing-xl);
}

/* Page Header */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-3xl);
  padding: var(--spacing-xl);
  background: var(--color-bg-card);
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-card);
}

.header-content {
  flex: 1;
}

.header-subtitle {
  font-family: var(--font-body);
  font-size: var(--text-base);
  color: var(--color-text-secondary);
  margin-top: var(--spacing-sm);
}

.header-meta {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

/* Loading State */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-3xl);
}

.spinner-wrapper {
  margin-bottom: var(--spacing-lg);
}

.spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-3xl);
}

.stat-card {
  position: relative;
  overflow: hidden;
}

.stat-icon {
  margin-bottom: var(--spacing-md);
  opacity: 0.9;
}

.stat-number {
  font-family: var(--font-display);
  font-size: var(--text-4xl);
  font-weight: 700;
  color: var(--color-primary);
  line-height: 1;
  margin-bottom: var(--spacing-sm);
  letter-spacing: -0.04em;
}

.accent-text {
  color: var(--color-accent);
}

.stat-label {
  font-family: var(--font-mono);
  font-size: var(--text-xs);
  color: var(--color-text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.1em;
  margin-bottom: var(--spacing-md);
}

.stat-trend {
  padding-top: var(--spacing-md);
  border-top: 1px solid var(--color-border-light);
}

.trend-badge {
  font-size: var(--text-xs);
}

.trend-range {
  font-family: var(--font-body);
  font-size: var(--text-sm);
  color: var(--color-text-tertiary);
}

/* Section Headers */
.section-header {
  margin-bottom: var(--spacing-xl);
}

.section-divider {
  height: 2px;
  background: linear-gradient(90deg, var(--color-accent) 0%, transparent 100%);
  margin-top: var(--spacing-md);
}

/* Distribution Section */
.distribution-section {
  margin-bottom: var(--spacing-3xl);
}

.distribution-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-lg);
}

.distribution-chart,
.dimension-card {
  padding: var(--spacing-xl);
}

/* Bar Chart */
.bar-chart {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.bar-row {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
}

.bar-label {
  width: 60px;
  font-family: var(--font-body);
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-text-primary);
}

.bar-container {
  flex: 1;
  height: 28px;
  background: rgba(127, 140, 141, 0.1);
  border-radius: var(--radius-md);
  position: relative;
}

.bar-fill {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding-right: var(--spacing-md);
  transition: width var(--transition-slow);
}

.bar-fill.excellent {
  background: linear-gradient(90deg, var(--status-completed), #2ecc71);
}

.bar-fill.good {
  background: linear-gradient(90deg, #5dade2, #3498db);
}

.bar-fill.average {
  background: linear-gradient(90deg, #f39c12, #e67e22);
}

.bar-fill.poor {
  background: linear-gradient(90deg, #e74c3c, #c0392b);
}

.bar-value {
  font-family: var(--font-mono);
  font-size: var(--text-xs);
  font-weight: 700;
  color: var(--color-text-white);
}

.chart-legend {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-md);
  margin-top: var(--spacing-xl);
  padding-top: var(--spacing-lg);
  border-top: 1px solid var(--color-border-light);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: var(--radius-xs);
}

.legend-dot.excellent { background: var(--status-completed); }
.legend-dot.good { background: #3498db; }
.legend-dot.average { background: #f39c12; }
.legend-dot.poor { background: #e74c3c; }

.legend-text {
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
}

/* Dimension Scores */
.dimension-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.dimension-item {
  padding: var(--spacing-md);
  background: rgba(52, 152, 219, 0.04);
  border-radius: var(--radius-md);
}

.dimension-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: var(--spacing-sm);
}

.dimension-label {
  font-family: var(--font-body);
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-text-primary);
}

.dimension-score {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  font-weight: 700;
  color: var(--color-accent);
}

.dimension-bar {
  height: 8px;
  background: rgba(127, 140, 141, 0.1);
  border-radius: var(--radius-xs);
}

.dimension-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--color-accent-light), var(--color-accent));
  border-radius: var(--radius-xs);
  transition: width var(--transition-slow);
}

.dimension-summary {
  margin-top: var(--spacing-lg);
  padding-top: var(--spacing-md);
  border-top: 1px solid var(--color-border-light);
}

/* Trend Section */
.trend-section {
  margin-bottom: var(--spacing-3xl);
}

.trend-chart {
  padding: var(--spacing-xl);
}

.line-chart {
  margin-top: var(--spacing-lg);
}

.chart-container {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  gap: var(--spacing-sm);
  height: 120px;
  padding: var(--spacing-md);
  background: rgba(52, 152, 219, 0.04);
  border-radius: var(--radius-lg);
}

.chart-column {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.column-value {
  font-family: var(--font-mono);
  font-size: var(--text-xs);
  font-weight: 700;
  color: var(--color-accent);
  margin-bottom: var(--spacing-sm);
}

.column-bar {
  width: 100%;
  max-width: 40px;
  background: linear-gradient(180deg, var(--color-accent), var(--color-accent-light));
  border-radius: var(--radius-xs) var(--radius-xs) 0 0;
  transition: height var(--transition-slow);
}

.column-label {
  font-family: var(--font-mono);
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
  margin-top: var(--spacing-sm);
}

/* Track Section */
.track-section {
  margin-bottom: var(--spacing-3xl);
}

.track-table {
  padding: 0;
  overflow: hidden;
}

.table-header {
  background: rgba(13, 33, 55, 0.04);
  border-bottom: 2px solid var(--color-border-light);
}

.table-row {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  padding: var(--spacing-lg);
  border-bottom: 1px solid var(--color-border-light);
  transition: all var(--transition-fast);
}

.table-row:hover {
  background: rgba(52, 152, 219, 0.04);
}

.table-body .table-row:last-child {
  border-bottom: none;
}

.table-cell {
  display: flex;
  align-items: center;
}

.track-name {
  font-family: var(--font-body);
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-text-primary);
}

.score-display {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  font-weight: 700;
  color: var(--color-accent);
}

.percentage-display {
  font-family: var(--font-mono);
  font-size: var(--text-sm);
  color: var(--color-text-tertiary);
}

.empty-row {
  padding: var(--spacing-3xl);
}

/* Recent Section */
.recent-section {
  margin-bottom: var(--spacing-2xl);
}

.recent-list {
  display: grid;
  gap: var(--spacing-md);
}

.review-item {
  padding: var(--spacing-lg);
  transition: all var(--transition-fast);
}

.review-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-elevated);
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--spacing-md);
}

.review-work {
  font-family: var(--font-body);
  font-size: var(--text-base);
  font-weight: 600;
  color: var(--color-text-primary);
}

.review-team {
  margin-top: var(--spacing-xs);
}

.review-score {
  flex-shrink: 0;
}

.score-badge {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  font-weight: 700;
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--radius-md);
}

.score-excellent {
  background: rgba(39, 174, 96, 0.12);
  color: var(--status-completed);
  border: 1px solid rgba(39, 174, 96, 0.3);
}

.score-good {
  background: rgba(52, 152, 219, 0.12);
  color: var(--color-accent);
  border: 1px solid rgba(52, 152, 219, 0.3);
}

.score-average {
  background: rgba(243, 156, 18, 0.12);
  color: var(--status-reviewing);
  border: 1px solid rgba(243, 156, 18, 0.3);
}

.score-poor {
  background: rgba(231, 76, 60, 0.12);
  color: var(--status-rejected);
  border: 1px solid rgba(231, 76, 60, 0.3);
}

.review-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: var(--spacing-md);
  border-top: 1px solid var(--color-border-light);
}

.review-date {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: var(--text-sm);
  color: var(--color-text-tertiary);
}

/* Responsive */
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .distribution-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: var(--spacing-lg);
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .table-row {
    grid-template-columns: 1fr 1fr;
    gap: var(--spacing-sm);
  }

  .chart-container {
    height: 80px;
  }

  .stat-number {
    font-size: var(--text-3xl);
  }
}

@media (max-width: 480px) {
  .review-header {
    flex-direction: column;
    gap: var(--spacing-md);
  }

  .review-footer {
    flex-direction: column;
    gap: var(--spacing-sm);
    align-items: flex-start;
  }
}
</style>