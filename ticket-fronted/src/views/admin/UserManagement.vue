<template>
  <div class="user-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>用户管理</h2>
      <p class="page-subtitle">管理系统用户账号，查看用户注册趋势</p>
    </div>


    <!-- 用户列表 -->
    <div class="user-list-section">
      <div class="list-header">
        <h3>用户列表</h3>
        <div class="search-controls">
          <el-input
            v-model="searchKey"
            placeholder="搜索邮箱或昵称"
            clearable
            style="width: 250px"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-select v-model="roleFilter" placeholder="角色筛选" clearable style="width: 120px; margin-left: 12px">
            <el-option label="游客" :value="1" />
            <el-option label="经销商" :value="2" />
          </el-select>
          <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 120px; margin-left: 12px">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
          <el-button type="primary" @click="handleSearch" style="margin-left: 12px">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
        </div>
      </div>

      <!-- 用户列表表格 -->
      <el-table
        v-loading="loading"
        :data="userList"
        stripe
        style="width: 100%"
        :header-cell-style="{ background: '#fafafa', color: '#333' }"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="头像" width="80">
          <template #default="scope">
            <el-avatar :src="scope.row.avatar" size="40">
              {{ scope.row.nickname?.[0] || '用' }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="userAccount" label="邮箱账号" min-width="200" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column label="角色" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.role === 2 ? 'warning' : 'info'">
              {{ scope.row.role === 1 ? '游客' : scope.row.role === 2 ? '经销商' : '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="账号状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.userStatus === 1 ? 'success' : 'danger'">
              {{ scope.row.userStatus === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.userStatus === 1"
              type="warning"
              size="small"
              @click="updateUserStatus(scope.row, 0)"
            >
              禁用
            </el-button>
            <el-button
              v-else
              type="success"
              size="small"
              @click="updateUserStatus(scope.row, 1)"
            >
              恢复
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="deleteUser(scope.row)"
              style="margin-left: 8px"
            >
              删除
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

// API imports
import { listUsers, updateUserStatus as updateStatusApi, deleteUser as deleteUserApi } from '@/admin/api'

// Types
interface UserVO {
  id: string
  userAccount: string
  nickname: string
  avatar?: string
  role: number
  userStatus: number
  auditStatus?: number
  createTime: string
  updateTime: string
}

// Reactive data
const userList = ref<UserVO[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// Search and filter
const searchKey = ref('')
const roleFilter = ref<number | undefined>()
const statusFilter = ref<number | undefined>()


// Methods
const loadUserList = async () => {
  try {
    loading.value = true
    const params = {
      current: currentPage.value,
      pageSize: pageSize.value,
      searchKey: searchKey.value || undefined,
      role: roleFilter.value,
      userStatus: statusFilter.value
    }

    const response = await listUsers(params)
    if (response.data.code === 200) {
      userList.value = response.data.data.records
      total.value = response.data.data.total
    } else {
      ElMessage.error(response.data.message || '获取用户列表失败')
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error(error.response?.data?.message || '加载用户列表失败')
  } finally {
    loading.value = false
  }
}


const handleSearch = () => {
  currentPage.value = 1
  loadUserList()
}

const handleSizeChange = (newSize: number) => {
  pageSize.value = newSize
  currentPage.value = 1
  loadUserList()
}

const handleCurrentChange = (newPage: number) => {
  currentPage.value = newPage
  loadUserList()
}

const updateUserStatus = async (user: UserVO, newStatus: number) => {
  try {
    const action = newStatus === 1 ? '恢复' : '禁用'
    await ElMessageBox.confirm(
      `确定要${action}用户"${user.nickname}"吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: newStatus === 1 ? 'success' : 'warning'
      }
    )

    const response = await updateStatusApi({ id: user.id, userStatus: newStatus })
    if (response.data.code === 200) {
      ElMessage.success(`${action}成功`)
      user.userStatus = newStatus
    } else {
      ElMessage.error(response.data.message || `${action}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const deleteUser = async (user: UserVO) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户"${user.nickname}"吗？此操作不可恢复！`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }
    )

    const response = await deleteUserApi({ id: user.id })
    if (response.data.code === 200) {
      ElMessage.success('删除成功')
      loadUserList()
    } else {
      ElMessage.error(response.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
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
  console.log('UserManagement页面已加载')
  loadUserList()
})
</script>

<style scoped>
.user-management {
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


/* 用户列表区域 */
.user-list-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.list-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1d2129;
}

.search-controls {
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

/* 分页样式 */
.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

:deep(.el-pagination) {
  padding: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-management {
    padding: 16px;
  }

  .search-controls {
    flex-direction: column;
    gap: 12px;
    width: 100%;
  }

  .search-controls .el-input,
  .search-controls .el-select {
    width: 100% !important;
    margin-left: 0 !important;
  }

  .list-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
}
</style>
