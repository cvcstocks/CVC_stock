package com.stock.mx2.service.impl;

import java.util.Date;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.Page;
import com.stock.mx2.dao.*;
import com.stock.mx2.pojo.*;
import com.stock.mx2.service.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.service.*;
import com.stock.mx2.utils.DateTimeUtil;
import com.stock.mx2.utils.KeyUtils;
import com.stock.mx2.utils.Pager;
import com.stock.mx2.utils.stock.BuyAndSellUtils;
import com.stock.mx2.utils.stock.GeneratePosition;
import com.stock.mx2.utils.stock.GetStayDays;
import com.stock.mx2.utils.stock.sina.SinaStockApi;
import com.stock.mx2.vo.agent.AgentIncomeVO;
import com.stock.mx2.vo.position.AdminPositionVO;
import com.stock.mx2.vo.position.AgentPositionVO;
import com.stock.mx2.vo.position.PositionProfitVO;
import com.stock.mx2.vo.position.PositionVO;
import com.stock.mx2.vo.position.UserPositionVO;
import com.stock.mx2.vo.stock.StockListVO;
import com.stock.mx2.dao.*;
import com.stock.mx2.vo.stockfutures.FuturesVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

@Service("iUserPositionService")
public class UserPositionServiceImpl implements IUserPositionService {

    private static final Logger log = LoggerFactory.getLogger(UserPositionServiceImpl.class);

    @Autowired
    UserPositionMapper userPositionMapper;
    @Autowired
    UserPositionFuturesMapper userPositionFuturesMapper;

    @Autowired
    private IStockFuturesService iStockFuturesService;

    @Autowired
    IUserService iUserService;

    @Autowired
    ISiteSettingService iSiteSettingService;

    @Autowired
    ISiteSpreadService iSiteSpreadService;

    @Autowired
    IStockService iStockService;

    @Autowired
    UserMapper userMapper;

    @Autowired
    UserCashDetailMapper userCashDetailMapper;


    @Autowired
    private UserLevelWalletService userLevelWalletService;
    @Autowired
    IAgentUserService iAgentUserService;
    @Autowired
    AgentUserMapper agentUserMapper;
    @Autowired
    SiteTaskLogMapper siteTaskLogMapper;
    @Autowired
    StockMapper stockMapper;
    @Autowired
    AgentAgencyFeeMapper agentAgencyFeeMapper;
    @Autowired
    IAgentAgencyFeeService iAgentAgencyFeeService;
    @Autowired
    ISiteProductService iSiteProductService;

    @Autowired
    FundsApplyMapper fundsApplyMapper;


    @Autowired
    private UserPositionItemMapper userPositionItemMapper;

    /**
     * @param stockId
     * @param buyNum
     * @param buyType buyType 0 現股 1 融資 融資三倍杠杆
     * @param lever
     * @param request
     * @return
     * @throws Exception
     */
    @Transactional
    @Override
    public ServerResponse buy(Integer stockId, Integer buyNum, Integer buyType, BigDecimal lever, HttpServletRequest request, @RequestParam("nowPrice") BigDecimal nowPrice) throws Exception {

        if (buyType == 0) {
            lever = BigDecimal.ONE;
        }

        // 判斷周末不能買
        Date today = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(today);
        int weekday = c.get(Calendar.DAY_OF_WEEK);
        if (weekday == 1) {
//            return ServerResponse.createByErrorMsg("非工作日，不可交易！");
        }
        if (weekday == 7) {
//            return ServerResponse.createByErrorMsg("非工作日，不可交易！");
        }
        String mMdd = DateUtil.format(new Date(), "MMdd");
        if ("0909".equals(mMdd) || "0910".equals(mMdd)) {
            return ServerResponse.createByErrorMsg("非工作日，不可交易！");
        }
        if ("1010".equals(mMdd)) {
            return ServerResponse.createByErrorMsg("非工作日，不可交易！");
        }

        if ("1231".equals(mMdd)) {
            return ServerResponse.createByErrorMsg("非工作日，不可交易！");
        }

        /*實名認證開關開啟*/
        SiteProduct siteProduct = iSiteProductService.getProductSetting();
        User user = this.iUserService.getCurrentRefreshUser(request);
        if (user == null) {
            return ServerResponse.createBySuccessMsg("請先登錄");
        }
        if (user.getIsLock() != null && user.getIsLock() == 1) {
            return ServerResponse.createByErrorMsg("您的賬號已被凍結，請聯繫客服");
        }

        if (user.getUserWalletLevel()==1){
            BigDecimal lose  = this.userPositionMapper.selectUserAllLose(user.getId());
            if (lose.compareTo(new BigDecimal(30000)) >= 0){
                return ServerResponse.createByErrorMsg("請您聯係客服激活賬戶");
            }
        }


//        if (siteProduct.getRealNameDisplay() && (StringUtils.isBlank(user.getRealName()) || StringUtils.isBlank(user.getIdCard()))) {
//            return ServerResponse.createByErrorMsg("下單失敗，請先實名認證");
//        }
        BigDecimal user_enable_amt = BigDecimal.ZERO;

        if (buyType == 0) {
            user_enable_amt = user.getEnableAmt();
            if (user.getUserWalletLevel()==1){
                user_enable_amt = user.getEnableAmt().add(user.getWithProfitWallet()).add(user.getProfitWallet()).add(user.getExperienceWallet());
            }
        } else {


            String userLever = user.getLever();
            if (userLever == null) {
                return ServerResponse.createByErrorMsg("請先開通融資賬號");
            }
            if (userLever.contains(lever.toString())) {
                user_enable_amt = user.getEnableIndexAmt();
            } else {
                return ServerResponse.createByErrorMsg("請先開通" + lever + "倍融資賬號");
            }
        }
        log.info("用戶 {} 下單，股票id = {} ，數量 = {} , 方向 = {} , 槓桿 = {}", new Object[]{user
                .getId(), stockId, buyNum, buyType, lever});
//        if (siteProduct.getRealNameDisplay() && user.getIsLock().intValue() == 1) {
//            return ServerResponse.createByErrorMsg("下單失敗，帳戶已被鎖定");
//        }
        if (siteProduct.getHolidayDisplay()) {
            return ServerResponse.createByErrorMsg("非工作日，不可交易！");
        }

        SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
        if (siteSetting == null) {
            log.error("下單出錯，網站設置表不存在");
            return ServerResponse.createByErrorMsg("下單失敗，系統設置錯誤");
        }

        String am_begin = siteSetting.getTransAmBegin();
        String am_end = siteSetting.getTransAmEnd();
        boolean am_flag = BuyAndSellUtils.isTransTime(am_begin, am_end);
        log.info("是否在上午交易時間 = {}", Boolean.valueOf(am_flag));
        if (!am_flag) {
//            return ServerResponse.createByErrorMsg("下單失敗，不在交易時段內");
        }

        Stock stock = null;
        ServerResponse stock_res = this.iStockService.findStockById(stockId);
        if (!stock_res.isSuccess()) {
            return ServerResponse.createByErrorMsg("下單失敗，股票代碼錯誤");
        }
        stock = (Stock) stock_res.getData();

        if (stock.getIsLock().intValue() != 0) {
            return ServerResponse.createByErrorMsg("下單失敗，當前股票不能交易");
        }

        List dbPosition = findPositionByStockCodeAndTimes(siteSetting.getBuySameTimes().intValue(), stock
                .getStockCode(), user.getId());
        if (dbPosition.size() >= siteSetting.getBuySameNums().intValue()) {
            return ServerResponse.createByErrorMsg("頻繁交易," + siteSetting.getBuySameTimes() + "分鐘內同一股票持倉不得超過" + siteSetting
                    .getBuySameNums() + "條");
        }

        Integer transNum = findPositionNumByTimes(siteSetting.getBuyNumTimes().intValue(), user.getId());
        if (transNum.intValue() / 100 >= siteSetting.getBuyNumLots().intValue()) {
            return ServerResponse.createByErrorMsg("頻繁交易," + siteSetting
                    .getBuyNumTimes() + "分鐘內不能超過" + siteSetting.getBuyNumLots() + "手");
        }

        if (buyNum.intValue() < siteSetting.getBuyMinNum().intValue()) {
            return ServerResponse.createByErrorMsg("下單失敗，購買數量小於" + siteSetting
                    .getBuyMinNum() + "股");
        }
        if (buyNum.intValue() > siteSetting.getBuyMaxNum().intValue()) {
            return ServerResponse.createByErrorMsg("下單失敗，購買數量大於" + siteSetting
                    .getBuyMaxNum() + "股");
        }


        System.out.println(stock.getStockGid());
        List<String> stockCodeList = new ArrayList<>();
        stockCodeList.add(stock.getStockCode());

        StockListVO stockListVO = null;
        List<StockListVO> stockListVOList = SinaStockApi.assembleStockListVO(
                SinaStockApi.getSinaStock(stockCodeList), stockCodeList);

        stockListVO = stockListVOList.get(0);

        boolean buyTime = BuyAndSellUtils.isTransTime("09:00", "09:05");

        BigDecimal now_price
                = new BigDecimal(stockListVO.getNowPrice());
        if (nowPrice != null && nowPrice.compareTo(BigDecimal.ZERO) != 0 && buyTime) {
            now_price = nowPrice;
        }
        if (nowPrice == null || nowPrice.compareTo(BigDecimal.ZERO) == 0) {
            if (buyTime) {
                BigDecimal nowPrice1 = getNowPrice(stock.getStockCode());
                if (nowPrice1 != null && nowPrice1.compareTo(BigDecimal.ZERO) != 0) {
                    now_price = nowPrice1;
                }
            }
        }


        if (now_price.compareTo(new BigDecimal("0")) == 0) {
            return ServerResponse.createByErrorMsg("報價0，請稍後再試");
        }


        double stock_crease = stockListVO.getHcrate().doubleValue();


//        BigDecimal maxRisePercent = new BigDecimal("0");
//        if (stock.getStockPlate() != null) {
//
//            maxRisePercent = new BigDecimal("0.2");
//            log.info("【科創股票】");
//        } else {
//            maxRisePercent = new BigDecimal("0.1");
//            log.info("【普通A股】");
//        }

        if (stockListVO.getName().startsWith("ST") || stockListVO.getName().endsWith("退")) {
            return ServerResponse.createByErrorMsg("ST和已退市的股票不能入倉");
        }
        BigDecimal hcrate = stockListVO.getHcrate();


        stock.setStockName(stockListVO.getName());



//
//        BigDecimal ztPrice = zsPrice.multiply(maxRisePercent).add(zsPrice);
//        ztPrice = ztPrice.setScale(2, 4);
//        BigDecimal chaPrice = ztPrice.subtract(zsPrice);
//
        //涨停白名单
        if (user.getRiseWhite()==null){
            user.setRiseWhite(0);
        }
        if (stock.getRiseWhite()==null){
            stock.setRiseWhite(0);
        }
        if (user.getRiseWhite()!=1 &&stock.getRiseWhite()!=1) {
            if (hcrate != null && user.getAgentId() != 1) {
                log.info("當前漲跌幅 = {} % , 最大漲停幅度 = {} %", hcrate, siteSetting.getCreaseMaxPercent());
                if (hcrate.compareTo(siteSetting.getCreaseMaxPercent()) >= 0) {
                    return ServerResponse.createByErrorMsg("當前股票成交餘量不足");
                }
            }

            if (stock.getStockPlate() == null || StringUtils.isEmpty(stock.getStockPlate())) {
                int maxcrease = siteSetting.getCreaseMaxPercent().intValue();
                if (buyType == 1) {
                    if (stock_crease > 0.0D &&
                            stock_crease >= maxcrease) {
                        return ServerResponse.createByErrorMsg("下單失敗，股票當前漲幅:" + stock_crease + ",大於最大漲幅:" + maxcrease);
                    }
                }
                if (stock_crease < 0.0D &&
                        -stock_crease > maxcrease) {
                    return ServerResponse.createByErrorMsg("下單失敗，股票當前跌幅:" + stock_crease + ",大於最大跌幅:" + maxcrease);

                }
            }
            ServerResponse serverResponse = this.iStockService.selectRateByDaysAndStockCode(stock
                    .getStockCode(), siteSetting.getStockDays().intValue());
            if (!serverResponse.isSuccess()) {
                return serverResponse;
            }
            BigDecimal daysRate = (BigDecimal) serverResponse.getData();
            log.info("股票 {} ， {} 天內 漲幅 {} , 设置的漲幅 = {}", new Object[]{stock.getStockCode(), siteSetting
                    .getStockDays(), daysRate, siteSetting.getStockRate()});

            if (daysRate != null &&
                    siteSetting.getStockRate().compareTo(daysRate) == -1) {
                return ServerResponse.createByErrorMsg(siteSetting.getStockDays() + "天內漲幅超過 " + siteSetting
                        .getStockRate() + "不能交易");
            }
        }

        //BigDecimal buy_amt = now_price.multiply(new BigDecimal(buyNum.intValue())).divide(new BigDecimal(lever.intValue())).setScale(2, 4);
        BigDecimal buy_amt = now_price.multiply(new BigDecimal(buyNum));


        //BigDecimal buy_amt_autual = now_price.multiply(new BigDecimal(buyNum.intValue())).divide(new BigDecimal(lever.intValue()), 2, 4);

        BigDecimal buy_amt_autual = buy_amt.divide(lever, 2, RoundingMode.HALF_UP);

        int compareInt = buy_amt_autual.compareTo(new BigDecimal(siteSetting.getBuyMinAmt().intValue()));
        if (compareInt == -1) {
            return ServerResponse.createByErrorMsg("下單失敗，購買金額小於" + siteSetting
                    .getBuyMinAmt() + "元");
        }


        BigDecimal max_buy_amt = user_enable_amt.multiply(siteSetting.getBuyMaxAmtPercent());
        int compareCwInt = buy_amt_autual.compareTo(max_buy_amt);
        if (compareCwInt == 1) {
            return ServerResponse.createByErrorMsg("下單失敗，不能超過可用資金的" + siteSetting
                    .getBuyMaxAmtPercent().multiply(new BigDecimal("100")) + "%");
        }


        int compareUserAmtInt = user_enable_amt.compareTo(buy_amt_autual);
        log.info("用戶可用金額 = {}  實際購買金額 =  {}", user_enable_amt, buy_amt_autual);
        log.info("比較 用戶金額 和 實際 購買金額 =  {}", Integer.valueOf(compareUserAmtInt));
        if (compareUserAmtInt == -1) {
            return ServerResponse.createByErrorMsg("下單失敗，可用金額小於" + buy_amt_autual + "元");
        }
        UserPosition userPosition = userPositionMapper.selectOne(new QueryWrapper<UserPosition>().eq("user_id", user.getId()).eq("stock_code", stock.getStockCode()).eq("order_lever", lever).eq("buy_type", buyType));
        if (userPosition == null) {
            userPosition = new UserPosition();
            userPosition.setPositionType(user.getAccountType());
            userPosition.setUserId(user.getId());
            userPosition.setNickName(user.getRealName());
            userPosition.setAgentId(user.getAgentId());
            userPosition.setStockCode(stock.getStockCode());
            userPosition.setStockName(stock.getStockName());
            userPosition.setStockGid(stock.getStockGid());
            userPosition.setStockSpell(stock.getStockSpell());
            userPosition.setBuyType(buyType);
            if (stock.getStockPlate() != null) {
                userPosition.setStockPlate(stock.getStockPlate());
            }
            userPosition.setIsLock(Integer.valueOf(0));
            userPosition.setOrderLever(lever);
            userPosition.setOrderDirection("買漲");

            userPosition.setOrderStayDays(0);
            userPosition.setOrderStayFee(new BigDecimal("0"));

            userPosition.setBuyOrderPrice(BigDecimal.ZERO);
            userPosition.setOrderNum(0);
            userPosition.setOrderTotalPrice(BigDecimal.ZERO);
            this.userPositionMapper.insert(userPosition);
        }

        if (userPosition.getOrderNum() == null || userPosition.getOrderNum() == 0) {
            userPosition.setBuyOrderTime(new Date());
            userPosition.setMarginAdd(BigDecimal.ZERO);
            userPosition.setExperienceWallet(0);

            userPosition.setPositionSn(KeyUtils.getUniqueKey());
        }
        if (user.getUserWalletLevel()==1){
            userPosition.setExperienceWallet(userPosition.getExperienceWallet()+buyNum);
        }
        userPosition.setOrderTotalPrice(userPosition.getOrderTotalPrice().add(buy_amt));
        userPosition.setBuyOrderPrice(userPosition.getBuyOrderPrice().multiply(new BigDecimal(userPosition.getOrderNum())).add(buy_amt).divide(new BigDecimal(userPosition.getOrderNum() + buyNum), 2, RoundingMode.HALF_UP));
        userPosition.setOrderNum(userPosition.getOrderNum() + buyNum);
        BigDecimal buy_fee_amt = buy_amt.multiply(siteSetting.getBuyFee()).setScale(2, RoundingMode.HALF_UP);
        log.info("用戶購買手續費（配資后總資金 * 百分比） = {}", buy_fee_amt);
//        BigDecimal buy_yhs_amt = buy_amt.multiply(siteSetting.getDutyFee()).setScale(2, 4);
//        log.info("用戶購買印花稅（配資后總資金 * 百分比） = {}", buy_yhs_amt);
//        userPosition.setOrderSpread(buy_yhs_amt);
        BigDecimal profit_and_lose = new BigDecimal("0");
        userPosition.setProfitAndLose(profit_and_lose);

        userPosition.setBuyOrderTime(new Date());
        BigDecimal all_profit_and_lose = profit_and_lose.subtract(buy_fee_amt);
        userPosition.setAllProfitAndLose(all_profit_and_lose);


        int insertPositionCount = 0;
        this.userPositionMapper.updateByPrimaryKey(userPosition);
        UserPositionItem userPositionItem = new UserPositionItem();
        userPositionItem.setPositionType(userPosition.getPositionType());
        userPositionItem.setPositionSn(userPosition.getPositionSn());
        userPositionItem.setUserId(userPosition.getUserId());
        userPositionItem.setNickName(userPosition.getNickName());
        userPositionItem.setAgentId(userPosition.getAgentId());
        userPositionItem.setStockName(userPosition.getStockName());
        userPositionItem.setStockCode(userPosition.getStockCode());
        userPositionItem.setStockGid(userPosition.getStockGid());
        userPositionItem.setStockSpell(userPosition.getStockSpell());
        userPositionItem.setBuyType(userPosition.getBuyType());
        userPositionItem.setStockPlate(userPosition.getStockPlate());
        userPositionItem.setOrderTime(new Date());
        userPositionItem.setBuyOrderPrice(now_price);
        userPositionItem.setSellOrderPrice(new BigDecimal("0"));
        userPositionItem.setOrderDirection(userPosition.getOrderDirection());
        userPositionItem.setOrderNum(buyNum);


        userPositionItem.setOrderLever(lever);
        userPositionItem.setOrderTotalPrice(buy_amt);
        userPositionItem.setOrderFee(buy_fee_amt);
        userPositionItem.setOrderType(0);
        userPositionItemMapper.insert(userPositionItem);

        insertPositionCount = userPosition.getId();
        if (insertPositionCount > 0) {
            int updateUserCount = 0;
            if (buyType == 0) {
                updateUserCount =   userLevelWalletService.updateUserEnableAmtByStockBuy(user.getId(), buy_amt_autual.add(buy_fee_amt));
            } else {
                user.setUserIndexAmt(user.getUserIndexAmt().subtract(buy_amt_autual).subtract(buy_fee_amt));
                user.setEnableIndexAmt(user.getEnableIndexAmt().subtract(buy_amt_autual).subtract(buy_fee_amt));
                 updateUserCount = this.userMapper.updateByPrimaryKeySelective(user);

            }


            UserCashDetail ucd = new UserCashDetail();
            ucd.setPositionId(userPosition.getId());
            ucd.setAgentId(user.getAgentId());
            ucd.setAgentName(user.getAgentName());
            ucd.setUserId(user.getId());
            ucd.setUserName(user.getRealName());
            ucd.setDeType("買入股票(" + (userPosition.getBuyType() == 0 ? "現股" : "國際金") + ")");
            ucd.setDeAmt(BigDecimal.ZERO.subtract(buy_amt_autual.add(buy_fee_amt)));
            ucd.setAddTime(new Date());
            ucd.setIsRead(Integer.valueOf(0));
            int insertSxfCount = this.userCashDetailMapper.insert(ucd);

            if (updateUserCount > 0) {
                log.info("【用戶交易下單】修改用戶金額成功");
            } else {
                log.error("用戶交易下單】修改用戶金額出錯");
                throw new Exception("用戶交易下單】修改用戶金額出錯");
            }
            //核算代理收入-入倉手續費
            iAgentAgencyFeeService.AgencyFeeIncome(1, userPositionItem);
            log.info("【用戶交易下單】保存持倉記錄成功");
        } else {
            log.error("用戶交易下單】保存持倉記錄出錯");
            throw new Exception("用戶交易下單】保存持倉記錄出錯");
        }
        return ServerResponse.createBySuccess("下單成功");
    }


    @Override
    @Transactional
    public ServerResponse sell(String positionSn, int doType, Integer sellNumber, @RequestParam("nowPrice") BigDecimal nowPrice) throws Exception {
        log.info("【用戶交易平倉】 positionSn = {} ， dotype = {}", positionSn, Integer.valueOf(doType));
        BigDecimal tyjAmount = BigDecimal.ZERO;

        SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
        if (siteSetting == null) {
            log.error("平倉出錯，網站設置表不存在");
            return ServerResponse.createByErrorMsg("下單失敗，系統設置錯誤");
        }
        SiteProduct siteProduct = iSiteProductService.getProductSetting();
        UserPosition userPosition = this.userPositionMapper.findPositionBySn(positionSn);

        if (userPosition == null || userPosition.getOrderNum() == 0) {
            return ServerResponse.createByErrorMsg("平倉失敗，訂單不存在");
        }
        if (doType != 0) {
            String am_begin = siteSetting.getTransAmBegin();
            String am_end = siteSetting.getTransAmEnd();
            boolean am_flag = BuyAndSellUtils.isTransTime(am_begin, am_end);
            log.info("是否在上午交易時間 = {} ", am_flag);
            if (!am_flag) {
//                return ServerResponse.createByErrorMsg("平倉失敗，不在交易時段內");
            }

            // 判斷周末不能買
            Date today = new Date();
            Calendar c = Calendar.getInstance();
            c.setTime(today);
            int weekday = c.get(Calendar.DAY_OF_WEEK);
            if (weekday == 1) {
//                return ServerResponse.createByErrorMsg("非工作日，不可交易！");
            }
            if (weekday == 7) {
//                return ServerResponse.createByErrorMsg("非工作日，不可交易！");
            }
            String mMdd = DateUtil.format(new Date(), "MMdd");
            if ("0909".equals(mMdd) || "0910".equals(mMdd)) {
                return ServerResponse.createByErrorMsg("非工作日，不可交易！");
            }
            if ("1010".equals(mMdd)) {
                return ServerResponse.createByErrorMsg("非工作日，不可交易！");
            }

            if ("1231".equals(mMdd)) {
                return ServerResponse.createByErrorMsg("非工作日，不可交易！");
            }


            List<UserPositionItem> userPositionItemList = userPositionItemMapper.selectList(new QueryWrapper<UserPositionItem>().eq("position_sn", positionSn));
            int sellNumberOld = 0;
            int buyNumber = 0;
            for (UserPositionItem userPositionItem : userPositionItemList) {
                if (userPositionItem.getOrderType() == 0) {
                    if (DateTimeUtil.sameDate(DateTimeUtil.getCurrentDate(), userPositionItem.getOrderTime()) && !userPositionItem.getStockName().startsWith("新股")) {
                    } else {
                        buyNumber += userPositionItem.getOrderNum();
                    }
                } else {
                    sellNumberOld += userPositionItem.getOrderNum();
                }
            }
            if (sellNumber > (buyNumber - sellNumberOld)) {
                return ServerResponse.createByErrorMsg("當天入倉的股票T+1天才能出倉,您當前可平倉" + (buyNumber - sellNumberOld) + "股");
            }


        }

        if (sellNumber == null || sellNumber == 0 || sellNumber > userPosition.getOrderNum()) {
            sellNumber = userPosition.getOrderNum();
        }

        User user = this.userMapper.selectByPrimaryKey(userPosition.getUserId());
        /*實名認證開關開啟*/

        if (/*siteProduct.getRealNameDisplay() && */user.getIsLock() == 1) {
            return ServerResponse.createByErrorMsg("平倉失敗，用戶已被鎖定");
        }


        if (userPosition.getSellOrderId() != null) {
            return ServerResponse.createByErrorMsg("平倉失敗，此訂單已平倉");
        }

        if (1 == userPosition.getIsLock().intValue()) {
            return ServerResponse.createByErrorMsg("平倉失敗 " + userPosition.getLockMsg());
        }


        List<String> stockCodeList = new ArrayList<>();
        stockCodeList.add(userPosition.getStockCode());

        StockListVO stockListVO = null;
        List<StockListVO> stockListVOList = SinaStockApi.assembleStockListVO(
                SinaStockApi.getSinaStock(stockCodeList), stockCodeList);

        stockListVO = stockListVOList.get(0);


        boolean buyTime = BuyAndSellUtils.isTransTime("09:00", "09:05");
        BigDecimal now_price = new BigDecimal(stockListVO.getNowPrice());
        if (nowPrice != null && nowPrice.compareTo(BigDecimal.ZERO) != 0 && doType != 0 && buyTime) {
            now_price = nowPrice;

        }
        if (nowPrice == null || nowPrice.compareTo(BigDecimal.ZERO) == 0) {
            if (buyTime) {
                BigDecimal nowPrice1 = getNowPrice(userPosition.getStockCode());
                if (nowPrice1 != null && nowPrice1.compareTo(BigDecimal.ZERO) != 0) {
                    now_price = nowPrice1;
                }
            }
        }
        if (now_price.compareTo(new BigDecimal("0")) != 1) {
            log.error("股票 = {} 收到報價 = {}", userPosition.getStockName(), now_price);
            return ServerResponse.createByErrorMsg("報價0，平倉失敗，請稍後再試");
        }


        System.out.println(stockListVO.getHcrate());
        double stock_crease = stockListVO.getHcrate().doubleValue();


//        BigDecimal zsPrice = new BigDecimal(stockListVO.getPreclose_px());

//        BigDecimal ztPrice = zsPrice.multiply(new BigDecimal("0.1")).add(zsPrice);
//        ztPrice = ztPrice.setScale(2, RoundingMode.HALF_UP);
//        BigDecimal chaPrice = ztPrice.subtract(zsPrice);

//        BigDecimal ztRate = chaPrice.multiply(new BigDecimal("100")).divide(zsPrice, 2, 4);

//        ztRate = ztRate.negate();
//        log.info("股票當前漲跌幅 = {} 跌停幅度 = {}", stock_crease, ztRate);
//        if ((new BigDecimal(String.valueOf(stock_crease))).compareTo(ztRate) == 0 && "買漲"
//                .equals(userPosition.getOrderDirection())) {
//            return ServerResponse.createByErrorMsg("當前股票已跌停不能賣出");
//        }

        Integer buy_num = sellNumber;

        //如果跌幅大于此股票最大涨跌幅。则只跌掉最大涨跌幅
        BigDecimal force_stop_percent = siteSetting.getForceStopPercent();

        //买入成本
        BigDecimal buyCost = userPosition.getBuyOrderPrice().multiply(new BigDecimal(buy_num)).divide(userPosition.getOrderLever(), 2, RoundingMode.HALF_UP);
        //買入市值判斷
        BigDecimal all_buy_amt = userPosition.getBuyOrderPrice().multiply(new BigDecimal(buy_num)).setScale(2, RoundingMode.HALF_UP);

        if (user.getUserWalletLevel()!=1&&userPosition.getExperienceWallet()!=null &&userPosition.getExperienceWallet()>0){
            if (buy_num<userPosition.getExperienceWallet()){
                tyjAmount =   userPosition.getBuyOrderPrice().multiply(new BigDecimal(buy_num)).setScale(2, RoundingMode.HALF_UP);
            }else{
                tyjAmount =   userPosition.getBuyOrderPrice().multiply(new BigDecimal(userPosition.getExperienceWallet())).setScale(2, RoundingMode.HALF_UP);
            }
        }
       //賣出市值判斷
        BigDecimal all_sell_amt = now_price.multiply(new BigDecimal(buy_num)).setScale(2, RoundingMode.HALF_UP);
        //浮動盈虧
        BigDecimal profitAndLose = all_sell_amt.subtract(all_buy_amt);
        //当前持仓漲跌幅度
        BigDecimal plRate = profitAndLose.divide(buyCost, 2, RoundingMode.HALF_UP);
        if (plRate.compareTo(BigDecimal.ZERO) < 0 && BigDecimal.ZERO.subtract(plRate).compareTo(force_stop_percent) > 0) {
            all_sell_amt = all_buy_amt.subtract(all_buy_amt.multiply(force_stop_percent.divide(userPosition.getOrderLever(), 4, RoundingMode.DOWN))).setScale(2, RoundingMode.HALF_UP);
            ;
            now_price = all_sell_amt.divide(new BigDecimal(buy_num), 2, RoundingMode.HALF_UP);
        }
        BigDecimal profitLoss = new BigDecimal("0");
        if ("買漲".equals(userPosition.getOrderDirection())) {
            log.info("買賣方向：{}", "漲");

            profitLoss = all_sell_amt.subtract(all_buy_amt);
        } else {
            log.info("買賣方向：{}", "跌");
            profitLoss = all_buy_amt.subtract(all_sell_amt);
        }
        log.info("買入總金額 = {} , 賣出總金額 = {} , 盈虧 = {}", all_buy_amt, all_sell_amt, profitLoss);
        BigDecimal user_all_amt = user.getUserAmt();
        BigDecimal user_enable_amt = user.getEnableAmt();
        if (userPosition.getBuyType() == 1) {
            user_all_amt = user.getUserIndexAmt();
            user_enable_amt = user.getEnableIndexAmt();
        }

        log.info("用戶原本總資金 = {} , 可用 = {}", user_all_amt, user_enable_amt);

        BigDecimal orderSpread = now_price.multiply(new BigDecimal(sellNumber + "")).multiply(siteSetting.getDutyFee());
        log.info("印花稅 = {}", orderSpread);

        BigDecimal buy_fee_amt = userPosition.getBuyOrderPrice().multiply(new BigDecimal(sellNumber)).multiply(siteSetting.getBuyFee()).setScale(2, RoundingMode.HALF_UP);
        log.info("買入手續費 = {}", buy_fee_amt);

        BigDecimal sell_fee_amt = now_price.multiply(new BigDecimal(sellNumber + "")).multiply(siteSetting.getSellFee()).setScale(2, RoundingMode.HALF_UP);
        log.info("賣出手續費 = {}", sell_fee_amt);

        //總手續費= 買入手續費+賣出手續費+印花稅+遞延費+點差費
        BigDecimal all_fee_amt = buy_fee_amt.add(sell_fee_amt).add(orderSpread);


        log.info("總的手續費費用 = {}", all_fee_amt);
        BigDecimal all_profit = profitLoss.subtract(all_fee_amt);
        userPosition.setOrderNum(userPosition.getOrderNum() - sellNumber);

        userPosition.setOrderTotalPrice(userPosition.getBuyOrderPrice().multiply(new BigDecimal(userPosition.getOrderNum())));
        int updatePositionCount = this.userPositionMapper.updateByPrimaryKeySelective(userPosition);
        UserPositionItem userPositionItem = new UserPositionItem();
        userPositionItem.setPositionType(userPosition.getPositionType());
        userPositionItem.setPositionSn(userPosition.getPositionSn());
        userPositionItem.setUserId(userPosition.getUserId());
        userPositionItem.setNickName(userPosition.getNickName());
        userPositionItem.setAgentId(userPosition.getAgentId());
        userPositionItem.setStockGid(userPosition.getStockGid());
        userPositionItem.setBuyOrderPrice(userPosition.getBuyOrderPrice());
        userPositionItem.setStockName(userPosition.getStockName());
        userPositionItem.setStockCode(userPosition.getStockCode());
        userPositionItem.setStockSpell(userPosition.getStockSpell());
        userPositionItem.setBuyType(userPosition.getBuyType());
        userPositionItem.setStockPlate(userPosition.getStockPlate());
        userPositionItem.setOrderTime(new Date());
        userPositionItem.setSellOrderPrice(now_price);
        userPositionItem.setOrderDirection(userPosition.getOrderDirection());
        userPositionItem.setOrderNum(sellNumber);
        userPositionItem.setOrderLever(userPosition.getOrderLever());
        userPositionItem.setOrderTotalPrice(all_sell_amt);
        userPositionItem.setOrderFee(sell_fee_amt.add(buy_fee_amt));
        userPositionItem.setOrderSpread(orderSpread);
        userPositionItem.setProfitAndLose(profitLoss);
        userPositionItem.setAllProfitAndLose(all_profit);
        userPositionItem.setOrderType(1);
        userPositionItemMapper.insert(userPositionItem);


        if (updatePositionCount > 0) {
            log.info("【用戶平倉】修改浮動盈虧記錄成功");
        } else {
            log.error("用戶平倉】修改浮動盈虧記錄出錯");
            throw new Exception("用戶平倉】修改浮動盈虧記錄出錯");
        }

        //買入消費金額
        BigDecimal freez_amt = all_buy_amt.divide(userPosition.getOrderLever(), 2, RoundingMode.HALF_UP);
        //修改用戶可用餘額=當前可用餘額+總盈虧+買入總金額+追加保證金
        user_enable_amt = user_enable_amt.add(freez_amt).add(profitLoss).subtract(sell_fee_amt).subtract(orderSpread);
        user_all_amt = user_all_amt.add(freez_amt).add(profitLoss).subtract(sell_fee_amt).subtract(orderSpread);
        profitLoss =       profitLoss.subtract(sell_fee_amt).subtract(orderSpread);
        int updateUserCount =0;



        UserCashDetail ucd = new UserCashDetail();
        ucd.setPositionId(userPosition.getId());
        ucd.setAgentId(user.getAgentId());
        ucd.setAgentName(user.getAgentName());
        ucd.setUserId(user.getId());
        ucd.setUserName(user.getRealName());
        ucd.setDeType("賣出股票(" + (userPosition.getBuyType() == 0 ? "現股" : "國際金") + ")");
        ucd.setDeAmt(all_buy_amt.add(profitLoss).subtract(sell_fee_amt).subtract(orderSpread));
        ucd.setDeSummary("賣出股票，" + userPosition.getStockCode() + "/" + userPosition.getStockName() + ",佔用本金：" + freez_amt + ",總手續費：" + all_fee_amt + ",印花稅：" + orderSpread + ",盈虧：" + profitLoss + "，总盈虧：" + all_profit);
        ucd.setAddTime(new Date());
        ucd.setIsRead(0);
        int insertSxfCount = this.userCashDetailMapper.insert(ucd);
        if (userPosition.getBuyType() == 1) {
            user.setUserIndexAmt(user_all_amt);
            user.setEnableIndexAmt(user_enable_amt);
            updateUserCount = this.userMapper.updateByPrimaryKeySelective(user);

        } else {

            freez_amt=freez_amt.subtract(tyjAmount);
            updateUserCount = userLevelWalletService.updateUserEnableAmtByStockLose(user.getId(),profitLoss,freez_amt);
        }
        if (updateUserCount > 0) {
            log.info("【用戶平倉】修改用戶金額成功");
        } else {
            log.error("用戶平倉】修改用戶金額出錯");
            throw new Exception("用戶平倉】修改用戶金額出錯");
        }
        if (insertSxfCount > 0) {
            //核算代理收入-平倉手續費
            log.info("分給代理錢" + userPositionItem.getId());
            iAgentAgencyFeeService.AgencyFeeIncome(2, userPositionItem);
            //核算代理收入-分紅
            iAgentAgencyFeeService.AgencyFeeIncome(4, userPositionItem);
            log.info("【用戶平倉】保存明細記錄成功");
        } else {
            log.error("用戶平倉】保存明細記錄出錯");
            throw new Exception("用戶平倉】保存明細記錄出錯");
        }

        return ServerResponse.createBySuccessMsg("平倉成功！");
    }

    //用戶追加保證金操作
    @Override
    public ServerResponse addmargin(String positionSn, int doType, BigDecimal marginAdd) throws Exception {
        log.info("【用戶追加保證金】 positionSn = {} ， dotype = {}", positionSn, Integer.valueOf(doType));

        SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
        if (siteSetting == null) {
            log.error("追加保證金出錯，網站設置表不存在");
            return ServerResponse.createByErrorMsg("追加失敗，系統設置錯誤");
        }

        if (doType != 0) {
            /*String am_begin = siteSetting.getTransAmBegin();
            String am_end = siteSetting.getTransAmEnd();
            String pm_begin = siteSetting.getTransPmBegin();
            String pm_end = siteSetting.getTransPmEnd();
            boolean am_flag = BuyAndSellUtils.isTransTime(am_begin, am_end);
            boolean pm_flag = BuyAndSellUtils.isTransTime(pm_begin, pm_end);
            log.info("是否在上午交易時間 = {} 是否在下午交易時間 = {}", Boolean.valueOf(am_flag), Boolean.valueOf(pm_flag));
            if (!am_flag && !pm_flag) {
                return ServerResponse.createByErrorMsg("追加失敗，不在交易時段內");
            }*/
        }

        UserPosition userPosition = this.userPositionMapper.findPositionBySn(positionSn);
        if (userPosition == null) {
            return ServerResponse.createByErrorMsg("追加失敗，訂單不存在");
        }

        User user = this.userMapper.selectByPrimaryKey(userPosition.getUserId());
        /*實名認證開關開啟*/
        SiteProduct siteProduct = iSiteProductService.getProductSetting();
        if (!siteProduct.getStockMarginDisplay()) {
            return ServerResponse.createByErrorMsg("不允許追加，請聯繫管理員");
        }

        if (siteProduct.getHolidayDisplay()) {
            return ServerResponse.createByErrorMsg("周末或節假日不能交易！");
        }

        if (/*siteProduct.getRealNameDisplay() && */user.getIsLock() == 1) {
            return ServerResponse.createByErrorMsg("追加失敗，用戶已被鎖定");
        }

        if (1 == userPosition.getIsLock().intValue()) {
            return ServerResponse.createByErrorMsg("追加失敗 " + userPosition.getLockMsg());
        }

        BigDecimal user_all_amt = user.getUserAmt();
        BigDecimal user_enable_amt = user.getEnableAmt();
        if (userPosition.getBuyType() == 1) {
            user_all_amt = user.getUserIndexAmt();
            user_enable_amt = user.getEnableIndexAmt();
        }
        int compareUserAmtInt = user_enable_amt.compareTo(marginAdd);
        log.info("用戶可用金額 = {}  追加金額 =  {}", user_enable_amt, marginAdd);
        log.info("比較 用戶金額 和 實際 購買金額 =  {}", Integer.valueOf(compareUserAmtInt));
        if (compareUserAmtInt == -1) {
            return ServerResponse.createByErrorMsg("追加失敗，融資可用金額小於" + marginAdd + "元");
        }


        userPosition.setMarginAdd(userPosition.getMarginAdd().add(marginAdd));

        int updatePositionCount = this.userPositionMapper.updateByPrimaryKeySelective(userPosition);
        if (updatePositionCount > 0) {
            log.info("【用戶追加保證金】追加保證金成功");
        } else {
            log.error("用戶追加保證金】追加保證金出錯");
            throw new Exception("用戶追加保證金】追加保證金出錯");
        }

        //修改用戶可用餘額=當前可用餘額-追加金額
        BigDecimal reckon_enable = user_enable_amt.subtract(marginAdd);

        log.info("用戶追加保證金后的總資金  = {} , 可用資金 = {}", user_all_amt, reckon_enable);

        if (userPosition.getBuyType() == 0) {
            user.setEnableAmt(reckon_enable);

        } else {
            user.setEnableIndexAmt(reckon_enable);

        }
        int updateUserCount = this.userMapper.updateByPrimaryKeySelective(user);
        if (updateUserCount > 0) {
            log.info("【用戶平倉】修改用戶金額成功");
        } else {
            log.error("用戶平倉】修改用戶金額出錯");
            throw new Exception("用戶平倉】修改用戶金額出錯");
        }

        UserCashDetail ucd = new UserCashDetail();
        ucd.setPositionId(userPosition.getId());
        ucd.setAgentId(user.getAgentId());
        ucd.setAgentName(user.getAgentName());
        ucd.setUserId(user.getId());
        ucd.setUserName(user.getRealName());
        ucd.setDeType("追加保證金");
        ucd.setDeAmt(marginAdd.multiply(new BigDecimal("-1")));
        ucd.setDeSummary("追加股票，" + userPosition.getStockCode() + "/" + userPosition.getStockName() + ",追加金額：" + marginAdd);

        ucd.setAddTime(new Date());
        ucd.setIsRead(Integer.valueOf(0));

        int insertSxfCount = this.userCashDetailMapper.insert(ucd);
        if (insertSxfCount > 0) {
            log.info("【用戶平倉】保存明細記錄成功");
        } else {
            log.error("用戶平倉】保存明細記錄出錯");
            throw new Exception("用戶平倉】保存明細記錄出錯");
        }

        return ServerResponse.createBySuccessMsg("追加成功！");
    }


    @Override
    public ServerResponse lock(Integer positionId, Integer state, String lockMsg) {
        if (positionId == null || state == null) {
            return ServerResponse.createByErrorMsg("參數不能為空");
        }

        UserPosition position = this.userPositionMapper.selectByPrimaryKey(positionId);
        if (position == null) {
            return ServerResponse.createByErrorMsg("持倉不存在");
        }

        if (position.getSellOrderId() != null) {
            return ServerResponse.createByErrorMsg("平倉單不能鎖倉");
        }

        if (state.intValue() == 1 &&
                StringUtils.isBlank(lockMsg)) {
            return ServerResponse.createByErrorMsg("鎖倉提示信息必填");
        }


        if (state.intValue() == 1) {
            position.setIsLock(Integer.valueOf(1));
            position.setLockMsg(lockMsg);
        } else {
            position.setIsLock(Integer.valueOf(0));
        }

        int updateCount = this.userPositionMapper.updateByPrimaryKeySelective(position);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMsg("操作成功");
        }
        return ServerResponse.createByErrorMsg("操作失敗");
    }

    @Override
    public ServerResponse del(Integer positionId) {
        if (positionId == null) {
            return ServerResponse.createByErrorMsg("id不能為空");
        }
        UserPosition position = this.userPositionMapper.selectByPrimaryKey(positionId);
        if (position == null) {
            ServerResponse.createByErrorMsg("該持倉不存在");
        }
        /*if (position.getSellOrderId() == null) {
            return ServerResponse.createByErrorMsg("持倉單不能刪除！");
        }*/
        int updateCount = this.userPositionMapper.deleteByPrimaryKey(positionId);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMsg("刪除成功");
        }
        return ServerResponse.createByErrorMsg("刪除失敗");
    }

    /**
     * 此方法棄用
     */
    @Override
    public ServerResponse findMyPositionByCodeAndSpellV1(String stockCode, String stockSpell, Integer state, HttpServletRequest request, int pageNum, int pageSize) {
        User user = this.iUserService.getCurrentUser(request);
        if (user == null) {
            return ServerResponse.createBySuccessMsg("請先登錄");
        }
        PageHelper.startPage(pageNum, pageSize);


        List<UserPosition> userPositions = this.userPositionMapper.findMyPositionByCodeAndSpell(user.getId(), stockCode, stockSpell, state);
        List<UserPositionVO> userPositionVOS = Lists.newArrayList();
        if (userPositions.size() > 0) {
            for (UserPosition position : userPositions) {
                UserPositionVO userPositionVO = assembleUserPositionVO(position);
                userPositionVOS.add(userPositionVO);
            }
        }

        PageInfo pageInfo = new PageInfo(userPositions);
        pageInfo.setList(userPositionVOS);

        return ServerResponse.createBySuccess(pageInfo);
    }

    @Override
    public PositionVO findUserPositionAllProfitAndLose(Integer userId) {
        List<UserPosition> userPositions = this.userPositionMapper.findPositionByUserIdAndSellIdIsNull(userId);
        BigDecimal allProfitAndLose = new BigDecimal("0");
        BigDecimal buyTotalAmount = new BigDecimal("0");

        BigDecimal userIndexBuyAmount = BigDecimal.ZERO;

        BigDecimal userBuyAmount = BigDecimal.ZERO;

        List<String> stockCodeList = new ArrayList<>();
        for (UserPosition position : userPositions) {
            stockCodeList.add(position.getStockCode());
        }
        List<StockListVO> stockListVOList = SinaStockApi.assembleStockListVO(
                SinaStockApi.getSinaStock(stockCodeList), stockCodeList);
        for (UserPosition position : userPositions) {
            BigDecimal nowPrice = null;

            StockListVO stockListVO = null;
            for (StockListVO stockListVOBy : stockListVOList) {
                if (stockListVOBy.getCode().equals(position.getStockCode())) {
                    stockListVO = stockListVOBy;
                    nowPrice = new BigDecimal(stockListVO.getNowPrice());

                    break;
                }
            }
            if (stockListVO == null) {
                nowPrice = new BigDecimal("0");
            }
            if (nowPrice.compareTo(new BigDecimal("0")) != 0) {
                BigDecimal buyPrice = position.getBuyOrderPrice();
                BigDecimal subPrice = nowPrice.subtract(buyPrice);
                BigDecimal profit_and_lose = subPrice.multiply(new BigDecimal(position.getOrderNum().intValue()));
                if ("買跌".equals(position.getOrderDirection())) {
                    profit_and_lose = profit_and_lose.negate();
                }
                SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
                BigDecimal buy_fee_amt = position.getBuyOrderPrice().multiply(new BigDecimal(position.getOrderNum())).multiply(siteSetting.getBuyFee()).setScale(2, 4);
                BigDecimal position_profit = profit_and_lose.subtract(buy_fee_amt);
                allProfitAndLose = allProfitAndLose.add(position_profit);

                BigDecimal subBuyTotalAmount = position.getBuyOrderPrice().multiply(new BigDecimal(position.getOrderNum())).divide(position.getOrderLever(), 2, RoundingMode.HALF_UP);
                buyTotalAmount = buyTotalAmount.add(subBuyTotalAmount);
                buyTotalAmount = buyTotalAmount.add(buy_fee_amt);


                if (position.getBuyType() == 0) {
                    //现股
                    userBuyAmount = userBuyAmount.add(position.getBuyOrderPrice().multiply(new BigDecimal(position.getOrderNum())));
                    userBuyAmount = userBuyAmount.add(buy_fee_amt);
                } else {
                    //融资
                    userIndexBuyAmount = userIndexBuyAmount.add(position.getBuyOrderPrice().multiply(new BigDecimal(position.getOrderNum())));
                    userIndexBuyAmount = userIndexBuyAmount.add(buy_fee_amt);
                }

                continue;
            }
            log.info("查詢所有持倉單的總盈虧，現價返回0，當前為集合競價");
        }

        QueryWrapper<UserPositionItem> queryWrapper = new QueryWrapper<UserPositionItem>().eq("user_id", userId).eq("order_type", 1);
        queryWrapper.orderByDesc("id");
        List<UserPositionItem> userPositionItemList = userPositionItemMapper.selectList(queryWrapper);
        BigDecimal userIndexSellAmount = BigDecimal.ZERO;

        BigDecimal userSellAmount = BigDecimal.ZERO;
        for (UserPositionItem userPositionItem : userPositionItemList) {
            if (userPositionItem.getBuyType() == 0) {
                //现股

                userSellAmount = userSellAmount.add(userPositionItem.getAllProfitAndLose());
            } else {
                //融资
                userIndexSellAmount = userIndexSellAmount.add(userPositionItem.getAllProfitAndLose());

            }

        }


        PositionVO positionVO = new PositionVO();
        positionVO.setUserBuySellLose(userIndexSellAmount);
        positionVO.setUserBuyBuyLose(userSellAmount);

        positionVO.setUserIndexBuyAmount(userIndexBuyAmount);
        positionVO.setUserBuyAmount(userBuyAmount);

        positionVO.setAllFreezAmt(buyTotalAmount);
        positionVO.setAllProfitAndLose(allProfitAndLose);
        return positionVO;
    }

    @Override
    public List<UserPosition> findPositionByUserIdAndSellIdIsNull(Integer userId) {
        return this.userPositionMapper.findPositionByUserIdAndSellIdIsNull(userId);
    }

    @Override
    public List<UserPosition> findPositionByStockCodeAndTimes(int minuteTimes, String stockCode, Integer userId) {
        Date paramTimes = null;
        paramTimes = DateTimeUtil.parseToDateByMinute(minuteTimes);

        return this.userPositionMapper.findPositionByStockCodeAndTimes(paramTimes, stockCode, userId);
    }

    @Override
    public Integer findPositionNumByTimes(int minuteTimes, Integer userId) {
        Date beginDate = DateTimeUtil.parseToDateByMinute(minuteTimes);
        Integer transNum = this.userPositionMapper.findPositionNumByTimes(beginDate, userId);
        log.info("用戶 {} 在 {} 分鐘之內 交易手數 {}", new Object[]{userId, Integer.valueOf(minuteTimes), transNum});
        return transNum;
    }

    @Override
    public ServerResponse listByAgent(Integer positionType, Integer state, Integer userId, Integer agentId, String positionSn, String beginTime, String endTime, HttpServletRequest request, int pageNum, int pageSize, Integer buyType) {
        AgentUser currentAgent = this.iAgentUserService.getCurrentAgent(request);
        if (currentAgent == null) {
            return ServerResponse.createByError("請先登錄", null);
        }

        if (agentId != null) {
            AgentUser agentUser = this.agentUserMapper.selectByPrimaryKey(agentId);
            if (agentUser != null && agentUser.getParentId().compareTo(currentAgent.getId()) != 0) {
                return ServerResponse.createByErrorMsg("不能查詢非下級代理用戶持倉");
            }
        }

        Integer searchId = null;
        if (agentId == null) {
            searchId = currentAgent.getId();
        } else {
            searchId = agentId;
        }


        Timestamp begin_time = null;
        if (StringUtils.isNotBlank(beginTime)) {
            begin_time = DateTimeUtil.searchStrToTimestamp(beginTime);
        }
        Timestamp end_time = null;
        if (StringUtils.isNotBlank(endTime)) {
            end_time = DateTimeUtil.searchStrToTimestamp(endTime);
        }

        PageHelper.startPage(pageNum, pageSize);


        List<UserPosition> userPositions = this.userPositionMapper.listByAgent(positionType, state, userId, searchId, positionSn, begin_time, end_time, buyType, null, null);
        List<AgentPositionVO> agentPositionVOS = Lists.newArrayList();
        for (UserPosition position : userPositions) {
            AgentPositionVO agentPositionVO = assembleAgentPositionVO(position);
            agentPositionVOS.add(agentPositionVO);
        }

        PageInfo pageInfo = new PageInfo(userPositions);
        pageInfo.setList(agentPositionVOS);

        return ServerResponse.createBySuccess(pageInfo);


    }

    @Override
    public ServerResponse getIncome(Integer agentId, Integer positionType, String beginTime, String endTime, Integer buyType) {
        if (StringUtils.isBlank(beginTime) || StringUtils.isBlank(endTime)) {
            return ServerResponse.createByErrorMsg("時間不能為空");
        }

        Timestamp begin_time = null;
        if (StringUtils.isNotBlank(beginTime)) {
            begin_time = DateTimeUtil.searchStrToTimestamp(beginTime);
        }
        Timestamp end_time = null;
        if (StringUtils.isNotBlank(endTime)) {
            end_time = DateTimeUtil.searchStrToTimestamp(endTime);
        }

        List<UserPositionItem> userPositions = userPositionItemMapper.listByAgent(positionType, Integer.valueOf(1), null, agentId, null, begin_time, end_time, buyType, null, null);


        BigDecimal order_fee_amt = new BigDecimal("0");
        BigDecimal order_profit_and_lose = new BigDecimal("0");
        BigDecimal order_profit_and_lose_all = new BigDecimal("0");
        BigDecimal sellTotalAmount = new BigDecimal("0");
        BigDecimal userBuyAmount = BigDecimal.ZERO;
        BigDecimal userIndexBuyAmount = BigDecimal.ZERO;
        int orderNumber = 0;
        SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();

        for (UserPositionItem position : userPositions) {

            if (position.getSellOrderPrice() != null) {
                sellTotalAmount = sellTotalAmount.add(position.getSellOrderPrice().multiply(new BigDecimal(position.getOrderNum())));
            }
            orderNumber = orderNumber + position.getOrderNum();
            order_fee_amt = order_fee_amt.add(position.getOrderFee()).add(position.getOrderSpread());
            order_profit_and_lose = order_profit_and_lose.add(position.getProfitAndLose());
            order_profit_and_lose_all = order_profit_and_lose_all.add(position.getAllProfitAndLose());
            BigDecimal buy_fee_amt = position.getBuyOrderPrice().multiply(new BigDecimal(position.getOrderNum())).multiply(siteSetting.getBuyFee()).setScale(2, 4);

            if (position.getBuyType() == 0) {
                //现股
                userBuyAmount = userBuyAmount.add(position.getBuyOrderPrice().multiply(new BigDecimal(position.getOrderNum())));
                userBuyAmount = userBuyAmount.add(buy_fee_amt);
            } else {
                //融资
                userIndexBuyAmount = userIndexBuyAmount.add(position.getBuyOrderPrice().multiply(new BigDecimal(position.getOrderNum())));
                userIndexBuyAmount = userIndexBuyAmount.add(buy_fee_amt);
            }

        }

        List<User> users = userMapper.selectList(new QueryWrapper<>());
        BigDecimal enbleAmount = BigDecimal.ZERO;

        for (User user : users) {
            enbleAmount = enbleAmount.add(user.getEnableAmt());
        }
        AgentIncomeVO agentIncomeVO = new AgentIncomeVO();
        agentIncomeVO.setOrderSize(userPositions.size());
        agentIncomeVO.setOrderFeeAmt(order_fee_amt);
        agentIncomeVO.setUserEnableAmount(enbleAmount);
        agentIncomeVO.setOrderProfitAndLose(order_profit_and_lose);
        agentIncomeVO.setOrderAllAmt(order_profit_and_lose_all);
        agentIncomeVO.setSellTotalAmount(sellTotalAmount);
        agentIncomeVO.setOrderNumber(orderNumber);
        agentIncomeVO.setBuyIndexAmountTotal(userIndexBuyAmount);
        agentIncomeVO.setBuyAmountTotal(userBuyAmount);

        return ServerResponse.createBySuccess(agentIncomeVO);
    }

    @Override
    public ServerResponse listByAdmin(Integer sum, String sort, String sortColmn, Integer agentId, Integer positionType, Integer state, Integer userId, String positionSn, String beginTime, String endTime, int pageNum, int pageSize, Integer buyType) {


        if(state== null){
            state = 0;
        }
        Timestamp begin_time = null;
        if (StringUtils.isNotBlank(beginTime)) {
            begin_time = DateTimeUtil.searchStrToTimestamp(beginTime);
        }
        Timestamp end_time = null;
        if (StringUtils.isNotBlank(endTime)) {
            end_time = DateTimeUtil.searchStrToTimestamp(endTime);
        }
        Page<UserPosition> page = null;

        if (sum != 1 && (sortColmn == null || "".equals(sortColmn))) {
            page = PageHelper.startPage(pageNum, pageSize);
        }


        if (state == 0) {
            List<UserPosition> userPositions = this.userPositionMapper.listByAgent(positionType, state, userId, agentId, positionSn, begin_time, end_time, buyType, sort, sortColmn);
            BigDecimal profitAndLose = BigDecimal.ZERO;
            Integer orderNumber = 0;
            BigDecimal buyTotalAmount = BigDecimal.ZERO;
            BigDecimal allProfitAndLose = BigDecimal.ZERO;

            List<AdminPositionVO> adminPositionVOS = assembleAdminPositionVOList(userPositions);

            for (AdminPositionVO adminPositionVO : adminPositionVOS) {
                if (adminPositionVO.getProfitAndLose() != null) {
                    profitAndLose = profitAndLose.add(adminPositionVO.getProfitAndLose().subtract(adminPositionVO.getOrderFee()));
                }
                if (adminPositionVO.getAllProfitAndLose() != null) {
                    allProfitAndLose = allProfitAndLose.add(adminPositionVO.getAllProfitAndLose());
                }
                if (adminPositionVO.getBuyTotalAmount() != null) {
                    buyTotalAmount = buyTotalAmount.add(adminPositionVO.getBuyTotalAmount());
                }
                if (adminPositionVO.getOrderNum() != null) {
                    orderNumber = orderNumber + adminPositionVO.getOrderNum();
                }
            }

            if (sortColmn != null) {
                switch (sortColmn) {
                    case "sellOrderPrice":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = o1.getSellOrderPrice().compareTo(o2.getSellOrderPrice());
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;
                    case "buyOrderPrice":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = o1.getBuyOrderPrice().compareTo(o2.getBuyOrderPrice());
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;
                    case "now_price":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = new BigDecimal(o1.getNow_price()).compareTo(new BigDecimal(o2.getNow_price()));
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;

                    case "profitAndLose":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = o1.getProfitAndLose().compareTo(o2.getProfitAndLose());
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;
                    case "allProfitAndLose":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = o1.getAllProfitAndLose().compareTo(o2.getAllProfitAndLose());
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;
                }
            }

            //adminPositionVOS 根據
            Pager<AdminPositionVO> pager = Pager.create(adminPositionVOS, pageSize);
            List<AdminPositionVO> page1 = pager.getPagedList(pageNum);
            AdminPositionPageVo adminPositionPageVo = new AdminPositionPageVo();
            adminPositionPageVo.setList(page1);
            adminPositionPageVo.setTotal(adminPositionVOS.size());

            if (sum != 1 && (sortColmn == null || "".equals(sortColmn))) {
                PageInfo pageInfo = new PageInfo(page);
                adminPositionPageVo.setList(adminPositionVOS);
                adminPositionPageVo.setTotal(Integer.valueOf(pageInfo.getTotal() + ""));
            }
            adminPositionPageVo.setPageNum(pageNum);
            adminPositionPageVo.setPageSize(pageSize);
            adminPositionPageVo.setAllProfitAndLose(allProfitAndLose);
            adminPositionPageVo.setProfitAndLose(profitAndLose);
            adminPositionPageVo.setOrderNum(orderNumber);
            adminPositionPageVo.setBuyTotalAmount(buyTotalAmount);
            return ServerResponse.createBySuccess(adminPositionPageVo);
        } else {
            List<UserPositionItem> userPositions = this.userPositionItemMapper.listByAgent(positionType, state, userId, agentId, positionSn, begin_time, end_time, buyType, sort, sortColmn);
            List<AdminPositionVO> adminPositionVOS = Lists.newArrayList();
            BigDecimal profitAndLose = BigDecimal.ZERO;
            Integer orderNumber = 0;
            BigDecimal buyTotalAmount = BigDecimal.ZERO;
            BigDecimal allProfitAndLose = BigDecimal.ZERO;
            for (UserPositionItem position : userPositions) {
                AdminPositionVO adminPositionVO = assembleAdminPositionItemVO(position);
                adminPositionVOS.add(adminPositionVO);
                if (adminPositionVO.getProfitAndLose() != null) {
                    profitAndLose = profitAndLose.add(adminPositionVO.getProfitAndLose().subtract(adminPositionVO.getOrderFee()));
                }
                if (adminPositionVO.getAllProfitAndLose() != null) {
                    allProfitAndLose = allProfitAndLose.add(adminPositionVO.getAllProfitAndLose());
                }
                if (adminPositionVO.getBuyTotalAmount() != null) {
                    buyTotalAmount = buyTotalAmount.add(adminPositionVO.getBuyTotalAmount());
                }
                if (adminPositionVO.getOrderNum() != null) {
                    orderNumber = orderNumber + adminPositionVO.getOrderNum();
                }
            }

            if (sortColmn != null) {
                switch (sortColmn) {
                    case "sellOrderPrice":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = o1.getSellOrderPrice().compareTo(o2.getSellOrderPrice());
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;
                    case "buyOrderPrice":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = o1.getBuyOrderPrice().compareTo(o2.getBuyOrderPrice());
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;
                    case "now_price":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = new BigDecimal(o1.getNow_price()).compareTo(new BigDecimal(o2.getNow_price()));
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;

                    case "profitAndLose":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = o1.getProfitAndLose().compareTo(o2.getProfitAndLose());
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;
                    case "allProfitAndLose":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = o1.getAllProfitAndLose().compareTo(o2.getAllProfitAndLose());
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;
                }
            }
            //adminPositionVOS 根據
            Pager<AdminPositionVO> pager = Pager.create(adminPositionVOS, pageSize);
            List<AdminPositionVO> page1 = pager.getPagedList(pageNum);
            AdminPositionPageVo adminPositionPageVo = new AdminPositionPageVo();
            adminPositionPageVo.setList(page1);
            adminPositionPageVo.setTotal(adminPositionVOS.size());
            if (sum != 1 && (sortColmn == null || "".equals(sortColmn))) {

                PageInfo pageInfo = new PageInfo(page);
                adminPositionPageVo.setTotal(Integer.valueOf(pageInfo.getTotal() + ""));
            }
            adminPositionPageVo.setPageNum(pageNum);
            adminPositionPageVo.setPageSize(pageSize);
            adminPositionPageVo.setAllProfitAndLose(allProfitAndLose);
            adminPositionPageVo.setProfitAndLose(profitAndLose);
            adminPositionPageVo.setOrderNum(orderNumber);
            adminPositionPageVo.setBuyTotalAmount(buyTotalAmount);
            return ServerResponse.createBySuccess(adminPositionPageVo);
        }
    }


    @Override
    public ServerResponse listByAdminNb(Integer sum, String sort, String sortColmn, Integer agentId, Integer positionType, Integer state, Integer userId, String positionSn, String beginTime, String endTime, int pageNum, int pageSize, Integer buyType) {
        Page<UserPosition> page = null;
        if (sum != 1 && (sortColmn == null || "".equals(sortColmn))) {
            page = PageHelper.startPage(pageNum, pageSize);
        }

        Timestamp begin_time = null;
        if (StringUtils.isNotBlank(beginTime)) {
            begin_time = DateTimeUtil.searchStrToTimestamp(beginTime);
        }
        Timestamp end_time = null;
        if (StringUtils.isNotBlank(endTime)) {
            end_time = DateTimeUtil.searchStrToTimestamp(endTime);
        }


        List<UserPosition> userPositions = this.userPositionMapper.listByAgentNb(positionType, state, userId, agentId, positionSn, begin_time, end_time, buyType, sort, sortColmn);
        List<AdminPositionVO> adminPositionVOS = Lists.newArrayList();

        BigDecimal profitAndLose = BigDecimal.ZERO;

        BigDecimal allProfitAndLose = BigDecimal.ZERO;
        for (UserPosition position : userPositions) {
            AdminPositionVO adminPositionVO = assembleAdminPositionVO(position);
            adminPositionVOS.add(adminPositionVO);
            if (adminPositionVO.getProfitAndLose() != null) {
                profitAndLose = profitAndLose.add(adminPositionVO.getProfitAndLose().subtract(adminPositionVO.getOrderFee()));
            }
            if (adminPositionVO.getAllProfitAndLose() != null) {
                allProfitAndLose = allProfitAndLose.add(adminPositionVO.getAllProfitAndLose());
            }
        }

        if (sortColmn != null) {
            switch (sortColmn) {
                case "sellOrderPrice":
                    adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                        @Override
                        //按照code的大小升序排列
                        public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                            int i = o1.getSellOrderPrice().compareTo(o2.getSellOrderPrice());
                            if ("ASC".equals(sort)) {
                                return i;
                            } else {
                                return Integer.compare(0, i);
                            }
                        }

                    });
                    break;
                case "buyOrderPrice":
                    adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                        @Override
                        //按照code的大小升序排列
                        public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                            int i = o1.getBuyOrderPrice().compareTo(o2.getBuyOrderPrice());
                            if ("ASC".equals(sort)) {
                                return i;
                            } else {
                                return Integer.compare(0, i);
                            }
                        }

                    });
                    break;
                case "now_price":
                    adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                        @Override
                        //按照code的大小升序排列
                        public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                            int i = new BigDecimal(o1.getNow_price()).compareTo(new BigDecimal(o2.getNow_price()));
                            if ("ASC".equals(sort)) {
                                return i;
                            } else {
                                return Integer.compare(0, i);
                            }
                        }

                    });
                    break;

                case "profitAndLose":
                    adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                        @Override
                        //按照code的大小升序排列
                        public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                            int i = o1.getProfitAndLose().compareTo(o2.getProfitAndLose());
                            if ("ASC".equals(sort)) {
                                return i;
                            } else {
                                return Integer.compare(0, i);
                            }
                        }

                    });
                    break;
                case "allProfitAndLose":
                    adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                        @Override
                        //按照code的大小升序排列
                        public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                            int i = o1.getAllProfitAndLose().compareTo(o2.getAllProfitAndLose());
                            if ("ASC".equals(sort)) {
                                return i;
                            } else {
                                return Integer.compare(0, i);
                            }
                        }

                    });
                    break;
            }
        }
        //adminPositionVOS 根據
        Pager<AdminPositionVO> pager = Pager.create(adminPositionVOS, pageSize);
        List<AdminPositionVO> page1 = pager.getPagedList(pageNum);

        AdminPositionPageVo adminPositionPageVo = new AdminPositionPageVo();
        adminPositionPageVo.setList(page1);
        adminPositionPageVo.setTotal(adminPositionVOS.size());
        if (sum != 1 && (sortColmn == null || "".equals(sortColmn))) {
            PageInfo pageInfo = new PageInfo(page);
            adminPositionPageVo.setTotal(Integer.valueOf(pageInfo.getTotal() + ""));
        }
        adminPositionPageVo.setPageNum(pageNum);
        adminPositionPageVo.setPageSize(pageSize);
        adminPositionPageVo.setAllProfitAndLose(allProfitAndLose);
        adminPositionPageVo.setProfitAndLose(profitAndLose);

        return ServerResponse.createBySuccess(adminPositionPageVo);
    }

    @Override
    public int CountPositionNum(Integer state, Integer accountType) {
        return this.userPositionMapper.CountPositionNum(state, accountType);
    }

    @Override
    public BigDecimal CountPositionProfitAndLose() {
        return this.userPositionMapper.CountPositionProfitAndLose();
    }

    @Override
    public BigDecimal CountPositionAllProfitAndLose() {
        return this.userPositionMapper.CountPositionAllProfitAndLose();
    }

    @Override
    public ServerResponse create(Integer userId, String stockCode, String buyPrice, String buyTime, Integer buyNum, Integer buyType, BigDecimal lever) {
        if (userId == null || StringUtils.isBlank(buyPrice) || StringUtils.isBlank(stockCode) ||
                StringUtils.isBlank(buyTime) || buyNum == null || buyType == null || lever == null) {

            return ServerResponse.createByErrorMsg("參數不能為空");
        }

        User user = this.userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return ServerResponse.createByErrorMsg("用戶不存在");
        }
        if (user.getAccountType().intValue() != 1) {
            return ServerResponse.createByErrorMsg("正式用戶不能生成持倉單");
        }

        Stock stock = (Stock) this.iStockService.findStockByCode(stockCode).getData();
        if (stock == null) {
            return ServerResponse.createByErrorMsg("股票不存在");
        }


        SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
        if (siteSetting == null) {
            log.error("下單出錯，網站設置表不存在");
            return ServerResponse.createByErrorMsg("下單失敗，系統設置錯誤");
        }
        BigDecimal user_enable_amt = user.getEnableAmt();

        if (buyType == 1) {
            user_enable_amt = user.getEnableIndexAmt();

        }


        BigDecimal buy_amt = (new BigDecimal(buyPrice)).multiply(new BigDecimal(buyNum.intValue()));
        BigDecimal buy_amt_autual = buy_amt.divide(new BigDecimal(lever.intValue()), 2, 4);


        int compareUserAmtInt = user_enable_amt.compareTo(buy_amt_autual);
        log.info("用戶可用金額 = {}  實際購買金額 =  {}", user_enable_amt, buy_amt_autual);
        log.info("比較 用戶金額 和 實際 購買金額 =  {}", Integer.valueOf(compareUserAmtInt));
        if (compareUserAmtInt == -1) {
            return ServerResponse.createByErrorMsg("下單失敗，用戶可用金額小於" + buy_amt_autual + "元");
        }


        BigDecimal reckon_enable = user_enable_amt.subtract(buy_amt_autual);
        if (buyType == 1) {
            user.setEnableIndexAmt(reckon_enable);
        } else {
            user.setEnableAmt(reckon_enable);
        }
        int updateUserCount = this.userMapper.updateByPrimaryKeySelective(user);
        if (updateUserCount > 0) {
            log.info("【用戶交易下單】修改用戶金額成功");
        } else {
            log.error("用戶交易下單】修改用戶金額出錯");
        }


        UserPosition userPosition = userPositionMapper.selectOne(new QueryWrapper<UserPosition>().eq("user_id", user.getId()).eq("stock_code", stock.getStockCode()).eq("buy_type", buyType));
        if (userPosition == null) {
            userPosition = new UserPosition();
            userPosition.setPositionType(user.getAccountType());
            userPosition.setUserId(user.getId());
            userPosition.setNickName(user.getRealName());
            userPosition.setAgentId(user.getAgentId());
            userPosition.setStockCode(stock.getStockCode());
            userPosition.setStockName(stock.getStockName());
            userPosition.setStockGid(stock.getStockGid());
            userPosition.setStockSpell(stock.getStockSpell());
            userPosition.setBuyType(buyType);
            if (stock.getStockPlate() != null) {
                userPosition.setStockPlate(stock.getStockPlate());
            }
            userPosition.setIsLock(Integer.valueOf(0));
            userPosition.setOrderLever(lever);
            userPosition.setOrderDirection("買漲");
            userPosition.setOrderStayDays(0);
            userPosition.setOrderStayFee(new BigDecimal("0"));

            userPosition.setBuyOrderPrice(BigDecimal.ZERO);
            userPosition.setOrderNum(0);
            userPosition.setOrderTotalPrice(BigDecimal.ZERO);
            this.userPositionMapper.insert(userPosition);
        }

        if (userPosition.getOrderNum() != null && userPosition.getOrderNum() != 0) {
            userPosition.setBuyOrderTime(new Date());
            userPosition.setMarginAdd(BigDecimal.ZERO);
            userPosition.setPositionSn(KeyUtils.getUniqueKey());
        }
        userPosition.setOrderTotalPrice(userPosition.getOrderTotalPrice().add(buy_amt));
        userPosition.setOrderNum(userPosition.getOrderNum() + buyNum);
        userPosition.setBuyOrderPrice(userPosition.getOrderTotalPrice().divide(new BigDecimal(userPosition.getOrderNum()), 2, RoundingMode.DOWN));
        BigDecimal buy_fee_amt = buy_amt.multiply(siteSetting.getBuyFee()).setScale(2, 4);
        log.info("用戶購買手續費（配資后總資金 * 百分比） = {}", buy_fee_amt);
        userPosition.setOrderFee(userPosition.getOrderFee().add(buy_fee_amt));
//        BigDecimal buy_yhs_amt = buy_amt.multiply(siteSetting.getDutyFee()).setScale(2, 4);
//        log.info("用戶購買印花稅（配資后總資金 * 百分比） = {}", buy_yhs_amt);
//        userPosition.setOrderSpread(buy_yhs_amt);
        BigDecimal profit_and_lose = new BigDecimal("0");
        userPosition.setProfitAndLose(profit_and_lose);


        BigDecimal all_profit_and_lose = profit_and_lose.subtract(buy_fee_amt);
        userPosition.setAllProfitAndLose(all_profit_and_lose);


        this.userPositionMapper.updateByPrimaryKey(userPosition);
        UserPositionItem userPositionItem = new UserPositionItem();
        userPositionItem.setPositionType(userPosition.getPositionType());
        userPositionItem.setPositionSn(userPosition.getPositionSn());
        userPositionItem.setUserId(userPosition.getUserId());
        userPositionItem.setNickName(userPosition.getNickName());
        userPositionItem.setAgentId(userPosition.getAgentId());
        userPositionItem.setStockName(userPosition.getStockName());
        userPositionItem.setStockCode(userPosition.getStockCode());
        userPositionItem.setStockGid(userPosition.getStockGid());
        userPositionItem.setStockSpell(userPosition.getStockSpell());
        userPositionItem.setBuyType(userPosition.getBuyType());
        userPositionItem.setStockPlate(userPosition.getStockPlate());
        userPositionItem.setOrderTime(new Date());
        userPositionItem.setBuyOrderPrice(new BigDecimal(buyPrice));
        userPositionItem.setSellOrderPrice(new BigDecimal("0"));
        userPositionItem.setOrderDirection(userPosition.getOrderDirection());
        userPositionItem.setOrderNum(buyNum);
        userPositionItem.setOrderLever(lever);
        userPositionItem.setOrderTotalPrice(buy_amt);
        userPositionItem.setOrderFee(buy_fee_amt);
        userPositionItem.setOrderType(0);
        int insertPositionCount = userPositionItemMapper.insert(userPositionItem);


//        BigDecimal buy_fee_amt = buy_amt.multiply(siteSetting.getBuyFee()).setScale(2, 4);
//        log.info("創建模擬持倉 手續費（配資后總資金 * 百分比） = {}", buy_fee_amt);
//        userPosition.setOrderFee(buy_fee_amt);
//
//
//        BigDecimal buy_yhs_amt = buy_amt.multiply(siteSetting.getDutyFee()).setScale(2, 4);
//        log.info("創建模擬持倉 印花稅（配資后總資金 * 百分比） = {}", buy_yhs_amt);
//        userPosition.setOrderSpread(buy_yhs_amt);
//
//
//        BigDecimal profit_and_lose = new BigDecimal("0");
//        userPosition.setProfitAndLose(profit_and_lose);
//
//
//        BigDecimal all_profit_and_lose = profit_and_lose.subtract(buy_fee_amt).subtract(buy_yhs_amt);
//        userPosition.setAllProfitAndLose(all_profit_and_lose);
//
//
//        userPosition.setOrderStayDays(Integer.valueOf(0));
//        userPosition.setOrderStayFee(new BigDecimal("0"));
//        userPosition.setSpreadRatePrice(new BigDecimal("0"));
//
//        int insertPositionCount = this.userPositionMapper.insert(userPosition);
        if (insertPositionCount > 0) {
            log.info("【創建模擬持倉】保存記錄成功");
        } else {
            log.error("【創建模擬持倉】保存記錄出錯");
        }

        return ServerResponse.createBySuccess("生成模擬持倉成功");
    }

    @Override
    public int deleteByUserId(Integer userId) {
        return this.userPositionMapper.deleteByUserId(userId);
    }

    @Override
    public void doClosingStayTask() {
        List<UserPosition> userPositions = this.userPositionMapper.findAllStayPosition();


        if (userPositions.size() > 0) {
            log.info("查詢到正在持倉的訂單數量 = {}", Integer.valueOf(userPositions.size()));

            SiteProduct siteProduct = iSiteProductService.getProductSetting();
            if (!siteProduct.getHolidayDisplay()) {
                for (UserPosition position : userPositions) {
                    int stayDays = GetStayDays.getDays(GetStayDays.getBeginDate(position.getBuyOrderTime()));
                    //遞延費特殊處理
                    stayDays = stayDays + 1;

                    log.info("");
                    log.info("開始處理 持倉訂單id = {} 訂單號 = {} 用戶id = {} realName = {} 留倉天數 = {}", new Object[]{position
                            .getId(), position.getPositionSn(), position.getUserId(), position
                            .getNickName(), Integer.valueOf(stayDays)});

                    if (stayDays != 0) {
                        log.info(" 開始收取 {} 天 留倉費", Integer.valueOf(stayDays));
                        try {
                            closingStayTask(position, Integer.valueOf(stayDays));
                        } catch (Exception e) {
                            log.error("doClosingStayTask = ", e);


                        }


                    } else {


                        log.info("持倉訂單 = {} ,持倉天數0天,不需要處理...", position.getId());
                    }

                    log.info("修改留倉費 處理結束。");
                    log.info("");
                }

                SiteTaskLog stl = new SiteTaskLog();
                stl.setTaskType("扣除留倉費");
                stl.setAddTime(new Date());
                stl.setIsSuccess(Integer.valueOf(0));
                stl.setTaskTarget("扣除留倉費，訂單數量為" + userPositions.size());
                this.siteTaskLogMapper.insert(stl);
            }
        } else {
            log.info("doClosingStayTask沒有正在持倉的訂單");
        }
    }

//    /*留倉到期強制平倉，每天15點執行*/
//    @Override
//    public void expireStayUnwindTask() {
//        List<UserPosition> userPositions = this.userPositionMapper.findAllStayPosition();
//
//
//        if (userPositions.size() > 0) {
//            log.info("查詢到正在持倉的訂單數量 = {}", Integer.valueOf(userPositions.size()));
//
//            SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
//            for (UserPosition position : userPositions) {
//                int stayDays = GetStayDays.getDays(GetStayDays.getBeginDate(position.getBuyOrderTime()));
//
//                log.info("");
//                log.info("開始處理 持倉訂單id = {} 訂單號 = {} 用戶id = {} realName = {} 留倉天數 = {}", new Object[]{position
//                        .getId(), position.getPositionSn(), position.getUserId(), position
//                        .getNickName(), Integer.valueOf(stayDays)});
//
//                //留倉達到最大天數
//                if (stayDays >= siteSetting.getStayMaxDays().intValue()) {
//                    log.info(" 開始強平 {} 天", Integer.valueOf(stayDays));
//                    try {
//                        this.sell(position.getPositionSn(), 0,0);
//                    } catch (Exception e) {
//                        log.error("expireStayUnwindTask = ", e);
//                    }
//                } else {
//                    log.info("持倉訂單 = {} ,持倉天數0天,不需要處理...", position.getId());
//                }
//            }
//        } else {
//            log.info("doClosingStayTask沒有正在持倉的訂單");
//        }
//    }

    @Transactional
    @Override
    public ServerResponse closingStayTask(UserPosition position, Integer stayDays) throws Exception {
        log.info("=================closingStayTask====================");
        log.info("修改留倉費，持倉id={},持倉天数={}", position.getId(), stayDays);

        SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
        if (siteSetting == null) {
            log.error("修改留倉費出錯，網站設置表不存在");
            return ServerResponse.createByErrorMsg("修改留倉費出錯，網站設置表不存在");
        }


        BigDecimal stayFee = position.getOrderTotalPrice().multiply(siteSetting.getStayFee());

        BigDecimal allStayFee = stayFee.multiply(new BigDecimal(stayDays.intValue()));

        log.info("總留倉費 = {}", allStayFee);


        position.setOrderStayFee(allStayFee);
        position.setOrderStayDays(stayDays);

        BigDecimal all_profit = position.getAllProfitAndLose().subtract(allStayFee);
        position.setAllProfitAndLose(all_profit);

        int updateCount = this.userPositionMapper.updateByPrimaryKeySelective(position);
        if (updateCount > 0) {
            //核算代理收入-延遞費
//            iAgentAgencyFeeService.AgencyFeeIncome(3, position.getPositionSn());
            log.info("【closingStayTask收持倉費】修改持倉記錄成功");
        } else {
            log.error("【closingStayTask收持倉費】修改持倉記錄出錯");
            throw new Exception("【closingStayTask收持倉費】修改持倉記錄出錯");
        }


        log.info("=======================================================");
        return ServerResponse.createBySuccess();
    }

    @Override
    public List<Integer> findDistinctUserIdList() {
        return this.userPositionMapper.findDistinctUserIdList();
    }

    @Override
    public List<Integer> findDistinctUserIdListAndRz() {
        return this.userPositionMapper.findDistinctUserIdListAndRz();
    }

    @Override
    public ServerResponse updatePrice(String positionSn, BigDecimal buyPrice, Date orderTime) {
        UserPosition position = userPositionMapper.findPositionBySn(positionSn);

        List<UserPositionItem> userPositionItemList = userPositionItemMapper.selectList(new QueryWrapper<UserPositionItem>().eq("position_sn", positionSn));
        SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();

        position.setBuyOrderPrice(buyPrice);
        position.setBuyOrderTime(orderTime);
        position.setOrderTotalPrice(new BigDecimal(position.getOrderNum()).multiply(buyPrice));
        for (UserPositionItem userPositionItem : userPositionItemList) {
            if (userPositionItem.getBuyType() == 0) {
                userPositionItem.setOrderTotalPrice(new BigDecimal(userPositionItem.getOrderNum()).multiply(buyPrice));
                userPositionItem.setBuyOrderPrice(buyPrice);
                userPositionItem.setOrderTime(orderTime);
                BigDecimal buy_fee_amt = userPositionItem.getOrderTotalPrice().multiply(siteSetting.getBuyFee()).setScale(2, RoundingMode.HALF_UP);
                userPositionItem.setOrderFee(buy_fee_amt);
                userPositionItemMapper.updateById(userPositionItem);
            }
        }
        userPositionMapper.updateById(position);
        return ServerResponse.createBySuccess();
    }

    @Override
    @Transactional
    public void sellByAll(List<PositionProfitVO> positionProfitVOList) {
        User user = userMapper.selectById(positionProfitVOList.get(0).getUserId());

        //拿出所有的PositionSn
        List<String> positionSnList = positionProfitVOList.stream().map(PositionProfitVO::getPositionSn).collect(Collectors.toList());
        List<UserPositionItem> positionItemList = userPositionItemMapper.selectList(new QueryWrapper<UserPositionItem>().in("position_sn", positionSnList));
        //positionItemList按照positionSn分组
        Map<String, List<UserPositionItem>> positionItemMap = positionItemList.stream().collect(Collectors.groupingBy(UserPositionItem::getPositionSn));


        //用戶強制平倉
        for (PositionProfitVO positionProfitVO : positionProfitVOList) {
            UserPosition userPosition = positionProfitVO.getUserPosition();
            Integer sellNumber = positionProfitVO.getUserPosition().getOrderNum();
            BigDecimal now_price = new BigDecimal(positionProfitVO.getNowPrice());
            BigDecimal all_sell_amt = now_price.multiply(new BigDecimal(sellNumber));

            log.info("買入總金額 = {} , 賣出總金額 = {} , 盈虧 = {}", positionProfitVO.getBuyTotalAmount(), positionProfitVO.getSellTotalAmount(), positionProfitVO.getProfitAndLose());

            BigDecimal user_all_amt = user.getUserIndexAmt();
            BigDecimal user_enable_amt = user.getEnableIndexAmt();

            log.info("用戶原本總資金 = {} , 可用 = {}", user_all_amt, user_enable_amt);
            //盈亏
            BigDecimal profitLoss = positionProfitVO.getProfitAndLose();
            log.info("盈亏 = {}", profitLoss);

            BigDecimal buy_fee_amt = positionProfitVO.getBuyFee();
            log.info("買入手續費 = {}", buy_fee_amt);

            BigDecimal orderSpread = positionProfitVO.getDutyFee();
            log.info("印花稅 = {}", orderSpread);

            BigDecimal sell_fee_amt = positionProfitVO.getSellFee();
            log.info("賣出手續費 = {}", sell_fee_amt);

            //總手續費= 買入手續費+賣出手續費+印花稅+遞延費+點差費
            BigDecimal all_fee_amt = buy_fee_amt.add(sell_fee_amt).add(orderSpread);


            log.info("總的手續費費用 = {}", all_fee_amt);
            BigDecimal all_profit = profitLoss.subtract(all_fee_amt);
            userPosition.setOrderNum(0);
            userPosition.setOrderTotalPrice(userPosition.getBuyOrderPrice().multiply(new BigDecimal(userPosition.getOrderNum())));
            int updatePositionCount = this.userPositionMapper.updateByPrimaryKeySelective(userPosition);
            UserPositionItem userPositionItem = new UserPositionItem();
            userPositionItem.setPositionType(userPosition.getPositionType());
            userPositionItem.setPositionSn(userPosition.getPositionSn());
            userPositionItem.setUserId(userPosition.getUserId());
            userPositionItem.setNickName(userPosition.getNickName());
            userPositionItem.setAgentId(userPosition.getAgentId());
            userPositionItem.setStockGid(userPosition.getStockGid());
            userPositionItem.setBuyOrderPrice(userPosition.getBuyOrderPrice());
            userPositionItem.setStockName(userPosition.getStockName());
            userPositionItem.setStockCode(userPosition.getStockCode());
            userPositionItem.setStockSpell(userPosition.getStockSpell());
            userPositionItem.setBuyType(userPosition.getBuyType());
            userPositionItem.setStockPlate(userPosition.getStockPlate());
            userPositionItem.setOrderTime(new Date());
            userPositionItem.setSellOrderPrice(now_price);
            userPositionItem.setOrderDirection(userPosition.getOrderDirection());
            userPositionItem.setOrderNum(sellNumber);
            userPositionItem.setOrderLever(userPosition.getOrderLever());
            userPositionItem.setOrderTotalPrice(all_sell_amt);
            userPositionItem.setOrderFee(sell_fee_amt);
            userPositionItem.setOrderSpread(orderSpread);
            userPositionItem.setProfitAndLose(profitLoss);
            userPositionItem.setAllProfitAndLose(all_profit);
            userPositionItem.setOrderType(1);
            userPositionItemMapper.insert(userPositionItem);


            if (updatePositionCount > 0) {
                log.info("【用戶平倉】修改浮動盈虧記錄成功");
            } else {
                log.error("用戶平倉】修改浮動盈虧記錄出錯");
            }

            //買入消費金額
            BigDecimal freez_amt = positionProfitVO.getBuyTotalAmount();
            //修改用戶可用餘額=當前可用餘額+總盈虧+買入總金額+追加保證金
            user_enable_amt = user_enable_amt.add(freez_amt).add(profitLoss).subtract(sell_fee_amt).subtract(orderSpread);
            user_all_amt = user_all_amt.add(freez_amt).add(profitLoss).subtract(sell_fee_amt).subtract(orderSpread);


            user.setUserIndexAmt(user_all_amt);
            user.setEnableIndexAmt(user_enable_amt);
            //如果UserIndexAmt小於0，則設置為0 继续从可用setEnableAmt中扣除

            if (user.getUserIndexAmt().compareTo(BigDecimal.ZERO) < 0) {
                user.setEnableAmt(user.getEnableAmt().add(user.getEnableIndexAmt()));
                user.setUserAmt(user.getUserAmt().add(user.getUserIndexAmt()));
                user.setEnableIndexAmt(BigDecimal.ZERO);
                user.setUserIndexAmt(BigDecimal.ZERO);
            }
            int updateUserCount = this.userMapper.updateByPrimaryKeySelective(user);
            if (updateUserCount > 0) {
                log.info("【用戶平倉】修改用戶金額成功");
            } else {
                log.error("用戶平倉】修改用戶金額出錯");
            }
            UserCashDetail ucd = new UserCashDetail();
            ucd.setPositionId(userPosition.getId());
            ucd.setAgentId(user.getAgentId());
            ucd.setAgentName(user.getAgentName());
            ucd.setUserId(user.getId());
            ucd.setUserName(user.getRealName());
            ucd.setDeType("强制平仓-賣出股票(" + (userPosition.getBuyType() == 0 ? "現股" : "國際金") + ")");
            ucd.setDeAmt(positionProfitVO.getBuyTotalAmount().add(profitLoss).subtract(sell_fee_amt).subtract(orderSpread));
            ucd.setDeSummary("强制平仓-股票，" + userPosition.getStockCode() + "/" + userPosition.getStockName() + ",佔用本金：" + freez_amt + ",總手續費：" + all_fee_amt + ",印花稅：" + orderSpread + ",盈虧：" + profitLoss + "，总盈虧：" + all_profit);
            ucd.setAddTime(new Date());
            ucd.setIsRead(0);
            this.userCashDetailMapper.insert(ucd);
        }
    }

    @Override
    public PositionVO findUserFutPositionAllProfitAndLose(Integer userId) {
        List<UserPositionFutures> userPositions = this.userPositionFuturesMapper.findPositionByUserIdAndSellIdIsNull(userId);
        BigDecimal allProfitAndLose = new BigDecimal("0");

        BigDecimal userIndexBuyAmount = BigDecimal.ZERO;


        List<FuturesVO> stockListVOList = new ArrayList<>();
        for (UserPositionFutures position : userPositions) {
            FuturesVO futuresVO = iStockFuturesService.querySingleMarket(position.getStockCode());
            stockListVOList.add(futuresVO);
        }

        for (UserPositionFutures position : userPositions) {
            BigDecimal nowPrice = null;
            for (FuturesVO stockListVOBy : stockListVOList) {


                if (stockListVOBy.getCode().equals(position.getStockCode())) {
                    nowPrice = new BigDecimal(stockListVOBy.getNowPrice());
                    userIndexBuyAmount = userIndexBuyAmount.add(new BigDecimal(position.getOrderNum()));
                    //浮動盈虧
                    BigDecimal profitAndLose = getLose(position.getBuyOrderPrice(),nowPrice,new BigDecimal(position.getOrderNum()),position.getOrderLever().intValue());
                    allProfitAndLose = allProfitAndLose.add(profitAndLose);
                }
            }
        }
        PositionVO positionVO = new PositionVO();
        positionVO.setAllProfitAndLose(allProfitAndLose);
        positionVO.setUserIndexBuyAmount(userIndexBuyAmount);
        return positionVO;
    }
    private BigDecimal getLose(BigDecimal buyPrice,BigDecimal sellPrice,BigDecimal orderNum,Integer level){
        //涨跌幅
        BigDecimal rate=  sellPrice.divide(buyPrice,6, RoundingMode.HALF_UP).subtract(BigDecimal.ONE);
        rate = rate.multiply(new BigDecimal(level));
        return orderNum.multiply(rate);
    }



    private AdminPositionVO assembleAdminPositionVO(UserPosition position) {
        AdminPositionVO adminPositionVO = new AdminPositionVO();

        adminPositionVO.setId(position.getId());
        adminPositionVO.setPositionSn(position.getPositionSn());
        adminPositionVO.setPositionType(position.getPositionType());
        adminPositionVO.setUserId(position.getUserId());
        adminPositionVO.setNickName(position.getNickName());
        adminPositionVO.setAgentId(position.getAgentId());
        adminPositionVO.setStockName(position.getStockName());
        adminPositionVO.setStockCode(position.getStockCode());
        adminPositionVO.setStockGid(position.getStockGid());
        adminPositionVO.setStockSpell(position.getStockSpell());
        adminPositionVO.setBuyOrderId(position.getBuyOrderId());
        adminPositionVO.setBuyOrderTime(position.getBuyOrderTime());
        adminPositionVO.setBuyOrderPrice(position.getBuyOrderPrice());
        adminPositionVO.setSellOrderId(position.getSellOrderId());
        adminPositionVO.setSellOrderTime(position.getSellOrderTime());
        adminPositionVO.setSellOrderPrice(position.getSellOrderPrice());
        adminPositionVO.setOrderDirection("買漲".equals(position.getOrderDirection())?1:2);
        adminPositionVO.setOrderNum(position.getOrderNum());
        adminPositionVO.setOrderLever(position.getOrderLever());
        adminPositionVO.setOrderTotalPrice(position.getOrderTotalPrice());
        adminPositionVO.setOrderFee(position.getOrderFee());
        adminPositionVO.setOrderSpread(position.getOrderSpread());
        adminPositionVO.setOrderStayFee(position.getOrderStayFee());
        adminPositionVO.setOrderStayDays(position.getOrderStayDays());
        adminPositionVO.setIsLock(position.getIsLock());
        adminPositionVO.setLockMsg(position.getLockMsg());
        adminPositionVO.setStockPlate(position.getStockPlate());

        if (position.getSellOrderTime() != null) {
            adminPositionVO.setProfitAndLose(position.getProfitAndLose());
            adminPositionVO.setAllProfitAndLose(position.getAllProfitAndLose());
            adminPositionVO.setNow_price(position.getSellOrderPrice().toString());
        } else {
            PositionProfitVO positionProfitVO = getPositionProfitVO(position);
            adminPositionVO.setProfitAndLose(positionProfitVO.getProfitAndLose());
            adminPositionVO.setAllProfitAndLose(positionProfitVO.getAllProfitAndLose());
            adminPositionVO.setNow_price(positionProfitVO.getNowPrice());
            adminPositionVO.setSellTotalAmount(positionProfitVO.getSellTotalAmount());
            adminPositionVO.setBuyTotalAmount(positionProfitVO.getBuyTotalAmount());
            adminPositionVO.setOrderFee(positionProfitVO.getBuyFee());
        }
        //付出成本
        adminPositionVO.setOrderTotalPrice(position.getBuyOrderPrice().multiply(new BigDecimal(position.getOrderNum())).divide(position.getOrderLever(), 2, RoundingMode.HALF_UP));


        return adminPositionVO;
    }

    private AdminPositionVO assembleAdminPositionItemVO(UserPositionItem position) {
        AdminPositionVO adminPositionVO = new AdminPositionVO();

        adminPositionVO.setId(position.getId());
        adminPositionVO.setPositionSn(position.getPositionSn());
        adminPositionVO.setPositionType(position.getPositionType());
        adminPositionVO.setUserId(position.getUserId());
        adminPositionVO.setNickName(position.getNickName());
        adminPositionVO.setAgentId(position.getAgentId());
        adminPositionVO.setStockName(position.getStockName());
        adminPositionVO.setStockCode(position.getStockCode());
        adminPositionVO.setStockGid(position.getStockGid());
        adminPositionVO.setStockSpell(position.getStockSpell());
        adminPositionVO.setBuyOrderTime(position.getOrderTime());
        adminPositionVO.setBuyOrderPrice(position.getBuyOrderPrice());
        adminPositionVO.setSellOrderTime(position.getOrderTime());
        adminPositionVO.setSellOrderPrice(position.getSellOrderPrice());
        adminPositionVO.setOrderDirection("買漲".equals(position.getOrderDirection())?1:2);
        adminPositionVO.setOrderNum(position.getOrderNum());
        adminPositionVO.setOrderLever(position.getOrderLever());
        adminPositionVO.setOrderTotalPrice(position.getOrderTotalPrice());
        adminPositionVO.setOrderFee(position.getOrderFee());
        adminPositionVO.setOrderSpread(position.getOrderSpread());
        adminPositionVO.setStockPlate(position.getStockPlate());

        adminPositionVO.setProfitAndLose(position.getProfitAndLose());
        adminPositionVO.setAllProfitAndLose(position.getAllProfitAndLose());
        adminPositionVO.setNow_price(position.getSellOrderPrice().toString());
        //付出成本
        adminPositionVO.setOrderTotalPrice(position.getBuyOrderPrice().multiply(new BigDecimal(position.getOrderNum())).divide(position.getOrderLever(), 2, RoundingMode.HALF_UP));


        return adminPositionVO;
    }

    private AgentPositionVO assembleAgentPositionVO(UserPosition position) {
        AgentPositionVO agentPositionVO = new AgentPositionVO();

        agentPositionVO.setId(position.getId());
        agentPositionVO.setPositionSn(position.getPositionSn());
        agentPositionVO.setPositionType(position.getPositionType());
        agentPositionVO.setUserId(position.getUserId());
        agentPositionVO.setNickName(position.getNickName());
        agentPositionVO.setAgentId(position.getAgentId());
        agentPositionVO.setStockName(position.getStockName());
        agentPositionVO.setStockCode(position.getStockCode());
        agentPositionVO.setStockGid(position.getStockGid());
        agentPositionVO.setStockSpell(position.getStockSpell());
        agentPositionVO.setBuyOrderId(position.getBuyOrderId());
        agentPositionVO.setBuyOrderTime(position.getBuyOrderTime());
        agentPositionVO.setBuyOrderPrice(position.getBuyOrderPrice());
        agentPositionVO.setSellOrderId(position.getSellOrderId());
        agentPositionVO.setSellOrderTime(position.getSellOrderTime());
        agentPositionVO.setSellOrderPrice(position.getSellOrderPrice());
        agentPositionVO.setOrderDirection(position.getOrderDirection());
        agentPositionVO.setOrderNum(position.getOrderNum());
        agentPositionVO.setOrderLever(position.getOrderLever());
        agentPositionVO.setOrderTotalPrice(position.getOrderTotalPrice());
        agentPositionVO.setOrderFee(position.getOrderFee());
        agentPositionVO.setOrderSpread(position.getOrderSpread());
        agentPositionVO.setOrderStayFee(position.getOrderStayFee());
        agentPositionVO.setOrderStayDays(position.getOrderStayDays());

        agentPositionVO.setIsLock(position.getIsLock());
        agentPositionVO.setLockMsg(position.getLockMsg());

        agentPositionVO.setStockPlate(position.getStockPlate());

        if (position.getSellOrderTime() != null) {
            agentPositionVO.setProfitAndLose(position.getProfitAndLose());
            agentPositionVO.setAllProfitAndLose(position.getAllProfitAndLose());
            agentPositionVO.setNow_price(position.getSellOrderPrice().toString());
        } else {
            PositionProfitVO positionProfitVO = getPositionProfitVO(position);
            agentPositionVO.setProfitAndLose(positionProfitVO.getProfitAndLose());
            agentPositionVO.setAllProfitAndLose(positionProfitVO.getAllProfitAndLose());
            agentPositionVO.setNow_price(positionProfitVO.getNowPrice());
        }


        return agentPositionVO;
    }

    private UserPositionVO assembleUserPositionVO(UserPosition position) {
        UserPositionVO userPositionVO = new UserPositionVO();

        userPositionVO.setId(position.getId());
        userPositionVO.setPositionType(position.getPositionType());
        userPositionVO.setPositionSn(position.getPositionSn());
        userPositionVO.setUserId(position.getUserId());
        userPositionVO.setNickName(position.getNickName());
        userPositionVO.setAgentId(position.getAgentId());
        userPositionVO.setStockName(position.getStockName());
        userPositionVO.setStockCode(position.getStockCode());
        userPositionVO.setStockGid(position.getStockGid());
        userPositionVO.setStockSpell(position.getStockSpell());
        userPositionVO.setBuyOrderId(position.getBuyOrderId());
        userPositionVO.setBuyOrderTime(position.getBuyOrderTime());
        userPositionVO.setBuyOrderPrice(position.getBuyOrderPrice());
        userPositionVO.setSellOrderId(position.getSellOrderId());
        userPositionVO.setSellOrderTime(position.getSellOrderTime());
        userPositionVO.setSellOrderPrice(position.getSellOrderPrice());
        userPositionVO.setProfitTargetPrice(position.getProfitTargetPrice());
        userPositionVO.setBuyType(position.getBuyType());
        userPositionVO.setStopTargetPrice(position.getStopTargetPrice());
        userPositionVO.setOrderDirection("買漲".equals(position.getOrderDirection())?1:2);
        userPositionVO.setOrderNum(position.getOrderNum());
        userPositionVO.setOrderLever(position.getOrderLever());
        userPositionVO.setOrderFee(position.getOrderFee());
        userPositionVO.setOrderSpread(position.getOrderSpread());
        userPositionVO.setOrderStayFee(position.getOrderStayFee());
        userPositionVO.setOrderStayDays(position.getOrderStayDays());
        userPositionVO.setMarginAdd(position.getMarginAdd());

        userPositionVO.setStockPlate(position.getStockPlate());
        userPositionVO.setSpreadRatePrice(position.getSpreadRatePrice());

        PositionProfitVO positionProfitVO = getPositionProfitVO(position);
        userPositionVO.setAllProfitAndLose(positionProfitVO.getAllProfitAndLose());
        userPositionVO.setNow_price(positionProfitVO.getNowPrice());
        //买入市值
        userPositionVO.setBuyTotalAmount(positionProfitVO.getBuyTotalAmount());
        //卖出市值
        userPositionVO.setSellTotalAmount(positionProfitVO.getSellTotalAmount());
        //付出成本
        userPositionVO.setOrderTotalPrice(positionProfitVO.getOrderTotalPrice());
        //浮动盈亏
        userPositionVO.setProfitAndLose(positionProfitVO.getProfitAndLose());
        userPositionVO.setOrderFee(positionProfitVO.getBuyFee());

        return userPositionVO;
    }

    @Override
    public PositionProfitVO getPositionProfitVO(UserPosition position) {

        String nowPrice = "";


        List<String> stockCodeList = new ArrayList<>();
        stockCodeList.add(position.getStockCode());

        List<StockListVO> stockListVOList = SinaStockApi.assembleStockListVO(
                SinaStockApi.getSinaStock(stockCodeList), stockCodeList);

        if (stockListVOList.size() > 0) {
            StockListVO stockListVO = stockListVOList.get(0);
            nowPrice = stockListVO.getNowPrice();
        } else {
            nowPrice = "0";
        }

        SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();

        PositionProfitVO positionProfitVO = new PositionProfitVO();
        //卖出市值
        positionProfitVO.setSellTotalAmount(new BigDecimal(nowPrice).multiply(new BigDecimal(position.getOrderNum())));
        //买入市值
        positionProfitVO.setBuyTotalAmount(position.getBuyOrderPrice().multiply(new BigDecimal(position.getOrderNum())));
        //盈亏= 卖出市值 - 买入市值
        positionProfitVO.setProfitAndLose(positionProfitVO.getSellTotalAmount().subtract(positionProfitVO.getBuyTotalAmount()));

        //成本
        positionProfitVO.setOrderTotalPrice(position.getBuyOrderPrice().multiply(new BigDecimal(position.getOrderNum())).divide(position.getOrderLever(), 2, RoundingMode.HALF_UP));

        //总盈亏 = 盈亏 - 买入手续费 - 预估卖出手续费 - 预估卖出印花税
        //买入手续费
        BigDecimal buy_fee_amt = position.getBuyOrderPrice().multiply(new BigDecimal(position.getOrderNum())).multiply(siteSetting.getBuyFee()).setScale(2, 4);
        //卖出手续费
        BigDecimal sell_fee_amt = new BigDecimal(nowPrice).multiply(new BigDecimal(position.getOrderNum())).multiply(siteSetting.getSellFee()).setScale(2, 4);
        //印花税
        BigDecimal orderSpread = new BigDecimal(nowPrice).multiply(new BigDecimal(position.getOrderNum())).multiply(siteSetting.getDutyFee());


        BigDecimal allProfitAndLose = positionProfitVO.getProfitAndLose().subtract(buy_fee_amt).subtract(sell_fee_amt).subtract(orderSpread);
        positionProfitVO.setAllProfitAndLose(allProfitAndLose);
        positionProfitVO.setNowPrice(nowPrice);
        positionProfitVO.setBuyFee(buy_fee_amt);

        return positionProfitVO;
    }

    /**
     * 查詢用戶持倉具體數據 - 根據當前價格計算
     *
     * @param userPositions
     * @return
     */
    @Override
    public List<PositionProfitVO> getPositionProfitVOList(List<UserPosition> userPositions) {

        List<PositionProfitVO> positionProfitVOList = new ArrayList<>();

        List<String> codeList = new ArrayList<>();

        for (UserPosition position : userPositions) {
            codeList.add(position.getStockCode());
        }

        log.error("強制平倉的數據具體查詢");
        List<StockListVO> stockListVOList = SinaStockApi.assembleStockListVO(
                SinaStockApi.getSinaStock(codeList), codeList);
        //將stockListVOList 轉換成map
        Map<String, StockListVO> stockListVOMap = new HashMap<>();
        for (StockListVO stockListVO : stockListVOList) {
            stockListVOMap.put(stockListVO.getCode(), stockListVO);
        }
        for (UserPosition position : userPositions) {
            StockListVO stockListVO = stockListVOMap.get(position.getStockCode());
            BigDecimal nowPrice = new BigDecimal(stockListVO.getNowPrice());

            PositionProfitVO positionProfitVO = new PositionProfitVO();
            positionProfitVO.setNowPrice(nowPrice.toString());

            //當前股票損益
            BigDecimal profitAndLose = nowPrice.subtract(position.getBuyOrderPrice()).multiply(new BigDecimal(position.getOrderNum()));
            positionProfitVO.setProfitAndLose(profitAndLose);
            //計算本金
            BigDecimal buyTotalAmount = position.getBuyOrderPrice().multiply(new BigDecimal(position.getOrderNum())).divide(position.getOrderLever(), 2, RoundingMode.HALF_UP);
            positionProfitVO.setBuyTotalAmount(buyTotalAmount);
            //當前市值  本金+ 盈虧
            BigDecimal orderTotalPrice = buyTotalAmount.add(profitAndLose);
            positionProfitVO.setOrderTotalPrice(orderTotalPrice);
            //計算手續費
            SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
            //買入手續費
            BigDecimal buyFee = buyTotalAmount.multiply(siteSetting.getBuyFee()).setScale(2, RoundingMode.HALF_UP);
            //賣出手续费
            BigDecimal sell_fee_amt = orderTotalPrice.multiply(siteSetting.getSellFee()).setScale(2, RoundingMode.HALF_UP);
            //證交稅
            BigDecimal orderSpread = orderTotalPrice.multiply(siteSetting.getDutyFee()).setScale(2, RoundingMode.HALF_UP);

            //賣出價格
            BigDecimal sellTotalAmount = orderTotalPrice.add(sell_fee_amt).add(orderSpread);
            positionProfitVO.setSellTotalAmount(sellTotalAmount);

            //總損益
            positionProfitVO.setAllProfitAndLose(profitAndLose.add(sell_fee_amt).add(orderSpread));
            positionProfitVO.setOrderLevel(position.getOrderLever());


            positionProfitVO.setBuyFee(buyFee);
            positionProfitVO.setSellFee(sell_fee_amt);
            positionProfitVO.setDutyFee(orderSpread);
            positionProfitVO.setUserId(position.getUserId());
            positionProfitVO.setPositionSn(position.getPositionSn());

            positionProfitVO.setUserPosition(position);
            positionProfitVOList.add(positionProfitVO);

        }
        return positionProfitVOList;
    }

    public PositionProfitVO getPositionProfitVOByStockVo(UserPosition position, StockListVO stockListVO) {

        String nowPrice = "";


        List<String> stockCodeList = new ArrayList<>();
        stockCodeList.add(position.getStockCode());
        nowPrice = stockListVO.getNowPrice();
        SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();

        PositionProfitVO positionProfitVO = new PositionProfitVO();
        //卖出市值
        positionProfitVO.setSellTotalAmount(new BigDecimal(nowPrice).multiply(new BigDecimal(position.getOrderNum())));
        //买入市值
        positionProfitVO.setBuyTotalAmount(position.getBuyOrderPrice().multiply(new BigDecimal(position.getOrderNum())));
        //盈亏= 卖出市值 - 买入市值
        positionProfitVO.setProfitAndLose(positionProfitVO.getSellTotalAmount().subtract(positionProfitVO.getBuyTotalAmount()));

        //成本
        positionProfitVO.setOrderTotalPrice(position.getBuyOrderPrice().multiply(new BigDecimal(position.getOrderNum())).divide(position.getOrderLever(), 2, RoundingMode.HALF_UP));

        //总盈亏 = 盈亏 - 买入手续费 - 预估卖出手续费 - 预估卖出印花税
        //买入手续费
        BigDecimal buy_fee_amt = position.getBuyOrderPrice().multiply(new BigDecimal(position.getOrderNum())).multiply(siteSetting.getBuyFee()).setScale(2, 4);
        //卖出手续费
        BigDecimal sell_fee_amt = new BigDecimal(nowPrice).multiply(new BigDecimal(position.getOrderNum())).multiply(siteSetting.getSellFee()).setScale(2, 4);
        //印花税
        BigDecimal orderSpread = new BigDecimal(nowPrice).multiply(new BigDecimal(position.getOrderNum())).multiply(siteSetting.getDutyFee());


        BigDecimal allProfitAndLose = positionProfitVO.getProfitAndLose().subtract(buy_fee_amt).subtract(sell_fee_amt).subtract(orderSpread);
        positionProfitVO.setAllProfitAndLose(allProfitAndLose);
        positionProfitVO.setNowPrice(nowPrice);
        positionProfitVO.setBuyFee(buy_fee_amt);

        return positionProfitVO;
    }


    @Override
    public ServerResponse sumByAdmin(Integer agentId, Integer positionType, Integer state, Integer userId, String positionSn, String beginTime, String endTime, Integer buyType) {


        return null;
    }

    @Override
    public ServerResponse findMyPositionByCodeAndSpellSum(HttpServletRequest request) {
        User user = this.iUserService.getCurrentUser(request);
        if (user == null) {
            return ServerResponse.createBySuccessMsg("請先登錄");
        }

        List<UserPosition> userPositions = this.userPositionMapper.findMyPositionByCodeAndSpell(user.getId(), null, null, 0);

        Map<String, Object> map = new HashMap<>();

        BigDecimal buyAmount = BigDecimal.ZERO;
        BigDecimal buyAmount2 = BigDecimal.ZERO;

        BigDecimal sellAmount = BigDecimal.ZERO;
        BigDecimal allProfitAndLose = BigDecimal.ZERO;
        String allProfitAndLoseBaifen = "";

        if (userPositions.size() > 0) {
            for (UserPosition position : userPositions) {

                UserPositionVO userPositionVO = assembleUserPositionVO(position);

                //总预估市值
                sellAmount = sellAmount.add(userPositionVO.getSellTotalAmount());
                buyAmount2 = buyAmount2.add(userPositionVO.getOrderTotalPrice());
                buyAmount = buyAmount.add(userPositionVO.getBuyTotalAmount());

                allProfitAndLose = allProfitAndLose.add(userPositionVO.getProfitAndLose().subtract(userPositionVO.getOrderFee()));
            }
        }

        if (allProfitAndLose.compareTo(BigDecimal.ZERO) == 0) {
            allProfitAndLoseBaifen = "0%";
        } else {
            NumberFormat percentInstance = NumberFormat.getPercentInstance();
            // 設置保留幾位小數，這裏設置的是保留兩位小數
            percentInstance.setMinimumFractionDigits(0);
            allProfitAndLoseBaifen = percentInstance.format(allProfitAndLose.doubleValue() / buyAmount2.doubleValue());
        }
        map.put("allProfitAndLoseBaifen", allProfitAndLoseBaifen);
        map.put("allProfitAndLose", allProfitAndLose);
        map.put("buyAmount", buyAmount);
        map.put("sellAmount", sellAmount);

        return ServerResponse.createBySuccess(map);
    }

    @Override
    public ServerResponse findMyPositionByCodeAndSpellV2(String stockCode, String stockSpell, Integer state, HttpServletRequest request, int pageNum, int pageSize) {
        User user = this.iUserService.getCurrentUser(request);
        SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();

        PageHelper.startPage(pageNum, pageSize);
        if (user == null) {
            return ServerResponse.createBySuccessMsg("請先登錄");
        }
        if (state == 0) {
            //查詢目前持倉數據
            List<UserPosition> userPositions = this.userPositionMapper.findMyPositionByCodeAndSpell(user.getId(), stockCode, stockSpell, state);
            List<UserPositionVO> userPositionVOS = Lists.newArrayList();
            if (userPositions.size() > 0) {
                for (UserPosition position : userPositions) {
                    UserPositionVO userPositionVO = assembleUserPositionVO(position);
                    List<UserPositionItem> userPositionItemList = userPositionItemMapper.selectList(new QueryWrapper<UserPositionItem>().eq("position_sn", position.getPositionSn()).orderByDesc("id"));

                    for (UserPositionItem userPositionItem : userPositionItemList) {
                        if (userPositionItem.getBuyType() == 0) {
                            //卖出市值
                            BigDecimal sellAmount = new BigDecimal(userPositionVO.getNow_price()).multiply(new BigDecimal(userPositionItem.getOrderNum()));
                            //买入市值
                            BigDecimal buyAmount = userPositionVO.getBuyOrderPrice().multiply(new BigDecimal(userPositionItem.getOrderNum()));
                            //盈亏= 卖出市值 - 买入市值
                            userPositionItem.setProfitAndLose(sellAmount.subtract(buyAmount));
                        }

                    }
                    userPositionVO.setUserPositionItemList(userPositionItemList);


                    userPositionVOS.add(userPositionVO);
                }
            }
            PageInfo pageInfo = new PageInfo(userPositions);
            pageInfo.setList(userPositionVOS);
            return ServerResponse.createBySuccess(pageInfo);
        } else {
            QueryWrapper<UserPositionItem> queryWrapper = new QueryWrapper<UserPositionItem>().eq("user_id", user.getId()).eq("order_type", 1);
            if (stockCode != null && (!"".equals(stockCode))) {
                queryWrapper = queryWrapper.like("stock_code", stockCode);
            }
            if (stockSpell != null && (!"".equals(stockSpell))) {
                queryWrapper = queryWrapper.like("stock_spell", stockSpell);
            }
            queryWrapper.orderByDesc("id");
            List<UserPositionItem> userPositionItemList = userPositionItemMapper.selectList(queryWrapper);


            for (UserPositionItem userPositionItem : userPositionItemList) {

                BigDecimal buy_fee_amt = userPositionItem.getBuyOrderPrice().multiply(new BigDecimal(userPositionItem.getOrderNum())).multiply(siteSetting.getBuyFee()).setScale(2, RoundingMode.HALF_UP);
                log.info("買入手續費 = {}", buy_fee_amt);

                BigDecimal sell_fee_amt = userPositionItem.getSellOrderPrice().multiply(new BigDecimal(userPositionItem.getOrderNum() + "")).multiply(siteSetting.getSellFee()).setScale(2, RoundingMode.HALF_UP);
                log.info("賣出手續費 = {}", sell_fee_amt);
                userPositionItem.setOrderFee(buy_fee_amt.add(sell_fee_amt));
                userPositionItem.setOrderTotalPrice(userPositionItem.getBuyOrderPrice().multiply(new BigDecimal(userPositionItem.getOrderNum())).divide(userPositionItem.getOrderLever(), 2, RoundingMode.HALF_UP));
            }
            PageInfo pageInfo = new PageInfo(userPositionItemList);
            pageInfo.setList(userPositionItemList);
            return ServerResponse.createBySuccess(pageInfo);
        }
    }


    @Override
    public List<AdminPositionVO> listByAdminExport(String sort, String sortColmn, Integer agentId, Integer positionType, Integer state, Integer userId, String positionSn, String beginTime, String endTime, int pageNum, int pageSize, Integer buyType) {


        Timestamp begin_time = null;
        if (StringUtils.isNotBlank(beginTime)) {
            begin_time = DateTimeUtil.searchStrToTimestamp(beginTime);
        }
        Timestamp end_time = null;
        if (StringUtils.isNotBlank(endTime)) {
            end_time = DateTimeUtil.searchStrToTimestamp(endTime);
        }


        if (state == 0) {
            List<UserPosition> userPositions = this.userPositionMapper.listByAgent(positionType, state, userId, agentId, positionSn, begin_time, end_time, buyType, sort, sortColmn);
            List<AdminPositionVO> adminPositionVOS = assembleAdminPositionVOList(userPositions);
            if (sortColmn != null) {
                switch (sortColmn) {
                    case "sellOrderPrice":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = o1.getSellOrderPrice().compareTo(o2.getSellOrderPrice());
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;
                    case "buyOrderPrice":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = o1.getBuyOrderPrice().compareTo(o2.getBuyOrderPrice());
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;
                    case "now_price":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = new BigDecimal(o1.getNow_price()).compareTo(new BigDecimal(o2.getNow_price()));
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;

                    case "profitAndLose":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = o1.getProfitAndLose().compareTo(o2.getProfitAndLose());
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;
                    case "allProfitAndLose":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = o1.getAllProfitAndLose().compareTo(o2.getAllProfitAndLose());
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;
                }
            }
            return adminPositionVOS;
        } else {
            List<UserPositionItem> userPositions = this.userPositionItemMapper.listByAgent(positionType, state, userId, agentId, positionSn, begin_time, end_time, buyType, sort, sortColmn);
            List<AdminPositionVO> adminPositionVOS = Lists.newArrayList();
            BigDecimal profitAndLose = BigDecimal.ZERO;
            Integer orderNumber = 0;
            BigDecimal buyTotalAmount = BigDecimal.ZERO;
            BigDecimal allProfitAndLose = BigDecimal.ZERO;
            for (UserPositionItem position : userPositions) {
                AdminPositionVO adminPositionVO = assembleAdminPositionItemVO(position);
                adminPositionVOS.add(adminPositionVO);
                if (adminPositionVO.getProfitAndLose() != null) {
                    profitAndLose = profitAndLose.add(adminPositionVO.getProfitAndLose().subtract(adminPositionVO.getOrderFee()));
                }
                if (adminPositionVO.getAllProfitAndLose() != null) {
                    allProfitAndLose = allProfitAndLose.add(adminPositionVO.getAllProfitAndLose());
                }
                if (adminPositionVO.getBuyTotalAmount() != null) {
                    buyTotalAmount = buyTotalAmount.add(adminPositionVO.getBuyTotalAmount());
                }
                if (adminPositionVO.getOrderNum() != null) {
                    orderNumber = orderNumber + adminPositionVO.getOrderNum();
                }
            }

            if (sortColmn != null) {
                switch (sortColmn) {
                    case "sellOrderPrice":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = o1.getSellOrderPrice().compareTo(o2.getSellOrderPrice());
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;
                    case "buyOrderPrice":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = o1.getBuyOrderPrice().compareTo(o2.getBuyOrderPrice());
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;
                    case "now_price":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = new BigDecimal(o1.getNow_price()).compareTo(new BigDecimal(o2.getNow_price()));
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;

                    case "profitAndLose":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = o1.getProfitAndLose().compareTo(o2.getProfitAndLose());
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;
                    case "allProfitAndLose":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = o1.getAllProfitAndLose().compareTo(o2.getAllProfitAndLose());
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;
                }
            }

            return adminPositionVOS;
        }


    }

    private List<AdminPositionVO> assembleAdminPositionVOList(List<UserPosition> userPositions) {


        List<AdminPositionVO> adminPositionVOList = new ArrayList<>();

        List<String> codeList = new ArrayList<>();

        for (UserPosition position : userPositions) {
            codeList.add(position.getStockCode());
        }

        Set<String> set = new HashSet<String>();
        set.addAll(codeList);     // 将list所有元素添加到set中    set集合特性会自动去重复
        codeList.clear();
        codeList.addAll(set);


        log.error("导出持仓数据-后台");
        List<StockListVO> stockListVOList = SinaStockApi.assembleStockListVO(
                SinaStockApi.getSinaStock(codeList), codeList);
        for (UserPosition position : userPositions) {


            AdminPositionVO adminPositionVO = new AdminPositionVO();

            adminPositionVO.setId(position.getId());
            adminPositionVO.setPositionSn(position.getPositionSn());
            adminPositionVO.setPositionType(position.getPositionType());
            adminPositionVO.setUserId(position.getUserId());
            adminPositionVO.setNickName(position.getNickName());
            adminPositionVO.setAgentId(position.getAgentId());
            adminPositionVO.setStockName(position.getStockName());
            adminPositionVO.setStockCode(position.getStockCode());
            adminPositionVO.setStockGid(position.getStockGid());
            adminPositionVO.setStockSpell(position.getStockSpell());
            adminPositionVO.setBuyOrderId(position.getBuyOrderId());
            adminPositionVO.setBuyOrderTime(position.getBuyOrderTime());
            adminPositionVO.setBuyOrderPrice(position.getBuyOrderPrice());
            adminPositionVO.setSellOrderId(position.getSellOrderId());
            adminPositionVO.setSellOrderTime(position.getSellOrderTime());
            adminPositionVO.setSellOrderPrice(position.getSellOrderPrice());
            adminPositionVO.setOrderDirection("買漲".equals(position.getOrderDirection())?1:2);
            adminPositionVO.setOrderNum(position.getOrderNum());
            adminPositionVO.setOrderLever(position.getOrderLever());
            adminPositionVO.setOrderTotalPrice(position.getOrderTotalPrice());
            adminPositionVO.setOrderFee(position.getOrderFee());
            adminPositionVO.setOrderSpread(position.getOrderSpread());
            adminPositionVO.setOrderStayFee(position.getOrderStayFee());
            adminPositionVO.setOrderStayDays(position.getOrderStayDays());
            adminPositionVO.setIsLock(position.getIsLock());
            adminPositionVO.setLockMsg(position.getLockMsg());
            adminPositionVO.setStockPlate(position.getStockPlate());

            if (position.getOrderNum() == null || position.getOrderNum() == 0) {
                adminPositionVO.setProfitAndLose(position.getProfitAndLose());
                adminPositionVO.setAllProfitAndLose(position.getAllProfitAndLose());
                adminPositionVO.setNow_price(position.getSellOrderPrice().toString());
            } else {

                for (StockListVO stockListVO : stockListVOList) {
                    if (position.getStockCode().equals(stockListVO.getCode())) {
                        PositionProfitVO positionProfitVO = getPositionProfitVOByStockVo(position, stockListVO);
                        adminPositionVO.setProfitAndLose(positionProfitVO.getProfitAndLose());
                        adminPositionVO.setAllProfitAndLose(positionProfitVO.getAllProfitAndLose());
                        adminPositionVO.setNow_price(positionProfitVO.getNowPrice());
                        adminPositionVO.setSellTotalAmount(positionProfitVO.getSellTotalAmount());
                        adminPositionVO.setBuyTotalAmount(positionProfitVO.getBuyTotalAmount());
                        adminPositionVO.setOrderFee(positionProfitVO.getBuyFee());
                    }
                }
            }
            //付出成本
            adminPositionVO.setOrderTotalPrice(position.getBuyOrderPrice().multiply(new BigDecimal(position.getOrderNum())).divide(position.getOrderLever(), 2, RoundingMode.HALF_UP));
            adminPositionVOList.add(adminPositionVO);


        }

        return adminPositionVOList;
    }

    @Override
    public ServerResponse newStockBuy(Stock stock, Integer buyNum, String phone, BigDecimal now_price, HttpServletRequest request) throws Exception {

        BigDecimal lever = BigDecimal.ONE;
        User user = this.iUserService.getUserByPhone(phone);
        if (user == null) {
            return ServerResponse.createBySuccessMsg("用户不存在");
        }

        SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
        if (siteSetting == null) {
            log.error("下單出錯，網站設置表不存在");
            return ServerResponse.createByErrorMsg("下單失敗，系統設置錯誤");
        }

        System.out.println(stock.getStockGid());
        BigDecimal buy_amt = now_price.multiply(new BigDecimal(buyNum));

        UserPosition userPosition = userPositionMapper.selectOne(new QueryWrapper<UserPosition>().eq("user_id", user.getId()).eq("stock_code", stock.getStockCode()).eq("buy_type", 0));
        if (userPosition == null) {
            userPosition = new UserPosition();
            userPosition.setPositionType(user.getAccountType());
            userPosition.setUserId(user.getId());
            userPosition.setNickName(user.getRealName());
            userPosition.setAgentId(user.getAgentId());
            userPosition.setStockCode(stock.getStockCode());
            userPosition.setStockName(stock.getStockName());
            userPosition.setStockGid(stock.getStockGid());
            userPosition.setStockSpell(stock.getStockSpell());
            userPosition.setBuyType(0);
            if (stock.getStockPlate() != null) {
                userPosition.setStockPlate(stock.getStockPlate());
            }
            userPosition.setIsLock(Integer.valueOf(0));
            userPosition.setOrderLever(lever);
            userPosition.setNewStock(1);
            userPosition.setOrderDirection("買漲");
            userPosition.setOrderStayDays(0);
            userPosition.setOrderStayFee(new BigDecimal("0"));

            userPosition.setBuyOrderPrice(BigDecimal.ZERO);
            userPosition.setOrderNum(0);
            userPosition.setOrderTotalPrice(BigDecimal.ZERO);
            this.userPositionMapper.insert(userPosition);
        }

        if (userPosition.getOrderNum() == null || userPosition.getOrderNum() == 0) {
            userPosition.setBuyOrderTime(new Date());
            userPosition.setMarginAdd(BigDecimal.ZERO);
            userPosition.setPositionSn(KeyUtils.getUniqueKey());
        }
        userPosition.setOrderTotalPrice(userPosition.getOrderTotalPrice().add(buy_amt));
        userPosition.setOrderNum(userPosition.getOrderNum() + buyNum);
        userPosition.setBuyOrderPrice(userPosition.getOrderTotalPrice().divide(new BigDecimal(userPosition.getOrderNum()), 2, RoundingMode.DOWN));
        BigDecimal buy_fee_amt = buy_amt.multiply(siteSetting.getBuyFee()).setScale(2, 4);
        log.info("用戶購買手續費（配資后總資金 * 百分比） = {}", buy_fee_amt);
//        log.info("用戶購買印花稅（配資后總資金 * 百分比） = {}", buy_yhs_amt);
//        userPosition.setOrderSpread(buy_yhs_amt);
        BigDecimal profit_and_lose = new BigDecimal("0");
        userPosition.setProfitAndLose(profit_and_lose);


        BigDecimal all_profit_and_lose = profit_and_lose.subtract(buy_fee_amt);
        userPosition.setAllProfitAndLose(all_profit_and_lose);


        int insertPositionCount = 0;
        this.userPositionMapper.updateByPrimaryKey(userPosition);
        UserPositionItem userPositionItem = new UserPositionItem();
        userPositionItem.setPositionType(userPosition.getPositionType());
        userPositionItem.setPositionSn(userPosition.getPositionSn());
        userPositionItem.setUserId(userPosition.getUserId());
        userPositionItem.setNickName(userPosition.getNickName());
        userPositionItem.setAgentId(userPosition.getAgentId());
        userPositionItem.setStockName("新股：" + userPosition.getStockName());
        userPositionItem.setStockCode(userPosition.getStockCode());
        userPositionItem.setStockGid(userPosition.getStockGid());
        userPositionItem.setStockSpell(userPosition.getStockSpell());
        userPositionItem.setBuyType(userPosition.getBuyType());
        userPositionItem.setStockPlate(userPosition.getStockPlate());
        userPositionItem.setOrderTime(new Date());
        userPositionItem.setBuyOrderPrice(now_price);
        userPositionItem.setSellOrderPrice(new BigDecimal("0"));
        userPositionItem.setOrderDirection(userPosition.getOrderDirection());
        userPositionItem.setOrderNum(buyNum);
        userPositionItem.setOrderLever(lever);
        userPositionItem.setOrderTotalPrice(buy_amt);
        userPositionItem.setOrderFee(buy_fee_amt);
        userPositionItem.setOrderType(0);
        userPositionItemMapper.insert(userPositionItem);

        insertPositionCount = userPosition.getId();
        if (insertPositionCount > 0) {
            //修改用戶可用餘額= 當前餘額-下單總金額
            BigDecimal reckon_enable = user.getEnableAmt().subtract(buy_fee_amt);
            BigDecimal reckon_all = user.getUserAmt().subtract(buy_fee_amt);

            user.setUserAmt(reckon_all);

            user.setEnableAmt(reckon_enable);
            int updateUserCount = this.userMapper.updateByPrimaryKeySelective(user);

            UserCashDetail ucd = new UserCashDetail();
            ucd.setPositionId(userPosition.getId());
            ucd.setAgentId(user.getAgentId());
            ucd.setAgentName(user.getAgentName());
            ucd.setUserId(user.getId());
            ucd.setUserName(user.getRealName());
            ucd.setDeType("新股持仓手续费");
            ucd.setDeAmt(BigDecimal.ZERO.subtract(buy_fee_amt));
            ucd.setAddTime(new Date());
            ucd.setIsRead(Integer.valueOf(0));
            int insertSxfCount = this.userCashDetailMapper.insert(ucd);
            if (updateUserCount > 0) {
                log.info("【用戶交易下單】修改用戶金額成功");
            } else {
                log.error("用戶交易下單】修改用戶金額出錯");
                throw new Exception("用戶交易下單】修改用戶金額出錯");
            }
            //核算代理收入-入倉手續費
            iAgentAgencyFeeService.AgencyFeeIncome(1, userPositionItem);
            log.info("【用戶交易下單】保存持倉記錄成功");
        } else {
            log.error("用戶交易下單】保存持倉記錄出錯");
            throw new Exception("用戶交易下單】保存持倉記錄出錯");
        }

        return ServerResponse.createBySuccess("下單成功");

    }

    @Override
    public List<UserPosition> findPositionByUserIdAndSellIdIsNullWhereRz(Integer userId) {
        return this.userPositionMapper.findPositionByUserIdAndSellIdIsNullWhereRz(userId);

    }

    @Override
    public List<UserPosition> findPositionByUserIdAndSellIdIsNullWhereRzAndAll() {
        return this.userPositionMapper.findPositionByUserIdAndSellIdIsNullWhereRzAndAll();

    }


    /*股票入倉最新top列表*/
    @Override
    public ServerResponse findPositionTopList(Integer pageSize) {
        List<UserPosition> userPositions = this.userPositionMapper.findPositionTopList(pageSize);

        List<UserPositionVO> userPositionVOS = Lists.newArrayList();
        if (userPositions.size() > 0) {
            for (UserPosition position : userPositions) {

                UserPositionVO userPositionVO = assembleUserPositionVO(position);
                userPositionVOS.add(userPositionVO);
            }
        }

        PageInfo pageInfo = new PageInfo(userPositions);
        pageInfo.setList(userPositionVOS);

        return ServerResponse.createBySuccess(pageInfo);
    }

    /*根據股票代碼查詢用戶最早入倉股票*/
    @Override
    public ServerResponse findUserPositionByCode(HttpServletRequest request, String stockCode) {
        User user = this.iUserService.getCurrentRefreshUser(request);
        if (user == null) {
            return ServerResponse.createBySuccessMsg("請先登錄");
        }
        UserPosition position = this.userPositionMapper.findUserPositionByCode(user.getId(), stockCode);

        List<UserPositionVO> userPositionVOS = Lists.newArrayList();
        UserPositionVO userPositionVO = null;
        if (position != null) {
            userPositionVO = assembleUserPositionVO(position);
        }
        userPositionVOS.add(userPositionVO);

        PageInfo pageInfo = new PageInfo();
        pageInfo.setList(userPositionVOS);

        return ServerResponse.createBySuccess(pageInfo);
    }


    private BigDecimal getNowPrice(String code) {
        BigDecimal price = null;
        try {
            String s = HttpUtil.get("https://tw.quote.finance.yahoo.net/quote/q?type=ta&perd=1m&mkt=10&sym=" + code + "&v=1");
            s = s.replaceAll("null\\(", "");
            s = s.replaceAll("\\);", "");
            JSONObject jsonObject = JSONObject.parseObject(s);
            JSONArray h = jsonObject.getJSONArray("ta");
            JSONObject jsonObject1 = h.getJSONObject(0);
            price = jsonObject1.getBigDecimal("h");
        } catch (Exception e) {
        }
        if (price == null) {
            try {
                String json = HttpUtil.get("https://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=tse_" + code + ".tw");
                JSONObject jsonObject = JSONObject.parseObject(json);
                JSONArray jsonArray = jsonObject.getJSONArray("msgArray");
                JSONObject nowData = jsonArray.getJSONObject(0);
                String nowPrice = nowData.getString("z");
                if (!"-".equals(nowPrice)) {
                    price = new BigDecimal(nowPrice);
                }
            } catch (Exception e) {
                e.printStackTrace();
                price = null;
            }
        }
        if (price == null) {
            try {
                String s = HttpUtil.get("http://47.243.121.145:8000/get_price/?code=" + code);
                price = new BigDecimal(s);
            } catch (Exception e) {
            }
        }

        return price;
    }

}

