package com.stock.mx2.vo.position;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class AgentPositionVO {
    private Integer id;
    private Integer positionType;
    private String positionSn;
    private Integer userId;
    private String nickName;
    private Integer agentId;

    private String stockName;

    private String stockCode;

    private String stockGid;

    private String stockSpell;

    private String buyOrderId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")

    private Date buyOrderTime;

    private BigDecimal buyOrderPrice;

    private String sellOrderId;

    private Date sellOrderTime;

    private BigDecimal sellOrderPrice;


    public void setId(Integer id) {
        this.id = id;
    }

    private BigDecimal profitTargetPrice;
    private BigDecimal stopTargetPrice;
    private String orderDirection;
    private Integer orderNum;
    private BigDecimal orderLever;
    private BigDecimal orderTotalPrice;
    private BigDecimal orderFee;
    private BigDecimal orderSpread;
    private BigDecimal orderStayFee;
    private Integer orderStayDays;
    private BigDecimal profitAndLose;
    private BigDecimal allProfitAndLose;
    private String now_price;
    private Integer isLock;
    private String lockMsg;
    private String stockPlate;

}
