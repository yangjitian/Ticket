package com.yudi.ticket.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户查询请求
 */
@Data
public class UserQueryRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 当前页号
     */
    private Long current = 1L;

    /**
     * 页面大小
     */
    private Long pageSize = 10L;

    /**
     * 搜索关键词（邮箱或昵称）
     */
    private String searchKey;

    /**
     * 角色筛选
     */
    private Integer role;

    /**
     * 用户状态筛选
     */
    private Integer userStatus;

    /**
     * 审核状态筛选（经销商专用）
     */
    private Integer auditStatus;
}
