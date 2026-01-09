package com.yudi.ticket.aop;

import cn.hutool.core.util.StrUtil;
import com.yudi.ticket.common.ErrorCode;
import com.yudi.ticket.common.ThrowUtils;
import com.yudi.ticket.exception.BusinessException;
import com.yudi.ticket.model.enums.UserType;
import com.yudi.ticket.service.UserService;
import com.yudi.ticket.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.annotation.Resource;

/**
 * 权限校验切面
 * 用于统一处理登录状态和权限校验
 */
@Aspect
@Component
@Slf4j
public class PermissionAspect {

    @Resource
    private UserService userService;

    /**
     * 登录状态校验切点
     */
    @Pointcut("@annotation(RequireLogin)")
    public void requireLoginPointcut() {}

    /**
     * 管理员权限校验切点
     */
    @Pointcut("@annotation(RequireAdmin)")
    public void requireAdminPointcut() {}

    /**
     * 校验用户登录状态
     */
    @Around("requireLoginPointcut()")
    public Object checkLoginStatus(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            log.error("无法获取请求上下文，方法: {}", joinPoint.getSignature().getName());
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "无法获取请求上下文");
        }

        HttpServletRequest request = attributes.getRequest();
        String token = getToken(request);

        // 解析token获取用户ID
        Long userId = JwtUtils.getUserId(token);

        // 校验登录状态（包含Redis校验和token刷新）
        boolean isLoggedIn = userService.checkLoginStatus(userId, token);
        if (!isLoggedIn) {
            log.warn("用户登录状态校验失败，userId: {}", userId);
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录已失效，请重新登录");
        }

        log.debug("用户登录状态校验通过，userId: {}", userId);

        // 校验通过，继续执行原方法
        return joinPoint.proceed();
    }

    /**
     * 拦截所有标注了@RequireAdmin注解的方法
     * 先校验登录状态，再校验管理员权限
     */
    @Around("requireAdminPointcut()")
    public Object checkAdminPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            log.error("无法获取请求上下文，方法: {}", joinPoint.getSignature().getName());
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "无法获取请求上下文");
        }

        HttpServletRequest request = attributes.getRequest();
        String token = getToken(request);

        // 解析token获取用户信息
        Long userId = JwtUtils.getUserId(token);
        Integer userRole = JwtUtils.getUserRole(token);

        if (userRole == null) {
            log.warn("Token无效或已过期");
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录已失效，请重新登录");
        }

        // 校验登录状态
        boolean isLoggedIn = userService.checkLoginStatus(userId, token);
        if (!isLoggedIn) {
            log.warn("管理员登录状态校验失败，userId: {}", userId);
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录已失效，请重新登录");
        }

        // 检查是否为管理员
        if (!UserType.ADMIN.getValue().equals(userRole)) {
            log.warn("非管理员用户尝试访问管理员权限接口，userId: {}, role: {}", userId, userRole);
            throw new BusinessException(ErrorCode.FORBIDDEN, "需要管理员权限");
        }

        log.debug("管理员权限校验通过，userId: {}", userId);

        // 权限验证通过，继续执行原方法
        return joinPoint.proceed();
    }

    /**
     * 从请求头获取token
     */
    private String getToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        ThrowUtils.throwIf(StrUtil.isBlank(token), ErrorCode.UNAUTHORIZED, "请求缺少Authorization头");

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        log.debug("接收到的Token: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
        return token;
    }
}
