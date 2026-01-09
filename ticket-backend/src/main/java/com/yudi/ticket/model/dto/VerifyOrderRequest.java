package com.yudi.ticket.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 核销订单请求DTO
 */
@Data
public class VerifyOrderRequest implements Serializable {

    /**
     * 核销码
     */
    private String verificationCode;

    /**
     * 操作员ID（景区工作人员ID）
     */
    private Long operatorId;

    private static final long serialVersionUID = 1L;
}
