package com.stock.mx2.pojo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class SiteTaskLog {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String taskType;
    private String taskCnt;
    private String taskTarget;
    private Integer isSuccess;
    private String errorMsg;
    private Date addTime;

    private BigDecimal amount;

    private Integer userId;



}
