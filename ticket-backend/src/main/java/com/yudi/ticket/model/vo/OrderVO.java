package com.yudi.ticket.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单视图对象
 */
@Data
public class OrderVO implements Serializable {

    /**
     * 订单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 系统订单号
     */
    private String orderSn;

    /**
     * 景区名称
     */
    private String spotName;

    /**
     * 景区图片
     */
    private String spotImage;

    /**
     * 游玩日期
     */
    private LocalDate visitDate;

    /**
     * 票种类型：1-原价票, 2-半价票, 3-免票
     */
    private Integer ticketType;

    /**
     * 票种名称
     */
    private String ticketTypeName;

    /**
     * 购买数量
     */
    private Integer buyNum;

    /**
     * 单价
     */
    private BigDecimal unitPrice;

    /**
     * 实付总额
     */
    private BigDecimal payAmount;

    /**
     * 订单状态: 0-待支付, 1-已支付待核销, 2-已核销, 3-已取消, 4-已退款, 5-已过期
     */
    private Integer orderStatus;

    /**
     * 订单状态名称
     */
    private String orderStatusName;

    /**
     * 核销码
     */
    private String verificationCode;

    /**
     * 支付完成时间
     */
    private LocalDateTime paymentTime;

    /**
     * 核销时间
     */
    private LocalDateTime verifyTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 过期时间（15分钟后）
     */
    private LocalDateTime expireTime;

    /**
     * 游客信息列表
     */
    private List<OrderVisitorVO> visitors;

    private static final long serialVersionUID = 1L;
}
