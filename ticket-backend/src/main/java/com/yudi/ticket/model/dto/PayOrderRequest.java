package com.yudi.ticket.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 支付订单请求DTO
 */
@Data
public class PayOrderRequest implements Serializable {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 支付方式：1-微信支付, 2-支付宝支付, 3-余额支付（暂时不支持）
     */
    private Integer payType;

    private static final long serialVersionUID = 1L;
}
