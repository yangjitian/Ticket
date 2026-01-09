package com.yudi.ticket.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户游客信息响应
 */
@Data
public class UserVisitorVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 所属用户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    /**
     * 游客姓名
     */
    private String visitorName;

    /**
     * 身份证号（前端显示时需要脱敏）
     */
    private String idCard;

    /**
     * 联系电话
     */
    private String contactTel;

    /**
     * 默认票种类型：1-成人, 2-儿童, 3-老人, 4-学生, 5-其他
     */
    private Integer defaultTicketType;

    /**
     * 票种类型名称
     */
    private String ticketTypeName;

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
}
