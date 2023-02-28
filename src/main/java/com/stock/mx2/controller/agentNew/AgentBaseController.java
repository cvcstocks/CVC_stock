package com.stock.mx2.controller.agentNew;

import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.dao.AgentUserMapper;
import com.stock.mx2.pojo.AgentUser;
import com.stock.mx2.service.IAgentUserService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;

public class AgentBaseController {
    @Autowired
    IAgentUserService iAgentUserService;

    @Autowired
    private AgentUserMapper agentUserMapper;
    public ServerResponse<Integer> getSearchId(Integer agentId, HttpServletRequest request){


        AgentUser currentAgent = this.iAgentUserService.getCurrentAgent(request);

        if (currentAgent == null){
            return   ServerResponse.createByError("請先登錄",null);
        }
        if (agentId != null && agentId!=0) {

            AgentUser agentUser = this.agentUserMapper.selectByPrimaryKey(agentId);
            if (agentUser ==null){
                return     ServerResponse.createByError("請先登錄",null);
            }
            if (agentUser.getParentId() != currentAgent.getId()) {

                return ServerResponse.createByErrorMsg("不能查詢非下級代理記錄");

            }

        }
        Integer searchId = null;

        if (agentId == null||agentId==0) {

            searchId = currentAgent.getId();

        } else {

            searchId = agentId;

        }
        return ServerResponse.createBySuccess(searchId);
    }
}
