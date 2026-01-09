package com.yudi.ticket.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yudi.ticket.aop.RequireAdmin;
import com.yudi.ticket.common.BaseResponse;
import com.yudi.ticket.common.ErrorCode;
import com.yudi.ticket.common.ThrowUtils;
import com.yudi.ticket.exception.BusinessException;
import com.yudi.ticket.model.dto.TicketPriceUpdateRequest;
import com.yudi.ticket.model.dto.TicketProductQueryRequest;
import com.yudi.ticket.model.dto.TicketProductUpdateRequest;
import com.yudi.ticket.model.entity.TicketProduct;
import com.yudi.ticket.model.vo.ScenicSpotTicketsVO;
import com.yudi.ticket.model.vo.TicketProductVO;
import com.yudi.ticket.service.TicketProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * 门票产品控制器
 */
@RestController
@RequestMapping("/ticketProduct")
@Slf4j
public class TicketProductController {

    @Resource
    private TicketProductService ticketProductService;

    @PostMapping("/list")
    public BaseResponse<IPage<TicketProductVO>> listTicketProducts(@RequestBody TicketProductQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAM_ERROR);
        IPage<TicketProductVO> page = ticketProductService.queryTicketProductPage(request);
        return BaseResponse.success(page);
    }

    @GetMapping("/get")
    public BaseResponse<TicketProductVO> getTicketProductById(@RequestParam Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAM_ERROR);
        TicketProductVO ticketProduct = ticketProductService.getTicketProductById(id);
        return BaseResponse.success(ticketProduct);
    }

    @PostMapping("/update")
    @RequireAdmin
    public BaseResponse<Boolean> updateTicketProduct(@RequestBody TicketProductUpdateRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAM_ERROR);

        boolean result = ticketProductService.updateTicketProduct(request);
        return BaseResponse.success(result);
    }

    @PostMapping("/update/price")
    @RequireAdmin
    public BaseResponse<Boolean> updateTicketPrice(@RequestBody TicketPriceUpdateRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAM_ERROR);
        ThrowUtils.throwIf(request.getId() == null, ErrorCode.PARAM_ERROR, "产品ID不能为空");
        ThrowUtils.throwIf(request.getPriceRetail() == null, ErrorCode.PARAM_ERROR, "零售价不能为空");
        ThrowUtils.throwIf(request.getPriceSettlement() == null, ErrorCode.PARAM_ERROR, "结算价不能为空");

        // 校验价格逻辑：结算价不能高于零售价
        if (request.getPriceSettlement().compareTo(request.getPriceRetail()) > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "平台结算价不能高于官网零售价");
        }

        boolean result = ticketProductService.updateTicketPrice(request);
        return BaseResponse.success(result);
    }

    @PostMapping("/toggle/status")
    @RequireAdmin
    public BaseResponse<Boolean> toggleTicketSaleStatus(@RequestBody TicketProduct request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAM_ERROR);
        ThrowUtils.throwIf(request.getId() == null, ErrorCode.PARAM_ERROR, "产品ID不能为空");
        ThrowUtils.throwIf(request.getSaleStatus() == null, ErrorCode.PARAM_ERROR, "销售状态不能为空");

        boolean result = ticketProductService.toggleSaleStatus(request.getId(), request.getSaleStatus());
        return BaseResponse.success(result);
    }

    @PostMapping("/delete")
    @RequireAdmin
    public BaseResponse<Boolean> deleteTicketProduct(@RequestBody TicketProduct request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAM_ERROR);
        ThrowUtils.throwIf(request.getId() == null, ErrorCode.PARAM_ERROR, "产品ID不能为空");

        // 逻辑删除
        boolean result = ticketProductService.removeById(request.getId());
        return BaseResponse.success(result);
    }

    @PostMapping("/scenicSpot/tickets/list")
    public BaseResponse<IPage<ScenicSpotTicketsVO>> listScenicSpotTickets(@RequestBody TicketProductQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAM_ERROR);
        IPage<ScenicSpotTicketsVO> page = ticketProductService.queryScenicSpotTicketsPage(request);
        return BaseResponse.success(page);
    }
}
