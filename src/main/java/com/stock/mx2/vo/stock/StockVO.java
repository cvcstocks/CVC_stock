
package com.stock.mx2.vo.stock;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockVO  {
    private int id;
    private String name;
    private String code;
    private String spell;
    private String gid;
    private String nowPrice;
    private BigDecimal hcrate;
    private String today_max;
    private String today_min;

    private String business_balance;

    private String business_amount;

    private String preclose_px;

    private String open_px;

    private String buy1;

    private String buy2;

    private String buy3;

    private String buy4;

    private String buy5;
    private String sell1;
    private String sell2;
    private String sell3;
    private String sell4;
    private String sell5;
    private String buy1_num;
    private String buy2_num;
    private String buy3_num;
    private String buy4_num;
    private String buy5_num;
    private String minImg;
    private String dayImg;
    private String weekImg;
    private String monthImg;

    private Integer depositAmt;

}

