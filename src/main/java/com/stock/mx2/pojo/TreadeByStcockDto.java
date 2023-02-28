package com.stock.mx2.pojo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TreadeByStcockDto {

    private Long userId;


    /**
     * 類型 1 增加 2 減少
     */
    private Integer type;

    private BigDecimal amount;

    private BigDecimal rate;
}
