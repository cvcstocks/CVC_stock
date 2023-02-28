package com.stock.mx2.vo.position;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class AdminPositionVO {
    private Integer id;


    @Excel(name ="持倉類型")
    private Integer positionType;
    @Excel(name ="持倉號")
    private String positionSn;
    @Excel(name = "用户ID")
    private Integer userId;
    @Excel(name = "用户昵称")
    private String nickName;
    @Excel(name = "代理ID")
    private Integer agentId;
    @Excel(name = "股票名称")
    private String stockName;

    @Excel(name = "股票代码")
    private String stockCode;

    @Excel(name = "股票GID")
    private String stockGid;

    @Excel(name = "股票关键字")
    private String stockSpell;

    private String buyOrderId;

    @Excel(name = "购买时间",databaseFormat = "yyyy-MM-dd HH:mm:ss",format = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date buyOrderTime;


    @Excel(name = "购买价格")
    private BigDecimal buyOrderPrice;

    private String sellOrderId;

    @Excel(name = "平仓时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date sellOrderTime;

    @Excel(name = "平仓价格")
    private BigDecimal sellOrderPrice;

    private BigDecimal profitTargetPrice;
    private BigDecimal stopTargetPrice;
    private Integer orderDirection;
    @Excel(name = "股数")
    private Integer orderNum;
    private BigDecimal orderLever;
    @Excel(name = "买入成本")
    private BigDecimal orderTotalPrice;
    @Excel(name = "手续费")
    private BigDecimal orderFee;

    private BigDecimal buyOrderFee;
    private BigDecimal orderSpread;
    private BigDecimal orderStayFee;
    private Integer orderStayDays;
    @Excel(name = "盈亏（不算任何手续费）")
    private BigDecimal profitAndLose;
    @Excel(name = "总盈亏")
    private BigDecimal allProfitAndLose;
    private String now_price;
    private Integer isLock;
    private String lockMsg;
    private String stockPlate;

    /**
     * 买入市值
     */
    @Excel(name = "买入市值")
    private BigDecimal buyTotalAmount;



    /**
     * 卖出市值（预估市值）
     */
    @Excel(name = "卖出市值（预估市值）")
    private BigDecimal sellTotalAmount;




}

