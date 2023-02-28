package com.stock.mx2.service;

import com.github.pagehelper.PageInfo;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.AgentAgencyFee;
import com.stock.mx2.pojo.AgentUser;
import com.stock.mx2.pojo.UserPositionItem;

import javax.servlet.http.HttpServletRequest;

/**
 * @author gg
 * @date 2020/06/06
 */
public interface IAgentAgencyFeeService {
    /**
     * 新增
     */
    int insert(AgentAgencyFee agentAgencyFee);


    /**
     * 更新
     */
    int update(AgentAgencyFee agentAgencyFee);

    /**
     * 代理費收入，考慮多級代理的問題
     */
    int AgencyFeeIncome(int feeType, UserPositionItem positionItem);

    /*查詢代理利潤明細列表*/
    ServerResponse<PageInfo> getAgentAgencyFeeList(int pageNum, int pageSize, HttpServletRequest request);

    /*查詢代理登錄信息*/
    AgentUser getCurrentAgent(HttpServletRequest request);

}
