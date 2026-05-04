<template>
  <div class="stats-page">
    <div class="container">
      <!-- Page Header -->
      <div class="page-header slide-in">
        <h1 class="page-title">数据统计</h1>
        <p class="body-text">系统数据分析与可视化</p>
      </div>

      <!-- Overview Stats -->
      <div class="overview-section scale-in mb-xl">
        <h2 class="section-title mb-lg">整体概览</h2>
        <div class="grid grid-4">
          <div class="stat-card card card-flat">
            <div class="stat-icon">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <rect x="5" y="5" width="22" height="22" rx="3" stroke="#5a7fa8" stroke-width="2"/>
                <path d="M10 10H22M10 14H22M10 18H18" stroke="#5a7fa8" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="stat-number">{{ stats.totalCompetitions }}</div>
            <div class="stat-label caption">赛事总数</div>
          </div>

          <div class="stat-card card card-flat">
            <div class="stat-icon">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <path d="M8 8H24V24H8V8Z" stroke="#5a7fa8" stroke-width="2"/>
                <path d="M12 12H20V20H12V12Z" stroke="#5a7fa8" stroke-width="2"/>
              </svg>
            </div>
            <div class="stat-number">{{ stats.totalTracks }}</div>
            <div class="stat-label caption">赛道总数</div>
          </div>

          <div class="stat-card card card-flat">
            <div class="stat-icon">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <circle cx="10" cy="10" r="6" stroke="#5a7fa8" stroke-width="2"/>
                <circle cx="22" cy="10" r="6" stroke="#5a7fa8" stroke-width="2"/>
                <circle cx="16" cy="22" r="6" stroke="#5a7fa8" stroke-width="2"/>
                <path d="M10 16V17C10 18.5 13 20 16 20C19 20 22 18.5 22 17V16" stroke="#5a7fa8" stroke-width="2"/>
              </svg>
            </div>
            <div class="stat-number">{{ stats.totalTeams }}</div>
            <div class="stat-label caption">团队总数</div>
          </div>

          <div class="stat-card card card-flat">
            <div class="stat-icon">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <rect x="6" y="6" width="20" height="20" rx="2" stroke="#5a7fa8" stroke-width="2"/>
                <path d="M10 10H22M10 14H22M10 18H16" stroke="#5a7fa8" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="stat-number">{{ stats.totalWorks }}</div>
            <div class="stat-label caption">作品总数</div>
          </div>
        </div>
      </div>

      <!-- User Stats -->
      <div class="user-section scale-in mb-xl">
        <h2 class="section-title mb-lg">用户分布</h2>
        <div class="grid grid-3">
          <div class="stat-card card card-flat">
            <div class="stat-icon">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <circle cx="16" cy="12" r="8" stroke="#5a7fa8" stroke-width="2"/>
                <path d="M8 24C8 20 12 18 16 18C20 18 24 20 24 24" stroke="#5a7fa8" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="stat-number">{{ stats.studentCount }}</div>
            <div class="stat-label caption">学生用户</div>
          </div>

          <div class="stat-card card card-flat">
            <div class="stat-icon">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <circle cx="16" cy="12" r="8" stroke="#10b981" stroke-width="2"/>
                <path d="M8 24C8 20 12 18 16 18C20 18 24 20 24 24" stroke="#10b981" stroke-width="2" stroke-linecap="round"/>
                <path d="M20 8L24 4M24 4L24 8M24 4L20 4" stroke="#10b981" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="stat-number">{{ stats.judgeCount }}</div>
            <div class="stat-label caption">评委用户</div>
          </div>

          <div class="stat-card card card-flat">
            <div class="stat-icon">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <circle cx="16" cy="12" r="8" stroke="#f59e0b" stroke-width="2"/>
                <path d="M8 24C8 20 12 18 16 18C20 18 24 20 24 24" stroke="#f59e0b" stroke-width="2" stroke-linecap="round"/>
                <circle cx="16" cy="12" r="3" stroke="#f59e0b" stroke-width="2"/>
              </svg>
            </div>
            <div class="stat-number">{{ stats.adminCount }}</div>
            <div class="stat-label caption">管理员用户</div>
          </div>
        </div>
      </div>

      <!-- Review Stats -->
      <div class="review-section scale-in mb-xl">
        <h2 class="section-title mb-lg">评审统计</h2>
        <div class="grid grid-3">
          <div class="stat-card card card-flat">
            <div class="stat-icon">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <circle cx="16" cy="16" r="12" stroke="#5a7fa8" stroke-width="2"/>
                <path d="M12 16L14 18L20 12" stroke="#5a7fa8" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="stat-number">{{ stats.completedReviews }}</div>
            <div class="stat-label caption">已完成评审</div>
          </div>

          <div class="stat-card card card-flat">
            <div class="stat-icon">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <circle cx="16" cy="16" r="12" stroke="#f59e0b" stroke-width="2"/>
                <path d="M16 10V16M16 16L20 20" stroke="#f59e0b" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="stat-number">{{ stats.pendingReviews }}</div>
            <div class="stat-label caption">待评审</div>
          </div>

          <div class="stat-card card card-flat">
            <div class="stat-icon">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <circle cx="16" cy="10" r="7" stroke="#ffd700" stroke-width="2"/>
                <path d="M11 17L8 28L16 24L24 28L21 17" stroke="#ffd700" stroke-width="2" stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="stat-number">{{ stats.awardedCount }}</div>
            <div class="stat-label caption">已设置奖项</div>
          </div>
        </div>
      </div>

      <!-- Summary Stats -->
      <div class="summary-section scale-in mb-xl">
        <h2 class="section-title mb-lg">数据汇总</h2>
        <div class="grid grid-2">
          <div class="stat-card card card-flat">
            <div class="stat-icon">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <path d="M6 8H26M6 14H26M6 20H26M6 26H26" stroke="#5a7fa8" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="stat-number">{{ stats.totalSubmissions }}</div>
            <div class="stat-label caption">提交总数</div>
          </div>

          <div class="stat-card card card-flat">
            <div class="stat-icon">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <circle cx="16" cy="16" r="12" stroke="#5a7fa8" stroke-width="2"/>
                <circle cx="16" cy="16" r="6" stroke="#5a7fa8" stroke-width="2"/>
              </svg>
            </div>
            <div class="stat-number">{{ stats.totalUsers }}</div>
            <div class="stat-label caption">用户总数</div>
          </div>
        </div>
      </div>

      <!-- Data Visualization Charts -->
      <div class="charts-section fade-in">
        <div class="section-header">
          <h2 class="section-title mb-lg">数据可视化分析</h2>
          <div class="section-divider"></div>
        </div>

        <div class="charts-grid">
          <!-- User Distribution Pie Chart -->
          <div class="chart-card card">
            <h3 class="chart-title">👥 用户角色分布</h3>
            <div ref="userPieChartRef" class="chart-container"></div>
          </div>

          <!-- Work Status Bar Chart -->
          <div class="chart-card card">
            <h3 class="chart-title">📊 作品状态统计</h3>
            <div ref="workStatusBarRef" class="chart-container"></div>
          </div>

          <!-- Review Progress Gauge -->
          <div class="chart-card card">
            <h3 class="chart-title">✅ 评审完成率</h3>
            <div ref="reviewGaugeRef" class="chart-container"></div>
          </div>

          <!-- Competition Overview Radar -->
          <div class="chart-card card">
            <h3 class="chart-title">🏆 赛事数据概览</h3>
            <div ref="competitionRadarRef" class="chart-container"></div>
          </div>

          <!-- College Distribution Pie Chart -->
          <div class="chart-card card">
            <h3 class="chart-title">🎓 学院分布统计</h3>
            <div ref="collegePieChartRef" class="chart-container"></div>
          </div>

          <!-- Major Distribution Bar Chart -->
          <div class="chart-card card">
            <h3 class="chart-title">📚 专业分布统计</h3>
            <div ref="majorBarChartRef" class="chart-container"></div>
          </div>

          <!-- Award Level Distribution Pie Chart -->
          <div class="chart-card card">
            <h3 class="chart-title">🏅 奖项等级分布</h3>
            <div ref="awardPieChartRef" class="chart-container"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { get } from '@/utils/api'
import { showWarning, showError } from '@/utils/messageUtils'
import * as echarts from 'echarts'

const router = useRouter()

const stats = ref({
  totalCompetitions: 0,
  totalTracks: 0,
  totalTeams: 0,
  totalWorks: 0,
  totalSubmissions: 0,
  totalUsers: 0,
  studentCount: 0,
  judgeCount: 0,
  adminCount: 0,
  completedReviews: 0,
  pendingReviews: 0,
  awardedCount: 0,
  collegeDistribution: {},
  majorDistribution: {},
  awardLevelDistribution: {}
})

// Chart refs
const userPieChartRef = ref(null)
const workStatusBarRef = ref(null)
const reviewGaugeRef = ref(null)
const competitionRadarRef = ref(null)
const collegePieChartRef = ref(null)
const majorBarChartRef = ref(null)
const awardPieChartRef = ref(null)

// Chart instances
let userPieChart = null
let workStatusBar = null
let reviewGauge = null
let competitionRadar = null
let collegePieChart = null
let majorBarChart = null
let awardPieChart = null

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

  // Initialize charts after data loaded
  await nextTick()
  setTimeout(() => {
    initCharts()
  }, 100)

  // Add window resize listener
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  // Clean up: remove resize listener and dispose charts
  window.removeEventListener('resize', handleResize)
  if (userPieChart) userPieChart.dispose()
  if (workStatusBar) workStatusBar.dispose()
  if (reviewGauge) reviewGauge.dispose()
  if (competitionRadar) competitionRadar.dispose()
  if (collegePieChart) collegePieChart.dispose()
  if (majorBarChart) majorBarChart.dispose()
  if (awardPieChart) awardPieChart.dispose()
})

const handleResize = () => {
  if (userPieChart) userPieChart.resize()
  if (workStatusBar) workStatusBar.resize()
  if (reviewGauge) reviewGauge.resize()
  if (competitionRadar) competitionRadar.resize()
  if (collegePieChart) collegePieChart.resize()
  if (majorBarChart) majorBarChart.resize()
  if (awardPieChart) awardPieChart.resize()
}

const fetchStats = async () => {
  try {
    const data = await get('/admin/system-stats')
    stats.value = data
    console.log('系统统计数据加载成功:', data)
  } catch (error) {
    console.error('获取统计数据失败', error)
    showError('获取统计数据失败')
  }
}

const initCharts = () => {
  console.log('开始初始化图表...')

  // 先销毁已存在的图表实例
  if (userPieChart) {
    userPieChart.dispose()
    userPieChart = null
  }
  if (workStatusBar) {
    workStatusBar.dispose()
    workStatusBar = null
  }
  if (reviewGauge) {
    reviewGauge.dispose()
    reviewGauge = null
  }
  if (competitionRadar) {
    competitionRadar.dispose()
    competitionRadar = null
  }

  // User Distribution Pie Chart
  if (userPieChartRef.value) {
    console.log('初始化用户分布饼图...')
    try {
      userPieChart = echarts.init(userPieChartRef.value)
      const pieOption = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c}人 ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          top: 'center'
        },
        series: [{
          name: '用户角色',
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['60%', '50%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: true,
            formatter: '{b}: {c}'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 16,
              fontWeight: 'bold'
            }
          },
          data: [
            {
              value: stats.value.studentCount || 0,
              name: '学生',
              itemStyle: { color: '#5a7fa8' }
            },
            {
              value: stats.value.judgeCount || 0,
              name: '评委',
              itemStyle: { color: '#10b981' }
            },
            {
              value: stats.value.adminCount || 0,
              name: '管理员',
              itemStyle: { color: '#f59e0b' }
            }
          ]
        }]
      }
      userPieChart.setOption(pieOption)
      console.log('用户分布饼图初始化成功')
    } catch (error) {
      console.error('用户分布饼图初始化失败:', error)
    }
  }

  // Work Status Bar Chart
  if (workStatusBarRef.value) {
    console.log('初始化作品状态柱状图...')
    try {
      workStatusBar = echarts.init(workStatusBarRef.value)
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
              value: stats.value.pendingReviews || 0,
              itemStyle: { color: '#f59e0b' }
            },
            {
              value: stats.value.completedReviews - stats.value.awardedCount || 0,
              itemStyle: { color: '#3498db' }
            },
            {
              value: stats.value.completedReviews || 0,
              itemStyle: { color: '#10b981' }
            },
            {
              value: stats.value.awardedCount || 0,
              itemStyle: { color: '#ffd700' }
            }
          ],
          label: {
            show: true,
            position: 'top'
          }
        }]
      }
      workStatusBar.setOption(barOption)
      console.log('作品状态柱状图初始化成功')
    } catch (error) {
      console.error('作品状态柱状图初始化失败:', error)
    }
  }

  // Review Progress Gauge
  if (reviewGaugeRef.value) {
    console.log('初始化评审完成率仪表盘...')
    try {
      reviewGauge = echarts.init(reviewGaugeRef.value)
      const totalReviews = (stats.value.completedReviews || 0) + (stats.value.pendingReviews || 0)
      const completionRate = totalReviews > 0
        ? ((stats.value.completedReviews || 0) / totalReviews * 100).toFixed(1)
        : 0

      const gaugeOption = {
        series: [{
          name: '评审完成率',
          type: 'gauge',
          progress: {
            show: true,
            width: 18
          },
          axisLine: {
            lineStyle: {
              width: 18
            }
          },
          axisTick: {
            show: false
          },
          splitLine: {
            length: 15,
            lineStyle: {
              width: 2,
              color: '#999'
            }
          },
          axisLabel: {
            distance: 25,
            color: '#999',
            fontSize: 12
          },
          pointer: {
            width: 5
          },
          title: {
            offsetCenter: [0, '80%']
          },
          detail: {
            valueAnimation: true,
            formatter: '{value}%',
            offsetCenter: [0, '50%'],
            fontSize: 24,
            fontWeight: 'bold'
          },
          data: [{
            value: completionRate,
            name: '完成率'
          }],
          itemStyle: {
            color: '#10b981'
          }
        }]
      }
      reviewGauge.setOption(gaugeOption)
      console.log('评审完成率仪表盘初始化成功，完成率:', completionRate + '%')
    } catch (error) {
      console.error('评审完成率仪表盘初始化失败:', error)
    }
  }

  // Competition Overview Radar Chart
  if (competitionRadarRef.value) {
    console.log('初始化赛事数据雷达图...')
    try {
      competitionRadar = echarts.init(competitionRadarRef.value)
      const radarOption = {
        tooltip: {
          trigger: 'item'
        },
        radar: {
          indicator: [
            { name: '赛事', max: Math.max(stats.value.totalCompetitions || 0, 10) },
            { name: '赛道', max: Math.max(stats.value.totalTracks || 0, 20) },
            { name: '团队', max: Math.max(stats.value.totalTeams || 0, 50) },
            { name: '作品', max: Math.max(stats.value.totalWorks || 0, 50) },
            { name: '提交', max: Math.max(stats.value.totalSubmissions || 0, 50) }
          ],
          center: ['50%', '55%'],
          radius: '65%'
        },
        series: [{
          name: '赛事数据',
          type: 'radar',
          data: [{
            value: [
              stats.value.totalCompetitions || 0,
              stats.value.totalTracks || 0,
              stats.value.totalTeams || 0,
              stats.value.totalWorks || 0,
              stats.value.totalSubmissions || 0
            ],
            name: '当前数据',
            areaStyle: {
              color: 'rgba(90, 127, 168, 0.3)'
            },
            lineStyle: {
              color: '#5a7fa8',
              width: 2
            },
            itemStyle: {
              color: '#5a7fa8'
            }
          }]
        }]
      }
      competitionRadar.setOption(radarOption)
      console.log('赛事数据雷达图初始化成功')
    } catch (error) {
      console.error('赛事数据雷达图初始化失败:', error)
    }
  }

  // College Distribution Pie Chart
  if (collegePieChartRef.value) {
    console.log('初始化学院分布饼图...')
    try {
      collegePieChart = echarts.init(collegePieChartRef.value)
      const collegeData = Object.entries(stats.value.collegeDistribution || {}).map(([name, value]) => ({
        name,
        value
      }))

      const collegePieOption = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c}人 ({d}%)'
        },
        legend: {
          type: 'scroll',
          orient: 'vertical',
          right: 10,
          top: 20,
          bottom: 20
        },
        series: [{
          name: '学院分布',
          type: 'pie',
          radius: ['35%', '65%'],
          center: ['40%', '50%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: false
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 14,
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: false
          },
          data: collegeData.length > 0 ? collegeData : [{ value: 0, name: '暂无数据' }]
        }]
      }
      collegePieChart.setOption(collegePieOption)
      console.log('学院分布饼图初始化成功，数据:', collegeData)
    } catch (error) {
      console.error('学院分布饼图初始化失败:', error)
    }
  }

  // Major Distribution Bar Chart
  if (majorBarChartRef.value) {
    console.log('初始化专业分布柱状图...')
    try {
      majorBarChart = echarts.init(majorBarChartRef.value)
      const majorData = Object.entries(stats.value.majorDistribution || {})

      const majorBarOption = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          formatter: '{b}: {c}人'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '15%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: majorData.map(([name]) => name),
          axisLabel: {
            interval: 0,
            rotate: 45,
            fontSize: 11
          }
        },
        yAxis: {
          type: 'value',
          name: '人数'
        },
        series: [{
          name: '专业人数',
          type: 'bar',
          barWidth: '60%',
          data: majorData.map(([name, value]) => ({
            value,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#5a7fa8' },
                { offset: 1, color: '#88b3e0' }
              ])
            }
          })),
          label: {
            show: true,
            position: 'top',
            fontSize: 12
          }
        }]
      }
      majorBarChart.setOption(majorBarOption)
      console.log('专业分布柱状图初始化成功，数据:', majorData)
    } catch (error) {
      console.error('专业分布柱状图初始化失败:', error)
    }
  }

  // Award Level Distribution Pie Chart
  if (awardPieChartRef.value) {
    console.log('初始化奖项等级分布饼图...')
    try {
      awardPieChart = echarts.init(awardPieChartRef.value)
      const awardData = Object.entries(stats.value.awardLevelDistribution || {}).map(([name, value]) => ({
        name,
        value,
        itemStyle: {
          color: name.includes('一等') || name.includes('金奖') ? '#ffd700' :
                 name.includes('二等') || name.includes('银奖') ? '#c0c0c0' :
                 name.includes('三等') || name.includes('铜奖') ? '#cd7f32' :
                 '#5a7fa8'
        }
      }))

      const awardPieOption = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c}个作品 ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          top: 'middle'
        },
        series: [{
          name: '奖项等级',
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['60%', '50%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: true,
            formatter: '{b}\n{c}个'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 16,
              fontWeight: 'bold'
            }
          },
          data: awardData.length > 0 ? awardData : [{ value: 0, name: '暂无奖项数据', itemStyle: { color: '#ccc' } }]
        }]
      }
      awardPieChart.setOption(awardPieOption)
      console.log('奖项等级分布饼图初始化成功，数据:', awardData)
    } catch (error) {
      console.error('奖项等级分布饼图初始化失败:', error)
    }
  }

  console.log('所有图表初始化完成')
}
</script>

<style scoped>
.stats-page {
  min-height: 100vh;
}

.page-header {
  margin-bottom: 32px;
}

.overview-section,
.user-section,
.review-section,
.summary-section {
  margin-bottom: 32px;
}

.section-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.mb-lg {
  margin-bottom: 24px;
}

.mb-xl {
  margin-bottom: 32px;
}

/* Chart Styles */
.charts-section {
  padding: 32px 0;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
}

.section-divider {
  flex: 1;
  height: 2px;
  background: linear-gradient(90deg, var(--color-accent), transparent);
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.chart-card {
  padding: 24px;
  background: linear-gradient(135deg, #FFFFFF, #F5F5F7);
  border-radius: 16px;
  transition: all 0.3s ease;
}

.chart-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.chart-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 20px;
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
</style>