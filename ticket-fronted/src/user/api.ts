import axios from 'axios'

// 后端地址配置
const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8088/api'

// 自定义JSON解析器，确保大整数ID保持为字符串
const parseJsonSafely = (text: string) => {
  // 预处理JSON字符串，将大整数ID包装为字符串
  const processedText = text.replace(/("id"\s*:\s*)(-?\d{15,})/g, '$1"$2"')
                              .replace(/("([a-zA-Z]+Id)"\s*:\s*)(-?\d{15,})/g, '$1"$2"')
                              .replace(/("spotId"\s*:\s*)(-?\d{15,})/g, '$1"$2"')

  const parsed = JSON.parse(processedText)

  // 递归处理所有对象的ID字段，确保字符串格式
  const ensureAllIdsAsString = (obj: any) => {
    if (obj && typeof obj === 'object') {
      if (Array.isArray(obj)) {
        obj.forEach(item => ensureAllIdsAsString(item))
      } else {
        for (const key in obj) {
          // 处理所有以Id结尾的字段，以及id字段，强制转换为字符串确保精度
          if ((key === 'id' || key.endsWith('Id') || key.endsWith('ID') || key === 'spotId') &&
              obj[key] != null) {
            // 无论当前是什么类型，都强制转换为字符串以确保精度
            obj[key] = String(obj[key])
          } else if (typeof obj[key] === 'object') {
            ensureAllIdsAsString(obj[key])
          }
        }
      }
    }
  }

  ensureAllIdsAsString(parsed)
  return parsed
}

const instance = axios.create({
  baseURL: BASE_URL,
  timeout: 10000,
  // 自定义响应转换器
  transformResponse: [(data, headers) => {
    const contentType = headers?.['content-type'] || ''
    if (contentType.includes('application/json') && typeof data === 'string') {
      try {
        return parseJsonSafely(data)
      } catch (e) {
        console.error('自定义JSON解析失败:', e)
        return JSON.parse(data)
      }
    }
    return data
  }]
})

// 请求拦截：自动注入用户 Authorization 头
instance.interceptors.request.use((config) => {
  const token = localStorage.getItem('user_token')
  if (token && config.headers) {
    config.headers['Authorization'] = `Bearer ${token}`
  }
  return config
})

// 响应拦截：统一处理 401 未授权
instance.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status
    if (status === 401) {
      // 清理用户本地登录信息
      localStorage.removeItem('user_token')
      localStorage.removeItem('user_user')
      localStorage.removeItem('user_userRole')
      // 跳转到用户登录页
      const origin = window.location.origin
      window.location.href = `${origin}/login`
      return Promise.reject(error)
    }
    return Promise.reject(error)
  }
)

export default instance

// 用户登录相关
export const userLogin = (userAccount: string, password: string) => {
  return instance.post('/user/login', { userAccount, password })
}

// 用户注册相关
export const userRegister = (registerData: any) => {
  return instance.post('/user/register', registerData)
}

// 用户登出
export const userLogout = () => {
  return instance.post('/user/logout')
}

// 获取当前用户信息
export const getCurrentUser = () => {
  return instance.post('/user/current')
}

// 发送邮箱验证码
export const sendEmailCode = (userAccount: string) => {
  return instance.post('/email/send-code', { userAccount })
}

// 发送邮箱换绑验证码
export const sendChangeEmailCode = (userAccount: string) => {
  return instance.post('/email/send-changeCode', { userAccount })
}

// 换绑邮箱
export const changeUserEmail = (emailData: { newEmail: string; verificationCode: string }) => {
  return instance.post('/user/change-email', emailData)
}

// 头像上传
export const updateUserAvatar = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return instance.post('/user/update/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 景区相关API
export const getScenicSpotList = (params: any) => {
  return instance.post('/scenicSpot/list', params)
}

export const getScenicSpotDetail = (id: string) => {
  return instance.get('/scenicSpot/get', { params: { id } })
}

export const getScenicStockOverview = (scenicId: string) => {
  return instance.get('/scenicStock/overview', { params: { scenicId } })
}

// 门票产品相关API
export const getTicketProducts = (spotId: string) => {
  return instance.post('/ticketProduct/list', {
    spotId: String(spotId), // 确保是字符串格式
    current: 1,
    pageSize: 100, // 获取所有产品
    saleStatus: 1   // 只获取上架的产品
  })
}

export const getTicketStock = (productId: string, date: string) => {
  return instance.get('/ticket/stock', { params: { productId, date } })
}

// 购票相关API
export const bookTicket = (bookData: any) => {
  // 使用BigInt处理大整数，然后转换为字符串发送
  const requestData = {
    spotId: BigInt(bookData.spotId).toString(), // 使用BigInt确保精度
    ticketType: Number(bookData.ticketType),
    visitDate: String(bookData.visitDate),
    visitorIds: bookData.visitorIds.map((id: any) => BigInt(id).toString()) // 游客ID也使用BigInt
  }

  console.log('发送购票请求数据:', requestData)
  console.log('原始spotId:', bookData.spotId, '类型:', typeof bookData.spotId)
  console.log('转换后spotId:', requestData.spotId, '类型:', typeof requestData.spotId)

  return instance.post('/order/book', requestData)
}

export const getBookTicketInfo = (spotId: string, ticketType: number, visitDate: string) => {
  return instance.get('/order/book-info', { params: { spotId, ticketType, visitDate } })
}

export const getOrderDetail = (orderId: string) => {
  return instance.get('/order/detail', { params: { orderId } })
}

// 订单相关API
export const createOrder = (orderData: any) => {
  return instance.post('/order/create', orderData)
}

export const getUserOrders = (params: any) => {
  return instance.get('/order/user/list', { params })
}

// 获取用户订单详情列表（VO格式，包含景区和产品信息）
export const getUserOrderVOs = (params: any) => {
  return instance.get('/order/user/list-vo', { params })
}

// 支付相关API
export const payOrder = (payData: any) => {
  return instance.post('/order/pay', payData)
}

// 取消订单
export const cancelOrder = (orderId: string) => {
  return instance.post('/order/cancel', { orderId })
}

// 删除订单
export const deleteOrder = (orderId: string) => {
  return instance.post('/order/delete', { orderId })
}

// 核销相关API
export const verifyOrder = (verificationCode: string) => {
  return instance.post('/order/verify', { verificationCode })
}

// 用户游客信息相关API
export const addUserVisitor = (visitorData: any) => {
  return instance.post('/user-visitor/add', visitorData)
}

export const updateUserVisitor = (visitorData: any) => {
  return instance.post('/user-visitor/update', visitorData)
}

export const deleteUserVisitor = (visitorId: string) => {
  return instance.post('/user-visitor/delete', { id: visitorId })
}

export const getUserVisitors = () => {
  return instance.get('/user-visitor/list')
}

export const getUserVisitorById = (id: string) => {
  return instance.get('/user-visitor/get', { params: { id } })
}

export const setDefaultVisitor = (visitorId: string) => {
  return instance.post('/user-visitor/set-default', null, { params: { visitorId } })
}

export const getDefaultVisitor = () => {
  return instance.get('/user-visitor/default')
}
