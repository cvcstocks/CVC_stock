package com.stock.mx2.vo.user;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;


@Data
public class UserInfoVO {

    /**
     * id
     */
    private Integer id;
    /**
     * 代理ID
     */
    private Integer agentId;

    @TableField(exist = false)
    private BigDecimal userAllRechargeAmount;
    /**
     * 代理名
     */
    private String agentName;
    /**
     * 手機號
     */
    private String phone;
    /**
     * 昵稱
     */
    private String nickName;



    private BigDecimal userAiAmt;

    /**
     * 真實姓名
     */
    private String realName;

    /**
     * 身份證號
     */
    private String idCard;

    /**
     * 賬號類型
     */
    private Integer accountType;

    /**
     *推薦手機
     */
    private String recomPhone;

    /**
     * 是否鎖定
     */
    private Integer isLock;

    /**
     * 註冊時間
     */
    private Date regTime;

    /**
     * 註冊IP
     */
    private String regIp;

    /**
     * 註冊地址
     */
    private String regAddress;

    /**
     * 實名圖片1
     */
    private String img1Key;

    /**
     * 實名圖片2
     */
    private String img2Key;


    /**
     * 實名圖片3
     */
    private String img3Key;
    /**
     * 是否實名
     */
    private Integer isActive;
    /**
     * 實名認證消息
     */
    private String authMsg;
    /**
     * 現股在用資金
     */
    private BigDecimal userAmt;
    /**
     * 現股可用資金
     */
    private BigDecimal enableAmt;

    private BigDecimal enableFutAmt;
    /**
     * 融資在用資金
     */
    private BigDecimal userIndexAmt;
    /**
     * 融資可用資金
     */
    private BigDecimal enableIndexAmt;
    /**
     * 总损益
     */
    private BigDecimal allProfitAndLose;
    /**
     * 現股持有成本
     */
    private BigDecimal allFreezAmt;
    /**
     * 融資總盈虧
     */
    private BigDecimal allIndexProfitAndLose;
    /**
     * 融資總持成本
     */
    private BigDecimal allIndexFreezAmt;
    /**
     * 槓桿倍數,多個用/分割
     */
    private String siteLever;

    /*操盤金額*/
    private BigDecimal tradingAmount;



    /**
     * 体验账户
     */
    private BigDecimal experienceWallet;

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




}

