package com.yudi.ticket.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 用户基础表
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 登录邮箱(账号)
     */
    private String userAccount;

    /**
     * 加密存储的密码
     */
    private String password;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 经销商钱包余额（仅分销商role=2有效）
     */
    private BigDecimal walletBalance;

    /**
     * 分销商等级: 1-金（提成0.3倍官网价）, 2-银（0.25倍）, 3-铜（0.2倍）, NULL-不适用
     */
    private Integer distributorLevel;

    /**
     * 角色: 0-管理员, 1-游客, 2-分销商
     */
    private Integer role;

    /**
     * 账号状态: 0-禁用, 1-正常
     */
    private Integer userStatus;

    /**
     * 分销商审核状态: 0-待审核, 1-通过, 2-拒绝（仅分销商有效）
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
     * 逻辑删除标记
     */
    private Integer isDeleted;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}