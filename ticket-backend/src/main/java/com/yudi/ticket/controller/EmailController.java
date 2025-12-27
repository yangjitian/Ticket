package com.yudi.ticket.controller;

import com.yudi.ticket.common.BaseResponse;
import com.yudi.ticket.common.ErrorCode;
import com.yudi.ticket.common.ThrowUtils;
import com.yudi.ticket.model.dto.EmailRequest;
import com.yudi.ticket.service.EmailService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/email")
public class EmailController {

    @Resource
    private EmailService emailService;

    @PostMapping("/send-registerCode")
    public BaseResponse<Boolean> sendRegisterCode(@RequestBody EmailRequest emailRequest) {
        ThrowUtils.throwIf(emailRequest == null, ErrorCode.PARAM_ERROR);
        String email = emailRequest.getUserAccount();
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
}
