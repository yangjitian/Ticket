package com.yudi.ticket.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yudi.ticket.aop.RequireAdmin;
import com.yudi.ticket.common.BaseResponse;
import com.yudi.ticket.common.ErrorCode;
import com.yudi.ticket.common.ThrowUtils;
import com.yudi.ticket.exception.BusinessException;
import com.yudi.ticket.model.dto.BatchScenicStockUpdateRequest;
import com.yudi.ticket.model.dto.ScenicStockQueryRequest;
import com.yudi.ticket.model.entity.ScenicStock;
import com.yudi.ticket.service.ScenicStockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

/**
 * 景区库存控制器
 */
@RestController
@RequestMapping("/scenicStock")
@Slf4j
public class ScenicStockController {

    @Resource
    private ScenicStockService scenicStockService;

    @PostMapping("/list")
    public BaseResponse<List<ScenicStock>> listScenicStocks(@RequestBody ScenicStockQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAM_ERROR);
        ThrowUtils.throwIf(request.getScenicId() == null, ErrorCode.PARAM_ERROR, "景区ID不能为空");

        LambdaQueryWrapper<ScenicStock> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScenicStock::getScenicId, request.getScenicId());

        if (request.getStartDate() != null) {
            queryWrapper.ge(ScenicStock::getStockDate, LocalDate.parse(request.getStartDate()));
        }

        if (request.getEndDate() != null) {
            queryWrapper.le(ScenicStock::getStockDate, LocalDate.parse(request.getEndDate()));
        }

        queryWrapper.orderByAsc(ScenicStock::getStockDate);

        List<ScenicStock> stocks = scenicStockService.list(queryWrapper);
        return BaseResponse.success(stocks);
    }

    @PostMapping("/batch/update")
    @RequireAdmin
    public BaseResponse<Boolean> batchUpdateScenicStock(@RequestBody BatchScenicStockUpdateRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAM_ERROR);
        ThrowUtils.throwIf(request.getScenicId() == null, ErrorCode.PARAM_ERROR, "景区ID不能为空");
        ThrowUtils.throwIf(request.getStocks() == null || request.getStocks().isEmpty(),
            ErrorCode.PARAM_ERROR, "库存数据不能为空");

        boolean result = scenicStockService.batchUpdateScenicStock(request);
        return BaseResponse.success(result);
    }

    @GetMapping("/overview")
    public BaseResponse<List<ScenicStock>> getScenicStockOverview(@RequestParam Long scenicId) {
        ThrowUtils.throwIf(scenicId== null, ErrorCode.PARAM_ERROR, "景区ID不能为空");

        // 获取未来7天的库存数据
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(6);

        LambdaQueryWrapper<ScenicStock> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScenicStock::getScenicId, scenicId)
            .between(ScenicStock::getStockDate, today, endDate)
            .orderByAsc(ScenicStock::getStockDate);

        List<ScenicStock> stocks = scenicStockService.list(queryWrapper);
        return BaseResponse.success(stocks);
    }
}
