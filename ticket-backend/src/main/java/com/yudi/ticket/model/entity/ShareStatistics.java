package com.yudi.ticket.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 业务-分享统计表
 * @TableName bus_share_statistics
 */
@TableName(value ="bus_share_statistics")
@Data
public class ShareStatistics implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 分销商ID
     */
    private Long distributorId;

    /**
     * 景区ID
     */
    private Long scenicId;

    /**
     * 分享码（可选，用于追踪）
     */
    private String shareCode;

    /**
     * 点击次数
     */
    private Integer clickCount;

    /**
     * 转化订单数
     */
    private Integer orderCount;

    /**
     * 统计日期
     */
    private LocalDate shareDate;

    /**
     * 
     */
    private LocalDateTime createTime;

    /**
     * 
     */
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
