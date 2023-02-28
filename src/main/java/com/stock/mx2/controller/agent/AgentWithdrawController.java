package com.stock.mx2.controller.agent;


import com.stock.mx2.common.ServerResponse;

import com.stock.mx2.service.IAgentUserService;
import com.stock.mx2.service.IUserWithdrawService;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping({"/agent/withdraw/"})
public class AgentWithdrawController {

    private static final Logger log = LoggerFactory.getLogger(AgentWithdrawController.class);

    @Autowired
    IUserWithdrawService iUserWithdrawService;
    @Autowired
    IAgentUserService iAgentUserService;

    //分頁查詢所有出金記錄信息及模糊查詢
    @RequestMapping({"list.do"})
    @ResponseBody
    public ServerResponse list(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "8") int pageSize, @RequestParam(value = "realName", required = false) String realName, @RequestParam(value = "state", required = false) Integer state, @RequestParam(value = "agentId", required = false) Integer agentId, HttpServletRequest request) {
        return this.iUserWithdrawService.listByAgent(agentId, realName, state, request, pageNum, pageSize);
    }
}
