<template>
  <div class="spot-detail-page">
    <div class="container">
      <!-- 景区基本信息 -->
      <div class="spot-header">
        <h1 class="spot-title">{{ spot.spotName }}</h1>
        <div class="spot-meta">
          <span class="spot-level">5A级景区</span>
          <span class="spot-address">
            <el-icon><Location /></el-icon>
            {{ spot.address }}
          </span>
          <span class="spot-status" :class="{ 'closed': spot.spotStatus === 0 }">
            {{ spot.spotStatus === 1 ? '开放中' : '已关闭' }}
          </span>
        </div>
      </div>

      <div class="content-wrapper">
        <!-- 左侧：景区详情 -->
        <div class="spot-detail">
          <!-- 图片轮播 -->
          <div class="image-carousel">
            <el-carousel
              v-if="spot.imageUrls && spot.imageUrls.length > 0"
              :interval="4000"
              type="card"
              height="400px"
              arrow="always"
            >
              <el-carousel-item
                v-for="(imageUrl, index) in spot.imageUrls"
                :key="index"
              >
                <img :src="imageUrl" :alt="`图片${index + 1}`" @error="handleImageError" />
              </el-carousel-item>
            </el-carousel>
            <div v-else class="no-images">
              <el-icon size="80"><Picture /></el-icon>
              <p>暂无图片</p>
            </div>
          </div>

          <!-- 景区介绍 -->
          <div class="spot-description">
            <h2>景区介绍</h2>
            <div class="description-content">
              <p v-if="spot.description">{{ spot.description }}</p>
              <p v-else>暂无详细介绍</p>
            </div>
          </div>

          <!-- 开放时间 -->
          <div class="spot-hours">
            <h2>开放时间</h2>
            <p>{{ formatBusinessHours(spot.openTime) }}</p>
          </div>

          <!-- 联系方式 -->
          <div class="spot-contact">
            <h2>联系方式</h2>
            <p>
              <el-icon><Phone /></el-icon>
              {{ spot.contactNum || '暂无联系电话' }}
            </p>
          </div>
        </div>

        <!-- 右侧：预订栏 -->
        <div class="booking-sidebar">
          <div class="booking-card">
            <h3>立即预订</h3>

            <!-- 票种选择 -->
            <div class="ticket-selection">
              <h4>选择票种</h4>
              <el-radio-group v-model="selectedTicketType" @change="handleTicketTypeChange">
                <el-radio
                  v-for="ticket in availableTickets"
                  :key="ticket.id"
                  :value="ticket.id"
                  class="ticket-option"
                >
                  <div class="ticket-info">
                    <span class="ticket-name">{{ ticket.productName }}</span>
                    <span class="ticket-price">￥{{ ticket.priceRetail }}</span>
                  </div>
                </el-radio>
              </el-radio-group>
            </div>

            <!-- 日期选择 -->
            <div class="date-selection">
              <h4>选择日期</h4>
              <div class="calendar-wrapper">
                <CalendarPicker
                  v-model="selectedDate"
                  :stock-data="stockData"
                  @date-select="handleDateSelect"
                />
              </div>
            </div>

            <!-- 数量选择 -->
            <div class="quantity-selection" v-if="selectedDate">
              <h4>选择数量</h4>
              <el-input-number
                v-model="ticketQuantity"
                :min="1"
                :max="selectedStock?.availableQty || 0"
                :disabled="!selectedStock"
                style="width: 100%"
              />
              <p v-if="selectedStock" class="stock-info">
                剩余库存：{{ selectedStock.availableQty }}张
              </p>
            </div>

            <!-- 价格总计 -->
            <div class="price-summary" v-if="selectedTicket && selectedDate">
              <div class="price-row">
                <span>票价</span>
                <span>￥{{ selectedTicket.priceRetail }}</span>
              </div>
              <div class="price-row">
                <span>数量</span>
                <span>×{{ ticketQuantity }}</span>
              </div>
              <div class="price-row total">
                <span>总价</span>
                <span>￥{{ totalPrice }}</span>
              </div>
            </div>

            <!-- 预订按钮 -->
            <el-button
              type="primary"
              size="large"
              :disabled="!canBook"
              @click="handleBookNow"
              style="width: 100%; margin-top: 20px;"
            >
              {{ isLoggedIn ? '立即预订' : '登录后预订' }}
            </el-button>

            <p class="booking-notice">
              <el-icon><InfoFilled /></el-icon>
              预订成功后，请在游玩当天出示身份证和订单二维码入园
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- 登录提示对话框 -->
    <el-dialog
      v-model="loginDialogVisible"
      title="需要登录"
      width="400px"
      center
    >
      <p>预订门票需要先登录您的账号</p>
      <template #footer>
        <el-button @click="loginDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="goToLogin">去登录</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Location,
  Phone,
  Picture,
  InfoFilled
} from '@element-plus/icons-vue'
import { getScenicSpotDetail, getTicketProducts, getTicketStock } from '@/user/api'
import CalendarPicker from '@/components/CalendarPicker.vue'
import type {
  ScenicSpot,
  TicketProduct,
  TicketStock,
  CalendarStockInfo
} from '@/types/user'

const router = useRouter()
const route = useRoute()

// 景区信息
const spot = ref<ScenicSpot | null>(null)

// 票务相关
const availableTickets = ref<TicketProduct[]>([])
const selectedTicketType = ref('')
const selectedTicket = computed(() =>
  availableTickets.value.find(t => t.id === selectedTicketType) || null
)

// 日期和库存
const selectedDate = ref('')
const stockData = ref<CalendarStockInfo[]>([])
const selectedStock = computed(() =>
  stockData.value.find(s => s.date === selectedDate.value) || null
)

// 数量选择
const ticketQuantity = ref(1)

// 总价计算
const totalPrice = computed(() => {
  if (!selectedTicket.value) return 0
  return selectedTicket.value.priceRetail * ticketQuantity.value
})

// 是否可以预订
const canBook = computed(() => {
  return selectedTicket.value && selectedDate.value && selectedStock.value?.isAvailable && isLoggedIn.value
})

// 登录状态
const isLoggedIn = computed(() => !!localStorage.getItem('token'))

// 对话框
const loginDialogVisible = ref(false)

// 获取景区详情
const fetchSpotDetail = async () => {
  try {
    const spotId = route.params.id as string
    const response = await getScenicSpotDetail(spotId)

    if (response.data.code === 200) {
      spot.value = response.data.data
      // 获取票务信息
      await fetchTicketProducts()
    } else {
      ElMessage.error(response.data.message || '获取景区详情失败')
    }
  } catch (error) {
    console.error('获取景区详情失败:', error)
    ElMessage.error('获取景区详情失败')
  }
}

// 获取票务产品
const fetchTicketProducts = async () => {
  if (!spot.value) return

  try {
    // TODO: 实现后端API
    // const response = await getTicketProducts(spot.value.id)
    // availableTickets.value = response.data.data || []

    // 暂时使用模拟数据
    availableTickets.value = [
      {
        id: '1',
        spotId: spot.value.id,
        productName: '成人票',
        ticketType: 1,
        priceRetail: 80,
        priceSettlement: 70,
        saleStatus: 1
      },
      {
        id: '2',
        spotId: spot.value.id,
        productName: '儿童票',
        ticketType: 2,
        priceRetail: 40,
        priceSettlement: 35,
        saleStatus: 1
      }
    ]
  } catch (error) {
    console.error('获取票务信息失败:', error)
    ElMessage.error('获取票务信息失败')
  }
}

// 获取库存数据
const fetchStockData = async () => {
  if (!selectedTicket.value) return

  try {
    // TODO: 实现后端API获取日历库存
    // 暂时使用模拟数据
    const mockStockData: CalendarStockInfo[] = []
    const today = new Date()

    for (let i = 0; i < 30; i++) {
      const date = new Date(today)
      date.setDate(today.getDate() + i)
      const dateStr = date.toISOString().split('T')[0]

      // 模拟库存：过去日期不可用，未来日期随机库存
      const isPast = date < today
      const availableQty = isPast ? 0 : Math.floor(Math.random() * 100) + 1

      mockStockData.push({
        date: dateStr,
        availableQty,
        price: selectedTicket.value.priceRetail,
        isAvailable: !isPast && availableQty > 0
      })
    }

    stockData.value = mockStockData
  } catch (error) {
    console.error('获取库存数据失败:', error)
  }
}

// 处理票种选择
const handleTicketTypeChange = () => {
  selectedDate.value = ''
  ticketQuantity.value = 1
  fetchStockData()
}

// 处理日期选择
const handleDateSelect = (date: string) => {
  selectedDate.value = date
  ticketQuantity.value = 1
}

// 处理预订
const handleBookNow = () => {
  if (!isLoggedIn.value) {
    loginDialogVisible.value = true
    return
  }

  if (!canBook.value) {
    ElMessage.warning('请选择完整的预订信息')
    return
  }

  // 跳转到确认订单页
  const orderData = {
    spotId: spot.value!.id,
    ticketId: selectedTicket.value!.id,
    visitDate: selectedDate.value,
    quantity: ticketQuantity.value,
    totalPrice: totalPrice.value
  }

  router.push({
    path: '/checkout',
    query: {
      data: encodeURIComponent(JSON.stringify(orderData))
    }
  })
}

// 跳转到登录页
const goToLogin = () => {
  loginDialogVisible.value = false
  router.push('/login')
}

// 格式化营业时间
const formatBusinessHours = (openTimeText: string) => {
  if (!openTimeText || openTimeText === '未设置') return '暂未设置开放时间'
  return openTimeText
}

// 处理图片加载错误
const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.style.display = 'none'
}

onMounted(() => {
  fetchSpotDetail()
})

// 监听票种变化
watch(selectedTicketType, () => {
  if (selectedTicketType.value) {
    fetchStockData()
  }
})
</script>

<style scoped>
.spot-detail-page {
  padding: 40px 0;
  background: #f8f9fa;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 景区标题 */
.spot-header {
  margin-bottom: 40px;
}

.spot-title {
  font-size: 2.5rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 1rem;
}

.spot-meta {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.spot-level {
  background: #faad14;
  color: white;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
}

.spot-address {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #666;
  font-size: 1rem;
}

.spot-status {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
  background: #52c41a;
  color: white;
}

.spot-status.closed {
  background: #f5222d;
}

/* 内容布局 */
.content-wrapper {
  display: grid;
  grid-template-columns: 1fr 350px;
  gap: 40px;
}

/* 景区详情 */
.spot-detail {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 图片轮播 */
.image-carousel {
  height: 400px;
  overflow: hidden;
}

.image-carousel .el-carousel {
  height: 100%;
}

.image-carousel img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-images {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  background: #f5f5f5;
  color: #999;
}

/* 景区介绍等内容 */
.spot-description,
.spot-hours,
.spot-contact {
  padding: 30px;
  border-bottom: 1px solid #f0f0f0;
}

.spot-description:last-child,
.spot-hours:last-child,
.spot-contact:last-child {
  border-bottom: none;
}

.spot-description h2,
.spot-hours h2,
.spot-contact h2 {
  font-size: 1.3rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
}

.spot-description p,
.spot-hours p {
  color: #666;
  line-height: 1.6;
}

.spot-contact p {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
}

/* 预订侧边栏 */
.booking-sidebar {
  position: sticky;
  top: 20px;
  height: fit-content;
}

.booking-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.booking-card h3 {
  font-size: 1.4rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 24px;
  text-align: center;
}

/* 票种选择 */
.ticket-selection,
.date-selection,
.quantity-selection {
  margin-bottom: 24px;
}

.ticket-selection h4,
.date-selection h4,
.quantity-selection h4 {
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.ticket-option {
  width: 100%;
  margin-bottom: 8px;
}

.ticket-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 8px 0;
}

.ticket-name {
  font-weight: 500;
}

.ticket-price {
  color: #f5222d;
  font-weight: 600;
}

/* 日历包装器 */
.calendar-wrapper {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
}

/* 数量选择 */
.stock-info {
  font-size: 0.9rem;
  color: #666;
  margin-top: 8px;
}

/* 价格总计 */
.price-summary {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 0.9rem;
}

.price-row.total {
  border-top: 1px solid #e8e8e8;
  padding-top: 12px;
  margin-top: 12px;
  font-size: 1rem;
  font-weight: 600;
  color: #f5222d;
}

.price-row:last-child {
  margin-bottom: 0;
}

/* 预订提示 */
.booking-notice {
  font-size: 0.8rem;
  color: #999;
  margin-top: 16px;
  display: flex;
  align-items: flex-start;
  gap: 6px;
  line-height: 1.4;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .content-wrapper {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .booking-sidebar {
    position: static;
    order: 2;
  }

  .booking-card {
    max-width: 500px;
    margin: 0 auto;
  }
}

@media (max-width: 768px) {
  .spot-detail-page {
    padding: 20px 0;
  }

  .container {
    padding: 0 15px;
  }

  .spot-title {
    font-size: 2rem;
  }

  .spot-meta {
    gap: 12px;
  }

  .image-carousel {
    height: 250px;
  }

  .spot-description,
  .spot-hours,
  .spot-contact {
    padding: 20px;
  }
}
</style>
