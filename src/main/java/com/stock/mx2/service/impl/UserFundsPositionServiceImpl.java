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
 * εεδΊ€ζ
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
            return ServerResponse.createByErrorMsg("δΈε?η°εΈΈοΌθ«η¨εΎεθ©¦οΌ");
        }
        ret = userFundsPositionMapper.insert(model);
        if(ret>0){
            return ServerResponse.createBySuccessMsg("δΈε?ζεοΌ");
        } else {
            return ServerResponse.createByErrorMsg("δΈε?ε€±ζοΌθ«η¨εΎεθ©¦οΌ");
        }
    }

    @Override
    public int update(UserFundsPosition model) {
        int ret = userFundsPositionMapper.update(model);
        return ret>0 ? ret: 0;
    }

    /**
     * εεδΊ€ζ-δΏε­
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
            return ServerResponse.createBySuccessMsg("ζδ½ζε");
        }
        return ServerResponse.createByErrorMsg("ζδ½ε€±ζ");
    }

    /*εεδΊ€ζ-ζ₯θ©’εθ‘¨*/
    @Override
    public ServerResponse<PageInfo> getList(int pageNum, int pageSize, String keyword, HttpServletRequest request){
        PageHelper.startPage(pageNum, pageSize);
        List<UserFundsPosition> listData = this.userFundsPositionMapper.pageList(pageNum, pageSize, keyword);
        PageInfo pageInfo = new PageInfo(listData);
        pageInfo.setList(listData);
        return ServerResponse.createBySuccess(pageInfo);
    }

    /*εεδΊ€ζ-ζ₯θ©’θ©³ζ*/
    @Override
    public ServerResponse getDetail(int id) {
        return ServerResponse.createBySuccess(this.userFundsPositionMapper.load(id));
    }


    /*
    * εεδΊ€ζ-η¨ζΆεΉ³εζδ½
    * */
    @Transactional
    public ServerResponse sellFunds(String positionSn, int doType) throws Exception {
        log.info("γη¨ζΆδΊ€ζεΉ³εγ positionSn = {} οΌ dotype = {}", positionSn, Integer.valueOf(doType));

        SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
        if (siteSetting == null) {
            log.error("εΉ³εεΊι―οΌηΆ²η«θ¨­η½?θ‘¨δΈε­ε¨");
            return ServerResponse.createByErrorMsg("δΈε?ε€±ζοΌη³»η΅±θ¨­η½?ι―θͺ€");
        }

        if (doType != 0) {
            String am_begin = siteSetting.getTransAmBegin();
            String am_end = siteSetting.getTransAmEnd();
            String pm_begin = siteSetting.getTransPmBegin();
            String pm_end = siteSetting.getTransPmEnd();
            boolean am_flag = BuyAndSellUtils.isTransTime(am_begin, am_end);
            boolean pm_flag = BuyAndSellUtils.isTransTime(pm_begin, pm_end);
            log.info("ζ―ε¦ε¨δΈεδΊ€ζζι = {} ζ―ε¦ε¨δΈεδΊ€ζζι = {}", Boolean.valueOf(am_flag), Boolean.valueOf(pm_flag));
            if (!am_flag && !pm_flag) {
//                return ServerResponse.createByErrorMsg("εΉ³εε€±ζοΌδΈε¨δΊ€ζζζ?΅ε§");
            }
        }

        UserFundsPosition userPosition = this.userFundsPositionMapper.findPositionBySn(positionSn);
        if (userPosition == null) {
            return ServerResponse.createByErrorMsg("εΉ³εε€±ζοΌθ¨ε?δΈε­ε¨");
        }

        User user = this.userMapper.selectByPrimaryKey(userPosition.getUserId());
        /*ε―¦εθͺθ­ιιιε*/
        SiteProduct siteProduct = iSiteProductService.getProductSetting();
        if (siteProduct.getRealNameDisplay() && user.getIsLock().intValue() == 1) {
            return ServerResponse.createByErrorMsg("εΉ³εε€±ζοΌη¨ζΆε·²θ’«ιε?");
        }
        if(siteProduct.getHolidayDisplay()){
            return ServerResponse.createByErrorMsg("ε¨ζ«ζη―εζ₯δΈθ½δΊ€ζοΌ");
        }

        if (userPosition.getSellOrderId() != null) {
            return ServerResponse.createByErrorMsg("εΉ³εε€±ζοΌζ­€θ¨ε?ε·²εΉ³ε");
        }

        if (1 == userPosition.getIsLock().intValue()) {
            return ServerResponse.createByErrorMsg("εΉ³εε€±ζ " + userPosition.getLockMsg());
        }

        if (!DateTimeUtil.isCanSell(userPosition.getBuyOrderTime(), siteSetting.getCantSellTimes().intValue())) {
            return ServerResponse.createByErrorMsg(siteSetting.getCantSellTimes() + "ειε§δΈθ½εΉ³ε");
        }
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(userPosition.getStockCode());

        List<StockListVO > stockListVOs = SinaStockApi.assembleStockListVO(SinaStockApi.getSinaStock(arrayList),arrayList);
        StockListVO stockListVO = stockListVOs.get(0);

        BigDecimal now_price = new BigDecimal(stockListVO.getNowPrice());
        if (now_price.compareTo(new BigDecimal("0")) != 1) {
            log.error("θ‘η₯¨ = {} ζΆε°ε ±εΉ = {}", userPosition.getStockName(), now_price);
            return ServerResponse.createByErrorMsg("ε ±εΉ0οΌεΉ³εε€±ζοΌθ«η¨εΎεθ©¦");
        }

        double stock_crease = stockListVO.getHcrate().doubleValue();

        BigDecimal zsPrice = new BigDecimal(stockListVO.getPreclose_px());

        BigDecimal ztPrice = zsPrice.multiply(new BigDecimal("0.1")).add(zsPrice);
        ztPrice = ztPrice.setScale(2, 4);
        BigDecimal chaPrice = ztPrice.subtract(zsPrice);

        BigDecimal ztRate = chaPrice.multiply(new BigDecimal("100")).divide(zsPrice, 2, 4);

        ztRate = ztRate.negate();
        log.info("θ‘η₯¨ηΆεζΌ²θ·εΉ = {} θ·εεΉεΊ¦ = {}", Double.valueOf(stock_crease), ztRate);
        if ((new BigDecimal(String.valueOf(stock_crease))).compareTo(ztRate) == 0 && "θ²·ζΌ²"
                .equals(userPosition.getOrderDirection())) {
            return ServerResponse.createByErrorMsg("ηΆεθ‘η₯¨ε·²θ·εδΈθ½θ³£εΊ");
        }

        Integer buy_num = userPosition.getOrderNum();

        BigDecimal all_buy_amt = userPosition.getOrderTotalPrice();
        BigDecimal all_sell_amt = now_price.multiply(new BigDecimal(buy_num.intValue()));

        BigDecimal profitLoss = new BigDecimal("0");
        if (1 == userPosition.getOrderDirection()) {
            log.info("θ²·θ³£ζΉεοΌ{}", "ζΌ²");

            profitLoss = all_sell_amt.subtract(all_buy_amt);
        } else {
            log.info("θ²·θ³£ζΉεοΌ{}", "θ·");
            profitLoss = all_buy_amt.subtract(all_sell_amt);
        }
        log.info("θ²·ε₯ηΈ½ιι‘ = {} , θ³£εΊηΈ½ιι‘ = {} , ηθ§ = {}", new Object[]{all_buy_amt, all_sell_amt, profitLoss});

        BigDecimal user_all_amt = user.getUserAmt();
        BigDecimal user_enable_amt = user.getEnableAmt();
        log.info("η¨ζΆεζ¬ηΈ½θ³ι = {} , ε―η¨ = {}", user_all_amt, user_enable_amt);

        BigDecimal buy_fee_amt = userPosition.getOrderFee();
        log.info("θ²·ε₯ζηΊθ²» = {}", buy_fee_amt);

        BigDecimal orderSpread = userPosition.getOrderSpread();
        log.info("ε°θ±η¨ = {}", orderSpread);

        BigDecimal orderStayFee = userPosition.getOrderStayFee();
        log.info("ηεθ²» = {}", orderStayFee);

        BigDecimal spreadRatePrice = userPosition.getSpreadRatePrice();
        log.info("ι»ε·?θ²» = {}", spreadRatePrice);

        BigDecimal sell_fee_amt = all_sell_amt.multiply(siteSetting.getSellFee()).setScale(2, 4);
        log.info("θ³£εΊζηΊθ²» = {}", sell_fee_amt);

        BigDecimal all_fee_amt = buy_fee_amt.add(sell_fee_amt).add(orderSpread).add(orderStayFee).add(spreadRatePrice);
        log.info("ηΈ½ηζηΊθ²»θ²»η¨ = {}", all_fee_amt);

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
            log.info("γη¨ζΆεΉ³εγδΏ?ζΉζ΅?εηθ§θ¨ιζε");
        } else {
            log.error("η¨ζΆεΉ³εγδΏ?ζΉζ΅?εηθ§θ¨ιεΊι―");
            throw new Exception("η¨ζΆεΉ³εγδΏ?ζΉζ΅?εηθ§θ¨ιεΊι―");
        }

        BigDecimal freez_amt = all_buy_amt.divide(new BigDecimal(userPosition.getOrderLever().intValue()), 2, 4);

        BigDecimal reckon_all = user_all_amt.add(all_profit);
        BigDecimal reckon_enable = user_enable_amt.add(all_profit).add(freez_amt);

        log.info("η¨ζΆεΉ³εεηηΈ½θ³ι  = {} , ε―η¨θ³ι = {}", reckon_all, reckon_enable);
        user.setUserAmt(reckon_all);
        user.setEnableAmt(reckon_enable);
        /*int updateUserCount = this.userMapper.updateByPrimaryKeySelective(user);
        if (updateUserCount > 0) {
            log.info("γη¨ζΆεΉ³εγδΏ?ζΉη¨ζΆιι‘ζε");
        } else {
            log.error("η¨ζΆεΉ³εγδΏ?ζΉη¨ζΆιι‘εΊι―");
            throw new Exception("η¨ζΆεΉ³εγδΏ?ζΉη¨ζΆιι‘εΊι―");
        }*/

        UserCashDetail ucd = new UserCashDetail();
        ucd.setPositionId(userPosition.getId());
        ucd.setAgentId(user.getAgentId());
        ucd.setAgentName(user.getAgentName());
        ucd.setUserId(user.getId());
        ucd.setUserName(user.getRealName());
        ucd.setDeType("ιθ³ηΈ½ηθ§");
        ucd.setDeAmt(all_profit);
        ucd.setDeSummary("θ³£εΊθ‘η₯¨οΌ" + userPosition.getStockCode() + "/" + userPosition.getStockName() + ",δ½η¨ζ¬ιοΌ" + freez_amt + ",ηΈ½ζηΊθ²»οΌ" + all_fee_amt + ",ηεθ²»οΌ" + orderStayFee+ ",ε°θ±η¨οΌ" + orderSpread + ",ι»ε·?θ²»οΌ" + spreadRatePrice + ",ηθ§οΌ" + profitLoss + "οΌζ»ηθ§οΌ" + all_profit);

        ucd.setAddTime(new Date());
        ucd.setIsRead(Integer.valueOf(0));

        int insertSxfCount = this.userCashDetailMapper.insert(ucd);
        if (insertSxfCount > 0) {
            /*//ζ Έη?δ»£ηζΆε₯-εΉ³εζηΊθ²»
            iAgentAgencyFeeService.AgencyFeeIncome(2,userPosition.getPositionSn());
            //ζ Έη?δ»£ηζΆε₯-εη΄
            iAgentAgencyFeeService.AgencyFeeIncome(4,userPosition.getPositionSn());*/
            log.info("γη¨ζΆεΉ³εγδΏε­ζη΄°θ¨ιζε");
        } else {
            log.error("η¨ζΆεΉ³εγδΏε­ζη΄°θ¨ιεΊι―");
            throw new Exception("η¨ζΆεΉ³εγδΏε­ζη΄°θ¨ιεΊι―");
        }

        return ServerResponse.createBySuccessMsg("εΉ³εζεοΌ");
    }


    /*
     * εεδΊ€ζ-ζ₯θ©’ζζεΉ³ε/ζεδΏ‘ζ―
     * */
    public ServerResponse findMyPositionByCodeAndSpell(String stockCode, String stockSpell, Integer state, HttpServletRequest request, int pageNum, int pageSize) {
        User user = this.iUserService.getCurrentUser(request);
        if (user == null ){
            return ServerResponse.createBySuccessMsg("θ«εη»ι");
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

    /*ζ Ήζεειθ³δ»£η’Όζ₯θ©’η¨ζΆζζ©ε₯εθ‘η₯¨*/
    public ServerResponse findUserFundsPositionByCode(HttpServletRequest request, String fundsCode) {
        User user = this.iUserService.getCurrentRefreshUser(request);
        if (user == null){
            return ServerResponse.createBySuccessMsg("θ«εη»ι");
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
            if ("θ²·θ·".equals(position.getOrderDirection())) {
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
            if ("θ²·θ·".equals(position.getOrderDirection())) {
                profitAndLose = profitAndLose.negate();
            }

            //ηΈ½ηθ§= ζ΅?εηθ§ β ζηΊθ²» β ε°θ±η¨ β ηεθ²» β ι»ε·?θ²»
            allProfitAndLose = profitAndLose.subtract(position.getOrderFee()).subtract(position.getOrderSpread()).subtract(position.getOrderStayFee()).subtract(position.getSpreadRatePrice());
        }
        PositionProfitVO positionProfitVO = new PositionProfitVO();
        positionProfitVO.setProfitAndLose(profitAndLose);
        positionProfitVO.setAllProfitAndLose(allProfitAndLose);
        positionProfitVO.setNowPrice(nowPrice);

        return positionProfitVO;
    }


}
