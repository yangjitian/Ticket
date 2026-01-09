package com.yudi.ticket.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 业务-订单游客实名表
 * @TableName bus_order_visitor
 */
@TableName(value ="bus_order_visitor")
@Data
public class OrderVisitor implements Serializable {

    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 游客姓名
     */
    private String visitorName;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 联系电话
     */
    private String contactTel;

    @TableField(exist = false)
    @Serial
    private static final long serialVersionUID = 1L;
}
