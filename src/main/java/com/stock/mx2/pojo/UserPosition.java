package com.stock.mx2.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.beans.ConstructorProperties;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class UserPosition {
    @TableId(type = IdType.AUTO)
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
    private Integer buyType;
    private String buyOrderId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date buyOrderTime;
    private BigDecimal buyOrderPrice;
    private String sellOrderId;
    private Date sellOrderTime;


    private BigDecimal sellOrderPrice;
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
    private Integer isLock;
    private String lockMsg;
    private String stockPlate;
    /*點差費金額*/
    private BigDecimal spreadRatePrice;
    /*追加保證金額*/
    private BigDecimal marginAdd;

    private Integer newStock;

    private Integer experienceWallet;


}

