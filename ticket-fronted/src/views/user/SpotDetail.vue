<template>
  <div class="spot-detail-page">
    <!-- 核心展示区 -->
    <div class="hero-section">
      <div class="container">
        <div class="hero-content">
          <!-- 左侧：图片轮播和景区描述 -->
          <div class="left-content">
            <!-- 图片轮播 -->
            <div class="image-gallery">
              <el-carousel
                v-if="spot?.imageUrls && spot.imageUrls.length > 0"
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

            <!-- 景区描述 -->
            <div class="description-section">
              <h2 class="section-title">景区描述</h2>
              <div class="introduction-content" v-html="spot?.description || '暂无详细介绍'"></div>
            </div>
          </div>

          <!-- 右侧：预订面板 -->
          <div class="booking-panel">
            <div class="booking-card">
              <!-- 标题区 -->
              <div class="title-section">
                <h1 class="spot-title">{{ spot?.spotName || '景区详情' }}</h1>
                <div class="title-badges">
                  <span v-if="spot?.level" class="badge level-badge">{{ spot.level }}景区</span>
                  <span v-if="spot?.spotStatus === 1" class="badge status-badge open">营业中</span>
                  <span v-else class="badge status-badge closed">已关闭</span>
                </div>
              </div>

              <!-- 基础信息区 -->
              <div class="meta-info">
                <div class="meta-item">
                  <el-icon><Location /></el-icon>
                  <span>{{ spot?.address || '暂无地址信息' }}</span>
                </div>
                <div class="meta-item">
                  <el-icon><Clock /></el-icon>
                  <span>{{ formatBusinessHours(spot?.openTime || '') }}</span>
                </div>
                <div class="meta-item">
                  <el-icon><Phone /></el-icon>
                  <span>{{ spot?.contactNum || '暂无联系电话' }}</span>
                </div>
              </div>

              <!-- 价格展示 -->
              <div class="price-display" v-if="minPrice">
                <span class="price-prefix">￥</span>
                <span class="price-amount">{{ minPrice }}</span>
                <span class="price-suffix">起</span>
              </div>

              <!-- 购票选择器 -->
              <div class="booking-form">
                <!-- 游玩日期选择 -->
                <div class="form-group">
                  <label class="form-label">游玩日期</label>
                  <div class="date-selector">
                    <div class="date-tabs">
                      <div
                        v-for="(date, index) in dateOptions"
                        :key="date.date"
                        class="date-tab"
                        :class="{
                          active: selectedDate === date.date,
                          available: date.available,
                          unavailable: !date.available
                        }"
                        @click="selectDate(date.date)"
                      >
                        <div class="date-info">
                          <div class="date-week">{{ date.week }}</div>
                          <div class="date-day">{{ date.day }}</div>
                        </div>
                        <div class="date-stock" v-if="date.available">余{{ date.stock }}</div>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 门票类型 -->
                <div class="form-group">
                  <label class="form-label">门票类型</label>
                    <el-radio-group v-model="selectedTicketType" @change="handleTicketTypeChange">
                    <div class="ticket-options">
                      <el-radio
                        v-for="ticket in availableTickets"
                        :key="ticket.id"
                        :value="ticket.id"
                        class="ticket-card"
                      >
                        <div class="ticket-content">
                          <div class="ticket-name" :class="getTicketTypeClass(ticket.ticketType)">
                            {{ getTicketTypeLabel(ticket.ticketType) }}
                          </div>
                          <div class="ticket-price">
                            <span v-if="ticket.priceRetail > 0">￥{{ ticket.priceRetail }}</span>
                            <span v-else>免费</span>
                          </div>
                        </div>
                      </el-radio>
                    </div>
                  </el-radio-group>
                </div>


                <!-- 操作按钮 -->
                <el-button
                  type="primary"
                  size="large"
                  :disabled="!canBook"
                  @click="handleBookNow"
                  class="book-button"
                >
                  {{ isLoggedIn ? '立即预订' : '登录后预订' }}
                </el-button>

                <div class="booking-tips">
                  <el-icon><InfoFilled /></el-icon>
                  <span>预订成功后，请在游玩当天出示身份证和订单二维码入园</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 详情内容区 -->
    <div class="content-section">
      <div class="container">
        <div class="content-wrapper">
          <!-- 主内容区 -->
          <div class="main-content">
            <el-tabs v-model="activeTab" class="detail-tabs">
              <!-- 购票须知 -->
              <el-tab-pane label="购票须知" name="notice">
                <div class="tab-content">
                  <div class="notice-content">
                    <div class="notice-item">
                      <h4>1. 票种说明</h4>
                      <div class="ticket-types">
                        <div class="ticket-type-item">
                          <span class="ticket-type-name">成人票：</span>
                          <span class="ticket-type-desc">原价票，适用于18-59周岁成人</span>
                        </div>
                        <div class="ticket-type-item">
                          <span class="ticket-type-name">学生票：</span>
                          <span class="ticket-type-desc">凭学生证半价优惠，适用于在校的全日制大学生</span>
                        </div>
                        <div class="ticket-type-item">
                          <span class="ticket-type-name">老人票：</span>
                          <span class="ticket-type-desc">凭身份证半价优惠，适用于60周岁以上老人</span>
                        </div>
                        <div class="ticket-type-item">
                          <span class="ticket-type-name">儿童票：</span>
                          <span class="ticket-type-desc">凭身份证半价优惠，适用于6~17岁儿童，6岁以下免票</span>
                        </div>
                        <div class="ticket-type-item">
                          <span class="ticket-type-name">军人票：</span>
                          <span class="ticket-type-desc">凭军人证免费，适用于现役军人</span>
                        </div>
                        <div class="ticket-type-item">
                          <span class="ticket-type-name">残疾人票：</span>
                          <span class="ticket-type-desc">凭残疾人证免费，适用于残疾人及一名陪同人员</span>
                        </div>
                      </div>
                    </div>
                    <div class="notice-item">
                      <h4>2. 入园须知</h4>
                      <p>需携带本人有效身份证件入园，儿童需成人陪同。优惠票种请携带相应证件，检票时出示验证。</p>
                    </div>
                    <div class="notice-item">
                      <h4>3. 退改规则</h4>
                      <p>未使用门票可于游玩日期前2小时申请退款，逾期不支持退改。优惠票种退款需同时退还相关证件原件。</p>
                    </div>
                    <div class="notice-item">
                      <h4>4. 温馨提示</h4>
                      <p>请提前到达景区入口，避开高峰时段，注意个人财物安全。如有疑问请咨询工作人员。</p>
                    </div>
                    <div class="notice-item">
                      <h4>5. 其他说明</h4>
                      <p>门票一经使用，不支持退换；请妥善保管门票和证件，避免遗失。景区有权对优惠政策进行最终解释。</p>
                    </div>
                  </div>
                </div>
              </el-tab-pane>

              <!-- 交通指南 -->
              <el-tab-pane label="交通指南" name="transport">
                <div class="tab-content">
                  <div class="transport-content">
                    <div class="map-container">
                      <div id="map" class="map-view"></div>
                    </div>
                    <div class="transport-info">
                      <h4>地址信息</h4>
                      <p><el-icon><Location /></el-icon> {{ spot?.address || '暂无地址信息' }}</p>
                      <h4>交通方式</h4>
                      <div class="transport-options">
                        <div class="transport-option">
                          <el-icon><Van /></el-icon>
                          <span>建议自驾或乘坐公交前往</span>
                        </div>
                        <div class="transport-option">
                          <el-icon><Guide /></el-icon>
                          <span>距离最近的地铁站约2公里</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>

          <!-- 侧边栏 -->
          <div class="sidebar">
            <div class="recommendations">
              <h3>热门推荐</h3>
              <div class="recommend-list">
                <div
                  v-for="item in recommendations"
                  :key="item.id"
                  class="recommend-item"
                  @click="goToSpot(item.id)"
                >
                  <img :src="item.image" :alt="item.name" @error="handleImageError" />
                  <div class="recommend-info">
                    <h4>{{ item.name }}</h4>
                    <p>{{ item.location }}</p>
                    <div class="recommend-price">￥{{ item.price }}起</div>
                  </div>
                </div>
              </div>
            </div>
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
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Location,
  Phone,
  Picture,
  InfoFilled,
  Clock,
  Van,
  Guide
} from '@element-plus/icons-vue'
import { getScenicSpotDetail, getScenicStockOverview, getTicketProducts, getScenicSpotList } from '@/user/api'
import type {
  ScenicSpot,
  TicketProduct,
  CalendarStockInfo,
  ScenicStock
} from '@/types/user'

// 高德地图类型声明
declare global {
  interface Window {
    AMap: any
  }
}

const router = useRouter()
const route = useRoute()

// 景区信息
const spot = ref<ScenicSpot | null>(null)

// 图片相关
// 移除了单个图片显示和缩略图切换逻辑，现在使用轮播图

// 票务相关
const availableTickets = ref<TicketProduct[]>([])
const selectedTicketType = ref('')
const selectedTicket = computed(() =>
  availableTickets.value.find(t => t.id === selectedTicketType.value) || null
)

// 日期选择器（7日日历条）
const dateOptions = ref<Array<{
  date: string
  week: string
  day: string
  available: boolean
  stock: number
}>>([])

// 选择的日期
const selectedDate = ref('')

// 数量固定为1
const ticketQuantity = ref(1)

// 总价计算
const totalPrice = computed(() => {
  if (!selectedTicket.value) return 0
  return selectedTicket.value.priceRetail * ticketQuantity.value
})

// 最低价格
const minPrice = computed(() => {
  if (availableTickets.value.length === 0) return null
  return Math.min(...availableTickets.value.map(t => t.priceRetail))
})

// 是否可以预订
const canBook = computed(() => {
  return !!(selectedTicket.value && selectedDate.value)
})

// 登录状态
const isLoggedIn = computed(() => !!localStorage.getItem('user_token'))

// Tab切换
const activeTab = ref('notice')

// 对话框
const loginDialogVisible = ref(false)

// 推荐数据（模拟）
const recommendations = ref([
  {
    id: '1',
    name: '北京故宫',
    location: '北京市',
    price: 60,
    image: 'https://via.placeholder.com/200x120'
  },
  {
    id: '2',
    name: '长城',
    location: '北京市',
    price: 80,
    image: 'https://via.placeholder.com/200x120'
  },
  {
    id: '3',
    name: '颐和园',
    location: '北京市',
    price: 30,
    image: 'https://via.placeholder.com/200x120'
  }
])

// 生成7日日历条数据
const generateDateOptions = async () => {
  if (!spot.value?.id) return

  try {
    const response = await getScenicStockOverview(spot.value.id)
    if (response.data.code === 200) {
      const stockData: ScenicStock[] = response.data.data || []

      // 创建7天的日期选项
      const options = []
      const today = new Date()
      const weekDays = ['日', '一', '二', '三', '四', '五', '六']

      for (let i = 0; i < 7; i++) {
        const date = new Date(today)
        date.setDate(today.getDate() + i)

        const dateStr = date.toISOString().split('T')[0] as string
        const weekDay = weekDays[date.getDay()]
        const day = date.getDate().toString()

        // 查找对应的库存数据
        const stockInfo = stockData.find(stock => stock.stockDate === dateStr)
        const totalQty = stockInfo ? stockInfo.totalQty : 0
        const soldQty = stockInfo ? stockInfo.soldQty : 0
        const availableQty = Math.max(0, totalQty - soldQty)

        // 如果有库存且景区开放，则该日期可用
        const available = spot.value?.spotStatus === 1 && availableQty > 0

        options.push({
          date: dateStr,
          week: `周${weekDay}`,
          day,
          available,
          stock: availableQty
        })
      }

      dateOptions.value = options

      // 自动选择第一个可用的日期
      if (options.length > 0 && !selectedDate.value) {
        const firstAvailableDate = options.find(option => option.available)
        if (firstAvailableDate) {
          selectedDate.value = firstAvailableDate.date
        }
      }
    } else {
      // 如果API调用失败，使用默认数据（景区开放但无库存信息）
      generateDefaultDateOptions()
    }
  } catch (error) {
    console.error('获取库存数据失败:', error)
    // 如果API调用失败，使用默认数据
    generateDefaultDateOptions()
  }
}

// 生成默认的日期选项（用于API调用失败时的兜底）
const generateDefaultDateOptions = () => {
  const options = []
  const today = new Date()
  const weekDays = ['日', '一', '二', '三', '四', '五', '六']

  for (let i = 0; i < 7; i++) {
    const date = new Date(today)
    date.setDate(today.getDate() + i)

    const dateStr = date.toISOString().split('T')[0] as string
    const weekDay = weekDays[date.getDay()]
    const day = date.getDate().toString()

    // 默认认为景区开放但无库存数据
    const available = spot.value?.spotStatus === 1

    options.push({
      date: dateStr,
      week: `周${weekDay}`,
      day,
      available,
      stock: available ? 999 : 0 // 无库存信息时显示999
    })
  }

  dateOptions.value = options

  // 自动选择第一个可用的日期
  if (options.length > 0 && !selectedDate.value) {
    const firstAvailableDate = options.find(option => option.available)
    if (firstAvailableDate) {
      selectedDate.value = firstAvailableDate.date
    }
  }
}

// 图片切换逻辑已移除，现在使用Element Plus轮播图组件

// 选择日期
const selectDate = (date: string) => {
  const option = dateOptions.value.find(d => d.date === date)
  if (option?.available) {
    selectedDate.value = date
  }
}

// 获取景区详情
const fetchSpotDetail = async () => {
  try {
    const spotId = route.params.id as string
    const response = await getScenicSpotDetail(spotId)

    if (response.data.code === 200) {
      spot.value = response.data.data
      // 获取票务信息
      await fetchTicketProducts()
      // 生成日期选项（现在是异步的）
      await generateDateOptions()
      // 初始化地图
      await nextTick()
      initMap()
      // 加载同省随机推荐景区（真实数据）
      await loadProvinceRecommendations()
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
    // 调用后端API获取真实的票务产品数据
    const response = await getTicketProducts(spot.value.id)

    if (response.data.code === 200) {
      const ticketData = response.data.data
      if (ticketData && ticketData.records) {
        availableTickets.value = ticketData.records
      } else {
        availableTickets.value = []
      }

      // 如果没有可用票种，显示提示
      if (availableTickets.value.length === 0) {
        ElMessage.warning('当前景区暂无可用的门票产品')
      }

      // 自动选择第一个票种
      if (availableTickets.value.length > 0 && !selectedTicketType.value) {
        selectedTicketType.value = availableTickets.value[0].id
      }
    } else {
      ElMessage.error(response.data.message || '获取票务信息失败')
      availableTickets.value = []
    }
  } catch (error) {
    console.error('获取票务信息失败:', error)
    ElMessage.error('获取票务信息失败')
    availableTickets.value = []
  }
}

// 初始化地图
const initMap = async () => {
  if (!spot.value?.address) return

  try {
    const mapElement = document.getElementById('map')
    if (!mapElement) return

    // 动态加载高德地图API
    if (!(window as any).AMap) {
      const script = document.createElement('script')
      script.src = 'https://webapi.amap.com/maps?v=2.0&key=your_amap_key'
      script.async = true
      document.head.appendChild(script)

      await new Promise((resolve, reject) => {
        script.onload = resolve
        script.onerror = reject
        setTimeout(() => reject(new Error('Map load timeout')), 10000)
      })
    }

    // 创建地图实例
    const map = new (window as any).AMap.Map('map', {
      zoom: 15,
      center: [116.3974, 39.9093], // 默认北京坐标
      viewMode: '2D'
    })

    // 添加地图控件
    map.addControl(new (window as any).AMap.Scale())
    map.addControl(new (window as any).AMap.ToolBar())

    // 地理编码：将地址转换为坐标
    const geocoder = new (window as any).AMap.Geocoder()
    geocoder.getLocation(spot.value.address, (status: string, result: any) => {
      if (status === 'complete' && result.geocodes.length > 0) {
        const location = result.geocodes[0].location
        map.setCenter(location)

        // 添加标记
        const marker = new (window as any).AMap.Marker({
          position: location,
          title: spot.value!.spotName
        })
        map.add(marker)

        // 添加信息窗口
        const infoWindow = new (window as any).AMap.InfoWindow({
          content: `
            <div style="padding: 10px; max-width: 200px;">
              <h4 style="margin: 0 0 8px; color: #333;">${spot.value!.spotName}</h4>
              <p style="margin: 0; color: #666; font-size: 14px;">${spot.value!.address}</p>
            </div>
          `,
          offset: new (window as any).AMap.Pixel(0, -30)
        })

        marker.on('click', () => {
          infoWindow.open(map, marker.getPosition())
        })

        // 默认打开信息窗口
        infoWindow.open(map, marker.getPosition())
      } else {
        // 如果地理编码失败，使用默认地图
        console.warn('地理编码失败，使用默认位置')
        mapElement.innerHTML = `
          <div style="width: 100%; height: 100%; background: #f0f0f0; display: flex; align-items: center; justify-content: center; color: #999;">
            <div style="text-align: center;">
              <div style="font-size: 24px; margin-bottom: 10px;">📍</div>
              <div>地址定位失败</div>
              <div style="font-size: 12px; margin-top: 5px;">${spot.value!.address}</div>
            </div>
          </div>
        `
      }
    })

  } catch (error) {
    console.error('地图加载失败:', error)
    // 降级显示
    const mapElement = document.getElementById('map')
    if (mapElement) {
      mapElement.innerHTML = `
        <div style="width: 100%; height: 100%; background: #f0f0f0; display: flex; align-items: center; justify-content: center; color: #999;">
          <div style="text-align: center;">
            <div style="font-size: 24px; margin-bottom: 10px;">🗺️</div>
            <div>地图服务暂不可用</div>
            <div style="font-size: 12px; margin-top: 5px;">${spot.value!.address}</div>
          </div>
        </div>
      `
    }
  }
}

// 处理票种选择
const handleTicketTypeChange = () => {
  // 票种变化时的处理逻辑
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

  // 跳转到购票人选择页面
  router.push({
    path: '/passenger-selection',
    query: {
      spotId: encodeURIComponent(String(spot.value!.id)), // 强制转换为字符串并编码
      ticketType: encodeURIComponent(String(selectedTicket.value!.ticketType)),
      visitDate: encodeURIComponent(selectedDate.value)
    }
  })
}

// 跳转到首页
const goHome = () => {
  router.push('/')
}

// 跳转到其他景区
const goToSpot = (spotId: string) => {
  router.push(`/spot/${spotId}`)
}

// 跳转到登录页
const goToLogin = () => {
  loginDialogVisible.value = false
  router.push('/login')
}

// 加载同省的随机三个景区作为真实推荐
const loadProvinceRecommendations = async () => {
  try {
    if (!spot.value?.province) {
      recommendations.value = []
      return
    }

    const params: any = {
      current: 1,
      pageSize: 50,
      province: spot.value.province,
      spotStatus: 1
    }

    const resp = await getScenicSpotList(params)
    if (resp.data.code !== 200) {
      console.warn('获取同省景区列表失败:', resp.data.message)
      recommendations.value = []
      return
    }

    const records: any[] = resp.data.data.records || []
    // 排除当前景区
    const candidates = records.filter(r => String(r.id) !== String(spot.value!.id))
    if (candidates.length === 0) {
      recommendations.value = []
      return
    }

    // 随机打乱并取前3个
    for (let i = candidates.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1))
      ;[candidates[i], candidates[j]] = [candidates[j], candidates[i]]
    }

    const chosen = candidates.slice(0, 3)

    // 为每个选中景区尝试获取最低票价
    const items = await Promise.all(chosen.map(async (c) => {
      let price = 0
      try {
        const tpResp = await getTicketProducts(c.id)
        if (tpResp.data.code === 200 && tpResp.data.data && tpResp.data.data.records && tpResp.data.data.records.length > 0) {
          const prices = tpResp.data.data.records.map((p: any) => Number(p.priceRetail || 0))
          price = Math.min(...prices)
        }
      } catch (e) {
        console.warn('获取推荐景区票价失败', c.id, e)
      }

      return {
        id: String(c.id),
        name: c.spotName || c.name || '景区',
        location: c.province || '',
        price: price || 0,
        image: (c.imageUrls && c.imageUrls.length > 0) ? c.imageUrls[0] : 'https://via.placeholder.com/200x120'
      }
    }))

    recommendations.value = items
  } catch (error) {
    console.error('加载同省推荐景区失败:', error)
    recommendations.value = []
  }
}

// 格式化营业时间
const formatBusinessHours = (openTimeText: string) => {
  if (!openTimeText || openTimeText === '未设置') return '暂未设置开放时间'
  return openTimeText
}

  // 获取票种类型标签（与管理员端保持一致）
  const getTicketTypeLabel = (ticketType: number) => {
    const ticketTypeMap: Record<number, string> = {
      1: '原价票',
      2: '半价票',
      3: '免票',
      4: '学生',
      5: '其他'
    }
    return ticketTypeMap[ticketType] || '未知'
  }

  // 获取票种类型CSS类（根据 ticketType 决定样式）
  const getTicketTypeClass = (ticketType: number) => {
    switch (ticketType) {
      case 1:
        return 'ticket-full' // 成人票：原价样式
      case 2:
      case 3:
      case 4:
        return 'ticket-discount' // 儿童/老人/学生：优惠样式
      case 5:
        return 'ticket-free'
      default:
        return 'ticket-full'
    }
  }

  // 处理图片加载错误
  const handleImageError = (event: Event) => {
    const img = event.target as HTMLImageElement
    img.style.display = 'none'
  }

onMounted(() => {
  // Capture distributor referral ID if present
  const refId = route.query.ref
  if (refId) {
    // sessionStorage is better for session-based attribution
    sessionStorage.setItem('distributorId', String(refId))
    console.log('Captured distributor ID:', refId)
  }

  fetchSpotDetail()
})
</script>

<style scoped>
.spot-detail-page {
  min-height: 100vh;
  background: #f8f9fa;
}

/* 核心展示区 */
.hero-section {
  background: white;
  padding: 40px 0 40px;
  margin-bottom: 20px;
}

.hero-section .container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.hero-content {
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 60px;
  align-items: start;
}

/* 左侧内容区域 */
.left-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 60px;
  margin-top: 70px;
}

/* 景区描述 */
.description-section {
  width: 100%;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #409eff;
}

.introduction-content {
  line-height: 1.8;
  color: #666;
  font-size: 16px;
}

.introduction-content img {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 16px 0;
}

.introduction-content p {
  margin-bottom: 16px;
}

.introduction-content h3,
.introduction-content h4 {
  color: #333;
  margin: 24px 0 16px;
}

/* 左侧图片轮播 */
.image-gallery {
  width: 100%;
}

.image-gallery .el-carousel {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.image-gallery .el-carousel__container {
  height: 400px;
}

.image-gallery img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-images {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400px;
  background: #f5f5f5;
  border-radius: 12px;
  color: #999;
}

/* 右侧预订面板 */
.booking-panel {
  position: sticky;
  top: 0;
}

.booking-card {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid #e8e8e8;
  margin-top: -20px;
}

/* 标题区 */
.title-section {
  margin-bottom: 24px;
}

.spot-title {
  font-size: 1.8rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 12px;
  line-height: 1.3;
}

.title-badges {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.level-badge {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.status-badge {
  font-size: 11px;
}

.status-badge.open {
  background: #52c41a;
  color: white;
}

.status-badge.closed {
  background: #f5222d;
  color: white;
}

/* 基础信息区 */
.meta-info {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 16px;
  color: #666;
}

.meta-item:last-child {
  margin-bottom: 0;
}

.meta-item .el-icon {
  color: #409eff;
  font-size: 16px;
}

/* 价格展示 */
.price-display {
  text-align: center;
  margin-bottom: 32px;
  padding: 20px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a52 100%);
  border-radius: 12px;
  color: white;
}

.price-prefix {
  font-size: 16px;
  opacity: 0.8;
}

.price-amount {
  font-size: 32px;
  font-weight: 700;
  margin: 0 4px;
}

.price-suffix {
  font-size: 16px;
  opacity: 0.8;
}

/* 购票选择器 */
.booking-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.form-label {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

/* 日期选择器 */
.date-selector {
  background: white;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  overflow: hidden;
}

.date-tabs {
  display: flex;
  overflow-x: auto;
}

.date-tab {
  flex: 1;
  min-width: 80px;
  padding: 16px 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  border-right: 1px solid #f0f0f0;
}

.date-tab:last-child {
  border-right: none;
}

.date-tab.available:hover {
  background: #f0f8ff;
}

.date-tab.available.active {
  background: #409eff;
  color: white;
}

.date-tab.unavailable {
  background: #f5f5f5;
  cursor: not-allowed;
  color: #ccc;
}

.date-info {
  margin-bottom: 4px;
}

.date-week {
  font-size: 11px;
  opacity: 0.7;
  margin-bottom: 2px;
}

.date-day {
  font-size: 16px;
  font-weight: 600;
}

.date-stock {
  font-size: 12px;
  color: #52c41a;
  font-weight: 500;
}

/* 门票类型 */
.ticket-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ticket-card {
  width: 100%;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.ticket-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.ticket-card :deep(.el-radio__input.is-checked + .el-radio__label) {
  color: #409eff;
}

.ticket-card :deep(.el-radio__input.is-checked .el-radio__inner) {
  border-color: #409eff;
  background: #409eff;
}

.ticket-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

  .ticket-name {
    font-weight: 500;
    padding: 6px 12px;
    border-radius: 4px;
    font-size: 13px;
    text-align: center;
    min-width: 60px;
  }

  /* 原价票样式 */
  .ticket-full {
    background: #e6f7ff;
    color: #1890ff;
    border: 1px solid #91d5ff;
  }

  /* 半价票样式 */
  .ticket-discount {
    background: #f6ffed;
    color: #52c41a;
    border: 1px solid #b7eb8f;
  }

  /* 免票样式 */
  .ticket-free {
    background: #fff7e6;
    color: #fa8c16;
    border: 1px solid #ffd591;
  }

.ticket-price {
  color: #f5222d;
  font-weight: 600;
  font-size: 16px;
}


/* 预订按钮 */
.book-button {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a52 100%);
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  margin: 24px 0 16px;
}

.book-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.3);
}

/* 预订提示 */
.booking-tips {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 12px;
  color: #999;
  line-height: 1.4;
}


/* 详情内容区 */
.content-section {
  background: #f8f9fa;
  padding: 40px 0;
  margin-top: -20px;
}

.content-section .container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.content-wrapper {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 60px;
}

/* 主内容区 */
.main-content {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.detail-tabs :deep(.el-tabs__header) {
  margin: 0;
  background: #f8f9fa;
  border-bottom: 1px solid #e8e8e8;
}

.detail-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.detail-tabs :deep(.el-tabs__item) {
  padding: 16px 24px;
  font-size: 15px;
  font-weight: 500;
  color: #666;
  border-bottom: none;
}

.detail-tabs :deep(.el-tabs__item.is-active) {
  color: #409eff;
  background: white;
  border-bottom: 2px solid #409eff;
}

.detail-tabs :deep(.el-tabs__item:hover) {
  color: #409eff;
}

.tab-content {
  padding: 40px;
}

/* 景区介绍 */
.introduction-content {
  line-height: 1.8;
  color: #666;
}

.introduction-content img {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 16px 0;
}

.introduction-content p {
  margin-bottom: 16px;
}

.introduction-content h3,
.introduction-content h4 {
  color: #333;
  margin: 24px 0 16px;
}

/* 购票须知 */
.notice-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.notice-item h4 {
  color: #333;
  margin-bottom: 8px;
  font-size: 16px;
}

  .notice-item p {
    color: #666;
    line-height: 1.6;
    margin: 0;
  }

  .ticket-types {
    padding: 12px;
  }

  .ticket-type-item {
    font-size: 14px;
    margin-bottom: 10px;
  }

  .ticket-type-name {
    min-width: 70px;
  }

/* 票种说明样式 */
.ticket-types {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  margin-top: 8px;
}

.ticket-type-item {
  display: flex;
  margin-bottom: 12px;
  line-height: 1.5;
}

.ticket-type-item:last-child {
  margin-bottom: 0;
}

.ticket-type-name {
  font-weight: 600;
  color: #409eff;
  min-width: 80px;
  flex-shrink: 0;
}

.ticket-type-desc {
  color: #666;
  margin-left: 8px;
}

/* 交通指南 */
.transport-content {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 40px;
}

.map-container {
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e8e8e8;
}

.map-view {
  width: 100%;
  height: 300px;
  background: #f5f5f5;
}

.transport-info h4 {
  color: #333;
  margin-bottom: 12px;
  font-size: 16px;
}

.transport-info p {
  color: #666;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.transport-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.transport-option {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 14px;
}

/* 侧边栏 */
.sidebar {
  height: fit-content;
}

.recommendations {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #e8e8e8;
}

.recommendations h3 {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
  text-align: center;
}

.recommend-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.recommend-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
}

.recommend-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
  transform: translateY(-2px);
}

.recommend-item img {
  width: 80px;
  height: 60px;
  border-radius: 8px;
  object-fit: cover;
}

.recommend-info {
  flex: 1;
}

.recommend-info h4 {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
  line-height: 1.3;
}

.recommend-info p {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}

.recommend-price {
  font-size: 14px;
  color: #f5222d;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .hero-content {
    gap: 40px;
  }

  .content-wrapper {
    gap: 40px;
  }
}

@media (max-width: 1024px) {
  .hero-content {
    grid-template-columns: 1fr;
    gap: 40px;
  }

  .booking-panel {
    position: static;
  }

  .booking-card {
    max-width: 600px;
    margin: 0 auto;
  }

  .content-wrapper {
    grid-template-columns: 1fr;
    gap: 40px;
  }

  .transport-content {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .thumbnail-list {
    justify-content: center;
  }
}

@media (max-width: 768px) {
  .hero-section {
    padding: 20px 0 40px;
  }

  .content-section {
    padding: 30px 0;
    margin-top: -10px;
  }

  .hero-section .container,
  .content-section .container {
    padding: 0 15px;
  }

  .left-content {
    gap: 40px;
    margin-top: 35px;
  }

  .spot-title {
    font-size: 1.5rem;
  }

  .image-gallery {
    margin-top: 90px;
  }

  .image-gallery .el-carousel__container {
    height: 250px;
  }

  .booking-card {
    padding: 20px;
    border-radius: 12px;
  }

  .title-badges {
    gap: 6px;
  }

  .badge {
    font-size: 11px;
    padding: 3px 8px;
  }

  .meta-info {
    padding: 16px;
  }

  .price-display {
    padding: 16px;
    margin-bottom: 20px;
  }

  .price-amount {
    font-size: 28px;
  }

  .date-tab {
    min-width: 70px;
    padding: 12px 6px;
  }

  .date-week {
    font-size: 10px;
  }

  .date-day {
    font-size: 15px;
  }

  .ticket-card {
    padding: 14px;
  }

  .tab-content {
    padding: 24px 16px;
  }

  .recommend-item {
    padding: 12px;
  }

  .recommend-item img {
    width: 70px;
    height: 50px;
  }

  .recommend-info h4 {
    font-size: 13px;
  }

  .section-title {
    font-size: 1.3rem;
    margin-bottom: 15px;
  }

  .ticket-name {
    padding: 4px 8px;
    font-size: 12px;
    min-width: 50px;
  }

  .recommendations {
    margin-top: 0;
  }
}

@media (max-width: 480px) {

  .date-tabs {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  .date-tab {
    min-width: 60px;
    padding: 10px 4px;
  }

  .date-day {
    font-size: 14px;
  }

  .date-stock {
    font-size: 11px;
  }

  .ticket-card {
    padding: 12px;
  }

  .ticket-name {
    font-size: 14px;
  }

  .ticket-price {
    font-size: 15px;
  }

  .summary-row {
    font-size: 13px;
  }

  .summary-row.total {
    font-size: 15px;
  }

  .book-button {
    height: 44px;
    font-size: 15px;
  }

  .transport-content {
    gap: 16px;
  }

  .map-view {
    height: 200px;
  }

  .transport-info h4 {
    font-size: 15px;
  }

  .recommend-list {
    gap: 12px;
  }

  .recommend-item {
    gap: 10px;
    padding: 10px;
  }

  .recommend-item img {
    width: 60px;
    height: 45px;
  }

  .recommend-info h4 {
    font-size: 12px;
  }

  .recommend-price {
    font-size: 13px;
  }
}

/* 平板适配 */
@media (max-width: 768px) and (min-width: 481px) {
  .image-gallery .el-carousel__container {
    height: 300px;
  }

  .booking-card {
    padding: 24px;
  }

  .price-amount {
    font-size: 30px;
  }
}

/* 超小屏幕 */
@media (max-width: 360px) {
  .date-tab {
    min-width: 55px;
    padding: 8px 3px;
  }

  .date-day {
    font-size: 13px;
  }

  .thumbnail-item {
    width: 45px;
    height: 34px;
  }

  .booking-card {
    padding: 16px;
  }

  .tab-content {
    padding: 16px 12px;
  }
}
</style>
