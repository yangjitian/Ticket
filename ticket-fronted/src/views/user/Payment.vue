<template>
  <div class="payment-page">
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
            <div class="step completed">
              <span class="step-num">3</span>
              <span class="step-text">确认订单</span>
            </div>
            <div class="step-line completed"></div>
            <div class="step active">
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

          <!-- 订单信息卡片 -->
          <div class="order-info-card">
            <div class="card-header">
              <h2>订单支付</h2>
              <div class="order-meta">
                <span class="order-sn">订单号：{{ orderDetail?.orderSn || '' }}</span>
                <span class="expire-time" v-if="timeRemaining > 0">
                  支付倒计时：{{ formatTime(timeRemaining) }}
                </span>
                <span class="expired" v-else>订单已过期</span>
              </div>
            </div>

            <div class="order-summary">
              <div class="summary-row">
                <span class="label">{{ orderDetail?.spotName }}</span>
                <span class="value">{{ orderDetail?.ticketTypeName }} × {{ orderDetail?.buyNum }}</span>
              </div>
              <div class="summary-row">
                <span class="label">游玩日期</span>
                <span class="value">{{ orderDetail?.visitDate ? formatDate(orderDetail.visitDate) : '' }}</span>
              </div>
              <div class="summary-row total">
                <span class="label">应付金额</span>
                <span class="value">¥{{ orderDetail?.payAmount }}</span>
              </div>
            </div>
          </div>

          <!-- 支付方式卡片 -->
          <div class="payment-card">
            <div class="card-header">
              <h3>选择支付方式</h3>
            </div>

            <div class="payment-methods">
              <div class="payment-method" :class="{ selected: selectedPaymentMethod === 1 }" @click="selectPaymentMethod(1)">
                <div class="method-radio">
                  <el-radio :model-value="selectedPaymentMethod" :value="1" @change="selectPaymentMethod(1)">
                    <div class="method-info">
                      <div class="method-icon">
                        <img src="https://via.placeholder.com/48x48?text=微信" alt="微信支付" />
                      </div>
                      <div class="method-details">
                        <div class="method-name">微信支付</div>
                        <div class="method-desc">推荐使用，安全快捷</div>
                      </div>
                    </div>
                  </el-radio>
                </div>
              </div>

              <div class="payment-method" :class="{ selected: selectedPaymentMethod === 2 }" @click="selectPaymentMethod(2)">
                <div class="method-radio">
                  <el-radio :model-value="selectedPaymentMethod" :value="2" @change="selectPaymentMethod(2)">
                    <div class="method-info">
                      <div class="method-icon">
                        <img src="https://via.placeholder.com/48x48?text=支付宝" alt="支付宝" />
                      </div>
                      <div class="method-details">
                        <div class="method-name">支付宝</div>
                        <div class="method-desc">快速到账，支持花呗</div>
                      </div>
                    </div>
                  </el-radio>
                </div>
              </div>
            </div>

            <!-- 模拟支付区域 -->
            <div class="mock-payment" v-if="showMockPayment">
              <div class="mock-header">
                <h4>{{ selectedPaymentMethod === 1 ? '微信支付' : '支付宝' }}</h4>
                <div class="mock-amount">¥{{ orderDetail?.payAmount }}</div>
              </div>

              <div class="mock-qr" v-if="selectedPaymentMethod === 1">
                <div class="qr-placeholder">
                  <el-icon size="80"><CreditCard /></el-icon>
                  <p>模拟微信支付二维码</p>
                  <p class="mock-tip">点击"确认支付"模拟支付成功</p>
                </div>
              </div>

              <div class="mock-form" v-if="selectedPaymentMethod === 2">
                <div class="form-placeholder">
                  <el-icon size="80"><Money /></el-icon>
                  <p>模拟支付宝支付页面</p>
                  <p class="mock-tip">点击"确认支付"模拟支付成功</p>
                </div>
              </div>
            </div>

            <div class="payment-actions">
              <el-button size="large" @click="cancelPayment">取消支付</el-button>
              <el-button
                type="primary"
                size="large"
                :loading="paying"
                :disabled="!selectedPaymentMethod || timeRemaining <= 0"
                @click="confirmPayment"
              >
                {{ paying ? '支付中...' : '确认支付' }}
              </el-button>
            </div>
          </div>

          <!-- 支付提示 -->
          <div class="payment-notice">
            <el-alert
              title="支付提醒"
              description="请在规定时间内完成支付，超时订单将自动取消。如遇支付问题，请联系客服。"
              type="warning"
              :closable="false"
              show-icon
            />
          </div>

        </div>
      </div>
    </div>

    <!-- 支付成功弹窗 -->
    <el-dialog
      v-model="successDialogVisible"
      title="支付成功"
      width="400px"
      center
      :close-on-click-modal="false"
      :show-close="false"
    >
      <div class="success-content">
        <div class="success-icon">
          <el-icon size="64" color="#52c41a"><SuccessFilled /></el-icon>
        </div>
        <div class="success-text">
          <p>支付成功！</p>
          <p class="order-info">订单号：{{ orderDetail?.orderSn || '' }}</p>
          <p class="amount-info">支付金额：¥{{ orderDetail?.payAmount || 0 }}</p>
        </div>
      </div>

      <template #footer>
        <el-button type="primary" @click="goToETicket" block>
          查看电子票
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
  SuccessFilled,
  CreditCard,
  Money
} from '@element-plus/icons-vue'
import { getOrderDetail, payOrder } from '@/user/api'
import type { OrderVO } from '@/types/user'

const router = useRouter()
const route = useRoute()

// 订单详情
const orderDetail = ref<OrderVO | null>(null)

// 选中的支付方式
const selectedPaymentMethod = ref(1)

// 支付状态
const paying = ref(false)
const showMockPayment = ref(false)
const successDialogVisible = ref(false)

// 倒计时
const timeRemaining = ref(15 * 60) // 15分钟
let countdownTimer: number | null = null

// 获取订单详情
const fetchOrderDetail = async () => {
  try {
    const query = route.query
    const orderId = decodeURIComponent(query.orderId as string)

    console.log('Payment - 解码前orderId:', query.orderId)
    console.log('Payment - 解码后orderId:', orderId)

    const response = await getOrderDetail(orderId)
    if (response.data.code === 200) {
      orderDetail.value = response.data.data
      // 如果订单已支付，跳转到电子票页面
      if (orderDetail.value && orderDetail.value.orderStatus !== 0) {
        ElMessage.warning('订单状态异常')
        router.back()
        return
      }
      startCountdown()
    } else {
      ElMessage.error(response.data.message || '获取订单详情失败')
      router.back()
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
    router.back()
  }
}

// 开始倒计时
const startCountdown = () => {
  if (orderDetail.value?.expireTime) {
    const expireTime = new Date(orderDetail.value.expireTime).getTime()
    const now = Date.now()
    timeRemaining.value = Math.max(0, Math.floor((expireTime - now) / 1000))

    countdownTimer = setInterval(() => {
      timeRemaining.value--
      if (timeRemaining.value <= 0) {
        clearInterval(countdownTimer!)
        countdownTimer = null
        ElMessage.warning('订单已过期，请重新下单')
        router.back()
      }
    }, 1000)
  }
}

// 选择支付方式
const selectPaymentMethod = (method: number) => {
  selectedPaymentMethod.value = method
  showMockPayment.value = true
}

// 确认支付
const confirmPayment = async () => {
  if (!selectedPaymentMethod.value) {
    ElMessage.warning('请选择支付方式')
    return
  }

  if (timeRemaining.value <= 0) {
    ElMessage.warning('订单已过期')
    return
  }

  try {
    paying.value = true

    // 模拟支付延迟
    await new Promise(resolve => setTimeout(resolve, 2000))

    const request = {
      orderId: orderDetail.value!.id,
      payType: selectedPaymentMethod.value
    }

    const response = await payOrder(request)
    if (response.data.code === 200) {
      // 支付成功
      orderDetail.value = response.data.data
      successDialogVisible.value = true
    } else {
      ElMessage.error(response.data.message || '支付失败')
    }
  } catch (error) {
    console.error('支付失败:', error)
    ElMessage.error('支付失败')
  } finally {
    paying.value = false
  }
}

// 取消支付
const cancelPayment = () => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
  router.back()
}

// 查看电子票
const goToETicket = () => {
  router.push({
    path: '/e-ticket',
    query: {
      orderId: encodeURIComponent(String(orderDetail.value?.id)) // 确保订单ID作为字符串传递并编码
    }
  })
}

// 返回上一页
const goBack = () => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
  router.back()
}

// 格式化时间
const formatTime = (seconds: number) => {
  const minutes = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
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

// 组件卸载时清理定时器
onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
})

onMounted(() => {
  fetchOrderDetail()
})
</script>

<style scoped>
.payment-page {
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

.step.completed + .step-line {
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

/* 订单信息卡片 */
.order-info-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.card-header h2 {
  margin: 0;
  color: #333;
  font-size: 20px;
  font-weight: 600;
}

.order-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  text-align: right;
  font-size: 14px;
}

.order-sn {
  color: #333;
  font-weight: 500;
}

.expire-time {
  color: #f5222d;
  font-weight: 600;
}

.expired {
  color: #ff4d4f;
  font-weight: 600;
}

.order-summary {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  font-size: 16px;
}

.summary-row .label {
  color: #666;
}

.summary-row .value {
  color: #333;
  font-weight: 500;
}

.summary-row.total {
  border-top: 1px solid #e8e8e8;
  padding-top: 16px;
  margin-top: 16px;
  font-size: 18px;
  font-weight: 600;
}

.summary-row.total .value {
  color: #f5222d;
  font-size: 24px;
}

/* 支付方式卡片 */
.payment-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.payment-card .card-header h3 {
  margin: 0 0 20px;
  color: #333;
  font-size: 18px;
  font-weight: 600;
}

.payment-methods {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

.payment-method {
  border: 2px solid #e8e8e8;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.payment-method:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.1);
}

.payment-method.selected {
  border-color: #409eff;
  background: #f0f8ff;
}

.method-radio {
  width: 100%;
}

.method-info {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
}

.method-icon img {
  width: 48px;
  height: 48px;
  border-radius: 8px;
}

.method-details {
  flex: 1;
}

.method-name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.method-desc {
  font-size: 14px;
  color: #999;
}

/* 模拟支付区域 */
.mock-payment {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 24px;
  background: #fafafa;
}

.mock-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.mock-header h4 {
  margin: 0;
  color: #333;
  font-size: 16px;
  font-weight: 600;
}

.mock-amount {
  font-size: 20px;
  font-weight: 700;
  color: #f5222d;
}

.mock-qr,
.mock-form {
  text-align: center;
  padding: 40px 20px;
}

.qr-placeholder,
.form-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.qr-placeholder p,
.form-placeholder p {
  margin: 0;
  color: #666;
  font-size: 16px;
}

.mock-tip {
  color: #999;
  font-size: 14px;
}

/* 支付操作 */
.payment-actions {
  display: flex;
  gap: 16px;
  justify-content: flex-end;
}

/* 支付提示 */
.payment-notice {
  margin-top: 16px;
}

/* 成功弹窗 */
.success-content {
  text-align: center;
  padding: 20px 0;
}

.success-icon {
  margin-bottom: 20px;
}

.success-text p {
  margin: 0 0 12px;
  font-size: 16px;
  color: #333;
}

.order-info,
.amount-info {
  color: #666;
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

  .order-info-card,
  .payment-card {
    padding: 16px;
  }

  .card-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .order-meta {
    text-align: left;
  }

  .payment-actions {
    flex-direction: column;
  }

  .payment-actions .el-button {
    width: 100%;
  }

  .method-info {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }
}
</style>
