<template>
  <div class="reviews-page">
    <div class="container">
      <!-- Page Header -->
      <div class="page-header slide-in">
        <h1 class="page-title">评审管理</h1>
        <p class="body-text">查看评审结果和设置奖项</p>
      </div>

      <!-- Filters -->
      <div class="filters-section card card-flat scale-in mb-lg">
        <div class="filters-row flex gap-md">
          <el-select v-model="filters.competitionId" placeholder="选择赛事" clearable style="width: 250px">
            <el-option v-for="comp in competitions" :key="comp.id" :label="comp.competitionName" :value="comp.id" />
          </el-select>

          <el-select v-model="filters.trackId" placeholder="选择赛道" clearable style="width: 200px" :disabled="!filters.competitionId">
            <el-option v-for="track in filteredTracks" :key="track.id" :label="track.trackName" :value="track.id" />
          </el-select>

          <el-select v-model="filters.awardLevel" placeholder="奖项等级" clearable style="width: 150px">
            <el-option label="一等奖" value="FIRST" />
            <el-option label="二等奖" value="SECOND" />
            <el-option label="三等奖" value="THIRD" />
            <el-option label="优秀奖" value="EXCELLENCE" />
          </el-select>

          <button class="btn-secondary" @click="fetchReviewResults">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <circle cx="10" cy="10" r="8" stroke="currentColor" stroke-width="2"/>
              <path d="M10 6V10L13 13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            搜索
          </button>
        </div>
      </div>

      <!-- Review Results List -->
      <div class="reviews-section scale-in">
        <!-- Loading -->
        <div v-if="loading" class="loading-state card">
          <div class="spinner"></div>
        </div>

        <!-- Data Table -->
        <div v-else-if="reviewResults.length > 0" class="data-table card">
          <el-table :data="reviewResults" style="width: 100%" stripe>
            <el-table-column label="提交编号" width="150">
              <template #default="{ row }">
                {{ row.submissionCode || 'N/A' }}
              </template>
            </el-table-column>
            <el-table-column label="团队名称" width="180">
              <template #default="{ row }">
                {{ row.teamName || '未知团队' }}
              </template>
            </el-table-column>
            <el-table-column label="AI评审分" width="120">
              <template #default="{ row }">
                <span :class="getScoreClass(row.aiScore)">
                  {{ row.aiScore ? row.aiScore.toFixed(1) : '未评审' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="评委评审分" width="120">
              <template #default="{ row }">
                <span :class="getScoreClass(row.judgeScore)">
                  {{ row.judgeScore ? row.judgeScore.toFixed(1) : '未评审' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="最终得分" width="120">
              <template #default="{ row }">
                <span :class="getScoreClass(row.finalScore)">
                  {{ row.finalScore ? row.finalScore.toFixed(1) : '待计算' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="奖项" width="120">
              <template #default="{ row }">
                <span :class="getAwardClass(row.awardLevel)" class="badge">
                  {{ getAwardText(row.awardLevel) || '未设置' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <span :class="getStatusClass(row.status)" class="badge">
                  {{ getStatusText(row.status) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="280" fixed="right">
              <template #default="{ row }">
                <div class="action-buttons flex gap-xs">
                  <button class="btn-action-detail" @click="viewDetail(row)">
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                      <circle cx="8" cy="6" r="4" stroke="currentColor" stroke-width="1.5"/>
                      <path d="M3 14C3 10 5.5 8 8 8C10.5 8 13 10 13 14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    </svg>
                    详情
                  </button>
                  <button class="btn-action-calc" @click="calculateFinal(row)" :disabled="row.status === 'COMPLETED'">
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                      <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.5"/>
                      <path d="M8 5V11M5 8H11" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    </svg>
                    计算结果
                  </button>
                  <button class="btn-action-award" @click="setAward(row)" :disabled="!row.finalScore">
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                      <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.5"/>
                      <path d="M8 4L10 8H6L8 4Z" fill="currentColor"/>
                    </svg>
                    设置奖项
                  </button>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <!-- Pagination -->
          <div class="pagination-wrapper mt-lg flex flex-center">
            <el-pagination
              :current-page="pagination.current"
              :page-size="pagination.size"
              :page-sizes="[10, 20, 50, 100]"
              :total="pagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>

        <!-- Empty -->
        <div v-else class="empty-state card">
          <div class="empty-icon">
            <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
              <path d="M8 12L24 6L40 12V24C40 32 32 38 24 38C16 38 8 32 8 24V12Z" stroke="#bdc3c7" stroke-width="2" stroke-linejoin="round"/>
              <path d="M16 24L20 28L32 16" stroke="#bdc3c7" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <h3 class="card-title mb-md">暂无评审结果</h3>
          <p class="caption">请先完成评审任务</p>
        </div>
      </div>

      <!-- Detail Dialog -->
      <el-dialog v-model="showDetailDialog" title="评审详情" width="800px">
        <div v-if="selectedResult" class="detail-content">
          <div class="detail-section mb-xl">
            <h4 class="section-title mb-md">基本信息</h4>
            <div class="detail-grid">
              <div class="detail-item">
                <span class="detail-label caption">提交编号</span>
                <span class="detail-value body-text">{{ selectedResult.submissionCode || 'N/A' }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label caption">团队名称</span>
                <span class="detail-value body-text">{{ selectedResult.teamName || '未知团队' }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label caption">赛道</span>
                <span class="detail-value body-text">{{ getTrackName(selectedResult.competitionTrackId) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label caption">状态</span>
                <span :class="getStatusClass(selectedResult.status)" class="badge">
                  {{ getStatusText(selectedResult.status) }}
                </span>
              </div>
            </div>
          </div>

          <div class="detail-section mb-xl">
            <h4 class="section-title mb-md">评审分数</h4>
            <div class="score-grid">
              <div class="score-item">
                <div class="score-label caption">AI评审分</div>
                <div class="score-value">{{ selectedResult.aiScore ? selectedResult.aiScore.toFixed(1) : '未评审' }}</div>
              </div>
              <div class="score-item">
                <div class="score-label caption">评委评审分</div>
                <div class="score-value">{{ selectedResult.judgeScore ? selectedResult.judgeScore.toFixed(1) : '未评审' }}</div>
              </div>
              <div class="score-item">
                <div class="score-label caption">最终得分</div>
                <div class="score-value final">{{ selectedResult.finalScore ? selectedResult.finalScore.toFixed(1) : '待计算' }}</div>
              </div>
            </div>
          </div>

          <div v-if="selectedResult.judgeReviews && selectedResult.judgeReviews.length > 0" class="detail-section">
            <h4 class="section-title mb-md">评委评审记录</h4>
            <div class="judge-reviews-list">
              <div v-for="(review, index) in selectedResult.judgeReviews" :key="index" class="judge-review-item">
                <div class="judge-info">
                  <span class="judge-name">{{ review.judgeName || '评委' + (index + 1) }}</span>
                  <span class="judge-score">{{ review.score }}分</span>
                </div>
                <div v-if="review.comments" class="judge-comments caption">
                  {{ review.comments }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-dialog>

      <!-- Award Dialog -->
      <el-dialog v-model="showAwardDialog" title="设置奖项等级" width="500px">
        <el-form :model="awardForm" label-width="100px">
          <el-form-item label="奖项等级">
            <el-select v-model="awardForm.awardLevel" placeholder="请选择奖项等级" style="width: 100%">
              <el-option label="一等奖" value="FIRST" />
              <el-option label="二等奖" value="SECOND" />
              <el-option label="三等奖" value="THIRD" />
              <el-option label="优秀奖" value="EXCELLENCE" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="showAwardDialog = false">取消</el-button>
            <el-button type="primary" @click="submitAward" :loading="submitting">设置</el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

const reviewResults = ref([])
const competitions = ref([])
const allTracks = ref([])
const judges = ref([])
const loading = ref(false)
const showDetailDialog = ref(false)
const showAwardDialog = ref(false)
const selectedResult = ref(null)
const submitting = ref(false)

const filters = reactive({
  competitionId: null,
  trackId: null,
  awardLevel: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const awardForm = reactive({
  reviewResultId: null,
  awardLevel: null
})

const filteredTracks = computed(() => {
  if (!filters.competitionId) return []
  return allTracks.value.filter(track => track.competitionId === filters.competitionId)
})

onMounted(async () => {
  await Promise.all([
    fetchCompetitions(),
    fetchReviewResults(),
    fetchJudges()
  ])
})

const fetchCompetitions = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch('/api/competitions', {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const data = await response.json()
    if (data.code === 200) {
      competitions.value = data.data.records || []
      // Fetch tracks for all competitions
      for (const comp of competitions.value) {
        const tracksResponse = await fetch(`/api/competitions/${comp.id}/tracks`, {
          headers: { 'Authorization': `Bearer ${token}` }
        })
        const tracksData = await tracksResponse.json()
        if (tracksData.code === 200) {
          allTracks.value.push(...(tracksData.data || []))
        }
      }
    }
  } catch (error) {
    ElMessage.error('获取赛事列表失败')
  }
}

const fetchReviewResults = async () => {
  loading.value = true

  try {
    const token = localStorage.getItem('token')

    if (filters.competitionId) {
      const response = await fetch(`/api/reviews/competition/${filters.competitionId}`, {
        headers: { 'Authorization': `Bearer ${token}` }
      })
      const data = await response.json()
      if (data.code === 200) {
        let result = data.data || []
        if (filters.trackId) {
          result = result.filter(r => r.competitionTrackId === filters.trackId)
        }
        if (filters.awardLevel) {
          result = result.filter(r => r.awardLevel === filters.awardLevel)
        }
        reviewResults.value = result
        pagination.total = result.length
      } else {
        ElMessage.error(data.message || '获取评审结果失败')
      }
    } else {
      reviewResults.value = []
      pagination.total = 0
    }
  } catch (error) {
    ElMessage.error('获取评审结果失败')
  } finally {
    loading.value = false
  }
}

const viewDetail = async (result) => {
  selectedResult.value = result
  showDetailDialog.value = true

  // Fetch judge reviews if not loaded
  if (result.submissionId && !result.judgeReviews) {
    try {
      const token = localStorage.getItem('token')
      const response = await fetch(`/api/reviews/judge/submission/${result.submissionId}`, {
        headers: { 'Authorization': `Bearer ${token}` }
      })
      const data = await response.json()
      if (data.code === 200) {
        selectedResult.value.judgeReviews = data.data || []
      }
    } catch (error) {
      console.error('获取评委评审记录失败')
    }
  }
}

const calculateFinal = async (result) => {
  try {
    await ElMessageBox.confirm(
      `确定要计算提交 "${result.submissionCode}" 的最终评审结果吗？`,
      '计算最终结果',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    submitting.value = true
    const token = localStorage.getItem('token')
    const response = await fetch(`/api/reviews/calculate/${result.submissionId}`, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${token}` }
    })

    const data = await response.json()
    if (data.code === 200) {
      ElMessage.success('评审结果已计算')
      await fetchReviewResults()
    } else {
      ElMessage.error(data.message || '计算失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('计算失败')
    }
  } finally {
    submitting.value = false
  }
}

const setAward = (result) => {
  awardForm.reviewResultId = result.id
  awardForm.awardLevel = result.awardLevel || null
  showAwardDialog.value = true
}

const submitAward = async () => {
  if (!awardForm.awardLevel) {
    ElMessage.warning('请选择奖项等级')
    return
  }

  submitting.value = true

  try {
    const token = localStorage.getItem('token')
    const response = await fetch(`/api/reviews/award/${awardForm.reviewResultId}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      body: new URLSearchParams({
        awardLevel: awardForm.awardLevel
      })
    })

    const data = await response.json()
    if (data.code === 200) {
      ElMessage.success('奖项已设置')
      showAwardDialog.value = false
      await fetchReviewResults()
    } else {
      ElMessage.error(data.message || '设置失败')
    }
  } catch (error) {
    ElMessage.error('设置失败')
  } finally {
    submitting.value = false
  }
}

const handleSizeChange = (val) => {
  pagination.size = val
  fetchReviewResults()
}

const handleCurrentChange = (val) => {
  pagination.current = val
  fetchReviewResults()
}

const getTrackName = (trackId) => {
  const track = allTracks.value.find(t => t.id === trackId)
  return track ? track.trackName : '未知赛道'
}

const getScoreClass = (score) => {
  if (!score) return ''
  if (score >= 90) return 'score-excellent'
  if (score >= 80) return 'score-good'
  if (score >= 70) return 'score-average'
  return 'score-low'
}

const getStatusClass = (status) => {
  return {
    'badge-warning': status === 'PENDING',
    'badge-primary': status === 'IN_PROGRESS',
    'badge-success': status === 'COMPLETED'
  }
}

const getStatusText = (status) => {
  const texts = {
    PENDING: '待评审',
    IN_PROGRESS: '评审中',
    COMPLETED: '已完成'
  }
  return texts[status] || status
}

const getAwardClass = (awardLevel) => {
  return {
    'badge-gold': awardLevel === 'FIRST',
    'badge-silver': awardLevel === 'SECOND',
    'badge-bronze': awardLevel === 'THIRD',
    'badge-blue': awardLevel === 'EXCELLENCE'
  }
}

const getAwardText = (awardLevel) => {
  const texts = {
    FIRST: '一等奖',
    SECOND: '二等奖',
    THIRD: '三等奖',
    EXCELLENCE: '优秀奖'
  }
  return texts[awardLevel] || ''
}

const fetchJudges = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch('/api/judges', {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const data = await response.json()
    if (data.code === 200) {
      judges.value = data.data || []
    }
  } catch (error) {
    ElMessage.error('获取评委列表失败')
  }
}

</script>

<style scoped>
.reviews-page {
  min-height: 100vh;
}

.page-header {
  margin-bottom: var(--spacing-xl);
}

.filters-section {
  padding: var(--spacing-lg);
}

.filters-row {
  flex-wrap: wrap;
}

.loading-state {
  padding: var(--spacing-3xl);
  text-align: center;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--color-border);
  border-top-color: var(--color-accent);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.data-table {
  padding: var(--spacing-xl);
}

.empty-state {
  padding: var(--spacing-3xl);
  text-align: center;
}

.empty-icon {
  margin-bottom: var(--spacing-lg);
  opacity: 0.5;
}

.detail-content {
  padding: var(--spacing-lg);
}

.detail-section {
  margin-bottom: var(--spacing-xl);
}

.section-title {
  font-family: var(--font-display);
  font-size: 17px;
  font-weight: 600;
  line-height: 1.24;
  letter-spacing: -0.374px;
  color: var(--color-near-black);
  margin-bottom: var(--spacing-md);
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-lg);
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.detail-label {
  color: var(--color-text-secondary);
}

.score-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--spacing-lg);
}

.score-item {
  text-align: center;
  padding: var(--spacing-lg);
  background: var(--color-surface-light);
  border-radius: 8px;
}

.score-label {
  margin-bottom: var(--spacing-sm);
}

.score-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-text);
}

.score-value.final {
  color: var(--color-accent);
}

.score-excellent {
  color: #10b981;
  font-weight: 600;
}

.score-good {
  color: #60a5fa;
  font-weight: 600;
}

.score-average {
  color: #f59e0b;
  font-weight: 600;
}

.score-low {
  color: #ef4444;
  font-weight: 600;
}

.judge-reviews-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.judge-review-item {
  padding: var(--spacing-md);
  background: var(--color-surface-light);
  border-radius: 6px;
}

.judge-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-xs);
}

.judge-name {
  font-weight: 600;
}

.judge-score {
  color: var(--color-accent);
  font-weight: 600;
}

.judge-comments {
  color: var(--color-text-secondary);
  line-height: 1.5;
}

.action-buttons {
  display: flex;
  gap: var(--spacing-xs);
  flex-wrap: wrap;
}

.btn-action-detail,
.btn-action-calc,
.btn-action-award {
  padding: 6px 12px;
  border-radius: 6px;
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  color: var(--color-text);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}

.btn-action-detail:hover {
  background: #60a5fa;
  color: var(--color-white);
  border-color: #60a5fa;
}

.btn-action-calc:hover:not(:disabled) {
  background: #10b981;
  color: var(--color-white);
  border-color: #10b981;
}

.btn-action-calc:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-action-award:hover:not(:disabled) {
  background: #f59e0b;
  color: var(--color-white);
  border-color: #f59e0b;
}

.btn-action-award:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.badge-gold {
  background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
  color: #8b4513;
  padding: 4px 12px;
  border-radius: 12px;
  font-weight: 600;
}

.badge-silver {
  background: linear-gradient(135deg, #c0c0c0 0%, #e8e8e8 100%);
  color: #4a5568;
  padding: 4px 12px;
  border-radius: 12px;
  font-weight: 600;
}

.badge-bronze {
  background: linear-gradient(135deg, #cd7f32 0%, #daa06d 100%);
  color: #8b4513;
  padding: 4px 12px;
  border-radius: 12px;
  font-weight: 600;
}

.badge-blue {
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
  padding: 4px 12px;
  border-radius: 12px;
  font-weight: 600;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-lg);
  }

  .filters-row {
    flex-direction: column;
  }

  .filters-row > * {
    width: 100%;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .score-grid {
    grid-template-columns: 1fr;
  }
}

.header-actions {
  margin-top: var(--spacing-md);
}

.assign-content {
  padding: var(--spacing-lg);
}

.assign-step {
  margin-bottom: var(--spacing-xl);
}

.submissions-list {
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: var(--spacing-md);
}

.submission-item {
  padding: var(--spacing-sm);
  border-bottom: 1px solid var(--color-border-light);
  margin-bottom: var(--spacing-xs);
}

.submission-item:last-child {
  border-bottom: none;
}

.submission-info {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.submission-code {
  font-weight: 600;
  color: var(--color-accent);
}

.submission-work {
  font-size: 14px;
  color: var(--color-text);
}

.submission-team {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.judge-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.judge-info {
  color: var(--color-text-secondary);
}
</style>