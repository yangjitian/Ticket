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
                <div class="product-name">{{ order.spotName }} - {{ order.ticketTypeName }}</div>
                <div class="product-details">
                  <span>游玩日期：{{ formatDate(order.visitDate) }}</span>
                  <span>数量：{{ order.buyNum }}张</span>
                  <span>单价：￥{{ order.unitPrice }}</span>
                </div>
              </div>

              <div class="order-amount">
                <div class="amount">￥{{ order.payAmount }}</div>
                <div class="settlement">票种：{{ order.ticketTypeName }}</div>
              </div>
            </div>

            <div class="order-actions">
              <el-button
                v-if="order.orderStatus === 0"
                type="primary"
                size="small"
                :loading="payLoading[order.id]"
                :disabled="payLoading[order.id]"
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
                :loading="cancelLoading[order.id]"
                :disabled="cancelLoading[order.id]"
                @click="cancelOrder(order)"
              >
                取消订单
              </el-button>

              <el-button
                v-if="order.orderStatus > 0"
                type="danger"
                size="small"
                :loading="deleteLoading[order.id]"
                :disabled="deleteLoading[order.id]"
                @click="deleteOrder(order)"
              >
                删除订单
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
          <div v-if="qrCodeDataUrl" class="qr-code-image">
            <img :src="qrCodeDataUrl" alt="核销码二维码" />
          </div>
          <div v-else class="qr-placeholder">
            <el-icon size="60"><Ticket /></el-icon>
            <p>{{ currentOrder?.verificationCode }}</p>
          </div>
        </div>
        <div class="verification-code-text">
          <p class="code-label">核销码：</p>
          <p class="code-value">{{ currentOrder?.verificationCode }}</p>
        </div>
        <div class="verification-tip">
          <p>请向景区工作人员出示二维码或核销码</p>
          <p>每个核销码只能使用一次</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Ticket } from '@element-plus/icons-vue'
import { getUserOrderVOs, payOrder, cancelOrder as cancelOrderApi, deleteOrder as deleteOrderApi } from '@/user/api'
import type { OrderInfo, OrderQueryRequest } from '@/types/user'

const router = useRouter()

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

// 二维码
const qrCodeDataUrl = ref('')

// 支付、取消、删除loading状态
const payLoading = ref<Record<string, boolean>>({})
const cancelLoading = ref<Record<string, boolean>>({})
const deleteLoading = ref<Record<string, boolean>>({})

// 获取订单列表
// 检查登录状态
const checkLoginStatus = () => {
  const token = localStorage.getItem('user_token')
  if (!token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return false
  }
  return true
}

const fetchOrders = async () => {
  // 先检查登录状态
  if (!checkLoginStatus()) {
    return
  }

  try {
    loading.value = true

    const params: OrderQueryRequest = {
      current: currentPage.value,
      pageSize: pageSize.value
    }

    if (statusFilter.value !== '') {
      params.orderStatus = Number(statusFilter.value)
    }

    // 调用真实的API
    const response = await getUserOrderVOs(params)

    if (response.data.code === 200) {
      const pageData = response.data.data
      orders.value = pageData.records || []
      total.value = pageData.total || 0
    } else {
      ElMessage.error(response.data.message || '获取订单列表失败')
    }

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
const payOrder = async (order: OrderInfo) => {
  const orderId = order.id as string

  // 设置loading状态
  payLoading.value[orderId] = true

  try {
    // 调用支付API
    const payData = {
      orderId: order.id,
      payType: 1 // 默认微信支付
    }

    const response = await payOrder(payData)

    if (response.data.code === 200) {
      ElMessage.success('支付成功')
      // 刷新订单列表
      fetchOrders()
    } else {
      ElMessage.error(response.data.message || '支付失败')
    }
  } catch (error) {
    console.error('支付订单失败:', error)
    ElMessage.error('支付失败，请重试')
  } finally {
    // 清除loading状态
    payLoading.value[orderId] = false
  }
}

// 查看核销码
const showVerificationCode = async (order: OrderInfo) => {
  currentOrder.value = order

  // 生成二维码
  if (order.verificationCode) {
    try {
      // 使用在线二维码API生成二维码
      const qrData = encodeURIComponent(`VERIFICATION:${order.verificationCode}:ORDER:${order.id}`)
      qrCodeDataUrl.value = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${qrData}&format=png`
    } catch (error) {
      console.error('生成二维码失败:', error)
      qrCodeDataUrl.value = ''
    }
  }

  verificationDialog.value = true
}

// 查看订单详情
const viewOrderDetail = (order: OrderInfo) => {
  ElMessage.info('查看订单详情功能开发中')
}

// 取消订单
const cancelOrder = async (order: OrderInfo) => {
  const orderId = order.id as string

  // 设置loading状态
  cancelLoading.value[orderId] = true

  try {
    await ElMessageBox.confirm('确定要取消此订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 调用取消订单API
    const response = await cancelOrderApi(order.id)

    if (response.data.code === 200) {
      ElMessage.success('订单已取消')
      fetchOrders()
    } else {
      ElMessage.error(response.data.message || '取消订单失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
      ElMessage.error('取消订单失败，请重试')
    }
  } finally {
    // 清除loading状态
    cancelLoading.value[orderId] = false
  }
}

// 删除订单
const deleteOrder = async (order: OrderInfo) => {
  const orderId = order.id as string

  // 设置loading状态
  deleteLoading.value[orderId] = true

  try {
    await ElMessageBox.confirm('确定要删除此订单吗？删除后不可恢复！', '警告', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    })

    // 调用删除订单API
    console.log('调用删除订单API - 订单ID:', order.id, '类型:', typeof order.id)
    const orderIdNum = Number(order.id)
    console.log('转换后的订单ID:', orderIdNum, '类型:', typeof orderIdNum)
    const response = await deleteOrderApi(orderIdNum)
    console.log('删除订单API响应:', response)

    if (response.data.code === 200) {
      ElMessage.success('订单已删除')
      console.log('订单删除成功，准备刷新订单列表')
      fetchOrders()
    } else {
      console.error('删除订单失败 - 响应码:', response.data.code, '消息:', response.data.message)
      ElMessage.error(response.data.message || '删除订单失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除订单失败:', error)
      ElMessage.error('删除订单失败，请重试')
    }
  } finally {
    // 清除loading状态
    deleteLoading.value[orderId] = false
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
  margin-bottom: 16px;
}

.qr-code-image {
  display: inline-block;
  padding: 12px;
  background: #f5f5f5;
  border-radius: 8px;
  border: 2px solid #e8e8e8;
}

.qr-code-image img {
  width: 180px;
  height: 180px;
  border-radius: 4px;
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

.verification-code-text {
  margin-bottom: 20px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
}

.code-label {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 4px;
}

.code-value {
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
  font-family: monospace;
  letter-spacing: 1px;
  word-break: break-all;
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
