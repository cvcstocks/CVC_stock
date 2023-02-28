package com.stock.mx2.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class StockFutures {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String futuresName;
    private String futuresCode;
    private String futuresGid;
    private String futuresUnit;
    private Integer futuresStandard;
    private String coinCode;
    private Integer homeShow;
    private Integer listShow;
    private Integer depositAmt;
    private Integer transFee;
    private Integer minNum;
    private Integer maxNum;

    private Date addTime;

}
