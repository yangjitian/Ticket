package com.yudi.ticket.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 门票产品查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TicketProductQueryRequest extends PageRequest implements Serializable {

    /**
     * 所属景区ID
     */
    private String spotId;

    /**
     * 景区名称（模糊搜索）
     */
    private String spotName;

    /**
     * 产品名称（模糊搜索）
     */
    private String productName;

    /**
     * 票种类型: 1-成人, 2-儿童, 3-老人, 4-学生, 5-其他
     */
    private Integer ticketType;

    /**
     * 上架状态: 1-上架, 0-下架
     */
    private Integer saleStatus;

    private static final long serialVersionUID = 1L;
}
