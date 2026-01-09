package com.yudi.ticket.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 景区库存查询请求
 */
@Data
public class ScenicStockQueryRequest implements Serializable {

    /**
     * 景区ID
     */
    private Long scenicId;

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;

    private static final long serialVersionUID = 1L;
}
