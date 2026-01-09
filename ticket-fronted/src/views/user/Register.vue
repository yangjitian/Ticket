<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-card">
        <!-- Logo和标题 -->
        <div class="register-header">
          <h1 class="register-title">旅游分销平台</h1>
          <p class="register-subtitle">创建账号，开启您的旅程</p>
        </div>

        <!-- 注册表单 -->
        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          label-position="top"
          class="register-form"
        >
          <el-form-item label="邮箱账号" prop="userAccount">
            <el-input
              v-model="registerForm.userAccount"
              placeholder="请输入邮箱账号"
              size="large"
              :prefix-icon="Message"
            />
          </el-form-item>

          <el-form-item label="验证码" prop="verificationCode">
            <div class="verification-row">
              <el-input
                v-model="registerForm.verificationCode"
                placeholder="请输入验证码"
                size="large"
                style="flex: 1"
              />
              <el-button
                type="primary"
                size="large"
                :loading="codeLoading"
                :disabled="codeCountdown > 0"
                @click="sendVerificationCode"
                style="margin-left: 10px; min-width: 120px"
              >
                {{ codeCountdown > 0 ? `${codeCountdown}s` : '发送验证码' }}
              </el-button>
            </div>
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码（至少6位）"
              size="large"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item label="确认密码" prop="checkPassword">
            <el-input
              v-model="registerForm.checkPassword"
              type="password"
              placeholder="请再次输入密码"
              size="large"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item label="用户类型" prop="role">
            <el-radio-group v-model="registerForm.role">
              <el-radio :value="1">普通用户</el-radio>
              <el-radio :value="2">经销商</el-radio>
            </el-radio-group>
            <div class="role-tip">
              <el-alert
                v-if="registerForm.role === 2"
                title="经销商注册说明"
                description="经销商注册后需要管理员审核，审核通过后可享受分销佣金功能。"
                type="info"
                :closable="false"
                show-icon
              />
            </div>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              @click="handleRegister"
              style="width: 100%"
            >
              注册账号
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 登录链接 -->
        <div class="register-footer">
          <p>已有账号？
            <router-link to="/login" class="login-link">立即登录</router-link>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Message, Lock } from '@element-plus/icons-vue'
import { userRegister, sendEmailCode } from '@/user/api'

const router = useRouter()

// 表单数据
const registerForm = reactive({
  userAccount: '',
  verificationCode: '',
  password: '',
  checkPassword: '',
  role: 1
})

// 表单验证规则
const registerRules = {
  userAccount: [
    { required: true, message: '请输入邮箱账号', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度为6位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  checkPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: any) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  role: [
    { required: true, message: '请选择用户类型', trigger: 'change' }
  ]
}

// 表单引用
const registerFormRef = ref()

// 加载状态
const loading = ref(false)
const codeLoading = ref(false)
const codeCountdown = ref(0)

// 发送验证码
const sendVerificationCode = async () => {
  try {
    // 验证邮箱格式
    if (!registerForm.userAccount) {
      ElMessage.error('请先输入邮箱账号')
      return
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(registerForm.userAccount)) {
      ElMessage.error('请输入正确的邮箱格式')
      return
    }

    codeLoading.value = true

    const response = await sendEmailCode(registerForm.userAccount)

    if (response.data.code === 200) {
      ElMessage.success('验证码已发送到您的邮箱')
      // 开始倒计时
      codeCountdown.value = 60
      const timer = setInterval(() => {
        codeCountdown.value--
        if (codeCountdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    } else {
      ElMessage.error(response.data.message || '发送验证码失败')
    }
  } catch (error: any) {
    console.error('发送验证码失败:', error)
    ElMessage.error(error.response?.data?.message || '发送验证码失败')
  } finally {
    codeLoading.value = false
  }
}

// 处理注册
const handleRegister = async () => {
  try {
    await registerFormRef.value?.validate()

    loading.value = true

    const response = await userRegister(registerForm)

    if (response.data.code === 200) {
      if (registerForm.role === 2) {
        // 经销商注册成功
        await ElMessageBox.alert(
          '经销商注册申请已提交，请等待管理员审核。审核结果将通过邮箱通知您。',
          '注册成功',
          {
            confirmButtonText: '知道了',
            type: 'success'
          }
        )
        router.push('/login')
      } else {
        // 普通用户注册成功
        ElMessage.success('注册成功，请登录')
        router.push('/login')
      }
    } else {
      ElMessage.error(response.data.message || '注册失败')
    }
  } catch (error: any) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('注册失败，请重试')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-container {
  width: 100%;
  max-width: 450px;
}

.register-card {
  background: white;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

/* 注册头部 */
.register-header {
  text-align: center;
  margin-bottom: 32px;
}

.register-title {
  font-size: 2rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.register-subtitle {
  color: #666;
  margin: 0;
  font-size: 0.9rem;
}

/* 注册表单 */
.register-form {
  margin-bottom: 24px;
}

.register-form .el-form-item {
  margin-bottom: 20px;
}

.register-form .el-form-item__label {
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

/* 验证码行 */
.verification-row {
  display: flex;
  align-items: center;
}

/* 用户类型提示 */
.role-tip {
  margin-top: 12px;
}

.role-tip .el-alert {
  padding: 12px;
}

.role-tip .el-alert__description {
  margin-top: 8px;
  color: #666;
  line-height: 1.5;
}

/* 注册底部 */
.register-footer {
  text-align: center;
  margin-bottom: 20px;
}

.register-footer p {
  color: #666;
  margin: 0;
  font-size: 0.9rem;
}

.login-link {
  color: #1890ff;
  text-decoration: none;
  font-weight: 500;
}

.login-link:hover {
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
  .register-page {
    padding: 15px;
  }

  .register-card {
    padding: 30px 20px;
  }

  .register-title {
    font-size: 1.8rem;
  }

  .verification-row {
    flex-direction: column;
    gap: 10px;
  }

  .verification-row .el-button {
    margin-left: 0;
    width: 100%;
  }
}
</style>
