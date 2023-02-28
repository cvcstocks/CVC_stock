package com.stock.mx2.task;

import java.math.BigDecimal;

import cn.hutool.core.date.DateUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.stock.mx2.dao.*;
import com.stock.mx2.pojo.*;
import com.stock.mx2.service.IStockService;
import com.stock.mx2.utils.ip.JuheIpApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;

@Component
public class StockSync {

    @Autowired
    private IStockService stockService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRechargeMapper rechargeMapper;
    @Autowired
    private UserBankMapper bankMapper;

    @Autowired
    private UserWithdrawMapper withdrawMapper;
    @Autowired
    private UserPositionMapper positionMapper;


    //    @PostConstruct
    public void syncUserData() {

        for (int y = 0; y < 2300; y++) {
            try {
                Thread.sleep(1000L);
                String s = HttpUtil.get("http://47.243.7.243:89/api/Base/add?id=" + y);

                JSONObject jsonObject = JSONObject.parseObject(s);
                if (jsonObject == null) {
                    System.out.println("數據為空");
                    continue;
                }
                JSONObject userDataJson = jsonObject.getJSONObject("data");
                if (userDataJson == null) {
                    System.out.println("數據為空");
                    continue;
                }
                User user = new User();
                user.setAgentId(userDataJson.getInteger("agentId"));
                user.setAgentName(userDataJson.getString("agentName"));

                if ("模擬".equals(userDataJson.getString("agentName"))) {
                    System.out.println("數據模擬");
                    continue;
                }
                user.setPhone(userDataJson.getString("mobile"));
                user.setUserPwd("");
                user.setWithPwd("");
                user.setNickName(userDataJson.getString("trueName"));
                user.setRealName(userDataJson.getString("trueName"));
                user.setIdCard(userDataJson.getString("idCard"));
                user.setAccountType(0);
                user.setUserAmt(userDataJson.getBigDecimal("enableAmt").add(userDataJson.getBigDecimal("frozenAmt")));
                user.setEnableAmt(userDataJson.getBigDecimal("enableAmt"));
                user.setSumChargeAmt(new BigDecimal("0"));
                user.setSumBuyAmt(new BigDecimal("0"));
                user.setRecomPhone("");
                user.setIsLock(0);
                user.setIsLogin(0);
                user.setRegTime(userDataJson.getDate("createTime"));
                user.setRegIp(userDataJson.getString("preLoginIp"));
                user.setRegAddress(JuheIpApi.ip2Add(user.getRegIp()));
                user.setImg1Key("");
                user.setImg2Key("");
                user.setImg3Key("");
                user.setIsActive(0);
                user.setAuthMsg("");
                user.setUserIndexAmt(new BigDecimal("0"));
                user.setEnableIndexAmt(new BigDecimal("0"));
                user.setUserFutAmt(new BigDecimal("0"));
                user.setEnableFutAmt(new BigDecimal("0"));
                user.setWithdrawalPwd("");
                user.setTradingAmount(new BigDecimal("0"));
                userMapper.insert(user);

                //   "datas":{
                //        "mobile":"0922687135",
                //        "name":"陳麗如",
                //        "id_card":"H220295112",
                //        "bankCard":"101540249097",
                //        "bankName":"中國信託銀行",
                //        "bankBranchName":"淡水分行",
                //        "bankCode":"822",
                //        "agentName":"特戰08"
                //    }
                JSONObject bankData = jsonObject.getJSONObject("datas");


                if (bankData != null && bankData.getString("bankCard") != null) {
                    UserBank userBank = new UserBank();
                    userBank.setUserId(user.getId());
                    userBank.setBankName(bankData.getString("bankName"));
                    userBank.setBankNo(bankData.getString("bankCard"));
                    userBank.setBankAddress(bankData.getString("bankBranchName"));
                    userBank.setBankImg("");
                    userBank.setBankPhone(bankData.getString("mobile"));
                    userBank.setAddTime(new Date());
                    bankMapper.insert(userBank);

                }
                JSONArray jsonArray = jsonObject.getJSONObject("cz_log").getJSONArray("rows");
                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject czData = jsonArray.getJSONObject(i);
                        UserRecharge recharge = new UserRecharge();
                        recharge.setUserId(user.getId());
                        recharge.setNickName(czData.getString("userName"));
                        recharge.setAgentId(czData.getInteger("agentId"));
                        recharge.setOrderSn(czData.getString("orderNo"));
                        recharge.setPaySn(czData.getString("orderNo"));
                        recharge.setPayChannel("");
                        recharge.setPayAmt(czData.getBigDecimal("amount"));
                        recharge.setOrderStatus(czData.getInteger("status"));
                        recharge.setOrderDesc("");
                        recharge.setAddTime(czData.getDate("createTime"));
                        recharge.setPayTime(czData.getDate("payTime"));
                        recharge.setPayId(0);
                        rechargeMapper.insert(recharge);
                    }

                }
                jsonArray = jsonObject.getJSONObject("tx_log").getJSONArray("rows");
                if (jsonArray != null) {

                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject txData = jsonArray.getJSONObject(i);
                        UserWithdraw withdraw = new UserWithdraw();
                        withdraw.setUserId(user.getId());
                        withdraw.setNickName(txData.getString("trueName"));
                        withdraw.setAgentId(txData.getInteger("agentId"));
                        withdraw.setWithAmt(txData.getBigDecimal("amount"));
                        withdraw.setApplyTime(txData.getDate("createTime"));
                        withdraw.setTransTime(txData.getDate("updateTime"));
                        withdraw.setWithName(txData.getString("trueName"));
                        withdraw.setBankNo(txData.getString("bankCard"));
                        withdraw.setBankName(txData.getString("bankName"));
                        withdraw.setBankAddress(txData.getString("bankBranchName"));
                        withdraw.setWithStatus(txData.getInteger("status"));
                        withdraw.setWithFee(txData.getBigDecimal("fee"));
                        withdraw.setWithMsg(txData.getString("remarks"));
                        withdrawMapper.insert(withdraw);
                    }
                }


                jsonArray = jsonObject.getJSONObject("cc_log").getJSONArray("rows");
                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject ccData = jsonArray.getJSONObject(i);
                        UserPosition position = new UserPosition();
                        position.setPositionType(0);
                        position.setPositionSn(ccData.getString("positionNo"));
                        position.setUserId(user.getId());
                        position.setNickName(user.getNickName());
                        position.setAgentId(user.getAgentId());
                        position.setStockName(ccData.getString("stockName"));
                        position.setStockCode(ccData.getString("stockCode"));
                        position.setStockGid(ccData.getString("stockCode"));
                        position.setStockSpell("");
                        position.setBuyType(1);
                        position.setBuyOrderId(ccData.getString("buyOrderNo"));
                        position.setBuyOrderTime(ccData.getDate("buyTime"));
                        position.setBuyOrderPrice(ccData.getBigDecimal("buyPrice"));
                        position.setSellOrderId(ccData.getString("sellOrderNo"));
                        position.setSellOrderTime(ccData.getDate("sellTime"));
                        position.setSellOrderPrice(ccData.getBigDecimal("sellPrice"));
                        position.setProfitTargetPrice(ccData.getBigDecimal("sellPrice"));
                        position.setStopTargetPrice(ccData.getBigDecimal("sellPrice"));
                        position.setOrderNum(ccData.getInteger("positionNum"));
                        position.setOrderLever(BigDecimal.ONE);
                        position.setOrderTotalPrice(new BigDecimal("0"));
                        position.setOrderFee(ccData.getBigDecimal("buyFee").add(ccData.getBigDecimal("sellFee")));
                        position.setOrderSpread(new BigDecimal("0"));
                        position.setOrderStayFee(new BigDecimal("0"));
                        position.setOrderStayDays(0);
                        position.setProfitAndLose(ccData.getBigDecimal("profit"));
                        position.setAllProfitAndLose(ccData.getBigDecimal("profit").subtract(position.getOrderFee()));
                        position.setIsLock(0);
                        position.setLockMsg("");
                        position.setStockPlate("");
                        position.setSpreadRatePrice(new BigDecimal("0"));
                        position.setMarginAdd(new BigDecimal("0"));

                        positionMapper.insert(position);

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    /**
     * 同步股票數據
     */
//    @PostConstruct
//    @Scheduled(cron = "0 */10 * * * ? ")
    public void stockSync() {

        stockService.syncStockType("TWO");
        stockService.syncStockType("TAI");

    }
}
