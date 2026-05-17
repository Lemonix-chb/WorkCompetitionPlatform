<template>
  <div class="pending-page">
    <div class="container">
      <!-- Page Header -->
      <div class="page-header slide-in">
        <h1 class="page-title">待评审作品</h1>
        <p class="body-text">查看需要评审的作品列表</p>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="loading-state card">
        <div class="spinner"></div>
      </div>

      <!-- Works List -->
      <div v-else-if="pendingTasks.length > 0" class="works-section scale-in">
        <div class="grid grid-auto">
          <div v-for="task in pendingTasks" :key="task.id" class="task-card card card-hover">
            <!-- Header -->
            <div class="card-header">
              <h3 class="card-title">{{ getWorkName(task.submissionId) }}</h3>
              <span class="badge badge-warning">待评审</span>
            </div>

            <!-- Info -->
            <div class="task-info-grid mb-lg">
              <div class="info-item">
                <span class="info-label caption">团队</span>
                <span class="info-value body-text">{{ getTeamName(task.teamId) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label caption">提交编号</span>
                <span class="info-value caption">{{ getSubmissionCode(task.submissionId) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label caption">分配时间</span>
                <span class="info-value caption">{{ formatDateTime(task.createTime) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label caption">作品类型</span>
                <span class="info-value body-text">{{ getWorkType(task.submissionId) }}</span>
              </div>
            </div>

            <!-- Actions -->
            <div class="card-footer flex gap-md">
              <button class="btn-primary" @click="showReviewDialog(task)">
                <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                  <circle cx="10" cy="10" r="7" stroke="currentColor" stroke-width="2"/>
                  <path d="M10 7V13M7 10H13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
                开始评审
              </button>
              <button class="btn-secondary" @click="viewWorkDetail(task.submissionId)">
                <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                  <circle cx="10" cy="6" r="4" stroke="currentColor" stroke-width="1.5"/>
                  <path d="M3 14C3 10 5.5 8 10 8C14.5 8 17 10 17 14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
                查看作品
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-else class="works-section scale-in">
        <div class="empty-state card">
          <div class="empty-icon">
            <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
              <rect x="8" y="8" width="32" height="28" rx="3" stroke="#bdc3c7" stroke-width="2"/>
              <path d="M12 16H36M12 20H36M12 24H28" stroke="#bdc3c7" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <h3 class="card-title mb-md">暂无待评审作品</h3>
          <p class="caption">等待管理员分配评审任务</p>
        </div>
      </div>

      <!-- Review Dialog -->
      <el-dialog v-model="showReviewDialogFlag" title="提交评审" width="700px">
        <!-- AI Review Reference Section -->
        <div v-if="currentAIReport" class="ai-review-reference mb-lg">
          <div class="reference-header">
            <span class="badge badge-info">AI评审参考</span>
            <span class="ai-score">AI综合得分: <strong>{{ currentAIReport.overallScore }}</strong> 分</span>
          </div>

          <div class="reference-content mt-md">
            <div class="ai-summary-section">
              <span class="section-label caption">AI评审摘要</span>
              <p class="body-text">{{ currentAIReport.reviewSummary }}</p>
            </div>

            <div class="ai-scores-grid mt-md">
              <div class="ai-score-item">
                <span class="score-label caption">创新性</span>
                <span class="score-value">{{ currentAIReport.innovationScore }}</span>
              </div>
              <div class="ai-score-item">
                <span class="score-label caption">实用性</span>
                <span class="score-value">{{ currentAIReport.practicalityScore }}</span>
              </div>
              <div class="ai-score-item">
                <span class="score-label caption">用户体验</span>
                <span class="score-value">{{ currentAIReport.userExperienceScore }}</span>
              </div>
              <div class="ai-score-item">
                <span class="score-label caption">文档质量</span>
                <span class="score-value">{{ currentAIReport.documentationScore }}</span>
              </div>
            </div>

            <div class="ai-suggestions-section mt-md" v-if="currentAIReport.improvementSuggestions">
              <span class="section-label caption">AI改进建议</span>
              <p class="body-text">{{ currentAIReport.improvementSuggestions }}</p>
            </div>
          </div>

          <div class="reference-note caption mt-md">
            以上为AI评审结果，仅供评审参考，请根据您的专业判断进行评分。
          </div>
        </div>

        <!-- Review Form -->
        <el-form :model="reviewForm" label-width="120px">
          <el-form-item label="作品名称">
            <span class="body-text">{{ currentTask ? getWorkName(currentTask.submissionId) : '' }}</span>
          </el-form-item>
          <el-form-item label="团队名称">
            <span class="body-text">{{ currentTask ? getTeamName(currentTask.teamId) : '' }}</span>
          </el-form-item>

          <!-- 三个维度评分 -->
          <div class="score-section">
            <h4 class="score-section-title mb-md">维度评分（满分100分）</h4>

            <el-form-item label="创新性" required>
              <el-slider
                v-model="reviewForm.innovationScore"
                :min="0"
                :max="35"
                show-input
              />
              <span class="score-hint caption">作品的新颖性、独特性和创意程度</span>
            </el-form-item>

            <el-form-item label="实用性" required>
              <el-slider
                v-model="reviewForm.practicalityScore"
                :min="0"
                :max="35"
                show-input
              />
              <span class="score-hint caption">作品的应用价值、解决问题能力</span>
            </el-form-item>

            <el-form-item label="用户体验" required>
              <el-slider
                v-model="reviewForm.userExperienceScore"
                :min="0"
                :max="30"
                show-input
              />
              <span class="score-hint caption">作品的易用性、交互设计、视觉呈现</span>
            </el-form-item>

            <!-- 自动计算总分 -->
            <el-form-item label="综合得分">
              <div class="total-score-display">
                <span class="total-score-value">{{ calculateTotalScore() }}</span>
                <span class="total-score-unit caption">/100分</span>
              </div>
            </el-form-item>
          </div>

          <el-form-item label="评审意见">
            <el-input
              v-model="reviewForm.comments"
              type="textarea"
              :rows="5"
              placeholder="请输入评审意见，包括作品的优点、不足和改进建议"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="showReviewDialogFlag = false">取消</el-button>
            <el-button type="primary" @click="submitReview" :loading="submitting">
              提交评审
            </el-button>
          </div>
        </template>
      </el-dialog>

      <!-- Work Detail Dialog -->
      <el-dialog v-model="showWorkDetailDialog" title="作品详情" width="700px">
        <div v-if="currentWork" class="detail-content">
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label caption">作品编号</span>
              <span class="detail-value body-text">{{ currentWork.workCode }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label caption">作品名称</span>
              <span class="detail-value body-text">{{ currentWork.workName }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label caption">团队</span>
              <span class="detail-value body-text">{{ getTeamName(currentWork.teamId) }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label caption">作品类型</span>
              <span class="detail-value body-text">{{ getWorkTypeText(currentWork.workType) }}</span>
            </div>
          </div>

          <div v-if="currentWork.description" class="detail-section mt-lg">
            <h4 class="section-title mb-md">作品简介</h4>
            <p class="body-text">{{ currentWork.description }}</p>
          </div>

          <div v-if="currentWork.innovationPoints" class="detail-section mt-lg">
            <h4 class="section-title mb-md">创新点说明</h4>
            <p class="body-text">{{ currentWork.innovationPoints }}</p>
          </div>

          <div v-if="currentWork.keyFeatures" class="detail-section mt-lg">
            <h4 class="section-title mb-md">关键功能特性</h4>
            <p class="body-text">{{ currentWork.keyFeatures }}</p>
          </div>

          <div v-if="currentWork.techStack" class="detail-section mt-lg">
            <h4 class="section-title mb-md">技术栈</h4>
            <p class="body-text">{{ currentWork.techStack }}</p>
          </div>

          <!-- Attachments Section -->
          <div v-if="currentWorkAttachments.length > 0" class="detail-section mt-lg">
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { get, post } from '@/utils/api'
import { formatDateTime } from '@/utils/dateUtils'
import { showSuccess, showWarning, showConfirm } from '@/utils/messageUtils'
import {
  formatFileSize,
  getAttachmentTypeText,
  downloadAttachment,
  fetchWorkAttachments
} from '@/utils/attachmentUtils'

const router = useRouter()

const pendingTasks = ref([])
const submissions = ref([])
const works = ref([])
const teams = ref([])
const loading = ref(false)
const submitting = ref(false)
const showReviewDialogFlag = ref(false)
const showWorkDetailDialog = ref(false)
const currentTask = ref(null)
const currentWork = ref(null)
const currentWorkAttachments = ref([])
const currentAIReport = ref(null) // AI评审报告

const reviewForm = reactive({
  judgeReviewId: null,
  innovationScore: 25,
  practicalityScore: 25,
  userExperienceScore: 20,
  comments: ''
})

const dimensionMarks = {
  0: '0分',
  20: '20分',
  35: '35分'
}

const scoreMarks = {
  0: '0分',
  60: '60分',
  80: '80分',
  100: '100分'
}

// 计算总分
const calculateTotalScore = () => {
  return reviewForm.innovationScore + reviewForm.practicalityScore + reviewForm.userExperienceScore
}

onMounted(async () => {
  const role = localStorage.getItem('userRole')

  if (role !== 'JUDGE') {
    showWarning('您没有权限访问此页面')
    router.push('/login')
    return
  }

  await fetchPendingTasks()
})

const fetchPendingTasks = async () => {
  loading.value = true
  try {
    // 获取我的评审任务
    const data = await get('/reviews/my')

    // 过滤出待评审的任务（DRAFT状态）
    const allTasks = data || []
    pendingTasks.value = allTasks.filter(task => task.status === 'DRAFT')

    // 如果有任务，获取相关的submission和work信息
    if (pendingTasks.value.length > 0) {
      await Promise.all([
        fetchSubmissions(),
        fetchWorks(),
        fetchTeams()
      ])
    }
  } catch (error) {
    // 错误已由 API 拦截器统一处理
    console.error('获取评审任务失败', error)
  } finally {
    loading.value = false
  }
}

const fetchSubmissions = async () => {
  try {
    const data = await get('/submissions/all')
    submissions.value = data || []
  } catch (error) {
    console.error('获取提交列表失败', error)
  }
}

const fetchWorks = async () => {
  try {
    const data = await get('/works/all')
    works.value = data || []
  } catch (error) {
    console.error('获取作品列表失败', error)
  }
}

const fetchTeams = async () => {
  try {
    const data = await get('/teams')
    teams.value = data || []
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

const getSubmissionCode = (submissionId) => {
  const submission = getSubmission(submissionId)
  return submission ? submission.submissionCode : 'N/A'
}

const getWorkType = (submissionId) => {
  const work = getWork(submissionId)
  if (!work) return '未知类型'
  return getWorkTypeText(work.workType)
}

const getWorkTypeText = (workType) => {
  const texts = {
    CODE: '程序设计',
    PPT: '演示文稿',
    VIDEO: '数媒动漫'
  }
  return texts[workType] || workType
}

const showReviewDialog = async (task) => {
  currentTask.value = task
  reviewForm.judgeReviewId = task.id
  reviewForm.innovationScore = 20
  reviewForm.practicalityScore = 20
  reviewForm.userExperienceScore = 20
  reviewForm.comments = ''
  currentAIReport.value = null

  // 加载 AI 评审报告作为参考
  try {
    const report = await get(`/ai-reviews/judge-report/${task.submissionId}`)
    currentAIReport.value = report

    // 如果有AI评分，可以作为初始参考值
    if (report) {
      reviewForm.innovationScore = report.innovationScore || 20
      reviewForm.practicalityScore = report.practicalityScore || 20
      reviewForm.userExperienceScore = report.userExperienceScore || 20
    }
  } catch (error) {
    // AI评审报告不存在时不影响评审流程
    console.log('AI评审报告不存在或加载失败', error)
  }

  showReviewDialogFlag.value = true
}

const viewWorkDetail = async (submissionId) => {
  const work = getWork(submissionId)
  if (work) {
    currentWork.value = work
    showWorkDetailDialog.value = true
    currentWorkAttachments.value = await fetchWorkAttachments(work.id)
  } else {
    showWarning('无法获取作品详情')
  }
}

const handleDownloadAttachment = async (attachment) => {
  await downloadAttachment(attachment)
}

const submitReview = async () => {
  if (!reviewForm.innovationScore || !reviewForm.practicalityScore || !reviewForm.userExperienceScore) {
    showWarning('请完成所有维度的评分')
    return
  }

  try {
    await showConfirm('提交评审后将无法修改，确认提交吗？', '提交确认')

    submitting.value = true

    // 构造URL查询参数（包含三个维度的评分）
    const queryParams = new URLSearchParams({
      judgeReviewId: reviewForm.judgeReviewId,
      innovationScore: reviewForm.innovationScore,
      practicalityScore: reviewForm.practicalityScore,
      userExperienceScore: reviewForm.userExperienceScore,
      overallScore: calculateTotalScore(),
      comments: reviewForm.comments || ''
    }).toString()

    await post(`/reviews/judge?${queryParams}`)

    showSuccess('评审已提交')
    showReviewDialogFlag.value = false
    await fetchPendingTasks()
  } catch (error) {
    if (error.message !== 'cancel') {
      // 错误已由 API 拦截器处理
      console.error('提交评审失败', error)
    }
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.pending-page {
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

.task-card {
  padding: var(--spacing-xl);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

.task-info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-md);
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.info-label {
  color: var(--color-text-secondary);
}

.info-value {
  font-weight: 500;
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

.detail-section {
  padding: var(--spacing-md);
  background: var(--color-bg-secondary);
  border-radius: var(--radius-md);
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
  .task-info-grid {
    grid-template-columns: 1fr;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }
}

/* ===== AI 评审参考区样式 ===== */

.ai-review-reference {
  padding: var(--spacing-lg);
  background: linear-gradient(135deg, rgba(52, 152, 219, 0.05) 0%, rgba(52, 152, 219, 0.1) 100%);
  border: 1px solid var(--color-accent);
  border-radius: var(--radius-md);
}

.reference-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.badge-info {
  background: var(--color-accent);
  color: white;
}

.ai-score {
  font-size: var(--text-lg);
  color: var(--color-text);
}

.ai-score strong {
  color: var(--color-accent);
  font-weight: 700;
}

.reference-content {
  display: flex;
  flex-direction: column;
}

.section-label {
  color: var(--color-text-secondary);
  font-weight: 600;
}

.ai-summary-section,
.ai-suggestions-section {
  padding: var(--spacing-md);
  background: var(--color-bg-secondary);
  border-radius: var(--radius-sm);
}

.ai-scores-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-md);
}

.ai-score-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--spacing-sm);
  background: var(--color-bg-secondary);
  border-radius: var(--radius-sm);
}

.ai-score-item .score-label {
  color: var(--color-text-secondary);
}

.ai-score-item .score-value {
  font-size: var(--text-xl);
  font-weight: 700;
  color: var(--color-accent);
}

.reference-note {
  padding: var(--spacing-sm);
  background: rgba(52, 152, 219, 0.1);
  border-radius: var(--radius-sm);
  color: var(--color-text-secondary);
  text-align: center;
}

@media (max-width: 768px) {
  .ai-scores-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .reference-header {
    flex-direction: column;
    gap: var(--spacing-sm);
  }
}

/* ===== 三维度评分样式 ===== */

.score-section {
  padding: var(--spacing-lg);
  background: var(--color-bg-secondary);
  border-radius: var(--radius-md);
  margin-bottom: var(--spacing-lg);
}

.score-section-title {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  font-weight: 600;
  color: var(--color-primary);
  padding-bottom: var(--spacing-sm);
  border-bottom: 2px solid var(--color-accent);
}

.score-hint {
  display: block;
  margin-top: var(--spacing-xs);
  color: var(--color-text-secondary);
  font-size: var(--text-xs);
  line-height: 1.4;
}

.total-score-display {
  display: flex;
  align-items: baseline;
  gap: var(--spacing-sm);
}

.total-score-value {
  font-family: var(--font-display);
  font-size: var(--text-4xl);
  font-weight: 700;
  color: var(--color-accent);
  line-height: 1;
}

.total-score-unit {
  color: var(--color-text-secondary);
}

@media (max-width: 768px) {
  .ai-scores-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .reference-header {
    flex-direction: column;
    gap: var(--spacing-sm);
  }

  .score-section {
    padding: var(--spacing-md);
  }

  .total-score-value {
    font-size: var(--text-3xl);
  }
}
</style>