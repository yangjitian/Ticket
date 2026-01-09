package com.yudi.ticket.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 业务-订单主表
 * @TableName bus_order
 */
@TableName(value ="bus_order")
@Data
public class Order implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 系统订单号
     */
    private String orderSn;

    /**
     * 下单用户ID（游客）
     */
    private Long buyerId;

    /**
     * 关联分销商ID（推广人）
     */
    private Long distributorId;

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 游玩日期
     */
    private LocalDate visitDate;

    /**
     * 购买数量
     */
    private Integer buyNum;

    /**
     * 下单时的实际销售单价（游客支付的价格）
     */
    private BigDecimal unitPrice;

    /**
     * 下单时的平台结算单价（快照）
     */
    private BigDecimal settlementUnitPrice;

    /**
     * 本订单分销商佣金总额
     */
    private BigDecimal commissionAmount;

    /**
     * 实付总额
     */
    private BigDecimal payAmount;

    /**
     * 订单状态: 0-待支付, 1-已支付待核销, 2-已核销, 3-已取消, 4-已退款, 5-已过期
     */
    private Integer orderStatus;

    /**
     * 核销码（建议格式: 景区简称-日期-随机码）
     */
    private String verificationCode;

    /**
     * 支付完成时间
     */
    private LocalDateTime paymentTime;

    /**
     * 实际核销时间
     */
    private LocalDateTime verifyTime;

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
    @TableField("is_deleted")
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}