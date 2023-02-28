package com.stock.mx2.utils.stock.qq;

import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.Stock;
import com.stock.mx2.pojo.StockFutures;
import com.stock.mx2.pojo.StockIndex;
import com.stock.mx2.utils.HttpClientRequest;
import com.stock.mx2.utils.PropertiesUtil;
import com.stock.mx2.utils.redis.JsonUtil;
import com.stock.mx2.utils.stock.sina.SinaStockApi;
import com.stock.mx2.utils.stock.sina.vo.SinaStockMinData;
import com.stock.mx2.vo.stock.k.MinDataVO;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QqStockApi {
    public static final String qq_url = PropertiesUtil.getProperty("qq.k.min.url");
    private static final Logger log = LoggerFactory.getLogger(SinaStockApi.class);

    public static ServerResponse<MinDataVO> getGpStockDayK(Stock stock) {
        String minUrl = PropertiesUtil.getProperty("qq.k.min.url");
        minUrl = minUrl.replace("code",stock.getStockGid());

        String hqstr = "";
        try {
            hqstr = HttpClientRequest.doGet(minUrl);
        } catch (Exception e) {
            log.error("獲取股票K線分時圖出錯，錯誤信息 = {}", e);
        }

        log.info(" qq-code = {} ", stock.getStockGid());
        if (StringUtils.isBlank(hqstr)) {
            return ServerResponse.createByErrorMsg("沒有查詢到行情數據");
        }
        String qqstr = hqstr.split("=")[1].replace("\";","").replace("\\n\\",",").replace("\n","").replace("\"","");
        String[] liststr = qqstr.split(",");
        List<SinaStockMinData> list = new ArrayList<>();
        for (int i = 1; i<liststr.length; i++){
            String[] dayarry = liststr[i].split(" ");
            SinaStockMinData model = new SinaStockMinData();
            model.setDay("20"+dayarry[0].substring(0,2)+"-"+dayarry[0].substring(2,4)+"-"+dayarry[0].substring(4,6));
            model.setOpen(dayarry[1]);
            model.setHigh(dayarry[3]);
            model.setLow(dayarry[4]);
            model.setClose(dayarry[2]);
            model.setMa_price(new BigDecimal("0").doubleValue());
            model.setMa_volume(dayarry[5]);
            model.setVolume(dayarry[5]);
            list.add(model);
        }
        int size = Integer.valueOf(PropertiesUtil.getProperty("qq.k.min.max.size"));
        if(list.size()>size){
            list = list.subList((list.size()-size-1),list.size());
        }
        MinDataVO minDataVO = new MinDataVO();
        minDataVO.setStockName(stock.getStockName());
        minDataVO.setStockCode(stock.getStockCode());
        minDataVO.setGid(stock.getStockGid());
        minDataVO.setData(list);
        return ServerResponse.createBySuccess(minDataVO);
    }

    public static ServerResponse<MinDataVO> getQqStockDayK(StockFutures stock) {
        String minUrl = PropertiesUtil.getProperty("sina.futures.day.min.url");
        minUrl = minUrl.replace("{code}",stock.getFuturesCode());
        SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd");//設置日期格式
        minUrl = minUrl.replace("{date}",df.format(new Date()));


        String hqstr = "";
        try {
            hqstr = HttpClientRequest.doGet(minUrl);
        } catch (Exception e) {
            log.error("獲取股票K線分時圖出錯，錯誤信息 = {}", e);
        }

        log.info(" 期貨日線-code = {} ", stock.getFuturesGid());
        if (StringUtils.isBlank(hqstr)) {
            return ServerResponse.createByErrorMsg("沒有查詢到行情數據");
        }
        hqstr = hqstr.split("\\(")[1].replace(");","");
        hqstr = hqstr.replaceAll("date","day");
        hqstr = hqstr.replaceAll("\"\"", "\"");

        List<SinaStockMinData> list = (List<SinaStockMinData>) JsonUtil.string2Obj(hqstr, new TypeReference<List<SinaStockMinData>>(){});
        int size = Integer.valueOf(PropertiesUtil.getProperty("sina.futures.day.min.max.size"));
        if(list.size()>size){
            list = list.subList((list.size()-size-1),list.size());
        }

        MinDataVO minDataVO = new MinDataVO();
        minDataVO.setStockName(stock.getFuturesName());
        minDataVO.setStockCode(stock.getFuturesCode());
        minDataVO.setGid(stock.getFuturesGid());
        minDataVO.setData(list);
        return ServerResponse.createBySuccess(minDataVO);
    }

    /*指數日線*/
    public static ServerResponse<MinDataVO> getQqIndexDayK(StockIndex stock) {
        String minUrl = PropertiesUtil.getProperty("sina.index.day.min.url");
        minUrl = minUrl.replace("{code}",stock.getIndexGid());
        SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd");//設置日期格式
        minUrl = minUrl.replace("{date}",df.format(new Date()));


        String hqstr = "";
        try {
            hqstr = HttpClientRequest.doGet(minUrl);
        } catch (Exception e) {
            log.error("獲取股票K線分時圖出錯，錯誤信息 = {}", e);
        }

        log.info(" 指數日線-code = {} ", stock.getIndexGid());
        if (StringUtils.isBlank(hqstr)) {
            return ServerResponse.createByErrorMsg("沒有查詢到行情數據");
        }
        hqstr = hqstr.split(":\\[\\[")[1];
        hqstr = hqstr.split("]]")[0].replace("],[",";");
        String[] liststr = hqstr.split(";");
        List<SinaStockMinData> list = new ArrayList<>();
        for (int i = 1; i<liststr.length; i++){
            String[] dayarry = liststr[i].split(",");
            SinaStockMinData model = new SinaStockMinData();
            model.setDay(filterCode(dayarry[0]));
            model.setOpen(filterCode(dayarry[1]));
            model.setHigh(filterCode(dayarry[3]));
            model.setLow(filterCode(dayarry[4]));
            model.setClose(filterCode(dayarry[2]));
            model.setMa_price(new BigDecimal("0").doubleValue());
            model.setMa_volume(filterCode(dayarry[5].split("\\.")[0]));
            model.setVolume(filterCode(dayarry[5].split("\\.")[0]));
            list.add(model);
        }

        MinDataVO minDataVO = new MinDataVO();
        minDataVO.setStockName(stock.getIndexName());
        minDataVO.setStockCode(stock.getIndexCode());
        minDataVO.setGid(stock.getIndexGid());
        minDataVO.setData(list);
        return ServerResponse.createBySuccess(minDataVO);
    }

    /**
     * 使用java正則表達式去掉多餘的.與0
     * @param s
     * @return
     */
    public static String subZeroAndDot(String s){
        BigDecimal value = new BigDecimal(s);
        BigDecimal noZeros = value.stripTrailingZeros();
        String result = noZeros.toPlainString();
        return result;
    }

    private static String filterCode(String str) {
        return str.replace("\"","");
    }

    /*股票月線、周線
     * type:month=月，week=周
     * */
    public static ServerResponse<MinDataVO> getGpStockMonthK(Stock stock,String type) {
        String minUrl = PropertiesUtil.getProperty("qq.month.min.url");
        minUrl = minUrl.replace("sz300750",stock.getStockGid());
        minUrl = minUrl.replace("month",type);

        String hqstr = "";
        try {
            hqstr = HttpClientRequest.doGet(minUrl);
            hqstr = hqstr.replace("qfqday","day");
        } catch (Exception e) {
            log.error("獲取股票K線分時圖出錯，錯誤信息 = {}", e);
        }

        log.info(" qq-code = {} ", stock.getStockGid());
        if (StringUtils.isBlank(hqstr)) {
            return ServerResponse.createByErrorMsg("沒有查詢到行情數據");
        }
        //String qqstr = hqstr.split("=")[1];
        JSONObject json = JSONObject.fromObject(hqstr);
        JSONObject data = json.getJSONObject("data");
        JSONObject listjson = data.getJSONObject(stock.getStockGid());
        JSONArray jsonArray = listjson.getJSONArray(type);

        List<SinaStockMinData> list = new ArrayList<>();
        for (int i = 0; i<jsonArray.size(); i++){
            String string = jsonArray.getString(i).replace("\"","").replace("[","").replace("]","");
            String[] dayarry = string.split(",");
            SinaStockMinData model = new SinaStockMinData();
            model.setDay(dayarry[0]);
            model.setOpen(dayarry[1]);
            model.setHigh(dayarry[3]);
            model.setLow(dayarry[4]);
            model.setClose(dayarry[2]);
            model.setMa_price(new BigDecimal("0").doubleValue());
            model.setMa_volume(dayarry[5]);
            model.setVolume(dayarry[5]);
            list.add(model);
        }

        if(list.size()>50){
            list = list.subList((list.size()-50),list.size());
        }

        MinDataVO minDataVO = new MinDataVO();
        minDataVO.setStockName(stock.getStockName());
        minDataVO.setStockCode(stock.getStockCode());
        minDataVO.setGid(stock.getStockGid());
        minDataVO.setData(list);
        return ServerResponse.createBySuccess(minDataVO);
    }


}
