package com.yudi.ticket.aop;

import cn.hutool.core.util.StrUtil;
import com.yudi.ticket.common.ErrorCode;
import com.yudi.ticket.model.enums.UserType;
import com.yudi.ticket.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 管理员权限切面
 * 用于拦截需要管理员权限的方法
 */
@Aspect
@Component
@Slf4j
public class AdminPermissionAspect {

    /**
     * 拦截所有标注了@RequireAdmin注解的方法
     */
    @Around("@annotation(RequireAdmin)")
    public Object checkAdminPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取当前请求
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            log.error("无法获取请求上下文，方法: {}", joinPoint.getSignature().getName());
            throw new RuntimeException(ErrorCode.UNAUTHORIZED.getMessage());
        }

        HttpServletRequest request = attributes.getRequest();

        // 获取token
        String token = request.getHeader("Authorization");
        if (StrUtil.isBlank(token)) {
            log.warn("请求缺少Authorization头");
            throw new RuntimeException(ErrorCode.UNAUTHORIZED.getMessage());
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 验证token并获取用户角色
        Integer userRole = JwtUtils.getUserRole(token);
        if (userRole == null) {
            log.warn("Token无效或已过期");
            throw new RuntimeException(ErrorCode.UNAUTHORIZED.getMessage());
        }

        // 检查是否为管理员
        if (!UserType.ADMIN.getValue().equals(userRole)) {
            log.warn("用户尝试访问管理员权限接口，角色: {}", userRole);
            throw new RuntimeException(ErrorCode.FORBIDDEN.getMessage());
        }

        // 权限验证通过，继续执行原方法
        return joinPoint.proceed();
    }
}
