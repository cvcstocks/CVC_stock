package com.stock.mx2.pojo;

import java.io.Serializable;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 *  funds_lever
 * @author lr 2020-07-23
 */
@Data
public class FundsLever implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主鍵id
     */
    private Integer id;

    /**
     * 周期類型：1按天、2按周、3按月
     */
    private Integer cycleType;

    /**
     * 槓桿
     */
    private Integer lever;

    /**
     * 管理費率
     */
    private BigDecimal manageRate;

    /**
     * 單股持倉比例
     */
    private BigDecimal singleHoldingRatio;

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

    public FundsLever() {
    }

}
