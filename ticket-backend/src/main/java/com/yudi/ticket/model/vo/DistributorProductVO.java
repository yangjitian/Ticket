package com.yudi.ticket.model.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DistributorProductVO {
    private String id; // Change Long to String
    private String spotId; // Change Long to String
    private String spotName;
    private String spotLevel;
    private String ticketType; // e.g., "成人票"
    private BigDecimal retailPrice;
    private BigDecimal settlementPrice;
    private BigDecimal myPrice; // Default sale price
    private BigDecimal profit; // Estimated profit
}
