package com.yudi.ticket.mapper;

import com.yudi.ticket.model.entity.ScenicStock;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author yudi
* @description 针对表【bus_scenic_stock(业务-景区日历库存表)】的数据库操作Mapper
* @createDate 2026-01-05 09:22:56
* @Entity com.yudi.ticket.model.entity.ScenicStock
*/
@Mapper
public interface ScenicStockMapper extends BaseMapper<ScenicStock> {

}
