package com.stock.mx2.controller.agent;


import com.github.pagehelper.PageInfo;

import com.stock.mx2.common.ServerResponse;

import com.stock.mx2.service.IUserRechargeService;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping({"/agent/recharge/"})
public class AgentRechargeController {

    private static final Logger log = LoggerFactory.getLogger(AgentRechargeController.class);

    @Autowired
    IUserRechargeService iUserRechargeService;

    //分頁查詢入金記錄信息及模糊查詢
    @RequestMapping({"list.do"})
    @ResponseBody
    public ServerResponse<PageInfo> list(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize, @RequestParam(value = "realName", required = false) String realName, @RequestParam(value = "payChannel", required = false) String payChannel, @RequestParam(value = "state", required = false) Integer state, @RequestParam(value = "agentId", required = false) Integer agentId, HttpServletRequest request) {
        return this.iUserRechargeService.listByAgent(agentId, realName, payChannel, state, request, pageNum, pageSize);
    }
}
