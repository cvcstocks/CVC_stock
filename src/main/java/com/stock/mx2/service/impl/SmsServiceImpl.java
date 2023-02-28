package com.stock.mx2.service.impl;


import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.dao.SiteSmsLogMapper;
import com.stock.mx2.pojo.SiteSmsLog;
import com.stock.mx2.service.ISmsService;
import com.stock.mx2.utils.DateTimeUtil;
import com.stock.mx2.utils.redis.RedisShardedPoolUtils;
import com.stock.mx2.utils.sms.ali.AliyunSms;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("iSmsService")
public class SmsServiceImpl implements ISmsService {
    private static final Logger log = LoggerFactory.getLogger(SmsServiceImpl.class);


    @Autowired
    SiteSmsLogMapper siteSmsLogMapper;

    public ServerResponse sendAliyunSMS(String phoneNum, String ali_template) {
        System.out.println(phoneNum);
        if (StringUtils.isBlank(phoneNum)) return ServerResponse.createByErrorMsg("發送失敗，手機號不能為空");

        String yzmCode = RandomStringUtils.randomNumeric(4);
        System.out.println("驗證碼：" + yzmCode);

        SendSmsResponse response = null;
        try {
            response = AliyunSms.sendSms(phoneNum, "鴻鵠", ali_template, yzmCode);
        } catch (Exception e) {
            log.error("發送短信異常：{}", e);
        }
        System.out.println("短信接口返回的數據----------------");
        System.out.println("Code=" + response.getCode());
        System.out.println("Message=" + response.getMessage());
        System.out.println("RequestId=" + response.getRequestId());
        System.out.println("BizId=" + response.getBizId());
        if (response.getCode() != null && response.getCode().equals("OK")) {
            String keys = "AliyunSmsCode:" + phoneNum;
            RedisShardedPoolUtils.setEx(keys, yzmCode, 5400);
            SiteSmsLog siteSmsLog = new SiteSmsLog();
            siteSmsLog.setSmsPhone(phoneNum);
            siteSmsLog.setSmsTitle("註冊驗證碼");
            siteSmsLog.setSmsCnt(yzmCode);
            siteSmsLog.setSmsTemplate(ali_template);
            siteSmsLog.setSmsStatus(Integer.valueOf(0));
            siteSmsLog.setAddTime(DateTimeUtil.getCurrentDate());
            this.siteSmsLogMapper.insert(siteSmsLog);
            return ServerResponse.createBySuccessMsg("發送成功");
        }
        return ServerResponse.createByErrorMsg("短信發送失敗，請重試");
    }
}
