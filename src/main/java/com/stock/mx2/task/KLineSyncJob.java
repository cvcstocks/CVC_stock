package com.stock.mx2.task;


import com.stock.mx2.service.KlineService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class KLineSyncJob {

    private org.slf4j.Logger logger = LoggerFactory.getLogger(KLineSyncJob.class);



    @Autowired
    private KlineService klineService;

    public static String PERIOD[] ={ "1min", "5min", "15min", "30min", "60min","4hour", "1day", "1mon", "1week" };

    private List<CoinSyncItem> coinList = new ArrayList<CoinSyncItem>(); // 交易对列表

    /**
     * 1分钟执行一次：检查
     */
    @Scheduled(fixedRate = 3000)
    public void syncKLine() {
        // 币种列表大小为0，或引擎中币种数量与当前列表数量不一致
        logger.info("分钟执行获取K线[Start]");
        Long currentTime = System.currentTimeMillis() / 1000;

        for (String period : PERIOD) {
            Long fromTime = klineService.findMaxTimestamp("TRX/USDT", period);
            if (fromTime == null) {
                fromTime = 0L;
            }
            long timeGap = currentTime - fromTime;
            if (period.equals("1min")) { // 超出1分钟
                if (fromTime == 0) {
                    //logger.info("分钟执行获取K线[1min] ===> from == 0");
                    // 初始化K线，获取最近600根K线
                    klineService.reqKLineList("TRX/USDT", period, currentTime - 60 * 600);
                } else {
                    //logger.info("分钟执行获取K线[1min] ===> from != 0, timeGap: {}", timeGap);
                    // 非初始化，获取最近产生的K线
                    long toTime = fromTime + (timeGap / 60) * 60 - 5;//timeGap - (timeGap % 60); // +10秒是为了获取本区间内的K线
                    klineService.reqKLineList("TRX/USDT", period, fromTime);
                }
            }

            if (period.equals("5min") && timeGap >= 60 * 5) { // 超出5分钟
                if (fromTime == 0) {
                    // 初始化K线，获取最近600根K线

                    klineService.reqKLineList("TRX/USDT", period, currentTime - 5 * 60 * 600);
                } else {
                    // 非初始化，获取最近产生的K线
                    long toTime = fromTime + (timeGap / 300) * 300 - 5;//timeGap - (timeGap % (5 * 60));
                    //logger.info("获取5分钟K线, From: {}, To: {}", fromTime, toTime);

                    klineService.reqKLineList("TRX/USDT", period, fromTime);
                }
            }

            if (period.equals("15min") && timeGap >= (60 * 15)) { // 超出15分钟
                if (fromTime == 0) {
                    // 初始化K线，获取最近600根K线

                    klineService.reqKLineList("TRX/USDT", period, currentTime - 15 * 60 * 600);
                } else {
                    // 非初始化，获取最近产生的K线
                    long toTime = fromTime + (timeGap / (60 * 15)) * 60 * 15 - 5;//timeGap - (timeGap % (15 * 60));

                    klineService.reqKLineList("TRX/USDT", period, fromTime);
                }
            }

            if (period.equals("30min") && timeGap >= (60 * 30)) { // 超出30分钟
                if (fromTime == 0) {
                    // 初始化K线，获取最近600根K线

                    klineService.reqKLineList("TRX/USDT", period, currentTime - 30 * 60 * 600);
                } else {
                    // 非初始化，获取最近产生的K线
                    long toTime = fromTime + (timeGap / (60 * 30)) * 60 * 30 - 5;//timeGap - (timeGap % (30 * 60));

                    klineService.reqKLineList("TRX/USDT", period, fromTime);
                }
            }

            if (period.equals("60min") && timeGap >= (60 * 60)) { // 超出60分钟
                if (fromTime == 0) {
                    // 初始化K线，获取最近600根K线

                    klineService.reqKLineList("TRX/USDT", period, currentTime - 60 * 60 * 600);
                } else {
                    // 非初始化，获取最近产生的K线
                    long toTime = fromTime + (timeGap / (60 * 60)) * 60 * 60 - 5;//timeGap - (timeGap % (60 * 60));

                    klineService.reqKLineList("TRX/USDT", period, fromTime);
                }
            }

            if (period.equals("4hour") && timeGap >= (60 * 60 * 4)) { // 超出4小时
                if (fromTime == 0) {
                    // 初始化K线，获取最近600根K线

                    klineService.reqKLineList("TRX/USDT", period, currentTime - 4 * 60 * 60 * 600);
                } else {
                    // 非初始化，获取最近产生的K线
                    long toTime = fromTime + (timeGap / (60 * 60 * 4)) * 60 * 60 * 4 - 5;//timeGap - (timeGap % (4 * 60 * 60));

                    klineService.reqKLineList("TRX/USDT", period, fromTime);
                }
            }

            if (period.equals("1day") && timeGap >= (60 * 60 * 24)) { // 超出24小时
                if (fromTime == 0) {
                    // 初始化K线，获取最近600根K线

                    klineService.reqKLineList("TRX/USDT", period, currentTime - 24 * 60 * 60 * 600);
                } else {
                    // 非初始化，获取最近产生的K线
                    long toTime = fromTime + (timeGap / (60 * 60 * 24)) * 60 * 60 * 24 - 5;//timeGap - (timeGap % (24 * 60 * 60));

                    klineService.reqKLineList("TRX/USDT", period, fromTime);
                }
            }

            if (period.equals("1week") && timeGap >= (60 * 60 * 24 * 7)) { // 超出24小时
                if (fromTime == 0) {
                    // 初始化K线，获取最近600根K线

                    klineService.reqKLineList("TRX/USDT", period, currentTime - 7 * 24 * 60 * 60 * 600);
                } else {
                    // 非初始化，获取最近产生的K线
                    long toTime = fromTime + (timeGap / (60 * 60 * 4 * 7)) * 60 * 60 * 4 * 7 - 5;//timeGap - (timeGap % (7 * 24 * 60 * 60));

                    klineService.reqKLineList("TRX/USDT", period, fromTime);
                }
            }

            if (period.equals("1mon") && timeGap >= (60 * 60 * 24 * 30)) { // 超出24小时
                if (fromTime == 0) {
                    // 初始化K线，获取最近600根K线
                    klineService.reqKLineList("TRX/USDT", period, currentTime - 30 * 24 * 60 * 60 * 600);
                } else {
                    // 非初始化，获取最近产生的K线
                    long toTime = fromTime + (timeGap / (60 * 60 * 4 * 30)) * 60 * 60 * 4 * 30 - 5;//timeGap - (timeGap % (30 * 24 * 60 * 60));
                    klineService.reqKLineList("TRX/USDT", period, fromTime);
                }
            }
        }
    }

    //每天删除两天前数据
    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteData() {
        logger.info("删除两天前数据");
        klineService.deleteData();
    }



    class CoinSyncItem{
        String symbol;
        Map<String, Long> lastUpdateTime;

        CoinSyncItem(String[] period){
            lastUpdateTime = new HashMap<String, Long>();
            for(String p : period) {
                lastUpdateTime.put(p, -1L);
            }
        }

        public String getSymbol() {
            return symbol;
        }
        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public void setLastPeriodTime(String period, Long time) {
            this.lastUpdateTime.put(period, time);
        }

        public Long getLastPeriodTime(String period) {
            return this.lastUpdateTime.get(period);
        }

    }
}
