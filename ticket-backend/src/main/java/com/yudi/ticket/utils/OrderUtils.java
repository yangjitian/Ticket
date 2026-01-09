package com.yudi.ticket.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;

import java.time.LocalDate;

/**
 * 订单工具类
 */
public class OrderUtils {

    /**
     * 生成订单号
     * 格式：ORD + 年月日 + 8位随机数
     */
    public static String generateOrderSn() {
        String dateStr = DateUtil.format(LocalDate.now().atStartOfDay(), "yyyyMMdd");
        String randomStr = RandomUtil.randomNumbers(8);
        return "ORD" + dateStr + randomStr;
    }

    /**
     * 生成核销码
     * 格式：8位字母数字混合随机码（大写字母+数字）
     * 示例：A1B2C3D4 或 X9Y8Z7W6
     * 参数已废弃，仅为保持兼容性
     */
    public static String generateVerificationCode(String spotShortName, LocalDate visitDate) {
        // 生成8位字母数字混合随机码（大写字母+数字）
        StringBuilder code = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        for (int i = 0; i < 8; i++) {
            int randomIndex = RandomUtil.randomInt(0, chars.length());
            code.append(chars.charAt(randomIndex));
        }

        return code.toString();
    }

    /**
     * 生成景区简称（从景区名称提取）
     * 已废弃，仅为兼容性保留
     */
    @Deprecated
    public static String generateSpotShortName(String spotName) {
        if (spotName == null || spotName.trim().isEmpty()) {
            return "XX";
        }

        // 提取中文字符作为简称
        String chineseChars = spotName.replaceAll("[^\\u4e00-\\u9fa5]", "");
        if (chineseChars.length() >= 2) {
            return chineseChars.substring(0, 2);
        } else if (chineseChars.length() == 1) {
            return chineseChars + "X";
        } else {
            // 如果没有中文，取前两个字母
            String letters = spotName.replaceAll("[^a-zA-Z]", "");
            if (letters.length() >= 2) {
                return letters.substring(0, 2).toUpperCase();
            }
            return "XX";
        }
    }
}
