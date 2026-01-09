package com.yudi.ticket.mapper;

import com.yudi.ticket.model.entity.Order;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Asus
* @description 针对表【bus_order(业务-订单主表)】的数据库操作Mapper
* @createDate 2026-01-05 15:53:49
* @Entity com.yudi.ticket.model.entity.Order
*/

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

}




