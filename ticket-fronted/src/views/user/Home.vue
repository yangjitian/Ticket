<template>
  <div class="home-page">
    <div style="padding: 50px; text-align: center; background: #f0f8ff; min-height: 100vh;">
      <h1 style="color: #1890ff; font-size: 2rem; margin-bottom: 20px;">欢迎来到我们的旅游分销平台！</h1>
      <p style="color: #666; font-size: 1.2rem; margin-bottom: 30px;">发现更多精彩景点，开启您的旅行之旅</p>

      <!-- 筛选区域 -->
      <div style="max-width: 1400px; margin: 0 auto; margin-bottom: 40px;">
        <div style="background: white; padding: 24px; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
          <div style="display: flex; align-items: center; gap: 20px; justify-content: center; flex-wrap: nowrap;">
            <div style="display: flex; align-items: center; gap: 10px;">
              <span style="color: #666; font-weight: 500; font-size: 16px;">地区：</span>
              <el-select
                v-model="filters.regions"
                placeholder="选择地区"
                clearable
                multiple
                size="large"
                style="width: 240px"
              >
                <el-option label="河南省" value="河南省" />
                <el-option label="北京市" value="北京市" />
                <el-option label="天津市" value="天津市" />
                <el-option label="上海市" value="上海市" />
                <el-option label="重庆市" value="重庆市" />

                <el-option label="河北省" value="河北省" />
                <el-option label="山西省" value="山西省" />
                <el-option label="辽宁省" value="辽宁省" />
                <el-option label="吉林省" value="吉林省" />
                <el-option label="黑龙江省" value="黑龙江省" />

                <el-option label="江苏省" value="江苏省" />
                <el-option label="浙江省" value="浙江省" />
                <el-option label="安徽省" value="安徽省" />
                <el-option label="福建省" value="福建省" />
                <el-option label="江西省" value="江西省" />

                <el-option label="山东省" value="山东省" />
                <el-option label="河南省" value="河南省" />
                <el-option label="湖北省" value="湖北省" />
                <el-option label="湖南省" value="湖南省" />
                <el-option label="广东省" value="广东省" />

                <el-option label="海南省" value="海南省" />
                <el-option label="四川省" value="四川省" />
                <el-option label="贵州省" value="贵州省" />
                <el-option label="云南省" value="云南省" />
                <el-option label="陕西省" value="陕西省" />

                <el-option label="甘肃省" value="甘肃省" />
                <el-option label="青海省" value="青海省" />
                <el-option label="台湾省" value="台湾省" />

                <el-option label="内蒙古自治区" value="内蒙古自治区" />
                <el-option label="广西壮族自治区" value="广西壮族自治区" />
                <el-option label="西藏自治区" value="西藏自治区" />
                <el-option label="宁夏回族自治区" value="宁夏回族自治区" />
                <el-option label="新疆维吾尔自治区" value="新疆维吾尔自治区" />

                <el-option label="香港特别行政区" value="香港特别行政区" />
                <el-option label="澳门特别行政区" value="澳门特别行政区" />
              </el-select>
            </div>

            <div style="display: flex; align-items: center; gap: 10px;">
              <span style="color: #666; font-weight: 500; font-size: 16px;">等级：</span>
              <el-select
                v-model="filters.levels"
                placeholder="选择等级"
                clearable
                multiple
                size="large"
                style="width: 160px"
              >
                <el-option label="5A级" value="5A" />
                <el-option label="4A级" value="4A" />
                <el-option label="3A级" value="3A" />
                <el-option label="2A级" value="2A" />
              </el-select>
            </div>

            <div style="display: flex; align-items: center; gap: 10px;">
              <span style="color: #666; font-weight: 500; font-size: 16px;">状态：</span>
              <el-select
                v-model="filters.status"
                placeholder="选择状态"
                clearable
                size="large"
                style="width: 140px"
              >
                <el-option label="全部" :value="null" />
                <el-option label="开放" :value="1" />
                <el-option label="关闭" :value="0" />
              </el-select>
            </div>

            <div style="display: flex; align-items: center; gap: 10px;">
              <span style="color: #666; font-weight: 500; font-size: 16px;">排序：</span>
              <el-select v-model="sortBy" placeholder="选择排序方式" size="large" style="width: 170px">
                <el-option label="默认排序" value="default" />
                <el-option label="热门优先" value="hot" />
                <el-option label="价格从低到高" value="price_asc" />
                <el-option label="价格从高到低" value="price_desc" />
              </el-select>
            </div>

            <div>
              <el-button @click="resetFilters" type="default" size="large">重置筛选</el-button>
            </div>
          </div>
        </div>
      </div>

      <div style="max-width: 1200px; margin: 0 auto;">
        <!-- 搜索结果提示 -->
        <div v-if="isSearching && searchQuery" class="search-result-hint" style="margin-bottom: 20px;">
          <el-alert
            :title="`搜索结果：${searchQuery}`"
            type="info"
            show-icon
            :closable="false"
          >
            <template #description>
              <span>找到 {{ total }} 个相关景区</span>
            </template>
          </el-alert>
        </div>

        <h2 style="color: #333; margin-bottom: 30px;">{{ isSearching ? '搜索结果' : '热门推荐' }}</h2>

        <!-- 搜索结果 -->
        <div v-if="isSearching" v-loading="loading" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(230px, 1fr)); gap: 16px;">
          <div
            v-for="spot in searchResults"
            :key="spot.id"
            style="background: linear-gradient(0deg, #b3d9ff 0%, #e6f3ff 100%); border-radius: 12px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1); cursor: pointer; transition: transform 0.3s ease;"
            @click="goToSpotDetail(spot.id)"
            @mouseenter="$event.currentTarget.style.transform = 'translateY(-4px)'"
            @mouseleave="$event.currentTarget.style.transform = 'translateY(0)'"
          >
            <!-- 景区图片 -->
            <div style="height: 220px; overflow: hidden; position: relative; background: linear-gradient(0deg, #b3d9ff 0%, #e6f3ff 100%);">
              <img
                v-if="spot.imageUrls && spot.imageUrls.length > 0"
                :src="spot.imageUrls[0]"
                :alt="spot.spotName"
                style="width: 100%; height: 100%; object-fit: contain; transition: transform 0.3s ease;"
                @error="handleImageError"
              />
              <div v-else style="width: 100%; height: 100%; background: linear-gradient(0deg, #f0f0f0 0%, #d1e7ff 100%); display: flex; flex-direction: column; align-items: center; justify-content: center; color: #666;">
                <el-icon size="64"><Picture /></el-icon>
                <span style="margin-top: 12px; font-size: 0.9rem;">暂无图片</span>
              </div>

              <!-- 状态标签 -->
              <div
                v-if="spot.spotStatus !== undefined"
                :style="{
                  position: 'absolute',
                  top: '44px',
                  right: '12px',
                  background: spot.spotStatus === 1 ? '#52c41a' : '#f5222d',
                  color: 'white',
                  padding: '4px 12px',
                  borderRadius: '20px',
                  fontSize: '12px',
                  fontWeight: '500'
                }"
              >
                {{ spot.spotStatus === 1 ? '开放' : '关闭' }}
              </div>

              <!-- 等级标签 -->
              <div
                v-if="spot.level"
                :style="{
                  position: 'absolute',
                  top: '12px',
                  right: '12px',
                  background: '#faad14',
                  color: 'white',
                  padding: '4px 12px',
                  borderRadius: '20px',
                  fontSize: '12px',
                  fontWeight: '500'
                }"
              >
                {{ spot.level }}级
              </div>
            </div>

            <!-- 景区信息 -->
            <div style="padding: 16px; padding-top: 12px;">
              <h3 style="color: #1a1a1a; margin-bottom: 8px; font-size: 1.1rem; font-weight: 600; display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical; overflow: hidden;">{{ spot.spotName }}</h3>
              <p style="color: #4a4a4a; font-size: 0.9rem; margin-bottom: 8px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">{{ spot.description }}</p>
              <p style="color: #666; font-size: 0.8rem; margin-bottom: 0;">{{ spot.province ? spot.province + ' · ' : '' }}{{ spot.address }}</p>
            </div>
          </div>

          <!-- 无数据提示 -->
          <div v-if="!loading && searchResults.length === 0" style="grid-column: 1 / -1; text-align: center; padding: 60px;">
            <el-empty description="暂无符合条件的景区" :image-size="80">
              <el-button type="primary" @click="resetFilters">清除筛选条件</el-button>
            </el-empty>
          </div>
        </div>

        <!-- 热门推荐 -->
        <div v-else v-loading="recommendedLoading" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(230px, 1fr)); gap: 16px;">
          <div
            v-for="spot in recommendedSpots"
            :key="spot.id"
            style="background: linear-gradient(0deg, #b3d9ff 0%, #e6f3ff 100%); border-radius: 12px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1); cursor: pointer; transition: transform 0.3s ease;"
            @click="goToSpotDetail(spot.id)"
            @mouseenter="$event.currentTarget.style.transform = 'translateY(-4px)'"
            @mouseleave="$event.currentTarget.style.transform = 'translateY(0)'"
          >
            <!-- 景区图片 -->
            <div style="height: 220px; overflow: hidden; position: relative; background: linear-gradient(0deg, #b3d9ff 0%, #e6f3ff 100%);">
              <img
                v-if="spot.imageUrls && spot.imageUrls.length > 0"
                :src="spot.imageUrls[0]"
                :alt="spot.spotName"
                style="width: 100%; height: 100%; object-fit: contain; transition: transform 0.3s ease;"
                @error="handleImageError"
                @mouseenter="$event.target.style.transform = 'scale(1.05)'"
                @mouseleave="$event.target.style.transform = 'scale(1)'"
              />
              <div v-else style="width: 100%; height: 100%; background: linear-gradient(0deg, #f0f0f0 0%, #d1e7ff 100%); display: flex; flex-direction: column; align-items: center; justify-content: center; color: #666;">
                <el-icon size="64"><Picture /></el-icon>
                <span style="margin-top: 12px; font-size: 0.9rem;">暂无图片</span>
              </div>

              <!-- 状态标签 -->
              <div
                v-if="spot.spotStatus !== undefined"
                :style="{
                  position: 'absolute',
                  top: '44px',
                  right: '12px',
                  background: spot.spotStatus === 1 ? '#52c41a' : '#f5222d',
                  color: 'white',
                  padding: '4px 12px',
                  borderRadius: '20px',
                  fontSize: '12px',
                  fontWeight: '500'
                }"
              >
                {{ spot.spotStatus === 1 ? '开放' : '关闭' }}
              </div>

              <!-- 等级标签 -->
              <div
                v-if="spot.level"
                :style="{
                  position: 'absolute',
                  top: '12px',
                  right: '12px',
                  background: '#faad14',
                  color: 'white',
                  padding: '4px 12px',
                  borderRadius: '20px',
                  fontSize: '12px',
                  fontWeight: '500'
                }"
              >
                {{ spot.level }}级
              </div>
            </div>

            <!-- 景区信息 -->
            <div style="padding: 16px; padding-top: 12px;">
              <h3 style="color: #1a1a1a; margin-bottom: 8px; font-size: 1.1rem; font-weight: 600; display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical; overflow: hidden;">{{ spot.spotName }}</h3>
              <p style="color: #4a4a4a; font-size: 0.9rem; margin-bottom: 8px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">{{ spot.description }}</p>
              <p style="color: #666; font-size: 0.8rem; margin-bottom: 0;">{{ spot.province ? spot.province + ' · ' : '' }}{{ spot.address }}</p>
            </div>
          </div>

          <!-- 无数据提示 -->
          <div v-if="!recommendedLoading && recommendedSpots.length === 0" style="grid-column: 1 / -1; text-align: center; padding: 60px;">
            <el-empty description="暂无推荐景区" :image-size="80"></el-empty>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { getScenicSpotList } from '@/user/api'
import type { ScenicSpot, ScenicSpotQueryRequest } from '@/types/user'

// 路由
const router = useRouter()
const route = useRoute()

// 筛选和排序
const sortBy = ref('default')

// 筛选条件
const filters = reactive({
  regions: [] as string[],
  levels: [] as string[],
  status: null as number | null
})

// 搜索关键词
const searchQuery = ref('')

// 搜索结果
const searchResults = ref<ScenicSpot[]>([])
const loading = ref(false)
const total = ref(0)

// 是否正在搜索
const isSearching = ref(false)

// 推荐景区
const recommendedSpots = ref<ScenicSpot[]>([])
const recommendedLoading = ref(false)

// 从路由查询参数初始化
const initFromQuery = () => {
  const query = route.query

  // 搜索关键词
  if (query.search) {
    searchQuery.value = query.search as string
    isSearching.value = true
  }

  // 地区筛选
  if (query.regions) {
    filters.regions = Array.isArray(query.regions)
      ? query.regions.map(r => r as string).filter(r => r)
      : [query.regions as string].filter(r => r)
  }

  // 等级筛选
  if (query.levels) {
    filters.levels = Array.isArray(query.levels)
      ? query.levels.map(l => l as string).filter(l => l)
      : [query.levels as string].filter(l => l)
  }

  // 状态筛选
  if (query.status !== undefined && query.status !== null) {
    filters.status = Number(query.status)
  }

  // 排序
  if (query.sortBy) {
    sortBy.value = query.sortBy as string
  }
}

// 获取推荐景区
const fetchRecommendedSpots = async () => {
  try {
    recommendedLoading.value = true

    const params: ScenicSpotQueryRequest = {
      current: 1,
      pageSize: 6, // 获取6个推荐景区
      spotStatus: 1 // 只获取开放的景区
    }

    const response = await getScenicSpotList(params)

    if (response.data.code === 200) {
      recommendedSpots.value = response.data.data.records || []
    } else {
      console.error('获取推荐景区失败:', response.data.message)
    }
  } catch (error) {
    console.error('获取推荐景区失败:', error)
  } finally {
    recommendedLoading.value = false
  }
}

// 获取景区列表
const fetchSpots = async () => {
  try {
    loading.value = true

    const params: ScenicSpotQueryRequest = {
      current: 1,
      pageSize: 12,
      spotStatus: filters.status !== null ? filters.status : undefined
    }

    // 添加搜索关键词
    if (searchQuery.value.trim()) {
      params.spotName = searchQuery.value.trim()
    }

    // 添加省份筛选
    if (filters.regions.length > 0) {
      params.province = filters.regions[0]
    }

    // 添加等级筛选
    if (filters.levels.length > 0) {
      params.level = filters.levels[0]
    }

    const response = await getScenicSpotList(params)

    if (response.data.code === 200) {
      searchResults.value = response.data.data.records || []
      total.value = response.data.data.total || 0
    } else {
      console.error('获取景区列表失败:', response.data.message)
    }
  } catch (error) {
    console.error('获取景区列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 重置筛选
const resetFilters = () => {
  filters.regions = []
  filters.levels = []
  filters.status = null
  sortBy.value = 'default'
  searchQuery.value = ''
  isSearching.value = false

  // 重置URL参数
  router.replace({ query: {} })
}

// 跳转到景区详情
const goToSpotDetail = (spotId: string) => {
  router.push(`/spot/${spotId}`)
}

// 处理图片加载错误
const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  const parent = img.parentElement
  if (parent) {
    parent.innerHTML = `
      <div style="width: 100%; height: 100%; background: #f5f5f5; display: flex; flex-direction: column; align-items: center; justify-content: center; color: #999;">
        <el-icon size="48"><Picture /></el-icon>
        <span style="margin-top: 8px;">暂无图片</span>
      </div>
    `
  }
}

// 监听筛选条件变化
watch(
  () => [filters.regions, filters.levels, filters.status, sortBy.value, searchQuery.value],
  () => {
    // 构建查询参数
    const query: any = {}
    if (searchQuery.value.trim()) {
      query.search = searchQuery.value.trim()
    }
    if (filters.regions.length > 0) {
      query.regions = filters.regions.join(',')
    }
    if (filters.levels.length > 0) {
      query.levels = filters.levels.join(',')
    }
    if (filters.status !== null) {
      query.status = filters.status
    }
    if (sortBy.value !== 'default') {
      query.sortBy = sortBy.value
    }

    // 更新URL并搜索
    router.replace({ query })
    isSearching.value = searchQuery.value.trim().length > 0 || filters.regions.length > 0 || filters.levels.length > 0 || filters.status !== null || sortBy.value !== 'default'

    if (isSearching.value) {
      fetchSpots()
    }
  },
  { deep: true }
)

onMounted(() => {
  initFromQuery()
  if (isSearching.value) {
    fetchSpots()
  } else {
    fetchRecommendedSpots()
  }
  console.log('Home component mounted')
})
</script>

<style scoped>
.home-page {
  min-height: 100vh;
}
</style>
