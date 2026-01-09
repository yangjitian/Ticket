// 用户端相关类型定义

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

// 景区查询请求
export interface ScenicSpotQueryRequest {
  current?: number
  pageSize?: number
  spotName?: string
  level?: string
  province?: string
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

// 景区库存
export interface ScenicStock {
  id: string
  scenicId: string
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

// 订单信息（兼容旧版本）
export interface OrderInfo {
  id: string
  orderSn: string
  buyerId?: string
  productId?: string
  spotName?: string
  spotImage?: string
  visitDate: string
  ticketType?: number
  ticketTypeName?: string
  buyNum: number
  unitPrice: number
  settlementUnitPrice?: number
  commissionAmount?: number
  payAmount: number
  orderStatus: number
  orderStatusName?: string
  verificationCode?: string
  paymentTime?: string
  verifyTime?: string
  createTime: string
  expireTime?: string
  updateTime?: string
  visitors?: OrderVisitorVO[]
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

// 用户游客信息
export interface UserVisitor {
  id: string
  userId: string
  visitorName: string
  idCard: string
  contactTel?: string
  defaultTicketType: number
  ticketTypeName: string
  isDefault: number
  createTime: string
  updateTime: string
}

// 用户游客信息添加请求
export interface UserVisitorAddRequest {
  visitorName: string
  idCard: string
  contactTel?: string
  defaultTicketType: number
  isDefault: number
}

// 用户游客信息更新请求
export interface UserVisitorUpdateRequest {
  id: string
  visitorName: string
  idCard: string
  contactTel?: string
  defaultTicketType: number
  isDefault: number
}

// 购票信息视图对象
export interface BookTicketVO {
  spotId: string
  spotName: string
  spotImage?: string
  visitDate: string
  ticketType: number
  ticketTypeName: string
  unitPrice: number
  quantity?: number
  totalAmount?: number
  availableVisitors: UserVisitorVO[]
  selectedVisitors?: UserVisitorVO[]
}

// 用户游客信息视图对象
export interface UserVisitorVO {
  id: string
  userId: string
  visitorName: string
  idCard: string
  contactTel?: string
  defaultTicketType: number
  ticketTypeName?: string
  isDefault: number
  createTime: string
  updateTime: string
}

// 订单视图对象
export interface OrderVO {
  id: string
  orderSn: string
  spotName: string
  spotImage?: string
  visitDate: string
  ticketType: number
  ticketTypeName: string
  buyNum: number
  unitPrice: number
  payAmount: number
  orderStatus: number
  orderStatusName?: string
  verificationCode?: string
  paymentTime?: string
  verifyTime?: string
  createTime: string
  expireTime?: string
  visitors: OrderVisitorVO[]
}

// 订单游客信息视图对象
export interface OrderVisitorVO {
  visitorName: string
  idCard: string
  contactTel?: string
  verifyStatus?: number
  verifyTime?: string
}

// 购票请求DTO
export interface BookTicketRequest {
  spotId: string
  ticketType: number
  visitDate: string
  visitorIds: string[]
  distributorId?: string
}

// 支付订单请求DTO
export interface PayOrderRequest {
  orderId: string
  payType: number
}

// 核销订单请求DTO
export interface VerifyOrderRequest {
  verificationCode: string
  operatorId?: string
}
