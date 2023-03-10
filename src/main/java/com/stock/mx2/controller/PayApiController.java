package com.stock.mx2.controller;

import com.google.common.base.Strings;
import com.stock.mx2.common.CmcPayConfig;
import com.stock.mx2.common.ServerResponse;

import com.stock.mx2.dao.UserRechargeMapper;
import com.stock.mx2.pojo.UserRecharge;
import com.stock.mx2.service.IPayService;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;

import com.stock.mx2.service.IUserRechargeService;
import com.stock.mx2.service.IUserService;
import com.stock.mx2.utils.pay.CmcPayOuterRequestUtil;
import net.sf.json.JSONObject;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping({"/api/pay/"})
public class PayApiController {
    private static final Logger log = LoggerFactory.getLogger(PayApiController.class);

    @Autowired
    IPayService iPayService;

    @Autowired
    IUserService iUserService;

    @Autowired
    IUserRechargeService iUserRechargeService;

    @Autowired
    UserRechargeMapper userRechargeMapper;

    @RequestMapping({"juhe1Notify.do"})
    @ResponseBody
    public void juhe1Notify(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServerResponse serverResponse = this.iPayService.juhe1Notify(request);
        if (serverResponse.isSuccess()) {
            response.getWriter().write("ok");
            log.info("第一個支付渠道的通知 返回 ok 成功");
        } else {
            log.error("juhe1Notify error Msg = {}", serverResponse.getMsg());
        }
    }

    @RequestMapping({"juhenewpayNotify.do"})
    @ResponseBody
    public JSONObject juhenewpayNotify(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("============開始回調===============");
        LinkedMap map = new LinkedMap();
        String orderno = (String) request.getParameter("orderno");
        String amount = (String) request.getParameter("payamount");
        String[] arr = orderno.split("_");
        map.put("payamount", request.getParameter("payamount"));
        map.put("orderno", orderno);
        map.put("status", (String) request.getParameter("status"));
        log.info("回調創建訂單前map==="+map.toString());

        JSONObject jsonObj = new JSONObject();
        Map<String,Object> json = new HashMap<String,Object>();


        if ("1".equals((String) request.getParameter("status"))) {
            // 這裏編寫用戶業務邏輯代碼，如存儲訂單狀態，自動發貨等
            //TODO
            if (amount != null & !StringUtils.isEmpty(amount)) {
                System.out.println("============更新用戶金額===============");
                Double aDouble = Double.valueOf(amount);
                Integer integer = aDouble.intValue();
                //判斷是否回調成功
                UserRecharge userRecharge = this.userRechargeMapper.findUserRechargeByOrderSn(orderno);
                if(userRecharge == null){
                    System.out.println("============充值完成===============");
                    iUserRechargeService.createOrder(Integer.valueOf(arr[0]), 1, aDouble.intValue(), orderno);
                    jsonObj.put("reason", "success");
                } else {
                    System.out.println("============充值失敗，已回調成功無需重複回調===============");
                    jsonObj.put("reason", "error");
                }
            } else{
                jsonObj.put("reason", "error");
            }
            System.out.println("============回調成功並結束===============");
            //jsonObj.putAll(map);
            return jsonObj;
        } else {
            System.out.println("============回調失敗並結束===============");
            jsonObj.put("reason", "error");
            return jsonObj;
        }

    }

    @RequestMapping({"juheh5payNotify.do"})
    @ResponseBody
    public void juheh5payNotify(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("============開始回調===============");
        LinkedMap map = new LinkedMap();
        String orderno = (String) request.getParameter("name");
        String amount = (String) request.getParameter("money");
        String[] arr = orderno.split("_");
        map.put("money", request.getParameter("money"));
        map.put("name", orderno);
        log.info("回調創建訂單前map==="+map.toString());

        JSONObject jsonObj = new JSONObject();
        Map<String,Object> json = new HashMap<String,Object>();


        //if ("200".equals((String) request.getParameter("status"))) {
            // 這裏編寫用戶業務邏輯代碼，如存儲訂單狀態，自動發貨等
            //TODO
            if (amount != null & !StringUtils.isEmpty(amount)) {
                System.out.println("============更新用戶金額===============");
                Double aDouble = Double.valueOf(amount);
                Integer integer = aDouble.intValue();
                //判斷是否回調成功
                UserRecharge userRecharge = this.userRechargeMapper.findUserRechargeByOrderSn(orderno);
                if(userRecharge == null){
                    System.out.println("============充值完成===============");
                    iUserRechargeService.createOrder(Integer.valueOf(arr[0]), 1, aDouble.intValue(), orderno);
                } else {
                    System.out.println("============充值失敗，已回調成功無需重複回調===============");
                }
            } else{
            }
            System.out.println("============回調成功並結束===============");
            //jsonObj.putAll(map);
            response.getWriter().write("success");
        /*} else {
            System.out.println("============回調失敗並結束===============");
            response.getWriter().write("error");
        }*/

    }

    @RequestMapping({"flyNotify.do"})
    @ResponseBody
    public void flyNotify(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServerResponse serverResponse = this.iPayService.flyNotify(request);
        if (serverResponse.isSuccess()) {
            response.getWriter().write("{\"reason\":\"success\"}");
            log.info("fly 支付渠道的通知 返回 success 成功");
        } else {
            log.error("fly notify error Msg = {}", serverResponse.getMsg());
        }
    }
}
