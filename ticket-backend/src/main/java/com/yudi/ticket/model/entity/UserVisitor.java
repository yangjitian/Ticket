package com.yudi.ticket.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户常用游客信息表
 * @TableName user_visitor
 */
@TableName(value ="user_visitor")
@Data
public class UserVisitor implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 游客姓名
     */
    private String visitorName;

    /**
     * 身份证号（建议加密存储）
     */
    private String idCard;

    /**
     * 联系电话（可选）
     */
    private String contactTel;

    /**
     * 默认票种类型：1-成人, 2-儿童, 3-老人, 4-学生, 5-其他
     */
    private Integer defaultTicketType;

    /**
     * 是否默认出行人：1-是，0-否
     */
    private Integer isDefault;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer isDeleted;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}