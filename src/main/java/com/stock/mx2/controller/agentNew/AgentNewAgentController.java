package com.stock.mx2.controller.agentNew;


import com.github.pagehelper.PageInfo;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.AgentUser;
import com.stock.mx2.service.IAgentUserService;
//import com.stock.mx2.service.IUserFuturesPositionService;
//import com.stock.mx2.service.IUserIndexPositionService;
import com.stock.mx2.service.IUserPositionService;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping({"/agentNew/agent/"})
public class AgentNewAgentController extends AgentBaseController{
    private static final Logger log = LoggerFactory.getLogger(AgentNewAgentController.class);

    @Autowired
    IAgentUserService iAgentUserService;

    @Autowired
    IUserPositionService iUserPositionService;

//    @Autowired
//    IUserIndexPositionService iUserIndexPositionService;
//
//    @Autowired
//    IUserFuturesPositionService iUserFuturesPositionService;

    //分頁查詢代理管理 所有代理信息 及模糊查詢
    @RequestMapping({"list.do"})
    @ResponseBody
    public ServerResponse list(@RequestParam(value = "id", defaultValue = "0") int id,@RequestParam(value = "realName", required = false) String realName, @RequestParam(value = "phone", required = false) String phone, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "12") int pageSize, HttpServletRequest request) {

        ServerResponse<Integer> searchData = super.getSearchId(id, request);
        if (searchData.isSuccess()){
            id = searchData.getData();
        }else {
            return searchData;
        }
        return this.iAgentUserService.listByAdmin(realName, phone, pageNum, pageSize, id, request);
    }


    //查詢股票持倉統計信息
    @RequestMapping({"getIncome.do"})
    @ResponseBody
    public ServerResponse getIncome(@RequestParam(value = "agentId", required = false) Integer agentId,Integer buyType, @RequestParam(value = "positionType", required = false) Integer positionType, @RequestParam(value = "beginTime", required = false) String beginTime, @RequestParam(value = "endTime", required = false) String endTime, HttpServletRequest request) {


        ServerResponse<Integer> searchData = super.getSearchId(agentId, request);
        if (searchData.isSuccess()){
            agentId = searchData.getData();
        }else {
            return searchData;
        }

        return this.iUserPositionService.getIncome(agentId, positionType, beginTime, endTime,buyType);
    }

}
