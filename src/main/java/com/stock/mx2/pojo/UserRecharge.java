package com.stock.mx2.pojo;


import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;

import java.util.Date;


@Data
public class UserRecharge {

    @Excel(name = "id")
    @TableId(type = IdType.AUTO)
    private Integer id;
    @Excel(name = "用户id")
    private Integer userId;
    @Excel(name = "用户名")
    private String nickName;
    @Excel(name = "代理ID")
    private Integer agentId;

    private String orderSn;

    private String paySn;
    @Excel(name = "充值渠道" ,replace = { "支付宝_0", "对公打款_1" })
    private String payChannel;
    @Excel(name = "充值金额")
    private BigDecimal payAmt;
    @Excel(name = "状态" ,replace = { "审核中_0", "成功_1", "失败_2" })
    private Integer orderStatus;

    private String orderDesc;

    @Excel(name = "申请时间", databaseFormat = "yyyyMMddHHmmss", format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date addTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Excel(name = "支付时间", databaseFormat = "yyyyMMddHHmmss", format = "yyyy-MM-dd HH:mm:ss")
    private Date payTime;

    /*支付通道主鍵id*/
    private Integer payId;

}
