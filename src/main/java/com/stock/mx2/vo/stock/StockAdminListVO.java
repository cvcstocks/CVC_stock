package com.stock.mx2.vo.stock;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class StockAdminListVO {
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
    private String nowPrice;
    private BigDecimal hcrate;
    private BigDecimal day3Rate;
    /*點差費率*/
    private BigDecimal spreadRate;
    private Integer riseWhite;

  }
