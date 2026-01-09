package com.yudi.ticket.model.vo;

import com.yudi.ticket.model.entity.ScenicSpot;
import com.yudi.ticket.model.entity.TicketProduct;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 门票产品VO（包含景区信息）
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TicketProductVO extends TicketProduct {

    /**
     * 景区信息
     */
    private ScenicSpot scenicSpot;

    private static final long serialVersionUID = 1L;
}
