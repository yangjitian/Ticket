<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside width="200px" class="admin-sidebar">
      <div class="sidebar-header">
        <h3>管理员后台</h3>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="admin-menu"
        @select="handleMenuSelect"
      >
        <el-menu-item index="dashboard">
          <el-icon><House /></el-icon>
          <span>控制台</span>
        </el-menu-item>
        <el-menu-item index="user-management">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="scenic-management">
          <el-icon><Location /></el-icon>
          <span>景区管理</span>
        </el-menu-item>
        <el-menu-item index="distributor-management">
          <el-icon><Shop /></el-icon>
          <span>经销商管理</span>
        </el-menu-item>
        <el-menu-item index="ticket-inventory">
          <el-icon><Tickets /></el-icon>
          <span>门票价格及库存管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 头部 -->
      <el-header class="admin-header">
        <div class="header-left">
          <span class="welcome-text">欢迎您，管理员</span>
        </div>
        <div class="header-right">
          <el-button type="text" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            退出登录
          </el-button>
        </div>
      </el-header>

      <!-- 内容区域 -->
      <el-main class="admin-main">
        <AdminHome v-if="currentView === 'dashboard'" />
        <UserManagement v-else-if="currentView === 'user-management'" />
        <ScenicManagement v-else-if="currentView === 'scenic-management'" />
        <DistributorManagement v-else-if="currentView === 'distributor-management'" />
        <TicketInventory v-else-if="currentView === 'ticket-inventory'" />
      </el-main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  House,
  User,
  Location,
  Shop,
  Tickets,
  SwitchButton
} from '@element-plus/icons-vue'
import AdminHome from './AdminHome.vue'
import UserManagement from './UserManagement.vue'
import ScenicManagement from './ScenicManagement.vue'
import DistributorManagement from './DistributorManagement.vue'
import TicketInventory from './TicketInventory.vue'
import { logout, getCurrentAdmin } from '@/admin/api'

const router = useRouter()
const route = useRoute()

// 当前激活的菜单和视图
const activeMenu = ref('dashboard')
const currentView = ref('dashboard')

// 根据路由路径设置当前视图
const setCurrentView = (path: string) => {
  if (path === '/admin/dashboard') {
    currentView.value = 'dashboard'
    activeMenu.value = 'dashboard'
  } else if (path === '/admin/user-management') {
    currentView.value = 'user-management'
    activeMenu.value = 'user-management'
  } else if (path === '/admin/scenic-management') {
    currentView.value = 'scenic-management'
    activeMenu.value = 'scenic-management'
  } else if (path === '/admin/distributor-management') {
    currentView.value = 'distributor-management'
    activeMenu.value = 'distributor-management'
  } else if (path === '/admin/ticket-inventory') {
    currentView.value = 'ticket-inventory'
    activeMenu.value = 'ticket-inventory'
  }
}

// 验证管理员token并初始化
const validateAdminToken = async () => {
  const token = localStorage.getItem('admin_token')

  if (token) {
    try {
      // 验证token有效性
      await getCurrentAdmin()
      // token有效，继续正常流程
      setCurrentView(route.path)
    } catch (error) {
      // token无效，跳转到登录页
      console.warn('管理员token无效:', error)
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_userRole')
      localStorage.removeItem('admin_user')
      router.push('/admin/login')
    }
  } else {
    // 没有token，跳转到登录页
    router.push('/admin/login')
  }
}

// 监听路由变化更新菜单激活状态
onMounted(async () => {
  await validateAdminToken()
})

// 菜单选择处理
const handleMenuSelect = (index: string) => {
  activeMenu.value = index
  currentView.value = index

  switch (index) {
    case 'dashboard':
      router.push('/admin/dashboard')
      break
    case 'user-management':
      router.push('/admin/user-management')
      break
    case 'scenic-management':
      router.push('/admin/scenic-management')
      break
    case 'distributor-management':
      router.push('/admin/distributor-management')
      break
    case 'ticket-inventory':
      router.push('/admin/ticket-inventory')
      break
  }
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 先清除管理员本地存储，防止其他API请求携带失效token
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_userRole')
    localStorage.removeItem('admin_user')

    // 再调用登出API
    try {
      await logout()
    } catch (error) {
      // 登出API失败不影响本地清理，静默处理
      console.warn('登出API调用失败:', error)
    }

    ElMessage.success('退出登录成功')

    // 跳转到登录页
    router.push('/admin/login')
  } catch (error) {
    // 用户取消操作或API调用失败
    if (error !== 'cancel') {
      console.error('退出登录失败:', error)
      // 即使API调用失败，也清除本地存储并跳转
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_userRole')
      localStorage.removeItem('admin_user')
      router.push('/admin/login')
    }
  }
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  display: flex;
}

.admin-sidebar {
  background-color: #f5f5f5;
  border-right: 1px solid #e6e6e6;
}

.sidebar-header {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid #e6e6e6;
}

.sidebar-header h3 {
  margin: 0;
  color: #333;
}

.admin-menu {
  border-right: none;
}

.admin-header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.welcome-text {
  color: #666;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.admin-main {
  flex: 1;
  padding: 20px;
  background-color: #f5f5f5;
}
</style>
