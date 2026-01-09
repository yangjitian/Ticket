package com.yudi.ticket.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户游客信息更新请求
 */
@Data
public class UserVisitorUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 游客姓名
     */
    private String visitorName;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 联系电话（可选）
     */
    private String contactTel;

    /**
     * 默认票种类型：1-原价票, 2-半价票, 3-免票
     */
    private Integer defaultTicketType;

    /**
     * 是否默认出行人：1-是，0-否
     */
    private Integer isDefault;
}
