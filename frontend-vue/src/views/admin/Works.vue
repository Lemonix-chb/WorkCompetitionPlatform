<template>
  <div class="works-page">
    <div class="container">
      <!-- Page Header -->
      <div class="page-header slide-in">
        <div class="header-content flex-between">
          <div>
            <h1 class="page-title">作品管理</h1>
            <p class="body-text">查看和管理所有提交的作品</p>
          </div>
          <div class="header-actions flex gap-md">
            <button class="btn-secondary" @click="showBatchAssignDialog">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M10 2L18 6V14L10 18L2 14V6L10 2Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                <path d="M10 6V10M6 8L10 10L14 8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              批量分配评委
            </button>
            <button class="btn-primary" @click="autoAssign">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <circle cx="10" cy="10" r="7" stroke="currentColor" stroke-width="2"/>
                <path d="M10 7V13M7 10H13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              自动分配
            </button>
          </div>
        </div>
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

          <el-select v-model="filters.status" placeholder="作品状态" clearable style="width: 150px">
            <el-option label="开发中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已提交" value="SUBMITTED" />
            <el-option label="已获奖" value="AWARDED" />
          </el-select>

          <button class="btn-secondary" @click="fetchWorks">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <circle cx="10" cy="10" r="8" stroke="currentColor" stroke-width="2"/>
              <path d="M10 6V10L13 13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            搜索
          </button>
        </div>
      </div>

      <!-- Works List -->
      <div class="works-section scale-in">
        <!-- Loading -->
        <div v-if="loading" class="loading-state card">
          <div class="spinner"></div>
        </div>

        <!-- Data Table -->
        <div v-else-if="works.length > 0" class="data-table card">
          <el-table :data="works" style="width: 100%" stripe @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" :selectable="isSelectable" />
            <el-table-column prop="workCode" label="作品编号" width="150" />
            <el-table-column label="作品名称" min-width="200">
              <template #default="{ row }">
                {{ row.workName || '未命名作品' }}
              </template>
            </el-table-column>
            <el-table-column label="团队" width="150">
              <template #default="{ row }">
                {{ getTeamName(row.teamId) }}
              </template>
            </el-table-column>
            <el-table-column label="赛道" width="150">
              <template #default="{ row }">
                {{ getTrackName(row.trackId) }}
              </template>
            </el-table-column>
            <el-table-column label="类型" width="120">
              <template #default="{ row }">
                {{ getWorkTypeText(row.workType) }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <span :class="getWorkStatusClass(row.developmentStatus)" class="badge">
                  {{ getWorkStatusText(row.developmentStatus) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="创建时间" width="160">
              <template #default="{ row }">
                {{ formatDateTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="280" fixed="right">
              <template #default="{ row }">
                <div class="action-buttons flex gap-xs">
                  <button class="btn-action-view" @click="viewDetail(row)">
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                      <circle cx="8" cy="6" r="4" stroke="currentColor" stroke-width="1.5"/>
                      <path d="M3 14C3 10 5.5 8 8 8C10.5 8 13 10 13 14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    </svg>
                    详情
                  </button>
                  <button class="btn-action-ai" @click="performAIReview(row)" :disabled="row.developmentStatus !== 'SUBMITTED'">
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                      <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.5"/>
                      <path d="M8 5V11M5 8H11" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    </svg>
                    AI初审
                  </button>
                  <button class="btn-action-review" @click="assignReview(row)" :disabled="row.developmentStatus !== 'SUBMITTED'">
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                      <path d="M8 2L14 8L8 14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    分配评委
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
              <rect x="8" y="8" width="32" height="28" rx="3" stroke="#bdc3c7" stroke-width="2"/>
              <path d="M12 16H36M12 20H36M12 24H28" stroke="#bdc3c7" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <h3 class="card-title mb-md">暂无作品提交</h3>
          <p class="caption">等待团队提交作品</p>
        </div>
      </div>

      <!-- Detail Dialog -->
      <el-dialog v-model="showDetailDialog" title="作品详情" width="700px">
        <div v-if="selectedWork" class="detail-content">
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label caption">作品编号</span>
              <span class="detail-value body-text">{{ selectedWork.workCode }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label caption">作品名称</span>
              <span class="detail-value body-text">{{ selectedWork.workName }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label caption">团队</span>
              <span class="detail-value body-text">{{ getTeamName(selectedWork.teamId) }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label caption">赛道</span>
              <span class="detail-value body-text">{{ getTrackName(selectedWork.trackId) }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label caption">作品类型</span>
              <span class="detail-value body-text">{{ getWorkTypeText(selectedWork.workType) }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label caption">状态</span>
              <span :class="getWorkStatusClass(selectedWork.developmentStatus)" class="badge">
                {{ getWorkStatusText(selectedWork.developmentStatus) }}
              </span>
            </div>
          </div>

          <div v-if="selectedWork.description" class="detail-section mt-lg">
            <h4 class="section-title mb-md">作品简介</h4>
            <p class="body-text">{{ selectedWork.description }}</p>
          </div>

          <div v-if="selectedWork.innovationPoints" class="detail-section mt-lg">
            <h4 class="section-title mb-md">创新点说明</h4>
            <p class="body-text">{{ selectedWork.innovationPoints }}</p>
          </div>

          <div v-if="selectedWork.keyFeatures" class="detail-section mt-lg">
            <h4 class="section-title mb-md">关键功能特性</h4>
            <p class="body-text">{{ selectedWork.keyFeatures }}</p>
          </div>

          <div v-if="selectedWork.techStack" class="detail-section mt-lg">
            <h4 class="section-title mb-md">技术栈</h4>
            <p class="body-text">{{ selectedWork.techStack }}</p>
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
                <button class="btn-download" @click="downloadAttachment(attachment)">
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

      <!-- Assign Review Dialog -->
      <el-dialog v-model="showAssignDialog" title="分配评委评审" width="500px">
        <el-form :model="assignForm" label-width="100px">
          <el-form-item label="选择评委">
            <el-select v-model="assignForm.judgeUserId" placeholder="请选择评委" style="width: 100%">
              <el-option v-for="judge in judges" :key="judge.id" :label="judge.realName" :value="judge.id" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="showAssignDialog = false">取消</el-button>
            <el-button type="primary" @click="submitAssign" :loading="submitting">分配</el-button>
          </div>
        </template>
      </el-dialog>

      <!-- Batch Assign Dialog -->
      <el-dialog v-model="showBatchAssignDialogFlag" title="批量分配评委" width="800px">
        <div class="batch-assign-content">
          <div class="batch-info mb-lg">
            <p class="body-text">已选择 <strong>{{ selectedWorks.length }}</strong> 个作品进行批量分配</p>
          </div>

          <div class="assign-step mb-xl">
            <h4 class="section-title mb-md">选择评委</h4>
            <el-select v-model="selectedJudgeForBatch" placeholder="请选择评委" style="width: 100%">
              <el-option v-for="judge in judges" :key="judge.id" :label="judge.realName" :value="judge.id">
                <div class="judge-option">
                  <span>{{ judge.realName }}</span>
                  <span class="judge-info caption">{{ judge.college || '未知学院' }}</span>
                </div>
              </el-option>
            </el-select>
          </div>
        </div>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="showBatchAssignDialogFlag = false">取消</el-button>
            <el-button type="primary" @click="submitBatchAssign" :loading="submitting" :disabled="selectedWorks.length === 0 || !selectedJudgeForBatch">
              批量分配
            </el-button>
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

const works = ref([])
const competitions = ref([])
const allTracks = ref([])
const teams = ref([])
const judges = ref([])
const loading = ref(false)
const showDetailDialog = ref(false)
const showAssignDialog = ref(false)
const showBatchAssignDialogFlag = ref(false)
const selectedWork = ref(null)
const submitting = ref(false)
const selectedWorks = ref([])
const selectedJudgeForBatch = ref(null)
const currentWorkAttachments = ref([])

const filters = reactive({
  competitionId: null,
  trackId: null,
  status: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const assignForm = reactive({
  workId: null,
  judgeUserId: null
})

const filteredTracks = computed(() => {
  if (!filters.competitionId) return []
  return allTracks.value.filter(track => track.competitionId === filters.competitionId)
})

onMounted(async () => {
  await Promise.all([
    fetchCompetitions(),
    fetchTeams(),
    fetchJudges(),
    fetchWorks()
  ])
})

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
    ElMessage.error('获取团队列表失败')
  }
}

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

const fetchJudges = async () => {
  try {
    const token = localStorage.getItem('token')
    const params = new URLSearchParams({ role: 'JUDGE', size: 100 })
    const response = await fetch(`/api/users?${params.toString()}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const data = await response.json()
    if (data.code === 200) {
      judges.value = data.data.records || []
    }
  } catch (error) {
    ElMessage.error('获取评委列表失败')
  }
}

const fetchWorks = async () => {
  loading.value = true

  try {
    const token = localStorage.getItem('token')

    // 如果选择了赛事，则按赛事查询；否则查询所有作品
    let url = filters.competitionId
      ? `/api/works/competition/${filters.competitionId}`
      : `/api/works/all`

    const response = await fetch(url, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const data = await response.json()

    if (data.code === 200) {
      let result = data.data || []

      // 如果选择了赛道，则按赛道过滤
      if (filters.trackId) {
        result = result.filter(w => w.trackId === filters.trackId)
      }

      // 如果选择了状态，则按状态过滤
      if (filters.status) {
        result = result.filter(w => w.developmentStatus === filters.status)
      }

      works.value = result
      pagination.total = result.length
    } else {
      ElMessage.error(data.message || '获取作品列表失败')
    }
  } catch (error) {
    ElMessage.error('获取作品列表失败')
  } finally {
    loading.value = false
  }
}

const viewDetail = async (work) => {
  selectedWork.value = work
  showDetailDialog.value = true
  await fetchWorkAttachments(work.id)
}

const fetchWorkAttachments = async (workId) => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch(`/api/upload/work/${workId}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const data = await response.json()
    if (data.code === 200) {
      currentWorkAttachments.value = data.data || []
    }
  } catch (error) {
    console.error('获取附件列表失败', error)
  }
}

const downloadAttachment = async (attachment) => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch(`/api/upload/download/${attachment.id}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })

    if (!response.ok) {
      ElMessage.error('下载失败')
      return
    }

    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = attachment.fileName
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)

    ElMessage.success('下载成功')
  } catch (error) {
    ElMessage.error('下载失败')
  }
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

const getAttachmentTypeText = (attachmentType) => {
  const texts = {
    SOURCE_CODE: '源代码',
    DOCUMENT: '文档',
    PRESENTATION: '演示文稿',
    VIDEO: '视频',
    OTHER: '其他'
  }
  return texts[attachmentType] || attachmentType
}

const performAIReview = async (work) => {
  try {
    await ElMessageBox.confirm(
      `确定要对作品 "${work.workName}" 执行AI初审吗？`,
      'AI初审',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    submitting.value = true
    const token = localStorage.getItem('token')
    // TODO: 需要先获取作品对应的submission记录
    const response = await fetch(`/api/reviews/ai/${work.id}`, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${token}` }
    })

    const data = await response.json()
    if (data.code === 200) {
      ElMessage.success('AI初审已执行')
      await fetchWorks()
    } else {
      ElMessage.error(data.message || 'AI初审失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('AI初审失败')
    }
  } finally {
    submitting.value = false
  }
}

const assignReview = (work) => {
  assignForm.workId = work.id
  assignForm.judgeUserId = null
  showAssignDialog.value = true
}

const submitAssign = async () => {
  if (!assignForm.judgeUserId) {
    ElMessage.warning('请选择评委')
    return
  }

  submitting.value = true

  try {
    const token = localStorage.getItem('token')
    const response = await fetch('/api/reviews/assign', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      body: new URLSearchParams({
        workId: assignForm.workId,
        judgeUserId: assignForm.judgeUserId
      })
    })

    const data = await response.json()
    if (data.code === 200) {
      ElMessage.success('评审任务已分配')
      showAssignDialog.value = false
      await fetchWorks()
    } else {
      ElMessage.error(data.message || '分配失败')
    }
  } catch (error) {
    ElMessage.error('分配失败')
  } finally {
    submitting.value = false
  }
}

const handleSizeChange = (val) => {
  pagination.size = val
  fetchWorks()
}

const handleCurrentChange = (val) => {
  pagination.current = val
  fetchWorks()
}

const getTeamName = (teamId) => {
  const team = teams.value.find(t => t.id === teamId)
  return team ? team.teamName : '未知团队'
}

const getTrackName = (trackId) => {
  const track = allTracks.value.find(t => t.id === trackId)
  return track ? track.trackName : '未知赛道'
}

const getWorkTypeText = (workType) => {
  const texts = {
    CODE: '程序设计',
    PPT: '演示文稿',
    VIDEO: '数媒动漫'
  }
  return texts[workType] || workType
}

const getWorkStatusClass = (status) => {
  return {
    'badge-warning': status === 'IN_PROGRESS',
    'badge-success': status === 'COMPLETED',
    'badge-primary': status === 'SUBMITTED',
    'badge-gold': status === 'AWARDED'
  }
}

const getWorkStatusText = (status) => {
  const texts = {
    IN_PROGRESS: '开发中',
    COMPLETED: '已完成',
    SUBMITTED: '已提交',
    AWARDED: '已获奖'
  }
  return texts[status] || status
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

const handleSelectionChange = (selection) => {
  selectedWorks.value = selection.map(work => work.id)
}

const isSelectable = (row) => {
  return row.developmentStatus === 'SUBMITTED'
}

const showBatchAssignDialog = () => {
  if (selectedWorks.value.length === 0) {
    ElMessage.warning('请先选择要分配评委的作品')
    return
  }
  selectedJudgeForBatch.value = null
  showBatchAssignDialogFlag.value = true
}

const submitBatchAssign = async () => {
  if (selectedWorks.value.length === 0) {
    ElMessage.warning('请选择至少一个作品')
    return
  }

  if (!selectedJudgeForBatch.value) {
    ElMessage.warning('请选择评委')
    return
  }

  submitting.value = true

  try {
    const token = localStorage.getItem('token')
    let successCount = 0
    let failCount = 0

    for (const workId of selectedWorks.value) {
      try {
        const response = await fetch('/api/reviews/assign', {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/x-www-form-urlencoded'
          },
          body: new URLSearchParams({
            workId: workId,
            judgeUserId: selectedJudgeForBatch.value
          })
        })

        const data = await response.json()
        if (data.code === 200) {
          successCount++
        } else {
          failCount++
        }
      } catch (error) {
        failCount++
      }
    }

    if (successCount > 0) {
      ElMessage.success(`成功分配 ${successCount} 个作品给评委`)
      showBatchAssignDialogFlag.value = false
      selectedWorks.value = []
      await fetchWorks()
    }

    if (failCount > 0) {
      ElMessage.warning(`${failCount} 个作品分配失败`)
    }
  } catch (error) {
    ElMessage.error('批量分配失败')
  } finally {
    submitting.value = false
  }
}

const autoAssign = async () => {
  try {
    await ElMessageBox.confirm(
      '系统将自动将所有已提交的作品平均分配给所有评委，每个作品分配2位评委。是否继续？',
      '自动分配确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    submitting.value = true
    const token = localStorage.getItem('token')

    const params = new URLSearchParams({
      competitionId: filters.competitionId || '',
      judgesPerSubmission: '2'
    })

    const response = await fetch(`/api/reviews/auto-assign?${params.toString()}`, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${token}` }
    })

    const data = await response.json()

    if (data.code === 200) {
      ElMessage.success({
        message: data.data,
        duration: 5000,
        showClose: true
      })
      await fetchWorks()
    } else {
      ElMessage.error(data.message || '自动分配失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('自动分配失败')
    }
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.works-page {
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

.action-buttons {
  display: flex;
  gap: var(--spacing-xs);
  flex-wrap: wrap;
}

.btn-action-view,
.btn-action-ai,
.btn-action-review {
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

.btn-action-view:hover {
  background: #60a5fa;
  color: var(--color-white);
  border-color: #60a5fa;
}

.btn-action-ai:hover:not(:disabled) {
  background: #10b981;
  color: var(--color-white);
  border-color: #10b981;
}

.btn-action-ai:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-action-review:hover {
  background: #8b5cf6;
  color: var(--color-white);
  border-color: #8b5cf6;
}

.badge-error {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.header-actions {
  animation-delay: 0.1s;
}

.batch-assign-content {
  padding: var(--spacing-lg);
}

.batch-info {
  padding: var(--spacing-md);
  background: var(--color-bg-secondary);
  border-radius: var(--radius-md);
}

.judge-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.judge-info {
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
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-lg);
  }

  .header-actions {
    width: 100%;
    flex-direction: column;
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
}
</style>