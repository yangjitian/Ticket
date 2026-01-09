package com.yudi.ticket.service;

public interface EmailService {

    /**
     * 发送验证码
     * @param email
     */
    void sendRegisterCode(String email);

    /**
     * 发送重置密码验证码
     * @param email
     */
    void sendResetCode(String email);

    /**
     * 发送邮箱换绑验证码
     * @param email
     */
    void sendChangeCode(String email);

    /**
     * 发送审核通过通知
     * @param email
     */
    void sendAuditPassNotification(String email);
}
