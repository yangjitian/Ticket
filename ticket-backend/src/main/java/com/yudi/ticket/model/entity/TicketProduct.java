package com.yudi.ticket.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 业务-门票产品表
 * @TableName bus_ticket_product
 */
@TableName(value ="bus_ticket_product")
@Data
public class TicketProduct implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
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
     * 官网指导零售价（平台最高限价）
     */
    private BigDecimal priceRetail;

    /**
     * 平台结算价（分销商最低成本价，佣金计算基准）
     */
    private BigDecimal priceSettlement;

    /**
     * 分销商默认销售价（NULL则使用price_retail）
     */
    private BigDecimal defaultDistSalePrice;

    /**
     * 上架状态: 1-上架, 0-下架
     */
    private Integer saleStatus;

    /**
     * 
     */
    private LocalDateTime createTime;

    /**
     * 
     */
    private LocalDateTime updateTime;

    /**
     * 
     */
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}