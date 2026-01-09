package com.yudi.ticket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yudi.ticket.mapper.OrderVisitorMapper;
import com.yudi.ticket.model.entity.OrderVisitor;
import com.yudi.ticket.service.OrderVisitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单游客信息服务实现
 */
@Slf4j
@Service
public class OrderVisitorServiceImpl extends ServiceImpl<OrderVisitorMapper, OrderVisitor>
        implements OrderVisitorService {

    @Override
    public List<OrderVisitor> listByOrderId(Long orderId) {
        QueryWrapper<OrderVisitor> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        return this.list(queryWrapper);
    }

    @Override
    public boolean saveBatchOrderVisitors(List<OrderVisitor> orderVisitors) {
        return this.saveBatch(orderVisitors);
    }
}
