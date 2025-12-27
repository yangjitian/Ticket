<template>
  <div class="user-profile-page">
    <div class="container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1 class="page-title">个人中心</h1>
        <p class="page-subtitle">管理您的个人信息和账户设置</p>
      </div>

      <div class="profile-content">
        <!-- 左侧菜单 -->
        <div class="profile-sidebar">
          <div class="menu-list">
            <div
              v-for="item in menuItems"
              :key="item.key"
              :class="['menu-item', { active: activeTab === item.key }]"
              @click="activeTab = item.key"
            >
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.label }}</span>
            </div>
          </div>
        </div>

        <!-- 右侧内容 -->
        <div class="profile-main">
          <!-- 基本信息 -->
          <div v-if="activeTab === 'basic'" class="profile-section">
            <h3 class="section-title">基本信息</h3>

            <div class="user-info-card">
              <div class="avatar-section">
                <el-avatar :src="userInfo.avatar" size="80" class="user-avatar">
                  {{ userInfo.nickname?.[0] || '用' }}
                </el-avatar>
                <div class="avatar-actions">
                  <el-button type="primary" size="small">更换头像</el-button>
                </div>
              </div>

              <div class="info-section">
                <div class="info-row">
                  <label>用户名：</label>
                  <span>{{ userInfo.nickname }}</span>
                </div>
                <div class="info-row">
                  <label>邮箱：</label>
                  <span>{{ userInfo.userAccount }}</span>
                </div>
                <div class="info-row">
                  <label>注册时间：</label>
                  <span>{{ formatDate(userInfo.createTime) }}</span>
                </div>
                <div class="info-row">
                  <label>账户状态：</label>
                  <el-tag :type="userInfo.userStatus === 1 ? 'success' : 'danger'">
                    {{ userInfo.userStatus === 1 ? '正常' : '禁用' }}
                  </el-tag>
                </div>
              </div>
            </div>
          </div>

          <!-- 修改密码 -->
          <div v-if="activeTab === 'password'" class="profile-section">
            <h3 class="section-title">修改密码</h3>

            <el-form
              ref="passwordFormRef"
              :model="passwordForm"
              :rules="passwordRules"
              label-width="100px"
              class="password-form"
            >
              <el-form-item label="当前密码" prop="oldPassword">
                <el-input
                  v-model="passwordForm.oldPassword"
                  type="password"
                  placeholder="请输入当前密码"
                  show-password
                />
              </el-form-item>

              <el-form-item label="新密码" prop="newPassword">
                <el-input
                  v-model="passwordForm.newPassword"
                  type="password"
                  placeholder="请输入新密码"
                  show-password
                />
              </el-form-item>

              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入新密码"
                  show-password
                />
              </el-form-item>

              <el-form-item>
                <el-button type="primary" :loading="updating" @click="updatePassword">
                  更新密码
                </el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 账户安全 -->
          <div v-if="activeTab === 'security'" class="profile-section">
            <h3 class="section-title">账户安全</h3>

            <div class="security-items">
              <div class="security-item">
                <div class="item-info">
                  <h4>登录密码</h4>
                  <p>用于登录账户的密码</p>
                </div>
                <el-button type="outline" @click="activeTab = 'password'">
                  修改
                </el-button>
              </div>

              <div class="security-item">
                <div class="item-info">
                  <h4>绑定邮箱</h4>
                  <p>{{ userInfo.userAccount }}</p>
                </div>
                <el-tag type="success">已绑定</el-tag>
              </div>

              <div class="security-item">
                <div class="item-info">
                  <h4>实名认证</h4>
                  <p>用于景区入园身份验证</p>
                </div>
                <el-tag type="warning">未认证</el-tag>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Lock, ShieldCheck } from '@element-plus/icons-vue'

// 菜单项
const menuItems = [
  { key: 'basic', label: '基本信息', icon: User },
  { key: 'password', label: '修改密码', icon: Lock },
  { key: 'security', label: '账户安全', icon: ShieldCheck }
]

// 活动标签
const activeTab = ref('basic')

// 用户信息
const userInfo = computed(() => {
  const user = localStorage.getItem('user')
  return user ? JSON.parse(user) : {}
})

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: any) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 表单引用
const passwordFormRef = ref()

// 状态
const updating = ref(false)

// 更新密码
const updatePassword = async () => {
  try {
    await passwordFormRef.value?.validate()

    updating.value = true

    // TODO: 调用更新密码API
    // const response = await updateUserPassword(passwordForm)

    // 模拟更新成功
    setTimeout(() => {
      ElMessage.success('密码更新成功')
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
      updating.value = false
    }, 1000)

  } catch (error) {
    updating.value = false
  }
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.user-profile-page {
  padding: 40px 0;
  background: #f8f9fa;
}

.container {
  max-width: 1000px;
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

/* 个人中心内容 */
.profile-content {
  display: grid;
  grid-template-columns: 250px 1fr;
  gap: 40px;
}

/* 侧边栏菜单 */
.profile-sidebar {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  height: fit-content;
}

.menu-list {
  padding: 20px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 24px;
  cursor: pointer;
  transition: all 0.3s;
  border-left: 3px solid transparent;
}

.menu-item:hover {
  background: #f5f5f5;
}

.menu-item.active {
  background: #e6f7ff;
  border-left-color: #1890ff;
  color: #1890ff;
}

/* 主要内容 */
.profile-main {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  min-height: 500px;
}

/* 内容区块 */
.profile-section {
  padding: 32px;
}

.section-title {
  font-size: 1.3rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

/* 用户信息卡片 */
.user-info-card {
  display: flex;
  gap: 32px;
  align-items: flex-start;
}

.avatar-section {
  text-align: center;
}

.user-avatar {
  margin-bottom: 16px;
}

.info-section {
  flex: 1;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
}

.info-row:last-child {
  border-bottom: none;
}

.info-row label {
  font-weight: 500;
  color: #666;
  width: 100px;
  flex-shrink: 0;
}

.info-row span {
  color: #333;
}

/* 密码表单 */
.password-form {
  max-width: 500px;
}

/* 安全设置 */
.security-items {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  transition: border-color 0.3s;
}

.security-item:hover {
  border-color: #1890ff;
}

.item-info h4 {
  margin: 0 0 4px 0;
  font-size: 1rem;
  font-weight: 600;
  color: #333;
}

.item-info p {
  margin: 0;
  color: #666;
  font-size: 0.9rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-profile-page {
    padding: 20px 0;
  }

  .container {
    padding: 0 15px;
  }

  .page-title {
    font-size: 2rem;
  }

  .profile-content {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .user-info-card {
    flex-direction: column;
    text-align: center;
    gap: 20px;
  }

  .info-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }

  .security-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .security-item .el-tag {
    align-self: flex-end;
  }
}
</style>
