package com.yudi.ticket.model.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DistributorWalletVO {
    private BigDecimal availableBalance;
    private List<BillVO> bills;

    @Data
    public static class BillVO {
        private Long id;
        private String time;
        private String type;
        private String description;
        private BigDecimal amount;
        private BigDecimal balance; // Balance after transaction
    }
}
