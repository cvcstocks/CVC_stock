package com.stock.mx2.controller.backend;


import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.UserStockSubscribe;
import com.stock.mx2.service.IUserStockSubscribeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping({"/admin/subscribe/"})
public class AdminUserStockSubscribeController {
    private static final Logger log = LoggerFactory.getLogger(AdminUserStockSubscribeController.class);
    @Autowired
    IUserStockSubscribeService iUserStockSubscribeService;

    //新股申購-列表查詢
    @RequestMapping({"getStockSubscribeList.do"})
    @ResponseBody
    public ServerResponse getStockSubscribeList(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "12") int pageSize, @RequestParam(value = "keyword", defaultValue = "") String keyword, HttpServletRequest request) {
        return this.iUserStockSubscribeService.getList(pageNum, pageSize, keyword, request);
    }

    //新股申購-保存
    @RequestMapping({"saveStockSubscribe.do"})
    @ResponseBody
    public ServerResponse saveStockSubscribe(UserStockSubscribe model, HttpServletRequest request) {
        return this.iUserStockSubscribeService.save(model, request);
    }

    //發送站內信
    @RequestMapping({"sendMsg.do"})
    @ResponseBody
    public ServerResponse sendMsg(UserStockSubscribe model, HttpServletRequest request) {
        return this.iUserStockSubscribeService.sendMsg(model, request);
    }

}
