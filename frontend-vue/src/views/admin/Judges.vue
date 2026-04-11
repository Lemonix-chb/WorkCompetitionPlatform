<template>
  <div class="judges-page">
    <div class="container">
      <!-- Page Header -->
      <div class="page-header slide-in">
        <div class="header-content flex-between">
          <div>
            <h1 class="page-title">评审员管理</h1>
            <p class="body-text">管理评审员信息和状态</p>
          </div>
          <button class="btn-primary" @click="openAddDialog">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M10 4V16M4 10H16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            新增评审员
          </button>
        </div>
      </div>

      <!-- Filters -->
      <div class="filters-section card card-flat scale-in mb-lg">
        <div class="filters-row flex gap-md">
          <el-input
            v-model="filters.keyword"
            placeholder="搜索评审员（姓名/工号）"
            clearable
            style="width: 250px"
          />

          <el-select v-model="filters.status" placeholder="用户状态" clearable style="width: 150px">
            <el-option label="待审核" value="PENDING" />
            <el-option label="已激活" value="ACTIVE" />
            <el-option label="已禁用" value="INACTIVE" />
          </el-select>

          <button class="btn-secondary" @click="fetchJudges">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <circle cx="10" cy="10" r="8" stroke="currentColor" stroke-width="2"/>
              <path d="M10 6V10L13 13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            搜索
          </button>
        </div>
      </div>

      <!-- Judges List -->
      <div class="judges-section scale-in">
        <!-- Loading -->
        <div v-if="loading" class="loading-state card">
          <div class="spinner"></div>
        </div>

        <!-- Data Table -->
        <div v-else-if="judges.length > 0" class="data-table card">
          <el-table :data="judges" style="width: 100%" stripe>
            <el-table-column prop="teacherNo" label="工号" width="120" />
            <el-table-column prop="realName" label="姓名" width="120" />
            <el-table-column prop="username" label="用户名" width="120" />
            <el-table-column prop="email" label="邮箱" min-width="180" />
            <el-table-column prop="phone" label="联系电话" width="150" />
            <el-table-column prop="college" label="学院" min-width="150" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <span :class="getStatusClass(row.status)" class="badge">
                  {{ getStatusText(row.status) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="注册时间" width="120">
              <template #default="{ row }">
                {{ formatDate(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="280" fixed="right">
              <template #default="{ row }">
                <div class="action-buttons flex gap-sm">
                  <button class="btn-action-view" @click="viewDetail(row)">
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                      <circle cx="8" cy="6" r="4" stroke="currentColor" stroke-width="1.5"/>
                      <path d="M3 14C3 10 5.5 8 8 8C10.5 8 13 10 13 14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    </svg>
                    查看
                  </button>
                  <button class="btn-action-edit" @click="editJudge(row)">
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                      <path d="M11.5 2.5L13.5 4.5M2 12L12 2M12 2L14 4L4 14H2V12Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    编辑
                  </button>
                  <button
                    v-if="row.status === 'PENDING'"
                    class="btn-action-approve"
                    @click="approveJudge(row)"
                  >
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                      <path d="M2 8L6 12L14 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    审核
                  </button>
                  <button
                    v-if="row.status === 'ACTIVE'"
                    class="btn-action-disable"
                    @click="disableJudge(row)"
                  >
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                      <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.5"/>
                      <path d="M5 8H11" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    </svg>
                    禁用
                  </button>
                  <button
                    v-if="row.status === 'INACTIVE'"
                    class="btn-action-activate"
                    @click="activateJudge(row)"
                  >
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                      <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.5"/>
                      <path d="M8 5V11M5 8H11" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    </svg>
                    激活
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
              <circle cx="24" cy="16" r="12" stroke="#bdc3c7" stroke-width="2"/>
              <path d="M12 40C12 34 18 30 24 30C30 30 36 34 36 40" stroke="#bdc3c7" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <h3 class="card-title mb-md">暂无评审员数据</h3>
          <p class="caption">等待评审员注册</p>
        </div>
      </div>

      <!-- Detail Dialog -->
      <el-dialog
        v-model="showDetailDialog"
        title="评审员详情"
        width="600px"
      >
        <div v-if="selectedJudge" class="detail-content">
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label caption">工号</span>
              <span class="detail-value body-text">{{ selectedJudge.teacherNo }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label caption">姓名</span>
              <span class="detail-value body-text">{{ selectedJudge.realName }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label caption">用户名</span>
              <span class="detail-value body-text">{{ selectedJudge.username }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label caption">邮箱</span>
              <span class="detail-value body-text">{{ selectedJudge.email }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label caption">联系电话</span>
              <span class="detail-value body-text">{{ selectedJudge.phone }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label caption">学院</span>
              <span class="detail-value body-text">{{ selectedJudge.college }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label caption">状态</span>
              <span :class="getStatusClass(selectedJudge.status)" class="badge">
                {{ getStatusText(selectedJudge.status) }}
              </span>
            </div>
            <div class="detail-item">
              <span class="detail-label caption">注册时间</span>
              <span class="detail-value caption">{{ formatDate(selectedJudge.createTime) }}</span>
            </div>
          </div>
        </div>
      </el-dialog>

      <!-- Edit Dialog -->
      <el-dialog
        v-model="showEditDialog"
        title="编辑评审员信息"
        width="600px"
      >
        <el-form :model="editForm" label-width="100px">
          <el-form-item label="工号">
            <el-input v-model="editForm.teacherNo" placeholder="请输入工号" />
          </el-form-item>
          <el-form-item label="姓名">
            <el-input v-model="editForm.realName" placeholder="请输入姓名" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="editForm.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item label="联系电话">
            <el-input v-model="editForm.phone" placeholder="请输入联系电话" />
          </el-form-item>
          <el-form-item label="学院">
            <el-input v-model="editForm.college" placeholder="请输入学院" />
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="showEditDialog = false">取消</el-button>
            <el-button type="primary" @click="submitEdit" :loading="submitting">保存</el-button>
          </div>
        </template>
      </el-dialog>

      <!-- Add Dialog -->
      <el-dialog
        v-model="showAddDialog"
        title="新增评审员"
        width="600px"
      >
        <el-form :model="addForm" label-width="100px">
          <el-form-item label="用户名" required>
            <el-input v-model="addForm.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码" required>
            <el-input v-model="addForm.password" type="password" placeholder="请输入密码" show-password />
          </el-form-item>
          <el-form-item label="姓名" required>
            <el-input v-model="addForm.realName" placeholder="请输入姓名" />
          </el-form-item>
          <el-form-item label="工号" required>
            <el-input v-model="addForm.teacherNo" placeholder="请输入工号" />
          </el-form-item>
          <el-form-item label="邮箱" required>
            <el-input v-model="addForm.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item label="联系电话">
            <el-input v-model="addForm.phone" placeholder="请输入联系电话" />
          </el-form-item>
          <el-form-item label="学院">
            <el-input v-model="addForm.college" placeholder="请输入学院" />
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="showAddDialog = false">取消</el-button>
            <el-button type="primary" @click="submitAdd" :loading="submitting">创建</el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const judges = ref([])
const loading = ref(false)
const showDetailDialog = ref(false)
const showEditDialog = ref(false)
const showAddDialog = ref(false)
const selectedJudge = ref(null)
const submitting = ref(false)

const editForm = reactive({
  id: null,
  teacherNo: '',
  realName: '',
  email: '',
  phone: '',
  college: ''
})

const addForm = reactive({
  username: '',
  password: '',
  realName: '',
  teacherNo: '',
  email: '',
  phone: '',
  college: '',
  role: 'JUDGE'
})

const filters = reactive({
  keyword: '',
  status: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

onMounted(async () => {
  await fetchJudges()
})

const fetchJudges = async () => {
  loading.value = true

  try {
    const token = localStorage.getItem('token')
    const params = new URLSearchParams({
      current: pagination.current,
      size: pagination.size,
      role: 'JUDGE'
    })

    if (filters.keyword) params.append('keyword', filters.keyword)
    if (filters.status) params.append('status', filters.status)

    const response = await fetch(`/api/users?${params.toString()}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })

    const data = await response.json()

    if (data.code === 200) {
      judges.value = data.data.records || []
      pagination.total = data.data.total || 0
    } else {
      ElMessage.error(data.message || '获取评审员列表失败')
    }
  } catch (error) {
    ElMessage.error('获取评审员列表失败')
  } finally {
    loading.value = false
  }
}

const viewDetail = (judge) => {
  selectedJudge.value = judge
  showDetailDialog.value = true
}

const editJudge = (judge) => {
  editForm.id = judge.id
  editForm.teacherNo = judge.teacherNo || ''
  editForm.realName = judge.realName || ''
  editForm.email = judge.email || ''
  editForm.phone = judge.phone || ''
  editForm.college = judge.college || ''
  showEditDialog.value = true
}

const submitEdit = async () => {
  submitting.value = true

  try {
    const token = localStorage.getItem('token')
    const response = await fetch(`/api/users/${editForm.id}`, {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        teacherNo: editForm.teacherNo,
        realName: editForm.realName,
        email: editForm.email,
        phone: editForm.phone,
        college: editForm.college
      })
    })

    const data = await response.json()

    if (data.code === 200) {
      ElMessage.success('评审员信息更新成功')
      showEditDialog.value = false
      await fetchJudges()
    } else {
      ElMessage.error(data.message || '更新失败')
    }
  } catch (error) {
    ElMessage.error('更新失败')
  } finally {
    submitting.value = false
  }
}

const openAddDialog = () => {
  addForm.username = ''
  addForm.password = ''
  addForm.realName = ''
  addForm.teacherNo = ''
  addForm.email = ''
  addForm.phone = ''
  addForm.college = ''
  showAddDialog.value = true
}

const submitAdd = async () => {
  if (!addForm.username || !addForm.password || !addForm.realName || !addForm.teacherNo || !addForm.email) {
    ElMessage.warning('请填写所有必填项')
    return
  }

  submitting.value = true

  try {
    const token = localStorage.getItem('token')
    const response = await fetch('/api/auth/register', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(addForm)
    })

    const data = await response.json()

    if (data.code === 200) {
      ElMessage.success('评审员创建成功')
      showAddDialog.value = false
      await fetchJudges()
    } else {
      ElMessage.error(data.message || '创建失败')
    }
  } catch (error) {
    ElMessage.error('创建失败')
  } finally {
    submitting.value = false
  }
}

const approveJudge = async (judge) => {
  try {
    await ElMessageBox.confirm(
      `确定要审核通过评审员 "${judge.realName}" 吗？`,
      '审核评审员',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const token = localStorage.getItem('token')
    const response = await fetch(`/api/users/${judge.id}/status?status=ACTIVE`, {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })

    const data = await response.json()

    if (data.code === 200) {
      ElMessage.success('审核成功')
      await fetchJudges()
    } else {
      ElMessage.error(data.message || '审核失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('审核失败')
    }
  }
}

const disableJudge = async (judge) => {
  try {
    await ElMessageBox.confirm(
      `确定要禁用评审员 "${judge.realName}" 吗？`,
      '禁用评审员',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const token = localStorage.getItem('token')
    const response = await fetch(`/api/users/${judge.id}/status?status=INACTIVE`, {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })

    const data = await response.json()

    if (data.code === 200) {
      ElMessage.success('评审员已禁用')
      await fetchJudges()
    } else {
      ElMessage.error(data.message || '禁用失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('禁用失败')
    }
  }
}

const activateJudge = async (judge) => {
  try {
    await ElMessageBox.confirm(
      `确定要激活评审员 "${judge.realName}" 吗？`,
      '激活评审员',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const token = localStorage.getItem('token')
    const response = await fetch(`/api/users/${judge.id}/status?status=ACTIVE`, {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })

    const data = await response.json()

    if (data.code === 200) {
      ElMessage.success('评审员已激活')
      await fetchJudges()
    } else {
      ElMessage.error(data.message || '激活失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('激活失败')
    }
  }
}

const handleSizeChange = (val) => {
  pagination.size = val
  fetchJudges()
}

const handleCurrentChange = (val) => {
  pagination.current = val
  fetchJudges()
}

const getStatusClass = (status) => {
  return {
    'badge-warning': status === 'PENDING',
    'badge-success': status === 'ACTIVE',
    'badge-gray': status === 'INACTIVE'
  }
}

const getStatusText = (status) => {
  const texts = {
    PENDING: '待审核',
    ACTIVE: '已激活',
    INACTIVE: '已禁用'
  }
  return texts[status] || status
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.judges-page {
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

.btn-text-danger {
  color: var(--color-error);
}

.btn-text-primary {
  color: var(--color-accent);
}

.action-buttons {
  display: flex;
  gap: var(--spacing-xs);
  flex-wrap: wrap;
}

.btn-action-view,
.btn-action-edit,
.btn-action-approve,
.btn-action-disable,
.btn-action-activate {
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

.btn-action-edit:hover {
  background: var(--color-accent);
  color: var(--color-white);
  border-color: var(--color-accent);
}

.btn-action-approve:hover {
  background: #10b981;
  color: var(--color-white);
  border-color: #10b981;
}

.btn-action-disable:hover {
  background: #ef4444;
  color: var(--color-white);
  border-color: #ef4444;
}

.btn-action-activate:hover {
  background: #3b82f6;
  color: var(--color-white);
  border-color: #3b82f6;
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
}
</style>