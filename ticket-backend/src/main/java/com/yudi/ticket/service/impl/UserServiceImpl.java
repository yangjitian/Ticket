package com.yudi.ticket.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Validator;
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
import com.yudi.ticket.common.PageResponse;
import com.yudi.ticket.model.vo.UserVO;
import com.yudi.ticket.model.vo.UserStatsVO;
import com.yudi.ticket.model.vo.DistributorVO;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Resource
    private UserMapper userMapper;

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
        user.setUserStatus(1); // 新注册用户默认为正常状态

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
    public UserLoginVO getCurrentUser(Long userId, String currentToken) {
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAM_ERROR);
        User user = userMapper.selectById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        UserLoginVO userLoginVO = new UserLoginVO();
        BeanUtil.copyProperties(user, userLoginVO);

        // 设置当前有效的token
        userLoginVO.setToken(currentToken);

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
    public void addTokenToBlacklist(String token) {
        // 1. 校验参数
        ThrowUtils.throwIf(StrUtil.isBlank(token), ErrorCode.PARAM_ERROR, "token不能为空");

        // 2. 计算token剩余有效期
        long expirationTime;
        try {
            // 从token中解析过期时间
            expirationTime = JwtUtils.getExpirationTime(token);
            long currentTime = System.currentTimeMillis() / 1000; // 秒级时间戳
            long remainingTime = expirationTime - currentTime;

            // 如果token已经过期，不需要加入黑名单
            if (remainingTime <= 0) {
                log.debug("Token已过期，无需加入黑名单");
                return;
            }

            // 3. 将token加入黑名单，过期时间设置为token的剩余有效期
            String blacklistKey = StrUtil.format("blacklist:token:{}", token);
            stringRedisTemplate.opsForValue().set(blacklistKey, "1", remainingTime, TimeUnit.SECONDS);

            log.info("Token已加入黑名单，剩余有效期: {}秒", remainingTime);

        } catch (Exception e) {
            log.warn("解析token过期时间失败: {}", e.getMessage());
            // 如果无法解析过期时间，默认设置30分钟黑名单时间
            String blacklistKey = StrUtil.format("blacklist:token:{}", token);
            stringRedisTemplate.opsForValue().set(blacklistKey, "1", 30, TimeUnit.MINUTES);
            log.info("Token已加入黑名单，默认有效期: 30分钟");
        }
    }

    @Override
    public boolean isTokenInBlacklist(String token) {
        // 1. 校验参数
        if (StrUtil.isBlank(token)) {
            return false;
        }

        // 2. 检查token是否在黑名单中
        String blacklistKey = StrUtil.format("blacklist:token:{}", token);
        String value = stringRedisTemplate.opsForValue().get(blacklistKey);

        return "1".equals(value);
    }


    @Override
    public boolean checkLoginStatus(Long userId, String token) {
        if (userId == null || StrUtil.isBlank(token)) {
            return false;
        }
        // 首先校验JWT token本身是否有效且未过期
        try {
            JwtUtils.getUserId(token);
        } catch (Exception e) {
            return false;
        }

        // 检查token是否在黑名单中（已登出的token）
        if (isTokenInBlacklist(token)) {
            log.warn("Token已在黑名单中，用户已登出，userId: {}", userId);
            return false;
        }

        String redisKey = StrUtil.format("{}{}", USER_LOGIN_STATE, userId);
        String storedToken = stringRedisTemplate.opsForValue().get(redisKey);

        // 如果Redis中有记录，直接比较token是否匹配
        if (storedToken != null) {
        return StrUtil.equals(token, storedToken);
        }

        // 如果Redis中没有记录，即使JWT token本身还没过期，也要禁止访问，强制重新登录
        log.warn("Redis中没有找到登录记录，登录状态已失效，userId: {}", userId);
        return false;
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
    public void changeEmail(UserChangeEmailRequest userChangeEmailRequest, Long userId) {
        ThrowUtils.throwIf(userChangeEmailRequest == null, ErrorCode.PARAM_ERROR);
        ThrowUtils.throwIf(userId == null, ErrorCode.UNAUTHORIZED);

        String newEmail = userChangeEmailRequest.getNewEmail();
        String verificationCode = userChangeEmailRequest.getVerificationCode();

        ThrowUtils.throwIf(StrUtil.hasBlank(newEmail, verificationCode), ErrorCode.PARAM_ERROR);

        // 验证邮箱格式
//        if (!Validator.isEmail(newEmail)) {
//            throw new BusinessException(ErrorCode.PARAM_ERROR, "邮箱格式不正确");
//        }

        // 验证验证码
        String redisKey = "email:change:" + newEmail;
        String storedCode = stringRedisTemplate.opsForValue().get(redisKey);
        if (StrUtil.isBlank(storedCode)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "验证码已过期，请重新获取");
        }
        if (!StrUtil.equals(verificationCode, storedCode)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "验证码错误");
        }

        // 检查新邮箱是否已被其他用户使用
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, newEmail);
        User existingUser = this.getOne(queryWrapper);
        if (existingUser != null && !existingUser.getId().equals(userId)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该邮箱已被其他用户使用");
        }

        // 获取当前用户
        User user = this.getById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.USER_NOT_EXIST);

        // 更新用户邮箱
        user.setUserAccount(newEmail);
        boolean result = this.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "邮箱换绑失败");

        // 清除验证码缓存
        stringRedisTemplate.delete(redisKey);

        log.info("用户 {} 邮箱换绑成功，新邮箱：{}", userId, newEmail);
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

    @Override
    public PageResponse<UserVO> listUsers(UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAM_ERROR);

        // 构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();

        // 搜索条件：邮箱或昵称
        if (StrUtil.isNotBlank(userQueryRequest.getSearchKey())) {
            queryWrapper.and(wrapper -> wrapper
                .like(User::getUserAccount, userQueryRequest.getSearchKey())
                .or()
                .like(User::getNickname, userQueryRequest.getSearchKey())
            );
        }

        // 角色筛选
        if (userQueryRequest.getRole() != null) {
            queryWrapper.eq(User::getRole, userQueryRequest.getRole());
        }

        // 用户状态筛选
        if (userQueryRequest.getUserStatus() != null) {
            queryWrapper.eq(User::getUserStatus, userQueryRequest.getUserStatus());
        }

        // 审核状态筛选
        if (userQueryRequest.getAuditStatus() != null) {
            queryWrapper.eq(User::getAuditStatus, userQueryRequest.getAuditStatus());
        }

        // 不显示管理员账号（管理员只能管理普通用户）
        queryWrapper.ne(User::getRole, 0);

        // 不显示已逻辑删除的用户
        queryWrapper.eq(User::getIsDeleted, 0);

        // 按创建时间倒序排序
        queryWrapper.orderByDesc(User::getCreateTime);

        // 分页查询
        long current = userQueryRequest.getCurrent() != null ? userQueryRequest.getCurrent() : 1L;
        long pageSize = userQueryRequest.getPageSize() != null ? userQueryRequest.getPageSize() : 10L;

        // 使用MyBatis-Plus的分页
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> page =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, pageSize);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> resultPage = this.page(page, queryWrapper);

        // 转换为VO对象
        List<UserVO> userVOList = resultPage.getRecords().stream()
            .map(user -> {
                UserVO userVO = new UserVO();
                BeanUtil.copyProperties(user, userVO);
                return userVO;
            })
            .collect(Collectors.toList());

        return new PageResponse<>(userVOList, resultPage.getTotal(), current, pageSize);
    }

    @Override
    public UserStatsVO getUserStats(UserStatsRequest userStatsRequest) {
        ThrowUtils.throwIf(userStatsRequest == null, ErrorCode.PARAM_ERROR);

        int days = userStatsRequest.getDays() != null ? userStatsRequest.getDays() : 7;
        if (days < 7 || days > 30) {
            days = 7; // 默认7天
        }

        UserStatsVO statsVO = new UserStatsVO();
        List<String> dates = new ArrayList<>();
        List<Integer> registerCounts = new ArrayList<>();

        // 生成日期列表（最近N天）
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            dates.add(date.toString());

            // 查询当天注册用户数
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);

            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.between(User::getCreateTime, startOfDay, endOfDay);
            queryWrapper.eq(User::getIsDeleted, 0); // 未删除的用户

            int count = (int) this.count(queryWrapper);
            registerCounts.add(count);
        }

        statsVO.setDates(dates);
        statsVO.setRegisterCounts(registerCounts);

        // 统计总用户数
        LambdaQueryWrapper<User> totalQuery = new LambdaQueryWrapper<>();
        totalQuery.eq(User::getIsDeleted, 0);
        statsVO.setTotalUsers(this.count(totalQuery));

        // 今日注册
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(23, 59, 59);
        LambdaQueryWrapper<User> todayQuery = new LambdaQueryWrapper<>();
        todayQuery.between(User::getCreateTime, todayStart, todayEnd);
        todayQuery.eq(User::getIsDeleted, 0);
        statsVO.setTodayUsers(this.count(todayQuery));

        // 本周注册
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDateTime weekStartTime = weekStart.atStartOfDay();
        LambdaQueryWrapper<User> weekQuery = new LambdaQueryWrapper<>();
        weekQuery.between(User::getCreateTime, weekStartTime, todayEnd);
        weekQuery.eq(User::getIsDeleted, 0);
        statsVO.setWeekUsers(this.count(weekQuery));

        // 本月注册
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDateTime monthStartTime = monthStart.atStartOfDay();
        LambdaQueryWrapper<User> monthQuery = new LambdaQueryWrapper<>();
        monthQuery.between(User::getCreateTime, monthStartTime, todayEnd);
        monthQuery.eq(User::getIsDeleted, 0);
        statsVO.setMonthUsers(this.count(monthQuery));

        return statsVO;
    }

    @Override
    public PageResponse<DistributorVO> listDistributors(UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAM_ERROR);

        // 构建查询条件 - 只查询经销商角色
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getRole, 2); // 只查询经销商
        queryWrapper.eq(User::getIsDeleted, 0); // 未删除的用户

        // 搜索条件：邮箱或昵称
        if (StrUtil.isNotBlank(userQueryRequest.getSearchKey())) {
            queryWrapper.and(wrapper -> wrapper
                .like(User::getUserAccount, userQueryRequest.getSearchKey())
                .or()
                .like(User::getNickname, userQueryRequest.getSearchKey())
            );
        }

        // 审核状态筛选
        if (userQueryRequest.getAuditStatus() != null) {
            queryWrapper.eq(User::getAuditStatus, userQueryRequest.getAuditStatus());
        }

        // 用户状态筛选
        if (userQueryRequest.getUserStatus() != null) {
            queryWrapper.eq(User::getUserStatus, userQueryRequest.getUserStatus());
        }

        // 按创建时间倒序排序
        queryWrapper.orderByDesc(User::getCreateTime);

        // 分页查询
        long current = userQueryRequest.getCurrent() != null ? userQueryRequest.getCurrent() : 1L;
        long pageSize = userQueryRequest.getPageSize() != null ? userQueryRequest.getPageSize() : 10L;

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> page =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, pageSize);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> resultPage = this.page(page, queryWrapper);

        // 转换为DistributorVO对象，并添加统计数据
        List<DistributorVO> distributorVOList = resultPage.getRecords().stream()
            .map(user -> {
                DistributorVO distributorVO = new DistributorVO();
                BeanUtil.copyProperties(user, distributorVO);

                // 获取经销商统计数据（暂时使用模拟数据，实际项目中应该从订单表统计）
                DistributorVO stats = getDistributorStats(user.getId());
                distributorVO.setOrderCount(stats.getOrderCount());
                distributorVO.setSalesAmount(stats.getSalesAmount());
                distributorVO.setCommission(stats.getCommission());

                return distributorVO;
            })
            .collect(Collectors.toList());

        return new PageResponse<>(distributorVOList, resultPage.getTotal(), current, pageSize);
    }

    @Override
    public DistributorVO getDistributorStats(Long distributorId) {
        ThrowUtils.throwIf(distributorId == null, ErrorCode.PARAM_ERROR);

        // 验证经销商身份
        User user = this.getById(distributorId);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "经销商不存在");
        ThrowUtils.throwIf(!user.getRole().equals(2), ErrorCode.PARAM_ERROR, "该用户不是经销商");

        DistributorVO statsVO = new DistributorVO();
        statsVO.setId(distributorId);

        // TODO: 实际项目中应该从订单表统计数据
        // 暂时使用基于用户ID的简单算法生成模拟数据，确保数据相对稳定
        long seed = distributorId % 1000; // 使用ID的后三位作为种子
        statsVO.setOrderCount((int) (seed + 5)); // 订单数：5-1004
        statsVO.setSalesAmount(seed * 100 + 1000L); // 销售额：1000-100400
        statsVO.setCommission(seed * 10 + 100L); // 佣金：100-10100

        return statsVO;
    }
}