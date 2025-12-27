package com.yudi.ticket.common;

import lombok.Getter;

/**
 * 统一API响应状态码枚举
 * <p>
 * 维护原则：
 * 1. 错误码按模块分段，方便定位问题
 * 2. message 必须通俗易懂，直接展示给前端用户
 * </p>
 *
 * @author YourName
 * @since 2025-12-24
 */
@Getter
public enum ErrorCode {

    /* ================== 基础状态 ================== */
    SUCCESS(200, "操作成功"),
    FAILURE(500, "业务处理失败"),
    
    /* ================== 系统级别 (1000 - 1999) ================== */
    SYSTEM_ERROR(1001, "系统繁忙，请稍后重试"),
    PARAM_ERROR(1002, "参数格式校验失败"),
    FILE_UPLOAD_ERROR(1003, "文件上传失败"),
    DATA_IMPORT_ERROR(1004, "数据导入异常"),

    /* ================== 用户与权限 (2000 - 2999) ================== */
    UNAUTHORIZED(2001, "用户未登录或Token已过期"),
    FORBIDDEN(2002, "无权限执行此操作"),
    USER_NOT_EXIST(2003, "用户不存在"),
    PASSWORD_ERROR(2004, "账号或密码错误"),
    ACCOUNT_LOCKED(2005, "账户已被冻结，请联系管理员"),
    USER_ACCOUNT_EXISTS(2006,"账号已存在"),
    // 分销商特有
    DISTRIBUTOR_NOT_QUALIFIED(2010, "分销商资格审核未通过"),
    BALANCE_NOT_ENOUGH(2011, "账户余额不足，请先充值"),

    /* ================== 景区与门票产品 (3000 - 3999) ================== */
    SCENIC_SPOT_NOT_EXIST(3001, "景区信息不存在"),
    TICKET_NOT_EXIST(3002, "门票商品不存在或已删除"),
    TICKET_OFF_SHELF(3003, "该门票已下架，暂停售卖"),
    TICKET_DATE_INVALID(3004, "该日期不在门票有效期内"),
    PRICE_CONFIG_ERROR(3005, "分销价格配置异常"),

    /* ================== 订单与交易 (4000 - 4999) ================== */
    ORDER_NOT_EXIST(4001, "订单不存在"),
    ORDER_STATUS_ERROR(4002, "订单状态不允许此操作"),
    ORDER_PAY_TIMEOUT(4003, "支付超时，订单已关闭"),
    REPEAT_PURCHASE(4004, "您已购买过该票，请勿重复下单"),
    PURCHASE_LIMIT_EXCEEDED(4005, "超出单人限购数量"),

    /* ================== 核销与库存核心 (5000 - 5999) ================== */
    // 并发抢票核心错误码
    STOCK_NOT_ENOUGH(5001, "手慢了，票已售罄"),
    LOCK_STOCK_FAILED(5002, "库存锁定失败"),
    
    // 核销核心错误码 (APP/闸机端使用)
    VERIFY_CODE_INVALID(5010, "无效的核销码"),
    VERIFY_CODE_USED(5011, "核销码已使用，请勿重复核销"),
    VERIFY_CODE_EXPIRED(5012, "门票已过期，无法核销"),
    VERIFY_SPOT_MISMATCH(5013, "门票不适用于当前景区");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 提示信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}