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
    role: string
    userStatus: string
    auditStatus?: string
  }
}

// 用户信息
export interface UserVO {
  id: string
  userAccount: string
  nickname: string
  avatar?: string
  role: number
  userStatus: number
  auditStatus?: number
  createTime: string
  updateTime: string
}

// 用户查询请求
export interface UserQueryRequest {
  current?: number
  pageSize?: number
  searchKey?: string
  role?: number
  userStatus?: number
  auditStatus?: number
}

// 用户统计数据
export interface UserStatsVO {
  dates: string[]
  registerCounts: number[]
  totalUsers: number
  todayUsers: number
  weekUsers: number
  monthUsers: number
}

// 经销商信息
export interface DistributorVO {
  id: string
  userAccount: string
  nickname: string
  avatar?: string
  role: number
  userStatus: number
  auditStatus: number
  createTime: string
  updateTime: string
  orderCount?: string
  salesAmount?: string
  commission?: string
}

// 经销商统计数据
export interface DistributorStatsVO {
  orderCount: string
  salesAmount: string
  commission: string
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
  level?: string
  province?: string
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
  level?: string
  province?: string
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
  level?: string
  province?: string
  address?: string
  spotStatus?: number
}

// 门票产品信息
export interface TicketProduct {
  id: string
  spotId: string
  productName: string
  ticketType: number
  priceRetail: number
  priceSettlement: number
  defaultDistSalePrice?: number
  saleStatus: number
  createTime: string
  updateTime: string
}

// 门票产品VO（包含景区信息）
export interface TicketProductVO extends TicketProduct {
  scenicSpot?: ScenicSpot
}

// 门票库存信息（已弃用，请使用景区库存管理）
// @deprecated 此接口已被弃用，库存管理现在通过景区库存实现
export interface TicketStock {
  id: string
  productId: string
  stockDate: string
  totalQty: number
  soldQty: number
  lockVersion: number
}

// 门票产品查询请求
export interface TicketProductQueryRequest {
  current?: number
  pageSize?: number
  spotId?: string
  spotName?: string
  productName?: string
  ticketType?: number
  saleStatus?: number
}

// 门票价格更新请求
export interface TicketPriceUpdateRequest {
  id: string
  priceRetail: number
  priceSettlement: number
}

// 门票产品更新请求
export interface TicketProductUpdateRequest {
  id: string
  spotId?: number
  productName?: string
  ticketType?: number
  priceRetail?: number
  priceSettlement?: number
  saleStatus?: number
}

// 门票库存查询请求（已弃用）
// @deprecated 此接口已被弃用，库存管理现在通过景区库存实现
export interface TicketStockQueryRequest {
  productId: string
  startDate?: string
  endDate?: string
}

// 门票库存更新请求（已弃用）
// @deprecated 此接口已被弃用，库存管理现在通过景区库存实现
export interface TicketStockUpdateRequest {
  productId: string
  stockDate: string
  totalQty: number
}

// 批量库存更新请求（已弃用）
// @deprecated 此接口已被弃用，库存管理现在通过景区库存实现
export interface BatchTicketStockUpdateRequest {
  productId: string
  stocks: Array<{
    stockDate: string
    totalQty: number
  }>
}

// 景区门票汇总信息
export interface ScenicSpotTicketsVO {
  scenicSpot: ScenicSpot
  ticketProducts: TicketProductVO[]
  futureStockSummary: {
    totalProducts: number
    daysWithStock: number
    totalStockQty: number
    soldOutDays: number
  }
}

// 控制台统计数据
export interface DashboardStatsVO {
  totalUsers: string
  totalScenicSpots: string
  totalDistributors: string
  approvedDistributors: string
  pendingDistributors: string
  totalRevenue: string
  todayNewUsers: string
  todayRevenue: string
  weekNewUsers: string
  weekRevenue: string
  monthNewUsers: string
  monthRevenue: string
}

// 收入统计数据
export interface RevenueStatsVO {
  dateOrderCounts: DateOrderCountVO[]
  dateRevenues: DateRevenueVO[]
  scenicRevenues: ScenicRevenueVO[]
}

export interface DateOrderCountVO {
  date: string
  orderCount: string
}

export interface DateRevenueVO {
  date: string
  revenue: string
}

export interface ScenicRevenueVO {
  scenicId: string
  scenicName: string
  totalRevenue: string
  orderCount: string
  percentage: string
}
