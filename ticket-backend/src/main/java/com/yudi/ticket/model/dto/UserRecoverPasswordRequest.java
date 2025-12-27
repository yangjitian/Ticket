package com.yudi.ticket.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户找回密码请求
 */
@Data
public class UserRecoverPasswordRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 账号（邮箱）
     */
    private String userAccount;

    /**
     * 验证码
     */
    private String verificationCode;

    private String newPassword;

    private String checkPassword;
}