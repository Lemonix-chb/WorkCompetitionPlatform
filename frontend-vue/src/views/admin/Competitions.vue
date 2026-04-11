<template>
  <div class="competitions-page">
    <div class="container">
      <!-- Page Header -->
      <div class="page-header slide-in">
        <div class="header-content flex-between">
          <div>
            <h1 class="page-title">赛事管理</h1>
            <p class="body-text">管理竞赛信息和状态</p>
          </div>
          <button class="btn-primary" @click="showCreateDialog">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M10 4V16M4 10H16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            创建赛事
          </button>
        </div>
      </div>

      <!-- Filters -->
      <div class="filters-section card card-flat scale-in mb-lg">
        <div class="filters-row flex gap-md">
          <el-input
            v-model="filters.keyword"
            placeholder="搜索赛事名称"
            clearable
            style="width: 250px"
          />

          <el-select v-model="filters.year" placeholder="年份" clearable style="width: 150px">
            <el-option :label="2024" :value="2024" />
            <el-option :label="2025" :value="2025" />
            <el-option :label="2026" :value="2026" />
            <el-option :label="2027" :value="2027" />
          </el-select>

          <el-select v-model="filters.status" placeholder="状态" clearable style="width: 150px">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="进行中" value="ONGOING" />
            <el-option label="已结束" value="FINISHED" />
          </el-select>

          <button class="btn-secondary" @click="fetchCompetitions">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <circle cx="10" cy="10" r="8" stroke="currentColor" stroke-width="2"/>
              <path d="M10 6V10L13 13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            搜索
          </button>
        </div>
      </div>

      <!-- Competitions List -->
      <div class="competitions-section scale-in">
        <!-- Loading -->
        <div v-if="loading" class="loading-state card">
          <div class="spinner"></div>
        </div>

        <!-- Data Table -->
        <div v-else-if="competitions.length > 0" class="data-table card">
          <el-table :data="competitions" style="width: 100%" stripe>
            <el-table-column prop="competitionName" label="赛事名称" min-width="200" />
            <el-table-column prop="competitionYear" label="年份" width="80" />
            <el-table-column prop="organizer" label="主办单位" min-width="150" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <span :class="getStatusClass(row.status)" class="badge">
                  {{ getStatusText(row.status) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="registrationStart" label="报名开始" width="120">
              <template #default="{ row }">
                {{ formatDate(row.registrationStart) }}
              </template>
            </el-table-column>
            <el-table-column prop="registrationEnd" label="报名截止" width="120">
              <template #default="{ row }">
                {{ formatDate(row.registrationEnd) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="280" fixed="right">
              <template #default="{ row }">
                <div class="action-buttons flex gap-sm">
                  <button class="btn-action-edit" @click="editCompetition(row)">
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                      <path d="M11.5 2.5L13.5 4.5M2 12L12 2M12 2L14 4L4 14H2V12Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    编辑
                  </button>
                  <button
                    v-if="row.status === 'DRAFT'"
                    class="btn-action-publish"
                    @click="publishCompetition(row)"
                  >
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                      <path d="M2 8L6 12L14 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    发布
                  </button>
                  <button
                    v-if="row.status === 'PUBLISHED'"
                    class="btn-action-start"
                    @click="startCompetition(row)"
                  >
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                      <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.5"/>
                      <path d="M8 5V11M5 8H11" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    </svg>
                    开始
                  </button>
                  <button
                    v-if="row.status === 'ONGOING'"
                    class="btn-action-finish"
                    @click="finishCompetition(row)"
                  >
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                      <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.5"/>
                      <path d="M5 8H11" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    </svg>
                    结束
                  </button>
                  <button class="btn-action-tracks" @click="viewTracks(row)">
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                      <rect x="2" y="2" width="12" height="3" rx="1" stroke="currentColor" stroke-width="1.5"/>
                      <rect x="2" y="7" width="12" height="3" rx="1" stroke="currentColor" stroke-width="1.5"/>
                      <rect x="2" y="12" width="12" height="2" rx="1" stroke="currentColor" stroke-width="1.5"/>
                    </svg>
                    赛道
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
          <h3 class="card-title mb-md">暂无赛事数据</h3>
          <p class="caption">点击上方按钮创建赛事</p>
        </div>
      </div>

      <!-- Create/Edit Dialog -->
      <el-dialog
        v-model="showDialog"
        :title="dialogTitle"
        width="600px"
      >
        <el-form :model="competitionForm" label-width="120px">
          <el-form-item label="赛事名称">
            <el-input v-model="competitionForm.competitionName" placeholder="请输入赛事名称" required />
          </el-form-item>

          <el-form-item label="赛事年份">
            <el-input-number v-model="competitionForm.competitionYear" :min="2020" :max="2030" />
          </el-form-item>

          <el-form-item label="主办单位">
            <el-input v-model="competitionForm.organizer" placeholder="请输入主办单位" />
          </el-form-item>

          <el-form-item label="赛事描述">
            <el-input
              v-model="competitionForm.description"
              type="textarea"
              :rows="3"
              placeholder="请输入赛事描述"
            />
          </el-form-item>

          <el-form-item label="报名时间">
            <el-date-picker
              v-model="competitionForm.registrationPeriod"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              value-format="YYYY-MM-DD HH:mm:ss"
              format="YYYY-MM-DD HH:mm"
              style="width: 100%"
              :teleported="false"
              popper-class="date-picker-popper"
            />
          </el-form-item>

          <el-form-item label="联系方式">
            <el-input v-model="competitionForm.contactPhone" placeholder="请输入联系电话" />
          </el-form-item>

          <el-form-item label="联系邮箱">
            <el-input v-model="competitionForm.contactEmail" placeholder="请输入联系邮箱" />
          </el-form-item>
        </el-form>

        <template #footer>
          <div class="dialog-footer">
            <el-button @click="showDialog = false">取消</el-button>
            <el-button type="primary" @click="handleSubmit" :loading="submitting">
              {{ isEdit ? '更新' : '创建' }}
            </el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

const competitions = ref([])
const loading = ref(false)
const showDialog = ref(false)
const dialogTitle = ref('创建赛事')
const isEdit = ref(false)
const submitting = ref(false)

const filters = reactive({
  year: null,
  status: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const competitionForm = reactive({
  id: null,
  competitionName: '',
  competitionYear: 2026,
  organizer: '',
  description: '',
  registrationPeriod: [],
  contactPhone: '',
  contactEmail: ''
})

onMounted(async () => {
  await fetchCompetitions()
})

const fetchCompetitions = async () => {
  loading.value = true

  try {
    const token = localStorage.getItem('token')
    const params = new URLSearchParams({
      current: pagination.current,
      size: pagination.size
    })

    if (filters.year) params.append('year', filters.year)
    if (filters.status) params.append('status', filters.status)

    const response = await fetch(`/api/competitions?${params.toString()}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })

    const data = await response.json()

    if (data.code === 200) {
      competitions.value = data.data.records || []
      pagination.total = data.data.total || 0
    } else {
      ElMessage.error(data.message || '获取赛事列表失败')
    }
  } catch (error) {
    ElMessage.error('获取赛事列表失败')
  } finally {
    loading.value = false
  }
}

const showCreateDialog = () => {
  isEdit.value = false
  dialogTitle.value = '创建赛事'
  resetForm()
  showDialog.value = true
}

const editCompetition = (comp) => {
  isEdit.value = true
  dialogTitle.value = '编辑赛事'

  competitionForm.id = comp.id
  competitionForm.competitionName = comp.competitionName
  competitionForm.competitionYear = comp.competitionYear
  competitionForm.organizer = comp.organizer || ''
  competitionForm.description = comp.description || ''

  // 处理日期时间格式 - 转换为字符串格式
  const start = comp.registrationStart ? formatDateTime(comp.registrationStart) : ''
  const end = comp.registrationEnd ? formatDateTime(comp.registrationEnd) : ''
  competitionForm.registrationPeriod = [start, end]

  competitionForm.contactPhone = comp.contactPhone || ''
  competitionForm.contactEmail = comp.contactEmail || ''

  showDialog.value = true
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  // 如果已经是字符串格式，直接返回
  if (typeof dateStr === 'string') return dateStr
  // 如果是Date对象或时间戳，转换为字符串
  const date = new Date(dateStr)
  return date.toISOString().slice(0, 19).replace('T', ' ')
}

const handleSubmit = async () => {
  if (!competitionForm.competitionName) {
    ElMessage.warning('请填写赛事名称')
    return
  }

  if (!competitionForm.registrationPeriod || competitionForm.registrationPeriod.length < 2) {
    ElMessage.warning('请选择报名时间')
    return
  }

  submitting.value = true

  try {
    const token = localStorage.getItem('token')
    const url = isEdit.value ? `/api/competitions/${competitionForm.id}` : '/api/competitions'
    const method = isEdit.value ? 'PUT' : 'POST'

    const body = {
      competitionName: competitionForm.competitionName,
      competitionYear: competitionForm.competitionYear,
      organizer: competitionForm.organizer,
      description: competitionForm.description,
      registrationStart: competitionForm.registrationPeriod[0] || null,
      registrationEnd: competitionForm.registrationPeriod[1] || null,
      submissionStart: competitionForm.registrationPeriod[0] || null,
      submissionEnd: competitionForm.registrationPeriod[1] || null,
      reviewStart: competitionForm.registrationPeriod[1] || null,
      reviewEnd: competitionForm.registrationPeriod[1] || null,
      contactPhone: competitionForm.contactPhone,
      contactEmail: competitionForm.contactEmail,
      status: isEdit.value ? undefined : 'DRAFT'
    }

    if (isEdit.value) {
      body.id = competitionForm.id
      // 编辑时不修改状态
      delete body.status
    }

    const response = await fetch(url, {
      method,
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(body)
    })

    const data = await response.json()

    if (data.code === 200) {
      ElMessage.success(isEdit.value ? '赛事更新成功' : '赛事创建成功')
      showDialog.value = false
      await fetchCompetitions()
    } else {
      ElMessage.error(data.message || '操作失败')
    }
  } catch (error) {
    console.error('提交错误:', error)
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

const publishCompetition = async (comp) => {
  try {
    await ElMessageBox.confirm(
      `确定要发布赛事 "${comp.competitionName}" 吗？`,
      '发布赛事',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const token = localStorage.getItem('token')
    const response = await fetch(`/api/competitions/${comp.id}/publish`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })

    const data = await response.json()

    if (data.code === 200) {
      ElMessage.success('赛事发布成功')
      await fetchCompetitions()
    } else {
      ElMessage.error(data.message || '发布失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('发布失败')
    }
  }
}

const startCompetition = async (comp) => {
  try {
    await ElMessageBox.confirm(
      `确定要开始赛事 "${comp.competitionName}" 吗？`,
      '开始赛事',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const token = localStorage.getItem('token')
    const response = await fetch(`/api/competitions/${comp.id}/start`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })

    const data = await response.json()

    if (data.code === 200) {
      ElMessage.success('赛事已开始')
      await fetchCompetitions()
    } else {
      ElMessage.error(data.message || '开始失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('开始失败')
    }
  }
}

const finishCompetition = async (comp) => {
  try {
    await ElMessageBox.confirm(
      `确定要结束赛事 "${comp.competitionName}" 吗？`,
      '结束赛事',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const token = localStorage.getItem('token')
    const response = await fetch(`/api/competitions/${comp.id}/finish`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })

    const data = await response.json()

    if (data.code === 200) {
      ElMessage.success('赛事已结束')
      await fetchCompetitions()
    } else {
      ElMessage.error(data.message || '结束失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('结束失败')
    }
  }
}

const viewTracks = (comp) => {
  router.push({
    path: `/competitions/${comp.id}/tracks`,
    query: { from: 'admin' }
  })
}

const handleSizeChange = (val) => {
  pagination.size = val
  fetchCompetitions()
}

const handleCurrentChange = (val) => {
  pagination.current = val
  fetchCompetitions()
}

const resetForm = () => {
  competitionForm.id = null
  competitionForm.competitionName = ''
  competitionForm.competitionYear = 2026
  competitionForm.organizer = ''
  competitionForm.description = ''
  competitionForm.registrationPeriod = []
  competitionForm.contactPhone = ''
  competitionForm.contactEmail = ''
}

const getStatusClass = (status) => {
  return {
    'badge-primary': status === 'PUBLISHED',
    'badge-warning': status === 'ONGOING',
    'badge-secondary': status === 'DRAFT',
    'badge-gray': status === 'FINISHED'
  }
}

const getStatusText = (status) => {
  const texts = {
    DRAFT: '草稿',
    PUBLISHED: '已发布',
    ONGOING: '进行中',
    FINISHED: '已结束'
  }
  return texts[status] || status
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.competitions-page {
  min-height: 100vh;
}

.page-header {
  margin-bottom: var(--spacing-xl);
}

.filters-section {
  padding: var(--spacing-lg);
}

.filters-row {
  align-items: center;
}

.filters-controls {
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

.btn-text-danger {
  color: var(--color-error);
}

.action-buttons {
  display: flex;
  gap: var(--spacing-xs);
  flex-wrap: wrap;
}

.btn-action-edit,
.btn-action-publish,
.btn-action-start,
.btn-action-finish,
.btn-action-tracks {
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

.btn-action-edit:hover {
  background: var(--color-accent);
  color: var(--color-white);
  border-color: var(--color-accent);
}

.btn-action-publish:hover {
  background: #10b981;
  color: var(--color-white);
  border-color: #10b981;
}

.btn-action-start:hover {
  background: #3b82f6;
  color: var(--color-white);
  border-color: #3b82f6;
}

.btn-action-finish:hover {
  background: #ef4444;
  color: var(--color-white);
  border-color: #ef4444;
}

.btn-action-tracks:hover {
  background: #8b5cf6;
  color: var(--color-white);
  border-color: #8b5cf6;
}

.date-picker-popper {
  z-index: 9999 !important;
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
}
</style>