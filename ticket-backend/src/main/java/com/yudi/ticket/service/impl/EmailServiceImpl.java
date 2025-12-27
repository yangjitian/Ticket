package com.yudi.ticket.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.yudi.ticket.service.EmailService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.from}")
    private String from;

    private static final String EMAIL_CODE_KEY = "reg:code";
    private static final String RESET_PASSWORD_CODE_KEY = "reset:code";

    @Override
    public void sendRegisterCode(String email) {
        // 1.生成验证码 存入Redis 5分钟过期
        String code = RandomUtil.randomNumbers(6);
        String codeKey = EMAIL_CODE_KEY + email;
        stringRedisTemplate.opsForValue().set(codeKey, code,5, TimeUnit.MINUTES);
        // 2.异步发送邮件
        String subject = "【聆音止梦景区售票】注册验证码";
        String content = "您好！您的验证码为：" + code + "，有效期5分钟。若非本人操作请忽略，切勿泄露！";
        sendEmail(email, subject, content);
        log.info("验证码{},邮件已发送至 {}",code, email);
    }

    @Override
    public void sendResetCode(String email) {
        // 1.生成验证码 存入Redis 5分钟过期
        String code = RandomUtil.randomNumbers(6);
        String codeKey = RESET_PASSWORD_CODE_KEY + email;
        stringRedisTemplate.opsForValue().set(codeKey, code, 5, TimeUnit.MINUTES);
        // 2.异步发送邮件
        String subject = "【聆音止梦景区售票】重置密码验证码";
        String content = "您好！您正在进行找回密码操作，验证码为：" + code + "，有效期5分钟。若非本人操作请忽略，切勿泄露！";
        sendEmail(email, subject, content);
        log.info("重置密码验证码{},邮件已发送至 {}", code, email);
    }

    @Override
    public void sendAuditPassNotification(String email) {
        String subject = "【聆音止梦景区售票】经销商资格审核通过通知";
        String content = "您好！您的经销商资格审核已通过，现在可以登录系统进行业务操作了。";
        sendEmail(email, subject, content);
        log.info("审核通过通知邮件已发送至 {}", email);
    }

    @Async
    public void sendEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);

            javaMailSender.send(message);
            log.info("邮件发送成功，收件人：{}，主题：{}", to, subject);
        } catch (Exception e) {
            log.error("邮件发送失败，收件人：{}，主题：{}，错误原因：{}", to, subject, e.getMessage());
        }
    }
}
