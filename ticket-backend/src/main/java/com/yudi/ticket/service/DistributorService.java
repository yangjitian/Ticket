package com.yudi.ticket.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yudi.ticket.model.vo.DistributorOrderVO;
import com.yudi.ticket.model.vo.DistributorOverviewVO;
import com.yudi.ticket.model.vo.DistributorProductVO;
import com.yudi.ticket.model.vo.DistributorWalletVO;
import java.math.BigDecimal;

public interface DistributorService {
    /**
     * get products
     */
    Page<DistributorProductVO> getProductList(int page, int size, Long distributorId, String keyword, String ticketType);

    /**
     * get orders
     */
    Page<DistributorOrderVO> getOrderList(int page, int size, Long distributorId);

    /**
     * get overview
     */
    DistributorOverviewVO getOverview(Long distributorId);

    /**
     * get wallet
     */
    DistributorWalletVO getWallet(Long distributorId, String month);

    /**
     * simulate withdrawal
     */
    boolean withdraw(Long distributorId, BigDecimal amount);
}
