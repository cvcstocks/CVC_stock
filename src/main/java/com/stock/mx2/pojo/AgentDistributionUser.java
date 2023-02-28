package com.stock.mx2.pojo;

import java.io.Serializable;
import java.util.Date;

public class AgentDistributionUser implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主鍵id
     */
    private Integer id;

    /**
     * 代理id
     */
    private Integer agentId;

    /**
     * 上級代理id
     */
    private Integer parentId;

    /**
     * 上級代理級別
     */
    private Integer parentLevel;

    /**
     * 添加時間
     */
    private Date addTime;

    /**
     * 修改時間
     */
    private Date updateTime;


   /* public AgentDistributionUser() {
    }*/

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAgentId() {
        return agentId;
    }

    public void setAgentId(Integer agentId) {
        this.agentId = agentId;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Integer getParentLevel() {
        return parentLevel;
    }

    public void setParentLevel(Integer parentLevel) {
        this.parentLevel = parentLevel;
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
}
