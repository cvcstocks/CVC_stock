package com.stock.mx2.pojo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 *  新股申購
 * @author lr 2020-09-11
 */
@Data
public class UserStockSubscribe implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主鍵id
     */
    private Integer id;

    /**
     * 用戶id
     */
    private Integer userId;

    /**
     * 用戶真實姓名
     */
    private String realName;

    /**
     * 用戶手機號
     */
    private String phone;

    /**
     * 管理員id
     */
    private Integer adminId;

    /**
     * 管理員姓名
     */
    private String adminName;

    /**
     * 提交金額
     */
    private BigDecimal submitAmount;

    /**
     * 狀態：1、預約成功，2、提交成功，3、已中籤，4、未中籤
     */
    private Integer status;

    /**
     * 添加時間
     */
    private Date addTime;

    /**
     * 提交時間
     */
    private Date submitTime;

    /**
     * 中籤時間
     */
    private Date endTime;

    /**
     * 備註
     */
    private String remarks;

    public UserStockSubscribe() {
    }

}
