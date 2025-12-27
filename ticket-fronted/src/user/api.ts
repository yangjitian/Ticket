import axios from 'axios'

// 后端地址配置
const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8088/api'

// 自定义JSON解析器，确保大整数ID保持为字符串
const parseJsonSafely = (text: string) => {
  const parsed = JSON.parse(text, (key: string, value: any) => {
    // 如果是id字段且是数字，转换为字符串
    if (key === 'id' && typeof value === 'number') {
      return String(value)
    }
    return value
  })

  // 递归处理所有对象的ID字段
  const ensureAllIdsAsString = (obj: any) => {
    if (obj && typeof obj === 'object') {
      if (Array.isArray(obj)) {
        obj.forEach(item => ensureAllIdsAsString(item))
      } else {
        for (const key in obj) {
          if (key === 'id' && obj[key] != null && typeof obj[key] === 'number') {
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

// 请求拦截：自动注入 Authorization 头
instance.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
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
      // 清理本地登录信息
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      localStorage.removeItem('userRole')
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

// 景区相关API
export const getScenicSpotList = (params: any) => {
  return instance.post('/scenicSpot/list', params)
}

export const getScenicSpotDetail = (id: string) => {
  return instance.get('/scenicSpot/get', { params: { id } })
}

// 门票产品相关API
export const getTicketProducts = (spotId: string) => {
  return instance.get('/ticket/product/list', { params: { spotId } })
}

export const getTicketStock = (productId: string, date: string) => {
  return instance.get('/ticket/stock', { params: { productId, date } })
}

// 订单相关API
export const createOrder = (orderData: any) => {
  return instance.post('/order/create', orderData)
}

export const getUserOrders = (params: any) => {
  return instance.get('/order/user/list', { params })
}

// 支付相关API
export const payOrder = (orderId: string, paymentMethod: string) => {
  return instance.post('/order/pay', { orderId, paymentMethod })
}
