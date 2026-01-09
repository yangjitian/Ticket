package com.yudi.ticket.config;

import com.yudi.ticket.service.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 应用启动时执行的任务
 */
@Component
@Slf4j
public class ApplicationStartupRunner implements ApplicationRunner {

    @Resource
    private DashboardService dashboardService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("应用启动：开始初始化控制台统计数据缓存");
        try {
            // 调用getDashboardStats来初始化缓存
            dashboardService.getDashboardStats();
            log.info("应用启动：控制台统计数据缓存初始化完成");
        } catch (Exception e) {
            log.error("应用启动：初始化控制台统计数据缓存失败", e);
        }
    }
}
