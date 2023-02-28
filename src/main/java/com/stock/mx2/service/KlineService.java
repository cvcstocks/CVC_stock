package com.stock.mx2.service;


import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.stock.mx2.dao.KLineMapper;
import com.stock.mx2.dao.TradePlateItemHistoryMapper;
import com.stock.mx2.pojo.KLine;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class KlineService {
    private org.slf4j.Logger logger = LoggerFactory.getLogger(KlineService.class);

    @Autowired
    private KLineMapper kLineDao;

    @Autowired
    private TradePlateItemHistoryMapper tradePlateItemHistoryDao;

    private double VOLUME_PERCENT = 0.13; // 火币成交量的百分比
    public static String PERIOD[] = {"1min", "5min", "15min", "30min", "60min", "4hour", "1day", "1mon", "1week"};


    // 同步K线
    public void reqKLineList(String symbol, String period, long from) {

//        String topic = String.format(KLINE, symbol.replace("/", "").toLowerCase(), period);

        // Huobi Websocket要求单次请求数据不能超过300条，因此需要在次对请求进行拆分
        long timeGap = System.currentTimeMillis() / 1000 - from; // 时间差
        long divideTime = 0;
        if (period.equals("1min")) divideTime = 60;//* 300; // 1分钟 * 300条
        if (period.equals("5min")) divideTime = 5 * 60;//* 300;
        if (period.equals("15min")) divideTime = 15 * 60;//* 300;
        if (period.equals("30min")) divideTime = 30 * 60;//* 300;
        if (period.equals("60min")) divideTime = 60 * 60;//* 300;
        if (period.equals("4hour")) divideTime = 4 * 60 * 60;//* 300;
        if (period.equals("1day")) divideTime = 24 * 60 * 60;//* 300;
        if (period.equals("1week")) divideTime = 7 * 24 * 60 * 60;//* 300;
        if (period.equals("1mon")) divideTime = 30 * 24 * 60 * 60;//* 300;
        if (timeGap == 0) {
            return;
        }
        int limit = (int) (timeGap / divideTime); // 分段数
        if (limit == 0) {
            return;
        }
        if (limit>=1999){
            limit = 1999;
        }

           String response = HttpUtil.get("https://api.huobi.pro/market/history/kline?period=" + period + "&size=" + limit + "&symbol=" + symbol.replace("/", "").toLowerCase());

        JSONObject jsonObject = JSONObject.parseObject(response);
//        Poke poke = marketService.findPoke(symbol, "kline");
        BigDecimal price =null;
        JSONArray klineList = jsonObject.getJSONArray("data");

        for (int y = 0; y < klineList.size(); y++) {
            JSONObject klineObj = klineList.getJSONObject(y);
            BigDecimal open = (price == null || y == 0) ? klineObj.getBigDecimal("open") : price; // 收盘价
            BigDecimal close = (price == null || y == klineList.size() - 1) ? klineObj.getBigDecimal("close") : price; // 收盘价
            BigDecimal high = price == null ? klineObj.getBigDecimal("high") : price; // 收盘价
            BigDecimal low = price == null ? klineObj.getBigDecimal("low") : price; // 收盘价
            BigDecimal amount = klineObj.getBigDecimal("amount"); // 收盘价
            BigDecimal vol = klineObj.getBigDecimal("vol"); // 收盘价
            int count = klineObj.getIntValue("count"); // 收盘价
            long time = klineObj.getLongValue("id");
            KLine kline = new KLine(period);
            kline.setClosePrice(close);
            kline.setCount(count);
            kline.setSymbol("TRX/USDT");
            kline.setHighestPrice(high);
            kline.setLowestPrice(low);
            kline.setOpenPrice(open);
                //所有价格*10
                kline.setClosePrice(kline.getClosePrice().multiply(new BigDecimal("54")));
                kline.setHighestPrice(kline.getHighestPrice().multiply(new BigDecimal("54")));
                kline.setLowestPrice(kline.getLowestPrice().multiply(new BigDecimal("54")));
                kline.setOpenPrice(kline.getOpenPrice().multiply(new BigDecimal("54")));
                kline.setVolume(kline.getVolume().multiply(new BigDecimal("54")));
                kline.setTurnover(kline.getTurnover().multiply(new BigDecimal("54")));
            kline.setTime(time);
            kline.setTurnover(amount.multiply(BigDecimal.valueOf(VOLUME_PERCENT)));
            kline.setVolume(vol.multiply(BigDecimal.valueOf(VOLUME_PERCENT)));
            //获取当前分钟时间戳
            long currentTime = System.currentTimeMillis() / 1000 / divideTime;
            currentTime = currentTime * divideTime - 5;
            if (time < currentTime) {
              kLineDao.insert(kline);
            }

        }


    }


    public void deleteData() {
        tradePlateItemHistoryDao.deleteHistory(System.currentTimeMillis() - (60 * 60 * 24 * 2 * 1000));
    }

    public Long findMaxTimestamp(String s, String period) {
    return     this.kLineDao.findMaxTimestamp(s, period);

    }

    public KLine getKline(String period,Long yesterday) {

        return this.kLineDao.getKline(period,yesterday);
    }

    public     List<KLine> getKlineList(String period, String start, String end,Long yesterday) {
        return this.kLineDao.getKlineList(period,start,end,yesterday);

    }
}
