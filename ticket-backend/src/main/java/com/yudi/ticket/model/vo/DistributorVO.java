package com.yudi.ticket.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 经销商信息视图对象
 */
@Data
public class DistributorVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 邮箱账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 用户角色（固定为2-分销商）
     */
    private Integer role = 2;

    /**
     * 账号状态（0-禁用, 1-正常）
     */
    private Integer userStatus;

    /**
     * 分销商审核状态（0-待审核, 1-通过, 2-拒绝）
     */
    private Integer auditStatus;

    /**
     * 注册时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 总订单数
     */
    private Integer orderCount;

    /**
     * 总销售额
     */
    private Long salesAmount;

    /**
     * 总佣金
     */
    private Long commission;
}
