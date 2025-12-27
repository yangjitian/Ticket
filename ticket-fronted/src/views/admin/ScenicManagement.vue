<template>
  <div class="scenic-management">
    <div class="page-header">
      <h2>景区管理</h2>
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon>
        添加景区
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="景区名称">
          <el-input
            v-model="searchForm.spotName"
            placeholder="请输入景区名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="地址">
          <el-input
            v-model="searchForm.address"
            placeholder="请输入地址"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.spotStatus"
            placeholder="请选择状态"
            clearable
            style="width: 120px"
          >
            <el-option label="开放" :value="1" />
            <el-option label="关闭" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 景区列表 -->
    <el-table
      v-loading="loading"
      :data="scenicList"
      style="width: 100%"
      stripe
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="spotName" label="景区名称" width="140" />
      <el-table-column prop="address" label="地址" min-width="160" />
      <el-table-column prop="contactNum" label="联系电话" width="120" />
      <el-table-column label="图片" width="120">
        <template #default="scope">
          <div v-if="scope.row.imageUrls && scope.row.imageUrls.length > 0" class="image-group">
            <el-image
              v-for="(url, index) in scope.row.imageUrls.slice(0, 3)"
              :key="index"
              :src="url"
              :preview-src-list="scope.row.imageUrls"
              style="width: 40px; height: 40px; object-fit: cover; margin-right: 4px;"
            />
            <span v-if="scope.row.imageUrls.length > 3" class="more-images">
              +{{ scope.row.imageUrls.length - 3 }}
            </span>
          </div>
          <span v-else>无图片</span>
        </template>
      </el-table-column>
      <el-table-column label="营业时间" width="120">
        <template #default="scope">
          {{ formatBusinessHours(scope.row.openTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="description" label="景区介绍" min-width="180">
        <template #default="scope">
          <div class="description-cell">
            {{ scope.row.description ? scope.row.description.substring(0, 50) + (scope.row.description.length > 50 ? '...' : '') : '暂无介绍' }}
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="spotStatus" label="状态" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.spotStatus === 1 ? 'success' : 'danger'">
            {{ scope.row.spotStatus === 1 ? '开放' : '关闭' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button size="small" type="primary" @click="showEditDialog(scope.row)">
            编辑
          </el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 添加/编辑景区对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '添加景区' : '编辑景区'"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="scenicFormRef"
        :model="scenicForm"
        :rules="scenicRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="景区名称" prop="spotName">
              <el-input v-model="scenicForm.spotName" placeholder="请输入景区名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactNum">
              <el-input v-model="scenicForm.contactNum" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="地址" prop="address">
          <el-input v-model="scenicForm.address" placeholder="请输入景区地址" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始营业时间" prop="openingTime">
              <el-time-picker
                v-model="scenicForm.openingTime"
                format="HH:mm"
                value-format="HH:mm"
                placeholder="选择开始营业时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束营业时间" prop="closingTime">
              <el-time-picker
                v-model="scenicForm.closingTime"
                format="HH:mm"
                value-format="HH:mm"
                placeholder="选择结束营业时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="开放时间说明" prop="openTime">
          <el-input
            v-model="scenicForm.openTime"
            type="textarea"
            :rows="2"
            placeholder="请输入其他开放时间说明（如节假日安排等）"
          />
        </el-form-item>

        <el-form-item label="景区介绍" prop="description">
          <el-input
            v-model="scenicForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入景区详细介绍"
          />
        </el-form-item>

        <el-form-item label="景区图片">
          <el-upload
            v-if="dialogType === 'edit'"
            ref="uploadRef"
            :file-list="fileList"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :before-upload="beforeUpload"
            :http-request="handleUpload"
            :limit="10"
            list-type="picture-card"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
            <template #tip>
              <div class="upload-tip">支持jpg、jpeg、png、gif、webp格式，文件大小不超过4MB</div>
            </template>
          </el-upload>
          <div v-else class="upload-disabled">
            <el-text type="info">请先保存景区信息，然后在编辑中上传图片</el-text>
          </div>
        </el-form-item>

        <el-form-item label="状态" prop="spotStatus">
          <el-radio-group v-model="scenicForm.spotStatus">
            <el-radio :value="1">开放</el-radio>
            <el-radio :value="0">关闭</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import {
  getScenicSpotList,
  addScenicSpot,
  updateScenicSpot,
  deleteScenicSpot,
  uploadScenicSpotImage
} from '@/admin/api'

interface ScenicSpot {
  id: string
  spotName: string
  description: string
  address: string
  openTime: string
  contactNum: string
  imageUrls: string[]
  spotStatus: number
  createTime: string
  updateTime: string
}

// 加载状态
const loading = ref(false)
const submitLoading = ref(false)

// 对话框状态
const dialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')

// 景区列表
const scenicList = ref<ScenicSpot[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 搜索表单
const searchForm = reactive({
  spotName: '',
  address: '',
  spotStatus: undefined as number | undefined
})

// 景区表单
const scenicForm = reactive({
  id: '',
  spotName: '',
  description: '',
  address: '',
  openTime: '', // 保持原有字段用于向后兼容
  openingTime: '', // 开始营业时间
  closingTime: '', // 结束营业时间
  contactNum: '',
  imageUrls: [] as string[],
  spotStatus: 1,
  originalImageUrls: [] as string[] // 存储编辑时的原始图片，用于比较
})

// 单独管理当前的图片URLs，与fileList同步
const currentImageUrls = ref<string[]>([])

// 当前编辑的景区ID（用于图片上传）
const currentScenicSpotId = ref('')

// 文件列表
const fileList = ref<any[]>([])

// 表单引用
const scenicFormRef = ref()
const uploadRef = ref()

// 表单验证规则
const scenicRules = {
  spotName: [
    { required: true, message: '请输入景区名称', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入景区地址', trigger: 'blur' }
  ],
  contactNum: [
    { required: true, message: '请输入联系电话', trigger: 'blur' }
  ],
  openingTime: [
    { required: true, message: '请选择开始营业时间', trigger: 'change' }
  ],
  closingTime: [
    { required: true, message: '请选择结束营业时间', trigger: 'change' }
  ],
  spotStatus: [
    { required: true, message: '请选择景区状态', trigger: 'change' }
  ]
}

// 获取景区列表
const fetchScenicList = async () => {
  try {
    loading.value = true

    const params = {
      current: currentPage.value,
      pageSize: pageSize.value,
      ...searchForm
    }

    const response = await getScenicSpotList(params)

    if (response.data.code === 200) {
      scenicList.value = response.data.data.records || []
      total.value = response.data.data.total || 0
    } else {
      ElMessage.error(response.data.message || '获取景区列表失败')
    }
  } catch (error) {
    console.error('获取景区列表失败:', error)
    ElMessage.error('获取景区列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchScenicList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    spotName: '',
    address: '',
    spotStatus: undefined
  })
  handleSearch()
}

// 显示添加对话框
const showAddDialog = () => {
  dialogType.value = 'add'
  dialogVisible.value = true
  currentScenicSpotId.value = '' // 添加模式下没有景区ID
  resetForm()
}

// 显示编辑对话框
const showEditDialog = (row: ScenicSpot) => {
  dialogType.value = 'edit'
  dialogVisible.value = true

  // 解析开放时间说明，提取营业时间
  const { openingTime, closingTime, description } = parseOpenTime(row.openTime || '')

  // 填充表单数据
  Object.assign(scenicForm, {
    id: row.id,
    spotName: row.spotName,
    description: row.description,
    address: row.address,
    openTime: description, // 其他说明文字
    openingTime: openingTime,
    closingTime: closingTime,
    contactNum: row.contactNum,
    imageUrls: [...(row.imageUrls || [])],
    originalImageUrls: [...(row.imageUrls || [])], // 保存原始图片用于比较
    spotStatus: row.spotStatus
  })

  // 设置当前景区ID和图片URLs
  currentScenicSpotId.value = row.id
  currentImageUrls.value = [...(row.imageUrls || [])]

  // 设置文件列表 - 为已存在的图片设置正确的格式
  fileList.value = (row.imageUrls || []).map((url, index) => ({
    name: `已上传图片-${index + 1}`,
    url: url,
    uid: `existing-${index}`,
    status: 'success', // 标记为已成功上传的文件
    percentage: 100, // 显示为100%完成
    // 确保Element Plus能正确识别这是一个已存在的文件
    response: { code: 200, data: url } // 模拟成功的响应
  }))
}

// 解析开放时间说明
const parseOpenTime = (openTimeText: string) => {
  let openingTime = ''
  let closingTime = ''
  let description = openTimeText

  // 尝试匹配时间格式，如 "09:00-18:00" 或 "9:00-6:00"
  const timePattern = /(\d{1,2}):(\d{2})\s*-\s*(\d{1,2}):(\d{2})/
  const match = openTimeText.match(timePattern)

  if (match && match[1] && match[2] && match[3] && match[4]) {
    openingTime = `${match[1].padStart(2, '0')}:${match[2]}`
    closingTime = `${match[3].padStart(2, '0')}:${match[4]}`
    // 移除时间部分，保留其他描述
    description = openTimeText.replace(timePattern, '').trim()
  }

  return { openingTime, closingTime, description }
}

// 组合开放时间说明
const combineOpenTime = (openingTime: string, closingTime: string, description: string) => {
  let result = ''

  if (openingTime && closingTime) {
    result += `${openingTime}-${closingTime}`
  }

  if (description) {
    result += (result ? ' ' : '') + description
  }

  return result || '未设置'
}

// 重置表单
const resetForm = () => {
  if (scenicFormRef.value) {
    scenicFormRef.value.resetFields()
  }
  Object.assign(scenicForm, {
    id: '',
    spotName: '',
    description: '',
    address: '',
    openTime: '',
    openingTime: '',
    closingTime: '',
    contactNum: '',
    imageUrls: [],
    originalImageUrls: [],
    spotStatus: 1
  })
  fileList.value = []
  currentImageUrls.value = []
}

// 文件上传处理
// Element Plus会自动更新fileList，所以这里只需要处理验证逻辑
const handleFileChange = (file: any, fileList: any[]) => {
  // 文件状态变化处理
}

const handleFileRemove = (file: any, fileList: any[]) => {
  // Element Plus会自动从fileList中移除文件
  // 更新currentImageUrls
  currentImageUrls.value = fileList
    .filter(f => f.url && f.status === 'success')
    .map(f => f.url)
}

// 不需要handleUploadSuccess，因为我们在handleUpload中处理成功情况

const handleUpload = async (options: any) => {
  const { file, onSuccess, onError } = options
  try {
    console.log('开始上传文件:', file.name, 'scenicSpotId:', currentScenicSpotId.value)
    const response = await uploadScenicSpotImage(file, currentScenicSpotId.value)
    console.log('上传响应:', response.data)

    if (response.data.code === 200) {
      const url = response.data.data
      console.log('上传成功，URL:', url)

      // 手动更新file对象
      file.url = url
      file.status = 'success'
      file.response = { code: 200, data: url }

      // 由于只支持一张图片，清空fileList并添加新文件
      fileList.value = [file]

      // 更新currentImageUrls
      currentImageUrls.value = [url]

      console.log('fileList更新后:', fileList.value)

      // 调用onSuccess让Element Plus知道上传成功
      onSuccess({ code: 200, data: url }, file)

      // 刷新景区列表数据
      console.log('上传成功，开始刷新数据')
      fetchScenicList()
    } else {
      console.log('上传失败:', response.data.message)
      file.status = 'fail'
      onError(new Error(response.data.message || '上传失败'))
    }
  } catch (error) {
    console.error('上传异常:', error)
    file.status = 'fail'
    onError(error)
  }
}

const beforeUpload = (file: File) => {
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
  const maxSize = 4 * 1024 * 1024 // 4MB

  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('只支持jpg、jpeg、png、gif、webp格式的图片')
    return false
  }

  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过4MB')
    return false
  }

  return true
}

// 提交表单
const handleSubmit = async () => {
  try {
    await scenicFormRef.value?.validate()

    submitLoading.value = true

    // 直接从fileList构建图片URLs（支持多张图片）
    let newImageUrls: string[] = fileList.value
      .filter(file => file.url && file.status === 'success')
      .map(file => file.url)

    // 如果是编辑模式且没有找到图片，使用原始图片作为兜底
    if (dialogType.value === 'edit' && newImageUrls.length === 0 && scenicForm.originalImageUrls.length > 0) {
      console.log('编辑模式：使用原始图片兜底')
      newImageUrls = [...scenicForm.originalImageUrls]
    }

    // 组合开放时间说明
    const combinedOpenTime = combineOpenTime(
      scenicForm.openingTime,
      scenicForm.closingTime,
      scenicForm.openTime
    )

    const submitData = {
      ...scenicForm,
      openTime: combinedOpenTime
      // 注意：imageUrl不再通过更新接口传递，由上传接口直接处理
    }

    let response
    if (dialogType.value === 'add') {
      response = await addScenicSpot(submitData)
    } else {
      response = await updateScenicSpot(submitData)
    }

    if (response.data.code === 200) {
      ElMessage.success(dialogType.value === 'add' ? '添加成功' : '更新成功')
      dialogVisible.value = false
      fetchScenicList()
    } else {
      ElMessage.error(response.data.message || '操作失败')
    }
  } catch (error: any) {
    console.error('提交失败:', error)
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('操作失败，请重试')
    }
  } finally {
    submitLoading.value = false
  }
}

// 删除景区
const handleDelete = async (row: ScenicSpot) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除景区"${row.spotName}"吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await deleteScenicSpot(row.id)

    if (response.data.code === 200) {
      ElMessage.success('删除成功')
      fetchScenicList()
    } else {
      ElMessage.error(response.data.message || '删除失败')
    }
  } catch (error) {
    // 用户取消操作
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 分页处理
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchScenicList()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  fetchScenicList()
}

// 格式化营业时间
const formatBusinessHours = (openTimeText: string) => {
  if (!openTimeText || openTimeText === '未设置') return '未设置'

  const { openingTime, closingTime, description } = parseOpenTime(openTimeText)

  if (openingTime && closingTime) {
    let result = `${openingTime}-${closingTime}`
    if (description) {
      result += ` (${description})`
    }
    return result
  }

  return openTimeText
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

// 初始化
// 监听fileList变化，更新currentImageUrls
watch(fileList, (newFileList) => {
  // 收集所有已成功上传的图片URL
  const imageUrls: string[] = newFileList
    .filter(file => file.url && file.status === 'success')
    .map(file => file.url)

  // 更新currentImageUrls，但避免在编辑初始化时被意外覆盖
  if (imageUrls.length > 0 || dialogType.value !== 'edit') {
    currentImageUrls.value = imageUrls
  }
}, { deep: true })

onMounted(() => {
  fetchScenicList()
})
</script>

<style scoped>
.scenic-management {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  box-sizing: border-box;
}

/* 响应式布局 */
@media (max-width: 1200px) {
  .scenic-management {
    padding: 15px;
    max-width: 100%;
  }
}

@media (max-width: 768px) {
  .scenic-management {
    padding: 10px;
  }

  .search-bar {
    padding: 15px;
  }

  .search-form {
    flex-direction: column;
    align-items: stretch;
  }

  .search-form .el-form-item {
    margin-bottom: 10px;
  }
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #333;
}

.search-bar {
  background: #f5f5f5;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.upload-tip {
  margin-top: 8px;
  color: #999;
  font-size: 12px;
}

.dialog-footer {
  text-align: right;
}

.image-group {
  display: flex;
  align-items: center;
}

.more-images {
  font-size: 12px;
  color: #999;
  margin-left: 4px;
}

.description-cell {
  line-height: 1.4;
  color: #666;
}
</style>
