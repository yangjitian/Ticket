package com.yudi.ticket.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户换绑邮箱请求
 */
@Data
public class UserChangeEmailRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 新邮箱地址
     */
    private String newEmail;

    /**
     * 验证码
     */
    private String verificationCode;
}
