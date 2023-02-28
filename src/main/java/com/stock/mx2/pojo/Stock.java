package com.stock.mx2.pojo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class Stock {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String stockName;
    private String stockCode;
    private String stockSpell;
    private String stockType;
    private String stockGid;
    private String stockPlate;
    private Integer isLock;
    private Integer isShow;
    private Date addTime;
    /*點差費率*/
    private BigDecimal spreadRate;

    private Integer stockTypeCode;

    private String stockTypeStr;

    private String exchange;

    private String stockCodeExchange;


    private Integer riseWhite;



}
