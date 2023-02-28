package com.stock.mx2.controller;


import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.service.IStockIndexService;
import com.stock.mx2.vo.stock.MarketVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;


@Controller
@RequestMapping({"/api/index/"})
public class StockIndexApiController {
    private static final Logger log = LoggerFactory.getLogger(StockIndexApiController.class);

    @Autowired
    IStockIndexService iStockIndexService;

    //查詢指數信息
    @RequestMapping({"queryHomeIndex.do"})
    @ResponseBody
    public ServerResponse queryHomeIndex() {
        return this.iStockIndexService.queryHomeIndex();
    }

    @RequestMapping({"queryListIndex.do"})
    @ResponseBody
    public ServerResponse queryListIndex(HttpServletRequest request) {
        return this.iStockIndexService.queryListIndex(request);
    }

    @RequestMapping({"queryTransIndex.do"})
    @ResponseBody
    public ServerResponse queryTransIndex(@RequestParam("indexId") Integer indexId) {
        return this.iStockIndexService.queryTransIndex(indexId);
    }

    @RequestMapping({"querySingleIndex.do"})
    @ResponseBody
    public ServerResponse querySingleIndex(@RequestParam("indexCode") String indexCode) {
        MarketVO marketVO = this.iStockIndexService.querySingleIndex(indexCode);
        return ServerResponse.createBySuccess(marketVO);
    }
}
