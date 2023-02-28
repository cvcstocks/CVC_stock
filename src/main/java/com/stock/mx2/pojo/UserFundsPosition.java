package com.stock.mx2.pojo;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 *  分倉交易表
 * @author lr 2020-07-27
 */
@Data
public class UserFundsPosition implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主鍵id
     */
    private Integer id;

    /**
     * 帳戶類型：0、實盤，1、模擬盤
     */
    private Integer positionType;

    /**
     * 訂單編號
     */
    private String positionSn;

    /**
     * 用戶id
     */
    private Integer userId;

    /**
     * 用戶真實姓名
     */
    private String nickName;

    /**
     * 子帳戶編號，默認從80000000開始
     */
    private Integer subaccountNumber;

    /**
     * 代理id
     */
    private Integer agentId;

    /**
     * 股票id
     */
    private Integer stockId;

    /**
     * 股票名稱
     */
    private String stockName;

    /**
     * 股票代碼
     */
    private String stockCode;

    /**
     * 股票gid
     */
    private String stockGid;

    /**
     * 股票簡拼
     */
    private String stockSpell;

    /**
     * 入倉單號
     */
    private String buyOrderId;

    /**
     * 入倉時間
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")

    private Date buyOrderTime;

    /**
     * 入倉單價
     */
    private BigDecimal buyOrderPrice;

    /**
     * 出倉單號
     */
    private String sellOrderId;

    /**
     * 出倉時間
     */
    private Date sellOrderTime;

    /**
     * 出倉單價
     */
    private BigDecimal sellOrderPrice;

    /**
     * 下單方向：買漲、買跌
     */
    private Integer orderDirection;

    /**
     * 訂單數量
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
     * 手續費
     */
    private BigDecimal orderFee;

    /**
     * 印花稅
     */
    private BigDecimal orderSpread;

    /**
     * 留倉費
     */
    private BigDecimal orderStayFee;

    /**
     * 留倉天數
     */
    private Integer orderStayDays;

    /**
     * 浮動盈虧
     */
    private BigDecimal profitAndLose;

    /**
     * 總盈虧
     */
    private BigDecimal allProfitAndLose;

    /**
     * 是否鎖倉：1、是，0、否
     */
    private Integer isLock;

    /**
     * 鎖倉原因
     */
    private String lockMsg;

    /**
     * 股票板塊：科創
     */
    private String stockPlate;

    /**
     * 點差費
     */
    private BigDecimal spreadRatePrice;

    public UserFundsPosition() {
    }

}

