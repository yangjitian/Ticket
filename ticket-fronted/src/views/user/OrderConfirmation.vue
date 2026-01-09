<template>
  <div class="order-confirmation-page">
    <!-- 头部导航 -->
    <div class="header-nav">
      <div class="container">
        <div class="nav-content">
          <div class="back-btn" @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </div>
          <div class="progress">
            <div class="step completed">
              <span class="step-num">1</span>
              <span class="step-text">选择票种</span>
            </div>
            <div class="step-line completed"></div>
            <div class="step completed">
              <span class="step-num">2</span>
              <span class="step-text">选择游客</span>
            </div>
            <div class="step-line completed"></div>
            <div class="step active">
              <span class="step-num">3</span>
              <span class="step-text">确认订单</span>
            </div>
            <div class="step-line"></div>
            <div class="step">
              <span class="step-num">4</span>
              <span class="step-text">支付</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主要内容 -->
    <div class="main-content">
      <div class="container">
        <div class="content-wrapper">

          <!-- 订单确认卡片 -->
          <div class="order-card">
            <div class="card-header">
              <h2>订单确认</h2>
              <div class="order-info">
                <span class="order-sn">订单号：{{ orderDetail?.orderSn || '' }}</span>
                <span class="create-time">下单时间：{{ orderDetail?.createTime ? formatDateTime(orderDetail.createTime) : '' }}</span>
              </div>
            </div>

            <!-- 景区信息 -->
            <div class="scenic-info">
              <div class="scenic-header">
                <div class="scenic-image">
                  <img :src="orderDetail?.spotImage || defaultImage" :alt="orderDetail?.spotName" />
                </div>
                <div class="scenic-details">
                  <h3>{{ orderDetail?.spotName }}</h3>
                  <div class="scenic-meta">
                    <span class="meta-item">
                      <el-icon><Calendar /></el-icon>
                      游玩日期：{{ orderDetail?.visitDate ? formatDate(orderDetail.visitDate) : '' }}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 门票信息 -->
            <div class="ticket-info">
              <h4>门票信息</h4>
              <div class="ticket-summary">
                <div class="summary-item">
                  <span class="ticket-name">{{ orderDetail?.ticketTypeName }}</span>
                  <span class="ticket-price">¥{{ orderDetail?.unitPrice }}</span>
                  <span class="ticket-quantity">×{{ orderDetail?.buyNum }}</span>
                  <span class="ticket-subtotal">¥{{ orderDetail?.payAmount }}</span>
                </div>
              </div>
            </div>

            <!-- 购票人信息 -->
            <div class="passenger-info">
              <h4>购票人信息</h4>
              <div class="passenger-list">
                <div class="passenger-item" v-for="(visitor, index) in orderDetail?.visitors" :key="index">
                  <div class="passenger-number">{{ index + 1 }}</div>
                  <div class="passenger-details">
                    <div class="passenger-name">{{ visitor.visitorName }}</div>
                    <div class="passenger-id">身份证：{{ visitor.idCard }}</div>
                    <div v-if="visitor.contactTel" class="passenger-phone">电话：{{ visitor.contactTel }}</div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 订单总计 -->
            <div class="order-total">
              <div class="total-row">
                <span class="total-label">订单总价：</span>
                <span class="total-amount">¥{{ orderDetail?.payAmount }}</span>
              </div>
            </div>

            <!-- 支付方式选择 -->
            <div class="payment-method">
              <h4>选择支付方式</h4>
              <div class="payment-options">
                <div class="payment-option">
                  <el-radio v-model="selectedPaymentMethod" :value="1">
                    <div class="payment-info">
                      <div class="payment-icon">
                        <img src="https://via.placeholder.com/32x32?text=微信" alt="微信支付" />
                      </div>
                      <div class="payment-text">
                        <div class="payment-name">微信支付</div>
                        <div class="payment-desc">推荐使用</div>
                      </div>
                    </div>
                  </el-radio>
                </div>
                <div class="payment-option">
                  <el-radio v-model="selectedPaymentMethod" :value="2">
                    <div class="payment-info">
                      <div class="payment-icon">
                        <img src="https://via.placeholder.com/32x32?text=支付宝" alt="支付宝" />
                      </div>
                      <div class="payment-text">
                        <div class="payment-name">支付宝</div>
                        <div class="payment-desc">快速到账</div>
                      </div>
                    </div>
                  </el-radio>
                </div>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="action-buttons">
              <el-button size="large" @click="modifyPassengers">修改购票人</el-button>
              <el-button
                type="primary"
                size="large"
                :loading="submitting"
                @click="confirmOrder"
              >
                立即支付 ¥{{ orderDetail?.payAmount }}
              </el-button>
            </div>

            <!-- 购票须知 -->
            <div class="booking-notice">
              <h4>购票须知</h4>
              <div class="notice-content">
                <p>• 请仔细核对购票人信息，确认无误后再支付</p>
                <p>• 门票一经使用不支持退换，请谨慎购买</p>
                <p>• 游玩当天请携带有效身份证件入园</p>
                <p>• 如有疑问请咨询景区工作人员</p>
              </div>
            </div>
          </div>

        </div>
      </div>
    </div>

    <!-- 支付倒计时弹窗 -->
    <el-dialog
      v-model="countdownDialogVisible"
      title="订单创建成功"
      width="400px"
      center
      :close-on-click-modal="false"
      :show-close="false"
    >
      <div class="countdown-content">
        <div class="countdown-icon">
          <el-icon size="48" color="#52c41a"><SuccessFilled /></el-icon>
        </div>
        <div class="countdown-text">
          <p>订单创建成功，请在 <span class="countdown-time">{{ countdown }}</span> 内完成支付</p>
          <p class="countdown-tip">超时订单将自动取消</p>
        </div>
      </div>

      <template #footer>
        <el-button type="primary" @click="proceedToPayment" block>
          立即支付
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft,
  Calendar,
  SuccessFilled
} from '@element-plus/icons-vue'
import { bookTicket } from '@/user/api'
import type { OrderVO } from '@/types/user'

const router = useRouter()
const route = useRoute()

// 订单详情
const orderDetail = ref<OrderVO | null>(null)

// 选中的支付方式
const selectedPaymentMethod = ref(1)

// 提交状态
const submitting = ref(false)

// 倒计时弹窗
const countdownDialogVisible = ref(false)
const countdown = ref('15:00')
let countdownTimer: number | null = null
let countdownSeconds = 15 * 60 // 15分钟

// 默认图片
const defaultImage = 'https://via.placeholder.com/120x80?text=景区'

// 创建订单
const createOrder = async () => {
  try {
    submitting.value = true

    const query = route.query
    // 对URL编码的参数进行解码
    const decodedSpotId = decodeURIComponent(query.spotId as string)
    const decodedVisitorIds = decodeURIComponent(query.visitorIds as string)

    console.log('OrderConfirmation - 解码前spotId:', query.spotId)
    console.log('OrderConfirmation - 解码后spotId:', decodedSpotId)

    const request = {
      spotId: BigInt(decodedSpotId).toString(), // 使用BigInt确保大整数精度
      ticketType: Number(decodeURIComponent(query.ticketType as string)),
      visitDate: decodeURIComponent(query.visitDate as string),
      visitorIds: decodedVisitorIds.split(',').map(id => BigInt(id.trim()).toString()) // 游客ID也使用BigInt
    }

    console.log('购票请求参数:', request)
    console.log('原始query.spotId:', query.spotId, '类型:', typeof query.spotId)
    console.log('转换后spotId:', request.spotId, '类型:', typeof request.spotId)

    const response = await bookTicket(request)
    if (response.data.code === 200) {
      orderDetail.value = response.data.data
      // 显示倒计时弹窗
      showCountdownDialog()
    } else {
      ElMessage.error(response.data.message || '创建订单失败')
      router.back()
    }
  } catch (error) {
    console.error('创建订单失败:', error)
    ElMessage.error('创建订单失败')
    router.back()
  } finally {
    submitting.value = false
  }
}

// 显示倒计时弹窗
const showCountdownDialog = () => {
  countdownDialogVisible.value = true
  countdownSeconds = 15 * 60
  updateCountdownDisplay()

  countdownTimer = setInterval(() => {
    countdownSeconds--
    updateCountdownDisplay()

    if (countdownSeconds <= 0) {
      clearInterval(countdownTimer!)
      countdownTimer = null
      countdownDialogVisible.value = false
      ElMessage.warning('订单已过期，请重新下单')
      router.back()
    }
  }, 1000)
}

// 更新倒计时显示
const updateCountdownDisplay = () => {
  const minutes = Math.floor(countdownSeconds / 60)
  const seconds = countdownSeconds % 60
  countdown.value = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
}

// 确认订单并支付
const confirmOrder = () => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
  countdownDialogVisible.value = false

  // 跳转到支付页面
  router.push({
    path: '/payment',
    query: {
      orderId: encodeURIComponent(String(orderDetail.value?.id)), // 确保订单ID作为字符串传递并编码
      payType: encodeURIComponent(String(selectedPaymentMethod.value))
    }
  })
}

// 立即支付（从倒计时弹窗）
const proceedToPayment = () => {
  confirmOrder()
}

// 修改购票人
const modifyPassengers = () => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
  router.back()
}

// 返回上一页
const goBack = () => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
  router.back()
}

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return ''
  const d = new Date(dateTime)
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 组件卸载时清理定时器
onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
})

onMounted(() => {
  createOrder()
})
</script>

<style scoped>
.order-confirmation-page {
  min-height: 100vh;
  background: #f8f9fa;
}

/* 头部导航 */
.header-nav {
  background: white;
  border-bottom: 1px solid #e8e8e8;
  padding: 20px 0;
}

.nav-content {
  display: flex;
  align-items: center;
  gap: 40px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.back-btn:hover {
  background: #f5f5f5;
  color: #409eff;
}

.progress {
  display: flex;
  align-items: center;
  flex: 1;
  max-width: 600px;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.step.completed .step-num,
.step.active .step-num {
  background: #409eff;
  color: white;
}

.step.completed .step-text,
.step.active .step-text {
  color: #409eff;
  font-weight: 500;
}

.step-num {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #e8e8e8;
  color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
}

.step-text {
  font-size: 12px;
  color: #666;
}

.step-line {
  flex: 1;
  height: 2px;
  background: #e8e8e8;
  margin: 0 20px;
}

.step.completed + .step-line,
.step.active + .step-line {
  background: #409eff;
}

/* 主要内容 */
.main-content {
  padding: 40px 0;
}

.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}

.content-wrapper {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 订单卡片 */
.order-card {
  background: white;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid #e8e8e8;
}

.card-header h2 {
  margin: 0;
  color: #333;
  font-size: 24px;
  font-weight: 600;
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  text-align: right;
  font-size: 14px;
  color: #666;
}

.order-sn {
  font-weight: 500;
  color: #333;
}

/* 景区信息 */
.scenic-info {
  margin-bottom: 32px;
}

.scenic-header {
  display: flex;
  gap: 20px;
  align-items: center;
}

.scenic-image img {
  width: 120px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
}

.scenic-details h3 {
  margin: 0 0 12px;
  color: #333;
  font-size: 18px;
  font-weight: 600;
}

.scenic-meta {
  display: flex;
  gap: 20px;
  color: #666;
  font-size: 14px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 门票信息 */
.ticket-info,
.passenger-info {
  margin-bottom: 32px;
}

.ticket-info h4,
.passenger-info h4 {
  margin: 0 0 16px;
  color: #333;
  font-size: 16px;
  font-weight: 600;
}

.ticket-summary {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
}

.ticket-name {
  color: #333;
  font-weight: 500;
}

.ticket-price {
  color: #f5222d;
  font-weight: 600;
}

.ticket-quantity {
  color: #666;
}

.ticket-subtotal {
  color: #f5222d;
  font-weight: 600;
  font-size: 18px;
}

/* 购票人信息 */
.passenger-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.passenger-item {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.passenger-number {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.passenger-details {
  flex: 1;
}

.passenger-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.passenger-id,
.passenger-phone {
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
}

/* 订单总计 */
.order-total {
  margin-bottom: 32px;
  padding: 20px;
  background: #fff7e6;
  border-radius: 8px;
  border: 1px solid #ffd591;
}

.total-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
}

.total-label {
  color: #333;
  font-weight: 500;
}

.total-amount {
  color: #f5222d;
  font-size: 24px;
  font-weight: 700;
}

/* 支付方式 */
.payment-method {
  margin-bottom: 32px;
}

.payment-method h4 {
  margin: 0 0 16px;
  color: #333;
  font-size: 16px;
  font-weight: 600;
}

.payment-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.payment-option {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.payment-option:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.payment-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.payment-icon img {
  width: 32px;
  height: 32px;
  border-radius: 4px;
}

.payment-text {
  flex: 1;
}

.payment-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.payment-desc {
  font-size: 14px;
  color: #999;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 16px;
  justify-content: flex-end;
  margin-bottom: 32px;
}

/* 购票须知 */
.booking-notice {
  border-top: 1px solid #e8e8e8;
  padding-top: 24px;
}

.booking-notice h4 {
  margin: 0 0 16px;
  color: #333;
  font-size: 16px;
  font-weight: 600;
}

.notice-content {
  color: #666;
  line-height: 1.6;
}

.notice-content p {
  margin: 0 0 8px;
}

/* 倒计时弹窗 */
.countdown-content {
  text-align: center;
  padding: 20px 0;
}

.countdown-icon {
  margin-bottom: 20px;
}

.countdown-text p {
  margin: 0 0 12px;
  font-size: 16px;
  color: #333;
}

.countdown-time {
  color: #f5222d;
  font-weight: 600;
  font-size: 18px;
}

.countdown-tip {
  color: #999;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .nav-content {
    flex-direction: column;
    gap: 20px;
  }

  .progress {
    width: 100%;
  }

  .container {
    padding: 0 15px;
  }

  .order-card {
    padding: 20px;
  }

  .card-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .order-info {
    text-align: left;
  }

  .scenic-header {
    flex-direction: column;
    gap: 12px;
    text-align: center;
  }

  .scenic-meta {
    justify-content: center;
  }

  .action-buttons {
    flex-direction: column;
  }

  .action-buttons .el-button {
    width: 100%;
  }

  .payment-options {
    gap: 8px;
  }
}
</style>
