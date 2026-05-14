<template>
  <div class="results-page">
    <!-- Hero -->
    <section class="section-dark">
      <div class="container">
        <h1 class="hero-title">🏆 成绩查询</h1>
        <h2 class="subheading">查看作品评审结果与获奖记录</h2>
      </div>
    </section>

    <!-- Results List -->
    <section class="section-light">
      <div class="container">
        <div v-if="loading" class="loading-state">
          <div class="spinner"></div>
        </div>

        <div v-else-if="results.length === 0" class="empty-state text-center">
          <div class="empty-icon">
            <svg width="64" height="64" viewBox="0 0 64 64" fill="none">
              <path d="M32 8L40 12V20C40 28 36 32 32 34C28 32 24 28 24 20V12L32 8Z"
                    stroke="#bdc3c7" stroke-width="2" fill="none"/>
              <circle cx="32" cy="18" r="3" stroke="#bdc3c7" stroke-width="2"/>
            </svg>
          </div>
          <h3 class="card-title mb-md">暂无评审结果</h3>
          <p class="caption">提交作品后即可查看评审结果</p>
        </div>

        <div v-else class="results-grid">
          <div
            v-for="result in results"
            :key="result.id"
            class="award-card"
            :class="getAwardCardClass(result.awardLevelText)"
          >
            <!-- Award Badge -->
            <div class="award-badge-section">
              <div class="award-badge" :class="getAwardBadgeClass(result.awardLevelText)">
                <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                  <path d="M16 4L20 12L28 12L22 18L24 26L16 22L8 26L10 18L4 12L12 12L16 4Z"
                        :fill="getAwardColor(result.awardLevelText)"
                        stroke="currentColor" stroke-width="2"/>
                </svg>
                <span class="award-text">{{ result.awardLevelText || '待定' }}</span>
              </div>
              <div v-if="result.rankInTrack && result.rankInTrack <= 3" class="rank-badge">
                <span class="rank-number">#{{ result.rankInTrack }}</span>
                <span class="rank-label">赛道排名</span>
              </div>
            </div>

            <!-- Work Info -->
            <div class="work-info-section">
              <h3 class="work-title">{{ result.workName }}</h3>
              <div class="competition-info">
                <span class="info-tag">{{ result.competitionName }}</span>
                <span class="info-tag">{{ result.trackName }}</span>
                <span class="info-tag type-tag">{{ result.workTypeText }}</span>
              </div>
            </div>

            <!-- Score Display -->
            <div class="score-display-section">
              <div class="score-item">
                <div class="score-circle ai-score">
                  <div class="score-ring" :style="getScoreRingStyle(result.aiScore)">
                    <span class="score-number">{{ result.aiScore || 0 }}</span>
                  </div>
                  <span class="score-label">AI评分</span>
                </div>
              </div>

              <div class="score-divider"></div>

              <div class="score-item">
                <div class="score-circle judge-score">
                  <div class="score-ring" :style="getScoreRingStyle(result.judgeAvgScore)">
                    <span class="score-number">{{ result.judgeAvgScore || 0 }}</span>
                  </div>
                  <span class="score-label">评委评分</span>
                </div>
              </div>

              <div class="score-divider"></div>

              <div class="score-item final-score-highlight">
                <div class="final-score-box">
                  <span class="final-score-value">{{ result.finalScore || 0 }}</span>
                  <span class="final-score-label">最终得分</span>
                </div>
              </div>
            </div>

            <!-- Submission Time -->
            <div class="submission-time">
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                <circle cx="7" cy="7" r="5" stroke="currentColor" stroke-width="1.5"/>
                <path d="M7 4V7L9 9" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
              <span class="time-text">{{ formatDateTime(result.submissionTime) }}</span>
            </div>

            <!-- Actions -->
            <div class="card-actions">
              <button class="action-btn detail-btn" @click="viewAIReviewDetail(result)">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.5"/>
                  <path d="M8 5V8L10 10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
                AI评审详情
              </button>
              <button class="action-btn judge-btn" @click="viewJudgeReview(result)">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <circle cx="8" cy="4" r="3" stroke="currentColor" stroke-width="1.5"/>
                  <path d="M4 12C4 10 6 9 8 9C10 9 12 10 12 12" stroke="currentColor" stroke-width="1.5"/>
                </svg>
                评委评审
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- AI评审详情对话框 -->
    <el-dialog v-model="aiReviewDialogVisible" title="AI智能评审报告" width="90%" :close-on-click-modal="false">
      <div v-if="aiReviewDetail" class="ai-review-container">
        <!-- 综合得分展示 -->
        <div class="score-hero">
          <div class="score-circle-wrapper">
            <div class="score-circle-big" :style="getScoreRingStyle(aiReviewDetail.overallScore)">
              <div class="score-inner">
                <div class="score-number-big">{{ aiReviewDetail.overallScore || 0 }}</div>
                <div class="score-unit">分</div>
              </div>
            </div>
            <div class="score-title">综合评分</div>
          </div>

          <!-- 维度评分 - 根据作品类型动态显示 -->
          <div class="dimension-scores">
            <!-- CODE作品评分维度 -->
            <div v-if="aiReviewDetail.workType === 'CODE'">
              <div v-if="aiReviewDetail.innovationScore" class="dim-item">
                <div class="dim-header">
                  <span class="dim-icon">💡</span>
                  <span class="dim-name">创新性</span>
                </div>
                <div class="dim-progress">
                  <div class="dim-bar" :style="{ width: (aiReviewDetail.innovationScore / 25 * 100) + '%' }"></div>
                </div>
                <div class="dim-value">{{ aiReviewDetail.innovationScore }} / 25</div>
              </div>

              <div v-if="aiReviewDetail.practicalityScore" class="dim-item">
                <div class="dim-header">
                  <span class="dim-icon">🔧</span>
                  <span class="dim-name">实用性</span>
                </div>
                <div class="dim-progress">
                  <div class="dim-bar practicality" :style="{ width: (aiReviewDetail.practicalityScore / 25 * 100) + '%' }"></div>
                </div>
                <div class="dim-value">{{ aiReviewDetail.practicalityScore }} / 25</div>
              </div>

              <div v-if="aiReviewDetail.userExperienceScore" class="dim-item">
                <div class="dim-header">
                  <span class="dim-icon">✨</span>
                  <span class="dim-name">用户体验</span>
                </div>
                <div class="dim-progress">
                  <div class="dim-bar experience" :style="{ width: (aiReviewDetail.userExperienceScore / 25 * 100) + '%' }"></div>
                </div>
                <div class="dim-value">{{ aiReviewDetail.userExperienceScore }} / 25</div>
              </div>

              <div v-if="aiReviewDetail.documentationScore" class="dim-item">
                <div class="dim-header">
                  <span class="dim-icon">📖</span>
                  <span class="dim-name">文档质量</span>
                </div>
                <div class="dim-progress">
                  <div class="dim-bar documentation" :style="{ width: (aiReviewDetail.documentationScore / 25 * 100) + '%' }"></div>
                </div>
                <div class="dim-value">{{ aiReviewDetail.documentationScore }} / 25</div>
              </div>

              <div v-if="aiReviewDetail.codeQualityScore" class="dim-item">
                <div class="dim-header">
                  <span class="dim-icon">⚡</span>
                  <span class="dim-name">代码质量</span>
                </div>
                <div class="dim-progress">
                  <div class="dim-bar code" :style="{ width: (aiReviewDetail.codeQualityScore / 25 * 100) + '%' }"></div>
                </div>
                <div class="dim-value">{{ aiReviewDetail.codeQualityScore }} / 25</div>
              </div>

              <div v-if="aiReviewDetail.duplicateRate != null" class="dim-item duplicate">
                <div class="dim-header">
                  <span class="dim-icon">🔄</span>
                  <span class="dim-name">代码重复率</span>
                </div>
                <div class="dim-progress duplicate">
                  <div class="dim-bar duplicate-bar" :style="{ width: String(aiReviewDetail.duplicateRate) + '%' }"></div>
                </div>
                <div class="dim-value warn">{{ aiReviewDetail.duplicateRate }}%</div>
              </div>
            </div>

            <!-- PPT作品评分维度 -->
            <div v-if="aiReviewDetail.workType === 'PPT'">
              <div v-if="aiReviewDetail.creativityScore" class="dim-item">
                <div class="dim-header">
                  <span class="dim-icon">🎨</span>
                  <span class="dim-name">创意</span>
                </div>
                <div class="dim-progress">
                  <div class="dim-bar creativity" :style="{ width: (aiReviewDetail.creativityScore / 25 * 100) + '%' }"></div>
                </div>
                <div class="dim-value">{{ aiReviewDetail.creativityScore }} / 25</div>
              </div>

              <div v-if="aiReviewDetail.visualEffectScore" class="dim-item">
                <div class="dim-header">
                  <span class="dim-icon">👁️</span>
                  <span class="dim-name">视觉效果</span>
                </div>
                <div class="dim-progress">
                  <div class="dim-bar visual" :style="{ width: (aiReviewDetail.visualEffectScore / 25 * 100) + '%' }"></div>
                </div>
                <div class="dim-value">{{ aiReviewDetail.visualEffectScore }} / 25</div>
              </div>

              <div v-if="aiReviewDetail.contentPresentationScore" class="dim-item">
                <div class="dim-header">
                  <span class="dim-icon">📊</span>
                  <span class="dim-name">内容呈现</span>
                </div>
                <div class="dim-progress">
                  <div class="dim-bar content" :style="{ width: (aiReviewDetail.contentPresentationScore / 25 * 100) + '%' }"></div>
                </div>
                <div class="dim-value">{{ aiReviewDetail.contentPresentationScore }} / 25</div>
              </div>

              <div v-if="aiReviewDetail.originalityScore" class="dim-item">
                <div class="dim-header">
                  <span class="dim-icon">🌟</span>
                  <span class="dim-name">原创性</span>
                </div>
                <div class="dim-progress">
                  <div class="dim-bar originality" :style="{ width: (aiReviewDetail.originalityScore / 25 * 100) + '%' }"></div>
                </div>
                <div class="dim-value">{{ aiReviewDetail.originalityScore }} / 25</div>
              </div>
            </div>

            <!-- VIDEO作品评分维度 -->
            <div v-if="aiReviewDetail.workType === 'VIDEO'">
              <div v-if="aiReviewDetail.storyScore" class="dim-item">
                <div class="dim-header">
                  <span class="dim-icon">📖</span>
                  <span class="dim-name">故事性</span>
                </div>
                <div class="dim-progress">
                  <div class="dim-bar story" :style="{ width: (aiReviewDetail.storyScore / 30 * 100) + '%' }"></div>
                </div>
                <div class="dim-value">{{ aiReviewDetail.storyScore }} / 30</div>
              </div>

              <div v-if="aiReviewDetail.visualEffectScore" class="dim-item">
                <div class="dim-header">
                  <span class="dim-icon">🎬</span>
                  <span class="dim-name">视觉效果</span>
                </div>
                <div class="dim-progress">
                  <div class="dim-bar visual" :style="{ width: (aiReviewDetail.visualEffectScore / 25 * 100) + '%' }"></div>
                </div>
                <div class="dim-value">{{ aiReviewDetail.visualEffectScore }} / 25</div>
              </div>

              <div v-if="aiReviewDetail.directorSkillScore" class="dim-item">
                <div class="dim-header">
                  <span class="dim-icon">🎯</span>
                  <span class="dim-name">导演技巧</span>
                </div>
                <div class="dim-progress">
                  <div class="dim-bar director" :style="{ width: (aiReviewDetail.directorSkillScore / 25 * 100) + '%' }"></div>
                </div>
                <div class="dim-value">{{ aiReviewDetail.directorSkillScore }} / 25</div>
              </div>

              <div v-if="aiReviewDetail.originalityScore" class="dim-item">
                <div class="dim-header">
                  <span class="dim-icon">🌟</span>
                  <span class="dim-name">原创性</span>
                </div>
                <div class="dim-progress">
                  <div class="dim-bar originality" :style="{ width: (aiReviewDetail.originalityScore / 20 * 100) + '%' }"></div>
                </div>
                <div class="dim-value">{{ aiReviewDetail.originalityScore }} / 20</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 指标卡片 -->
        <div class="metrics-row">
          <div class="metric-card">
            <div class="metric-icon risk">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                <path d="M12 2L22 20H2L12 2Z" stroke="currentColor" stroke-width="2" fill="none"/>
              </svg>
            </div>
            <div class="metric-info">
              <div class="metric-label">风险等级</div>
              <div class="metric-value" :class="'risk-' + (aiReviewDetail.riskLevel || 'LOW')">
                {{ aiReviewDetail.riskLevel || 'LOW' }}
              </div>
            </div>
          </div>

          <div class="metric-card">
            <div class="metric-icon complexity">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                <path d="M12 6V12L16 14" stroke="currentColor" stroke-width="2"/>
              </svg>
            </div>
            <div class="metric-info">
              <div class="metric-label">复杂度</div>
              <div class="metric-value" :class="'complex-' + (aiReviewDetail.complexityLevel || 'LOW')">
                {{ aiReviewDetail.complexityLevel || 'LOW' }}
              </div>
            </div>
          </div>

          <div class="metric-card">
            <div class="metric-icon model">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                <rect x="4" y="4" width="16" height="16" rx="2" stroke="currentColor" stroke-width="2"/>
                <circle cx="8" cy="8" r="1" fill="currentColor"/>
                <circle cx="16" cy="16" r="1" fill="currentColor"/>
              </svg>
            </div>
            <div class="metric-info">
              <div class="metric-label">AI模型</div>
              <div class="metric-value model-name">{{ aiReviewDetail.aiModel || 'DeepSeek-V3' }}</div>
            </div>
          </div>
        </div>

        <!-- 评审摘要 -->
        <div v-if="aiReviewDetail.reviewSummary" class="summary-card">
          <div class="card-header">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M10 2C5.58 2 2 5.58 2 10C2 14.42 5.58 18 10 18C14.42 18 18 14.42 18 10C18 5.58 14.42 2 10 2ZM10 16C6.69 16 4 13.31 4 10C4 6.69 6.69 4 10 4C13.31 4 16 6.69 16 10C16 13.31 13.31 16 10 16Z" fill="currentColor"/>
              <path d="M10 6C8.9 6 8 6.9 8 8C8 9.1 8.9 10 10 10C11.1 10 12 9.1 12 8C12 6.9 11.1 6 10 6ZM10 11C8.34 11 7 12.34 7 14H13C13 12.34 11.66 11 10 11Z" fill="currentColor"/>
            </svg>
            <h3>评审摘要</h3>
          </div>
          <div class="card-content">
            <p>{{ aiReviewDetail.reviewSummary }}</p>
          </div>
        </div>

        <!-- 改进建议 -->
        <div v-if="aiReviewDetail.improvementSuggestions" class="suggestions-card">
          <div class="card-header">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M9 11L12 14L18 8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M2 11L5 14L11 8" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.3"/>
            </svg>
            <h3>改进建议</h3>
          </div>
          <div class="card-content suggestion">
            <p>{{ aiReviewDetail.improvementSuggestions }}</p>
          </div>
        </div>

        <!-- 评审时间 -->
        <div class="review-meta">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.5"/>
            <path d="M8 4V8L10 10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <span>评审时间：{{ formatDateTime(aiReviewDetail.reviewTime) }}</span>
        </div>
      </div>

      <template #footer>
        <el-button class="dialog-close-btn" @click="aiReviewDialogVisible = false">关闭报告</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElDialog, ElButton } from 'element-plus'
import { get } from '@/utils/api'
import { showError, showInfo } from '@/utils/messageUtils'
import { formatDateTime } from '@/utils/dateUtils'

const router = useRouter()
const results = ref([])
const aiReviewDialogVisible = ref(false)
const aiReviewDetail = ref(null)
const loading = ref(false)

onMounted(async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
    return
  }
  await fetchResults()
})

// 奖项徽章样式类
const getAwardCardClass = (awardLevelText) => {
  if (!awardLevelText) return 'award-card-pending'
  if (awardLevelText.includes('一等奖') || awardLevelText.includes('金奖')) return 'award-card-gold'
  if (awardLevelText.includes('二等奖') || awardLevelText.includes('银奖')) return 'award-card-silver'
  if (awardLevelText.includes('三等奖') || awardLevelText.includes('铜奖')) return 'award-card-bronze'
  return 'award-card-other'
}

const getAwardBadgeClass = (awardLevelText) => {
  if (!awardLevelText) return 'badge-pending'
  if (awardLevelText.includes('一等奖') || awardLevelText.includes('金奖')) return 'badge-gold'
  if (awardLevelText.includes('二等奖') || awardLevelText.includes('银奖')) return 'badge-silver'
  if (awardLevelText.includes('三等奖') || awardLevelText.includes('铜奖')) return 'badge-bronze'
  return 'badge-other'
}

const getAwardColor = (awardLevelText) => {
  if (!awardLevelText) return '#9E9E9E'
  if (awardLevelText.includes('一等奖') || awardLevelText.includes('金奖')) return '#FFD700'
  if (awardLevelText.includes('二等奖') || awardLevelText.includes('银奖')) return '#C0C0C0'
  if (awardLevelText.includes('三等奖') || awardLevelText.includes('铜奖')) return '#CD7F32'
  return '#5a7fa8'
}

// 评分环形进度条样式
const getScoreRingStyle = (score) => {
  const percentage = score ? (score / 100 * 360) : 0
  const color = score >= 90 ? '#4CAF50' : score >= 80 ? '#2196F3' : score >= 70 ? '#FF9800' : '#F44336'
  return {
    background: `conic-gradient(${color} ${percentage}deg, #E0E0E0 ${percentage}deg)`
  }
}

const viewAIReviewDetail = async (result) => {
  try {
    const submissionId = result.submissionId || result.id
    const data = await get(`/ai-reviews/detail/${submissionId}`)
    aiReviewDetail.value = data
    aiReviewDialogVisible.value = true
  } catch (error) {
    showError('获取AI评审详情失败')
    console.error('获取AI评审详情失败', error)
  }
}

const viewJudgeReview = (result) => {
  showInfo('评委评审详情功能开发中...')
}

const fetchResults = async () => {
  loading.value = true
  try {
    const data = await get('/student/results')
    results.value = data || []
  } catch (error) {
    showError('获取成绩失败')
    console.error('获取成绩失败', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.results-page {
  min-height: 100vh;
  background: var(--color-bg-primary);
}

/* Hero Section */
.hero-title {
  font-size: 48px;
  font-weight: 700;
  margin-bottom: 12px;
}

.subheading {
  font-size: 20px;
  color: var(--color-text-secondary);
  font-weight: 500;
}

/* Empty State */
.empty-state {
  padding: 80px 0;
}

.empty-icon {
  margin-bottom: 24px;
}

/* Results Grid Layout */
.results-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 32px;
  padding: 32px 0;
}

/* Award Card Base Design */
.award-card {
  position: relative;
  background: linear-gradient(135deg, #FFFFFF 0%, #F5F5F7 100%);
  border-radius: 20px;
  padding: 32px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08), 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.award-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15), 0 6px 16px rgba(0, 0, 0, 0.08);
}

/* Award Level Variants */
.award-card-gold {
  background: linear-gradient(135deg, #FFF8E1 0%, #FFE082 100%);
  border: 2px solid #FFD700;
}

.award-card-gold::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 6px;
  background: linear-gradient(90deg, #FFD700, #FFA500, #FFD700);
}

.award-card-silver {
  background: linear-gradient(135deg, #F5F5F7 0%, #E0E0E0 100%);
  border: 2px solid #C0C0C0;
}

.award-card-silver::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 6px;
  background: linear-gradient(90deg, #C0C0C0, #A8A8A8, #C0C0C0);
}

.award-card-bronze {
  background: linear-gradient(135deg, #FBE9E7 0%, #FFCCBC 100%);
  border: 2px solid #CD7F32;
}

.award-card-bronze::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 6px;
  background: linear-gradient(90deg, #CD7F32, #A56728, #CD7F32);
}

.award-card-other {
  background: linear-gradient(135deg, #F5F5F7 0%, #FFFFFF 100%);
  border: 2px solid #5a7fa8;
}

.award-card-pending {
  background: linear-gradient(135deg, #FFFFFF 0%, #F5F5F7 100%);
  border: 2px solid #E0E0E0;
}

/* Award Badge Section */
.award-badge-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.award-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border-radius: 50px;
  font-size: 18px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.badge-gold {
  background: linear-gradient(135deg, #FFD700, #FFA500);
  color: white;
  box-shadow: 0 4px 12px rgba(255, 215, 0, 0.4);
}

.badge-silver {
  background: linear-gradient(135deg, #C0C0C0, #A8A8A8);
  color: white;
  box-shadow: 0 4px 12px rgba(192, 192, 192, 0.4);
}

.badge-bronze {
  background: linear-gradient(135deg, #CD7F32, #A56728);
  color: white;
  box-shadow: 0 4px 12px rgba(205, 127, 50, 0.4);
}

.badge-other {
  background: linear-gradient(135deg, #5a7fa8, #4a6fa8);
  color: white;
  box-shadow: 0 4px 12px rgba(90, 127, 168, 0.3);
}

.badge-pending {
  background: linear-gradient(135deg, #9E9E9E, #BDBDBD);
  color: white;
  box-shadow: 0 4px 12px rgba(158, 158, 158, 0.3);
}

.award-text {
  letter-spacing: 1px;
}

/* Rank Badge */
.rank-badge {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 16px;
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.1), rgba(255, 165, 0, 0.2));
  border-radius: 12px;
  border: 2px solid rgba(255, 215, 0, 0.3);
}

.rank-number {
  font-size: 28px;
  font-weight: 700;
  color: #FFD700;
  font-family: 'SF Pro Display', -apple-system, sans-serif;
}

.rank-label {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-top: 4px;
}

/* Work Info Section */
.work-info-section {
  margin-bottom: 28px;
}

.work-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: 12px;
  line-height: 1.3;
}

.competition-info {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.info-tag {
  padding: 6px 12px;
  background: rgba(0, 113, 227, 0.1);
  color: #0071e3;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  border: 1px solid rgba(0, 113, 227, 0.2);
}

.type-tag {
  background: rgba(90, 127, 168, 0.1);
  color: #5a7fa8;
  border-color: rgba(90, 127, 168, 0.2);
}

/* Score Display Section */
.score-display-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  background: linear-gradient(135deg, #FFFFFF 0%, #F5F5F7 100%);
  border-radius: 16px;
  margin-bottom: 20px;
}

.score-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.score-circle {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  position: relative;
}

.score-ring {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  transition: all 0.5s ease;
}

.score-ring::before {
  content: '';
  position: absolute;
  width: 60px;
  height: 60px;
  background: white;
  border-radius: 50%;
  z-index: 1;
}

.score-number {
  position: relative;
  z-index: 2;
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text-primary);
}

.score-label {
  font-size: 12px;
  color: var(--color-text-secondary);
  font-weight: 500;
  margin-top: 8px;
}

.score-divider {
  width: 1px;
  height: 60px;
  background: linear-gradient(180deg, transparent, #E0E0E0, transparent);
}

/* Final Score Highlight */
.final-score-highlight {
  flex: 1.2;
}

.final-score-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16px 24px;
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.1), rgba(255, 165, 0, 0.2));
  border-radius: 16px;
  border: 2px solid rgba(255, 215, 0, 0.3);
  min-width: 100px;
}

.final-score-value {
  font-size: 32px;
  font-weight: 800;
  color: #FFD700;
  font-family: 'SF Pro Display', -apple-system, sans-serif;
}

.final-score-label {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-top: 4px;
  font-weight: 500;
}

/* Submission Time */
.submission-time {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: rgba(0, 0, 0, 0.03);
  border-radius: 8px;
  margin-bottom: 20px;
  color: var(--color-text-secondary);
}

.time-text {
  font-size: 13px;
  font-weight: 500;
}

/* Card Actions */
.card-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
  border: none;
  cursor: pointer;
  flex: 1;
  min-width: 140px;
  justify-content: center;
}

.detail-btn {
  background: linear-gradient(135deg, #0071e3, #0077ED);
  color: white;
}

.detail-btn:hover {
  background: linear-gradient(135deg, #0077ED, #0071e3);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 113, 227, 0.3);
}

.judge-btn {
  background: linear-gradient(135deg, #5a7fa8, #4a6fa8);
  color: white;
}

.judge-btn:hover {
  background: linear-gradient(135deg, #4a6fa8, #5a7fa8);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(90, 127, 168, 0.3);
}

/* AI Review Dialog - Simplified */
.ai-review-container {
  padding: 32px;
  background: linear-gradient(135deg, #FFFFFF 0%, #F5F5F7 100%);
  border-radius: 20px;
}

.score-hero {
  display: flex;
  gap: 40px;
  align-items: flex-start;
  padding: 32px;
  background: linear-gradient(135deg, rgba(0, 113, 227, 0.03) 0%, rgba(0, 113, 227, 0.06) 100%);
  border-radius: 16px;
  margin-bottom: 24px;
}

.score-circle-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.score-circle-big {
  width: 160px;
  height: 160px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  box-shadow: 0 8px 24px rgba(0, 113, 227, 0.2);
  transition: all 0.5s ease;
}

.score-circle-big::before {
  content: '';
  position: absolute;
  width: 120px;
  height: 120px;
  background: white;
  border-radius: 50%;
  z-index: 1;
}

.score-inner {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.score-number-big {
  font-size: 44px;
  font-weight: 800;
  color: var(--color-text-primary);
  font-family: 'SF Pro Display', -apple-system, sans-serif;
}

.score-unit {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin-top: -4px;
}

.score-title {
  font-size: 16px;
  color: var(--color-text-secondary);
  font-weight: 600;
}

.dimension-scores {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dim-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.dim-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dim-icon {
  font-size: 18px;
}

.dim-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-secondary);
}

.dim-progress {
  height: 24px;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 8px;
  overflow: hidden;
  position: relative;
}

.dim-bar {
  height: 100%;
  background: linear-gradient(90deg, #0071e3, #42A5F5);
  border-radius: 8px;
  transition: width 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

.dim-bar.practicality {
  background: linear-gradient(90deg, #2196F3, #42A5F5);
}

.dim-bar.experience {
  background: linear-gradient(90deg, #FF69B4, #DA70D6);
}

.dim-bar.documentation {
  background: linear-gradient(90deg, #4CAF50, #66BB6A);
}

/* PPT作品评分维度样式 */
.dim-bar.creativity {
  background: linear-gradient(90deg, #FF69B4, #DA70D6);
}

.dim-bar.visual {
  background: linear-gradient(90deg, #00CED1, #20B2AA);
}

.dim-bar.content {
  background: linear-gradient(90deg, #7B68EE, #6A5ACD);
}

.dim-bar.originality {
  background: linear-gradient(90deg, #FFD700, #FFA500);
}

/* VIDEO作品评分维度样式 */
.dim-bar.story {
  background: linear-gradient(90deg, #4CAF50, #66BB6A);
}

.dim-bar.director {
  background: linear-gradient(90deg, #FF9800, #FFB74D);
}

.dim-bar.code {
  background: linear-gradient(90deg, #7B68EE, #6A5ACD);
}

.dim-bar.duplicate-bar {
  background: linear-gradient(90deg, #F44336, #EF5350);
}

.dim-value {
  font-size: 13px;
  color: var(--color-text-secondary);
  font-weight: 500;
}

.dim-value.warn {
  color: #F44336;
  font-weight: 600;
}

.dim-progress.duplicate {
  background: rgba(244, 67, 54, 0.1);
}

/* Metrics Row */
.metrics-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.metric-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.metric-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(0, 113, 227, 0.1), rgba(0, 113, 227, 0.15));
  color: #0071e3;
}

.metric-icon.risk {
  background: linear-gradient(135deg, rgba(255, 152, 0, 0.1), rgba(255, 152, 0, 0.15));
  color: #FF9800;
}

.metric-icon.complexity {
  background: linear-gradient(135deg, rgba(76, 175, 80, 0.1), rgba(76, 175, 80, 0.15));
  color: #4CAF50;
}

.metric-info {
  flex: 1;
}

.metric-label {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-bottom: 4px;
}

.metric-value {
  font-size: 18px;
  font-weight: 700;
  font-family: 'SF Pro Display', -apple-system, sans-serif;
}

.risk-LOW {
  color: #4CAF50;
}

.risk-MEDIUM {
  color: #FF9800;
}

.risk-HIGH {
  color: #F44336;
}

.complex-LOW {
  color: #4CAF50;
}

.complex-MEDIUM {
  color: #FF9800;
}

.complex-HIGH {
  color: #F44336;
}

.model-name {
  color: #0071e3;
  font-size: 14px;
}

/* Summary and Suggestions Cards */
.summary-card,
.suggestions-card {
  padding: 24px;
  background: white;
  border-radius: 12px;
  margin-bottom: 16px;
  border-left: 4px solid #0071e3;
  transition: all 0.3s ease;
}

.suggestions-card {
  border-left-color: #4CAF50;
  background: linear-gradient(135deg, rgba(76, 175, 80, 0.03), white);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  color: #0071e3;
}

.suggestions-card .card-header {
  color: #4CAF50;
}

.card-header h3 {
  font-size: 18px;
  font-weight: 700;
  margin: 0;
}

.card-content p {
  font-size: 16px;
  line-height: 1.8;
  color: var(--color-text-primary);
  margin: 0;
}

.card-content.suggestion p {
  font-weight: 500;
}

/* Review Meta */
.review-meta {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px;
  background: rgba(0, 0, 0, 0.03);
  border-radius: 8px;
  font-size: 13px;
  color: var(--color-text-secondary);
}

.dialog-close-btn {
  background: linear-gradient(135deg, #0071e3, #0077ED);
  color: white;
  border: none;
  padding: 12px 32px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.dialog-close-btn:hover {
  background: linear-gradient(135deg, #0077ED, #0071e3);
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 113, 227, 0.3);
}

.mb-md {
  margin-bottom: 16px;
}

.mb-xl {
  margin-bottom: 24px;
}

.mb-lg {
  margin-bottom: 24px;
}

/* Responsive Design */
@media (max-width: 768px) {
  .results-grid {
    grid-template-columns: 1fr;
    gap: 24px;
    padding: 24px 0;
  }

  .award-card {
    padding: 24px;
  }

  .hero-title {
    font-size: 32px;
  }

  .score-display-section {
    flex-direction: column;
    gap: 16px;
  }

  .score-divider {
    width: 100%;
    height: 1px;
  }

  .action-btn {
    min-width: 100%;
  }
}

@media (max-width: 480px) {
  .work-title {
    font-size: 20px;
  }

  .score-ring {
    width: 60px;
    height: 60px;
  }

  .score-ring::before {
    width: 45px;
    height: 45px;
  }

  .score-number {
    font-size: 18px;
  }

  .final-score-value {
    font-size: 24px;
  }

  .competition-info {
    flex-direction: column;
  }
}
</style>
