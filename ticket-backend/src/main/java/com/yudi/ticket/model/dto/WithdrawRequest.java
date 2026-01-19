package com.yudi.ticket.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 提现请求
 */
@Data
public class WithdrawRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 提现金额
     */
    private BigDecimal amount;
}
