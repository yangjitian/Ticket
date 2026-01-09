package com.yudi.ticket.service;

import com.yudi.ticket.model.dto.BookTicketRequest;
import com.yudi.ticket.model.dto.CreateOrderRequest;
import com.yudi.ticket.model.dto.OrderQueryRequest;
import com.yudi.ticket.model.dto.PayOrderRequest;
import com.yudi.ticket.model.dto.VerifyOrderRequest;
import com.yudi.ticket.model.entity.Order;
import com.yudi.ticket.model.vo.BookTicketVO;
import com.yudi.ticket.model.vo.OrderVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Asus
* @description 针对表【bus_order(业务-订单主表)】的数据库操作Service
* @createDate 2026-01-05 15:53:49
*/
public interface OrderService extends IService<Order> {

    /**
     * 创建订单
     */
    Order createOrder(CreateOrderRequest request);

    /**
     * 购票 - 创建订单（新版本）
     */
    OrderVO bookTicket(BookTicketRequest request, Long userId);

    /**
     * 获取购票信息
     */
    BookTicketVO getBookTicketInfo(Long spotId, Integer ticketType, String visitDate, Long userId);

    /**
     * 支付订单
     */
    boolean payOrder(Long orderId, String paymentMethod);

    /**
     * 支付订单（新版本）
     */
    OrderVO payOrder(PayOrderRequest request, Long userId);

    /**
     * 分页查询用户订单
     */
    IPage<Order> queryUserOrders(OrderQueryRequest request);

    /**
     * 分页查询用户订单详情（VO格式）
     */
    IPage<OrderVO> queryUserOrderVOs(OrderQueryRequest request, Long userId);

    /**
     * 查询用户订单详情
     */
    OrderVO getOrderDetail(Long orderId, Long userId);

    /**
     * 取消订单
     */
    boolean cancelOrder(Long orderId);

    /**
     * 删除订单（逻辑删除）
     */
    boolean deleteOrder(Long orderId, Long userId);

    /**
     * 核销订单
     */
    boolean verifyOrder(String verificationCode);

    /**
     * 核销订单（新版本）
     */
    boolean verifyOrder(VerifyOrderRequest request);

    /**
     * 处理过期订单
     */
    void processExpiredOrders();
}
