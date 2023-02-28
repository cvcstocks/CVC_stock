package com.stock.mx2.controller;


import com.stock.mx2.common.ServerResponse;

import com.stock.mx2.service.IAgentUserService;

import com.stock.mx2.utils.PropertiesUtil;

import com.stock.mx2.utils.redis.CookieUtils;

import com.stock.mx2.utils.redis.JsonUtil;

import com.stock.mx2.utils.redis.RedisConst;

import com.stock.mx2.utils.redis.RedisShardedPoolUtils;

import com.stock.mx2.vo.agent.AgentLoginResultVO;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping({"/api/agent/"})
public class AgentApiController {
    private static final Logger log = LoggerFactory.getLogger(AgentApiController.class);

    @Autowired
    IAgentUserService iAgentUserService;

    //代理後台登錄
    @RequestMapping({"login.do"})
    @ResponseBody
    public ServerResponse login(@RequestParam("agentPhone") String agentPhone, @RequestParam("agentPwd") String agentPwd, @RequestParam(value = "verifyCode", required = false, defaultValue = "") String verifyCode, HttpSession httpSession, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        ServerResponse serverResponse = this.iAgentUserService.login(agentPhone, agentPwd, verifyCode, httpServletRequest);
        String token = RedisConst.getAgentRedisKey(httpSession.getId());
        if (serverResponse.isSuccess()) {
            String redisSetExResult = RedisShardedPoolUtils.setEx(token,
                    JsonUtil.obj2String(serverResponse.getData()), 5400);
            log.info("redis setex agent result : {}", redisSetExResult);
            AgentLoginResultVO resultVO = new AgentLoginResultVO();
            resultVO.setToken(token);
            return ServerResponse.createBySuccess("登陸成功", resultVO);
        }
        return serverResponse;
    }

    //代理後台退出登錄
    @RequestMapping({"logout.do"})
    @ResponseBody
    public ServerResponse logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        String cookie_name = PropertiesUtil.getProperty("agent.cookie.name");
        String logintoken = CookieUtils.readLoginToken(httpServletRequest, cookie_name);
        log.info("代理 token = {} ,退出登陸", logintoken);
        RedisShardedPoolUtils.del(logintoken);
        CookieUtils.delLoginToken(httpServletRequest, httpServletResponse, cookie_name);
        return ServerResponse.createBySuccess();
    }
}
