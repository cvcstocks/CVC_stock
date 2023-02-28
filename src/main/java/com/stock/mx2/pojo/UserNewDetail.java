package com.stock.mx2.pojo;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class UserNewDetail {

    @Excel(name = "用戶ID")
    @TableId(type=IdType.AUTO)
    private Integer id;

    @Excel(name = "代理ID")
    private Integer agentId;

    @Excel(name = "代理名稱")
    private String agentName;

    @Excel(name = "用戶ID")
    private Integer userId;

    @Excel(name = "用戶名稱")
    private String userName;

    @Excel(name = "持倉ID")
    private Integer positionId;

    @Excel(name = "類型")
    private String deType;

    @Excel(name = "金額")
    private BigDecimal deAmt;
    @Excel(name = "描述")
    private String deSummary;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Excel(name = "創建時間", databaseFormat = "yyyyMMddHHmmss", format = "yyyy-MM-dd HH:mm:ss")
    private Date addTime;

    private String addIp;

    private String addAddress;

    private Integer isRead;

}
