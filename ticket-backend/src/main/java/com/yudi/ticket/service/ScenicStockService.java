package com.yudi.ticket.service;

import com.yudi.ticket.model.dto.BatchScenicStockUpdateRequest;
import com.yudi.ticket.model.entity.ScenicStock;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author yudi
* @description 针对表【bus_scenic_stock(业务-景区日历库存表)】的数据库操作Service
* @createDate 2026-01-05 09:22:56
*/
public interface ScenicStockService extends IService<ScenicStock> {

    /**
     * 批量更新景区库存
     */
    boolean batchUpdateScenicStock(BatchScenicStockUpdateRequest request);
}
