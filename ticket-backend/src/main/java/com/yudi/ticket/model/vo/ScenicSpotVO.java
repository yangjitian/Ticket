package com.yudi.ticket.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 景点信息响应
 */
@Data
public class ScenicSpotVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 景点ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 景区名称
     */
    private String spotName;

    /**
     * 景区等级，如：5A、4A、国家级等
     */
    private String level;

    /**
     * 景区所属省份（如：浙江省、北京市）
     */
    private String province;

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

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
