package com.stock.mx2.vo.stock;

import lombok.Data;

@Data
public class StockOptionListVO {
    private int id;
    private String stockName;
    private String stockCode;
    private String stockGid;
    private String nowPrice;


    private String hcrate;
    private String preclose_px;
    private String open_px;



    private String stock_plate;
    private String stock_type;

    private String volume;

    private String hcrates;

    private String stock_type_str;

    /*是否添加自選：1、添加自選，0、未添加自選*/
    private String isOption;


}

