package com.stock.mx2.service.impl;

import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.config.StockPoll;
import com.stock.mx2.dao.*;
import com.stock.mx2.pojo.*;
import com.stock.mx2.service.IStockMarketsDayService;
import com.stock.mx2.service.IStockOptionService;
import com.stock.mx2.service.IStockService;
import com.stock.mx2.service.IUserService;
import com.stock.mx2.utils.HttpClientRequest;
import com.stock.mx2.utils.PropertiesUtil;
import com.stock.mx2.utils.stock.pinyin.GetPyByChinese;
import com.stock.mx2.utils.stock.qq.QqStockApi;
import com.stock.mx2.utils.stock.sina.SinaStockApi;
import com.stock.mx2.vo.stock.*;
import com.stock.mx2.vo.stock.k.MinDataVO;
import com.stock.mx2.vo.stock.k.echarts.EchartsDataVO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("iStockService")
public class StockServiceImpl implements IStockService {
    private static final Logger log = LoggerFactory.getLogger(StockServiceImpl.class);

    @Autowired
    StockMapper stockMapper;

    @Autowired
    private NewStockMapper newStockMapper;

    @Autowired
    RealTimeMapper realTimeMapper;

    @Autowired
    private StockTypeMapper stockTypeMapper;

    @Autowired
    IStockMarketsDayService iStockMarketsDayService;

    @Autowired
    StockPoll stockPoll;

    @Autowired
    StockFuturesMapper stockFuturesMapper;

    @Autowired
    StockIndexMapper stockIndexMapper;

    @Autowired
    IUserService iUserService;

    @Autowired
    IStockOptionService iStockOptionService;

    public ServerResponse getMarket() {
        String market_url = PropertiesUtil.getProperty("sina.market.url");
        String result = null;
        try {
            result = HttpClientRequest.doGet(market_url);
        } catch (Exception e) {
            log.error("e = {}", e);
        }
        String[] marketArray = result.split(";");
        List<MarketVO> marketVOS = Lists.newArrayList();
        for (int i = 0; i < marketArray.length; i++) {
            String hqstr = marketArray[i];
            try {
                if (StringUtils.isNotBlank(hqstr)) {
                    hqstr = hqstr.substring(hqstr.indexOf("\"") + 1, hqstr.lastIndexOf("\""));
                    MarketVO marketVO = new MarketVO();
                    String[] sh01_arr = hqstr.split(",");
                    marketVO.setName(sh01_arr[0]);
                    marketVO.setNowPrice(sh01_arr[1]);
                    marketVO.setIncrease(sh01_arr[2]);
                    marketVO.setIncreaseRate(sh01_arr[3]);
                    marketVOS.add(marketVO);
                }
            } catch (Exception e) {
                log.error("str = {} ,  e = {}", hqstr, e);
            }
        }
        MarketVOResult marketVOResult = new MarketVOResult();
        marketVOResult.setMarket(marketVOS);
        return ServerResponse.createBySuccess(marketVOResult);
    }

    public ServerResponse getStock(int pageNum, int pageSize, String keyWords, String stockPlate, String stockType, HttpServletRequest request, String stockTypeInt, String exchange) {
        PageHelper.startPage(pageNum, pageSize);
        User user = iUserService.getCurrentUser(request);



        List<Stock> stockList = this.stockMapper.findStockListByKeyWords(keyWords, stockPlate, stockType,stockTypeInt,exchange, Integer.valueOf(0));
        ArrayList<String> objects = new ArrayList<>();
        for (Stock stock : stockList) {
            objects.add(stock.getStockCode());
        }
        List<StockListVO> stockListVOs = SinaStockApi.assembleStockListVO(SinaStockApi.getSinaStock(objects),objects);

        List<StockListVO> stockListVOList = new ArrayList<>();

        if (stockList.size() > 0)
            for (Stock stock : stockList) {
                for (StockListVO listVO : stockListVOs) {
                    if (listVO.getCode().equals(stock.getStockCode())){
                        listVO.setName(stock.getStockName());
                        listVO.setCode(stock.getStockCode());
                        listVO.setSpell(stock.getStockSpell());
                        listVO.setGid(stock.getStockCodeExchange());
                        listVO.setStock_plate(stock.getStockPlate());
                        listVO.setStock_type(stock.getStockType());
                        //是否添加自選
                        if (user != null){
                            listVO.setIsOption(iStockOptionService.isMyOption(user.getId(), stock.getStockCode()));

                        }
                        stockListVOList.add(listVO);
                    }
                }

            }
        PageInfo pageInfo = new PageInfo(stockListVOList);
        pageInfo.setList(stockListVOList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    public void z1() {
        this.stockPoll.z1();
    }

    public void z11() {
        this.stockPoll.z11();
    }

    public void z12() {
        this.stockPoll.z12();
    }

    public void z2() {
        this.stockPoll.z2();
    }

    public void z21() {
        this.stockPoll.z21();
    }

    public void z22() {
        this.stockPoll.z22();
    }

    public void z3() {
        this.stockPoll.z3();
    }

    public void z31() {
        this.stockPoll.z31();
    }

    public void z32() {
        this.stockPoll.z32();
    }

    public void z4() {
        this.stockPoll.z4();
    }

    public void z41() {
        this.stockPoll.z41();
    }

    public void z42() {
        this.stockPoll.z42();
    }

    public void h1() {
        this.stockPoll.h1();
    }

    public void h11() {
        this.stockPoll.h11();
    }

    public void h12() {
        this.stockPoll.h12();
    }

    public void h2() {
        this.stockPoll.h2();
    }

    public void h21() {
        this.stockPoll.h21();
    }

    public void h22() {
        this.stockPoll.h22();
    }

    public void h3() {
        this.stockPoll.h3();
    }

    public void h31() {
        this.stockPoll.h31();
    }

    public void h32() {
        this.stockPoll.h32();
    }

    public ServerResponse getDateline(HttpServletResponse response, String code) {
        if (StringUtils.isBlank(code))
            return ServerResponse.createByErrorMsg("");
        List<Stock> stockByCode = this.stockMapper.findStockByCode(code);
        Stock stock = null;
        if (stockByCode.size()>0) {
            stock =stockByCode.get(0);
        }
        if (stock == null)
            return ServerResponse.createByErrorMsg("");
        response.setHeader("Access-Control-Allow-Origin", "*");
        Date time = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String end = sdf.format(time);
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(2, -3);
        Date m = c.getTime();
        String mon = sdf.format(m);
        String methodUrl = "http://q.stock.sohu.com/hisHq?code=cn_" + code + "+&start=" + mon + "&end=" + end + "&stat=1&order=D";
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        String line = null;
        EchartsDataVO echartsDataVO = new EchartsDataVO();
        try {
            URL url = new URL(methodUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            if (connection.getResponseCode() == 200) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "gbk"));
                StringBuilder result = new StringBuilder();
                while ((line = reader.readLine()) != null)
                    result.append(line).append(System.getProperty("line.separator"));
                JSONArray jsonArray = JSONArray.fromObject(result.toString());
                JSONObject json = jsonArray.getJSONObject(0);
                JSONArray jsonArray1 = JSONArray.fromObject(json.get("hq"));
                Collections.reverse((List<?>) jsonArray1);
                double[][] values = (double[][]) null;
                Object[][] volumes = (Object[][]) null;
                String[] date = null;
                values = new double[jsonArray1.size()][5];
                volumes = new Object[jsonArray1.size()][3];
                date = new String[jsonArray1.size()];
                for (int i = 0; i < jsonArray1.size(); i++) {
                    JSONArray js = JSONArray.fromObject(jsonArray1.get(i));
                    date[i] = js.get(0).toString();
                    values[i][0] = Double.valueOf(js.get(1).toString()).doubleValue();
                    values[i][1] = Double.valueOf(js.get(2).toString()).doubleValue();
                    values[i][2] = Double.valueOf(js.get(5).toString()).doubleValue();
                    values[i][3] = Double.valueOf(js.get(6).toString()).doubleValue();
                    values[i][4] = Double.valueOf(js.get(7).toString()).doubleValue();
                    volumes[i][0] = Integer.valueOf(i);
                    volumes[i][1] = Double.valueOf(js.get(7).toString());
                    volumes[i][2] = Integer.valueOf((Double.valueOf(js.get(3).toString()).doubleValue() > 0.0D) ? 1 : -1);
                }
                echartsDataVO.setDate(date);
                echartsDataVO.setValues(values);
                echartsDataVO.setVolumes(volumes);
                echartsDataVO.setStockCode(stock.getStockCode());
                echartsDataVO.setStockName(stock.getStockName());
                log.info(String.valueOf(echartsDataVO));
                ServerResponse.createBySuccess(echartsDataVO);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            connection.disconnect();
        }
        return ServerResponse.createBySuccess(echartsDataVO);
    }

    public ServerResponse getSingleStock(String code) {
        if (StringUtils.isBlank(code))
            return ServerResponse.createByErrorMsg("");
        Stock stock = new Stock();
        Integer depositAmt = 0;
        //期貨
        if (code.contains("hf")) {
            StockFutures futmodel = stockFuturesMapper.selectFuturesByCode(code.replace("hf_", ""));
            stock.setStockGid(futmodel.getFuturesGid());
            stock.setStockCode(futmodel.getFuturesCode());
            stock.setStockName(futmodel.getFuturesName());
            stock.setAddTime(futmodel.getAddTime());
            stock.setId(futmodel.getId());
            stock.setStockSpell("0");
            depositAmt = futmodel.getDepositAmt();
        } else if (code.contains("sh") || code.contains("sz")) { //指數
            StockIndex model = stockIndexMapper.selectIndexByCode(code.replace("sh", "").replace("sz", ""));
            stock.setStockGid(model.getIndexGid());
            stock.setStockCode(model.getIndexCode());
            stock.setStockName(model.getIndexName());
            stock.setAddTime(model.getAddTime());
            stock.setId(model.getId());
            stock.setStockSpell("0");
            depositAmt = model.getDepositAmt();
        } else {//股票
            List<Stock> stockByCode = this.stockMapper.findStockByCode(code);
            if (stockByCode.size()>0) {
                stock =stockByCode.get(0);
            }
        }

        if (stock == null)
            return ServerResponse.createByErrorMsg("");


        ArrayList<String> objects = new ArrayList<>();
        objects.add(stock.getStockCode());
        String sinaResult = SinaStockApi.getSinaStock(objects);
        StockVO stockVO = new StockVO();

            stockVO = SinaStockApi.assembleStockVO(sinaResult,stock.getStockCode());
        stockVO.setDepositAmt(depositAmt);
        stockVO.setId(stock.getId().intValue());
        stockVO.setCode(stock.getStockCode());
        stockVO.setSpell(stock.getStockSpell());
        stockVO.setGid(stock.getStockGid());

        return ServerResponse.createBySuccess(stockVO);

    }


    public ServerResponse getMinK(String code, Integer time, Integer ma, Integer size) {
        if (StringUtils.isBlank(code) || time == null || ma == null || size == null)
            return ServerResponse.createByErrorMsg("");

        List<Stock> stockByCode = this.stockMapper.findStockByCode(code);
        Stock stock = null;
        if (stockByCode.size()>0) {
            stock =stockByCode.get(0);
        }
        if (stock == null)
            return ServerResponse.createByErrorMsg("");
        return SinaStockApi.getStockMinK(stock, time.intValue(), ma.intValue(), size.intValue());
    }

    public ServerResponse getMinK_Echarts(String code, Integer time, Integer ma, Integer size) {
        if (StringUtils.isBlank(code) || time == null || ma == null || size == null)
            return ServerResponse.createByErrorMsg("");

        List<Stock> stockByCode = this.stockMapper.findStockByCode(code);
        Stock stock = null;
        if (stockByCode.size()>0) {
            stock =stockByCode.get(0);
        }
        if (stock == null)
            return ServerResponse.createByErrorMsg("");
        ServerResponse<MinDataVO> serverResponse = SinaStockApi.getStockMinK(stock, time.intValue(), ma.intValue(), size.intValue());
        MinDataVO minDataVO = (MinDataVO) serverResponse.getData();
        EchartsDataVO echartsDataVO = SinaStockApi.assembleEchartsDataVO(minDataVO);
        return ServerResponse.createBySuccess(echartsDataVO);
    }

    /*期貨分時-k線*/
    public ServerResponse getFuturesMinK_Echarts(String code, Integer time, Integer size) {
        if (StringUtils.isBlank(code) || time == null)
            return ServerResponse.createByErrorMsg("");
        StockFutures stock = this.stockFuturesMapper.selectFuturesByCode(code.split("_")[1]);
        if (stock == null)
            return ServerResponse.createByErrorMsg("");
        ServerResponse<MinDataVO> serverResponse = SinaStockApi.getFuturesMinK(stock, time.intValue(), size.intValue());
        MinDataVO minDataVO = (MinDataVO) serverResponse.getData();
        EchartsDataVO echartsDataVO = SinaStockApi.assembleEchartsDataVO(minDataVO);
        return ServerResponse.createBySuccess(echartsDataVO);
    }

    /*指數分時-k線*/
    public ServerResponse getIndexMinK_Echarts(String code, Integer time, Integer size) {
        if (StringUtils.isBlank(code) || time == null)
            return ServerResponse.createByErrorMsg("");
        StockIndex stock = this.stockIndexMapper.selectIndexByCode(code.replace("sh", "").replace("sz", ""));
        if (stock == null)
            return ServerResponse.createByErrorMsg("");
        ServerResponse<MinDataVO> serverResponse = SinaStockApi.getIndexMinK(stock, time.intValue(), size.intValue());
        MinDataVO minDataVO = (MinDataVO) serverResponse.getData();
        EchartsDataVO echartsDataVO = SinaStockApi.assembleEchartsDataVO(minDataVO);
        return ServerResponse.createBySuccess(echartsDataVO);
    }

    /*股票日線-K線*/
    public ServerResponse getDayK_Echarts(String code) {
        if (StringUtils.isBlank(code))
            return ServerResponse.createByErrorMsg("");

        List<Stock> stockByCode = this.stockMapper.findStockByCode(code);
        Stock stock = null;
        if (stockByCode.size()>0) {
            stock =stockByCode.get(0);
        }
        if (stock == null)
            return ServerResponse.createByErrorMsg("");
        ServerResponse<MinDataVO> serverResponse = QqStockApi.getGpStockMonthK(stock, "day");
        MinDataVO minDataVO = (MinDataVO) serverResponse.getData();
        EchartsDataVO echartsDataVO = SinaStockApi.assembleEchartsDataVO(minDataVO);
        return ServerResponse.createBySuccess(echartsDataVO);
    }

    /*期貨日線-K線*/
    public ServerResponse getFuturesDayK(String code) {
        if (StringUtils.isBlank(code))
            return ServerResponse.createByErrorMsg("");
        StockFutures stock = this.stockFuturesMapper.selectFuturesByCode(code.split("_")[1]);
        if (stock == null)
            return ServerResponse.createByErrorMsg("");
        ServerResponse<MinDataVO> serverResponse = QqStockApi.getQqStockDayK(stock);
        MinDataVO minDataVO = (MinDataVO) serverResponse.getData();
        EchartsDataVO echartsDataVO = SinaStockApi.assembleEchartsDataVO(minDataVO);
        return ServerResponse.createBySuccess(echartsDataVO);
    }

    /*指數日線-K線*/
    public ServerResponse getIndexDayK(String code) {
        if (StringUtils.isBlank(code))
            return ServerResponse.createByErrorMsg("");
        StockIndex stock = this.stockIndexMapper.selectIndexByCode(code.replace("sh", "").replace("sz", ""));
        if (stock == null)
            return ServerResponse.createByErrorMsg("");
        ServerResponse<MinDataVO> serverResponse = QqStockApi.getQqIndexDayK(stock);
        MinDataVO minDataVO = (MinDataVO) serverResponse.getData();
        EchartsDataVO echartsDataVO = SinaStockApi.assembleEchartsDataVO(minDataVO);
        return ServerResponse.createBySuccess(echartsDataVO);
    }

    public ServerResponse<Stock> findStockByName(String name) {
        return ServerResponse.createBySuccess(this.stockMapper.findStockByName(name));
    }

    public ServerResponse<Stock> findStockByCode(String code) {


        List<Stock> stockByCode = this.stockMapper.findStockByCode(code);
        Stock stock = null;
        if (stockByCode.size()>0) {
            stock =stockByCode.get(0);
        }
        return ServerResponse.createBySuccess(stock);
    }

    public ServerResponse<Stock> findStockById(Integer stockId) {
        return ServerResponse.createBySuccess(this.stockMapper.selectByPrimaryKey(stockId));
    }

    public ServerResponse<PageInfo> listByAdmin(Integer showState, Integer lockState, String code, String name, String stockPlate, String stockType,Integer riseWhite, int pageNum, int pageSize, HttpServletRequest request) {
        PageHelper.startPage(pageNum, pageSize);
        List<Stock> stockList = this.stockMapper.listByAdmin(showState, lockState, code, name, stockPlate, stockType,riseWhite);
        List<StockAdminListVO> stockAdminListVOS = Lists.newArrayList();
        for (Stock stock : stockList) {
            StockAdminListVO stockAdminListVO = assembleStockAdminListVO(stock);

            stockAdminListVOS.add(stockAdminListVO);
        }
        PageInfo pageInfo = new PageInfo(stockList);
        pageInfo.setList(stockAdminListVOS);
        return ServerResponse.createBySuccess(pageInfo);
    }

    private StockAdminListVO assembleStockAdminListVO(Stock stock) {
        StockAdminListVO stockAdminListVO = new StockAdminListVO();
        stockAdminListVO.setId(stock.getId());
        stockAdminListVO.setStockName(stock.getStockName());
        stockAdminListVO.setStockCode(stock.getStockCode());
        stockAdminListVO.setStockSpell(stock.getStockSpell());
        stockAdminListVO.setStockType(stock.getStockType());
        stockAdminListVO.setStockGid(stock.getStockGid());
        stockAdminListVO.setStockPlate(stock.getStockPlate());
        stockAdminListVO.setRiseWhite(stock.getRiseWhite());
        stockAdminListVO.setIsLock(stock.getIsLock());
        stockAdminListVO.setIsShow(stock.getIsShow());
        stockAdminListVO.setAddTime(stock.getAddTime());
        ArrayList<String> objects = new ArrayList<>();
        objects.add(stock.getStockCode());
        List<StockListVO> stockListVOs = SinaStockApi.assembleStockListVO(SinaStockApi.getSinaStock(objects),objects);
        StockListVO stockListVO = stockListVOs.get(0);
        stockAdminListVO.setNowPrice(stockListVO.getNowPrice());
        stockAdminListVO.setHcrate(stockListVO.getHcrate());
        stockAdminListVO.setSpreadRate(stock.getSpreadRate());
        ServerResponse serverResponse = selectRateByDaysAndStockCode(stock.getStockCode(), 3);
        BigDecimal day3Rate = new BigDecimal("0");
        if (serverResponse.isSuccess())
            day3Rate = (BigDecimal) serverResponse.getData();
        stockAdminListVO.setDay3Rate(day3Rate);
        return stockAdminListVO;
    }

    public ServerResponse updateLock(Integer stockId) {
        Stock stock = this.stockMapper.selectByPrimaryKey(stockId);
        if (stock == null)
            return ServerResponse.createByErrorMsg("");
        if (stock.getIsLock().intValue() == 1) {
            stock.setIsLock(Integer.valueOf(0));
        } else {
            stock.setIsLock(Integer.valueOf(1));
        }
        int updateCount = this.stockMapper.updateByPrimaryKeySelective(stock);
        if (updateCount > 0)
            return ServerResponse.createBySuccessMsg("");
        return ServerResponse.createByErrorMsg("");
    }

    public ServerResponse updateShow(Integer stockId) {
        Stock stock = this.stockMapper.selectByPrimaryKey(stockId);
        if (stock == null)
            return ServerResponse.createByErrorMsg("");
        if (stock.getIsShow().intValue() == 0) {
            stock.setIsShow(Integer.valueOf(1));
        } else {
            stock.setIsShow(Integer.valueOf(0));
        }
        int updateCount = this.stockMapper.updateByPrimaryKeySelective(stock);
        if (updateCount > 0)
            return ServerResponse.createBySuccessMsg("");
        return ServerResponse.createByErrorMsg("");
    }

    public ServerResponse addStock(String stockName, String stockCode, String stockType, String stockPlate, Integer isLock, Integer isShow) {
        if (StringUtils.isBlank(stockName) || StringUtils.isBlank(stockCode) || StringUtils.isBlank(stockType) || isLock == null || isShow == null)
            return ServerResponse.createByErrorMsg("");
        Stock cstock = (Stock) findStockByCode(stockCode).getData();
        if (cstock != null)
            return ServerResponse.createByErrorMsg("");
        Stock nstock = (Stock) findStockByName(stockName).getData();
        if (nstock != null)
            return ServerResponse.createByErrorMsg("");
        Stock stock = new Stock();
        stock.setStockName(stockName);
        stock.setStockCode(stockCode);
        stock.setStockSpell(GetPyByChinese.converterToFirstSpell(stockName));
        stock.setStockType(stockType);
        stock.setStockGid(stockType + stockCode);
        stock.setIsLock(isLock);
        stock.setIsShow(isShow);
        stock.setAddTime(new Date());
        if (stockPlate != null)
            stock.setStockPlate(stockPlate);

        if (stockPlate != null && StringUtils.isNotEmpty(stockPlate) && stockCode.startsWith("300"))
            stock.setStockPlate("創業");
        else if (stockPlate != null && StringUtils.isNotEmpty(stockPlate) && stockCode.startsWith("688"))
            stock.setStockPlate("科創");
        else
            stock.setStockPlate(null);

        int insertCount = this.stockMapper.insert(stock);
        if (insertCount > 0)
            return ServerResponse.createBySuccessMsg("");
        return ServerResponse.createByErrorMsg("");
    }

    public int CountStockNum() {
        return this.stockMapper.CountStockNum();
    }

    public int CountShowNum(Integer showState) {
        return this.stockMapper.CountShowNum(showState);
    }

    public int CountUnLockNum(Integer lockState) {
        return this.stockMapper.CountUnLockNum(lockState);
    }

    public List findStockList() {
        return this.stockMapper.findStockList();
    }

    public ServerResponse selectRateByDaysAndStockCode(String stockCode, int days) {

        List<Stock> stockByCode = this.stockMapper.findStockByCode(stockCode);
        Stock stock = null;
        if (stockByCode.size()>0) {
            stock =stockByCode.get(0);
        }
        if (stock == null)
            return ServerResponse.createByErrorMsg("");
        BigDecimal daysRate = this.iStockMarketsDayService.selectRateByDaysAndStockCode(stock.getId(), days);
        return ServerResponse.createBySuccess(daysRate);
    }

    public ServerResponse updateStock(Stock model) {
        if (StringUtils.isBlank(model.getId().toString()) || StringUtils.isBlank(model.getStockName()))
            return ServerResponse.createByErrorMsg("");
        Stock stock = this.stockMapper.selectByPrimaryKey(model.getId());
        if (stock == null)
            return ServerResponse.createByErrorMsg("");
        stock.setStockName(model.getStockName());
        if (model.getSpreadRate() != null)
            stock.setSpreadRate(model.getSpreadRate());
        int updateCount = this.stockMapper.updateByPrimaryKeySelective(stock);
        if (updateCount > 0)
            return ServerResponse.createBySuccessMsg("");
        return ServerResponse.createByErrorMsg("");
    }

    public ServerResponse deleteByPrimaryKey(Integer id) {
        int updateCount = this.stockMapper.deleteByPrimaryKey(id);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMsg("操作成功");
        }
        return ServerResponse.createByErrorMsg("操作失敗");
    }

    public ServerResponse getTwMarket() {


        String TwMarket_url = PropertiesUtil.getProperty("sina.TwMarket.list.url");
        String result = null;
        try {
            result = HttpClientRequest.doGet(TwMarket_url);
        } catch (Exception e) {
            log.error("e = {}", e);
        }

//    com.alibaba.fastjson.JSONArray jsonArray = JSON.parseArray(result);
        com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result);
//    String[] TwMarketArray = result.split(",");
        List<TwMarketVO> TwMarketVOS = Lists.newArrayList();
//    for (int i = 0; i < TwMarketArray.length; i++) {

//    com.alibaba.fastjson.JSONObject Twhqstr = jsonArray.getJSONObject(i);
        String Twhqstr = jsonObject.getString("rows");
        com.alibaba.fastjson.JSONArray jsonArray = JSON.parseArray(Twhqstr);

        for (int i = 0; i < jsonArray.size(); i++) {
//      String hqstr = jsonArray[i];

            try {
                if (Twhqstr != null) {
//          Twhqstr = Twhqstr.substring(Twhqstr.indexOf("\"") + 1, Twhqstr.lastIndexOf("\""));
                    TwMarketVO marketVO = new TwMarketVO();
//          String regularMarketTime = Twhqstr.getString("regularMarketTime");
//          DateTimeFormatter dtf1 = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:SSSZ");
//          DateTime dt= dtf1.parseDateTime(regularMarketTime);
//          DateTimeFormatter dtf2= DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
//          String[] sh01_arr = Twhqstr.split(",");
//          marketVO.setRegularMarketTime(dt.toString(dtf2));
                    marketVO.setName(jsonArray.getJSONObject(0).getString("name"));
                    marketVO.setType(jsonArray.getJSONObject(0).getString("type"));
                    marketVO.setExchange(jsonArray.getJSONObject(0).getString("exchange"));
                    TwMarketVOS.add(marketVO);

                }
            } catch (Exception e) {
                log.error("str = {} ,  e = {}", Twhqstr, e);
            }
        }
//    JSONObject jsonObj = JSONObject.fromObject(result);

//    String regularMarketTime = result.getString("regularMarketTime");
//    String price = jsonObj.getString("price");
//    TwMarketVO TwMarketVO = new TwMarketVO();
//    TwMarketVO.setregularMarketTime = regularMarketTime;
//    TwMarketVO.setprice = price;

//    TwMarketVOResult TwMarketVOResult = new TwMarketVOResult();
        TwMarketVOResult TwmarketVOResult = new TwMarketVOResult();
        TwmarketVOResult.setTwMarket(TwMarketVOS);
        return ServerResponse.createBySuccess(TwmarketVOResult);
    }

    @Override
    public ServerResponse syncStockType(String exchange) {

        List<StockType> stockTypeList = new ArrayList<>();
        String StockType_url = PropertiesUtil.getProperty("sina.StockType.list.url");
        StockType_url = StockType_url + exchange;
        String result = null;
        try {
            result = HttpClientRequest.doGet(StockType_url);
        } catch (Exception e) {
            log.error("e = {}", e);
        }

        if (result == null || result.equals("")) {
            return ServerResponse.createByError();
        }

        com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result);
        if (jsonObject == null) {
            return ServerResponse.createByError();
        }
        com.alibaba.fastjson.JSONArray rows = jsonObject.getJSONArray("rows");
        if (rows == null || rows.size() == 0) {
            return ServerResponse.createByError();
        }
        for (int i = 0; i < rows.size(); i++) {
            com.alibaba.fastjson.JSONObject object = rows.getJSONObject(i);
            Long count = stockTypeMapper.selectCount(new QueryWrapper<StockType>().eq("type", object.getInteger("type")).eq("exchange", exchange));
            if (count > 0) {
                continue;
            }
            StockType stockType = new StockType();
            stockType.setType(object.getInteger("type"));
            stockType.setExchange(exchange);
            stockType.setName(object.getString("name"));
            stockType.setCreateTime(new Date());
            stockTypeMapper.insert(stockType);
            stockTypeList.add(stockType);
        }
        stockTypeList =    stockTypeMapper.selectList(new QueryWrapper<StockType>().eq("exchange", exchange));
        for (StockType stockType : stockTypeList) {
            String stockCategory_url = PropertiesUtil.getProperty("sina.StockCategory.list.url");
            stockCategory_url = stockCategory_url + "?type=" + stockType.getType() + "&exchange=" + exchange + "&pageNum=1&pageSize=200";
            result = null;
            try {
                result = HttpClientRequest.doGet(stockCategory_url);
            } catch (Exception e) {
                log.error("e = {}", e);
            }

            if (result == null || result.equals("")) {
                log.error("無股票"+result);
                return ServerResponse.createByError();
            }

            jsonObject = JSON.parseObject(result);
            if (jsonObject == null) {
                log.error("無細節"+jsonObject);

                return ServerResponse.createByError();
            }
            rows = jsonObject.getJSONArray("rows");
            if (rows == null || rows.size() == 0) {
                return ServerResponse.createByError();
            }
            for (int i = 0; i < rows.size(); i++) {
                com.alibaba.fastjson.JSONObject object = rows.getJSONObject(i);
                com.alibaba.fastjson.JSONArray stock = object.getJSONArray("stock");

                log.info("stock_type_code"+stockType.getType()+"exchange"+exchange+"stock_code"+stock.getString(1));
                Long selectCount = newStockMapper.selectCount(new QueryWrapper<Stock>().eq("stock_type_code", stockType.getType()).eq("exchange", exchange).eq("stock_code_exchange", stock.getString(1)));
                if (selectCount > 0) {
                    log.error("股票重複");
                    continue;
                }


                String stockDetailUrl = PropertiesUtil.getProperty("sina.TwMarket.url");
                stockDetailUrl = stockDetailUrl + stock.getString(1);
                try {
                    result = HttpClientRequest.doGet(stockDetailUrl);
                } catch (Exception e) {

                    log.error("e = {}", e);
                }

                if (result == null || result.equals("")) {
                    log.error("明細請求為空");

                    return ServerResponse.createByError();
                }
                System.out.println(result);
                com.alibaba.fastjson.JSONArray stockDetailArray = JSON.parseArray(result);
                if (stockDetailArray.size() == 0){
                    continue;
                }
                com.alibaba.fastjson.JSONObject stockDetailObject = stockDetailArray.getJSONObject(0);

                Stock createStock = new Stock();

                //{
                //        "ask":"104.5",
                //        "bid":"103.5",
                //        "change":"-1.5",
                //        "changePercent":"-1.45%",
                //        "changeStatus":"down",
                //        "exchange":"TWO",
                //        "holdingType":"EQUITY",
                //        "limitDown":false,
                //        "limitDownPrice":"93.2",
                //        "limitUp":false,
                //        "limitUpPrice":"113.5",
                //        "marketStatus":"close",
                //        "messageBoardId":"finmb_9755641_lang_zh",
                //        "previousVolume":"12000",
                //        "price":"102.0",
                //        "regularMarketDayHigh":"103.5",
                //        "regularMarketDayLow":"102.0",
                //        "regularMarketOpen":"103.5",
                //        "regularMarketPreviousClose":"103.5",
                //        "regularMarketTime":"2022-07-19T03:33:48Z",
                //        "relatedSymbol":null,
                //        "relatedSymbolName":null,
                //        "sectorId":"122",
                //        "sectorName":"櫃食品",
                //        "symbol":"4205.TWO",
                //        "symbolName":"中華食",
                //        "systexId":"4205",
                //        "turnoverM":"0.307",
                //        "volume":"3000",
                //        "currency":"TWD",
                //        "epsTrailingTwelveMonths":null,
                //        "exchangeDataDelayedBy":null,
                //        "fiftyTwoWeekHigh":null,
                //        "fiftyTwoWeekLow":null,
                //        "marketCap":null,
                //        "marketState":null,
                //        "trailingPE":null,
                //        "previousVolumeK":12,
                //        "volumeK":3
                //    }
                createStock.setStockName(stockDetailObject.getString("symbolName"));
                createStock.setStockCodeExchange(stockDetailObject.getString("symbol"));
                createStock.setStockCode(stockDetailObject.getString("systexId"));
                createStock.setStockType("sh");
                createStock.setStockGid( stock.getString(1));
                createStock.setIsLock(0);
                createStock.setIsShow(0);
                createStock.setAddTime(new Date());
                createStock.setSpreadRate(new BigDecimal("0"));
                createStock.setStockTypeCode(stockType.getType());
                createStock.setStockTypeStr(stockType.getName());
                createStock.setExchange(exchange);
                newStockMapper.insert(createStock);
            }
        }


        return ServerResponse.createBySuccess(rows);
    }

    @Override
    public ServerResponse getStockType(String exchange) {
        QueryWrapper<StockType> wrapper = new QueryWrapper<>();
        if (exchange != null && (!"".equals(exchange))) {
            wrapper.eq("exchange", exchange);
        }
        wrapper.orderByDesc("id");
        List<StockType> stockTypeList = stockTypeMapper.selectList(wrapper);
        return ServerResponse.createBySuccess(stockTypeList);
    }

    @Override
    public ServerResponse updateRiseWith(Integer id) {
        Stock stock = this.stockMapper.selectByPrimaryKey(id);
        if (stock == null) {
            return ServerResponse.createByErrorMsg("股票不存在");
        }
        if (stock.getRiseWhite() == 1) {
            stock.setRiseWhite(0);
        } else {
            stock.setRiseWhite(1);
        }
        this.stockMapper.updateById(stock);
        return ServerResponse.createBySuccess("修改成功");
    }


}
