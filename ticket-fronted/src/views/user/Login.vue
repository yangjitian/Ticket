<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-card">
        <!-- Logo和标题 -->
        <div class="login-header">
          <h1 class="login-title">旅游分销平台</h1>
          <p class="login-subtitle">登录您的账号，开启精彩旅程</p>
        </div>

        <!-- 登录表单 -->
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          label-position="top"
          class="login-form"
        >
          <el-form-item label="邮箱账号" prop="userAccount">
            <el-input
              v-model="loginForm.userAccount"
              placeholder="请输入邮箱账号"
              size="large"
              :prefix-icon="Message"
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              @click="handleLogin"
              style="width: 100%"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 注册链接 -->
        <div class="login-footer">
          <p>还没有账号？
            <router-link to="/register" class="register-link">立即注册</router-link>
          </p>
        </div>

        <!-- 返回首页 -->
        <div class="back-home">
          <router-link to="/" class="back-link">
            🏠 返回首页
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Message, Lock } from '@element-plus/icons-vue'
import { userLogin } from '@/user/api'

const router = useRouter()

// 表单数据
const loginForm = reactive({
  userAccount: '',
  password: ''
})

// 表单验证规则
const loginRules = {
  userAccount: [
    { required: true, message: '请输入邮箱账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

// 表单引用
const loginFormRef = ref()

// 加载状态
const loading = ref(false)

// 处理登录
const handleLogin = async () => {
  try {
    await loginFormRef.value?.validate()

    loading.value = true

    const response = await userLogin(loginForm.userAccount, loginForm.password)

    if (response.data.code === 200) {
      const userData = response.data.data // UserLoginVO对象

      // 保存用户登录信息到localStorage (使用user_前缀)
      localStorage.setItem('user_token', userData.token)
      // 移除token后保存用户信息
      const userInfo = { ...userData }
      delete userInfo.token
      localStorage.setItem('user_user', JSON.stringify(userInfo))
      localStorage.setItem('user_userRole', userData.role.toString())

      ElMessage.success('登录成功')

      // 检查是否是经销商且审核状态为待审核
      if (userInfo.role === 2 && userInfo.auditStatus === 0) {
        ElMessage.info('您的经销商申请正在审核中，请耐心等待')
      }

      // 跳转到首页
      router.push('/')
    } else {
      ElMessage.error(response.data.message || '登录失败')
    }
  } catch (error: any) {
    console.error('登录异常:', error)
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('登录失败，请重试')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-container {
  width: 100%;
  max-width: 400px;
}

.login-card {
  background: white;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

/* 登录头部 */
.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-title {
  font-size: 2rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.login-subtitle {
  color: #666;
  margin: 0;
  font-size: 0.9rem;
}

/* 登录表单 */
.login-form {
  margin-bottom: 24px;
}

.login-form .el-form-item {
  margin-bottom: 20px;
}

.login-form .el-form-item__label {
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

/* 登录底部 */
.login-footer {
  text-align: center;
  margin-bottom: 20px;
}

.login-footer p {
  color: #666;
  margin: 0;
  font-size: 0.9rem;
}

.register-link {
  color: #1890ff;
  text-decoration: none;
  font-weight: 500;
}

.register-link:hover {
  text-decoration: underline;
}

/* 返回首页 */
.back-home {
  text-align: center;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #666;
  text-decoration: none;
  font-size: 0.9rem;
  transition: color 0.3s;
}

.back-link:hover {
  color: #1890ff;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-page {
    padding: 15px;
  }

  .login-card {
    padding: 30px 20px;
  }

  .login-title {
    font-size: 1.8rem;
  }
}
</style>
