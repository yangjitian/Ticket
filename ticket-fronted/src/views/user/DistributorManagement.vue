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
            <div class="chart-area" style="width: 100%">
              <div class="chart-header">
                <h3>近15天佣金收益趋势</h3>
              </div>
              <div ref="commissionChartRef" class="commission-chart"></div>
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
            <el-table :data="filteredTickets" stripe style="width: 100%" v-loading="loading">
              <el-table-column label="景区信息" min-width="180">
                <template #default="scope">
                  <div class="spot-info-cell">
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
              <el-table-column label="分销售价" width="160" align="center">
                <template #default="scope">
                  <div class="price-display">
                    <span class="fixed-price">¥{{ scope.row.myPrice }}</span>
                    <span class="price-note">(同零售价)</span>
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
              <el-table-column label="操作" width="140" align="center">
                <template #default="scope">
                  <el-button
                    type="primary"
                    size="small"
                    @click="copyLink(scope.row)"
                  >
                   <el-icon style="margin-right: 4px"><CopyDocument /></el-icon>
                    推广链接
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
                  :type="(order.status === '已完成' || order.status === '待核销') ? 'success' :
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
  SwitchButton, CopyDocument
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { 
  getDistributorProducts, 
  getDistributorOrders, 
  getDistributorOverview, 
  getDistributorWallet, 
  withdrawBalance,
  getCurrentUser
} from '@/user/api'

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
  spotId: string
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
const trendData = ref<any[]>([]) // Add trendData ref

// User info
const userInfo = reactive({
  id: '',
  nickname: '',
  auditStatus: '1', 
  avatar: ''
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
  totalSales: 0,
  totalOrders: 0,
  totalProfit: 0,
  availableBalance: 0
})

// Tickets data
const tickets = ref<Ticket[]>([])
const loading = ref(false)

// Top spots data
const topSpots = ref<any[]>([])

// Orders data
const salesOrders = ref<Order[]>([])
const ordersTotal = ref(0)

// Bills data
const bills = ref<Bill[]>([])

// Search and filter
const ticketSearch = ref('')
const ticketTypeFilter = ref('')
const billMonth = ref('')

// Withdraw dialog
const withdrawDialogVisible = ref(false)
const withdrawForm = reactive({
  amount: 0,
  actualAmount: 0 // For display only, same as amount
})

// Computed
const filteredTickets = computed(() => {
  return tickets.value.filter(ticket => {
    const nameMatch = !ticketSearch.value || ticket.spotName.includes(ticketSearch.value)
    const typeMatch = !ticketTypeFilter.value || ticket.ticketType === ticketTypeFilter.value
    return nameMatch && typeMatch
  })
})

const filteredBills = computed(() => {
  // Backend filtering usually, but here we do simple client or assume backend does it
  return bills.value
})

// Methods
const fetchUserInfo = async () => {
    try {
        const res = await getCurrentUser()
        if (res.data.code === 200) {
            const u = res.data.data
            userInfo.id = u.id
            userInfo.nickname = u.nickname
            userInfo.auditStatus = u.auditStatus ? String(u.auditStatus) : '0' // Assuming 1 is approved
            // update stats if available in user object, but we have dedicated api
        }
    } catch(e) {
        console.error(e)
    }
}

const fetchOverview = async () => {
    try {
        const res = await getDistributorOverview()
        if (res.data.code === 200) {
            const data = res.data.data
            statsData.totalSales = data.totalSales || 0
            statsData.totalOrders = data.totalOrders || 0
            statsData.totalProfit = data.totalProfit || 0
            statsData.availableBalance = data.availableBalance || 0
            
            // Map trend data
            trendData.value = data.trend || []

            // Re-render chart if active
            if (activePage.value === 'dashboard') {
                nextTick(() => {
                    renderCommissionChart()
                })
            }
        }
    } catch (e) { console.error(e) }
}

const fetchProducts = async () => {
    loading.value = true
    try {
        const res = await getDistributorProducts({ current: 1, pageSize: 100 }) // fetch all for now
        if (res.data.code === 200) {
            tickets.value = res.data.data.records.map((item: any) => ({
                id: String(item.id),
                spotId: String(item.spotId),
                spotName: item.spotName,
                spotImage: '', 
                spotLevel: item.spotLevel,
                ticketType: item.ticketType,
                retailPrice: item.retailPrice,
                settlementPrice: item.settlementPrice,
                myPrice: item.myPrice, // This is fixed to retailPrice in backend now
                profit: item.profit
            }))
        }
    } catch (e) { console.error(e) }
    finally { loading.value = false }
}

const fetchOrders = async () => {
    try {
        const res = await getDistributorOrders({ current: 1, pageSize: 50 })
        if (res.data.code === 200) {
            salesOrders.value = res.data.data.records.map((order: any) => ({
                id: order.id,
                orderNumber: order.orderSn,
                spotName: order.spotName,
                playDate: order.playDate,
                totalAmount: order.totalAmount,
                commission: order.commission,
                createTime: order.createTime.replace('T', ' '),
                status: order.orderStatus
            }))
            ordersTotal.value = res.data.data.total
        }
    } catch (e) { console.error(e) }
}

const fetchWallet = async () => {
    try {
        const res = await getDistributorWallet(billMonth.value)
        if (res.data.code === 200) {
            statsData.availableBalance = res.data.data.availableBalance
            bills.value = res.data.data.bills
        }
    } catch (e) { console.error(e) }
}

const switchPage = (pageKey: string) => {
  activePage.value = pageKey
  if (pageKey === 'dashboard') {
    fetchOverview()
    nextTick(() => {
      renderCommissionChart()
    })
  } else if (pageKey === 'pricing') {
      fetchProducts()
  } else if (pageKey === 'orders') {
      fetchOrders()
  } else if (pageKey === 'finance') {
      fetchWallet()
  }
}

const formatCurrency = (amount: number) => {
  return `¥${(amount || 0).toLocaleString()}`
}

const renderCommissionChart = () => {
  if (!commissionChartRef.value) return
  echarts.dispose(commissionChartRef.value)
  const chart = echarts.init(commissionChartRef.value)
  
  const dates = []
  const commissions = []
  
  // Use real trend data
  if (trendData.value && trendData.value.length > 0) {
      // Data is ordered from -14 days to today
      trendData.value.forEach((item: any) => {
          // Format date: 2024-01-01 -> 01-01
          const dateStr = item.date ? item.date.substring(5) : ''
          dates.push(dateStr)
          commissions.push(item.profit || 0)
      })
  } else {
      // Fallback empty chart if no data
      for (let i = 14; i >= 0; i--) {
        const date = new Date()
        date.setDate(date.getDate() - i)
        dates.push(date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' }))
        commissions.push(0)
      }
  }

  const option = {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value', name: '佣金(元)' },
    series: [{
      name: '佣金收益',
      type: 'line',
      data: commissions,
      smooth: true,
      itemStyle: { color: '#1890ff' },
      areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(24, 144, 255, 0.3)' }, { offset: 1, color: 'rgba(24, 144, 255, 0.1)' }] } }
    }]
  }
  chart.setOption(option)
  const resizeObserver = new ResizeObserver(() => chart.resize())
  resizeObserver.observe(commissionChartRef.value)
  return () => { resizeObserver.disconnect(); chart.dispose() }
}

const filterTickets = () => {}
const resetFilters = () => { ticketSearch.value = ''; ticketTypeFilter.value = '' }

const calculateProfit = (ticket: Ticket) => {
    // Read only now
}

const copyLink = (ticket: Ticket) => {
    if (!userInfo.id) {
        ElMessage.warning('用户信息未加载，无法生成对应的推广链接')
        return
    }
    if (!ticket.spotId) {
        console.error('Ticket missing spotId:', ticket)
        ElMessage.error('无法获取景区ID，请刷新重试')
        return
    }
    console.log('Generating link for ticket:', ticket)
    const origin = window.location.origin
    const url = `${origin}/spot/${ticket.spotId}?ref=${userInfo.id}`
    console.log('Generated URL:', url)
    navigator.clipboard.writeText(url).then(() => {
        ElMessage.success('推广链接已复制')
    }).catch(() => {
        // Fallback for some browsers
        const input = document.createElement('input')
        input.value = url
        document.body.appendChild(input)
        input.select()
        document.execCommand('copy')
        document.body.removeChild(input)
        ElMessage.success('推广链接已复制')
    })
}

const showWithdrawDialog = () => {
  withdrawForm.amount = 0
  withdrawForm.actualAmount = 0
  withdrawDialogVisible.value = true
}

const submitWithdrawForm = async () => {
  if (withdrawForm.amount < 100) {
      ElMessage.warning('最低提现金额100元')
      return
  }
  if (withdrawForm.amount > statsData.availableBalance) {
      ElMessage.warning('余额不足')
      return
  }
  try {
      const res = await withdrawBalance(withdrawForm.amount)
      if (res.data.code === 200) {
          ElMessage.success('提现申请成功')
          withdrawDialogVisible.value = false
          fetchWallet() // refresh
      } else {
          ElMessage.error(res.data.message || '提现失败')
      }
  } catch (e) {
      console.error(e)
      ElMessage.error('提现失败')
  }
}

const filterBillsByMonth = () => {
    fetchWallet()
}

const handleLogout = () => {
  localStorage.removeItem('user_token')
  router.push('/login')
}

const withdrawRules = {
  amount: [
    { required: true, message: '请输入提现金额', trigger: 'blur' },
    { type: 'number', min: 100, message: '提现金额不能小于100元', trigger: 'blur' }
  ]
}

onMounted(async () => {
    await fetchUserInfo()
    if (activePage.value === 'dashboard') {
        fetchOverview()
        nextTick(() => renderCommissionChart())
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

