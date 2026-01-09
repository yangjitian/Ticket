package com.yudi.ticket.model.dto;

import lombok.Data;

@Data
public class PageRequest {

    private Integer current = 1;
    private Integer pageSize = 10;

    // 兼容旧的pageNum属性
    public Integer getPageNum() {
        return current;
    }

    public void setPageNum(Integer pageNum) {
        this.current = pageNum;
    }

    // 显式添加无参构造（解决 Lombok 报错）
    public PageRequest() {
        // 可以为空，也可以初始化默认值
    }

    // 如果你原来有有参构造，也保留
    public PageRequest(Integer current, Integer pageSize) {
        this.current = current;
        this.pageSize = pageSize;
    }
}