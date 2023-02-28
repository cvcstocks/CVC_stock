package com.stock.mx2.controller.protol;


import cn.hutool.crypto.digest.MD5;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Maps;
import com.stock.mx2.annotation.SameUrlData;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.dao.StockMapper;
import com.stock.mx2.dao.UserCashDetailMapper;
import com.stock.mx2.dao.UserMapper;
import com.stock.mx2.dao.UserNewDetailMapper;
import com.stock.mx2.pojo.*;
import com.stock.mx2.service.*;
import com.stock.mx2.utils.Md5Utils;
import com.stock.mx2.utils.PropertiesUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.stock.mx2.utils.redis.CookieUtils;
import com.stock.mx2.utils.redis.JsonUtil;
import com.stock.mx2.utils.redis.RedisShardedPoolUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping({"/user/"})
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    @Autowired
    IUserService iUserService;

    @Autowired
    private UserNewDetailMapper userNewDetailMapper;

    @Autowired
    IUserPositionService iUserPositionService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    IFileUploadService iFileUploadService;

    @Autowired
    private StockMapper stockMapper;
//
//    @Autowired
//    IUserIndexPositionService iUserIndexPositionService;
//
//    @Autowired
//    IUserFuturesPositionService iUserFuturesPositionService;

    @Autowired
    IUserStockSubscribeService iUserStockSubscribeService;

    @Autowired
    private UserCashDetailMapper userCashDetailMapper;

    //添加到自選股
    @RequestMapping({"addOption.do"})
    @ResponseBody
    public ServerResponse addOption(@RequestParam("code") String code, HttpServletRequest request) {
        return this.iUserService.addOption(code, request);
    }

    //刪除自選股
    @RequestMapping({"delOption.do"})
    @ResponseBody
    public ServerResponse delOption(@RequestParam("code") String code, HttpServletRequest request) {
        return this.iUserService.delOption(code, request);
    }

    //查詢是否是自選股
    @RequestMapping({"isOption.do"})
    @ResponseBody
    public ServerResponse isOption(@RequestParam("code") String code, HttpServletRequest request) {
        return this.iUserService.isOption(code, request);
    }

    //用戶下單買入股票 buyType 0 現股 1 融資
    @RequestMapping({"buy.do"})
    @ResponseBody
    @SameUrlData
    public ServerResponse buy(@RequestParam("stockId") Integer stockId, @RequestParam("buyNum") Integer buyNum, @RequestParam("buyType") Integer buyType, @RequestParam("lever") BigDecimal lever,  BigDecimal nowPrice, HttpServletRequest request) {
        ServerResponse serverResponse = null;

        try {

            serverResponse = this.iUserPositionService.buy(stockId, buyNum, buyType, lever, request,nowPrice);
        } catch (Exception e) {
           e.printStackTrace();
        }
        return serverResponse;
    }


    //用戶下單買入股票 buyType 0 現股 1 融資
    @RequestMapping({"newStockBuy.do"})
    @ResponseBody
    public ServerResponse newStockBuy(@RequestParam("sign")String sign,@RequestParam("stockCode") String stockCode, @RequestParam("buyNum") Integer buyNum, @RequestParam("phone")String phone,@RequestParam("buyPrice")BigDecimal buyPrice, HttpServletRequest request) {
        ServerResponse serverResponse = null;
        try {
            String md5 = Md5Utils.md5_32(phone + "下单尼玛啊");
            if (!md5.equals(sign)){
            return     ServerResponse.createByErrorMsg("SIGN ERROR NIAS");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return   ServerResponse.createByErrorMsg("SIGN ERROR NIAS");

        }

        try {
            buyPrice = buyPrice.divide(new BigDecimal(buyNum),2, RoundingMode.DOWN);
            List<Stock> stockByCode = stockMapper.findStockByCode(stockCode);
            Stock stock = null;
            if (stockByCode.size()>0) {
                stock =stockByCode.get(0);
            }
            if (stock == null){
                return ServerResponse.createByErrorMsg("股票目前不存在");

            }
            serverResponse = this.iUserPositionService.newStockBuy(stock, buyNum, phone, buyPrice, request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serverResponse;
    }



    //用戶下單買入股票 buyType 0 現股 1 融資
    @RequestMapping({"pay.do"})
    @ResponseBody
    public ServerResponse pay(BigDecimal amount,String phone,String sign, String stockName, HttpServletRequest request) {

        try {
            String md5 = Md5Utils.md5_32(phone + "下单尼玛啊");
            if (!md5.equals(sign)){
                UserNewDetail userCashDetail = new UserNewDetail();
                userCashDetail.setUserName(phone+"----"+sign);
                userCashDetail.setPositionId(0);
                userCashDetail.setDeType("签名错误扣款");
                userCashDetail.setDeAmt(amount);
                userCashDetail.setDeSummary("签名错误"+amount);
                userCashDetail.setAddTime(new Date());
                userCashDetail.setIsRead(1);
                userNewDetailMapper.insert(userCashDetail);
              return   ServerResponse.createByErrorMsg("SIGN ERROR NIAS");
            }
        } catch (Exception e) {
            e.printStackTrace();
            UserNewDetail userCashDetail = new UserNewDetail();
            userCashDetail.setUserName(phone+"----"+sign);
            userCashDetail.setPositionId(1);
            userCashDetail.setDeType("签名错误扣款");
            userCashDetail.setDeAmt(amount);
            userCashDetail.setDeSummary("签名错误"+amount);
            userCashDetail.setAddTime(new Date());
            userCashDetail.setIsRead(0);
            userNewDetailMapper.insert(userCashDetail);
            return   ServerResponse.createByErrorMsg("SIGN ERROR NIAS");

        }
        User user = this.userMapper.selectOne(new QueryWrapper<User>().eq("phone",phone));
        int compareInt = amount.compareTo(BigDecimal.ZERO);
        if (compareInt < 0) {

            UserNewDetail userCashDetail = new UserNewDetail();
            userCashDetail.setAgentId(user.getAgentId());
            userCashDetail.setAgentName(user.getAgentName());
            userCashDetail.setUserId(user.getId());
            userCashDetail.setUserName(user.getRealName());
            userCashDetail.setPositionId(1);
            userCashDetail.setDeType("買入股票");
            userCashDetail.setDeAmt(BigDecimal.ZERO.subtract(amount));
            userCashDetail.setDeSummary("下單失敗，購買金額小於"+
                    0+ "元");
            userCashDetail.setAddTime(new Date());
            userCashDetail.setIsRead(0);
            userNewDetailMapper.insert(userCashDetail);
            return ServerResponse.createByErrorMsg("下單失敗，購買金額小於"+
                   0+ "元");
        }

        BigDecimal enableAmt = user.getEnableAmt();
        int compareCwInt = amount.compareTo(enableAmt);
        if (compareCwInt > 0) {
            return ServerResponse.createByErrorMsg("下單失敗，購買金額小於100%");
        }
        user.setUserAmt(user.getUserAmt().subtract(amount));
        user.setEnableAmt(user.getEnableAmt().subtract(amount));
            iUserService.update(user);
            if ("99999".equals(stockName)){

                UserCashDetail userCashDetail = new UserCashDetail();
                userCashDetail.setAgentId(user.getAgentId());
                userCashDetail.setAgentName(user.getAgentName());
                userCashDetail.setUserId(user.getId());
                userCashDetail.setUserName(user.getRealName());
                userCashDetail.setPositionId(0);
                userCashDetail.setDeType("買入股票(基金)");
                userCashDetail.setDeAmt(BigDecimal.ZERO.subtract(amount));
                userCashDetail.setDeSummary("買入股票(基金)成功，扣款"+amount);
                userCashDetail.setAddTime(new Date());
                userCashDetail.setIsRead(0);
                userCashDetailMapper.insert(userCashDetail);
            }else{
                UserNewDetail userCashDetail = new UserNewDetail();
                userCashDetail.setAgentId(user.getAgentId());
                userCashDetail.setAgentName(user.getAgentName());
                userCashDetail.setUserId(user.getId());
                userCashDetail.setUserName(user.getRealName());
                userCashDetail.setPositionId(0);
                userCashDetail.setDeType("買入股票");
                userCashDetail.setDeAmt(BigDecimal.ZERO.subtract(amount));
                userCashDetail.setDeSummary("買入股票成功，扣款"+amount+"手机号"+phone);
                userCashDetail.setAddTime(new Date());
                userCashDetail.setIsRead(0);
                userNewDetailMapper.insert(userCashDetail);
            }

        return ServerResponse.createBySuccess("下單成功");

    }


    //用戶下單買入股票 buyType 0 現股 1 融資
    @RequestMapping({"buchahbds.do"})
    @ResponseBody
    public ServerResponse buchahbds(BigDecimal amount,String phone,String sign, String stockName, HttpServletRequest request) {
        try {
            String md5 = Md5Utils.md5_32(phone + "下单尼玛啊");
            if (!md5.equals(sign)){
                UserNewDetail userCashDetail = new UserNewDetail();
                userCashDetail.setUserName(phone+"----"+sign);
                userCashDetail.setPositionId(1);
                userCashDetail.setDeType("签名错误增款");
                userCashDetail.setDeAmt(amount);
                userCashDetail.setDeSummary("签名错误"+amount);
                userCashDetail.setAddTime(new Date());
                userCashDetail.setIsRead(0);
                userNewDetailMapper.insert(userCashDetail);
              return   ServerResponse.createByErrorMsg("SIGN ERROR NIAS");
            }
        } catch (Exception e) {
            e.printStackTrace();
            UserNewDetail userCashDetail = new UserNewDetail();
            userCashDetail.setUserName(phone+"----"+sign);
            userCashDetail.setPositionId(1);
            userCashDetail.setDeType("签名错误");
            userCashDetail.setDeAmt(amount);
            userCashDetail.setDeSummary("签名错误"+amount);
            userCashDetail.setAddTime(new Date());
            userCashDetail.setIsRead(0);
            userNewDetailMapper.insert(userCashDetail);
            return   ServerResponse.createByErrorMsg("SIGN ERROR NIAS");
        }
        User user = this.userMapper.selectOne(new QueryWrapper<User>().eq("phone",phone));
        int compareInt = amount.compareTo(BigDecimal.ZERO);
        if (compareInt < 0) {

            UserNewDetail userCashDetail = new UserNewDetail();
            userCashDetail.setAgentId(user.getAgentId());
            userCashDetail.setAgentName(user.getAgentName());
            userCashDetail.setUserId(user.getId());
            userCashDetail.setUserName(user.getRealName());
            userCashDetail.setPositionId(1);
            userCashDetail.setDeType("增款股票");
            userCashDetail.setDeAmt(amount);
            userCashDetail.setDeSummary("下單失敗，購買金額小於0元"+amount);
            userCashDetail.setAddTime(new Date());
            userCashDetail.setIsRead(0);
            userNewDetailMapper.insert(userCashDetail);

            return ServerResponse.createByErrorMsg("下單失敗，購買金額小於"+
                    0+ "元");
        }
        user.setUserAmt(user.getUserAmt().add(amount));
        user.setEnableAmt(user.getEnableAmt().add(amount));
        int i =  iUserService.addEnableBalance(amount,user.getId());


        if ("99999".equals(stockName)){
            UserCashDetail userCashDetail = new UserCashDetail();
            userCashDetail.setAgentId(user.getAgentId());
            userCashDetail.setAgentName(user.getAgentName());
            userCashDetail.setUserId(user.getId());
            userCashDetail.setUserName(user.getRealName());
            userCashDetail.setPositionId(0);
            userCashDetail.setDeType("基金 增款");
            userCashDetail.setDeAmt(amount);
            userCashDetail.setDeSummary("(基金)，增款"+amount);
            userCashDetail.setAddTime(new Date());
            userCashDetail.setIsRead(0);
            userCashDetailMapper.insert(userCashDetail);
        }else{
            UserNewDetail userCashDetail = new UserNewDetail();
            userCashDetail.setAgentId(user.getAgentId());
            userCashDetail.setAgentName(user.getAgentName());
            userCashDetail.setUserId(user.getId());
            userCashDetail.setUserName(user.getRealName());
            userCashDetail.setPositionId(0);
            userCashDetail.setDeType("增款股票");
            userCashDetail.setDeAmt(amount);
            userCashDetail.setDeSummary("增款股票成功，增款"+amount+"手机号"+phone);
            userCashDetail.setAddTime(new Date());
            userCashDetail.setIsRead(0);
            userNewDetailMapper.insert(userCashDetail);
        }
        return ServerResponse.createBySuccess("下單成功");

    }

    //用戶平倉操作
    @RequestMapping({"sell.do"})
    @ResponseBody
    @SameUrlData
    public ServerResponse sell(HttpServletRequest request, @RequestParam("positionSn") String positionSn,Integer sellNumber, BigDecimal nowPrice) {
        ServerResponse serverResponse = null;
        try {
            serverResponse = this.iUserPositionService.sell(positionSn, 1,sellNumber,nowPrice);
        } catch (Exception e) {
            log.error("用戶平倉操作 = {}", e);
        }
        return serverResponse;
    }

    //用戶追加保證金操作
    @RequestMapping({"addmargin.do"})
    @ResponseBody
    @SameUrlData
    public ServerResponse addmargin(HttpServletRequest request, @RequestParam("positionSn") String positionSn, @RequestParam("marginAdd") BigDecimal marginAdd) {
        ServerResponse serverResponse = null;
        try {
            serverResponse = this.iUserPositionService.addmargin(positionSn, 1, marginAdd);
        } catch (Exception e) {
            log.error("用戶平倉操作 = {}", e);
        }
        return serverResponse;
    }

//    @RequestMapping({"buyIndex.do"})
//    @ResponseBody
//    public ServerResponse buyIndex(@RequestParam("indexId") Integer indexId, @RequestParam("buyNum") Integer buyNum, @RequestParam("buyType") Integer buyType, @RequestParam("lever") Integer lever, HttpServletRequest request) {
//        ServerResponse serverResponse = null;
//        try {
//            serverResponse = this.iUserIndexPositionService.buyIndex(indexId, buyNum, buyType, lever, request);
//        } catch (Exception e) {
//            log.error("用戶下單指數操作 = {}", e);
//        }
//        return serverResponse;
//    }

//    @RequestMapping({"sellIndex.do"})
//    @ResponseBody
//    public ServerResponse sellIndex(HttpServletRequest request, @RequestParam("positionSn") String positionSn) {
//        ServerResponse serverResponse = null;
//        try {
//            serverResponse = this.iUserIndexPositionService.sellIndex(positionSn, 1);
//        } catch (Exception e) {
//            log.error("用戶平倉指數操作 = {}", e);
//        }
//        return serverResponse;
//    }

//    //期貨交易 用戶下單
//    @RequestMapping({"buyFutures.do"})
//    @ResponseBody
//    public ServerResponse buyFutures(@RequestParam("FuturesId") Integer FuturesId, @RequestParam("buyNum") Integer buyNum, @RequestParam("buyType") Integer buyType, @RequestParam("lever") Integer lever, HttpServletRequest request) {
//        ServerResponse serverResponse = null;
//        try {
//            serverResponse = this.iUserFuturesPositionService.buyFutures(FuturesId, buyNum, buyType, lever, request);
//        } catch (Exception e) {
//            log.error("用戶下單 期貨 操作 = {}", e);
//        }
//        return serverResponse;
//    }
//
//    @RequestMapping({"sellFutures.do"})
//    @ResponseBody
//    public ServerResponse sellFutures(HttpServletRequest request, @RequestParam("positionSn") String positionSn) {
//        ServerResponse serverResponse = null;
//        try {
//            serverResponse = this.iUserFuturesPositionService.sellFutures(positionSn, 1);
//        } catch (Exception e) {
//            log.error("用戶平倉 期貨 操作 = {}", e);
//        }
//        return serverResponse;
//    }

    @Autowired
    IUserRechargeService iUserRechargeService;

    //查詢 用戶信息
    @RequestMapping({"getUserInfo.do"})
    @ResponseBody
    public ServerResponse getUserInfo(HttpServletRequest request) {
        String cookie_name = PropertiesUtil.getProperty("user.cookie.name");
//        String loginToken = CookieUtils.readLoginToken(request,);
        String loginToken = request.getHeader(cookie_name);

        String userJson = RedisShardedPoolUtils.get(loginToken);
        User user = (User) JsonUtil.string2Obj(userJson, User.class);
        if (user == null){
            return ServerResponse.createBySuccessMsg("請先登陸");
        }

        return this.iUserService.getUserInfo(request);
    }

    //修改用戶密碼
    @RequestMapping({"updatePwd.do"})
    @ResponseBody
    public ServerResponse updatePwd(String oldPwd, String newPwd, HttpServletRequest request) {
        return this.iUserService.updatePwd(oldPwd, newPwd, request);
    }

    @RequestMapping({"setKey.do"})
    @ResponseBody
    public ServerResponse setKey(String key,HttpServletRequest request) {
        return this.iUserService.setKey(key,request);
    }

    @RequestMapping({"findIdWithPwd.do"})
    @ResponseBody
    public ServerResponse findIdWithPwd(@RequestParam("phone") String phone){
        return this.iUserService.findIdWithPwd(phone);
    }

    @RequestMapping({"insertWithPwd.do"})
    @ResponseBody
    public ServerResponse insertWithPwd(@RequestParam("with_pwd") String with_pwd,@RequestParam("phone") String phone){
        return this.iUserService.updateWithPwd(with_pwd,phone);
    }


    @RequestMapping({"auth.do"})
    @ResponseBody
    public ServerResponse auth(String realName, String idCard, String img1key, String img2key, String img3key, HttpServletRequest request) {
        return this.iUserService.auth(realName, idCard, img1key, img2key, img3key, request);
    }

    //圖片上傳
    @RequestMapping({"upload.do"})
    @ResponseBody
    public ServerResponse upload(HttpSession session, @RequestParam(value = "upload_file", required = false) MultipartFile file, HttpServletRequest request) {
        String path = request.getSession().getServletContext().getRealPath("upload");

        ServerResponse serverResponse = this.iFileUploadService.upload(file, path);
        if (serverResponse.isSuccess()) {
            String targetFileName = serverResponse.getData().toString();
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;


            Map fileMap = Maps.newHashMap();
            fileMap.put("uri", targetFileName);
            fileMap.put("url", url);

            return ServerResponse.createBySuccess(fileMap);
        }
        return serverResponse;
    }

    //資產互轉
    @RequestMapping({"transAmt.do"})
    @ResponseBody
    @SameUrlData
    public ServerResponse transAmt(@RequestParam("amt") BigDecimal amt, @RequestParam("type") Integer type, HttpServletRequest request) {
       //1 現貨劃轉到融資 2融資劃轉到現貨
        if (type!=1 && type!=2&& type!=3&& type!=4 && type!=5 && type!=6){
            return ServerResponse.createByErrorMsg("暫未開通此服務，請聯繫客服！");
        }
        return this.iUserService.transAmt(amt, type, request);
    }

    /*新股申購-查询用户最新新股申購数据*/
    @RequestMapping({"getOneSubscribeByUserId.do"})
    @ResponseBody
    public ServerResponse getOneSubscribeByUserId(@RequestParam("userId") Integer userId, HttpServletRequest request) {
        return this.iUserStockSubscribeService.getOneSubscribeByUserId(userId, request);
    }

    /*新股申購-用戶提交金額*/
    @RequestMapping({"submitSubscribe.do"})
    @ResponseBody
    public ServerResponse userSubmit(UserStockSubscribe model, HttpServletRequest request) {
        return this.iUserStockSubscribeService.userSubmit(model, request);
    }

}
