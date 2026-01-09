package com.yudi.ticket.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户信息视图对象
 */
@Data
public class UserVO implements Serializable {

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
     * 用户角色（0-管理员, 1-游客, 2-分销商）
     */
    private Integer role;

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
    private Date createTime;

    /**
     * 最后更新时间
     */
    private Date updateTime;
}
