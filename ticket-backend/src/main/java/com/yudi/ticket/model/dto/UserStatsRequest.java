package com.yudi.ticket.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户统计请求
 */
@Data
public class UserStatsRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 统计天数（7-30天）
     */
    private Integer days = 7;
}
