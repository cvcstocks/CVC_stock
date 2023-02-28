package com.stock.mx2.controller.protol;


import com.github.pagehelper.PageInfo;
import com.stock.mx2.annotation.SameUrlData;
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
@RequestMapping({"/user/recharge/"})
public class UserRechargeController {
    private static final Logger log = LoggerFactory.getLogger(UserRechargeController.class);

    @Autowired
    IUserRechargeService iUserRechargeService;

    //分頁查詢所有充值記錄
    @RequestMapping({"list.do"})
    @ResponseBody
    public ServerResponse<PageInfo> list(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize, @RequestParam(value = "payChannel", required = false) String payChannel, @RequestParam(value = "orderStatus", required = false) String orderStatus, HttpServletRequest request) {
        return this.iUserRechargeService.findUserChargeList(payChannel, orderStatus, request, pageNum, pageSize);
    }

    //帳戶線下充值轉賬 創建充值訂單
    @RequestMapping({"inMoney.do"})
    @ResponseBody
    @SameUrlData
    public ServerResponse inMoney(String amt, String payType, HttpServletRequest request) {
        return this.iUserRechargeService.inMoney(amt, payType, request);
    }
}

