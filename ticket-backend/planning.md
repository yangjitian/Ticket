# 🎟️ 旅游门票分销管理系统 - 项目详细说明

## 【题目概述】

在当今数字化快速发展的时代，旅游业也迎来了全面的数字化转型。为了更好地满足景区管理、提升游客体验以及优化销售策略，本项目精心打造了一款**旅游门票分销管理系统**。该系统基于电子商务、信息技术、数据管理和客户服务等多种技术和手段，为景区提供销售、营销、预订、门票管理、客户服务等全方位服务。

## 系统概述

**旅游门票分销管理系统**是专门针对旅游景区和门票销售开发的管理系统。通过整合旅游业中的食、住、行、娱、购、游等资源通道，建立一个内容丰富、多样化的资源共享平台。该系统旨在帮助景区与各类分销渠道如在线旅游平台、OTA、旅行社等进行合作，实现门票的分销、销售与管理，从而提高景区的曝光率和门票销售效率。

本项目是一个服务于**景区、分销商（旅行社/代理）和游客**的 B2B2C 旅游门票分销平台。系统解决了传统线下售票效率低、渠道管理混乱的问题，核心业务包含**票务管理、多级分销、库存并发控制、以及核销验票**。

## 【功能模块】

### 1. 用户管理系统
- **用户注册登录**：支持邮箱注册、登录，提供JWT无状态认证
- **角色管理**：管理员、分销商、游客三种角色权限控制
- **用户信息管理**：个人资料维护、头像上传、密码修改
- **分销商审核**：管理员审核分销商资格申请

### 2. 景区管理
- **景区信息管理**：景区基本信息、图片上传、状态管理
- **景区分类**：支持景区等级（如5A、4A）、省份等分类管理
- **景区展示**：前端展示景区详情、图片轮播等

### 3. 门票管理
- **门票产品设置**：门票名称、票种类型（成人/儿童/老人/学生）、价格设置
- **价格策略**：官网零售价、分销商结算价、默认销售价
- **上架管理**：门票的上架/下架状态控制
- **库存管理**：每日库存设置、实时库存监控

### 4. 分销渠道管理
- **分销商管理**：分销商注册审核、分销商等级管理（金/银/铜）
- **佣金体系**：基于分销商等级的佣金比例设置
- **分销数据统计**：分销商销售业绩、佣金统计
- **推广链接**：分销商生成专属推广链接

### 5. 订单管理
- **订单创建**：游客选购门票、填写游客信息、下单支付
- **订单状态流转**：待支付→已支付待核销→已核销→已取消→已退款→已过期
- **订单查询**：用户订单历史、分销商订单管理
- **订单统计**：销售数据统计、财务报表生成

### 6. 核销管理
- **电子票券**：生成核销码（景区简称-日期-随机码格式）
- **多渠道核销**：设备端、PU端、手机端等多种核销方式
- **核销验证**：订单状态校验、有效期验证
- **核销记录**：核销时间、核销人员记录

### 7. 财务管理
- **自动结算**：订单支付后自动计算分销商佣金
- **佣金记录**：详细的佣金流水记录
- **财务报表**：销售收入、分销商佣金、平台利润统计
- **数据导出**：支持报表导出功能

## 产品功能

### 核心业务流程

#### 1. 分销流程
管理员发布门票 (原价 ¥100) → 设置分销价 (¥80) → 分销商生成链接 → 游客购买 (¥100) → **分销商赚取差价 (¥20)**

#### 2. 高并发抢票
游客下单 → **Redis 预减库存 (Lua脚本)** → 发送消息队列 (可选) → 异步扣减 MySQL 库存 → 生成订单

#### 3. 核销流程
游客出示二维码 → 闸机/工作人员扫码 → 请求后端 verify 接口 → **校验状态与有效期** → 修改订单状态为"已使用"

## 系统特点

### 1. 多级分销体系
系统打破了传统的营销模式，实现了多级分销：
- **自主管理**：供应商可以自主管理分销平台，掌握核心利益
- **灵活配置**：根据自身需求配置分销渠道、价格和授信额度
- **等级体系**：金牌分销商（提成0.3倍官网价）、银牌（0.25倍）、铜牌（0.2倍）

### 2. 平台对接能力
系统能够快速对接OTA、旅行社和同业分销平台，为供应商和分销商提供高效便捷的在线供应链体验平台。

### 3. 票券核销创新
- **统一发码**：系统采用统一发码和统一验证的方式，实现了共享验证终端
- **多端验证**：设备端、PU端和手机端等多种验证渠道
- **灵活便捷**：使得票券核销更加灵活和便捷

### 4. 财务管理自动化
系统实现了自动化的财务管理功能：
- **自动统计**：自动统计销售数据和佣金结算
- **自动结算**：无需人工参与，自动生成报表
- **高效准确**：提高财务管理的效率和准确性

## 🖥️ 前端模块

### 前端技术栈

| 技术分类 | 具体技术 | 版本 | 说明 |
| --- | --- | --- | --- |
| **核心框架** | Vue.js | 3.5.25 | 渐进式JavaScript框架，组合式API |
| **开发语言** | TypeScript | ~5.9.0 | 类型安全的JavaScript超集 |
| **UI组件库** | Element Plus | ^2.13.0 | 基于Vue 3的企业级组件库 |
| **状态管理** | Pinia | ^3.0.4 | Vue 3官方推荐的状态管理库 |
| **路由管理** | Vue Router | ^4.6.3 | Vue 3官方路由管理器 |
| **HTTP客户端** | Axios | ^1.13.2 | 基于Promise的HTTP库 |
| **图表组件** | ECharts | ^6.0.0 | 百度开源的JavaScript图表库 |
| **构建工具** | Vite | ^7.2.4 | 下一代前端构建工具 |
| **代码质量** | ESLint | ^9.39.1 | JavaScript代码检查工具 |
| **格式化** | Prettier | 3.6.2 | 代码格式化工具 |

### 前端架构设计

```
ticket-fronted/
├── public/                 # 静态资源目录
│   └── favicon.ico        # 网站图标
├── src/                   # 源代码目录
│   ├── admin/            # 管理员模块
│   │   ├── api.ts        # 管理员API接口
│   │   └── views/        # 管理员页面
│   ├── user/             # 用户模块
│   │   ├── api.ts        # 用户API接口
│   │   └── views/        # 用户页面
│   ├── components/       # 公共组件
│   ├── router/           # 路由配置
│   ├── stores/           # Pinia状态管理
│   ├── types/            # TypeScript类型定义
│   ├── utils/            # 工具函数
│   ├── App.vue           # 根组件
│   └── main.ts           # 入口文件
├── dist/                 # 构建输出目录
├── node_modules/         # 依赖包
└── package.json          # 项目配置
```

### 前端功能模块

#### 1. 用户端功能 (User Module)

##### 游客首页 (Home.vue)
- **景区列表展示**：分页加载景区信息，支持按省份/等级筛选
- **景区详情页**：景区介绍、图片轮播、门票价格展示
- **门票预订流程**：
  - 选择游玩日期和票种
  - 填写游客信息（实名制）
  - 选择分销商渠道（可选）
  - 结算价格计算

##### 用户中心 (UserProfile.vue, UserOrders.vue)
- **个人信息管理**：头像上传、昵称修改、密码更换
- **常用游客管理**：添加/编辑常用出行人信息
- **订单管理**：订单历史查询、订单状态跟踪、退款申请

##### 购物流程 (Checkout.vue, PurchaseFlow.vue)
- **购物车管理**：添加门票、修改数量、删除商品
- **订单确认**：展示订单详情、价格明细、优惠信息
- **支付集成**：模拟支付流程（实际项目可集成支付宝/微信支付）

#### 2. 管理员端功能 (Admin Module)

##### 仪表板 (Dashboard.vue)
- **数据统计图表**：使用ECharts展示销售数据、用户增长、订单趋势
- **实时监控**：在线用户数、今日销售额、库存预警
- **快捷操作**：快速添加景区、审核分销商、处理订单

##### 用户管理 (UserManagement.vue)
- **用户列表**：分页展示所有用户，支持按角色筛选
- **用户详情**：查看用户基本信息、订单历史、登录记录
- **用户操作**：禁用/启用账户、修改角色、重置密码

##### 分销商管理 (DistributorManagement.vue)
- **分销商审核**：待审核分销商列表，审核通过/拒绝
- **分销商统计**：各分销商销售业绩、佣金统计、等级管理
- **分销商详情**：查看分销商信息、推广链接、佣金记录

##### 景区管理 (ScenicManagement.vue)
- **景区CRUD**：添加/编辑/删除景区信息
- **图片管理**：景区图片上传、排序、删除
- **景区状态**：开放/关闭景区，设置景区等级

##### 门票库存管理 (TicketInventory.vue)
- **库存设置**：按日期设置各景区每日库存
- **库存监控**：实时库存余量、销量统计、缺货预警
- **价格管理**：设置官网价、分销商结算价、促销价格

### 前端技术特性

#### 1. 响应式设计
- **移动端适配**：支持手机、平板、PC多端访问
- **自适应布局**：Element Plus响应式栅格系统
- **触摸友好**：移动端手势操作支持

#### 2. 用户体验优化
- **加载状态**：页面加载动画、按钮loading状态
- **错误处理**：网络错误重试、表单验证提示
- **缓存策略**：本地存储用户偏好设置、搜索历史

#### 3. 开发体验
- **热重载**：Vite开发服务器支持热更新
- **类型安全**：TypeScript严格类型检查
- **代码规范**：ESLint + Prettier保证代码质量

## 🚀 后端模块

### 后端技术栈

| 技术分类 | 具体技术 | 版本 | 说明 |
| --- | --- | --- | --- |
| **开发语言** | Java | 21 LTS | 最新长期支持版本，Record、模式匹配等特性 |
| **核心框架** | Spring Boot | 3.2.4 | 自动配置，原生支持Jakarta EE |
| **Web框架** | Spring MVC | - | RESTful API开发 |
| **安全框架** | Spring Security | - | 认证授权框架 |
| **数据库** | MySQL | 8.0 | 关系型数据库 |
| **缓存** | Redis | 7.x | 高性能键值存储 |
| **ORM框架** | MyBatis Plus | 3.5.5 | MyBatis增强工具 |
| **API文档** | Knife4j | 4.5.0 | 基于Swagger的增强UI |
| **工具库** | Hutool | - | Java工具类库 |
| **代码简化** | Lombok | - | 自动生成getter/setter等 |
| **连接池** | Druid | - | 阿里开源数据库连接池 |

### 后端架构设计

```
ticket-backend/
├── src/main/java/com/yudi/ticket/
│   ├── common/           # 公共模块
│   │   ├── BaseResponse.java    # 统一响应封装
│   │   ├── ErrorCode.java       # 错误码枚举
│   │   ├── PageResponse.java    # 分页响应
│   │   └── ThrowUtils.java      # 异常工具类
│   ├── config/           # 配置模块
│   │   ├── CosClientConfig.java # COS配置
│   │   ├── RedisConfig.java     # Redis配置
│   │   ├── SecurityConfig.java  # 安全配置
│   │   └── WebConfig.java       # Web配置
│   ├── controller/       # 控制器层
│   │   ├── UserController.java      # 用户接口
│   │   ├── OrderController.java     # 订单接口
│   │   ├── ScenicSpotController.java # 景区接口
│   │   ├── TicketProductController.java # 门票接口
│   │   └── ScenicStockController.java   # 库存接口
│   ├── service/          # 业务逻辑层
│   │   ├── impl/         # 业务实现类
│   │   └── interfaces    # 业务接口
│   ├── mapper/           # 数据访问层
│   │   ├── UserMapper.java
│   │   ├── OrderMapper.java
│   │   ├── ScenicSpotMapper.java
│   │   └── TicketProductMapper.java
│   ├── entity/           # 实体层
│   │   ├── dto/          # 数据传输对象
│   │   ├── vo/           # 视图对象
│   │   └── po/           # 持久化对象
│   ├── exception/        # 异常处理
│   │   ├── BusinessException.java
│   │   └── GlobalExceptionHandler.java
│   ├── utils/            # 工具类
│   │   ├── JwtUtils.java # JWT工具
│   │   └── UsernameGenerator.java # 用户名生成器
│   ├── aop/              # 切面编程
│   │   ├── RequireLogin.java     # 登录校验注解
│   │   ├── RequireAdmin.java     # 管理员权限注解
│   │   └── PermissionAspect.java # 权限切面
│   └── manager/          # 通用业务层
│       └── CosManager.java       # 文件管理
├── src/main/resources/
│   ├── mapper/           # MyBatis XML映射
│   ├── application.yml   # 主配置文件
│   ├── application-local.yml # 本地配置
│   └── static/           # 静态资源
├── sql/                 # SQL脚本
└── pom.xml              # Maven配置
```

### 后端核心功能

#### 1. 用户认证与授权

##### JWT认证机制
```java
// JWT工具类核心功能
public class JwtUtils {
    // 生成Token
    public static String generateToken(Long userId, String username, Integer role);
    // 验证Token
    public static boolean validateToken(String token);
    // 解析用户ID
    public static Long getUserId(String token);
    // Token黑名单管理
    public static void addToBlacklist(String token);
}
```

##### 权限控制
- **基于注解的权限控制**：`@RequireLogin`、`@RequireAdmin`
- **AOP切面实现**：权限校验、日志记录
- **RBAC模型**：角色-权限关联管理

#### 2. 订单处理系统

##### 高并发库存管理
```java
@Service
public class ScenicStockServiceImpl {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // Redis预减库存（Lua脚本保证原子性）
    public boolean preReduceStock(Long scenicId, LocalDate visitDate, int quantity) {
        String key = "stock:" + scenicId + ":" + visitDate;
        String luaScript = "if redis.call('get', KEYS[1]) >= ARGV[1] then " +
                          "redis.call('decrby', KEYS[1], ARGV[1]); return 1; " +
                          "else return 0; end";
        return redisTemplate.execute(new DefaultRedisScript<>(luaScript, Long.class),
                                   Collections.singletonList(key),
                                   String.valueOf(quantity)) == 1;
    }
}
```

##### 订单状态机
- **待支付** → **已支付待核销** → **已核销** → **已取消** → **已退款** → **已过期**
- **状态流转控制**：防止非法状态跳转
- **异步状态更新**：消息队列处理状态变更

#### 3. 分销佣金系统

##### 佣金计算引擎
```java
public class CommissionCalculator {
    // 根据分销商等级计算佣金
    public BigDecimal calculateCommission(Order order, Distributor distributor) {
        BigDecimal basePrice = order.getSettlementUnitPrice();
        BigDecimal rate = getCommissionRate(distributor.getLevel());
        return basePrice.multiply(rate).multiply(new BigDecimal(order.getBuyNum()));
    }

    private BigDecimal getCommissionRate(Integer level) {
        return switch (level) {
            case 1 -> new BigDecimal("0.30"); // 金牌
            case 2 -> new BigDecimal("0.25"); // 银牌
            case 3 -> new BigDecimal("0.20"); // 铜牌
            default -> BigDecimal.ZERO;
        };
    }
}
```

##### 自动佣金结算
- **订单支付成功触发**：自动计算并记录佣金
- **佣金流水记录**：详细记录每笔佣金的来源和金额
- **分销商余额管理**：实时更新分销商可提现金额

### 后端安全特性

#### 1. 身份认证安全
- **密码加密**：BCrypt强加密算法
- **JWT Token管理**：自动过期、黑名单机制
- **会话管理**：分布式Session管理

#### 2. 接口安全防护
- **参数校验**：JSR-303 Bean Validation
- **SQL注入防护**：预编译SQL语句
- **XSS防护**：输入过滤和转义
- **CSRF防护**：Token验证机制

#### 3. 数据安全
- **敏感数据加密**：身份证号等敏感信息加密存储
- **数据脱敏**：返回数据时对敏感信息进行脱敏处理
- **审计日志**：关键操作的完整审计记录

### 后端性能优化

#### 1. 缓存策略
- **多级缓存**：本地缓存 + Redis分布式缓存
- **缓存预热**：系统启动时预加载热点数据
- **缓存更新**：主动更新 + 过期淘汰双重策略

#### 2. 数据库优化
- **读写分离**：主库写、从库读
- **分表分库**：按时间/地区分表
- **索引优化**：复合索引、覆盖索引
- **连接池调优**：Druid连接池参数优化

#### 3. 并发处理
- **分布式锁**：Redisson实现分布式锁
- **限流熔断**：Sentinel流量控制
- **异步处理**：线程池 + 消息队列

## 🗄️ 数据库设计模块

### 数据库架构设计

#### 总体架构
```
旅游门票分销数据库 (tourism_distribution_db)
├── 用户体系表群
│   ├── user - 用户基础表
│   └── user_visitor - 用户常用游客信息表
├── 产品体系表群
│   ├── bus_scenic_spot - 景区信息表
│   ├── bus_ticket_product - 门票产品表
│   └── bus_scenic_stock - 景区日历库存表
├── 订单体系表群
│   ├── bus_order - 订单主表
│   ├── bus_order_visitor - 订单游客实名表
│   └── bus_commission_log - 佣金记录流水表
└── 系统配置表群 (预留扩展)
```

#### 数据库特性
- **字符集**：utf8mb4 (支持Emoji表情)
- **引擎选择**：InnoDB (支持事务、行级锁、外键)
- **主键策略**：自增ID + 唯一索引保证数据唯一性
- **分表策略**：按时间范围分表（订单表、佣金表）

### 核心数据表详细设计

#### 1. 用户表 (user)

```sql
CREATE TABLE `user` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_account` varchar(100) NOT NULL COMMENT '登录邮箱(账号)',
    `password` varchar(100) NOT NULL COMMENT '加密存储的密码',
    `nickname` varchar(64) DEFAULT '新用户' COMMENT '用户昵称',
    `avatar` varchar(255) DEFAULT NULL COMMENT '用户头像地址(URL)',
    `role` tinyint NOT NULL DEFAULT '1' COMMENT '角色: 0-管理员, 1-游客, 2-分销商',
    `distributor_level` tinyint DEFAULT '3' COMMENT '分销商等级: 1-金, 2-银, 3-铜',
    `user_status` tinyint DEFAULT '1' COMMENT '账号状态: 0-禁用, 1-正常',
    `audit_status` tinyint DEFAULT '0' COMMENT '分销商审核状态: 0-待审核, 1-通过, 2-拒绝',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_account` (`user_account`),
    INDEX `idx_role` (`role`),
    INDEX `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户基础表';
```

**索引设计**：
- 主键索引：`id`
- 唯一索引：`user_account` (账号唯一性)
- 普通索引：`role` (按角色查询)、`audit_status` (分销商审核查询)

#### 2. 景区信息表 (bus_scenic_spot)

```sql
CREATE TABLE `bus_scenic_spot` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `spot_name` varchar(100) NOT NULL COMMENT '景区名称',
    `level` varchar(20) DEFAULT NULL COMMENT '景区等级，如：5A、4A、国家级等',
    `province` varchar(50) DEFAULT NULL COMMENT '景区所属省份',
    `description` text COMMENT '详情介绍',
    `address` varchar(255) DEFAULT NULL COMMENT '地址',
    `open_time` varchar(100) DEFAULT NULL COMMENT '开放时间说明',
    `contact_num` varchar(20) DEFAULT NULL COMMENT '联系电话',
    `spot_status` tinyint DEFAULT '1' COMMENT '状态: 1-开放, 0-关闭',
    `image_urls` text COMMENT '景区图片URL数组(JSON格式)',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` tinyint DEFAULT '0',
    PRIMARY KEY (`id`),
    INDEX `idx_province` (`province`),
    INDEX `idx_level` (`level`),
    INDEX `idx_status` (`spot_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务-景区信息表';
```

**字段说明**：
- `image_urls`：JSON数组格式存储多张图片URL
- `level`：景区等级（5A/4A/3A等）
- `province`：省份信息，便于区域筛选

#### 3. 门票产品表 (bus_ticket_product)

```sql
CREATE TABLE `bus_ticket_product` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `spot_id` bigint NOT NULL COMMENT '所属景区ID',
    `product_name` varchar(100) NOT NULL COMMENT '产品名称',
    `ticket_type` tinyint NOT NULL COMMENT '票种类型: 1-成人, 2-儿童, 3-老人, 4-学生, 5-其他',
    `price_retail` decimal(10,2) NOT NULL COMMENT '官网指导零售价',
    `price_settlement` decimal(10,2) NOT NULL COMMENT '平台结算价',
    `default_dist_sale_price` decimal(10,2) DEFAULT NULL COMMENT '分销商默认销售价',
    `sale_status` tinyint DEFAULT '1' COMMENT '上架状态: 1-上架, 0-下架',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` tinyint DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `idx_spot_id` (`spot_id`),
    KEY `idx_ticket_type` (`ticket_type`),
    KEY `idx_sale_status` (`sale_status`),
    FOREIGN KEY (`spot_id`) REFERENCES `bus_scenic_spot` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务-门票产品表';
```

**业务规则**：
- `price_settlement ≤ price_retail`：结算价不能高于零售价
- `default_dist_sale_price`：为空时使用`price_retail`

#### 4. 景区日历库存表 (bus_scenic_stock)

```sql
CREATE TABLE `bus_scenic_stock` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `scenic_id` bigint NOT NULL COMMENT '关联景区ID',
    `stock_date` date NOT NULL COMMENT '库存日期',
    `total_qty` int NOT NULL DEFAULT '0' COMMENT '总库存量',
    `sold_qty` int NOT NULL DEFAULT '0' COMMENT '已售数量',
    `lock_version` int DEFAULT '0' COMMENT '乐观锁版本',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_scenic_date` (`scenic_id`, `stock_date`),
    KEY `idx_scenic_id` (`scenic_id`),
    KEY `idx_stock_date` (`stock_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务-景区日历库存表';
```

**核心特性**：
- **唯一约束**：`(scenic_id, stock_date)`保证同一天一个景区只有一条记录
- **乐观锁**：`lock_version`字段防止并发更新冲突
- **库存计算**：可用库存 = `total_qty - sold_qty`

#### 5. 订单主表 (bus_order)

```sql
CREATE TABLE `bus_order` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `order_sn` varchar(64) NOT NULL COMMENT '系统订单号',
    `buyer_id` bigint NOT NULL COMMENT '下单用户ID',
    `distributor_id` bigint DEFAULT NULL COMMENT '关联分销商ID',
    `product_id` bigint NOT NULL COMMENT '产品ID',
    `visit_date` date NOT NULL COMMENT '游玩日期',
    `buy_num` int NOT NULL COMMENT '购买数量',
    `unit_price` decimal(10,2) NOT NULL COMMENT '实际销售单价',
    `settlement_unit_price` decimal(10,2) NOT NULL COMMENT '平台结算单价',
    `commission_amount` decimal(10,2) DEFAULT '0.00' COMMENT '分销商佣金总额',
    `pay_amount` decimal(10,2) NOT NULL COMMENT '实付总额',
    `order_status` tinyint DEFAULT '0' COMMENT '订单状态',
    `verification_code` varchar(64) DEFAULT NULL COMMENT '核销码',
    `payment_time` datetime DEFAULT NULL COMMENT '支付完成时间',
    `verify_time` datetime DEFAULT NULL COMMENT '实际核销时间',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` tinyint DEFAULT '0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_sn` (`order_sn`),
    KEY `idx_buyer` (`buyer_id`),
    KEY `idx_distributor` (`distributor_id`),
    KEY `idx_verification` (`verification_code`),
    KEY `idx_visit_date` (`visit_date`),
    KEY `idx_status` (`order_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务-订单主表';
```

**订单号生成规则**：
- 格式：`T + 时间戳(13位) + 随机数(4位)`
- 示例：`T17040672000001234`

**核销码生成规则**：
- 格式：`景区简称-YYYYMMDD-随机码`
- 示例：`HZWC-20231231-ABCD`

#### 6. 订单游客实名表 (bus_order_visitor)

```sql
CREATE TABLE `bus_order_visitor` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `order_id` bigint NOT NULL,
    `visitor_name` varchar(32) NOT NULL,
    `id_card` varchar(32) NOT NULL COMMENT '身份证号（建议加密存储）',
    `contact_tel` varchar(20) DEFAULT NULL,
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务-订单游客实名表';
```

**数据安全**：
- `id_card`字段建议使用AES加密存储
- 敏感信息在返回前端时进行脱敏处理

#### 7. 佣金记录流水表 (bus_commission_log)

```sql
CREATE TABLE `bus_commission_log` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `distributor_id` bigint NOT NULL,
    `order_id` bigint NOT NULL,
    `profit_amount` decimal(10,2) NOT NULL COMMENT '结算佣金',
    `log_remark` varchar(255) DEFAULT '门票销售佣金',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_distributor_id` (`distributor_id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务-佣金记录流水表';
```

**佣金计算逻辑**：
```java
// 佣金 = (平台结算价 × 购买数量) × 佣金比例
commission = (settlementUnitPrice × buyNum) × commissionRate
```

#### 8. 用户常用游客信息表 (user_visitor)

```sql
CREATE TABLE `user_visitor` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` bigint NOT NULL COMMENT '所属用户ID',
    `visitor_name` varchar(32) NOT NULL COMMENT '游客姓名',
    `id_card` varchar(32) NOT NULL COMMENT '身份证号',
    `contact_tel` varchar(20) DEFAULT NULL COMMENT '联系电话',
    `default_ticket_type` tinyint DEFAULT '1' COMMENT '默认票种类型',
    `is_default` tinyint DEFAULT '0' COMMENT '是否默认出行人',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_is_default` (`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户常用游客信息表';
```

### 数据库优化策略

#### 1. 索引优化
- **主键索引**：所有表都有自增主键
- **唯一索引**：保证业务唯一性约束
- **复合索引**：多字段查询优化
- **覆盖索引**：减少回表查询

#### 2. 分表策略
- **订单表分表**：按月份分表（bus_order_202401, bus_order_202402...）
- **佣金表分表**：按季度分表
- **库存表分表**：按景区ID哈希分表

#### 3. 数据归档
- **历史数据归档**：3个月前的订单数据归档到历史库
- **冷热数据分离**：热点数据保留在SSD，冷数据存储在HDD
- **数据压缩**：历史数据压缩存储

#### 4. 备份策略
- **全量备份**：每天凌晨2点全库备份
- **增量备份**：每小时增量备份
- **异地容灾**：跨机房数据同步

## 🎯 项目特色

1. **真实的商业场景**：基于实际旅游行业需求设计
2. **完整的业务闭环**：从景区管理到游客核销的全流程
3. **高并发处理**：Redis + Lua脚本处理抢票场景
4. **多角色协作**：管理员、分销商、游客三方协同
5. **现代化技术栈**：Java 21 + Spring Boot 3 + Vue 3 + TypeScript
6. **企业级架构**：DDD分层架构，易于扩展维护
7. **数据安全**：多重安全防护，敏感数据加密
8. **性能优化**：缓存、索引、分表等多维度优化

---

**项目状态：** 开发中 (v0.0.1) | **适用场景：** 高等院校毕业设计，企业级旅游系统参考

---

**项目状态：** 开发中 (v0.0.1) | **适用场景：** 高等院校毕业设计，企业级旅游系统参考