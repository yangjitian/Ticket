<template>
  <div class="distributor-layout">
    <!-- 侧边导航栏 -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <h3>经销管理</h3>
      </div>
      <nav class="sidebar-nav">
        <div
          v-for="item in menuItems"
          :key="item.key"
          :class="['nav-item', { active: activePage === item.key }]"
          @click="switchPage(item.key)"
        >
          <el-icon :size="20">
            <component :is="item.icon" />
          </el-icon>
          <span class="nav-text">{{ item.label }}</span>
        </div>
      </nav>
    </aside>

    <!-- 右侧内容区域 -->
    <div class="main-area">
      <!-- 顶部状态栏 -->
      <header class="top-header">
        <div class="header-content">
          <div class="user-info">
            <span class="welcome-text">欢迎您，{{ userInfo.nickname }}</span>
            <el-tag
              :type="userInfo.auditStatus === '1' ? 'success' : userInfo.auditStatus === '0' ? 'warning' : 'danger'"
              size="small"
            >
              {{ userInfo.auditStatus === '1' ? '已认证经销商' : userInfo.auditStatus === '0' ? '审核中' : '审核失败' }}
            </el-tag>
          </div>
          <el-button type="text" @click="handleLogout" class="logout-btn">
            <el-icon><SwitchButton /></el-icon>
            退出登录
          </el-button>
        </div>
      </header>

      <!-- 主体内容区 -->
      <main class="content-area">
        <!-- 经营概览页 -->
        <div v-if="activePage === 'dashboard'" class="page-dashboard">
          <!-- 统计卡片区 -->
          <div class="stats-cards">
            <div class="stat-card">
              <div class="card-icon sales-icon">
                <el-icon size="32"><Money /></el-icon>
              </div>
              <div class="card-content">
                <div class="card-title">累计销售额</div>
                <div class="card-value">{{ formatCurrency(statsData.totalSales) }}</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="card-icon orders-icon">
                <el-icon size="32"><Tickets /></el-icon>
              </div>
              <div class="card-content">
                <div class="card-title">成交订单总数</div>
                <div class="card-value">{{ statsData.totalOrders }}</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="card-icon profit-icon">
                <el-icon size="32"><TrendCharts /></el-icon>
              </div>
              <div class="card-content">
                <div class="card-title">累计利润</div>
                <div class="card-value profit-value">{{ formatCurrency(statsData.totalProfit) }}</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="card-icon balance-icon">
                <el-icon size="32"><Wallet /></el-icon>
              </div>
              <div class="card-content">
                <div class="card-title">可提现余额</div>
                <div class="card-value balance-value">{{ formatCurrency(statsData.availableBalance) }}</div>
              </div>
            </div>
          </div>

          <!-- 趋势分析区 -->
          <div class="analysis-section">
            <div class="chart-area">
              <div class="chart-header">
                <h3>近15天佣金收益趋势</h3>
              </div>
              <div ref="commissionChartRef" class="commission-chart"></div>
            </div>
            <div class="top-spots">
              <h3>热销景区TOP 3</h3>
              <div class="spots-list">
                <div
                  v-for="(spot, index) in topSpots"
                  :key="spot.id"
                  class="spot-item"
                >
                  <div class="spot-rank">{{ index + 1 }}</div>
                  <el-avatar :src="spot.image" size="60" shape="square" class="spot-image" />
                  <div class="spot-info">
                    <div class="spot-name">{{ spot.name }}</div>
                    <div class="spot-sales">{{ spot.salesCount }}单</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 门票分销页 -->
        <div v-if="activePage === 'pricing'" class="page-pricing">
          <!-- 搜索过滤区 -->
          <div class="filter-bar">
            <el-input
              v-model="ticketSearch"
              placeholder="搜索景区名称"
              clearable
              style="width: 200px"
              @input="filterTickets"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-select
              v-model="ticketTypeFilter"
              placeholder="票种类型"
              clearable
              style="width: 120px; margin-left: 12px"
              @change="filterTickets"
            >
              <el-option label="全部" value="" />
              <el-option label="成人票" value="成人票" />
              <el-option label="儿童票" value="儿童票" />
              <el-option label="学生票" value="学生票" />
              <el-option label="老人票" value="老人票" />
            </el-select>
            <el-button @click="resetFilters" style="margin-left: 12px">一键重置</el-button>
          </div>

          <!-- 门票定价表格 -->
          <div class="pricing-table">
            <el-table :data="filteredTickets" stripe style="width: 100%">
              <el-table-column label="景区信息" min-width="180">
                <template #default="scope">
                  <div class="spot-info-cell">
                    <el-avatar :src="scope.row.spotImage" size="50" shape="square" />
                    <div class="spot-details">
                      <div class="spot-name">{{ scope.row.spotName }}</div>
                      <div class="spot-level">{{ scope.row.spotLevel }}</div>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="ticketType" label="票种名称" width="120" align="center" />
              <el-table-column label="价格参考" width="140" align="center">
                <template #default="scope">
                  <div class="price-reference">
                    <div class="reference-item">
                      <span class="label">零售价：</span>
                      <span class="value">¥{{ scope.row.retailPrice }}</span>
                    </div>
                    <div class="reference-item">
                      <span class="label">结算价：</span>
                      <span class="value">¥{{ scope.row.settlementPrice }}</span>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="我的售价" width="160" align="center">
                <template #default="scope">
                  <div class="price-input-wrapper">
                    <el-input-number
                      v-model="scope.row.myPrice"
                      :min="scope.row.settlementPrice"
                      :precision="2"
                      controls-position="right"
                      size="small"
                      :class="{ 'price-error': scope.row.myPrice < scope.row.settlementPrice }"
                      @change="calculateProfit(scope.row)"
                    >
                      <template #prefix>¥</template>
                    </el-input-number>
                    <div v-if="scope.row.myPrice < scope.row.settlementPrice" class="price-error-text">
                      售价不得低于结算价
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="预计利润" width="120" align="center">
                <template #default="scope">
                  <span class="profit-text" :class="{ 'profit-zero': scope.row.profit <= 0 }">
                    ¥{{ scope.row.profit.toFixed(2) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" align="center">
                <template #default="scope">
                  <el-button
                    type="primary"
                    size="small"
                    :disabled="scope.row.myPrice < scope.row.settlementPrice"
                    @click="savePricing(scope.row)"
                  >
                    保存定价
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <!-- 销售订单页 -->
        <div v-if="activePage === 'orders'" class="page-orders">
          <div class="orders-list">
            <div
              v-for="order in salesOrders"
              :key="order.id"
              class="order-item"
            >
              <div class="order-left">
                <div class="order-number">{{ order.orderNumber }}</div>
                <div class="order-time">{{ order.createTime }}</div>
              </div>
              <div class="order-middle">
                <div class="spot-name">{{ order.spotName }}</div>
                <div class="play-date">游玩日期：{{ order.playDate }}</div>
              </div>
              <div class="order-right">
                <div class="order-amount">¥{{ order.totalAmount }}</div>
                <div class="commission-amount">预计分成：¥{{ order.commission }}</div>
                <el-tag
                  :type="order.status === '已完成' ? 'success' :
                        order.status === '待支付' ? 'warning' :
                        order.status === '已退款' ? 'danger' : 'info'"
                  size="small"
                >
                  {{ order.status }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>

        <!-- 财务钱包页 -->
        <div v-if="activePage === 'finance'" class="page-finance">
          <!-- 钱包状态区 -->
          <div class="wallet-section">
            <div class="wallet-card">
              <div class="wallet-header">
                <h3>我的钱包</h3>
                <div class="wallet-balance">
                  <div class="balance-label">可提现余额</div>
                  <div class="balance-amount">{{ formatCurrency(statsData.availableBalance) }}</div>
                </div>
              </div>
              <div class="wallet-actions">
                <el-button
                  type="primary"
                  size="large"
                  @click="showWithdrawDialog"
                  :disabled="statsData.availableBalance < 100"
                >
                  申请提现
                </el-button>
                <div class="withdraw-tip">提现金额需满100元，预计1个工作日到账</div>
              </div>
            </div>
          </div>

          <!-- 账单流水区 -->
          <div class="bills-section">
            <div class="bills-header">
              <h3>账单流水</h3>
              <el-select v-model="billMonth" placeholder="选择月份" size="small" @change="filterBillsByMonth">
                <el-option label="全部" value="" />
                <el-option label="2024-12" value="2024-12" />
                <el-option label="2024-11" value="2024-11" />
                <el-option label="2024-10" value="2024-10" />
              </el-select>
            </div>
            <div class="bills-list">
              <div
                v-for="bill in filteredBills"
                :key="bill.id"
                class="bill-item"
              >
                <div class="bill-time">{{ bill.time }}</div>
                <div class="bill-content">
                  <div class="bill-type">{{ bill.type }}</div>
                  <div class="bill-desc">{{ bill.description }}</div>
                </div>
                <div class="bill-amount">
                  <span :class="bill.amount > 0 ? 'amount-income' : 'amount-expense'">
                    {{ bill.amount > 0 ? '+' : '' }}{{ formatCurrency(Math.abs(bill.amount)) }}
                  </span>
                </div>
                <div class="bill-balance">余额：{{ formatCurrency(bill.balance) }}</div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>

    <!-- 提现申请对话框 -->
    <el-dialog
      v-model="withdrawDialogVisible"
      title="申请提现"
      width="400px"
    >
      <el-form :model="withdrawForm" :rules="withdrawRules" ref="withdrawFormRef" label-width="80px">
        <el-form-item label="提现金额" prop="amount">
          <el-input-number
            v-model="withdrawForm.amount"
            :min="100"
            :max="statsData.availableBalance"
            :precision="2"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
        <div class="withdraw-info">
          <p>可提现余额：{{ formatCurrency(statsData.availableBalance) }}</p>
          <p>实际到账：{{ formatCurrency(withdrawForm.actualAmount) }}</p>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="withdrawDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitWithdrawForm">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  HomeFilled, PriceTag, List, Wallet,
  Search, Money, Tickets, TrendCharts,
  SwitchButton
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'

// Router
const router = useRouter()

// Types
interface MenuItem {
  key: string
  label: string
  icon: any
}

interface Ticket {
  id: string
  spotName: string
  spotImage: string
  spotLevel: string
  ticketType: string
  retailPrice: number
  settlementPrice: number
  myPrice: number
  profit: number
}

interface Order {
  id: string
  orderNumber: string
  spotName: string
  playDate: string
  totalAmount: number
  commission: number
  createTime: string
  status: string
}

interface Bill {
  id: string
  time: string
  type: string
  description: string
  amount: number
  balance: number
}

// Reactive data
const activePage = ref('dashboard')
const commissionChartRef = ref<HTMLElement>()

// User info (mock data - should come from login state)
const userInfo = reactive({
  nickname: '张三经销商',
  auditStatus: '1' // 1: 已认证, 0: 审核中, 2: 审核失败
})

// Menu items
const menuItems: MenuItem[] = [
  { key: 'dashboard', label: '经营概览', icon: HomeFilled },
  { key: 'pricing', label: '门票分销', icon: PriceTag },
  { key: 'orders', label: '销售订单', icon: List },
  { key: 'finance', label: '财务钱包', icon: Wallet }
]

// Statistics data
const statsData = reactive({
  totalSales: 187500,
  totalOrders: 1250,
  totalProfit: 37500,
  availableBalance: 31250
})

// Tickets data
const tickets = ref<Ticket[]>([
  {
    id: '1',
    spotName: '西湖景区',
    spotImage: '',
    spotLevel: '5A',
    ticketType: '成人票',
    retailPrice: 120,
    settlementPrice: 80,
    myPrice: 100,
    profit: 20
  },
  {
    id: '2',
    spotName: '西湖景区',
    spotImage: '',
    spotLevel: '5A',
    ticketType: '儿童票',
    retailPrice: 60,
    settlementPrice: 40,
    myPrice: 50,
    profit: 10
  },
  {
    id: '3',
    spotName: '灵隐寺',
    spotImage: '',
    spotLevel: '4A',
    ticketType: '成人票',
    retailPrice: 45,
    settlementPrice: 30,
    myPrice: 40,
    profit: 10
  },
  {
    id: '4',
    spotName: '普陀山',
    spotImage: '',
    spotLevel: '5A',
    ticketType: '成人票',
    retailPrice: 150,
    settlementPrice: 100,
    myPrice: 130,
    profit: 30
  }
])

// Top spots data
const topSpots = ref([
  { id: '1', name: '西湖景区', image: '', salesCount: 456 },
  { id: '2', name: '灵隐寺', image: '', salesCount: 234 },
  { id: '3', name: '普陀山', image: '', salesCount: 189 }
])

// Orders data
const salesOrders = ref<Order[]>([
  {
    id: '1',
    orderNumber: 'ORD20241201001',
    spotName: '西湖景区',
    playDate: '2024-12-15',
    totalAmount: 200,
    commission: 40,
    createTime: '2024-12-01 10:30:00',
    status: '已完成'
  },
  {
    id: '2',
    orderNumber: 'ORD20241201002',
    spotName: '灵隐寺',
    playDate: '2024-12-16',
    totalAmount: 40,
    commission: 10,
    createTime: '2024-12-01 14:20:00',
    status: '已完成'
  },
  {
    id: '3',
    orderNumber: 'ORD20241201003',
    spotName: '普陀山',
    playDate: '2024-12-18',
    totalAmount: 130,
    commission: 30,
    createTime: '2024-12-02 09:15:00',
    status: '待支付'
  }
])

// Bills data
const bills = ref<Bill[]>([
  {
    id: '1',
    time: '2024-12-01 10:30:00',
    type: '订单分佣',
    description: '西湖景区成人票销售',
    amount: 40,
    balance: 31250
  },
  {
    id: '2',
    time: '2024-11-28 15:00:00',
    type: '余额提现',
    description: '提现到银行卡',
    amount: -5000,
    balance: 31050
  },
  {
    id: '3',
    time: '2024-11-25 09:30:00',
    type: '平台费用',
    description: '平台服务费',
    amount: -50,
    balance: 31000
  }
])

// Search and filter
const ticketSearch = ref('')
const ticketTypeFilter = ref('')
const billMonth = ref('')

// Withdraw dialog
const withdrawDialogVisible = ref(false)
const withdrawForm = reactive({
  amount: 1000,
  actualAmount: 1000
})

// Computed
const filteredTickets = computed(() => {
  return tickets.value.filter(ticket => {
    const nameMatch = ticket.spotName.includes(ticketSearch.value)
    const typeMatch = !ticketTypeFilter.value || ticket.ticketType === ticketTypeFilter.value
    return nameMatch && typeMatch
  })
})

const filteredBills = computed(() => {
  if (!billMonth.value) return bills.value
  return bills.value.filter(bill => bill.time.startsWith(billMonth.value))
})

// Methods
const switchPage = (pageKey: string) => {
  activePage.value = pageKey
  if (pageKey === 'dashboard') {
    nextTick(() => {
      renderCommissionChart()
    })
  }
}

const formatCurrency = (amount: number) => {
  return `¥${amount.toLocaleString()}`
}

const renderCommissionChart = () => {
  if (!commissionChartRef.value) return

  // 销毁之前的图表实例
  echarts.dispose(commissionChartRef.value)

  const chart = echarts.init(commissionChartRef.value)

  // 生成近15天的数据
  const dates = []
  const commissions = []

  for (let i = 14; i >= 0; i--) {
    const date = new Date()
    date.setDate(date.getDate() - i)
    dates.push(date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' }))
    commissions.push(Math.floor(Math.random() * 2000) + 500)
  }

  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const param = params[0]
        return `${param.name}<br/>佣金: ${formatCurrency(param.value)}`
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
      data: dates,
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      name: '佣金(元)',
      axisLabel: {
        formatter: '{value}元'
      }
    },
    series: [{
      name: '佣金收益',
      type: 'line',
      data: commissions,
      smooth: true,
      itemStyle: {
        color: '#1890ff'
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [{
            offset: 0, color: 'rgba(24, 144, 255, 0.3)'
          }, {
            offset: 1, color: 'rgba(24, 144, 255, 0.1)'
          }]
        }
      }
    }]
  }

  chart.setOption(option)

  // 响应式调整
  const resizeObserver = new ResizeObserver(() => {
    chart.resize()
  })
  resizeObserver.observe(commissionChartRef.value)

  return () => {
    resizeObserver.disconnect()
    chart.dispose()
  }
}

const filterTickets = () => {
  // 筛选逻辑已在computed中实现
}

const resetFilters = () => {
  ticketSearch.value = ''
  ticketTypeFilter.value = ''
}

const calculateProfit = (ticket: Ticket) => {
  ticket.profit = ticket.myPrice - ticket.settlementPrice
}

const savePricing = (ticket: Ticket) => {
  if (ticket.myPrice < ticket.settlementPrice) {
    ElMessage.error('售价不得低于结算价')
    return
  }

  ElMessage.success(`"${ticket.spotName}-${ticket.ticketType}"定价已保存`)
}

const showWithdrawDialog = () => {
  withdrawForm.amount = Math.min(1000, statsData.availableBalance)
  withdrawForm.actualAmount = withdrawForm.amount
  withdrawDialogVisible.value = true
}

const submitWithdrawForm = async () => {
  ElMessage.success('提现申请已提交，预计1个工作日到账')
  withdrawDialogVisible.value = false
}

const filterBillsByMonth = () => {
  // 筛选逻辑已在computed中实现
}

const handleLogout = () => {
  router.push('/login')
}

// Form validation rules
const withdrawRules = {
  amount: [
    { required: true, message: '请输入提现金额', trigger: 'blur' },
    { type: 'number', min: 100, message: '提现金额不能小于100元', trigger: 'blur' }
  ]
}

// Lifecycle
onMounted(() => {
  if (activePage.value === 'dashboard') {
    nextTick(() => {
      renderCommissionChart()
    })
  }
})
</script>

<style scoped>
/* 整体布局 */
.distributor-layout {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
}

/* 侧边导航栏 */
.sidebar {
  width: 240px;
  background: white;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 24px 20px;
  border-bottom: 1px solid #e5e6eb;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1d2129;
}

.sidebar-nav {
  flex: 1;
  padding: 16px 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  cursor: pointer;
  transition: all 0.3s;
  color: #666;
}

.nav-item:hover {
  background: #f5f7fa;
  color: #1890ff;
}

.nav-item.active {
  background: #e6f7ff;
  color: #1890ff;
  border-right: 3px solid #1890ff;
}

.nav-text {
  font-weight: 500;
}

/* 主内容区域 */
.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* 顶部状态栏 */
.top-header {
  background: white;
  border-bottom: 1px solid #e5e6eb;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.welcome-text {
  font-size: 16px;
  font-weight: 500;
  color: #1d2129;
}

.logout-btn {
  color: #666;
}

/* 主体内容区 */
.content-area {
  flex: 1;
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  gap: 16px;
}

.card-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.sales-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.orders-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.profit-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.balance-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: white;
}

.card-content {
  flex: 1;
}

.card-title {
  font-size: 14px;
  color: #86909c;
  margin-bottom: 8px;
}

.card-value {
  font-size: 28px;
  font-weight: 700;
  color: #1d2129;
  margin: 0;
}

.profit-value {
  color: #52c41a;
}

.balance-value {
  color: #fa8c16;
}

/* 趋势分析区 */
.analysis-section {
  display: grid;
  grid-template-columns: 3fr 1fr;
  gap: 24px;
}

.chart-area {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1d2129;
}

.commission-chart {
  width: 100%;
  height: 350px;
}

.top-spots {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.top-spots h3 {
  margin: 0 0 20px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1d2129;
}

.spots-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.spot-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  background: #fafbfc;
}

.spot-rank {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #1890ff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
}

.spot-image {
  border-radius: 6px;
}

.spot-info {
  flex: 1;
}

.spot-name {
  font-weight: 600;
  color: #1d2129;
  margin-bottom: 4px;
}

.spot-sales {
  font-size: 12px;
  color: #86909c;
}

/* 门票分销页 */
.filter-bar {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.pricing-table {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.spot-info-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.spot-details {
  flex: 1;
}

.spot-name {
  font-weight: 600;
  color: #1d2129;
  margin-bottom: 2px;
}

.spot-level {
  font-size: 12px;
  color: #86909c;
}

.price-reference {
  line-height: 1.4;
}

.reference-item {
  margin-bottom: 4px;
}

.reference-item .label {
  font-size: 12px;
  color: #86909c;
  margin-right: 8px;
}

.reference-item .value {
  font-size: 13px;
  color: #1d2129;
}

.price-input-wrapper {
  position: relative;
}

.price-error .el-input {
  --el-input-border-color: #ff4d4f;
}

.price-error-text {
  position: absolute;
  top: 100%;
  left: 0;
  font-size: 12px;
  color: #ff4d4f;
  margin-top: 4px;
}

.profit-text {
  font-weight: 600;
  color: #52c41a;
}

.profit-zero {
  color: #ff4d4f;
}

/* 销售订单页 */
.orders-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-item {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: grid;
  grid-template-columns: 1fr 2fr 1fr;
  gap: 20px;
  align-items: center;
}

.order-left {
  text-align: left;
}

.order-number {
  font-weight: 600;
  color: #1d2129;
  margin-bottom: 4px;
}

.order-time {
  font-size: 12px;
  color: #86909c;
}

.order-middle {
  text-align: center;
}

.spot-name {
  font-weight: 600;
  color: #1d2129;
  margin-bottom: 4px;
}

.play-date {
  font-size: 12px;
  color: #86909c;
}

.order-right {
  text-align: right;
}

.order-amount {
  font-size: 18px;
  font-weight: 700;
  color: #1d2129;
  margin-bottom: 4px;
}

.commission-amount {
  font-size: 12px;
  color: #86909c;
}

/* 财务钱包页 */
.wallet-section {
  margin-bottom: 32px;
}

.wallet-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 16px;
  padding: 32px;
  position: relative;
  overflow: hidden;
}

.wallet-card::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  pointer-events: none;
}

.wallet-header {
  position: relative;
  z-index: 1;
}

.wallet-header h3 {
  margin: 0 0 16px 0;
  font-size: 20px;
  opacity: 0.9;
}

.wallet-balance {
  margin-bottom: 24px;
}

.balance-label {
  font-size: 14px;
  opacity: 0.8;
  margin-bottom: 8px;
}

.balance-amount {
  font-size: 36px;
  font-weight: 700;
  margin: 0;
}

.wallet-actions {
  position: relative;
  z-index: 1;
}

.withdraw-tip {
  font-size: 12px;
  opacity: 0.8;
  margin-top: 12px;
  text-align: center;
}

.bills-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.bills-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.bills-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1d2129;
}

.bills-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.bill-item {
  display: grid;
  grid-template-columns: 160px 1fr 120px 100px;
  gap: 16px;
  align-items: center;
  padding: 16px;
  border-radius: 8px;
  background: #fafbfc;
}

.bill-time {
  font-size: 12px;
  color: #86909c;
}

.bill-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.bill-type {
  font-weight: 600;
  color: #1d2129;
  font-size: 14px;
}

.bill-desc {
  font-size: 12px;
  color: #86909c;
}

.amount-income {
  color: #52c41a;
  font-weight: 600;
}

.amount-expense {
  color: #ff4d4f;
  font-weight: 600;
}

.bill-balance {
  font-size: 12px;
  color: #86909c;
  text-align: right;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .sidebar {
    width: 200px;
  }

  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }

  .analysis-section {
    grid-template-columns: 1fr;
  }

  .order-item {
    grid-template-columns: 1fr;
    gap: 12px;
    text-align: center;
  }

  .bill-item {
    grid-template-columns: 1fr;
    gap: 8px;
    text-align: center;
  }
}

@media (max-width: 768px) {
  .sidebar {
    width: 100%;
    position: fixed;
    top: 0;
    left: 0;
    z-index: 1000;
    height: auto;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  .main-area {
    margin-top: 80px;
  }

  .header-content {
    padding: 12px 16px;
  }

  .content-area {
    padding: 16px;
  }

  .stats-cards {
    grid-template-columns: 1fr;
  }

  .wallet-card {
    padding: 24px;
  }

  .balance-amount {
    font-size: 28px;
  }
}

/* 表格样式 */
:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table th) {
  background: #fafbfc !important;
  color: #1d2129 !important;
  font-weight: 600;
  border-bottom: 2px solid #e5e6eb;
}

:deep(.el-table td) {
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-table tbody tr:hover > td) {
  background: #f8f9fa;
}
</style>

