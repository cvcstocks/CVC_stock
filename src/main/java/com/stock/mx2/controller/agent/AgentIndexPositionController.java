//
//package com.stock.mx2.controller.agent;
//
//
//import com.stock.mx2.common.ServerResponse;
//import com.stock.mx2.service.IUserIndexPositionService;
//import javax.servlet.http.HttpServletRequest;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.ResponseBody;
//
//@Controller
//@RequestMapping({"/agent/index/position/"})
//public class AgentIndexPositionController {
//    private static final Logger log = LoggerFactory.getLogger(AgentIndexPositionController.class);
//
//    @Autowired
//    IUserIndexPositionService iUserIndexPositionService;
//
//    //查詢持倉管理 所有指數平倉單信息及模糊查詢
//    @RequestMapping({"list.do"})
//    @ResponseBody
//    public ServerResponse list(HttpServletRequest request, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize, @RequestParam(value = "positionType", required = false) Integer positionType, @RequestParam(value = "state", required = false) Integer state, @RequestParam(value = "userId", required = false) Integer userId, @RequestParam(value = "agentId", required = false) Integer agentId, @RequestParam(value = "positionSn", required = false) String positionSn, @RequestParam(value = "beginTime", required = false) String beginTime, @RequestParam(value = "endTime", required = false) String endTime) {
//        return this.iUserIndexPositionService.listByAgent(positionType, state, userId, agentId, positionSn, beginTime, endTime, request, pageNum, pageSize);
//    }
//}
