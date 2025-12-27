package com.yudi.ticket.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

/**
 * 用户类型枚举
 */
@Getter
public enum UserType {

    ADMIN("超级管理员", 0),
    USER("普通用户", 1),
    DISTRIBUTOR("经销商", 2);

    private final String text;

    private final Integer value;

    UserType(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    public static UserType getEnumByValue(Integer value) {
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }
        for (UserType anEnum : UserType.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
