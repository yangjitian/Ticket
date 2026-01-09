<template>
  <div class="admin-home">
    <h2>管理员控制台</h2>

    <div class="stats-grid">
      <el-card class="stat-card clickable" @click="showUserStats">
        <div class="stat-content">
          <div class="stat-icon">
            <el-icon size="32"><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ dashboardStats.totalUsers || 0 }}</div>
            <div class="stat-label">总用户数</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card clickable" @click="showScenicStats">
        <div class="stat-content">
          <div class="stat-icon">
            <el-icon size="32"><Location /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ dashboardStats.totalScenicSpots || 0 }}</div>
            <div class="stat-label">景区数量</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card clickable" @click="showDistributorStats">
        <div class="stat-content">
          <div class="stat-icon">
            <el-icon size="32"><Shop /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ dashboardStats.totalDistributors || 0 }}</div>
            <div class="stat-label">经销商数量</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card clickable" @click="showRevenueStats">
        <div class="stat-content">
          <div class="stat-icon">
            <el-icon size="32"><Money /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">¥{{ formatCurrency(dashboardStats.totalRevenue) }}</div>
            <div class="stat-label">景区收入总额</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 详细信息展示区域 -->
    <div v-if="showDetails && activeDetailType" class="details-section">
      <!-- 用户统计详情 -->
      <div v-if="activeDetailType === 'users'" class="detail-content">
        <div class="detail-header">
          <h3>用户统计详情</h3>
          <div class="detail-controls">
            <el-select v-model="userStatsDays" placeholder="选择时间范围" size="small" @change="loadUserStats">
              <el-option label="最近7天" :value="7" />
              <el-option label="最近14天" :value="14" />
              <el-option label="最近30天" :value="30" />
            </el-select>
          </div>
        </div>
        <div class="user-stats-cards">
          <div class="user-stat-card">
            <div class="stat-value">{{ userStats.totalUsers || 0 }}</div>
            <div class="stat-label">总用户数</div>
          </div>
          <div class="user-stat-card">
            <div class="stat-value">{{ userStats.todayUsers || 0 }}</div>
            <div class="stat-label">今日注册</div>
          </div>
          <div class="user-stat-card">
            <div class="stat-value">{{ userStats.weekUsers || 0 }}</div>
            <div class="stat-label">本周注册</div>
          </div>
          <div class="user-stat-card">
            <div class="stat-value">{{ userStats.monthUsers || 0 }}</div>
            <div class="stat-label">本月注册</div>
          </div>
        </div>
        <div class="chart-container">
          <div ref="userChartRef" class="chart"></div>
        </div>
      </div>

      <!-- 景区统计详情 -->
      <div v-if="activeDetailType === 'scenics'" class="detail-content">
        <div class="detail-header">
          <h3>景区统计详情</h3>
        </div>
        <div class="scenic-stats-content">
          <div class="scenic-stat-item">
            <div class="scenic-stat-value">{{ dashboardStats.totalScenicSpots || 0 }}</div>
            <div class="scenic-stat-label">总景区数量</div>
          </div>
          <div class="scenic-stat-item">
            <div class="scenic-stat-value">{{ scenicStats.activeScenics || 0 }}</div>
            <div class="scenic-stat-label">开放中景区</div>
          </div>
          <div class="scenic-stat-item">
            <div class="scenic-stat-value">{{ scenicStats.closedScenics || 0 }}</div>
            <div class="scenic-stat-label">关闭中景区</div>
          </div>
        </div>
        <div class="scenic-list">
          <h4>景区列表</h4>
          <el-table :data="scenicList" stripe style="width: 100%">
            <el-table-column prop="spotName" label="景区名称" min-width="200" />
            <el-table-column prop="level" label="等级" width="100" />
            <el-table-column prop="province" label="省份" width="120" />
            <el-table-column label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.spotStatus === 1 ? 'success' : 'danger'">
                  {{ scope.row.spotStatus === 1 ? '开放' : '关闭' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
          </el-table>
        </div>
      </div>

      <!-- 经销商统计详情 -->
      <div v-if="activeDetailType === 'distributors'" class="detail-content">
        <div class="detail-header">
          <h3>经销商统计详情</h3>
        </div>
        <div class="distributor-stats-content">
          <div class="distributor-stat-item">
            <div class="distributor-stat-value">{{ dashboardStats.totalDistributors || 0 }}</div>
            <div class="distributor-stat-label">总经销商数量</div>
          </div>
          <div class="distributor-stat-item">
            <div class="distributor-stat-value">{{ dashboardStats.approvedDistributors || 0 }}</div>
            <div class="distributor-stat-label">已通过经销商</div>
          </div>
          <div class="distributor-stat-item">
            <div class="distributor-stat-value">{{ dashboardStats.pendingDistributors || 0 }}</div>
            <div class="distributor-stat-label">待审核经销商</div>
          </div>
        </div>
        <div class="distributor-list">
          <h4>经销商列表</h4>
          <el-table :data="distributorList" stripe style="width: 100%">
            <el-table-column prop="userAccount" label="邮箱账号" min-width="200" />
            <el-table-column prop="nickname" label="昵称" min-width="120" />
            <el-table-column label="审核状态" width="100">
              <template #default="scope">
                <el-tag :type="getAuditStatusType(scope.row.auditStatus)">
                  {{ getAuditStatusText(scope.row.auditStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="佣金" width="120">
              <template #default="scope">
                ¥{{ scope.row.commission || '0.00' }}
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="注册时间" width="180" />
          </el-table>
        </div>
      </div>

      <!-- 收入统计详情 -->
      <div v-if="activeDetailType === 'revenue'" class="detail-content">
        <div class="detail-header">
          <h3>收入统计详情</h3>
          <div class="detail-controls">
            <el-select v-model="revenueStatsDays" placeholder="选择时间范围" size="small" @change="loadRevenueStats">
              <el-option label="最近7天" :value="7" />
              <el-option label="最近14天" :value="14" />
              <el-option label="最近30天" :value="30" />
            </el-select>
          </div>
        </div>

        <!-- 统计卡片 -->
        <div class="revenue-stats-cards">
          <div class="revenue-stat-card">
            <div class="stat-value">{{ revenueStats.totalOrders || 0 }}</div>
            <div class="stat-label">总订单数</div>
          </div>
          <div class="revenue-stat-card">
            <div class="stat-value">¥{{ formatCurrency(revenueStats.totalRevenue) }}</div>
            <div class="stat-label">总收入</div>
          </div>
          <div class="revenue-stat-card">
            <div class="stat-value">¥{{ revenueStats.avgOrderValue || '0.00' }}</div>
            <div class="stat-label">平均客单价</div>
          </div>
          <div class="revenue-stat-card">
            <div class="stat-value">{{ revenueStats.activeScenics || 0 }}</div>
            <div class="stat-label">活跃景区数</div>
          </div>
        </div>

        <!-- 收入趋势图表 -->
        <div class="revenue-chart-section">
          <div class="dual-chart-container">
            <div class="chart-left">
              <h4>每日订单数趋势</h4>
              <div ref="orderCountChartRef" class="chart"></div>
            </div>
            <div class="chart-right">
              <h4>每日收入趋势</h4>
              <div ref="revenueChartRef" class="chart"></div>
            </div>
          </div>
        </div>

        <!-- 各景区营收情况 -->
        <div class="scenic-revenue-section">
          <h4>各景区营收情况</h4>
          <el-table :data="revenueStats.scenicRevenues" stripe style="width: 100%">
            <el-table-column prop="scenicName" label="景区名称" min-width="200" />
            <el-table-column label="总收入" width="120">
              <template #default="scope">
                ¥{{ formatCurrency(scope.row.totalRevenue) }}
              </template>
            </el-table-column>
            <el-table-column prop="orderCount" label="订单数量" width="100" />
            <el-table-column label="占比" width="100">
              <template #default="scope">
                {{ scope.row.percentage }}%
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <!-- 默认欢迎信息 -->
    <div v-if="!showDetails" class="welcome-message">
      <el-card>
        <h3>欢迎使用景点门票分销管理系统</h3>
        <p>请从左侧菜单选择要管理的功能模块。</p>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { User, Location, Shop, Tickets, Money } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import type { DashboardStatsVO, RevenueStatsVO, UserStatsVO, DistributorVO, ScenicSpot } from '@/types/api'
import { getDashboardStats, getRevenueStats, getUserStats, listDistributors, getScenicSpotList } from '@/admin/api'

// 响应式数据
const dashboardStats = ref<DashboardStatsVO>({
  totalUsers: '0',
  totalScenicSpots: '0',
  totalDistributors: '0',
  totalRevenue: '0',
  todayNewUsers: '0',
  todayRevenue: '0',
  weekNewUsers: '0',
  weekRevenue: '0',
  monthNewUsers: '0',
  monthRevenue: '0'
})

const showDetails = ref(false)
const activeDetailType = ref<string>('')

// 用户统计相关
const userStats = ref<UserStatsVO>({
  dates: [],
  registerCounts: [],
  totalUsers: 0,
  todayUsers: 0,
  weekUsers: 0,
  monthUsers: 0
})
const userStatsDays = ref(7)
const userChartRef = ref<HTMLElement>()

// 景区统计相关
const scenicStats = ref({
  activeScenics: 0,
  closedScenics: 0
})
const scenicList = ref<ScenicSpot[]>([])

// 经销商统计相关（已移到dashboardStats中）
const distributorList = ref<DistributorVO[]>([])

// 收入统计相关
const revenueStats = ref<RevenueStatsVO & {
  totalOrders: string
  totalRevenue: string
  avgOrderValue: string
  activeScenics: string
}>({
  totalOrders: '0',
  totalRevenue: '0',
  avgOrderValue: '0.00',
  activeScenics: '0',
  dateOrderCounts: [],
  dateRevenues: [],
  scenicRevenues: []
})
const revenueStatsDays = ref(7)
const orderCountChartRef = ref<HTMLElement>()
const revenueChartRef = ref<HTMLElement>()

// 方法
const loadDashboardStats = async () => {
  try {
    const response = await getDashboardStats()
    if (response.data.code === 200) {
      dashboardStats.value = response.data.data
    } else {
      ElMessage.error(response.data.message || '获取统计数据失败')
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
  }
}

const formatCurrency = (amount: string | number) => {
  const num = typeof amount === 'string' ? parseFloat(amount) : amount
  return isNaN(num) ? '0.00' : num.toFixed(2)
}

const showUserStats = async () => {
  activeDetailType.value = 'users'
  showDetails.value = true
  await loadUserStats()
}

const showScenicStats = async () => {
  activeDetailType.value = 'scenics'
  showDetails.value = true
  await loadScenicStats()
}

const showDistributorStats = async () => {
  activeDetailType.value = 'distributors'
  showDetails.value = true
  await loadDistributorStats()
}

const showRevenueStats = async () => {
  activeDetailType.value = 'revenue'
  showDetails.value = true
  await loadRevenueStats()
}

const loadUserStats = async () => {
  try {
    const params = { days: userStatsDays.value }
    const response = await getUserStats(params)
    if (response.data.code === 200) {
      userStats.value = response.data.data
      await nextTick()
      renderUserChart()
    } else {
      ElMessage.error(response.data.message || '获取用户统计失败')
    }
  } catch (error) {
    console.error('加载用户统计失败:', error)
    ElMessage.error('加载用户统计失败')
  }
}

const loadScenicStats = async () => {
  try {
    const response = await getScenicSpotList({})
    if (response.data.code === 200) {
      scenicList.value = response.data.data.records
      scenicStats.value.activeScenics = scenicList.value.filter(s => s.spotStatus === 1).length
      scenicStats.value.closedScenics = scenicList.value.filter(s => s.spotStatus === 0).length
    } else {
      ElMessage.error(response.data.message || '获取景区数据失败')
    }
  } catch (error) {
    console.error('加载景区统计失败:', error)
    ElMessage.error('加载景区统计失败')
  }
}

const loadDistributorStats = async () => {
  try {
    const response = await listDistributors({})
    if (response.data.code === 200) {
      distributorList.value = response.data.data.records
    } else {
      ElMessage.error(response.data.message || '获取经销商数据失败')
    }
  } catch (error) {
    console.error('加载经销商统计失败:', error)
    ElMessage.error('加载经销商统计失败')
  }
}

const loadRevenueStats = async () => {
  try {
    const params = { days: revenueStatsDays.value }
    const response = await getRevenueStats(params)
    if (response.data.code === 200) {
      const data = response.data.data

      // 计算统计数据
      const totalOrders = data.dateOrderCounts?.reduce((sum, item) => sum + parseInt(item.orderCount || '0'), 0) || 0
      const totalRevenue = data.dateRevenues?.reduce((sum, item) => sum + parseFloat(item.revenue || '0'), 0) || 0
      const avgOrderValue = totalOrders > 0 ? (totalRevenue / totalOrders).toFixed(2) : '0.00'
      const activeScenics = data.scenicRevenues?.length || 0

      // 确保图表数据为数字类型
      const processedData = {
        ...data,
        dateOrderCounts: data.dateOrderCounts?.map(item => ({
          ...item,
          orderCount: parseInt(item.orderCount || '0')
        })) || [],
        dateRevenues: data.dateRevenues?.map(item => ({
          ...item,
          revenue: parseFloat(item.revenue || '0')
        })) || []
      }

      revenueStats.value = {
        ...processedData,
        totalOrders: String(totalOrders),
        totalRevenue: String(totalRevenue),
        avgOrderValue: avgOrderValue,
        activeScenics: String(activeScenics)
      }

      await nextTick()
      renderRevenueCharts()
    } else {
      ElMessage.error(response.data.message || '获取收入统计失败')
    }
  } catch (error) {
    console.error('加载收入统计失败:', error)
    ElMessage.error('加载收入统计失败')
  }
}

const renderUserChart = () => {
  if (!userChartRef.value || !userStats.value.dates?.length) return

  try {
    echarts.dispose(userChartRef.value)
    const chart = echarts.init(userChartRef.value)

    const option = {
      title: {
        text: '用户注册趋势',
        left: 'center',
        textStyle: { fontSize: 16, fontWeight: 'normal', color: '#1d2129' }
      },
      tooltip: {
        trigger: 'axis',
        formatter: (params: any) => {
          const param = params[0]
          return `${param.name}<br/>注册人数: ${param.value}`
        }
      },
      grid: { left: '3%', right: '4%', bottom: '10%', containLabel: true },
      xAxis: {
        type: 'category',
        data: userStats.value.dates,
        axisLabel: { rotate: 45, color: '#86909c', fontSize: 12 }
      },
      yAxis: {
        type: 'value',
        name: '注册人数',
        nameTextStyle: { color: '#86909c' },
        axisLabel: { color: '#86909c', fontSize: 12 }
      },
      series: [{
        name: '注册人数',
        type: 'bar',
        data: userStats.value.registerCounts,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#1890ff' },
            { offset: 1, color: '#40a9ff' }
          ])
        },
        barWidth: '60%'
      }]
    }

    chart.setOption(option)
  } catch (error) {
    console.error('渲染用户图表失败:', error)
  }
}

const renderRevenueCharts = () => {

  // 渲染订单数图表
  if (orderCountChartRef.value && revenueStats.value.dateOrderCounts?.length) {
    try {
      echarts.dispose(orderCountChartRef.value)
      const chart = echarts.init(orderCountChartRef.value)

      const option = {
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          borderColor: '#e5e6eb',
          textStyle: {
            color: '#1d2129',
            fontSize: 12
          },
          formatter: (params: any) => {
            const param = params[0]
            return `<div style="font-weight: 500; margin-bottom: 4px;">${param.name}</div>
                    <div style="display: flex; align-items: center;">
                      <div style="width: 8px; height: 8px; background: #52c41a; border-radius: 50%; margin-right: 6px;"></div>
                      <span>订单数: ${param.value}</span>
                    </div>`
          }
        },
        grid: {
          left: '8%',
          right: '8%',
          bottom: '15%',
          top: '15%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: revenueStats.value.dateOrderCounts.map(item => item.date),
          axisLabel: {
            rotate: 45,
            color: '#86909c',
            fontSize: 11,
            interval: 0
          },
          axisLine: {
            lineStyle: {
              color: '#e5e6eb'
            }
          },
          axisTick: {
            show: false
          }
        },
        yAxis: {
          type: 'value',
          name: '订单数量',
          nameTextStyle: {
            color: '#86909c',
            fontSize: 12
          },
          axisLabel: {
            color: '#86909c',
            fontSize: 11
          },
          axisLine: {
            show: false
          },
          axisTick: {
            show: false
          },
          splitLine: {
            lineStyle: {
              color: '#f0f0f0',
              type: 'dashed'
            }
          }
        },
        series: [{
          name: '订单数',
          type: 'line',
          data: revenueStats.value.dateOrderCounts.map(item => parseInt(item.orderCount || '0')),
          smooth: true,
          symbol: 'circle',
          symbolSize: 6,
          lineStyle: {
            width: 3,
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: '#52c41a' },
              { offset: 1, color: '#36cfc9' }
            ])
          },
          itemStyle: {
            color: '#52c41a',
            borderColor: '#fff',
            borderWidth: 2,
            shadowColor: 'rgba(82, 196, 26, 0.3)',
            shadowBlur: 4
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(82, 196, 26, 0.1)' },
              { offset: 1, color: 'rgba(54, 207, 201, 0.02)' }
            ])
          },
          emphasis: {
            itemStyle: {
              borderColor: '#52c41a',
              borderWidth: 3,
              shadowColor: 'rgba(82, 196, 26, 0.5)',
              shadowBlur: 8
            }
          }
        }]
      }

      chart.setOption(option)

      // 添加响应式调整
      const resizeObserver = new ResizeObserver(() => {
        chart.resize()
      })
      resizeObserver.observe(orderCountChartRef.value)

      // 保持响应式调整，不在此处返回清理函数（避免提前结束渲染流程）
    } catch (error) {
      console.error('渲染订单数图表失败:', error)
    }
  }

  // 渲染收入图表
  console.log('检查收入图表渲染条件:', {
    hasRef: !!revenueChartRef.value,
    hasData: revenueStats.value.dateRevenues?.length > 0,
    dataLength: revenueStats.value.dateRevenues?.length,
    firstItem: revenueStats.value.dateRevenues?.[0]
  })

  if (revenueChartRef.value && revenueStats.value.dateRevenues?.length) {
    try {
      console.log('开始渲染收入图表，数据:', revenueStats.value.dateRevenues)
      echarts.dispose(revenueChartRef.value)
      const chart = echarts.init(revenueChartRef.value)

      const option = {
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(0, 0, 0, 0.8)',
          borderColor: 'transparent',
          textStyle: {
            color: '#fff',
            fontSize: 13
          },
          formatter: (params: any) => {
            const param = params[0]
            return `${param.name}<br/><span style="color:#faad14;">●</span> 收入: ¥${formatCurrency(param.value)}`
          }
        },
        grid: { left: '8%', right: '8%', bottom: '15%', top: '15%', containLabel: true },
        xAxis: {
          type: 'category',
          data: revenueStats.value.dateRevenues.map(item => item.date),
          axisLabel: {
            rotate: 45,
            color: '#86909c',
            fontSize: 11,
            interval: 0
          },
          axisLine: {
            lineStyle: {
              color: '#e5e6eb'
            }
          },
          axisTick: {
            show: false
          }
        },
        yAxis: {
          type: 'value',
          name: '收入金额(元)',
          nameTextStyle: {
            color: '#86909c',
            fontSize: 12
          },
          axisLabel: {
            color: '#86909c',
            fontSize: 11,
            formatter: (value: number) => {
              if (value >= 10000) {
                return (value / 10000).toFixed(1) + '万'
              } else if (value >= 1000) {
                return (value / 1000).toFixed(1) + 'k'
              }
              return value.toString()
            }
          },
          axisLine: {
            show: false
          },
          axisTick: {
            show: false
          },
          splitLine: {
            lineStyle: {
              color: '#f0f0f0',
              type: 'dashed'
            }
          },
          min: 0 // 确保Y轴从0开始
        },
        series: [{
          name: '收入',
          type: 'bar',
          data: revenueStats.value.dateRevenues.map(item => {
            const value = parseFloat(item.revenue || '0')
            console.log('收入数据转换:', item.revenue, '->', value)
            return value
          }),
          barWidth: '70%',
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#faad14' },
              { offset: 1, color: '#fa8c16' }
            ]),
            borderRadius: [4, 4, 0, 0],
            shadowColor: 'rgba(250, 173, 20, 0.3)',
            shadowBlur: 8,
            shadowOffsetY: 2
          },
          emphasis: {
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#ff7a45' },
                { offset: 1, color: '#fa8c16' }
              ]),
              shadowColor: 'rgba(250, 173, 20, 0.5)',
              shadowBlur: 12,
              shadowOffsetY: 4
            }
          },
          label: {
            show: true,
            position: 'top',
            color: '#fa8c16',
            fontSize: 10,
            fontWeight: 'bold',
            formatter: (params: any) => {
              const value = parseFloat(params.value)
              if (value >= 10000) {
                return (value / 10000).toFixed(1) + '万'
              } else if (value >= 1000) {
                return (value / 1000).toFixed(1) + 'k'
              }
              return formatCurrency(params.value)
            }
          }
        }]
      }

      chart.setOption(option)

      // 添加响应式调整
      const resizeObserver = new ResizeObserver(() => {
        chart.resize()
      })
      resizeObserver.observe(revenueChartRef.value)

      // 保持响应式调整，不在此处返回清理函数（避免提前结束渲染流程）
    } catch (error) {
      console.error('渲染收入图表失败:', error)
    }
  }
}

const getAuditStatusType = (status: number) => {
  switch (status) {
    case 1: return 'success'
    case 2: return 'danger'
    default: return 'warning'
  }
}

const getAuditStatusText = (status: number) => {
  switch (status) {
    case 0: return '待审核'
    case 1: return '通过'
    case 2: return '拒绝'
    default: return '未知'
  }
}

// 生命周期
onMounted(() => {
  loadDashboardStats()
})
</script>

<style scoped>
.admin-home {
  padding: 20px;
}

.admin-home h2 {
  margin-bottom: 30px;
  color: #333;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.stat-card.clickable:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  color: #409eff;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

.details-section {
  margin-top: 30px;
}

.detail-content {
  background: white;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.detail-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1d2129;
}

.detail-controls {
  display: flex;
  align-items: center;
}

/* 用户统计样式 */
.user-stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.user-stat-card {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  border: 1px solid #e5e6eb;
}

.user-stat-card .stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1890ff;
  margin-bottom: 8px;
}

.user-stat-card .stat-label {
  font-size: 14px;
  color: #86909c;
  font-weight: 500;
}

.chart-container {
  height: 400px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e5e6eb;
  padding: 20px;
}

.chart {
  width: 100%;
  height: 100%;
}

/* 景区统计样式 */
.scenic-stats-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.scenic-stat-item {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  border: 1px solid #e5e6eb;
}

.scenic-stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #52c41a;
  margin-bottom: 8px;
}

.scenic-stat-label {
  font-size: 14px;
  color: #86909c;
  font-weight: 500;
}

.scenic-list h4 {
  margin-bottom: 16px;
  color: #1d2129;
  font-size: 16px;
  font-weight: 600;
}

/* 经销商统计样式 */
.distributor-stats-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.distributor-stat-item {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  border: 1px solid #e5e6eb;
}

.distributor-stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #722ed1;
  margin-bottom: 8px;
}

.distributor-stat-label {
  font-size: 14px;
  color: #86909c;
  font-weight: 500;
}

.distributor-list h4 {
  margin-bottom: 16px;
  color: #1d2129;
  font-size: 16px;
  font-weight: 600;
}

/* 收入统计详情样式 */
.revenue-stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.revenue-stat-card {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  border: 1px solid #e5e6eb;
}

.revenue-stat-card .stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #fa8c16;
  margin-bottom: 8px;
}

.revenue-stat-card .stat-label {
  font-size: 14px;
  color: #86909c;
  font-weight: 500;
}

.revenue-chart-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.dual-chart-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
  align-items: start;
}

.chart-left h4,
.chart-right h4 {
  margin-bottom: 16px;
  color: #1d2129;
  font-size: 16px;
  font-weight: 600;
  text-align: center;
}

.dual-chart-container .chart {
  height: 400px;
  width: 100%;
}

.scenic-revenue-section h4 {
  margin-bottom: 16px;
  color: #1d2129;
  font-size: 16px;
  font-weight: 600;
}

.welcome-message {
  margin-top: 30px;
}

.welcome-message h3 {
  margin-bottom: 10px;
  color: #333;
}

.welcome-message p {
  color: #666;
  line-height: 1.6;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .admin-home {
    padding: 16px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .revenue-stats-cards {
    grid-template-columns: 1fr;
  }

  .dual-chart-container {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .dual-chart-container .chart {
    height: 350px;
  }

  .user-stats-cards,
  .scenic-stats-content,
  .distributor-stats-content {
    grid-template-columns: 1fr;
  }

  .detail-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
}
</style>
