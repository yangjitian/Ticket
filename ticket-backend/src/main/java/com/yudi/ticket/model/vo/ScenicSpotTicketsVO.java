package com.yudi.ticket.model.vo;

import com.yudi.ticket.model.entity.ScenicSpot;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 景区门票汇总信息VO
 */
@Data
public class ScenicSpotTicketsVO implements Serializable {

    /**
     * 景区信息
     */
    private ScenicSpot scenicSpot;

    /**
     * 门票产品列表
     */
    private List<TicketProductVO> ticketProducts;

    /**
     * 未来库存概览
     */
    private FutureStockSummary futureStockSummary;

    @Data
    public static class FutureStockSummary implements Serializable {
        /**
         * 总产品数
         */
        private Integer totalProducts;

        /**
         * 有库存的天数
         */
        private Integer daysWithStock;

        /**
         * 总库存量
         */
        private Integer totalStockQty;

        /**
         * 已售罄的天数
         */
        private Integer soldOutDays;

        private static final long serialVersionUID = 1L;
    }

    private static final long serialVersionUID = 1L;
}
