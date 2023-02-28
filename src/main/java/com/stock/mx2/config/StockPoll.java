package com.stock.mx2.config;

import com.stock.mx2.dao.RealTimeMapper;
import com.stock.mx2.dao.StockFuturesMapper;
import com.stock.mx2.dao.StockIndexMapper;
import com.stock.mx2.dao.StockMapper;
import com.stock.mx2.pojo.Stock;
import com.stock.mx2.pojo.StockFutures;
import com.stock.mx2.pojo.StockIndex;
import com.stock.mx2.utils.stock.BuyAndSellUtils;
import com.stock.mx2.utils.stock.sina.SinaStockApi;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class StockPoll {
    @Resource
    StockMapper stockMapper;

    @Resource
    RealTimeMapper realTimeMapper;


    @Resource
    StockIndexMapper stockIndexMapper;

    private ThreadPoolExecutor pool;

    @PostConstruct
    public void initPool() {
        this.pool = new ThreadPoolExecutor(50, 70, 20L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(240));
        this.pool.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
    }

    public void test(String stockType, Integer stock_num, Integer stock_nums) {
        List<Stock> stockCodes = this.stockMapper.findStockCode(stockType, stock_num, stock_nums);
        //取出stockCodes 中所有的 stockCode 作為String List

        List<String> stockCodeList =new ArrayList<>();
        for (Stock stock : stockCodes) {
            stockCodeList.add(stock.getStockCode());
        }

        String sinaStock = SinaStockApi.getSinaStock(stockCodeList);



        for (Stock stock : stockCodes) {
            String stockGid = stock.getStockGid();
            String[] arrayOfString = sinaStock.split(",");
            StockTask stockTask = new StockTask();
            stockTask.splits((Object[])arrayOfString);
            stockTask.stockGid(stockGid);

            /*//處理均價
            String details = SinaStockApi.getSinaStock(stockGid);
            String averagePrice = details.split(",")[29];
            stockTask.averagePrice(Double.parseDouble(averagePrice));*/

            stockTask.averagePrice(Double.parseDouble("0"));
            stockTask.StockPoll(this);
            stockTask.RealTimeMapper(this.realTimeMapper);

            this.pool.submit(stockTask);
        }
    }

    //0-540
    public void z1() {
        test("sz", Integer.valueOf(0), Integer.valueOf(200));
    }
    public void z11() {
        test("sz", Integer.valueOf(200), Integer.valueOf(200));
    }
    public void z12() {
        test("sz", Integer.valueOf(400), Integer.valueOf(140));
    }

    //540-540
    public void z2() {
        test("sz", Integer.valueOf(540), Integer.valueOf(200));
    }
    public void z21() {
        test("sz", Integer.valueOf(740), Integer.valueOf(200));
    }
    public void z22() {
        test("sz", Integer.valueOf(940), Integer.valueOf(140));
    }

    //1080 - 540
    public void z3() {
        test("sz", Integer.valueOf(1080), Integer.valueOf(200));
    }
    public void z31() {
        test("sz", Integer.valueOf(1280), Integer.valueOf(200));
    }
    public void z32() {
        test("sz", Integer.valueOf(1480), Integer.valueOf(140));
    }

    //1620 - 539
    public void z4() {
        test("sz", Integer.valueOf(1620), Integer.valueOf(200));
    }
    public void z41() {
        test("sz", Integer.valueOf(1820), Integer.valueOf(200));
    }
    public void z42() {
        test("sz", Integer.valueOf(2020), Integer.valueOf(139));
    }

    //0-484
    public void h1() {
        test("sh", Integer.valueOf(0), Integer.valueOf(200));
    }
    public void h11() {
        test("sh", Integer.valueOf(200), Integer.valueOf(200));
    }
    public void h12() {
        test("sh", Integer.valueOf(400), Integer.valueOf(84));
    }

    //484-484
    public void h2() {
        test("sh", Integer.valueOf(484), Integer.valueOf(200));
    }
    public void h21() {
        test("sh", Integer.valueOf(684), Integer.valueOf(200));
    }
    public void h22() {
        test("sh", Integer.valueOf(884), Integer.valueOf(84));
    }

    //968-485
    public void h3() {
        test("sh", Integer.valueOf(968), Integer.valueOf(200));
    }
    public void h31() {
        test("sh", Integer.valueOf(1168), Integer.valueOf(200));
    }
    public void h32() {
        test("sh", Integer.valueOf(1368), Integer.valueOf(85));
    }

//    public void qh1() {
//        qhDataGrab("qh", Integer.valueOf(0), Integer.valueOf(540));
//    }

    /*期貨k線分時數據抓取*/
//    public void qhDataGrab(String stockType, Integer stock_num, Integer stock_nums) {
//        List<StockFutures> stockCodes = this.stockFuturesMapper.queryList();
//        System.out.println("qh-stockCodes" + stockCodes.size() + "--stockCodes");
//        boolean am = false;
//        boolean pm = false;
//        boolean pm2 = false;
//        try {
//            am = BuyAndSellUtils.isTransTime("9:15", "12:00");
//            pm = BuyAndSellUtils.isTransTime("13:00", "16:30");
//            pm2 = BuyAndSellUtils.isTransTime("17:15", "23:45");
//        } catch (Exception e) {
//        }
//        for (StockFutures stock : stockCodes) {
//            String stockGid = stock.getFuturesGid();
//            //恒生指數特殊處理，不在指定時間段跳過
//            if("hf_HSI".equals(stockGid) && !am && !pm && !pm2){
//                continue;
//            }
//            String sinaStock = SinaStockApi.getSinaStock(stockGid);
//            if(sinaStock.length()>10) {
//                String[] arrayOfString = sinaStock.split(",");
//                //倫敦金格式不正確，特殊處理
//                if (arrayOfString.length <= 14) {
//                    sinaStock = sinaStock.replace("\"", ",1\"");
//                    arrayOfString = sinaStock.split(",");
//                }
//                //拼接需要的字符串：1價格+3漲跌幅度+4成交量+5成交額
//                double rates = 0;
////                BigDecimal b1 = new BigDecimal(arrayOfString[3].toString());
//                BigDecimal b2 = new BigDecimal(arrayOfString[4].toString());
//                BigDecimal b3 = b1.subtract(b2);
//                String s = arrayOfString[14].toString();
//                int index = s.indexOf("\"");
//                String substring = s.substring(0, index);
//                rates = b3.multiply(new BigDecimal("100")).divide(b1, 2, BigDecimal.ROUND_HALF_UP).doubleValue();
//                String qhstr = "0," + arrayOfString[0].toString() + ",0," + rates + "," + substring + "," + arrayOfString[9].split("\\.")[0].toString() + "\"";
//                String[] arrayqh = qhstr.split(",");
//                StockTask stockTask = new StockTask();
//                stockTask.splits((Object[]) arrayqh);
//                stockTask.stockGid(stockGid);
//                stockTask.averagePrice(new Double("0"));
//                stockTask.StockPoll(this);
//                stockTask.RealTimeMapper(this.realTimeMapper);
//                this.pool.submit(stockTask);
//            }
//        }
//    }

    public void zs1() {
        zsDataGrab();
    }

    /*指數k線分時數據抓取*/
    public void zsDataGrab() {
        if (true){
            return;
        }
        List<StockIndex> stockCodes = this.stockIndexMapper.queryListIndex();
        System.out.println("zs-stockCodes" + stockCodes.size() + "--stockCodes");
        for (StockIndex stock : stockCodes) {
            String stockGid = stock.getIndexGid();
            ArrayList<String> objects = new ArrayList<>();
            String sinaStock = "";
            String[] arrayOfString = sinaStock.split(",");
            StockTask stockTask = new StockTask();
            stockTask.splits((Object[])arrayOfString);
            stockTask.stockGid(stockGid);
            stockTask.StockPoll(this);
            stockTask.RealTimeMapper(this.realTimeMapper);
            this.pool.submit(stockTask);
        }
    }
}
