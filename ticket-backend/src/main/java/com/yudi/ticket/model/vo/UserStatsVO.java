package com.yudi.ticket.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 用户统计视图对象
 */
@Data
public class UserStatsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日期列表
     */
    private List<String> dates;

    /**
     * 注册数量列表
     */
    private List<Integer> registerCounts;

    /**
     * 总注册用户数
     */
    private Long totalUsers;

    /**
     * 今日注册用户数
     */
    private Long todayUsers;

    /**
     * 本周注册用户数
     */
    private Long weekUsers;

    /**
     * 本月注册用户数
     */
    private Long monthUsers;
}
