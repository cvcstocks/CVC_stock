package com.stock.mx2.service;

import com.stock.mx2.pojo.AgentDistributionUser;

public interface IAgentDistributionUserService {
    /**
     * 新增
     */
    int insert(AgentDistributionUser agentDistributionUser);

    /**
     * 更新
     */
    int update(AgentDistributionUser agentDistributionUser);
}
