package com.stock.mx2.vo.position;

import com.stock.mx2.pojo.User;
import com.stock.mx2.pojo.UserPosition;
import com.stock.mx2.pojo.UserPositionFutures;
import com.stock.mx2.pojo.UserPositionItem;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PositionProfitVO {

    private Integer userId;



    private UserPosition userPosition;
    private UserPositionFutures userPositionFutures;

    private String positionSn;

    private String nowPrice;


    private BigDecimal profitAndLose;


    /**
     * 卖出市值（预估市值）
     */
    private BigDecimal sellTotalAmount;

    /**
     * 买入市值
     */
    private BigDecimal buyTotalAmount;

    /**
     * 付出成本
     */
    private BigDecimal orderTotalPrice;


    private BigDecimal profitAndLoseRate;


    private BigDecimal allProfitAndLose;

    private BigDecimal orderLevel;

    private BigDecimal buyFee;
    private BigDecimal sellFee;
    private BigDecimal dutyFee;




}

