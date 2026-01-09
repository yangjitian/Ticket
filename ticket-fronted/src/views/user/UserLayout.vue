<template>
  <div class="user-layout">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="container">
        <div class="header-content">
          <!-- Logo -->
          <div class="logo">
            <router-link to="/" class="logo-link">
              <h1>旅游分销平台</h1>
            </router-link>
          </div>

          <!-- 导航菜单 -->
          <nav class="nav">
            <router-link to="/" class="nav-link" active-class="active">首页</router-link>
          </nav>

          <!-- 用户区域 -->
          <div class="user-area">
            <!-- 搜索框 -->
            <div class="search-box">
              <el-input
                v-model="searchQuery"
                placeholder="搜索景区..."
                @keyup.enter="handleSearch"
                clearable
                style="width: 280px"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </div>

            <!-- 用户信息 -->
            <div v-if="userState.isLoggedIn" class="user-info">
              <el-dropdown @command="handleUserCommand">
                <span class="user-dropdown">
                  <el-avatar :src="userState.userAvatar" size="default">{{ userState.userNickname?.[0] || '用' }}</el-avatar>
                  <span class="username">{{ userState.userNickname || '用户' }}</span>
                  <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <!-- 经销商审核状态提示 -->
                    <div v-if="userState.userRole === '2'" class="audit-status" :class="getAuditStatusClass()">
                      <el-icon>
                        <Clock v-if="userState.userAuditStatus === '0'" />
                        <Check v-else-if="userState.userAuditStatus === '1'" />
                        <Close v-else-if="userState.userAuditStatus === '2'" />
                        <QuestionFilled v-else />
                      </el-icon>
                      <span>{{ getAuditStatusText() }}</span>
                    </div>
                    <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                    <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                    <!-- 经销商审核通过后显示经销管理 -->
                    <el-dropdown-item v-if="userState.userRole === '2' && userState.userAuditStatus === '1'" command="distributor" divided>
                      经销管理
                    </el-dropdown-item>
                    <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>

            <!-- 登录按钮 -->
            <div v-else class="login-btn">
              <el-button type="primary" @click="goToLogin">登录</el-button>
            </div>
          </div>
        </div>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <main class="main-content">
      <router-view />
    </main>

    <!-- 底部信息 -->
    <footer class="footer">
      <div class="container">
        <div class="footer-content">
          <div class="footer-section">
            <h3>旅游分销平台</h3>
            <p>专业的景区门票分销平台，为您提供便捷的旅游服务</p>
          </div>
          <div class="footer-section">
            <h4>快速链接</h4>
            <ul>
              <li><router-link to="/">首页</router-link></li>
              <li><router-link to="/about">关于我们</router-link></li>
              <li><a href="#contact">联系我们</a></li>
            </ul>
          </div>
          <div class="footer-section">
            <h4>服务支持</h4>
            <ul>
              <li><a href="#help">帮助中心</a></li>
              <li><a href="#privacy">隐私政策</a></li>
              <li><a href="#terms">使用条款</a></li>
            </ul>
          </div>
        </div>
        <div class="footer-bottom">
          <p>&copy; 2025 旅游分销平台. All rights reserved.</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, ArrowDown, Clock, Check, Close, QuestionFilled } from '@element-plus/icons-vue'
import { userLogout, getCurrentUser } from '@/user/api'
import axios from 'axios'

const router = useRouter()

// 搜索相关
const searchQuery = ref('')

// 页面加载时初始化状态
onMounted(() => {
  initUserState()

  // 监听localStorage变化，更新用户状态
  window.addEventListener('storage', (e) => {
    if (e.key === 'user_token' || e.key === 'user_user' || e.key === 'user_userRole') {
      initUserState()
    }
  })
})

// 用户状态 - 使用reactive对象管理
const userState = reactive({
  isLoggedIn: false,
  userNickname: null as string | null,
  userAvatar: null as string | null,
  userRole: null as string | null,
  userAuditStatus: null as string | null,
  userStatus: null as string | null
})

// 初始化用户状态
const initUserState = async () => {
  const token = localStorage.getItem('user_token')

  // 如果有token，验证其有效性并获取最新用户信息
  if (token) {
    try {
      // 调用API获取当前用户信息（包含最新状态）
      const response = await getCurrentUser()
      if (response.data.code === 200) {
        const userData = response.data.data
        userState.isLoggedIn = true
        userState.userNickname = userData.nickname || null
        userState.userAvatar = userData.avatar || null
        userState.userRole = userData.role || null
        userState.userAuditStatus = userData.auditStatus || null
        userState.userStatus = userData.userStatus || null

        // 更新localStorage中的用户信息
        localStorage.setItem('user_user', JSON.stringify(userData))
        localStorage.setItem('user_userRole', userData.role)
        localStorage.setItem('user_userAuditStatus', userData.auditStatus)
      } else {
        throw new Error(response.data.message || '获取用户信息失败')
      }
    } catch (error) {
      // token无效或API调用失败，清除本地状态
      console.warn('获取用户信息失败，清除登录状态:', error)
      clearUserStates()
      return
    }
  } else {
    userState.isLoggedIn = false
    userState.userNickname = null
    userState.userAvatar = null
    userState.userRole = null
    userState.userAuditStatus = null
    userState.userStatus = null
  }
}

// 处理搜索
const handleSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({
      path: '/',
      query: { search: searchQuery.value.trim() }
    })
  }
}

// 处理用户菜单
const handleUserCommand = (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'orders':
      router.push('/orders')
      break
    case 'distributor':
      router.push('/distributor')
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 清理用户状态（只清理用户相关的，不影响管理员）
const clearUserStates = () => {

  // 清理localStorage中的用户相关数据
  localStorage.removeItem('user_token')
  localStorage.removeItem('user_user')
  localStorage.removeItem('user_userRole')

  // 清理sessionStorage（如果有使用）
  sessionStorage.removeItem('user_token')
  sessionStorage.removeItem('user_user')
  sessionStorage.removeItem('user_userRole')

  // 清理可能存在的其他缓存
  try {
    // 清理所有以user开头的localStorage项
    const keysToRemove = []
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i)
      if (key && key.startsWith('user_')) {
        keysToRemove.push(key)
      }
    }
    keysToRemove.forEach(key => {
      localStorage.removeItem(key)
    })

    // 清理sessionStorage
    for (let i = 0; i < sessionStorage.length; i++) {
      const key = sessionStorage.key(i)
      if (key && key.startsWith('user_')) {
        sessionStorage.removeItem(key)
      }
    }
  } catch (error) {
    console.warn('清理缓存时出错:', error)
  }

  // 更新reactive状态
  userState.isLoggedIn = false
  userState.userNickname = null
  userState.userAvatar = null
  userState.userRole = null
  userState.userAuditStatus = null
  userState.userStatus = null

}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 获取当前用户token，用于API调用
    const currentToken = localStorage.getItem('user_token')

    // 先调用登出API
    if (currentToken) {
      try {
        await axios.post('http://localhost:8088/api/user/logout', {}, {
          headers: {
            'Authorization': `Bearer ${currentToken}`
          }
        })
      } catch (error) {
        // 登出API失败不影响本地清理，静默处理
        console.warn('后端登出失败，但本地状态已清理:', error)
      }
    }

    // 清理用户状态（优先执行，提升响应速度）
    clearUserStates()

    // 额外清理：清除所有可能的缓存和状态
    try {
      localStorage.removeItem('user')
    localStorage.removeItem('token')
    localStorage.removeItem('userRole')
      sessionStorage.clear()
    } catch (error) {
      console.warn('额外清理过程中出错:', error)
    }

    // 显示成功消息并立即跳转
    ElMessage.success({
      message: '退出登录成功',
      duration: 800,
      onClose: () => {
        // 消息关闭时立即跳转
        window.location.href = '/login'
      }
    })

    // 备用跳转（防止消息关闭事件不触发）
    setTimeout(() => {
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }, 100)

  } catch {
    // 用户取消操作
  }
}

// 获取审核状态文本
const getAuditStatusText = () => {
  if (userState.userAuditStatus === '0') return '审核中'
  if (userState.userAuditStatus === '1') return '审核通过'
  if (userState.userAuditStatus === '2') return '审核拒绝'
  return '未知状态'
}

// 获取审核状态样式类
const getAuditStatusClass = () => {
  if (userState.userAuditStatus === '0') return 'audit-pending'
  if (userState.userAuditStatus === '1') return 'audit-passed'
  if (userState.userAuditStatus === '2') return 'audit-rejected'
  return 'audit-unknown'
}



// 跳转到登录页
const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.user-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

/* 顶部导航栏 */
.header {
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 80px;
}

/* Logo */
.logo h1 {
  margin: 0;
  font-size: 32px;
  font-weight: 600;
  color: #1890ff;
}

.logo-link {
  text-decoration: none;
  color: inherit;
}

/* 导航菜单 */
.nav {
  display: flex;
  gap: 32px;
}

.nav-link {
  text-decoration: none;
  color: #666;
  font-weight: 500;
  font-size: 16px;
  padding: 12px 20px;
  border-radius: 8px;
  transition: all 0.3s;
}

.nav-link:hover,
.nav-link.active {
  color: #1890ff;
  background-color: #f0f8ff;
}

/* 用户区域 */
.user-area {
  display: flex;
  align-items: center;
  gap: 24px;
}

/* 搜索框 */
.search-box .el-input {
  border-radius: 20px;
}

/* 用户信息 */
.user-info .user-dropdown {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 10px 14px;
  border-radius: 8px;
  transition: background-color 0.3s;
}

.user-info .user-dropdown:hover {
  background-color: #f5f5f5;
}

.username {
  font-weight: 500;
  font-size: 16px;
  color: #333;
}

.dropdown-icon {
  color: #999;
}

/* 审核状态 */
.audit-status {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  font-size: 14px;
  border-bottom: 1px solid #ebeef5;
}

.audit-status.audit-pending {
  color: #e6a23c;
}

.audit-status.audit-passed {
  color: #67c23a;
}

.audit-status.audit-rejected {
  color: #f56c6c;
}

.audit-status.audit-unknown {
  color: #909399;
}

/* 主内容区域 */
.main-content {
  flex: 1;
  padding: 20px 0;
}

/* 底部 */
.footer {
  background: #2c3e50;
  color: white;
  margin-top: auto;
}

.footer-content {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  gap: 40px;
  padding: 40px 0;
}

.footer-section h3 {
  margin: 0 0 16px 0;
  font-size: 20px;
  color: #1890ff;
}

.footer-section h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
}

.footer-section p {
  color: #bdc3c7;
  line-height: 1.6;
}

.footer-section ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.footer-section li {
  margin-bottom: 8px;
}

.footer-section a {
  color: #bdc3c7;
  text-decoration: none;
  transition: color 0.3s;
}

.footer-section a:hover {
  color: #1890ff;
}

.footer-bottom {
  border-top: 1px solid #34495e;
  padding: 20px 0;
  text-align: center;
  color: #95a5a6;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    height: auto;
    padding: 12px 0;
    flex-direction: column;
    gap: 12px;
  }

  .nav {
    gap: 16px;
  }

  .user-area {
    width: 100%;
    justify-content: space-between;
  }

  .search-box .el-input {
    width: 150px !important;
  }

  .footer-content {
    grid-template-columns: 1fr;
    gap: 24px;
  }
}
</style>
