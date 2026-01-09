package com.yudi.ticket.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户登录视图对象
 */
@Data
public class UserLoginVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String userAccount;

    private String nickname;

    @JsonSerialize(using = ToStringSerializer.class)
    private Integer role;

    @JsonSerialize(using = ToStringSerializer.class)
    private Integer userStatus;

    /**
     * 分销商审核状态（0-待审核, 1-通过, 2-拒绝）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Integer auditStatus;

    private String avatar;

    private String token;
}