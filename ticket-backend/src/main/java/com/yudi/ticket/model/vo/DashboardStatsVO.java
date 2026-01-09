package com.yudi.ticket.model.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 控制台统计数据VO
 */
@Data
public class DashboardStatsVO {

    /**
     * 总用户数
     */
    private Long totalUsers;

    /**
     * 景区数量
     */
    private Long totalScenicSpots;

    /**
     * 总经销商数量（包括待审核）
     */
    private Long totalDistributors;

    /**
     * 已通过经销商数量
     */
    private Long approvedDistributors;

    /**
     * 待审核经销商数量
     */
    private Long pendingDistributors;

    /**
     * 景区收入总额
     */
    private BigDecimal totalRevenue;

    /**
     * 今日新增用户数
     */
    private Long todayNewUsers;

    /**
     * 今日收入
     */
    private BigDecimal todayRevenue;

    /**
     * 本周新增用户数
     */
    private Long weekNewUsers;

    /**
     * 本周收入
     */
    private BigDecimal weekRevenue;

    /**
     * 本月新增用户数
     */
    private Long monthNewUsers;

    /**
     * 本月收入
     */
    private BigDecimal monthRevenue;
}
