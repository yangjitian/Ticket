package com.yudi.ticket.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 购票信息视图对象
 */
@Data
public class BookTicketVO implements Serializable {

    /**
     * 景区ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long spotId;

    /**
     * 景区名称
     */
    private String spotName;

    /**
     * 景区图片
     */
    private String spotImage;

    /**
     * 游玩日期
     */
    private LocalDate visitDate;

    /**
     * 票种类型：1-原价票, 2-半价票, 3-免票
     */
    private Integer ticketType;

    /**
     * 票种名称
     */
    private String ticketTypeName;

    /**
     * 单价
     */
    private BigDecimal unitPrice;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 可选游客列表
     */
    private List<UserVisitorVO> availableVisitors;

    /**
     * 已选择的游客列表
     */
    private List<UserVisitorVO> selectedVisitors;

    private static final long serialVersionUID = 1L;
}
