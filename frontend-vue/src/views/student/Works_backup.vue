<template>
  <div class="works-page">
    <div class="container">
      <!-- Page Header -->
      <div class="page-header slide-in">
        <h1 class="page-title">我的作品</h1>
        <p class="body-text">管理和提交参赛作品</p>
      </div>

      <!-- Action Bar -->
      <div class="action-bar scale-in mb-lg">
        <button class="btn-primary" @click="showCreateDialog">
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M10 4V16M4 10H16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          创建新作品
        </button>
      </div>

      <!-- Works List -->
      <div class="works-section">
        <!-- Loading -->
        <div v-if="loading" class="loading-state">
          <div class="spinner"></div>
        </div>

        <!-- Empty State -->
        <div v-else-if="works.length === 0" class="empty-state card">
          <div class="empty-icon">
            <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
              <rect x="8" y="8" width="32" height="24" rx="3" stroke="#bdc3c7" stroke-width="2"/>
              <path d="M12 16H36M12 20H36M12 24H28" stroke="#bdc3c7" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <h3 class="card-title mb-md">暂无作品</h3>
          <p class="caption">创建团队后即可开始制作作品</p>
        </div>

        <!-- Works Grid -->
        <div v-else class="grid grid-auto">
          <div v-for="work in works" :key="work.id" class="work-card card card-hover">
            <!-- Header -->
            <div class="card-header">
              <h3 class="card-title">{{ work.workName }}</h3>
              <span :class="getStatusClass(work.developmentStatus)" class="badge">
                {{ getStatusText(work.developmentStatus) }}
              </span>
            </div>

            <!-- Submission Countdown -->
            <div v-if="getSubmissionCountdown(work)" class="countdown-section mb-md">
              <span class="countdown-text accent">{{ getSubmissionCountdown(work) }}</span>
            </div>

            <!-- Submission Not Started Warning -->
            <div v-if="isSubmissionNotStarted(work)" class="deadline-warning mb-md">
              <span class="warning-text">⏰ 提交未开始（{{ getSubmissionStartTime(work) }}）</span>
            </div>

            <!-- Submission Deadline Warning -->
            <div v-if="isSubmissionClosed(work)" class="deadline-warning mb-md">
              <span class="warning-text">⚠️ 提交已截止</span>
            </div>

            <!-- Info -->
            <div class="work-info-grid mb-lg">
              <div class="info-item">
                <span class="info-label caption">作品编号</span>
                <span class="info-value caption">{{ work.workCode }}</span>
              </div>
              <div class="info-item">
                <span class="info-label caption">作品类型</span>
                <span class="info-value body-text">{{ getWorkTypeText(work.workType) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label caption">当前版本</span>
                <span class="info-value body-text">v{{ work.currentVersion }}</span>
              </div>
              <div class="info-item">
                <span class="info-label caption">创建时间</span>
                <span class="info-value caption">{{ formatDate(work.createTime) }}</span>
              </div>
            </div>

            <!-- Description -->
            <div v-if="work.description" class="work-description mb-lg">
              <p class="caption">{{ work.description }}</p>
            </div>

            <!-- Attachments -->
            <div class="work-attachments mb-lg">
              <h4 class="section-title mb-md">作品附件</h4>
              <div v-if="getWorkAttachments(work.id).length === 0" class="caption">
                暂无附件
              </div>
              <div v-else class="attachments-list">
                <div v-for="attachment in getWorkAttachments(work.id)" :key="attachment.id" class="attachment-item">
                  <span class="attachment-name body-text">{{ attachment.fileName }}</span>
                  <span class="attachment-size caption">{{ formatFileSize(attachment.fileSize) }}</span>
                  <button class="btn-text btn-delete" @click="deleteAttachment(attachment)">删除</button>
                </div>
              </div>
            </div>

            <!-- AI Review Status -->
            <div v-if="work.aiReviewStatus" class="ai-review-status mb-lg">
              <div class="status-header">
                <span class="status-label caption">AI审核状态</span>
                <span :class="getAIStatusClass(work.aiReviewStatus.status)" class="badge">
                  {{ getAIStatusText(work.aiReviewStatus.status) }}
                </span>
              </div>

              <!-- Validating Progress -->
              <div v-if="work.aiReviewStatus.status === 'VALIDATING'" class="review-progress">
                <div class="progress-bar">
                  <div class="progress-fill"></div>
                </div>
                <span class="progress-text caption">AI正在审核中，请稍候...</span>
              </div>

              <!-- Review Result -->
              <div v-if="work.aiReviewStatus.status === 'VALID' || work.aiReviewStatus.status === 'INVALID'" class="review-result">
                <div class="score-display">
                  <span :class="work.aiReviewStatus.overallScore >= 60 ? 'score-pass' : 'score-fail'" class="score-value">
                    {{ work.aiReviewStatus.overallScore }}分
                  </span>
                  <span class="badge-lg" :class="getRiskClass(work.aiReviewStatus.riskLevel)">
                    {{ getRiskText(work.aiReviewStatus.riskLevel) }}风险
                  </span>
                </div>
                <div v-if="work.aiReviewStatus.status === 'INVALID'" class="risk-info caption">
                  ⚠️ 作品存在较大问题，建议修改后重新提交
                </div>
              </div>

              <!-- View Report Button -->
              <button v-if="work.aiReviewStatus.status !== 'PENDING'" class="btn-secondary mt-md" @click="viewAIReport(work)">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <rect x="2" y="2" width="12" height="12" rx="2" stroke="currentColor" stroke-width="1.5"/>
                  <path d="M5 5H11M5 7H11M5 9H8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
                查看详细报告
              </button>

              <!-- Manual Refresh Status Button -->
              <button v-if="work.aiReviewStatus.status === 'VALIDATING'" class="btn-text mt-sm" @click="refreshAIStatus(work)">
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                  <path d="M7 1V3M7 11V13M1 7H3M11 7H13M3.5 3.5L4.9 4.9M9.1 9.1L10.5 10.5M3.5 10.5L4.9 9.1M9.1 4.9L10.5 3.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
                刷新状态
              </button>
            </div>

            <!-- Actions -->
            <div class="card-actions flex gap-md">
              <button class="btn-secondary" @click="editWork(work)">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <path d="M2 14L14 2M14 2H8M14 2V8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
                编辑作品
              </button>

              <button class="btn-secondary" @click="showUploadDialog(work)">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <path d="M8 2V14M4 8L8 4L12 8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
                上传文件
              </button>

              <button v-if="work.developmentStatus === 'IN_PROGRESS'" class="btn-secondary" @click="markCompleted(work)">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.5"/>
                  <path d="M5 8L7 10L11 6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                标记完成
              </button>

              <button v-if="work.developmentStatus === 'COMPLETED'" class="btn-primary" @click="submitWork(work)">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.5"/>
                  <path d="M5 8L7 10L11 6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                提交作品
              </button>

              <button v-if="work.developmentStatus === 'SUBMITTED'" class="btn-secondary" @click="submitWork(work)">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.5"/>
                  <path d="M5 8L7 10L11 6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                重新提交
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Create Work Dialog -->
      <el-dialog
        v-model="showCreateWorkDialog"
        title="创建新作品"
        width="600px"
      >
        <el-form :model="createWorkForm" label-width="100px">
          <el-form-item label="作品名称" required>
            <el-input
              v-model="createWorkForm.workName"
              placeholder="请输入作品名称"
            />
          </el-form-item>
          <el-form-item label="选择团队" required>
            <el-select
              v-model="createWorkForm.teamId"
              placeholder="请选择团队"
              @change="handleTeamChange"
            >
              <el-option
                v-for="team in teams"
                :key="team.id"
                :label="team.teamName"
                :value="team.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="作品简介">
            <el-input
              v-model="createWorkForm.description"
              type="textarea"
              :rows="3"
              placeholder="请输入作品简介"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="showCreateWorkDialog = false">取消</el-button>
            <el-button type="primary" @click="handleCreateWork" :loading="creating">
              创建作品
            </el-button>
          </div>
        </template>
      </el-dialog>

      <!-- Edit Work Dialog -->
      <el-dialog
        v-model="showEditWorkDialog"
        title="编辑作品"
        width="600px"
      >
        <el-form :model="editWorkForm" label-width="100px">
          <el-form-item label="作品名称" required>
            <el-input
              v-model="editWorkForm.workName"
              placeholder="请输入作品名称"
            />
          </el-form-item>
          <el-form-item label="作品简介">
            <el-input
              v-model="editWorkForm.description"
              type="textarea"
              :rows="3"
              placeholder="请输入作品简介"
            />
          </el-form-item>
          <el-form-item label="创新点说明">
            <el-input
              v-model="editWorkForm.innovationPoints"
              type="textarea"
              :rows="2"
              placeholder="请描述作品的创新点"
            />
          </el-form-item>
          <el-form-item label="关键功能">
            <el-input
              v-model="editWorkForm.keyFeatures"
              type="textarea"
              :rows="2"
              placeholder="请描述关键功能特性"
            />
          </el-form-item>
          <el-form-item label="技术栈" v-if="editWorkForm.workType === 'CODE'">
            <el-input
              v-model="editWorkForm.techStack"
              placeholder="请输入使用的技术栈"
            />
          </el-form-item>
          <el-form-item label="团队分工">
            <el-input
              v-model="editWorkForm.divisionOfLabor"
              type="textarea"
              :rows="2"
              placeholder="请描述团队成员分工"
            />
          </el-form-item>
          <el-form-item label="目标用户">
            <el-input
              v-model="editWorkForm.targetUsers"
              placeholder="请描述目标用户或应用场景"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="showEditWorkDialog = false">取消</el-button>
            <el-button type="primary" @click="handleUpdateWork" :loading="updating">
              保存修改
            </el-button>
          </div>
        </template>
      </el-dialog>

      <!-- Upload File Dialog -->
      <el-dialog
        v-model="showUploadFileDialog"
        title="上传作品文件"
        width="500px"
      >
        <el-form :model="uploadForm" label-width="100px">
          <el-form-item label="选择文件" required>
            <input
              type="file"
              @change="handleFileSelect"
              class="file-input"
            />
            <div v-if="uploadForm.file" class="selected-file">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M4 4H16M6 4V16C6 17 7 18 8 18H12C13 18 14 17 14 16V4" stroke="currentColor" stroke-width="2"/>
              </svg>
              <span>{{ uploadForm.file.name }}</span>
              <span class="file-size caption">{{ formatFileSize(uploadForm.file.size) }}</span>
            </div>
          </el-form-item>
          <el-form-item label="文件类型" required>
            <el-radio-group v-model="uploadForm.attachmentType">
              <el-radio label="SOURCE">源代码</el-radio>
              <el-radio label="DOCUMENT">文档</el-radio>
              <el-radio label="DEMO">演示视频</el-radio>
              <el-radio label="OTHER">其他</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="showUploadFileDialog = false">取消</el-button>
            <el-button type="primary" @click="handleUploadFile" :loading="uploading">
              开始上传
            </el-button>
          </div>
        </template>
      </el-dialog>

      <!-- AI Review Report Dialog -->
      <el-dialog
        v-model="showAIReportDialog"
        title="AI审核报告"
        width="700px"
      >
        <div v-if="currentAIReport" class="ai-report-content">
          <div class="report-header mb-lg">
            <div class="main-score">
              <span class="score-number">{{ currentAIReport.overallScore }}</span>
              <span class="score-unit caption">/100</span>
            </div>
            <span class="badge-lg" :class="getRiskClass(currentAIReport.riskLevel)">
              {{ getRiskText(currentAIReport.riskLevel) }}风险
            </span>
          </div>

          <div class="score-details mb-lg">
            <div class="score-item">
              <span class="item-label caption">创新性</span>
              <div class="score-bar">
                <div class="score-bar-fill" :style="{ width: (currentAIReport.innovationScore / 25 * 100) + '%' }"></div>
              </div>
              <span class="item-value body-text">{{ currentAIReport.innovationScore || 0 }}</span>
            </div>
            <div class="score-item">
              <span class="item-label caption">实用性</span>
              <div class="score-bar">
                <div class="score-bar-fill" :style="{ width: (currentAIReport.practicalityScore / 25 * 100) + '%' }"></div>
              </div>
              <span class="item-value body-text">{{ currentAIReport.practicalityScore || 0 }}</span>
            </div>
            <div class="score-item">
              <span class="item-label caption">用户体验</span>
              <div class="score-bar">
                <div class="score-bar-fill" :style="{ width: (currentAIReport.userExperienceScore / 25 * 100) + '%' }"></div>
              </div>
              <span class="item-value body-text">{{ currentAIReport.userExperienceScore || 0 }}</span>
            </div>
            <div class="score-item">
              <span class="item-label caption">文档质量</span>
              <div class="score-bar">
                <div class="score-bar-fill" :style="{ width: (currentAIReport.documentationScore / 25 * 100) + '%' }"></div>
              </div>
              <span class="item-value body-text">{{ currentAIReport.documentationScore || 0 }}</span>
            </div>
          </div>

          <div v-if="currentAIReport.codeQualityMetrics" class="code-quality mb-lg">
            <h4 class="section-title mb-md">代码质量指标</h4>
            <div class="quality-metrics">
              <div class="metric-item">
                <span class="metric-label caption">代码重复率</span>
                <span :class="getDuplicateClass(currentAIReport.codeQualityMetrics.duplicateRate)" class="metric-value">
                  {{ currentAIReport.codeQualityMetrics.duplicateRate }}%
                </span>
              </div>
              <div class="metric-item">
                <span class="metric-label caption">代码复杂度</span>
                <span class="metric-value body-text">{{ currentAIReport.codeQualityMetrics.complexity || '中等' }}</span>
              </div>
            </div>
          </div>

          <div class="review-summary-section mb-lg">
            <h4 class="section-title mb-md">评审摘要</h4>
            <p class="caption">{{ currentAIReport.reviewSummary || '暂无摘要' }}</p>
          </div>

          <div v-if="currentAIReport.improvementSuggestions && currentAIReport.improvementSuggestions.length > 0" class="suggestions-section">
            <h4 class="section-title mb-md">改进建议</h4>
            <ul class="suggestions-list caption">
              <li v-for="(suggestion, index) in currentAIReport.improvementSuggestions" :key="index">
                {{ suggestion }}
              </li>
            </ul>
          </div>
        </div>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="showAIReportDialog = false">关闭</el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { get, post, put, del, uploadFile } from '@/utils/api'
import { showSuccess, showError, showWarning, showConfirm, showInfo } from '@/utils/messageUtils'
import { formatDateTime } from '@/utils/dateUtils'

const router = useRouter()

const works = ref([])
const teams = ref([])
const workAttachments = ref({})
const loading = ref(false)
const creating = ref(false)
const updating = ref(false)
const uploading = ref(false)
const timeStatusMap = ref({})

const showCreateWorkDialog = ref(false)
const showEditWorkDialog = ref(false)
const showUploadFileDialog = ref(false)
const showAIReportDialog = ref(false)

const currentAIReport = ref(null)
const pollingIntervals = ref({})

const createWorkForm = ref({
  workName: '',
  teamId: '',
  competitionId: 1,
  trackId: '',
  description: ''
})

const editWorkForm = ref({
  id: '',
  workName: '',
  description: '',
  innovationPoints: '',
  keyFeatures: '',
  techStack: '',
  divisionOfLabor: '',
  targetUsers: '',
  workType: ''
})

const uploadForm = ref({
  workId: '',
  file: null,
  attachmentType: 'SOURCE'
})

onMounted(async () => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('userRole')

  if (!token || role !== 'STUDENT') {
    showWarning('您没有权限访问此页面')
    router.push('/login')
    return
  }

  await Promise.all([
    fetchWorks(),
    fetchTeams()
  ])

  for (const work of works.value) {
    await fetchWorkAttachments(work.id)
  }

  await fetchAllTimeStatuses()
})

const fetchAllTimeStatuses = async () => {
  const promises = works.value.map(work =>
    get(`/competitions/${work.competitionId}/time-status`)
      .then(status => {
        timeStatusMap.value[work.competitionId] = status
      })
      .catch(error => {
        console.error(`Failed to fetch time status for competition ${work.competitionId}`, error)
      })
  )
  await Promise.all(promises)
}

const fetchWorks = async () => {
  loading.value = true
  try {
    const data = await get('/works/my')
    works.value = data || []

    for (const work of works.value) {
      if (work.developmentStatus === 'SUBMITTED') {
        try {
          const statusData = await get(`/ai-reviews/status/${work.id}`)
          work.aiReviewStatus = statusData
          if (statusData?.status === 'VALIDATING') {
            startPolling(work.id)
          }
        } catch (error) {
          console.error('获取AI审核状态失败', error)
        }
      }
    }
  } catch (error) {
    showError('获取作品列表失败')
  } finally {
    loading.value = false
  }
}

const fetchTeams = async () => {
  try {
    const data = await get('/teams/my')
    teams.value = data || []
  } catch (error) {
    console.error('获取团队列表失败', error)
  }
}

const fetchWorkAttachments = async (workId) => {
  try {
    const data = await get(`/upload/work/${workId}`)
    workAttachments.value[workId] = data || []
  } catch (error) {
    console.error('获取附件列表失败', error)
  }
}

const getWorkAttachments = (workId) => {
  return workAttachments.value[workId] || []
}

const showCreateDialog = () => {
  createWorkForm.value = {
    workName: '',
    teamId: '',
    competitionId: 1,
    trackId: '',
    description: ''
  }
  showCreateWorkDialog.value = true
}

const handleTeamChange = async (teamId) => {
  const team = teams.value.find(t => t.id === teamId)
  if (team) {
    createWorkForm.value.trackId = team.competitionTrackId

    try {
      const registrations = await get(`/teams/${teamId}/registrations`)
      if (registrations && registrations.length > 0) {
        const approvedReg = registrations.find(r => r.status === 'APPROVED')
        if (approvedReg) {
          createWorkForm.value.competitionId = approvedReg.competitionId
        } else {
          createWorkForm.value.competitionId = registrations[0].competitionId
        }
      }
    } catch (error) {
      console.error('获取团队报名记录失败', error)
    }
  }
}

const handleCreateWork = async () => {
  if (!createWorkForm.value.workName) {
    showWarning('请输入作品名称')
    return
  }
  if (!createWorkForm.value.teamId) {
    showWarning('请选择团队')
    return
  }

  creating.value = true
  try {
    const queryParams = new URLSearchParams({
      workName: createWorkForm.value.workName,
      teamId: createWorkForm.value.teamId,
      competitionId: createWorkForm.value.competitionId,
      trackId: createWorkForm.value.trackId
    }).toString()

    await post(`/works?${queryParams}`)

    showSuccess('作品创建成功')
    showCreateWorkDialog.value = false
    await fetchWorks()
  } catch (error) {
    showError('创建作品失败')
  } finally {
    creating.value = false
  }
}

const editWork = (work) => {
  editWorkForm.value = {
    id: work.id,
    workName: work.workName,
    description: work.description || '',
    innovationPoints: work.innovationPoints || '',
    keyFeatures: work.keyFeatures || '',
    techStack: work.techStack || '',
    divisionOfLabor: work.divisionOfLabor || '',
    targetUsers: work.targetUsers || '',
    workType: work.workType
  }
  showEditWorkDialog.value = true
}

const handleUpdateWork = async () => {
  if (!editWorkForm.value.workName) {
    showWarning('请输入作品名称')
    return
  }

  updating.value = true
  try {
    const params = new URLSearchParams({
      workName: editWorkForm.value.workName,
      description: editWorkForm.value.description,
      innovationPoints: editWorkForm.value.innovationPoints,
      keyFeatures: editWorkForm.value.keyFeatures,
      techStack: editWorkForm.value.techStack,
      divisionOfLabor: editWorkForm.value.divisionOfLabor,
      targetUsers: editWorkForm.value.targetUsers
    })

    await put(`/works/${editWorkForm.value.id}?${params.toString()}`)
    showSuccess('作品信息已更新')
    showEditWorkDialog.value = false
    await fetchWorks()
  } catch (error) {
    showError('更新作品失败')
  } finally {
    updating.value = false
  }
}

const showUploadDialog = (work) => {
  uploadForm.value = {
    workId: work.id,
    file: null,
    attachmentType: 'SOURCE'
  }
  showUploadFileDialog.value = true
}

const handleFileSelect = (event) => {
  const file = event.target.files[0]
  if (file) {
    uploadForm.value.file = file
  }
}

const handleUploadFile = async () => {
  if (!uploadForm.value.file) {
    showWarning('请选择文件')
    return
  }

  uploading.value = true
  try {
    await uploadFile(
      `/upload/work/${uploadForm.value.workId}`,
      uploadForm.value.file,
      { attachmentType: uploadForm.value.attachmentType }
    )

    showSuccess('文件上传成功')
    showUploadFileDialog.value = false
    await fetchWorkAttachments(uploadForm.value.workId)
  } catch (error) {
    showError('上传失败')
  } finally {
    uploading.value = false
  }
}

const deleteAttachment = async (attachment) => {
  try {
    await showConfirm(`确定要删除文件 ${attachment.fileName} 吗？`, '删除文件')

    await del(`/upload/${attachment.id}`)
    showSuccess('文件已删除')
    await fetchWorkAttachments(attachment.workId)
  } catch (error) {
    if (error !== 'cancel') {
      showError('删除失败')
    }
  }
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

const markCompleted = async (work) => {
  try {
    await showConfirm('确定要将作品标记为已完成吗？标记后将可以提交评审。', '标记完成')

    await post(`/works/${work.id}/complete`)
    showSuccess('作品已标记为完成')
    await fetchWorks()
  } catch (error) {
    if (error !== 'cancel') {
      showError('标记完成失败')
    }
  }
}

const submitWork = async (work) => {
  console.log('submitWork called for work:', work.id, 'status:', work.developmentStatus)

  const timeStatus = timeStatusMap.value[work.competitionId]
  if (timeStatus && !timeStatus.canSubmit) {
    const endTime = formatDateTime(timeStatus.submissionEnd)
    showWarning(`作品提交已截止，截止时间：${endTime}`)
    return
  }

  try {
    const isResubmit = work.developmentStatus === 'SUBMITTED'
    const message = isResubmit
      ? '重新提交作品将更新评审信息，之前的评审任务将被取消。\n\n确定要重新提交吗？'
      : '提交作品后将进入评审流程，无法撤回。\n\n确定要提交吗？'

    console.log('Showing confirm dialog with message:', message)

    // 使用 ElMessageBox.confirm 直接调用，确保对话框显示
    await ElMessageBox.confirm(
      message,
      isResubmit ? '重新提交作品' : '提交作品',
      {
        confirmButtonText: '确定提交',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      }
    )

    console.log('User confirmed submission, calling API...')
    showInfo('正在提交作品...')

    await post(`/works/${work.id}/submit`)
    console.log('API call successful')

    showSuccess(isResubmit ? '✅ 作品已重新提交，AI正在审核中' : '✅ 作品已提交，AI正在审核中')
    await fetchWorks()
  } catch (error) {
    console.error('submitWork error:', error)
    if (error === 'cancel') {
      showInfo('已取消提交')
    } else {
      showError(`提交失败：${error.message || '未知错误'}`)
    }
  }
}

const getStatusClass = (status) => {
  return {
    'badge-primary': status === 'SUBMITTED' || status === 'AWARDED',
    'badge-warning': status === 'IN_PROGRESS',
    'badge-gray': status === 'COMPLETED'
  }
}

const getStatusText = (status) => {
  const texts = {
    IN_PROGRESS: '开发中',
    COMPLETED: '已完成',
    SUBMITTED: '已提交',
    AWARDED: '已获奖'
  }
  return texts[status] || status
}

const getWorkTypeText = (type) => {
  const texts = {
    CODE: '程序设计',
    PPT: '演示文稿',
    VIDEO: '数媒动漫'
  }
  return texts[type] || type
}

const formatDate = (dateStr) => {
  return formatDateTime(dateStr)
}

const getSubmissionCountdown = (work) => {
  const timeStatus = timeStatusMap.value[work.competitionId]
  if (!timeStatus) return ''

  if (timeStatus.currentPhase === 'SUBMISSION' && timeStatus.submissionDaysRemaining) {
    return `提交剩余 ${timeStatus.submissionDaysRemaining} 天`
  }
  return ''
}

const isSubmissionClosed = (work) => {
  const timeStatus = timeStatusMap.value[work.competitionId]
  if (!timeStatus) return false
  return timeStatus.currentPhase === 'FINISHED' ||
         (timeStatus.currentPhase !== 'BEFORE_SUBMISSION' &&
          timeStatus.currentPhase !== 'SUBMISSION' &&
          !timeStatus.canSubmit)
}

const isSubmissionNotStarted = (work) => {
  const timeStatus = timeStatusMap.value[work.competitionId]
  if (!timeStatus) return false
  return timeStatus.currentPhase === 'BEFORE_SUBMISSION'
}

const getSubmissionStartTime = (work) => {
  const timeStatus = timeStatusMap.value[work.competitionId]
  if (!timeStatus) return ''
  return formatDateTime(timeStatus.submissionStart)
}

const getAIStatusClass = (status) => {
  return {
    'badge-warning': status === 'VALIDATING',
    'badge-primary': status === 'VALID',
    'badge-danger': status === 'INVALID'
  }
}

const getAIStatusText = (status) => {
  const texts = {
    VALIDATING: 'AI审核中',
    VALID: '审核通过',
    INVALID: '审核未通过'
  }
  return texts[status] || status
}

const getRiskClass = (level) => {
  return {
    'badge-primary': level === 'LOW',
    'badge-warning': level === 'MEDIUM',
    'badge-danger': level === 'HIGH'
  }
}

const getRiskText = (level) => {
  const texts = {
    LOW: '低',
    MEDIUM: '中等',
    HIGH: '高'
  }
  return texts[level] || level
}

const getDuplicateClass = (rate) => {
  if (rate > 30) return 'metric-danger'
  if (rate > 20) return 'metric-warning'
  return 'metric-good'
}

const startPolling = (workId) => {
  const token = localStorage.getItem('token')
  if (!token) {
    console.warn('User not logged in, skipping polling')
    return
  }

  if (!workId || workId <= 0) {
    console.warn('Invalid workId, skipping polling')
    return
  }

  if (pollingIntervals.value[workId]) return

  const pollInterval = setInterval(async () => {
    try {
      const statusData = await get(`/ai-reviews/status/${workId}`)

      const work = works.value.find(w => w.id === workId)
      if (work) {
        work.aiReviewStatus = statusData
      }

      if (statusData?.status !== 'VALIDATING') {
        clearInterval(pollInterval)
        pollingIntervals.value[workId] = null

        if (statusData?.status === 'VALID') {
          showSuccess('AI审核已完成，作品审核通过！')
        } else if (statusData?.status === 'INVALID') {
          showWarning('AI审核已完成，作品审核未通过，请查看报告。')
        }
      }
    } catch (error) {
      console.error('轮询AI审核状态失败', error)
      if (error.message && error.message.includes('403')) {
        clearInterval(pollInterval)
        pollingIntervals.value[workId] = null
      }
    }
  }, 5000)

  pollingIntervals.value[workId] = pollInterval

  setTimeout(() => {
    if (pollingIntervals.value[workId]) {
      clearInterval(pollingIntervals.value[workId])
      pollingIntervals.value[workId] = null
    }
  }, 600000)
}

const refreshAIStatus = async (work) => {
  try {
    showInfo('正在刷新AI审核状态...')
    const statusData = await get(`/ai-reviews/status/${work.id}`)
    work.aiReviewStatus = statusData

    // 如果状态不再是VALIDATING，停止轮询
    if (statusData?.status !== 'VALIDATING') {
      if (pollingIntervals.value[work.id]) {
        clearInterval(pollingIntervals.value[work.id])
        pollingIntervals.value[work.id] = null
      }

      if (statusData?.status === 'VALID') {
        showSuccess('AI审核已完成，作品审核通过！')
      } else if (statusData?.status === 'INVALID') {
        showWarning('AI审核已完成，作品审核未通过。')
      }
    } else {
      showInfo('AI审核仍在进行中...')
    }
  } catch (error) {
    showError('刷新状态失败')
  }
}

const viewAIReport = async (work) => {
  try {
    const submissionData = await get(`/submissions/work/${work.id}`)
    if (!submissionData || !submissionData.id) {
      showError('作品尚未提交，无法查看审核报告')
      return
    }

    const detailDTO = await get(`/ai-reviews/detail/${submissionData.id}`)
    currentAIReport.value = detailDTO
    showAIReportDialog.value = true
  } catch (error) {
    showError('获取AI审核报告失败')
  }
}
</script>

<style scoped>
.works-page {
  min-height: 100vh;
}

.page-header {
  margin-bottom: var(--spacing-2xl);
}

.action-bar {
  animation-delay: 0.1s;
}

.works-section {
  animation-delay: 0.2s;
}

.loading-state {
  padding: var(--spacing-3xl) 0;
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

.work-card {
  padding: var(--spacing-xl);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

.countdown-section {
  padding: var(--spacing-sm) var(--spacing-md);
  background: rgba(90, 127, 168, 0.1);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
}

.countdown-text {
  font-weight: 600;
  font-size: 14px;
}

.deadline-warning {
  padding: var(--spacing-sm) var(--spacing-md);
  background: rgba(231, 76, 60, 0.1);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
}

.warning-text {
  color: #e74c3c;
  font-weight: 600;
  font-size: 14px;
}

.work-info-grid {
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

.work-description {
  color: var(--color-text-secondary);
  padding: var(--spacing-md);
  background: var(--color-bg-secondary);
  border-radius: var(--radius-md);
}

.empty-state {
  padding: var(--spacing-3xl);
  text-align: center;
}

.empty-icon {
  margin-bottom: var(--spacing-lg);
  opacity: 0.5;
}

.work-attachments {
  background: var(--color-bg-secondary);
  padding: var(--spacing-lg);
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
  padding: var(--spacing-sm);
  background: var(--color-bg);
  border-radius: var(--radius-sm);
}

.attachment-name {
  flex: 1;
}

.attachment-size {
  color: var(--color-text-secondary);
}

.btn-text {
  padding: var(--spacing-sm) var(--spacing-md);
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 14px;
  transition: color var(--transition-fast);
}

.btn-delete {
  color: #e74c3c;
}

.btn-delete:hover {
  color: #c0392b;
}

.file-input {
  width: 100%;
  padding: var(--spacing-md);
  border: 2px dashed var(--color-border);
  border-radius: var(--radius-md);
  cursor: pointer;
}

.selected-file {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-md);
  background: var(--color-bg-secondary);
  border-radius: var(--radius-sm);
  margin-top: var(--spacing-md);
}

.file-size {
  color: var(--color-text-secondary);
}

@media (max-width: 768px) {
  .work-info-grid {
    grid-template-columns: 1fr;
  }

  .attachment-item {
    flex-wrap: wrap;
  }
}

/* AI Review Status Styles */

.ai-review-status {
  padding: var(--spacing-md);
  background: var(--color-bg-secondary);
  border-radius: var(--radius-md);
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md);
}

.status-label {
  color: var(--color-text-secondary);
}

.review-progress {
  margin-top: var(--spacing-md);
}

.progress-bar {
  height: 8px;
  background: var(--color-border-light);
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: var(--color-accent);
  width: 60%;
  animation: progress-animation 2s ease-in-out infinite;
}

@keyframes progress-animation {
  0% { width: 30%; }
  50% { width: 70%; }
  100% { width: 30%; }
}

.progress-text {
  display: block;
  margin-top: var(--spacing-sm);
  color: var(--color-text-secondary);
}

.review-result {
  margin-top: var(--spacing-md);
}

.score-display {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.score-value {
  font-size: var(--text-2xl);
  font-weight: 700;
}

.score-pass {
  color: var(--status-completed);
}

.score-fail {
  color: var(--status-danger);
}

.risk-info {
  margin-top: var(--spacing-sm);
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.badge-danger {
  background: var(--status-danger);
  color: white;
}

.badge-lg {
  padding: var(--spacing-sm) var(--spacing-md);
  font-size: var(--text-sm);
}

/* AI Review Report Dialog Styles */

.ai-report-content {
  padding: var(--spacing-md);
}

.report-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-lg);
  background: var(--color-bg-secondary);
  border-radius: var(--radius-md);
}

.main-score {
  display: flex;
  align-items: baseline;
}

.score-number {
  font-size: 48px;
  font-weight: 700;
  color: var(--color-accent);
}

.score-unit {
  font-size: var(--text-lg);
  color: var(--color-text-secondary);
  margin-left: var(--spacing-xs);
}

.score-details {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.score-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.item-label {
  width: 80px;
  color: var(--color-text-secondary);
}

.score-bar {
  flex: 1;
  height: 12px;
  background: var(--color-border-light);
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.score-bar-fill {
  height: 100%;
  background: var(--color-accent);
  border-radius: var(--radius-sm);
  transition: width 0.3s ease;
}

.item-value {
  width: 50px;
  text-align: right;
  font-weight: 600;
}

.code-quality {
  padding: var(--spacing-md);
  background: var(--color-bg-secondary);
  border-radius: var(--radius-md);
}

.quality-metrics {
  display: flex;
  gap: var(--spacing-xl);
}

.metric-item {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.metric-label {
  color: var(--color-text-secondary);
}

.metric-value {
  font-weight: 600;
  font-size: var(--text-lg);
}

.metric-good {
  color: var(--status-completed);
}

.metric-warning {
  color: var(--status-pending);
}

.metric-danger {
  color: var(--status-danger);
}

.review-summary-section,
.suggestions-section {
  padding: var(--spacing-md);
  background: var(--color-bg-secondary);
  border-radius: var(--radius-md);
}

.section-title {
  font-family: var(--font-display);
  font-size: var(--text-base);
  font-weight: 600;
  color: var(--color-primary);
}

.suggestions-list {
  list-style: disc;
  padding-left: var(--spacing-lg);
}
</style>