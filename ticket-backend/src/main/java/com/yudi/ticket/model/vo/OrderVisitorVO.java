package com.yudi.ticket.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 订单游客信息视图对象
 */
@Data
public class OrderVisitorVO implements Serializable {

    /**
     * 游客姓名
     */
    private String visitorName;

    /**
     * 身份证号（脱敏显示）
     */
    private String idCard;

    /**
     * 联系电话
     */
    private String contactTel;

    /**
     * 核销状态：0-未核销, 1-已核销
     */
    private Integer verifyStatus;

    /**
     * 核销时间
     */
    private String verifyTime;

    private static final long serialVersionUID = 1L;
}
