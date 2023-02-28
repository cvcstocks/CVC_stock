package com.stock.mx2.pojo;
import lombok.Data;

import java.util.Date;
@Data
public class StockOption {
    private Integer id;
    private Integer userId;
    private Integer stockId;
    private Date addTime;
    private String stockCode;
    private String stockName;
    private String stockGid;
    private Integer isLock;

    private Integer status;


}
