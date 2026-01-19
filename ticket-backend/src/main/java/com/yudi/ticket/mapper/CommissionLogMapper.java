package com.yudi.ticket.mapper;

import com.yudi.ticket.model.entity.CommissionLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author admin
* @description 针对表【bus_commission_log(业务-佣金记录流水表)】的数据库操作Mapper
* @createDate 2024-12-01 10:00:00
* @Entity com.yudi.ticket.model.entity.CommissionLog
*/
@Mapper
public interface CommissionLogMapper extends BaseMapper<CommissionLog> {

}
