package com.stock.mx2.pojo;

import java.io.Serializable;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 *  配資證券信息
 * @author lr 2020-07-24
 */
@Data
public class FundsSecuritiesInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主鍵id
     */
    private Integer id;

    /**
     * 證券機構id
     */
    private Integer dealerInstitutionsId;

    /**
     * 證券機構名稱
     */
    private String dealerInstitutionsName;

    /**
     * 證券營業部
     */
    private String salesDepartment;

    /**
     * 證券帳戶
     */
    private String accountName;

    /**
     * 交易通帳戶
     */
    private String transactAccount;

    /**
     * 交易通密碼
     */
    private String transactPassword;

    /**
     * 通訊密碼
     */
    private String communicationPassword;

    /**
     * 傭金比例
     */
    private BigDecimal commissionRatio;

    /**
     * 最低傭金，單位元
     */
    private BigDecimal minimumCommissions;

    /**
     * 狀態：1、啟用，0、停用
     */
    private Integer status;

    /**
     * 添加時間
     */
    private Date addTime;

    /**
     * 修改時間
     */
    private Date updateTime;

    public FundsSecuritiesInfo() {
    }

}
