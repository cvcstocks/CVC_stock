package com.stock.mx2.controller.agent;

import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.service.IAgentUserService;
import com.stock.mx2.service.IUserService;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping({"/agent/user/"})
public class AgentUserController {
    private static final Logger log = LoggerFactory.getLogger(AgentUserController.class);

    @Autowired
    IUserService iUserService;

    @Autowired
    IAgentUserService iAgentUserService;

    //分頁查詢用戶管理信息及模糊查詢
    @RequestMapping({"list.do"})
    @ResponseBody
    public ServerResponse list(@RequestParam(value = "realName", required = false) String realName, @RequestParam(value = "phone", required = false) String phone, @RequestParam(value = "agentId", required = false) Integer agentId, @RequestParam(value = "accountType", required = false) Integer accountType, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "12") int pageSize, HttpServletRequest request) {
        return this.iUserService.listByAgent(realName, phone, agentId, accountType, pageNum, pageSize, request);
    }
}
