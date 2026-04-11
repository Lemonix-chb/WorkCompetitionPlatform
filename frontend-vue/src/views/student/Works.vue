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
              <p class="body-text">{{ work.description }}</p>
            </div>

            <!-- Attachments -->
            <div class="work-attachments mb-lg" v-if="getWorkAttachments(work.id).length > 0">
              <h4 class="section-title mb-md">已上传文件</h4>
              <div class="attachments-list">
                <div v-for="attachment in getWorkAttachments(work.id)" :key="attachment.id" class="attachment-item">
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <path d="M2 4H14M4 4V12C4 13 5 14 6 14H10C11 14 12 13 12 12V4" stroke="currentColor" stroke-width="2"/>
                  </svg>
                  <span class="attachment-name body-text">{{ attachment.fileName }}</span>
                  <span class="attachment-size caption">{{ formatFileSize(attachment.fileSize) }}</span>
                  <button class="btn-text btn-delete" @click="deleteAttachment(attachment)">删除</button>
                </div>
              </div>
            </div>

            <!-- Actions -->
            <div class="card-footer flex gap-md">
              <button class="btn-primary" @click="editWork(work)" v-if="work.developmentStatus !== 'AWARDED'">
                编辑作品
              </button>
              <button class="btn-secondary" @click="showUploadDialog(work)">上传文件</button>
              <button class="btn-secondary" @click="submitWork(work)"
                      v-if="work.developmentStatus === 'COMPLETED' || work.developmentStatus === 'SUBMITTED'">
                {{ work.developmentStatus === 'SUBMITTED' ? '重新提交评审' : '提交评审' }}
              </button>
              <button class="btn-secondary" @click="markCompleted(work)" v-if="work.developmentStatus === 'IN_PROGRESS'">
                标记完成
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
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const works = ref([])
const teams = ref([])
const workAttachments = ref({})
const loading = ref(false)
const creating = ref(false)
const updating = ref(false)
const uploading = ref(false)

const showCreateWorkDialog = ref(false)
const showEditWorkDialog = ref(false)
const showUploadFileDialog = ref(false)

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
    ElMessage.warning('您没有权限访问此页面')
    return
  }

  await Promise.all([
    fetchWorks(),
    fetchTeams()
  ])

  // 加载每个作品的附件
  for (const work of works.value) {
    await fetchWorkAttachments(work.id)
  }
})

const fetchWorks = async () => {
  loading.value = true
  try {
    const token = localStorage.getItem('token')
    const response = await fetch('/api/works/my', {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const data = await response.json()

    if (data.code === 200) {
      works.value = data.data || []
    } else {
      ElMessage.error(data.message || '获取作品列表失败')
    }
  } catch (error) {
    ElMessage.error('获取作品列表失败')
  } finally {
    loading.value = false
  }
}

const fetchTeams = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch('/api/teams/my', {
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

const fetchWorkAttachments = async (workId) => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch(`/api/upload/work/${workId}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const data = await response.json()

    if (data.code === 200) {
      workAttachments.value[workId] = data.data || []
    }
  } catch (error) {
    console.error('获取附件列表失败', error)
  }
}

const getWorkAttachments = (workId) => {
  return workAttachments.value[workId] || []
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
    ElMessage.warning('请选择文件')
    return
  }

  uploading.value = true
  try {
    const token = localStorage.getItem('token')
    const formData = new FormData()
    formData.append('file', uploadForm.value.file)
    formData.append('attachmentType', uploadForm.value.attachmentType)

    const response = await fetch(`/api/upload/work/${uploadForm.value.workId}`, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${token}` },
      body: formData
    })

    const data = await response.json()

    if (data.code === 200) {
      ElMessage.success('文件上传成功')
      showUploadFileDialog.value = false
      await fetchWorkAttachments(uploadForm.value.workId)
    } else {
      ElMessage.error(data.message || '上传失败')
    }
  } catch (error) {
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
  }
}

const deleteAttachment = async (attachment) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除文件 ${attachment.fileName} 吗？`,
      '删除文件',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const token = localStorage.getItem('token')
    const response = await fetch(`/api/upload/${attachment.id}`, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${token}` }
    })

    const data = await response.json()

    if (data.code === 200) {
      ElMessage.success('文件已删除')
      await fetchWorkAttachments(attachment.workId)
    } else {
      ElMessage.error(data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
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

const handleTeamChange = (teamId) => {
  const team = teams.value.find(t => t.id === teamId)
  if (team) {
    createWorkForm.value.trackId = team.competitionTrackId
  }
}

const handleCreateWork = async () => {
  if (!createWorkForm.value.workName) {
    ElMessage.warning('请输入作品名称')
    return
  }
  if (!createWorkForm.value.teamId) {
    ElMessage.warning('请选择团队')
    return
  }

  creating.value = true
  try {
    const token = localStorage.getItem('token')
    const params = new URLSearchParams({
      workName: createWorkForm.value.workName,
      teamId: createWorkForm.value.teamId,
      competitionId: createWorkForm.value.competitionId,
      trackId: createWorkForm.value.trackId
    })

    const response = await fetch(`/api/works?${params.toString()}`, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${token}` }
    })

    const data = await response.json()

    if (data.code === 200) {
      ElMessage.success('作品创建成功')
      showCreateWorkDialog.value = false
      await fetchWorks()
    } else {
      ElMessage.error(data.message || '创建作品失败')
    }
  } catch (error) {
    ElMessage.error('创建作品失败')
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
    ElMessage.warning('请输入作品名称')
    return
  }

  updating.value = true
  try {
    const token = localStorage.getItem('token')
    const params = new URLSearchParams({
      workName: editWorkForm.value.workName,
      description: editWorkForm.value.description,
      innovationPoints: editWorkForm.value.innovationPoints,
      keyFeatures: editWorkForm.value.keyFeatures,
      techStack: editWorkForm.value.techStack,
      divisionOfLabor: editWorkForm.value.divisionOfLabor,
      targetUsers: editWorkForm.value.targetUsers
    })

    const response = await fetch(`/api/works/${editWorkForm.value.id}?${params.toString()}`, {
      method: 'PUT',
      headers: { 'Authorization': `Bearer ${token}` }
    })

    const data = await response.json()

    if (data.code === 200) {
      ElMessage.success('作品信息已更新')
      showEditWorkDialog.value = false
      await fetchWorks()
    } else {
      ElMessage.error(data.message || '更新作品失败')
    }
  } catch (error) {
    ElMessage.error('更新作品失败')
  } finally {
    updating.value = false
  }
}

const markCompleted = async (work) => {
  try {
    await ElMessageBox.confirm(
      '确定要将作品标记为已完成吗？标记后将可以提交评审。',
      '标记完成',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    const token = localStorage.getItem('token')
    const response = await fetch(`/api/works/${work.id}/complete`, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${token}` }
    })

    const data = await response.json()

    if (data.code === 200) {
      ElMessage.success('作品已标记为完成')
      await fetchWorks()
    } else {
      ElMessage.error(data.message || '标记完成失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('标记完成失败')
    }
  }
}

const submitWork = async (work) => {
  try {
    const isResubmit = work.developmentStatus === 'SUBMITTED'
    const message = isResubmit
      ? '重新提交作品将更新评审信息，之前的评审任务将被取消。确定要重新提交吗？'
      : '提交作品后将进入评审流程，确定要提交吗？'

    await ElMessageBox.confirm(
      message,
      isResubmit ? '重新提交作品' : '提交作品',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const token = localStorage.getItem('token')
    const response = await fetch(`/api/works/${work.id}/submit`, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${token}` }
    })

    const data = await response.json()

    if (data.code === 200) {
      ElMessage.success(isResubmit ? '作品已重新提交' : '作品已提交')
      await fetchWorks()
    } else {
      ElMessage.error(data.message || '提交作品失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('提交作品失败')
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
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
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
  align-items: flex-start;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
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
</style>