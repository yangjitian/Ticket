package com.yudi.ticket.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户审核请求
 */
@Data
public class UserAuditRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户 id
     */
    private Long id;

    /**
     * 审核状态（1-通过 2-拒绝）
     */
    private Integer auditStatus;
}