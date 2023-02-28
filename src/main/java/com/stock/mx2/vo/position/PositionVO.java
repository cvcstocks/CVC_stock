package com.stock.mx2.vo.position;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.math.BigDecimal;


@Data
public class PositionVO {
    private BigDecimal allProfitAndLose;
    private BigDecimal allFreezAmt;

    private BigDecimal userIndexBuyAmount;

    private BigDecimal userBuyAmount;

    @TableField(exist = false)
    private BigDecimal userBuySellLose;

    @TableField(exist = false)
    private BigDecimal userBuyBuyLose;


}
