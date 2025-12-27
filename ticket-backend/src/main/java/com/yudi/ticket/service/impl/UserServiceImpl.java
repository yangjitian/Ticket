package com.yudi.ticket.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yudi.ticket.common.ErrorCode;
import com.yudi.ticket.common.ThrowUtils;
import com.yudi.ticket.exception.BusinessException;
import com.yudi.ticket.manager.CosManager;
import com.yudi.ticket.model.dto.*;
import com.yudi.ticket.model.dto.UserAuditRequest;
import com.yudi.ticket.model.entity.User;
import com.yudi.ticket.model.vo.UserLoginVO;
import com.yudi.ticket.mapper.UserMapper;
import com.yudi.ticket.model.enums.UserType;
import com.yudi.ticket.service.EmailService;
import com.yudi.ticket.service.UserService;
import com.yudi.ticket.utils.JwtUtils;
import com.yudi.ticket.utils.UsernameGenerator;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper,User> implements UserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CosManager cosManager;

    @Resource
    private EmailService emailService;

    private static final String EMAIL_CODE_KEY = "reg:code";
    private static final String RESET_PASSWORD_CODE_KEY = "reset:code";
    private static final String USER_LOGIN_STATE = "user:login:";

    private static final long MAX_AVATAR_SIZE = 4L * 1024 * 1024;
    private static final Set<String> ALLOWED_AVATAR_SUFFIX = CollUtil.newHashSet("jpg", "jpeg", "png", "gif", "bmp", "webp");
    private static final String PROJECT_NAME = "Ticket";

    @Override
    public Long userRegister(UserRegisterRequest userRegisterRequest) {
        // 1. 校验参数
        String userAccount = userRegisterRequest.getUserAccount();
        String verificationCode = userRegisterRequest.getVerificationCode();
        String password = userRegisterRequest.getPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        Integer role = userRegisterRequest.getRole();

        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, verificationCode, password, checkPassword), ErrorCode.PARAM_ERROR);
        ThrowUtils.throwIf(password.length() < 8 || checkPassword.length() < 8, ErrorCode.PARAM_ERROR, "密码长度不能小于 8 位");
        ThrowUtils.throwIf(!password.equals(checkPassword), ErrorCode.PARAM_ERROR, "两次输入的密码不一致");

        UserType userType = UserType.getEnumByValue(role);
        ThrowUtils.throwIf(userType == null || UserType.ADMIN.equals(userType), ErrorCode.PARAM_ERROR, "非法角色申请");

        // 2. 校验验证码
        String cacheCode = stringRedisTemplate.opsForValue().get(EMAIL_CODE_KEY + userAccount);
        ThrowUtils.throwIf(!verificationCode.equals(cacheCode), ErrorCode.PARAM_ERROR, "验证码错误或已过期");

        // 3. 检查用户是否已存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, userAccount);
        long count = this.count(queryWrapper);
        ThrowUtils.throwIf(count > 0,ErrorCode.USER_ACCOUNT_EXISTS,"账户已存在");

        // 4. 插入用户
        User user = new User();
        user.setUserAccount(userAccount);
        user.setNickname(UsernameGenerator.generateUniqueUsername());
        user.setPassword(BCrypt.hashpw(password));
        user.setRole(role);

        if (UserType.DISTRIBUTOR.equals(userType)) {
            user.setAuditStatus(0);
        }
        boolean result = this.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR,"注册失败");
        stringRedisTemplate.delete(EMAIL_CODE_KEY + userAccount);
        log.info("用户注册成功，账号:{}，用户ID:{}",userAccount,user.getId());
        return user.getId();
    }

    @Override
    public UserLoginVO userLogin(UserLoginRequest userLoginRequest) {

        String userAccount = userLoginRequest.getUserAccount();
        String password = userLoginRequest.getPassword();
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, password), ErrorCode.PARAM_ERROR);

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, userAccount);
        User user = this.getOne(queryWrapper);
        ThrowUtils.throwIf(user == null, ErrorCode.USER_NOT_EXIST, "用户不存在");

        // 校验用户状态
        ThrowUtils.throwIf(user.getUserStatus() != null && user.getUserStatus() == 0, ErrorCode.FORBIDDEN, "账号已被禁用");

        boolean checkPassword = BCrypt.checkpw(password, user.getPassword());
        ThrowUtils.throwIf(!checkPassword , ErrorCode.PASSWORD_ERROR, "密码错误");

        String token = JwtUtils.createToken(user.getId(), user.getRole());
        String redisKey = StrUtil.format("{}{}", USER_LOGIN_STATE, user.getId());
        stringRedisTemplate.opsForValue().set(redisKey, token, 30, TimeUnit.MINUTES);


        UserLoginVO userLoginVO = new UserLoginVO();
        BeanUtil.copyProperties(user, userLoginVO);
        userLoginVO.setToken(token);

        log.info("用户登录成功，账号:{}", userAccount);
        return userLoginVO;
    }

    @Override
    public void userLogout(Long userId) {
        // 1. 校验参数
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAM_ERROR);

        String redisKey = StrUtil.format("{}{}", USER_LOGIN_STATE, userId);
        stringRedisTemplate.delete(redisKey);
        log.info("用户 id={} 已执行登出操作", userId);
    }


    @Override
    public boolean checkLoginStatus(Long userId, String token) {
        if (userId == null || StrUtil.isBlank(token)) {
            return false;
        }
        String redisKey = StrUtil.format("{}{}", USER_LOGIN_STATE, userId);
        String storedToken = stringRedisTemplate.opsForValue().get(redisKey);
        // 校验 Redis 中的 Token 是否与传入的一致
        return StrUtil.equals(token, storedToken);
    }

    @Override
    public String updateAvatar(MultipartFile multipartFile, Long userId) {
        // 1. 校验
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAM_ERROR, "文件不能为空");
        ThrowUtils.throwIf(multipartFile.getSize() > MAX_AVATAR_SIZE, ErrorCode.PARAM_ERROR, "文件大小不能超过 4MB");

        String suffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        ThrowUtils.throwIf(!ALLOWED_AVATAR_SUFFIX.contains(suffix), ErrorCode.PARAM_ERROR, "不支持的文件格式");

        // 2. 上传
        String filename = IdUtil.fastSimpleUUID() + "." + suffix;
        String key = String.format("%s/avatar/%s/%s", PROJECT_NAME, userId, filename);
        File file = null;
        try {
            // 创建临时文件
            file = File.createTempFile(filename, null);
            multipartFile.transferTo(file);
            cosManager.putObject(key, file);
            String url = cosManager.getFileUrl(key);

            // 3. 更新数据库
            User user = new User();
            user.setId(userId);
            user.setAvatar(url);
            boolean result = this.updateById(user);
            ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "更新头像失败");
            return url;
        } catch (Exception e) {
            log.error("file upload error, key = " + key, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        } finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", file.getAbsolutePath());
                }
            }
        }
    }

    @Override
    public void updateMyInfo(UserUpdateRequest userUpdateRequest, Long userId) {
        ThrowUtils.throwIf(userUpdateRequest == null, ErrorCode.PARAM_ERROR);
        ThrowUtils.throwIf(userId == null, ErrorCode.UNAUTHORIZED);

        User user = new User();
        user.setId(userId);
        user.setNickname(userUpdateRequest.getNickname());

        boolean result = this.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "更新用户信息失败");
    }

    @Override
    public void updatePassword(UserUpdatePasswordRequest userUpdatePasswordRequest, Long userId) {
        ThrowUtils.throwIf(userUpdatePasswordRequest == null, ErrorCode.PARAM_ERROR);
        ThrowUtils.throwIf(userId == null, ErrorCode.UNAUTHORIZED);

        String oldPassword = userUpdatePasswordRequest.getOldPassword();
        String newPassword = userUpdatePasswordRequest.getNewPassword();
        String checkPassword = userUpdatePasswordRequest.getCheckPassword();

        ThrowUtils.throwIf(StrUtil.hasBlank(oldPassword, newPassword, checkPassword), ErrorCode.PARAM_ERROR);
        ThrowUtils.throwIf(newPassword.length() < 8, ErrorCode.PARAM_ERROR, "新密码长度不能小于 8 位");
        ThrowUtils.throwIf(!newPassword.equals(checkPassword), ErrorCode.PARAM_ERROR, "两次输入的密码不一致");

        User user = this.getById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.USER_NOT_EXIST);

        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR, "旧密码错误");
        }

        String encryptPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        user.setPassword(encryptPassword);
        
        boolean result = this.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "修改密码失败");
    }

    @Override
    public void userRecoverPassword(UserRecoverPasswordRequest userRecoverPasswordRequest) {
        ThrowUtils.throwIf(userRecoverPasswordRequest == null, ErrorCode.PARAM_ERROR);
        String userAccount = userRecoverPasswordRequest.getUserAccount();
        String verificationCode = userRecoverPasswordRequest.getVerificationCode();
        String newPassword = userRecoverPasswordRequest.getNewPassword();
        String checkPassword = userRecoverPasswordRequest.getCheckPassword();

        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, verificationCode, newPassword, checkPassword), ErrorCode.PARAM_ERROR);
        ThrowUtils.throwIf(newPassword.length() < 8, ErrorCode.PARAM_ERROR, "新密码长度不能小于 8 位");
        ThrowUtils.throwIf(!newPassword.equals(checkPassword), ErrorCode.PARAM_ERROR, "两次输入的密码不一致");

        String cacheCode = stringRedisTemplate.opsForValue().get(RESET_PASSWORD_CODE_KEY + userAccount);
        ThrowUtils.throwIf(cacheCode == null || !cacheCode.equals(verificationCode), ErrorCode.PARAM_ERROR, "验证码错误或已过期");

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, userAccount);
        User user = this.getOne(queryWrapper);
        ThrowUtils.throwIf(user == null, ErrorCode.USER_NOT_EXIST);

        String encryptPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        user.setPassword(encryptPassword);
        boolean result = this.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "重置密码失败");

        stringRedisTemplate.delete(RESET_PASSWORD_CODE_KEY + userAccount);
    }

    @Override
    public void updateUserStatus(UserStatusUpdateRequest userStatusUpdateRequest) {
        ThrowUtils.throwIf(userStatusUpdateRequest == null, ErrorCode.PARAM_ERROR);
        Long id = userStatusUpdateRequest.getId();
        Integer userStatus = userStatusUpdateRequest.getUserStatus();
        ThrowUtils.throwIf(id == null || userStatus == null, ErrorCode.PARAM_ERROR);

        User user = new User();
        user.setId(id);
        user.setUserStatus(userStatus);
        boolean result = this.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "更新用户状态失败");
    }

    @Override
    public void deleteUser(UserDeleteRequest userDeleteRequest) {
        ThrowUtils.throwIf(userDeleteRequest == null, ErrorCode.PARAM_ERROR);
        Long id = userDeleteRequest.getId();
        ThrowUtils.throwIf(id == null, ErrorCode.PARAM_ERROR);

        User user = new User();
        user.setId(id);
        user.setIsDeleted(1);
        boolean result = this.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "删除用户失败");
    }

    @Override
    public void auditDistributor(UserAuditRequest userAuditRequest) {
        ThrowUtils.throwIf(userAuditRequest == null, ErrorCode.PARAM_ERROR);
        Long id = userAuditRequest.getId();
        Integer auditStatus = userAuditRequest.getAuditStatus();
        ThrowUtils.throwIf(id == null || auditStatus == null, ErrorCode.PARAM_ERROR);

        User user = this.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.USER_NOT_EXIST);

        ThrowUtils.throwIf(!UserType.DISTRIBUTOR.getValue().equals(user.getRole()), ErrorCode.PARAM_ERROR, "该用户不是经销商");

        User updateUser = new User();
        updateUser.setId(id);
        updateUser.setAuditStatus(auditStatus);
        boolean result = this.updateById(updateUser);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "审核失败");

        if (auditStatus == 1) {
            emailService.sendAuditPassNotification(user.getUserAccount());
        }
    }
}