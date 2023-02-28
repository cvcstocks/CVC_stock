package com.stock.mx2.utils.smsUtil;

import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.controller.SmsApiController;
import com.stock.mx2.dao.SiteSmsLogMapper;
import com.stock.mx2.pojo.SiteSmsLog;
import com.stock.mx2.service.ISiteSmsLogService;
import com.stock.mx2.service.impl.SiteSmsLogServiceImpl;
import com.stock.mx2.utils.DateTimeUtil;
import com.stock.mx2.utils.PropertiesUtil;
import com.stock.mx2.utils.pay.CmcPayOuterRequestUtil;
import com.stock.mx2.utils.redis.RedisShardedPoolUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class smsUtil {
    private static final Logger log = LoggerFactory.getLogger(SmsApiController.class);

    public String sendSMS(String telephone) {
        String code = RandomStringUtils.randomNumeric(4);
        CmcPayOuterRequestUtil requestUtil = new CmcPayOuterRequestUtil();
        /*【高盛商贏】*/
        String uid = PropertiesUtil.getProperty("wj.sms.uid");
        String key = PropertiesUtil.getProperty("wj.sms.key");
        String coding = PropertiesUtil.getProperty("wj.sms.coding");
        String smscontent = "您正在申請手機註冊，驗證碼為：" + code + "，5分鐘內有效！";
        try {
            uid = URLEncoder.encode(uid,"UTF-8");
            smscontent = URLEncoder.encode(smscontent,"UTF-8");
        } catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        String url = "http://"+ coding +".api.smschinese.cn/?Uid="+ uid +"&Key="+ key +"&smsMob=" + telephone + "&smsText="+smscontent;
        log.info("smsurl"+url);
        String result = requestUtil.sendGet(url);
        log.info("smsresult="+result+"==code="+code);
        if (Integer.valueOf(result) < 0) {
            return "";
        } else {
            String keys = "AliyunSmsCode:" + telephone;
            RedisShardedPoolUtils.setEx(keys, code, 5400);
            return code;
        }
    }

}
