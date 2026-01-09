package com.yudi.ticket.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户游客信息删除请求
 */
@Data
public class UserVisitorDeleteRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;
}
