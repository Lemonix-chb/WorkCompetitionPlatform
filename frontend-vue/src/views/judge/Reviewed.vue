<template>
  <div class="reviewed-page">
    <div class="container">
      <!-- Page Header -->
      <div class="page-header slide-in">
        <h1 class="page-title">已评审作品</h1>
        <p class="body-text">查看已完成的评审记录</p>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="loading-state card">
        <div class="spinner"></div>
      </div>

      <!-- Reviewed List -->
      <div v-else-if="reviewedTasks.length > 0" class="works-section scale-in">
        <div class="data-table card">
          <el-table :data="reviewedTasks" style="width: 100%" stripe>
            <el-table-column label="作品名称" min-width="200">
              <template #default="{ row }">
                {{ getWorkName(row.submissionId) }}
              </template>
            </el-table-column>
            <el-table-column label="团队" width="180">
              <template #default="{ row }">
                {{ getTeamName(row.teamId) }}
              </template>
            </el-table-column>
            <el-table-column label="评审分数" width="120">
              <template #default="{ row }">
                <span :class="getScoreClass(row.overallScore)" class="score-badge">
                  {{ row.overallScore ? row.overallScore.toFixed(0) : 'N/A' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="评审时间" width="160">
              <template #default="{ row }">
                {{ formatDateTime(row.reviewTime) }}
              </template>
            </el-table-column>
            <el-table-column label="评审意见" min-width="200">
              <template #default="{ row }">
                <span class="comment-text">{{ row.reviewComment || '无意见' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <button class="btn-action-view" @click="viewReviewDetail(row)">
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <circle cx="8" cy="6" r="4" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M3 14C3 10 5.5 8 8 8C10.5 8 13 10 13 14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                  查看详情
                </button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- Empty State -->
      <div v-else class="works-section scale-in">
        <div class="empty-state card">
          <div class="empty-icon">
            <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
              <circle cx="24" cy="24" r="20" stroke="#bdc3c7" stroke-width="2"/>
              <path d="M18 24L22 28L30 20" stroke="#bdc3c7" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <h3 class="card-title mb-md">暂无已评审作品</h3>
          <p class="caption">开始评审作品吧</p>
        </div>
      </div>

      <!-- Detail Dialog -->
      <el-dialog v-model="showDetailDialog" title="评审详情" width="700px">
        <div v-if="currentReview" class="detail-content">
          <div class="detail-section mb-xl">
            <h4 class="section-title mb-md">作品信息</h4>
            <div class="detail-grid">
              <div class="detail-item">
                <span class="detail-label caption">作品名称</span>
                <span class="detail-value body-text">{{ getWorkName(currentReview.submissionId) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label caption">团队名称</span>
                <span class="detail-value body-text">{{ getTeamName(currentReview.teamId) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label caption">作品类型</span>
                <span class="detail-value body-text">{{ getWorkType(currentReview.submissionId) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label caption">评审时间</span>
                <span class="detail-value caption">{{ formatDateTime(currentReview.reviewTime) }}</span>
              </div>
            </div>
          </div>

          <div class="detail-section mb-xl">
            <h4 class="section-title mb-md">评审结果</h4>
            <div class="score-display">
              <div class="score-item">
                <div class="score-label caption">评审分数</div>
                <div class="score-value">{{ currentReview.overallScore ? currentReview.overallScore.toFixed(0) : 'N/A' }}</div>
              </div>
            </div>
          </div>

          <div v-if="currentReview.reviewComment" class="detail-section">
            <h4 class="section-title mb-md">评审意见</h4>
            <p class="body-text">{{ currentReview.reviewComment }}</p>
          </div>

          <!-- Attachments Section -->
          <div v-if="currentWorkAttachments.length > 0" class="detail-section">
            <h4 class="section-title mb-md">作品附件</h4>
            <div class="attachments-list">
              <div v-for="attachment in currentWorkAttachments" :key="attachment.id" class="attachment-item">
                <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                  <path d="M4 4H16M6 4V16C6 17 7 18 8 18H12C13 18 14 17 14 16V4" stroke="currentColor" stroke-width="2"/>
                </svg>
                <div class="attachment-info">
                  <span class="attachment-name body-text">{{ attachment.fileName }}</span>
                  <span class="attachment-meta caption">
                    {{ getAttachmentTypeText(attachment.attachmentType) }} |
                    {{ formatFileSize(attachment.fileSize) }}
                  </span>
                </div>
                <button class="btn-download" @click="handleDownloadAttachment(attachment)">
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <path d="M8 3V11M4 7L8 11L12 7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  下载
                </button>
              </div>
            </div>
          </div>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  formatFileSize,
  getAttachmentTypeText,
  downloadAttachment,
  fetchWorkAttachments
} from '@/utils/attachmentUtils'

const router = useRouter()

const reviewedTasks = ref([])
const submissions = ref([])
const works = ref([])
const teams = ref([])
const loading = ref(false)
const showDetailDialog = ref(false)
const currentReview = ref(null)
const currentWorkAttachments = ref([])

onMounted(async () => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('userRole')

  if (!token || role !== 'JUDGE') {
    ElMessage.warning('您没有权限访问此页面')
    router.push('/login')
    return
  }

  await fetchReviewedTasks()
})

const fetchReviewedTasks = async () => {
  loading.value = true
  try {
    const token = localStorage.getItem('token')

    // 获取我的评审任务
    const response = await fetch('/api/reviews/my', {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const data = await response.json()

    if (data.code === 200) {
      // 过滤出已评审的任务（SUBMITTED状态）
      const allTasks = data.data || []
      reviewedTasks.value = allTasks.filter(task => task.status === 'SUBMITTED')

      // 如果有任务，获取相关的submission和work信息
      if (reviewedTasks.value.length > 0) {
        await Promise.all([
          fetchSubmissions(),
          fetchWorks(),
          fetchTeams()
        ])
      }
    } else {
      ElMessage.error(data.message || '获取评审记录失败')
    }
  } catch (error) {
    ElMessage.error('获取评审记录失败')
  } finally {
    loading.value = false
  }
}

const fetchSubmissions = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch('/api/submissions/all', {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const data = await response.json()
    if (data.code === 200) {
      submissions.value = data.data || []
    }
  } catch (error) {
    console.error('获取提交列表失败', error)
  }
}

const fetchWorks = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch('/api/works/all', {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const data = await response.json()
    if (data.code === 200) {
      works.value = data.data || []
    }
  } catch (error) {
    console.error('获取作品列表失败', error)
  }
}

const fetchTeams = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch('/api/teams', {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const data = await response.json()
    if (data.code === 200) {
      teams.value = data.data || []
    }
  } catch (error) {
    console.error('获取团队列表失败', error)
  }
}

const getSubmission = (submissionId) => {
  return submissions.value.find(s => s.id === submissionId)
}

const getWork = (submissionId) => {
  const submission = getSubmission(submissionId)
  if (!submission) return null
  return works.value.find(w => w.id === submission.workId)
}

const getWorkName = (submissionId) => {
  const work = getWork(submissionId)
  return work ? work.workName : '未知作品'
}

const getTeamName = (teamId) => {
  const team = teams.value.find(t => t.id === teamId)
  return team ? team.teamName : '未知团队'
}

const getWorkType = (submissionId) => {
  const work = getWork(submissionId)
  if (!work) return '未知类型'
  const texts = {
    CODE: '程序设计',
    PPT: '演示文稿',
    VIDEO: '数媒动漫'
  }
  return texts[work.workType] || work.workType
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

const getScoreClass = (score) => {
  if (!score) return ''
  if (score >= 90) return 'score-excellent'
  if (score >= 80) return 'score-good'
  if (score >= 60) return 'score-pass'
  return 'score-fail'
}

const viewReviewDetail = async (review) => {
  currentReview.value = review
  showDetailDialog.value = true
  const work = getWork(review.submissionId)
  if (work) {
    const token = localStorage.getItem('token')
    currentWorkAttachments.value = await fetchWorkAttachments(work.id, token)
  }
}

const handleDownloadAttachment = async (attachment) => {
  const token = localStorage.getItem('token')
  await downloadAttachment(attachment, token)
}
</script>

<style scoped>
.reviewed-page {
  min-height: 100vh;
}

.page-header {
  margin-bottom: var(--spacing-2xl);
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

.works-section {
  animation-delay: 0.1s;
}

.data-table {
  padding: var(--spacing-xl);
}

.score-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-weight: 600;
}

.score-excellent {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.score-good {
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
}

.score-pass {
  background: rgba(251, 191, 36, 0.1);
  color: #fbbf24;
}

.score-fail {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.comment-text {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.btn-action-view {
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
}

.btn-action-view:hover {
  background: #60a5fa;
  color: var(--color-white);
  border-color: #60a5fa;
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

.score-display {
  display: flex;
  justify-content: center;
}

.score-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-md);
}

.score-label {
  color: var(--color-text-secondary);
}

.score-value {
  font-size: 48px;
  font-weight: 700;
  color: var(--color-accent);
}

.attachments-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-md);
  background: var(--color-bg-primary);
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
}

.attachment-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.attachment-name {
  font-weight: 500;
}

.attachment-meta {
  color: var(--color-text-secondary);
}

.btn-download {
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--color-accent);
  color: var(--color-text-on-accent);
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: 14px;
  transition: all 0.2s ease;
}

.btn-download:hover {
  background: var(--color-accent-hover);
  transform: translateY(-1px);
}

@media (max-width: 768px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>