package com.stock.mx2.service.impl;

import com.stock.mx2.common.CmcPayConfig;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.dao.UserMapper;
import com.stock.mx2.dao.UserRechargeMapper;
import com.stock.mx2.pojo.User;
import com.stock.mx2.pojo.UserRecharge;
import com.stock.mx2.service.IPayService;
import com.stock.mx2.service.IUserService;
import com.stock.mx2.utils.KeyUtils;
import com.stock.mx2.utils.PropertiesUtil;
import com.stock.mx2.utils.pay.CmcPayOuterRequestUtil;
import com.stock.mx2.utils.pay.CmcPayTool;
import com.stock.mx2.vo.pay.FlyPayVO;
import com.stock.mx2.vo.pay.GuoPayVO;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("iPayService")
public class PayServiceImpl implements IPayService {
    private static final Logger log = LoggerFactory.getLogger(PayServiceImpl.class);

    @Autowired
    IUserService iUserService;

    @Autowired
    UserRechargeMapper userRechargeMapper;

    @Autowired
    UserMapper userMapper;

    private static final String juhe1_key = "vd7omkkexkt7fvl6wm2jl9yan3g79y6i";

    public ServerResponse juhe1(String payType, String payAmt, HttpServletRequest request) {
        if (StringUtils.isBlank(payType) || StringUtils.isBlank(payAmt)) {
            return ServerResponse.createByErrorMsg("參數不能為空");
        }
        BigDecimal payAmtBig = new BigDecimal(payAmt);
        if ((new BigDecimal("0")).compareTo(payAmtBig) != -1) {
            return ServerResponse.createByErrorMsg("支付金額必須大於0");
        }

        User user = this.iUserService.getCurrentRefreshUser(request);
        if (user == null){
            return ServerResponse.createBySuccessMsg("請先登錄");
        }

        UserRecharge userRecharge = new UserRecharge();

        userRecharge.setUserId(user.getId());
        userRecharge.setNickName(user.getRealName());
        userRecharge.setAgentId(user.getAgentId());

        String ordersn = KeyUtils.getRechargeOrderSn();
        userRecharge.setOrderSn(ordersn);

        String saveChannel = "";
        if ("902".equals(payType)) {
            saveChannel = "微信-線上";
        } else {
            saveChannel = "支付寶-線上";
        }
        userRecharge.setPayChannel(saveChannel);
        userRecharge.setPayAmt(new BigDecimal(payAmt));
        userRecharge.setOrderStatus(Integer.valueOf(0));
        userRecharge.setAddTime(new Date());

        int insertCount = this.userRechargeMapper.insert(userRecharge);
        if (insertCount > 0) {
            log.info("線上支付，創建支付訂單成功！");
        } else {
            log.info("線上支付，創建支付訂單失敗！");
        }


        String pay_bankcode = "";
        if ("902".equals(payType)) {
            pay_bankcode = payType;
        } else if ("907".equals(payType)) {
            pay_bankcode = payType;
        } else {

            pay_bankcode = "903";
        }
        String AuthorizationURL = "http://pay.ksunpay.com:8080/Pay_Index.html";
        String merchantId = "10098";
        String keyValue = "vd7omkkexkt7fvl6wm2jl9yan3g79y6i";

        String pay_memberid = merchantId;
        String pay_orderid = ordersn;
        String pay_applydate = generateTime();


        String pay_notifyurl = PropertiesUtil.getProperty("juhe1.pay.notifyurl");

        String pay_callbackurl = PropertiesUtil.getProperty("juhe1.pay.callbackurl");

        String pay_amount = payAmt;
        String pay_attach = "";
        String pay_productname = "100元話費充值";
        String pay_productnum = "";
        String pay_productdesc = "";
        String pay_producturl = "";
        String stringSignTemp = "pay_amount=" + pay_amount + "&pay_applydate=" + pay_applydate + "&pay_bankcode=" + pay_bankcode + "&pay_callbackurl=" + pay_callbackurl + "&pay_memberid=" + pay_memberid + "&pay_notifyurl=" + pay_notifyurl + "&pay_orderid=" + pay_orderid + "&key=" + keyValue + "";


        String pay_md5sign = "";
        try {
            pay_md5sign = md5(stringSignTemp);
        } catch (Exception e) {
            log.error("加密md5出錯 e= {}", e);
        }

        GuoPayVO guoPayVO = new GuoPayVO();
        guoPayVO.setPostUrl(AuthorizationURL);
        guoPayVO.setPay_memberid(merchantId);
        guoPayVO.setPay_orderid(pay_orderid);
        guoPayVO.setPay_applydate(pay_applydate);
        guoPayVO.setPay_bankcode(pay_bankcode);
        guoPayVO.setPay_notifyurl(pay_notifyurl);
        guoPayVO.setPay_callbackurl(pay_callbackurl);
        guoPayVO.setPay_amount(pay_amount);
        guoPayVO.setPay_productname(pay_productname);
        guoPayVO.setPay_productnum(pay_productnum);
        guoPayVO.setPay_productdesc(pay_productdesc);
        guoPayVO.setPay_producturl(pay_producturl);
        guoPayVO.setPay_md5sign(pay_md5sign);

        return ServerResponse.createBySuccess(guoPayVO);
    }


    public ServerResponse juhe1Notify(HttpServletRequest request) {
        String memberid = request.getParameter("memberid");
        String orderid = request.getParameter("orderid");
        String amount = request.getParameter("amount");
        String datetime = request.getParameter("datetime");
        String returncode = request.getParameter("returncode");
        String transaction_id = request.getParameter("transaction_id");
        String attach = request.getParameter("attach");
        String sign = request.getParameter("sign");

        log.info("支付通知的 orderid = {}", orderid);
        log.info("支付通知的 transaction_id = {}", transaction_id);
        log.info("支付通知的 amount = {}", amount);
        log.info("支付通知的 returncode = {}", returncode);

        String keyValue = "vd7omkkexkt7fvl6wm2jl9yan3g79y6i";
        String SignTemp = "amount=" + amount + "&datetime=" + datetime + "&memberid=" + memberid + "&orderid=" + orderid + "&returncode=" + returncode + "&transaction_id=" + transaction_id + "&key=" + keyValue + "";


        log.info("signtemp : {}", SignTemp);


        String md5sign = "";
        try {
            md5sign = md5(SignTemp);
        } catch (Exception e) {
            log.info("線上支付後台通知 生成md5sign出錯，e = {}", e);
        }
        log.info("支付通知的sign = {}", sign);
        log.info("自己加密的sign = {}", md5sign);


        ServerResponse serverResponse = ServerResponse.createByError();
        if (sign.equals(md5sign)) {
            log.info("後台通知校驗簽名 通過");
            if (returncode.equals("00")) {

                log.info("後台通知成功，開始修改訂單 {}", orderid);
                serverResponse = doSuccess(orderid, amount);
            } else {
                log.error("後台通知 returncode 不為 00 ， 支付不成功");
            }
        } else {
            log.info("後台通知校驗簽名 不通過");
        }

        return serverResponse;
    }


    public ServerResponse flyPay(String payType, String payAmt, String currency, HttpServletRequest request) {
        User user = this.iUserService.getCurrentRefreshUser(request);
        if (user == null){
            return ServerResponse.createBySuccessMsg("請先登錄");
        }

        UserRecharge userRecharge = new UserRecharge();

        userRecharge.setUserId(user.getId());
        userRecharge.setNickName(user.getRealName());
        userRecharge.setAgentId(user.getAgentId());

        String ordersn = KeyUtils.getRechargeOrderSn();
        userRecharge.setOrderSn(ordersn);

        userRecharge.setPayChannel(payType);
        userRecharge.setPayAmt(new BigDecimal(payAmt));
        userRecharge.setOrderStatus(Integer.valueOf(0));
        userRecharge.setAddTime(new Date());

        int insertCount = this.userRechargeMapper.insert(userRecharge);
        if (insertCount > 0) {
            log.info("fly支付，創建支付訂單成功！");
        } else {
            log.info("fly支付，創建支付訂單失敗！");
        }


        FlyPayVO flyPayVO = new FlyPayVO();
        flyPayVO.setOrderno(ordersn);
        flyPayVO.setOrderamount(payAmt);
        flyPayVO.setPaytype(payType);
        flyPayVO.setOrdercurrency(currency);

        String sign = "";


        String tempStr = flyPayVO.getMerchantid() + ordersn + flyPayVO.getOrderamount() + flyPayVO.getServerbackurl() + flyPayVO.getCallbackurl() + PropertiesUtil.getProperty("fly.pay.token");


        sign = DigestUtils.md5Hex(tempStr);


        flyPayVO.setSign(sign);

        return ServerResponse.createBySuccess(flyPayVO);
    }


    public ServerResponse flyNotify(HttpServletRequest request) {
        String orderno = request.getParameter("orderno");
        String orderamount = request.getParameter("orderamount");
        String payamount = request.getParameter("payamount");
        String paytype = request.getParameter("paytype");
        String confirmpaytime = request.getParameter("confirmpaytime");
        String status = request.getParameter("status");
        String sign = request.getParameter("sign");

        log.info("支付通知的 orderno = {}", orderno);
        log.info("支付通知的 payamount = {}", payamount);
        log.info("支付通知的 status = {}", status);

        return doSuccess(orderno, payamount);
    }


    private ServerResponse doSuccess(String orderId, String amount) {
        UserRecharge userRecharge = this.userRechargeMapper.findUserRechargeByOrderSn(orderId);
        if (userRecharge == null) {
            return ServerResponse.createByErrorMsg("後台通知，查不到訂單");
        }

        User user = this.userMapper.selectByPrimaryKey(userRecharge.getUserId());
        if (user == null) {
            return ServerResponse.createByErrorMsg("後台通知，查不到用戶");
        }


        if (userRecharge.getPayAmt().compareTo(new BigDecimal(amount)) != 0) {
            return ServerResponse.createByErrorMsg("處理失敗，後台通知金額和訂單金額不一致");
        }


        userRecharge.setPayTime(new Date());
        userRecharge.setOrderStatus(Integer.valueOf(1));
        int updateCount = this.userRechargeMapper.updateByPrimaryKeySelective(userRecharge);
        if (updateCount > 0) {
            log.info("後台通知，修改訂單狀態成功");

            BigDecimal total_amt = user.getUserAmt().add(userRecharge.getPayAmt());
            user.setUserAmt(total_amt);
            BigDecimal total_enable = user.getEnableAmt().add(userRecharge.getPayAmt());
            user.setEnableAmt(total_enable);
            int updateUserCount = this.userMapper.updateByPrimaryKeySelective(user);
            if (updateUserCount > 0) {
                return ServerResponse.createBySuccessMsg("後台通知 處理成功");
            }
            return ServerResponse.createByErrorMsg("後台通知 處理失敗 修改用戶資金不成功");
        }

        log.error("後台通知，修改訂單狀態失敗");
        return ServerResponse.createByErrorMsg("後台通知修改訂單的狀態失敗");
    }


    public String generateOrderId() {
        String keyup_prefix = (new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date());
        String keyup_append = String.valueOf((new Random()).nextInt(899999) + 100000);
        return keyup_prefix + keyup_append;
    }


    public String generateTime() {
        return (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date());
    }


    public String md5(String str) throws NoSuchAlgorithmException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            byte[] byteDigest = md.digest();


            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < byteDigest.length; offset++) {
                int i = byteDigest[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }

            return buf.toString().toUpperCase();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static void main(String[] args) {
        BigDecimal a = new BigDecimal("0.011");
        BigDecimal b = new BigDecimal("0.010");
        System.out.println(a.compareTo(b));
    }


    @Override
    public ServerResponse juhenewpay(String paramString1, String paramString2, HttpServletRequest request) throws Exception {
        User user = iUserService.getCurrentRefreshUser(request);
        if (user == null){
            return ServerResponse.createBySuccessMsg("請先登錄");
        }
        String orderid = user.getId() + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String repson = CmcPayTool.submitOrder(paramString2, orderid, paramString1, request);
        JSONObject json = JSONObject.fromObject(repson);
        //JSONObject data = (JSONObject) json.get("data");
        return ServerResponse.createBySuccess(json);
    }

    @Override
    public ServerResponse juheh5pay(String paramString1, String paramString2, HttpServletRequest request) throws Exception {
        User user = iUserService.getCurrentRefreshUser(request);
        if (user == null){
            return ServerResponse.createBySuccessMsg("請先登錄");
        }
        String orderid = user.getId() + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String repson = CmcPayTool.submitOrderH5(paramString2, orderid, paramString1);
        JSONObject json = JSONObject.fromObject(repson);
        return ServerResponse.createBySuccess(json);
    }

    @Override
    public ServerResponse juhenewpayNotify(HttpServletRequest request) throws UnsupportedEncodingException {
        LinkedMap map = new LinkedMap();
        String out_trade_no = request.getParameter("out_trade_no");
        String[] arr = out_trade_no.split("_");
        map.put("amount", (String) request.getParameter("amount"));
        map.put("appid", (String) request.getParameter("appid"));
        map.put("currency_type", (String) request.getParameter("currency_type"));
        map.put("goods_name", (String) request.getParameter("goods_name"));
        map.put("out_trade_no", out_trade_no);
        map.put("pay_id", (String) request.getParameter("pay_id"));
        map.put("pay_no", (String) request.getParameter("pay_no"));
        map.put("payment", (String) request.getParameter("payment"));
        map.put("resp_code", (String) request.getParameter("resp_code"));
        map.put("resp_desc", URLDecoder.decode((String) request.getParameter("resp_desc"), "UTF-8"));
        map.put("sign_type", (String) request.getParameter("sign_type"));
        map.put("tran_amount", (String) request.getParameter("tran_amount"));
        map.put("version", (String) request.getParameter("version"));
        String _sign = CmcPayOuterRequestUtil.getSign(map, CmcPayConfig.KEY);
        if ("00".equals((String) request.getParameter("resp_code")) && _sign.equals((String) request.getParameter("sign"))) {
            iUserService.updateUserAmt((Double) map.get("amount"), new Integer(arr[0]));
//            response.getWriter().println("success");
        } else {
//            response.getWriter().println("error");
        }
        return null;
    }
}
