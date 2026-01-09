<template>
  <div class="distributor-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>经销商管理</h2>
      <p class="page-subtitle">审核经销商申请，管理经销商信息和业绩数据</p>
    </div>

    <!-- 审核申请区域 -->
    <div class="audit-section">
      <div class="section-header">
        <h3>经销商申请审核</h3>
        <el-tag type="warning">{{ pendingAudits.length }} 个待审核</el-tag>
      </div>

      <div v-if="pendingAudits.length === 0" class="no-audits">
        <el-empty description="暂无待审核的经销商申请" />
      </div>

      <div v-else class="audit-list">
        <div
          v-for="distributor in pendingAudits"
          :key="distributor.id"
          class="audit-card"
        >
          <div class="audit-info">
            <div class="info-row">
              <span class="label">申请人：</span>
              <span class="value">{{ distributor.nickname }} ({{ distributor.userAccount }})</span>
            </div>
            <div class="info-row">
              <span class="label">申请时间：</span>
              <span class="value">{{ formatDate(distributor.createTime) }}</span>
            </div>
            <div class="info-row">
              <span class="label">审核状态：</span>
              <span class="value">
                <el-tag :type="distributor.auditStatus === 1 ? 'success' : distributor.auditStatus === 2 ? 'danger' : 'warning'" size="small">
                  {{ distributor.auditStatus === 1 ? '已通过' : distributor.auditStatus === 2 ? '已拒绝' : '待审核' }}
                </el-tag>
              </span>
            </div>
          </div>
          <div class="audit-actions">
            <el-button type="success" size="small" @click="auditDistributor(distributor, 1)">
              通过
            </el-button>
            <el-button type="danger" size="small" @click="auditDistributor(distributor, 2)">
              拒绝
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 经销商列表 -->
    <div class="distributor-list-section">
      <div class="section-header">
        <h3>经销商列表</h3>
        <div class="filter-controls">
          <el-input
            v-model="searchKey"
            placeholder="搜索经销商"
            clearable
            style="width: 250px"
            @keyup.enter="loadDistributors"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="loadDistributors" style="margin-left: 12px">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="distributorList"
        stripe
        style="width: 100%"
        :header-cell-style="{ background: '#fafafa', color: '#333' }"
      >
        <el-table-column label="头像" width="80">
          <template #default="scope">
            <el-avatar :src="scope.row.avatar" size="40">
              {{ scope.row.nickname?.[0] || '经' }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="userAccount" label="邮箱账号" min-width="200" />
        <el-table-column prop="nickname" label="经销商名称" min-width="120" />
        <el-table-column label="审核状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.auditStatus === 1 ? 'success' : scope.row.auditStatus === 2 ? 'danger' : 'warning'">
              {{ scope.row.auditStatus === 1 ? '已通过' : scope.row.auditStatus === 2 ? '已拒绝' : '待审核' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="订单数" width="100" align="center">
          <template #default="scope">
            <span class="stat-number">{{ scope.row.orderCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="销售额(元)" width="120" align="center">
          <template #default="scope">
            <span class="stat-number">{{ scope.row.salesAmount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="佣金(元)" width="120" align="center">
          <template #default="scope">
            <span class="stat-number">{{ scope.row.commission || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewDistributorDetail(scope.row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
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

    <!-- 经销商详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="经销商详情"
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-if="currentDistributor" class="distributor-detail">
        <div class="detail-header">
          <el-avatar :src="currentDistributor.avatar" size="80">
            {{ currentDistributor.nickname?.[0] || '经' }}
          </el-avatar>
          <div class="detail-info">
            <h3>{{ currentDistributor.nickname }}</h3>
            <p>{{ currentDistributor.userAccount }}</p>
            <el-tag :type="currentDistributor.auditStatus === 1 ? 'success' : currentDistributor.auditStatus === 2 ? 'danger' : 'warning'">
              {{ currentDistributor.auditStatus === 1 ? '已认证经销商' : currentDistributor.auditStatus === 2 ? '审核未通过' : '待审核' }}
            </el-tag>
          </div>
        </div>

        <div class="detail-stats">
          <div class="stat-item">
            <div class="stat-value">{{ currentDistributor.orderCount || 0 }}</div>
            <div class="stat-label">总订单数</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">¥{{ currentDistributor.salesAmount || 0 }}</div>
            <div class="stat-label">总销售额</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">¥{{ currentDistributor.commission || 0 }}</div>
            <div class="stat-label">总佣金</div>
          </div>
        </div>

        <div class="detail-content">
          <div class="info-section">
            <h4>基本信息</h4>
            <div class="info-grid">
              <div class="info-item">
                <label>邮箱账号：</label>
                <span>{{ currentDistributor.userAccount }}</span>
              </div>
              <div class="info-item">
                <label>注册时间：</label>
                <span>{{ formatDate(currentDistributor.createTime) }}</span>
              </div>
              <div class="info-item">
                <label>审核状态：</label>
                <el-tag :type="currentDistributor.auditStatus === 1 ? 'success' : currentDistributor.auditStatus === 2 ? 'danger' : 'warning'" size="small">
                  {{ currentDistributor.auditStatus === 1 ? '已通过' : currentDistributor.auditStatus === 2 ? '已拒绝' : '待审核' }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

// API imports
import { listDistributors, auditDistributor as auditApi, getDistributorStats } from '@/admin/api'

// Types
import type { DistributorVO } from '@/types/api'

// Reactive data
const distributorList = ref<DistributorVO[]>([])
const pendingAudits = ref<DistributorVO[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKey = ref('')

// Detail dialog
const detailDialogVisible = ref(false)
const currentDistributor = ref<DistributorVO | null>(null)

// Methods
// 加载经销商列表
const loadDistributors = async () => {
  try {
    loading.value = true
    const params = {
      current: currentPage.value,
      pageSize: pageSize.value,
      searchKey: searchKey.value || undefined,
      role: 2, // 只查询经销商
      auditStatus: undefined // 查询所有审核状态的经销商
    }

    const response = await listDistributors(params)
    if (response.data.code === 200) {
      distributorList.value = response.data.data.records
      total.value = response.data.data.total
    } else {
      ElMessage.error(response.data.message || '获取经销商列表失败')
    }
  } catch (error: any) {
    console.error('加载经销商列表失败:', error)
    ElMessage.error(error.response?.data?.message || '加载经销商列表失败')
  } finally {
    loading.value = false
  }
}

// 加载待审核申请
const loadPendingAudits = async () => {
  try {
    const params = {
      current: 1,
      pageSize: 100, // 获取所有待审核的申请
      auditStatus: 0 // 待审核状态
    }

    const response = await listDistributors(params)
    if (response.data.code === 200) {
      pendingAudits.value = response.data.data.records
    }
  } catch (error: any) {
    console.error('加载待审核申请失败:', error)
    // 如果API调用失败，使用空数组
    pendingAudits.value = []
  }
}

const auditDistributor = async (distributor: DistributorVO, auditStatus: number) => {
  try {
    const action = auditStatus === 1 ? '通过' : '拒绝'
    await ElMessageBox.confirm(
      `确定要${action}经销商"${distributor.nickname}"的申请吗？`,
      '审核确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: auditStatus === 1 ? 'success' : 'warning'
      }
    )

    const response = await auditApi({ id: distributor.id, auditStatus })
    if (response.data.code === 200) {
      ElMessage.success(`审核${action}成功`)

      // 从待审核列表中移除
      const index = pendingAudits.value.findIndex(d => d.id === distributor.id)
      if (index > -1) {
        pendingAudits.value.splice(index, 1)
      }

      // 如果详情对话框中显示的是当前被审核的经销商，更新其状态
      if (currentDistributor.value && currentDistributor.value.id === distributor.id) {
        currentDistributor.value.auditStatus = auditStatus
      }

      // 刷新经销商列表
      loadDistributors()
    } else {
      ElMessage.error(response.data.message || `审核${action}失败`)
    }

  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('审核失败:', error)
      ElMessage.error(error.response?.data?.message || '审核失败')
    }
  }
}

const viewDistributorDetail = (distributor: DistributorVO) => {
  currentDistributor.value = distributor
  detailDialogVisible.value = true
}

const handleSizeChange = (newSize: number) => {
  pageSize.value = newSize
  currentPage.value = 1
  loadDistributors()
}

const handleCurrentChange = (newPage: number) => {
  currentPage.value = newPage
  loadDistributors()
}

const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// Lifecycle
onMounted(() => {
  loadDistributors()
  loadPendingAudits()
})
</script>

<style scoped>
.distributor-management {
  padding: 24px;
  background: #f8f9fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 32px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #1d2129;
  font-size: 28px;
  font-weight: 600;
}

.page-subtitle {
  margin: 0;
  color: #86909c;
  font-size: 16px;
}

/* 审核区域 */
.audit-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1d2129;
}

.audit-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.audit-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: white;
  transition: all 0.3s;
}

.audit-card:hover {
  border-color: #1890ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.1);
}

.audit-info {
  flex: 1;
}

.info-row {
  display: flex;
  margin-bottom: 8px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-row .label {
  font-weight: 600;
  color: #1d2129;
  min-width: 80px;
}

.info-row .value {
  color: #666;
  flex: 1;
}

.audit-actions {
  display: flex;
  gap: 12px;
}

.no-audits {
  padding: 40px 0;
}

/* 经销商列表区域 */
.distributor-list-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.filter-controls {
  display: flex;
  align-items: center;
}

/* 表格样式 */
:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table th) {
  background: #fafbfc !important;
  color: #1d2129 !important;
  font-weight: 600;
  border-bottom: 2px solid #e5e6eb;
}

:deep(.el-table td) {
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-table tbody tr:hover > td) {
  background: #f8f9fa;
}

.stat-number {
  font-weight: 600;
  color: #1890ff;
}

/* 分页样式 */
.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

:deep(.el-pagination) {
  padding: 0;
}

/* 经销商详情对话框 */
.distributor-detail {
  max-width: 600px;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 2px solid #e5e6eb;
}

.detail-info h3 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #1d2129;
}

.detail-info p {
  margin: 0 0 12px 0;
  color: #86909c;
  font-size: 16px;
}

.detail-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 32px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e5e6eb;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1890ff;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #86909c;
  font-weight: 500;
}

.detail-content {
  margin-top: 24px;
}

.info-section h4 {
  margin: 0 0 20px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1d2129;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background: #fafbfc;
  border-radius: 6px;
  border: 1px solid #e5e6eb;
}

.info-item label {
  font-weight: 600;
  color: #1d2129;
  min-width: 100px;
}

.info-item span {
  color: #666;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .distributor-management {
    padding: 16px;
  }

  .audit-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .audit-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .detail-header {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }

  .detail-stats {
    grid-template-columns: 1fr;
  }

  .filter-controls {
    flex-direction: column;
    gap: 12px;
    width: 100%;
  }

  .filter-controls .el-input {
    width: 100% !important;
  }
}
</style>
