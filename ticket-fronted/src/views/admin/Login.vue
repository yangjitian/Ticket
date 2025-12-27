<template>
  <div class="login-container">
    <div class="login-card">
      <h2 class="login-title">管理员登录</h2>
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-width="80px"
        class="login-form"
      >
        <el-form-item label="邮箱" prop="userAccount">
          <el-input
            v-model="loginForm.userAccount"
            placeholder="请输入管理员邮箱"
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '@/admin/api'

const router = useRouter()

// 表单引用
const loginFormRef = ref()

// 登录表单数据
const loginForm = reactive({
  userAccount: '',
  password: ''
})

// 表单验证规则
const loginRules = {
  userAccount: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

// 加载状态
const loading = ref(false)

// 登录处理
const handleLogin = async () => {
  try {
    await loginFormRef.value?.validate()

    loading.value = true

    const response = await login(loginForm.userAccount, loginForm.password)

    if (response.data.code === 200) {
      const userLoginVO = response.data.data

      // 保存token和用户信息到localStorage
      localStorage.setItem('token', userLoginVO.token)
      localStorage.setItem('userRole', userLoginVO.role.toString())
      localStorage.setItem('user', JSON.stringify(userLoginVO))

      ElMessage.success('登录成功')

      // 跳转到管理首页
      router.push('/admin/dashboard')
    } else {
      console.log('响应code不为0，显示错误消息:', response.data.message)
      ElMessage.error(response.data.message || '登录失败')
    }
  } catch (error: any) {
    console.error('登录失败:', error)
    console.error('错误详情:', error.response?.data)
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
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.login-title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-weight: 500;
}

.login-form {
  max-width: 100%;
}

.login-btn {
  width: 100%;
  height: 45px;
  font-size: 16px;
}
</style>
