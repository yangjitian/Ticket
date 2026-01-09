package com.yudi.ticket.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批量景区库存更新请求
 */
@Data
public class BatchScenicStockUpdateRequest implements Serializable {

    /**
     * 景区ID
     */
    private Long scenicId;

    /**
     * 库存数据列表
     */
    private List<StockItem> stocks;

    @Data
    public static class StockItem implements Serializable {
        /**
         * 库存日期
         */
        private String stockDate;

        /**
         * 总库存量
         */
        private Integer totalQty;

        private static final long serialVersionUID = 1L;
    }

    private static final long serialVersionUID = 1L;
}
