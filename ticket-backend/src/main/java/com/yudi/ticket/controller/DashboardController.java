package com.yudi.ticket.controller;

import com.yudi.ticket.aop.RequireAdmin;
import com.yudi.ticket.common.BaseResponse;
import com.yudi.ticket.common.ErrorCode;
import com.yudi.ticket.common.ThrowUtils;
import com.yudi.ticket.model.vo.DashboardStatsVO;
import com.yudi.ticket.model.vo.RevenueStatsVO;
import com.yudi.ticket.service.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

/**
 * 控制台控制器
 */
@RestController
@RequestMapping("/dashboard")
@Slf4j
public class DashboardController {

    @Resource
    private DashboardService dashboardService;

    /**
     * 获取控制台统计数据
     */
    @GetMapping("/stats")
    @RequireAdmin
    public BaseResponse<DashboardStatsVO> getDashboardStats() {
        DashboardStatsVO stats = dashboardService.getDashboardStats();
        return BaseResponse.success(stats);
    }

    /**
     * 获取收入统计数据
     */
    @GetMapping("/revenue-stats")
    @RequireAdmin
    public BaseResponse<RevenueStatsVO> getRevenueStats(@RequestParam(defaultValue = "7") Integer days) {
        ThrowUtils.throwIf(days == null || days <= 0 || days > 365, ErrorCode.PARAM_ERROR, "天数参数无效");
        RevenueStatsVO stats = dashboardService.getRevenueStats(days);
        return BaseResponse.success(stats);
    }
}
