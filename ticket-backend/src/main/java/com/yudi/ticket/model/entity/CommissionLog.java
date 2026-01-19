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
 * 业务-佣金记录流水表
 * @TableName bus_commission_log
 */
@TableName(value ="bus_commission_log")
@Data
public class CommissionLog implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long distributorId;

    /**
     * 
     */
    private Long orderId;

    /**
     * 订单号（冗余字段，便于查询）
     */
    private String orderSn;

    /**
     * 景区名称（冗余字段，便于展示）
     */
    private String scenicName;

    /**
     * 结算佣金
     */
    private BigDecimal profitAmount;

    /**
     * 操作后余额
     */
    private BigDecimal balanceAfter;

    /**
     * 流水类型: 1-佣金收入, 2-提现支出, 3-平台费用
     */
    private Integer logType;

    /**
     * 
     */
    private String logRemark;

    /**
     * 
     */
    private LocalDateTime createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
