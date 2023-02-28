package com.stock.mx2.service.impl;


import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.dao.StockIndexMapper;
import com.stock.mx2.pojo.StockIndex;
import com.stock.mx2.pojo.User;
import com.stock.mx2.service.IStockIndexService;
import com.stock.mx2.service.IStockOptionService;
import com.stock.mx2.service.IUserService;
import com.stock.mx2.utils.HttpClientRequest;
import com.stock.mx2.utils.PropertiesUtil;
import com.stock.mx2.vo.stock.MarketVO;
import com.stock.mx2.vo.stockindex.StockIndexVO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import com.stock.mx2.vo.stockindex.TwdStockIndexVO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("iStockIndexService")
public class StockIndexServiceImpl implements IStockIndexService {
    private static final Logger log = LoggerFactory.getLogger(StockIndexServiceImpl.class);


    @Autowired
    StockIndexMapper stockIndexMapper;

    @Autowired
    IUserService iUserService;

    @Autowired
    IStockOptionService iStockOptionService;


    public ServerResponse listByAdmin(Integer homeShow, Integer listShow, Integer transState, String indexCode, String indexName, int pageNum, int pageSize, HttpServletRequest request) {
        PageHelper.startPage(pageNum, pageSize);

        List<StockIndex> stockIndexList = this.stockIndexMapper.listByAdmin(homeShow, listShow, transState, indexCode, indexName);
        List<StockIndexVO> stockIndexVOS = Lists.newArrayList();
        for (StockIndex stockIndex : stockIndexList) {
            StockIndexVO stockIndexVO = assembleStockIndexVO(stockIndex.getIndexCode());
            stockIndexVOS.add(stockIndexVO);
        }
        PageInfo pageInfo = new PageInfo(stockIndexList);
        pageInfo.setList(stockIndexVOS);

        return ServerResponse.createBySuccess(pageInfo);
    }

    private StockIndexVO assembleStockIndexVO(String stockIndexCode) {

        MarketVO marketVO = querySingleIndex(stockIndexCode);


        StockIndexVO stockIndexVO = new StockIndexVO();


        stockIndexVO.setIndexName(marketVO.getName());
        stockIndexVO.setIndexCode(stockIndexCode);
        stockIndexVO.setCurrentPoint(marketVO.getNowPrice());
        stockIndexVO.setFloatPoint(marketVO.getIncrease());
        stockIndexVO.setFloatRate(marketVO.getIncreaseRate());

        return stockIndexVO;
    }


    public ServerResponse updateIndex(StockIndex stockIndex) {
        if (stockIndex.getId() == null) {
            return ServerResponse.createByErrorMsg("修改id不能為空");
        }

        StockIndex dbindex = this.stockIndexMapper.selectByPrimaryKey(stockIndex.getId());
        dbindex.setHomeShow(stockIndex.getHomeShow());
        dbindex.setListShow(stockIndex.getListShow());
        dbindex.setTransState(stockIndex.getTransState());
        dbindex.setDepositAmt(stockIndex.getDepositAmt());
        dbindex.setTransFee(stockIndex.getTransFee());
        dbindex.setEachPoint(stockIndex.getEachPoint());
        dbindex.setMinNum(stockIndex.getMinNum());
        dbindex.setMaxNum(stockIndex.getMaxNum());

        int updateCount = this.stockIndexMapper.updateByPrimaryKey(dbindex);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMsg("修改成功");
        }
        return ServerResponse.createByErrorMsg("修改失敗");
    }


    public ServerResponse addIndex(StockIndex stockIndex) {
        log.info("name = {} code = {} gid = {}", new Object[]{stockIndex
                .getIndexName(), stockIndex.getIndexCode(), stockIndex.getIndexGid()});
        if (StringUtils.isBlank(stockIndex.getIndexName()) ||
                StringUtils.isBlank(stockIndex.getIndexCode()) ||
                StringUtils.isBlank(stockIndex.getIndexGid())) {
            return ServerResponse.createByErrorMsg("參數不能為空");
        }

        StockIndex nameIndex = this.stockIndexMapper.selectIndexByName(stockIndex.getIndexName());
        if (nameIndex != null) {
            return ServerResponse.createByErrorMsg("指數名稱已存在");
        }
        StockIndex codeIndex = this.stockIndexMapper.selectIndexByCode(stockIndex.getIndexCode());
        if (codeIndex != null) {
            return ServerResponse.createByErrorMsg("指數代碼已存在");
        }

        stockIndex.setAddTime(new Date());
        int insertCount = this.stockIndexMapper.insert(stockIndex);

        if (insertCount > 0) {
            return ServerResponse.createBySuccessMsg("添加成功");
        }
        return ServerResponse.createByErrorMsg("添加失敗");
    }

    @Override
    public ServerResponse queryHomeIndex() {
        List<String> list = new ArrayList<>();

        list.add("TWS:OTC01:INDEX");
        list.add("TWS:TSE01:INDEX");

        String s = HttpUtil.get("https://ws.api.cnyes.com/ws/api/v1/quote/quotes/TWS:TSE01:INDEX,TWS:OTC01:INDEX?column=G");
        JSONObject jsonObject = JSONObject.parseObject(s);
        JSONArray items = jsonObject.getJSONArray("data");
        List<StockIndexVO> stockIndexVOS = Lists.newArrayList();

        for (int i = 0; i < items.size(); i++) {
            JSONObject object = items.getJSONObject(i);

            for (String name : list) {
                if (object.getString("0").equals(name)){
                    StockIndexVO stockIndexVO = new StockIndexVO();
                    stockIndexVO.setIndexName(object.getString("0"));
                    stockIndexVO.setIndexCode(object.getString("200009"));
                    stockIndexVO.setCurrentPoint(object.getString("6"));
                    stockIndexVO.setFloatPoint(object.getString("11"));
                    stockIndexVO.setFloatRate(object.getString("56"));

                    try{
                        stockIndexVO.setVolume(object.getBigDecimal("800001").divide(new BigDecimal("100000000"),2, RoundingMode.HALF_UP));

                    }catch ( Exception e){

                    }

                    stockIndexVOS.add(stockIndexVO);
                }
            }
        }
        return ServerResponse.createBySuccess(stockIndexVOS);
    }


    public ServerResponse queryListIndex(HttpServletRequest request) {
        List<StockIndex> list = this.stockIndexMapper.queryListIndex();
        List<StockIndexVO> stockIndexVOS = Lists.newArrayList();
        User user = iUserService.getCurrentUser(request);
        for (StockIndex stockIndex : list) {
            StockIndexVO stockIndexVO = assembleStockIndexVO(stockIndex.getIndexCode());
            //是否添加自選
            if (user == null) {
                stockIndexVO.setIsOption("0");
            } else {
                stockIndexVO.setIsOption(iStockOptionService.isMyOption(user.getId(), stockIndex.getIndexCode()));
            }
            stockIndexVOS.add(stockIndexVO);
        }
        return ServerResponse.createBySuccess(stockIndexVOS);
    }


    public ServerResponse queryTransIndex(Integer indexId) {
        StockIndex stockIndex = this.stockIndexMapper.selectByPrimaryKey(indexId);
        if (1 == stockIndex.getTransState().intValue()) {
            return ServerResponse.createBySuccessMsg("可交易");
        }
        return ServerResponse.createByErrorMsg("不可交易");
    }


    public MarketVO querySingleIndex(String indexCode) {
        String market_url = PropertiesUtil.getProperty("sina.index.market.url");

        String result = null;
        try {
            market_url = market_url + indexCode;
            //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //System.out.print("指數請求開始，時間："+sdf.format(new Date())+"，market_url："+market_url + "\n");

            result = HttpClientRequest.doGet(market_url);
            //System.out.print("指數請求結束，時間："+sdf.format(new Date())+"，result："+result + "\n");
        } catch (Exception e) {
            log.error("獲取 大盤指數 出錯 e = {}", e);
        }


        MarketVO marketVO = null;
        try {
            if (StringUtils.isNotBlank(result)) {


                JSONObject jsonObject = JSONObject.parseObject(result);

                JSONArray data = jsonObject.getJSONArray("data");
                JSONObject object = data.getJSONObject(0);
                marketVO = new MarketVO();
                marketVO.setName(object.getString("200009"));
                marketVO.setNowPrice(object.getString("12"));
                marketVO.setIncrease(object.getString("11"));
                marketVO.setIncreaseRate(object.getString("56"));
            }
        } catch (Exception e) {
            log.error("轉換大盤指數出錯 str = {} ,  e = {}", result, e);
        }

        return marketVO;
    }


    public StockIndex selectIndexById(Integer indexId) {
        return this.stockIndexMapper.selectByPrimaryKey(indexId);
    }
}
