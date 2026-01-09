package com.yudi.ticket.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yudi.ticket.common.ErrorCode;
import com.yudi.ticket.exception.BusinessException;
import com.yudi.ticket.mapper.ScenicSpotMapper;
import com.yudi.ticket.mapper.ScenicStockMapper;
import com.yudi.ticket.model.dto.BatchScenicStockUpdateRequest;
import com.yudi.ticket.model.entity.ScenicSpot;
import com.yudi.ticket.model.entity.ScenicStock;
import com.yudi.ticket.service.ScenicStockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDate;

/**
* @author yudi
* @description 针对表【bus_scenic_stock(业务-景区日历库存表)】的数据库操作Service实现
* @createDate 2026-01-05 09:22:56
*/
@Service
@Slf4j
public class ScenicStockServiceImpl extends ServiceImpl<ScenicStockMapper, ScenicStock>
    implements ScenicStockService{

    @Resource
    private ScenicSpotMapper scenicSpotMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateScenicStock(BatchScenicStockUpdateRequest request) {
        // 校验景区是否存在
        ScenicSpot scenicSpot = scenicSpotMapper.selectById(request.getScenicId());
        if (scenicSpot == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "景区不存在");
        }

        if (CollUtil.isEmpty(request.getStocks())) {
            return true;
        }

        // 批量更新或插入库存记录
        for (BatchScenicStockUpdateRequest.StockItem stockItem : request.getStocks()) {
            // 查找现有记录
            ScenicStock existingStock = this.getOne(
                new LambdaQueryWrapper<ScenicStock>()
                    .eq(ScenicStock::getScenicId, request.getScenicId())
                    .eq(ScenicStock::getStockDate, LocalDate.parse(stockItem.getStockDate()))
            );

            if (existingStock != null) {
                // 更新现有记录
                // 检查是否减少库存到已售数量以下
                if (stockItem.getTotalQty() < existingStock.getSoldQty()) {
                    throw new BusinessException(ErrorCode.PARAM_ERROR,
                        String.format("日期 %s 的库存不能小于已售数量 %d", stockItem.getStockDate(), existingStock.getSoldQty()));
                }

                existingStock.setTotalQty(stockItem.getTotalQty());
                existingStock.setLockVersion(existingStock.getLockVersion() + 1);
                this.updateById(existingStock);
            } else {
                // 创建新记录
                if (stockItem.getTotalQty() < 0) {
                    throw new BusinessException(ErrorCode.PARAM_ERROR, "库存数量不能为负数");
                }

                ScenicStock newStock = new ScenicStock();
                newStock.setScenicId(request.getScenicId());
                newStock.setStockDate(LocalDate.parse(stockItem.getStockDate()));
                newStock.setTotalQty(stockItem.getTotalQty());
                newStock.setSoldQty(0);
                newStock.setLockVersion(0);
                this.save(newStock);
            }
        }

        return true;
    }
}
