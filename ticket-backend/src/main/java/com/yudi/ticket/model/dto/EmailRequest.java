package com.yudi.ticket.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户发送验证码请求
 */
@Data
public class EmailRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String userAccount;
}