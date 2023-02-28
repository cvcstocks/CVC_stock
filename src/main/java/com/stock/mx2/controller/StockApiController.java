package com.stock.mx2.controller;

import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.service.IStockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping({"/api/stock/"})
public class StockApiController {
    private static final Logger log = LoggerFactory.getLogger(StockApiController.class);

    @Autowired
    IStockService iStockService;

    //查詢 股票指數、大盤指數信息
    @RequestMapping({"getMarket.do"})
    @ResponseBody
    public ServerResponse getMarket() {
        return this.iStockService.getMarket();
    }

    //查詢官網PC端交易 所有股票信息及指定股票信息
    @RequestMapping({"getStock.do"})
    @ResponseBody
    public ServerResponse getStock(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "1000") int pageSize, @RequestParam(value = "stockPlate", required = false) String stockPlate, @RequestParam(value = "stockType", required = false) String stockType, @RequestParam(value = "keyWords", required = false) String keyWords, HttpServletRequest request

            , @RequestParam(value = "stockTypeInt", required = false) String stockTypeInt,
                                   @RequestParam(value = "exchange", required = false) String exchange
    ) {
        return this.iStockService.getStock(pageNum, pageSize, keyWords, stockPlate, stockType, request,stockTypeInt,exchange);
    }

    //通過股票代碼查詢股票信息
    @RequestMapping({"getSingleStock.do"})
    @ResponseBody
    public ServerResponse getSingleStock(@RequestParam("code") String code) {
        return this.iStockService.getSingleStock(code);
    }

    @RequestMapping({"getMinK.do"})
    @ResponseBody
    public ServerResponse getMinK(@RequestParam("code") String code, @RequestParam("time") Integer time, @RequestParam("ma") Integer ma, @RequestParam("size") Integer size) {
        return this.iStockService.getMinK(code, time, ma, size);
    }

    /*查詢股票日線*/
    @RequestMapping({"getDayK.do"})
    @ResponseBody
    public ServerResponse getDayK(@RequestParam("code") String code) {
        return this.iStockService.getDayK_Echarts(code);
    }

    //查詢股票歷史數據數據
    @RequestMapping({"getMinK_Echarts.do"})
    @ResponseBody
    public ServerResponse getMinK_Echarts(@RequestParam("code") String code, @RequestParam("time") Integer time, @RequestParam("ma") Integer ma, @RequestParam("size") Integer size) {
        return this.iStockService.getMinK_Echarts(code, time, ma, size);
    }

    /*期貨分時-k線*/
    @RequestMapping({"getFuturesMinK_Echarts.do"})
    @ResponseBody
    public ServerResponse getFuturesMinK_Echarts(@RequestParam("code") String code, @RequestParam("time") Integer time, @RequestParam("size") Integer size) {
        return this.iStockService.getFuturesMinK_Echarts(code, time, size);
    }

    /*指數分時-k線*/
    @RequestMapping({"getIndexMinK_Echarts.do"})
    @ResponseBody
    public ServerResponse getIndexMinK_Echarts(@RequestParam("code") String code, @RequestParam("time") Integer time, @RequestParam("size") Integer size) {
        return this.iStockService.getIndexMinK_Echarts(code, time, size);
    }

    /*查詢期貨日線*/
    @RequestMapping({"getFuturesDayK.do"})
    @ResponseBody
    public ServerResponse getFuturesDayK(@RequestParam("code") String code) {
        return this.iStockService.getFuturesDayK(code);
    }

    /*指數日線*/
    @RequestMapping({"getIndexDayK.do"})
    @ResponseBody
    public ServerResponse getIndexDayK(@RequestParam("code") String code) {
        return this.iStockService.getIndexDayK(code);
    }
    //台股Api
    @RequestMapping({"getTwMarket.do"})
    @ResponseBody
    public ServerResponse getTwMarket() {
        return this.iStockService.getTwMarket();
    }
    //細分類別 getStockType.do
    @RequestMapping({"getStockType.do"})
    @ResponseBody
    public ServerResponse getStockType(@RequestParam(value = "exchange",required = false) String exchange) {

        return this.iStockService.getStockType(exchange);
    }
}
