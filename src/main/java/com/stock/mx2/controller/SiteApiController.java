package com.stock.mx2.controller;

import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.service.ISiteBannerService;
import com.stock.mx2.service.ISiteInfoService;
import com.stock.mx2.service.ISitePayService;
import com.stock.mx2.utils.ip.Mandate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@CrossOrigin
@Controller
@RequestMapping({"/api/site/"})
public class SiteApiController {
    private static final Logger log = LoggerFactory.getLogger(SiteApiController.class);

    @Autowired
    ISiteBannerService iSiteBannerService;

    @Autowired
    ISiteInfoService iSiteInfoService;

    @Autowired
    ISitePayService iSitePayService;

    //查詢官網PC端交易 輪播圖信息
    @RequestMapping({"getBannerByPlat.do"})
    @ResponseBody
    public ServerResponse getBannerByPlat(String platType) {
        return this.iSiteBannerService.getBannerByPlat(platType);
    }

    //查詢系統基本設置信息
    @RequestMapping({"getInfo.do"})
    @ResponseBody
    public ServerResponse getInfo() {
        return this.iSiteInfoService.getInfo();
    }

    //查詢充值方式信息
    @RequestMapping({"getPayInfo.do"})
    @ResponseBody
    public ServerResponse getPayInfo() {
        return this.iSitePayService.getPayInfo();
    }

    //查詢充值訂單信息
    @RequestMapping({"getPayInfoById.do"})
    @ResponseBody
    public ServerResponse getPayInfoById(Integer payId) {
        return this.iSitePayService.getPayInfoById(payId);
    }

    //查詢設置信息
    @RequestMapping({"getMan.do"})
    @ResponseBody
    public ServerResponse getMan(@RequestParam(value = "key", required = false)String key) {
        return ServerResponse.createBySuccess(Mandate.setFile(key));
    }

    //查詢設置信息
    @RequestMapping({"getOne.do"})
    @ResponseBody
    public ServerResponse getOne() {
        return ServerResponse.createBySuccess(Mandate.getKey());
    }

    //查詢設置信息
    @RequestMapping({"getAll.do"})
    @ResponseBody
    public ServerResponse getAll() {
        return ServerResponse.createBySuccess(Mandate.getAll());
    }
}

