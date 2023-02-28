package com.stock.mx2.vo.stockindex;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class StockIndexVO {
    private Integer id;
    private String indexName;
    private String indexCode;
    private String indexGid;
    private Integer homeShow;
    private Integer listShow;
    private Integer transState;
    private Integer depositAmt;

    public void setId(Integer id) {
        this.id = id;
    }

    private Integer transFee;
    private Integer eachPoint;
    private Integer minNum;
    private Integer maxNum;
    private Date addTime;
    private String tDesc;
    private String currentPoint;
    private String floatPoint;
    private String floatRate;
    /*是否添加自選：1、添加自選，0、未添加自選*/
    private String isOption;


    private BigDecimal volume;

}

