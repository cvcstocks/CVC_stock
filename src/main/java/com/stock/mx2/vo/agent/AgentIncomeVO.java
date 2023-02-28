package com.stock.mx2.vo.agent;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class AgentIncomeVO {

    private Integer orderSize;

    private BigDecimal orderFeeAmt;

    private BigDecimal orderProfitAndLose;

    private BigDecimal orderAllAmt;

    private  Integer orderNumber;

    private BigDecimal sellTotalAmount;


    private BigDecimal buyAmountTotal;
    private BigDecimal buyIndexAmountTotal;

    private BigDecimal userEnableAmount;

}
