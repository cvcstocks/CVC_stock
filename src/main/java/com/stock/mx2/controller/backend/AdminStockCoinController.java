package com.stock.mx2.controller.backend;

import com.stock.mx2.common.ServerResponse;

import com.stock.mx2.pojo.StockCoin;

import com.stock.mx2.service.IStockCoinService;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping({"/admin/coin/"})
public class AdminStockCoinController {
    private static final Logger log = LoggerFactory.getLogger(AdminStockCoinController.class);

    @Autowired
    IStockCoinService iStockCoinService;

    //分頁查詢基幣管理 基幣信息及模糊查詢
    @RequestMapping({"list.do"})
    @ResponseBody
    public ServerResponse list(@RequestParam(value = "coinName", required = false) String coinName, @RequestParam(value = "coinCode", required = false) String coinCode, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        return this.iStockCoinService.listByAdmin(coinName, coinCode, pageNum, pageSize);
    }

    //添加基幣管理 基幣信息
    @RequestMapping({"add.do"})
    @ResponseBody
    public ServerResponse add(StockCoin stockCoin) {
        return this.iStockCoinService.add(stockCoin);
    }

    //修改基幣管理 基幣信息
    @RequestMapping({"update.do"})
    @ResponseBody
    public ServerResponse update(StockCoin stockCoin) {
        return this.iStockCoinService.update(stockCoin);
    }

    //查詢指定基幣信息
    @RequestMapping({"getSelectCoin.do"})
    @ResponseBody
    public ServerResponse getSelectCoin() {
        return ServerResponse.createBySuccess(this.iStockCoinService.getSelectCoin());
    }
}
