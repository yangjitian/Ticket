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
            <div class="section-header">
            <h3 class="section-title">基本信息</h3>
              <div v-if="!isEditingBasicInfo" class="section-actions">
                <el-button type="primary" @click="startEditBasicInfo">
                  <el-icon><Edit /></el-icon>
                  编辑信息
                </el-button>
              </div>
            </div>

            <div v-if="loadingUserInfo" class="loading-section">
              <el-icon class="is-loading">
                <Loading />
              </el-icon>
              <span>加载中...</span>
            </div>

            <!-- 编辑模式 -->
            <div v-else-if="isEditingBasicInfo" class="edit-form-card">
              <el-form
                ref="basicInfoFormRef"
                :model="basicInfoForm"
                :rules="basicInfoRules"
                label-width="100px"
              >
                <el-form-item label="用户名" prop="nickname">
                  <el-input
                    v-model="basicInfoForm.nickname"
                    placeholder="请输入用户名"
                    maxlength="20"
                    show-word-limit
                  />
                </el-form-item>

                <el-form-item label="头像">
                  <div class="avatar-upload-section">
                    <el-avatar :src="basicInfoForm.avatar" size="140" class="user-avatar">
                      {{ basicInfoForm.nickname?.[0] || '用' }}
                    </el-avatar>
                    <div class="upload-actions">
                      <el-upload
                        ref="avatarUploadRef"
                        :show-file-list="false"
                        :http-request="handleAvatarUpload"
                        :before-upload="beforeAvatarUpload"
                      >
                        <el-button type="primary" size="small" :loading="uploadingAvatar">选择图片</el-button>
                      </el-upload>
                      <p class="upload-tip">支持 JPG、PNG 格式，大小不超过 4MB</p>
                    </div>
                  </div>
                </el-form-item>

                <el-form-item>
                  <el-button @click="cancelEditBasicInfo">取消</el-button>
                  <el-button type="primary" :loading="updatingBasicInfo" @click="saveBasicInfo">
                    保存
                  </el-button>
                </el-form-item>
              </el-form>
            </div>

            <!-- 查看模式 -->
            <div v-else class="user-info-card">
              <div class="avatar-section">
                <el-avatar :src="userInfo.avatar" size="140" class="user-avatar">
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
                  <label>账户状态：</label>
                  <el-tag :type="userInfo.userStatus === 1 ? 'success' : 'danger'">
                    {{ userInfo.userStatus === 1 ? '正常' : '禁用' }}
                  </el-tag>
                </div>
              </div>
            </div>
          </div>

          <!-- 游客信息管理 -->
          <div v-if="activeTab === 'visitors'" class="profile-section">
            <div class="section-header">
              <h3 class="section-title">游客信息管理</h3>
              <el-button type="primary" @click="showAddVisitorDialog">
                <el-icon><Plus /></el-icon>
                添加游客
              </el-button>
            </div>

            <div class="visitors-list">
              <div
                v-for="visitor in visitors"
                :key="visitor.id"
                class="visitor-card"
                :class="{ 'default-visitor': visitor.isDefault === 1 }"
              >
                <div class="visitor-info">
                  <div class="visitor-name">
                    {{ visitor.visitorName }}
                    <el-tag v-if="visitor.isDefault === 1" size="small" type="success">默认</el-tag>
                  </div>
                  <div class="visitor-details">
                    <div class="detail-item">
                      <span class="label">身份证：</span>
                      <span class="value">{{ maskIdCard(visitor.idCard) }}</span>
                    </div>
                    <div class="detail-item">
                      <span class="label">票种：</span>
                      <span class="value">{{ visitor.ticketTypeName }}</span>
                    </div>
                    <div v-if="visitor.contactTel" class="detail-item">
                      <span class="label">电话：</span>
                      <span class="value">{{ visitor.contactTel }}</span>
                    </div>
                  </div>
                </div>

                <div class="visitor-actions">
                  <el-button
                    v-if="visitor.isDefault === 0"
                    size="small"
                    type="text"
                    @click="setDefaultVisitorHandler(visitor.id)"
                  >
                    设为默认
                  </el-button>
                  <el-button size="small" type="text" @click="showEditVisitorDialog(visitor)">
                    编辑
                  </el-button>
                  <el-button size="small" type="text" danger @click="deleteVisitor(visitor)">
                    删除
                  </el-button>
                </div>
              </div>

              <div v-if="visitors.length === 0" class="no-visitors">
                <el-empty description="暂无游客信息">
                  <el-button type="primary" @click="showAddVisitorDialog">添加第一个游客</el-button>
                </el-empty>
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

          <!-- 邮箱换绑 -->
          <div v-if="activeTab === 'email'" class="profile-section">
            <h3 class="section-title">邮箱换绑</h3>

            <div class="email-change-card">
              <div class="current-email">
                <h4>当前邮箱</h4>
                <p>{{ userInfo.userAccount }}</p>
              </div>

              <el-form
                ref="emailFormRef"
                :model="emailForm"
                :rules="emailRules"
                label-width="120px"
                class="email-form"
              >
                <el-form-item label="新邮箱地址" prop="newEmail">
                  <el-input
                    v-model="emailForm.newEmail"
                    placeholder="请输入新邮箱地址"
                    :disabled="countdown > 0"
                  />
                </el-form-item>

                <el-form-item label="验证码" prop="verificationCode">
                  <div class="code-input-row">
                    <el-input
                      v-model="emailForm.verificationCode"
                      placeholder="请输入6位验证码"
                      maxlength="6"
                    />
                    <el-button
                      type="primary"
                      :loading="sendingCode"
                      :disabled="countdown > 0 || !emailForm.newEmail"
                      @click="sendEmailVerificationCode"
                    >
                      {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
                    </el-button>
                  </div>
                </el-form-item>

                <el-form-item>
                  <el-button :loading="updatingEmail" type="primary" @click="changeEmail">
                    确认换绑
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
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

      <!-- 游客信息对话框 -->
      <el-dialog
        v-model="visitorDialogVisible"
        :title="visitorDialogType === 'add' ? '添加游客信息' : '编辑游客信息'"
        width="600px"
        :close-on-click-modal="false"
      >
        <el-form
          ref="visitorFormRef"
          :model="visitorForm"
          :rules="visitorRules"
          label-width="100px"
        >
          <el-form-item label="游客姓名" prop="visitorName">
            <el-input
              v-model="visitorForm.visitorName"
              placeholder="请输入游客姓名"
              maxlength="32"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="身份证号" prop="idCard">
            <el-input
              v-model="visitorForm.idCard"
              placeholder="请输入身份证号"
              maxlength="18"
            />
          </el-form-item>

          <el-form-item label="联系电话">
            <el-input
              v-model="visitorForm.contactTel"
              placeholder="请输入联系电话（可选）"
              maxlength="20"
            />
          </el-form-item>

          <el-form-item label="默认票种" prop="defaultTicketType">
            <el-select
              v-model="visitorForm.defaultTicketType"
              placeholder="请选择默认票种"
              style="width: 100%"
            >
              <el-option
                v-for="option in ticketTypeOptions"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
            <div class="ticket-type-hint">
              <el-icon size="14" color="#909399"><InfoFilled /></el-icon>
              <el-text size="small" type="info">
                票种规则请见景区详情页的购票须知
              </el-text>
            </div>
          </el-form-item>

          <el-form-item label="设为默认">
            <el-checkbox v-model="visitorForm.isDefault">
              设为默认出行人（每次购票时会优先选择）
            </el-checkbox>
          </el-form-item>
        </el-form>

        <template #footer>
          <span class="dialog-footer">
            <el-button @click="visitorDialogVisible = false">取消</el-button>
            <el-button
              type="primary"
              :loading="visitorLoading"
              @click="saveVisitor"
            >
              {{ visitorDialogType === 'add' ? '添加' : '保存' }}
            </el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Lock, Setting, UserFilled, Plus, Loading, Edit, InfoFilled } from '@element-plus/icons-vue'
import {
  getUserVisitors,
  addUserVisitor,
  updateUserVisitor,
  deleteUserVisitor,
  setDefaultVisitor,
  getCurrentUser,
  sendChangeEmailCode,
  changeUserEmail,
  updateUserAvatar
} from '@/user/api'
import type { UserVisitor, UserVisitorAddRequest, UserVisitorUpdateRequest } from '@/types/user'

const router = useRouter()
const route = useRoute()

// 用户信息类型定义
interface UserInfo {
  id?: string
  nickname?: string
  userAccount?: string
  avatar?: string
  role?: number
  userStatus?: number
  auditStatus?: number
  createTime?: string
  updateTime?: string
}

// 菜单项
const menuItems = [
  { key: 'basic', label: '基本信息', icon: User },
  { key: 'visitors', label: '游客信息', icon: 'UserFilled' },
  { key: 'password', label: '修改密码', icon: Lock },
  { key: 'email', label: '邮箱换绑', icon: 'Message' },
  { key: 'security', label: '账户安全', icon: Setting }
]

// 活动标签
const activeTab = ref('basic')

// 表单引用
const basicInfoFormRef = ref()
const visitorFormRef = ref()
const emailFormRef = ref()
const avatarUploadRef = ref()

// 用户信息
const userInfo = ref<UserInfo>({})
const loadingUserInfo = ref(false)

// 编辑状态
const isEditingBasicInfo = ref(false)
const updatingBasicInfo = ref(false)
const uploadingAvatar = ref(false)

// 基本信息表单
const basicInfoForm = reactive({
  nickname: '',
  avatar: ''
})

// 基本信息表单验证规则
const basicInfoRules = {
  nickname: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在2到20个字符', trigger: 'blur' }
  ]
}

// 检查用户是否已登录
const checkLoginStatus = () => {
  const token = localStorage.getItem('user_token')
  if (!token) {
    // 未登录，跳转到登录页
    router.push('/login')
    return false
  }
  return true
}

// 获取用户信息
const fetchUserInfo = async () => {
  // 先检查登录状态
  if (!checkLoginStatus()) {
    return
  }

  try {
    loadingUserInfo.value = true
    const response = await getCurrentUser()
    if (response.data.code === 200) {
      userInfo.value = response.data.data
    } else {
      throw new Error('获取用户信息失败')
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('登录已失效，请重新登录')
    // 跳转到登录页
    router.push('/login')
  } finally {
    loadingUserInfo.value = false
  }
}

// 开始编辑基本信息
const startEditBasicInfo = () => {
  Object.assign(basicInfoForm, {
    nickname: userInfo.value.nickname || '',
    avatar: userInfo.value.avatar || ''
  })
  isEditingBasicInfo.value = true
}

// 取消编辑基本信息
const cancelEditBasicInfo = () => {
  isEditingBasicInfo.value = false
}

// 保存基本信息
const saveBasicInfo = async () => {
  try {
    updatingBasicInfo.value = true
    // TODO: 调用更新基本信息API
    // const response = await updateUserInfo(basicInfoForm)

    // 模拟更新成功
    setTimeout(async () => {
      userInfo.value.nickname = basicInfoForm.nickname
      userInfo.value.avatar = basicInfoForm.avatar
      isEditingBasicInfo.value = false

      // 刷新用户信息确保数据同步
      await fetchUserInfo()

      ElMessage.success('基本信息更新成功')
      updatingBasicInfo.value = false
    }, 1000)

  } catch (error) {
    updatingBasicInfo.value = false
    ElMessage.error('更新失败，请重试')
  }
}

// 头像上传处理
const handleAvatarUpload = async (options: any) => {
  const { file, onSuccess, onError } = options

  try {
    uploadingAvatar.value = true
    console.log('开始上传头像文件:', file.name)

    const response = await updateUserAvatar(file)
    console.log('头像上传响应:', response.data)

    if (response.data.code === 200) {
      const avatarUrl = response.data.data
      console.log('头像上传成功，URL:', avatarUrl)

    // 更新表单数据
      basicInfoForm.avatar = avatarUrl

    // 刷新用户信息以显示最新头像
    await fetchUserInfo()
    console.log('用户信息已刷新，当前头像URL:', userInfo.value.avatar)

    ElMessage.success('头像上传成功')

      // 调用onSuccess让Element Plus知道上传成功
      onSuccess({ code: 200, data: avatarUrl }, file)
  } else {
      console.log('头像上传失败:', response.data.message)
      const error = new Error(response.data.message || '头像上传失败')
      onError(error)
    }
  } catch (error: any) {
    console.error('头像上传异常:', error)
    const errorMessage = error.response?.data?.message || '头像上传失败，请重试'
    ElMessage.error(errorMessage)
    onError(error)
  } finally {
    uploadingAvatar.value = false
  }
}

// 头像上传前验证
const beforeAvatarUpload = (file: File) => {
  const isValidType = ['image/jpeg', 'image/jpg', 'image/png'].includes(file.type)
  const isValidSize = file.size / 1024 / 1024 < 4

  if (!isValidType) {
    ElMessage.error('只支持 JPG、PNG 格式的图片')
    return false
  }
  if (!isValidSize) {
    ElMessage.error('图片大小不能超过 4MB')
    return false
  }
  return true
}

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

// 邮箱换绑状态
const emailForm = reactive({
  newEmail: '',
  verificationCode: ''
})

const emailRules = {
  newEmail: [
    { required: true, message: '请输入新邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

const updatingEmail = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)

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

// 游客信息管理
const visitors = ref<UserVisitor[]>([])
const visitorDialogVisible = ref(false)
const visitorDialogType = ref<'add' | 'edit'>('add')
const currentVisitor = ref<UserVisitor | null>(null)
const visitorLoading = ref(false)

// 游客表单（内部使用布尔值，提交时转换为数字）
const visitorForm = reactive<{
  visitorName: string
  idCard: string
  contactTel: string
  defaultTicketType: number
  isDefault: boolean
}>({
  visitorName: '',
  idCard: '',
  contactTel: '',
  defaultTicketType: 1,
  isDefault: false
})

// 游客表单验证规则
const visitorRules = {
  visitorName: [
    { required: true, message: '请输入游客姓名', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' }
  ],
  defaultTicketType: [
    { required: true, message: '请选择默认票种', trigger: 'change' }
  ]
}

// 票种选项
const ticketTypeOptions = [
  { label: '原价票', value: 1 },
  { label: '半价票', value: 2 },
  { label: '免票', value: 3 }
]

// 获取游客信息列表
const fetchVisitors = async () => {
  try {
    const response = await getUserVisitors()
    if (response.data.code === 200) {
      visitors.value = response.data.data || []
    }
  } catch (error) {
    console.error('获取游客信息失败:', error)
  }
}

// 显示添加游客对话框
const showAddVisitorDialog = () => {
  visitorDialogType.value = 'add'
  visitorDialogVisible.value = true
  Object.assign(visitorForm, {
    visitorName: '',
    idCard: '',
    contactTel: '',
    defaultTicketType: 1,  // 默认选择原价票
    isDefault: false  // 使用布尔值用于checkbox
  })
}

// 显示编辑游客对话框
const showEditVisitorDialog = (visitor: UserVisitor) => {
  visitorDialogType.value = 'edit'
  visitorDialogVisible.value = true
  currentVisitor.value = visitor
  Object.assign(visitorForm, {
    visitorName: visitor.visitorName,
    idCard: visitor.idCard,
    contactTel: visitor.contactTel || '',
    defaultTicketType: visitor.defaultTicketType,
    isDefault: visitor.isDefault === 1  // 将数字转换为布尔值用于checkbox
  })
}

// 保存游客信息
const saveVisitor = async () => {
  try {
    visitorLoading.value = true

    // 准备提交数据，将布尔值转换为数字
    const submitData = {
      ...visitorForm,
      isDefault: visitorForm.isDefault ? 1 : 0
    }

    if (visitorDialogType.value === 'add') {
      const response = await addUserVisitor(submitData)
      if (response.data.code === 200) {
        ElMessage.success('添加游客信息成功')
        fetchVisitors()
        visitorDialogVisible.value = false
      }
    } else {
      const updateData: UserVisitorUpdateRequest = {
        id: currentVisitor.value!.id,
        ...submitData
      }
      const response = await updateUserVisitor(updateData)
      if (response.data.code === 200) {
        ElMessage.success('更新游客信息成功')
        fetchVisitors()
        visitorDialogVisible.value = false
      }
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    visitorLoading.value = false
  }
}

// 删除游客信息
const deleteVisitor = async (visitor: UserVisitor) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除游客"${visitor.visitorName}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await deleteUserVisitor(visitor.id)
    if (response.data.code === 200) {
      ElMessage.success('删除游客信息成功')
      fetchVisitors()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 设置默认游客
const setDefaultVisitorHandler = async (visitorId: string) => {
  try {
    const response = await setDefaultVisitor(visitorId)
    if (response.data.code === 200) {
      ElMessage.success('设置默认游客成功')
      fetchVisitors()
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '设置失败')
  }
}

// 身份证号脱敏显示
const maskIdCard = (idCard: string) => {
  if (!idCard || idCard.length < 8) return idCard
  return idCard.substring(0, 4) + '****' + idCard.substring(idCard.length - 4)
}

// 页面加载时获取用户信息和游客信息
onMounted(async () => {
  await fetchUserInfo()

  // 检查URL参数，切换到对应标签
  const tab = route.query.tab as string
  if (tab && ['basic', 'visitors', 'password', 'email', 'security'].includes(tab)) {
    activeTab.value = tab
  }

  if (activeTab.value === 'visitors') {
    fetchVisitors()
  }
})

// 监听标签变化
watch(activeTab, (newTab) => {
  if (newTab === 'visitors') {
    fetchVisitors()
  }
})

// 发送邮箱验证码
const sendEmailVerificationCode = async () => {
  console.log('📧 发送邮箱换绑验证码函数被调用')
  console.log('📧 新邮箱地址:', emailForm.newEmail)

  const email = emailForm.newEmail?.trim()
  if (!email) {
    console.log('❌ 邮箱为空')
    ElMessage.error('请先输入新邮箱地址')
    return
  }

  console.log('✅ 邮箱验证通过，开始发送请求...')

  try {
    sendingCode.value = true
    console.log('🚀 发送请求到 /email/send-changeCode')
    const response = await sendChangeEmailCode(email)
    console.log('📨 后端响应:', response)

    if (response.data.code === 200) {
      console.log('✅ 验证码发送成功')
      ElMessage.success('验证码发送成功')
      countdown.value = 60
      const timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    } else {
      console.log('❌ 后端返回错误码:', response.data.code)
      ElMessage.error(response.data.message || '验证码发送失败')
    }
  } catch (error: any) {
    console.error('💥 发送验证码出现异常:', error)
    console.error('💥 错误详情:', error.response?.data)
    ElMessage.error(error.response?.data?.message || '验证码发送失败，请检查网络连接')
  } finally {
    sendingCode.value = false
  }
}

// 换绑邮箱
const changeEmail = async () => {
  try {
    await emailFormRef.value?.validate()
    updatingEmail.value = true

    const response = await changeUserEmail({
      newEmail: emailForm.newEmail,
      verificationCode: emailForm.verificationCode
    })

    if (response.data.code === 200) {
      // 更新本地用户信息
      userInfo.value.userAccount = emailForm.newEmail
      emailForm.newEmail = ''
      emailForm.verificationCode = ''
      // 换绑成功后取消验证码发送限时
      countdown.value = 0
      ElMessage.success('邮箱换绑成功')
    } else {
      ElMessage.error(response.data.message || '邮箱换绑失败')
    }

  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '换绑失败，请重试')
  } finally {
    updatingEmail.value = false
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
  padding: 50px 0 60px;
  background: #f8f9fa;
  min-height: calc(100vh - 60px);
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 32px;
}

/* 页面标题 */
.page-header {
  text-align: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 3.2rem;
  font-weight: 700;
  color: #1d2129;
  margin-bottom: 1.2rem;
  letter-spacing: -0.02em;
}

.page-subtitle {
  font-size: 1.1rem;
  color: #86909c;
  margin: 0;
  font-weight: 400;
}

/* 个人中心内容 */
.profile-content {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 50px;
  align-items: start;
  margin-top: -10px; /* 向上移动信息展示框 */
  margin-left: -20px; /* 向左移动侧边栏 */
}

/* 侧边栏菜单 */
.profile-sidebar {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  height: fit-content;
  border: 1px solid #f0f0f0;
  overflow: hidden;
  margin-top: 80px; /* 向下移动侧边栏更多 */
}

.menu-list {
  padding: 20px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-left: 3px solid transparent;
  position: relative;
  font-size: 16px;
  font-weight: 500;
}

.menu-item:hover {
  background: #f8f9fa;
  transform: translateX(2px);
}

.menu-item.active {
  background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
  border-left-color: #1890ff;
  color: #1890ff;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.15);
}

/* 游客信息管理 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.visitors-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.visitor-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 36px;
  border: 1px solid #e4e7ed;
  border-radius: 16px;
  background: white;
  transition: all 0.3s;
}

.visitor-card:hover {
  border-color: #c0c4cc;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.visitor-card.default-visitor {
  border-color: #67c23a;
  background: #f0f9ff;
}

.visitor-info {
  flex: 1;
}

.visitor-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.visitor-details {
  display: flex;
  gap: 24px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.detail-item .label {
  color: #909399;
  font-size: 14px;
}

.detail-item .value {
  color: #606266;
  font-size: 14px;
}

.visitor-actions {
  display: flex;
  gap: 8px;
}

.no-visitors {
  padding: 40px 0;
}

/* 主要内容 */
.profile-main {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  min-height: 600px;
  border: 1px solid #f0f0f0;
}

/* 内容区块 */
.profile-section {
  padding: 80px;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1d2129;
  margin-bottom: 35px;
  padding-bottom: 18px;
  border-bottom: 2px solid #e5e6eb;
  position: relative;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 45px;
  height: 2px;
  background: linear-gradient(90deg, #1890ff 0%, #36cfc9 100%);
  border-radius: 1px;
}

/* 加载状态 */
.loading-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  gap: 16px;
  color: #86909c;
}

.loading-section .el-icon {
  font-size: 32px;
}

/* 编辑表单卡片 */
.edit-form-card {
  background: #fafbfc;
  border-radius: 20px;
  padding: 60px;
  border: 1px solid #e5e6eb;
}

.edit-form-card .el-form-item {
  margin-bottom: 40px;
}

.edit-form-card .el-form-item__label {
  font-weight: 600;
  font-size: 16px;
  color: #4e5969;
}

/* 头像上传区域 */
.avatar-upload-section {
  display: flex;
  align-items: center;
  gap: 32px;
}

.upload-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.upload-tip {
  font-size: 14px;
  color: #86909c;
  margin: 0;
}

/* 用户信息卡片 */
.user-info-card {
  display: flex;
  gap: 64px;
  align-items: flex-start;
  background: #fafbfc;
  border-radius: 16px;
  padding: 48px;
  border: 1px solid #e5e6eb;
}

.avatar-section {
  text-align: center;
  flex-shrink: 0;
}

.user-avatar {
  margin-bottom: 28px;
  border: 6px solid #fff;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.18);
}

.avatar-actions {
  margin-top: 16px;
}

.avatar-actions .el-button {
  border-radius: 24px;
  font-weight: 500;
  font-size: 14px;
  height: 36px;
  padding: 0 20px;
}

.info-section {
  flex: 1;
  min-width: 0;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px 28px;
  background: white;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  transition: all 0.3s ease;
}

.info-row:hover {
  border-color: #d9d9d9;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-row label {
  font-weight: 600;
  color: #4e5969;
  width: 120px;
  flex-shrink: 0;
  font-size: 15px;
}

.info-row span {
  color: #1d2129;
  font-size: 15px;
  font-weight: 500;
}

/* 密码表单 */
.password-form {
  max-width: 520px;
  background: #fafbfc;
  padding: 32px;
  border-radius: 12px;
  border: 1px solid #e5e6eb;
}

.password-form .el-form-item {
  margin-bottom: 32px;
}

.password-form .el-form-item__label {
  font-weight: 600;
  font-size: 16px;
  color: #4e5969;
  margin-bottom: 12px;
}

/* 邮箱换绑 */
.email-change-card {
  max-width: 700px;
  background: #fafbfc;
  padding: 48px;
  border-radius: 16px;
  border: 1px solid #e5e6eb;
}

.current-email {
  margin-bottom: 40px;
  padding: 28px;
  background: white;
  border-radius: 12px;
  border: 1px solid #e5e6eb;
}

.current-email h4 {
  margin: 0 0 12px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1d2129;
}

.current-email p {
  margin: 0;
  font-size: 16px;
  color: #86909c;
}

.email-form .el-form-item {
  margin-bottom: 32px;
}

.email-form .el-form-item__label {
  font-weight: 600;
  font-size: 16px;
  color: #4e5969;
}

.code-input-row {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.code-input-row .el-input {
  flex: 1;
}

.code-input-row .el-button {
  flex-shrink: 0;
  width: 140px;
  height: 40px;
}

/* 安全设置 */
.security-items {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 36px 44px;
  border: 1px solid #f0f0f0;
  border-radius: 16px;
  background: white;
  transition: all 0.3s ease;
  cursor: pointer;
}

.security-item:hover {
  border-color: #1890ff;
  box-shadow: 0 4px 16px rgba(24, 144, 255, 0.1);
  transform: translateY(-2px);
}

.item-info h4 {
  margin: 0 0 8px 0;
  font-size: 1.3rem;
  font-weight: 600;
  color: #1d2129;
}

.item-info p {
  margin: 0;
  color: #86909c;
  font-size: 1.05rem;
  line-height: 1.5;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .profile-content {
    grid-template-columns: 250px 1fr;
    gap: 28px;
  }
}

@media (max-width: 768px) {
  .user-profile-page {
    padding: 40px 0 20px;
  }

  .container {
    padding: 0 16px;
  }

  .page-title {
    font-size: 2.4rem;
  }

  .page-subtitle {
    font-size: 1.1rem;
  }

  .profile-content {
    grid-template-columns: 1fr;
    gap: 32px;
  }

  .profile-sidebar {
    order: 2;
  }

  .profile-main {
    order: 1;
  }

  .user-info-card {
    flex-direction: column;
    text-align: center;
    gap: 32px;
    padding: 24px;
  }

  .info-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
    text-align: left;
  }

  .info-row label {
    width: auto;
    font-size: 14px;
  }

  .info-row span {
    font-size: 14px;
  }

  .security-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    padding: 24px;
  }

  .security-item .el-tag {
    align-self: flex-start;
    margin-top: 8px;
  }

  .code-input-row {
    flex-direction: column;
    gap: 12px;
  }

  .code-input-row .el-button {
    width: 100%;
  }

  .email-change-card {
    padding: 20px;
  }

  .current-email {
    padding: 16px;
  }

  .profile-section {
    padding: 24px;
  }

  .password-form {
    padding: 24px;
    max-width: 100%;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 2rem;
  }

  .user-info-card {
    padding: 20px;
  }

  .profile-section {
    padding: 20px;
  }

  .password-form {
    padding: 20px;
  }

  .ticket-type-hint {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-top: 6px;
    color: #909399;
    font-size: 12px;
  }
}
</style>
