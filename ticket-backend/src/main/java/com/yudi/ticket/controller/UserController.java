package com.yudi.ticket.controller;

import cn.hutool.core.util.StrUtil;
import com.yudi.ticket.aop.RequireAdmin;
import com.yudi.ticket.common.BaseResponse;
import com.yudi.ticket.common.ErrorCode;
import com.yudi.ticket.common.ThrowUtils;
import com.yudi.ticket.model.dto.*;
import com.yudi.ticket.model.dto.UserAuditRequest;
import com.yudi.ticket.model.enums.UserType;
import com.yudi.ticket.model.vo.UserLoginVO;
import com.yudi.ticket.service.UserService;
import com.yudi.ticket.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public BaseResponse<Long> register(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAM_ERROR);
        Long userId = userService.userRegister(userRegisterRequest);
        return BaseResponse.success(userId);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public BaseResponse<UserLoginVO> login(@RequestBody UserLoginRequest userLoginRequest) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAM_ERROR);
        UserLoginVO userLoginVO = userService.userLogin(userLoginRequest);
        return BaseResponse.success(userLoginVO);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> logout(HttpServletRequest request) {
        String token = getToken(request);
        Long userId = JwtUtils.getUserId(token);
        // 校验 Redis 状态
        boolean isLoggedIn = userService.checkLoginStatus(userId, token);
        ThrowUtils.throwIf(!isLoggedIn, ErrorCode.UNAUTHORIZED, "登录已失效");
        
        userService.userLogout(userId);
        return BaseResponse.success(true);
    }

    /**
     * 更新用户头像
     */
    @PostMapping("/update/avatar")
    public BaseResponse<String> updateAvatar(@RequestPart("file") MultipartFile multipartFile, HttpServletRequest request) {
        String token = getToken(request);
        Long userId = JwtUtils.getUserId(token);
        // 校验 Redis 状态
        boolean isLoggedIn = userService.checkLoginStatus(userId, token);
        ThrowUtils.throwIf(!isLoggedIn, ErrorCode.UNAUTHORIZED);

        String url = userService.updateAvatar(multipartFile, userId);
        return BaseResponse.success(url);
    }

    /**
     * 更新个人信息
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyInfo(@RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        String token = getToken(request);
        Long userId = JwtUtils.getUserId(token);
        ThrowUtils.throwIf(userId == null, ErrorCode.UNAUTHORIZED);

        userService.updateMyInfo(userUpdateRequest, userId);
        return BaseResponse.success(true);
    }

    /**
     * 修改密码
     */
    @PostMapping("/update/password")
    public BaseResponse<Boolean> updatePassword(@RequestBody UserUpdatePasswordRequest userUpdatePasswordRequest, HttpServletRequest request) {
        String token = getToken(request);
        Long userId = JwtUtils.getUserId(token);
        ThrowUtils.throwIf(userId == null, ErrorCode.UNAUTHORIZED);

        userService.updatePassword(userUpdatePasswordRequest, userId);
        return BaseResponse.success(true);
    }

    /**
     * 用户找回密码
     */
    @PostMapping("/recover-password")
    public BaseResponse<Boolean> recoverPassword(@RequestBody UserRecoverPasswordRequest userRecoverPasswordRequest) {
        ThrowUtils.throwIf(userRecoverPasswordRequest == null, ErrorCode.PARAM_ERROR);
        userService.userRecoverPassword(userRecoverPasswordRequest);
        return BaseResponse.success(true);
    }

    /**
     * 更新用户状态（管理员）
     */
    @PostMapping("/update/status")
    @RequireAdmin
    public BaseResponse<Boolean> updateUserStatus(@RequestBody UserStatusUpdateRequest userStatusUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userStatusUpdateRequest == null, ErrorCode.PARAM_ERROR);
        userService.updateUserStatus(userStatusUpdateRequest);
        return BaseResponse.success(true);
    }

    /**
     * 删除用户（管理员）
     */
    @PostMapping("/delete")
    @RequireAdmin
    public BaseResponse<Boolean> deleteUser(@RequestBody UserDeleteRequest userDeleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userDeleteRequest == null, ErrorCode.PARAM_ERROR);
        userService.deleteUser(userDeleteRequest);
        return BaseResponse.success(true);
    }

    /**
     * 审核经销商（管理员）
     */
    @PostMapping("/audit")
    @RequireAdmin
    public BaseResponse<Boolean> auditDistributor(@RequestBody UserAuditRequest userAuditRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userAuditRequest == null, ErrorCode.PARAM_ERROR);
        userService.auditDistributor(userAuditRequest);
        return BaseResponse.success(true);
    }

    /**
     * 鉴权
     * @param request
     * @return
     */
    private static String getToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        ThrowUtils.throwIf(StrUtil.isBlank(token), ErrorCode.UNAUTHORIZED);
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        log.info("接收到的 Token: {}", token);
        return token;
    }
}