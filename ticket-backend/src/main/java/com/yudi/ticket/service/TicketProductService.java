package com.yudi.ticket.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yudi.ticket.model.dto.TicketPriceUpdateRequest;
import com.yudi.ticket.model.dto.TicketProductQueryRequest;
import com.yudi.ticket.model.dto.TicketProductUpdateRequest;
import com.yudi.ticket.model.entity.TicketProduct;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yudi.ticket.model.vo.ScenicSpotTicketsVO;
import com.yudi.ticket.model.vo.TicketProductVO;

/**
 * @author yudi
 * @createDate 2026-01-05 09:22:56
 */
public interface TicketProductService extends IService<TicketProduct> {

    /**
     * 分页查询门票产品
     */
    IPage<TicketProductVO> queryTicketProductPage(TicketProductQueryRequest request);

    /**
     * 根据ID获取门票产品详情
     */
    TicketProductVO getTicketProductById(Long id);

    /**
     * 更新门票产品信息
     */
    boolean updateTicketProduct(TicketProductUpdateRequest request);

    /**
     * 更新门票价格
     */
    boolean updateTicketPrice(TicketPriceUpdateRequest request);

    /**
     * 切换销售状态
     */
    boolean toggleSaleStatus(Long productId, Integer saleStatus);

    /**
     * 分页查询景区门票汇总信息
     */
    IPage<ScenicSpotTicketsVO> queryScenicSpotTicketsPage(TicketProductQueryRequest request);
}
