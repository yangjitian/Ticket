package com.yudi.ticket.model.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DistributorOverviewVO {
    private BigDecimal totalSales;
    private Integer totalOrders;
    private BigDecimal totalProfit;
    private BigDecimal availableBalance;
    
    // Profit Trend (Last 15 days)
    private List<TrendVO> trend;
    
    @Data
    public static class TrendVO {
        private String date; // yyyy-MM-dd
        private BigDecimal profit;
    }
}
