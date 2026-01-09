package com.yudi.ticket.controller;

import com.yudi.ticket.common.BaseResponse;
import com.yudi.ticket.common.ErrorCode;
import com.yudi.ticket.common.ThrowUtils;
import com.yudi.ticket.model.dto.EmailRequest;
import com.yudi.ticket.service.EmailService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/email")
@Slf4j
public class EmailController {

    @Resource
    private EmailService emailService;


    @PostMapping("/send-code")
    public BaseResponse<Boolean> sendCode(@RequestBody EmailRequest emailRequest) {
        ThrowUtils.throwIf(emailRequest == null, ErrorCode.PARAM_ERROR);
        String email = emailRequest.getUserAccount();
        ThrowUtils.throwIf(email == null, ErrorCode.PARAM_ERROR, "邮箱不能为空");
        //        ThrowUtils.throwIf(!Validator.isEmail(email),ErrorCode.PARAM_ERROR);
        emailService.sendRegisterCode(email);
        return BaseResponse.success(true);
    }

    @PostMapping("/send-resetCode")
    public BaseResponse<Boolean> sendResetCode(@RequestBody EmailRequest emailRequest) {
        ThrowUtils.throwIf(emailRequest == null, ErrorCode.PARAM_ERROR);
        String email = emailRequest.getUserAccount();
        emailService.sendResetCode(email);
        return BaseResponse.success(true);
    }

    @PostMapping("/send-changeCode")
    public BaseResponse<Boolean> sendChangeCode(@RequestBody EmailRequest emailRequest) {
        log.info("收到邮箱换绑验证码请求: {}", emailRequest);
        ThrowUtils.throwIf(emailRequest == null, ErrorCode.PARAM_ERROR);
        String email = emailRequest.getUserAccount();
        log.info("发送换绑验证码到邮箱: {}", email);
        ThrowUtils.throwIf(email == null, ErrorCode.PARAM_ERROR, "邮箱不能为空");
        emailService.sendChangeCode(email);
        log.info("邮箱换绑验证码发送完成: {}", email);
        return BaseResponse.success(true);
    }
}
