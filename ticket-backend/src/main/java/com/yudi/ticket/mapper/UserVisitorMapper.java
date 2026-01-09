package com.yudi.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yudi.ticket.model.entity.UserVisitor;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户游客信息Mapper
 */
@Mapper
public interface UserVisitorMapper extends BaseMapper<UserVisitor> {

}