package com.stock.mx2.controller.backend;


import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.SitePay;
import com.stock.mx2.service.ISitePayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping({"/admin/pay/"})
public class AdminSitePayController {
    private static final Logger log = LoggerFactory.getLogger(AdminSitePayController.class);

    @Autowired
    ISitePayService iSitePayService;

    //添加系統基本設置 支付渠道設置信息
    @RequestMapping({"add.do"})
    @ResponseBody
    public ServerResponse add(SitePay sitePay) {
        return this.iSitePayService.add(sitePay);
    }

    //修改系統基本設置 支付渠道設置信息
    @RequestMapping({"update.do"})
    @ResponseBody
    public ServerResponse update(SitePay sitePay) {
        return this.iSitePayService.update(sitePay);
    }

    //分頁查詢系統基本設置 支付渠道設置信息及模糊查詢
    @RequestMapping({"list.do"})
    @ResponseBody
    public ServerResponse list(@RequestParam("channelType") String channelType, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        return this.iSitePayService.listByAdmin(channelType, pageNum, pageSize);
    }

    //刪除系統基本設置 支付渠道設置信息
    @RequestMapping({"del.do"})
    @ResponseBody
    public ServerResponse del(@RequestParam("cId") Integer cId) {
        return this.iSitePayService.del(cId);
    }

}
