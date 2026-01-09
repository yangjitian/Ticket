package com.yudi.ticket.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yudi.ticket.common.ErrorCode;
import com.yudi.ticket.exception.BusinessException;
import com.yudi.ticket.mapper.OrderMapper;
import com.yudi.ticket.mapper.OrderVisitorMapper;
import com.yudi.ticket.mapper.ScenicSpotMapper;
import com.yudi.ticket.mapper.ScenicStockMapper;
import com.yudi.ticket.mapper.TicketProductMapper;
import com.yudi.ticket.mapper.UserMapper;
import com.yudi.ticket.mapper.UserVisitorMapper;
import com.yudi.ticket.model.dto.BookTicketRequest;
import com.yudi.ticket.model.dto.CreateOrderRequest;
import com.yudi.ticket.model.dto.OrderQueryRequest;
import com.yudi.ticket.model.dto.PayOrderRequest;
import com.yudi.ticket.model.dto.VerifyOrderRequest;
import com.yudi.ticket.model.entity.Order;
import com.yudi.ticket.model.entity.OrderVisitor;
import com.yudi.ticket.model.entity.ScenicSpot;
import com.yudi.ticket.model.entity.ScenicStock;
import com.yudi.ticket.model.entity.TicketProduct;
import com.yudi.ticket.model.entity.User;
import com.yudi.ticket.model.entity.UserVisitor;
import com.yudi.ticket.model.vo.BookTicketVO;
import com.yudi.ticket.model.vo.OrderVO;
import com.yudi.ticket.model.vo.OrderVisitorVO;
import com.yudi.ticket.model.vo.UserVisitorVO;
import com.yudi.ticket.service.OrderService;
import com.yudi.ticket.service.OrderVisitorService;
import com.yudi.ticket.service.ScenicStockService;
import com.yudi.ticket.service.UserVisitorService;
import com.yudi.ticket.utils.OrderUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
* @author Asus
* @description 针对表【bus_order(业务-订单主表)】的数据库操作Service实现
* @createDate 2026-01-05 15:53:49
*/
@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order>
    implements OrderService{

    @Resource
    private UserMapper userMapper;

    @Resource
    private TicketProductMapper ticketProductMapper;

    @Resource
    private ScenicSpotMapper scenicSpotMapper;

    @Resource
    private ScenicStockMapper scenicStockMapper;

    @Resource
    private UserVisitorMapper userVisitorMapper;

    @Resource
    private OrderVisitorMapper orderVisitorMapper;

    @Resource
    private UserVisitorService userVisitorService;

    @Resource
    private OrderVisitorService orderVisitorService;

    @Resource
    private ScenicStockService scenicStockService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 创建订单
     */
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(CreateOrderRequest request) {
        // 参数校验
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请求参数不能为空");
        }
        if (request.getProductId() == null || request.getBuyNum() == null || request.getBuyNum() <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "产品信息或购买数量有误");
        }

        // 校验用户是否存在
        User user = userMapper.selectById(request.getBuyerId());
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        // 校验产品是否存在
        TicketProduct product = ticketProductMapper.selectById(request.getProductId());
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "门票产品不存在");
        }

        // 校验购买数量不超过限制
        if (request.getBuyNum() > 10) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "单次最多购买10张门票");
        }

        // 生成订单号
        String orderSn = generateOrderSn();

        // 生成核销码
        String verificationCode = generateVerificationCode(product.getSpotId(), request.getVisitDate());

        // 创建订单
        Order order = new Order();
        order.setOrderSn(orderSn);
        order.setBuyerId(request.getBuyerId());
        order.setDistributorId(request.getDistributorId());
        order.setProductId(request.getProductId());
        order.setVisitDate(request.getVisitDate());
        order.setBuyNum(request.getBuyNum());
        order.setUnitPrice(product.getPriceRetail());
        order.setSettlementUnitPrice(product.getPriceSettlement());
        order.setCommissionAmount(product.getPriceRetail().subtract(product.getPriceSettlement()).multiply(BigDecimal.valueOf(request.getBuyNum())));
        order.setPayAmount(product.getPriceRetail().multiply(BigDecimal.valueOf(request.getBuyNum())));
        order.setOrderStatus(0); // 待支付
        order.setVerificationCode(verificationCode);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        order.setIsDeleted(0);

        this.save(order);

        log.info("订单创建成功 - 订单号: {}, 用户ID: {}, 产品ID: {}", orderSn, request.getBuyerId(), request.getProductId());
        return order;
    }

    /**
     * 分页查询用户订单
     */
    public IPage<Order> queryUserOrders(OrderQueryRequest request) {
        if (request == null || request.getBuyerId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户ID不能为空");
        }

        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getBuyerId, request.getBuyerId())
            .eq(Order::getIsDeleted, 0)
            .orderByDesc(Order::getCreateTime);

        // 状态筛选
        if (request.getOrderStatus() != null) {
            queryWrapper.eq(Order::getOrderStatus, request.getOrderStatus());
        }

        Page<Order> page = new Page<>(request.getCurrent(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    /**
     * 分页查询用户订单详情（VO格式）
     */
    @Override
    public IPage<OrderVO> queryUserOrderVOs(OrderQueryRequest request, Long userId) {
        // 校验用户权限
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        // 查询用户订单
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getBuyerId, request.getBuyerId() != null ? request.getBuyerId() : userId)
            .eq(Order::getIsDeleted, 0)
            .orderByDesc(Order::getCreateTime);

        log.info("查询用户订单 - userId:{}, buyerId:{}, isDeleted=0", userId, request.getBuyerId());

        // 状态筛选
        if (request.getOrderStatus() != null) {
            queryWrapper.eq(Order::getOrderStatus, request.getOrderStatus());
        }

        Page<Order> page = new Page<>(request.getCurrent(), request.getPageSize());
        IPage<Order> orderPage = this.page(page, queryWrapper);

        // 转换为VO格式
        List<OrderVO> orderVOs = new ArrayList<>();
        for (Order order : orderPage.getRecords()) {
            try {
                OrderVO orderVO = getOrderDetail(order.getId(), userId);
                orderVOs.add(orderVO);
            } catch (Exception e) {
                log.error("转换订单VO失败 - 订单ID:{}, 错误:{}", order.getId(), e.getMessage());
                // 跳过有问题的订单，继续处理其他订单
            }
        }

        log.info("转换后VO数量:{}", orderVOs.size());

        // 创建新的分页结果
        Page<OrderVO> voPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        voPage.setRecords(orderVOs);
        voPage.setPages(orderPage.getPages());

        return voPage;
    }

    /**
     * 支付订单
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean payOrder(Long orderId, String paymentMethod) {
        if (orderId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "订单ID不能为空");
        }

        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }

        if (order.getOrderStatus() != 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "订单状态不允许支付");
        }

        // 更新订单状态
        order.setOrderStatus(1); // 已支付待核销
        order.setPaymentTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        this.updateById(order);

        log.info("订单支付成功 - 订单号: {}, 支付方式: {}", order.getOrderSn(), paymentMethod);
        return true;
    }

    /**
     * 取消订单
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Long orderId) {
        if (orderId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "订单ID不能为空");
        }

        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }

        if (order.getOrderStatus() != 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "订单状态不允许取消");
        }

        // 更新订单状态
        order.setOrderStatus(3); // 已取消
        order.setUpdateTime(LocalDateTime.now());

        this.updateById(order);

        // 回滚库存（Redis和MySQL）
        TicketProduct product = ticketProductMapper.selectById(order.getProductId());
        if (product != null) {
            restoreStockWithRedis(product.getSpotId(), order.getVisitDate(), order.getBuyNum());
        }

        log.info("订单取消成功 - 订单号: {}", order.getOrderSn());
        return true;
    }

    /**
     * 删除订单（逻辑删除）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteOrder(Long orderId, Long userId) {
        log.info("开始删除订单 - 订单ID:{}, 用户ID:{}", orderId, userId);

        if (orderId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "订单ID不能为空");
        }

        Order order = this.getById(orderId);
        if (order == null) {
            log.error("订单不存在 - 订单ID:{}", orderId);
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }

        log.info("找到订单 - 订单号:{}, 买家ID:{}, 订单状态:{}, isDeleted:{}",
                order.getOrderSn(), order.getBuyerId(), order.getOrderStatus(), order.getIsDeleted());

        // 校验订单归属
        if (!order.getBuyerId().equals(userId)) {
            log.error("无权删除此订单 - 订单买家ID:{}, 请求用户ID:{}", order.getBuyerId(), userId);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "无权删除此订单");
        }

        // 只有已取消、已退款、已过期、已核销的订单才能删除
        if (order.getOrderStatus() == 0) {
            log.error("待支付订单不能删除 - 订单状态:{}", order.getOrderStatus());
            throw new BusinessException(ErrorCode.PARAM_ERROR, "待支付订单不能删除，请先取消订单");
        }

        // 检查是否已经删除
        if (order.getIsDeleted() != null && order.getIsDeleted() == 1) {
            log.warn("订单已被删除 - 订单ID:{}", orderId);
            return true; // 已经删除了，返回成功
        }

        // 逻辑删除订单
        Integer oldIsDeleted = order.getIsDeleted();
        order.setIsDeleted(1);
        order.setUpdateTime(LocalDateTime.now());

        log.info("准备删除订单 - 订单ID:{}, 订单号:{}, isDeleted:{} -> 1", orderId, order.getOrderSn(), oldIsDeleted);

        // 执行更新
        LambdaUpdateWrapper<Order> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Order::getId, orderId)
                    .set(Order::getIsDeleted, 1)
                    .set(Order::getUpdateTime, LocalDateTime.now());

        boolean result = this.update(updateWrapper);

        if (result) {
            log.info("订单删除成功 - 订单号:{}", order.getOrderSn());

            // 验证更新结果
            Order updatedOrder = this.getById(orderId);
            if (updatedOrder != null) {
                log.info("验证更新结果 - 订单ID:{}, isDeleted:{}", orderId, updatedOrder.getIsDeleted());
            }
        } else {
            log.error("订单删除失败 - 订单ID:{}", orderId);
        }

        return result;
    }

    /**
     * 核销订单
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean verifyOrder(String verificationCode) {
        if (StrUtil.isBlank(verificationCode)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "核销码不能为空");
        }

        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getVerificationCode, verificationCode)
            .eq(Order::getOrderStatus, 1) // 已支付待核销
            .eq(Order::getIsDeleted, 0);

        Order order = this.getOne(queryWrapper);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "核销码无效或订单状态不允许核销");
        }

        // 更新订单状态
        order.setOrderStatus(2); // 已核销
        order.setVerifyTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        this.updateById(order);

        log.info("订单核销成功 - 订单号: {}, 核销码: {}", order.getOrderSn(), verificationCode);
        return true;
    }

    /**
     * 购票 - 创建订单（新版本）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO bookTicket(BookTicketRequest request, Long userId) {
        // 参数校验
        if (request == null || request.getSpotId() == null || request.getTicketType() == null ||
            request.getVisitDate() == null || CollUtil.isEmpty(request.getVisitorIds())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请求参数不完整");
        }

        // 校验用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        // 获取景区信息
        ScenicSpot scenicSpot = scenicSpotMapper.selectById(request.getSpotId());
        if (scenicSpot == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "景区不存在");
        }

        // 查找对应票种的产品
        LambdaQueryWrapper<TicketProduct> productQuery = new LambdaQueryWrapper<>();
        productQuery.eq(TicketProduct::getSpotId, request.getSpotId())
                   .eq(TicketProduct::getTicketType, request.getTicketType());
        TicketProduct product = ticketProductMapper.selectOne(productQuery);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "该票种暂未开放销售");
        }

        // 校验游客信息
        List<UserVisitor> visitors = userVisitorMapper.selectBatchIds(request.getVisitorIds());
        if (visitors.size() != request.getVisitorIds().size()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "部分游客信息不存在");
        }

        // 校验游客是否属于当前用户
        for (UserVisitor visitor : visitors) {
            if (!visitor.getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "无权使用该游客信息");
            }
        }

        int buyNum = visitors.size();

        // 校验购买数量
        if (buyNum > 10) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "单次最多购买10张门票");
        }

        // 校验库存并扣减（使用Redis Lua脚本）
        boolean stockDeducted = deductStockWithRedis(request.getSpotId(), request.getVisitDate(), buyNum);
        if (!stockDeducted) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "库存不足");
        }

        // 同步更新MySQL库存数据
        updateMySQLStock(request.getSpotId(), request.getVisitDate(), buyNum);

        try {
            // 生成订单号和核销码
            String orderSn = OrderUtils.generateOrderSn();
            String verificationCode = OrderUtils.generateVerificationCode(null, null);

            // 创建订单
            Order order = new Order();
            order.setOrderSn(orderSn);
            order.setBuyerId(userId);
            order.setDistributorId(request.getDistributorId());
            order.setProductId(product.getId());
            order.setVisitDate(request.getVisitDate());
            order.setBuyNum(buyNum);
            order.setUnitPrice(product.getPriceRetail());
            order.setSettlementUnitPrice(product.getPriceSettlement());
            order.setCommissionAmount(product.getPriceRetail().subtract(product.getPriceSettlement()).multiply(BigDecimal.valueOf(buyNum)));
            order.setPayAmount(product.getPriceRetail().multiply(BigDecimal.valueOf(buyNum)));
            order.setOrderStatus(0); // 待支付
            order.setVerificationCode(verificationCode);
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            order.setIsDeleted(0);

            this.save(order);

            // 保存订单游客信息
            List<OrderVisitor> orderVisitors = visitors.stream().map(visitor -> {
                OrderVisitor orderVisitor = new OrderVisitor();
                orderVisitor.setOrderId(order.getId());
                orderVisitor.setVisitorName(visitor.getVisitorName());
                orderVisitor.setIdCard(visitor.getIdCard());
                orderVisitor.setContactTel(visitor.getContactTel());
                return orderVisitor;
            }).collect(Collectors.toList());

            orderVisitorService.saveBatchOrderVisitors(orderVisitors);

            // 设置订单过期时间（15分钟）
            redisTemplate.expire("order:expire:" + order.getId(), 15, TimeUnit.MINUTES);

            log.info("购票订单创建成功 - 订单号: {}, 用户ID: {}, 景区: {}, 票种: {}, 数量: {}",
                    orderSn, userId, scenicSpot.getSpotName(), request.getTicketType(), buyNum);

            // 转换为VO返回
            return convertToOrderVO(order, scenicSpot, product, orderVisitors);

        } catch (Exception e) {
            // 回滚Redis库存（MySQL库存由Spring事务自动回滚）
            restoreStockWithRedisOnly(request.getSpotId(), request.getVisitDate(), buyNum);
            throw e;
        }
    }

    /**
     * 获取购票信息
     */
    @Override
    public BookTicketVO getBookTicketInfo(Long spotId, Integer ticketType, String visitDate, Long userId) {
        // 获取景区信息
        ScenicSpot scenicSpot = scenicSpotMapper.selectById(spotId);
        if (scenicSpot == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "景区不存在");
        }

        // 获取票种产品
        LambdaQueryWrapper<TicketProduct> productQuery = new LambdaQueryWrapper<>();
        productQuery.eq(TicketProduct::getSpotId, spotId)
                   .eq(TicketProduct::getTicketType, ticketType);
        TicketProduct product = ticketProductMapper.selectOne(productQuery);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "该票种暂未开放销售");
        }

        // 获取用户可选游客
        List<UserVisitorVO> availableVisitors = userVisitorService.listUserVisitors(userId);

        BookTicketVO vo = new BookTicketVO();
        vo.setSpotId(spotId);
        vo.setSpotName(scenicSpot.getSpotName());
        vo.setSpotImage(scenicSpot.getImageUrls() != null && scenicSpot.getImageUrls().length() > 0 ?
                       scenicSpot.getImageUrls().split(",")[0] : null);
        vo.setVisitDate(LocalDate.parse(visitDate));
        vo.setTicketType(ticketType);
        vo.setTicketTypeName(getTicketTypeName(ticketType));
        vo.setUnitPrice(product.getPriceRetail());
        vo.setAvailableVisitors(availableVisitors);
        vo.setSelectedVisitors(new ArrayList<>()); // 默认空选择

        return vo;
    }

    /**
     * 支付订单（新版本）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO payOrder(PayOrderRequest request, Long userId) {
        if (request == null || request.getOrderId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请求参数不能为空");
        }

        Order order = this.getById(request.getOrderId());
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }

        // 校验订单归属
        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "无权操作此订单");
        }

        if (order.getOrderStatus() != 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "订单状态不允许支付");
        }

        // 模拟支付成功（实际项目中这里应该调用第三方支付接口）
        log.info("模拟支付订单 - 订单号: {}, 支付方式: {}", order.getOrderSn(), request.getPayType());

        // 更新订单状态
        order.setOrderStatus(1); // 已支付待核销
        order.setPaymentTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        this.updateById(order);

        // 清除过期标记
        redisTemplate.delete("order:expire:" + order.getId());

        log.info("订单支付成功 - 订单号: {}", order.getOrderSn());

        // 返回完整的订单信息
        return getOrderDetail(order.getId(), userId);
    }

    /**
     * 查询用户订单详情
     */
    @Override
    public OrderVO getOrderDetail(Long orderId, Long userId) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }

        // 校验订单归属
        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "无权查看此订单");
        }

        // 获取相关信息
        ScenicSpot scenicSpot = scenicSpotMapper.selectById(
            ticketProductMapper.selectById(order.getProductId()).getSpotId()
        );
        TicketProduct product = ticketProductMapper.selectById(order.getProductId());
        List<OrderVisitor> orderVisitors = orderVisitorService.listByOrderId(orderId);

        return convertToOrderVO(order, scenicSpot, product, orderVisitors);
    }

    /**
     * 核销订单（新版本）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean verifyOrder(VerifyOrderRequest request) {
        if (request == null || StrUtil.isBlank(request.getVerificationCode())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "核销码不能为空");
        }

        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getVerificationCode, request.getVerificationCode())
                   .eq(Order::getOrderStatus, 1) // 已支付待核销
                   .eq(Order::getIsDeleted, 0);

        Order order = this.getOne(queryWrapper);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "核销码无效或订单状态不允许核销");
        }

        // 更新订单状态
        order.setOrderStatus(2); // 已核销
        order.setVerifyTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        this.updateById(order);

        log.info("订单核销成功 - 订单号: {}, 核销码: {}, 操作员: {}",
                order.getOrderSn(), request.getVerificationCode(), request.getOperatorId());
        return true;
    }

    /**
     * 处理过期订单
     */
    @Override
    public void processExpiredOrders() {
        // 查找所有待支付且超过15分钟的订单
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(15);

        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getOrderStatus, 0) // 待支付
                   .lt(Order::getCreateTime, expireTime)
                   .eq(Order::getIsDeleted, 0);

        List<Order> expiredOrders = this.list(queryWrapper);

        for (Order order : expiredOrders) {
            // 更新订单状态为已过期
            order.setOrderStatus(5); // 已过期
            order.setUpdateTime(LocalDateTime.now());
            this.updateById(order);

            // 回滚库存
            TicketProduct product = ticketProductMapper.selectById(order.getProductId());
            if (product != null) {
                restoreStockWithRedis(product.getSpotId(), order.getVisitDate(), order.getBuyNum());
            }

            log.info("订单已过期 - 订单号: {}", order.getOrderSn());
        }
    }

    /**
     * 使用Redis Lua脚本扣减库存
     */
    private boolean deductStockWithRedis(Long spotId, LocalDate visitDate, int quantity) {
        String key = "stock:" + spotId + ":" + visitDate.toString();

        // 先检查Redis中是否有库存数据，如果没有则从数据库初始化
        Object stockValue = redisTemplate.opsForValue().get(key);
        if (stockValue == null) {
            // 从数据库查询库存
            LambdaQueryWrapper<ScenicStock> stockQuery = new LambdaQueryWrapper<>();
            stockQuery.eq(ScenicStock::getScenicId, spotId)
                     .eq(ScenicStock::getStockDate, visitDate);
            ScenicStock scenicStock = scenicStockMapper.selectOne(stockQuery);

            if (scenicStock != null && scenicStock.getTotalQty() > 0) {
                // 初始化Redis库存 - 存储为整数
                redisTemplate.opsForValue().set(key, scenicStock.getTotalQty());
                log.info("初始化Redis库存 - 景区ID:{} 日期:{} 库存:{}", spotId, visitDate, scenicStock.getTotalQty());

                // 验证库存是否成功设置
                Object verifyStock = redisTemplate.opsForValue().get(key);
                if (verifyStock == null) {
                    log.error("Redis库存初始化失败 - key:{}", key);
                    return false;
                }
                log.info("Redis库存初始化验证成功 - key:{} value:{}", key, verifyStock);
            } else {
                log.warn("数据库中无库存数据 - 景区ID:{} 日期:{}", spotId, visitDate);
                return false;
            }
        }

        // 执行扣减操作 - Redis中存储整数，参数直接是数字
        String script = "local stock = redis.call('get', KEYS[1])\n" +
                       "if stock == nil then return 0 end\n" +
                       "local stock_num = stock\n" +  // Redis中已经是数字
                       "local qty_num = ARGV[1]\n" +  // 参数直接是数字
                       "if stock_num == nil then return -3 end\n" +
                       "if qty_num == nil then return -4 end\n" +
                       "if stock_num < qty_num then return -2 end\n" +
                       "redis.call('incrby', KEYS[1], -qty_num)\n" +
                       "return 1";

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(script);
        redisScript.setResultType(Long.class);

        // 添加调试日志
        log.info("准备执行Redis库存扣减 - key:{} quantity:{}", key, quantity);
        Object currentStock = redisTemplate.opsForValue().get(key);
        log.info("当前Redis库存 - key:{} value:{} type:{}", key, currentStock,
                currentStock != null ? currentStock.getClass() : "null");
        log.info("参数信息 - quantity:{}", quantity);

        try {
            // 传递数字而不是字符串
            Long result = redisTemplate.execute(redisScript, Arrays.asList(key), quantity);
            log.info("Redis库存扣减执行结果 - key:{} quantity:{} result:{}", key, quantity, result);

            if (result == null) {
                log.error("Redis脚本执行返回null - key:{} quantity:{}", key, quantity);
                return false;
            }

            if (result == -3) {
                log.error("Redis脚本执行失败：库存值为nil - stock:{}", currentStock);
                return false;
            } else if (result == -4) {
                log.error("Redis脚本执行失败：数量值转换失败 - qty:{}", quantity);
                return false;
            } else if (result == -2) {
                log.warn("Redis脚本执行失败：库存不足 - stock:{} qty:{}", currentStock, quantity);
                return false;
            } else if (result == 1) {
                log.info("Redis库存扣减成功 - key:{} quantity:{}", key, quantity);
                return true;
            } else {
                log.error("Redis脚本执行失败：未知结果 {} - key:{} quantity:{}", result, key, quantity);
                return false;
            }
        } catch (Exception e) {
            log.error("Redis库存扣减执行失败 - key:{} quantity:{} error:{}", key, quantity, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 同步更新MySQL库存数据
     */
    private void updateMySQLStock(Long spotId, LocalDate visitDate, int quantity) {
        try {
            // 查询对应的库存记录
            LambdaQueryWrapper<ScenicStock> stockQuery = new LambdaQueryWrapper<>();
            stockQuery.eq(ScenicStock::getScenicId, spotId)
                     .eq(ScenicStock::getStockDate, visitDate);
            ScenicStock scenicStock = scenicStockMapper.selectOne(stockQuery);

            if (scenicStock != null) {
                // 更新已售数量 - 使用乐观锁确保并发安全
                int currentSoldQty = scenicStock.getSoldQty();
                int newSoldQty = currentSoldQty + quantity;

                // 检查是否超过总库存
                if (newSoldQty > scenicStock.getTotalQty()) {
                    log.error("MySQL库存更新失败：销售数量超过总库存 - 景区ID:{} 日期:{} 当前已售:{} 购买数量:{} 总库存:{}",
                            spotId, visitDate, currentSoldQty, quantity, scenicStock.getTotalQty());
                    throw new BusinessException(ErrorCode.PARAM_ERROR, "库存不足");
                }

                // 更新库存
                scenicStock.setSoldQty(newSoldQty);
                scenicStock.setLockVersion(scenicStock.getLockVersion() + 1);

                int updateResult = scenicStockMapper.updateById(scenicStock);
                if (updateResult > 0) {
                    log.info("MySQL库存更新成功 - 景区ID:{} 日期:{} 已售数量:{} -> {}",
                            spotId, visitDate, currentSoldQty, newSoldQty);
                } else {
                    log.error("MySQL库存更新失败：乐观锁冲突 - 景区ID:{} 日期:{}",
                            spotId, visitDate);
                    throw new BusinessException(ErrorCode.PARAM_ERROR, "库存更新失败，请重试");
                }
            } else {
                log.error("MySQL库存更新失败：未找到库存记录 - 景区ID:{} 日期:{}",
                        spotId, visitDate);
                throw new BusinessException(ErrorCode.PARAM_ERROR, "库存记录不存在");
            }
        } catch (Exception e) {
            log.error("MySQL库存更新异常 - 景区ID:{} 日期:{} 数量:{} 错误:{}",
                    spotId, visitDate, quantity, e.getMessage(), e);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "库存更新失败");
        }
    }

    /**
     * 只回滚Redis库存（用于异常处理，MySQL由事务自动回滚）
     */
    private void restoreStockWithRedisOnly(Long spotId, LocalDate visitDate, int quantity) {
        String key = "stock:" + spotId + ":" + visitDate.toString();
        try {
            redisTemplate.opsForValue().increment(key, quantity);
            log.info("Redis库存回滚成功 - 景区ID:{} 日期:{} 回滚数量:{}", spotId, visitDate, quantity);
        } catch (Exception e) {
            log.error("Redis库存回滚失败 - 景区ID:{} 日期:{} 回滚数量:{}", spotId, visitDate, quantity, e);
        }
    }

    /**
     * 使用Redis回滚库存（同时回滚MySQL，用于订单取消/过期）
     */
    private void restoreStockWithRedis(Long spotId, LocalDate visitDate, int quantity) {
        String key = "stock:" + spotId + ":" + visitDate.toString();
        try {
            redisTemplate.opsForValue().increment(key, quantity);
            log.info("Redis库存回滚成功 - 景区ID:{} 日期:{} 回滚数量:{}", spotId, visitDate, quantity);

            // 同时回滚MySQL库存
            restoreMySQLStock(spotId, visitDate, quantity);
        } catch (Exception e) {
            log.error("Redis库存回滚失败 - 景区ID:{} 日期:{} 回滚数量:{}", spotId, visitDate, quantity, e);
        }
    }

    /**
     * 回滚MySQL库存数据
     */
    private void restoreMySQLStock(Long spotId, LocalDate visitDate, int quantity) {
        try {
            LambdaQueryWrapper<ScenicStock> stockQuery = new LambdaQueryWrapper<>();
            stockQuery.eq(ScenicStock::getScenicId, spotId)
                     .eq(ScenicStock::getStockDate, visitDate);
            ScenicStock scenicStock = scenicStockMapper.selectOne(stockQuery);

            if (scenicStock != null && scenicStock.getSoldQty() >= quantity) {
                scenicStock.setSoldQty(scenicStock.getSoldQty() - quantity);
                scenicStock.setLockVersion(scenicStock.getLockVersion() + 1);

                int updateResult = scenicStockMapper.updateById(scenicStock);
                if (updateResult > 0) {
                    log.info("MySQL库存回滚成功 - 景区ID:{} 日期:{} 已售数量:{} -> {}",
                            spotId, visitDate, scenicStock.getSoldQty() + quantity, scenicStock.getSoldQty());
                } else {
                    log.error("MySQL库存回滚失败：乐观锁冲突 - 景区ID:{} 日期:{}",
                            spotId, visitDate);
                }
            } else {
                log.warn("MySQL库存回滚跳过：库存记录不存在或已售数量不足 - 景区ID:{} 日期:{}",
                        spotId, visitDate);
            }
        } catch (Exception e) {
            log.error("MySQL库存回滚异常 - 景区ID:{} 日期:{} 数量:{} 错误:{}",
                    spotId, visitDate, quantity, e.getMessage(), e);
        }
    }

    /**
     * 转换为OrderVO
     */
    private OrderVO convertToOrderVO(Order order, ScenicSpot scenicSpot, TicketProduct product, List<OrderVisitor> orderVisitors) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderSn(order.getOrderSn());
        vo.setSpotName(scenicSpot.getSpotName());
        vo.setSpotImage(scenicSpot.getImageUrls() != null && scenicSpot.getImageUrls().length() > 0 ?
                       scenicSpot.getImageUrls().split(",")[0] : null);
        vo.setVisitDate(order.getVisitDate());
        vo.setTicketType(product.getTicketType());
        vo.setTicketTypeName(getTicketTypeName(product.getTicketType()));
        vo.setBuyNum(order.getBuyNum());
        vo.setUnitPrice(order.getUnitPrice());
        vo.setPayAmount(order.getPayAmount());
        vo.setOrderStatus(order.getOrderStatus());
        vo.setOrderStatusName(getOrderStatusName(order.getOrderStatus()));
        vo.setVerificationCode(order.getVerificationCode());
        vo.setPaymentTime(order.getPaymentTime());
        vo.setVerifyTime(order.getVerifyTime());
        vo.setCreateTime(order.getCreateTime());
        vo.setExpireTime(order.getCreateTime().plusMinutes(15));

        // 转换游客信息
        List<OrderVisitorVO> visitorVOs = orderVisitors.stream().map(visitor -> {
            OrderVisitorVO visitorVO = new OrderVisitorVO();
            visitorVO.setVisitorName(visitor.getVisitorName());
            visitorVO.setIdCard(maskIdCard(visitor.getIdCard()));
            visitorVO.setContactTel(visitor.getContactTel());
            visitorVO.setVerifyStatus(order.getOrderStatus() == 2 ? 1 : 0);
            if (order.getVerifyTime() != null) {
                visitorVO.setVerifyTime(order.getVerifyTime().toString());
            }
            return visitorVO;
        }).collect(Collectors.toList());
        vo.setVisitors(visitorVOs);

        return vo;
    }

    /**
     * 获取票种类型名称
     */
    private String getTicketTypeName(Integer ticketType) {
        switch (ticketType) {
            case 1: return "原价票";
            case 2: return "半价票";
            case 3: return "免票";
            default: return "未知票种";
        }
    }

    /**
     * 获取订单状态名称
     */
    private String getOrderStatusName(Integer orderStatus) {
        switch (orderStatus) {
            case 0: return "待支付";
            case 1: return "已支付待核销";
            case 2: return "已核销";
            case 3: return "已取消";
            case 4: return "已退款";
            case 5: return "已过期";
            default: return "未知状态";
        }
    }

    /**
     * 身份证号脱敏
     */
    private String maskIdCard(String idCard) {
        if (StrUtil.isBlank(idCard) || idCard.length() < 8) {
            return idCard;
        }
        return idCard.substring(0, 4) + "****" + idCard.substring(idCard.length() - 4);
    }

    /**
     * 生成订单号
     */
    private String generateOrderSn() {
        return OrderUtils.generateOrderSn();
    }

    /**
     * 定时任务：处理过期订单（每分钟执行一次）
     */
    @Scheduled(fixedRate = 60000, initialDelay = 30000) // 启动后30秒延迟，之后每分钟执行一次
    public void processExpiredOrdersTask() {
        try {
            processExpiredOrders();
        } catch (Exception e) {
            log.error("处理过期订单失败", e);
        }
    }

    /**
     * 生成核销码
     */
    private String generateVerificationCode(Long spotId, LocalDate visitDate) {
        // 生成8位随机字母数字混合核销码
        return OrderUtils.generateVerificationCode(null, null);
    }
}




