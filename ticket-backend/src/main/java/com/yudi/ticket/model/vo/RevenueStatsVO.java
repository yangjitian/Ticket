package com.yudi.ticket.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 收入统计数据VO
 */
@Data
public class RevenueStatsVO {

    /**
     * 日期订单数统计
     */
    private List<DateOrderCountVO> dateOrderCounts;

    /**
     * 日期收入统计
     */
    private List<DateRevenueVO> dateRevenues;

    /**
     * 各景区营收情况
     */
    private List<ScenicRevenueVO> scenicRevenues;

    /**
     * 日期订单数统计VO
     */
    @Data
    public static class DateOrderCountVO {
        /**
         * 日期
         */
        private String date;

        /**
         * 订单数量
         */
        private Long orderCount;
    }

    /**
     * 日期收入统计VO
     */
    @Data
    public static class DateRevenueVO {
        /**
         * 日期
         */
        private String date;

        /**
         * 收入金额
         */
        private BigDecimal revenue;
    }

    /**
     * 景区营收统计VO
     */
    @Data
    public static class ScenicRevenueVO {
        /**
         * 景区ID
         */
        private Long scenicId;

        /**
         * 景区名称
         */
        private String scenicName;

        /**
         * 总收入
         */
        private BigDecimal totalRevenue;

        /**
         * 订单数量
         */
        private Long orderCount;

        /**
         * 占比
         */
        private BigDecimal percentage;
    }
}
