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
            <router-link to="/spots" class="nav-link" active-class="active">景区库</router-link>
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
                style="width: 200px"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </div>

            <!-- 用户信息 -->
            <div v-if="isLoggedIn" class="user-info">
              <el-dropdown @command="handleUserCommand">
                <span class="user-dropdown">
                  <el-avatar :src="userAvatar" size="small">{{ userNickname?.[0] || '用' }}</el-avatar>
                  <span class="username">{{ userNickname || '用户' }}</span>
                  <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                    <el-dropdown-item command="orders">我的订单</el-dropdown-item>
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
              <li><router-link to="/spots">景区库</router-link></li>
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { Search, ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()

// 搜索相关
const searchQuery = ref('')

// 用户状态
const isLoggedIn = computed(() => {
  return !!localStorage.getItem('token')
})

const userNickname = computed(() => {
  const user = localStorage.getItem('user')
  return user ? JSON.parse(user).nickname : null
})

const userAvatar = computed(() => {
  const user = localStorage.getItem('user')
  return user ? JSON.parse(user).avatar : null
})

// 处理搜索
const handleSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({
      path: '/spots',
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
    case 'logout':
      handleLogout()
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

    localStorage.removeItem('token')
    localStorage.removeItem('user')
    localStorage.removeItem('userRole')

    router.push('/')
  } catch {
    // 用户取消操作
  }
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
  height: 64px;
}

/* Logo */
.logo h1 {
  margin: 0;
  font-size: 24px;
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
  padding: 8px 16px;
  border-radius: 6px;
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
  gap: 16px;
}

/* 搜索框 */
.search-box .el-input {
  border-radius: 20px;
}

/* 用户信息 */
.user-info .user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 6px;
  transition: background-color 0.3s;
}

.user-info .user-dropdown:hover {
  background-color: #f5f5f5;
}

.username {
  font-weight: 500;
  color: #333;
}

.dropdown-icon {
  color: #999;
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
