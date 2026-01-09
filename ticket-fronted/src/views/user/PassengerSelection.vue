<template>
  <div class="passenger-selection-page">
    <!-- 头部导航 -->
    <div class="header-nav">
      <div class="container">
        <div class="nav-content">
          <div class="back-btn" @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </div>
          <div class="progress">
            <div class="step active">
              <span class="step-num">1</span>
              <span class="step-text">选择票种</span>
            </div>
            <div class="step-line"></div>
            <div class="step active">
              <span class="step-num">2</span>
              <span class="step-text">选择游客</span>
            </div>
            <div class="step-line"></div>
            <div class="step">
              <span class="step-num">3</span>
              <span class="step-text">确认订单</span>
            </div>
            <div class="step-line"></div>
            <div class="step">
              <span class="step-num">4</span>
              <span class="step-text">支付</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主要内容 -->
    <div class="main-content">
      <div class="container">
        <div class="content-wrapper">

          <!-- 订单信息卡片 -->
          <div class="order-info-card">
            <div class="order-header">
              <h2>购票信息确认</h2>
            </div>
            <div class="order-details">
              <div class="detail-row">
                <span class="label">景区：</span>
                <span class="value">{{ bookTicketInfo?.spotName || '' }}</span>
              </div>
              <div class="detail-row">
                <span class="label">游玩日期：</span>
                <span class="value">{{ bookTicketInfo?.visitDate ? formatDate(bookTicketInfo.visitDate) : '' }}</span>
              </div>
              <div class="detail-row">
                <span class="label">票种：</span>
                <span class="value ticket-type">{{ bookTicketInfo?.ticketTypeName || '' }}</span>
              </div>
            </div>
          </div>

          <!-- 选择购票人 -->
          <div class="passenger-selection-card">
            <div class="card-header">
              <h3>选择购票人</h3>
              <el-button type="primary" @click="goToPassengerManagement">
                <el-icon><Plus /></el-icon>
                添加游客
              </el-button>
            </div>

            <div class="passenger-list">
              <div class="passenger-group">
                <div class="group-header">
                  <h4>{{ bookTicketInfo?.ticketTypeName || '' }}购票人</h4>
                  <span class="group-desc">选择需要购买此票种的游客</span>
                </div>

                <div class="passenger-options">
                  <div
                    v-for="visitor in filteredVisitors"
                    :key="visitor.id"
                    class="passenger-item"
                    :class="{ selected: isSelected(visitor.id) }"
                    @click="toggleVisitor(visitor.id)"
                  >
                    <div class="passenger-checkbox">
                      <el-checkbox
                        :model-value="isSelected(visitor.id)"
                        @change="toggleVisitor(visitor.id)"
                      />
                    </div>
                    <div class="passenger-info">
                      <div class="passenger-name">{{ visitor.visitorName }}</div>
                      <div class="passenger-details">
                        <span class="id-card">{{ maskIdCard(visitor.idCard) }}</span>
                        <span v-if="visitor.contactTel" class="contact-tel">{{ visitor.contactTel }}</span>
                      </div>
                      <div class="ticket-type-badge" :class="getTicketTypeClass(visitor.defaultTicketType)">
                        {{ getTicketTypeLabel(visitor.defaultTicketType) }}
                      </div>
                    </div>
                  </div>

                  <!-- 无符合条件的游客提示 -->
                  <div v-if="filteredVisitors.length === 0" class="no-visitors">
                    <el-empty description="暂无符合当前票种条件的游客">
                      <el-button type="primary" @click="goToPassengerManagement">
                        去添加游客信息
                      </el-button>
                    </el-empty>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 订单摘要 -->
          <div class="order-summary-card">
            <div class="summary-header">
              <h3>订单摘要</h3>
            </div>

            <div class="summary-content">
                <div class="summary-row">
                  <span class="label">{{ bookTicketInfo?.ticketTypeName || '' }}</span>
                  <span class="value">¥{{ bookTicketInfo?.unitPrice || 0 }} × {{ selectedCount }}</span>
                </div>
              <div class="summary-row total">
                <span class="label">总计</span>
                <span class="value">¥{{ totalAmount }}</span>
              </div>
            </div>

            <div class="action-buttons">
              <el-button size="large" @click="goBack">返回修改</el-button>
              <el-button
                type="primary"
                size="large"
                :disabled="selectedCount === 0"
                @click="confirmSelection"
              >
                确认选择 ({{ selectedCount }}人)
              </el-button>
            </div>
          </div>

        </div>
      </div>
    </div>

    <!-- 添加游客弹窗 -->
    <el-dialog
      v-model="addVisitorDialogVisible"
      title="添加新游客"
      width="500px"
      center
    >
      <el-form
        ref="visitorFormRef"
        :model="visitorForm"
        :rules="visitorFormRules"
        label-width="100px"
      >
        <el-form-item label="游客姓名" prop="visitorName">
          <el-input v-model="visitorForm.visitorName" placeholder="请输入真实姓名" />
        </el-form-item>

        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="visitorForm.idCard" placeholder="请输入18位身份证号" />
        </el-form-item>

        <el-form-item label="联系电话" prop="contactTel">
          <el-input v-model="visitorForm.contactTel" placeholder="请输入联系电话" />
        </el-form-item>

        <el-form-item label="默认票种" prop="defaultTicketType">
          <el-radio-group v-model="visitorForm.defaultTicketType">
            <el-radio :value="1">原价票</el-radio>
            <el-radio :value="2">半价票</el-radio>
            <el-radio :value="3">免票</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="addVisitorDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAddVisitor">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft,
  Plus
} from '@element-plus/icons-vue'
import { getBookTicketInfo, addUserVisitor } from '@/user/api'
import type { BookTicketVO, UserVisitorVO } from '@/types/user'

const router = useRouter()
const route = useRoute()

// 购票信息
const bookTicketInfo = ref<BookTicketVO | null>(null)

// 选中的游客ID列表
const selectedVisitorIds = ref<string[]>([])

// 添加游客弹窗
const addVisitorDialogVisible = ref(false)

// 游客表单
const visitorFormRef = ref()
const visitorForm = reactive({
  visitorName: '',
  idCard: '',
  contactTel: '',
  defaultTicketType: 1
})

// 表单验证规则
const visitorFormRules = {
  visitorName: [
    { required: true, message: '请输入游客姓名', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[1-2]\d|3[0-1])\d{3}[0-9Xx]$/,
      message: '请输入正确的18位身份证号', trigger: 'blur' }
  ],
  contactTel: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  defaultTicketType: [
    { required: true, message: '请选择默认票种', trigger: 'change' }
  ]
}

// 计算属性
const selectedCount = computed(() => selectedVisitorIds.value.length)

const totalAmount = computed(() => {
  if (!bookTicketInfo.value) return 0
  return bookTicketInfo.value.unitPrice * selectedCount.value
})

// 根据票种过滤游客列表
const filteredVisitors = computed(() => {
  if (!bookTicketInfo.value || !bookTicketInfo.value.availableVisitors) {
    return []
  }

  const ticketType = bookTicketInfo.value.ticketType

  // 根据票种过滤游客
  return bookTicketInfo.value.availableVisitors.filter(visitor => {
    // 原价票：所有游客都可以购买
    if (ticketType === 1) {
      return true
    }

    // 半价票：学生票（需要检查身份证年龄或特定标识）
    if (ticketType === 2) {
      // 检查是否为学生或其他半价人群
      // 这里可以根据身份证号判断年龄，或者根据其他字段判断
      // 暂时显示所有设置了默认半价票的游客，以及年龄在18-25岁之间的游客
      return checkCanBuyHalfPrice(visitor)
    }

    // 免票：军人、残疾人等
    if (ticketType === 3) {
      // 检查是否符合免票条件
      // 这里可以根据身份证或其他标识判断
      // 暂时显示所有设置了默认免票的游客
      return visitor.defaultTicketType === 3
    }

    return true
  })
})

// 检查游客是否可以购买半价票
const checkCanBuyHalfPrice = (visitor: any) => {
  // 如果游客默认票种是半价票，直接允许
  if (visitor.defaultTicketType === 2) {
    return true
  }

  // 根据身份证判断年龄（18-25岁视为学生）
  if (visitor.idCard && visitor.idCard.length >= 18) {
    try {
      const birthYear = parseInt(visitor.idCard.substring(6, 10))
      const currentYear = new Date().getFullYear()
      const age = currentYear - birthYear

      // 18-25岁之间，可能是学生
      if (age >= 18 && age <= 25) {
        return true
      }

      // 60岁以上老人也可以半价
      if (age >= 60) {
        return true
      }
    } catch (e) {
      // 身份证解析失败，使用默认逻辑
    }
  }

  return false
}

// 获取购票信息
const fetchBookTicketInfo = async () => {
  try {
    const query = route.query
    // 对URL编码的参数进行解码
    const spotId = decodeURIComponent(query.spotId as string)
    const ticketType = Number(decodeURIComponent(query.ticketType as string))
    const visitDate = decodeURIComponent(query.visitDate as string)

    console.log('PassengerSelection - 解码前参数:', query)
    console.log('PassengerSelection - 解码后参数:', { spotId, ticketType, visitDate })
    console.log('PassengerSelection - spotId类型:', typeof spotId, '值:', spotId)

    const response = await getBookTicketInfo(spotId, ticketType, visitDate)
    if (response.data.code === 200) {
      console.log('PassengerSelection - API返回数据:', response.data.data)
      console.log('PassengerSelection - 返回的spotId:', response.data.data.spotId, '类型:', typeof response.data.data.spotId)
      bookTicketInfo.value = response.data.data
    } else {
      ElMessage.error(response.data.message || '获取购票信息失败')
      router.back()
    }
  } catch (error) {
    console.error('获取购票信息失败:', error)
    ElMessage.error('获取购票信息失败')
    router.back()
  }
}

// 判断游客是否已选择
const isSelected = (visitorId: string) => {
  return selectedVisitorIds.value.includes(visitorId)
}

// 切换游客选择状态
const toggleVisitor = (visitorId: string) => {
  const index = selectedVisitorIds.value.indexOf(visitorId)
  if (index > -1) {
    selectedVisitorIds.value.splice(index, 1)
  } else {
    selectedVisitorIds.value.push(visitorId)
  }
}

// 确认选择
const confirmSelection = () => {
  if (selectedCount.value === 0) {
    ElMessage.warning('请至少选择一位购票人')
    return
  }

  // 跳转到订单确认页面
  const info = bookTicketInfo.value
  if (!info) return

  // 使用encodeURIComponent确保URL参数正确编码
  router.push({
    path: '/order-confirmation',
    query: {
      spotId: encodeURIComponent(String(info.spotId)), // 强制转换为字符串并编码
      ticketType: encodeURIComponent(info.ticketType.toString()),
      visitDate: encodeURIComponent(info.visitDate),
      visitorIds: encodeURIComponent(selectedVisitorIds.value.map(id => String(id)).join(',')) // 确保游客ID作为字符串传递
    }
  })
}

// 回到上一页
const goBack = () => {
  router.back()
}

// 跳转到乘客管理页面（用户资料页的游客标签）
const goToPassengerManagement = () => {
  router.push('/profile?tab=visitors')
}

// 提交添加游客
const submitAddVisitor = async () => {
  if (!visitorFormRef.value) return

  await visitorFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        const response = await addUserVisitor(visitorForm)
        if (response.data.code === 200) {
          ElMessage.success('添加游客成功')
          addVisitorDialogVisible.value = false
          // 重置表单
          Object.assign(visitorForm, {
            visitorName: '',
            idCard: '',
            contactTel: '',
            defaultTicketType: 1
          })
          // 重新获取购票信息
          await fetchBookTicketInfo()
        } else {
          ElMessage.error(response.data.message || '添加游客失败')
        }
      } catch (error) {
        console.error('添加游客失败:', error)
        ElMessage.error('添加游客失败')
      }
    }
  })
}

// 获取票种类型标签
const getTicketTypeLabel = (ticketType: number) => {
  switch (ticketType) {
    case 1: return '原价票'
    case 2: return '半价票'
    case 3: return '免票'
    default: return '未知'
  }
}

// 获取票种类型CSS类
const getTicketTypeClass = (ticketType: number) => {
  switch (ticketType) {
    case 1: return 'ticket-full'
    case 2: return 'ticket-discount'
    case 3: return 'ticket-free'
    default: return ''
  }
}

// 身份证号脱敏
const maskIdCard = (idCard: string) => {
  if (!idCard || idCard.length < 8) return idCard
  return idCard.substring(0, 4) + '****' + idCard.substring(idCard.length - 4)
}

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
}

onMounted(() => {
  fetchBookTicketInfo()
})
</script>

<style scoped>
.passenger-selection-page {
  min-height: 100vh;
  background: #f8f9fa;
}

/* 头部导航 */
.header-nav {
  background: white;
  border-bottom: 1px solid #e8e8e8;
  padding: 20px 0;
}

.nav-content {
  display: flex;
  align-items: center;
  gap: 40px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.back-btn:hover {
  background: #f5f5f5;
  color: #409eff;
}

.progress {
  display: flex;
  align-items: center;
  flex: 1;
  max-width: 600px;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.step.active .step-num {
  background: #409eff;
  color: white;
}

.step-num {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #e8e8e8;
  color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
}

.step-text {
  font-size: 12px;
  color: #666;
}

.step.active .step-text {
  color: #409eff;
  font-weight: 500;
}

.step-line {
  flex: 1;
  height: 2px;
  background: #e8e8e8;
  margin: 0 20px;
}

.step.active + .step-line {
  background: #409eff;
}

/* 主要内容 */
.main-content {
  padding: 40px 0;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 20px;
}

.content-wrapper {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 订单信息卡片 */
.order-info-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.order-header h2 {
  margin: 0 0 20px;
  color: #333;
  font-size: 20px;
  font-weight: 600;
}

.order-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-row {
  display: flex;
  align-items: center;
  font-size: 16px;
}

.detail-row .label {
  color: #666;
  min-width: 80px;
}

.detail-row .value {
  color: #333;
  font-weight: 500;
}

.ticket-type {
  color: #409eff;
  font-weight: 600;
}

/* 购票人选择卡片 */
.passenger-selection-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.card-header h3 {
  margin: 0;
  color: #333;
  font-size: 18px;
  font-weight: 600;
}

.passenger-list {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.passenger-group {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 20px;
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.group-header h4 {
  margin: 0;
  color: #333;
  font-size: 16px;
  font-weight: 600;
}

.group-desc {
  color: #999;
  font-size: 14px;
}

.passenger-options {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.passenger-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.passenger-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.passenger-item.selected {
  border-color: #409eff;
  background: #f0f8ff;
}

.passenger-checkbox {
  flex-shrink: 0;
}

.passenger-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.passenger-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.passenger-details {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: #666;
}

.ticket-type-badge {
  align-self: flex-start;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  text-align: center;
  min-width: 60px;
}

.ticket-full {
  background: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
}

.ticket-discount {
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.ticket-free {
  background: #fff7e6;
  color: #fa8c16;
  border: 1px solid #ffd591;
}

/* 订单摘要卡片 */
.order-summary-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.summary-header h3 {
  margin: 0 0 20px;
  color: #333;
  font-size: 18px;
  font-weight: 600;
}

.summary-content {
  margin-bottom: 24px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  font-size: 16px;
}

.summary-row .label {
  color: #666;
}

.summary-row .value {
  color: #333;
  font-weight: 500;
}

.summary-row.total {
  border-top: 1px solid #e8e8e8;
  padding-top: 16px;
  margin-top: 16px;
  font-size: 18px;
  font-weight: 600;
  color: #f5222d;
}

.action-buttons {
  display: flex;
  gap: 16px;
  justify-content: flex-end;
}

/* 无符合条件的游客提示 */
.no-visitors {
  padding: 40px 20px;
  text-align: center;
  color: #999;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .nav-content {
    flex-direction: column;
    gap: 20px;
  }

  .progress {
    width: 100%;
  }

  .container {
    padding: 0 15px;
  }

  .order-info-card,
  .passenger-selection-card,
  .order-summary-card {
    padding: 16px;
  }

  .detail-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }

  .card-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .action-buttons {
    flex-direction: column;
  }

  .action-buttons .el-button {
    width: 100%;
  }
}
</style>
