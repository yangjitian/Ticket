import axios from 'axios'
import JSONbig from 'json-bigint'

// 后端地址（如需更改请在此处修改）
// 注意：后端 spring boot 在 application.yml 中配置了 server.port=8088 和 context-path=/api
// 因此默认 baseURL 指向 http://localhost:8088/api
const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8088/api'

// 使用json-bigint库安全解析JSON，保持大整数精度
const parseJsonSafely = (text: string) => {
  console.log('开始使用json-bigint解析JSON响应，输入文本:', text.substring(0, 100) + '...')

  try {
    // 配置json-bigint，将大整数存储为字符串
    const JSONbigString = JSONbig({
      storeAsString: true
    })

    console.log('创建json-bigint实例成功')

    // 使用配置后的json-bigint解析，所有大整数会自动转换为字符串
    const parsed = JSONbigString.parse(text)
    console.log('json-bigint解析成功，结果:', parsed)
    console.log('json-bigint解析完成，大整数已自动转换为字符串')
    return parsed
  } catch (e) {
    console.error('json-bigint解析失败:', e)
    console.error('失败的文本:', text)

    // 如果json-bigint失败，尝试手动处理大整数
    try {
      console.log('尝试手动正则表达式处理大整数')
      // 使用正则表达式将大整数替换为字符串
      const manualProcessed = text.replace(/:\s*(\d{15,})/g, ': "$1"')
      console.log('手动处理后的文本:', manualProcessed.substring(0, 100) + '...')

      const parsed = JSON.parse(manualProcessed)
      console.log('手动处理解析成功')
      return parsed
    } catch (manualError) {
      console.error('手动处理也失败:', manualError)
      // 最后回退到标准JSON.parse
      console.warn('回退到标准JSON.parse')
      return JSON.parse(text)
    }
  }
}


const instance = axios.create({
  baseURL: BASE_URL,
  timeout: 10000,
  // 禁用自动JSON解析和序列化，完全由我们控制
  transformResponse: [(data) => {
    // 返回原始字符串，让response interceptor处理
    return data
  }],
  // 禁用请求数据的自动序列化，由request interceptor处理
  transformRequest: [(data) => {
    // 返回原始数据，让request interceptor处理序列化
    return data
  }]
})

// 安全的JSON序列化函数，确保大整数保持为字符串
const safeJsonStringify = (obj: any): string => {
  return JSONbig({
    storeAsString: true
  }).stringify(obj)
}

// 请求拦截：自动注入管理员 Authorization 头，并确保大整数在序列化时保持为字符串
instance.interceptors.request.use((config) => {
  const token = localStorage.getItem('admin_token')
  if (token && config.headers) {
    config.headers['Authorization'] = `Bearer ${token}`
  }

  // 如果是POST请求且有数据，使用安全的JSON序列化
  if (config.method?.toLowerCase() === 'post' && config.data) {
    // 如果不是multipart/form-data请求，使用安全的JSON序列化
    const isMultipart = config.headers['Content-Type'] === 'multipart/form-data' ||
                       (config.data instanceof FormData)

    if (!isMultipart) {
    console.log('POST请求数据序列化，使用安全JSON序列化保持大整数精度')
    console.log('原始请求数据:', config.data)

    // 使用json-bigint进行安全的序列化
    config.data = safeJsonStringify(config.data)
    console.log('安全序列化后的数据:', config.data)

    // 设置Content-Type为application/json
    config.headers['Content-Type'] = 'application/json'
    } else {
      console.log('检测到multipart/form-data请求，跳过JSON序列化')
    }
  }

  return config
})


// 响应拦截：统一处理 401（未授权），并处理大整数ID精度问题
instance.interceptors.response.use(
  (response) => {
    // 处理响应数据，确保大整数ID不丢失精度
    const contentType = response.headers['content-type'] || ''
    const isJsonResponse = contentType.includes('application/json')

    if (isJsonResponse && response.data && typeof response.data === 'string') {
      try {
        console.log('检测到JSON响应，开始使用json-bigint解析，数据长度:', response.data.length)
        console.log('JSON数据预览:', response.data.substring(0, 200) + '...')
        response.data = parseJsonSafely(response.data)
        console.log('JSON响应数据解析完成，ID精度已保持')
      } catch (e) {
        console.error('JSON响应数据解析失败:', e)
        // 如果解析失败，尝试使用标准JSON.parse作为兜底
        try {
          response.data = JSON.parse(response.data)
          console.warn('使用了兜底JSON.parse，可能丢失ID精度')
        } catch (fallbackError) {
          console.error('兜底JSON.parse也失败:', fallbackError)
        }
      }
    } else if (response.data && typeof response.data === 'object') {
      // 如果数据已经被解析为对象，说明axios自动解析了，这时ID可能已经丢失
      console.warn('数据已被自动解析为对象，可能已丢失ID精度，尝试重新处理')
      // 这里可以添加额外的处理逻辑，但通常已经太晚了
    } else {
      console.log('响应不是JSON类型或数据格式不正确，跳过处理')
    }

    return response
  },
  (error) => {
    const status = error?.response?.status
    if (status === 401) {
      // 清理管理员本地登录信息
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_userRole')
      localStorage.removeItem('admin_user')
      // 跳转到管理员登录页（使用绝对路径，保证不会再次被 base history 前缀影响）
      const origin = window.location.origin
      window.location.href = `${origin}/admin/login`
      return Promise.reject(error)
    }
    return Promise.reject(error)
  }
)

export default instance

import type {
  UserQueryRequest,
  UserStatsVO,
  DistributorVO,
  ScenicSpotQueryRequest,
  ScenicSpotAddRequest,
  ScenicSpotUpdateRequest,
  TicketProductQueryRequest,
  TicketPriceUpdateRequest,
  TicketProductUpdateRequest,
  ScenicSpotTicketsVO,
  TicketProductVO
} from '@/types/api'

export const login = (userAccount: string, password: string) => {
  return instance.post('/user/login', { userAccount, password })
}

export const logout = () => {
  return instance.post('/user/logout')
}

export const getCurrentAdmin = () => {
  return instance.post('/user/current')
}

// 用户管理相关API
export const listUsers = (params: UserQueryRequest) => {
  return instance.post('/user/list', params)
}

export const updateUserStatus = (data: { id: string; userStatus: number }) => {
  return instance.post('/user/update/status', data)
}

export const deleteUser = (data: { id: string }) => {
  return instance.post('/user/delete', data)
}

export const getUserStats = (params: { days: number }) => {
  return instance.post('/user/stats', params)
}

// 经销商管理相关API
export const listDistributors = (params: UserQueryRequest) => {
  return instance.post('/user/distributor/list', params)
}

export const auditDistributor = (data: { id: string; auditStatus: number }) => {
  return instance.post('/user/audit', data)
}

export const getDistributorStats = (distributorId: string) => {
  return instance.get(`/user/distributor/stats/${distributorId}`)
}

// 景区管理相关API
export const getScenicSpotList = (params: ScenicSpotQueryRequest) => {
  return instance.post('/scenicSpot/list', params)
}

export const addScenicSpot = (data: ScenicSpotAddRequest) => {
  return instance.post('/scenicSpot/add', data)
}

export const updateScenicSpot = (data: ScenicSpotUpdateRequest) => {
  return instance.post('/scenicSpot/update', data)
}

export const deleteScenicSpot = (id: string) => {
  return instance.post('/scenicSpot/delete', null, { params: { id } })
}

export const getScenicSpotDetail = (id: string) => {
  return instance.get('/scenicSpot/get', { params: { id } })
}

export const uploadScenicSpotImage = (file: File, scenicSpotId: string) => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('scenicSpotId', scenicSpotId)
  return instance.post('/scenicSpot/upload/image', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 门票产品管理相关API
export const getTicketProductList = (params: TicketProductQueryRequest) => {
  return instance.post('/ticketProduct/list', params)
}

export const getTicketProductDetail = (id: string) => {
  return instance.get('/ticketProduct/get', { params: { id } })
}

export const updateTicketProduct = (data: TicketProductUpdateRequest) => {
  return instance.post('/ticketProduct/update', data)
}

export const updateTicketPrice = (data: TicketPriceUpdateRequest) => {
  return instance.post('/ticketProduct/update/price', data)
}

export const toggleTicketSaleStatus = (data: { id: string; saleStatus: number }) => {
  return instance.post('/ticketProduct/toggle/status', data)
}

export const deleteTicketProduct = (data: { id: string }) => {
  return instance.post('/ticketProduct/delete', data)
}

// 门票库存管理相关API（已弃用，请使用景区库存管理API）

// 门票价格及库存管理页面专用API
export const getScenicSpotTicketsList = (params: {
  current?: number
  pageSize?: number
  spotName?: string
  spotStatus?: number
}) => {
  return instance.post('/ticketProduct/scenicSpot/tickets/list', params)
}

// 景区库存管理相关API
export const getScenicStockList = (params: {
  scenicId: string
  startDate?: string
  endDate?: string
}) => {
  return instance.post('/scenicStock/list', params)
}

export const batchUpdateScenicStock = (data: {
  scenicId: string
  stocks: Array<{
    stockDate: string
    totalQty: number
  }>
}) => {
  return instance.post('/scenicStock/batch/update', data)
}

// 控制台统计相关API
export const getDashboardStats = () => {
  return instance.get('/dashboard/stats')
}

export const getRevenueStats = (params: { days: number }) => {
  return instance.get('/dashboard/revenue-stats', { params })
}



