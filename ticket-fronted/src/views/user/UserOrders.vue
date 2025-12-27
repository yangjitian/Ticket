<template>
  <div class="user-orders-page">
    <div class="container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1 class="page-title">我的订单</h1>
        <p class="page-subtitle">查看和管理您的旅游订单</p>
      </div>

      <!-- 订单列表 -->
      <div class="orders-content">
        <!-- 筛选和搜索 -->
        <div class="orders-filter">
          <el-select v-model="statusFilter" placeholder="订单状态" clearable style="width: 150px">
            <el-option label="全部订单" value="" />
            <el-option label="待支付" :value="0" />
            <el-option label="已支付待核销" :value="1" />
            <el-option label="已核销" :value="2" />
            <el-option label="已取消" :value="3" />
            <el-option label="已退款" :value="4" />
            <el-option label="已过期" :value="5" />
          </el-select>

          <el-input
            v-model="searchQuery"
            placeholder="搜索景区名称或订单号"
            clearable
            style="width: 250px; margin-left: 16px"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>

          <el-button type="primary" @click="handleSearch" style="margin-left: 12px">
            搜索
          </el-button>
        </div>

        <!-- 订单列表 -->
        <div v-loading="loading" class="orders-list">
          <div
            v-for="order in orders"
            :key="order.id"
            class="order-card"
          >
            <div class="order-header">
              <div class="order-info">
                <span class="order-number">订单号：{{ order.orderSn }}</span>
                <span class="order-time">{{ formatDateTime(order.createTime) }}</span>
              </div>
              <div class="order-status">
                <el-tag :type="getStatusType(order.orderStatus)">
                  {{ getStatusText(order.orderStatus) }}
                </el-tag>
              </div>
            </div>

            <div class="order-content">
              <div class="product-info">
                <div class="product-name">{{ order.productName }}</div>
                <div class="product-details">
                  <span>游玩日期：{{ formatDate(order.visitDate) }}</span>
                  <span>数量：{{ order.buyNum }}张</span>
                  <span>单价：￥{{ order.unitPrice }}</span>
                </div>
              </div>

              <div class="order-amount">
                <div class="amount">￥{{ order.payAmount }}</div>
                <div class="settlement">结算价：￥{{ order.settlementUnitPrice }}</div>
              </div>
            </div>

            <div class="order-actions">
              <el-button
                v-if="order.orderStatus === 0"
                type="primary"
                size="small"
                @click="payOrder(order)"
              >
                立即支付
              </el-button>

              <el-button
                v-if="order.verificationCode && order.orderStatus === 1"
                type="success"
                size="small"
                @click="showVerificationCode(order)"
              >
                查看核销码
              </el-button>

              <el-button
                v-if="order.orderStatus >= 2"
                type="info"
                size="small"
                @click="viewOrderDetail(order)"
              >
                查看详情
              </el-button>

              <el-button
                v-if="order.orderStatus === 0"
                type="danger"
                size="small"
                @click="cancelOrder(order)"
              >
                取消订单
              </el-button>
            </div>
          </div>

          <!-- 无订单提示 -->
          <div v-if="!loading && orders.length === 0" class="no-orders">
            <el-empty description="暂无订单记录">
              <el-button type="primary" @click="$router.push('/')">
                去逛逛
              </el-button>
            </el-empty>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>

    <!-- 核销码对话框 -->
    <el-dialog
      v-model="verificationDialog"
      title="核销码"
      width="400px"
      center
    >
      <div class="verification-content">
        <div class="qr-code">
          <div class="qr-placeholder">
            <el-icon size="60"><Ticket /></el-icon>
            <p>{{ currentOrder?.verificationCode }}</p>
          </div>
        </div>
        <div class="verification-tip">
          <p>请向景区工作人员出示此核销码</p>
          <p>每个核销码只能使用一次</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Ticket } from '@element-plus/icons-vue'
import { getUserOrders } from '@/user/api'
import type { OrderInfo, OrderQueryRequest } from '@/types/user'

// 订单列表
const orders = ref<OrderInfo[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 筛选和搜索
const statusFilter = ref('')
const searchQuery = ref('')

// 对话框
const verificationDialog = ref(false)
const currentOrder = ref<OrderInfo | null>(null)

// 获取订单列表
const fetchOrders = async () => {
  try {
    loading.value = true

    const params: OrderQueryRequest = {
      current: currentPage.value,
      pageSize: pageSize.value
    }

    if (statusFilter.value !== '') {
      params.orderStatus = Number(statusFilter.value)
    }

    // TODO: 实现后端API
    // const response = await getUserOrders(params)

    // 暂时使用模拟数据
    const mockOrders: OrderInfo[] = [
      {
        id: '1',
        orderSn: 'ORDER20241227001',
        buyerId: '1',
        productId: '1',
        visitDate: '2024-12-28',
        buyNum: 2,
        unitPrice: 80,
        settlementUnitPrice: 70,
        commissionAmount: 20,
        payAmount: 160,
        orderStatus: 1,
        verificationCode: 'SCENIC001122334455',
        createTime: '2024-12-27 10:30:00'
      },
      {
        id: '2',
        orderSn: 'ORDER20241227002',
        buyerId: '1',
        productId: '2',
        visitDate: '2024-12-29',
        buyNum: 1,
        unitPrice: 40,
        settlementUnitPrice: 35,
        commissionAmount: 5,
        payAmount: 40,
        orderStatus: 2,
        verificationCode: 'SCENIC001122334456',
        verifyTime: '2024-12-29 14:20:00',
        createTime: '2024-12-27 15:45:00'
      }
    ]

    // 筛选模拟数据
    let filteredOrders = mockOrders
    if (statusFilter.value !== '') {
      filteredOrders = mockOrders.filter(order =>
        order.orderStatus === Number(statusFilter.value)
      )
    }

    orders.value = filteredOrders.slice(
      (currentPage.value - 1) * pageSize.value,
      currentPage.value * pageSize.value
    )
    total.value = filteredOrders.length

  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchOrders()
}

// 分页处理
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchOrders()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  fetchOrders()
}

// 获取状态样式
const getStatusType = (status: number) => {
  const statusMap: Record<number, string> = {
    0: 'warning',
    1: 'info',
    2: 'success',
    3: 'danger',
    4: 'info',
    5: 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    0: '待支付',
    1: '已支付待核销',
    2: '已核销',
    3: '已取消',
    4: '已退款',
    5: '已过期'
  }
  return statusMap[status] || '未知状态'
}

// 支付订单
const payOrder = (order: OrderInfo) => {
  // 跳转到支付页面或显示支付对话框
  ElMessage.info('跳转到支付页面')
}

// 查看核销码
const showVerificationCode = (order: OrderInfo) => {
  currentOrder.value = order
  verificationDialog.value = true
}

// 查看订单详情
const viewOrderDetail = (order: OrderInfo) => {
  ElMessage.info('查看订单详情功能开发中')
}

// 取消订单
const cancelOrder = async (order: OrderInfo) => {
  try {
    await ElMessageBox.confirm('确定要取消此订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // TODO: 调用取消订单API
    ElMessage.success('订单已取消')
    fetchOrders()
  } catch {
    // 用户取消操作
  }
}

// 格式化日期时间
const formatDateTime = (dateTimeStr: string) => {
  if (!dateTimeStr) return ''
  const date = new Date(dateTimeStr)
  return date.toLocaleString('zh-CN')
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.user-orders-page {
  padding: 40px 0;
  background: #f8f9fa;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 页面标题 */
.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 1rem;
}

.page-subtitle {
  font-size: 1.1rem;
  color: #666;
  margin: 0;
}

/* 订单内容 */
.orders-content {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 订单筛选 */
.orders-filter {
  padding: 24px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

/* 订单列表 */
.orders-list {
  padding: 0;
}

.order-card {
  border-bottom: 1px solid #f0f0f0;
  padding: 24px;
  transition: background-color 0.3s;
}

.order-card:hover {
  background: #fafafa;
}

.order-card:last-child {
  border-bottom: none;
}

/* 订单头部 */
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.order-number {
  font-weight: 600;
  color: #333;
}

.order-time {
  font-size: 0.9rem;
  color: #999;
}

/* 订单内容 */
.order-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.product-details {
  display: flex;
  gap: 16px;
  font-size: 0.9rem;
  color: #666;
}

.order-amount {
  text-align: right;
}

.amount {
  font-size: 1.2rem;
  font-weight: 600;
  color: #f5222d;
  margin-bottom: 4px;
}

.settlement {
  font-size: 0.8rem;
  color: #999;
}

/* 订单操作 */
.order-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* 无订单状态 */
.no-orders {
  padding: 60px 20px;
  text-align: center;
}

/* 分页 */
.pagination-wrapper {
  padding: 24px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: center;
}

/* 核销码对话框 */
.verification-content {
  text-align: center;
}

.qr-code {
  margin-bottom: 24px;
}

.qr-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 40px;
  background: #f5f5f5;
  border-radius: 8px;
  border: 2px dashed #d9d9d9;
}

.qr-placeholder p {
  font-size: 1.2rem;
  font-weight: 600;
  color: #333;
  margin: 0;
  font-family: monospace;
  letter-spacing: 2px;
}

.verification-tip {
  color: #666;
  font-size: 0.9rem;
}

.verification-tip p {
  margin: 8px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-orders-page {
    padding: 20px 0;
  }

  .container {
    padding: 0 15px;
  }

  .page-title {
    font-size: 2rem;
  }

  .orders-filter {
    padding: 16px;
    flex-direction: column;
    align-items: stretch;
  }

  .orders-filter .el-input,
  .orders-filter .el-select {
    width: 100% !important;
    margin-left: 0 !important;
  }

  .orders-filter .el-button {
    margin-left: 0 !important;
    margin-top: 12px;
  }

  .order-content {
    flex-direction: column;
    gap: 12px;
  }

  .order-amount {
    text-align: left;
  }

  .order-actions {
    justify-content: center;
  }
}
</style>
