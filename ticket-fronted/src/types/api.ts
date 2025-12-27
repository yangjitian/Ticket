// API响应类型定义
export interface ApiResponse<T = any> {
  code: number
  data: T
  message: string
}

// 用户登录响应
export interface LoginResponse {
  token: string
  user: {
    id: string
    userAccount: string
    nickname: string
    role: number
    userStatus: number
  }
}

// 分页响应
export interface PageResponse<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// 景区信息
export interface ScenicSpot {
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

// 景区添加请求
export interface ScenicSpotAddRequest {
  spotName: string
  description: string
  address: string
  openTime: string
  contactNum: string
  imageUrls: string[]
  spotStatus: number
}

// 景区更新请求
export interface ScenicSpotUpdateRequest extends ScenicSpotAddRequest {
  id: string
}

// 景区查询请求
export interface ScenicSpotQueryRequest {
  current?: number
  pageSize?: number
  id?: string
  spotName?: string
  address?: string
  spotStatus?: number
}
