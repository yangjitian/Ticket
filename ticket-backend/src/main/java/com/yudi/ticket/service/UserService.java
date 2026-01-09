package com.yudi.ticket.service;

import com.yudi.ticket.model.dto.UserRegisterRequest;
import com.yudi.ticket.model.dto.UserLoginRequest;
import com.yudi.ticket.model.dto.UserUpdateRequest;
import com.yudi.ticket.model.dto.UserRecoverPasswordRequest;
import com.yudi.ticket.model.dto.UserUpdatePasswordRequest;
import com.yudi.ticket.model.dto.UserChangeEmailRequest;
import com.yudi.ticket.model.dto.UserDeleteRequest;
import com.yudi.ticket.model.dto.UserAuditRequest;
import com.yudi.ticket.model.dto.UserStatusUpdateRequest;
import com.yudi.ticket.model.dto.UserQueryRequest;
import com.yudi.ticket.model.dto.UserStatsRequest;
import com.yudi.ticket.common.PageResponse;
import com.yudi.ticket.model.vo.UserVO;
import com.yudi.ticket.model.vo.UserStatsVO;
import com.yudi.ticket.model.vo.DistributorVO;
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
     * 获取当前用户信息
     *
     * @param userId 用户ID
     * @param currentToken 当前有效的token
     * @return 用户登录视图信息
     */
    UserLoginVO getCurrentUser(Long userId, String currentToken);

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
     * 换绑邮箱
     *
     * @param userChangeEmailRequest 换绑邮箱请求
     * @param userId                 用户ID
     */
    void changeEmail(UserChangeEmailRequest userChangeEmailRequest, Long userId);

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

    /**
     * 将token加入黑名单（登出时调用）
     *
     * @param token JWT token
     */
    void addTokenToBlacklist(String token);

    /**
     * 检查token是否在黑名单中
     *
     * @param token JWT token
     * @return true如果在黑名单中
     */
    boolean isTokenInBlacklist(String token);

    /**
     * 分页查询用户列表
     *
     * @param userQueryRequest 查询请求
     * @return 分页用户列表
     */
    PageResponse<UserVO> listUsers(UserQueryRequest userQueryRequest);

    /**
     * 获取用户注册统计数据
     *
     * @param userStatsRequest 统计请求
     * @return 用户统计数据
     */
    UserStatsVO getUserStats(UserStatsRequest userStatsRequest);

    /**
     * 获取经销商列表（管理员）
     *
     * @param userQueryRequest 查询请求
     * @return 经销商列表
     */
    PageResponse<DistributorVO> listDistributors(UserQueryRequest userQueryRequest);

    /**
     * 获取经销商统计数据（订单数、销售额、佣金）
     *
     * @param distributorId 经销商ID
     * @return 经销商统计数据
     */
    DistributorVO getDistributorStats(Long distributorId);
}