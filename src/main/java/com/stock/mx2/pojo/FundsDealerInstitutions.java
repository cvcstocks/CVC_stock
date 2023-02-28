package com.stock.mx2.pojo;

import java.io.Serializable;
import lombok.Data;
import java.util.Date;
import java.util.List;

/**
 *  配資券商機構
 * @author lr 2020-07-24
 */
@Data
public class FundsDealerInstitutions implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主鍵id
     */
    private Integer id;

    /**
     * 券商編號id
     */
    private Integer dealerNumber;

    /**
     * 券商名稱
     */
    private String dealerName;

    /**
     * 客戶端版本號
     */
    private String clientVersionNumber;

    /**
     * 備註
     */
    private String remarks;

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

    public FundsDealerInstitutions() {
    }

}
