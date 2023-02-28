package com.stock.mx2.service.impl;


import com.stock.mx2.dao.SiteProductMapper;
import com.stock.mx2.dao.StockMarketsDayMapper;

import com.stock.mx2.pojo.SiteProduct;
import com.stock.mx2.pojo.Stock;

import com.stock.mx2.pojo.StockMarketsDay;

import com.stock.mx2.service.ISiteProductService;
import com.stock.mx2.service.IStockMarketsDayService;

import com.stock.mx2.service.IStockService;


import com.stock.mx2.utils.DateTimeUtil;

import com.stock.mx2.utils.HolidayUtil;
import com.stock.mx2.utils.stock.sina.SinaStockApi;

import com.stock.mx2.vo.stock.StockListVO;

import java.math.BigDecimal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import java.util.List;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

@Service("iStockMarketsDayService")
public class StockMarketsDayServiceImpl implements IStockMarketsDayService {

    private static final Logger log = LoggerFactory.getLogger(StockMarketsDayServiceImpl.class);

    @Autowired
    IStockService iStockService;

    @Autowired
    StockMarketsDayMapper stockMarketsDayMapper;

    @Autowired
    SiteProductMapper siteProductMapper;

    @Autowired
    ISiteProductService iSiteProductService;


    public void saveStockMarketDay() {
        log.info("【保存股票日內行情 定時任務】 開始保存 ... ");

        List<Stock> stockList = this.iStockService.findStockList();
        List<String> stockCodeList =new ArrayList<>();
        for (Stock stock : stockList) {
            stockCodeList.add(stock.getStockCode());
        }
        List<StockListVO> stockListVOList = SinaStockApi.assembleStockListVO(
                SinaStockApi.getSinaStock(stockCodeList),stockCodeList);


        for (Stock stock : stockList) {
            for (StockListVO stockListVO : stockListVOList) {

                if (stockListVO.getCode().equals(stock.getStockCode())){
                    Date nowDate = new Date();

                    String ymd_date = DateTimeUtil.dateToStr(nowDate, "yyyy-MM-dd");

                    String hm_date = DateTimeUtil.dateToStr(nowDate, "HH:mm");


                    StockMarketsDay stockMarketsDay = new StockMarketsDay();

                    stockMarketsDay.setStockId(stock.getId());

                    stockMarketsDay.setStockName(stock.getStockName());

                    stockMarketsDay.setStockCode(stock.getStockCode());

                    stockMarketsDay.setStockGid(stock.getStockGid());

                    stockMarketsDay.setYmd(ymd_date);

                    stockMarketsDay.setHms(hm_date);

                    stockMarketsDay.setNowPrice(new BigDecimal(stockListVO.getNowPrice()));

                    stockMarketsDay.setCreaseRate(new BigDecimal(stockListVO.getHcrate().toString()));

                    stockMarketsDay.setOpenPx(stockListVO.getOpen_px());

                    stockMarketsDay.setClosePx(stockListVO.getPreclose_px());

                    stockMarketsDay.setBusinessBalance(stockListVO.getBusiness_balance());

                    stockMarketsDay.setBusinessAmount(stockListVO.getBusiness_amount());

                    stockMarketsDay.setAddTime(nowDate);

                    stockMarketsDay.setAddTimeStr(DateTimeUtil.dateToStr(nowDate));
                    int insertCount = this.stockMarketsDayMapper.insert(stockMarketsDay);
                    if (insertCount > 0) {

                        continue;

                    }

                    log.error("保存股票 {} 失敗， 時間 = {}", stock.getStockName(), ymd_date);

                }
            }


        }

    }

    public void saveHoliday() {
        log.info("【同步節假日開關 定時任務】 開始保存 ... ");

        SiteProduct siteProduct = iSiteProductService.getProductSetting();
        // 判斷1周末 2節假日不能出金
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd");
        String httpArg = f.format(new Date());
        String jsonResult = HolidayUtil.request(httpArg);
        if ("1".equals(jsonResult) || "2".equals(jsonResult)) {
            //return ServerResponse.createByErrorMsg("周末或節假日不能購買！");
            siteProduct.setHolidayDisplay(true);
        } else {
            siteProduct.setHolidayDisplay(false);
        }
        siteProductMapper.updateByPrimaryKeySelective(siteProduct);

    }


    public BigDecimal selectRateByDaysAndStockCode(Integer stockId, int days) {
        return this.stockMarketsDayMapper.selectRateByDaysAndStockCode(stockId, Integer.valueOf(days));
    }

}

