<template>
  <div class="spot-list-page">
    <div class="container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1 class="page-title">景区库</h1>
        <p class="page-subtitle">发现更多精彩景点，开启您的旅行之旅</p>
      </div>

      <div class="content-wrapper">
        <!-- 筛选侧边栏 -->
        <aside class="filter-sidebar">
          <div class="filter-section">
            <h3 class="filter-title">地区筛选</h3>
            <el-checkbox-group v-model="filters.regions">
              <el-checkbox label="北京市">北京市</el-checkbox>
              <el-checkbox label="上海市">上海市</el-checkbox>
              <el-checkbox label="广东省">广东省</el-checkbox>
              <el-checkbox label="江苏省">江苏省</el-checkbox>
              <el-checkbox label="浙江省">浙江省</el-checkbox>
              <el-checkbox label="山东省">山东省</el-checkbox>
            </el-checkbox-group>
          </div>

          <div class="filter-section">
            <h3 class="filter-title">景区等级</h3>
            <el-checkbox-group v-model="filters.levels">
              <el-checkbox label="5A">5A级</el-checkbox>
              <el-checkbox label="4A">4A级</el-checkbox>
              <el-checkbox label="3A">3A级</el-checkbox>
              <el-checkbox label="2A">2A级</el-checkbox>
            </el-checkbox-group>
          </div>

          <div class="filter-section">
            <h3 class="filter-title">票种类型</h3>
            <el-checkbox-group v-model="filters.ticketTypes">
              <el-checkbox :label="1">成人票</el-checkbox>
              <el-checkbox :label="2">儿童票</el-checkbox>
              <el-checkbox :label="3">老人票</el-checkbox>
              <el-checkbox :label="4">学生票</el-checkbox>
            </el-checkbox-group>
          </div>

          <div class="filter-section">
            <h3 class="filter-title">状态</h3>
            <el-radio-group v-model="filters.status">
              <el-radio :value="null">全部</el-radio>
              <el-radio :value="1">开放</el-radio>
              <el-radio :value="0">关闭</el-radio>
            </el-radio-group>
          </div>

          <div class="filter-actions">
            <el-button @click="resetFilters" style="width: 100%;">重置筛选</el-button>
          </div>
        </aside>

        <!-- 主要内容区域 -->
        <main class="main-content">
          <!-- 搜索和排序 -->
          <div class="content-header">
            <div class="search-bar">
              <el-input
                v-model="searchQuery"
                placeholder="搜索景区名称或地址"
                clearable
                style="width: 300px"
                @keyup.enter="handleSearch"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-button type="primary" @click="handleSearch" style="margin-left: 12px;">
                搜索
              </el-button>
            </div>

            <div class="sort-options">
              <span class="sort-label">排序：</span>
              <el-select v-model="sortBy" placeholder="选择排序方式" style="width: 150px">
                <el-option label="默认排序" value="default" />
                <el-option label="热门优先" value="hot" />
                <el-option label="价格从低到高" value="price_asc" />
                <el-option label="价格从高到低" value="price_desc" />
              </el-select>
            </div>
          </div>

          <!-- 景区网格 -->
          <div v-loading="loading" class="spots-grid">
            <div
              v-for="spot in spots"
              :key="spot.id"
              class="spot-card"
              @click="goToSpotDetail(spot.id)"
            >
              <!-- 景区图片 -->
              <div class="spot-image">
                <img
                  v-if="spot.imageUrls && spot.imageUrls.length > 0"
                  :src="spot.imageUrls[0]"
                  :alt="spot.spotName"
                  @error="handleImageError"
                />
                <div v-else class="no-image">
                  <el-icon size="48"><Picture /></el-icon>
                  <span>暂无图片</span>
                </div>

                <!-- 状态标签 -->
                <div class="spot-status" :class="{ 'closed': spot.spotStatus === 0 }">
                  {{ spot.spotStatus === 1 ? '开放' : '关闭' }}
                </div>

                <!-- 级别标签 -->
                <div class="spot-level">
                  5A级
                </div>
              </div>

              <!-- 景区信息 -->
              <div class="spot-info">
                <h3 class="spot-name">{{ spot.spotName }}</h3>
                <p class="spot-address">
                  <el-icon><Location /></el-icon>
                  {{ spot.address }}
                </p>
                <p class="spot-description">{{ spot.description }}</p>

                <!-- 价格信息 -->
                <div class="spot-price">
                  <div class="price-info">
                    <span class="price">￥80</span>
                    <span class="price-unit">起</span>
                  </div>
                  <el-button type="primary" size="small" @click.stop="goToSpotDetail(spot.id)">
                    查看详情
                  </el-button>
                </div>
              </div>
            </div>

            <!-- 无数据提示 -->
            <div v-if="!loading && spots.length === 0" class="no-data">
              <el-empty description="暂无符合条件的景区">
                <el-button type="primary" @click="resetFilters">清除筛选条件</el-button>
              </el-empty>
            </div>
          </div>

          <!-- 分页 -->
          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[12, 24, 36, 48]"
              :total="total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </main>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Location, Picture } from '@element-plus/icons-vue'
import { getScenicSpotList } from '@/user/api'
import type { ScenicSpot, ScenicSpotQueryRequest } from '@/types/user'

const router = useRouter()
const route = useRoute()

// 数据状态
const spots = ref<ScenicSpot[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)

// 搜索和筛选
const searchQuery = ref('')
const sortBy = ref('default')

// 筛选条件
const filters = reactive({
  regions: [] as string[],
  levels: [] as string[],
  ticketTypes: [] as number[],
  status: null as number | null
})

// 从路由查询参数初始化搜索条件
const initFromQuery = () => {
  const query = route.query
  if (query.search) {
    searchQuery.value = query.search as string
  }
  if (query.visitDate) {
    // 可以后续添加日期筛选
  }
}

// 获取景区列表
const fetchSpots = async () => {
  try {
    loading.value = true

    const params: ScenicSpotQueryRequest = {
      current: currentPage.value,
      pageSize: pageSize.value,
      spotName: searchQuery.value || undefined,
      spotStatus: filters.status !== null ? filters.status : undefined
    }

    // TODO: 添加更多筛选条件支持
    // if (filters.regions.length > 0) params.regions = filters.regions
    // if (filters.levels.length > 0) params.levels = filters.levels

    const response = await getScenicSpotList(params)

    if (response.data.code === 200) {
      spots.value = response.data.data.records || []
      total.value = response.data.data.total || 0
    } else {
      ElMessage.error(response.data.message || '获取景区列表失败')
    }
  } catch (error) {
    console.error('获取景区列表失败:', error)
    ElMessage.error('获取景区列表失败')
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchSpots()
}

// 重置筛选
const resetFilters = () => {
  filters.regions = []
  filters.levels = []
  filters.ticketTypes = []
  filters.status = null
  searchQuery.value = ''
  currentPage.value = 1
  fetchSpots()
}

// 分页处理
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchSpots()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  fetchSpots()
}

// 跳转到景区详情
const goToSpotDetail = (spotId: string) => {
  router.push(`/spot/${spotId}`)
}

// 处理图片加载错误
const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.style.display = 'none'
  const parent = img.parentElement
  if (parent) {
    parent.innerHTML = `
      <div class="no-image">
        <el-icon size="48"><Picture /></el-icon>
        <span>暂无图片</span>
      </div>
    `
  }
}

// 监听筛选条件变化
watch(
  () => [filters.regions, filters.levels, filters.ticketTypes, filters.status],
  () => {
    currentPage.value = 1
    fetchSpots()
  },
  { deep: true }
)

onMounted(() => {
  initFromQuery()
  fetchSpots()
})
</script>

<style scoped>
.spot-list-page {
  padding: 40px 0;
  background: #f8f9fa;
  min-height: calc(100vh - 200px);
}

.container {
  max-width: 1200px;
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

/* 内容布局 */
.content-wrapper {
  display: grid;
  grid-template-columns: 250px 1fr;
  gap: 40px;
}

/* 筛选侧边栏 */
.filter-sidebar {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  height: fit-content;
  position: sticky;
  top: 20px;
}

.filter-section {
  margin-bottom: 24px;
}

.filter-section:last-child {
  margin-bottom: 0;
}

.filter-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.filter-section .el-checkbox-group,
.filter-section .el-radio-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-actions {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

/* 主要内容区域 */
.main-content {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 内容头部 */
.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.search-bar {
  display: flex;
  align-items: center;
}

.sort-options {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sort-label {
  font-size: 0.9rem;
  color: #666;
}

/* 景区网格 */
.spots-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  padding: 24px;
}

/* 景区卡片 */
.spot-card {
  background: white;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
}

.spot-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  border-color: #1890ff;
}

/* 景区图片 */
.spot-image {
  position: relative;
  height: 180px;
  overflow: hidden;
}

.spot-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.spot-card:hover .spot-image img {
  transform: scale(1.05);
}

.no-image {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  background: #f5f5f5;
  color: #999;
}

.spot-status {
  position: absolute;
  top: 12px;
  right: 12px;
  background: #52c41a;
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.spot-status.closed {
  background: #f5222d;
}

.spot-level {
  position: absolute;
  top: 12px;
  left: 12px;
  background: #faad14;
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

/* 景区信息 */
.spot-info {
  padding: 16px;
}

.spot-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.spot-address {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 8px;
}

.spot-description {
  font-size: 0.9rem;
  color: #666;
  line-height: 1.5;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.spot-price {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.price-info {
  display: flex;
  align-items: baseline;
}

.price {
  font-size: 1.3rem;
  font-weight: 600;
  color: #f5222d;
}

.price-unit {
  font-size: 0.8rem;
  color: #999;
  margin-left: 2px;
}

/* 无数据状态 */
.no-data {
  grid-column: 1 / -1;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 24px;
  border-top: 1px solid #f0f0f0;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .content-wrapper {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .filter-sidebar {
    position: static;
    order: 2;
  }

  .spots-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  }
}

@media (max-width: 768px) {
  .spot-list-page {
    padding: 20px 0;
  }

  .container {
    padding: 0 15px;
  }

  .page-title {
    font-size: 2rem;
  }

  .content-header {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }

  .search-bar .el-input {
    width: 100% !important;
  }

  .spots-grid {
    grid-template-columns: 1fr;
    padding: 16px;
  }

  .filter-sidebar {
    padding: 16px;
  }

  .content-header,
  .pagination-wrapper {
    padding: 16px;
  }
}
</style>
