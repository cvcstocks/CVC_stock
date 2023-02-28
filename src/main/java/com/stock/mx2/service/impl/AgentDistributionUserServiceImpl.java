package com.stock.mx2.service.impl;

import com.stock.mx2.dao.AgentDistributionUserMapper;
import com.stock.mx2.pojo.AgentDistributionUser;
import com.stock.mx2.service.IAgentDistributionUserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service("IAgentDistributionUserService")
public class AgentDistributionUserServiceImpl implements IAgentDistributionUserService {
    @Resource
    private AgentDistributionUserMapper agentDistributionUserMapper;


    @Override
    public int insert(AgentDistributionUser agentDistributionUser) {
        int ret = 0;
        // valid
        if (agentDistributionUser == null) {
            return 0;
        }

        ret = agentDistributionUserMapper.insert(agentDistributionUser);
        return ret;
    }

    @Override
    public int update(AgentDistributionUser agentDistributionUser) {
        int ret = agentDistributionUserMapper.update(agentDistributionUser);
        return ret>0 ? ret: 0;
    }
}
