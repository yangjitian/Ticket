<template>
  <div class="book-tickets-page">
    <div class="container">
      <div class="booking-success">
        <div class="success-icon">
          <el-icon size="64" color="#52c41a"><SuccessFilled /></el-icon>
        </div>
        <h1 class="success-title">预订成功！</h1>
        <p class="success-message">您的门票已预订成功，请在游玩当天出示订单二维码入园</p>

        <div class="order-info">
          <div class="info-item">
            <span class="label">订单编号：</span>
            <span class="value">{{ orderNumber }}</span>
          </div>
          <div class="info-item">
            <span class="label">景区名称：</span>
            <span class="value">{{ orderInfo.spotName }}</span>
          </div>
          <div class="info-item">
            <span class="label">游玩日期：</span>
            <span class="value">{{ formatDate(orderInfo.visitDate) }}</span>
          </div>
          <div class="info-item">
            <span class="label">门票类型：</span>
            <span class="value">{{ orderInfo.ticketName }}</span>
          </div>
          <div class="info-item">
            <span class="label">购买数量：</span>
            <span class="value">{{ orderInfo.quantity }} 张</span>
          </div>
          <div class="info-item total">
            <span class="label">订单金额：</span>
            <span class="value">￥{{ orderInfo.totalPrice }}</span>
          </div>
        </div>

        <div class="visitors-info">
          <h3>游客信息</h3>
          <div class="visitors-list">
            <div
              v-for="(visitor, index) in orderInfo.visitors"
              :key="index"
              class="visitor-item"
            >
              <div class="visitor-name">游客 {{ index + 1 }}: {{ visitor.visitorName }}</div>
              <div class="visitor-detail">身份证: {{ visitor.idCard }}</div>
              <div class="visitor-detail">联系电话: {{ visitor.contactTel }}</div>
            </div>
          </div>
        </div>

        <div class="action-buttons">
          <el-button @click="goHome" size="large">返回首页</el-button>
          <el-button type="primary" @click="viewOrders" size="large">查看订单</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { SuccessFilled } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

// 订单信息
const orderInfo = reactive({
  spotId: '',
  spotName: '',
  ticketId: '',
  ticketName: '',
  visitDate: '',
  quantity: 1,
  unitPrice: 0,
  totalPrice: 0,
  visitors: [] as Array<{
    visitorName: string
    idCard: string
    contactTel: string
  }>
})

// 订单编号（模拟）
const orderNumber = ref('')

// 初始化订单信息
const initOrderInfo = () => {
  const orderData = route.query.data as string
  if (orderData) {
    try {
      const parsed = JSON.parse(decodeURIComponent(orderData))
      Object.assign(orderInfo, parsed)

      // 生成模拟订单编号
      orderNumber.value = `ORD${Date.now()}${Math.random().toString(36).substr(2, 6).toUpperCase()}`
    } catch (error) {
      console.error('解析订单数据失败:', error)
      // 如果解析失败，使用模拟数据
      generateMockOrder()
    }
  } else {
    // 如果没有传递数据，使用模拟数据
    generateMockOrder()
  }
}

// 生成模拟订单数据
const generateMockOrder = () => {
  orderInfo.spotId = '1'
  orderInfo.spotName = '模拟景区'
  orderInfo.ticketId = '1'
  orderInfo.ticketName = '成人票'
  orderInfo.visitDate = new Date().toISOString().split('T')[0]
  orderInfo.quantity = 2
  orderInfo.unitPrice = 100
  orderInfo.totalPrice = 200
  orderInfo.visitors = [
    {
      visitorName: '张三',
      idCard: '110101199001011234',
      contactTel: '13800138001'
    },
    {
      visitorName: '李四',
      idCard: '110101199001015678',
      contactTel: '13800138002'
    }
  ]
  orderNumber.value = `ORD${Date.now()}MOCK`
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

// 返回首页
const goHome = () => {
  router.push('/')
}

// 查看订单
const viewOrders = () => {
  router.push('/orders')
}

onMounted(() => {
  initOrderInfo()
})
</script>

<style scoped>
.book-tickets-page {
  min-height: 100vh;
  background: #f8f9fa;
  padding: 40px 0;
}

.container {
  max-width: 600px;
  margin: 0 auto;
  padding: 0 20px;
}

.booking-success {
  background: white;
  border-radius: 16px;
  padding: 40px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.success-icon {
  margin-bottom: 24px;
}

.success-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.success-message {
  color: #666;
  font-size: 16px;
  margin-bottom: 32px;
  line-height: 1.5;
}

.order-info {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 32px;
  text-align: left;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #e8e8e8;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item.total {
  border-top: 2px solid #409eff;
  padding-top: 16px;
  margin-top: 8px;
  font-weight: 600;
  color: #f5222d;
}

.label {
  color: #666;
  font-weight: 500;
}

.value {
  color: #333;
  font-weight: 600;
}

.visitors-info {
  margin-bottom: 32px;
  text-align: left;
}

.visitors-info h3 {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #409eff;
}

.visitors-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.visitor-item {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #e8e8e8;
}

.visitor-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.visitor-detail {
  color: #666;
  font-size: 14px;
  margin-bottom: 4px;
}

.visitor-detail:last-child {
  margin-bottom: 0;
}

.action-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
}

@media (max-width: 768px) {
  .container {
    padding: 0 15px;
  }

  .booking-success {
    padding: 24px;
  }

  .success-title {
    font-size: 20px;
  }

  .action-buttons {
    flex-direction: column;
  }

  .order-info {
    padding: 16px;
  }

  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}
</style>
