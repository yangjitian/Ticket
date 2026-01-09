package com.yudi.ticket.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 购票请求DTO
 */
@Data
public class BookTicketRequest implements Serializable {

    /**
     * 景区ID
     */
    private Long spotId;

    /**
     * 票种类型：1-原价票, 2-半价票, 3-免票
     */
    private Integer ticketType;

    /**
     * 游玩日期
     */
    private LocalDate visitDate;

    /**
     * 选择的游客ID列表
     */
    private List<Long> visitorIds;

    /**
     * 分销商ID（可选，用于佣金计算）
     */
    private Long distributorId;

    private static final long serialVersionUID = 1L;
}
