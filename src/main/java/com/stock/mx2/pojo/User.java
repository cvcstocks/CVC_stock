package com.stock.mx2.pojo;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class User {
    @TableId(type = IdType.AUTO)
    @Excel(name = "ID")
    private Integer id;
    @Excel(name = "代理ID")
    private Integer agentId;
    @Excel(name = "代理名称")
    private String agentName;
    @Excel(name = "手机号")
    private String phone;
    private String userPwd;
    private String withPwd;
    @Excel(name = "昵称")
    private String nickName;
    @Excel(name = "真实姓名")
    private String realName;
    @Excel(name = "身份证号")
    private String idCard;


    private Integer accountType;
    @Excel(name = "总资金")
    private BigDecimal userAmt;

    private BigDecimal userAiAmt;



    /**
     * 現股可用金額
     */
    @Excel(name = "現股可用金額")
    private BigDecimal enableAmt;

    private BigDecimal sumChargeAmt;

    private BigDecimal sumBuyAmt;



    private String recomPhone;
    @Excel(name = "交易状态" ,replace = { "可交易_0", "不可交易_1"  })
    private Integer isLock;
    @Excel(name = "登录状态" ,replace = { "可登录_0", "不可登录_1"  })
    private Integer isLogin;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Excel(name = "注册时间", databaseFormat = "yyyyMMddHHmmss", format = "yyyy-MM-dd HH:mm:ss")
    private Date regTime;
    private String regIp;
    private String regAddress;
    private String img1Key;
    private String img2Key;
    private String img3Key;
    @Excel(name = "认证信息" ,replace = { "未认证_0", "待审核_1", "成功_2" , "驳回_3" })
    private Integer isActive;
    private String authMsg;
    private BigDecimal userIndexAmt;


    /**
     * 融資可用金額
     */
    @Excel(name = "融資可用金額")
    private BigDecimal enableIndexAmt;


    private BigDecimal userFutAmt;

    private BigDecimal enableFutAmt;


    private String withdrawalPwd;


    /*總操盤金額*/
    private BigDecimal tradingAmount;


    private String lever;

    private Integer userLevel;

    /**
     * 为 2 时，用户为测试账户
     */
    private Integer testUser;

    private Date laseLevelTime;

    /**
     * 最近一次发短信类型
     */
    private Integer smsTimeType;
    @TableField(exist = false)
    private List<SiteTaskLog> taskLogList;

    @TableField(exist = false)
    private BigDecimal userBuyAmount;

    @TableField(exist = false)
    private BigDecimal userIndexBuyAmount;

    @TableField(exist = false)
    private BigDecimal lose;

    @TableField(exist = false)
    private BigDecimal userIndexLose;

    @TableField(exist = false)
    private BigDecimal userLose;

    @TableField(exist = false)
    private BigDecimal userAllRechargeAmount;
    @TableField(exist = false)
    private BigDecimal allNewAmount;


    @TableField(exist = false)
    private BigDecimal userBuyFutAmount;
    @TableField(exist = false)
    private BigDecimal userBuyFutLose;

    @TableField(exist = false)
    @Excel(name = "新股总持成本")
    private BigDecimal allNewBalance;
    @TableField(exist = false)
    @Excel(name = "deep总持成本")
    private BigDecimal allDeepBalance;
    @TableField(exist = false)
    @Excel(name = "deep盈亏")
    private BigDecimal allDeepLose;
    @TableField(exist = false)
    @Excel(name = "新股盈亏")
    private BigDecimal allNewLose;



    private Integer riseWhite;
    //ALTER TABLE `xingu`.`user`
    //ADD COLUMN `experience_wallet` decimal(10, 2) NOT NULL DEFAULT 0 COMMENT '体验账户' AFTER `user_ai_amt`,
    //ADD COLUMN `profit_wallet` decimal(10, 2) NOT NULL DEFAULT 0 COMMENT '盈利账户' AFTER `experience_wallet`,
    //ADD COLUMN `with_profit_wallet` decimal(10, 2) NOT NULL DEFAULT 0 COMMENT '可提现盈利账户' AFTER `profit_wallet`;

    /**
     * 体验账户
     */
    private BigDecimal experienceWallet;

    private Integer experienceWalletLevel;

    /**
     * 盈利账户
     */
    private BigDecimal profitWallet;

    /**
     * 可提现盈利账户
     */
    private BigDecimal withProfitWallet;


    /**
     * 1种⼦级：未⼊⾦
     * 2过度级：待⼊⾦
     * 3培养级：已⼊⾦
     * 4开发级：⼊⾦⾦额10-100w
     * 5⻩⾦级：⼊⾦⾦额100-200w
     */
    private Integer userWalletLevel;


    /**
     * 总收益
     */
    @TableField(exist = false)
    private BigDecimal allProfit;


   private Date useKeyTime;
}
