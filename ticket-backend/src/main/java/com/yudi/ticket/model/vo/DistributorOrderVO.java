package com.yudi.ticket.model.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DistributorOrderVO {
    private Long id;
    private String orderNumber;
    private String spotName;
    private String playDate;
    private BigDecimal totalAmount;
    private BigDecimal commission;
    private String createTime; // Formatted date string
    private String status;
}
