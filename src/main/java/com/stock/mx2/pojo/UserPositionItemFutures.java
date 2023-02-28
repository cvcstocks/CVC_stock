package com.stock.mx2.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用戶持倉下單明細（賣出）
 */
@Data
public class UserPositionItemFutures {
    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 持倉類型
     */
    private Integer positionType;
    /**
     * 持倉編號
     */
    private String positionSn;
    /**
     * 用戶ID
     */
    private Integer userId;
    /**
     * 用戶昵稱
     */
    private String nickName;
    /**
     * 代理ID
     */
    private Integer agentId;
    /**
     * 股票名稱
     */
    private String stockName;
    /**
     * 股票編號
     */
    private String stockCode;
    /**
     * 股票GID
     */
    private String stockGid;
    /**
     * 股票關鍵字
     */
    private String stockSpell;
    /**
     * 購買類型
     */
    private Integer buyType;

    /**
     * 下單時間
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderTime;
    /**
     * 購買價格
     */
    private BigDecimal buyOrderPrice;


    /**
     * 出售價格
     */
    private BigDecimal sellOrderPrice;


    /**
     * 訂單方向
     */
    private Integer orderDirection;
    /**
     * 下單數量
     */
    private Integer orderNum;
    /**
     * 訂單槓桿
     */
    private BigDecimal orderLever;
    /**
     * 訂單總金額
     */
    private BigDecimal orderTotalPrice;
    /**
     * 訂單手續費
     */
    private BigDecimal orderFee;
    /**
     * 訂單正交稅
     */
    private BigDecimal orderSpread;


    /**
     * 損益
     */
    private BigDecimal profitAndLose;

    /**
     * 總損益
     */
    private BigDecimal allProfitAndLose;


    /**
     * 股票關鍵字
     */
    private String stockPlate;
    /*點差費金額*/
    private BigDecimal spreadRatePrice;
    /*追加保證金額*/
    private BigDecimal marginAdd;

    /**
     * 0 買入 1 賣出
     */
    private Integer orderType;

    @TableField(exist = false)
    private BigDecimal nowPrice;



}

