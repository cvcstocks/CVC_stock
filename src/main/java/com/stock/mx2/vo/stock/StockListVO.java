package com.stock.mx2.vo.stock;


import lombok.Data;

import java.math.BigDecimal;


@Data
public class StockListVO {

    private String name;

    private String code;

    private String spell;

    private String gid;

    private String nowPrice;

    private BigDecimal hcrate;
    private BigDecimal hcrates;

    private String today_max;


    private String today_min;
    private String business_balance;
    private String business_amount;
    private String preclose_px;
    private String open_px;
    private BigDecimal day3Rate;
    private String stock_type;
    private String stock_plate;
    /*是否添加自選：1、添加自選，0、未添加自選*/
    private String isOption;

    private String volume;

}
