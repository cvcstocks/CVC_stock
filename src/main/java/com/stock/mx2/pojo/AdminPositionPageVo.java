package com.stock.mx2.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AdminPositionPageVo {
private Integer pageSize;

private Integer pageNum;

private Integer total;

private BigDecimal profitAndLose;

private BigDecimal allProfitAndLose;

    private BigDecimal buyTotalAmount;

private Integer orderNum;

private List list;
}
