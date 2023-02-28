package com.stock.mx2.pojo;

import java.io.Serializable;
import java.util.Date;

public class SiteMessage implements Serializable {
    /**
     * 主鍵id
     */
    private Integer id;

    /**
     * 用戶id
     */
    private Integer userId;

    /**
     * 用戶名
     */
    private String userName;

    /**
     * 類型名稱
     */
    private String typeName;



    /**
     * 狀態：1、未讀，2、已讀
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

    /**
     * 內容
     */
    private String content;


    public SiteMessage() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getAddTime() {
        return addTime;
    }

    public void setAddTime(Date addTime) {
        this.addTime = addTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}

