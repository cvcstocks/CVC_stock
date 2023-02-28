package com.stock.mx2.task;


import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.stock.mx2.dao.TradePlateItemHistoryMapper;
import com.stock.mx2.pojo.CoinThumb;
import com.stock.mx2.pojo.TradePlateItemHistory;
import com.stock.mx2.service.IUserService;

import com.stock.mx2.utils.DateTimeUtil;

import com.stock.mx2.utils.stock.BuyAndSellUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


@Component
public class ForceSellTask {

    private static final Logger log = LoggerFactory.getLogger(ForceSellTask.class);


    @Autowired
    IUserService iUserService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    TradePlateItemHistoryMapper tradePlateItemHistoryMapper;

    private static final String am_begin = "9:30";


    private static final String am_end = "11:30";


    private static final String pm_begin = "13:00";


    private static final String pm_end = "15:00";

//
//    /*用戶持倉單-用戶總金額-強平定時*/
//    @Scheduled(cron = "0 0/3 9-15 ? * MON-FRI")
//    public void banlanceUserPositionTaskV1() {
//
//        boolean am = false;
//
//        boolean pm = false;
//
//        try {
//
//            am = BuyAndSellUtils.isTransTime("9:30", "11:30");
//
//            pm = BuyAndSellUtils.isTransTime("13:00", "15:00");
//
//        } catch (Exception e) {
//
//            log.error("執行定時任務出錯，e = {}", e);
//
//        }
//
//
//        log.info("當前 am = {}  pm = {}", Boolean.valueOf(am), Boolean.valueOf(pm));
//
//        if (am || pm) {
//
//            log.info("=====掃描用戶持倉執行，當前時間 {} =====", DateTimeUtil.dateToStr(new Date()));
//
//            dotask();
//
//            log.info("=====掃描用戶持倉結束，當前時間 {} =====", DateTimeUtil.dateToStr(new Date()));
//
//        } else {
//
//            log.info("當前時間不為周一至周五，或者不在交易時間內，不執行（強平）定時任務");
//
//        }
//
//    }


//    public void dotask() {
//        this.iUserService.ForceSellTask();
//    }

    /*用戶持倉單-單支股票盈虧-強平定時*/
//    @Scheduled(cron = "0 0/1 * * * *")
    public void stockProfitLossOneTask() {
        boolean am = false;
        boolean pm = false;
        //todo 測試完成需要關閉註釋
//        try {
//            am = BuyAndSellUtils.isTransTime("9:30", "11:30");
//            pm = BuyAndSellUtils.isTransTime("13:00", "15:00");
//        } catch (Exception e) {
//            log.error("執行定時任務出錯，e = {}", e);
//        }

//        log.info("當前 am = {}  pm = {}", Boolean.valueOf(am), Boolean.valueOf(pm));
//        if (am || pm) {
        log.info("=====掃描單支股票盈虧執行，當前時間 {} =====", DateTimeUtil.dateToStr(new Date()));
        this.iUserService.ForceSellOneStockTask();
        log.info("=====掃描單支股票盈虧結束，當前時間 {} =====", DateTimeUtil.dateToStr(new Date()));

//        } else {
//            log.info("當前時間不為周一至周五，或者不在交易時間內，不執行（強平）單支股票盈虧定時任務");
//        }

    }

    /*用戶股票持倉單-強平提醒推送消息定時*/
//    @Scheduled(cron = "0 0/1 * * * *")
    public void banlanceUserPositionMessage() {

        boolean am = false;

        boolean pm = false;

        try {

            am = BuyAndSellUtils.isTransTime("9:30", "11:30");

            pm = BuyAndSellUtils.isTransTime("13:00", "15:00");

        } catch (Exception e) {

            log.error("執行定時任務出錯，e = {}", e);

        }


        log.info("當前 am = {}  pm = {}", Boolean.valueOf(am), Boolean.valueOf(pm));

        if (am || pm) {

            log.info("=====掃描用戶持倉執行，當前時間 {} =====", DateTimeUtil.dateToStr(new Date()));

            this.iUserService.ForceSellMessageTask();

            log.info("=====掃描用戶持倉結束，當前時間 {} =====", DateTimeUtil.dateToStr(new Date()));

        } else {

            log.info("當前時間不為周一至周五，或者不在交易時間內，不執行（強平）提醒推送消息任務");

        }

    }

//
//    /*用戶持倉單-單支股票盈虧-強平定時*/
//    @Scheduled(fixedRate = 3000)
//    public void stockProfitLossOneTaskFutures() {
//
//        log.info("=====掃描單支股票盈虧執行，當前時間 {} =====", DateTimeUtil.dateToStr(new Date()));
//        this.iUserService.ForceSellOneStockTaskFutures();
//        log.info("=====掃描單支股票盈虧結束，當前時間 {} =====", DateTimeUtil.dateToStr(new Date()));
//    }

    /*用戶持倉單-單支股票盈虧-強平定時*/
    @Scheduled(fixedRate = 1000)
    public void newThumb() {
        String json = HttpUtil.get("https://api.huobi.pro/market/detail?symbol=trxusdt");
        JSONObject jsonObject = JSONObject.parseObject(json);
        JSONObject tick = jsonObject.getJSONObject("tick");
        Map<String, Object> map = new HashMap<>();
        map.put("change", 0);
        map.put("chg", 0);
        map.put("close", tick.getBigDecimal("close").multiply(new BigDecimal("54")));
        map.put("high", tick.getBigDecimal("high").multiply(new BigDecimal("54")));
        map.put("lastDayClose", 0);
        map.put("low", tick.getBigDecimal("low").multiply(new BigDecimal("54")));
        map.put("open", tick.getBigDecimal("open").multiply(new BigDecimal("54")));
        map.put("turnover", tick.getBigDecimal("amount").multiply(new BigDecimal("54")));
        map.put("volume", tick.getBigDecimal("vol"));
        map.put("zone", 0);



        TradePlateItemHistory tradePlateItemHistory = new TradePlateItemHistory();
        tradePlateItemHistory.setType("thumb");
        tradePlateItemHistory.setJson(JSON.toJSONString(map));
        tradePlateItemHistory.setTime(jsonObject.getLong("ts"));
        tradePlateItemHistoryMapper.insert(tradePlateItemHistory);



        json = HttpUtil.get("https://api.huobi.pro/market/detail/merged?symbol=trxusdt");
        jsonObject = JSONObject.parseObject(json);
        tick = jsonObject.getJSONObject("tick");
        tradePlateItemHistory = new TradePlateItemHistory();
        tradePlateItemHistory.setType("merged");
        tradePlateItemHistory.setJson(JSON.toJSONString(tick));
        tradePlateItemHistory.setTime(jsonObject.getLong("ts"));
        tradePlateItemHistoryMapper.insert(tradePlateItemHistory);
    }



    /*用戶持倉單-單支股票盈虧-強平定時*/
    @Scheduled(fixedRate = 300)
    @PostConstruct
    public void getNowPrice() {
        Long yesterday =System.currentTimeMillis() - 24 * 60 * 60 * 1000;

        String coinThumbStr = tradePlateItemHistoryMapper.getTradePlateItemHistory(yesterday , "thumb");
        CoinThumb coinThumb = JSONObject.parseObject(coinThumbStr, CoinThumb.class);
         String  result = HttpUtil.get("https://togfmxkzwys8iwtk.srmd.ink/amlla291/index/gupiao");
        com.alibaba.fastjson2.JSONObject jsonObject = com.alibaba.fastjson2.JSONObject.parseObject(result);
        jsonObject = jsonObject.getJSONObject("qt");
        String p = jsonObject.getString("p");
        this.iUserService.ForceSellOneStockTaskFutures(new BigDecimal(p),coinThumb.getClose());
        redisTemplate.opsForValue().set("102_NG00Y", p);
        redisTemplate.opsForValue().set("109_NG_YZCF00X", coinThumb.getClose().toString());
    }

    public static void main(String[] args) {
        Long yesterday =System.currentTimeMillis() - 24 * 60 * 60 * 1000;

        System.out.println(yesterday);
    }

    /*用戶股票持倉單-強平提醒推送消息定時*/

    @Scheduled(fixedRate = 3000)
    public void banlanceUserPositionMessageFutures() {


        log.info("=====掃描用戶持倉執行，當前時間 {} =====", DateTimeUtil.dateToStr(new Date()));

        this.iUserService.ForceSellMessageTaskFutures();

        log.info("=====掃描用戶持倉結束，當前時間 {} =====", DateTimeUtil.dateToStr(new Date()));


    }

}
