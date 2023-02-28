package com.stock.mx2.utils.stock.sina;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.Stock;
import com.stock.mx2.pojo.StockFutures;
import com.stock.mx2.pojo.StockIndex;
import com.stock.mx2.utils.HttpClientRequest;
import com.stock.mx2.utils.PropertiesUtil;
import com.stock.mx2.utils.redis.JsonUtil;
import com.stock.mx2.utils.stock.sina.vo.SinaStockMinData;
import com.stock.mx2.vo.stock.StockListVO;
import com.stock.mx2.vo.stock.StockVO;
import com.stock.mx2.vo.stock.k.MinDataVO;
import com.stock.mx2.vo.stock.k.echarts.EchartsDataVO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SinaStockApi {
    public static final String sina_url = PropertiesUtil.getProperty("sina.TwMarket.url");
    private static final Logger log = LoggerFactory.getLogger(SinaStockApi.class);

    public static String getSinaStock(List<String> stockGids) {

        if (stockGids.size() == 0) {
            return "";
        }


        StringBuilder urlParams = new StringBuilder();

        for (String stockGid : stockGids) {
            urlParams.append("TWG:").append(stockGid).append(":STOCK,");
            urlParams.append("TWS:").append(stockGid).append(":STOCK,");
        }

//        urlParams 去除最後一個逗號
        urlParams.deleteCharAt(urlParams.length() - 1);
        String sina_result = "";
        System.out.println(
                sina_url + urlParams.toString()
        );
        try {
            sina_result = HttpClientRequest.doGet(sina_url + urlParams.toString());
        } catch (Exception e) {
            log.error("獲取股票行情出錯，錯誤信息 = {}", e);
        }
        return sina_result;
    }


    public static List<StockListVO> assembleStockListVO(String sinaResult, List<String> codeList) {
        if (sinaResult.equals("") || sinaResult == null) {
            log.info("數據為空");
            return new ArrayList<>();
        }

        com.alibaba.fastjson.JSONObject resJsonObject = JSON.parseObject(sinaResult);
        if (resJsonObject.getJSONArray("data")==null){

            return new ArrayList<>();

        }

        com.alibaba.fastjson.JSONArray jsonArray =resJsonObject.getJSONArray("data");
        if (jsonArray.size() == 0) {
            log.info("數據為空");
            return new ArrayList<>();
        }

        List<StockListVO> stockListVOList = Lists.newArrayList();
        com.alibaba.fastjson.JSONObject jsonObject = null;
        for (String code : codeList) {
            StockListVO stockListVO = null;
            for (int i = 0; i < jsonArray.size(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject != null) {
                    String resCode = jsonObject.getString("200010");
                    if (code.equals(resCode) && jsonObject.getString("6")!=null) {
                        String string = jsonObject.getString("6");
                        if (string==null){
                            continue;
                        }
                        if (new BigDecimal(string).compareTo(BigDecimal.ZERO)==0){
                            continue;
                        }
                        stockListVO = new StockListVO();
                        stockListVO.setName(jsonObject.getString("200009"));
                        stockListVO.setCode(resCode);
                        stockListVO.setSpell("");
                        stockListVO.setGid(jsonObject.getString("200009"));
                        stockListVO.setNowPrice(jsonObject.getString("6"));

//                        try {
//                            String s = HttpUtil.get("http://47.243.121.145:8000/get_price/?code=" +code);
//                            BigDecimal nowPrice = new BigDecimal(s);
//                            stockListVO.setNowPrice(nowPrice.toString());
//                        }catch (Exception e){
                            stockListVO.setNowPrice(jsonObject.getString("6"));
//                        }
                        stockListVO.setHcrate(jsonObject.getBigDecimal("56"));
                        stockListVO.setHcrates(jsonObject.getBigDecimal("11"));
                        stockListVO.setToday_max(jsonObject.getString("12"));
                        stockListVO.setToday_min(jsonObject.getString("13"));
                        stockListVO.setBusiness_balance("");
                        stockListVO.setBusiness_amount("");
                        stockListVO.setPreclose_px(jsonObject.getString("21"));
                        stockListVO.setOpen_px(jsonObject.getString("19"));
                        stockListVO.setDay3Rate(new BigDecimal("0"));
                        stockListVO.setStock_type("");
                        stockListVO.setVolume(jsonObject.getString("800001"));
                        stockListVO.setStock_plate("");
                        stockListVO.setIsOption("");
                    }
                }

            }
            if (stockListVO != null) {
                stockListVOList.add(stockListVO);
            }
        }


        //如果长度不匹配
        if (stockListVOList.size()!= codeList.size()){
            //循环出cnyes 没有的股票数据 去官方查找。
            for (String code : codeList) {
                boolean codeFlag = false;
                for (StockListVO listVO : stockListVOList) {
                    if (code.equals(listVO.getCode())) {
                        codeFlag = true;
                        break;
                    }
                }
                if (!codeFlag){
                    try{
                        StockListVO stockListVO = searchByAdmin(code);
                        if (stockListVO!=null){
                            stockListVOList.add(stockListVO);
                        }
                    }catch (Exception e){
                        continue;
                    }

                }
            }



        }
        return stockListVOList;
    }

    private static StockListVO searchByAdmin(String code) {
        String json  = HttpUtil.get("https://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=tse_"+code+".tw&json=1&delay=0&_="+System.currentTimeMillis());
        if (json == null || "".equals(json)){
            return null;
        }
        JSONObject jsonObject = null;
        try {
             jsonObject = JSONObject.parseObject(json);
        }catch (Exception e){
            return null;
        }
        if (jsonObject == null ){
            return null;
        }
        JSONArray jsonArray = jsonObject.getJSONArray("msgArray");
        if (jsonArray == null ||jsonArray.size()==0){
            return null;
        }
        JSONObject nowData = jsonArray.getJSONObject(0);
        if (nowData == null ||  nowData.getString("z")==null||nowData.getString("n")==null){
            return null;
        }

        StockListVO stockListVO = new StockListVO();
        stockListVO.setNowPrice(nowData.getString("z"));
        stockListVO.setName(nowData.getString("n"));
        stockListVO.setCode(code);
        stockListVO.setSpell("");
        stockListVO.setGid(nowData.getString("n"));

        //最新价
        if ("-".equals(stockListVO.getNowPrice())){
            stockListVO.setNowPrice(nowData.getString("o"));

        }else{
            System.out.println(code+"最新价"+stockListVO.getNowPrice());
        }
        //最新价
        if ("-".equals(stockListVO.getNowPrice())){
            stockListVO.setNowPrice(nowData.getString("y"));
        }else{
            System.out.println(code+"开盘价"+stockListVO.getNowPrice());
        }
        if ("-".equals(stockListVO.getNowPrice())) {
            return null;
        }else {
            System.out.println(code+"昨收价"+stockListVO.getNowPrice());

        }

            BigDecimal yamount = new BigDecimal(nowData.getString("y"));

        //漲跌額
        BigDecimal buy2 = new BigDecimal(stockListVO.getNowPrice()).subtract(yamount);
        stockListVO.setHcrates(buy2);
        //涨跌幅
        BigDecimal hcrate = buy2.divide(yamount,4, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
        stockListVO.setHcrate(hcrate);
        BigDecimal h = yamount;
        if (!"-".equals(nowData.getString("h"))){
             h = new BigDecimal(nowData.getString("h")).setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal l = yamount;
        if (!"-".equals(nowData.getString("l"))){
            l = new BigDecimal(nowData.getString("l")).setScale(2, RoundingMode.HALF_UP);
        }
        stockListVO.setToday_max(h.toString());
        stockListVO.setToday_min(l.toString());
        stockListVO.setBusiness_balance("");
        stockListVO.setBusiness_amount("");
        BigDecimal o = yamount;
        if (!"-".equals(nowData.getString("o"))){
            o = new BigDecimal(nowData.getString("o")).setScale(2, RoundingMode.HALF_UP);
        }

        stockListVO.setOpen_px(o.toString());
        stockListVO.setDay3Rate(new BigDecimal("0"));
        stockListVO.setStock_type("");
        stockListVO.setVolume(jsonObject.getString("v"));
        stockListVO.setStock_plate("");
        stockListVO.setIsOption("");
        return stockListVO;
    }



    public static ServerResponse<MinDataVO> getStockMinK(Stock stock, int time, int ma, int size) {
        int maxSize = Integer.parseInt(PropertiesUtil.getProperty("sina.k.min.max.size"));
        if (size > maxSize) {
            size = maxSize;
        }

        String minUrl = PropertiesUtil.getProperty("sina.k.min.url");
        minUrl = minUrl + "?symbol=" + stock.getStockGid() + "&scale=" + time + "&ma=" + ma + "&datalen=" + size;

        String hqstr = "";
        try {
            hqstr = HttpClientRequest.doGet(minUrl);
        } catch (Exception e) {
            log.error("獲取股票K線分時圖出錯，錯誤信息 = {}", e);
        }

        log.info(" time = {} ma = {} size = {}", new Object[]{Integer.valueOf(time), Integer.valueOf(ma), Integer.valueOf(size)});


        hqstr = hqstr.replace("day", "\"day\"").replace("open", "\"open\"").replace("high", "\"high\"").replace("low", "\"low\"").replace("close", "\"close\"");

        if (ma == 5) {

            hqstr = hqstr.replace("ma_volume5", "\"ma_volume\"").replace(",volume", ",\"volume\"").replace("ma_price5", "\"ma_price\"");
        } else if (ma == 10) {


            hqstr = hqstr.replace("ma_volume10", "\"ma_volume\"").replace(",volume", ",\"volume\"").replace("ma_price10", "\"ma_price\"");
        } else if (ma == 15) {

            hqstr = hqstr.replace("ma_volume15", "\"ma_volume\"").replace(",volume", ",\"volume\"").replace("ma_price15", "\"ma_price\"");
        } else {
            return ServerResponse.createByErrorMsg("ma 取值 5，10，15");
        }


        if (StringUtils.isBlank(hqstr)) {
            return ServerResponse.createByErrorMsg("沒有查詢到行情數據");
        }

        MinDataVO minDataVO = new MinDataVO();
        minDataVO.setStockName(stock.getStockName());
        minDataVO.setStockCode(stock.getStockCode());
        minDataVO.setGid(stock.getStockGid());

        hqstr = hqstr.replaceAll("\"\"", "\"");

        List<SinaStockMinData> list = (List<SinaStockMinData>) JsonUtil.string2Obj(hqstr, new TypeReference<List<SinaStockMinData>>() {
        });
        log.info("需要查詢的行情size為： {}", Integer.valueOf(size));

        minDataVO.setData(list);

        return ServerResponse.createBySuccess(minDataVO);
    }

    /*期貨分時-k線
     * stock：期貨代碼
     * time：5、15、30、60，單位分鐘
     * */
    public static ServerResponse<MinDataVO> getFuturesMinK(StockFutures stock, int time, int size) {
        String minUrl = PropertiesUtil.getProperty("sina.futures.k.min.url").replace("{code}", stock.getFuturesCode()).replace("{time}", String.valueOf(time));
        String stamp = String.valueOf(new Date().getTime());// new Date()為獲取當前系統時間
        minUrl = minUrl.replace("{stamp}", stamp);

        String hqstr = "";
        try {
            hqstr = HttpClientRequest.doGet(minUrl);
        } catch (Exception e) {
            log.error("獲取股票K線分時圖出錯，錯誤信息 = {}", e);
        }

        log.info("期貨分時 - time = {} ", time);

        hqstr = hqstr.split("\\[")[1].replace("]);", "");
        hqstr = hqstr.replace("d", "\"day\"");
        hqstr = hqstr.replace("o", "\"open\"");
        hqstr = hqstr.replace("h", "\"high\"");
        hqstr = hqstr.replace("l", "\"low\"");
        hqstr = hqstr.replace("c", "\"close\"");
        hqstr = hqstr.replace("v", "\"volume\"");

        if (StringUtils.isBlank(hqstr)) {
            return ServerResponse.createByErrorMsg("沒有查詢到行情數據");
        }

        MinDataVO minDataVO = new MinDataVO();
        minDataVO.setStockName(stock.getFuturesName());
        minDataVO.setStockCode(stock.getFuturesCode());
        minDataVO.setGid(stock.getFuturesGid());

        hqstr = hqstr.replaceAll("\"\"", "\"");
        hqstr = "[" + hqstr + "]";

        List<SinaStockMinData> list = (List<SinaStockMinData>) JsonUtil.string2Obj(hqstr, new TypeReference<List<SinaStockMinData>>() {
        });
        //int size = Integer.valueOf(PropertiesUtil.getProperty("sina.futures.k.min.max.size"));
        if (list.size() > size) {
            list = list.subList((list.size() - size - 1), list.size());
        }
        minDataVO.setData(list);
        return ServerResponse.createBySuccess(minDataVO);
    }

    public static EchartsDataVO assembleEchartsDataVO(MinDataVO minDataVO) {
        EchartsDataVO echartsDataVO = new EchartsDataVO();
        echartsDataVO.setStockName(minDataVO.getStockName());
        echartsDataVO.setStockCode(minDataVO.getStockCode());

        List<SinaStockMinData> minDataList = minDataVO.getData();


        double[][] values = (double[][]) null;
        Object[][] volumes = (Object[][]) null;
        String[] date = null;

        if (minDataList.size() > 0) {

            values = new double[minDataList.size()][5];

            volumes = new Object[minDataList.size()][3];

            date = new String[minDataList.size()];

            for (int i = 0; i < minDataList.size(); i++) {
                SinaStockMinData sinaStockMinData = (SinaStockMinData) minDataList.get(i);

                for (int j = 0; j < values[i].length; j++) {
                    values[i][0] = Double.valueOf(sinaStockMinData.getOpen()).doubleValue();
                    values[i][1] = Double.valueOf(sinaStockMinData.getClose()).doubleValue();
                    values[i][2] = Double.valueOf(sinaStockMinData.getLow()).doubleValue();
                    values[i][3] = Double.valueOf(sinaStockMinData.getHigh()).doubleValue();
                    values[i][4] = Double.valueOf(sinaStockMinData.getVolume()).doubleValue();
                }
                for (int k = 0; k < volumes[i].length; k++) {
                    volumes[i][0] = Integer.valueOf(i);
                    volumes[i][1] = Double.valueOf(sinaStockMinData.getVolume());

                    if ((new BigDecimal(sinaStockMinData.getClose()))
                            .compareTo(new BigDecimal(sinaStockMinData.getOpen())) == 1) {
                        volumes[i][2] = Integer.valueOf(1);
                    } else {
                        volumes[i][2] = Integer.valueOf(-1);
                    }
                }

                date[i] = sinaStockMinData.getDay();
            }
        }

        echartsDataVO.setValues(values);
        echartsDataVO.setVolumes(volumes);
        echartsDataVO.setDate(date);

        return echartsDataVO;
    }

    /*指數分時-k線
     * stock：指數代碼
     * time：5、15、30、60，單位分鐘
     * */
    public static ServerResponse<MinDataVO> getIndexMinK(StockIndex stock, int time, int size) {
        String minUrl = PropertiesUtil.getProperty("sina.index.k.min.url").replace("{code}", stock.getIndexGid()).replace("{time}", String.valueOf(time));
        String stamp = String.valueOf(new Date().getTime());// new Date()為獲取當前系統時間
        minUrl = minUrl.replace("{stamp}", stamp);

        String hqstr = "";
        try {
            hqstr = HttpClientRequest.doGet(minUrl);
        } catch (Exception e) {
            log.error("獲取股票K線分時圖出錯，錯誤信息 = {}", e);
        }

        log.info("期貨分時 - time = {} ", time);

        hqstr = hqstr.split("\\[")[1].replace("]);", "");
        /*hqstr = hqstr.replace("d","\"day\"");
        hqstr = hqstr.replace("o","\"open\"");
        hqstr = hqstr.replace("h","\"high\"");
        hqstr = hqstr.replace("l","\"low\"");
        hqstr = hqstr.replace("c","\"close\"");
        hqstr = hqstr.replace("v","\"volume\"");*/

        if (StringUtils.isBlank(hqstr)) {
            return ServerResponse.createByErrorMsg("沒有查詢到行情數據");
        }

        MinDataVO minDataVO = new MinDataVO();
        minDataVO.setStockName(stock.getIndexName());
        minDataVO.setStockCode(stock.getIndexCode());
        minDataVO.setGid(stock.getIndexGid());

        hqstr = hqstr.replaceAll("\"\"", "\"");
        hqstr = "[" + hqstr + "]";

        List<SinaStockMinData> list = (List<SinaStockMinData>) JsonUtil.string2Obj(hqstr, new TypeReference<List<SinaStockMinData>>() {
        });
        //int size = Integer.valueOf(PropertiesUtil.getProperty("sina.index.k.min.max.size"));
        if (list.size() > size) {
            list = list.subList((list.size() - size - 1), list.size());
        }
        minDataVO.setData(list);
        return ServerResponse.createBySuccess(minDataVO);
    }


    public static StockVO assembleStockVO(String sinaResult,String code) {
        StockVO stockVO = new StockVO();
        if (sinaResult.equals("") || sinaResult == null) {
            log.info("數據為空");
            return stockVO;
        }

        com.alibaba.fastjson.JSONObject resJsonObject = JSON.parseObject(sinaResult);

        com.alibaba.fastjson.JSONArray jsonArray =resJsonObject.getJSONArray("data");
        JSONObject Twhqstr = null;
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            if ( jsonObject.getString("6")!=null) {
                Twhqstr = jsonArray.getJSONObject(i);
            }
        }
        if (Twhqstr != null) {
            stockVO.setName(Twhqstr.getString("200009"));
            stockVO.setBuy1(Twhqstr.getString("800001"));
            stockVO.setBuy2(Twhqstr.getString("11"));
            stockVO.setNowPrice(Twhqstr.getString("6"));
            stockVO.setHcrate(Twhqstr.getBigDecimal("56"));
        }

        if (stockVO.getNowPrice() == null){
            StockListVO stockListVO = searchByAdmin(code);
            if (stockListVO==null){
                return stockVO;
            }
            stockVO.setNowPrice(stockListVO.getNowPrice());
            stockVO.setHcrate(stockListVO.getHcrate());
            stockVO.setName(stockListVO.getName());
            stockVO.setBuy1(stockListVO.getVolume());
            stockVO.setBuy2(stockListVO.getHcrates().toString());
        }

        return stockVO;
    }
}

