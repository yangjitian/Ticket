package com.yudi.ticket.service;

import com.yudi.ticket.model.dto.UserRegisterRequest;
import com.yudi.ticket.model.dto.UserLoginRequest;
import com.yudi.ticket.model.dto.UserUpdateRequest;
import com.yudi.ticket.model.dto.UserRecoverPasswordRequest;
import com.yudi.ticket.model.dto.UserUpdatePasswordRequest;
import com.yudi.ticket.model.dto.UserDeleteRequest;
import com.yudi.ticket.model.dto.UserAuditRequest;
import com.yudi.ticket.model.dto.UserStatusUpdateRequest;
import com.yudi.ticket.model.vo.UserLoginVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户服务
 */
public interface UserService {

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    Long userRegister(UserRegisterRequest userRegisterRequest);

    /**
     * 用户登录
     *
     * @param userLoginRequest 登录请求
     * @return 用户登录视图信息
     */
    UserLoginVO userLogin(UserLoginRequest userLoginRequest);

    /**
     * 用户登出
     *
     * @param userId 用户ID
     */
    void userLogout(Long userId);


    /**
     * 校验用户登录状态
     * @param userId 用户ID
     * @param token 当前Token
     * @return 是否有效
     */
    boolean checkLoginStatus(Long userId, String token);

    /**
     * 更新用户头像
     *
     * @param multipartFile 头像文件
     * @param userId        用户ID
     * @return 新头像 URL
     */
    String updateAvatar(MultipartFile multipartFile, Long userId);

    /**
     * 更新个人信息
     *
     * @param userUpdateRequest 更新请求
     * @param userId            用户ID
     */
    void updateMyInfo(UserUpdateRequest userUpdateRequest, Long userId);

    /**
     * 修改密码
     *
     * @param userUpdatePasswordRequest 修改密码请求
     * @param userId                    用户ID
     */
    void updatePassword(UserUpdatePasswordRequest userUpdatePasswordRequest, Long userId);

    /**
     * 用户找回密码
     *
     * @param userRecoverPasswordRequest 找回密码请求
     */
    void userRecoverPassword(UserRecoverPasswordRequest userRecoverPasswordRequest);

    /**
     * 更新用户状态（管理员）
     *
     * @param userStatusUpdateRequest
     */
    void updateUserStatus(UserStatusUpdateRequest userStatusUpdateRequest);

    /**
     * 删除用户（管理员）
     *
     * @param userDeleteRequest
     */
    void deleteUser(UserDeleteRequest userDeleteRequest);

    /**
     * 审核经销商（管理员）
     *
     * @param userAuditRequest
     */
    void auditDistributor(UserAuditRequest userAuditRequest);
}