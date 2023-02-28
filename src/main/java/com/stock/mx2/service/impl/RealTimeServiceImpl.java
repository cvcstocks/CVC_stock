package com.stock.mx2.service.impl;

import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.dao.RealTimeMapper;
import com.stock.mx2.pojo.RealTime;
import com.stock.mx2.service.RealTimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RealTimeServiceImpl implements RealTimeService {

    @Autowired
    RealTimeMapper realTimeMapper;

    public ServerResponse deleteRealTime() {
        this.realTimeMapper.deleteStockCode();
        return ServerResponse.createBySuccessMsg("刪除成功");
    }

    public ServerResponse deleteFuturesRealTime() {
        this.realTimeMapper.deleteStockFuturesCode();
        return ServerResponse.createBySuccessMsg("刪除成功");
    }

    public ServerResponse findStock(String stockCode) {
        List<RealTime> stock = new ArrayList<>();
        if (stockCode.startsWith("6", 0)) {
            stock = this.realTimeMapper.findStock("sh" + stockCode);
        } else if (stockCode.startsWith("3", 0) || stockCode.startsWith("0", 0) || stockCode.startsWith("2", 0)) {
            stock = this.realTimeMapper.findStock("sz" + stockCode);
        } else {
            stock = this.realTimeMapper.findStock(stockCode);
        }
        Map<String, Object> map = new HashMap<>();
        List<Double> price = new ArrayList();
        List<Double> averagePrice = new ArrayList();
        List<Double> rates = new ArrayList();
        List<String> time = new ArrayList();
        List<Integer> volumes = new ArrayList();
        List<Integer> amounts = new ArrayList();
        map.put("stockCode", stockCode);
        map.put("size", Integer.valueOf(stock.size()));
        for (RealTime realTime : stock) {
            double p = realTime.getPrice();
            price.add(Double.valueOf(p));
            /*double avg = realTime.getAveragePrice();
            averagePrice.add(Double.valueOf(avg));*/
            double r = realTime.getRates();
            rates.add(Double.valueOf(r));
            String t = realTime.getTime();
            time.add(t);
            int v = realTime.getVolumes();
            volumes.add(Integer.valueOf(v));
            int a = realTime.getAmounts();
            amounts.add(Integer.valueOf(a));
        }
        map.put("time", time);
        map.put("volumes", volumes);
        map.put("price", price);
        map.put("averagePrice", averagePrice);
        map.put("rates", rates);
        map.put("amounts", amounts);
        return ServerResponse.createBySuccess(map);
    }

}
