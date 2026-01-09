package com.yudi.ticket.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 创建订单请求
 */
@Data
public class CreateOrderRequest implements Serializable {

    /**
     * 下单用户ID
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
     * 游客信息列表
     */
    private List<VisitorInfo> visitors;

    @Data
    public static class VisitorInfo implements Serializable {
        /**
         * 游客姓名
         */
        private String visitorName;

        /**
         * 身份证号
         */
        private String idCard;

        /**
         * 联系电话
         */
        private String contactTel;
    }

    private static final long serialVersionUID = 1L;
}
