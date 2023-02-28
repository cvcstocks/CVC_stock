package com.stock.mx2.service.impl;


import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;

import com.github.pagehelper.PageInfo;

import com.google.common.collect.Lists;

import com.stock.mx2.common.ServerResponse;

import com.stock.mx2.dao.KLineMapper;
import com.stock.mx2.dao.StockFuturesMapper;

import com.stock.mx2.dao.TradePlateItemHistoryMapper;
import com.stock.mx2.pojo.*;

import com.stock.mx2.service.IStockCoinService;

import com.stock.mx2.service.IStockFuturesService;

import com.stock.mx2.service.IStockOptionService;
import com.stock.mx2.service.IUserService;
import com.stock.mx2.utils.DateTimeUtil;
import com.stock.mx2.utils.HttpClientRequest;

import com.stock.mx2.utils.PropertiesUtil;

import com.stock.mx2.vo.foreigncurrency.ExchangeVO;

import com.stock.mx2.vo.stockfutures.FuturesAdminListVO;

import com.stock.mx2.vo.stockfutures.FuturesVO;

import com.stock.mx2.vo.stockfutures.StockFuturesListVO;

import java.math.BigDecimal;

import java.math.RoundingMode;
import java.util.Date;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;


@Service("iStockFuturesService")

public class StockFuturesServiceImpl implements IStockFuturesService {

    private static final Logger log = LoggerFactory.getLogger(StockFuturesServiceImpl.class);

    @Autowired
    StockFuturesMapper stockFuturesMapper;

    @Autowired
    IStockCoinService iStockCoinService;

    @Autowired
    IUserService iUserService;

    @Autowired
    IStockOptionService iStockOptionService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private KLineMapper kLineMapper;

    @Autowired
    TradePlateItemHistoryMapper tradePlateItemHistoryMapper;


    public ServerResponse listByAdmin(String fuName, String fuCode, int pageNum, int pageSize) {

        PageHelper.startPage(pageNum, pageSize);

        List<StockFutures> stockFutures = this.stockFuturesMapper.listByAdmin(fuName, fuCode);


        List<FuturesAdminListVO> futuresAdminListVOS = Lists.newArrayList();

        for (StockFutures stockFuture : stockFutures) {


            FuturesAdminListVO futuresAdminListVO = assembleFuturesAdminListVO(stockFuture);

            futuresAdminListVOS.add(futuresAdminListVO);

        }


        PageInfo pageInfo = new PageInfo(stockFutures);

        pageInfo.setList(futuresAdminListVOS);

        return ServerResponse.createBySuccess(pageInfo);

    }

    private FuturesAdminListVO assembleFuturesAdminListVO(StockFutures stockFutures) {

        FuturesAdminListVO futuresAdminListVO = new FuturesAdminListVO();


        futuresAdminListVO.setId(stockFutures.getId());

        futuresAdminListVO.setFuturesName(stockFutures.getFuturesName());

        futuresAdminListVO.setFuturesCode(stockFutures.getFuturesCode());

        futuresAdminListVO.setFuturesGid(stockFutures.getFuturesGid());

        futuresAdminListVO.setFuturesUnit(stockFutures.getFuturesUnit());

        futuresAdminListVO.setFuturesStandard(stockFutures.getFuturesStandard());

        futuresAdminListVO.setCoinCode(stockFutures.getCoinCode());

        futuresAdminListVO.setHomeShow(stockFutures.getHomeShow());

        futuresAdminListVO.setListShow(stockFutures.getListShow());

        futuresAdminListVO.setDepositAmt(stockFutures.getDepositAmt());

        futuresAdminListVO.setTransFee(stockFutures.getTransFee());

        futuresAdminListVO.setMinNum(stockFutures.getMinNum());

        futuresAdminListVO.setMaxNum(stockFutures.getMaxNum());

        futuresAdminListVO.setAddTime(stockFutures.getAddTime());


        FuturesVO futuresVO = querySingleMarket(stockFutures.getFuturesGid());

        if (futuresVO != null) {

            futuresAdminListVO.setNow_price(futuresVO.getNowPrice());

            futuresAdminListVO.setLast_close(futuresVO.getLastClose());

        }


        BigDecimal coinRate = new BigDecimal("0");

        ServerResponse serverResponse = getExchangeRate(stockFutures.getCoinCode());

        if (serverResponse.isSuccess()) {

            coinRate = (BigDecimal) serverResponse.getData();

        }

        futuresAdminListVO.setCoinRate(coinRate);


        return futuresAdminListVO;

    }


    public ServerResponse add(StockFutures stockFutures) {

        if (StringUtils.isBlank(stockFutures.getFuturesName()) ||

                StringUtils.isBlank(stockFutures.getFuturesCode())) {

            return ServerResponse.createByErrorMsg("參數不能為空");

        }


        StockFutures fuName = this.stockFuturesMapper.selectFuturesByName(stockFutures.getFuturesName());

        if (fuName != null) {

            return ServerResponse.createByErrorMsg("產品名不能重複");

        }


        StockFutures fuCode = this.stockFuturesMapper.selectFuturesByCode(stockFutures.getFuturesCode());

        if (fuCode != null) {

            return ServerResponse.createByErrorMsg("代碼不能重複");

        }


        StockCoin stockCoin = this.iStockCoinService.selectCoinByCode(stockFutures.getCoinCode());

        if (stockCoin == null) {

            return ServerResponse.createByErrorMsg("基幣不存在");

        }


        stockFutures.setAddTime(new Date());


        int insertCount = this.stockFuturesMapper.insert(stockFutures);

        if (insertCount > 0) {

            return ServerResponse.createBySuccessMsg("添加成功");

        }

        return ServerResponse.createByErrorMsg("添加失敗");

    }


    public ServerResponse update(StockFutures stockFutures) {

        if (stockFutures.getId() == null) {

            return ServerResponse.createByErrorMsg("修改id不能為空");

        }


        StockFutures dbFutures = this.stockFuturesMapper.selectByPrimaryKey(stockFutures.getId());

        if (dbFutures == null) {

            return ServerResponse.createByErrorMsg("產品不存在");

        }


        if (stockFutures.getFuturesName() != null) {

            return ServerResponse.createByErrorMsg("產品名不能修改");

        }


        if (stockFutures.getFuturesCode() != null) {

            return ServerResponse.createByErrorMsg("產品代碼不能修改");

        }


        if (stockFutures.getFuturesGid() != null) {

            return ServerResponse.createByErrorMsg("Gid不能修改");

        }


        int updateCount = this.stockFuturesMapper.updateByPrimaryKeySelective(stockFutures);

        if (updateCount > 0) {

            return ServerResponse.createBySuccessMsg("修改成功");

        }

        return ServerResponse.createByErrorMsg("修改失敗");

    }


    public ServerResponse queryHome() {

        List<StockFutures> list = this.stockFuturesMapper.queryHome();


        List<StockFuturesListVO> stockFuturesListVOS = Lists.newArrayList();

        for (StockFutures stockFutures : list) {


            StockFuturesListVO stockFuturesListVO = assembleStockFuturesListVO(stockFutures);

            stockFuturesListVOS.add(stockFuturesListVO);

        }

        return ServerResponse.createBySuccess(stockFuturesListVOS);

    }


    public ServerResponse queryList(HttpServletRequest request) {

        List<StockFutures> list = this.stockFuturesMapper.queryList();


        List<StockFuturesListVO> stockFuturesListVOS = Lists.newArrayList();
        User user = iUserService.getCurrentUser(request);
        if (user == null) {
            return ServerResponse.createBySuccessMsg("請先登錄");
        }
        for (StockFutures stockFutures : list) {


            StockFuturesListVO stockFuturesListVO = assembleStockFuturesListVO(stockFutures);
            //是否添加自選
            if (user == null) {
                stockFuturesListVO.setIsOption("0");
            } else {
                stockFuturesListVO.setIsOption(iStockOptionService.isMyOption(user.getId(), stockFutures.getFuturesCode()));
            }

            stockFuturesListVOS.add(stockFuturesListVO);

        }

        return ServerResponse.createBySuccess(stockFuturesListVOS);

    }

    @Override
    public ServerResponse queryTrans(Integer paramInteger) {
        return null;
    }

    private StockFuturesListVO assembleStockFuturesListVO(StockFutures stockFutures) {

        StockFuturesListVO stockFuturesListVO = new StockFuturesListVO();


        stockFuturesListVO.setId(stockFutures.getId());

        stockFuturesListVO.setFuturesName(stockFutures.getFuturesName());

        stockFuturesListVO.setFuturesCode(stockFutures.getFuturesCode());

        stockFuturesListVO.setFuturesGid(stockFutures.getFuturesGid());

        stockFuturesListVO.setFuturesUnit(stockFutures.getFuturesUnit());

        stockFuturesListVO.setFuturesStandard(stockFutures.getFuturesStandard());

        stockFuturesListVO.setCoinCode(stockFutures.getCoinCode());

        stockFuturesListVO.setHomeShow(stockFutures.getHomeShow());

        stockFuturesListVO.setListShow(stockFutures.getListShow());

        stockFuturesListVO.setDepositAmt(stockFutures.getDepositAmt());

        stockFuturesListVO.setTransFee(stockFutures.getTransFee());

        stockFuturesListVO.setMinNum(stockFutures.getMinNum());

        stockFuturesListVO.setMaxNum(stockFutures.getMaxNum());

        stockFuturesListVO.setAddTime(stockFutures.getAddTime());


        FuturesVO futuresVO = querySingleMarket(stockFutures.getFuturesGid());

        if (futuresVO != null) {

            stockFuturesListVO.setNowPrice(futuresVO.getNowPrice());

        }


        BigDecimal coinRate = new BigDecimal("0");

        ServerResponse serverResponse = getExchangeRate(stockFutures.getCoinCode());

        if (serverResponse.isSuccess()) {

            coinRate = (BigDecimal) serverResponse.getData();

        }

        stockFuturesListVO.setCoinRate(coinRate);


        return stockFuturesListVO;

    }





    public FuturesVO querySingleMarket(String futuresGid) {
        if (futuresGid.equals("109_NG_YZCF00X")) {
            FuturesVO futuresVO = new FuturesVO();
            Long yesterday =System.currentTimeMillis() - 24 * 60 * 60 * 1000;
            //获取今天前一天的行情
            String coinThumbStr = tradePlateItemHistoryMapper.getTradePlateItemHistory(yesterday , "thumb");
            //深度
            String mergedJson = tradePlateItemHistoryMapper.getTradePlateItemHistory(yesterday , "merged");
            JSONObject merged = JSONObject.parseObject(mergedJson);


            BigDecimal buyPrice = new BigDecimal("0");
            BigDecimal buyNum = new BigDecimal("0");
            BigDecimal sellPrice = new BigDecimal("0");
            BigDecimal sellNum = new BigDecimal("0");
            if (merged != null ) {
                buyPrice = merged.getJSONArray("bid").getBigDecimal(0).multiply(new BigDecimal("54"));
                buyNum = merged.getJSONArray("bid").getBigDecimal(1);
                sellPrice =  merged.getJSONArray("ask").getBigDecimal(0).multiply(new BigDecimal("54"));
                sellNum = merged.getJSONArray("ask").getBigDecimal(1);
            }

            CoinThumb coinThumb = JSON.parseObject(coinThumbStr, CoinThumb.class);
            futuresVO.setNowPrice(coinThumb.getClose().toString());
            futuresVO.setMinPrice(coinThumb.getLow().toString());
            futuresVO.setMaxPrice(coinThumb.getHigh().toString());
            futuresVO.setLastClose(coinThumb.getLastDayClose().toString());

            futuresVO.setCode(futuresGid);
            futuresVO.setBuyPrice(buyPrice.toString());
            futuresVO.setSellPrice(sellPrice.toString());

            //今天的秒级时间戳
            //获取昨天的时间戳
            long yesterdayDate = System.currentTimeMillis() - 24 * 60 * 60 * 1000;
            yesterdayDate = yesterdayDate / 1000;
            KLine kline = this.kLineMapper.getKline("1day",yesterdayDate);
            futuresVO.setExtra1(coinThumb.getClose().subtract(kline.getOpenPrice()).divide(kline.getOpenPrice(),8, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).setScale(6, RoundingMode.HALF_UP).toString());
            futuresVO.setExtra2(buyNum.toString());
            futuresVO.setExtra3(sellNum.toString());
            futuresVO.setExtra4(coinThumb.getClose().subtract(kline.getOpenPrice()).toString());
            futuresVO.setTodayOpen(kline.getOpenPrice().toString());
            futuresVO.setName("天然氣 NG");

            String nowPriceStr = redisTemplate.opsForValue().get(futuresGid);
            if (nowPriceStr!=null){
                futuresVO.setNowPrice(nowPriceStr);
            }
            futuresVO.setNowPrice(nowPriceStr);

            return futuresVO;


        } else {
            String market_url = PropertiesUtil.getProperty("sina.single.futures.url");

            String result = null;

            try {
                futuresGid = futuresGid + "_qt";
                market_url = market_url + futuresGid;

                result = HttpUtil.get("https://togfmxkzwys8iwtk.srmd.ink/amlla291/index/gupiao");

            } catch (Exception e) {

                log.error("獲取 期貨行情 出錯 e = {}", e);

            }
            if (result == null || result.equals("")) {

                return null;

            }

            JSONObject jsonObject = JSONObject.parseObject(result);

            jsonObject = jsonObject.getJSONObject("qt");
            FuturesVO futuresVO = new FuturesVO();

            try {
                futuresVO.setNowPrice(jsonObject.getString("p"));
                futuresVO.setExtra1(jsonObject.getString("zdf"));
                futuresVO.setMinPrice(jsonObject.getString("l"));
                futuresVO.setMaxPrice(jsonObject.getString("h"));
                futuresVO.setLastClose(jsonObject.getString("qrspj"));
                futuresVO.setBuyPrice(jsonObject.getString("mcj"));
                futuresVO.setSellPrice(jsonObject.getString("mrj"));
                futuresVO.setCode(futuresGid);

                futuresVO.setExtra2(jsonObject.getString("wp"));

                futuresVO.setExtra3(jsonObject.getString("np"));
                futuresVO.setExtra4(jsonObject.getString("zde"));

                futuresVO.setName("天然氣");
                futuresVO.setTodayOpen(jsonObject.getString("o"));


            } catch (Exception e) {

                log.error("轉換 期貨行情 出錯 str = {} ,  e = {}", result, e);

            }

            String nowPriceStr = redisTemplate.opsForValue().get(futuresGid);
            if (nowPriceStr!=null){
                futuresVO.setNowPrice(nowPriceStr);
            }
            return futuresVO;

        }


    }


    public ServerResponse<ExchangeVO> queryExchangeVO(String coinCode) {

        StockCoin stockCoin = this.iStockCoinService.selectCoinByCode(coinCode);

        if (stockCoin == null) {

            return ServerResponse.createByErrorMsg("不存在該幣種");

        }

        if (stockCoin.getCoinGid() == null) {

            return ServerResponse.createByErrorMsg("coin gid 為空");

        }


        String market_url = PropertiesUtil.getProperty("sina.single.exchange.url");

        String result = null;

        try {

            market_url = market_url + stockCoin.getCoinGid();

            result = HttpClientRequest.doGet(market_url);

        } catch (Exception e) {

            log.error("獲取 外匯 行情 出錯 e = {}", e);

        }


        ExchangeVO exchangeVO = null;

        try {

            if (StringUtils.isNotBlank(result)) {

                result = result.substring(result.indexOf("\"") + 1, result

                        .lastIndexOf("\""));


                exchangeVO = new ExchangeVO();

                if (result.contains(",")) {

                    String[] sh01_arr = result.split(",");

                    exchangeVO.setHourTime(sh01_arr[0]);

                    exchangeVO.setExtra1(sh01_arr[1]);

                    exchangeVO.setExtra2(sh01_arr[2]);

                    exchangeVO.setLastClose(sh01_arr[3]);

                    exchangeVO.setBofu(sh01_arr[4]);

                    exchangeVO.setTodayOpen(sh01_arr[5]);

                    exchangeVO.setMaxPrice(sh01_arr[6]);

                    exchangeVO.setMinPrice(sh01_arr[7]);

                    exchangeVO.setNowPrice(sh01_arr[8]);

                    exchangeVO.setName(sh01_arr[9]);

                }

            }

        } catch (Exception e) {

            log.error("轉換 外匯行情 出錯 str = {} ,  e = {}", result, e);

        }

        return ServerResponse.createBySuccess(exchangeVO);

    }


    public ServerResponse<BigDecimal> getExchangeRate(String coinCode) {

        StockCoin stockCoin = this.iStockCoinService.selectCoinByCode(coinCode);

        if (stockCoin == null) {

            return ServerResponse.createByErrorMsg("基幣設置錯誤");

        }


        BigDecimal exchangeRate = null;

        ExchangeVO exchangeVO = null;

        if (stockCoin.getDynamicRate().intValue() == 0) {


            ServerResponse serverResponse = queryExchangeVO(coinCode);

            if (serverResponse.isSuccess()) {

                exchangeVO = (ExchangeVO) serverResponse.getData();

                exchangeRate = new BigDecimal(exchangeVO.getNowPrice());

            } else {

                return ServerResponse.createByErrorMsg(serverResponse.getMsg());

            }

        } else {


            exchangeRate = stockCoin.getDefaultRate();

        }


        exchangeRate = exchangeRate.setScale(2, 4);


        return ServerResponse.createBySuccess(exchangeRate);

    }


    public StockFutures selectFuturesById(Integer futuresId) {
        return this.stockFuturesMapper.selectByPrimaryKey(futuresId);
    }


    public StockFutures selectFuturesByCode(String futuresCode) {
        return this.stockFuturesMapper.selectFuturesByCode(futuresCode);
    }

}

