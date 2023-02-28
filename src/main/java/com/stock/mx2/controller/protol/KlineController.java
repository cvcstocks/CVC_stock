package com.stock.mx2.controller.protol;

import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.KLine;
import com.stock.mx2.service.KlineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/kline")
public class KlineController
{
    @Autowired
    private KlineService klineService;



    @RequestMapping("/getKline")
    public ServerResponse getKline(String period)
    {

        //获取昨天的时间戳
        long yesterday = System.currentTimeMillis() - 24 * 60 * 60 * 1000;
        yesterday = yesterday / 1000;
        KLine kline = this.klineService.getKline(period,yesterday);
        kline.setTime(kline.getTime() + 24 * 60 * 60);

        String [] data =  new String[]{String.valueOf(kline.getOpenPrice()), String.valueOf(kline.getClosePrice()), String.valueOf(kline.getLowestPrice()), String.valueOf(kline.getHighestPrice()), String.valueOf(kline.getVolume()), String.valueOf(kline.getTime())};
        return ServerResponse.createBySuccess(data);
    }
    @RequestMapping("/getKlineList")
    public ServerResponse getKlineList(String period,String start,String end)
    {
        long yesterday = System.currentTimeMillis() - 24 * 60 * 60 * 1000;
        yesterday = yesterday / 1000;
        start = Long.parseLong(start)  - 24 * 60 * 60+"";
        end = Long.parseLong(end)  - 24 * 60 * 60+"";

        List<KLine> klineList = this.klineService.getKlineList(period, start, end, yesterday);


        List<String[]> dataList = new ArrayList<>();

        for (KLine kLine : klineList) {
            kLine.setTime(kLine.getTime() + 24 * 60 * 60);
            String [] data =  new String[]{String.valueOf(kLine.getOpenPrice()), String.valueOf(kLine.getClosePrice()), String.valueOf(kLine.getLowestPrice()), String.valueOf(kLine.getHighestPrice()), String.valueOf(kLine.getVolume()), String.valueOf(kLine.getTime())};
            dataList.add(data);
        }

        return ServerResponse.createBySuccess(dataList);
    }



}
