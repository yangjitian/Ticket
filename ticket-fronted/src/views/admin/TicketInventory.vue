<template>
  <div class="ticket-inventory-management">
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item>首页</el-breadcrumb-item>
        <el-breadcrumb-item>资源中心</el-breadcrumb-item>
        <el-breadcrumb-item>门票价格及库存管理</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="page-header">
      <h2>门票价格及库存管理</h2>
      <div class="header-actions">
        <el-input
          v-model="searchForm.spotName"
          placeholder="搜索景区名称"
          style="width: 250px; margin-right: 10px;"
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
      </div>
    </div>

    <!-- 景区门票列表 -->
    <div class="scenic-spots-list">
      <div
        v-for="scenicSpotTickets in scenicSpots"
        :key="scenicSpotTickets.scenicSpot.id"
        class="scenic-spot-card"
      >
        <!-- 景区头部信息 -->
        <div class="spot-header">
          <div class="spot-info">
            <div class="spot-title">
              <h3>{{ scenicSpotTickets.scenicSpot.spotName }}</h3>
              <el-tag v-if="scenicSpotTickets.scenicSpot.level" size="small" class="level-tag">
                {{ scenicSpotTickets.scenicSpot.level }}
              </el-tag>
              <el-tag
                :type="scenicSpotTickets.scenicSpot.spotStatus === 1 ? 'success' : 'danger'"
                size="small"
              >
                {{ scenicSpotTickets.scenicSpot.spotStatus === 1 ? '开放' : '关闭' }}
              </el-tag>
            </div>
            <div class="spot-meta">
              <span class="meta-item">
                <el-icon><Location /></el-icon>
                {{ scenicSpotTickets.scenicSpot.address || '暂无地址' }}
              </span>
              <span class="meta-item">
                <el-icon><Phone /></el-icon>
                {{ scenicSpotTickets.scenicSpot.contactNum || '暂无电话' }}
              </span>
            </div>
          </div>
          <div class="spot-actions">
            <el-button type="primary" @click="manageTickets(scenicSpotTickets.scenicSpot)">
              <el-icon><Tickets /></el-icon>
              管理门票
            </el-button>
            <el-button type="warning" @click="manageScenicStock(scenicSpotTickets.scenicSpot)">
              <el-icon><Box /></el-icon>
              景区库存
            </el-button>
          </div>
        </div>

        <!-- 门票产品列表 -->
        <div class="tickets-section">
          <div class="section-header">
            <h4>门票产品 ({{ scenicSpotTickets.ticketProducts.length }}个)</h4>
          </div>

          <div v-if="scenicSpotTickets.ticketProducts.length > 0" class="tickets-grid">
            <div
              v-for="product in scenicSpotTickets.ticketProducts"
              :key="product.id"
              class="ticket-product-item"
            >
              <div class="product-info">
                <div class="product-name">
                  {{ product.productName }}
                  <el-tag size="small" class="ticket-type-tag">
                    {{ getTicketTypeLabel(product.ticketType) }}
                  </el-tag>
                  <el-tag
                    :type="product.saleStatus === 1 ? 'success' : 'info'"
                    size="small"
                  >
                    {{ product.saleStatus === 1 ? '上架' : '下架' }}
                  </el-tag>
                </div>
                <div class="product-prices">
                  <div class="price-item">
                    <span class="label">官网零售价：</span>
                    <span class="value">¥{{ product.priceRetail }}</span>
                  </div>
                  <div class="price-item">
                    <span class="label">平台结算价：</span>
                    <span class="value">¥{{ product.priceSettlement }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div v-else class="no-tickets">
            <el-empty description="暂无门票产品" />
          </div>
        </div>

        <!-- 库存概览 -->
        <div class="stock-summary-section">
          <div class="section-header">
            <h4>未来7天库存概览</h4>
          </div>

          <div class="stock-summary-grid">
            <div class="summary-item">
              <div class="summary-value">{{ scenicSpotTickets.futureStockSummary.totalProducts }}</div>
              <div class="summary-label">总产品数</div>
            </div>
            <div class="summary-item">
              <div class="summary-value">{{ scenicSpotTickets.futureStockSummary.daysWithStock }}</div>
              <div class="summary-label">有库存天数</div>
            </div>
            <div class="summary-item">
              <div class="summary-value">{{ scenicSpotTickets.futureStockSummary.totalStockQty }}</div>
              <div class="summary-label">总库存量</div>
            </div>
            <div class="summary-item">
              <div class="summary-value warning">{{ scenicSpotTickets.futureStockSummary.soldOutDays }}</div>
              <div class="summary-label">售罄天数</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>

      <!-- 门票产品管理悬浮弹窗 -->
      <el-dialog
        v-model="ticketDialogVisible"
        :title="`${currentScenicSpot?.spotName || ''} - 门票产品管理`"
        width="1200px"
        :close-on-click-modal="false"
        top="5vh"
      >
        <div class="ticket-management-content">
          <!-- 操作按钮区 -->
          <div class="action-bar">
            <el-button type="primary" @click="showAddProductDialog">
              <el-icon><Plus /></el-icon>
              添加门票产品
            </el-button>
          </div>

          <!-- 门票产品列表 -->
          <div class="products-list">
            <el-table
              :data="currentSpotProducts"
              style="width: 100%"
              :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
              v-loading="productsLoading"
            >
              <el-table-column prop="productName" label="产品名称" min-width="200">
                <template #default="scope">
                  <div class="product-name">
                    <span>{{ scope.row.productName }}</span>
                    <el-tag size="small" class="ticket-type-tag">
                      {{ getTicketTypeLabel(scope.row.ticketType) }}
                    </el-tag>
                  </div>
                </template>
              </el-table-column>

              <el-table-column label="价格信息" min-width="200">
                <template #default="scope">
                  <div class="price-info">
                    <div class="price-item">
                      <span class="label">官网零售价：</span>
                      <span class="value">¥{{ scope.row.priceRetail }}</span>
                    </div>
                    <div class="price-item">
                      <span class="label">平台结算价：</span>
                      <span class="value">¥{{ scope.row.priceSettlement }}</span>
                    </div>
                  </div>
                </template>
              </el-table-column>

              <el-table-column label="状态" width="100">
                <template #default="scope">
                  <el-switch
                    v-model="scope.row.saleStatus"
                    :active-value="1"
                    :inactive-value="0"
                    @change="handleToggleProductStatus(scope.row)"
                  />
                </template>
              </el-table-column>

              <el-table-column label="操作" width="200" fixed="right">
                <template #default="scope">
                  <el-button
                    type="primary"
                    size="small"
                    @click="showEditProductDialog(scope.row)"
                  >
                    编辑
                  </el-button>
                  <el-button
                    type="danger"
                    size="small"
                    @click="handleDeleteProduct(scope.row)"
                    style="margin-left: 8px;"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <template #footer>
          <el-button @click="ticketDialogVisible = false">关闭</el-button>
        </template>
      </el-dialog>

      <!-- 添加/编辑门票产品弹窗 -->
      <el-dialog
        v-model="productDialogVisible"
        :title="isEditProduct ? '编辑门票产品' : '添加门票产品'"
        width="600px"
        :close-on-click-modal="false"
      >
        <el-form
          ref="productFormRef"
          :model="productForm"
          :rules="productRules"
          label-width="120px"
        >
          <el-form-item label="产品名称" prop="productName">
            <el-input
              v-model="productForm.productName"
              placeholder="请输入产品名称"
            />
          </el-form-item>

          <el-form-item label="票种类型" prop="ticketType">
            <el-select
              v-model="productForm.ticketType"
              placeholder="请选择票种类型"
              style="width: 100%"
            >
              <el-option label="原价票" :value="1" />
              <el-option label="半价票" :value="2" />
              <el-option label="免票" :value="3" />
              <el-option label="学生" :value="4" disabled />
              <el-option label="其他" :value="5" disabled />
            </el-select>
          </el-form-item>

          <el-form-item label="官网零售价" prop="priceRetail">
            <el-input-number
              v-model="productForm.priceRetail"
              :precision="2"
              :min="0"
              :max="99999"
              controls-position="right"
              style="width: 100%;"
            />
            <div class="form-tip">游客在前端看到的价格</div>
          </el-form-item>

          <el-form-item label="平台结算价" prop="priceSettlement">
            <el-input-number
              v-model="productForm.priceSettlement"
              :precision="2"
              :min="0"
              :max="99999"
              controls-position="right"
              style="width: 100%;"
            />
            <div class="form-tip">分销商的分佣基准成本</div>
          </el-form-item>

          <el-form-item label="销售状态" prop="saleStatus">
            <el-radio-group v-model="productForm.saleStatus">
              <el-radio :value="1">上架</el-radio>
              <el-radio :value="0">下架</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="productDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleProductSubmit" :loading="productSubmitLoading">
            {{ isEditProduct ? '更新' : '添加' }}
          </el-button>
        </template>
      </el-dialog>

      <!-- 景区库存管理弹窗 -->
      <el-dialog
        v-model="stockDialogVisible"
        :title="`景区库存管理 - ${currentScenicSpot?.spotName || ''}`"
        width="900px"
        :close-on-click-modal="false"
      >
        <div v-if="currentScenicSpot" class="stock-dialog-content">
          <div class="stock-intro">
            <el-alert
              title="景区总库存说明"
              description="景区总库存是所有票种共享的库存，无论购买哪种票型都会从总库存中扣减。"
              type="info"
              show-icon
              :closable="false"
            />
          </div>

          <el-divider />
          <!-- 批量操作区 -->
          <div class="batch-actions">
            <div class="batch-input">
              <span class="label">设置统一库存量：</span>
              <el-input-number
                v-model="batchStockQty"
                :min="0"
                :max="99999"
                controls-position="right"
                style="width: 120px; margin: 0 8px;"
              />
              <el-button type="primary" @click="applyBatchStock">
                应用到所有日期
              </el-button>
            </div>
          </div>

          <el-divider />

          <!-- 七天库存卡片 -->
          <div class="stock-cards-grid">
            <div
              v-for="(stock, index) in stockForm.stocks"
              :key="index"
              class="stock-card"
              :class="{ 'weekend': isWeekend(stock.stockDate) }"
            >
              <div class="card-header">
                <div class="date-info">
                  <div class="date">{{ formatDate(stock.stockDate) }}</div>
                  <div class="weekday">{{ getWeekday(stock.stockDate) }}</div>
                </div>
              </div>

              <div class="card-content">
                <div class="stock-input">
                  <span class="label">总库存</span>
                  <el-input-number
                    v-model="stock.totalQty"
                    :min="0"
                    :max="99999"
                    controls-position="right"
                    size="small"
                    style="width: 100px;"
                  />
                </div>

                <div class="stock-info">
                  <div class="info-item">
                    <span class="label">当前库存：</span>
                    <span class="value">{{ stock.totalQty }} 张</span>
                  </div>
                  <div class="info-item">
                    <span class="label">已售出：</span>
                    <span class="value sold">{{ stock.soldQty || 0 }} 张</span>
                  </div>
                  <div class="info-item">
                    <span class="label">可用库存：</span>
                    <span class="value available">{{ Math.max(0, stock.totalQty - (stock.soldQty || 0)) }} 张</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="modal-footer-note">
            <p><strong>说明：</strong>景区总库存由所有票种共享，修改库存采用"覆盖模式"，保存后将直接更新对应日期的库存总量。</p>
          </div>
        </div>

        <template #footer>
          <el-button @click="stockDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleStockSubmit" :loading="stockSubmitLoading">
            保存配置
          </el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Location, Phone, Tickets, Plus, Box } from '@element-plus/icons-vue'
import {
  getScenicSpotTicketsList,
  getTicketProductList,
  updateTicketProduct,
  toggleTicketSaleStatus,
  deleteTicketProduct,
  getScenicStockList,
  batchUpdateScenicStock
} from '@/admin/api'
import type { ScenicSpotTicketsVO, TicketProductVO, ScenicSpot } from '@/types/api'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const scenicSpots = ref<ScenicSpotTicketsVO[]>([])

// 分页数据
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

// 搜索表单
const searchForm = reactive({
  spotName: ''
})

// 门票管理弹窗相关
const ticketDialogVisible = ref(false)
const currentScenicSpot = ref<ScenicSpot | null>(null)
const currentSpotProducts = ref<TicketProductVO[]>([])
const productsLoading = ref(false)

// 门票产品弹窗相关
const productDialogVisible = ref(false)
const isEditProduct = ref(false)
const productSubmitLoading = ref(false)
const productForm = reactive({
  id: '',
  productName: '',
  ticketType: undefined as number | undefined,
  priceRetail: 0,
  priceSettlement: 0,
  saleStatus: 1
})
const productFormRef = ref()

// 景区库存管理弹窗相关
const stockDialogVisible = ref(false)
const stockSubmitLoading = ref(false)
const batchStockQty = ref(0)
const stockForm = reactive({
  stocks: [] as Array<{
    stockDate: string
    totalQty: number
    soldQty: number
  }>
})

// 表单验证规则
const productRules = {
  productName: [
    { required: true, message: '请输入产品名称', trigger: 'blur' }
  ],
  ticketType: [
    { required: true, message: '请选择票种类型', trigger: 'change' }
  ],
  priceRetail: [
    { required: true, message: '请输入官网零售价', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格不能小于0', trigger: 'blur' }
  ],
  priceSettlement: [
    { required: true, message: '请输入平台结算价', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格不能小于0', trigger: 'blur' }
  ]
}

// 方法
const getTicketTypeLabel = (type: number) => {
  const ticketTypeMap = {
    1: '原价票',
    2: '半价票',
    3: '免票',
    4: '学生',  // 已废弃，统一使用半价票
    5: '其他'   // 已废弃
  }
  return ticketTypeMap[type as keyof typeof ticketTypeMap] || '未知'
}

const loadScenicSpots = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      pageSize: pagination.pageSize,
      spotName: searchForm.spotName || undefined
    }

    const response = await getScenicSpotTicketsList(params)

    if (response.data.code === 200) {
      scenicSpots.value = response.data.data.records || []
      pagination.total = response.data.data.total || 0

      // 调试：检查接收到的数据ID是否正确
      console.log('前端组件接收到的景区数据:', scenicSpots.value)
      if (scenicSpots.value.length > 0) {
        console.log('第一个景区ID:', scenicSpots.value[0].scenicSpot.id, '类型:', typeof scenicSpots.value[0].scenicSpot.id)
        console.log('第一个景区ID长度:', String(scenicSpots.value[0].scenicSpot.id).length)
      }
    }
  } catch (error) {
    ElMessage.error('加载景区门票数据失败')
    console.error('加载景区门票数据失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadScenicSpots()
}

const handleSizeChange = (newSize: number) => {
  pagination.pageSize = newSize
  pagination.current = 1
  loadScenicSpots()
}

const handleCurrentChange = (newPage: number) => {
  pagination.current = newPage
  loadScenicSpots()
}

const manageTickets = async (scenicSpot: ScenicSpot) => {
  console.log('manageTickets接收到的景区:', scenicSpot)
  console.log('传递的景区ID:', scenicSpot.id, '类型:', typeof scenicSpot.id)
  currentScenicSpot.value = scenicSpot
  ticketDialogVisible.value = true

  // 加载该景区的门票产品
  await loadSpotProducts(scenicSpot.id)
}

const manageScenicStock = async (scenicSpot: ScenicSpot) => {
  console.log('manageScenicStock接收到的景区:', scenicSpot)
  console.log('传递的景区ID:', scenicSpot.id, '类型:', typeof scenicSpot.id)
  currentScenicSpot.value = scenicSpot
  stockDialogVisible.value = true

  // 加载该景区的库存数据
  await loadScenicStock(scenicSpot.id)
}

// 加载指定景区的门票产品
const loadSpotProducts = async (spotId: string) => {
  productsLoading.value = true
  try {
    const response = await getTicketProductList({
      spotId: spotId, // 直接传递字符串类型的ID
      current: 1,
      pageSize: 100
    })

    if (response.data.code === 200) {
      currentSpotProducts.value = response.data.data.records || []
    }
  } catch (error) {
    ElMessage.error('加载门票产品失败')
    console.error('加载门票产品失败:', error)
  } finally {
    productsLoading.value = false
  }
}

// 加载指定景区的库存数据
const loadScenicStock = async (scenicId: string) => {
  // 初始化7天的库存数据
  const dates = []
  const today = new Date()
  for (let i = 0; i < 7; i++) {
    const date = new Date(today)
    date.setDate(today.getDate() + i)
    dates.push(date.toISOString().split('T')[0])
  }

  stockForm.stocks = dates.map(date => ({
    stockDate: date,
    totalQty: 0,
    soldQty: 0
  }))

  try {
    // 调用后端API获取景区库存数据
    console.log('获取景区库存数据，景区ID:', scenicId)
    const startDate = dates[0]
    const endDate = dates[dates.length - 1]

    const response = await getScenicStockList({
      scenicId: scenicId,
      startDate,
      endDate
    })

    if (response.data.code === 200 && response.data.data) {
      // 将现有库存数据合并到表单中
      const existingStocks = response.data.data
      stockForm.stocks = stockForm.stocks.map(stockItem => {
        const existingStock = existingStocks.find((s: any) =>
          s.stockDate === stockItem.stockDate
        )
        return {
          stockDate: stockItem.stockDate,
          totalQty: existingStock ? existingStock.totalQty : 0,
          soldQty: existingStock ? existingStock.soldQty : 0
        }
      })
      console.log('成功加载景区库存数据:', stockForm.stocks)
    }
  } catch (error) {
    console.error('获取景区库存数据失败:', error)
    // 获取失败时使用默认值（已在上面初始化）
    ElMessage.warning('获取景区库存数据失败，使用默认值')
  }
}

// 显示添加门票产品弹窗
const showAddProductDialog = () => {
  isEditProduct.value = false
  productForm.id = ''
  productForm.productName = ''
  productForm.ticketType = undefined
  productForm.priceRetail = 0
  productForm.priceSettlement = 0
  productForm.saleStatus = 1
  productDialogVisible.value = true
}

// 显示编辑门票产品弹窗
const showEditProductDialog = (product: TicketProductVO) => {
  isEditProduct.value = true
  productForm.id = product.id
  productForm.productName = product.productName
  productForm.ticketType = product.ticketType
  productForm.priceRetail = product.priceRetail
  productForm.priceSettlement = product.priceSettlement
  productForm.saleStatus = product.saleStatus
  productDialogVisible.value = true
}

// 提交门票产品表单
const handleProductSubmit = async () => {
  if (!productFormRef.value) return

  await productFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return

    // 校验价格逻辑
    if (productForm.priceSettlement > productForm.priceRetail) {
      ElMessage.error('平台结算价不能高于官网零售价')
      return
    }

    // 如果价格为0（免费），确保票种类型为免票（3）
    if (productForm.priceRetail === 0 && productForm.ticketType !== 3) {
      productForm.ticketType = 3
      ElMessage.info('免费票已自动设置为免票类型')
    }

    productSubmitLoading.value = true
    try {
      const formData = {
        ...productForm,
        spotId: currentScenicSpot.value?.id
      }

      const response = await updateTicketProduct(formData)

      if (response.data.code === 200) {
        ElMessage.success(isEditProduct.value ? '更新成功' : '添加成功')
        productDialogVisible.value = false
        await loadSpotProducts(currentScenicSpot.value!.id)
        // 刷新主页面数据
        await loadScenicSpots()
      }
    } catch (error) {
      ElMessage.error(isEditProduct.value ? '更新失败' : '添加失败')
    } finally {
      productSubmitLoading.value = false
    }
  })
}

// 切换门票产品销售状态
const handleToggleProductStatus = async (product: TicketProductVO) => {
  try {
    const response = await toggleTicketSaleStatus({
      id: product.id,
      saleStatus: product.saleStatus
    })

    if (response.data.code === 200) {
      ElMessage.success('状态更新成功')
      // 刷新主页面数据
      await loadScenicSpots()
    }
  } catch (error) {
    ElMessage.error('状态更新失败')
    // 恢复原状态
    product.saleStatus = product.saleStatus === 1 ? 0 : 1
  }
}

// 删除门票产品
const handleDeleteProduct = async (product: TicketProductVO) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除门票产品"${product.productName}"吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    const response = await deleteTicketProduct({ id: product.id })

    if (response.data.code === 200) {
      ElMessage.success('删除成功')
      await loadSpotProducts(currentScenicSpot.value!.id)
      // 刷新主页面数据
      await loadScenicSpots()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}


// 应用批量库存设置
const applyBatchStock = () => {
  stockForm.stocks.forEach(stock => {
    stock.totalQty = batchStockQty.value
  })
}

// 判断是否为周末
const isWeekend = (dateStr: string) => {
  const date = new Date(dateStr)
  const day = date.getDay()
  return day === 0 || day === 6
}

// 格式化日期显示
const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return `${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
}

// 获取星期几
const getWeekday = (dateStr: string) => {
  const date = new Date(dateStr)
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return weekdays[date.getDay()]
}

// 提交景区库存配置
const handleStockSubmit = async () => {
  if (!currentScenicSpot.value) return

  stockSubmitLoading.value = true
  try {
    console.log('保存景区库存，景区ID:', currentScenicSpot.value.id, '库存数据:', stockForm.stocks)

    const response = await batchUpdateScenicStock({
      scenicId: currentScenicSpot.value.id,
      stocks: stockForm.stocks.map(stock => ({
        stockDate: stock.stockDate,
        totalQty: stock.totalQty
      }))
    })

    if (response.data.code === 200) {
      ElMessage.success('景区库存更新成功')
      stockDialogVisible.value = false
      // 刷新主页面数据
      await loadScenicSpots()
    }
  } catch (error) {
    console.error('景区库存更新失败:', error)
    ElMessage.error('景区库存更新失败')
  } finally {
    stockSubmitLoading.value = false
  }
}

// 生命周期
onMounted(() => {
  loadScenicSpots()
})
</script>

<style scoped>
.ticket-inventory-management {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  background: white;
  padding: 20px;
  border-radius: 6px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.page-header h2 {
  margin: 0 0 10px 0;
  color: #333;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.scenic-spots-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.scenic-spot-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.spot-header {
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.spot-info {
  flex: 1;
}

.spot-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.spot-title h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.level-tag {
  background: #e1f3d8;
  color: #67c23a;
  border-color: #b3e5a4;
}

.spot-meta {
  display: flex;
  gap: 20px;
  color: #909399;
  font-size: 14px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.spot-actions {
  flex-shrink: 0;
  display: flex;
  gap: 10px;
}

.tickets-section,
.stock-summary-section {
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
}

.tickets-section:last-child,
.stock-summary-section:last-child {
  border-bottom: none;
}

.section-header h4 {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.tickets-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 15px;
}

.ticket-product-item {
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  padding: 15px;
  background: #fafbfc;
}

.product-name {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  font-weight: 500;
  color: #303133;
}

.ticket-type-tag {
  background: #e1f3d8;
  color: #67c23a;
  border-color: #b3e5a4;
}

.product-prices {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.price-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.price-item .label {
  color: #909399;
}

.price-item .value {
  font-weight: 600;
  color: #303133;
}

.no-tickets {
  padding: 40px 0;
}

.stock-summary-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.summary-item {
  text-align: center;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 6px;
}

.summary-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.summary-value.warning {
  color: #e6a23c;
}

.summary-label {
  color: #909399;
  font-size: 12px;
}

.pagination-wrapper {
  background: white;
  padding: 20px;
  border-radius: 6px;
  margin-top: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: center;
}

/* 门票管理弹窗样式 */
.ticket-management-content {
  max-height: 70vh;
  overflow-y: auto;
}

.action-bar {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.products-list {
  margin-bottom: 20px;
}

.product-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.ticket-type-tag {
  background: #e1f3d8;
  color: #67c23a;
  border-color: #b3e5a4;
}

.price-info {
  line-height: 1.8;
}

.price-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price-item .label {
  color: #909399;
  font-size: 12px;
  min-width: 80px;
}

.price-item .value {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.form-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}

/* 库存管理弹窗样式 */
.stock-dialog-content {
  max-height: 600px;
  overflow-y: auto;
}

.batch-actions {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 6px;
  margin-bottom: 10px;
}

.batch-input {
  display: flex;
  align-items: center;
}

.batch-input .label {
  font-weight: 500;
  color: #303133;
  margin-right: 8px;
}

.stock-cards-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 12px;
  margin: 20px 0;
}

.stock-card {
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 12px;
  text-align: center;
  background: white;
  transition: all 0.2s ease;
}

.stock-card.weekend {
  background: #fef7f0;
  border-color: #f5dab1;
}

.stock-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stock-card .card-header {
  margin-bottom: 12px;
  padding: 0;
  border: none;
  background: none;
  color: inherit;
}

.date-info .date {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.date-info .weekday {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.stock-input {
  margin-bottom: 12px;
}

.stock-input .label {
  display: block;
  font-size: 12px;
  color: #606266;
  margin-bottom: 6px;
}

.stock-info {
  border-top: 1px solid #ebeef5;
  padding-top: 8px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  margin-bottom: 4px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-item .label {
  color: #909399;
}

.info-item .value {
  color: #303133;
  font-weight: 500;
}

.info-item .value.sold {
  color: #f56c6c;
}

.info-item .value.available {
  color: #67c23a;
}

.modal-footer-note {
  margin-top: 20px;
  padding: 12px;
  background: #fffbe6;
  border: 1px solid #f5dab1;
  border-radius: 4px;
}

.modal-footer-note p {
  margin: 0;
  color: #e6a23c;
  font-size: 13px;
}
</style>
