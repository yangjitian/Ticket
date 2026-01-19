package com.yudi.ticket.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yudi.ticket.mapper.*;
import com.yudi.ticket.model.entity.*;
import com.yudi.ticket.model.vo.DistributorOrderVO;
import com.yudi.ticket.model.vo.DistributorOverviewVO;
import com.yudi.ticket.model.vo.DistributorProductVO;
import com.yudi.ticket.model.vo.DistributorWalletVO;
import com.yudi.ticket.service.DistributorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DistributorServiceImpl implements DistributorService {

    private final TicketProductMapper ticketProductMapper;
    private final ScenicSpotMapper scenicSpotMapper;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final CommissionLogMapper commissionLogMapper;

    @Override
    public Page<DistributorProductVO> getProductList(int page, int size, Long distributorId, String keyword, String ticketType) {
        Page<TicketProduct> productPage = new Page<>(page, size);
        LambdaQueryWrapper<TicketProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TicketProduct::getSaleStatus, 1); // Only on-sale
        wrapper.eq(TicketProduct::getIsDeleted, 0);

        // Filter by ticket type
        if (StrUtil.isNotBlank(ticketType)) {
             Integer typeCode = getTicketTypeCode(ticketType);
             if (typeCode != null) {
                 wrapper.eq(TicketProduct::getTicketType, typeCode);
             }
        }
        
        // Use separate query for spot name filter if needed, 
        // but simpler to filter by product name or handle spot join. 
        // For now, let's filter by product name if keyword is provided.
        // If keyword is intended for Spot Name, we'd need complex query or 2-step.
        // Let's assume keyword matches Scenic Name.
        List<Long> spotIds = null;
        if (StrUtil.isNotBlank(keyword)) {
            List<ScenicSpot> spots = scenicSpotMapper.selectList(new LambdaQueryWrapper<ScenicSpot>()
                    .like(ScenicSpot::getSpotName, keyword));
            if (CollUtil.isEmpty(spots)) {
                return new Page<>(page, size); // No spots found
            }
            spotIds = spots.stream().map(ScenicSpot::getId).collect(Collectors.toList());
            wrapper.in(TicketProduct::getSpotId, spotIds);
        }

        ticketProductMapper.selectPage(productPage, wrapper);

        Page<DistributorProductVO> voPage = new Page<>(productPage.getCurrent(), productPage.getSize(), productPage.getTotal());
        if (CollUtil.isEmpty(productPage.getRecords())) {
            return voPage;
        }

        // Fetch Spots
        List<Long> finalSpotIds = productPage.getRecords().stream()
                .map(TicketProduct::getSpotId).distinct().collect(Collectors.toList());
        Map<Long, ScenicSpot> spotMap = scenicSpotMapper.selectBatchIds(finalSpotIds).stream()
                .collect(Collectors.toMap(ScenicSpot::getId, Function.identity()));

        List<DistributorProductVO> vos = productPage.getRecords().stream().map(p -> {
            DistributorProductVO vo = new DistributorProductVO();
            vo.setId(String.valueOf(p.getId()));
            vo.setSpotId(String.valueOf(p.getSpotId()));
            ScenicSpot spot = spotMap.get(p.getSpotId());
            if (spot != null) {
                vo.setSpotName(spot.getSpotName());
                vo.setSpotLevel(spot.getLevel());
            }
            vo.setTicketType(getTicketTypeName(p.getTicketType()));
            vo.setRetailPrice(p.getPriceRetail());
            vo.setSettlementPrice(p.getPriceSettlement());
            // Fixed selling price = Retail Price (as per requirement 1)
            vo.setMyPrice(p.getPriceRetail());
            vo.setProfit(p.getPriceRetail().subtract(p.getPriceSettlement()));
            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(vos);
        return voPage;
    }

    @Override
    public Page<DistributorOrderVO> getOrderList(int page, int size, Long distributorId) {
        Page<Order> orderPage = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getDistributorId, distributorId);
        wrapper.orderByDesc(Order::getCreateTime);

        orderMapper.selectPage(orderPage, wrapper);

        Page<DistributorOrderVO> voPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        if (CollUtil.isEmpty(orderPage.getRecords())) {
            return voPage;
        }

        // Get Product IDs to fetch Spot Names
        List<Long> productIds = orderPage.getRecords().stream().map(Order::getProductId).collect(Collectors.toList());
        List<TicketProduct> products = ticketProductMapper.selectBatchIds(productIds);
        Map<Long, TicketProduct> productMap = products.stream().collect(Collectors.toMap(TicketProduct::getId, Function.identity()));
        
        // Get Spot IDs
        List<Long> spotIds = products.stream().map(TicketProduct::getSpotId).collect(Collectors.toList());
        Map<Long, ScenicSpot> spotMap = CollUtil.isEmpty(spotIds) ? Collections.emptyMap() :
                scenicSpotMapper.selectBatchIds(spotIds).stream().collect(Collectors.toMap(ScenicSpot::getId, Function.identity()));

        List<DistributorOrderVO> vos = orderPage.getRecords().stream().map(o -> {
            DistributorOrderVO vo = new DistributorOrderVO();
            vo.setId(o.getId());
            vo.setOrderNumber(o.getOrderSn());
            
            TicketProduct p = productMap.get(o.getProductId());
            if (p != null) {
                ScenicSpot s = spotMap.get(p.getSpotId());
                vo.setSpotName(s != null ? s.getSpotName() : p.getProductName());
            } else {
                vo.setSpotName("Unknown Product");
            }

            vo.setPlayDate(o.getVisitDate().toString());
            vo.setTotalAmount(o.getPayAmount());
            vo.setCommission(o.getCommissionAmount());
            vo.setCreateTime(o.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            vo.setStatus(getOrderStatusText(o.getOrderStatus()));
            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(vos);
        return voPage;
    }

    @Override
    public DistributorOverviewVO getOverview(Long distributorId) {
        DistributorOverviewVO vo = new DistributorOverviewVO();
        
        // Wallet
        User user = userMapper.selectById(distributorId);
        vo.setAvailableBalance(user.getWalletBalance() != null ? user.getWalletBalance() : BigDecimal.ZERO);

        // Stats
        LambdaQueryWrapper<Order> query = new LambdaQueryWrapper<>();
        query.eq(Order::getDistributorId, distributorId);
        // Valid orders (Paid or Verified)
        query.in(Order::getOrderStatus, 1, 2); 
        
        List<Order> orders = orderMapper.selectList(query);
        
        BigDecimal totalSales = orders.stream().map(Order::getPayAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalProfit = orders.stream().map(Order::getCommissionAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        
        vo.setTotalSales(totalSales);
        vo.setTotalProfit(totalProfit);
        vo.setTotalOrders(orders.size()); // Count of valid orders

        // Profit Trend (Last 15 days)
        // Ensure we cover all 15 days even if 0 profit
        List<DistributorOverviewVO.TrendVO> trends = new ArrayList<>();
        java.time.LocalDate today = java.time.LocalDate.now();
        
        // Fetch logs for last 15 days
        LambdaQueryWrapper<CommissionLog> logQuery = new LambdaQueryWrapper<>();
        logQuery.eq(CommissionLog::getDistributorId, distributorId);
        logQuery.eq(CommissionLog::getLogType, 1); // Income only
        logQuery.ge(CommissionLog::getCreateTime, today.minusDays(14).atStartOfDay());
        List<CommissionLog> recentLogs = commissionLogMapper.selectList(logQuery);
        
        // Map: Date String -> Sum Profit
        Map<String, BigDecimal> dailyProfit = recentLogs.stream()
                .collect(Collectors.groupingBy(
                        log -> log.getCreateTime().toLocalDate().toString(),
                        Collectors.reducing(BigDecimal.ZERO, CommissionLog::getProfitAmount, BigDecimal::add)
                ));

        for (int i = 14; i >= 0; i--) {
            java.time.LocalDate date = today.minusDays(i);
            String dateStr = date.toString();
            DistributorOverviewVO.TrendVO t = new DistributorOverviewVO.TrendVO();
            t.setDate(dateStr);
            t.setProfit(dailyProfit.getOrDefault(dateStr, BigDecimal.ZERO));
            trends.add(t);
        }
        vo.setTrend(trends);

        return vo;
    }

    @Override
    public DistributorWalletVO getWallet(Long distributorId, String month) {
        DistributorWalletVO vo = new DistributorWalletVO();
        User user = userMapper.selectById(distributorId);
        vo.setAvailableBalance(user.getWalletBalance() != null ? user.getWalletBalance() : BigDecimal.ZERO);
        
        LambdaQueryWrapper<CommissionLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommissionLog::getDistributorId, distributorId);
        wrapper.orderByDesc(CommissionLog::getCreateTime);
        if (StrUtil.isNotBlank(month)) {
             wrapper.likeRight(CommissionLog::getCreateTime, month); // '2024-12%'
        }
        
        List<CommissionLog> logs = commissionLogMapper.selectList(wrapper);
        List<DistributorWalletVO.BillVO> bills = logs.stream().map(log -> {
            DistributorWalletVO.BillVO bill = new DistributorWalletVO.BillVO();
            bill.setId(log.getId());
            bill.setTime(log.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            bill.setType(getLogTypeText(log.getLogType()));
            bill.setAmount(log.getLogType() == 2 ? log.getProfitAmount().negate() : log.getProfitAmount());
            bill.setBalance(log.getBalanceAfter());
            bill.setDescription(log.getLogRemark());
            return bill;
        }).collect(Collectors.toList());
        
        vo.setBills(bills);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean withdraw(Long distributorId, BigDecimal amount) {
        User user = userMapper.selectById(distributorId);
        if (user == null) return false;
        BigDecimal current = user.getWalletBalance() != null ? user.getWalletBalance() : BigDecimal.ZERO;
        
        if (current.compareTo(amount) < 0) {
            return false; // Insufficient balance
        }
        
        // Deduct
        user.setWalletBalance(current.subtract(amount));
        userMapper.updateById(user);
        
        // Log
        CommissionLog log = new CommissionLog();
        log.setDistributorId(distributorId);
        log.setOrderId(0L); // System
        log.setLogType(2); // Withdraw
        log.setProfitAmount(amount);
        log.setBalanceAfter(user.getWalletBalance());
        log.setLogRemark("余额提现");
        log.setScenicName("系统");
        commissionLogMapper.insert(log);
        
        return true;
    }

    private String getTicketTypeName(Integer type) {
        // 1-成人, 2-儿童, 3-老人, 4-学生, 5-其他
        if (type == null) return "";
        return switch (type) {
            case 1 -> "成人票";
            case 2 -> "儿童票";
            case 3 -> "老人票";
            case 4 -> "学生票";
            case 5 -> "其他";
            default -> "其他";
        };
    }
    
    private Integer getTicketTypeCode(String name) {
        return switch (name) {
            case "成人票" -> 1;
            case "儿童票" -> 2;
            case "老人票" -> 3;
            case "学生票" -> 4;
            case "其他" -> 5;
            default -> null; // Return null if doesn't match
        };
    }

    private String getOrderStatusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "待支付";
            case 1 -> "待核销";
            case 2 -> "已完成"; 
            case 3 -> "已取消";
            case 4 -> "已退款";
            case 5 -> "已过期";
            default -> "未知";
        };
    }
    
    private String getLogTypeText(Integer type) {
        if (type == null) return "其他";
         return switch (type) {
             case 1 -> "佣金收入";
             case 2 -> "提现支出";
             case 3 -> "平台费用";
             default -> "其他";
         };
    }
}
