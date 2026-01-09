package com.yudi.ticket.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.Data;

/**
 * 业务-景区日历库存表
 * @TableName bus_scenic_stock
 */
@TableName(value ="bus_scenic_stock")
@Data
public class ScenicStock implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联景区ID
     */
    private Long scenicId;

    /**
     * 库存日期
     */
    private LocalDate stockDate;

    /**
     * 总库存量
     */
    private Integer totalQty;

    /**
     * 已售数量
     */
    private Integer soldQty;

    /**
     * 乐观锁版本
     */
    private Integer lockVersion;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
