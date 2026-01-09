package com.yudi.ticket.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 门票价格更新请求
 */
@Data
public class TicketPriceUpdateRequest implements Serializable {

    /**
     * 产品ID
     */
    private Long id;

    /**
     * 官网指导零售价
     */
    private BigDecimal priceRetail;

    /**
     * 平台结算价
     */
    private BigDecimal priceSettlement;

    private static final long serialVersionUID = 1L;
}
