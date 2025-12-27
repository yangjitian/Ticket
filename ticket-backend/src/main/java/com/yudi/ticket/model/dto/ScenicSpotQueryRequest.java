package com.yudi.ticket.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 景点查询请求
 */
@Data
public class ScenicSpotQueryRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    private long current = 1;

    /**
     * 页面大小
     */
    private long pageSize = 10;

    /**
     * 景点ID
     */
    private Long id;

    /**
     * 景区名称（模糊查询）
     */
    private String spotName;

    /**
     * 地址（模糊查询）
     */
    private String address;

    /**
     * 状态: 1-开放, 0-关闭
     */
    private Integer spotStatus;
}
