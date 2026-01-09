package com.yudi.ticket.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户游客信息查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserVisitorQueryRequest extends PageRequest {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 游客姓名（模糊查询）
     */
    private String visitorName;

    /**
     * 是否默认出行人
     */
    private Integer isDefault;
}
