package com.stock.mx2.controller;


import com.stock.mx2.common.ServerResponse;

import com.stock.mx2.pojo.SiteSmsLog;
import com.stock.mx2.service.ISiteSmsLogService;
import com.stock.mx2.service.ISmsService;

import com.stock.mx2.utils.DateTimeUtil;
import com.stock.mx2.utils.smsUtil.smsUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping({"/api/sms/"})
public class SmsApiController {
    private static final Logger log = LoggerFactory.getLogger(SmsApiController.class);

    @Autowired
    ISmsService iSmsService;

    @Autowired
    ISiteSmsLogService iSiteSmsLogService;

    //註冊用戶 短信發送
    @RequestMapping({"sendRegSms.do"})
    @ResponseBody
    public ServerResponse sendRegSms(String phoneNum) {
        if (StringUtils.isBlank(phoneNum)) {
            return ServerResponse.createByErrorMsg("發送失敗，手機號不能為空");
        }
        smsUtil smsUtil = new smsUtil();
        log.info("smsphone"+phoneNum);
        String code = smsUtil.sendSMS(phoneNum);
        if (!StringUtils.isEmpty(code)) {
            SiteSmsLog siteSmsLog = new SiteSmsLog();
            siteSmsLog.setSmsPhone(phoneNum);
            siteSmsLog.setSmsTitle("註冊驗證碼");
            siteSmsLog.setSmsCnt(code);
            siteSmsLog.setSmsStatus(Integer.valueOf(0));
            siteSmsLog.setSmsTemplate("字段無用");
            siteSmsLog.setAddTime(DateTimeUtil.getCurrentDate());
            iSiteSmsLogService.addData(siteSmsLog);
            return ServerResponse.createBySuccessMsg("發送成功");
        } else {
            return ServerResponse.createByErrorMsg("短信發送失敗，請重試");
        }
    }

    //找回密碼 短信發送
    @RequestMapping({"sendForgetSms.do"})
    @ResponseBody
    public ServerResponse sendForgetSms(String phoneNum) {
        return this.iSmsService.sendAliyunSMS(phoneNum, "SMS_174915941");
    }
}
