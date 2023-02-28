package com.stock.mx2.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.dao.UserCashDetailMapper;
import com.stock.mx2.dao.UserFundsPositionMapper;
import com.stock.mx2.dao.UserMapper;
import com.stock.mx2.pojo.*;
import com.stock.mx2.service.*;
import com.stock.mx2.utils.DateTimeUtil;
import com.stock.mx2.utils.KeyUtils;
import com.stock.mx2.utils.stock.BuyAndSellUtils;
import com.stock.mx2.utils.stock.GeneratePosition;
import com.stock.mx2.utils.stock.sina.SinaStockApi;
import com.stock.mx2.vo.position.PositionProfitVO;
import com.stock.mx2.vo.position.UserPositionVO;
import com.stock.mx2.vo.stock.StockListVO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 分倉交易
 * @author lr
 * @date 2020/07/24
 */
@Service("IUserFundsPositionService")
public class UserFundsPositionServiceImpl implements IUserFundsPositionService {
    private static final Logger log = LoggerFactory.getLogger(UserPositionServiceImpl.class);

    @Resource
    private UserFundsPositionMapper userFundsPositionMapper;
    @Autowired
    UserMapper userMapper;
    @Autowired
    UserCashDetailMapper userCashDetailMapper;

    @Autowired
    ISiteProductService iSiteProductService;
    @Autowired
    IUserService iUserService;
    @Autowired
    ISiteSettingService iSiteSettingService;
    @Autowired
    IStockService iStockService;
    @Autowired
    ISiteSpreadService iSiteSpreadService;
    @Autowired
    IAgentAgencyFeeService iAgentAgencyFeeService;


    @Override
    public ServerResponse insert(UserFundsPosition model, HttpServletRequest request) {
        int ret = 0;
        if (model == null) {
            return ServerResponse.createByErrorMsg("下單異常，請稍後再試！");
        }
        ret = userFundsPositionMapper.insert(model);
        if(ret>0){
            return ServerResponse.createBySuccessMsg("下單成功！");
        } else {
            return ServerResponse.createByErrorMsg("下單失敗，請稍後再試！");
        }
    }

    @Override
    public int update(UserFundsPosition model) {
        int ret = userFundsPositionMapper.update(model);
        return ret>0 ? ret: 0;
    }

    /**
     * 分倉交易-保存
     */
    @Override
    public ServerResponse save(UserFundsPosition model) {
        int ret = 0;
        if(model!=null && model.getId()>0){
            ret = userFundsPositionMapper.update(model);
        } else{
            ret = userFundsPositionMapper.insert(model);
        }
        if(ret>0){
            return ServerResponse.createBySuccessMsg("操作成功");
        }
        return ServerResponse.createByErrorMsg("操作失敗");
    }

    /*分倉交易-查詢列表*/
    @Override
    public ServerResponse<PageInfo> getList(int pageNum, int pageSize, String keyword, HttpServletRequest request){
        PageHelper.startPage(pageNum, pageSize);
        List<UserFundsPosition> listData = this.userFundsPositionMapper.pageList(pageNum, pageSize, keyword);
        PageInfo pageInfo = new PageInfo(listData);
        pageInfo.setList(listData);
        return ServerResponse.createBySuccess(pageInfo);
    }

    /*分倉交易-查詢詳情*/
    @Override
    public ServerResponse getDetail(int id) {
        return ServerResponse.createBySuccess(this.userFundsPositionMapper.load(id));
    }


    /*
    * 分倉交易-用戶平倉操作
    * */
    @Transactional
    public ServerResponse sellFunds(String positionSn, int doType) throws Exception {
        log.info("【用戶交易平倉】 positionSn = {} ， dotype = {}", positionSn, Integer.valueOf(doType));

        SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
        if (siteSetting == null) {
            log.error("平倉出錯，網站設置表不存在");
            return ServerResponse.createByErrorMsg("下單失敗，系統設置錯誤");
        }

        if (doType != 0) {
            String am_begin = siteSetting.getTransAmBegin();
            String am_end = siteSetting.getTransAmEnd();
            String pm_begin = siteSetting.getTransPmBegin();
            String pm_end = siteSetting.getTransPmEnd();
            boolean am_flag = BuyAndSellUtils.isTransTime(am_begin, am_end);
            boolean pm_flag = BuyAndSellUtils.isTransTime(pm_begin, pm_end);
            log.info("是否在上午交易時間 = {} 是否在下午交易時間 = {}", Boolean.valueOf(am_flag), Boolean.valueOf(pm_flag));
            if (!am_flag && !pm_flag) {
//                return ServerResponse.createByErrorMsg("平倉失敗，不在交易時段內");
            }
        }

        UserFundsPosition userPosition = this.userFundsPositionMapper.findPositionBySn(positionSn);
        if (userPosition == null) {
            return ServerResponse.createByErrorMsg("平倉失敗，訂單不存在");
        }

        User user = this.userMapper.selectByPrimaryKey(userPosition.getUserId());
        /*實名認證開關開啟*/
        SiteProduct siteProduct = iSiteProductService.getProductSetting();
        if (siteProduct.getRealNameDisplay() && user.getIsLock().intValue() == 1) {
            return ServerResponse.createByErrorMsg("平倉失敗，用戶已被鎖定");
        }
        if(siteProduct.getHolidayDisplay()){
            return ServerResponse.createByErrorMsg("周末或節假日不能交易！");
        }

        if (userPosition.getSellOrderId() != null) {
            return ServerResponse.createByErrorMsg("平倉失敗，此訂單已平倉");
        }

        if (1 == userPosition.getIsLock().intValue()) {
            return ServerResponse.createByErrorMsg("平倉失敗 " + userPosition.getLockMsg());
        }

        if (!DateTimeUtil.isCanSell(userPosition.getBuyOrderTime(), siteSetting.getCantSellTimes().intValue())) {
            return ServerResponse.createByErrorMsg(siteSetting.getCantSellTimes() + "分鐘內不能平倉");
        }
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(userPosition.getStockCode());

        List<StockListVO > stockListVOs = SinaStockApi.assembleStockListVO(SinaStockApi.getSinaStock(arrayList),arrayList);
        StockListVO stockListVO = stockListVOs.get(0);

        BigDecimal now_price = new BigDecimal(stockListVO.getNowPrice());
        if (now_price.compareTo(new BigDecimal("0")) != 1) {
            log.error("股票 = {} 收到報價 = {}", userPosition.getStockName(), now_price);
            return ServerResponse.createByErrorMsg("報價0，平倉失敗，請稍後再試");
        }

        double stock_crease = stockListVO.getHcrate().doubleValue();

        BigDecimal zsPrice = new BigDecimal(stockListVO.getPreclose_px());

        BigDecimal ztPrice = zsPrice.multiply(new BigDecimal("0.1")).add(zsPrice);
        ztPrice = ztPrice.setScale(2, 4);
        BigDecimal chaPrice = ztPrice.subtract(zsPrice);

        BigDecimal ztRate = chaPrice.multiply(new BigDecimal("100")).divide(zsPrice, 2, 4);

        ztRate = ztRate.negate();
        log.info("股票當前漲跌幅 = {} 跌停幅度 = {}", Double.valueOf(stock_crease), ztRate);
        if ((new BigDecimal(String.valueOf(stock_crease))).compareTo(ztRate) == 0 && "買漲"
                .equals(userPosition.getOrderDirection())) {
            return ServerResponse.createByErrorMsg("當前股票已跌停不能賣出");
        }

        Integer buy_num = userPosition.getOrderNum();

        BigDecimal all_buy_amt = userPosition.getOrderTotalPrice();
        BigDecimal all_sell_amt = now_price.multiply(new BigDecimal(buy_num.intValue()));

        BigDecimal profitLoss = new BigDecimal("0");
        if (1 == userPosition.getOrderDirection()) {
            log.info("買賣方向：{}", "漲");

            profitLoss = all_sell_amt.subtract(all_buy_amt);
        } else {
            log.info("買賣方向：{}", "跌");
            profitLoss = all_buy_amt.subtract(all_sell_amt);
        }
        log.info("買入總金額 = {} , 賣出總金額 = {} , 盈虧 = {}", new Object[]{all_buy_amt, all_sell_amt, profitLoss});

        BigDecimal user_all_amt = user.getUserAmt();
        BigDecimal user_enable_amt = user.getEnableAmt();
        log.info("用戶原本總資金 = {} , 可用 = {}", user_all_amt, user_enable_amt);

        BigDecimal buy_fee_amt = userPosition.getOrderFee();
        log.info("買入手續費 = {}", buy_fee_amt);

        BigDecimal orderSpread = userPosition.getOrderSpread();
        log.info("印花稅 = {}", orderSpread);

        BigDecimal orderStayFee = userPosition.getOrderStayFee();
        log.info("留倉費 = {}", orderStayFee);

        BigDecimal spreadRatePrice = userPosition.getSpreadRatePrice();
        log.info("點差費 = {}", spreadRatePrice);

        BigDecimal sell_fee_amt = all_sell_amt.multiply(siteSetting.getSellFee()).setScale(2, 4);
        log.info("賣出手續費 = {}", sell_fee_amt);

        BigDecimal all_fee_amt = buy_fee_amt.add(sell_fee_amt).add(orderSpread).add(orderStayFee).add(spreadRatePrice);
        log.info("總的手續費費用 = {}", all_fee_amt);

        userPosition.setSellOrderId(GeneratePosition.getPositionId());
        userPosition.setSellOrderPrice(now_price);
        userPosition.setSellOrderTime(new Date());

        BigDecimal order_fee_all = buy_fee_amt.add(sell_fee_amt);
        userPosition.setOrderFee(order_fee_all);

        userPosition.setProfitAndLose(profitLoss);

        BigDecimal all_profit = profitLoss.subtract(all_fee_amt);
        userPosition.setAllProfitAndLose(all_profit);

        int updatePositionCount = this.userFundsPositionMapper.update(userPosition);
        if (updatePositionCount > 0) {
            log.info("【用戶平倉】修改浮動盈虧記錄成功");
        } else {
            log.error("用戶平倉】修改浮動盈虧記錄出錯");
            throw new Exception("用戶平倉】修改浮動盈虧記錄出錯");
        }

        BigDecimal freez_amt = all_buy_amt.divide(new BigDecimal(userPosition.getOrderLever().intValue()), 2, 4);

        BigDecimal reckon_all = user_all_amt.add(all_profit);
        BigDecimal reckon_enable = user_enable_amt.add(all_profit).add(freez_amt);

        log.info("用戶平倉后的總資金  = {} , 可用資金 = {}", reckon_all, reckon_enable);
        user.setUserAmt(reckon_all);
        user.setEnableAmt(reckon_enable);
        /*int updateUserCount = this.userMapper.updateByPrimaryKeySelective(user);
        if (updateUserCount > 0) {
            log.info("【用戶平倉】修改用戶金額成功");
        } else {
            log.error("用戶平倉】修改用戶金額出錯");
            throw new Exception("用戶平倉】修改用戶金額出錯");
        }*/

        UserCashDetail ucd = new UserCashDetail();
        ucd.setPositionId(userPosition.getId());
        ucd.setAgentId(user.getAgentId());
        ucd.setAgentName(user.getAgentName());
        ucd.setUserId(user.getId());
        ucd.setUserName(user.getRealName());
        ucd.setDeType("配資總盈虧");
        ucd.setDeAmt(all_profit);
        ucd.setDeSummary("賣出股票，" + userPosition.getStockCode() + "/" + userPosition.getStockName() + ",佔用本金：" + freez_amt + ",總手續費：" + all_fee_amt + ",留倉費：" + orderStayFee+ ",印花稅：" + orderSpread + ",點差費：" + spreadRatePrice + ",盈虧：" + profitLoss + "，总盈虧：" + all_profit);

        ucd.setAddTime(new Date());
        ucd.setIsRead(Integer.valueOf(0));

        int insertSxfCount = this.userCashDetailMapper.insert(ucd);
        if (insertSxfCount > 0) {
            /*//核算代理收入-平倉手續費
            iAgentAgencyFeeService.AgencyFeeIncome(2,userPosition.getPositionSn());
            //核算代理收入-分紅
            iAgentAgencyFeeService.AgencyFeeIncome(4,userPosition.getPositionSn());*/
            log.info("【用戶平倉】保存明細記錄成功");
        } else {
            log.error("用戶平倉】保存明細記錄出錯");
            throw new Exception("用戶平倉】保存明細記錄出錯");
        }

        return ServerResponse.createBySuccessMsg("平倉成功！");
    }


    /*
     * 分倉交易-查詢所有平倉/持倉信息
     * */
    public ServerResponse findMyPositionByCodeAndSpell(String stockCode, String stockSpell, Integer state, HttpServletRequest request, int pageNum, int pageSize) {
        User user = this.iUserService.getCurrentUser(request);
        if (user == null ){
            return ServerResponse.createBySuccessMsg("請先登錄");
        }
        PageHelper.startPage(pageNum, pageSize);


        List<UserFundsPosition> userPositions = this.userFundsPositionMapper.findMyPositionByCodeAndSpell(user.getId(), stockCode, stockSpell, state);

        List<UserPositionVO> userPositionVOS = Lists.newArrayList();
        if (userPositions.size() > 0) {
            for (UserFundsPosition position : userPositions) {

                UserPositionVO userPositionVO = assembleUserPositionVO(position);
                userPositionVOS.add(userPositionVO);
            }
        }

        PageInfo pageInfo = new PageInfo(userPositions);
        pageInfo.setList(userPositionVOS);

        return ServerResponse.createBySuccess(pageInfo);
    }

    /*根據分倉配資代碼查詢用戶最早入倉股票*/
    public ServerResponse findUserFundsPositionByCode(HttpServletRequest request, String fundsCode) {
        User user = this.iUserService.getCurrentRefreshUser(request);
        if (user == null){
            return ServerResponse.createBySuccessMsg("請先登錄");
        }
        UserFundsPosition position = this.userFundsPositionMapper.findUserFundsPositionByCode(user.getId(), fundsCode);

        List<UserPositionVO> userPositionVOS = Lists.newArrayList();
        UserPositionVO userPositionVO = null;
        if(position != null){
            userPositionVO = assembleUserPositionVO(position);
        }
        userPositionVOS.add(userPositionVO);

        PageInfo pageInfo = new PageInfo();
        pageInfo.setList(userPositionVOS);

        return ServerResponse.createBySuccess(pageInfo);
    }

    private UserPositionVO assembleUserPositionVO(UserFundsPosition position) {
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
        /*userPositionVO.setProfitTargetPrice(position.getProfitTargetPrice());
        userPositionVO.setStopTargetPrice(position.getStopTargetPrice());*/
        userPositionVO.setOrderDirection(position.getOrderDirection());
        userPositionVO.setOrderNum(position.getOrderNum());
        userPositionVO.setOrderLever(position.getOrderLever());
        userPositionVO.setOrderTotalPrice(position.getOrderTotalPrice());
        userPositionVO.setOrderFee(position.getOrderFee());
        userPositionVO.setOrderSpread(position.getOrderSpread());
        userPositionVO.setOrderStayFee(position.getOrderStayFee());
        userPositionVO.setOrderStayDays(position.getOrderStayDays());

        userPositionVO.setStockPlate(position.getStockPlate());
        userPositionVO.setSpreadRatePrice(position.getSpreadRatePrice());

        PositionProfitVO positionProfitVO = getPositionProfitVO(position);
        userPositionVO.setProfitAndLose(positionProfitVO.getProfitAndLose());
        userPositionVO.setAllProfitAndLose(positionProfitVO.getAllProfitAndLose());
        userPositionVO.setNow_price(positionProfitVO.getNowPrice());


        return userPositionVO;
    }

    private PositionProfitVO getPositionProfitVO(UserFundsPosition position) {
        BigDecimal profitAndLose = new BigDecimal("0");
        BigDecimal allProfitAndLose = new BigDecimal("0");
        String nowPrice = "";

        if (position.getSellOrderId() != null) {

            BigDecimal subPrice = position.getSellOrderPrice().subtract(position.getBuyOrderPrice());
            profitAndLose = subPrice.multiply(new BigDecimal(position.getOrderNum().intValue()));
            if ("買跌".equals(position.getOrderDirection())) {
                profitAndLose = profitAndLose.negate();
            }


            allProfitAndLose = profitAndLose.subtract(position.getOrderFee()).subtract(position.getOrderSpread()).subtract(position.getOrderStayFee()).subtract(position.getSpreadRatePrice());
        } else {


            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.add(position.getStockCode());

            List<StockListVO > stockListVOs = SinaStockApi.assembleStockListVO(SinaStockApi.getSinaStock(arrayList),arrayList);
            StockListVO stockListVO = stockListVOs.get(0);
            nowPrice = stockListVO.getNowPrice();


            BigDecimal buyOrderPrice = position.getBuyOrderPrice();
            if (buyOrderPrice == null){
                buyOrderPrice = BigDecimal.ZERO;
            }

            BigDecimal subPrice = (new BigDecimal(nowPrice)).subtract(buyOrderPrice);
            profitAndLose = subPrice.multiply(new BigDecimal(position.getOrderNum().intValue()));
            if ("買跌".equals(position.getOrderDirection())) {
                profitAndLose = profitAndLose.negate();
            }

            //總盈虧= 浮動盈虧 – 手續費 – 印花稅 – 留倉費 – 點差費
            allProfitAndLose = profitAndLose.subtract(position.getOrderFee()).subtract(position.getOrderSpread()).subtract(position.getOrderStayFee()).subtract(position.getSpreadRatePrice());
        }
        PositionProfitVO positionProfitVO = new PositionProfitVO();
        positionProfitVO.setProfitAndLose(profitAndLose);
        positionProfitVO.setAllProfitAndLose(allProfitAndLose);
        positionProfitVO.setNowPrice(nowPrice);

        return positionProfitVO;
    }


}
