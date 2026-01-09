package com.yudi.ticket.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 门票产品更新请求
 */
@Data
public class TicketProductUpdateRequest implements Serializable {

    /**
     * 产品ID
     */
    private Long id;

    /**
     * 所属景区ID
     */
    private Long spotId;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 票种类型: 1-成人, 2-儿童, 3-老人, 4-学生, 5-其他
     */
    private Integer ticketType;

    /**
     * 官网指导零售价
     */
    private BigDecimal priceRetail;

    /**
     * 平台结算价
     */
    private BigDecimal priceSettlement;

    /**
     * 上架状态: 1-上架, 0-下架
     */
    private Integer saleStatus;

    private static final long serialVersionUID = 1L;
}
