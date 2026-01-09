package com.yudi.ticket.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 订单查询请求
 */
@Data
public class OrderQueryRequest implements Serializable {

    /**
     * 当前页码
     */
    private long current = 1;

    /**
     * 页面大小
     */
    private long pageSize = 10;

    /**
     * 买家用户ID
     */
    private Long buyerId;

    /**
     * 订单状态: 0-待支付, 1-已支付待核销, 2-已核销, 3-已取消, 4-已退款, 5-已过期
     */
    private Integer orderStatus;

    private static final long serialVersionUID = 1L;
}
