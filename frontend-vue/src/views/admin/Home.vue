<template>
  <div class="admin-home">
    <div class="container">
      <!-- Academic Page Header -->
      <div class="page-header slide-in">
        <div class="header-content">
          <div class="academic-badge-header">
            <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
              <!-- Academic Shield -->
              <path d="M16 1.5L28.5 6.5V16C28.5 24 23 29 16 30.5C9 29 3.5 24 3.5 16V6.5L16 1.5Z"
                    fill="var(--color-primary)"
                    stroke="var(--color-accent)"
                    stroke-width="0.75"/>
              <!-- Management Icon -->
              <circle cx="16" cy="12" r="4" stroke="var(--color-accent)" stroke-width="1" fill="none"/>
              <path d="M10 24C10 20 13 18 16 18C19 18 22 20 22 24"
                    stroke="var(--color-accent)"
                    stroke-width="1"
                    stroke-linecap="round"/>
              <!-- Tools Icon -->
              <rect x="9" y="22" width="2" height="2" fill="var(--color-accent)" rx="0.5"/>
              <rect x="21" y="22" width="2" height="2" fill="var(--color-accent)" rx="0.5"/>
            </svg>
          </div>
          <div class="header-text">
            <h1 class="page-title">管理中心</h1>
            <p class="header-subtitle">欢迎回来，{{ userName }}</p>
          </div>
        </div>
        <div class="header-meta">
          <span class="meta-label">当前状态</span>
          <span class="badge badge-completed">系统运行中</span>
        </div>
      </div>

      <!-- Professional Stats Section -->
      <div class="stats-section scale-in">
        <div class="section-header">
          <h2 class="section-title">系统概览</h2>
          <div class="section-divider"></div>
        </div>

        <div class="grid grid-4">
          <div class="stat-card card">
            <div class="stat-icon">
              <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
                <!-- Competition Trophy -->
                <path d="M24 8L32 12V20C32 28 28 32 24 34C20 32 16 28 16 20V12L24 8Z"
                      fill="none"
                      stroke="var(--color-accent)"
                      stroke-width="2"/>
                <circle cx="24" cy="18" r="3" fill="var(--color-accent)"/>
                <!-- Laurels -->
                <path d="M12 36C12 36 14 32 16 34M32 34C32 34 34 32 36 36"
                      stroke="var(--color-accent)"
                      stroke-width="1.5"
                      stroke-linecap="round"/>
                <rect x="20" y="36" width="8" height="4" rx="1" fill="var(--color-accent)"/>
              </svg>
            </div>
            <div class="stat-number">{{ stats.totalCompetitions }}</div>
            <div class="stat-label">赛事总数</div>
            <div class="stat-trend">
              <span class="trend-text">进行中 {{ stats.activeCompetitions || 0 }} 个</span>
            </div>
          </div>

          <div class="stat-card card">
            <div class="stat-icon">
              <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
                <!-- Team Group -->
                <circle cx="16" cy="16" r="6" stroke="var(--color-accent)" stroke-width="2" fill="none"/>
                <circle cx="32" cy="16" r="6" stroke="var(--color-accent)" stroke-width="2" fill="none"/>
                <circle cx="24" cy="28" r="6" stroke="var(--color-accent)" stroke-width="2" fill="none"/>
                <!-- Connections -->
                <path d="M16 22V23C16 26 20 28 24 28C28 28 32 26 32 23V22"
                      stroke="var(--color-accent)"
                      stroke-width="1.5"
                      stroke-linecap="round"/>
              </svg>
            </div>
            <div class="stat-number">{{ stats.totalTeams }}</div>
            <div class="stat-label">报名团队</div>
            <div class="stat-trend">
              <span class="trend-text">本周新增 {{ stats.newTeamsThisWeek || 0 }} 个</span>
            </div>
          </div>

          <div class="stat-card card card-accent">
            <div class="stat-icon">
              <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
                <!-- Work Document -->
                <rect x="10" y="8" width="28" height="32" rx="3" fill="none" stroke="var(--color-accent)" stroke-width="2"/>
                <!-- Text Lines -->
                <line x1="14" y1="14" x2="34" y2="14" stroke="var(--color-accent)" stroke-width="1.5" stroke-linecap="round"/>
                <line x1="14" y1="20" x2="34" y2="20" stroke="var(--color-accent)" stroke-width="1.5" stroke-linecap="round"/>
                <line x1="14" y1="26" x2="26" y2="26" stroke="var(--color-accent)" stroke-width="1.5" stroke-linecap="round"/>
                <!-- Check Mark -->
                <path d="M30 26L32 28L36 24"
                      stroke="var(--color-accent)"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="stat-number">{{ stats.totalWorks }}</div>
            <div class="stat-label">提交作品</div>
            <div class="stat-trend">
              <span class="trend-text accent-text">待评审 {{ stats.pendingWorks || 0 }} 个</span>
            </div>
          </div>

          <div class="stat-card card">
            <div class="stat-icon">
              <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
                <!-- User Profile -->
                <circle cx="24" cy="16" r="10" stroke="var(--color-accent)" stroke-width="2" fill="none"/>
                <!-- Body -->
                <path d="M12 40C12 34 18 30 24 30C30 30 36 34 36 40"
                      stroke="var(--color-accent)"
                      stroke-width="2"
                      stroke-linecap="round"/>
                <!-- Academic Hat -->
                <path d="M14 12L24 8L34 12L24 16L14 12Z"
                      fill="var(--color-accent)"
                      opacity="0.3"/>
              </svg>
            </div>
            <div class="stat-number">{{ stats.totalUsers }}</div>
            <div class="stat-label">注册用户</div>
            <div class="stat-trend">
              <span class="trend-text">学生 {{ stats.totalStudents || 0 }} 人</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Management Actions Section -->
      <div class="actions-section slide-in">
        <div class="section-header">
          <h2 class="section-title">系统管理</h2>
          <div class="section-divider"></div>
        </div>

        <div class="grid grid-3">
          <!-- Competition Management -->
          <router-link to="/admin/competitions" class="action-card card card-interactive">
            <div class="action-icon-wrapper">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <path d="M16 4L24 8V16C24 24 20 28 16 30C12 28 8 24 8 16V8L16 4Z"
                      fill="none"
                      stroke="var(--color-accent)"
                      stroke-width="2"/>
                <circle cx="16" cy="12" r="2" fill="var(--color-accent)"/>
              </svg>
            </div>
            <div class="action-content">
              <h3 class="card-title">赛事管理</h3>
              <p class="caption">创建、编辑和发布竞赛信息</p>
              <div class="action-meta">
                <span class="badge badge-pending">需配置</span>
              </div>
            </div>
            <div class="action-arrow">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                <path d="M8 6L16 12L8 18" stroke="var(--color-text-tertiary)" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
          </router-link>

          <!-- Judge Management -->
          <router-link to="/admin/judges" class="action-card card card-interactive">
            <div class="action-icon-wrapper">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <!-- Judge Badge -->
                <circle cx="16" cy="10" r="6" stroke="var(--color-accent)" stroke-width="2" fill="none"/>
                <path d="M8 26C8 22 12 20 16 20C20 20 24 22 24 26"
                      stroke="var(--color-accent)"
                      stroke-width="2"
                      stroke-linecap="round"/>
                <!-- Magnifying Glass -->
                <circle cx="22" cy="22" r="3" stroke="var(--color-accent)" stroke-width="1.5" fill="none"/>
                <line x1="24" y1="24" x2="26" y2="26" stroke="var(--color-accent)" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="action-content">
              <h3 class="card-title">评审员管理</h3>
              <p class="caption">分配评审任务、管理评委信息</p>
              <div class="action-meta">
                <span class="badge badge-reviewing">已分配 {{ stats.totalJudges || 0 }} 人</span>
              </div>
            </div>
            <div class="action-arrow">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                <path d="M8 6L16 12L8 18" stroke="var(--color-text-tertiary)" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
          </router-link>

          <!-- Student Management -->
          <router-link to="/admin/students" class="action-card card card-interactive">
            <div class="action-icon-wrapper">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <circle cx="16" cy="10" r="6" stroke="var(--color-accent)" stroke-width="2" fill="none"/>
                <path d="M8 26C8 22 12 20 16 20C20 20 24 22 24 26"
                      stroke="var(--color-accent)"
                      stroke-width="2"
                      stroke-linecap="round"/>
              </svg>
            </div>
            <div class="action-content">
              <h3 class="card-title">参赛者管理</h3>
              <p class="caption">管理学生参赛信息</p>
              <div class="action-meta">
                <span class="badge badge-completed">{{ stats.totalStudents || 0 }} 人参与</span>
              </div>
            </div>
            <div class="action-arrow">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                <path d="M8 6L16 12L8 18" stroke="var(--color-text-tertiary)" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
          </router-link>

          <!-- Works Management -->
          <router-link to="/admin/works" class="action-card card card-interactive card-accent">
            <div class="action-icon-wrapper">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <rect x="6" y="6" width="20" height="20" rx="2" fill="none" stroke="var(--color-accent)" stroke-width="2"/>
                <line x1="10" y1="10" x2="22" y2="10" stroke="var(--color-accent)" stroke-width="1.5" stroke-linecap="round"/>
                <line x1="10" y1="14" x2="22" y2="14" stroke="var(--color-accent)" stroke-width="1.5" stroke-linecap="round"/>
                <line x1="10" y1="18" x2="16" y2="18" stroke="var(--color-accent)" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="action-content">
              <h3 class="card-title accent-text">作品管理</h3>
              <p class="caption">评审提交作品、管理文件</p>
              <div class="action-meta">
                <span class="badge badge-awarded">{{ stats.totalWorks || 0 }} 个作品</span>
              </div>
            </div>
            <div class="action-arrow accent-arrow">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                <path d="M8 6L16 12L8 18" stroke="var(--color-accent)" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
          </router-link>

          <!-- Review Management -->
          <router-link to="/admin/reviews" class="action-card card card-interactive">
            <div class="action-icon-wrapper">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <!-- Review Shield -->
                <path d="M16 2L26 7V16C26 23 21 27 16 28.5C11 27 6 23 6 16V7L16 2Z"
                      fill="none"
                      stroke="var(--color-accent)"
                      stroke-width="2"/>
                <!-- Check Mark -->
                <path d="M10 16L13 19L22 12"
                      stroke="var(--color-accent)"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="action-content">
              <h3 class="card-title">评审管理</h3>
              <p class="caption">分配任务、监督评审进度</p>
              <div class="action-meta">
                <div class="review-progress">
                  <div class="progress-fill completed" style="width: 75%"></div>
                </div>
                <span class="progress-text">完成 75%</span>
              </div>
            </div>
            <div class="action-arrow">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                <path d="M8 6L16 12L8 18" stroke="var(--color-text-tertiary)" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
          </router-link>

          <!-- Statistics -->
          <router-link to="/admin/stats" class="action-card card card-interactive">
            <div class="action-icon-wrapper">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <!-- Bar Chart -->
                <rect x="6" y="20" width="4" height="6" rx="1" fill="var(--color-accent)" opacity="0.6"/>
                <rect x="12" y="14" width="4" height="12" rx="1" fill="var(--color-accent)" opacity="0.8"/>
                <rect x="18" y="8" width="4" height="18" rx="1" fill="var(--color-accent)"/>
                <!-- Trend Line -->
                <path d="M8 18L14 12L20 6L26 8"
                      stroke="var(--color-accent)"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="action-content">
              <h3 class="card-title">数据统计</h3>
              <p class="caption">系统数据分析、报表导出</p>
              <div class="action-meta">
                <span class="badge badge-ai">AI分析</span>
              </div>
            </div>
            <div class="action-arrow">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                <path d="M8 6L16 12L8 18" stroke="var(--color-text-tertiary)" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
          </router-link>
        </div>
      </div>

      <!-- Quick Status Overview -->
      <div class="status-section fade-in">
        <div class="section-header">
          <h2 class="section-title">评审状态监控</h2>
          <div class="section-divider"></div>
        </div>

        <div class="status-cards">
          <div class="status-card card">
            <div class="status-indicator pending"></div>
            <div class="status-content">
              <h4 class="status-title">待评审</h4>
              <p class="status-number">{{ stats.pendingWorks || 0 }} 个作品</p>
              <p class="status-desc">等待评委分配</p>
            </div>
          </div>

          <div class="status-card card">
            <div class="status-indicator reviewing"></div>
            <div class="status-content">
              <h4 class="status-title">评审中</h4>
              <p class="status-number">{{ stats.reviewingWorks || 0 }} 个作品</p>
              <p class="status-desc">评委正在评审</p>
            </div>
          </div>

          <div class="status-card card">
            <div class="status-indicator completed"></div>
            <div class="status-content">
              <h4 class="status-title">已完成</h4>
              <p class="status-number">{{ stats.completedReviews || 0 }} 个评审</p>
              <p class="status-desc">评审结果已提交</p>
            </div>
          </div>

          <div class="status-card card card-accent">
            <div class="status-indicator awarded"></div>
            <div class="status-content">
              <h4 class="status-title">已获奖</h4>
              <p class="status-number accent-text">{{ stats.awardedWorks || 0 }} 个作品</p>
              <p class="status-desc">评审结果优异</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Data Visualization Charts -->
      <div class="charts-section fade-in">
        <div class="section-header">
          <h2 class="section-title">数据可视化分析</h2>
          <div class="section-divider"></div>
        </div>

        <div class="charts-grid">
          <!-- Works Submission Trend Chart -->
          <div class="chart-card card">
            <h3 class="chart-title">📊 作品提交趋势</h3>
            <div ref="lineChartRef" class="chart-container"></div>
          </div>

          <!-- Work Type Distribution Chart -->
          <div class="chart-card card">
            <h3 class="chart-title">📈 作品类型分布</h3>
            <div ref="pieChartRef" class="chart-container"></div>
          </div>

          <!-- Review Progress Chart -->
          <div class="chart-card card">
            <h3 class="chart-title">📉 评审进度统计</h3>
            <div ref="barChartRef" class="chart-container"></div>
          </div>

          <!-- AI Review Pass Rate Chart -->
          <div class="chart-card card">
            <h3 class="chart-title">🎯 AI评审通过率</h3>
            <div ref="gaugeChartRef" class="chart-container"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { get } from '@/utils/api'
import { showSuccess, showError, showWarning } from '@/utils/messageUtils'
import * as echarts from 'echarts'

const router = useRouter()

const userName = computed(() => localStorage.getItem('userName') || '管理员')

const stats = ref({
  totalCompetitions: 0,
  activeCompetitions: 0,
  totalTeams: 0,
  newTeamsThisWeek: 0,
  totalWorks: 0,
  pendingWorks: 0,
  reviewingWorks: 0,
  completedReviews: 0,
  awardedWorks: 0,
  totalUsers: 0,
  totalStudents: 0,
  totalJudges: 0,
  codeWorks: 0,
  pptWorks: 0,
  videoWorks: 0,
  aiValidCount: 0,
  aiInvalidCount: 0,
  submissionTrend: {
    dates: [],
    counts: []
  }
})

// Chart refs
const lineChartRef = ref(null)
const pieChartRef = ref(null)
const barChartRef = ref(null)
const gaugeChartRef = ref(null)

// Chart instances
let lineChart = null
let pieChart = null
let barChart = null
let gaugeChart = null

onMounted(async () => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('userRole')

  if (!token) {
    router.push('/login')
    return
  }

  if (role !== 'ADMIN') {
    showWarning('您没有权限访问管理员页面')
    router.push('/login')
    return
  }

  await fetchStats()

  // Initialize charts after data loaded and DOM rendered
  await nextTick()
  setTimeout(() => {
    initCharts()
  }, 100)

  // Add window resize listener for responsive charts
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  // Clean up: remove resize listener and dispose charts
  window.removeEventListener('resize', handleResize)
  if (lineChart) lineChart.dispose()
  if (pieChart) pieChart.dispose()
  if (barChart) barChart.dispose()
  if (gaugeChart) gaugeChart.dispose()
})

const handleResize = () => {
  if (lineChart) lineChart.resize()
  if (pieChart) pieChart.resize()
  if (barChart) barChart.resize()
  if (gaugeChart) gaugeChart.resize()
}

const fetchStats = async () => {
  try {
    const data = await get('/admin/stats')
    stats.value = data
    console.log('管理员统计数据加载成功:', data)
    console.log('提交趋势数据:', data.submissionTrend)
  } catch (error) {
    console.error('获取统计数据失败', error)
    showError('获取统计数据失败')
  }
}

const initCharts = () => {
  console.log('开始初始化图表...')

  // 先销毁已存在的图表实例（避免重复初始化）
  if (lineChart) {
    lineChart.dispose()
    lineChart = null
  }
  if (pieChart) {
    pieChart.dispose()
    pieChart = null
  }
  if (barChart) {
    barChart.dispose()
    barChart = null
  }
  if (gaugeChart) {
    gaugeChart.dispose()
    gaugeChart = null
  }

  // Line Chart - Works Submission Trend
  if (lineChartRef.value) {
    console.log('初始化折线图...')
    try {
      lineChart = echarts.init(lineChartRef.value)
      const lineOption = {
        tooltip: {
          trigger: 'axis'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: stats.value.submissionTrend?.dates || ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          name: '作品提交数',
          type: 'line',
          smooth: true,
          lineStyle: {
            color: '#0071e3',
            width: 3
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
              offset: 0,
              color: 'rgba(0, 113, 227, 0.3)'
            }, {
              offset: 1,
              color: 'rgba(0, 113, 227, 0.1)'
            }])
          },
          data: stats.value.submissionTrend?.counts || [5, 8, 12, 15, 10, 7, 3]
        }]
      }
      lineChart.setOption(lineOption)
      console.log('折线图初始化成功，数据:', stats.value.submissionTrend)
    } catch (error) {
      console.error('折线图初始化失败:', error)
    }
  } else {
    console.warn('折线图容器不存在')
  }

  // Pie Chart - Work Type Distribution
  if (pieChartRef.value) {
    console.log('初始化饼图...')
    try {
      pieChart = echarts.init(pieChartRef.value)
      const pieOption = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [{
          name: '作品类型',
          type: 'pie',
          radius: '60%',
          center: ['60%', '50%'],
          data: [
            { value: stats.value.codeWorks || 0, name: '程序设计', itemStyle: { color: '#7B68EE' } },
            { value: stats.value.pptWorks || 0, name: '演示文稿', itemStyle: { color: '#FF69B4' } },
            { value: stats.value.videoWorks || 0, name: '数媒动漫', itemStyle: { color: '#00CED1' } }
          ],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }]
      }
      pieChart.setOption(pieOption)
      console.log('饼图初始化成功')
    } catch (error) {
      console.error('饼图初始化失败:', error)
    }
  }

  // Bar Chart - Review Progress
  if (barChartRef.value) {
    console.log('初始化柱状图...')
    try {
      barChart = echarts.init(barChartRef.value)
      const barOption = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: ['待评审', '评审中', '已完成', '已获奖']
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          name: '作品数量',
          type: 'bar',
          barWidth: '60%',
          data: [
            {
              value: stats.value.pendingWorks || 0,
              itemStyle: { color: '#f39c12' }
            },
            {
              value: stats.value.reviewingWorks || 0,
              itemStyle: { color: '#3498db' }
            },
            {
              value: stats.value.completedReviews || 0,
            itemStyle: { color: '#2ecc71' }
          },
          {
            value: stats.value.awardedWorks || 0,
            itemStyle: { color: '#FFD700' }
          }
        ]
      }]
    }
    barChart.setOption(barOption)
    console.log('柱状图初始化成功')
    } catch (error) {
      console.error('柱状图初始化失败:', error)
    }
  }

  // Gauge Chart - AI Review Pass Rate
  if (gaugeChartRef.value) {
    console.log('初始化仪表盘...')
    try {
      gaugeChart = echarts.init(gaugeChartRef.value)
      const totalAIReviews = (stats.value.aiValidCount || 0) + (stats.value.aiInvalidCount || 0)
      const passRate = totalAIReviews > 0 ? ((stats.value.aiValidCount || 0) / totalAIReviews * 100).toFixed(1) : 0

      const gaugeOption = {
        series: [{
          name: 'AI评审通过率',
          type: 'gauge',
          detail: { formatter: '{value}%' },
          data: [{
            value: passRate,
            name: '通过率'
          }],
          axisLine: {
            lineStyle: {
              width: 20,
              color: [
                [0.3, '#f39c12'],
                [0.7, '#3498db'],
                [1, '#2ecc71']
              ]
            }
          },
        pointer: {
          width: 5
        },
        title: {
          offsetCenter: [0, '80%']
        },
        detail: {
          offsetCenter: [0, '50%'],
          fontSize: 24,
          fontWeight: 'bold'
        }
      }]
    }
    gaugeChart.setOption(gaugeOption)
    console.log('仪表盘初始化成功，通过率:', passRate + '%')
    } catch (error) {
      console.error('仪表盘初始化失败:', error)
    }
  }

  console.log('所有图表初始化完成')
}
</script>

<style scoped>
.admin-home {
  min-height: 100vh;
}

.container {
  padding-top: var(--spacing-xl);
}

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
  display: flex;
  align-items: center;
  gap: var(--spacing-xl);
}

.academic-badge-header {
  display: flex;
  align-items: center;
  justify-content: center;
  filter: drop-shadow(0 0 8px rgba(52, 152, 219, 0.4));
}

.header-text {
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

.meta-label {
  font-size: var(--text-sm);
  color: var(--color-text-tertiary);
}

.section-header {
  margin-bottom: var(--spacing-xl);
}

.section-title {
  font-family: var(--font-display);
  font-size: var(--text-3xl);
  font-weight: 500;
  color: var(--color-primary-medium);
  letter-spacing: -0.02em;
  margin-bottom: var(--spacing-md);
}

.section-divider {
  height: 2px;
  background: linear-gradient(90deg, var(--color-accent) 0%, transparent 100%);
  margin-top: var(--spacing-md);
}

.stats-section {
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
  font-size: var(--text-5xl);
  font-weight: 700;
  color: var(--color-primary);
  line-height: 1;
  margin-bottom: var(--spacing-sm);
  letter-spacing: -0.04em;
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

.trend-text {
  font-family: var(--font-body);
  font-size: var(--text-sm);
  color: var(--color-text-tertiary);
}

.accent-text {
  color: var(--color-accent);
  font-weight: 600;
}

.actions-section {
  margin-bottom: var(--spacing-3xl);
}

.action-card {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  padding: var(--spacing-xl);
  cursor: pointer;
  text-decoration: none;
  border-left: 4px solid transparent;
  position: relative;
  overflow: hidden;
}

.action-card:hover {
  border-left-color: var(--color-accent);
}

.action-card.card-accent {
  border-left-color: var(--color-accent);
}

.action-icon-wrapper {
  width: 64px;
  height: 64px;
  background: rgba(52, 152, 219, 0.08);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all var(--transition-fast);
}

.action-card:hover .action-icon-wrapper {
  background: rgba(52, 152, 219, 0.12);
}

.action-content {
  flex: 1;
}

.action-meta {
  margin-top: var(--spacing-md);
}

.action-arrow {
  opacity: 0.3;
  transition: all var(--transition-fast);
  flex-shrink: 0;
}

.action-card:hover .action-arrow {
  opacity: 1;
  transform: translateX(4px);
}

.accent-arrow {
  opacity: 0.6;
}

.action-card:hover .accent-arrow {
  opacity: 1;
}

.review-progress {
  position: relative;
  height: 6px;
  background: rgba(127, 140, 141, 0.1);
  border-radius: var(--radius-md);
  margin-bottom: var(--spacing-sm);
  overflow: hidden;
}

.progress-fill {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  border-radius: var(--radius-md);
  transition: width var(--transition-slow);
}

.progress-fill.completed {
  background: var(--status-completed);
}

.progress-text {
  font-family: var(--font-mono);
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
  letter-spacing: 0.05em;
}

.status-section {
  margin-bottom: var(--spacing-2xl);
}

.status-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-lg);
}

.status-card {
  padding: var(--spacing-xl);
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
}

.status-indicator {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 3px solid;
  flex-shrink: 0;
  position: relative;
}

.status-indicator.pending {
  background: transparent;
  border-color: var(--status-pending);
}

.status-indicator.reviewing {
  background: var(--status-reviewing);
  border-color: var(--status-reviewing);
  animation: pulse 2s ease infinite;
}

.status-indicator.completed {
  background: var(--status-completed);
  border-color: var(--status-completed);
}

.status-indicator.awarded {
  background: var(--status-awarded);
  border-color: var(--status-awarded);
}

.status-content {
  flex: 1;
}

.status-title {
  font-family: var(--font-body);
  font-size: var(--text-base);
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-sm);
}

.status-number {
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  font-weight: 600;
  color: var(--color-primary);
  margin-bottom: var(--spacing-xs);
}

.status-desc {
  font-size: var(--text-sm);
  color: var(--color-text-tertiary);
}

/* Charts Section */

.charts-section {
  margin-bottom: var(--spacing-2xl);
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-xl);
}

.chart-card {
  padding: var(--spacing-xl);
}

.chart-title {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  font-weight: 600;
  color: var(--color-primary);
  margin-bottom: var(--spacing-lg);
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.chart-container {
  width: 100%;
  height: 300px;
}

@media (max-width: 1200px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }

  .chart-container {
    height: 250px;
  }
}

@media (max-width: 1200px) {
  .status-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: var(--spacing-lg);
  }

  .header-meta {
    width: 100%;
    justify-content: flex-start;
  }

  .grid-4 {
    grid-template-columns: repeat(2, 1fr);
  }

  .grid-3 {
    grid-template-columns: 1fr;
  }

  .status-cards {
    grid-template-columns: 1fr;
  }

  .stat-number {
    font-size: var(--text-4xl);
  }

  .action-icon-wrapper {
    width: 56px;
    height: 56px;
  }
}

@media (max-width: 480px) {
  .grid-4 {
    grid-template-columns: 1fr;
  }

  .stat-card {
    padding: var(--spacing-lg);
  }

  .action-card {
    flex-direction: column;
    text-align: center;
    gap: var(--spacing-md);
    padding: var(--spacing-lg);
  }

  .action-arrow {
    display: none;
  }
}
</style>