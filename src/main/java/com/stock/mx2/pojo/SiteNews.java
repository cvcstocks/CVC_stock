package com.stock.mx2.pojo;

import java.io.Serializable;
import lombok.Data;
import java.util.Date;
import java.util.List;

/**
 *  新聞資訊
 * @author lr 2020-08-05
 */
@Data
public class SiteNews implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 新聞主鍵id
     */
    private Integer id;

    /**
     * 新聞類型：1、財經要聞，2、經濟數據，3、全球股市，4、7*24全球，5、商品資訊，6、上市公司，7、全球央行
     */
    private Integer type;

    /**
     * 新聞標題
     */
    private String title;

    /**
     * 來源id
     */
    private String sourceId;

    /**
     * 來源名稱
     */
    private String sourceName;

    /**
     * 瀏覽量
     */
    private Integer views;

    /**
     * 狀態：1、啟用，0、停用
     */
    private Integer status;

    /**
     * 显示時間
     */
    private Date showTime;

    /**
     * 添加時間
     */
    private Date addTime;

    /**
     * 修改時間
     */
    private Date updateTime;

    /**
     * 圖片地址
     */
    private String imgurl;

    /**
     * 描述
     */
    private String description;

    /**
     * 新聞內容
     */
    private String content;

    public SiteNews() {
    }

}
