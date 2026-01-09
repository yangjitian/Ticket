package com.yudi.ticket.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yudi.ticket.common.ErrorCode;
import com.yudi.ticket.exception.BusinessException;
import com.yudi.ticket.mapper.ScenicSpotMapper;
import com.yudi.ticket.mapper.TicketProductMapper;
import com.yudi.ticket.model.entity.ScenicStock;
import com.yudi.ticket.service.ScenicStockService;
import com.yudi.ticket.model.dto.TicketPriceUpdateRequest;
import com.yudi.ticket.model.dto.TicketProductQueryRequest;
import com.yudi.ticket.model.dto.TicketProductUpdateRequest;
import com.yudi.ticket.model.entity.ScenicSpot;
import com.yudi.ticket.model.entity.TicketProduct;
import com.yudi.ticket.model.vo.ScenicSpotTicketsVO;
import com.yudi.ticket.model.vo.TicketProductVO;
import com.yudi.ticket.service.TicketProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yudi
 * @createDate 2026-01-05 09:22:56
 */
@Service
@Slf4j
public class TicketProductServiceImpl extends ServiceImpl<TicketProductMapper, TicketProduct>
    implements TicketProductService {

    @Resource
    private ScenicSpotMapper scenicSpotMapper;


    @Resource
    private ScenicStockService scenicStockService;

    @Override
    public IPage<TicketProductVO> queryTicketProductPage(TicketProductQueryRequest request) {
        // 构建查询条件
        LambdaQueryWrapper<TicketProduct> queryWrapper = new LambdaQueryWrapper<>();

        // 景区ID过滤
        if (StrUtil.isNotBlank(request.getSpotId())) {
            queryWrapper.eq(TicketProduct::getSpotId, request.getSpotId());
        }

        // 产品名称模糊搜索
        if (StrUtil.isNotBlank(request.getProductName())) {
            queryWrapper.like(TicketProduct::getProductName, request.getProductName());
        }

        // 票种类型过滤
        if (request.getTicketType() != null) {
            queryWrapper.eq(TicketProduct::getTicketType, request.getTicketType());
        }

        // 销售状态过滤
        if (request.getSaleStatus() != null) {
            queryWrapper.eq(TicketProduct::getSaleStatus, request.getSaleStatus());
        }

        // 景区名称关联查询（如果提供了景区名称搜索）
        if (StrUtil.isNotBlank(request.getSpotName())) {
            List<ScenicSpot> scenicSpots = scenicSpotMapper.selectList(
                new LambdaQueryWrapper<ScenicSpot>()
                    .like(ScenicSpot::getSpotName, request.getSpotName())
            );
            if (CollUtil.isNotEmpty(scenicSpots)) {
                List<Long> spotIds = scenicSpots.stream()
                    .map(ScenicSpot::getId)
                    .collect(Collectors.toList());
                queryWrapper.in(TicketProduct::getSpotId, spotIds);
            } else {
                // 如果没有匹配的景区，返回空结果
                return new Page<>(request.getCurrent(), request.getPageSize(), 0);
            }
        }

        queryWrapper.orderByDesc(TicketProduct::getUpdateTime);

        // 分页查询
        Page<TicketProduct> page = new Page<>(request.getCurrent(), request.getPageSize());
        Page<TicketProduct> productPage = this.page(page, queryWrapper);

        // 转换为VO
        Page<TicketProductVO> voPage = new Page<>(productPage.getCurrent(), productPage.getPages(), productPage.getTotal());
        List<TicketProductVO> voList = productPage.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public TicketProductVO getTicketProductById(Long id) {
        TicketProduct ticketProduct = this.getById(id);
        if (ticketProduct == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "门票产品不存在");
        }
        return convertToVO(ticketProduct);
    }

    @Override
    public boolean updateTicketProduct(TicketProductUpdateRequest request) {
        if (request.getId() == null || "".equals(request.getId())) {
            // 新增操作
            TicketProduct newProduct = new TicketProduct();
            BeanUtil.copyProperties(request, newProduct, "id");
            return this.save(newProduct);
        } else {
            // 更新操作
            TicketProduct ticketProduct = this.getById(request.getId());
            if (ticketProduct == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "门票产品不存在");
            }

            // 更新字段
            BeanUtil.copyProperties(request, ticketProduct, "id", "spotId", "createTime");
            ticketProduct.setUpdateTime(null); // 让数据库自动更新

            return this.updateById(ticketProduct);
        }
    }

    @Override
    public boolean updateTicketPrice(TicketPriceUpdateRequest request) {
        TicketProduct ticketProduct = this.getById(request.getId());
        if (ticketProduct == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "门票产品不存在");
        }

        ticketProduct.setPriceRetail(request.getPriceRetail());
        ticketProduct.setPriceSettlement(request.getPriceSettlement());
        ticketProduct.setUpdateTime(null); // 让数据库自动更新

        return this.updateById(ticketProduct);
    }

    @Override
    public boolean toggleSaleStatus(Long productId, Integer saleStatus) {
        TicketProduct ticketProduct = this.getById(productId);
        if (ticketProduct == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "门票产品不存在");
        }

        ticketProduct.setSaleStatus(saleStatus);
        ticketProduct.setUpdateTime(null); // 让数据库自动更新

        return this.updateById(ticketProduct);
    }

    @Override
    public IPage<ScenicSpotTicketsVO> queryScenicSpotTicketsPage(TicketProductQueryRequest request) {
        // 首先查询景区（带分页）
        LambdaQueryWrapper<ScenicSpot> spotQueryWrapper = new LambdaQueryWrapper<>();

        // 景区ID过滤
        if (StrUtil.isNotBlank(request.getSpotId())) {
            spotQueryWrapper.eq(ScenicSpot::getId, request.getSpotId());
        }

        // 景区名称模糊搜索
        if (StrUtil.isNotBlank(request.getSpotName())) {
            spotQueryWrapper.like(ScenicSpot::getSpotName, request.getSpotName());
        }

        spotQueryWrapper.orderByDesc(ScenicSpot::getUpdateTime);

        // 分页查询景区
        Page<ScenicSpot> spotPage = new Page<>(request.getCurrent(), request.getPageSize());
        Page<ScenicSpot> scenicSpotPage = scenicSpotMapper.selectPage(spotPage, spotQueryWrapper);

        // 为每个景区查询门票产品和库存信息
        List<ScenicSpotTicketsVO> voList = scenicSpotPage.getRecords().stream()
            .map(scenicSpot -> {
                ScenicSpotTicketsVO vo = new ScenicSpotTicketsVO();
                vo.setScenicSpot(scenicSpot);

                // 查询该景区的所有门票产品
                List<TicketProduct> products = this.list(
                    new LambdaQueryWrapper<TicketProduct>()
                        .eq(TicketProduct::getSpotId, scenicSpot.getId())
                        .orderByDesc(TicketProduct::getUpdateTime)
                );

                List<TicketProductVO> productVOs = products.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
                vo.setTicketProducts(productVOs);

                // 计算未来7天库存概览
                ScenicSpotTicketsVO.FutureStockSummary summary = calculateFutureStockSummary(products, scenicSpot.getId());
                vo.setFutureStockSummary(summary);

                return vo;
            })
            .collect(Collectors.toList());

        // 转换为分页结果
        Page<ScenicSpotTicketsVO> resultPage = new Page<>(scenicSpotPage.getCurrent(), scenicSpotPage.getSize(), scenicSpotPage.getTotal());
        resultPage.setRecords(voList);

        return resultPage;
    }

    /**
     * 计算未来7天库存概览
     */
    private ScenicSpotTicketsVO.FutureStockSummary calculateFutureStockSummary(List<TicketProduct> products, Long scenicId) {
        ScenicSpotTicketsVO.FutureStockSummary summary = new ScenicSpotTicketsVO.FutureStockSummary();
        summary.setTotalProducts(products.size());

        if (CollUtil.isEmpty(products)) {
            summary.setDaysWithStock(0);
            summary.setTotalStockQty(0);
            summary.setSoldOutDays(0);
            return summary;
        }

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(6); // 未来7天（包括今天）

        // 查询景区未来7天总库存
        List<ScenicStock> stocks = scenicStockService.list(
            new LambdaQueryWrapper<ScenicStock>()
                .eq(ScenicStock::getScenicId, scenicId)
                .between(ScenicStock::getStockDate, today, endDate)
        );

        // 按日期分组统计
        Map<LocalDate, ScenicStock> stocksByDate = stocks.stream()
            .collect(Collectors.toMap(ScenicStock::getStockDate, stock -> stock));

        int totalStockQty = 0;
        int soldOutDays = 0;
        int daysWithStock = 0;

        // 检查每一天
        for (LocalDate date = today; !date.isAfter(endDate); date = date.plusDays(1)) {
            ScenicStock dayStock = stocksByDate.get(date);
            if (dayStock != null) {
                // 计算当天景区总库存
                int dayTotalStock = dayStock.getTotalQty() - dayStock.getSoldQty();

                totalStockQty += dayTotalStock;

                if (dayTotalStock <= 0) {
                    soldOutDays++;
                } else {
                    daysWithStock++;
                }
            } else {
                // 如果没有库存记录，视为售罄
                soldOutDays++;
            }
        }

        summary.setDaysWithStock(daysWithStock);
        summary.setTotalStockQty(totalStockQty);
        summary.setSoldOutDays(soldOutDays);

        return summary;
    }

    /**
     * 转换为VO（包含景区信息）
     */
    private TicketProductVO convertToVO(TicketProduct ticketProduct) {
        TicketProductVO vo = new TicketProductVO();
        BeanUtil.copyProperties(ticketProduct, vo);

        // 查询景区信息
        ScenicSpot scenicSpot = scenicSpotMapper.selectById(ticketProduct.getSpotId());
        vo.setScenicSpot(scenicSpot);

        return vo;
    }
}




