// 用户端相关类型定义

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

// 景区查询请求
export interface ScenicSpotQueryRequest {
  current?: number
  pageSize?: number
  spotName?: string
  address?: string
  spotStatus?: number
  search?: string
}

// 景区查询响应
export interface ScenicSpotPageResponse {
  records: ScenicSpot[]
  total: number
  current: number
  size: number
  pages: number
}

// 门票产品
export interface TicketProduct {
  id: string
  spotId: string
  productName: string
  ticketType: number // 1-成人, 2-儿童, 3-老人, 4-学生, 5-其他
  priceRetail: number
  priceSettlement: number
  defaultDistSalePrice?: number
  saleStatus: number
}

// 门票库存
export interface TicketStock {
  id: string
  productId: string
  stockDate: string
  totalQty: number
  soldQty: number
  lockVersion: number
}

// 日历库存信息
export interface CalendarStockInfo {
  date: string
  availableQty: number
  price: number
  isAvailable: boolean
}

// 订单信息
export interface OrderInfo {
  id: string
  orderSn: string
  buyerId: string
  productId: string
  visitDate: string
  buyNum: number
  unitPrice: number
  settlementUnitPrice: number
  commissionAmount: number
  payAmount: number
  orderStatus: number
  verificationCode?: string
  paymentTime?: string
  verifyTime?: string
  createTime: string
  updateTime: string
}

// 游客信息
export interface VisitorInfo {
  id?: string
  orderId?: string
  visitorName: string
  idCard: string
  contactTel?: string
}

// 创建订单请求
export interface CreateOrderRequest {
  productId: string
  visitDate: string
  buyNum: number
  visitors: VisitorInfo[]
}

// 订单查询请求
export interface OrderQueryRequest {
  current?: number
  pageSize?: number
  orderStatus?: number
}

// 用户信息
export interface UserInfo {
  id: string
  userAccount: string
  nickname: string
  avatar?: string
  role: number
  userStatus: number
  auditStatus: number
  createTime: string
  updateTime: string
}

// 登录请求
export interface LoginRequest {
  userAccount: string
  password: string
}

// 登录响应
export interface LoginResponse {
  token: string
  user: UserInfo
  role: string
}
