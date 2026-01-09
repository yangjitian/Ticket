package com.yudi.ticket.service;

import com.yudi.ticket.model.vo.DashboardStatsVO;
import com.yudi.ticket.model.vo.RevenueStatsVO;

/**
 * 控制台服务接口
 */
public interface DashboardService {

    /**
     * 获取控制台统计数据
     * @return 统计数据
     */
    DashboardStatsVO getDashboardStats();

    DashboardStatsVO getDashboardStatsFromDB();

    /**
     * 获取收入统计数据
     * @param days 天数范围（7、14、30等）
     * @return 收入统计数据
     */
    RevenueStatsVO getRevenueStats(Integer days);
}
