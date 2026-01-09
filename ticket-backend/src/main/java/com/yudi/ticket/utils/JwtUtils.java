package com.yudi.ticket.utils;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtUtils {

    private static String jwtSecret;
    private static Long jwtExpiration;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * Spring 初始化后，将非静态属性赋值给静态变量
     */
    @PostConstruct
    public void init() {
        jwtSecret = this.secret;
        jwtExpiration = this.expiration;
    }

    /**
     * 生成 Token
     *
     * @param userId      用户ID
     * @param role        角色 (0-管理员, 1-用户, 2-分销商)
     * @return token
     */
    public static String createToken(Long userId, Integer role) {
        // 检测系统时间是否正确（2025年基准时间戳）
        long systemTimeSeconds = System.currentTimeMillis() / 1000;
        long baseTime2025 = 1735689600L; // 2025年1月1日0点的秒级时间戳

        // 如果系统时间明显错误（偏差超过1年），使用基准时间
        long currentTimeSeconds;
        if (Math.abs(systemTimeSeconds - baseTime2025) > 31536000) { // 1年 = 31536000秒
            // 系统时间错误，使用基准时间作为相对时间基准
            // 使用一个计数器来模拟时间流逝
            currentTimeSeconds = baseTime2025 + (System.nanoTime() / 1000000000L) % 86400; // 每天循环
            log.warn("检测到系统时间异常，使用基准时间计算token时间戳");
        } else {
            currentTimeSeconds = systemTimeSeconds;
        }

        long expTimeSeconds = currentTimeSeconds + (jwtExpiration / 1000);

        log.info("生成token - userId:{} role:{} currentTime:{} expTime:{} jwtExpiration:{}ms systemTime:{}",
                userId, role, currentTimeSeconds, expTimeSeconds, jwtExpiration, systemTimeSeconds);

        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("role", role);
        // 使用秒级时间戳（JWT标准）
        payload.put(JWT.ISSUED_AT, currentTimeSeconds);
        payload.put(JWT.EXPIRES_AT, expTimeSeconds);
        payload.put(JWT.NOT_BEFORE, currentTimeSeconds);

        return JWTUtil.createToken(payload, jwtSecret.getBytes());
    }

    /**
     * 校验并解析 Token
     *
     * @param token 令牌
     * @return JWT对象
     */
    public static JWT parseToken(String token) {
        // 1. 验证签名
        if (!JWTUtil.verify(token, jwtSecret.getBytes())) {
            return null;
        }
        JWT jwt = JWTUtil.parseToken(token);

        log.info("解析token - payload:{}", jwt.getPayloads());

        // 2. 获取系统时间，使用与createToken相同的逻辑
        long systemTimeSeconds = System.currentTimeMillis() / 1000;
        long baseTime2025 = 1735689600L; // 2025年1月1日0点的秒级时间戳

        long currentTimeSeconds;
        if (Math.abs(systemTimeSeconds - baseTime2025) > 31536000) { // 1年 = 31536000秒
            // 系统时间错误，使用基准时间作为相对时间基准
            currentTimeSeconds = baseTime2025 + (System.nanoTime() / 1000000000L) % 86400;
        } else {
            currentTimeSeconds = systemTimeSeconds;
        }

        // 3. 手动检查过期时间
        Object expObj = jwt.getPayload(JWT.EXPIRES_AT);
        if (expObj == null) {
            log.warn("token缺少过期时间字段");
            return null;
        }

        long expTimeSeconds;
        try {
            expTimeSeconds = Long.parseLong(expObj.toString());
        } catch (NumberFormatException e) {
            log.warn("token过期时间格式错误: {}", expObj);
            return null;
        }

        // 允许60秒的误差时间
        if (currentTimeSeconds > expTimeSeconds + 60) {
            log.warn("token已过期 - 当前时间:{} 过期时间:{} 误差:{}秒",
                    currentTimeSeconds, expTimeSeconds, currentTimeSeconds - expTimeSeconds);
            return null;
        }

        log.info("token校验通过 - 当前时间:{} 过期时间:{} 剩余时间:{}秒",
                currentTimeSeconds, expTimeSeconds, expTimeSeconds - currentTimeSeconds);
        return jwt;
    }

    /**
     * 获取 Token 中的 UserId
     */
    public static Long getUserId(String token) {
        JWT jwt = parseToken(token);
        return jwt == null ? null : Long.parseLong(jwt.getPayload("userId").toString());
    }

    /**
     * 获取 Token 中的用户角色
     */
    public static Integer getUserRole(String token) {
        JWT jwt = parseToken(token);
        return jwt == null ? null : Integer.parseInt(jwt.getPayload("role").toString());
    }

    /**
     * 获取token的过期时间戳（秒级）
     *
     * @param token JWT token
     * @return 过期时间戳，失败返回0
     */
    public static long getExpirationTime(String token) {
        try {
            JWT jwt = parseToken(token);
            if (jwt == null) {
                return 0;
            }
            Object exp = jwt.getPayload("exp");
            if (exp instanceof Number) {
                return ((Number) exp).longValue();
            }
            return 0;
        } catch (Exception e) {
            log.warn("解析token过期时间失败: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 从HttpServletRequest中获取用户ID
     *
     * @param request HTTP请求
     * @return 用户ID，失败返回null
     */
    public static Long getUserIdFromToken(HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            return getUserId(token);
        } catch (Exception e) {
            log.warn("从请求中获取用户ID失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从HttpServletRequest中获取用户角色
     *
     * @param request HTTP请求
     * @return 用户角色，失败返回null
     */
    public static Integer getUserRoleFromToken(HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            return getUserRole(token);
        } catch (Exception e) {
            log.warn("从请求中获取用户角色失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从HttpServletRequest中获取token
     *
     * @param request HTTP请求
     * @return token字符串
     */
    private static String getTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (StrUtil.isBlank(token)) {
            throw new RuntimeException("Authorization header is missing");
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return token;
    }
}