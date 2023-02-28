package com.stock.mx2.pojo;

import lombok.Data;

import java.beans.ConstructorProperties;
import java.math.BigDecimal;

@Data
public class SiteSetting {
    private Integer id;
    private BigDecimal buyFee;
    private BigDecimal sellFee;
    private BigDecimal stayFee;
    private BigDecimal dutyFee;
    private Integer stayMaxDays;
    private Integer buyMinAmt;
    private Integer chargeMinAmt;
    private Integer buyMinNum;

    private BigDecimal exchangeRate;

    private BigDecimal forceStopFee;

    private BigDecimal buyMaxAmtPercent;

    private BigDecimal forceStopPercent;

    private BigDecimal hightAndLow;

    private Integer withMinAmt;

    private BigDecimal creaseMaxPercent;

    private Integer buyMaxNum;


    public void setId(Integer id) {
        this.id = id;
    }

    private Integer withTimeBegin;
    private Integer withTimeEnd;
    private String transAmBegin;
    private String transAmEnd;
    private String transPmBegin;
    private String transPmEnd;
    private Integer withFeeSingle;
    private BigDecimal withFeePercent;
    private String siteLever;
    private Integer buySameTimes;
    private Integer buySameNums;
    private Integer buyNumTimes;
    private Integer buyNumLots;
    private Integer cantSellTimes;
    private BigDecimal kcCreaseMaxPercent;
    private Integer stockDays;
    private BigDecimal stockRate;
    /*強平提醒比例*/
    private BigDecimal forceStopRemindRatio;
    /*創業漲跌比例*/
    private BigDecimal cyCreaseMaxPercent;
}
