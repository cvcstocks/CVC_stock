package com.stock.mx2.controller.backend;


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
@RequestMapping({"/admin/agent/"})
public class AdminAgentController {
    private static final Logger log = LoggerFactory.getLogger(AdminAgentController.class);

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
        return this.iAgentUserService.listByAdmin(realName, phone, pageNum, pageSize, id, request);
    }

    //添加代理管理 下級代理
    @RequestMapping({"add.do"})
    @ResponseBody
    public ServerResponse add(AgentUser agentUser, HttpServletRequest request) {
        return this.iAgentUserService.add(agentUser, request);
    }

    //修改代理管理 代理信息
    @RequestMapping({"update.do"})
    @ResponseBody
    public ServerResponse update(AgentUser agentUser) {
        return this.iAgentUserService.update(agentUser);
    }

    //查詢股票持倉統計信息
    @RequestMapping({"getIncome.do"})
    @ResponseBody
    public ServerResponse getIncome(@RequestParam(value = "agentId", required = false) Integer agentId,Integer buyType, @RequestParam(value = "positionType", required = false) Integer positionType, @RequestParam(value = "beginTime", required = false) String beginTime, @RequestParam(value = "endTime", required = false) String endTime, HttpServletRequest request) {
        return this.iUserPositionService.getIncome(agentId, positionType, beginTime, endTime,buyType);
    }

//    //查詢指數持倉統計信息
//    @RequestMapping({"getIndexIncome.do"})
//    @ResponseBody
//    public ServerResponse getIndexIncome(@RequestParam(value = "agentId", required = false) Integer agentId, @RequestParam(value = "positionType", required = false) Integer positionType, @RequestParam(value = "beginTime", required = false) String beginTime, @RequestParam(value = "endTime", required = false) String endTime, HttpServletRequest request) {
//        return this.iUserIndexPositionService.getIndexIncome(agentId, positionType, beginTime, endTime);
//    }
//
//    //查詢期貨持倉統計信息
//    @RequestMapping({"getFuturesIncome.do"})
//    @ResponseBody
//    public ServerResponse getFuturesIncome(@RequestParam(value = "agentId", required = false) Integer agentId, @RequestParam(value = "positionType", required = false) Integer positionType, @RequestParam(value = "beginTime", required = false) String beginTime, @RequestParam(value = "endTime", required = false) String endTime, HttpServletRequest request) {
//        return this.iUserFuturesPositionService.getFuturesIncome(agentId, positionType, beginTime, endTime);
//    }

    //修改用戶列表 用戶資金入款/扣款
    @RequestMapping({"updateAgentAmt.do"})
    @ResponseBody
    public ServerResponse updateAgentAmt(Integer agentId, Integer amt, Integer direction) {
        return this.iAgentUserService.updateAgentAmt(agentId, amt, direction);
    }

    //刪除代理
    @RequestMapping({"delAgent.do"})
    @ResponseBody
    public ServerResponse delAgent(Integer agentId) {
        return this.iAgentUserService.delAgent(agentId);
    }
}
