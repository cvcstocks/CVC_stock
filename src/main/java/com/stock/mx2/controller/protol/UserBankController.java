package com.stock.mx2.controller.protol;


import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.UserBank;
import com.stock.mx2.service.IUserBankService;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping({"/user/bank/"})
public class UserBankController {
    private static final Logger log = LoggerFactory.getLogger(UserBankController.class);

    @Autowired
    IUserBankService iUserBankService;

    @RequestMapping({"add.do"})
    @ResponseBody
    public ServerResponse add(UserBank bank, HttpServletRequest request) {
        return this.iUserBankService.addBank(bank, request);
    }

    //修改銀行卡信息
    @RequestMapping({"update.do"})
    @ResponseBody
    public ServerResponse update(UserBank bank, HttpServletRequest request) {
        return this.iUserBankService.updateBank(bank, request);
    }

    //查詢用戶銀行卡信息
    @RequestMapping({"getBankInfo.do"})
    @ResponseBody
    public ServerResponse getBankInfo(HttpServletRequest request) {
        return this.iUserBankService.getBankInfo(request);
    }
}
