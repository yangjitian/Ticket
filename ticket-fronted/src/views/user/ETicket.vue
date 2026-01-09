<template>
  <div class="e-ticket-page">
    <!-- 头部 -->
    <div class="header">
      <div class="container">
        <div class="header-content">
          <h1>电子票</h1>
          <div class="order-info">
              <span>订单号：{{ orderDetail?.orderSn || '' }}</span>
            <span>状态：{{ getOrderStatusText(orderDetail?.orderStatus) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 主要内容 -->
    <div class="main-content">
      <div class="container">
        <div class="content-wrapper">

          <!-- 景区信息卡片 -->
          <div class="scenic-card">
            <div class="scenic-header">
              <div class="scenic-image">
                <img :src="orderDetail?.spotImage || defaultImage" :alt="orderDetail?.spotName || ''" />
              </div>
              <div class="scenic-info">
                <h2>{{ orderDetail?.spotName }}</h2>
                <div class="scenic-details">
                  <div class="detail-item">
                    <el-icon><Calendar /></el-icon>
                    <span>游玩日期：{{ orderDetail?.visitDate ? formatDate(orderDetail.visitDate) : '' }}</span>
                  </div>
                  <div class="detail-item">
                    <el-icon><Ticket /></el-icon>
                    <span>票种：{{ orderDetail?.ticketTypeName }}</span>
                  </div>
                  <div class="detail-item">
                    <el-icon><User /></el-icon>
                    <span>购票人数：{{ orderDetail?.buyNum }}人</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 电子票列表 -->
          <div class="tickets-section">
            <div class="section-header">
              <h3>电子票券</h3>
              <div class="verification-code">
                <span>核销码：{{ orderDetail?.verificationCode || '' }}</span>
                <el-button type="primary" size="small" @click="copyVerificationCode">
                  <el-icon><CopyDocument /></el-icon>
                  复制
                </el-button>
              </div>
            </div>

            <div class="tickets-list">
              <div
                v-for="(visitor, index) in orderDetail?.visitors"
                :key="index"
                class="ticket-card"
                :class="{ used: visitor.verifyStatus === 1 }"
              >
                <div class="ticket-header">
                  <div class="ticket-number">票券 {{ index + 1 }}</div>
                  <div class="ticket-status" :class="getStatusClass(visitor.verifyStatus)">
                    {{ getStatusText(visitor.verifyStatus) }}
                  </div>
                </div>

                <div class="ticket-body">
                  <div class="passenger-info">
                    <div class="passenger-name">{{ visitor.visitorName }}</div>
                    <div class="passenger-id">身份证：{{ visitor.idCard }}</div>
                    <div v-if="visitor.contactTel" class="passenger-phone">电话：{{ visitor.contactTel }}</div>
                  </div>

                  <!-- 模拟二维码区域（用核销码替代） -->
                  <div class="qr-code-section">
                    <div class="qr-placeholder">
                      <div class="verification-code-display">
                        <div class="code-text">{{ getIndividualCode(index) }}</div>
                        <div class="code-desc">核销码</div>
                      </div>
                      <div class="qr-note">
                        <p>请向工作人员出示此核销码</p>
                        <p>或告知订单核销码：{{ orderDetail?.verificationCode }}</p>
                      </div>
                    </div>
                  </div>
                </div>

                <div class="ticket-footer">
                  <div class="ticket-price">¥{{ orderDetail?.unitPrice }}</div>
                  <div class="ticket-time">
                    <div>购买时间：{{ orderDetail?.createTime ? formatDateTime(orderDetail.createTime) : '' }}</div>
                    <div v-if="visitor.verifyTime">核销时间：{{ visitor.verifyTime }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 使用说明 -->
          <div class="usage-notice">
            <h3>使用说明</h3>
            <div class="notice-content">
              <div class="notice-item">
                <el-icon><InfoFilled /></el-icon>
                <span>请在游玩当天携带有效身份证件前往景区检票口</span>
              </div>
              <div class="notice-item">
                <el-icon><InfoFilled /></el-icon>
                <span>向工作人员出示电子票或告知核销码进行检票入园</span>
              </div>
              <div class="notice-item">
                <el-icon><InfoFilled /></el-icon>
                <span>每张电子票只能使用一次，请妥善保管</span>
              </div>
              <div class="notice-item">
                <el-icon><InfoFilled /></el-icon>
                <span>如有疑问请咨询景区工作人员或拨打客服热线</span>
              </div>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="action-buttons">
            <el-button size="large" @click="goHome">返回首页</el-button>
            <el-button type="primary" size="large" @click="shareTicket">
              <el-icon><Share /></el-icon>
              分享给朋友
            </el-button>
          </div>

        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Calendar,
  Ticket,
  User,
  CopyDocument,
  InfoFilled,
  Share
} from '@element-plus/icons-vue'
import { getOrderDetail } from '@/user/api'
import type { OrderVO } from '@/types/user'

const router = useRouter()
const route = useRoute()

// 订单详情
const orderDetail = ref<OrderVO | null>(null)

// 默认图片
const defaultImage = 'https://via.placeholder.com/120x80?text=景区'

// 获取订单详情
const fetchOrderDetail = async () => {
  try {
    const query = route.query
    const orderId = decodeURIComponent(query.orderId as string)

    console.log('ETicket - 解码前orderId:', query.orderId)
    console.log('ETicket - 解码后orderId:', orderId)

    const response = await getOrderDetail(orderId)
    if (response.data.code === 200) {
      orderDetail.value = response.data.data
    } else {
      ElMessage.error(response.data.message || '获取订单详情失败')
      router.push('/')
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
    router.push('/')
  }
}

// 获取订单状态文本
const getOrderStatusText = (status?: number) => {
  switch (status) {
    case 0: return '待支付'
    case 1: return '已支付待核销'
    case 2: return '已核销'
    case 3: return '已取消'
    case 4: return '已退款'
    case 5: return '已过期'
    default: return '未知状态'
  }
}

// 获取票券状态文本
const getStatusText = (status?: number) => {
  return status === 1 ? '已使用' : '未使用'
}

// 获取票券状态样式类
const getStatusClass = (status?: number) => {
  return status === 1 ? 'used' : 'unused'
}

// 获取单个票券的核销码（模拟）
const getIndividualCode = (index: number) => {
  if (!orderDetail.value?.verificationCode) return ''
  return orderDetail.value.verificationCode + '-' + (index + 1).toString().padStart(2, '0')
}

// 复制核销码
const copyVerificationCode = async () => {
  try {
    await navigator.clipboard.writeText(orderDetail.value?.verificationCode || '')
    ElMessage.success('核销码已复制到剪贴板')
  } catch (error) {
    // 降级处理
    const textArea = document.createElement('textarea')
    textArea.value = orderDetail.value?.verificationCode || ''
    document.body.appendChild(textArea)
    textArea.select()
    document.execCommand('copy')
    document.body.removeChild(textArea)
    ElMessage.success('核销码已复制到剪贴板')
  }
}

// 返回首页
const goHome = () => {
  router.push('/')
}

// 分享电子票
const shareTicket = () => {
        if (navigator.share) {
          const info = orderDetail.value
          navigator.share({
            title: '电子票',
            text: `我的${info?.spotName || ''}电子票，订单号：${info?.orderSn || ''}`,
            url: window.location.href
          })
  } else {
    copyVerificationCode()
    ElMessage.success('分享链接已复制，请手动分享给朋友')
  }
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
    minute: '2-digit'
  })
}

onMounted(() => {
  fetchOrderDetail()
})
</script>

<style scoped>
.e-ticket-page {
  min-height: 100vh;
  background: #f8f9fa;
}

/* 头部 */
.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 40px 0 60px;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 20px;
}

.header-content {
  text-align: center;
}

.header-content h1 {
  margin: 0 0 16px;
  font-size: 32px;
  font-weight: 700;
}

.order-info {
  display: flex;
  justify-content: center;
  gap: 32px;
  font-size: 16px;
}

.order-info span {
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
}

/* 主要内容 */
.main-content {
  margin-top: -40px;
  padding-bottom: 40px;
}

.content-wrapper {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 景区信息卡片 */
.scenic-card {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.scenic-header {
  display: flex;
  gap: 24px;
  align-items: center;
}

.scenic-image img {
  width: 120px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
}

.scenic-info h2 {
  margin: 0 0 16px;
  color: #333;
  font-size: 24px;
  font-weight: 600;
}

.scenic-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 16px;
}

/* 电子票列表 */
.tickets-section {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h3 {
  margin: 0;
  color: #333;
  font-size: 20px;
  font-weight: 600;
}

.verification-code {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  font-weight: 600;
  color: #f5222d;
}

.tickets-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.ticket-card {
  border: 2px solid #e8e8e8;
  border-radius: 12px;
  padding: 24px;
  background: white;
  transition: all 0.3s ease;
}

.ticket-card.used {
  border-color: #52c41a;
  background: #f6ffed;
}

.ticket-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.ticket-number {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.ticket-status {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
}

.ticket-status.unused {
  background: #e6f7ff;
  color: #1890ff;
}

.ticket-status.used {
  background: #f6ffed;
  color: #52c41a;
}

.ticket-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.passenger-info {
  flex: 1;
}

.passenger-name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.passenger-id,
.passenger-phone {
  color: #666;
  margin-bottom: 4px;
  font-size: 14px;
}

.qr-code-section {
  flex-shrink: 0;
  margin-left: 40px;
}

.qr-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  min-width: 200px;
}

.verification-code-display {
  text-align: center;
}

.code-text {
  font-size: 20px;
  font-weight: 700;
  color: #f5222d;
  letter-spacing: 2px;
  margin-bottom: 8px;
}

.code-desc {
  font-size: 12px;
  color: #999;
}

.qr-note {
  text-align: center;
  font-size: 12px;
  color: #666;
}

.qr-note p {
  margin: 4px 0;
}

.ticket-footer {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.ticket-price {
  font-size: 24px;
  font-weight: 700;
  color: #f5222d;
}

.ticket-time {
  text-align: right;
  font-size: 12px;
  color: #999;
}

.ticket-time div {
  margin-bottom: 4px;
}

/* 使用说明 */
.usage-notice {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.usage-notice h3 {
  margin: 0 0 20px;
  color: #333;
  font-size: 18px;
  font-weight: 600;
}

.notice-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.notice-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  color: #666;
  font-size: 16px;
  line-height: 1.5;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .container {
    padding: 0 15px;
  }

  .header {
    padding: 30px 0 50px;
  }

  .header-content h1 {
    font-size: 24px;
  }

  .order-info {
    flex-direction: column;
    gap: 12px;
  }

  .scenic-card,
  .tickets-section,
  .usage-notice {
    padding: 20px;
  }

  .scenic-header {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }

  .section-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .ticket-body {
    flex-direction: column;
    gap: 20px;
  }

  .qr-code-section {
    margin-left: 0;
  }

  .ticket-footer {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .action-buttons {
    flex-direction: column;
  }

  .action-buttons .el-button {
    width: 100%;
  }
}
</style>
