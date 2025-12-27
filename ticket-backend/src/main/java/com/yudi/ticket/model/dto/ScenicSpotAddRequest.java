package com.yudi.ticket.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 景点添加请求
 */
@Data
public class ScenicSpotAddRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 景区名称
     */
    private String spotName;

    /**
     * 详情介绍
     */
    private String description;

    /**
     * 地址
     */
    private String address;

    /**
     * 开放时间说明
     */
    private String openTime;

    /**
     * 联系电话
     */
    private String contactNum;

    /**
     * 图片地址列表
     */
    private List<String> imageUrls;

    /**
     * 状态: 1-开放, 0-关闭
     */
    private Integer spotStatus;
}
