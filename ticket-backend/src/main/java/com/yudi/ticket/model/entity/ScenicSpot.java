package com.yudi.ticket.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 业务-景区信息表
 * @TableName bus_scenic_spot
 */
@TableName(value ="bus_scenic_spot")
@Data
public class ScenicSpot implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.ASSIGN_ID)
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
     * 图片地址JSON数组
     */
    @TableField("image_urls")
    private String imageUrls;

    /**
     * 状态: 1-开放, 0-关闭
     */
    private Integer spotStatus;

    /**
     * 
     */
    private LocalDateTime createTime;

    /**
     * 
     */
    private LocalDateTime updateTime;

    /**
     * 
     */
    private Integer isDeleted;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}