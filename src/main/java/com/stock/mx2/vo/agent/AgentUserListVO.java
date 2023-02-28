
package com.stock.mx2.vo.agent;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AgentUserListVO {
    private Integer id;
    private Integer agentId;
    private String agentName;
    private String phone;

    private String realName;

    private String idCard;

    private Integer accountType;

    private Integer isLock;

    private Integer isLogin;

    private String regAddress;

    private Integer isActive;

    private String bankName;

    private String bankNo;

    private String bankAddress;


    public void setId(Integer id) {
        this.id = id;
    }

    private BigDecimal userAmt;
    private BigDecimal enableAmt;
    private BigDecimal forceLine;
    private BigDecimal allProfitAndLose;
    private BigDecimal allFreezAmt;
    private BigDecimal userIndexAmt;
    private BigDecimal enableIndexAmt;
    private BigDecimal indexForceLine;
    private BigDecimal allIndexFreezAmt;
    private BigDecimal allIndexProfitAndLose;
    private BigDecimal userFuturesAmt;
    private BigDecimal enableFuturesAmt;
    private BigDecimal futuresForceLine;
    private BigDecimal allFuturesFreezAmt;
    private BigDecimal allFuturesProfitAndLose;

    private BigDecimal allNewAmount;

    private BigDecimal allNewBalance;
    private BigDecimal allDeepBalance;
    private BigDecimal allDeepLose;
    private BigDecimal allNewLose;



}
