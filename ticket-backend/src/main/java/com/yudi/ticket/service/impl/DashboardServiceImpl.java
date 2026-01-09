package com.yudi.ticket.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yudi.ticket.mapper.OrderMapper;
import com.yudi.ticket.mapper.ScenicSpotMapper;
import com.yudi.ticket.mapper.UserMapper;
import com.yudi.ticket.model.entity.Order;
import com.yudi.ticket.model.entity.ScenicSpot;
import com.yudi.ticket.model.entity.User;
import com.yudi.ticket.model.vo.DashboardStatsVO;
import com.yudi.ticket.model.vo.RevenueStatsVO;
import com.yudi.ticket.service.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

/**
 * 控制台服务实现类
 */
@Service
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private ScenicSpotMapper scenicSpotMapper;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String DASHBOARD_STATS_KEY = "dashboard:stats";
    private static final String REVENUE_STATS_KEY_PREFIX = "dashboard:revenue:stats:";

    @Override
    public DashboardStatsVO getDashboardStats() {
        // 先尝试从Redis获取
        DashboardStatsVO cachedStats = (DashboardStatsVO) redisTemplate.opsForValue().get(DASHBOARD_STATS_KEY);
        if (cachedStats != null) {
            log.debug("从Redis获取控制台统计数据");
            return cachedStats;
        }

        // Redis中没有，从数据库获取并缓存到Redis（永久缓存）
        log.info("从数据库获取控制台统计数据并永久缓存到Redis");
        DashboardStatsVO stats = getDashboardStatsFromDB();
        redisTemplate.opsForValue().set(DASHBOARD_STATS_KEY, stats);
        return stats;
    }

    @Override
    public DashboardStatsVO getDashboardStatsFromDB() {
        DashboardStatsVO stats = new DashboardStatsVO();

        // 总用户数
        QueryWrapper<User> userQuery = new QueryWrapper<>();
        userQuery.eq("is_deleted", 0);
        stats.setTotalUsers(userMapper.selectCount(userQuery));

        // 景区数量
        QueryWrapper<ScenicSpot> scenicQuery = new QueryWrapper<>();
        scenicQuery.eq("is_deleted", 0);
        stats.setTotalScenicSpots(scenicSpotMapper.selectCount(scenicQuery));

        // 总经销商数量（包括待审核）
        QueryWrapper<User> allDistributorsQuery = new QueryWrapper<>();
        allDistributorsQuery.eq("is_deleted", 0)
                           .eq("role", 2);
        stats.setTotalDistributors(userMapper.selectCount(allDistributorsQuery));

        // 已通过经销商数量
        QueryWrapper<User> approvedDistributorsQuery = new QueryWrapper<>();
        approvedDistributorsQuery.eq("is_deleted", 0)
                                .eq("role", 2)
                                .eq("audit_status", 1);
        stats.setApprovedDistributors(userMapper.selectCount(approvedDistributorsQuery));

        // 待审核经销商数量
        QueryWrapper<User> pendingDistributorsQuery = new QueryWrapper<>();
        pendingDistributorsQuery.eq("is_deleted", 0)
                               .eq("role", 2)
                               .eq("audit_status", 0);
        stats.setPendingDistributors(userMapper.selectCount(pendingDistributorsQuery));

        // 景区收入总额（已支付订单的总金额）
        QueryWrapper<Order> revenueQuery = new QueryWrapper<>();
        revenueQuery.eq("is_deleted", 0)
                   .eq("order_status", 1); // 已支付订单
        List<Order> orders = orderMapper.selectList(revenueQuery);
        BigDecimal totalRevenue = orders.stream()
                .map(Order::getPayAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTotalRevenue(totalRevenue);

        // 计算今日、昨日、本周、本月的新增用户和收入
        Date now = new Date();
        Date todayStart = DateUtil.beginOfDay(now);
        Date yesterdayStart = DateUtil.beginOfDay(DateUtil.offsetDay(now, -1));
        Date weekStart = DateUtil.beginOfWeek(now);
        Date monthStart = DateUtil.beginOfMonth(now);

        // 今日新增用户
        QueryWrapper<User> todayUserQuery = new QueryWrapper<>();
        todayUserQuery.eq("is_deleted", 0)
                     .ge("create_time", todayStart);
        stats.setTodayNewUsers(userMapper.selectCount(todayUserQuery));

        // 本周新增用户
        QueryWrapper<User> weekUserQuery = new QueryWrapper<>();
        weekUserQuery.eq("is_deleted", 0)
                    .ge("create_time", weekStart);
        stats.setWeekNewUsers(userMapper.selectCount(weekUserQuery));

        // 本月新增用户
        QueryWrapper<User> monthUserQuery = new QueryWrapper<>();
        monthUserQuery.eq("is_deleted", 0)
                     .ge("create_time", monthStart);
        stats.setMonthNewUsers(userMapper.selectCount(monthUserQuery));

        // 今日收入
        QueryWrapper<Order> todayRevenueQuery = new QueryWrapper<>();
        todayRevenueQuery.eq("is_deleted", 0)
                        .eq("order_status", 1)
                        .ge("payment_time", todayStart);
        List<Order> todayOrders = orderMapper.selectList(todayRevenueQuery);
        BigDecimal todayRevenue = todayOrders.stream()
                .map(Order::getPayAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTodayRevenue(todayRevenue);

        // 本周收入
        QueryWrapper<Order> weekRevenueQuery = new QueryWrapper<>();
        weekRevenueQuery.eq("is_deleted", 0)
                       .eq("order_status", 1)
                       .ge("payment_time", weekStart);
        List<Order> weekOrders = orderMapper.selectList(weekRevenueQuery);
        BigDecimal weekRevenue = weekOrders.stream()
                .map(Order::getPayAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setWeekRevenue(weekRevenue);

        // 本月收入
        QueryWrapper<Order> monthRevenueQuery = new QueryWrapper<>();
        monthRevenueQuery.eq("is_deleted", 0)
                        .eq("order_status", 1)
                        .ge("payment_time", monthStart);
        List<Order> monthOrders = orderMapper.selectList(monthRevenueQuery);
        BigDecimal monthRevenue = monthOrders.stream()
                .map(Order::getPayAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setMonthRevenue(monthRevenue);

        return stats;
    }

    @Override
    public RevenueStatsVO getRevenueStats(Integer days) {
        // 尝试从Redis获取
        String cacheKey = REVENUE_STATS_KEY_PREFIX + days;
        RevenueStatsVO cachedStats = (RevenueStatsVO) redisTemplate.opsForValue().get(cacheKey);
        if (cachedStats != null) {
            log.info("从Redis获取收入统计数据，days={}", days);
            return cachedStats;
        }

        // Redis中没有，从数据库获取并缓存
        log.info("从数据库获取收入统计数据并永久缓存到Redis，days={}", days);
        RevenueStatsVO stats = new RevenueStatsVO();

        // 计算日期范围
        Date endDate = new Date();
        Date startDate = DateUtil.offsetDay(endDate, -days + 1);

        // 1. 日期订单数统计
        List<RevenueStatsVO.DateOrderCountVO> dateOrderCounts = new ArrayList<>();
        // 2. 日期收入统计
        List<RevenueStatsVO.DateRevenueVO> dateRevenues = new ArrayList<>();

        // 生成日期列表
        List<Date> dateList = new ArrayList<>();
        Date currentDate = startDate;
        while (!currentDate.after(endDate)) {
            dateList.add(currentDate);
            currentDate = DateUtil.offsetDay(currentDate, 1);
        }

        // 统计每日的订单数和收入
        for (Date date : dateList) {
            Date dayStart = DateUtil.beginOfDay(date);
            Date dayEnd = DateUtil.endOfDay(date);
            String dateStr = DateUtil.format(date, "yyyy-MM-dd");

            // 当日订单数
            QueryWrapper<Order> orderQuery = new QueryWrapper<>();
            orderQuery.eq("is_deleted", 0)
                     .eq("order_status", 1)
                     .ge("payment_time", dayStart)
                     .le("payment_time", dayEnd);
            Long orderCount = orderMapper.selectCount(orderQuery);

            RevenueStatsVO.DateOrderCountVO orderCountVO = new RevenueStatsVO.DateOrderCountVO();
            orderCountVO.setDate(dateStr);
            orderCountVO.setOrderCount(orderCount);
            dateOrderCounts.add(orderCountVO);

        // 当日收入
        List<Order> dayOrders = orderMapper.selectList(orderQuery);
        BigDecimal dayRevenue = dayOrders.stream()
                .map(Order::getPayAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.debug("日期 {}: 订单数 {}, 收入 {}", dateStr, orderCount, dayRevenue);

        RevenueStatsVO.DateRevenueVO revenueVO = new RevenueStatsVO.DateRevenueVO();
        revenueVO.setDate(dateStr);
        revenueVO.setRevenue(dayRevenue);
        dateRevenues.add(revenueVO);
        }

        stats.setDateOrderCounts(dateOrderCounts);
        stats.setDateRevenues(dateRevenues);

        // 3. 各景区营收情况
        List<RevenueStatsVO.ScenicRevenueVO> scenicRevenues = new ArrayList<>();

        // 获取所有景区
        QueryWrapper<ScenicSpot> scenicQuery = new QueryWrapper<>();
        scenicQuery.eq("is_deleted", 0);
        List<ScenicSpot> scenics = scenicSpotMapper.selectList(scenicQuery);

        BigDecimal totalRevenue = BigDecimal.ZERO;

        // 统计每个景区的收入
        for (ScenicSpot scenic : scenics) {
            // 找到该景区的所有门票产品
            List<Order> scenicOrders = orderMapper.selectList(new QueryWrapper<Order>()
                    .eq("is_deleted", 0)
                    .eq("order_status", 1)
                    .inSql("product_id", "SELECT id FROM bus_ticket_product WHERE spot_id = " + scenic.getId()));

            BigDecimal scenicRevenue = scenicOrders.stream()
                    .map(Order::getPayAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (scenicRevenue.compareTo(BigDecimal.ZERO) > 0) {
                RevenueStatsVO.ScenicRevenueVO scenicRevenueVO = new RevenueStatsVO.ScenicRevenueVO();
                scenicRevenueVO.setScenicId(scenic.getId());
                scenicRevenueVO.setScenicName(scenic.getSpotName());
                scenicRevenueVO.setTotalRevenue(scenicRevenue);
                scenicRevenueVO.setOrderCount((long) scenicOrders.size());
                scenicRevenues.add(scenicRevenueVO);

                totalRevenue = totalRevenue.add(scenicRevenue);
            }
        }

        // 计算占比
        if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            for (RevenueStatsVO.ScenicRevenueVO scenicRevenue : scenicRevenues) {
                BigDecimal percentage = scenicRevenue.getTotalRevenue()
                        .divide(totalRevenue, 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(new BigDecimal("100"));
                scenicRevenue.setPercentage(percentage);
            }
        }

        // 按收入降序排序
        scenicRevenues.sort((a, b) -> b.getTotalRevenue().compareTo(a.getTotalRevenue()));

        stats.setScenicRevenues(scenicRevenues);

        // 缓存结果（永久缓存）
        redisTemplate.opsForValue().set(cacheKey, stats);

        log.info("收入统计数据计算完成，日期范围: {} 天，订单数统计: {} 条，收入统计: {} 条",
                days, dateOrderCounts.size(), dateRevenues.size());

        return stats;
    }
}
