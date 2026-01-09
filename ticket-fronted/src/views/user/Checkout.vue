<template>
  <div class="checkout-page">
    <div class="container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1 class="page-title">确认订单</h1>
        <p class="page-subtitle">请确认订单信息并填写游客信息</p>
      </div>

      <div class="checkout-content">
        <!-- 订单信息 -->
        <div class="order-info">
          <div class="info-card">
            <h3>订单信息</h3>
            <div class="order-details">
              <div class="detail-row">
                <span class="label">景区名称：</span>
                <span class="value">{{ orderInfo.spotName }}</span>
              </div>
              <div class="detail-row">
                <span class="label">票种：</span>
                <span class="value">{{ orderInfo.ticketName }}</span>
              </div>
              <div class="detail-row">
                <span class="label">游玩日期：</span>
                <span class="value">{{ formatDate(orderInfo.visitDate) }}</span>
              </div>
              <div class="detail-row">
                <span class="label">单价：</span>
                <span class="value">￥{{ orderInfo.unitPrice }}</span>
              </div>
              <div class="detail-row">
                <span class="label">数量：</span>
                <span class="value">{{ orderInfo.quantity }}张</span>
              </div>
              <div class="detail-row total">
                <span class="label">总价：</span>
                <span class="value">￥{{ orderInfo.totalPrice }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 游客信息表单 -->
        <div class="visitor-forms">
          <div class="info-card">
            <h3>游客信息</h3>
            <p class="form-tip">
              <el-icon><InfoFilled /></el-icon>
              请准确填写游客的真实姓名和身份证号，用于景区入园验证
            </p>

            <!-- 动态生成游客表单 -->
            <div
              v-for="(visitor, index) in visitors"
              :key="index"
              class="visitor-form-item"
            >
              <h4 class="visitor-title">游客 {{ index + 1 }}</h4>

              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item
                    :label="`游客 ${index + 1} 姓名`"
                    :prop="`visitors.${index}.visitorName`"
                    :rules="[
                      { required: true, message: '请输入游客姓名', trigger: 'blur' },
                      { min: 2, max: 10, message: '姓名长度在2-10个字符', trigger: 'blur' }
                    ]"
                  >
                    <el-input
                      v-model="visitors[index].visitorName"
                      placeholder="请输入真实姓名"
                      size="large"
                    />
                  </el-form-item>
                </el-col>

                <el-col :span="12">
                  <el-form-item
                    :label="`游客 ${index + 1} 身份证`"
                    :prop="`visitors.${index}.idCard`"
                    :rules="[
                      { required: true, message: '请输入身份证号', trigger: 'blur' },
                      { pattern: /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/,
                        message: '请输入正确的身份证号', trigger: 'blur' }
                    ]"
                  >
                    <el-input
                      v-model="visitors[index].idCard"
                      placeholder="请输入身份证号"
                      size="large"
                      maxlength="18"
                      show-word-limit
                    />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item
                :label="`游客 ${index + 1} 联系电话`"
                :prop="`visitors.${index}.contactTel`"
                :rules="[
                  { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
                ]"
              >
                <el-input
                  v-model="visitors[index].contactTel"
                  placeholder="请输入联系电话（选填）"
                  size="large"
                  maxlength="11"
                />
              </el-form-item>
            </div>

            <!-- 添加游客按钮 -->
            <div class="add-visitor">
              <el-button
                type="outline"
                icon="Plus"
                @click="addVisitor"
                :disabled="visitors.length >= 10"
              >
                添加游客
              </el-button>
              <span class="visitor-count">当前 {{ visitors.length }} 人</span>
            </div>
          </div>
        </div>

        <!-- 订单总计和提交 -->
        <div class="order-summary">
          <div class="summary-card">
            <div class="summary-content">
              <div class="summary-row">
                <span>商品总价：</span>
                <span>￥{{ orderInfo.totalPrice }}</span>
              </div>
              <div class="summary-row total">
                <span>应付总额：</span>
                <span>￥{{ orderInfo.totalPrice }}</span>
              </div>
            </div>

            <div class="action-buttons">
              <el-button size="large" @click="goBack">返回修改</el-button>
              <el-button
                type="primary"
                size="large"
                :loading="submitting"
                @click="handleSubmit"
              >
                提交订单
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 支付对话框 -->
    <el-dialog
      v-model="paymentDialog"
      title="模拟支付"
      width="400px"
      center
      :close-on-click-modal="false"
      :show-close="false"
    >
      <div class="payment-content">
        <div class="qr-code">
          <div class="qr-placeholder">
            <el-icon size="80"><CreditCard /></el-icon>
            <p>支付二维码</p>
            <p class="amount">￥{{ orderInfo.totalPrice }}</p>
          </div>
        </div>

        <div class="payment-tip">
          <p>请使用手机扫描二维码完成支付</p>
          <p class="small-tip">（这是模拟支付，点击下方按钮即可完成）</p>
        </div>
      </div>

      <template #footer>
        <el-button @click="cancelPayment">取消支付</el-button>
        <el-button type="primary" @click="confirmPayment">
          已完成支付
        </el-button>
      </template>
    </el-dialog>

    <!-- 支付成功对话框 -->
    <el-dialog
      v-model="successDialog"
      title="支付成功"
      width="400px"
      center
      :close-on-click-modal="false"
      :show-close="false"
    >
      <div class="success-content">
        <el-icon size="64" color="#52c41a"><SuccessFilled /></el-icon>
        <h3>支付成功！</h3>
        <p>订单号：{{ orderNumber }}</p>
        <p>请凭订单号和身份证到景区入园</p>
      </div>

      <template #footer>
        <el-button type="primary" @click="goToOrders">
          查看订单
        </el-button>
        <el-button @click="goToHome">返回首页</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { InfoFilled, Plus, CreditCard, SuccessFilled } from '@element-plus/icons-vue'
import { createOrder, payOrder } from '@/user/api'
import type { VisitorInfo, CreateOrderRequest } from '@/types/user'

const router = useRouter()
const route = useRoute()

// 订单信息
const orderInfo = reactive({
  spotId: '',
  spotName: '',
  ticketId: '',
  ticketName: '',
  visitDate: '',
  quantity: 1,
  unitPrice: 0,
  totalPrice: 0
})

// 游客信息
const visitors = ref<VisitorInfo[]>([
  {
    visitorName: '',
    idCard: '',
    contactTel: ''
  }
])

// 状态
const submitting = ref(false)
const paymentDialog = ref(false)
const successDialog = ref(false)
const orderNumber = ref('')

// 初始化订单信息
// 检查登录状态
const checkLoginStatus = () => {
  const token = localStorage.getItem('user_token')
  if (!token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return false
  }
  return true
}

const initOrderInfo = () => {
  // 先检查登录状态
  if (!checkLoginStatus()) {
    return
  }

  const orderData = route.query.data as string
  if (orderData) {
    try {
      const parsed = JSON.parse(decodeURIComponent(orderData))

      // 合并订单信息（这里需要根据实际数据结构调整）
      Object.assign(orderInfo, parsed)

      // 确保有足够数量的游客表单
      while (visitors.value.length < orderInfo.quantity) {
        visitors.value.push({
          visitorName: '',
          idCard: '',
          contactTel: ''
        })
      }
    } catch (error) {
      console.error('解析订单数据失败:', error)
      ElMessage.error('订单数据错误，请重新选择')
      router.back()
    }
  } else {
    ElMessage.error('订单数据缺失，请重新选择')
    router.back()
  }
}

// 添加游客
const addVisitor = () => {
  if (visitors.value.length < 10) {
    visitors.value.push({
      visitorName: '',
      idCard: '',
      contactTel: ''
    })
  }
}

// 返回修改
const goBack = () => {
  router.back()
}

// 提交订单
const handleSubmit = async () => {
  // 验证游客信息
  const invalidVisitors = visitors.value.filter(v => !v.visitorName || !v.idCard)
  if (invalidVisitors.length > 0) {
    ElMessage.error('请填写完整的游客信息')
    return
  }

  if (visitors.value.length !== orderInfo.quantity) {
    ElMessage.error(`需要填写 ${orderInfo.quantity} 个游客的信息，当前 ${visitors.value.length} 个`)
    return
  }

  try {
    submitting.value = true

    const orderRequest: CreateOrderRequest = {
      productId: orderInfo.ticketId,
      visitDate: orderInfo.visitDate,
      buyNum: orderInfo.quantity,
      visitors: visitors.value
    }

    const response = await createOrder(orderRequest)

    if (response.data.code === 200) {
      orderNumber.value = response.data.data.orderSn || `ORDER${Date.now()}`
      paymentDialog.value = true
    } else {
      ElMessage.error(response.data.message || '创建订单失败')
    }
  } catch (error) {
    console.error('创建订单失败:', error)
    ElMessage.error('创建订单失败，请重试')
  } finally {
    submitting.value = false
  }
}

// 取消支付
const cancelPayment = () => {
  paymentDialog.value = false
  ElMessage.info('支付已取消')
}

// 确认支付
const confirmPayment = async () => {
  try {
    // 模拟支付API调用
    // const response = await payOrder(orderNumber.value, 'wechat')

    // 模拟支付成功
    paymentDialog.value = false
    successDialog.value = true

    ElMessage.success('支付成功！')
  } catch (error) {
    ElMessage.error('支付失败，请重试')
  }
}

// 查看订单
const goToOrders = () => {
  successDialog.value = false
  router.push('/orders')
}

// 返回首页
const goToHome = () => {
  successDialog.value = false
  router.push('/')
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
}

onMounted(() => {
  initOrderInfo()
})
</script>

<style scoped>
.checkout-page {
  padding: 40px 0;
  background: #f8f9fa;
  min-height: calc(100vh - 200px);
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

/* 结账内容 */
.checkout-content {
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 40px;
}

/* 信息卡片 */
.info-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
}

.info-card h3 {
  font-size: 1.3rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
}

/* 订单详情 */
.order-details {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #e8e8e8;
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-row.total {
  border-top: 2px solid #1890ff;
  padding-top: 16px;
  margin-top: 16px;
  font-size: 1.1rem;
  font-weight: 600;
  color: #f5222d;
}

.label {
  color: #666;
}

.value {
  font-weight: 500;
}

/* 游客表单 */
.form-tip {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  background: #e6f7ff;
  color: #1890ff;
  padding: 12px;
  border-radius: 6px;
  margin-bottom: 24px;
  font-size: 0.9rem;
}

.visitor-form-item {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 16px;
}

.visitor-form-item:last-child {
  margin-bottom: 0;
}

.visitor-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e8e8e8;
}

.add-visitor {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #e8e8e8;
}

.visitor-count {
  color: #666;
  font-size: 0.9rem;
}

/* 订单总计 */
.order-summary {
  position: sticky;
  top: 20px;
  height: fit-content;
}

.summary-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.summary-content {
  padding: 24px;
  border-bottom: 1px solid #e8e8e8;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.summary-row.total {
  font-size: 1.2rem;
  font-weight: 600;
  color: #f5222d;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e8e8e8;
}

.action-buttons {
  padding: 24px;
  display: flex;
  gap: 12px;
}

.action-buttons .el-button {
  flex: 1;
}

/* 支付对话框 */
.payment-content {
  text-align: center;
}

.qr-code {
  margin-bottom: 24px;
}

.qr-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 40px;
  background: #f5f5f5;
  border-radius: 8px;
  border: 2px dashed #d9d9d9;
}

.qr-placeholder .amount {
  font-size: 1.5rem;
  font-weight: 600;
  color: #f5222d;
}

.payment-tip {
  margin-bottom: 20px;
}

.payment-tip .small-tip {
  font-size: 0.8rem;
  color: #999;
  margin-top: 4px;
}

/* 成功对话框 */
.success-content {
  text-align: center;
  padding: 20px 0;
}

.success-content h3 {
  color: #52c41a;
  margin: 16px 0 8px 0;
}

.success-content p {
  color: #666;
  margin: 8px 0;
  font-size: 0.9rem;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .checkout-content {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .order-summary {
    position: static;
  }
}

@media (max-width: 768px) {
  .checkout-page {
    padding: 20px 0;
  }

  .container {
    padding: 0 15px;
  }

  .page-title {
    font-size: 2rem;
  }

  .info-card {
    padding: 16px;
  }

  .visitor-form-item {
    padding: 16px;
  }

  .action-buttons {
    flex-direction: column;
  }
}
</style>
