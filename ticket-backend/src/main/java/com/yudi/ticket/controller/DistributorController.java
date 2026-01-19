package com.yudi.ticket.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yudi.ticket.aop.RequireLogin;
import com.yudi.ticket.common.BaseResponse;
import com.yudi.ticket.common.ErrorCode;
import com.yudi.ticket.common.PageResponse;
import com.yudi.ticket.common.ThrowUtils;
import com.yudi.ticket.model.vo.DistributorOrderVO;
import com.yudi.ticket.model.vo.DistributorOverviewVO;
import com.yudi.ticket.model.vo.DistributorProductVO;
import com.yudi.ticket.model.vo.DistributorWalletVO;
import com.yudi.ticket.service.DistributorService;
import com.yudi.ticket.model.dto.WithdrawRequest;
import com.yudi.ticket.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/distributor")
@Slf4j
public class DistributorController {

    @Resource
    private DistributorService distributorService;

    // Helper to get User Id
    private Long getLoginUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        ThrowUtils.throwIf(StrUtil.isBlank(token), ErrorCode.UNAUTHORIZED);
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return JwtUtils.getUserId(token);
    }
    
    @GetMapping("/products")
    @RequireLogin
    public BaseResponse<PageResponse<DistributorProductVO>> getProducts(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String ticketType,
            HttpServletRequest request) {
        Long distributorId = getLoginUserId(request);
        Page<DistributorProductVO> result = distributorService.getProductList(current, pageSize, distributorId, keyword, ticketType);
        return BaseResponse.success(new PageResponse<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @GetMapping("/orders")
    @RequireLogin
    public BaseResponse<PageResponse<DistributorOrderVO>> getOrders(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        Long distributorId = getLoginUserId(request);
        Page<DistributorOrderVO> result = distributorService.getOrderList(current, pageSize, distributorId);
        return BaseResponse.success(new PageResponse<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @GetMapping("/overview")
    @RequireLogin
    public BaseResponse<DistributorOverviewVO> getOverview(HttpServletRequest request) {
        Long distributorId = getLoginUserId(request);
        DistributorOverviewVO result = distributorService.getOverview(distributorId);
        return BaseResponse.success(result);
    }

    @GetMapping("/wallet")
    @RequireLogin
    public BaseResponse<DistributorWalletVO> getWallet(
            @RequestParam(required = false) String month,
            HttpServletRequest request) {
        Long distributorId = getLoginUserId(request);
        DistributorWalletVO result = distributorService.getWallet(distributorId, month);
        return BaseResponse.success(result);
    }

    @PostMapping("/withdraw")
    @RequireLogin
    public BaseResponse<Boolean> withdraw(@RequestBody WithdrawRequest withdrawRequest, HttpServletRequest request) {
        if (withdrawRequest == null || withdrawRequest.getAmount() == null || withdrawRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Invalid amount");
        }
        Long distributorId = getLoginUserId(request);
        boolean success = distributorService.withdraw(distributorId, withdrawRequest.getAmount());
        if (!success) {
            return BaseResponse.error(ErrorCode.BALANCE_NOT_ENOUGH, "余额不足");
        }
        return BaseResponse.success(true);
    }

}
