package com.yudi.ticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yudi.ticket.model.entity.OrderVisitor;

import java.util.List;

/**
 * 订单游客信息服务
 */
public interface OrderVisitorService extends IService<OrderVisitor> {

    /**
     * 根据订单ID查询游客信息列表
     *
     * @param orderId 订单ID
     * @return 游客信息列表
     */
    List<OrderVisitor> listByOrderId(Long orderId);

    /**
     * 批量保存订单游客信息
     *
     * @param orderVisitors 游客信息列表
     * @return 是否成功
     */
    boolean saveBatchOrderVisitors(List<OrderVisitor> orderVisitors);
}
