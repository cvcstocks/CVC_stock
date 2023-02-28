package com.stock.mx2.pojo;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;

import java.util.Date;


@Data
public class UserWithdraw {
    @Excel(name = "id")
    private Integer id;
    @Excel(name = "用户id")
    private Integer userId;
    @Excel(name = "用户名")
    private String nickName;
    @Excel(name = "代理ID")
    private Integer agentId;
    @Excel(name = "出金金额")
    private BigDecimal withAmt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Excel(name = "申请时间", databaseFormat = "yyyyMMddHHmmss", format = "yyyy-MM-dd HH:mm:ss")
    private Date applyTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Excel(name = "出金时间", databaseFormat = "yyyyMMddHHmmss", format = "yyyy-MM-dd HH:mm:ss")
    private Date transTime;
    @Excel(name = "银行卡绑定姓名")
    private String withName;
    @Excel(name = "真实姓名")
    private String realName;
    @Excel(name = "银行卡号")
    private String bankNo;
    @Excel(name = "银行名称")
    private String bankName;
    @Excel(name = "银行支行")
    private String bankAddress;
    @Excel(name = "状态" ,replace = { "审核中_0", "成功_1", "失败_2", "取消_3"  })
    private Integer withStatus;
    @Excel(name = "手续费")
    private BigDecimal withFee;
    @Excel(name = "原因")
    private String withMsg;



}
