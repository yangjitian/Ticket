package com.yudi.ticket.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yudi.ticket.common.BaseResponse;
import com.yudi.ticket.common.ErrorCode;
import com.yudi.ticket.common.ThrowUtils;
import com.yudi.ticket.model.dto.BookTicketRequest;
import com.yudi.ticket.model.dto.CreateOrderRequest;
import com.yudi.ticket.model.dto.OrderQueryRequest;
import com.yudi.ticket.model.dto.PayOrderRequest;
import com.yudi.ticket.model.dto.VerifyOrderRequest;
import com.yudi.ticket.model.entity.Order;
import com.yudi.ticket.model.vo.BookTicketVO;
import com.yudi.ticket.model.vo.OrderVO;
import com.yudi.ticket.service.OrderService;
import com.yudi.ticket.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {

    @Resource
    private OrderService orderService;

    /**
     * 购票 - 创建订单
     */
    @PostMapping("/book")
    public BaseResponse<OrderVO> bookTicket(@RequestBody BookTicketRequest request, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAM_ERROR);
        Long userId = JwtUtils.getUserIdFromToken(httpRequest);
        ThrowUtils.throwIf(userId == null, ErrorCode.UNAUTHORIZED);
        OrderVO order = orderService.bookTicket(request, userId);
        return BaseResponse.success(order);
    }

    /**
     * 获取购票信息
     */
    @GetMapping("/book-info")
    public BaseResponse<BookTicketVO> getBookTicketInfo(
            @RequestParam Long spotId,
            @RequestParam Integer ticketType,
            @RequestParam String visitDate,
            HttpServletRequest httpRequest) {
        Long userId = JwtUtils.getUserIdFromToken(httpRequest);
        ThrowUtils.throwIf(userId == null, ErrorCode.UNAUTHORIZED);
        BookTicketVO vo = orderService.getBookTicketInfo(spotId, ticketType, visitDate, userId);
        return BaseResponse.success(vo);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/detail")
    public BaseResponse<OrderVO> getOrderDetail(@RequestParam Long orderId, HttpServletRequest httpRequest) {
        Long userId = JwtUtils.getUserIdFromToken(httpRequest);
        ThrowUtils.throwIf(userId == null, ErrorCode.UNAUTHORIZED);
        OrderVO order = orderService.getOrderDetail(orderId, userId);
        return BaseResponse.success(order);
    }

    @PostMapping("/create")
    public BaseResponse<Order> createOrder(@RequestBody CreateOrderRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAM_ERROR);
        ThrowUtils.throwIf(request.getBuyerId() == null, ErrorCode.PARAM_ERROR, "用户ID不能为空");
        Order order = orderService.createOrder(request);
        return BaseResponse.success(order);
    }

    @GetMapping("/user/list")
    public BaseResponse<IPage<Order>> getUserOrders(OrderQueryRequest request) {
        if (request == null) {
            request = new OrderQueryRequest();
        }
        IPage<Order> page = orderService.queryUserOrders(request);
        return BaseResponse.success(page);
    }

    /**
     * 获取用户订单详情列表（VO格式）
     */
    @GetMapping("/user/list-vo")
    public BaseResponse<IPage<OrderVO>> getUserOrderVOs(OrderQueryRequest request, HttpServletRequest httpRequest) {
        Long userId = JwtUtils.getUserIdFromToken(httpRequest);
        ThrowUtils.throwIf(userId == null, ErrorCode.UNAUTHORIZED);

        if (request == null) {
            request = new OrderQueryRequest();
        }

        // 设置用户ID，确保只能查询自己的订单
        request.setBuyerId(userId);

        IPage<OrderVO> page = orderService.queryUserOrderVOs(request, userId);
        return BaseResponse.success(page);
    }

    /**
     * 支付订单（新版本）
     */
    @PostMapping("/pay")
    public BaseResponse<OrderVO> payOrder(@RequestBody PayOrderRequest request, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAM_ERROR);
        ThrowUtils.throwIf(request.getOrderId() == null, ErrorCode.PARAM_ERROR, "订单ID不能为空");
        Long userId = JwtUtils.getUserIdFromToken(httpRequest);
        ThrowUtils.throwIf(userId == null, ErrorCode.UNAUTHORIZED);
        OrderVO order = orderService.payOrder(request, userId);
        return BaseResponse.success(order);
    }

    @PostMapping("/cancel")
    public BaseResponse<Boolean> cancelOrder(@RequestBody CancelOrderRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAM_ERROR);
        ThrowUtils.throwIf(request.getOrderId() == null, ErrorCode.PARAM_ERROR, "订单ID不能为空");
        boolean result = orderService.cancelOrder(request.getOrderId());
        return BaseResponse.success(result);
    }

    /**
     * 删除订单
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteOrder(@RequestBody DeleteOrderRequest request, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAM_ERROR);
        ThrowUtils.throwIf(request.getOrderId() == null, ErrorCode.PARAM_ERROR, "订单ID不能为空");
        Long userId = JwtUtils.getUserIdFromToken(httpRequest);
        ThrowUtils.throwIf(userId == null, ErrorCode.UNAUTHORIZED);
        boolean result = orderService.deleteOrder(request.getOrderId(), userId);
        return BaseResponse.success(result);
    }

    // 删除订单请求DTO
    public static class DeleteOrderRequest {
        private Long orderId;

        public Long getOrderId() {
            return orderId;
        }

        public void setOrderId(Long orderId) {
            this.orderId = orderId;
        }
    }

    /**
     * 核销订单（新版本）
     */
    @PostMapping("/verify")
    public BaseResponse<Boolean> verifyOrder(@RequestBody VerifyOrderRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAM_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(request.getVerificationCode()), ErrorCode.PARAM_ERROR, "核销码不能为空");
        boolean result = orderService.verifyOrder(request);
        return BaseResponse.success(result);
    }

    // 内部DTO类
    public static class CancelOrderRequest {
        private Long orderId;

        public Long getOrderId() {
            return orderId;
        }

        public void setOrderId(Long orderId) {
            this.orderId = orderId;
        }
    }
}
