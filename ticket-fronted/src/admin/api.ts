import axios from 'axios'

// 后端地址（如需更改请在此处修改）
// 注意：后端 spring boot 在 application.yml 中配置了 server.port=8088 和 context-path=/api
// 因此默认 baseURL 指向 http://localhost:8088/api
const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8088/api'

// 自定义JSON解析器，确保大整数ID保持为字符串（用于显示）
const parseJsonSafely = (text: string) => {
  const parsed = JSON.parse(text, (key, value) => {
    // 如果是id字段且是数字，转换为字符串（仅用于前端显示，避免JavaScript精度丢失）
    if (key === 'id' && typeof value === 'number') {
      return String(value)
    }
    return value
  })

  // 递归处理所有对象的ID字段，确保数组中的对象也被处理
  const ensureAllIdsAsString = (obj: any) => {
    if (obj && typeof obj === 'object') {
      if (Array.isArray(obj)) {
        // 处理数组
        obj.forEach(item => ensureAllIdsAsString(item))
      } else {
        // 处理对象
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
  // 自定义响应转换器，确保大整数ID不丢失精度
  transformResponse: [(data, headers) => {
    // 如果是JSON响应，使用自定义解析器
    const contentType = headers?.['content-type'] || ''
    if (contentType.includes('application/json') && typeof data === 'string') {
      try {
        // 使用自定义JSON解析器处理大整数ID
        return parseJsonSafely(data)
      } catch (e) {
        console.error('自定义JSON解析失败:', e)
        // 如果自定义解析失败，回退到默认解析
        return JSON.parse(data)
      }
    }
    return data
  }]
})

// 请求拦截：自动注入 Authorization 头（若存在）
instance.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token && config.headers) {
    config.headers['Authorization'] = `Bearer ${token}`
  }

  return config
})


// 响应拦截：统一处理 401（未授权），自动清理并跳转到登录页
// 注意：JSON解析现在在transformResponse中处理，这里只处理认证相关逻辑
instance.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    const status = error?.response?.status
    if (status === 401) {
      // 清理本地登录信息
      localStorage.removeItem('token')
      localStorage.removeItem('userRole')
      localStorage.removeItem('user')
      // 跳转到管理员登录页（使用绝对路径，保证不会再次被 base history 前缀影响）
      const origin = window.location.origin
      window.location.href = `${origin}/admin/login`
      return Promise.reject(error)
    }
    return Promise.reject(error)
  }
)

export default instance

import type { LoginResponse, PageResponse, ScenicSpot, ScenicSpotQueryRequest, ScenicSpotAddRequest, ScenicSpotUpdateRequest } from '@/types/api'

export const login = (userAccount: string, password: string) => {
  return instance.post('/user/login', { userAccount, password })
}

export const logout = () => {
  return instance.post('/user/logout')
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



